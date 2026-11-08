// ============================================================
// type-item.js — 普通物品资产类型模块 (Item)
// 结合 MCreator 核心参数模型深度重构，支持高级模式素材地址选择与资产路径绑定
// 严格遵守 Google Gemini 规范与 Zero-Emoji 规则
// ============================================================

(function (global) {
  'use strict';

  const DEFAULT_CONFIG = {
    // 标识与命名空间 (技术栈与平台规范由外部挂载 Skill 统一管辖)
    modId: 'custom_mod',
    itemId: 'custom_item',
    nameZh: '自定义物品',
    nameEn: 'Custom Item',
    creativeTab: 'MISC', // MISC | FOOD_AND_DRINKS | INGREDIENTS | COMBAT | TOOLS_AND_UTILITIES | BUILDING_BLOCKS

    // MCreator 视觉素材地址绑定 (高级模式重点)
    asgetItemTexture: 'assets/custom_mod/textures/item/custom_item.png',
    assetCustomModel: '', // 留空则使用 generated 默认 2D 图标
    assetUseSound: 'minecraft:entity.generic.eat',

    // 基础规格 (MCreator Properties)
    maxStackSize: 64,
    rarity: 'COMMON', // COMMON | UNCOMMON | RARE | EPIC
    hasFoil: false, // 常驻附魔光效 (Enchantment Glint)
    isFireResistant: false, // 免疫火焰与岩浆焚烧
    enchantability: 0, // 附魔亲和度权重 (0-30)
    repairMaterial: '', // 铁砧修复材料 (如 #forge:ingots/copper)
    burnTime: 0, // 熔炉燃烧时间 (ticks)
    craftRemainder: '', // 合成返还容器物 (如 minecraft:glass_bottle / minecraft:bowl)

    // MCreator 使用动作与动画机制 (Item Use Animation)
    itemUseAnimation: 'NONE', // NONE | EAT | DRINK | BLOCK | BOW | SPEAR | CROSSBOW
    useDuration: 32, // 使用动画持续时间 (ticks)

    // 食物与营养机制 (MCreator Food Properties)
    isFood: false,
    nutrition: 4,
    saturationModifier: 0.3,
    isMeat: false, // 是否为肉类 (狼是否食用)
    canAlwaysEat: false, // 饱食度满时是否可吃
    isFastFood: false, // 快速进食 (16 ticks)
    foodEffectType: 'NONE', // NONE | REGENERATION | SPEED | ABSORPTION | RESISTANCE | NIGHT_VISION | CUSTOM
    foodEffectDuration: 200,
    foodEffectAmplifier: 0,
    foodEffectProbability: 1.0,

    // 交互与右键触发 (MCreator Triggers)
    hasRightClickAction: false,
    rightClickActionType: 'NONE', // NONE | CONSUME | THROWABLE | OPEN_GUI | CUSTOM
    cooldownTicks: 0,
    rightClickDescription: '',

    // 描述与提示 (MCreator Tooltips)
    hasTooltip: false,
    tooltipText: '',
    tooltipShiftOnly: false,

    // 工程与构建规范
    codeGenerationStrategy: 'DATAGEN', // DATAGEN | STATIC_JSON
    generateLangFiles: true,
    conventionalCommit: true
  };

  const PRESETS = {
    hallowed_golden_apple: {
      name: '圣辉附魔金苹果 (神话级食物)',
      desc: '满饱食度可食用，常驻附魔光晕与岩浆防火，提供高额营养、生命恢复与伤害吸收。',
      mode: 'advanced',
      data: {
        ...DEFAULT_CONFIG,
        itemId: 'hallowed_golden_apple',
        nameZh: '圣辉附魔金苹果',
        nameEn: 'Hallowed Golden Apple',
        creativeTab: 'FOOD_AND_DRINKS',
        rarity: 'EPIC',
        hasFoil: true,
        isFireResistant: true,
        itemUseAnimation: 'EAT',
        useDuration: 32,
        isFood: true,
        nutrition: 8,
        saturationModifier: 1.2,
        canAlwaysEat: true,
        foodEffectType: 'REGENERATION',
        foodEffectDuration: 400,
        foodEffectAmplifier: 1,
        foodEffectProbability: 1.0,
        hasTooltip: true,
        tooltipText: '蕴含远古圣堂守护之力的神秘金苹果，提供持久生命复苏。',
        asgetItemTexture: 'assets/custom_mod/textures/item/hallowed_golden_apple.png'
      }
    },
    industrial_fuel_cell: {
      name: '工业高能等离子燃料棒 (工业燃料)',
      desc: '高效压缩工业燃料，燃烧时间 6400 刻(可烧制32件物品)，合成后返还空金属外壳。',
      mode: 'simple',
      data: {
        ...DEFAULT_CONFIG,
        itemId: 'plasma_fuel_cell',
        nameZh: '等离子高能燃料电池',
        nameEn: 'Plasma Fuel Cell',
        creativeTab: 'INGREDIENTS',
        maxStackSize: 16,
        rarity: 'UNCOMMON',
        isFireResistant: true,
        burnTime: 6400,
        craftRemainder: 'minecraft:iron_ingot',
        hasTooltip: true,
        tooltipText: '工业核聚变高能反应燃料，用尽后返还金属外壳。',
        asgetItemTexture: 'assets/custom_mod/textures/item/plasma_fuel_cell.png'
      }
    }
  };

  const TypeItem = {
    id: 'item',
    label: '普通物品 (Item)',
    description: '基础合成原料、营养食物、药效药丸或功能消耗道具，具备动画与右键交互',
    iconName: 'mc_item',
    defaultConfig: DEFAULT_CONFIG,
    presets: PRESETS,

    renderConfigFlow(c, isAdv, activeStep) {
      const modId = c.modId || 'custom_mod';
      const itemId = c.itemId || 'custom_item';

      return `
        <div class="gemini-assistant-bubble">
          <div class="assistant-avatar">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 2C12 7.523 7.523 12 2 12C7.523 12 12 16.477 12 22C12 16.477 16.477 12 22 12C16.477 12 12 7.523 12 2Z"/></svg>
          </div>
          <div class="assistant-content">
            <div class="assistant-title">${isAdv ? '正在进行 [高级模式] MCreator 物品架构配置' : '正在进行 [普通模式] 物品速填'}</div>
            <div class="assistant-desc">
              ${isAdv 
                ? '已开启 MCreator 完整使用动作动画、进食药效概率、合成容器残余与材质/模型素材路径选择绑定。' 
                : '配置物品名称、堆叠数、稀有度与基本食物或燃料属性。未配置项自动赋予原版通用物品标准。'}
            </div>
          </div>
          <div class="bubble-actions">
            <button class="btn-bubble-preset" onclick="PromptUI.showPresetPicker()">套用经典模板</button>
          </div>
        </div>

        <div class="flow-step-bar">
          <div class="step-item ${activeStep >= 1 ? 'active' : ''}" onclick="PromptUI.goToStep(1)">
            <span class="step-num">1</span>
            <span class="step-name">核心规格与动画</span>
          </div>
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 2 ? 'active' : ''}" onclick="PromptUI.goToStep(2)">
            <span class="step-num">2</span>
            <span class="step-name">食物与交互触发</span>
          </div>
          ${isAdv ? `
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 3 ? 'active' : ''}" onclick="PromptUI.goToStep(3)">
            <span class="step-num">3</span>
            <span class="step-name">MCreator 素材绑定</span>
          </div>` : ''}
        </div>

        <div class="config-grid-sections">
          <!-- 步骤 1：MCreator 核心规格、动画与标识 -->
          <div class="config-section-card ${activeStep === 1 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 1</span>
              <h3>物品核心标识与 MCreator 基础规格</h3>
              <span class="section-status-hint">底层规范由外部 Skill 统一接管</span>
            </div>

            <!-- 核心标识紧凑栏 -->
            <div class="grid-3 mb-3" style="margin-bottom:14px;">
              <div class="form-group">
                <label>物品注册英文 ID (snake_case) <span class="required">*</span></label>
                <input type="text" class="form-control" value="${c.itemId || 'custom_item'}" 
                       placeholder="如 ancient_rune_core" oninput="PromptUI.updateVal('itemId', this.value)" />
              </div>
              <div class="form-group">
                <label>物品中文名称</label>
                <input type="text" class="form-control" value="${c.nameZh || '自定义物品'}" 
                       placeholder="如：远古符文核心" oninput="PromptUI.updateVal('nameZh', this.value); PromptUI.syncOmniSearch(this.value);" />
              </div>
              <div class="form-group">
                <label>创造模式标签页 (Creative Tab)</label>
                <select class="form-control" onchange="PromptUI.updateVal('creativeTab', this.value)">
                  <option value="MISC" ${c.creativeTab === 'MISC' ? 'selected' : ''}>杂项 (Misc)</option>
                  <option value="FOOD_AND_DRINKS" ${c.creativeTab === 'FOOD_AND_DRINKS' ? 'selected' : ''}>食物与饮品 (Food & Drinks)</option>
                  <option value="INGREDIENTS" ${c.creativeTab === 'INGREDIENTS' ? 'selected' : ''}>原材料 (Ingredients)</option>
                  <option value="COMBAT" ${c.creativeTab === 'COMBAT' ? 'selected' : ''}>战斗 (Combat)</option>
                  <option value="TOOLS_AND_UTILITIES" ${c.creativeTab === 'TOOLS_AND_UTILITIES' ? 'selected' : ''}>工具与实用 (Tools)</option>
                </select>
              </div>
            </div>
            </div>
            <div class="section-body grid-3">
              <div class="form-group">
                <label>最大堆叠数量 (Max Stack: 1-64)</label>
                <select class="form-control" onchange="PromptUI.updateVal('maxStackSize', parseInt(this.value))">
                  <option value="64" ${c.maxStackSize === 64 ? 'selected' : ''}>64 (默认标准堆叠)</option>
                  <option value="16" ${c.maxStackSize === 16 ? 'selected' : ''}>16 (类似末影珍珠/鸡蛋/桶)</option>
                  <option value="1" ${c.maxStackSize === 1 ? 'selected' : ''}>1 (不可堆叠/装备/容器)</option>
                </select>
              </div>
              <div class="form-group">
                <label>稀有度品阶 (Rarity)</label>
                <select class="form-control" onchange="PromptUI.updateVal('rarity', this.value)">
                  <option value="COMMON" ${c.rarity === 'COMMON' ? 'selected' : ''}>COMMON (白字普通)</option>
                  <option value="UNCOMMON" ${c.rarity === 'UNCOMMON' ? 'selected' : ''}>UNCOMMON (黄字罕见)</option>
                  <option value="RARE" ${c.rarity === 'RARE' ? 'selected' : ''}>RARE (青字稀有)</option>
                  <option value="EPIC" ${c.rarity === 'EPIC' ? 'selected' : ''}>EPIC (紫字史诗)</option>
                </select>
              </div>
              <div class="form-group">
                <label>使用动作动画 (ItemUseAnimation)</label>
                <select class="form-control" onchange="PromptUI.updateVal('itemUseAnimation', this.value)">
                  <option value="NONE" ${c.itemUseAnimation === 'NONE' ? 'selected' : ''}>NONE (无动画/瞬间使用)</option>
                  <option value="EAT" ${c.itemUseAnimation === 'EAT' ? 'selected' : ''}>EAT (食用咀嚼)</option>
                  <option value="DRINK" ${c.itemUseAnimation === 'DRINK' ? 'selected' : ''}>DRINK (饮用动画与音效)</option>
                  <option value="BLOCK" ${c.itemUseAnimation === 'BLOCK' ? 'selected' : ''}>BLOCK (格挡防御)</option>
                  <option value="BOW" ${c.itemUseAnimation === 'BOW' ? 'selected' : ''}>BOW (拉弓蓄力)</option>
                  <option value="SPEAR" ${c.itemUseAnimation === 'SPEAR' ? 'selected' : ''}>SPEAR (三叉戟蓄力投掷)</option>
                  <option value="CROSSBOW" ${c.itemUseAnimation === 'CROSSBOW' ? 'selected' : ''}>CROSSBOW (装填弩机)</option>
                </select>
              </div>
              <div class="form-group">
                <label>使用动作持续时间 (ticks: 1-100)</label>
                <input type="number" class="form-control" value="${c.useDuration || 32}" min="1" max="72000"
                       oninput="PromptUI.updateVal('useDuration', parseInt(this.value)||32)" />
              </div>
              <div class="form-group">
                <label>熔炉燃烧时间 (BurnTime in Ticks)</label>
                <input type="number" class="form-control" value="${c.burnTime || 0}" min="0" step="200"
                       placeholder="0 表示不可作燃料，200 为烧制1个物品"
                       oninput="PromptUI.updateVal('burnTime', parseInt(this.value)||0)" />
              </div>
              <div class="form-group">
                <label>合成/使用返还容器 (Craft Remainder)</label>
                <input type="text" class="form-control" value="${c.craftRemainder || ''}" 
                       placeholder="如 minecraft:glass_bottle 或 minecraft:bowl" 
                       oninput="PromptUI.updateVal('craftRemainder', this.value)" />
              </div>
              <div class="form-group">
                <label>附魔能力值 (Enchantability: 0-30)</label>
                <input type="number" class="form-control" value="${c.enchantability || 0}" min="0" max="30"
                       placeholder="金制为22，普通物品一般为0或10"
                       oninput="PromptUI.updateVal('enchantability', parseInt(this.value)||0)" />
              </div>
              <div class="form-group">
                <label>铁砧修复原料 (Repair Material Tag/Item)</label>
                <input type="text" class="form-control" value="${c.repairMaterial || ''}" 
                       placeholder="如 minecraft:diamond 或 #forge:gems" 
                       oninput="PromptUI.updateVal('repairMaterial', this.value)" />
              </div>
            </div>

            <div style="display:flex; flex-wrap:wrap; gap:16px; margin-top:14px;">
              <label class="pill-checkbox">
                <input type="checkbox" ${c.hasFoil ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('hasFoil', this.checked)" />
                <span>常驻附魔光晕 (Enchantment Glint)</span>
              </label>
              <label class="pill-checkbox">
                <input type="checkbox" ${c.isFireResistant ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('isFireResistant', this.checked)" />
                <span>免疫火焰与熔岩焚毁 (Fire Resistant)</span>
              </label>
            </div>
          </div>

          <!-- 步骤 2：食物机制与右键触发 -->
          <div class="config-section-card ${activeStep === 2 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 2</span>
              <h3>食物与药效机制、交互触发器 (MCreator Food & Procedures)</h3>
            </div>
            
            <div class="form-group mb-3">
              <label class="pill-checkbox">
                <input type="checkbox" ${c.isFood ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('isFood', this.checked); PromptUI.rerenderFlow();" />
                <span style="font-weight:600;">启用食物机制 (FoodProperties)</span>
              </label>
            </div>

            ${c.isFood ? `
            <div class="sub-form-box">
              <div class="grid-3">
                <div class="form-group">
                  <label>回复饱食度点数 (Nutrition: 1-20)</label>
                  <input type="number" class="form-control" value="${c.nutrition || 4}" min="1" max="20"
                         oninput="PromptUI.updateVal('nutrition', parseInt(this.value)||4)" />
                </div>
                <div class="form-group">
                  <label>饱和度修正倍率 (Saturation)</label>
                  <input type="number" class="form-control" value="${c.saturationModifier || 0.3}" step="0.1" min="0.1" max="2.0"
                         oninput="PromptUI.updateVal('saturationModifier', parseFloat(this.value)||0.3)" />
                </div>
                <div class="form-group">
                  <label>食用附加药水效果</label>
                  <select class="form-control" onchange="PromptUI.updateVal('foodEffectType', this.value)">
                    <option value="NONE" ${c.foodEffectType === 'NONE' ? 'selected' : ''}>无附加效果</option>
                    <option value="REGENERATION" ${c.foodEffectType === 'REGENERATION' ? 'selected' : ''}>生命恢复 (Regeneration)</option>
                    <option value="SPEED" ${c.foodEffectType === 'SPEED' ? 'selected' : ''}>速度提升 (Speed)</option>
                    <option value="ABSORPTION" ${c.foodEffectType === 'ABSORPTION' ? 'selected' : ''}>伤害吸收 (Absorption)</option>
                    <option value="RESISTANCE" ${c.foodEffectType === 'RESISTANCE' ? 'selected' : ''}>抗性提升 (Resistance)</option>
                    <option value="NIGHT_VISION" ${c.foodEffectType === 'NIGHT_VISION' ? 'selected' : ''}>夜视 (Night Vision)</option>
                  </select>
                </div>
              </div>
              <div style="display:flex; flex-wrap:wrap; gap:16px; margin-top:12px;">
                <label class="pill-checkbox">
                  <input type="checkbox" ${c.canAlwaysEat ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('canAlwaysEat', this.checked)" />
                  <span>饱食度满时仍可食用 (如金苹果)</span>
                </label>
                <label class="pill-checkbox">
                  <input type="checkbox" ${c.isMeat ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('isMeat', this.checked)" />
                  <span>属于肉类食品 (可喂养/驯服狼)</span>
                </label>
                <label class="pill-checkbox">
                  <input type="checkbox" ${c.isFastFood ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('isFastFood', this.checked)" />
                  <span>快速进食 (耗时减半 16 ticks)</span>
                </label>
              </div>
            </div>
            ` : ''}

            <div class="grid-2 mt-3">
              <div class="form-group">
                <label>右键使用交互 (use / onRightClick)</label>
                <label class="pill-checkbox">
                  <input type="checkbox" ${c.hasRightClickAction ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('hasRightClickAction', this.checked); PromptUI.rerenderFlow();" />
                  <span>启用右键特殊交互逻辑</span>
                </label>
                ${c.hasRightClickAction ? `
                  <textarea class="form-control mt-2" rows="2" 
                            placeholder="如：右键消耗自身并释放闪电或给予实体增益"
                            oninput="PromptUI.updateVal('rightClickDescription', this.value)">${c.rightClickDescription || ''}</textarea>
                  <div class="form-group mt-2">
                    <label>冷却时间 (Cooldown Ticks)</label>
                    <input type="number" class="form-control" value="${c.cooldownTicks || 0}" min="0" step="10"
                           placeholder="20 ticks = 1 秒"
                           oninput="PromptUI.updateVal('cooldownTicks', parseInt(this.value)||0)" />
                  </div>
                ` : ''}
              </div>
              <div class="form-group">
                <label>悬浮文字提示 (Tooltip)</label>
                <label class="pill-checkbox">
                  <input type="checkbox" ${c.hasTooltip ? 'checked' : ''} 
                         onchange="PromptUI.updateVal('hasTooltip', this.checked); PromptUI.rerenderFlow();" />
                  <span>启用描述说明文本</span>
                </label>
                ${c.hasTooltip ? `
                  <input type="text" class="form-control mt-2" value="${c.tooltipText || ''}" 
                         placeholder="如：远古符文凝结而成的神秘矿石核心"
                         oninput="PromptUI.updateVal('tooltipText', this.value)" />
                  <label class="pill-checkbox mt-2">
                    <input type="checkbox" ${c.tooltipShiftOnly ? 'checked' : ''} 
                           onchange="PromptUI.updateVal('tooltipShiftOnly', this.checked)" />
                    <span>仅在玩家按住 Shift 时显示提示详情</span>
                  </label>
                ` : ''}
              </div>
            </div>
          </div>

          <!-- 步骤 3：MCreator 素材地址选择绑定与高级构建 (高级模式专享) -->
          ${isAdv ? `
          <div class="config-section-card advanced-exclusive ${activeStep === 3 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag purple">步骤 3 · 高级模式专享</span>
              <h3>MCreator 素材地址选择与数据构建</h3>
            </div>

            <!-- 素材地址绑定 -->
            <div style="margin-bottom:20px;">
              <h4 style="margin:0 0 10px 0; font-size:14px; font-weight:600; color:var(--gemini-text);">
                相关素材地址绑定 (Textures, Models & Sounds)
              </h4>
              <div class="grid-2" style="gap:12px;">
                ${PromptUI.renderAssetPicker({
                  key: 'asgetItemTexture',
                  label: '物品 2D 材质贴图 (Item Texture PNG)',
                  hint: '标准 16x16 / 32x32 物品 PNG 贴图',
                  value: c.asgetItemTexture || `assets/${modId}/textures/item/${itemId}.png`,
                  defaultPath: `assets/${modId}/textures/item/${itemId}.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/item/`,
                  icon: 'image'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetCustomModel',
                  label: '自定义 3D 物品模型 JSON (可选)',
                  hint: '留空则自动使用标准 2D generated 扁平模型',
                  value: c.assetCustomModel || '',
                  defaultPath: `assets/${modId}/models/item/${itemId}.json`,
                  accept: '.json',
                  prefix: `assets/${modId}/models/item/`,
                  icon: 'model'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetUseSound',
                  label: '使用/进食音效 (Sound Event)',
                  hint: '原版或自定义音频事件',
                  value: c.assetUseSound || 'minecraft:entity.generic.eat',
                  defaultPath: 'minecraft:entity.generic.eat',
                  accept: '.ogg',
                  icon: 'audio'
                })}
              </div>
            </div>

            <!-- 代码资产构建规范 -->
            <div class="section-body grid-2">
              <div class="form-group">
                <label>代码生成策略</label>
                <select class="form-control" onchange="PromptUI.updateVal('codeGenerationStrategy', this.value)">
                  <option value="DATAGEN" ${c.codeGenerationStrategy === 'DATAGEN' ? 'selected' : ''}>Forge 自动化 DataGen 生成器</option>
                  <option value="STATIC_JSON" ${c.codeGenerationStrategy === 'STATIC_JSON' ? 'selected' : ''}>静态 JSON 资产文件</option>
                </select>
              </div>
              <div class="form-group">
                <label>自动生成语言键值 (Lang Keys)</label>
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
      const itemId = (c.itemId || 'custom_item').trim().toLowerCase().replace(/[^a-z0-9_]/g, '_');
      const itemClass = itemId.split('_').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('') + 'Item';
      const nameZh = c.nameZh || '自定义物品';
      const nameEn = c.nameEn || 'Custom Item';
      const mountedSkill = skillPath || 'd:/xiangmu/yezi/.agents/skills/moonmod/SKILL.md';

      let md = '';
      md += `# 项目核心规范与执行契约 (External Skill Mounting)\n`;
      md += `> **外部项目规范**：\`${mountedSkill}\`\n`;
      md += `> **执行指令**：开始编写代码前，必须优先完整读取并无条件遵循上述挂载的 Skill 规范文档（包括目标平台版本、Forge/NeoForge API 契约、分包命名结构、前后端分离以及 DataGen 规范）。\n\n`;

      md += `# 目标资产需求定义 (Target Item: ${nameZh})\n`;
      md += `请为模组实现全新物品：${nameZh} (${modId}:${itemId})，严格遵循 MCreator 核心元素定义与工业级规范。\n\n`;

      md += `## 1. 基础标识与物理规格 (Specifications)\n`;
      md += `- 资产标识：Mod ID: \`${modId}\` | 注册名: \`${itemId}\` | 创造栏: \`${c.creativeTab || 'MISC'}\`\n`;
      md += `- 本地化显示：中文: \`${nameZh}\` | 英文: \`${nameEn}\`\n`;
      md += `- 堆叠与稀有度：最大堆叠: \`${c.maxStackSize || 64}\` | 稀有度: \`Rarity.${c.rarity || 'COMMON'}\` | 附魔光效: \`${c.hasFoil ? '常驻闪烁' : '无'}\` | 附魔亲和度: \`${c.enchantability || 0}\`\n`;
      if (c.repairMaterial) md += `- 铁砧修复原料：Tag/Item: \`${c.repairMaterial}\`\n`;
      md += `- 动作动画：\`UseAnim.${c.itemUseAnimation || 'NONE'}\` (持续刻数: \`${c.useDuration || 32}\` ticks)\n`;
      if (c.burnTime > 0) md += `- 熔炉燃烧值：\`${c.burnTime}\` ticks (可烧制 ${Math.round(c.burnTime / 200)} 个物品)\n`;
      if (c.craftRemainder) md += `- 容器返还残余物：\`${c.craftRemainder}\`\n`;
      if (c.isFireResistant) md += `- 特性：免疫火焰与熔岩焚毁 (fireResistant)\n`;
      md += `\n`;

      if (c.isFood) {
        md += `### 食物与营养机制 (Food Properties)\n`;
        md += `- 营养值：\`${c.nutrition || 4}\` 点饱食度 | 饱和度修正：\`${c.saturationModifier || 0.3}\`\n`;
        md += `- 食用限制：\`${c.canAlwaysEat ? '饱食度满时依然可吃 (alwaysEat)' : '饥饿时方可食用'}\` | 肉类：\`${c.isMeat ? '是 (狼可食用)' : '否'}\` | 快速进食：\`${c.isFastFood ? '是 (fastFood 16 ticks)' : '否'}\`\n`;
        if (c.foodEffectType && c.foodEffectType !== 'NONE') {
          md += `- 附加药效：\`MobEffects.${c.foodEffectType}\` (持续: ${c.foodEffectDuration} ticks, 等级: ${c.foodEffectAmplifier}, 几率: ${c.foodEffectProbability * 100}%)\n`;
        }
        md += `\n`;
      }

      if (c.hasRightClickAction) {
        md += `### 交互触发器 (Procedures & Triggers)\n`;
        md += `- 右键事件描述：${c.rightClickDescription || '触发特殊动作'}\n`;
        if (c.cooldownTicks > 0) md += `- 冷却时间：${c.cooldownTicks} ticks\n`;
        md += `\n`;
      }

      if (c.hasTooltip) {
        md += `### 提示信息 (Tooltip)\n`;
        md += `- 内容：\`${c.tooltipText || ''}\` (${c.tooltipShiftOnly ? '按住 Shift 显示' : '常驻显示'})\n\n`;
      }

      // 高级模式素材绑定规范
      if (isAdv) {
        md += `# 素材与资源资产路径绑定规范 (Assets & Texture Mappings)\n`;
        md += `> **重要约束**：以下素材已在项目中完成绘制或存放，DataGen 代码与模型定义**必须严格精准对齐下列相对路径**，严禁擅自修改或使用虚构的文件名：\n\n`;
        md += `- **物品 2D 材质贴图**：\`${c.asgetItemTexture || `assets/${modId}/textures/item/${itemId}.png`}\`\n`;
        if (c.assetCustomModel) {
          md += `- **自定义 3D 模型 JSON 路径**：\`${c.assetCustomModel}\`\n`;
        } else {
          md += `- **模型定义**：使用标准 \`item/generated\` 单层贴图渲染。\n`;
        }
        md += `- **使用音效**：\`${c.assetUseSound || 'minecraft:entity.generic.eat'}\`\n\n`;
      }

      md += `# 输出文件目录树规范 (Output Files Layout)\n`;
      md += `1. Java 业务代码：\n`;
      md += `   - \`src/main/java/com/example/${modId}/item/${itemClass}.java\`\n`;
      md += `   - \`src/main/java/com/example/${modId}/registry/ModItems.java\`\n`;
      md += `2. 数据与资产层 (${c.codeGenerationStrategy === 'DATAGEN' ? '自动化 DataGen' : '静态 JSON 资源'})：\n`;
      md += `   - 模型定义：\`assets/${modId}/models/item/${itemId}.json\`\n`;
      md += `   - 语言文件：\`assets/${modId}/lang/zh_cn.json\`, \`assets/${modId}/lang/en_us.json\`\n\n`;

      if (c.conventionalCommit) {
        md += `# Git 规范提交信息\n`;
        md += `\`\`\`bash\ngit commit -m "feat(item): add ${nameEn} (${itemId}) with MCreator specs and asset bindings"\n\`\`\`\n`;
      }

      return md;
    }
  };

  global.TypeItem = TypeItem;
  if (global.TypeRegistry) {
    global.TypeRegistry.register('item', TypeItem);
  }
})(window);
