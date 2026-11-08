// ============================================================
// type-mob-effect.js — 药水效果资产类型模块 (MobEffect)
// 结合 MCreator 核心参数模型深度重构，支持高级模式素材地址选择与资产路径绑定
// 严格遵守 Google Gemini 规范与 Zero-Emoji 规则
// ============================================================

(function (global) {
  'use strict';

  const DEFAULT_CONFIG = {
    // 基础资产标识
    modId: 'custom_mod',
    effectId: 'custom_effect',
    nameZh: '自定义药水效果',
    nameEn: 'Custom Effect',
    effectCategory: 'BENEFICIAL', // BENEFICIAL | HARMFUL | NEUTRAL
    effectColorHex: '#9900FF',

    // MCreator 药水素材地址绑定 (高级模式重点)
    assetEffectIcon: 'assets/custom_mod/textures/mob_effect/custom_effect.png',
    assetParticleTexture: '', // 留空则使用原版药水气泡
    assetAppliedSound: 'minecraft:entity.player.levelup',

    // Tick 心跳与周期触发 (MCreator Procedures)
    isInstantenous: false, // 是否为瞬间生效 (像瞬间治疗/伤害)
    hasTickLogic: true,
    tickInterval: 20, // 20 ticks = 1 秒
    tickActionDescription: '每隔 20 tick (1 秒) 恢复实体 1 点生命值，并消除其着火状态',
    cureItems: 'MILK_BUCKET', // MILK_BUCKET | HONEY_BOTTLE | NONE

    // 粒子与视觉反馈
    hasParticles: true,
    ambientParticles: false, // 信标同款隐匿微弱粒子

    // 属性修饰器 (MCreator Attribute Modifiers)
    hasAttributeModifier: true,
    targetAttribute: 'minecraft:generic.movement_speed', // generic.movement_speed | generic.attack_damage | generic.max_health | generic.armor
    modifierAmount: 0.2, // +20%
    modifierOperation: 'MULTIPLY_TOTAL', // ADDITION | MULTIPLY_BASE | MULTIPLY_TOTAL
    modifierUuid: 'd1544a86-7248-4cb2-9333-3d0cf3b1b933',

    // 药水物品与酿造配方 (MCreator Potions & Brewing)
    generatePotionItem: true,
    potionDuration: 3600, // 3 分钟 (3600 ticks)
    hasLongPotion: true,
    longPotionDuration: 9600, // 8 分钟
    hasStrongPotion: true,
    strongPotionAmplifier: 1, // II 级
    brewingIngredient: 'minecraft:nether_wart',
    brewingBasePotion: 'minecraft:awkward',

    // 工程构建规范
    codeGenerationStrategy: 'DATAGEN',
    conventionalCommit: true
  };

  const PRESETS = {
    astral_regeneration: {
      name: '星辉愈合祝福 (增益效果·生命恢复与移速加成)',
      desc: '有益药效，每秒恢复生命值，附带 +20% 移速加成，支持酿造延长版与强化版。',
      mode: 'advanced',
      data: {
        ...DEFAULT_CONFIG,
        effectId: 'astral_regeneration',
        nameZh: '星辉愈合祝福',
        nameEn: 'Astral Regeneration',
        effectCategory: 'BENEFICIAL',
        effectColorHex: '#4285F4',
        tickInterval: 20,
        tickActionDescription: '每隔 20 ticks 为实体恢复 2 点生命值并驱散消极负面状态',
        hasAttributeModifier: true,
        targetAttribute: 'minecraft:generic.movement_speed',
        modifierAmount: 0.2,
        generatePotionItem: true,
        brewingIngredient: 'custom_mod:astral_crystal_shard',
        assetEffectIcon: 'assets/custom_mod/textures/mob_effect/astral_regeneration.png'
      }
    },
    corrosive_acid: {
      name: '腐蚀强酸侵蚀 (有害药效·护甲削弱与周期性伤害)',
      desc: '有害药效，每隔 20 刻扣除真实伤害，削减目标 4 点护甲值。',
      mode: 'simple',
      data: {
        ...DEFAULT_CONFIG,
        effectId: 'corrosive_acid',
        nameZh: '强酸侵蚀',
        nameEn: 'Corrosive Acid',
        effectCategory: 'HARMFUL',
        effectColorHex: '#7CBB00',
        tickInterval: 20,
        tickActionDescription: '每隔 20 ticks 对实体造成 1 点穿甲魔法伤害',
        hasAttributeModifier: true,
        targetAttribute: 'minecraft:generic.armor',
        modifierAmount: -4.0,
        modifierOperation: 'ADDITION',
        assetEffectIcon: 'assets/custom_mod/textures/mob_effect/corrosive_acid.png'
      }
    }
  };

  const TypeMobEffect = {
    id: 'mob_effect',
    label: '药水效果 (MobEffect)',
    description: '正面增益、负面诅咒或中立状态，具备周期 Tick 心跳、属性修正器与自动化酿造配方',
    iconName: 'mc_mob_effect',
    defaultConfig: DEFAULT_CONFIG,
    presets: PRESETS,

    renderConfigFlow(c, isAdv, activeStep) {
      const modId = c.modId || 'custom_mod';
      const effectId = c.effectId || 'custom_effect';

      return `
        <div class="gemini-assistant-bubble">
          <div class="assistant-avatar">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 2C12 7.523 7.523 12 2 12C7.523 12 12 16.477 12 22C12 16.477 16.477 12 22 12C16.477 12 12 7.523 12 2Z"/></svg>
          </div>
          <div class="assistant-content">
            <div class="assistant-title">${isAdv ? '正在进行 [高级模式] MCreator 药水效果架构配置' : '正在进行 [普通模式] 药水速填'}</div>
            <div class="assistant-desc">
              ${isAdv 
                ? '已开启 MCreator 完整属性修改器 (AttributeModifier)、Tick 脉冲、衍生药水瓶/酿造台配方与 18x18 状态栏图标素材地址选择绑定。' 
                : '配置效果类别、粒子颜色、心跳周期与基本逻辑。未配置项自动按标准原版药水机制提供安全配置。'}
            </div>
          </div>
          <div class="bubble-actions">
            <button class="btn-bubble-preset" onclick="PromptUI.showPresetPicker()">套用经典模板</button>
          </div>
        </div>

        <div class="flow-step-bar">
          <div class="step-item ${activeStep >= 1 ? 'active' : ''}" onclick="PromptUI.goToStep(1)">
            <span class="step-num">1</span>
            <span class="step-name">脉冲与属性修正</span>
          </div>
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 2 ? 'active' : ''}" onclick="PromptUI.goToStep(2)">
            <span class="step-num">2</span>
            <span class="step-name">药水物品与酿造</span>
          </div>
          ${isAdv ? `
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 3 ? 'active' : ''}" onclick="PromptUI.goToStep(3)">
            <span class="step-num">3</span>
            <span class="step-name">素材绑定与构建</span>
          </div>` : ''}
        </div>

        <div class="config-grid-sections">
          <!-- 步骤 1：核心标识、脉冲与属性修改器 -->
          <div class="config-section-card ${activeStep === 1 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 1</span>
              <h3>药水效果核心标识与 MCreator 属性修饰器</h3>
              <span class="section-status-hint">底层规范由外部 Skill 统一接管</span>
            </div>

            <!-- 核心标识紧凑栏 -->
            <div class="grid-4 mb-3" style="margin-bottom:14px;">
              <div class="form-group">
                <label>效果注册英文 ID (snake_case) <span class="required">*</span></label>
                <input type="text" class="form-control" value="${c.effectId || 'custom_effect'}" 
                       placeholder="如 astral_regeneration" oninput="PromptUI.updateVal('effectId', this.value)" />
              </div>
              <div class="form-group">
                <label>效果中文名称</label>
                <input type="text" class="form-control" value="${c.nameZh || '自定义药水效果'}" 
                       placeholder="如：星辉愈合" oninput="PromptUI.updateVal('nameZh', this.value); PromptUI.syncOmniSearch(this.value);" />
              </div>
              <div class="form-group">
                <label>效果类别 (Category)</label>
                <select class="form-control" onchange="PromptUI.updateVal('effectCategory', this.value)">
                  <option value="BENEFICIAL" ${c.effectCategory === 'BENEFICIAL' ? 'selected' : ''}>有益增益 (BENEFICIAL 蓝色标签)</option>
                  <option value="HARMFUL" ${c.effectCategory === 'HARMFUL' ? 'selected' : ''}>有害减益 (HARMFUL 红色标签)</option>
                  <option value="NEUTRAL" ${c.effectCategory === 'NEUTRAL' ? 'selected' : ''}>中立状态 (NEUTRAL 白色标签)</option>
                </select>
              </div>
              <div class="form-group">
                <label>粒子色谱颜色 (Hex Color)</label>
                <div style="display:flex; gap:10px; align-items:center;">
                  <input type="color" value="${c.effectColorHex || '#9900FF'}" 
                         onchange="PromptUI.updateVal('effectColorHex', this.value)" />
                  <input type="text" class="form-control" value="${c.effectColorHex || '#9900FF'}" style="flex:1;"
                         oninput="PromptUI.updateVal('effectColorHex', this.value)" />
                </div>
              </div>
            </div>

            <div class="section-body grid-2">
              <div class="form-group">
                <label>心跳触发间隔 (Tick Interval)</label>
                <input type="number" class="form-control" value="${c.tickInterval}" min="1" step="5"
                       placeholder="20 ticks = 1 秒"
                       oninput="PromptUI.updateVal('tickInterval', parseInt(this.value)||20)" />
              </div>
              <div class="form-group">
                <label>解除状态物品 (Curative Items)</label>
                <select class="form-control" onchange="PromptUI.updateVal('cureItems', this.value)">
                  <option value="MILK_BUCKET" ${c.cureItems === 'MILK_BUCKET' ? 'selected' : ''}>牛奶桶 (Milk Bucket 原版规范)</option>
                  <option value="HONEY_BOTTLE" ${c.cureItems === 'HONEY_BOTTLE' ? 'selected' : ''}>蜂蜜瓶 (Honey Bottle 解毒)</option>
                  <option value="NONE" ${c.cureItems === 'NONE' ? 'selected' : ''}>不可解除 (无法通过喝牛奶清除)</option>
                </select>
              </div>
            </div>

            <div class="form-group mt-3">
              <label>每周期心跳触发逻辑说明 (applyEffectTick)</label>
              <textarea class="form-control" rows="2" 
                        oninput="PromptUI.updateVal('tickActionDescription', this.value)">${c.tickActionDescription || ''}</textarea>
            </div>

            <div class="sub-form-box mt-3">
              <div class="form-group mb-2">
                <label class="pill-checkbox">
                  <input type="checkbox" ${c.hasAttributeModifier ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('hasAttributeModifier', this.checked); PromptUI.rerenderFlow();" />
                  <span style="font-weight:600;">启用属性修饰器 (AttributeModifier)</span>
                </label>
              </div>
              ${c.hasAttributeModifier ? `
              <div class="grid-3">
                <div class="form-group">
                  <label>作用属性目标</label>
                  <select class="form-control" onchange="PromptUI.updateVal('targetAttribute', this.value)">
                    <option value="minecraft:generic.movement_speed" ${c.targetAttribute === 'minecraft:generic.movement_speed' ? 'selected' : ''}>移动速度 (Movement Speed)</option>
                    <option value="minecraft:generic.attack_damage" ${c.targetAttribute === 'minecraft:generic.attack_damage' ? 'selected' : ''}>攻击伤害 (Attack Damage)</option>
                    <option value="minecraft:generic.max_health" ${c.targetAttribute === 'minecraft:generic.max_health' ? 'selected' : ''}>最大生命上限 (Max Health)</option>
                    <option value="minecraft:generic.armor" ${c.targetAttribute === 'minecraft:generic.armor' ? 'selected' : ''}>护甲值 (Armor)</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>修正算法 (Operation)</label>
                  <select class="form-control" onchange="PromptUI.updateVal('modifierOperation', this.value)">
                    <option value="MULTIPLY_TOTAL" ${c.modifierOperation === 'MULTIPLY_TOTAL' ? 'selected' : ''}>最终乘算 (MULTIPLY_TOTAL)</option>
                    <option value="ADDITION" ${c.modifierOperation === 'ADDITION' ? 'selected' : ''}>基础加减法 (ADDITION)</option>
                    <option value="MULTIPLY_BASE" ${c.modifierOperation === 'MULTIPLY_BASE' ? 'selected' : ''}>基础百分比 (MULTIPLY_BASE)</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>修饰幅度 (Amount)</label>
                  <input type="number" class="form-control" value="${c.modifierAmount}" step="0.05"
                         oninput="PromptUI.updateVal('modifierAmount', parseFloat(this.value)||0.2)" />
                </div>
              </div>
              ` : ''}
            </div>
          </div>

          <!-- 步骤 2：药水物品与酿造配方 -->
          <div class="config-section-card ${activeStep === 2 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 2</span>
              <h3>药水物品形态与酿造台配方 (MCreator Brewing)</h3>
            </div>
            
            <div class="form-group mb-3">
              <label class="pill-checkbox">
                <input type="checkbox" ${c.generatePotionItem ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('generatePotionItem', this.checked); PromptUI.rerenderFlow();" />
                <span style="font-weight:600;">自动生成对应 Potion 物品、喷溅药水与滞留药水</span>
              </label>
            </div>

            ${c.generatePotionItem ? `
            <div class="sub-form-box">
              <div class="grid-2">
                <div class="form-group">
                  <label>基础药水持续时间 (Ticks: 3600=3分钟)</label>
                  <input type="number" class="form-control" value="${c.potionDuration || 3600}" step="600"
                         oninput="PromptUI.updateVal('potionDuration', parseInt(this.value)||3600)" />
                </div>
                <div class="form-group">
                  <label>酿造基础材料 (Brewing Ingredient)</label>
                  <input type="text" class="form-control" value="${c.brewingIngredient || 'minecraft:nether_wart'}" 
                         placeholder="如 custom_mod:crystal_shard"
                         oninput="PromptUI.updateVal('brewingIngredient', this.value)" />
                </div>
              </div>
              <div style="display:flex; flex-wrap:wrap; gap:16px; margin-top:12px;">
                <label class="pill-checkbox">
                  <input type="checkbox" ${c.hasLongPotion ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('hasLongPotion', this.checked)" />
                  <span>红石延长版药水 (8分钟)</span>
                </label>
                <label class="pill-checkbox">
                  <input type="checkbox" ${c.hasStrongPotion ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('hasStrongPotion', this.checked)" />
                  <span>荧石强化版药水 (II级)</span>
                </label>
              </div>
            </div>
            ` : ''}
          </div>

          <!-- 步骤 3：MCreator 素材地址选择绑定与构建 (高级模式专享) -->
          ${isAdv ? `
          <div class="config-section-card advanced-exclusive ${activeStep === 3 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag purple">步骤 3 · 高级模式专享</span>
              <h3>MCreator 素材地址选择与工程构建</h3>
            </div>

            <!-- 药效图标与音效 -->
            <div style="margin-bottom:20px;">
              <h4 style="margin:0 0 10px 0; font-size:14px; font-weight:600; color:var(--gemini-text);">
                状态栏图标、粒子材质与音效素材绑定
              </h4>
              <div class="grid-2" style="gap:12px;">
                ${PromptUI.renderAssetPicker({
                  key: 'assetEffectIcon',
                  label: '状态栏/背包药水图标 (MobEffect Icon PNG)',
                  hint: 'textures/mob_effect/<effect>.png (18x18 像素标准图标)',
                  value: c.assetEffectIcon || `assets/${modId}/textures/mob_effect/${effectId}.png`,
                  defaultPath: `assets/${modId}/textures/mob_effect/${effectId}.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/mob_effect/`,
                  icon: 'image'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetParticleTexture',
                  label: '自定义药水粒子贴图 (可选)',
                  hint: '留空则使用原版药水气泡粒子',
                  value: c.assetParticleTexture || '',
                  defaultPath: `assets/${modId}/textures/particle/${effectId}_particle.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/particle/`,
                  icon: 'image'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetAppliedSound',
                  label: '获得药效反馈音效 (Sound Event)',
                  hint: '赋予药效时播放的声音',
                  value: c.assetAppliedSound || 'minecraft:entity.player.levelup',
                  defaultPath: 'minecraft:entity.player.levelup',
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
                <label>语言映射</label>
                <input type="text" class="form-control" value="zh_cn.json / en_us.json" disabled />
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
      const effectId = (c.effectId || 'custom_effect').trim().toLowerCase().replace(/[^a-z0-9_]/g, '_');
      const effectClass = effectId.split('_').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('') + 'MobEffect';
      const nameZh = c.nameZh || '自定义药水效果';
      const nameEn = c.nameEn || 'Custom Effect';
      const mountedSkill = skillPath || 'd:/xiangmu/yezi/.agents/skills/moonmod/SKILL.md';

      let md = '';
      md += `# 项目核心规范与执行契约 (External Skill Mounting)\n`;
      md += `> **外部项目规范**：\`${mountedSkill}\`\n`;
      md += `> **执行指令**：开始编写代码前，必须优先完整读取并无条件遵循上述挂载的 Skill 规范文档（包括目标平台版本、Forge/NeoForge API 契约、分包命名结构、前后端分离以及 DataGen 规范）。\n\n`;

      md += `# 目标资产需求定义 (Target MobEffect: ${nameZh})\n`;
      md += `请为模组实现全新药水状态效果：${nameZh} (${modId}:${effectId})，严格对齐 MCreator 核心元素定义与工业级规范。\n\n`;

      md += `## 1. 基础标识与核心能力规格 (Specifications)\n`;
      md += `- 资产标识：Mod ID: \`${modId}\` | 注册名: \`${effectId}\` | 分类: \`MobEffectCategory.${c.effectCategory}\`\n`;
      md += `- 粒子色值：\`${c.effectColorHex}\` | 解除物品：\`${c.cureItems}\`\n`;
      md += `- 心跳周期：\`${c.tickInterval}\` ticks (每隔 ${c.tickInterval / 20} 秒执行一次 \`applyEffectTick\`)\n`;
      md += `- 心跳行为描述：${c.tickActionDescription}\n\n`;

      if (c.hasAttributeModifier) {
        md += `### 属性修改器 (Attribute Modifier)\n`;
        md += `- 目标属性：\`${c.targetAttribute}\`\n`;
        md += `- 数值与算法：\`${c.modifierAmount}\` (${c.modifierOperation})\n\n`;
      }

      if (c.generatePotionItem) {
        md += `### 衍生药水物品与酿造 (Potions & Brewing)\n`;
        md += `- 标准药水持续：\`${c.potionDuration}\` ticks\n`;
        if (c.hasLongPotion) md += `- 红石延长版：\`${c.longPotionDuration || 9600}\` ticks\n`;
        if (c.hasStrongPotion) md += `- 荧石强化版：II 级 (Amplifier 1)\n`;
        md += `- 酿造台配方：基础药水 [${c.brewingBasePotion || 'minecraft:awkward'}] + 材料 [${c.brewingIngredient}]\n\n`;
      }

      // 高级模式素材绑定规范
      if (isAdv) {
        md += `# 素材与资源资产路径绑定规范 (Assets & Texture Mappings)\n`;
        md += `> **重要约束**：以下素材已在项目中完成绘制或存放，GUI 渲染与音效注册**必须严格精准对齐下列相对路径**，严禁擅自修改或使用虚构的文件名：\n\n`;
        md += `- **18x18 药水状态栏图标**：\`${c.assetEffectIcon || `assets/${modId}/textures/mob_effect/${effectId}.png`}\`\n`;
        if (c.assetParticleTexture) {
          md += `- **自定义粒子纹理**：\`${c.assetParticleTexture}\`\n`;
        }
        md += `- **效果生效音效**：\`${c.assetAppliedSound || 'minecraft:entity.player.levelup'}\`\n\n`;
      }

      md += `# 输出文件目录树规范 (Output Files Layout)\n`;
      md += `1. Java 业务代码：\n`;
      md += `   - \`src/main/java/com/example/${modId}/effect/${effectClass}.java\`\n`;
      md += `   - \`src/main/java/com/example/${modId}/registry/ModMobEffects.java\`\n`;
      md += `   - \`src/main/java/com/example/${modId}/registry/ModPotions.java\`\n`;
      md += `2. 数据与资产层：\n`;
      md += `   - 语言文件：\`assets/${modId}/lang/zh_cn.json\`, \`assets/${modId}/lang/en_us.json\`\n\n`;

      if (c.conventionalCommit) {
        md += `# Git 规范提交信息\n`;
        md += `\`\`\`bash\ngit commit -m "feat(effect): add ${nameEn} (${effectId}) with MCreator specs and asset bindings"\n\`\`\`\n`;
      }

      return md;
    }
  };

  global.TypeMobEffect = TypeMobEffect;
  if (global.TypeRegistry) {
    global.TypeRegistry.register('mob_effect', TypeMobEffect);
  }
})(window);
