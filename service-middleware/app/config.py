import os
from dotenv import load_dotenv

# 加载 .env 环境变量文件
load_dotenv()


class Settings:
    # LLM Settings (兼容 OpenAI 协议)
    LLM_API_KEY: str = os.getenv("LLM_API_KEY", "your_api_key_here")
    LLM_BASE_URL: str = os.getenv("LLM_BASE_URL", "https://api.deepseek.com/v1").rstrip("/")
    LLM_MODEL: str = os.getenv("LLM_MODEL", "deepseek-chat")

    # 服务网络监听配置
    HOST: str = os.getenv("HOST", "127.0.0.1")
    PORT: int = int(os.getenv("PORT", "8000"))

    # 记忆管理 (Mem0) 配置
    MEM0_ENABLED: bool = os.getenv("MEM0_ENABLED", "true").lower() in ("true", "1", "yes")


settings = Settings()
