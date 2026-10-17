"""按官方立绘重画阿赖耶识的物品栏图标（32x32）。

参考图特征：
- 刀鞘：白色 + 红色汉字（三生缘分 / 三千世界 / 三世因果）+ 两端红绳缠绕
- 刀柄：红绳缠绕 + 白色菱形柄卷
- 护手：圆形金色（tsuba）
- 刀身：白/银

产出：
  textures/item/alaya_vijnana.png        鞘击态（白鞘）
  textures/item/alaya_vijnana_drawn.png  拔刀态（白刃）
"""

from PIL import Image, ImageDraw
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "moonproject", "textures", "item")

W = 32
OUTLINE = (36, 24, 20, 255)      # 描边（深焦褐）
WHITE = (240, 238, 230, 255)     # 鞘/刃 本体
WHITE_HI = (255, 255, 255, 255)  # 高光
WHITE_SH = (196, 196, 190, 255)  # 暗部
RED = (161, 22, 27, 255)         # 红绳
RED_D = (110, 14, 18, 255)       # 红绳暗部
GOLD = (217, 194, 122, 255)      # 护手
GOLD_D = (156, 132, 70, 255)
STEEL = (232, 236, 240, 255)     # 刀身
STEEL_SH = (176, 186, 196, 255)


def blank():
    return Image.new("RGBA", (W, W), (0, 0, 0, 0))


def thick_line(d, x0, y0, x1, y1, fill, width=1):
    d.line([(x0, y0), (x1, y1)], fill=fill, width=width)


def draw_wrap(d, x, y, dx, dy, length=4):
    """在刀柄/鞘上画红绳缠绕（几道斜线）。"""
    for i in range(length):
        cx = x + dx * i
        cy = y + dy * i
        d.line([(cx - 1, cy + 1), (cx + 1, cy - 1)], fill=RED, width=1)


def sheathed():
    """鞘击态：白色刀鞘 + 红绳 + 金色护手 + 红柄。"""
    img = blank()
    d = ImageDraw.Draw(img)

    # 刀鞘主体（左下 -> 右上），先粗描边再填白
    thick_line(d, 5, 27, 26, 6, OUTLINE, 5)
    thick_line(d, 5, 27, 26, 6, WHITE, 3)
    thick_line(d, 6, 26, 24, 8, WHITE_HI, 1)
    thick_line(d, 5, 26, 23, 8, WHITE_SH, 1)

    # 鞘身红色汉字（用短线表示，避开中线）
    for i in range(5):
        bx = 9 + i * 3
        by = 23 - i * 3
        d.line([(bx, by - 1), (bx + 1, by + 1)], fill=RED, width=1)
        if i % 2 == 0:
            d.point((bx + 2, by), fill=RED_D)

    # 两端红绳缠绕
    for i in range(3):
        d.line([(6 + i, 26 - i), (8 + i, 24 - i)], fill=RED, width=2)
        d.line([(22 + i, 10 - i), (24 + i, 8 - i)], fill=RED, width=2)

    # 护手（金色圆盘）
    d.ellipse([19, 11, 25, 17], fill=GOLD, outline=OUTLINE)
    d.point((22, 13), fill=(255, 246, 210, 255))

    # 刀柄（红绳 + 白菱形）
    thick_line(d, 25, 7, 30, 2, OUTLINE, 4)
    thick_line(d, 25, 7, 30, 2, RED, 2)
    d.point((27, 5), fill=WHITE)
    d.point((29, 3), fill=WHITE)

    return img


def drawn():
    """拔刀态：白刃 + 金色护手 + 红柄。"""
    img = blank()
    d = ImageDraw.Draw(img)

    # 刀身
    thick_line(d, 4, 28, 27, 5, OUTLINE, 4)
    thick_line(d, 4, 28, 27, 5, STEEL, 2)
    thick_line(d, 5, 27, 26, 6, WHITE_HI, 1)
    # 刃纹（血红细线）
    for i in range(6):
        bx = 8 + i * 3
        by = 24 - i * 3
        d.point((bx, by), fill=(139, 26, 26, 255))

    # 护手（金色圆盘）
    d.ellipse([19, 11, 25, 17], fill=GOLD, outline=OUTLINE)
    d.point((22, 13), fill=(255, 246, 210, 255))

    # 刀柄
    thick_line(d, 25, 7, 30, 2, OUTLINE, 4)
    thick_line(d, 25, 7, 30, 2, RED, 2)
    d.point((27, 5), fill=WHITE)
    d.point((29, 3), fill=WHITE)

    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    for name, image in (("alaya_vijnana", sheathed()), ("alaya_vijnana_drawn", drawn())):
        path = os.path.join(OUT, name + ".png")
        image.save(path)
        print("wrote", path, image.size)


if __name__ == "__main__":
    main()
