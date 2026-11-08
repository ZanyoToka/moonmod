import json
import urllib.request
import urllib.error
import time

URL = "http://127.0.0.1:8000/api/npc/chat"

payload_1 = {
    "player_id": "11111111-2222-3333-4444-555555555555",
    "player_name": "Steve",
    "npc_id": "99999999-8888-7777-6666-555555555555",
    "message": "你好啊，村民！我刚刚在下雪的平原上迷路了，肚子好饿，手里只有一块生铁，能给我点吃的吗？",
    "environment": {
        "biome": "minecraft:snowy_plains",
        "is_day": False,
        "weather": "rain",
        "held_item": "minecraft:raw_iron"
    }
}

payload_2 = {
    "player_id": "11111111-2222-3333-4444-555555555555",
    "player_name": "Steve",
    "npc_id": "99999999-8888-7777-6666-555555555555",
    "message": "还记得我刚刚给你的问候吗？你觉得雪原的夜晚冷吗？",
    "environment": {
        "biome": "minecraft:snowy_plains",
        "is_day": False,
        "weather": "rain",
        "held_item": "minecraft:bread"
    }
}


def send_chat(payload, round_name):
    print(f"\n==================== 正在测试 [{round_name}] ====================")
    print(f"玩家说: {payload['message']}")
    print(f"环境信息: {payload['environment']}")
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        URL,
        data=data,
        headers={"Content-Type": "application/json"}
    )
    try:
        t0 = time.time()
        with urllib.request.urlopen(req, timeout=10) as response:
            status = response.getcode()
            body = response.read().decode("utf-8")
            elapsed = time.time() - t0
            print(f"响应状态码: {status} (耗时: {elapsed:.2f}s)")
            print("回传 JSON:")
            print(json.dumps(json.loads(body), indent=2, ensure_ascii=False))
    except urllib.error.URLError as e:
        print(f"请求失败: {e}")
        print("请确认中间件服务是否已在 127.0.0.1:8000 启动。")


if __name__ == "__main__":
    print("AI NPC 中间件端到端连通性测试脚本")
    send_chat(payload_1, "第 1 轮对话 (初识与索要食物)")
    print("\n等待 2 秒以便后台记忆持久化任务完成...")
    time.sleep(2)
    send_chat(payload_2, "第 2 轮对话 (检验记忆召回)")
