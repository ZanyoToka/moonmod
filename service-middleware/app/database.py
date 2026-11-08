import sqlite3
from pathlib import Path
from typing import List, Dict, Any, Optional

DB_DIR = Path(__file__).resolve().parent.parent / "data"
DB_PATH = DB_DIR / "ai_npc.db"


class Database:
    """持久化 SQLite 高性能数据库引擎，统一管理长期记忆、对话历史与实体心情好感度"""

    def __init__(self):
        DB_DIR.mkdir(parents=True, exist_ok=True)
        self.db_path = DB_PATH
        self._init_tables()

    def _get_connection(self):
        conn = sqlite3.connect(self.db_path, timeout=10.0, check_same_thread=False)
        conn.row_factory = sqlite3.Row
        # 开启 WAL 预写日志与高性能缓存配置
        conn.execute("PRAGMA journal_mode=WAL;")
        conn.execute("PRAGMA synchronous=NORMAL;")
        conn.execute("PRAGMA cache_size=10000;")
        return conn

    def _init_tables(self):
        with self._get_connection() as conn:
            # 1. 记忆事实表 (Memories)
            conn.execute("""
            CREATE TABLE IF NOT EXISTS memories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_key TEXT NOT NULL,
                player_name TEXT NOT NULL,
                npc_name TEXT NOT NULL,
                fact TEXT NOT NULL,
                category TEXT DEFAULT 'general',
                importance INTEGER DEFAULT 1,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """)

            # 2. 对话记录表 (Conversations)
            conn.execute("""
            CREATE TABLE IF NOT EXISTS conversations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_key TEXT NOT NULL,
                player_name TEXT NOT NULL,
                npc_name TEXT NOT NULL,
                player_message TEXT NOT NULL,
                npc_speech TEXT NOT NULL,
                emotion TEXT DEFAULT 'neutral',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """)

            # 3. 关系与心情状态表 (Mood & Relationships)
            conn.execute("""
            CREATE TABLE IF NOT EXISTS mood_state (
                session_key TEXT PRIMARY KEY,
                player_name TEXT NOT NULL,
                npc_name TEXT NOT NULL,
                mood INTEGER DEFAULT 60,
                affection INTEGER DEFAULT 20,
                current_emotion TEXT DEFAULT 'happy',
                interaction_count INTEGER DEFAULT 0,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """)

            # 高性能索引加速会话查询与事实检索
            conn.execute("CREATE INDEX IF NOT EXISTS idx_mem_session ON memories(session_key, importance DESC);")
            conn.execute("CREATE INDEX IF NOT EXISTS idx_conv_session ON conversations(session_key, id DESC);")
            conn.commit()

    # ================= 记忆管理 API =================
    def add_memory(self, session_key: str, player_name: str, npc_name: str, fact: str, category: str = "general", importance: int = 1):
        with self._get_connection() as conn:
            conn.execute("""
            INSERT INTO memories (session_key, player_name, npc_name, fact, category, importance)
            VALUES (?, ?, ?, ?, ?, ?)
            """, (session_key, player_name, npc_name, fact, category, importance))
            conn.commit()

    def get_memories(self, session_key: str, query: str = "", limit: int = 5) -> List[Dict[str, Any]]:
        with self._get_connection() as conn:
            if query:
                keywords = [k.strip() for k in query.split() if len(k.strip()) > 1]
                if keywords:
                    like_clauses = " OR ".join(["fact LIKE ?" for _ in keywords])
                    params = [f"%{k}%" for k in keywords] + [session_key, limit]
                    cursor = conn.execute(f"""
                    SELECT id, fact, category, importance, created_at FROM memories
                    WHERE ({like_clauses}) AND session_key = ?
                    ORDER BY importance DESC, id DESC LIMIT ?
                    """, params)
                    rows = cursor.fetchall()
                    if rows:
                        return [dict(r) for r in rows]

            cursor = conn.execute("""
            SELECT id, fact, category, importance, created_at FROM memories
            WHERE session_key = ?
            ORDER BY importance DESC, id DESC LIMIT ?
            """, (session_key, limit))
            return [dict(r) for r in cursor.fetchall()]

    def get_all_memories(self) -> List[Dict[str, Any]]:
        with self._get_connection() as conn:
            cursor = conn.execute("SELECT * FROM memories ORDER BY id DESC")
            return [dict(r) for r in cursor.fetchall()]

    def delete_memory(self, memory_id: int):
        with self._get_connection() as conn:
            conn.execute("DELETE FROM memories WHERE id = ?", (memory_id,))
            conn.commit()

    def reset_memories(self, session_key: Optional[str] = None):
        """重置记忆库 (支持清除指定会话或全部清空)"""
        with self._get_connection() as conn:
            if session_key:
                conn.execute("DELETE FROM memories WHERE session_key = ?", (session_key,))
                conn.execute("DELETE FROM conversations WHERE session_key = ?", (session_key,))
            else:
                conn.execute("DELETE FROM memories")
                conn.execute("DELETE FROM conversations")
            conn.commit()

    # ================= 对话历史记录 API =================
    def add_conversation(self, session_key: str, player_name: str, npc_name: str, player_msg: str, npc_speech: str, emotion: str):
        with self._get_connection() as conn:
            conn.execute("""
            INSERT INTO conversations (session_key, player_name, npc_name, player_message, npc_speech, emotion)
            VALUES (?, ?, ?, ?, ?, ?)
            """, (session_key, player_name, npc_name, player_msg, npc_speech, emotion))
            conn.commit()

    def get_recent_conversations(self, session_key: str, limit: int = 10) -> List[Dict[str, Any]]:
        with self._get_connection() as conn:
            cursor = conn.execute("""
            SELECT player_message, npc_speech, emotion, created_at FROM conversations
            WHERE session_key = ? ORDER BY id DESC LIMIT ?
            """, (session_key, limit))
            rows = cursor.fetchall()
            return [dict(r) for r in reversed(rows)]

    # ================= 心情与好感度状态 API =================
    def get_mood_state(self, session_key: str, player_name: str = "Player", npc_name: str = "NPC") -> Dict[str, Any]:
        with self._get_connection() as conn:
            cursor = conn.execute("SELECT * FROM mood_state WHERE session_key = ?", (session_key,))
            row = cursor.fetchone()
            if row:
                return dict(row)
            # 初始化默认好感度与心情
            conn.execute("""
            INSERT INTO mood_state (session_key, player_name, npc_name, mood, affection, current_emotion, interaction_count)
            VALUES (?, ?, ?, 65, 20, 'happy', 0)
            """, (session_key, player_name, npc_name))
            conn.commit()
            return {
                "session_key": session_key,
                "player_name": player_name,
                "npc_name": npc_name,
                "mood": 65,
                "affection": 20,
                "current_emotion": "happy",
                "interaction_count": 0
            }

    def update_mood_state(self, session_key: str, mood_delta: int, affection_delta: int, emotion: str):
        current = self.get_mood_state(session_key)
        new_mood = max(0, min(100, current["mood"] + mood_delta))
        new_affection = max(0, min(100, current["affection"] + affection_delta))
        new_count = current["interaction_count"] + 1

        with self._get_connection() as conn:
            conn.execute("""
            UPDATE mood_state
            SET mood = ?, affection = ?, current_emotion = ?, interaction_count = ?, updated_at = CURRENT_TIMESTAMP
            WHERE session_key = ?
            """, (new_mood, new_affection, emotion, new_count, session_key))
            conn.commit()

        return {
            "mood": new_mood,
            "affection": new_affection,
            "current_emotion": emotion,
            "interaction_count": new_count
        }

    def reset_mood_state(self, session_key: Optional[str] = None):
        with self._get_connection() as conn:
            if session_key:
                conn.execute("UPDATE mood_state SET mood = 65, affection = 20, current_emotion = 'happy' WHERE session_key = ?", (session_key,))
            else:
                conn.execute("UPDATE mood_state SET mood = 65, affection = 20, current_emotion = 'happy'")
            conn.commit()

    # ================= 任务调度与随身行囊 API =================
    def get_task_mode(self, npc_id: str = "DeepSeek大肥鱼") -> str:
        with self._get_connection() as conn:
            conn.execute("""
            CREATE TABLE IF NOT EXISTS npc_runtime (
                npc_id TEXT PRIMARY KEY,
                task_mode TEXT DEFAULT 'idle',
                last_action TEXT DEFAULT '待命',
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """)
            cursor = conn.cursor()
            cursor.execute("SELECT task_mode FROM npc_runtime WHERE npc_id = ?", (npc_id,))
            row = cursor.fetchone()
            if row:
                return row["task_mode"]
            conn.execute("INSERT OR IGNORE INTO npc_runtime (npc_id, task_mode) VALUES (?, 'idle')", (npc_id,))
            conn.commit()
            return "idle"

    def set_task_mode(self, task_mode: str, npc_id: str = "DeepSeek大肥鱼"):
        with self._get_connection() as conn:
            conn.execute("""
            INSERT INTO npc_runtime (npc_id, task_mode, updated_at)
            VALUES (?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(npc_id) DO UPDATE SET task_mode = excluded.task_mode, updated_at = CURRENT_TIMESTAMP
            """, (npc_id, task_mode))
            conn.commit()

    def get_inventory(self, npc_id: str = "DeepSeek大肥鱼") -> List[Dict[str, Any]]:
        with self._get_connection() as conn:
            conn.execute("""
            CREATE TABLE IF NOT EXISTS npc_inventory_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                npc_id TEXT NOT NULL,
                slot INTEGER NOT NULL,
                item_id TEXT NOT NULL,
                item_name TEXT NOT NULL,
                count INTEGER NOT NULL,
                slot_type TEXT DEFAULT 'backpack',
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """)
            cursor = conn.cursor()
            cursor.execute("SELECT slot, item_id, item_name, count, slot_type FROM npc_inventory_items WHERE npc_id = ? ORDER BY slot ASC", (npc_id,))
            rows = [dict(r) for r in cursor.fetchall()]
            if not rows:
                # 预置初始随身行囊与装备
                defaults = [
                    (npc_id, 0, "minecraft:iron_sword", "铁剑", 1, "mainhand"),
                    (npc_id, 1, "minecraft:shield", "盾牌", 1, "offhand"),
                    (npc_id, 2, "minecraft:iron_helmet", "铁头盔", 1, "armor_head"),
                    (npc_id, 3, "minecraft:iron_chestplate", "铁胸甲", 1, "armor_chest"),
                    (npc_id, 6, "minecraft:bread", "面包", 16, "backpack"),
                    (npc_id, 7, "minecraft:cake", "小蛋糕", 2, "backpack"),
                    (npc_id, 8, "minecraft:wheat_seeds", "小麦种子", 32, "backpack"),
                    (npc_id, 9, "minecraft:oak_log", "橡木原木", 12, "backpack")
                ]
                conn.executemany("""
                INSERT INTO npc_inventory_items (npc_id, slot, item_id, item_name, count, slot_type)
                VALUES (?, ?, ?, ?, ?, ?)
                """, defaults)
                conn.commit()
                cursor.execute("SELECT slot, item_id, item_name, count, slot_type FROM npc_inventory_items WHERE npc_id = ? ORDER BY slot ASC", (npc_id,))
                rows = [dict(r) for r in cursor.fetchall()]
            return rows

    def add_inventory_item(self, item_id: str, item_name: str, count: int, npc_id: str = "DeepSeek大肥鱼"):
        with self._get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT id, slot, count FROM npc_inventory_items WHERE npc_id = ? AND item_id = ? AND slot >= 6 LIMIT 1", (npc_id, item_id))
            row = cursor.fetchone()
            if row:
                conn.execute("UPDATE npc_inventory_items SET count = count + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", (count, row["id"]))
            else:
                cursor.execute("SELECT slot FROM npc_inventory_items WHERE npc_id = ? AND slot >= 6", (npc_id,))
                used_slots = {r["slot"] for r in cursor.fetchall()}
                target_slot = 6
                for s in range(6, 24):
                    if s not in used_slots:
                        target_slot = s
                        break
                conn.execute("""
                INSERT INTO npc_inventory_items (npc_id, slot, item_id, item_name, count, slot_type)
                VALUES (?, ?, ?, ?, ?, 'backpack')
                """, (npc_id, target_slot, item_id, item_name, count))
            conn.commit()


db = Database()
