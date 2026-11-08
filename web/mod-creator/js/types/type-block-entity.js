// ============================================================
// type-block-entity.js — 方块实体与机器资产类型模块 (BlockEntity + GUI)
// 结合 MCreator 核心参数模型深度重构，支持高级模式素材地址选择与资产路径绑定
// 严格遵守 Google Gemini 规范与 Zero-Emoji 规则
// ============================================================

(function (global) {
  'use strict';

  const DEFAULT_CONFIG = {
    // 基础资产标识
    modId: 'custom_mod',
    blockEntityId: 'custom_machine',
    nameZh: '自定义加工机器',
    nameEn: 'Custom Machine',
    blockSound: 'METAL',
    hardness: 3.5,
    resistance: 12.0,
    creativeTab: 'FUNCTIONAL_BLOCKS',

    // MCreator 机器素材地址绑定 (高级模式重点)
    assetGuiTexture: 'assets/custom_mod/textures/gui/container/custom_machine.png',
    assetJeiTexture: 'assets/custom_mod/textures/gui/jei/custom_machine_jei.png',
    assetWorkingSound: 'minecraft:block.blastfurnace.fire_crackle',
    assetSpecialModel: '', // 留空则使用标准方块模型

    // MCreator 核心能力与容量 (Capabilities)
    hasItemHandler: true,
    inputSlotCount: 2,
    outputSlotCount: 1,
    fuelSlotCount: 1,
    hasEnergy: true,
    energyCapacity: 50000,
    maxEnergyTransfer: 1000,
    energyPerTick: 20,
    hasFluid: false,
    fluidCapacity: 4000,
    supportedFluid: 'minecraft:water',

    // 心跳逻辑与状态机 (Ticker & Logic)
    hasTicker: true,
    processingTime: 100, // 5秒
    hasLitState: true,
    facingType: 'HORIZONTAL_FACING',
    emitsComparator: true,
    redstoneControl: 'IGNORED', // IGNORED | HIGH_SIGNAL | LOW_SIGNAL

    // 容器界面与网络同步 (GUI & Sync)
    hasContainerGui: true,
    menuTitle: '加工中心工作台',
    progressBarType: 'ARROW_RIGHT', // ARROW_RIGHT | FLAME_UP | ENERGY_BAR
    syncDeltaPacket: true,
    dropInventoryOnBreak: true,

    // 高阶工业级系统
    sidedCapabilityConfig: true, // 自动化面配置 (顶部输入/底部输出/侧面供能)
    redstoneControl: 'IGNORED', // IGNORED | REQUIRE_LOW | REQUIRE_HIGH
    clientRenderType: 'STATIC_BLOCK_MODEL', // STATIC_BLOCK_MODEL | BER_ANIMATED
    codeGenerationStrategy: 'DATAGEN',
    conventionalCommit: true
  };

  const PRESETS = {
    alloy_smelter: {
      name: '双室合金熔炼炉 (GUI双进单出·FE能量)',
      desc: '支持双原料输入与单成品输出，消耗 FE 能量，带动态工作点火状态与 GUI 进度条。',
      mode: 'advanced',
      data: {
        ...DEFAULT_CONFIG,
        blockEntityId: 'alloy_smelter',
        nameZh: '等离子双室合金炉',
        nameEn: 'Plasma Alloy Smelter',
        hardness: 4.0,
        resistance: 15.0,
        hasItemHandler: true,
        inputSlotCount: 2,
        outputSlotCount: 1,
        fuelSlotCount: 0,
        hasEnergy: true,
        energyCapacity: 100000,
        maxEnergyTransfer: 2000,
        energyPerTick: 40,
        processingTime: 120,
        menuTitle: '等离子合金冶炼控制台',
        progressBarType: 'ARROW_RIGHT',
        assetGuiTexture: 'assets/custom_mod/textures/gui/container/alloy_smelter.png',
        assetJeiTexture: 'assets/custom_mod/textures/gui/jei/alloy_smelter_jei.png'
      }
    },
    fluid_infuser: {
      name: '高压流体灌注机 (能量+流体储罐+物品)',
      desc: '4000mB 流体储罐与单物品槽位交互，需供给电能，全套自动化面配置。',
      mode: 'advanced',
      data: {
        ...DEFAULT_CONFIG,
        blockEntityId: 'fluid_infuser',
        nameZh: '高压流体灌注机',
        nameEn: 'High-Pressure Fluid Infuser',
        hasItemHandler: true,
        inputSlotCount: 1,
        outputSlotCount: 1,
        hasEnergy: true,
        energyCapacity: 50000,
        hasFluid: true,
        fluidCapacity: 8000,
        supportedFluid: 'custom_mod:molten_crystal',
        progressBarType: 'ENERGY_BAR',
        assetGuiTexture: 'assets/custom_mod/textures/gui/container/fluid_infuser.png'
      }
    }
  };

  const TypeBlockEntity = {
    id: 'block_entity',
    label: '机器方块实体 (BlockEntity)',
    description: '工业机械、储能电池或容器，具备物品栏、FE 能量、流体槽、Tick 循环与 GUI 界面',
    iconName: 'mc_block_entity',
    defaultConfig: DEFAULT_CONFIG,
    presets: PRESETS,

    renderConfigFlow(c, isAdv, activeStep) {
      const modId = c.modId || 'custom_mod';
      const blockEntityId = c.blockEntityId || 'custom_machine';

      return `
        <div class="gemini-assistant-bubble">
          <div class="assistant-avatar">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 2C12 7.523 7.523 12 2 12C7.523 12 12 16.477 12 22C12 16.477 16.477 12 22 12C16.477 12 12 7.523 12 2Z"/></svg>
          </div>
          <div class="assistant-content">
            <div class="assistant-title">${isAdv ? '正在进行 [高级模式] MCreator 机器与方块实体架构配置' : '正在进行 [普通模式] 机器速填'}</div>
            <div class="assistant-desc">
              ${isAdv 
                ? '已开启 MCreator 机器槽位规划、FE 能量/流体 Capabilities、侧面自动化、心跳 Ticker 与 GUI/JEI 背景贴图素材地址选择绑定。' 
                : '配置机器名称、槽位数、FE 能量容量与加工周期。未配置项自动按标准工业机器标准赋默认值。'}
            </div>
          </div>
          <div class="bubble-actions">
            <button class="btn-bubble-preset" onclick="PromptUI.showPresetPicker()">套用经典模板</button>
          </div>
        </div>

        <div class="flow-step-bar">
          <div class="step-item ${activeStep >= 1 ? 'active' : ''}" onclick="PromptUI.goToStep(1)">
            <span class="step-num">1</span>
            <span class="step-name">槽位与能源容量</span>
          </div>
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 2 ? 'active' : ''}" onclick="PromptUI.goToStep(2)">
            <span class="step-num">2</span>
            <span class="step-name">界面与心跳逻辑</span>
          </div>
          ${isAdv ? `
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 3 ? 'active' : ''}" onclick="PromptUI.goToStep(3)">
            <span class="step-num">3</span>
            <span class="step-name">素材绑定与构建</span>
          </div>` : ''}
        </div>

        <div class="config-grid-sections">
          <!-- 步骤 1：核心标识与槽位能力 -->
          <div class="config-section-card ${activeStep === 1 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 1</span>
              <h3>机器方块核心标识与 Forge Capabilities</h3>
              <span class="section-status-hint">底层规范由外部 Skill 统一接管</span>
            </div>

            <!-- 核心标识紧凑栏 -->
            <div class="grid-3 mb-3" style="margin-bottom:14px;">
              <div class="form-group">
                <label>机器注册英文 ID (snake_case) <span class="required">*</span></label>
                <input type="text" class="form-control" value="${c.blockEntityId || 'custom_machine'}" 
                       placeholder="如 alloy_smelter" oninput="PromptUI.updateVal('blockEntityId', this.value)" />
              </div>
              <div class="form-group">
                <label>机器中文显示名</label>
                <input type="text" class="form-control" value="${c.nameZh || '自定义加工机器'}" 
                       placeholder="如：等离子双室合金炉" oninput="PromptUI.updateVal('nameZh', this.value); PromptUI.syncOmniSearch(this.value);" />
              </div>
              <div class="form-group">
                <label>创造模式标签栏 (Creative Tab)</label>
                <select class="form-control" onchange="PromptUI.updateVal('creativeTab', this.value)">
                  <option value="FUNCTIONAL_BLOCKS" ${c.creativeTab === 'FUNCTIONAL_BLOCKS' ? 'selected' : ''}>功能方块 (Functional Blocks)</option>
                  <option value="REDSTONE_BLOCKS" ${c.creativeTab === 'REDSTONE_BLOCKS' ? 'selected' : ''}>红石元件 (Redstone Blocks)</option>
                  <option value="BUILDING_BLOCKS" ${c.creativeTab === 'BUILDING_BLOCKS' ? 'selected' : ''}>建筑方块 (Building Blocks)</option>
                </select>
              </div>
            </div>
            <div class="sub-form-box mb-3">
              <h4 style="margin:0 0 10px 0; font-size:13px; color:var(--gemini-text);">物品栏槽位规划 (Inventory Slots)</h4>
              <div class="grid-3">
                <div class="form-group">
                  <label>原料输入槽数量 (Input)</label>
                  <input type="number" class="form-control" value="${c.inputSlotCount || 1}" min="0" max="9"
                         oninput="PromptUI.updateVal('inputSlotCount', parseInt(this.value)||0)" />
                </div>
                <div class="form-group">
                  <label>成品产出槽数量 (Output)</label>
                  <input type="number" class="form-control" value="${c.outputSlotCount || 1}" min="0" max="9"
                         oninput="PromptUI.updateVal('outputSlotCount', parseInt(this.value)||0)" />
                </div>
                <div class="form-group">
                  <label>燃料/升级槽数量 (Upgrade/Fuel)</label>
                  <input type="number" class="form-control" value="${c.fuelSlotCount || 0}" min="0" max="4"
                         oninput="PromptUI.updateVal('fuelSlotCount', parseInt(this.value)||0)" />
                </div>
              </div>
            </div>

            <div class="section-body grid-3">
              <div class="form-group">
                <label>FE 能量支持 (Forge Energy)</label>
                <label class="pill-checkbox mt-2">
                  <input type="checkbox" ${c.hasEnergy ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('hasEnergy', this.checked); PromptUI.rerenderFlow();" />
                  <span>启用 FE 电能系统</span>
                </label>
              </div>
              ${c.hasEnergy ? `
              <div class="form-group">
                <label>能量缓存容量 (FE Capacity)</label>
                <input type="number" class="form-control" value="${c.energyCapacity}" step="10000" min="1000"
                       oninput="PromptUI.updateVal('energyCapacity', parseInt(this.value)||50000)" />
              </div>
              <div class="form-group">
                <label>每 Tick 运行能耗 (FE/t)</label>
                <input type="number" class="form-control" value="${c.energyPerTick}" step="5" min="0"
                       oninput="PromptUI.updateVal('energyPerTick', parseInt(this.value)||20)" />
              </div>
              ` : ''}
              <div class="form-group">
                <label>流体储罐支持 (Fluid Tank)</label>
                <label class="pill-checkbox mt-2">
                  <input type="checkbox" ${c.hasFluid ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('hasFluid', this.checked); PromptUI.rerenderFlow();" />
                  <span>启用流体储罐能力</span>
                </label>
              </div>
              ${c.hasFluid ? `
              <div class="form-group">
                <label>储罐容量 (mB, 1桶=1000mB)</label>
                <input type="number" class="form-control" value="${c.fluidCapacity}" step="1000" min="1000"
                       oninput="PromptUI.updateVal('fluidCapacity', parseInt(this.value)||4000)" />
              </div>
              <div class="form-group">
                <label>支持流体类型 ID</label>
                <input type="text" class="form-control" value="${c.supportedFluid || 'minecraft:water'}" 
                       oninput="PromptUI.updateVal('supportedFluid', this.value)" />
              </div>
              ` : ''}
            </div>

            <div style="display:flex; flex-wrap:wrap; gap:16px; margin-top:14px;">
              <label class="pill-checkbox">
                <input type="checkbox" ${c.sidedCapabilityConfig ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('sidedCapabilityConfig', this.checked)" />
                <span>启用侧面自动化推拉 (顶部输入/底部输出/侧面能量)</span>
              </label>
              <label class="pill-checkbox">
                <input type="checkbox" ${c.dropInventoryOnBreak ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('dropInventoryOnBreak', this.checked)" />
                <span>方块破坏时自动掉落内部物品 (Containers.dropContents)</span>
              </label>
            </div>
            <div class="form-group mt-3" style="max-width:320px;">
              <label>红石信号控制模式 (Redstone Control)</label>
              <select class="form-control" onchange="PromptUI.updateVal('redstoneControl', this.value)">
                <option value="IGNORED" ${c.redstoneControl === 'IGNORED' ? 'selected' : ''}>IGNORED (忽略红石，始终自动加工)</option>
                <option value="HIGH_SIGNAL" ${c.redstoneControl === 'HIGH_SIGNAL' ? 'selected' : ''}>HIGH_SIGNAL (仅在接收到红石高电平时加工)</option>
                <option value="LOW_SIGNAL" ${c.redstoneControl === 'LOW_SIGNAL' ? 'selected' : ''}>LOW_SIGNAL (仅在无红石信号/低电平时加工)</option>
              </select>
            </div>
          </div>

          <!-- 步骤 2：界面与心跳逻辑 -->
          <div class="config-section-card ${activeStep === 2 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 2</span>
              <h3>MCreator GUI 容器界面与心跳 Ticker</h3>
            </div>
            <div class="section-body grid-3">
              <div class="form-group">
                <label>加工周期时长 (Ticks: 20=1秒)</label>
                <input type="number" class="form-control" value="${c.processingTime}" min="1" step="20"
                       oninput="PromptUI.updateVal('processingTime', parseInt(this.value)||100)" />
              </div>
              <div class="form-group">
                <label>GUI 标题文本 (Menu Title)</label>
                <input type="text" class="form-control" value="${c.menuTitle || '加工中心'}" 
                       oninput="PromptUI.updateVal('menuTitle', this.value)" />
              </div>
              <div class="form-group">
                <label>GUI 进度条类型 (Progress Bar)</label>
                <select class="form-control" onchange="PromptUI.updateVal('progressBarType', this.value)">
                  <option value="ARROW_RIGHT" ${c.progressBarType === 'ARROW_RIGHT' ? 'selected' : ''}>向右水平箭头 (ARROW_RIGHT)</option>
                  <option value="FLAME_UP" ${c.progressBarType === 'FLAME_UP' ? 'selected' : ''}>向上燃烧火焰 (FLAME_UP)</option>
                  <option value="ENERGY_BAR" ${c.progressBarType === 'ENERGY_BAR' ? 'selected' : ''}>垂直能量电量柱 (ENERGY_BAR)</option>
                </select>
              </div>
            </div>

            <div style="display:flex; flex-wrap:wrap; gap:16px; margin-top:14px;">
              <label class="pill-checkbox">
                <input type="checkbox" ${c.hasLitState ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('hasLitState', this.checked)" />
                <span>具有工作点亮 LIT 状态机 (切换发光纹理)</span>
              </label>
              <label class="pill-checkbox">
                <input type="checkbox" ${c.syncDeltaPacket ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('syncDeltaPacket', this.checked)" />
                <span>增量网络数据包同步 (Client Delta Sync)</span>
              </label>
            </div>
          </div>

          <!-- 步骤 3：MCreator 素材地址选择绑定与构建 (高级模式专享) -->
          ${isAdv ? `
          <div class="config-section-card advanced-exclusive ${activeStep === 3 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag purple">步骤 3 · 高级模式专享</span>
              <h3>MCreator 素材地址选择与工程构建</h3>
            </div>

            <!-- GUI 贴图与音效素材 -->
            <div style="margin-bottom:20px;">
              <h4 style="margin:0 0 10px 0; font-size:14px; font-weight:600; color:var(--gemini-text);">
                GUI 背景贴图、JEI 兼容与运转音效地址绑定
              </h4>
              <div class="grid-2" style="gap:12px;">
                ${PromptUI.renderAssetPicker({
                  key: 'assetGuiTexture',
                  label: '容器 GUI 界面底图 (GUI Background PNG)',
                  hint: 'textures/gui/container/<machine>.png (256x256)',
                  value: c.assetGuiTexture || `assets/${modId}/textures/gui/container/${blockEntityId}.png`,
                  defaultPath: `assets/${modId}/textures/gui/container/${blockEntityId}.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/gui/container/`,
                  icon: 'image'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetJeiTexture',
                  label: 'JEI 配方界面背景底图 (JEI Recipe PNG, 可选)',
                  hint: 'textures/gui/jei/<machine>_recipe.png',
                  value: c.assetJeiTexture || `assets/${modId}/textures/gui/jei/${blockEntityId}_jei.png`,
                  defaultPath: `assets/${modId}/textures/gui/jei/${blockEntityId}_jei.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/gui/jei/`,
                  icon: 'image'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetWorkingSound',
                  label: '运转工作音效 (Working Sound Event)',
                  hint: '工作加工时循环播放',
                  value: c.assetWorkingSound || 'minecraft:block.blastfurnace.fire_crackle',
                  defaultPath: 'minecraft:block.blastfurnace.fire_crackle',
                  accept: '.ogg',
                  icon: 'audio'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetSpecialModel',
                  label: '方块实体 3D 特殊模型 (BER/GeckoLib, 可选)',
                  hint: '需动态旋转/机械动画时填写',
                  value: c.assetSpecialModel || '',
                  defaultPath: `assets/${modId}/geo/${blockEntityId}.geo.json`,
                  accept: '.json',
                  prefix: `assets/${modId}/geo/`,
                  icon: 'model'
                })}
              </div>
            </div>

            <!-- 数据构建规范 -->
            <div class="section-body grid-2">
              <div class="form-group">
                <label>代码生成策略</label>
                <select class="form-control" onchange="PromptUI.updateVal('codeGenerationStrategy', this.value)">
                  <option value="DATAGEN" ${c.codeGenerationStrategy === 'DATAGEN' ? 'selected' : ''}>Forge 自动化 DataGen 生成器</option>
                  <option value="STATIC_JSON" ${c.codeGenerationStrategy === 'STATIC_JSON' ? 'selected' : ''}>静态 JSON 资产文件</option>
                </select>
              </div>
              <div class="form-group">
                <label>客户端渲染模式</label>
                <select class="form-control" onchange="PromptUI.updateVal('clientRenderType', this.value)">
                  <option value="STATIC_BLOCK_MODEL" ${c.clientRenderType === 'STATIC_BLOCK_MODEL' ? 'selected' : ''}>静态烘焙方块模型 (高帧率推荐)</option>
                  <option value="BER_ANIMATED" ${c.clientRenderType === 'BER_ANIMATED' ? 'selected' : ''}>BER 动态实体渲染器 (BlockEntityRenderer)</option>
                </select>
              </div>
            </div>
          </div>
          ` : ''}
        </div>

        <div class="flow-footer-nav">
          <button class="btn-flow-nav" onclick="PromptUI.prevStep()" ${activeStep === 1 ? 'disabled' : ''}>上一步</button>
          <div class="flow-step-indicator">第 <strong>${activeStep}</strong> / ${isAdv ? 3 : 2} 步</div>
          <button class="btn-flow-nav primary" onclick="PromptUI.nextStep()">
            ${activeStep === (isAdv ? 3 : 2) ? '已就绪 · 查看预览' : '下一步'}
          </button>
        </div>
      `;
    },

    generateMarkdown(c, isAdv, skillPath) {
      const modId = (c.modId || 'custom_mod').trim().toLowerCase().replace(/[^a-z0-9_]/g, '_');
      const beId = (c.blockEntityId || 'custom_machine').trim().toLowerCase().replace(/[^a-z0-9_]/g, '_');
      const baseClass = beId.split('_').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('');
      const blockClass = baseClass + 'Block';
      const beClass = baseClass + 'BlockEntity';
      const menuClass = baseClass + 'Menu';
      const screenClass = baseClass + 'Screen';
      const nameZh = c.nameZh || '自定义加工机器';
      const nameEn = c.nameEn || 'Custom Machine';
      const mountedSkill = skillPath || 'd:/xiangmu/yezi/.agents/skills/moonmod/SKILL.md';

      let md = '';
      md += `# 项目核心规范与执行契约 (External Skill Mounting)\n`;
      md += `> **外部项目规范**：\`${mountedSkill}\`\n`;
      md += `> **执行指令**：开始编写代码前，必须优先完整读取并无条件遵循上述挂载的 Skill 规范文档（包括目标平台版本、Forge/NeoForge API 契约、分包命名结构、前后端分离以及 DataGen 规范）。\n\n`;

      md += `# 目标资产需求定义 (Target BlockEntity: ${nameZh})\n`;
      md += `请为模组实现全新机器方块实体：${nameZh} (${modId}:${beId})，严格对齐 MCreator 核心元素配置与工业级标准。\n\n`;

      md += `## 1. 基础标识与核心能力规格 (Capabilities & Specs)\n`;
      md += `- 资产标识：Mod ID: \`${modId}\` | 注册名: \`${beId}\` | 创造栏: \`${c.creativeTab || 'FUNCTIONAL_BLOCKS'}\`\n`;
      md += `- 本地化显示：中文: \`${nameZh}\` | 英文: \`${nameEn}\`\n`;
      md += `- 槽位规格：输入槽: \`${c.inputSlotCount}\` | 产出槽: \`${c.outputSlotCount}\` | 燃料/升级槽: \`${c.fuelSlotCount}\`\n`;
      if (c.hasEnergy) {
        md += `- 能量系统：FE 容量: \`${c.energyCapacity}\` FE | 单次传输: \`${c.maxEnergyTransfer}\` FE/t | 运行能耗: \`${c.energyPerTick}\` FE/t\n`;
      }
      if (c.hasFluid) {
        md += `- 流体系统：储罐容量: \`${c.fluidCapacity}\` mB | 支持流体: \`${c.supportedFluid}\`\n`;
      }
      md += `- 逻辑调度：心跳周期: \`${c.processingTime}\` ticks | LIT工作点亮状态机: \`${c.hasLitState ? '开启' : '关闭'}\` | 红石控制模式: \`${c.redstoneControl || 'IGNORED'}\`\n`;
      md += `- 自动化传输：侧面自动化: \`${c.sidedCapabilityConfig ? '开启 (顶部原料/底部产物/四周能量)' : '全向无差别'}\` | 破坏掉落内容物: \`${c.dropInventoryOnBreak ? '是' : '否'}\`\n\n`;

      // 高级模式素材绑定规范
      if (isAdv) {
        md += `# 素材与资源资产路径绑定规范 (Assets & Texture Mappings)\n`;
        md += `> **重要约束**：以下素材已在项目中完成绘制或存放，GUI 界面渲染类与音效注册**必须严格精准对齐下列相对路径**，严禁擅自修改或使用虚构的文件名：\n\n`;
        md += `- **容器 GUI 界面底图**：\`${c.assetGuiTexture || `assets/${modId}/textures/gui/container/${beId}.png`}\`\n`;
        if (c.assetJeiTexture) {
          md += `- **JEI 配方界面背景**：\`${c.assetJeiTexture}\`\n`;
        }
        md += `- **运转工作音效**：\`${c.assetWorkingSound || 'minecraft:block.blastfurnace.fire_crackle'}\`\n`;
        if (c.assetSpecialModel) {
          md += `- **方块特殊 3D 模型**：\`${c.assetSpecialModel}\`\n`;
        }
        md += `\n`;
      }

      md += `# 输出文件目录树规范 (Output Files Layout)\n`;
      md += `1. Java 业务代码：\n`;
      md += `   - \`src/main/java/com/example/${modId}/block/${blockClass}.java\`\n`;
      md += `   - \`src/main/java/com/example/${modId}/block/entity/${beClass}.java\`\n`;
      md += `   - \`src/main/java/com/example/${modId}/inventory/${menuClass}.java\`\n`;
      md += `   - \`src/main/java/com/example/${modId}/client/gui/${screenClass}.java\`\n`;
      md += `   - \`src/main/java/com/example/${modId}/registry/ModBlockEntities.java\`\n`;
      md += `   - \`src/main/java/com/example/${modId}/registry/ModMenus.java\`\n`;
      md += `2. 数据与资产层：\n`;
      md += `   - 语言文件：\`assets/${modId}/lang/zh_cn.json\`, \`assets/${modId}/lang/en_us.json\`\n\n`;

      if (c.conventionalCommit) {
        md += `# Git 规范提交信息\n`;
        md += `\`\`\`bash\ngit commit -m "feat(machine): add ${nameEn} (${beId}) with MCreator capabilities and GUI asset bindings"\n\`\`\`\n`;
      }

      return md;
    }
  };

  global.TypeBlockEntity = TypeBlockEntity;
  if (global.TypeRegistry) {
    global.TypeRegistry.register('block_entity', TypeBlockEntity);
  }
})(window);
