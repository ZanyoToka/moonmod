#!/usr/bin/env bash
set -e

echo "========================================================"
echo " 启动 Minecraft AI NPC 中间件服务 (FastAPI + Mem0)"
echo "========================================================"

if [ ! -d ".venv" ]; then
    echo "[INFO] 创建虚拟环境 .venv ..."
    python3 -m venv .venv
fi

echo "[INFO] 激活虚拟环境..."
source .venv/bin/activate

echo "[INFO] 安装依赖..."
pip install -r requirements.txt

echo "[INFO] 启动 Uvicorn 服务 (http://127.0.0.1:8000)..."
uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
