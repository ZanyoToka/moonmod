// ============================================================
// type-tool.js — 武器与工具资产类型模块 (Tool)
// 结合 MCreator 核心参数模型深度重构，支持高级模式素材地址选择与资产路径绑定
// 严格遵守 Google Gemini 规范与 Zero-Emoji 规则
// ============================================================

(function (global) {
  'use strict';

  const DEFAULT_CONFIG = {
    // 标识与命名空间 (技术栈与平台规范由外部挂载 Skill 统一管辖)
    modId: 'custom_mod',
    toolId: 'custom_sword',
    nameZh: '自定义武器',
    nameEn: 'Custom Weapon',
    creativeTab: 'COMBAT', // COMBAT | TOOLS_AND_UTILITIES

    // MCreator 素材地址绑定 (高级模式重点)
    assetToolTexture: 'assets/custom_mod/textures/item/custom_sword.png',
    assetCustomModel: '', // 留空则使用 item/handheld 标准模型
    assetAttackSound: 'minecraft:entity.player.attack.sweep',
    assetHitSound: 'minecraft:entity.player.attack.strong',

    // 工具分类与梯队等级 (MCreator Tool Type & Tier)
    toolType: 'sword', // sword | pickaxe | axe | shovel | hoe | multi_tool
    tier: 'diamond', // wood | stone | iron | gold | diamond | netherite | custom
    miningLevel: 3, // 采掘等级: 0木石 | 1铁 | 2钻石 | 3下界合金 | 4神话
    knockbackBonus: 0.0, // 攻击额外击退距离加成 (0.0-3.0)
    
    // 战斗与挖掘核心规格 (MCreator Properties)
    attackDamageModifier: 3.0, // 基础附加攻击伤害 (剑通常+3，配合Tier达到7点)
    attackSpeedModifier: -2.4, // 攻速偏置 (4.0 - 2.4 = 1.6次/秒；斧头一般为-3.0)
    durability: 1561, // 耐久度
    miningSpeed: 8.0, // 采掘速度乘数
    enchantability: 14, // 附魔亲和度
    repairMaterial: 'minecraft:diamond', // 铁砧维修材料
    isFireResistant: false, // 免疫火焰熔岩
    hasSweeping: true, // 横扫之刃能力 (剑类专属)

    // 特殊技能与命中被动 (MCreator Procedures & Triggers)
    hasSpecialAbility: false,
    abilityType: 'NONE', // NONE | ON_HIT_EFFECT | RIGHT_CLICK_SKILL | MINING_AOE
    hitEffect: 'NONE', // NONE | WITHER | SLOWNESS | POISON | FIRE | LIGHTNING
    hitEffectDuration: 60,
    hitEffectAmplifier: 0,
    rightClickSkillCooldown: 80,
    abilityDescription: '',

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
    ancient_netherite_greatsword: {
      name: '远古下界合金重剑 (破甲重击与凋零)',
      desc: '单发极致伤害，攻击命中赋予目标凋零 II 效果持续4秒，免疫火焰与岩浆焚化。',
      mode: 'advanced',
      data: {
        ...DEFAULT_CONFIG,
        toolId: 'ancient_greatsword',
        nameZh: '远古下界合金重剑',
        nameEn: 'Ancient Netherite Greatsword',
        creativeTab: 'COMBAT',
        toolType: 'sword',
        tier: 'netherite',
        attackDamageModifier: 5.0,
        attackSpeedModifier: -2.8,
        durability: 2031,
        enchantability: 18,
        repairMaterial: 'minecraft:netherite_ingot',
        isFireResistant: true,
        hasSweeping: true,
        hasSpecialAbility: true,
        abilityType: 'ON_HIT_EFFECT',
        hitEffect: 'WITHER',
        hitEffectDuration: 80,
        hitEffectAmplifier: 1,
        abilityDescription: '命中生物时附加凋零 II 效果并击退目标',
        hasTooltip: true,
        tooltipText: '蕴含下界熔岩威能的重刃，一击足以撕裂防御。',
        assetToolTexture: 'assets/custom_mod/textures/item/ancient_greatsword.png'
      }
    },
    omni_mining_drill: {
      name: '全能采矿钻头 (镐斧铲三合一 Multi-tool)',
      desc: '集镐、斧、铲于一身的多功能采矿工具，超高挖掘速率，支持快速平整地形。',
      mode: 'simple',
      data: {
        ...DEFAULT_CONFIG,
        toolId: 'omni_mining_drill',
        nameZh: '全能采矿电钻',
        nameEn: 'Omni Mining Drill',
        creativeTab: 'TOOLS_AND_UTILITIES',
        toolType: 'multi_tool',
        tier: 'diamond',
        attackDamageModifier: 2.0,
        attackSpeedModifier: -2.0,
        durability: 3000,
        miningSpeed: 12.0,
        repairMaterial: 'minecraft:iron_block',
        isFireResistant: true,
        hasTooltip: true,
        tooltipText: '工业级复合采矿工具，兼容石头、泥土与木质方块采掘。',
        assetToolTexture: 'assets/custom_mod/textures/item/omni_mining_drill.png'
      }
    }
  };

  const TypeTool = {
    id: 'tool',
    label: '武器与工具 (Tool)',
    description: '剑、镐、斧、铲、锄或全能工具，具备采掘效率、攻击力、攻速偏置与特技被动',
    iconName: 'mc_tool',
    defaultConfig: DEFAULT_CONFIG,
    presets: PRESETS,

    renderConfigFlow(c, isAdv, activeStep) {
      const modId = c.modId || 'custom_mod';
      const toolId = c.toolId || 'custom_sword';

      return `
        <div class="gemini-assistant-bubble">
          <div class="assistant-avatar">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 2C12 7.523 7.523 12 2 12C7.523 12 12 16.477 12 22C12 16.477 16.477 12 22 12C16.477 12 12 7.523 12 2Z"/></svg>
          </div>
          <div class="assistant-content">
            <div class="assistant-title">${isAdv ? '正在进行 [高级模式] MCreator 武器与工具架构配置' : '正在进行 [普通模式] 工具速填'}</div>
            <div class="assistant-desc">
              ${isAdv 
                ? '已开启 MCreator 规范的完整 Tier 梯队、伤害攻速偏置、命中被动效果与材质素材路径选择绑定。' 
                : '配置工具分类、材质梯队、攻击力与耐久度。未配置项自动按对应原版材料梯队赋予标准参数。'}
            </div>
          </div>
          <div class="bubble-actions">
            <button class="btn-bubble-preset" onclick="PromptUI.showPresetPicker()">套用经典模板</button>
          </div>
        </div>

        <div class="flow-step-bar">
          <div class="step-item ${activeStep >= 1 ? 'active' : ''}" onclick="PromptUI.goToStep(1)">
            <span class="step-num">1</span>
            <span class="step-name">工具类型与数值</span>
          </div>
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 2 ? 'active' : ''}" onclick="PromptUI.goToStep(2)">
            <span class="step-num">2</span>
            <span class="step-name">特技与被动触发</span>
          </div>
          ${isAdv ? `
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 3 ? 'active' : ''}" onclick="PromptUI.goToStep(3)">
            <span class="step-num">3</span>
            <span class="step-name">MCreator 素材绑定</span>
          </div>` : ''}
        </div>

        <div class="config-grid-sections">
          <!-- 步骤 1：MCreator 工具类型与核心规格 (整合核心标识) -->
          <div class="config-section-card ${activeStep === 1 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 1</span>
              <h3>工具核心标识、Tier 梯队与战斗数值</h3>
              <span class="section-status-hint">底层规范由外部 Skill 统一接管</span>
            </div>

            <!-- 核心标识紧凑栏 -->
            <div class="grid-3 mb-3" style="margin-bottom:14px;">
              <div class="form-group">
                <label>工具注册英文 ID (snake_case) <span class="required">*</span></label>
                <input type="text" class="form-control" value="${c.toolId || 'custom_sword'}" 
                       placeholder="如 ancient_sword" oninput="PromptUI.updateVal('toolId', this.value)" />
              </div>
              <div class="form-group">
                <label>工具/武器中文名称</label>
                <input type="text" class="form-control" value="${c.nameZh || '自定义武器'}" 
                       placeholder="如：远古重剑" oninput="PromptUI.updateVal('nameZh', this.value); PromptUI.syncOmniSearch(this.value);" />
              </div>
              <div class="form-group">
                <label>创造模式标签栏 (Creative Tab)</label>
                <select class="form-control" onchange="PromptUI.updateVal('creativeTab', this.value)">
                  <option value="COMBAT" ${c.creativeTab === 'COMBAT' ? 'selected' : ''}>战斗武器 (Combat)</option>
                  <option value="TOOLS_AND_UTILITIES" ${c.creativeTab === 'TOOLS_AND_UTILITIES' ? 'selected' : ''}>工具实用 (Tools)</option>
                </select>
              </div>
            </div>
            </div>
            <div class="section-body grid-3">
              <div class="form-group">
                <label>工具类型分类 (Tool Type)</label>
                <select class="form-control" onchange="PromptUI.updateVal('toolType', this.value)">
                  <option value="sword" ${c.toolType === 'sword' ? 'selected' : ''}>剑 (Sword / 武器)</option>
                  <option value="pickaxe" ${c.toolType === 'pickaxe' ? 'selected' : ''}>镐 (Pickaxe / 矿物采掘)</option>
                  <option value="axe" ${c.toolType === 'axe' ? 'selected' : ''}>斧 (Axe / 伐木与破盾高伤)</option>
                  <option value="shovel" ${c.toolType === 'shovel' ? 'selected' : ''}>铲 (Shovel / 泥沙采掘)</option>
                  <option value="hoe" ${c.toolType === 'hoe' ? 'selected' : ''}>锄 (Hoe / 耕地与植物)</option>
                  <option value="multi_tool" ${c.toolType === 'multi_tool' ? 'selected' : ''}>多功能全能工具 (Multi-tool / 镐斧铲三合一)</option>
                </select>
              </div>
              <div class="form-group">
                <label>材质梯队 (Tier / TierMaterial)</label>
                <select class="form-control" onchange="PromptUI.updateVal('tier', this.value)">
                  <option value="wood" ${c.tier === 'wood' ? 'selected' : ''}>木制 (Wood Tier / Level 0)</option>
                  <option value="stone" ${c.tier === 'stone' ? 'selected' : ''}>石制 (Stone Tier / Level 1)</option>
                  <option value="iron" ${c.tier === 'iron' ? 'selected' : ''}>铁制 (Iron Tier / Level 2)</option>
                  <option value="diamond" ${c.tier === 'diamond' ? 'selected' : ''}>钻石 (Diamond Tier / Level 3)</option>
                  <option value="netherite" ${c.tier === 'netherite' ? 'selected' : ''}>下界合金 (Netherite Tier / Level 4)</option>
                  <option value="custom" ${c.tier === 'custom' ? 'selected' : ''}>自定义独立 Tier 梯队</option>
                </select>
              </div>
              <div class="form-group">
                <label>附加攻击伤害 (Attack Damage vs Mobs)</label>
                <input type="number" class="form-control" value="${c.attackDamageModifier}" step="0.5" min="0"
                       oninput="PromptUI.updateVal('attackDamageModifier', parseFloat(this.value)||3.0)" />
              </div>
              <div class="form-group">
                <label>攻速偏置 (Attack Speed Modifier)</label>
                <select class="form-control" onchange="PromptUI.updateVal('attackSpeedModifier', parseFloat(this.value))">
                  <option value="-2.4" ${c.attackSpeedModifier === -2.4 ? 'selected' : ''}>-2.4 (标准单手剑: 1.6次/秒)</option>
                  <option value="-3.0" ${c.attackSpeedModifier === -3.0 ? 'selected' : ''}>-3.0 (标准战斧: 1.0次/秒)</option>
                  <option value="-3.2" ${c.attackSpeedModifier === -3.2 ? 'selected' : ''}>-3.2 (重型巨剑/大锤: 0.8次/秒)</option>
                  <option value="-1.5" ${c.attackSpeedModifier === -1.5 ? 'selected' : ''}>-1.5 (快速匕首/刺剑: 2.5次/秒)</option>
                </select>
              </div>
              <div class="form-group">
                <label>最大耐久度 (Durability)</label>
                <input type="number" class="form-control" value="${c.durability}" min="1" step="50"
                       oninput="PromptUI.updateVal('durability', parseInt(this.value)||1561)" />
              </div>
              <div class="form-group">
                <label>采掘效率速率 (Mining Speed)</label>
                <input type="number" class="form-control" value="${c.miningSpeed}" step="0.5" min="1.0"
                       oninput="PromptUI.updateVal('miningSpeed', parseFloat(this.value)||8.0)" />
              </div>
              <div class="form-group">
                <label>铁砧修复物品材料 (Repair Item)</label>
                <input type="text" class="form-control" value="${c.repairMaterial || 'minecraft:diamond'}" 
                       placeholder="如 minecraft:diamond 或 custom_mod:crystal" 
                       oninput="PromptUI.updateVal('repairMaterial', this.value)" />
              </div>
              <div class="form-group">
                <label>采掘等级 (Harvest Level / Tier Level)</label>
                <select class="form-control" onchange="PromptUI.updateVal('miningLevel', parseInt(this.value))">
                  <option value="0" ${c.miningLevel === 0 ? 'selected' : ''}>Level 0 (木/金级 - 煤炭/石块)</option>
                  <option value="1" ${c.miningLevel === 1 ? 'selected' : ''}>Level 1 (石级 - 铁矿/青金石)</option>
                  <option value="2" ${c.miningLevel === 2 ? 'selected' : ''}>Level 2 (铁级 - 钻石/红石/金矿)</option>
                  <option value="3" ${c.miningLevel === 3 ? 'selected' : ''}>Level 3 (钻石级 - 远古残骸/黑曜石)</option>
                  <option value="4" ${c.miningLevel === 4 ? 'selected' : ''}>Level 4 (下界合金级及神话)</option>
                </select>
              </div>
              <div class="form-group">
                <label>额外攻击击退距离 (Knockback Bonus)</label>
                <input type="number" class="form-control" value="${c.knockbackBonus || 0.0}" step="0.5" min="0" max="5.0"
                       placeholder="0.0 表示原版击退，1.0 额外击退1格"
                       oninput="PromptUI.updateVal('knockbackBonus', parseFloat(this.value)||0.0)" />
              </div>
              <div class="form-group">
                <label>附魔亲和度 (Enchantability: 1-30)</label>
                <input type="number" class="form-control" value="${c.enchantability || 14}" min="1" max="50"
                       oninput="PromptUI.updateVal('enchantability', parseInt(this.value)||14)" />
              </div>
            </div>

            <div style="display:flex; flex-wrap:wrap; gap:16px; margin-top:14px;">
              <label class="pill-checkbox">
                <input type="checkbox" ${c.hasSweeping ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('hasSweeping', this.checked)" />
                <span>支持横扫之刃斩击 (Sweeping Attack)</span>
              </label>
              <label class="pill-checkbox">
                <input type="checkbox" ${c.isFireResistant ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('isFireResistant', this.checked)" />
                <span>免疫火焰与熔岩焚化 (Fire Resistant)</span>
              </label>
            </div>
          </div>

          <!-- 步骤 2：技能与命中被动 -->
          <div class="config-section-card ${activeStep === 2 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 2</span>
              <h3>特技与命中被动效果 (MCreator Procedures)</h3>
            </div>
            <div class="form-group mb-3">
              <label class="pill-checkbox">
                <input type="checkbox" ${c.hasSpecialAbility ? 'checked' : ''} 
                       onchange="PromptUI.updateVal('hasSpecialAbility', this.checked); PromptUI.rerenderFlow();" />
                <span style="font-weight:600;">启用武器专属特技或命中状态赋予</span>
              </label>
            </div>

            ${c.hasSpecialAbility ? `
            <div class="sub-form-box">
              <div class="grid-2">
                <div class="form-group">
                  <label>特技类型 (Ability Type)</label>
                  <select class="form-control" onchange="PromptUI.updateVal('abilityType', this.value); PromptUI.rerenderFlow();">
                    <option value="ON_HIT_EFFECT" ${c.abilityType === 'ON_HIT_EFFECT' ? 'selected' : ''}>命中生物附加药水状态 (On Hit Effect)</option>
                    <option value="RIGHT_CLICK_SKILL" ${c.abilityType === 'RIGHT_CLICK_SKILL' ? 'selected' : ''}>右键释放特技技能 (Right Click Active Skill)</option>
                    <option value="MINING_AOE" ${c.abilityType === 'MINING_AOE' ? 'selected' : ''}>采掘 3x3 范围连锁爆破 (AOE Mining)</option>
                  </select>
                </div>
                ${c.abilityType === 'ON_HIT_EFFECT' ? `
                <div class="form-group">
                  <label>赋予目标状态 (Effect)</label>
                  <select class="form-control" onchange="PromptUI.updateVal('hitEffect', this.value)">
                    <option value="WITHER" ${c.hitEffect === 'WITHER' ? 'selected' : ''}>凋零 (Wither)</option>
                    <option value="SLOWNESS" ${c.hitEffect === 'SLOWNESS' ? 'selected' : ''}>缓慢 (Slowness)</option>
                    <option value="POISON" ${c.hitEffect === 'POISON' ? 'selected' : ''}>中毒 (Poison)</option>
                    <option value="FIRE" ${c.hitEffect === 'FIRE' ? 'selected' : ''}>点燃目标着火 (Set On Fire)</option>
                    <option value="LIGHTNING" ${c.hitEffect === 'LIGHTNING' ? 'selected' : ''}>召唤雷击 (Strike Lightning)</option>
                  </select>
                </div>
                ` : ''}
              </div>
              <div class="form-group mt-2">
                <label>技能与被动详细逻辑说明</label>
                <textarea class="form-control" rows="2" 
                          placeholder="如：攻击命中时为目标赋予 4 秒凋零并击退 2 格"
                          oninput="PromptUI.updateVal('abilityDescription', this.value)">${c.abilityDescription || ''}</textarea>
              </div>
            </div>
            ` : ''}

            <div class="form-group mt-3">
              <label>悬浮提示信息 (Tooltip)</label>
              <input type="text" class="form-control" value="${c.tooltipText || ''}" 
                     placeholder="如：蕴含下界熔岩威能的重刃，一击足以撕裂防御"
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

            <!-- 素材选择区域 -->
            <div style="margin-bottom:20px;">
              <h4 style="margin:0 0 10px 0; font-size:14px; font-weight:600; color:var(--gemini-text);">
                相关素材地址绑定 (Textures, Models & Sound Assets)
              </h4>
              <div class="grid-2" style="gap:12px;">
                ${PromptUI.renderAssetPicker({
                  key: 'assetToolTexture',
                  label: '工具 2D 材质贴图 (Tool Texture PNG)',
                  hint: '标准 16x16 / 32x32 物品贴图',
                  value: c.assetToolTexture || `assets/${modId}/textures/item/${toolId}.png`,
                  defaultPath: `assets/${modId}/textures/item/${toolId}.png`,
                  accept: '.png',
                  prefix: `assets/${modId}/textures/item/`,
                  icon: 'image'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetCustomModel',
                  label: '自定义 3D 手持模型 JSON (可选)',
                  hint: '留空则使用 Minecraft 官方 item/handheld 模型',
                  value: c.assetCustomModel || '',
                  defaultPath: `assets/${modId}/models/item/${toolId}.json`,
                  accept: '.json',
                  prefix: `assets/${modId}/models/item/`,
                  icon: 'model'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetAttackSound',
                  label: '挥舞/横扫斩击音效 (Swing Sound)',
                  hint: '原版或自定义音频事件',
                  value: c.assetAttackSound || 'minecraft:entity.player.attack.sweep',
                  defaultPath: 'minecraft:entity.player.attack.sweep',
                  accept: '.ogg',
                  icon: 'audio'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetHitSound',
                  label: '武器击中目标音效 (Hit Sound)',
                  hint: '命中时的音效反馈',
                  value: c.assetHitSound || 'minecraft:entity.player.attack.strong',
                  defaultPath: 'minecraft:entity.player.attack.strong',
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
      const toolId = (c.toolId || 'custom_sword').trim().toLowerCase().replace(/[^a-z0-9_]/g, '_');
      const toolClass = toolId.split('_').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join('') + 'Item';
      const nameZh = c.nameZh || '自定义武器';
      const nameEn = c.nameEn || 'Custom Weapon';
      const mountedSkill = skillPath || 'd:/xiangmu/yezi/.agents/skills/moonmod/SKILL.md';

      let md = '';
      md += `# 项目核心规范与执行契约 (External Skill Mounting)\n`;
      md += `> **外部项目规范**：\`${mountedSkill}\`\n`;
      md += `> **执行指令**：开始编写代码前，必须优先完整读取并无条件遵循上述挂载的 Skill 规范文档（包括目标平台版本、Forge/NeoForge API 契约、分包命名结构、前后端分离以及 DataGen 规范）。\n\n`;

      md += `# 目标资产需求定义 (Target Tool: ${nameZh})\n`;
      md += `请为模组实现全新工具/武器：${nameZh} (${modId}:${toolId})，严格对齐 MCreator 核心元素规范与工业级标准。\n\n`;

      md += `## 1. 基础标识与战斗采掘规格 (Specifications)\n`;
      md += `- 资产标识：Mod ID: \`${modId}\` | 注册名: \`${toolId}\` | 创造栏: \`${c.creativeTab || 'COMBAT'}\`\n`;
      md += `- 本地化显示：中文: \`${nameZh}\` | 英文: \`${nameEn}\`\n`;
      md += `- 工具类型：\`${c.toolType.toUpperCase()}\` | 梯队等级：\`Tier.${c.tier.toUpperCase()}\` | 采掘等级: \`Level ${c.miningLevel || 0}\`\n`;
      md += `- 战斗属性：基础附加伤害: \`${c.attackDamageModifier}\` | 攻速偏置: \`${c.attackSpeedModifier}\` | 最大耐久: \`${c.durability}\` | 额外击退: \`${c.knockbackBonus || 0.0}\`\n`;
      md += `- 采掘属性：采掘效率速率: \`${c.miningSpeed}\` | 附魔亲和度: \`${c.enchantability || 14}\` | 铁砧修复物: \`${c.repairMaterial || 'minecraft:diamond'}\`\n`;
      md += `- 战斗特性：横扫攻击: \`${c.hasSweeping ? '支持' : '不支持'}\` | 免疫火焰: \`${c.isFireResistant ? '免疫' : '否'}\`\n\n`;

      if (c.hasSpecialAbility) {
        md += `### 特技与命中触发器 (Special Ability)\n`;
        md += `- 机制分类：\`${c.abilityType}\`\n`;
        if (c.abilityType === 'ON_HIT_EFFECT') {
          md += `- 命中赋予状态：\`MobEffects.${c.hitEffect}\` (持续: ${c.hitEffectDuration} ticks, 等级: ${c.hitEffectAmplifier})\n`;
        }
        md += `- 逻辑描述：${c.abilityDescription || '触发特殊被动'}\n\n`;
      }

      if (c.hasTooltip && c.tooltipText) {
        md += `### 悬浮提示 (Tooltip)\n`;
        md += `- 内容：\`${c.tooltipText}\`\n\n`;
      }

      // 高级模式素材绑定规范
      if (isAdv) {
        md += `# 素材与资源资产路径绑定规范 (Assets & Texture Mappings)\n`;
        md += `> **重要约束**：以下素材已在项目中完成绘制或存放，DataGen 代码与手持模型定义**必须严格精准对齐下列相对路径**，严禁擅自修改或使用虚构的文件名：\n\n`;
        md += `- **工具 2D 材质贴图**：\`${c.assetToolTexture || `assets/${modId}/textures/item/${toolId}.png`}\`\n`;
        if (c.assetCustomModel) {
          md += `- **自定义 3D 模型 JSON 路径**：\`${c.assetCustomModel}\`\n`;
        } else {
          md += `- **模型定义**：使用标准 \`item/handheld\` 父级模型渲染。\n`;
        }
        md += `- **挥击音效**：\`${c.assetAttackSound || 'minecraft:entity.player.attack.sweep'}\`\n`;
        md += `- **命中音效**：\`${c.assetHitSound || 'minecraft:entity.player.attack.strong'}\`\n\n`;
      }

      md += `# 输出文件目录树规范 (Output Files Layout)\n`;
      md += `1. Java 业务代码：\n`;
      md += `   - \`src/main/java/com/example/${modId}/item/${toolClass}.java\`\n`;
      md += `   - \`src/main/java/com/example/${modId}/registry/ModItems.java\`\n`;
      md += `2. 数据与资产层 (${c.codeGenerationStrategy === 'DATAGEN' ? '自动化 DataGen' : '静态 JSON 资源'})：\n`;
      md += `   - 模型定义：\`assets/${modId}/models/item/${toolId}.json\`\n`;
      md += `   - 语言文件：\`assets/${modId}/lang/zh_cn.json\`, \`assets/${modId}/lang/en_us.json\`\n\n`;

      if (c.conventionalCommit) {
        md += `# Git 规范提交信息\n`;
        md += `\`\`\`bash\ngit commit -m "feat(tool): add ${nameEn} (${toolId}) with MCreator specs and asset bindings"\n\`\`\`\n`;
      }

      return md;
    }
  };

  global.TypeTool = TypeTool;
  if (global.TypeRegistry) {
    global.TypeRegistry.register('tool', TypeTool);
  }
})(window);
