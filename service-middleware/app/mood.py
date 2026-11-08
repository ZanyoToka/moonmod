from typing import Dict, Any
from app.database import db


class MoodManager:
    """管理 NPC 心情、好感度与情绪状态系统"""

    @staticmethod
    def get_mood_description(mood: int) -> str:
        if mood >= 85:
            return "极其兴奋、心情大好"
        elif mood >= 65:
            return "心情愉悦、开朗活泼"
        elif mood >= 45:
            return "平静温和"
        elif mood >= 25:
            return "略显低落、心不在焉"
        else:
            return "十分难过委屈、缺乏干劲"

    @staticmethod
    def get_affection_level(affection: int) -> str:
        if affection >= 80:
            return "Lv.4 亲密无间的挚友 (对玩家极具依赖与好感)"
        elif affection >= 50:
            return "Lv.3 信任的好友 (无话不谈)"
        elif affection >= 25:
            return "Lv.2 熟悉的朋友 (互动顺畅)"
        else:
            return "Lv.1 刚认识的新旅人 (略带生疏)"

    @classmethod
    def get_status(cls, session_key: str, player_name: str = "Player", npc_name: str = "NPC") -> Dict[str, Any]:
        state = db.get_mood_state(session_key, player_name, npc_name)
        state["mood_desc"] = cls.get_mood_description(state["mood"])
        state["affection_desc"] = cls.get_affection_level(state["affection"])
        return state

    @classmethod
    def apply_interaction_deltas(cls, session_key: str, mood_delta: int, affection_delta: int, emotion: str) -> Dict[str, Any]:
        updated = db.update_mood_state(session_key, mood_delta, affection_delta, emotion)
        updated["mood_desc"] = cls.get_mood_description(updated["mood"])
        updated["affection_desc"] = cls.get_affection_level(updated["affection"])
        return updated


mood_manager = MoodManager()
