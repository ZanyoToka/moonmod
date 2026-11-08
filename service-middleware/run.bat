@echo off
chcp 65001 >nul
echo ========================================================
echo  启动 Minecraft AI NPC 中间件服务 (FastAPI + Mem0)
echo ========================================================

if not exist .venv (
    echo [INFO] 未检测到虚拟环境，正在创建虚拟环境 .venv ...
    python -m venv .venv
)

echo [INFO] 激活虚拟环境...
if exist .venv\Scripts\activate.bat (
    call .venv\Scripts\activate.bat
) else if exist .venv\bin\activate.bat (
    call .venv\bin\activate.bat
)

echo [INFO] 检查并安装依赖...
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

echo [INFO] 启动 Uvicorn 服务 (http://127.0.0.1:8000)...
uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
pause
