from typing import List, Optional
from pydantic import BaseModel, Field


class EnvironmentInfo(BaseModel):
    biome: str = Field(default="minecraft:plains", description="当前生物群系")
    is_day: bool = Field(default=True, description="当前是否为白天")
    weather: str = Field(default="clear", description="当前天气状态: clear/rain/thunder")
    held_item: str = Field(default="minecraft:air", description="玩家主手手持物品ID")


class ChatRequest(BaseModel):
    player_id: str = Field(..., description="玩家UUID")
    player_name: str = Field(..., description="玩家名称")
    npc_id: str = Field(..., description="NPC实体UUID")
    npc_name: Optional[str] = Field(default="DeepSeek大肥鱼", description="NPC实体展示名称")
    message: str = Field(..., description="玩家输入的消息")
    environment: EnvironmentInfo = Field(default_factory=EnvironmentInfo, description="环境上下文")


class Action(BaseModel):
    type: str = Field(..., description="动作类型: GIVE_ITEM, PLAY_SOUND, MOVE_TO, FOLLOW_PLAYER, EMOTE, ANIMATION")
    item: Optional[str] = Field(default=None, description="发放物品命名空间ID (如 minecraft:cake)")
    count: Optional[int] = Field(default=1, description="发放物品数量")
    sound: Optional[str] = Field(default=None, description="播放音效命名空间ID (如 minecraft:entity.player.levelup)")
    emotion: Optional[str] = Field(default="happy", description="情绪标签: happy/angry/heart/sad/excited/surprise/clap/proud/dance")
    anim: Optional[str] = Field(default=None, description="3D姿态动画名称: wave/think/happy/clap/surprise/proud/dance/angry/sad/bow/talk")
    duration: Optional[int] = Field(default=50, description="动画持续tick数 (20 ticks = 1s)")


class ChatResponse(BaseModel):
    speech: str = Field(..., description="NPC台词，50字以内，符合人设口吻")
    mood: int = Field(default=65, description="当前心情值 (0-100)")
    affection: int = Field(default=20, description="当前好感度 (0-100)")
    emotion: str = Field(default="happy", description="本次表现的情绪状态")
    actions: List[Action] = Field(default_factory=list, description="具身执行动作列表")
