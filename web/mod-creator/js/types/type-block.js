// ============================================================
// type-block.js — 方块资产类型模块 (Block)
// 结合 MCreator 核心参数模型深度重构，支持高级模式素材地址选择与资产路径绑定
// 严格遵守 Google Gemini 规范与 Zero-Emoji 规则
// ============================================================

(function (global) {
  'use strict';

  const DEFAULT_CONFIG = {
    // 基础标识与命名空间 (技术栈与平台规范由外部挂载 Skill 统一管辖)
    modId: 'custom_mod',
    blockId: 'custom_block',
    nameZh: '自定义方块',
    nameEn: 'Custom Block',
    creativeTab: 'BUILDING_BLOCKS',

    // MCreator 视觉模式与素材地址绑定 (高级模式重点)
    textureMode: 'SINGLE', // SINGLE (单面同材质) | MULTI_FACE (六面独立) | PILLAR (柱状) | CUSTOM_MODEL (自定义模型)
    assetTextureAll: 'assets/custom_mod/textures/block/custom_block.png',
    assetTextureTop: 'assets/custom_mod/textures/block/custom_block_top.png',
    assetTextureBottom: 'assets/custom_mod/textures/block/custom_block_bottom.png',
    assetTextureFront: 'assets/custom_mod/textures/block/custom_block_front.png',
    assetTextureBack: 'assets/custom_mod/textures/block/custom_block_back.png',
    assetTextureLeft: 'assets/custom_mod/textures/block/custom_block_left.png',
    assetTextureRight: 'assets/custom_mod/textures/block/custom_block_right.png',
    assetCustomModel: 'assets/custom_mod/models/block/custom_block.json',
    assetBreakSound: 'minecraft:block.stone.break',

    // MCreator 物理属性与环境交互
    materialSound: 'STONE', // STONE | METAL | WOOD | GLASS | NETHERITE_BLOCK | SLIME | GRAVEL | GRASS
    hardness: 1.5,
    resistance: 6.0,
    lightLevel: 0, // 0-15
    opacity: 15, // 0-15 (15完全不透光，0完全透明)
    friction: 0.6, // 0.6 标准, 0.98 冰面
    speedFactor: 1.0, // 灵魂沙 0.4
    jumpFactor: 1.0,
    hasGravity: false, // 类似沙子/沙砾下落
    isPassable: false, // 无碰撞箱可直接穿过 (类似高草丛)
    isWaterloggable: false, // 支持含水特性 (Waterlogged)
    isClimbable: false, // 允许攀爬 (梯子/脚手架特性)
    contactDamage: 0, // 踩踏/接触伤害 (类似仙人掌/岩浆块)
    pistonPushReaction: 'NORMAL', // NORMAL | BLOCK | DESTROY | PUSH_ONLY
    flammability: 0, // 0-300
    fireSpreadSpeed: 0, // 0-100

    // 采掘与掉落规则 (MCreator Harvest & Drop)
    requiresTool: true,
    toolType: 'pickaxe', // pickaxe | axe | shovel | hoe | multi_tool
    toolTier: 'stone', // wood | stone | iron | diamond | netherite
    dropsSelf: true,
    dropCustomItem: '',
    dropCountMin: 1,
    dropCountMax: 1,
    fortuneBonus: false,
    silkTouchAlternative: true,
    dropExperienceMin: 0,
    dropExperienceMax: 0,

    // 外观、渲染管线与状态机
    renderLayer: 'Solid', // Solid | Cutout | CutoutMipped | Translucent
    cullIdenticalFaces: true,
    hasFacing: false,
    facingType: 'HORIZONTAL_FACING', // HORIZONTAL_FACING | FACING
    hasDynamicState: false,
    dynamicStateProps: 'LIT',

    // 交互与触发器 (MCreator Procedures / Triggers)
    hasRightClickAction: false,
    rightClickDescription: '',
    hasStepOnAction: false,
    stepOnDescription: '',
    hasNeighborUpdate: false,

    // 高阶工业级系统
    voxelShapeType: 'FULL_CUBE', // FULL_CUBE | CUSTOM_VOXEL
    precomputeRotations: true,
    redstoneRole: 'NONE', // NONE | CONDUCTOR | EMITTER | COMPARATOR
    signalStrength: 0,
    hasBlockEntity: false,
    hasTicker: false,
    codeGenerationStrategy: 'DATAGEN',
    conventionalCommit: true
  };

  const PRESETS = {
    standard_stone: {
      name: '标准建筑方块 (全贴图一致)',
      desc: '标准的六面同贴图建筑方块，完整立方体碰撞箱，石镐挖掘掉落自身。',
      mode: 'simple',
      data: {
        ...DEFAULT_CONFIG,
        blockId: 'polished_granite_pillar',
        nameZh: '花岗岩石柱',
        nameEn: 'Polished Granite Pillar',
        materialSound: 'STONE',
        hardness: 1.8,
        resistance: 6.0,
        textureMode: 'SINGLE',
        assetTextureAll: 'assets/custom_mod/textures/block/polished_granite_pillar.png'
      }
    },
    glowing_gem_ore: {
      name: '深层发光矿石 (时运与精准采集)',
      desc: '发光矿石，受铁镐采掘约束，受时运增益影响，精准采集掉落原矿，默认掉落宝石碎片。',
      mode: 'advanced',
      data: {
        ...DEFAULT_CONFIG,
        blockId: 'astral_crystal_ore',
        nameZh: '星辉水晶矿石',
        nameEn: 'Astral Crystal Ore',
        materialSound: 'STONE',
        hardness: 3.5,
        resistance: 9.0,
        lightLevel: 9,
        requiresTool: true,
        toolType: 'pickaxe',
        toolTier: 'iron',
        dropsSelf: false,
        dropCustomItem: 'astral_crystal_shard',
        dropCountMin: 1,
        dropCountMax: 3,
        fortuneBonus: true,
        silkTouchAlternative: true,
        dropExperienceMin: 2,
        dropExperienceMax: 6,
        textureMode: 'SINGLE',
        assetTextureAll: 'assets/custom_mod/textures/block/astral_crystal_ore.png'
      }
    },
    carved_pillar_furnace: {
      name: '六面异构带朝向熔炉方块 (Pillar/Multi-Face)',
      desc: '顶部、底部、侧面不同材质贴图，带水平四向朝向与右键点火状态机。',
      mode: 'advanced',
      data: {
        ...DEFAULT_CONFIG,
        blockId: 'runic_altar_block',
        nameZh: '符文熔铸祭坛',
        nameEn: 'Runic Altar Block',
        materialSound: 'STONE',
        hardness: 3.0,
        resistance: 10.0,
        textureMode: 'MULTI_FACE',
        hasFacing: true,
        facingType: 'HORIZONTAL_FACING',
        hasDynamicState: true,
        dynamicStateProps: 'LIT, POWERED',
        assetTextureTop: 'assets/custom_mod/textures/block/runic_altar_top.png',
        assetTextureBottom: 'assets/custom_mod/textures/block/runic_altar_bottom.png',
        assetTextureFront: 'assets/custom_mod/textures/block/runic_altar_front.png',
        assetTextureBack: 'assets/custom_mod/textures/block/runic_altar_side.png',
        assetTextureLeft: 'assets/custom_mod/textures/block/runic_altar_side.png',
        assetTextureRight: 'assets/custom_mod/textures/block/runic_altar_side.png'
      }
    }
  };

  const TypeBlock = {
    id: 'block',
    label: '方块 (Block)',
    description: '三维建筑、功能机巧或矿石方块，具备体素碰撞、材质贴图绑定与采掘掉落规则',
    iconName: 'mc_block',
    defaultConfig: DEFAULT_CONFIG,
    presets: PRESETS,

    renderConfigFlow(c, isAdv, activeStep) {
      const modId = c.modId || 'custom_mod';
      const blockId = c.blockId || 'custom_block';

      return `
        <div class="gemini-assistant-bubble">
          <div class="assistant-avatar">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 2C12 7.523 7.523 12 2 12C7.523 12 12 16.477 12 22C12 16.477 16.477 12 22 12C16.477 12 12 7.523 12 2Z"/></svg>
          </div>
          <div class="assistant-content">
            <div class="assistant-title">${isAdv ? '正在进行 [高级模式] MCreator 架构级方块配置' : '正在进行 [普通模式] 极简速填'}</div>
            <div class="assistant-desc">
              ${isAdv 
                ? '已开启 MCreator 规范全量物理、贴图模式与素材地址绑定。支持直接指定或浏览选择本地纹理与模型资产。' 
                : '只需输入方块名称、材质音效、硬度与掉落规则。未填写项将自动赋予原版标准岩石参数。'}
            </div>
          </div>
          <div class="bubble-actions">
            <button class="btn-bubble-preset" onclick="PromptUI.showPresetPicker()">套用经典模板</button>
          </div>
        </div>

        <div class="flow-step-bar">
          <div class="step-item ${activeStep >= 1 ? 'active' : ''}" onclick="PromptUI.goToStep(1)">
            <span class="step-num">1</span>
            <span class="step-name">核心物理与采掘</span>
          </div>
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 2 ? 'active' : ''}" onclick="PromptUI.goToStep(2)">
            <span class="step-num">2</span>
            <span class="step-name">外观状态与交互</span>
          </div>
          ${isAdv ? `
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 3 ? 'active' : ''}" onclick="PromptUI.goToStep(3)">
            <span class="step-num">3</span>
            <span class="step-name">MCreator 素材绑定</span>
          </div>` : ''}
        </div>

        <div class="config-grid-sections">
          <!-- 步骤 1：核心物理与采掘规格 (含资产核心注册标识，技术栈平台由总Skill统一挂载) -->
          <div class="config-section-card ${activeStep === 1 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 1</span>
              <h3>方块核心标识与 MCreator 物理采掘规则</h3>
              <span class="section-status-hint">底层规范由外部 Skill 统一接管</span>
            </div>

            <!-- 核心标识紧凑栏 -->
            <div class="grid-3 mb-3" style="margin-bottom:14px;">
              <div class="form-group">
                <label>注册英文 ID (snake_case) <span class="required">*</span></label>
                <input type="text" class="form-control" value="${c.blockId || 'custom_block'}" 
                       placeholder="如 astral_crystal_ore" oninput="PromptUI.updateVal('blockId', this.value)" />
              </div>
              <div class="form-group">
                <label>中文显示名</label>
                <input type="text" class="form-control" value="${c.nameZh || '自定义方块'}" 
                       placeholder="如：星辉水晶矿石" oninput="PromptUI.updateVal('nameZh', this.value); PromptUI.syncOmniSearch(this.value);" />
              </div>
              <div class="form-group">
                <label>创造模式标签栏 (Creative Tab)</label>
                <select class="form-control" onchange="PromptUI.updateVal('creativeTab', this.value)">
                  <option value="BUILDING_BLOCKS" ${c.creativeTab === 'BUILDING_BLOCKS' ? 'selected' : ''}>建筑方块 (Building Blocks)</option>
                  <option value="NATURAL_BLOCKS" ${c.creativeTab === 'NATURAL_BLOCKS' ? 'selected' : ''}>天然方块 (Natural Blocks)</option>
                  <option value="FUNCTIONAL_BLOCKS" ${c.creativeTab === 'FUNCTIONAL_BLOCKS' ? 'selected' : ''}>功能性方块 (Functional Blocks)</option>
                  <option value="REDSTONE_BLOCKS" ${c.creativeTab === 'REDSTONE_BLOCKS' ? 'selected' : ''}>红石元件 (Redstone)</option>
                </select>
              </div>
            </div>
            </div>
            <div class="section-body grid-3">
              <div class="form-group">
                <label>材质与触感音效 (SoundType)</label>
                <select class="form-control" onchange="PromptUI.updateVal('materialSound', this.value)">
                  <option value="STONE" ${c.materialSound === 'STONE' ? 'selected' : ''}>石头 (STONE)</option>
                  <option value="METAL" ${c.materialSound === 'METAL' ? 'selected' : ''}>金属 (METAL)</option>
                  <option value="WOOD" ${c.materialSound === 'WOOD' ? 'selected' : ''}>木材 (WOOD)</option>
                  <option value="GLASS" ${c.materialSound === 'GLASS' ? 'selected' : ''}>玻璃 (GLASS)</option>
                  <option value="SLIME" ${c.materialSound === 'SLIME' ? 'selected' : ''}>黏液 (SLIME)</option>
                  <option value="GRAVEL" ${c.materialSound === 'GRAVEL' ? 'selected' : ''}>沙砾 (GRAVEL)</option>
                  <option value="NETHERITE_BLOCK" ${c.materialSound === 'NETHERITE_BLOCK' ? 'selected' : ''}>下界合金 (NETHERITE)</option>
                </select>
              </div>
              <div class="form-group">
                <label>采掘硬度 (Hardness: 0-50)</label>
                <div class="slider-wrapper">
                  <input type="range" min="0.5" max="50" step="0.5" value="${c.hardness}" 
                         oninput="PromptUI.updateVal('hardness', parseFloat(this.value)); document.getElementById('valHardness').innerText = this.value;" />
                  <span class="slider-val" id="valHardness">${c.hardness}</span>
                </div>
              </div>
              <div class="form-group">
                <label>抗爆阻力 (Resistance: 0-1200)</label>
                <input type="number" class="form-control" value="${c.resistance}" step="1"
                       oninput="PromptUI.updateVal('resistance', parseFloat(this.value) || 6.0)" />
              </div>
              <div class="form-group">
                <label>发光等级 (0-15)</label>
                <div class="slider-wrapper">
                  <input type="range" min="0" max="15" step="1" value="${c.lightLevel}" 
                         oninput="PromptUI.updateVal('lightLevel', parseInt(this.value)); document.getElementById('valLight').innerText = this.value;" />
                  <span class="slider-val" id="valLight">${c.lightLevel}</span>
                </div>
              </div>
              <div class="form-group">
                <label>有效采掘工具</label>
                <select class="form-control" onchange="PromptUI.updateVal('toolType', this.value)">
                  <option value="pickaxe" ${c.toolType === 'pickaxe' ? 'selected' : ''}>镐子 (Pickaxe)</option>
                  <option value="axe" ${c.toolType === 'axe' ? 'selected' : ''}>斧头 (Axe)</option>
                  <option value="shovel" ${c.toolType === 'shovel' ? 'selected' : ''}>铲子 (Shovel)</option>
                  <option value="hoe" ${c.toolType === 'hoe' ? 'selected' : ''}>锄头 (Hoe)</option>
                </select>
              </div>
              <div class="form-group">
                <label>最低工具等级限制</label>
                <select class="form-control" onchange="PromptUI.updateVal('toolTier', this.value)">
                  <option value="wood" ${c.toolTier === 'wood' ? 'selected' : ''}>木制及以上 (Wood / Level 0)</option>
                  <option value="stone" ${c.toolTier === 'stone' ? 'selected' : ''}>石制及以上 (Stone / Level 1)</option>
                  <option value="iron" ${c.toolTier === 'iron' ? 'selected' : ''}>铁制及以上 (Iron / Level 2)</option>
                  <option value="diamond" ${c.toolTier === 'diamond' ? 'selected' : ''}>钻石级及以上 (Diamond / Level 3)</option>
                  <option value="netherite" ${c.toolTier === 'netherite' ? 'selected' : ''}>下界合金级 (Netherite / Level 4)</option>
                </select>
              </div>
              <div class="form-group">
                <label>掉落规则</label>
                <select class="form-control" onchange="PromptUI.updateVal('dropsSelf', this.value === 'true'); PromptUI.rerenderFlow();">
                  <option value="true" ${c.dropsSelf ? 'selected' : ''}>掉落方块自身</option>
                  <option value="false" ${!c.dropsSelf ? 'selected' : ''}>掉落独立物品/碎片</option>
                </select>
              </div>
              ${!c.dropsSelf ? `
              <div class="form-group">
                <label>自定义掉落物 ID</label>
                <input type="text" class="form-control" value="${c.dropCustomItem || ''}" 
                       placeholder="如 ${modId}:gem_shard" oninput="PromptUI.updateVal('dropCustomItem', this.value)" />
              </div>
              <div class="form-group">
                <label>掉落数量范围 (最小 ~ 最大)</label>
                <div style="display:flex; gap:8px;">
                  <input type="number" class="form-control" style="width:50%" value="${c.dropCountMin || 1}" min="1" 
                         oninput="PromptUI.updateVal('dropCountMin', parseInt(this.value)||1)" />
                  <input type="number" class="form-control" style="width:50%" value="${c.dropCountMax || 1}" min="1" 
                         oninput="PromptUI.updateVal('dropCountMax', parseInt(this.value)||1)" />
                </div>
              </div>
              ` : ''}
              <div class="form-group">
                <label>活塞推动反应 (Piston Reaction)</label>
                <select class="form-control" onchange="PromptUI.updateVal('pistonPushReaction', this.value)">
                  <option value="NORMAL" ${c.pistonPushReaction === 'NORMAL' ? 'selected' : ''}>NORMAL (正常被活塞推动/拉回)</option>
                  <option value="BLOCK" ${c.pistonPushReaction === 'BLOCK' ? 'selected' : ''}>BLOCK (阻挡活塞，不可推动)</option>
                  <option value="DESTROY" ${c.pistonPushReaction === 'DESTROY' ? 'selected' : ''}>DESTROY (被活塞推动时摧毁掉落)</option>
                  <option value="PUSH_ONLY" ${c.pistonPushReaction === 'PUSH_ONLY' ? 'selected' : ''}>PUSH_ONLY (仅允许推动，不可拉动)</option>
                </select>
              </div>
            </div>

            <!-- MCreator 特性勾选矩阵 -->
            <div style="display:flex; flex-wrap:wrap; gap:16px; margin-top:14px;">
              <label class="pill-checkbox">
                <input type="checkbox" ${c.hasGravity ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('hasGravity', this.checked)" />
                <span>具有重力物理 (类似沙子下落)</span>
              </label>
              <label class="pill-checkbox">
                <input type="checkbox" ${c.isPassable ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('isPassable', this.checked)" />
                <span>允许穿透 (无碰撞箱/像草丛)</span>
              </label>
              <label class="pill-checkbox">
                <input type="checkbox" ${c.fortuneBonus ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('fortuneBonus', this.checked)" />
                <span>受时运附魔 (Fortune) 掉落加成</span>
              </label>
              <label class="pill-checkbox">
                <input type="checkbox" ${c.silkTouchAlternative ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('silkTouchAlternative', this.checked)" />
                <span>精准采集 (Silk Touch) 掉落方块原物</span>
              </label>
              <label class="pill-checkbox">
                <input type="checkbox" ${c.isWaterloggable ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('isWaterloggable', this.checked)" />
                <span>含水特性 (Waterloggable)</span>
              </label>
              <label class="pill-checkbox">
                <input type="checkbox" ${c.isClimbable ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('isClimbable', this.checked)" />
                <span>允许攀爬 (Climbable)</span>
              </label>
            </div>
          </div>

          <!-- 步骤 2：外观渲染、朝向与交互触发 -->
          <div class="config-section-card ${activeStep === 2 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 2</span>
              <h3>状态机、外观渲染与交互事件</h3>
            </div>
            <div class="section-body grid-2">
              <div class="form-group">
                <label>渲染图层管线 (Render Type)</label>
                <select class="form-control" onchange="PromptUI.updateVal('renderLayer', this.value)">
                  <option value="Solid" ${c.renderLayer === 'Solid' ? 'selected' : ''}>Solid (实心不透明，默认)</option>
                  <option value="Cutout" ${c.renderLayer === 'Cutout' ? 'selected' : ''}>Cutout (镂空透明，树叶/铁栅栏/植物)</option>
                  <option value="CutoutMipped" ${c.renderLayer === 'CutoutMipped' ? 'selected' : ''}>CutoutMipped (带多级纹理的镂空)</option>
                  <option value="Translucent" ${c.renderLayer === 'Translucent' ? 'selected' : ''}>Translucent (半透明混合，彩色玻璃/冰块)</option>
                </select>
              </div>
              <div class="form-group">
                <label>方块朝向特性 (Facing)</label>
                <select class="form-control" onchange="PromptUI.updateVal('hasFacing', this.value !== 'NONE'); PromptUI.updateVal('facingType', this.value);">
                  <option value="NONE" ${!c.hasFacing ? 'selected' : ''}>无朝向 (各向同性六面一致)</option>
                  <option value="HORIZONTAL_FACING" ${c.facingType === 'HORIZONTAL_FACING' ? 'selected' : ''}>水平四向 (朝向玩家，熔炉/箱子)</option>
                  <option value="FACING" ${c.facingType === 'FACING' ? 'selected' : ''}>全向六向 (支持上下全方位，投掷器/活塞)</option>
                </select>
              </div>
              <div class="form-group">
                <label>玩家右键交互事件 (use / onUse)</label>
                <label class="pill-checkbox">
                  <input type="checkbox" ${c.hasRightClickAction ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('hasRightClickAction', this.checked); PromptUI.rerenderFlow();" />
                  <span>启用右键交互逻辑</span>
                </label>
                ${c.hasRightClickAction ? `
                  <textarea class="form-control mt-2" rows="2" 
                            placeholder="如：右键切换激活点亮状态，并播放打火音效与粒子"
                            oninput="PromptUI.updateVal('rightClickDescription', this.value)">${c.rightClickDescription || ''}</textarea>
                ` : ''}
              </div>
              <div class="form-group">
                <label>实体踩踏事件 (stepOn)</label>
                <label class="pill-checkbox">
                  <input type="checkbox" ${c.hasStepOnAction ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('hasStepOnAction', this.checked); PromptUI.rerenderFlow();" />
                  <span>启用踩踏触发</span>
                </label>
                ${c.hasStepOnAction ? `
                  <textarea class="form-control mt-2" rows="2" 
                            placeholder="如：踩踏时给予实体减速或弹跳效果"
                            oninput="PromptUI.updateVal('stepOnDescription', this.value)">${c.stepOnDescription || ''}</textarea>
                ` : ''}
              </div>
            </div>
          </div>

          <!-- 步骤 3：MCreator 素材地址选择绑定 + 高阶微观子系统 (高级模式专享) -->
          ${isAdv ? `
          <div class="config-section-card advanced-exclusive ${activeStep === 3 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag purple">步骤 3 · 高级模式专享</span>
              <h3>MCreator 素材地址选择与高阶微观子系统</h3>
            </div>

            <!-- 1. MCreator 素材地址绑定模块 -->
            <div style="margin-bottom:20px;">
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
                <h4 style="margin:0; font-size:14px; font-weight:600; color:var(--gemini-text);">
                  相关素材地址绑定 (Textures & Models)
                </h4>
                <div class="form-group" style="flex-direction:row; align-items:center; gap:8px;">
                  <span style="font-size:12px; color:var(--gemini-text-secondary);">贴图映射模式：</span>
                  <select class="form-control" style="padding:4px 8px; font-size:12px;" 
                          onchange="PromptUI.updateVal('textureMode', this.value); PromptUI.rerenderFlow();">
                    <option value="SINGLE" ${c.textureMode === 'SINGLE' ? 'selected' : ''}>单贴图全立方体 (Single All)</option>
                    <option value="MULTI_FACE" ${c.textureMode === 'MULTI_FACE' ? 'selected' : ''}>六面独立贴图 (Multi-Face)</option>
                    <option value="PILLAR" ${c.textureMode === 'PILLAR' ? 'selected' : ''}>柱状贴图 (Top/Bottom + Side)</option>
                    <option value="CUSTOM_MODEL" ${c.textureMode === 'CUSTOM_MODEL' ? 'selected' : ''}>自定义外部 JSON 模型</option>
                  </select>
                </div>
              </div>

              <!-- 根据贴图模式渲染素材选择器 -->
              ${c.textureMode === 'SINGLE' ? `
                ${PromptUI.renderAssetPicker({
                  key: 'assetTextureAll',
                  label: '方块全局贴图 (All Sides Texture)',
                  hint: '六面统一贴图 (16x16 PNG)',
                  value: c.assetTextureAll || `assets/${modId}/textures/block/${blockId}.png`,
                  defaultPath: `assets/${modId}/textures/block/${blockId}.png`,
                  placeholder: `assets/${modId}/textures/block/${blockId}.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/block/`,
                  icon: 'image'
                })}
              ` : c.textureMode === 'PILLAR' ? `
                <div class="grid-2">
                  ${PromptUI.renderAssetPicker({
                    key: 'assetTextureTop',
                    label: '顶底两面贴图 (End Texture)',
                    hint: '顶部与底部通用材质',
                    value: c.assetTextureTop || `assets/${modId}/textures/block/${blockId}_top.png`,
                    defaultPath: `assets/${modId}/textures/block/${blockId}_top.png`,
                    accept: '.png',
                    prefix: `assets/${modId}/textures/block/`,
                    icon: 'image'
                  })}
                  ${PromptUI.renderAssetPicker({
                    key: 'assetTextureFront',
                    label: '四周侧面贴图 (Side Texture)',
                    hint: '水平四个侧面共用贴图',
                    value: c.assetTextureFront || `assets/${modId}/textures/block/${blockId}_side.png`,
                    defaultPath: `assets/${modId}/textures/block/${blockId}_side.png`,
                    accept: '.png',
                    prefix: `assets/${modId}/textures/block/`,
                    icon: 'image'
                  })}
                </div>
              ` : c.textureMode === 'MULTI_FACE' ? `
                <div class="grid-2" style="gap:10px;">
                  ${PromptUI.renderAssetPicker({
                    key: 'assetTextureTop',
                    label: '顶部贴图 (Top)',
                    hint: '顶部 (+Y)',
                    value: c.assetTextureTop || `assets/${modId}/textures/block/${blockId}_top.png`,
                    defaultPath: `assets/${modId}/textures/block/${blockId}_top.png`,
                    accept: '.png',
                    prefix: `assets/${modId}/textures/block/`,
                    icon: 'image'
                  })}
                  ${PromptUI.renderAssetPicker({
                    key: 'assetTextureBottom',
                    label: '底部贴图 (Bottom)',
                    hint: '底部 (-Y)',
                    value: c.assetTextureBottom || `assets/${modId}/textures/block/${blockId}_bottom.png`,
                    defaultPath: `assets/${modId}/textures/block/${blockId}_bottom.png`,
                    accept: '.png',
                    prefix: `assets/${modId}/textures/block/`,
                    icon: 'image'
                  })}
                  ${PromptUI.renderAssetPicker({
                    key: 'assetTextureFront',
                    label: '正面贴图 (North / Front)',
                    hint: '朝北正面 (-Z)',
                    value: c.assetTextureFront || `assets/${modId}/textures/block/${blockId}_front.png`,
                    defaultPath: `assets/${modId}/textures/block/${blockId}_front.png`,
                    accept: '.png',
                    prefix: `assets/${modId}/textures/block/`,
                    icon: 'image'
                  })}
                  ${PromptUI.renderAssetPicker({
                    key: 'assetTextureBack',
                    label: '背面贴图 (South / Back)',
                    hint: '朝南背面 (+Z)',
                    value: c.assetTextureBack || `assets/${modId}/textures/block/${blockId}_back.png`,
                    defaultPath: `assets/${modId}/textures/block/${blockId}_back.png`,
                    accept: '.png',
                    prefix: `assets/${modId}/textures/block/`,
                    icon: 'image'
                  })}
                  ${PromptUI.renderAssetPicker({
                    key: 'assetTextureLeft',
                    label: '左侧贴图 (West / Left)',
                    hint: '朝西左面 (-X)',
                    value: c.assetTextureLeft || `assets/${modId}/textures/block/${blockId}_left.png`,
                    defaultPath: `assets/${modId}/textures/block/${blockId}_left.png`,
                    accept: '.png',
                    prefix: `assets/${modId}/textures/block/`,
                    icon: 'image'
                  })}
                  ${PromptUI.renderAssetPicker({
                    key: 'assetTextureRight',
                    label: '右侧贴图 (East / Right)',
                    hint: '朝东右面 (+X)',
                    value: c.assetTextureRight || `assets/${modId}/textures/block/${blockId}_right.png`,
                    defaultPath: `assets/${modId}/textures/block/${blockId}_right.png`,
                    accept: '.png',
                    prefix: `assets/${modId}/textures/block/`,
                    icon: 'image'
                  })}
                </div>
              ` : `
                <!-- 自定义外部 JSON 模型 -->
                <div class="grid-2">
                  ${PromptUI.renderAssetPicker({
                    key: 'assetCustomModel',
                    label: '自定义 Block 模型 JSON 地址',
                    hint: 'BlockBench 导出的 Java Block/Item 模型 JSON',
                    value: c.assetCustomModel || `assets/${modId}/models/block/${blockId}.json`,
                    defaultPath: `assets/${modId}/models/block/${blockId}.json`,
                    accept: '.json',
                    prefix: `assets/${modId}/models/block/`,
                    icon: 'model'
                  })}
                  ${PromptUI.renderAssetPicker({
                    key: 'assetTextureAll',
                    label: '关联材质贴图文件',
                    hint: '模型引用的主要材质贴图 PNG',
                    value: c.assetTextureAll || `assets/${modId}/textures/block/${blockId}.png`,
                    defaultPath: `assets/${modId}/textures/block/${blockId}.png`,
                    accept: '.png',
                    prefix: `assets/${modId}/textures/block/`,
                    icon: 'image'
                  })}
                </div>
              `}
            </div>

            <!-- 2. 高阶微观子系统 (体素、红石、DataGen) -->
            <div class="section-body grid-3">
              <div class="form-group">
                <label>体素碰撞箱类型</label>
                <select class="form-control" onchange="PromptUI.updateVal('voxelShapeType', this.value)">
                  <option value="FULL_CUBE" ${c.voxelShapeType === 'FULL_CUBE' ? 'selected' : ''}>标准完整立方体 (1x1x1)</option>
                  <option value="CUSTOM_VOXEL" ${c.voxelShapeType === 'CUSTOM_VOXEL' ? 'selected' : ''}>定制复合体素切分 (VoxelShape)</option>
                </select>
              </div>
              <div class="form-group">
                <label>红石网络拓扑角色</label>
                <select class="form-control" onchange="PromptUI.updateVal('redstoneRole', this.value)">
                  <option value="NONE" ${c.redstoneRole === 'NONE' ? 'selected' : ''}>绝缘体 (不传导红石)</option>
                  <option value="CONDUCTOR" ${c.redstoneRole === 'CONDUCTOR' ? 'selected' : ''}>红石导体 (正常传导充能)</option>
                  <option value="EMITTER" ${c.redstoneRole === 'EMITTER' ? 'selected' : ''}>红石信号源 (向四周输出)</option>
                  <option value="COMPARATOR" ${c.redstoneRole === 'COMPARATOR' ? 'selected' : ''}>模拟输出端 (支持比较器)</option>
                </select>
              </div>
              <div class="form-group">
                <label>代码资产生成策略</label>
                <select class="form-control" onchange="PromptUI.updateVal('codeGenerationStrategy', this.value)">
                  <option value="DATAGEN" ${c.codeGenerationStrategy === 'DATAGEN' ? 'selected' : ''}>Forge 自动化 DataGen 生成器</option>
                  <option value="STATIC_JSON" ${c.codeGenerationStrategy === 'STATIC_JSON' ? 'selected' : ''}>静态 JSON 资产文件</option>
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
      const blockId = (c.blockId || 'custom_block').trim().toLowerCase().replace(/[^a-z0-9_]/g, '_');
      const blockClass = blockId.split('_').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('') + 'Block';
      const nameZh = c.nameZh || '自定义方块';
      const nameEn = c.nameEn || 'Custom Block';
      const mountedSkill = skillPath || 'd:/xiangmu/yezi/.agents/skills/moonmod/SKILL.md';

      let md = '';
      md += `# 项目核心规范与执行契约 (External Skill Mounting)\n`;
      md += `> **外部项目规范**：\`${mountedSkill}\`\n`;
      md += `> **执行指令**：开始编写代码前，必须优先完整读取并无条件遵循上述挂载的 Skill 规范文档（包括目标平台版本、Forge/NeoForge API 契约、分包命名结构、前后端分离以及 DataGen 规范）。\n\n`;

      md += `# 目标资产需求定义 (Target Block: ${nameZh})\n`;
      md += `请为模组实现全新方块：${nameZh} (${modId}:${blockId})，严格对齐 MCreator 核心元素配置与工程标准。\n\n`;

      md += `## 1. 基础标识与物理采掘规格 (Specifications)\n`;
      md += `- 资产标识：Mod ID: \`${modId}\` | 注册名: \`${blockId}\` | 创造栏: \`${c.creativeTab || 'BUILDING_BLOCKS'}\`\n`;
      md += `- 本地化显示：中文: \`${nameZh}\` | 英文: \`${nameEn}\`\n`;
      md += `- 物理属性：SoundType.${c.materialSound || 'STONE'} | 硬度: \`${c.hardness}\` | 抗爆阻力: \`${c.resistance}\` | 发光等级: \`${c.lightLevel || 0}\`\n`;
      md += `- MCreator 状态特性：含水支持 (Waterlogged): \`${c.isWaterloggable ? '是' : '否'}\` | 攀爬特性 (Climbable): \`${c.isClimbable ? '是 (类似脚手架/梯子)' : '否'}\` | 渲染管线: \`${c.renderLayer || 'Solid'}\`\n`;
      md += `- 活塞与重力：活塞反应: \`PushReaction.${c.pistonPushReaction || 'NORMAL'}\` | 重力物理: \`${c.hasGravity ? '是 (类似沙子受重力下落)' : '否'}\` | 穿透性: \`${c.isPassable ? '可自由穿透 (无碰撞)' : '完整固体'}\`\n`;
      md += `- 采掘限制：`;
      if (c.requiresTool) {
        md += `必须使用 [${c.toolType || 'pickaxe'}] 且最低等级为 [${c.toolTier || 'stone'}]；`;
      } else {
        md += `无需特殊工具，空手可挖掘；`;
      }
      if (c.dropsSelf) {
        md += `破坏后掉落方块自身。`;
      } else {
        md += `破坏后掉落 [${c.dropCustomItem || (blockId + '_drop')}]，数量 [${c.dropCountMin || 1}~${c.dropCountMax || 1}]。`;
      }
      if (c.fortuneBonus) md += ` (受时运 Fortune 加成)`;
      if (c.silkTouchAlternative) md += ` (精准采集 Silk Touch 掉落方块自身)`;
      md += `\n\n`;

      // 高级模式下的素材地址选择绑定
      if (isAdv) {
        md += `# 素材与资源资产路径绑定规范 (Assets & Texture Mappings)\n`;
        md += `> **重要约束**：以下素材已在项目中完成绘制或存放，DataGen 代码、BlockState 与模型定义**必须严格精准对齐下列相对路径**，严禁擅自捏造或使用默认占位图：\n\n`;
        md += `- **贴图映射模式**：\`${c.textureMode || 'SINGLE'}\`\n`;
        if (c.textureMode === 'SINGLE') {
          md += `- **方块全向主贴图**：\`${c.assetTextureAll || `assets/${modId}/textures/block/${blockId}.png`}\`\n`;
        } else if (c.textureMode === 'PILLAR') {
          md += `- **顶底面贴图 (End)**：\`${c.assetTextureTop || `assets/${modId}/textures/block/${blockId}_top.png`}\`\n`;
          md += `- **四周侧面贴图 (Side)**：\`${c.assetTextureFront || `assets/${modId}/textures/block/${blockId}_side.png`}\`\n`;
        } else if (c.textureMode === 'MULTI_FACE') {
          md += `- **顶部贴图 (Top)**：\`${c.assetTextureTop || `assets/${modId}/textures/block/${blockId}_top.png`}\`\n`;
          md += `- **底部贴图 (Bottom)**：\`${c.assetTextureBottom || `assets/${modId}/textures/block/${blockId}_bottom.png`}\`\n`;
          md += `- **正面贴图 (North)**：\`${c.assetTextureFront || `assets/${modId}/textures/block/${blockId}_front.png`}\`\n`;
          md += `- **背面贴图 (South)**：\`${c.assetTextureBack || `assets/${modId}/textures/block/${blockId}_back.png`}\`\n`;
          md += `- **左侧贴图 (West)**：\`${c.assetTextureLeft || `assets/${modId}/textures/block/${blockId}_left.png`}\`\n`;
          md += `- **右侧贴图 (East)**：\`${c.assetTextureRight || `assets/${modId}/textures/block/${blockId}_right.png`}\`\n`;
        } else if (c.textureMode === 'CUSTOM_MODEL') {
          md += `- **自定义模型 JSON 路径**：\`${c.assetCustomModel || `assets/${modId}/models/block/${blockId}.json`}\`\n`;
          md += `- **关联材质贴图**：\`${c.assetTextureAll || `assets/${modId}/textures/block/${blockId}.png`}\`\n`;
        }
        md += `- **采掘音效**：\`${c.assetBreakSound || 'minecraft:block.stone.break'}\`\n\n`;

        md += `# MCreator 高级微观架构子系统 (Advanced Subsystems)\n`;
        md += `### 1. 渲染管线与遮挡\n- 渲染图层：\`${c.renderLayer || 'Solid'}\`\n- 面剔除：\`${c.cullIdenticalFaces ? '开启相邻同类背面消隐' : '禁止面剔除'}\`\n\n`;
        md += `### 2. 空间几何与体素切分\n- 形状：\`${c.voxelShapeType === 'FULL_CUBE' ? '完整立方体 (1x1x1)' : '定制复合体素 VoxelShape'}\`\n- 预计算优化：静态常量缓存旋转体素矩阵以避免 Tick 垃圾回收压力。\n\n`;
        md += `### 3. 状态机与朝向拓扑\n- 朝向：\`${c.hasFacing ? c.facingType : '各向同性对称'}\`\n- 动态状态属性：\`${c.hasDynamicState ? c.dynamicStateProps : '无额外布尔状态'}\`\n\n`;
        md += `### 4. 交互触发器与红石网络\n- 右键交互 (use)：\`${c.hasRightClickAction ? (c.rightClickDescription || '切换状态并播放音效') : '无右键交互'}\`\n- 踩踏事件 (stepOn)：\`${c.hasStepOnAction ? (c.stepOnDescription || '赋予踩踏效果') : '无踩踏响应'}\`\n- 红石网络拓扑：\`${c.redstoneRole || 'NONE'}\`\n\n`;
      }

      md += `# 输出文件目录树规范 (Output Files Layout)\n`;
      md += `1. Java 业务代码：\n`;
      md += `   - \`src/main/java/com/example/${modId}/block/${blockClass}.java\`\n`;
      md += `   - \`src/main/java/com/example/${modId}/registry/ModBlocks.java\`\n`;
      md += `2. 数据与资产层 (${c.codeGenerationStrategy === 'DATAGEN' ? '自动化 DataGen' : '静态 JSON 资源'})：\n`;
      md += `   - 状态机与模型：\`assets/${modId}/blockstates/${blockId}.json\`, \`assets/${modId}/models/block/${blockId}.json\`, \`assets/${modId}/models/item/${blockId}.json\`\n`;
      md += `   - 语言文件：\`assets/${modId}/lang/zh_cn.json\`, \`assets/${modId}/lang/en_us.json\`\n`;
      md += `   - 采掘标签：\`data/minecraft/tags/blocks/mineable/${c.toolType || 'pickaxe'}.json\`\n`;
      md += `   - 战利品表：\`data/${modId}/loot_tables/blocks/${blockId}.json\`\n\n`;

      if (c.conventionalCommit) {
        md += `# Git 规范提交信息\n`;
        md += `\`\`\`bash\ngit commit -m "feat(block): add ${nameEn} (${blockId}) with MCreator specs and asset bindings"\n\`\`\`\n`;
      }

      return md;
    }
  };

  global.TypeBlock = TypeBlock;
  if (global.TypeRegistry) {
    global.TypeRegistry.register('block', TypeBlock);
  }
})(window);
