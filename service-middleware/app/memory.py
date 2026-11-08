import logging
from typing import List, Dict, Any, Optional
from app.config import settings
from app.database import db

logger = logging.getLogger("npc_memory")


class MemoryManager:
    """底层稳固的长期记忆管理系统（基于 SQLite 持久化与情境检索）"""

    def __init__(self):
        self.mem0 = None
        if settings.MEM0_ENABLED and settings.LLM_API_KEY != "your_api_key_here":
            self._init_mem0()

    def _init_mem0(self):
        try:
            from mem0 import Memory
            mem0_config = {
                "llm": {
                    "provider": "openai",
                    "config": {
                        "model": settings.LLM_MODEL,
                        "api_key": settings.LLM_API_KEY,
                        "openai_base_url": settings.LLM_BASE_URL
                    }
                },
                "version": "v1.1"
            }
            self.mem0 = Memory.from_config(mem0_config)
            logger.info("Mem0 向量记忆扩展系统初始化成功。")
        except Exception as e:
            logger.info(f"Mem0 未开启或未安装，使用持久化 SQLite 长期事实记忆引擎: {e}")
            self.mem0 = None

    def get_memories(self, session_key: str, query: str = "", limit: int = 5) -> List[str]:
        """检索该玩家与该 NPC 之间的历史事实卡片"""
        # 1. 如果 Mem0 可用，先检索向量记忆
        memories = []
        if self.mem0:
            try:
                results = self.mem0.search(query=query, user_id=session_key, limit=limit)
                items = results.get("results", []) if isinstance(results, dict) else results
                for item in items:
                    if isinstance(item, dict) and "memory" in item:
                        memories.append(item["memory"])
                    elif isinstance(item, str):
                        memories.append(item)
            except Exception as e:
                logger.debug(f"Mem0 检索降级: {e}")

        # 2. 从本地 SQLite 数据库中检索结构化事实（结合关键词和时间顺序）
        sql_memories = db.get_memories(session_key=session_key, query=query, limit=limit)
        for m in sql_memories:
            fact = m["fact"]
            if fact not in memories:
                memories.append(fact)

        return memories[:limit]

    def add_memory(self, session_key: str, player_name: str, npc_name: str, fact: str, category: str = "general", importance: int = 1):
        """将提炼的新事实异步存入数据库与向量库"""
        # 写入本地 SQLite 数据库
        db.add_memory(session_key, player_name, npc_name, fact, category, importance)

        # 写入 Mem0 向量库 (若启用)
        if self.mem0:
            try:
                self.mem0.add(fact, user_id=session_key)
            except Exception as e:
                logger.debug(f"Mem0 异步同步跳过: {e}")

        logger.info(f"成功持久化记忆事实 (会话: {session_key}): {fact}")

    def reset_all(self, session_key: Optional[str] = None):
        """重置记忆与对话历史"""
        db.reset_memories(session_key)
        db.reset_mood_state(session_key)
        if self.mem0:
            try:
                if session_key:
                    self.mem0.delete_all(user_id=session_key)
                else:
                    self.mem0.reset()
            except Exception as e:
                logger.debug(f"Mem0 重置: {e}")
        logger.info(f"已重置记忆状态 (会话: {session_key if session_key else '全部'})")


memory_manager = MemoryManager()
