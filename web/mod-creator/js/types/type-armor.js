// ============================================================
// type-armor.js — 防具装备资产类型模块 (Armor)
// 结合 MCreator 核心参数模型深度重构，支持高级模式素材地址选择与资产路径绑定
// 严格遵守 Google Gemini 规范与 Zero-Emoji 规则
// ============================================================

(function (global) {
  'use strict';

  const DEFAULT_CONFIG = {
    // 标识与命名空间 (技术栈与平台规范由外部挂载 Skill 统一管辖)
    modId: 'custom_mod',
    armorId: 'custom_armor',
    nameZh: '自定义防具',
    nameEn: 'Custom Armor',
    creativeTab: 'COMBAT',

    // MCreator 部位生成模式与材质梯队
    armorGenerationMode: 'FULL_SET', // FULL_SET (整套四件套) | HELMET | CHESTPLATE | LEGGINGS | BOOTS
    tierMaterial: 'netherite', // leather | chainmail | iron | gold | diamond | netherite | custom
    
    // MCreator 素材地址绑定 (高级模式重点)
    assetArmorLayer1: 'assets/custom_mod/textures/models/armor/custom_armor_layer_1.png',
    assetArmorLayer2: 'assets/custom_mod/textures/models/armor/custom_armor_layer_2.png',
    assetItemHelmet: 'assets/custom_mod/textures/item/custom_armor_helmet.png',
    assetItemChestplate: 'assets/custom_mod/textures/item/custom_armor_chestplate.png',
    assetItemLeggings: 'assets/custom_mod/textures/item/custom_armor_leggings.png',
    assetItemBoots: 'assets/custom_mod/textures/item/custom_armor_boots.png',
    equipSound: 'minecraft:item.armor.equip_netherite',

    // MCreator 各部位独立防御数值与规格
    defenseHelmet: 3,
    defenseChestplate: 8,
    defenseLeggings: 6,
    defenseBoots: 3,
    toughness: 3.0, // 护甲韧性 (Netherite为3.0, Diamond为2.0)
    knockbackResistance: 0.1, // 击退抗性 (0.1 = 10%)
    durabilityMultiplier: 37, // 耐久度基数乘数 (Netherite 37, Diamond 33, Iron 15)
    enchantability: 15,
    repairMaterial: 'minecraft:netherite_ingot',
    isFireResistant: true,
    makesPiglinsNeutral: false, // 类似金护甲使得猪灵中立 (Piglin Neutral)

    // 全身套装被动效果 (MCreator Set Bonus Trigger)
    hasSetBonus: true,
    setBonusType: 'EFFECT', // EFFECT | FLIGHT | FIRE_IMMUNITY | DAMAGE_REDUCTION
    setBonusEffect: 'RESISTANCE', // RESISTANCE | STRENGTH | SPEED | REGENERATION | FIRE_RESISTANCE | NIGHT_VISION
    setBonusAmplifier: 0, // 0 = 等级 I
    setBonusDescription: '全身四件套集齐时激活护甲共鸣，常驻提供抗性提升 I 与击退抵御',

    // 描述与提示
    hasTooltip: true,
    tooltipText: '',
    tooltipShiftOnly: false,

    // 工程构建规范
    codeGenerationStrategy: 'DATAGEN', // DATAGEN | STATIC_JSON
    generateLangFiles: true,
    conventionalCommit: true
  };

  const PRESETS = {
    sculk_warden_set: {
      name: '深渊幽匿守卫者战甲 (全套防具·极高韧性与冲击波反震)',
      desc: '四件套全套生成，极高韧性4.0，击退抗性0.2，全套穿戴激活夜视与抗性提升。',
      mode: 'advanced',
      data: {
        ...DEFAULT_CONFIG,
        armorId: 'sculk_warden',
        nameZh: '幽匿守卫战铠',
        nameEn: 'Sculk Warden Armor',
        armorGenerationMode: 'FULL_SET',
        tierMaterial: 'netherite',
        defenseHelmet: 4,
        defenseChestplate: 9,
        defenseLeggings: 7,
        defenseBoots: 4,
        toughness: 4.0,
        knockbackResistance: 0.2,
        durabilityMultiplier: 45,
        enchantability: 20,
        repairMaterial: 'minecraft:echo_shard',
        isFireResistant: true,
        hasSetBonus: true,
        setBonusEffect: 'RESISTANCE',
        setBonusAmplifier: 1,
        setBonusDescription: '全套穿戴共鸣远古幽匿回响，赋予抗性提升 II 并对近战攻击者施加反冲',
        assetArmorLayer1: 'assets/custom_mod/textures/models/armor/sculk_warden_layer_1.png',
        assetArmorLayer2: 'assets/custom_mod/textures/models/armor/sculk_warden_layer_2.png'
      }
    },
    aeromancer_wings: {
      name: '天风使者轻灵战靴 (单件装备·速度提升)',
      desc: '仅生成靴子单件，轻量化高附魔亲和度，穿戴提供常驻敏捷速度提升。',
      mode: 'simple',
      data: {
        ...DEFAULT_CONFIG,
        armorId: 'aeromancer_boots',
        nameZh: '天风使者战靴',
        nameEn: 'Aeromancer Boots',
        armorGenerationMode: 'BOOTS',
        tierMaterial: 'diamond',
        defenseBoots: 3,
        toughness: 2.0,
        knockbackResistance: 0.0,
        durabilityMultiplier: 33,
        hasSetBonus: true,
        setBonusEffect: 'SPEED',
        setBonusAmplifier: 0,
        setBonusDescription: '穿戴战靴获得轻灵疾行，移动速度增加 20%',
        assetArmorLayer1: 'assets/custom_mod/textures/models/armor/aeromancer_layer_1.png',
        assetArmorLayer2: 'assets/custom_mod/textures/models/armor/aeromancer_layer_2.png'
      }
    }
  };

  const TypeArmor = {
    id: 'armor',
    label: '防具装备 (Armor)',
    description: '头盔、胸甲、护腿、靴子或完整四件套，具备独立防御值、护甲韧性、击退抗性与全套被动',
    iconName: 'mc_armor',
    defaultConfig: DEFAULT_CONFIG,
    presets: PRESETS,

    renderConfigFlow(c, isAdv, activeStep) {
      const modId = c.modId || 'custom_mod';
      const armorId = c.armorId || 'custom_armor';

      return `
        <div class="gemini-assistant-bubble">
          <div class="assistant-avatar">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 2C12 7.523 7.523 12 2 12C7.523 12 12 16.477 12 22C12 16.477 16.477 12 22 12C16.477 12 12 7.523 12 2Z"/></svg>
          </div>
          <div class="assistant-content">
            <div class="assistant-title">${isAdv ? '正在进行 [高级模式] MCreator 防具架构配置' : '正在进行 [普通模式] 防具速填'}</div>
            <div class="assistant-desc">
              ${isAdv 
                ? '已开启 MCreator 全套/单件部位模式、各部位独立防御、Layer 1/2 模型图层与 4 件物品贴图素材地址选择绑定。' 
                : '配置防具基础材质、防御韧性与部位模式。未配置项自动按原版下界合金或钻石梯队赋予标准参数。'}
            </div>
          </div>
          <div class="bubble-actions">
            <button class="btn-bubble-preset" onclick="PromptUI.showPresetPicker()">套用经典模板</button>
          </div>
        </div>

        <div class="flow-step-bar">
          <div class="step-item ${activeStep >= 1 ? 'active' : ''}" onclick="PromptUI.goToStep(1)">
            <span class="step-num">1</span>
            <span class="step-name">部位与防御数值</span>
          </div>
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 2 ? 'active' : ''}" onclick="PromptUI.goToStep(2)">
            <span class="step-num">2</span>
            <span class="step-name">套装奖励被动</span>
          </div>
          ${isAdv ? `
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 3 ? 'active' : ''}" onclick="PromptUI.goToStep(3)">
            <span class="step-num">3</span>
            <span class="step-name">MCreator 贴图绑定</span>
          </div>` : ''}
        </div>

        <div class="config-grid-sections">
          <!-- 步骤 1：MCreator 部位模式与防御数值 (整合核心标识) -->
          <div class="config-section-card ${activeStep === 1 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 1</span>
              <h3>防具核心标识、部位生成与防御数值</h3>
              <span class="section-status-hint">底层规范由外部 Skill 统一接管</span>
            </div>

            <!-- 核心标识紧凑栏 -->
            <div class="grid-3 mb-3" style="margin-bottom:14px;">
              <div class="form-group">
                <label>防具注册英文 ID 前缀 (snake_case) <span class="required">*</span></label>
                <input type="text" class="form-control" value="${c.armorId || 'custom_armor'}" 
                       placeholder="如 sculk_warden" oninput="PromptUI.updateVal('armorId', this.value)" />
              </div>
              <div class="form-group">
                <label>防具系列中文名称</label>
                <input type="text" class="form-control" value="${c.nameZh || '自定义防具'}" 
                       placeholder="如：幽匿战铠" oninput="PromptUI.updateVal('nameZh', this.value); PromptUI.syncOmniSearch(this.value);" />
              </div>
              <div class="form-group">
                <label>创造模式标签栏 (Creative Tab)</label>
                <select class="form-control" onchange="PromptUI.updateVal('creativeTab', this.value)">
                  <option value="COMBAT" ${c.creativeTab === 'COMBAT' ? 'selected' : ''}>战斗防具 (Combat)</option>
                  <option value="MISC" ${c.creativeTab === 'MISC' ? 'selected' : ''}>杂项 (Misc)</option>
                </select>
              </div>
            </div>
            </div>
            <div class="section-body grid-2 mb-3">
              <div class="form-group">
                <label>防具生成部位模式 (Armor Generation Mode)</label>
                <select class="form-control" onchange="PromptUI.updateVal('armorGenerationMode', this.value); PromptUI.rerenderFlow();">
                  <option value="FULL_SET" ${c.armorGenerationMode === 'FULL_SET' ? 'selected' : ''}>完整四件套 (全套头盔/胸甲/护腿/靴子)</option>
                  <option value="HELMET" ${c.armorGenerationMode === 'HELMET' ? 'selected' : ''}>仅头盔 (Helmet Only)</option>
                  <option value="CHESTPLATE" ${c.armorGenerationMode === 'CHESTPLATE' ? 'selected' : ''}>仅胸甲 (Chestplate Only)</option>
                  <option value="LEGGINGS" ${c.armorGenerationMode === 'LEGGINGS' ? 'selected' : ''}>仅护腿 (Leggings Only)</option>
                  <option value="BOOTS" ${c.armorGenerationMode === 'BOOTS' ? 'selected' : ''}>仅靴子 (Boots Only)</option>
                </select>
              </div>
              <div class="form-group">
                <label>基础材质等级 (Armor Material)</label>
                <select class="form-control" onchange="PromptUI.updateVal('tierMaterial', this.value)">
                  <option value="netherite" ${c.tierMaterial === 'netherite' ? 'selected' : ''}>下界合金 (Netherite 等级)</option>
                  <option value="diamond" ${c.tierMaterial === 'diamond' ? 'selected' : ''}>钻石 (Diamond 等级)</option>
                  <option value="iron" ${c.tierMaterial === 'iron' ? 'selected' : ''}>铁制 (Iron 等级)</option>
                  <option value="gold" ${c.tierMaterial === 'gold' ? 'selected' : ''}>黄金 (Gold 等级)</option>
                  <option value="leather" ${c.tierMaterial === 'leather' ? 'selected' : ''}>皮革 (Leather 等级)</option>
                </select>
              </div>
            </div>

            <!-- 各部位独立护甲防御点数 -->
            <div class="sub-form-box mb-3">
              <h4 style="margin:0 0 10px 0; font-size:13px; color:var(--gemini-text);">各部件独立防御点数 (Defense Points)</h4>
              <div class="grid-4" style="display:grid; grid-template-columns:repeat(auto-fit, minmax(130px, 1fr)); gap:12px;">
                <div class="form-group">
                  <label>头盔防御 (Helmet)</label>
                  <input type="number" class="form-control" value="${c.defenseHelmet || 3}" min="1" max="20"
                         oninput="PromptUI.updateVal('defenseHelmet', parseInt(this.value)||3)" />
                </div>
                <div class="form-group">
                  <label>胸甲防御 (Chestplate)</label>
                  <input type="number" class="form-control" value="${c.defenseChestplate || 8}" min="1" max="30"
                         oninput="PromptUI.updateVal('defenseChestplate', parseInt(this.value)||8)" />
                </div>
                <div class="form-group">
                  <label>护腿防御 (Leggings)</label>
                  <input type="number" class="form-control" value="${c.defenseLeggings || 6}" min="1" max="25"
                         oninput="PromptUI.updateVal('defenseLeggings', parseInt(this.value)||6)" />
                </div>
                <div class="form-group">
                  <label>靴子防御 (Boots)</label>
                  <input type="number" class="form-control" value="${c.defenseBoots || 3}" min="1" max="20"
                         oninput="PromptUI.updateVal('defenseBoots', parseInt(this.value)||3)" />
                </div>
              </div>
            </div>

            <div class="section-body grid-3">
              <div class="form-group">
                <label>护甲韧性 (Toughness: 0-10)</label>
                <input type="number" class="form-control" value="${c.toughness}" step="0.5" min="0"
                       oninput="PromptUI.updateVal('toughness', parseFloat(this.value)||3.0)" />
              </div>
              <div class="form-group">
                <label>击退抗性 (Knockback Resistance: 0-1)</label>
                <input type="number" class="form-control" value="${c.knockbackResistance}" step="0.05" min="0" max="1"
                       oninput="PromptUI.updateVal('knockbackResistance', parseFloat(this.value)||0.1)" />
              </div>
              <div class="form-group">
                <label>耐久度乘数 (Durability Multiplier)</label>
                <input type="number" class="form-control" value="${c.durabilityMultiplier || 37}" min="1"
                       oninput="PromptUI.updateVal('durabilityMultiplier', parseInt(this.value)||37)" />
              </div>
              <div class="form-group">
                <label>铁砧修复物品材料 (Repair Item)</label>
                <input type="text" class="form-control" value="${c.repairMaterial || 'minecraft:netherite_ingot'}" 
                       placeholder="如 minecraft:netherite_ingot" 
                       oninput="PromptUI.updateVal('repairMaterial', this.value)" />
              </div>
              <div class="form-group">
                <label>附魔亲和度 (Enchantability)</label>
                <input type="number" class="form-control" value="${c.enchantability || 15}" min="1" max="40"
                       oninput="PromptUI.updateVal('enchantability', parseInt(this.value)||15)" />
              </div>
              <div class="form-group">
                <label>装备穿戴音效 (Sound Event)</label>
                <input type="text" class="form-control" value="${c.equipSound || 'minecraft:item.armor.equip_netherite'}" 
                       placeholder="如 minecraft:item.armor.equip_netherite" 
                       oninput="PromptUI.updateVal('equipSound', this.value)" />
              </div>
            </div>

            <div style="display:flex; flex-wrap:wrap; gap:16px; margin-top:14px;">
              <label class="pill-checkbox">
                <input type="checkbox" ${c.isFireResistant ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('isFireResistant', this.checked)" />
                <span>免疫火焰与熔岩焚毁 (Fire Resistant)</span>
              </label>
              <label class="pill-checkbox">
                <input type="checkbox" ${c.makesPiglinsNeutral ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('makesPiglinsNeutral', this.checked)" />
                <span>猪灵中立防具特性 (Makes Piglins Neutral)</span>
              </label>
            </div>
          </div>

          <!-- 步骤 2：套装奖励效果 -->
          <div class="config-section-card ${activeStep === 2 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 2</span>
              <h3>全身套装被动奖励 (MCreator Set Bonus Procedures)</h3>
            </div>
            <div class="form-group mb-3">
              <label class="pill-checkbox">
                <input type="checkbox" ${c.hasSetBonus ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('hasSetBonus', this.checked); PromptUI.rerenderFlow();" />
                <span style="font-weight:600;">启用全身套装集齐触发被动技能 (Set Bonus)</span>
              </label>
            </div>

            ${c.hasSetBonus ? `
            <div class="sub-form-box">
              <div class="grid-2">
                <div class="form-group">
                  <label>常驻赋予药水效果 (Set Effect)</label>
                  <select class="form-control" onchange="PromptUI.updateVal('setBonusEffect', this.value)">
                    <option value="RESISTANCE" ${c.setBonusEffect === 'RESISTANCE' ? 'selected' : ''}>抗性提升 (Resistance)</option>
                    <option value="STRENGTH" ${c.setBonusEffect === 'STRENGTH' ? 'selected' : ''}>力量 (Strength)</option>
                    <option value="SPEED" ${c.setBonusEffect === 'SPEED' ? 'selected' : ''}>速度提升 (Speed)</option>
                    <option value="REGENERATION" ${c.setBonusEffect === 'REGENERATION' ? 'selected' : ''}>生命恢复 (Regeneration)</option>
                    <option value="FIRE_RESISTANCE" ${c.setBonusEffect === 'FIRE_RESISTANCE' ? 'selected' : ''}>抗火 (Fire Resistance)</option>
                    <option value="NIGHT_VISION" ${c.setBonusEffect === 'NIGHT_VISION' ? 'selected' : ''}>夜视 (Night Vision)</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>效果等级 (Amplifier)</label>
                  <select class="form-control" onchange="PromptUI.updateVal('setBonusAmplifier', parseInt(this.value))">
                    <option value="0" ${c.setBonusAmplifier === 0 ? 'selected' : ''}>I 级 (Amplifier 0)</option>
                    <option value="1" ${c.setBonusAmplifier === 1 ? 'selected' : ''}>II 级 (Amplifier 1)</option>
                  </select>
                </div>
              </div>
              <div class="form-group mt-2">
                <label>套装效果描述文本</label>
                <input type="text" class="form-control" value="${c.setBonusDescription || ''}" 
                       placeholder="如：全身四件套集齐时激活护甲共鸣，常驻提供抗性提升 I"
                       oninput="PromptUI.updateVal('setBonusDescription', this.value)" />
              </div>
            </div>
            ` : ''}

            <div class="form-group mt-3">
              <label>悬浮提示信息 (Tooltip)</label>
              <input type="text" class="form-control" value="${c.tooltipText || ''}" 
                     placeholder="如：深渊远古守护者锻造的坚固重铠"
                     oninput="PromptUI.updateVal('tooltipText', this.value)" />
            </div>
          </div>

          <!-- 步骤 3：MCreator 素材地址选择绑定与构建 (高级模式专享) -->
          ${isAdv ? `
          <div class="config-section-card advanced-exclusive ${activeStep === 3 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag purple">步骤 3 · 高级模式专享</span>
              <h3>MCreator 素材地址选择与工程构建</h3>
            </div>

            <!-- 护甲图层贴图 (Layer 1 & Layer 2) -->
            <div style="margin-bottom:20px;">
              <h4 style="margin:0 0 10px 0; font-size:14px; font-weight:600; color:var(--gemini-text);">
                穿着模型图层贴图 (Armor Layer 1 & 2 Textures)
              </h4>
              <div class="grid-2" style="gap:12px;">
                ${PromptUI.renderAssetPicker({
                  key: 'assetArmorLayer1',
                  label: '护甲图层 1 贴图 (Layer 1: 头盔/胸甲/靴子)',
                  hint: 'models/armor/<material>_layer_1.png (64x32 或 128x64)',
                  value: c.assetArmorLayer1 || `assets/${modId}/textures/models/armor/${armorId}_layer_1.png`,
                  defaultPath: `assets/${modId}/textures/models/armor/${armorId}_layer_1.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/models/armor/`,
                  icon: 'layers'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetArmorLayer2',
                  label: '护甲图层 2 贴图 (Layer 2: 护腿 Leggings)',
                  hint: 'models/armor/<material>_layer_2.png (64x32 或 128x64)',
                  value: c.assetArmorLayer2 || `assets/${modId}/textures/models/armor/${armorId}_layer_2.png`,
                  defaultPath: `assets/${modId}/textures/models/armor/${armorId}_layer_2.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/models/armor/`,
                  icon: 'layers'
                })}
              </div>
            </div>

            <!-- 4 件物品独立贴图 (Item Textures) -->
            <div style="margin-bottom:20px;">
              <h4 style="margin:0 0 10px 0; font-size:14px; font-weight:600; color:var(--gemini-text);">
                4 件物品独立贴图 (Item Textures)
              </h4>
              <div class="grid-2" style="gap:10px;">
                ${PromptUI.renderAssetPicker({
                  key: 'assetItemHelmet',
                  label: '头盔物品贴图 (Helmet Item PNG)',
                  hint: 'textures/item/<armor>_helmet.png',
                  value: c.assetItemHelmet || `assets/${modId}/textures/item/${armorId}_helmet.png`,
                  defaultPath: `assets/${modId}/textures/item/${armorId}_helmet.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/item/`,
                  icon: 'image'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetItemChestplate',
                  label: '胸甲物品贴图 (Chestplate Item PNG)',
                  hint: 'textures/item/<armor>_chestplate.png',
                  value: c.assetItemChestplate || `assets/${modId}/textures/item/${armorId}_chestplate.png`,
                  defaultPath: `assets/${modId}/textures/item/${armorId}_chestplate.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/item/`,
                  icon: 'image'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetItemLeggings',
                  label: '护腿物品贴图 (Leggings Item PNG)',
                  hint: 'textures/item/<armor>_leggings.png',
                  value: c.assetItemLeggings || `assets/${modId}/textures/item/${armorId}_leggings.png`,
                  defaultPath: `assets/${modId}/textures/item/${armorId}_leggings.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/item/`,
                  icon: 'image'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetItemBoots',
                  label: '靴子物品贴图 (Boots Item PNG)',
                  hint: 'textures/item/<armor>_boots.png',
                  value: c.assetItemBoots || `assets/${modId}/textures/item/${armorId}_boots.png`,
                  defaultPath: `assets/${modId}/textures/item/${armorId}_boots.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/item/`,
                  icon: 'image'
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
                <label>自动生成语言映射</label>
                <select class="form-control" onchange="PromptUI.updateVal('generateLangFiles', this.value === 'true')">
                  <option value="true" ${c.generateLangFiles ? 'selected' : ''}>生成 zh_cn.json 与 en_us.json</option>
                  <option value="false" ${!c.generateLangFiles ? 'selected' : ''}>不生成语言映射</option>
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
      const armorId = (c.armorId || 'custom_armor').trim().toLowerCase().replace(/[^a-z0-9_]/g, '_');
      const armorMaterialClass = armorId.split('_').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('') + 'ArmorMaterial';
      const nameZh = c.nameZh || '自定义防具';
      const nameEn = c.nameEn || 'Custom Armor';
      const mountedSkill = skillPath || 'd:/xiangmu/yezi/.agents/skills/moonmod/SKILL.md';

      let md = '';
      md += `# 项目核心规范与执行契约 (External Skill Mounting)\n`;
      md += `> **外部项目规范**：\`${mountedSkill}\`\n`;
      md += `> **执行指令**：开始编写代码前，必须优先完整读取并无条件遵循上述挂载的 Skill 规范文档（包括目标平台版本、Forge/NeoForge API 契约、分包命名结构、前后端分离以及 DataGen 规范）。\n\n`;

      md += `# 目标资产需求定义 (Target Armor: ${nameZh})\n`;
      md += `请为模组实现全新防具装备：${nameZh} (${modId}:${armorId})，严格遵循 MCreator 核心元素定义与工业级规范。\n\n`;

      md += `## 1. 基础标识与部位防御规格 (Specifications)\n`;
      md += `- 资产标识：Mod ID: \`${modId}\` | 注册名: \`${armorId}\` | 创造栏: \`${c.creativeTab || 'COMBAT'}\`\n`;
      md += `- 本地化显示：中文: \`${nameZh}\` | 英文: \`${nameEn}\`\n`;
      md += `- 部位模式：\`${c.armorGenerationMode === 'FULL_SET' ? '完整四件套 (头盔/胸甲/护腿/靴子)' : c.armorGenerationMode.toUpperCase()}\`\n`;
      md += `- 各部件防御数值：头盔: \`${c.defenseHelmet || 3}\` | 胸甲: \`${c.defenseChestplate || 8}\` | 护腿: \`${c.defenseLeggings || 6}\` | 靴子: \`${c.defenseBoots || 3}\`\n`;
      md += `- 材质参数：护甲韧性: \`${c.toughness || 3.0}\` | 击退抗性: \`${c.knockbackResistance || 0.1}\` | 耐久系数: \`${c.durabilityMultiplier || 37}\` | 附魔亲和度: \`${c.enchantability || 15}\`\n`;
      md += `- 铁砧修理物：\`${c.repairMaterial || 'minecraft:netherite_ingot'}\` | 穿戴音效：\`${c.equipSound || 'minecraft:item.armor.equip_netherite'}\` | 防火特性: \`${c.isFireResistant ? '免疫火焰熔岩' : '否'}\` | 猪灵中立: \`${c.makesPiglinsNeutral ? '是 (类似金护甲)' : '否'}\`\n\n`;

      if (c.hasSetBonus) {
        md += `### 全身套装集齐被动技能 (Set Bonus)\n`;
        md += `- 触发机制：当玩家同时装备全部 4 件装备时，每 Tick 赋予玩家状态效果：\`MobEffects.${c.setBonusEffect}\` (Amplifier: ${c.setBonusAmplifier})\n`;
        md += `- 效果描述：${c.setBonusDescription || '激活护甲共鸣'}\n\n`;
      }

      // 高级模式素材绑定规范
      if (isAdv) {
        md += `# 素材与资源资产路径绑定规范 (Assets & Texture Mappings)\n`;
        md += `> **重要约束**：以下素材已在项目中完成绘制或存放，DataGen 代码与实体穿着模型定义**必须严格精准对齐下列相对路径**，严禁擅自修改或使用虚构的文件名：\n\n`;
        md += `- **穿着模型图层 1 (Layer 1: 头盔/胸甲/靴子)**：\`${c.assetArmorLayer1 || `assets/${modId}/textures/models/armor/${armorId}_layer_1.png`}\`\n`;
        md += `- **穿着模型图层 2 (Layer 2: 护腿 Leggings)**：\`${c.assetArmorLayer2 || `assets/${modId}/textures/models/armor/${armorId}_layer_2.png`}\`\n`;
        md += `- **4 件独立物品贴图**：\n`;
        md += `  - 头盔物品贴图：\`${c.assetItemHelmet || `assets/${modId}/textures/item/${armorId}_helmet.png`}\`\n`;
        md += `  - 胸甲物品贴图：\`${c.assetItemChestplate || `assets/${modId}/textures/item/${armorId}_chestplate.png`}\`\n`;
        md += `  - 护腿物品贴图：\`${c.assetItemLeggings || `assets/${modId}/textures/item/${armorId}_leggings.png`}\`\n`;
        md += `  - 靴子物品贴图：\`${c.assetItemBoots || `assets/${modId}/textures/item/${armorId}_boots.png`}\`\n\n`;
      }

      md += `# 输出文件目录树规范 (Output Files Layout)\n`;
      md += `1. Java 业务代码：\n`;
      md += `   - \`src/main/java/com/example/${modId}/item/armor/${armorMaterialClass}.java\`\n`;
      md += `   - \`src/main/java/com/example/${modId}/registry/ModItems.java\`\n`;
      md += `2. 数据与资产层 (${c.codeGenerationStrategy === 'DATAGEN' ? '自动化 DataGen' : '静态 JSON 资源'})：\n`;
      md += `   - 模型定义：\`assets/${modId}/models/item/${armorId}_helmet.json\`, \`${armorId}_chestplate.json\`, 等\n`;
      md += `   - 语言文件：\`assets/${modId}/lang/zh_cn.json\`, \`assets/${modId}/lang/en_us.json\`\n\n`;

      if (c.conventionalCommit) {
        md += `# Git 规范提交信息\n`;
        md += `\`\`\`bash\ngit commit -m "feat(armor): add ${nameEn} (${armorId}) with MCreator specs and asset bindings"\n\`\`\`\n`;
      }

      return md;
    }
  };

  global.TypeArmor = TypeArmor;
  if (global.TypeRegistry) {
    global.TypeRegistry.register('armor', TypeArmor);
  }
})(window);
