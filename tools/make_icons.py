#!/usr/bin/env python3
from PIL import Image, ImageDraw, ImageFont
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "res")
FONT = os.path.join(ROOT, "src", "main", "assets", "materialicons.ttf")

BASE = (30, 30, 46)
CRUST = (17, 17, 27)
SURFACE = (49, 50, 68)
MAUVE = (203, 166, 247)
BLUE = (137, 180, 250)

DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}


def gradient(size, top, bottom):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for y in range(size):
        t = y / max(1, size - 1)
        c = (
            int(top[0] + (bottom[0] - top[0]) * t),
            int(top[1] + (bottom[1] - top[1]) * t),
            int(top[2] + (bottom[2] - top[2]) * t),
            255,
        )
        d.line([(0, y), (size, y)], fill=c)
    return img


def glyph_mask(size, codepoint, font_size, offset_y=0):
    font = ImageFont.truetype(FONT, font_size)
    mask = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(mask)
    ch = chr(codepoint)
    box = d.textbbox((0, 0), ch, font=font)
    w = box[2] - box[0]
    h = box[3] - box[1]
    x = (size - w) / 2 - box[0]
    y = (size - h) / 2 - box[1] + offset_y
    d.text((x, y), ch, font=font, fill=255)
    return mask


def rounded_mask(size, radius):
    mask = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(mask)
    d.rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=255)
    return mask


def build_master(size=1024):
    bg = gradient(size, SURFACE, CRUST)
    mask = rounded_mask(size, int(size * 0.225))
    icon = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    icon.paste(bg, (0, 0), mask)

    grad = gradient(size, MAUVE, BLUE)
    gm = glyph_mask(size, 0xE157, int(size * 0.52))
    icon.paste(grad, (0, 0), gm)
    return icon


def build_foreground(size=432):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    grad = gradient(size, MAUVE, BLUE)
    gm = glyph_mask(size, 0xE157, int(size * 0.34))
    img.paste(grad, (0, 0), gm)
    return img


def main():
    master = build_master(1024)
    for name, px in DENSITIES.items():
        d = os.path.join(RES, "mipmap-" + name)
        os.makedirs(d, exist_ok=True)
        img = master.resize((px, px), Image.LANCZOS)
        img.save(os.path.join(d, "ic_launcher.png"))
        img.save(os.path.join(d, "ic_launcher_round.png"))

    fg = build_foreground(432)
    fg.save(os.path.join(RES, "drawable", "ic_launcher_foreground.png"))
    print("icons written")


if __name__ == "__main__":
    main()
