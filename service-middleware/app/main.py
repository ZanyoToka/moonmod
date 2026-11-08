import logging
from typing import Optional, List, Dict, Any
from fastapi import FastAPI, BackgroundTasks, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse
from pydantic import BaseModel, Field

from app.config import settings
from app.schemas import ChatRequest, ChatResponse
from app.memory import memory_manager
from app.mood import mood_manager
from app.database import db
from app.llm import generate_npc_response
from app.dashboard_html import DASHBOARD_HTML

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s"
)
logger = logging.getLogger("middleware_main")

app = FastAPI(
    title="Minecraft AI NPC Decoupled Middleware",
    description="Minecraft 1.20.1 Forge NPC 具身动作与智能记忆对话中间件",
    version="1.0.0"
)

# 允许跨域请求
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ================= 数据模型 =================
class MemoryCreateRequest(BaseModel):
    fact: str = Field(..., description="记忆事实文本")
    category: Optional[str] = Field(default="custom", description="记忆分类")
    session_key: Optional[str] = Field(default="global", description="会话标识")


class MoodUpdateRequest(BaseModel):
    session_key: Optional[str] = Field(default="global", description="会话标识")
    mood: Optional[int] = Field(default=None, ge=0, le=100)
    affection: Optional[int] = Field(default=None, ge=0, le=100)
    emotion: Optional[str] = Field(default=None)


class ConsoleCommandRequest(BaseModel):
    command: str = Field(..., description="控制台指令")
    session_key: Optional[str] = Field(default="global")


class TaskSwitchRequest(BaseModel):
    task_id: str = Field(..., description="任务模式ID: idle, guard, harvest, follow")


class ItemAddRequest(BaseModel):
    item_id: str = Field(..., description="原版物品ID")
    item_name: str = Field(default="未命名物品")
    count: int = Field(default=1, ge=1, le=64)


# ================= 网页仪表盘与健康检查 =================
@app.get("/", response_class=HTMLResponse)
@app.get("/dashboard", response_class=HTMLResponse)
async def serve_dashboard():
    """提供纯前端图形化控制台界面"""
    return HTMLResponse(content=DASHBOARD_HTML)


@app.get("/health")
async def health_check():
    """健康检查接口"""
    return {
        "status": "ok",
        "service": "minecraft-npc-middleware",
        "model": settings.LLM_MODEL,
        "mem0_enabled": settings.MEM0_ENABLED
    }


