"""生成 7 个状态效果的图标（18x18，MC 的 mob_effect 图集规格）。

产物：assets/moonproject/textures/mob_effect/<id>.png
没有图标时 MC 会显示品红黑格子（缺失贴图），所以这一组必须存在。
"""

from PIL import Image, ImageDraw
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "moonproject", "textures", "mob_effect")
SIZE = 18


def blank():
    return Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))


def disc(draw, box, fill, outline=None):
    draw.ellipse(box, fill=fill, outline=outline)


def bleed():
    """血滴。"""
    img = blank()
    d = ImageDraw.Draw(img)
    d.polygon([(9, 1), (14, 9), (9, 16), (4, 9)], fill=(150, 24, 24, 255))
    d.polygon([(9, 4), (12, 9), (9, 13), (6, 9)], fill=(200, 60, 60, 255))
    d.point((8, 7), fill=(255, 170, 170, 255))
    return img


def burn():
    """火焰。"""
    img = blank()
    d = ImageDraw.Draw(img)
    d.polygon([(9, 1), (13, 7), (12, 12), (9, 16), (5, 12), (6, 6)], fill=(226, 110, 26, 255))
    d.polygon([(9, 5), (11, 10), (9, 14), (7, 10)], fill=(255, 196, 90, 255))
    d.point((9, 11), fill=(255, 245, 210, 255))
    return img


def poise():
    """呼吸：一圈气流。"""
    img = blank()
    d = ImageDraw.Draw(img)
    d.arc([2, 3, 15, 16], start=20, end=300, fill=(214, 232, 245, 255), width=2)
    d.arc([4, 6, 13, 13], start=200, end=520, fill=(150, 190, 220, 255), width=2)
    d.point((13, 5), fill=(255, 255, 255, 255))
    return img


def laceration():
    """裂伤：一道撕裂口。"""
    img = blank()
    d = ImageDraw.Draw(img)
    d.polygon([(2, 4), (16, 10), (16, 12), (2, 6)], fill=(176, 58, 90, 255))
    for i in range(4):
        x = 4 + i * 3
        d.line([(x, 6), (x + 2, 12)], fill=(230, 120, 150, 255), width=1)
    return img


def cutoff():
    """斩断：断开的横线。"""
    img = blank()
    d = ImageDraw.Draw(img)
    d.rectangle([1, 7, 7, 11], fill=(122, 92, 176, 255))
    d.rectangle([11, 7, 17, 11], fill=(122, 92, 176, 255))
    d.polygon([(7, 4), (12, 9), (7, 14)], fill=(190, 160, 245, 255))
    return img


def cutbond():
    """斩缘：层叠的环。"""
    img = blank()
    d = ImageDraw.Draw(img)
    d.ellipse([1, 1, 16, 16], outline=(176, 200, 232, 255), width=2)
    d.ellipse([5, 5, 12, 12], outline=(120, 160, 210, 255), width=2)
    d.point((9, 2), fill=(255, 255, 255, 255))
    return img


def black_scar():
    """焦黑斩痕：焦痕 + 星芒。"""
    img = blank()
    d = ImageDraw.Draw(img)
    d.ellipse([3, 3, 14, 14], fill=(42, 29, 24, 255), outline=(120, 60, 40, 255))
    d.line([(9, 1), (9, 16)], fill=(200, 110, 70, 255), width=1)
    d.line([(2, 9), (15, 9)], fill=(200, 110, 70, 255), width=1)
    d.point((9, 9), fill=(255, 210, 150, 255))
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    icons = {
        "black_scar": black_scar(),
        "bleed": bleed(),
        "burn": burn(),
        "poise": poise(),
        "laceration": laceration(),
        "cutoff": cutoff(),
        "cutbond": cutbond(),
    }
    for name, image in icons.items():
        path = os.path.join(OUT, name + ".png")
        image.save(path)
        print("wrote", path, image.size)


if __name__ == "__main__":
    main()
