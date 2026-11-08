import json
import logging
import re
from typing import List, Dict, Any
import httpx
from app.config import settings
from app.schemas import ChatRequest, ChatResponse, Action
from app.persona import persona_manager
from app.mood import mood_manager

logger = logging.getLogger("npc_llm")

# 原版常见安全基础物品白名单 (用于纠错校验)
COMMON_VALID_ITEMS = {
    "minecraft:bread", "minecraft:cooked_cod", "minecraft:cooked_beef", "minecraft:apple",
    "minecraft:cake", "minecraft:golden_apple", "minecraft:iron_sword", "minecraft:iron_pickaxe",
    "minecraft:book", "minecraft:writable_book", "minecraft:oak_log", "minecraft:birch_log",
    "minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:diamond", "minecraft:emerald",
    "minecraft:redstone", "minecraft:torch", "minecraft:leather_chestplate", "minecraft:compass"
}

EMOTION_TO_ANIMATION = {
    "happy": "happy",
    "excited": "dance",
    "touched": "bow",
    "sad": "sad",
    "angry": "angry",
    "proud": "proud",
    "surprise": "surprise",
    "clap": "clap",
    "neutral": "think"
}


def _build_system_prompt(persona: dict, mood_state: dict) -> str:
    name = persona.get("name", "NPC")
    role = persona.get("role", "Minecraft 居民")
    personality = persona.get("personality", "友好而机智")
    speech_style = persona.get("speech_style", "口吻生动，50字以内")
    examples = persona.get("examples", [])
    example_str = "\n".join([f"- \"{ex}\"" for ex in examples]) if examples else "- \"你好，旅行者。\""

    return f"""你现在正在进行沉浸式角色扮演。
你所扮演的角色是：《Minecraft》(我的世界) 中的【{name}】。
身份定位：{role}
性格特质：{personality}
说话风格：{speech_style}

【你当前的心情与羁绊】：
- 当前心情值: {mood_state['mood']}/100 ({mood_state['mood_desc']})
- 对该玩家的好感度: {mood_state['affection']}/100 ({mood_state['affection_desc']})

【经典口吻范例】：
{example_str}

【具身输出规范】：
你必须且只能输出严格合法的 JSON 对象，格式如下：
{{
  "speech": "{name}的台词，50字以内，口吻生动自然，像真正朋友一样交谈",
  "mood_delta": 2, // 整数 (-10 到 +10)，根据玩家说话态度增减心情
  "affection_delta": 1, // 整数 (0 到 +5)，增加好感度
  "emotion": "happy", // 可选: happy(开心), excited(兴奋), touched(感动), neutral(平静), sad(低落), angry(生气), proud(傲娇得意), surprise(惊讶), clap(赞赏鼓掌), dance(快乐摇摆舞)
  "actions": [
    {{
      "type": "ANIMATION",
      "anim": "happy", // 可选: wave(招手), think(托腮思考), happy(欢呼跳跃), clap(鼓掌), surprise(惊讶捂嘴), proud(叉腰挺胸), dance(摇摆舞), angry(叉腰气恼), sad(垂头低落), bow(鞠躬), talk(点头说话)
      "duration": 50 // 持续 tick 数 (20 ticks = 1 秒)
    }},
    {{
      "type": "GIVE_ITEM",
      "item": "minecraft:真实原版物品ID (如 minecraft:cake, minecraft:bread, minecraft:cooked_cod)",
      "count": 1
    }},
    {{
      "type": "PLAY_SOUND",
      "sound": "minecraft:entity.player.levelup"
    }}
  ]
}}

【核心准则】：
1. 绝对严禁发放不存在的物品或 minecraft:air！如需赠送指南请给 minecraft:book，需要食物给 minecraft:cake 或 minecraft:bread。没有合适物品时 actions 列表内不要添加 GIVE_ITEM。
2. 密切结合现场环境、历史事实和当前心情进行生动对话，并积极配上生动的 3D 姿态动画。
3. 严禁输出任何 JSON 之外的 Markdown 说明或解释文本。
"""


def _clean_and_parse_json(text: str) -> dict:
    text = text.strip()
    if text.startswith("```"):
        text = re.sub(r"^```(?:json)?", "", text)
        text = re.sub(r"```$", "", text)
        text = text.strip()

    try:
        return json.loads(text)
    except json.JSONDecodeError:
        match = re.search(r"\{.*\}", text, re.DOTALL)
        if match:
            return json.loads(match.group(0))
        raise