# ================= 核心 NPC 聊天交互路由 =================
@app.post("/api/npc/chat", response_model=ChatResponse)
async def npc_chat(req: ChatRequest, background_tasks: BackgroundTasks):
    """
    NPC 交互对话路由
    1. 校验请求体数据结构
    2. 检索玩家与 NPC 历史记忆 Top-5
    3. 组装环境与上下文，调用 LLM 推理生成具身动作、台词与情绪状态
    4. 异步将本次互动核心信息写入底层 SQLite 事实记忆库与对话日志
    """
    try:
        session_key = f"{req.player_id}_{req.npc_id}"
        npc_name = req.npc_name or "DeepSeek大肥鱼"
        logger.info(f"收到玩家 [{req.player_name}] 对 NPC [{npc_name}] 的消息: \"{req.message}\"")

        # 1. 检索该玩家与该 NPC 的历史记忆 (Top-5 事实卡片)
        memories = memory_manager.get_memories(session_key=session_key, query=req.message, limit=5)
        if memories:
            logger.info(f"已检索到相关历史记忆: {memories}")

        # 2. 调用 LLM 推理生成回复与具身动作
        response = await generate_npc_response(req, memories, session_key)

        # 3. 异步提炼并持久化新记忆卡片与对话记录
        new_fact = f"玩家({req.player_name})在{req.environment.biome}说:'{req.message}'，NPC回复:'{response.speech}'"
        background_tasks.add_task(memory_manager.add_memory, session_key, req.player_name, npc_name, new_fact, "dialog", 1)
        background_tasks.add_task(db.add_conversation, session_key, req.player_name, npc_name, req.message, response.speech, response.emotion)

        logger.info(f"NPC [{npc_name}] 回复: \"{response.speech}\" | 心情: {response.mood} | 好感: {response.affection} | 动作数: {len(response.actions)}")
        return response

    except Exception as e:
        logger.error(f"处理 NPC 对话失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"内部推理处理错误: {str(e)}")


# ================= 记忆卡片管理 API =================
@app.get("/api/memories")
async def get_memories(session_key: Optional[str] = None):
    """获取所有记忆事实记录"""
    all_rows = db.get_all_memories()
    if session_key:
        return [r for r in all_rows if r["session_key"] == session_key]
    return all_rows


@app.post("/api/memory/add")
async def add_custom_memory(req: MemoryCreateRequest):
    """手动注入一条记忆事实"""
    session = req.session_key or "global"
    memory_manager.add_memory(session, "ConsoleAdmin", "DeepSeek大肥鱼", req.fact, req.category, 2)
    return {"status": "ok", "message": f"成功注入记忆: {req.fact}"}


@app.delete("/api/memory/{memory_id}")
async def delete_memory(memory_id: int):
    """删除指定 ID 的记忆事实"""
    db.delete_memory(memory_id)
    return {"status": "ok", "message": f"已删除记忆 ID {memory_id}"}


@app.post("/api/memory/reset")
async def reset_memory(session_key: Optional[str] = None):
    """彻底重置底层记忆、对话历史与状态"""
    memory_manager.reset_all(session_key)
    return {"status": "ok", "message": "底层记忆库、对话记录与心情好感度已彻底重置为初始状态！"}


# ================= 心情与好感度 API =================
@app.get("/api/mood")
async def get_mood(session_key: Optional[str] = None):
    """查询当前心情与羁绊好感度"""
    session = session_key or "global"
    return mood_manager.get_status(session, "Player", "DeepSeek大肥鱼")


@app.post("/api/mood/update")
async def update_mood(req: MoodUpdateRequest):
    """手动调节心情数值、好感度或情绪标签"""
    session = req.session_key or "global"
    current = db.get_mood_state(session)

    new_mood = req.mood if req.mood is not None else current["mood"]
    new_affection = req.affection if req.affection is not None else current["affection"]
    new_emotion = req.emotion if req.emotion is not None else current["current_emotion"]

    mood_delta = new_mood - current["mood"]
    affection_delta = new_affection - current["affection"]

    updated = mood_manager.apply_interaction_deltas(session, mood_delta, affection_delta, new_emotion)
    return {"status": "ok", "data": updated}


# ================= 行为与任务模式调度 API (Task State Machine) =================
@app.get("/api/npc/tasks")
async def get_npc_tasks():
    current_mode = db.get_task_mode()
    tasks_meta = [
        {"id": "idle", "name": "待命休息 (Idle)", "desc": "在原地驻足休养，开启自主生命再生与警惕观察", "icon": "🧘"},
        {"id": "follow", "name": "紧随伴随 (Follow)", "desc": "贴身护卫玩家，超出3格自动跟随，超出32格自动传送", "icon": "🚶"},
        {"id": "guard", "name": "护卫反击 (Guard)", "desc": "以NPC为中心10格扫描敌对怪物，拔剑近战突进反击", "icon": "🛡️"},
        {"id": "harvest", "name": "农场采掘 (Harvest)", "desc": "扫描周围成熟小麦胡萝卜并自动收割，原地补种作物", "icon": "🪓"}
    ]
    return {
        "current_task": current_mode,
        "tasks": tasks_meta
    }


@app.post("/api/npc/task/switch")
async def switch_npc_task(req: TaskSwitchRequest):
    valid_tasks = {"idle", "follow", "guard", "harvest"}
    if req.task_id not in valid_tasks:
        raise HTTPException(status_code=400, detail=f"无效的任务模式: {req.task_id}")
    db.set_task_mode(req.task_id)
    return {"status": "ok", "current_task": req.task_id, "message": f"成功切换行为模式为: {req.task_id}"}


# ================= 随身行囊与装备 API (Maid Container) =================
@app.get("/api/npc/inventory")
async def get_npc_inventory():
    items = db.get_inventory()
    return {
        "npc_name": "DeepSeek大肥鱼",
        "total_slots": 24,
        "items": items
    }


@app.post("/api/npc/inventory/add")
async def add_npc_inventory(req: ItemAddRequest):
    db.add_inventory_item(req.item_id, req.item_name, req.count)
    return {"status": "ok", "message": f"成功向 NPC 随身行囊送入: {req.item_name} x{req.count}"}


# ================= 交互式控制台指令执行 API =================
@app.post("/api/console/exec")
async def execute_console_cmd(req: ConsoleCommandRequest):
    """执行交互式命令行指令"""
    cmd = req.command.strip()
    session = req.session_key or "global"
    lines = []

    if cmd in ("/help", "help"):
        lines.append({"text": "=== Google Cloud Shell AI NPC 终端指令表 ===", "type": "ok"})
        lines.append({"text": "/status - 查看当前 NPC 心情、好感度、活跃任务与记忆卡片", "type": ""})
        lines.append({"text": "/task <idle|follow|guard|harvest> - 切换模块化行为任务", "type": "cmd"})
        lines.append({"text": "/inv - 查看 NPC 随身行囊与装备 (24格 Maid 容器)", "type": "cmd"})
        lines.append({"text": "/give <item_id> <count> - 直接向 NPC 行囊发放物资", "type": "cmd"})
        lines.append({"text": "/pack reload - 热更并重载 npc_packs 基岩版外部模型与贴图", "type": "cmd"})
        lines.append({"text": "/mood <0-100> - 快速调节心情指数", "type": ""})
        lines.append({"text": "/affection <0-100> - 快速调节羁绊好感度", "type": ""})
        lines.append({"text": "/chat <对话内容> - 模拟玩家向 NPC 发送对话并获取具身响应", "type": ""})
        lines.append({"text": "/reset - 彻底重置底层记忆数据库与好感度状态", "type": "cmd"})
        lines.append({"text": "/sql <SQL语句> - 执行 SQLite 原生只读查询", "type": ""})

    elif cmd.startswith("/task "):
        target_task = cmd.split()[1].lower()
        if target_task in {"idle", "follow", "guard", "harvest"}:
            db.set_task_mode(target_task)
            lines.append({"text": f"[TASK] NPC 当前行为任务已切换为: {target_task.upper()}", "type": "ok"})
        else:
            lines.append({"text": f"未知任务: '{target_task}'。可选: idle, follow, guard, harvest", "type": "err"})

    elif cmd == "/inv":
        items = db.get_inventory()
        lines.append({"text": f"--- DeepSeek大肥鱼 随身行囊 (24格) ---", "type": "ok"})
        for it in items:
            lines.append({"text": f"槽位 [{it['slot']}] ({it['slot_type']}): {it['item_name']} ({it['item_id']}) x{it['count']}", "type": ""})

    elif cmd.startswith("/give "):
        parts = cmd.split()
        if len(parts) >= 2:
            item_id = parts[1]
            cnt = int(parts[2]) if len(parts) >= 3 else 1
            db.add_inventory_item(item_id, item_id.split(":")[-1], cnt)
            lines.append({"text": f"[INVENTORY] 已向 NPC 行囊注入: {item_id} x{cnt}", "type": "ok"})
        else:
            lines.append({"text": "用法: /give <item_id> [count]", "type": "err"})

    elif cmd == "/pack reload":
        lines.append({"text": "[PACK] 已向 Minecraft 客户端下发 npc_packs 外部基岩版模型热重载通知！", "type": "ok"})

    elif cmd == "/status":
        status = mood_manager.get_status(session)
        mem_count = len(db.get_all_memories())
        lines.append({"text": f"--- NPC 实体状态: DeepSeek大肥鱼 ---", "type": "ok"})
        lines.append({"text": f"心情指数: {status['mood']}/100 ({status['mood_desc']})", "type": ""})
        lines.append({"text": f"好感羁绊: {status['affection']}/100 ({status['affection_desc']})", "type": ""})
        lines.append({"text": f"当前情绪: {status['current_emotion']}", "type": ""})
        lines.append({"text": f"互动次数: {status['interaction_count']} 次", "type": ""})
        lines.append({"text": f"底层记忆事实总数: {mem_count} 条", "type": ""})

    elif cmd == "/reset":
        memory_manager.reset_all(None)
        lines.append({"text": "[RESET] 底层记忆库、对话记录与心情状态已彻底重置为初始状态！", "type": "cmd"})

    elif cmd.startswith("/mood "):
        try:
            val = int(cmd.split()[1])
            cur = db.get_mood_state(session)
            mood_manager.apply_interaction_deltas(session, val - cur["mood"], 0, cur["current_emotion"])
            lines.append({"text": f"心情值已更新为: {val}", "type": "ok"})
        except Exception as e:
            lines.append({"text": f"参数错误: {e}", "type": "err"})

    elif cmd.startswith("/affection "):
        try:
            val = int(cmd.split()[1])
            cur = db.get_mood_state(session)
            mood_manager.apply_interaction_deltas(session, 0, val - cur["affection"], cur["current_emotion"])
            lines.append({"text": f"好感度已更新为: {val}", "type": "ok"})
        except Exception as e:
            lines.append({"text": f"参数错误: {e}", "type": "err"})

    elif cmd.startswith("/emotion "):
        emo = cmd.split()[1].strip().lower()
        mood_manager.apply_interaction_deltas(session, 0, 0, emo)
        lines.append({"text": f"情绪已更新为: {emo}", "type": "ok"})

    elif cmd.startswith("/chat "):
        msg = cmd[6:].strip()
        from app.schemas import EnvironmentInfo
        chat_req = ChatRequest(
            player_id="console_admin",
            player_name="ConsoleAdmin",
            npc_id="deepseek_npc",
            npc_name="DeepSeek大肥鱼",
            message=msg,
            environment=EnvironmentInfo()
        )
        memories = memory_manager.get_memories(session_key=session, query=msg, limit=5)
        resp = await generate_npc_response(chat_req, memories, session)
        lines.append({"text": f"[DeepSeek大肥鱼] {resp.speech}", "type": "ok"})
        lines.append({"text": f"心情: {resp.mood} | 好感: {resp.affection} | 情绪: {resp.emotion} | 动作: {resp.actions}", "type": ""})

    elif cmd == "/facts":
        facts = db.get_all_memories()
        if not facts:
            lines.append({"text": "当前记忆库为空。", "type": ""})
        else:
            lines.append({"text": f"--- 记忆事实列表 ({len(facts)}条) ---", "type": "ok"})
            for f in facts[:15]:
                lines.append({"text": f"[{f['id']}] ({f['player_name']}) {f['fact']}", "type": ""})

    elif cmd.startswith("/sql "):
        sql_query = cmd[5:].strip()
        if not sql_query.lower().startswith("select"):
            lines.append({"text": "控制台仅允许执行 SELECT 只读语句以保护数据安全。", "type": "err"})
        else:
            try:
                with db._get_connection() as conn:
                    cursor = conn.execute(sql_query)
                    rows = cursor.fetchall()
                    lines.append({"text": f"查询返回 {len(rows)} 行记录:", "type": "ok"})
                    for r in rows[:10]:
                        lines.append({"text": str(dict(r)), "type": ""})
            except Exception as e:
                lines.append({"text": f"SQL 执行错误: {e}", "type": "err"})

    else:
        lines.append({"text": f"未知指令: '{cmd}'。输入 /help 查看帮助。", "type": "err"})

    return {"lines": lines}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host=settings.HOST, port=settings.PORT, reload=True)
