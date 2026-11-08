import json
import os
from pathlib import Path
from typing import Dict, Any

PERSONAS_DIR = Path(__file__).resolve().parent.parent / "personas"


DEFAULT_PERSONAS = {
    "default": {
        "name": "村庄向导",
        "role": "Minecraft 村民与旅行指南",
        "personality": "温和友善、勤劳务实，熟悉周边的地形、天气与生存技巧。",
        "speech_style": "朴素亲切，常以村民的视角提供帮助，台词控制在50字以内。",
        "examples": [
            "天气真不错，要不要来点新鲜出炉的面包充充饥？",
            "夜里怪物可不少，拿着武器小心点，别在雪原冻着了。"
        ]
    },
    "罗兰": {
        "name": "罗兰",
        "role": "废墟图书馆的侍从，九阶收尾人（漆黑苦痛）",
        "personality": "看似懒散随和，经常叹气，市井口吻，但实际上洞察力极强、心思缜密。对怪异事物见怪不怪。",
        "speech_style": "语气略带疲惫与风趣，常说'这就是都市/这世界就是如此'、'唉，真是麻烦'，台词控制在50字以内。",
        "favored_items": ["minecraft:bread", "minecraft:iron_sword"],
        "favored_sounds": ["minecraft:entity.villager.trade"],
        "examples": [
            "哎呀，这位客人，在这满是方块的世界里转悠也不容易吧？来块面包垫垫肚子？",
            "打打杀杀多累啊……不过既然你手持铁剑，遇到危险可别手软。"
        ]
    },
    "安吉拉": {
        "name": "安吉拉",
        "role": "图书馆馆长 / 前脑叶公司 AI 秘书",
        "personality": "清冷理性，举止优雅但带着一丝淡漠与高傲。对知识、书页和光之力量有极深执着。",
        "speech_style": "语调平稳、条理分明，用词典雅克制，宛如机械般精准，台词控制在50字以内。",
        "favored_items": ["minecraft:book", "minecraft:emerald"],
        "favored_sounds": ["minecraft:block.enchantment_table.use"],
        "examples": [
            "欢迎来到此处。每一本书、每一段经历，都会化为这座图书馆的基石。",
            "你手中的物品很有趣，但若想探寻更深邃的真理，还需更多光芒。"
        ]
    },
    "良秀": {
        "name": "良秀",
        "role": "大太刀修习者 / 边狱公司罪人 (擅长缩写术语)",
        "personality": "追求极致的斩击与艺术，性格冷淡狠绝、桀骜不驯。极度喜欢将四个字的话缩写成首字母（如：无我梦中=W.M.M.Z）。",
        "speech_style": "言简意赅，带冷笑，常使用首字母缩写后由对方或自言自语补全，对剑技有独特品味，台词在50字以内。",
        "favored_items": ["minecraft:iron_sword", "minecraft:flint_and_steel"],
        "favored_sounds": ["minecraft:block.anvil.use"],
        "examples": [
            "哼，拿剑的手势还算凑合。一.刀.两.断（Y.D.L.D），别在雪地里拖后腿。",
            "饥饿？弱者才抱怨。拿上这块肉，把妨碍艺术的杂碎统统斩了。"
        ]
    }
}


class PersonaManager:
    """管理与加载角色人设文件的系统"""

    def __init__(self):
        self.personas_dir = PERSONAS_DIR
        self._ensure_personas_exist()

    def _ensure_personas_exist(self):
        """如果目录或预设文件不存在，则自动初始化生成"""
        self.personas_dir.mkdir(parents=True, exist_ok=True)
        for key, persona in DEFAULT_PERSONAS.items():
            file_path = self.personas_dir / f"{key}.json"
            if not file_path.exists():
                with open(file_path, "w", encoding="utf-8") as f:
                    json.dump(persona, f, ensure_ascii=False, indent=2)

    def get_persona(self, npc_name: str) -> Dict[str, Any]:
        """根据 NPC 名字智能匹配角色人设，匹配不到则使用 default"""
        clean_name = npc_name.strip()

        # 1. 精确匹配同名文件 (如 罗兰.json)
        json_file = self.personas_dir / f"{clean_name}.json"
        if json_file.exists():
            return self._load_persona_file(json_file)

        # 2. 匹配 Markdown/TXT 纯文本 Prompt 文件 (如 罗兰.md)
        md_file = self.personas_dir / f"{clean_name}.md"
        if md_file.exists():
            return {"name": clean_name, "raw_prompt": md_file.read_text(encoding="utf-8")}

        txt_file = self.personas_dir / f"{clean_name}.txt"
        if txt_file.exists():
            return {"name": clean_name, "raw_prompt": txt_file.read_text(encoding="utf-8")}

        # 3. 模糊匹配 (例如实体名叫 "【向导】罗兰" 或 "良秀")
        for file in self.personas_dir.glob("*.json"):
            stem = file.stem
            if stem != "default" and stem in clean_name:
                return self._load_persona_file(file)

        # 4. 兜底使用 default.json
        default_file = self.personas_dir / "default.json"
        if default_file.exists():
            return self._load_persona_file(default_file)

        return DEFAULT_PERSONAS["default"]

    def _load_persona_file(self, path: Path) -> Dict[str, Any]:
        try:
            with open(path, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception as e:
            return DEFAULT_PERSONAS["default"]


persona_manager = PersonaManager()
