package com.listenit.music.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateInfo(
    val isChecking: Boolean = false,
    val isUpdateAvailable: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val latestVersion: String = "",
    val currentVersion: String = "",
    val releaseNotes: String = "",
    val releaseUrl: String = "",
    val downloadUrl: String? = null,
    val isDismissed: Boolean = false,
    val message: String? = null,
    val downloadedApkPath: String? = null,
)

@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences("listenit_updates", Context.MODE_PRIVATE)

    private val _updateInfo = MutableStateFlow(
        UpdateInfo(
            currentVersion = getCurrentVersion(),
        ),
    )
    val updateInfo: StateFlow<UpdateInfo> = _updateInfo.asStateFlow()

    init {
        checkForUpdate(isSilent = true)
    }

    fun getCurrentVersion(): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
    } catch (_: Exception) {
        "1.0.0"
    }

    fun checkForUpdate(isSilent: Boolean = false) {
        scope.launch {
            _updateInfo.update {
                it.copy(
                    isChecking = true,
                    message = if (!isSilent) "Checking for updates..." else it.message,
                )
            }

            var resolvedVersion: String? = null
            var resolvedDownloadUrl: String? = null
            var resolvedReleaseUrl = "https://tuneglass.vercel.app"
            var resolvedNotes = ""

            // 1. First check the official hosted website endpoint: https://tuneglass.vercel.app/version.json
            runCatching {
                val siteRequest = Request.Builder()
                    .url("https://tuneglass.vercel.app/version.json")
                    .header("Accept", "application/json")
                    .header("User-Agent", "TuneGlass-Android")
                    .build()
                val siteResp = withContext(Dispatchers.IO) { okHttpClient.newCall(siteRequest).execute() }
                if (siteResp.isSuccessful) {
                    val body = siteResp.body?.string().orEmpty()
                    if (body.isNotBlank() && body.trim().startsWith("{")) {
                        val siteJson = JSONObject(body)
                        val ver = siteJson.optString("version", siteJson.optString("versionName", ""))
                        if (ver.isNotBlank()) {
                            resolvedVersion = ver
                            resolvedDownloadUrl = siteJson.optString("downloadUrl", "https://tuneglass.vercel.app/tuneglass.apk")
                            resolvedNotes = siteJson.optString("releaseNotes", siteJson.optString("notes", ""))
                            resolvedReleaseUrl = siteJson.optString("releaseUrl", "https://tuneglass.vercel.app")
                        }
                    }
                }
            }

            // 2. Fall back / augment with GitHub releases
            if (resolvedVersion == null) {
                runCatching {
                    val request = Request.Builder()
                        .url("https://api.github.com/repos/Kmrr-Gautam/TuneGlassApk/releases/latest")
                        .header("Accept", "application/vnd.github.v3+json")
                        .header("User-Agent", "TuneGlass-Android")
                        .build()

                    val response = withContext(Dispatchers.IO) {
                        okHttpClient.newCall(request).execute()
                    }

                    if (response.isSuccessful) {
                        val body = response.body?.string().orEmpty()
                        val json = JSONObject(body)
                        val tagName = json.optString("tag_name", "")
                        resolvedReleaseUrl = json.optString("html_url", "https://github.com/Kmrr-Gautam/TuneGlassApk/releases")
                        resolvedNotes = json.optString("body", "")

                        val assets = json.optJSONArray("assets")
                        if (assets != null) {
                            for (i in 0 until assets.length()) {
                                val asset = assets.optJSONObject(i)
                                val name = asset?.optString("name", "").orEmpty()
                                if (name.endsWith(".apk", ignoreCase = true)) {
                                    resolvedDownloadUrl = asset?.optString("browser_download_url")
                                    break
                                }
                            }
                        }
                        resolvedVersion = tagName
                    }
                }
            }

            val currentVersion = getCurrentVersion()
            val cleanTag = (resolvedVersion ?: "").removePrefix("v").removePrefix("V")
            val hasNewer = if (cleanTag.isNotBlank()) isNewerVersion(cleanTag, currentVersion) else false
            val dismissedVersion = prefs.getString("dismissed_version", null)
            val isDismissed = dismissedVersion == cleanTag

            _updateInfo.update {
                it.copy(
                    isChecking = false,
                    isUpdateAvailable = hasNewer,
                    latestVersion = cleanTag.ifBlank { currentVersion },
                    currentVersion = currentVersion,
                    releaseNotes = resolvedNotes,
                    releaseUrl = resolvedReleaseUrl,
                    downloadUrl = resolvedDownloadUrl ?: "https://tuneglass.vercel.app/tuneglass.apk",
                    isDismissed = isDismissed,
                    message = if (!isSilent) {
                        if (hasNewer) "New version $cleanTag available!" else "You're on the latest version ($currentVersion)"
                    } else null,
                )
            }
        }
    }

    fun dismissUpdate(version: String) {
        val clean = version.removePrefix("v").removePrefix("V")
        prefs.edit().putString("dismissed_version", clean).apply()
        _updateInfo.update { it.copy(isDismissed = true) }
    }

    /**
     * Downloads the latest APK update with live progress and directly invokes
     * the system package installer.
     */
    fun downloadAndInstall(context: Context) {
        val targetUrl = _updateInfo.value.downloadUrl?.takeIf { it.isNotBlank() }
            ?: "https://tuneglass.vercel.app/tuneglass.apk"

        // If target is not an APK link, fallback to opening browser
        if (!targetUrl.contains(".apk", ignoreCase = true) && !targetUrl.contains("vercel.app", ignoreCase = true)) {
            openUpdate(context)
            return
        }

        // If we already have a freshly downloaded update APK, trigger install directly
        val existingPath = _updateInfo.value.downloadedApkPath
        if (existingPath != null) {
            val file = File(existingPath)
            if (file.exists() && file.length() > 1024 * 1024) {
                installDownloadedApk(context, file)
                return
            }
        }

        scope.launch {
            _updateInfo.update {
                it.copy(
                    isDownloading = true,
                    downloadProgress = 0f,
                    message = "Downloading TuneGlass update...",
                )
            }

            try {
                val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
                val targetFile = File(updatesDir, "TuneGlass-update.apk")
                if (targetFile.exists()) targetFile.delete()

                val request = Request.Builder()
                    .url(targetUrl)
                    .header("User-Agent", "TuneGlass-Android")
                    .build()

                val response = withContext(Dispatchers.IO) {
                    okHttpClient.newCall(request).execute()
                }

                if (!response.isSuccessful) {
                    val code = response.code
                    _updateInfo.update {
                        it.copy(
                            isDownloading = false,
                            message = "Download failed (HTTP $code)",
                        )
                    }
                    return@launch
                }

                val body = response.body ?: throw IllegalStateException("Empty response body")
                val totalBytes = body.contentLength()

                withContext(Dispatchers.IO) {
                    body.byteStream().use { input ->
                        FileOutputStream(targetFile).use { output ->
                            val buffer = ByteArray(32 * 1024)
                            var bytesRead: Int
                            var accumulated = 0L
                            var lastReport = 0L

                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                accumulated += bytesRead

                                val now = System.currentTimeMillis()
                                if (now - lastReport > 200 && totalBytes > 0) {
                                    lastReport = now
                                    val progress = (accumulated.toFloat() / totalBytes).coerceIn(0f, 1f)
                                    _updateInfo.update {
                                        it.copy(downloadProgress = progress)
                                    }
                                }
                            }
                            output.flush()
                        }
                    }
                }

                _updateInfo.update {
                    it.copy(
                        isDownloading = false,
                        downloadProgress = 1f,
                        downloadedApkPath = targetFile.absolutePath,
                        message = "Download complete. Starting installation...",
                    )
                }

                withContext(Dispatchers.Main) {
                    installDownloadedApk(context, targetFile)
                }
            } catch (e: Exception) {
                _updateInfo.update {
                    it.copy(
                        isDownloading = false,
                        message = "Update download failed: ${e.message ?: "Network error"}",
                    )
                }
            }
        }
    }

    /**
     * Prompts Android package installer via FileProvider.
     */
    fun installDownloadedApk(context: Context, apkFile: File) {
        if (!apkFile.exists() || apkFile.length() < 1024) {
            _updateInfo.update { it.copy(message = "APK file is missing or corrupted") }
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(permissionIntent)
                _updateInfo.update { it.copy(message = "Please allow TuneGlass to install unknown apps") }
                return
            }
        }

        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile,
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            _updateInfo.update {
                it.copy(message = "Could not launch package installer: ${e.message}")
            }
            // Fallback to opening browser link
            openUpdate(context)
        }
    }

    fun openUpdate(context: Context) {
        val targetUrl = _updateInfo.value.downloadUrl ?: _updateInfo.value.releaseUrl.ifBlank {
            "https://tuneglass.vercel.app"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) { }
    }

    fun isNewerVersion(remote: String, local: String): Boolean {
        if (remote.isBlank() || local.isBlank()) return false
        val cleanRemote = remote.removePrefix("v").removePrefix("V").substringBefore("-")
        val cleanLocal = local.removePrefix("v").removePrefix("V").substringBefore("-")
        if (cleanRemote == cleanLocal) return false

        val rParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val cParts = cleanLocal.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(rParts.size, cParts.size)
        for (i in 0 until maxLen) {
            val r = rParts.getOrElse(i) { 0 }
            val c = cParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