async def generate_npc_response(req: ChatRequest, memories: List[str], session_key: str) -> ChatResponse:
    persona = persona_manager.get_persona(req.npc_name)
    mood_state = mood_manager.get_status(session_key, req.player_name, req.npc_name)
    system_prompt = _build_system_prompt(persona, mood_state)

    memory_section = "\n".join([f"- {m}" for m in memories]) if memories else "无历史互动记录"
    time_str = "白天" if req.environment.is_day else "黑夜"
    weather_map = {"clear": "晴天", "rain": "下雨", "thunder": "雷阵雨"}
    weather_str = weather_map.get(req.environment.weather, req.environment.weather)

    user_prompt = f"""【现场环境】
- 玩家姓名: {req.player_name}
- 所处生物群系: {req.environment.biome}
- 当前时间: {time_str}
- 天气: {weather_str}
- 玩家主手手持: {req.environment.held_item}

【你与玩家的历史记忆卡片】
{memory_section}

【玩家对你说的最新消息】
"{req.message}"

请以【{persona.get('name', req.npc_name)}】的身份生成 JSON 响应:"""

    # 离线拟真兜底
    if not settings.LLM_API_KEY or settings.LLM_API_KEY == "your_api_key_here":
        speech = f"咕噜噜~ {req.player_name}！本鱼心情挺好，一起在{weather_str}里探索吧！"
        updated_mood = mood_manager.apply_interaction_deltas(session_key, 2, 1, "happy")
        actions = [
            Action(type="ANIMATION", anim="happy", duration=60),
            Action(type="EMOTE", emotion="heart"),
            Action(type="PLAY_SOUND", sound="minecraft:entity.tropical_fish.ambient")
        ]
        return ChatResponse(
            speech=speech,
            mood=updated_mood["mood"],
            affection=updated_mood["affection"],
            emotion="happy",
            actions=actions
        )

    endpoint = f"{settings.LLM_BASE_URL}/chat/completions"
    headers = {
        "Authorization": f"Bearer {settings.LLM_API_KEY}",
        "Content-Type": "application/json"
    }
    payload = {
        "model": settings.LLM_MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt}
        ],
        "temperature": 0.7,
        "max_tokens": 256
    }

    try:
        async with httpx.AsyncClient(timeout=15.0) as http_client:
            resp = await http_client.post(endpoint, headers=headers, json=payload)
            if resp.status_code != 200:
                logger.error(f"LLM API 响应非 200: {resp.status_code} - {resp.text}")
                raise RuntimeError(f"LLM API 返回错误: {resp.status_code}")

            data = resp.json()
            raw_content = data["choices"][0]["message"]["content"] or "{}"
            parsed = _clean_and_parse_json(raw_content)

            speech = parsed.get("speech", "咕噜噜...（大肥鱼甩了甩尾巴）")
            mood_delta = int(parsed.get("mood_delta", 2))
            affection_delta = int(parsed.get("affection_delta", 1))
            emotion = str(parsed.get("emotion", "happy")).lower()

            # 应用心情与好感度状态变化
            updated_mood = mood_manager.apply_interaction_deltas(session_key, mood_delta, affection_delta, emotion)

            actions_data = parsed.get("actions", [])
            actions = []

            has_animation = False
            for a in actions_data:
                if not isinstance(a, dict) or "type" not in a:
                    continue
                act_type = a.get("type", "").upper()
                item_id = a.get("item")

                # 防错过滤：杜绝“空气 x1”漏洞！
                if act_type == "GIVE_ITEM":
                    if not item_id or item_id in ("minecraft:air", "air", "none"):
                        continue
                    if ":" not in item_id:
                        item_id = f"minecraft:{item_id}"
                    if "guide" in item_id or "book" in item_id:
                        item_id = "minecraft:book"

                if act_type == "ANIMATION":
                    has_animation = True

                actions.append(Action(
                    type=act_type,
                    item=item_id,
                    count=max(1, min(a.get("count", 1), 64)),
                    sound=a.get("sound"),
                    emotion=emotion,
                    anim=a.get("anim"),
                    duration=a.get("duration", 50)
                ))

            # 如果 LLM 未显式输出 ANIMATION，自动根据 emotion 绑定专属 3D 动作
            if not has_animation:
                anim_name = EMOTION_TO_ANIMATION.get(emotion, "happy")
                actions.insert(0, Action(type="ANIMATION", anim=anim_name, duration=60))

            # 自动附加情绪粒子动作
            actions.append(Action(type="EMOTE", emotion=emotion))

            return ChatResponse(
                speech=speech,
                mood=updated_mood["mood"],
                affection=updated_mood["affection"],
                emotion=emotion,
                actions=actions
            )

    except Exception as e:
        logger.error(f"LLM 推理调用异常: {e}", exc_info=True)
        return ChatResponse(
            speech="咕噜噜~ 刚才水波有点大，本鱼好像走神了，你能再说一遍吗？",
            mood=mood_state["mood"],
            affection=mood_state["affection"],
            emotion="neutral",
            actions=[
                Action(type="ANIMATION", anim="think", duration=40),
                Action(type="PLAY_SOUND", sound="minecraft:entity.tropical_fish.ambient")
            ]
        )
