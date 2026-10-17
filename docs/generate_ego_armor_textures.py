"""生成 E.G.O 装甲的占位贴图。

产物：
  textures/item/ego_helmet.png / ego_chestplate.png / ego_leggings.png / ego_boots.png  (16x16 物品图标)
  textures/models/armor/ego_layer_1.png / ego_layer_2.png                              (64x32 盔甲图层)

配色沿用设计规格书 4.2 的调色板：焦褐黑 / 血红 / 淡金。
这些都是占位图，正式贴图由用户后续替换。
"""

from PIL import Image, ImageDraw
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ITEM_DIR = os.path.join(ROOT, "src", "main", "resources", "assets", "moonproject", "textures", "item")
ARMOR_DIR = os.path.join(ROOT, "src", "main", "resources", "assets", "moonproject", "textures", "models", "armor")

BASE = (42, 29, 24, 255)      # 焦褐黑
DARK = (24, 16, 13, 255)      # 更暗的阴影
BLOOD = (139, 26, 26, 255)    # 血红
GOLD = (216, 176, 74, 255)    # 淡金
EDGE = (232, 217, 160, 255)   # 高光边
CLEAR = (0, 0, 0, 0)


def blank(w=16, h=16):
    return Image.new("RGBA", (w, h), CLEAR)


def helmet():
    img = blank()
    d = ImageDraw.Draw(img)
    d.rectangle([3, 2, 12, 7], fill=BASE)
    d.rectangle([2, 5, 13, 12], fill=BASE)
    d.rectangle([2, 5, 13, 6], fill=GOLD)
    d.rectangle([5, 8, 10, 12], fill=DARK)
    d.point((4, 9), fill=BLOOD)
    d.point((11, 9), fill=BLOOD)
    d.rectangle([2, 12, 3, 13], fill=EDGE)
    d.rectangle([12, 12, 13, 13], fill=EDGE)
    return img


def chestplate():
    img = blank()
    d = ImageDraw.Draw(img)
    d.rectangle([4, 2, 11, 5], fill=BASE)       # 领口
    d.rectangle([3, 5, 12, 13], fill=BASE)      # 躯干
    d.rectangle([1, 6, 2, 11], fill=BASE)       # 左肩
    d.rectangle([13, 6, 14, 11], fill=BASE)     # 右肩
    d.rectangle([1, 6, 14, 6], fill=GOLD)       # 金色横条
    d.rectangle([6, 8, 9, 12], fill=DARK)       # 胸口暗纹
    d.rectangle([7, 9, 8, 10], fill=BLOOD)      # 核心
    d.rectangle([3, 13, 12, 13], fill=EDGE)
    return img


def leggings():
    img = blank()
    d = ImageDraw.Draw(img)
    d.rectangle([3, 2, 12, 5], fill=BASE)
    d.rectangle([3, 2, 12, 2], fill=GOLD)
    d.rectangle([3, 6, 6, 13], fill=BASE)
    d.rectangle([9, 6, 12, 13], fill=BASE)
    d.rectangle([3, 13, 6, 13], fill=DARK)
    d.rectangle([9, 13, 12, 13], fill=DARK)
    d.point((5, 8), fill=BLOOD)
    d.point((10, 8), fill=BLOOD)
    return img


def boots():
    img = blank()
    d = ImageDraw.Draw(img)
    d.rectangle([2, 4, 6, 11], fill=BASE)
    d.rectangle([9, 4, 13, 11], fill=BASE)
    d.rectangle([2, 4, 6, 4], fill=GOLD)
    d.rectangle([9, 4, 13, 4], fill=GOLD)
    d.rectangle([2, 11, 7, 12], fill=DARK)      # 鞋底
    d.rectangle([8, 11, 13, 12], fill=DARK)
    d.point((4, 7), fill=BLOOD)
    d.point((11, 7), fill=BLOOD)
    return img


def armor_layer():
    """盔甲图层：把 UV 区域整体铺成装甲色，加金色饰边与暗部，做出占位"装甲"观感。"""
    img = Image.new("RGBA", (64, 32), CLEAR)
    d = ImageDraw.Draw(img)

    # 整体铺底（未被 UV 采样的区域不影响渲染）
    d.rectangle([0, 0, 63, 31], fill=BASE)

    # 明暗分层，让模型上有立体感
    d.rectangle([0, 0, 63, 15], fill=(48, 33, 27, 255))
    d.rectangle([0, 16, 63, 31], fill=(36, 25, 21, 255))

    # 金色饰边
    d.rectangle([0, 0, 63, 1], fill=GOLD)
    d.rectangle([0, 15, 63, 16], fill=GOLD)
    d.rectangle([0, 30, 63, 31], fill=DARK)

    # 血红核心点缀
    d.rectangle([4, 4, 5, 5], fill=BLOOD)
    d.rectangle([20, 4, 21, 5], fill=BLOOD)
    d.rectangle([40, 20, 41, 21], fill=BLOOD)
    return img


def main():
    os.makedirs(ITEM_DIR, exist_ok=True)
    os.makedirs(ARMOR_DIR, exist_ok=True)

    for name, image in (
        ("ego_helmet", helmet()),
        ("ego_chestplate", chestplate()),
        ("ego_leggings", leggings()),
        ("ego_boots", boots()),
    ):
        path = os.path.join(ITEM_DIR, name + ".png")
        image.save(path)
        print("wrote", path, image.size)

    for index in (1, 2):
        path = os.path.join(ARMOR_DIR, "ego_layer_%d.png" % index)
        armor_layer().save(path)
        print("wrote", path, (64, 32))


if __name__ == "__main__":
    main()
