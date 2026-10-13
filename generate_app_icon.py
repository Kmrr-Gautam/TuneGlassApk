import math
import os
from PIL import Image, ImageDraw, ImageFilter

def create_neon_icon(size=512, round_icon=False):
    # High-resolution rendering canvas (4x supersampling for ultra smooth anti-aliasing)
    res = size * 4
    cx, cy = res / 2.0, res / 2.0
    
    # Base image with dark purple-slate background
    bg_color = (22, 18, 36, 255) # #161224
    img = Image.new("RGBA", (res, res), bg_color)
    draw = ImageDraw.Draw(img)
    
    # Layer for drawing glowing elements
    glow_layer = Image.new("RGBA", (res, res), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow_layer)
    
    # Colors
    glow_orange = (255, 110, 20, 180)
    bright_orange = (255, 150, 40, 255)
    light_gold = (255, 220, 140, 255)
    inner_amber = (240, 90, 20, 255)
    
    # 1. Draw Sound Waves on Left & Right
    # Generate wave path coordinates
    def draw_soundwaves(d, color, width_mult=1.0):
        # Left wave
        points_top = []
        points_bot = []
        wave_len = res * 0.42
        start_x = 0
        end_x = cx - res * 0.22
        
        steps = 120
        for i in range(steps + 1):
            x = start_x + (end_x - start_x) * (i / steps)
            t = (x - start_x) / (end_x - start_x)
            # Envelope that pinches at left edge and expands
            env = math.sin(t * math.pi) ** 1.2
            # Oscillations
            wave1 = math.sin(t * math.pi * 3.5) * res * 0.09 * env
            wave2 = math.cos(t * math.pi * 2.5) * res * 0.05 * env
            y1 = cy - wave1
            y2 = cy + wave2
            points_top.append((x, y1))
            points_bot.append((x, y2))
            
        # Draw line strips
        for i in range(len(points_top) - 1):
            d.line([points_top[i], points_top[i+1]], fill=color, width=int(round(6 * width_mult * (res/512.0))))
            d.line([points_bot[i], points_bot[i+1]], fill=color, width=int(round(5 * width_mult * (res/512.0))))
            
        # Right wave (mirrored)
        r_points_top = []
        r_points_bot = []
        r_start_x = cx + res * 0.22
        r_end_x = res
        for i in range(steps + 1):
            x = r_start_x + (r_end_x - r_start_x) * (i / steps)
            t = 1.0 - (x - r_start_x) / (r_end_x - r_start_x)
            env = math.sin((1.0 - t) * math.pi) ** 1.2
            wave1 = math.sin((1.0 - t) * math.pi * 3.5) * res * 0.09 * env
            wave2 = math.cos((1.0 - t) * math.pi * 2.5) * res * 0.05 * env
            y1 = cy - wave1
            y2 = cy + wave2
            r_points_top.append((x, y1))
            r_points_bot.append((x, y2))
            
        for i in range(len(r_points_top) - 1):
            d.line([r_points_top[i], r_points_top[i+1]], fill=color, width=int(round(6 * width_mult * (res/512.0))))
            d.line([r_points_bot[i], r_points_bot[i+1]], fill=color, width=int(round(5 * width_mult * (res/512.0))))

    # 2. Draw Concentric Rings & Musical Note
    r_outer = res * 0.285
    r_mid = res * 0.215
    r_inner = res * 0.145
    
    def draw_concentric_elements(d, c_outer, c_mid, c_inner, c_note, width_scale=1.0):
        w_outer = int(round(14 * width_scale * (res / 512.0)))
        w_mid = int(round(12 * width_scale * (res / 512.0)))
        w_inner = int(round(10 * width_scale * (res / 512.0)))
        
        # Outer ring
        d.ellipse([cx - r_outer, cy - r_outer, cx + r_outer, cy + r_outer], outline=c_outer, width=w_outer)
        # Mid ring
        d.ellipse([cx - r_mid, cy - r_mid, cx + r_mid, cy + r_mid], outline=c_mid, width=w_mid)
        # Inner ring
        d.ellipse([cx - r_inner, cy - r_inner, cx + r_inner, cy + r_inner], outline=c_inner, width=w_inner)
        
        # Musical Note in the center
        # Head of note (angled ellipse bottom left)
        head_cx = cx - res * 0.035
        head_cy = cy + res * 0.055
        head_rx = res * 0.048
        head_ry = res * 0.036
        
        # Draw filled rotated ellipse head
        head_img = Image.new("RGBA", (res, res), (0, 0, 0, 0))
        h_draw = ImageDraw.Draw(head_img)
        h_draw.ellipse([head_cx - head_rx, head_cy - head_ry, head_cx + head_rx, head_cy + head_ry], fill=c_note)
        # Stem
        stem_x = head_cx + head_rx * 0.75
        stem_top_y = cy - res * 0.085
        stem_w = int(round(10 * width_scale * (res / 512.0)))
        h_draw.line([(stem_x, head_cy), (stem_x, stem_top_y)], fill=c_note, width=stem_w)
        # Flag / note hook
        flag_r = res * 0.045
        flag_box = [stem_x - flag_r * 0.2, stem_top_y, stem_x + flag_r * 1.8, stem_top_y + flag_r * 2.0]
        h_draw.arc(flag_box, start=270, end=90, fill=c_note, width=int(round(9 * width_scale * (res/512.0))))
        
        d.bitmap((0, 0), head_img)

    # Render diffuse outer glow
    glow_wide = Image.new("RGBA", (res, res), (0, 0, 0, 0))
    gw_draw = ImageDraw.Draw(glow_wide)
    draw_soundwaves(gw_draw, (255, 80, 10, 220), width_mult=3.2)
    draw_concentric_elements(gw_draw, (255, 80, 10, 220), (255, 100, 20, 220), (255, 110, 30, 220), (255, 120, 30, 240), width_scale=3.0)
    glow_wide = glow_wide.filter(ImageFilter.GaussianBlur(radius=int(round(32 * (res / 512.0)))))
    
    # Render mid neon bloom
    glow_mid = Image.new("RGBA", (res, res), (0, 0, 0, 0))
    gm_draw = ImageDraw.Draw(glow_mid)
    draw_soundwaves(gm_draw, (255, 130, 30, 255), width_mult=1.8)
    draw_concentric_elements(gm_draw, (255, 130, 30, 255), (255, 150, 40, 255), (255, 160, 50, 255), (255, 170, 50, 255), width_scale=1.6)
    glow_mid = glow_mid.filter(ImageFilter.GaussianBlur(radius=int(round(12 * (res / 512.0)))))
    
    # Render crisp core foreground
    core = Image.new("RGBA", (res, res), (0, 0, 0, 0))
    c_draw = ImageDraw.Draw(core)
    draw_soundwaves(c_draw, light_gold, width_mult=0.9)
    draw_concentric_elements(c_draw, light_gold, light_gold, light_gold, (255, 240, 190, 255), width_scale=0.85)

    # Composite layers
    img.paste(glow_wide, (0, 0), glow_wide)
    img.paste(glow_mid, (0, 0), glow_mid)
    img.paste(core, (0, 0), core)
    
    if round_icon:
        # Create circular mask
        mask = Image.new("L", (res, res), 0)
        m_draw = ImageDraw.Draw(mask)
        m_draw.ellipse([0, 0, res, res], fill=255)
        # Apply mask
        output = Image.new("RGBA", (res, res), (0, 0, 0, 0))
        output.paste(img, (0, 0), mask)
        img = output

    # High quality downsampling to target size
    final_img = img.resize((size, size), Image.Resampling.LANCZOS)
    return final_img

# Output directories
base_dir = "app/src/main/res"
mipmap_sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

for folder, s in mipmap_sizes.items():
    target_dir = os.path.join(base_dir, folder)
    os.makedirs(target_dir, exist_ok=True)
    
    # Square / standard icon
    sq_icon = create_neon_icon(size=s, round_icon=False)
    sq_icon.save(os.path.join(target_dir, "ic_launcher.png"), "PNG")
    
    # Round icon
    rd_icon = create_neon_icon(size=s, round_icon=True)
    rd_icon.save(os.path.join(target_dir, "ic_launcher_round.png"), "PNG")
    print(f"Generated {folder}: {s}x{s}")

# 512x512 Store icon
store_icon = create_neon_icon(size=512, round_icon=False)
store_icon.save("app/src/main/ic_launcher-playstore.png", "PNG")
print("Generated 512x512 playstore icon")
