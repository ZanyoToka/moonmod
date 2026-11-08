DASHBOARD_HTML = """<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Google Cloud Console - DeepSeek AI NPC 控制台</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Google+Sans:wght@400;500;700&family=Roboto+Mono:wght@400;500&family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
  <style>
    :root {
      --g-bg: #131314;
      --g-surface: #1e1f20;
      --g-surface-elevated: #28292a;
      --g-surface-hover: #333537;
      --g-outline: #444746;
      --g-outline-subtle: #2d3032;
      --g-text: #e3e3e3;
      --g-text-secondary: #c4c7c5;
      --g-text-tertiary: #8e918f;
      
      --google-blue: #8ab4f8;
      --google-blue-container: rgba(138, 180, 248, 0.16);
      --google-green: #81c995;
      --google-green-container: rgba(129, 201, 149, 0.16);
      --google-yellow: #fdd663;
      --google-yellow-container: rgba(253, 214, 99, 0.16);
      --google-red: #f28b82;
      --google-red-container: rgba(242, 139, 130, 0.16);
      
      --radius-sm: 8px;
      --radius-md: 16px;
      --radius-lg: 24px;
      --radius-full: 9999px;
    }

    * { box-sizing: border-box; margin: 0; padding: 0; }
    
    body {
      background: var(--g-bg);
      color: var(--g-text);
      font-family: 'Google Sans', 'Roboto', -apple-system, BlinkMacSystemFont, sans-serif;
      line-height: 1.5;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    /* 顶部导航栏 - Google Cloud 风格 */
    header {
      background: var(--g-surface);
      border-bottom: 1px solid var(--g-outline-subtle);
      padding: 12px 24px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      position: sticky;
      top: 0;
      z-index: 50;
    }

    .brand-section {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .google-logo {
      font-size: 20px;
      font-weight: 500;
      letter-spacing: -0.5px;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .google-dot-group {
      display: inline-flex;
      gap: 3px;
    }
    .g-dot { width: 8px; height: 8px; border-radius: 50%; }
    .dot-blue { background: #4285F4; }
    .dot-red { background: #EA4335; }
    .dot-yellow { background: #FBBC04; }
    .dot-green { background: #34A853; }

    .project-pill {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: var(--g-surface-elevated);
      border: 1px solid var(--g-outline);
      padding: 6px 14px;
      border-radius: var(--radius-full);
      font-size: 13px;
      color: var(--g-text);
    }

    .status-pill {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: var(--google-green-container);
      color: var(--google-green);
      border: 1px solid rgba(129, 201, 149, 0.3);
      padding: 6px 14px;
      border-radius: var(--radius-full);
      font-size: 12px;
      font-weight: 500;
    }
    .pulse-dot {
      width: 8px;
      height: 8px;
      background: var(--google-green);
      border-radius: 50%;
      box-shadow: 0 0 8px var(--google-green);
      animation: pulse 2s infinite;
    }
    @keyframes pulse {
      0%, 100% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.4; transform: scale(0.85); }
    }

    /* 主布局容器 */
    .main-wrapper {
      max-width: 1380px;
      width: 100%;
      margin: 0 auto;
      padding: 24px;
      display: flex;
      flex-direction: column;
      gap: 20px;
      flex: 1;
    }

    /* Google 风格胶囊 Tab 导航 */
    .tab-bar {
      display: flex;
      gap: 8px;
      background: var(--g-surface);
      padding: 6px;
      border-radius: var(--radius-full);
      border: 1px solid var(--g-outline-subtle);
      width: fit-content;
      overflow-x: auto;
    }

    .tab-btn {
      background: transparent;
      border: none;
      color: var(--g-text-secondary);
      font-family: inherit;
      font-size: 14px;
      font-weight: 500;
      padding: 8px 20px;
      border-radius: var(--radius-full);
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: all 0.2s ease;
      white-space: nowrap;
    }

    .tab-btn:hover {
      background: var(--g-surface-elevated);
      color: var(--g-text);
    }

    .tab-btn.active {
      background: var(--google-blue);
      color: #041e49;
      font-weight: 700;
    }

    /* Tab 内容容器 */
    .tab-pane {
      display: none;
      animation: fadeIn 0.25s ease forwards;
    }
    .tab-pane.active {
      display: block;
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(6px); }
      to { opacity: 1; transform: translateY(0); }
    }

    /* Google Material Card 设计 */
    .card {
      background: var(--g-surface);
      border: 1px solid var(--g-outline-subtle);
      border-radius: var(--radius-md);
      padding: 20px 24px;
      position: relative;
    }

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--g-text);
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;
    }

    .grid-3 {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 20px;
    }
    .grid-2 {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
    }
    @media (max-width: 900px) {
      .grid-3, .grid-2 { grid-template-columns: 1fr; }
    }

    /* 监控概览组件 */
    .metric-box {
      background: var(--g-surface-elevated);
      border-radius: var(--radius-sm);
      padding: 16px;
      border: 1px solid var(--g-outline-subtle);
    }
    .metric-value {
      font-size: 32px;
      font-weight: 700;
      margin-top: 4px;
    }
    .metric-progress {
      height: 6px;
      background: rgba(255,255,255,0.08);
      border-radius: var(--radius-full);
      margin-top: 10px;
      overflow: hidden;
    }
    .metric-bar {
      height: 100%;
      border-radius: var(--radius-full);
      transition: width 0.4s ease;
    }

    /* 任务模式卡片 (Touhou Little Maid 架构) */
    .task-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 16px;
    }
    .task-card {
      background: var(--g-surface-elevated);
      border: 2px solid var(--g-outline-subtle);
      border-radius: var(--radius-md);
      padding: 18px;
      cursor: pointer;
      transition: all 0.2s ease;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }
    .task-card:hover {
      border-color: var(--google-blue);
      transform: translateY(-2px);
    }
    .task-card.active {
      border-color: var(--google-green);
      background: var(--google-green-container);
    }
    .task-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;
    }
    .task-name {
      font-size: 16px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .task-badge {
      font-size: 11px;
      padding: 3px 8px;
      border-radius: var(--radius-full);
      background: rgba(255,255,255,0.1);
    }
    .task-card.active .task-badge {
      background: var(--google-green);
      color: #041e49;
      font-weight: 700;
    }

    /* 随身行囊可视化 (Maid Inventory 24 格) */
    .inv-container {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }
    .inv-section-title {
      font-size: 13px;
      color: var(--g-text-secondary);
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      margin-bottom: 8px;
    }
    .slots-grid-backpack {
      display: grid;
      grid-template-columns: repeat(9, 1fr);
      gap: 8px;
    }
    .slots-grid-equipment {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
    }
    .slot-item {
      background: var(--g-surface-elevated);
      border: 1px solid var(--g-outline);
      border-radius: var(--radius-sm);
      min-height: 60px;
      padding: 8px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      position: relative;
      transition: all 0.15s ease;
    }
    .slot-item:hover {
      border-color: var(--google-blue);
      background: var(--g-surface-hover);
    }
    .slot-name {
      font-size: 11px;
      color: var(--g-text);
      text-align: center;
      word-break: break-all;
    }
    .slot-count {
      position: absolute;
      bottom: 4px;
      right: 6px;
      font-size: 11px;
      font-weight: 700;
      color: var(--google-yellow);
    }
    .slot-type-badge {
      position: absolute;
      top: 3px;
      left: 4px;
      font-size: 9px;
      color: var(--g-text-tertiary);
    }

    /* 记忆卡片列表 (Google Keep 风格) */
    .memory-cards {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 16px;
      margin-top: 16px;
    }
    .memory-card {
      background: var(--g-surface-elevated);
      border: 1px solid var(--g-outline-subtle);
      border-radius: var(--radius-md);
      padding: 16px;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      gap: 12px;
      transition: border-color 0.2s ease;
    }
    .memory-card:hover {
      border-color: var(--google-yellow);
    }
    .memory-content {
      font-size: 14px;
      color: var(--g-text);
      line-height: 1.5;
    }
    .memory-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-top: 1px solid var(--g-outline-subtle);
      padding-top: 8px;
      font-size: 12px;
      color: var(--g-text-tertiary);
    }
    .btn-delete {
      background: transparent;
      border: none;
      color: var(--google-red);
      cursor: pointer;
      font-size: 12px;
      padding: 4px 8px;
      border-radius: var(--radius-sm);
    }
    .btn-delete:hover {
      background: var(--google-red-container);
    }

    /* Google Cloud Shell 命令行终端 */
    .terminal-window {
      background: #0e0e0e;
      border: 1px solid var(--g-outline);
      border-radius: var(--radius-md);
      overflow: hidden;
      display: flex;
      flex-direction: column;
      height: 520px;
      font-family: 'Roboto Mono', monospace;
    }
    .terminal-topbar {
      background: #1b1b1b;
      padding: 8px 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid var(--g-outline);
      font-size: 12px;
      color: var(--g-text-secondary);
    }
    .terminal-output {
      flex: 1;
      padding: 16px;
      overflow-y: auto;
      font-size: 13px;
      line-height: 1.6;
    }
    .t-line-ok { color: var(--google-green); }
    .t-line-cmd { color: var(--google-yellow); font-weight: 600; }
    .t-line-err { color: var(--google-red); }
    .t-line-prompt { color: var(--google-blue); }
    .terminal-input-bar {
      display: flex;
      align-items: center;
      background: #151515;
      padding: 10px 16px;
      border-top: 1px solid var(--g-outline);
    }
    .terminal-prompt {
      color: var(--google-green);
      font-size: 13px;
      margin-right: 10px;
      font-weight: 600;
    }
    .terminal-input {
      flex: 1;
      background: transparent;
      border: none;
      outline: none;
      color: #fff;
      font-family: 'Roboto Mono', monospace;
      font-size: 13px;
    }

    /* Google 按钮与输入框 */
    .btn-primary {
      background: var(--google-blue);
      color: #041e49;
      font-family: 'Google Sans', sans-serif;
      font-weight: 600;
      font-size: 13px;
      border: none;
      padding: 8px 18px;
      border-radius: var(--radius-full);
      cursor: pointer;
      transition: background 0.2s ease;
    }
    .btn-primary:hover {
      background: #a8c7fa;
    }
    .btn-secondary {
      background: transparent;
      color: var(--google-blue);
      border: 1px solid var(--g-outline);
      font-family: 'Google Sans', sans-serif;
      font-weight: 500;
      font-size: 13px;
      padding: 7px 16px;
      border-radius: var(--radius-full);
      cursor: pointer;
    }
    .btn-secondary:hover {
      background: var(--google-blue-container);
    }

    .form-input {
      background: var(--g-surface-elevated);
      border: 1px solid var(--g-outline);
      border-radius: var(--radius-sm);
      color: var(--g-text);
      font-family: inherit;
      font-size: 14px;
      padding: 8px 12px;
      outline: none;
    }
    .form-input:focus {
      border-color: var(--google-blue);
    }
  </style>
</head>
<body>

  <!-- Google Cloud Header -->
  <header>
    <div class="brand-section">
      <div class="google-logo">
        <span class="google-dot-group">
          <span class="g-dot dot-blue"></span>
          <span class="g-dot dot-red"></span>
          <span class="g-dot dot-yellow"></span>
          <span class="g-dot dot-green"></span>
        </span>
        <span style="font-weight: 700;">Google Cloud</span>
        <span style="color: var(--g-text-tertiary); font-weight: 400;">| AI NPC Studio</span>
      </div>
      <div class="project-pill">
        <span>项目: <strong>deepseek-maid-npc</strong></span>
      </div>
    </div>
    <div style="display: flex; align-items: center; gap: 12px;">
      <div class="status-pill">
        <span class="pulse-dot"></span>
        <span>Forge 1.20.1 & Middleware 运行就绪</span>
      </div>
      <button class="btn-secondary" onclick="refreshAllData()">刷新数据</button>
    </div>
  </header>

  <!-- 主容器 -->
  <div class="main-wrapper">
    
    <!-- 胶囊导航 Tabs -->
    <div class="tab-bar">
      <button class="tab-btn active" onclick="switchTab('overview')">📊 监控概览</button>
      <button class="tab-btn" onclick="switchTab('tasks')">🤖 任务调度中心</button>
      <button class="tab-btn" onclick="switchTab('inventory')">🎒 随身行囊与装备</button>
      <button class="tab-btn" onclick="switchTab('memory')">🧠 事实知识库</button>
      <button class="tab-btn" onclick="switchTab('terminal')">💻 Google Cloud Shell</button>
    </div>

    <!-- TAB 1: 监控概览 -->
    <div id="tab-overview" class="tab-pane active">
      <div class="grid-3" style="margin-bottom: 20px;">
        <div class="card">
          <div class="card-title">
            <span>💖 心情指数 (Mood)</span>
            <span id="overview-mood-tag" style="color: var(--google-yellow); font-size: 13px;">良好</span>
          </div>
          <div class="metric-value" id="overview-mood" style="color: #f472b6;">65 / 100</div>
          <div class="metric-progress">
            <div id="overview-mood-bar" class="metric-bar" style="width: 65%; background: #f472b6;"></div>
          </div>
        </div>

        <div class="card">
          <div class="card-title">
            <span>⭐ 羁绊好感度 (Affection)</span>
            <span id="overview-aff-tag" style="color: var(--google-yellow); font-size: 13px;">Lv.1 初识</span>
          </div>
          <div class="metric-value" id="overview-aff" style="color: var(--google-yellow);">20 / 100</div>
          <div class="metric-progress">
            <div id="overview-aff-bar" class="metric-bar" style="width: 20%; background: var(--google-yellow);"></div>
          </div>
        </div>

        <div class="card">
          <div class="card-title">
            <span>🤖 活跃行为状态 (Task)</span>
            <span id="overview-active-task-badge" class="task-badge" style="background: var(--google-green); color: #041e49; font-weight: 700;">IDLE</span>
          </div>
          <div class="metric-value" id="overview-task-name" style="color: var(--google-blue); font-size: 26px;">待命休息</div>
          <p id="overview-task-desc" style="font-size: 12px; color: var(--g-text-secondary); margin-top: 6px;">在原地驻足修养，开启自主生命再生与观察。</p>
        </div>
      </div>

      <div class="grid-2">
        <div class="card">
          <div class="card-title">🎭 当前情绪与微观表情</div>
          <div style="display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 16px;" id="emotion-chips">
            <button class="btn-secondary" onclick="setEmotion('happy')">😊 开心 (Happy)</button>
            <button class="btn-secondary" onclick="setEmotion('excited')">⚡ 兴奋 (Excited)</button>
            <button class="btn-secondary" onclick="setEmotion('touched')">💖 感动 (Touched)</button>
            <button class="btn-secondary" onclick="setEmotion('dance')">🎶 摇摆 (Dance)</button>
            <button class="btn-secondary" onclick="setEmotion('sad')">💧 低落 (Sad)</button>
            <button class="btn-secondary" onclick="setEmotion('angry')">💢 气恼 (Angry)</button>
          </div>
          <div style="background: var(--g-surface-elevated); padding: 14px; border-radius: var(--radius-sm); font-size: 13px;">
            <span>当前主导情绪: <strong id="cur-emotion-text" style="color: var(--google-green);">happy</strong></span><br>
            <span style="color: var(--g-text-tertiary); font-size: 12px;">当玩家靠近 16 格内时，实体将自动朝空中释放对应的氛围粒子与 3D 漫画气泡。</span>
          </div>
        </div>

        <div class="card">
          <div class="card-title">🎮 实体具身动作管线</div>
          <div style="display: flex; flex-direction: column; gap: 10px; font-size: 13px;">
            <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--g-outline-subtle); padding-bottom: 6px;">
              <span>骨骼动画引擎</span>
              <strong style="color: var(--google-blue);">GeckoLib & Bedrock 3D Hierarchy</strong>
            </div>
            <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--g-outline-subtle); padding-bottom: 6px;">
              <span>外部资源热重载</span>
              <strong style="color: var(--google-green);">run/npc_packs/*.zip 热加载支持</strong>
            </div>
            <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--g-outline-subtle); padding-bottom: 6px;">
              <span>交互分流机制</span>
              <strong style="color: var(--google-yellow);">普通右键 = 沉浸对话 | Shift+右键 = 管理行囊</strong>
            </div>
            <div style="display: flex; justify-content: space-between;">
              <span>当前皮肤模型</span>
              <strong style="color: var(--google-blue);">DeepSeek大肥鱼 (LittleSkin 926524)</strong>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- TAB 2: 任务调度中心 -->
    <div id="tab-tasks" class="tab-pane">
      <div class="card" style="margin-bottom: 20px;">
        <div class="card-title">
          <span>模块化任务状态机 (Modular Task Machine)</span>
          <span style="font-size: 12px; color: var(--g-text-secondary);">借鉴 Touhou Little Maid 架构</span>
        </div>
        <p style="font-size: 13px; color: var(--g-text-secondary); margin-bottom: 16px;">
          点击卡片可即时向服务端下发调度指令，切换 DeepSeek 大肥鱼的执行行为。遇到攻击或对话时状态机将自动挂起并在结束后平滑恢复。
        </p>
        <div class="task-grid" id="tasks-container">
          <!-- 动态加载卡片 -->
        </div>
      </div>

      <div class="card">
        <div class="card-title">任务调度控制日志</div>
        <div id="task-log-box" style="background: #000; border-radius: var(--radius-sm); padding: 14px; font-family: 'Roboto Mono', monospace; font-size: 12px; height: 160px; overflow-y: auto; color: #a3e635;">
          [TASK DISPATCHER] 任务调度管理器在线，就绪接收远程指令...
        </div>
      </div>
    </div>

    <!-- TAB 3: 随身行囊与装备 (Maid Inventory 24格) -->
    <div id="tab-inventory" class="tab-pane">
      <div class="card" style="margin-bottom: 20px;">
        <div class="card-title">
          <span>NPC 实体独立随身行囊 (Maid Inventory - 24 Slots)</span>
          <span style="font-size: 12px; color: var(--google-blue);">4护甲 + 2手持 + 18背包大容量</span>
        </div>

        <div class="inv-container">
          <!-- 装备与手持 -->
          <div>
            <div class="inv-section-title">装备栏与手持武器 (Slots 0 ~ 5)</div>
            <div class="slots-grid-equipment" id="inv-equip-grid">
              <!-- 动态装备槽 -->
            </div>
          </div>

          <!-- 18 格大背包 -->
          <div>
            <div class="inv-section-title">随身行囊存储仓 (Slots 6 ~ 23)</div>
            <div class="slots-grid-backpack" id="inv-backpack-grid">
              <!-- 动态背包槽 -->
            </div>
          </div>
        </div>
      </div>

      <!-- 送礼与物品注入 -->
      <div class="card">
        <div class="card-title">向 NPC 随身行囊赠送物品</div>
        <div style="display: flex; gap: 12px; flex-wrap: wrap; align-items: center;">
          <input type="text" id="add-item-id" class="form-input" placeholder="物品 ID (如 minecraft:golden_apple)" style="width: 280px;" value="minecraft:golden_apple">
          <input type="text" id="add-item-name" class="form-input" placeholder="显示名称 (如 金苹果)" style="width: 160px;" value="金苹果">
          <input type="number" id="add-item-count" class="form-input" placeholder="数量" style="width: 80px;" value="4" min="1" max="64">
          <button class="btn-primary" onclick="submitAddItem()">放入行囊</button>
        </div>
      </div>
    </div>

    <!-- TAB 4: 事实知识库 (Google Keep 风格) -->
    <div id="tab-memory" class="tab-pane">
      <div class="card">
        <div class="card-title">
          <span>长期事实记忆库 (SQLite Fact Engine)</span>
          <div style="display: flex; gap: 10px;">
            <button class="btn-secondary" onclick="resetAllMemories()" style="color: var(--google-red);">彻底清空记忆</button>
          </div>
        </div>

        <div style="display: flex; gap: 12px; margin-bottom: 16px;">
          <input type="text" id="new-fact-input" class="form-input" placeholder="录入新事实：例如 '玩家喜欢在森林边缘建造白桦木小屋'..." style="flex: 1;">
          <button class="btn-primary" onclick="submitAddFact()">写入记忆卡片</button>
        </div>

        <div class="memory-cards" id="memory-cards-container">
          <!-- 动态记忆卡片 -->
        </div>
      </div>
    </div>

    <!-- TAB 5: Google Cloud Shell 终端 -->
    <div id="tab-terminal" class="tab-pane">
      <div class="terminal-window">
        <div class="terminal-topbar">
          <span>cloudshell:~/minecraft-npc (deepseek-maid)</span>
          <span>bash (x86_64)</span>
        </div>
        <div class="terminal-output" id="terminal-out">
          <div class="t-line-prompt">Welcome to Google Cloud Shell for DeepSeek AI NPC.</div>
          <div class="t-line-prompt">Type '/help' to view available interactive commands.</div>
        </div>
        <div class="terminal-input-bar">
          <span class="terminal-prompt">dev@cloudshell:~$</span>
          <input type="text" id="term-input" class="terminal-input" placeholder="输入控制台指令并按回车..." onkeydown="handleTermKey(event)">
        </div>
      </div>
    </div>

  </div>

  <script>
    // Tab 切换逻辑
    function switchTab(tabId) {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
      
      event.currentTarget.classList.add('active');
      const targetPane = document.getElementById('tab-' + tabId);
      if (targetPane) targetPane.classList.add('active');

      if (tabId === 'tasks') loadTasks();
      if (tabId === 'inventory') loadInventory();
      if (tabId === 'memory') loadMemories();
    }

    // 格式化数据刷新
    async function refreshAllData() {
      await loadMood();
      await loadTasks();
      await loadInventory();
      await loadMemories();
    }

    // 1. 加载心情与情绪
    async function loadMood() {
      try {
        const res = await fetch('/api/mood');
        const data = await res.json();
        document.getElementById('overview-mood').innerText = `${data.mood} / 100`;
        document.getElementById('overview-mood-bar').style.width = `${data.mood}%`;
        document.getElementById('overview-mood-tag').innerText = data.mood_desc;

        document.getElementById('overview-aff').innerText = `${data.affection} / 100`;
        document.getElementById('overview-aff-bar').style.width = `${data.affection}%`;
        document.getElementById('overview-aff-tag').innerText = data.affection_desc;

        document.getElementById('cur-emotion-text').innerText = data.current_emotion;
      } catch (e) {
        console.error('加载心情失败:', e);
      }
    }

    async function setEmotion(emotion) {
      try {
        await fetch('/api/mood/update', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({ emotion: emotion })
        });
        loadMood();
      } catch (e) {
        alert('修改情绪失败: ' + e);
      }
    }

    // 2. 加载任务列表 (Task State Machine)
    async function loadTasks() {
      try {
        const res = await fetch('/api/npc/tasks');
        const data = await res.json();
        const container = document.getElementById('tasks-container');
        container.innerHTML = '';

        const currentTask = data.current_task || 'idle';
        document.getElementById('overview-active-task-badge').innerText = currentTask.toUpperCase();

        data.tasks.forEach(t => {
          const isActive = t.id === currentTask;
          if (isActive) {
            document.getElementById('overview-task-name').innerText = t.name;
            document.getElementById('overview-task-desc').innerText = t.desc;
          }

          const card = document.createElement('div');
          card.className = `task-card ${isActive ? 'active' : ''}`;
          card.onclick = () => switchTask(t.id);
          card.innerHTML = `
            <div>
              <div class="task-header">
                <span class="task-name">${t.icon} ${t.name}</span>
                <span class="task-badge">${isActive ? '执行中' : '就绪'}</span>
              </div>
              <p style="font-size: 12px; color: var(--g-text-secondary); line-height: 1.4;">${t.desc}</p>
            </div>
            <div style="margin-top: 12px;">
              <button class="btn-primary" style="font-size: 11px; padding: 4px 12px;">${isActive ? '当前活跃模式' : '切换为该任务'}</button>
            </div>
          `;
          container.appendChild(card);
        });
      } catch (e) {
        console.error('加载任务失败:', e);
      }
    }

    async function switchTask(taskId) {
      try {
        const res = await fetch('/api/npc/task/switch', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({ task_id: taskId })
        });
        const data = await res.json();
        const logBox = document.getElementById('task-log-box');
        logBox.innerHTML += `<div>[${new Date().toLocaleTimeString()}] 状态机调度: 成功切换为任务 -> ${taskId.toUpperCase()}</div>`;
        logBox.scrollTop = logBox.scrollHeight;
        loadTasks();
      } catch (e) {
        alert('切换任务失败: ' + e);
      }
    }

    // 3. 加载随身行囊
    async function loadInventory() {
      try {
        const res = await fetch('/api/npc/inventory');
        const data = await res.json();
        
        const equipGrid = document.getElementById('inv-equip-grid');
        const backpackGrid = document.getElementById('inv-backpack-grid');
        equipGrid.innerHTML = '';
        backpackGrid.innerHTML = '';

        const itemMap = {};
        (data.items || []).forEach(it => { itemMap[it.slot] = it; });

        // 装备 6 格 (0..5)
        const equipSlots = [
          { slot: 2, label: '头盔' },
          { slot: 3, label: '胸甲' },
          { slot: 4, label: '护腿' },
          { slot: 5, label: '靴子' },
          { slot: 0, label: '主手武器' },
          { slot: 1, label: '副手持物' }
        ];

        equipSlots.forEach(s => {
          const it = itemMap[s.slot];
          const div = document.createElement('div');
          div.className = 'slot-item';
          div.style.width = '120px';
          div.innerHTML = `
            <span class="slot-type-badge">${s.label}</span>
            <span class="slot-name">${it ? it.item_name : '<span style="color:var(--g-text-tertiary);">[空置]</span>'}</span>
            ${it ? `<span class="slot-count">x${it.count}</span>` : ''}
          `;
          equipGrid.appendChild(div);
        });

        // 背包 18 格 (6..23)
        for (let i = 6; i < 24; i++) {
          const it = itemMap[i];
          const div = document.createElement('div');
          div.className = 'slot-item';
          div.innerHTML = `
            <span class="slot-type-badge">#${i - 5}</span>
            <span class="slot-name">${it ? it.item_name : ''}</span>
            ${it ? `<span class="slot-count">x${it.count}</span>` : ''}
          `;
          backpackGrid.appendChild(div);
        }
      } catch (e) {
        console.error('加载行囊失败:', e);
      }
    }

    async function submitAddItem() {
      const itemId = document.getElementById('add-item-id').value.trim();
      const itemName = document.getElementById('add-item-name').value.trim();
      const count = parseInt(document.getElementById('add-item-count').value.trim()) || 1;

      if (!itemId) { alert('请输入物品 ID'); return; }

      try {
        const res = await fetch('/api/npc/inventory/add', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({ item_id: itemId, item_name: itemName, count: count })
        });
        const data = await res.json();
        alert(data.message);
        loadInventory();
      } catch (e) {
        alert('物品添加失败: ' + e);
      }
    }

    // 4. 记忆事实库
    async function loadMemories() {
      try {
        const res = await fetch('/api/memories');
        const list = await res.json();
        const container = document.getElementById('memory-cards-container');
        container.innerHTML = '';

        if (!list || list.length === 0) {
          container.innerHTML = '<div style="color:var(--g-text-tertiary); font-size:13px;">记忆库为空，请在上方输入事实进行录入。</div>';
          return;
        }

        list.forEach(m => {
          const card = document.createElement('div');
          card.className = 'memory-card';
          card.innerHTML = `
            <div class="memory-content">${m.fact}</div>
            <div class="memory-footer">
              <span>类别: ${m.category || 'dialog'} &bull; #${m.id}</span>
              <button class="btn-delete" onclick="deleteMemory(${m.id})">删除</button>
            </div>
          `;
          container.appendChild(card);
        });
      } catch (e) {
        console.error('加载记忆失败:', e);
      }
    }

    async function submitAddFact() {
      const fact = document.getElementById('new-fact-input').value.trim();
      if (!fact) return;

      try {
        await fetch('/api/memory/add', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({ fact: fact, category: 'manual' })
        });
        document.getElementById('new-fact-input').value = '';
        loadMemories();
      } catch (e) {
        alert('写入记忆失败: ' + e);
      }
    }

    async function deleteMemory(id) {
      if (!confirm('确定删除该记忆卡片？')) return;
      try {
        await fetch('/api/memory/' + id, { method: 'DELETE' });
        loadMemories();
      } catch (e) {
        alert('删除失败: ' + e);
      }
    }

    async function resetAllMemories() {
      if (!confirm('警告：此操作将彻底抹除当前所有长期记忆、对话日志并重置好感度，确定执行？')) return;
      try {
        await fetch('/api/memory/reset', { method: 'POST' });
        loadMemories();
        loadMood();
      } catch (e) {
        alert('重置失败: ' + e);
      }
    }

    // 5. Cloud Shell 终端
    async function handleTermKey(e) {
      if (e.key === 'Enter') {
        const input = document.getElementById('term-input');
        const cmd = input.value.trim();
        if (!cmd) return;

        input.value = '';
        const out = document.getElementById('terminal-out');
        out.innerHTML += `<div><span class="terminal-prompt">dev@cloudshell:~$</span> <span class="t-line-cmd">${cmd}</span></div>`;

        try {
          const res = await fetch('/api/console/exec', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ command: cmd })
          });
          const data = await res.json();
          (data.lines || []).forEach(l => {
            const cls = l.type === 'ok' ? 't-line-ok' : (l.type === 'err' ? 't-line-err' : (l.type === 'cmd' ? 't-line-cmd' : ''));
            out.innerHTML += `<div class="${cls}">${l.text}</div>`;
          });
        } catch (err) {
          out.innerHTML += `<div class="t-line-err">执行失败: ${err}</div>`;
        }
        out.scrollTop = out.scrollHeight;
      }
    }

    // 页面初始化自动拉取
    window.addEventListener('DOMContentLoaded', () => {
      loadMood();
      loadTasks();
    });
  </script>
</body>
</html>
"""
