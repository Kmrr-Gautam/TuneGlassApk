package com.listenit.music.data.playlist

import kotlinx.serialization.Serializable

@Serializable
data class SpotifyUserPlaylist(
    val id: String,
    val name: String,
    val description: String? = null,
    val trackCount: Int = 0,
    val artworkUrl: String? = null,
    val ownerName: String? = null,
    val isOwner: Boolean = false,
)

data class SpotifyAccount(
    val displayName: String = "",
    val userId: String = "",
    val avatarUrl: String? = null,
    val isConnected: Boolean = false,
)
