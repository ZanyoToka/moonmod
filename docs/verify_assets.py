"""构建前自检（不需要 Gradle）：检查语言键、模型、贴图、配方、效果注册的一致性。

用法：python docs/verify_assets.py
任何 ❌ 都必须在进游戏前修掉，否则会表现为「物品变成紫黑格 / 提示文字是原始 key / 配方失效」。
"""

import json
import os
import re
import sys

# 控制台可能是 GBK，先把标准输出切到 UTF-8，避免中文/符号打印失败
try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
JAVA = os.path.join(ROOT, "src", "main", "java")
MODID = "moonproject"


def read_json(path):
    with open(path, encoding="utf-8") as handle:
        return json.load(handle)


errors = []

# ── 1) 语言键：代码里引用的 key 必须在中英文语言文件里都存在 ──────────────────
lang_zh = read_json(os.path.join(RES, "assets", MODID, "lang", "zh_cn.json"))
lang_en = read_json(os.path.join(RES, "assets", MODID, "lang", "en_us.json"))

used_keys = set()
for dirpath, _, files in os.walk(JAVA):
    for name in files:
        if not name.endswith(".java"):
            continue
        text = open(os.path.join(dirpath, name), encoding="utf-8").read()
        for match in re.finditer(r'"((?:item|effect|itemGroup)\.moonproject[^"]*)"', text):
            used_keys.add(match.group(1))

for key in sorted(used_keys):
    if key not in lang_zh:
        errors.append("缺少中文语言键: " + key)
    if key not in lang_en:
        errors.append("缺少英文语言键: " + key)

# ── 2) 物品：注册的每个 id 都要有模型 + 贴图 + 物品名 ─────────────────────────
items_src = open(os.path.join(JAVA, "com", "moonproject", "moonmod", "item", "ModItems.java"),
                 encoding="utf-8").read()
item_ids = re.findall(r'ITEMS\.register\("([^"]+)"', items_src)

for item_id in item_ids:
    model_path = os.path.join(RES, "assets", MODID, "models", "item", item_id + ".json")
    if not os.path.exists(model_path):
        errors.append("缺少模型: models/item/%s.json" % item_id)
    else:
        textures = read_json(model_path).get("textures", {})
        layer = textures.get("layer0")
        if layer:
            namespace, path = layer.split(":", 1) if ":" in layer else (MODID, layer)
            texture_path = os.path.join(RES, "assets", namespace, "textures", path + ".png")
            if not os.path.exists(texture_path):
                errors.append("缺少贴图: textures/%s.png（物品 %s）" % (path, item_id))
    if ("item.%s.%s" % (MODID, item_id)) not in lang_zh:
        errors.append("缺少物品名: item.%s.%s" % (MODID, item_id))

# ── 3) 状态效果：注册的效果都要有名字 ─────────────────────────────────────────
effects_src = open(os.path.join(JAVA, "com", "moonproject", "moonmod", "effect", "ModEffects.java"),
                   encoding="utf-8").read()
effect_ids = re.findall(r'register\("([a-z_]+)"', effects_src)
for effect_id in effect_ids:
    if ("effect.%s.%s" % (MODID, effect_id)) not in lang_zh:
        errors.append("缺少效果名: effect.%s.%s" % (MODID, effect_id))
    icon_path = os.path.join(RES, "assets", MODID, "textures", "mob_effect", effect_id + ".png")
    if not os.path.exists(icon_path):
        errors.append("缺少效果图标（游戏里会显示品红黑格子）: textures/mob_effect/%s.png" % effect_id)

# ── 4) 配方：引用的物品必须已注册，产物同理 ──────────────────────────────────
recipe_dir = os.path.join(RES, "data", MODID, "recipes")
if os.path.isdir(recipe_dir):
    for name in sorted(os.listdir(recipe_dir)):
        if not name.endswith(".json"):
            continue
        data = read_json(os.path.join(recipe_dir, name))
        for ingredient in (data.get("key") or {}).values():
            ref = ingredient.get("item")
            if ref and ref.startswith(MODID + ":"):
                if ref.split(":", 1)[1] not in item_ids:
                    errors.append("配方 %s 引用了未注册物品: %s" % (name, ref))
        result = (data.get("result") or {}).get("item")
        if result and result.startswith(MODID + ":"):
            if result.split(":", 1)[1] not in item_ids:
                errors.append("配方 %s 的产物未注册: %s" % (name, result))

# ── 5) Config 的方法名与键名唯一性（重复会直接编译/加载失败）────────────────
config_path = os.path.join(JAVA, "com", "moonproject", "moonmod", "config", "MoonProjectConfig.java")
if os.path.exists(config_path):
    with open(config_path, encoding="utf-8") as handle:
        config_src = handle.read()
    methods = re.findall(r"^\s{4}public static [A-Za-z\[\]]+ ([a-zA-Z0-9_]+)\(\)", config_src, re.M)
    counts = {}
    for name in methods:
        counts[name] = counts.get(name, 0) + 1
    for name, count in counts.items():
        if count > 1:
            errors.append("MoonProjectConfig 方法重复定义: %s x%d" % (name, count))
    config_keys = re.findall(r'\.define(?:InRange)?\("([A-Za-z0-9_]+)"', config_src)
    key_counts = {}
    for key in config_keys:
        key_counts[key] = key_counts.get(key, 0) + 1
    for key, count in key_counts.items():
        if count > 1:
            errors.append("Config 键重复: %s x%d" % (key, count))

# ── 6) 报告结果 ─────────────────────────────────────────────────────────────
print("检查物品 %d 个、效果 %d 个、语言键 %d 个、Config 方法 %d 个、Config 键 %d 个"
      % (len(item_ids), len(effect_ids), len(used_keys),
         len(methods) if os.path.exists(config_path) else 0,
         len(config_keys) if os.path.exists(config_path) else 0))
if errors:
    print("\n发现 %d 个问题：" % len(errors))
    for line in errors:
        print("  [x] " + line)
    sys.exit(1)
print("[OK] 资源与语言自检全部通过")
