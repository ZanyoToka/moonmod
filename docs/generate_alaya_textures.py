# -*- coding: utf-8 -*-
"""生成阿赖耶识的两张占位贴图（32×32，带 alpha）。

用法：python generate_alaya_textures.py
输出：
  src/main/resources/assets/moonproject/textures/item/alaya_vijnana.png        鞘击态（刀在鞘中）
  src/main/resources/assets/moonproject/textures/item/alaya_vijnana_drawn.png  解放态（白刃出鞘）
  docs/texture-preview-8x.png                                                  8 倍放大预览，便于人工检查

色板取自设计规格书 4.2。这是打样占位图，正式贴图请在 Aseprite 里重绘。
"""
import os

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(HERE)
TEX_DIR = os.path.join(REPO, "src", "main", "resources", "assets", "moonproject", "textures", "item")

SIZE = 32
TRANSPARENT = (0, 0, 0, 0)

C_HANDLE = (35, 38, 46, 255)        # 缠柄
C_HANDLE_WRAP = (58, 63, 75, 255)   # 柄卷纹
C_FITTING = (110, 122, 143, 255)    # 鞘口/目贯等金属件
C_SHEATH = (27, 31, 42, 255)        # 刀鞘主体
C_SHEATH_HL = (58, 65, 82, 255)     # 刀鞘受光面
C_EDGE = (242, 246, 255, 255)       # 刃口亮线
C_BLADE = (232, 236, 242, 130)      # 半透明刀身
C_BLADE_FAINT = (201, 214, 232, 85)  # 刀身背光侧，更透明


def stamp(img, p0, p1, layers):
    """沿 p0→p1 逐点绘制；layers 是 [(垂直偏移, 颜色), ...]。"""
    dx, dy = p1[0] - p0[0], p1[1] - p0[1]
    length = max(abs(dx), abs(dy))
    if length == 0:
        return
    nx, ny = -dy / length, dx / length
    steps = length * 4
    for i in range(steps + 1):
        t = i / steps
        x = p0[0] + dx * t
        y = p0[1] + dy * t
        for off, color in layers:
            px = int(round(x + nx * off))
            py = int(round(y + ny * off))
            if 0 <= px < SIZE and 0 <= py < SIZE:
                img.putpixel((px, py), color)


def base_hilt(img):
    """柄（缠柄 + 柄头）与镡（护手），两个形态共用。"""
    stamp(img, (5, 28), (10, 23), [(0, C_HANDLE), (-1, C_HANDLE), (1, C_HANDLE)])
    # 柄卷纹：沿柄每隔一段打一个亮点
    stamp(img, (6, 27), (9, 24), [(0, C_HANDLE_WRAP)])
    # 镡（护手）：垂直于刀身的一小段
    stamp(img, (10, 22), (12, 20), [(-1, C_FITTING), (0, C_FITTING), (1, C_FITTING), (2, C_FITTING)])


def make_sheathed():
    img = Image.new("RGBA", (SIZE, SIZE), TRANSPARENT)
    base_hilt(img)
    # 刀鞘主体：近黑，受光面略亮
    stamp(img, (12, 20), (29, 3), [(0, C_SHEATH), (-1, C_SHEATH_HL), (1, C_SHEATH)])
    # 鞘口与鞘尻的金属件
    stamp(img, (12, 20), (13, 19), [(0, C_FITTING), (-1, C_FITTING), (1, C_FITTING)])
    stamp(img, (28, 4), (29, 3), [(0, C_FITTING), (-1, C_FITTING), (1, C_FITTING)])
    return img


def make_drawn():
    img = Image.new("RGBA", (SIZE, SIZE), TRANSPARENT)
    base_hilt(img)
    # 刃：亮线 + 半透明刀身 + 更透明的背光侧
    stamp(img, (12, 20), (30, 2), [(-1, C_EDGE), (0, C_BLADE), (1, C_BLADE_FAINT)])
    # 刀尖收束
    stamp(img, (29, 3), (30, 2), [(0, C_EDGE), (-1, C_EDGE)])
    return img


def save(img, path):
    img.save(path)
    print("已生成", path, img.size)


def main():
    os.makedirs(TEX_DIR, exist_ok=True)
    sheathed = make_sheathed()
    drawn = make_drawn()
    save(sheathed, os.path.join(TEX_DIR, "alaya_vijnana.png"))
    save(drawn, os.path.join(TEX_DIR, "alaya_vijnana_drawn.png"))

    # 8 倍放大预览（并排），便于人工检查
    preview = Image.new("RGBA", (SIZE * 2 + 4, SIZE), (24, 24, 28, 255))
    preview.alpha_composite(sheathed, (0, 0))
    preview.alpha_composite(drawn, (SIZE + 4, 0))
    preview = preview.resize(((SIZE * 2 + 4) * 8, SIZE * 8), Image.NEAREST)
    out = os.path.join(REPO, "docs", "texture-preview-8x.png")
    preview.save(out)
    print("已生成预览", out, preview.size)


if __name__ == "__main__":
    main()
