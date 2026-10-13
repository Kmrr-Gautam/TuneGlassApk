import os
from PIL import Image, ImageDraw, ImageFilter

def generate_all_icons():
    src_path = r"C:\Users\Gautam Kumar\.gemini\antigravity-ide\brain\bf4a9024-4e46-4165-a317-be8ca85330d7\.user_uploaded\media_1791049028175.png"
    src = Image.open(src_path).convert("RGBA")
    w, h = src.size

    # 1. Cleanly extract the white waveform + music emblem
    emblem = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    min_x, max_x, min_y, max_y = w, 0, h, 0

    for x in range(w):
        for y in range(h):
            r, g, b, a = src.getpixel((x, y))
            dist = ((r - 54)**2 + (g - 117)**2 + (b - 135)**2)**0.5
            if dist > 45 and (r + g + b) > 380:
                alpha = min(255, int(dist * 2.4))
                # Normalize color to clean off-white #EEF6F5
                emblem.putpixel((x, y), (238, 246, 245, alpha))
                min_x = min(min_x, x)
                max_x = max(max_x, x)
                min_y = min(min_y, y)
                max_y = max(max_y, y)

    # Crop emblem tightly
    padding = 4
    crop_box = (
        max(0, min_x - padding),
        max(0, min_y - padding),
        min(w, max_x + padding),
        min(h, max_y + padding),
    )
    cropped_emblem = emblem.crop(crop_box)
    ew, eh = cropped_emblem.size

    teal_bg = (50, 115, 133, 255) # #327385
    teal_top = (58, 128, 148, 255) # subtle ambient glow

    def render_icon(size, is_round=False, for_adaptive_fg=False):
        res = size * 2 # Supersampling
        canvas = Image.new("RGBA", (res, res), (0, 0, 0, 0))
        draw = ImageDraw.Draw(canvas)

        if not for_adaptive_fg:
            # Background shape
            margin = int(res * 0.04)
            if is_round:
                draw.ellipse([margin, margin, res - margin, res - margin], fill=teal_bg)
            else:
                radius = int(res * 0.22)
                draw.rounded_rectangle([margin, margin, res - margin, res - margin], radius=radius, fill=teal_bg)

            # Ambient soft highlight on top-center
            glow = Image.new("RGBA", (res, res), (0, 0, 0, 0))
            g_draw = ImageDraw.Draw(glow)
            g_draw.ellipse([int(res * 0.15), int(res * 0.02), int(res * 0.85), int(res * 0.45)], fill=(255, 255, 255, 28))
            glow = glow.filter(ImageFilter.GaussianBlur(int(res * 0.08)))
            canvas = Image.alpha_composite(canvas, glow)

        # Scale emblem to fit nicely within icon safe zone
        # Android launcher safe zone is ~66% of canvas
        target_emblem_w = int(res * (0.64 if for_adaptive_fg else 0.76))
        scale_ratio = target_emblem_w / float(ew)
        target_emblem_h = int(eh * scale_ratio)
        
        resized_emblem = cropped_emblem.resize((target_emblem_w, target_emblem_h), Image.Resampling.LANCZOS)
        
        # Center the emblem
        pos_x = (res - target_emblem_w) // 2
        pos_y = (res - target_emblem_h) // 2
        
        # Add subtle drop shadow underneath emblem for rich depth
        shadow = Image.new("RGBA", (res, res), (0, 0, 0, 0))
        shadow.paste((0, 0, 0, 80), (pos_x, pos_y + max(2, int(res * 0.012))), resized_emblem)
        shadow = shadow.filter(ImageFilter.GaussianBlur(max(1, int(res * 0.015))))
        canvas = Image.alpha_composite(canvas, shadow)

        # Paste the crisp emblem
        canvas.paste(resized_emblem, (pos_x, pos_y), resized_emblem)

        return canvas.resize((size, size), Image.Resampling.LANCZOS)

    # Master icons
    logo_512 = render_icon(512, is_round=False)
    logo_512.save("tuneglass_logo.png")
    logo_512.save("listenit_logo.png")

    # Adaptive foreground image (108dp default at 432x432 px)
    adaptive_fg = render_icon(432, for_adaptive_fg=True)
    os.makedirs("app/src/main/res/drawable-nodpi", exist_ok=True)
    adaptive_fg.save("app/src/main/res/drawable-nodpi/ic_launcher_fg_raster.png")

    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    for folder, px in densities.items():
        p = os.path.join("app/src/main/res", folder)
        os.makedirs(p, exist_ok=True)
        ico = render_icon(px, is_round=False)
        ico.save(os.path.join(p, "ic_launcher.png"))
        ico_round = render_icon(px, is_round=True)
        ico_round.save(os.path.join(p, "ic_launcher_round.png"))

    print("Successfully generated all TuneGlass icons from the new logo!")

if __name__ == "__main__":
    generate_all_icons()
