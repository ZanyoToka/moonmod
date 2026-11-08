// ============================================================
// type-entity.js — 生物与实体资产类型模块 (Entity / Mob)
// 结合 MCreator 核心参数模型深度重构，支持高级模式素材地址选择与资产路径绑定
// 严格遵守 Google Gemini 规范与 Zero-Emoji 规则
// ============================================================

(function (global) {
  'use strict';

  const DEFAULT_CONFIG = {
    // 标识与命名空间 (技术栈与平台规范由外部挂载 Skill 统一管辖)
    modId: 'custom_mod',
    entityId: 'custom_entity',
    nameZh: '自定义生物',
    nameEn: 'Custom Entity',
    entityCategory: 'MONSTER', // MONSTER | CREATURE | AMBIENT | WATER_CREATURE | BOSS
    spawnEgg: true,
    eggPrimaryColor: '#4A3B32',
    eggSecondaryColor: '#8C7355',
    creativeTab: 'SPAWN_EGGS',

    // MCreator 实体素材地址绑定 (高级模式重点)
    assetEntityTexture: 'assets/custom_mod/textures/entity/custom_entity/custom_entity.png',
    assetGlowTexture: '', // 留空则无发光层
    assetGeoModel: 'assets/custom_mod/geo/custom_entity.geo.json',
    assetAnimation: 'assets/custom_mod/animations/custom_entity.animation.json',
    assetAmbientSound: 'minecraft:entity.zombie.ambient',
    assetHurtSound: 'minecraft:entity.zombie.hurt',
    assetDeathSound: 'minecraft:entity.zombie.death',

    // 属性规格 (MCreator Attributes)
    maxHealth: 20.0,
    movementSpeed: 0.25,
    attackDamage: 4.0,
    armor: 2.0,
    followRange: 32.0,
    knockbackResistance: 0.0,

    // 物理与碰撞箱 (MCreator Hitbox & Movement)
    hitboxWidth: 0.6,
    hitboxHeight: 1.8,
    movementType: 'WALKING', // WALKING | FLYING | SWIMMING | AMPHIBIOUS
    creatureAttribute: 'UNDEAD', // UNDEAD | ARTHROPOD | ILLAGER | WATER | REGULAR
    experienceReward: 5,

    // 免疫特性 (MCreator Immunities)
    isImmuneToFire: false,
    isImmuneToFallDamage: false,
    isImmuneToDrowning: false,
    isImmuneToSuffocation: false,

    // AI 行为与目标选择器 (MCreator AI Goals)
    aiPreset: 'HOSTILE_MELEE', // HOSTILE_MELEE | PASSIVE_PANIC | TAMABLE_PET | RANGED_ATTACK | BOSS_PHASED
    hasMeleeAttack: true,
    targetsPlayer: true,
    targetsIronGolem: false,
    avoidsSunlight: false,
    canTame: false,
    tameItem: 'minecraft:bone',
    canBreed: false,
    breedItem: 'minecraft:wheat',

    // Boss 专属配置 (Boss Bar)
    isBoss: false,
    bossBarColor: 'PURPLE', // PINK | BLUE | RED | GREEN | YELLOW | PURPLE | WHITE
    bossBarOverlay: 'PROGRESS', // PROGRESS | NOTCHED_6 | NOTCHED_10 | NOTCHED_12 | NOTCHED_20
    darkenScreen: false,

    // 掉落物与生成规则
    dropsCustomLoot: true,
    primaryDropItem: 'minecraft:rotten_flesh',
    dropCountMin: 1,
    dropCountMax: 3,
    rareDropItem: 'custom_mod:crystal_core',
    rareDropChance: 0.05,
    spawnBiomeTypes: 'OVERWORLD_PLAINS_FOREST',
    spawnWeight: 80,
    minGroupSize: 2,
    maxGroupSize: 4,

    // 工程构建规范
    codeGenerationStrategy: 'DATAGEN',
    conventionalCommit: true
  };

  const PRESETS = {
    crystal_golem_boss: {
      name: '晶簇远古傀儡 Boss (Boss血条·免疫火摔·重装)',
      desc: '首领级生物，80点生命值，具备 Boss 进度条与高额击退抗性，免疫火焰与坠落伤害。',
      mode: 'advanced',
      data: {
        ...DEFAULT_CONFIG,
        entityId: 'crystal_golem',
        nameZh: '晶簇远古巨傀儡',
        nameEn: 'Crystal Ancient Golem',
        entityCategory: 'BOSS',
        maxHealth: 150.0,
        movementSpeed: 0.22,
        attackDamage: 12.0,
        armor: 12.0,
        followRange: 48.0,
        knockbackResistance: 0.8,
        hitboxWidth: 1.4,
        hitboxHeight: 2.7,
        isImmuneToFire: true,
        isImmuneToFallDamage: true,
        isBoss: true,
        bossBarColor: 'BLUE',
        bossBarOverlay: 'NOTCHED_10',
        darkenScreen: true,
        aiPreset: 'BOSS_PHASED',
        hasMeleeAttack: true,
        targetsPlayer: true,
        experienceReward: 50,
        assetEntityTexture: 'assets/custom_mod/textures/entity/crystal_golem/crystal_golem.png',
        assetGlowTexture: 'assets/custom_mod/textures/entity/crystal_golem/crystal_golem_glow.png',
        assetGeoModel: 'assets/custom_mod/geo/crystal_golem.geo.json',
        assetAnimation: 'assets/custom_mod/animations/crystal_golem.animation.json'
      }
    },
    abyssal_crawler: {
      name: '深渊爬行潜伏兽 (常规怪物·夜间生成)',
      desc: '常规敌对怪物，夜间或洞穴生成，快速敏捷攻击玩家，击败掉落深渊晶核。',
      mode: 'simple',
      data: {
        ...DEFAULT_CONFIG,
        entityId: 'abyssal_crawler',
        nameZh: '深渊潜伏爬行者',
        nameEn: 'Abyssal Crawler',
        entityCategory: 'MONSTER',
        maxHealth: 24.0,
        movementSpeed: 0.32,
        attackDamage: 5.0,
        armor: 4.0,
        followRange: 32.0,
        hitboxWidth: 0.8,
        hitboxHeight: 0.9,
        aiPreset: 'HOSTILE_MELEE',
        hasMeleeAttack: true,
        targetsPlayer: true,
        experienceReward: 8,
        assetEntityTexture: 'assets/custom_mod/textures/entity/abyssal_crawler/abyssal_crawler.png'
      }
    }
  };

  const TypeEntity = {
    id: 'entity',
    label: '生物实体 (Entity)',
    description: '敌对怪物、被动生物或首领 Boss，具备生命值、AI 任务树、碰撞箱、Boss 血条与掉落表',
    iconName: 'mc_entity',
    defaultConfig: DEFAULT_CONFIG,
    presets: PRESETS,

    renderConfigFlow(c, isAdv, activeStep) {
      const modId = c.modId || 'custom_mod';
      const entityId = c.entityId || 'custom_entity';

      return `
        <div class="gemini-assistant-bubble">
          <div class="assistant-avatar">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 2C12 7.523 7.523 12 2 12C7.523 12 12 16.477 12 22C12 16.477 16.477 12 22 12C16.477 12 12 7.523 12 2Z"/></svg>
          </div>
          <div class="assistant-content">
            <div class="assistant-title">${isAdv ? '正在进行 [高级模式] MCreator 生物实体架构配置' : '正在进行 [普通模式] 生物速填'}</div>
            <div class="assistant-desc">
              ${isAdv 
                ? '已开启 MCreator 完整生物属性、免疫状态、Boss 血条样式、AI 目标树与 GeckoLib 皮肤/模型/动画素材地址选择绑定。' 
                : '配置生物类别、生命值、攻击力与基本 AI。未配置项自动按原版标准僵尸/生物赋予安全默认值。'}
            </div>
          </div>
          <div class="bubble-actions">
            <button class="btn-bubble-preset" onclick="PromptUI.showPresetPicker()">套用经典模板</button>
          </div>
        </div>

        <div class="flow-step-bar">
          <div class="step-item ${activeStep >= 1 ? 'active' : ''}" onclick="PromptUI.goToStep(1)">
            <span class="step-num">1</span>
            <span class="step-name">属性数值与免疫</span>
          </div>
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 2 ? 'active' : ''}" onclick="PromptUI.goToStep(2)">
            <span class="step-num">2</span>
            <span class="step-name">AI行为与Boss机制</span>
          </div>
          ${isAdv ? `
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 3 ? 'active' : ''}" onclick="PromptUI.goToStep(3)">
            <span class="step-num">3</span>
            <span class="step-name">GeckoLib 模型绑定</span>
          </div>` : ''}
        </div>

        <div class="config-grid-sections">
          <!-- 步骤 1：MCreator 属性规格与免疫特性 (整合核心标识) -->
          <div class="config-section-card ${activeStep === 1 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 1</span>
              <h3>生物核心标识、属性数值与伤害免疫</h3>
              <span class="section-status-hint">底层规范由外部 Skill 统一接管</span>
            </div>

            <!-- 核心标识紧凑栏 -->
            <div class="grid-3 mb-3" style="margin-bottom:14px;">
              <div class="form-group">
                <label>生物注册英文 ID (snake_case) <span class="required">*</span></label>
                <input type="text" class="form-control" value="${c.entityId || 'custom_entity'}" 
                       placeholder="如 crystal_golem" oninput="PromptUI.updateVal('entityId', this.value)" />
              </div>
              <div class="form-group">
                <label>生物中文名称</label>
                <input type="text" class="form-control" value="${c.nameZh || '自定义生物'}" 
                       placeholder="如：晶簇巨傀儡" oninput="PromptUI.updateVal('nameZh', this.value); PromptUI.syncOmniSearch(this.value);" />
              </div>
              <div class="form-group">
                <label>生物分类 (Category)</label>
                <select class="form-control" onchange="PromptUI.updateVal('entityCategory', this.value)">
                  <option value="MONSTER" ${c.entityCategory === 'MONSTER' ? 'selected' : ''}>敌对怪物 (Monster)</option>
                  <option value="CREATURE" ${c.entityCategory === 'CREATURE' ? 'selected' : ''}>被动动物 (Creature)</option>
                  <option value="AMBIENT" ${c.entityCategory === 'AMBIENT' ? 'selected' : ''}>环境生物 (Ambient 如蝙蝠)</option>
                  <option value="WATER_CREATURE" ${c.entityCategory === 'WATER_CREATURE' ? 'selected' : ''}>水生生物 (Water Creature)</option>
                  <option value="BOSS" ${c.entityCategory === 'BOSS' ? 'selected' : ''}>首领 Boss</option>
                </select>
              </div>
            </div>
            </div>
            <div class="section-body grid-3">
              <div class="form-group">
                <label>最大生命值 (Max Health)</label>
                <input type="number" class="form-control" value="${c.maxHealth}" min="1" step="1"
                       oninput="PromptUI.updateVal('maxHealth', parseFloat(this.value)||20.0)" />
              </div>
              <div class="form-group">
                <label>生物生物属性 (Mob Attribute)</label>
                <select class="form-control" onchange="PromptUI.updateVal('creatureAttribute', this.value)">
                  <option value="REGULAR" ${c.creatureAttribute === 'REGULAR' ? 'selected' : ''}>常规生物 (REGULAR)</option>
                  <option value="UNDEAD" ${c.creatureAttribute === 'UNDEAD' ? 'selected' : ''}>亡灵生物 (UNDEAD - 亡灵杀手克制/水下不窒息/药水颠倒)</option>
                  <option value="ARTHROPOD" ${c.creatureAttribute === 'ARTHROPOD' ? 'selected' : ''}>节肢生物 (ARTHROPOD - 节肢杀手克制)</option>
                  <option value="ILLAGER" ${c.creatureAttribute === 'ILLAGER' ? 'selected' : ''}>灾厄村民 (ILLAGER)</option>
                  <option value="WATER" ${c.creatureAttribute === 'WATER' ? 'selected' : ''}>水生生物 (WATER)</option>
                </select>
              </div>
              <div class="form-group">
                <label>移动速度 (Movement Speed)</label>
                <input type="number" class="form-control" value="${c.movementSpeed}" step="0.05" min="0.05"
                       oninput="PromptUI.updateVal('movementSpeed', parseFloat(this.value)||0.25)" />
              </div>
              <div class="form-group">
                <label>基础近战攻击力 (Attack Damage)</label>
                <input type="number" class="form-control" value="${c.attackDamage}" step="0.5" min="0"
                       oninput="PromptUI.updateVal('attackDamage', parseFloat(this.value)||4.0)" />
              </div>
              <div class="form-group">
                <label>基础护甲点数 (Armor)</label>
                <input type="number" class="form-control" value="${c.armor}" step="1" min="0"
                       oninput="PromptUI.updateVal('armor', parseFloat(this.value)||2.0)" />
              </div>
              <div class="form-group">
                <label>追踪视距半径 (Follow Range: 8-128)</label>
                <input type="number" class="form-control" value="${c.followRange}" step="4" min="8"
                       oninput="PromptUI.updateVal('followRange', parseFloat(this.value)||32.0)" />
              </div>
              <div class="form-group">
                <label>击退抗性 (Knockback Resistance: 0-1)</label>
                <input type="number" class="form-control" value="${c.knockbackResistance}" step="0.1" min="0" max="1"
                       oninput="PromptUI.updateVal('knockbackResistance', parseFloat(this.value)||0.0)" />
              </div>
              <div class="form-group">
                <label>碰撞箱宽度 (Width: 格)</label>
                <input type="number" class="form-control" value="${c.hitboxWidth}" step="0.1" min="0.1"
                       oninput="PromptUI.updateVal('hitboxWidth', parseFloat(this.value)||0.6)" />
              </div>
              <div class="form-group">
                <label>碰撞箱高度 (Height: 格)</label>
                <input type="number" class="form-control" value="${c.hitboxHeight}" step="0.1" min="0.1"
                       oninput="PromptUI.updateVal('hitboxHeight', parseFloat(this.value)||1.8)" />
              </div>
              <div class="form-group">
                <label>移动机制类型 (Movement Type)</label>
                <select class="form-control" onchange="PromptUI.updateVal('movementType', this.value)">
                  <option value="WALKING" ${c.movementType === 'WALKING' ? 'selected' : ''}>陆地行走 (Walking)</option>
                  <option value="FLYING" ${c.movementType === 'FLYING' ? 'selected' : ''}>空中飞行 (Flying)</option>
                  <option value="SWIMMING" ${c.movementType === 'SWIMMING' ? 'selected' : ''}>水中游泳 (Swimming)</option>
                  <option value="AMPHIBIOUS" ${c.movementType === 'AMPHIBIOUS' ? 'selected' : ''}>两栖移动 (Amphibious)</option>
                </select>
              </div>
            </div>

            <!-- MCreator 免疫特性勾选矩阵 -->
            <div style="display:flex; flex-wrap:wrap; gap:16px; margin-top:14px;">
              <label class="pill-checkbox">
                <input type="checkbox" ${c.isImmuneToFire ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('isImmuneToFire', this.checked)" />
                <span>免疫火焰与熔岩焚化 (Fire Immunity)</span>
              </label>
              <label class="pill-checkbox">
                <input type="checkbox" ${c.isImmuneToFallDamage ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('isImmuneToFallDamage', this.checked)" />
                <span>免疫摔落掉落伤害 (Fall Immunity)</span>
              </label>
              <label class="pill-checkbox">
                <input type="checkbox" ${c.isImmuneToDrowning ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('isImmuneToDrowning', this.checked)" />
                <span>水下呼吸/免溺亡 (Drowning Immunity)</span>
              </label>
              <label class="pill-checkbox">
                <input type="checkbox" ${c.isImmuneToSuffocation ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('isImmuneToSuffocation', this.checked)" />
                <span>卡墙免受挤压窒息 (Suffocation Immunity)</span>
              </label>
            </div>
          </div>

          <!-- 步骤 2：AI行为与Boss机制 -->
          <div class="config-section-card ${activeStep === 2 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 2</span>
              <h3>MCreator AI 目标选择器与 Boss 机制</h3>
            </div>
            
            <div class="grid-2 mb-3">
              <div class="form-group">
                <label>AI 行为预设树 (AI Tasks Preset)</label>
                <select class="form-control" onchange="PromptUI.updateVal('aiPreset', this.value)">
                  <option value="HOSTILE_MELEE" ${c.aiPreset === 'HOSTILE_MELEE' ? 'selected' : ''}>敌对近战 (近战攻击玩家与铁傀儡)</option>
                  <option value="PASSIVE_PANIC" ${c.aiPreset === 'PASSIVE_PANIC' ? 'selected' : ''}>被动恐慌 (受击乱跑逃跑/吃草)</option>
                  <option value="TAMABLE_PET" ${c.aiPreset === 'TAMABLE_PET' ? 'selected' : ''}>可驯服宠物 (跟随主人/协助战斗)</option>
                  <option value="RANGED_ATTACK" ${c.aiPreset === 'RANGED_ATTACK' ? 'selected' : ''}>远程弹射攻击 (射箭/火球)</option>
                  <option value="BOSS_PHASED" ${c.aiPreset === 'BOSS_PHASED' ? 'selected' : ''}>首领多阶段战术 (Boss Phased AI)</option>
                </select>
              </div>
              <div class="form-group">
                <label>是否为首领 Boss (Boss Bar)</label>
                <label class="pill-checkbox mt-2">
                  <input type="checkbox" ${c.isBoss ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('isBoss', this.checked); PromptUI.rerenderFlow();" />
                  <span>启用首领 Boss 血条与迷雾战场</span>
                </label>
              </div>
            </div>

            ${c.isBoss ? `
            <div class="sub-form-box mb-3">
              <div class="grid-3">
                <div class="form-group">
                  <label>Boss 血条颜色 (Color)</label>
                  <select class="form-control" onchange="PromptUI.updateVal('bossBarColor', this.value)">
                    <option value="PURPLE" ${c.bossBarColor === 'PURPLE' ? 'selected' : ''}>紫色 (PURPLE)</option>
                    <option value="RED" ${c.bossBarColor === 'RED' ? 'selected' : ''}>红色 (RED)</option>
                    <option value="BLUE" ${c.bossBarColor === 'BLUE' ? 'selected' : ''}>蓝色 (BLUE)</option>
                    <option value="PINK" ${c.bossBarColor === 'PINK' ? 'selected' : ''}>粉红 (PINK)</option>
                    <option value="GREEN" ${c.bossBarColor === 'GREEN' ? 'selected' : ''}>绿色 (GREEN)</option>
                    <option value="YELLOW" ${c.bossBarColor === 'YELLOW' ? 'selected' : ''}>黄色 (YELLOW)</option>
                    <option value="WHITE" ${c.bossBarColor === 'WHITE' ? 'selected' : ''}>白色 (WHITE)</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>Boss 血条分节样式 (Overlay)</label>
                  <select class="form-control" onchange="PromptUI.updateVal('bossBarOverlay', this.value)">
                    <option value="PROGRESS" ${c.bossBarOverlay === 'PROGRESS' ? 'selected' : ''}>平滑长条 (PROGRESS)</option>
                    <option value="NOTCHED_6" ${c.bossBarOverlay === 'NOTCHED_6' ? 'selected' : ''}>6 节分段 (NOTCHED_6)</option>
                    <option value="NOTCHED_10" ${c.bossBarOverlay === 'NOTCHED_10' ? 'selected' : ''}>10 节分段 (NOTCHED_10)</option>
                    <option value="NOTCHED_12" ${c.bossBarOverlay === 'NOTCHED_12' ? 'selected' : ''}>12 节分段 (NOTCHED_12)</option>
                    <option value="NOTCHED_20" ${c.bossBarOverlay === 'NOTCHED_20' ? 'selected' : ''}>20 节分段 (NOTCHED_20)</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>战场氛围</label>
                  <label class="pill-checkbox mt-2">
                    <input type="checkbox" ${c.darkenScreen ? 'checked' : ''} 
                           onchange="PromptUI.updateVal('darkenScreen', this.checked)" />
                    <span>压暗天空迷雾 (Darken Screen)</span>
                  </label>
                </div>
              </div>
            </div>
            ` : ''}

            <!-- 战利品与生成配置 -->
            <div class="section-body grid-2">
              <div class="form-group">
                <label>基础掉落物 (Primary Drop)</label>
                <input type="text" class="form-control" value="${c.primaryDropItem || 'minecraft:rotten_flesh'}" 
                       placeholder="如 custom_mod:crystal_core" 
                       oninput="PromptUI.updateVal('primaryDropItem', this.value)" />
              </div>
              <div class="form-group">
                <label>击杀经验值 (XP Reward)</label>
                <input type="number" class="form-control" value="${c.experienceReward || 5}" min="0"
                       oninput="PromptUI.updateVal('experienceReward', parseInt(this.value)||5)" />
              </div>
            </div>
          </div>

          <!-- 步骤 3：MCreator 素材地址选择绑定与构建 (高级模式专享) -->
          ${isAdv ? `
          <div class="config-section-card advanced-exclusive ${activeStep === 3 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag purple">步骤 3 · 高级模式专享</span>
              <h3>MCreator 素材地址选择与数据构建</h3>
            </div>

            <!-- 实体纹理与 GeckoLib 模型/动画 -->
            <div style="margin-bottom:20px;">
              <h4 style="margin:0 0 10px 0; font-size:14px; font-weight:600; color:var(--gemini-text);">
                实体材质、GeckoLib 模型与动画文件地址绑定
              </h4>
              <div class="grid-2" style="gap:12px;">
                ${PromptUI.renderAssetPicker({
                  key: 'assetEntityTexture',
                  label: '实体主体皮肤贴图 (Texture PNG)',
                  hint: 'textures/entity/<entity>/<entity>.png',
                  value: c.assetEntityTexture || `assets/${modId}/textures/entity/${entityId}/${entityId}.png`,
                  defaultPath: `assets/${modId}/textures/entity/${entityId}/${entityId}.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/entity/${entityId}/`,
                  icon: 'image'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetGlowTexture',
                  label: '自发光/夜光图层贴图 (Glow Layer PNG, 可选)',
                  hint: '在黑暗中高亮发光部分贴图',
                  value: c.assetGlowTexture || '',
                  defaultPath: `assets/${modId}/textures/entity/${entityId}/${entityId}_glow.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/entity/${entityId}/`,
                  icon: 'image'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetGeoModel',
                  label: 'GeckoLib 3D 骨骼模型 (geo.json)',
                  hint: 'BlockBench 导出的 GeckoLib 模型文件',
                  value: c.assetGeoModel || `assets/${modId}/geo/${entityId}.geo.json`,
                  defaultPath: `assets/${modId}/geo/${entityId}.geo.json`,
                  accept: '.json',
                  prefix: `assets/${modId}/geo/`,
                  icon: 'model'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetAnimation',
                  label: 'GeckoLib 动画文件 (animation.json)',
                  hint: '包含 idle, walk, attack 等动作状态',
                  value: c.assetAnimation || `assets/${modId}/animations/${entityId}.animation.json`,
                  defaultPath: `assets/${modId}/animations/${entityId}.animation.json`,
                  accept: '.json',
                  prefix: `assets/${modId}/animations/`,
                  icon: 'model'
                })}
              </div>
            </div>

            <!-- 音效素材 -->
            <div style="margin-bottom:20px;">
              <h4 style="margin:0 0 10px 0; font-size:14px; font-weight:600; color:var(--gemini-text);">
                生物音效事件配置 (Sound Assets)
              </h4>
              <div class="grid-3" style="gap:10px;">
                ${PromptUI.renderAssetPicker({
                  key: 'assetAmbientSound',
                  label: '日常叫声音效 (Ambient)',
                  hint: '原版或自定义音频事件',
                  value: c.assetAmbientSound || 'minecraft:entity.zombie.ambient',
                  defaultPath: 'minecraft:entity.zombie.ambient',
                  accept: '.ogg',
                  icon: 'audio'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetHurtSound',
                  label: '受击受伤音效 (Hurt)',
                  hint: '受到伤害时播放',
                  value: c.assetHurtSound || 'minecraft:entity.zombie.hurt',
                  defaultPath: 'minecraft:entity.zombie.hurt',
                  accept: '.ogg',
                  icon: 'audio'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetDeathSound',
                  label: '死亡音效 (Death)',
                  hint: '生命耗尽时播放',
                  value: c.assetDeathSound || 'minecraft:entity.zombie.death',
                  defaultPath: 'minecraft:entity.zombie.death',
                  accept: '.ogg',
                  icon: 'audio'
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
                <label>生成刷怪蛋物品 (Spawn Egg Item)</label>
                <label class="pill-checkbox mt-2">
                  <input type="checkbox" ${c.spawnEgg ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('spawnEgg', this.checked)" />
                  <span>自动注册对应刷怪蛋物品并赋予选定颜色</span>
                </label>
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
      const entityId = (c.entityId || 'custom_entity').trim().toLowerCase().replace(/[^a-z0-9_]/g, '_');
      const entityClass = entityId.split('_').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('') + 'Entity';
      const nameZh = c.nameZh || '自定义生物';
      const nameEn = c.nameEn || 'Custom Entity';
      const mountedSkill = skillPath || 'd:/xiangmu/yezi/.agents/skills/moonmod/SKILL.md';

      let md = '';
      md += `# 项目核心规范与执行契约 (External Skill Mounting)\n`;
      md += `> **外部项目规范**：\`${mountedSkill}\`\n`;
      md += `> **执行指令**：开始编写代码前，必须优先完整读取并无条件遵循上述挂载的 Skill 规范文档（包括目标平台版本、Forge/NeoForge API 契约、分包命名结构、前后端分离以及 DataGen 规范）。\n\n`;

      md += `# 目标资产需求定义 (Target Entity: ${nameZh})\n`;
      md += `请为模组实现全新生物实体：${nameZh} (${modId}:${entityId})，严格遵循 MCreator 核心元素定义与工业级规范。\n\n`;

      md += `## 1. 基础标识与核心属性规格 (Specifications)\n`;
      md += `- 资产标识：Mod ID: \`${modId}\` | 注册名: \`${entityId}\` | 分类: \`MobCategory.${c.entityCategory}\` | 属性: \`MobAttribute.${c.creatureAttribute || 'REGULAR'}\`\n`;
      md += `- 本地化显示：中文: \`${nameZh}\` | 英文: \`${nameEn}\`\n`;
      md += `- 核心属性：最大生命: \`${c.maxHealth}\` | 移速: \`${c.movementSpeed}\` | 基础攻击: \`${c.attackDamage}\` | 基础护甲: \`${c.armor}\` | 视距: \`${c.followRange}\` | 击退抗性: \`${c.knockbackResistance}\`\n`;
      md += `- 碰撞尺寸与移动：宽: \`${c.hitboxWidth}\` 格 | 高: \`${c.hitboxHeight}\` 格 | 移动机制: \`${c.movementType}\`\n`;
      md += `- 免疫特性：防火: \`${c.isImmuneToFire ? '是' : '否'}\` | 防摔: \`${c.isImmuneToFallDamage ? '是' : '否'}\` | 防溺亡: \`${c.isImmuneToDrowning ? '是' : '否'}\` | 防窒息: \`${c.isImmuneToSuffocation ? '是' : '否'}\`\n\n`;

      if (c.isBoss) {
        md += `### 首领 Boss 机制 (Boss Battle)\n`;
        md += `- Boss 血条：颜色 \`BossEvent.BossBarColor.${c.bossBarColor}\` | 样式 \`BossEvent.BossBarOverlay.${c.bossBarOverlay}\`\n`;
        md += `- 迷雾压暗天空：\`${c.darkenScreen ? '开启' : '关闭'}\`\n\n`;
      }

      md += `### AI 目标选择器任务树 (AI Goals)\n`;
      md += `- 行为预设：\`${c.aiPreset}\`\n`;
      md += `- 近战攻击目标：\`${c.targetsPlayer ? '追击玩家' : '无'}，${c.targetsIronGolem ? '追击铁傀儡' : ''}\`\n\n`;

      // 高级模式素材绑定规范
      if (isAdv) {
        md += `# 素材与资源资产路径绑定规范 (Assets & Texture Mappings)\n`;
        md += `> **重要约束**：以下素材已在项目中完成绘制或存放，渲染器、GeckoLib 模型定义与音效注册**必须严格精准对齐下列相对路径**，严禁擅自修改或使用虚构的文件名：\n\n`;
        md += `- **实体主体皮肤贴图**：\`${c.assetEntityTexture || `assets/${modId}/textures/entity/${entityId}/${entityId}.png`}\`\n`;
        if (c.assetGlowTexture) {
          md += `- **自发光/夜光贴图**：\`${c.assetGlowTexture}\`\n`;
        }
        md += `- **GeckoLib 3D 模型文件**：\`${c.assetGeoModel || `assets/${modId}/geo/${entityId}.geo.json`}\`\n`;
        md += `- **GeckoLib 动画文件**：\`${c.assetAnimation || `assets/${modId}/animations/${entityId}.animation.json`}\`\n`;
        md += `- **音效资源**：\n`;
        md += `  - 日常环境音效：\`${c.assetAmbientSound || 'minecraft:entity.zombie.ambient'}\`\n`;
        md += `  - 受伤受击音效：\`${c.assetHurtSound || 'minecraft:entity.zombie.hurt'}\`\n`;
        md += `  - 死亡丧命音效：\`${c.assetDeathSound || 'minecraft:entity.zombie.death'}\`\n\n`;
      }

      md += `# 输出文件目录树规范 (Output Files Layout)\n`;
      md += `1. Java 业务代码：\n`;
      md += `   - \`src/main/java/com/example/${modId}/entity/${entityClass}.java\`\n`;
      md += `   - \`src/main/java/com/example/${modId}/client/renderer/${entityClass}Renderer.java\`\n`;
      md += `   - \`src/main/java/com/example/${modId}/registry/ModEntities.java\`\n`;
      md += `2. 数据与资产层：\n`;
      md += `   - 战利品表：\`data/${modId}/loot_tables/entities/${entityId}.json\`\n`;
      md += `   - 语言文件：\`assets/${modId}/lang/zh_cn.json\`, \`assets/${modId}/lang/en_us.json\`\n\n`;

      if (c.conventionalCommit) {
        md += `# Git 规范提交信息\n`;
        md += `\`\`\`bash\ngit commit -m "feat(entity): add ${nameEn} (${entityId}) with MCreator specs and asset bindings"\n\`\`\`\n`;
      }

      return md;
    }
  };

  global.TypeEntity = TypeEntity;
  if (global.TypeRegistry) {
    global.TypeRegistry.register('entity', TypeEntity);
  }
})(window);
