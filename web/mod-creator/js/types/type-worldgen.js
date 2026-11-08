// ============================================================
// type-worldgen.js — 世界生成特征与矿物资产类型模块 (WorldGen Feature)
// 结合 MCreator 核心参数模型深度重构，支持高级模式素材地址选择与资产路径绑定
// 严格遵守 Google Gemini 规范与 Zero-Emoji 规则
// ============================================================

(function (global) {
  'use strict';

  const DEFAULT_CONFIG = {
    // 基础资产标识
    modId: 'custom_mod',
    featureId: 'custom_ore_feature',
    nameZh: '自定义矿脉生成',
    nameEn: 'Custom Ore Feature',
    featureType: 'ORE', // ORE | STRUCTURE | GEODE | DUNGEON

    // MCreator 素材地址绑定 (高级模式重点)
    assetStructureNbt: 'data/custom_mod/structures/custom_structure.nbt',
    assetProcessorJson: '', // 处理器规则
    assetLootTable: 'data/custom_mod/loot_tables/chests/custom_structure_chest.json',

    // 矿石生成规格与靶体 (MCreator Ore Targets & Placement)
    replaceTarget: 'DEEPSLATE', // STONE | DEEPSLATE | NETHERRACK | END_STONE | DIRT
    oreBlockId: 'custom_mod:deepslate_astral_ore',
    clusterSize: 8,
    discardOnAirChance: 0.0,

    // 高度区间与分布曲线 (MCreator Height Range & Distribution)
    distributionType: 'TRIANGLE', // TRIANGLE | UNIFORM | TRAPEZOID
    minHeight: -64,
    maxHeight: 16,
    veinsPerChunk: 7,
    rareChance: 1, // 1 表示每区块均尝试生成

    // 群系过滤与修饰器 (MCreator Biome Filtering & BiomeModifier)
    biomeFilter: 'OVERWORLD_MINING', // OVERWORLD_MINING | ALL_OVERWORLD | MOUNTAINS_ONLY | NETHER_WASTES | END_HIGHLANDS
    customBiomeTag: '#minecraft:is_overworld',
    generationStep: 'UNDERGROUND_ORES', // UNDERGROUND_ORES | SURFACE_STRUCTURES | VEGETAL_DECORATION

    // 高阶工业级系统
    useJsonConfiguredFeature: true,
    useJsonPlacedFeature: true,
    forgeBiomeModifier: true,
    codeGenerationStrategy: 'DATAGEN',
    conventionalCommit: true
  };

  const PRESETS = {
    deepslate_rare_vein: {
      name: '深层深板岩稀有矿脉 (三角分布 -64~16)',
      desc: '生成于主世界深板岩岩层的稀有高阶矿物，采用三角分布曲线在 -32 高度聚集，8格小矿簇。',
      mode: 'advanced',
      data: {
        ...DEFAULT_CONFIG,
        featureId: 'deepslate_astral_ore_vein',
        nameZh: '深层星辉水晶矿脉',
        nameEn: 'Deepslate Astral Ore Vein',
        featureType: 'ORE',
        replaceTarget: 'DEEPSLATE',
        oreBlockId: 'custom_mod:deepslate_astral_ore',
        clusterSize: 8,
        distributionType: 'TRIANGLE',
        minHeight: -64,
        maxHeight: 16,
        veinsPerChunk: 6
      }
    },
    ancient_ruins_structure: {
      name: '远古遗迹废墟结构 (NBT 模板文件与宝箱)',
      desc: '地表/地底远古神殿遗迹，通过 NBT 模板构建，带专属战利品宝箱表。',
      mode: 'advanced',
      data: {
        ...DEFAULT_CONFIG,
        featureId: 'ancient_ruins',
        nameZh: '远古神殿遗迹',
        nameEn: 'Ancient Ruins',
        featureType: 'STRUCTURE',
        generationStep: 'SURFACE_STRUCTURES',
        biomeFilter: 'ALL_OVERWORLD',
        assetStructureNbt: 'data/custom_mod/structures/ancient_ruins.nbt',
        assetLootTable: 'data/custom_mod/loot_tables/chests/ancient_ruins_chest.json'
      }
    }
  };

  const TypeWorldGen = {
    id: 'worldgen',
    label: '世界生成 (WorldGen)',
    description: '地下矿脉、地牢或 NBT 建筑结构，具备高度分布曲线、靶体判定与 Forge BiomeModifier',
    iconName: 'mc_worldgen',
    defaultConfig: DEFAULT_CONFIG,
    presets: PRESETS,

    renderConfigFlow(c, isAdv, activeStep) {
      const modId = c.modId || 'custom_mod';
      const featureId = c.featureId || 'custom_ore_feature';

      return `
        <div class="gemini-assistant-bubble">
          <div class="assistant-avatar">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M12 2C12 7.523 7.523 12 2 12C7.523 12 12 16.477 12 22C12 16.477 16.477 12 22 12C16.477 12 12 7.523 12 2Z"/></svg>
          </div>
          <div class="assistant-content">
            <div class="assistant-title">${isAdv ? '正在进行 [高级模式] MCreator 世界生成架构配置' : '正在进行 [普通模式] 矿物速填'}</div>
            <div class="assistant-desc">
              ${isAdv 
                ? '已开启 MCreator 矿物/结构类型切换、三角/均匀分布曲线、BiomeModifier 标签注入与 NBT 模板文件素材地址选择绑定。' 
                : '配置矿石 ID、替换靶体、单簇大小与 Y 轴生成高度。未配置项自动按主世界铁矿标准提供安全配置。'}
            </div>
          </div>
          <div class="bubble-actions">
            <button class="btn-bubble-preset" onclick="PromptUI.showPresetPicker()">套用经典模板</button>
          </div>
        </div>

        <div class="flow-step-bar">
          <div class="step-item ${activeStep >= 1 ? 'active' : ''}" onclick="PromptUI.goToStep(1)">
            <span class="step-num">1</span>
            <span class="step-name">靶体与高度分布</span>
          </div>
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 2 ? 'active' : ''}" onclick="PromptUI.goToStep(2)">
            <span class="step-num">2</span>
            <span class="step-name">群系修饰器</span>
          </div>
          ${isAdv ? `
          <div class="step-divider"></div>
          <div class="step-item ${activeStep >= 3 ? 'active' : ''}" onclick="PromptUI.goToStep(3)">
            <span class="step-num">3</span>
            <span class="step-name">素材绑定与构建</span>
          </div>` : ''}
        </div>

        <div class="config-grid-sections">
          <!-- 步骤 1：核心标识、替换靶体与高度分布 -->
          <div class="config-section-card ${activeStep === 1 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 1</span>
              <h3>世界生成特征核心标识与 MCreator 替换靶体</h3>
              <span class="section-status-hint">底层规范由外部 Skill 统一接管</span>
            </div>

            <!-- 核心标识紧凑栏 -->
            <div class="grid-3 mb-3" style="margin-bottom:14px;">
              <div class="form-group">
                <label>特征注册英文 ID (snake_case) <span class="required">*</span></label>
                <input type="text" class="form-control" value="${c.featureId || 'custom_ore_feature'}" 
                       placeholder="如 deepslate_astral_ore_vein" oninput="PromptUI.updateVal('featureId', this.value)" />
              </div>
              <div class="form-group">
                <label>生成特征中文名</label>
                <input type="text" class="form-control" value="${c.nameZh || '自定义矿脉生成'}" 
                       placeholder="如：深层星辉水晶矿脉" oninput="PromptUI.updateVal('nameZh', this.value); PromptUI.syncOmniSearch(this.value);" />
              </div>
              <div class="form-group">
                <label>生成特征类型 (Feature Type)</label>
                <select class="form-control" onchange="PromptUI.updateVal('featureType', this.value); PromptUI.rerenderFlow();">
                  <option value="ORE" ${c.featureType === 'ORE' ? 'selected' : ''}>地下矿脉 (Ore Vein Feature)</option>
                  <option value="STRUCTURE" ${c.featureType === 'STRUCTURE' ? 'selected' : ''}>NBT 结构建筑 (Structure Feature)</option>
                  <option value="GEODE" ${c.featureType === 'GEODE' ? 'selected' : ''}>空心晶洞 (Geode Feature)</option>
                </select>
              </div>
            </div>
            
            ${c.featureType === 'ORE' ? `
            <div class="section-body grid-3">
              <div class="form-group">
                <label>替换靶体方块 (RuleTest Target)</label>
                <select class="form-control" onchange="PromptUI.updateVal('replaceTarget', this.value)">
                  <option value="DEEPSLATE" ${c.replaceTarget === 'DEEPSLATE' ? 'selected' : ''}>深板岩层 (Deepslate: Y < 0)</option>
                  <option value="STONE" ${c.replaceTarget === 'STONE' ? 'selected' : ''}>主世界石头 (Stone: Y > 0)</option>
                  <option value="NETHERRACK" ${c.replaceTarget === 'NETHERRACK' ? 'selected' : ''}>下界岩 (Netherrack)</option>
                  <option value="END_STONE" ${c.replaceTarget === 'END_STONE' ? 'selected' : ''}>末地石 (End Stone)</option>
                </select>
              </div>
              <div class="form-group">
                <label>生成的目标矿物方块 ID</label>
                <input type="text" class="form-control" value="${c.oreBlockId || `${modId}:deepslate_ore`}" 
                       placeholder="如 ${modId}:deepslate_astral_ore" 
                       oninput="PromptUI.updateVal('oreBlockId', this.value)" />
              </div>
              <div class="form-group">
                <label>单矿簇大小 (Cluster Size: 1-64)</label>
                <input type="number" class="form-control" value="${c.clusterSize}" min="1" max="64"
                       oninput="PromptUI.updateVal('clusterSize', parseInt(this.value)||8)" />
              </div>
            </div>
            ` : ''}

            <div class="sub-form-box mt-3">
              <h4 style="margin:0 0 10px 0; font-size:13px; color:var(--gemini-text);">高度区间与分布算法 (Height Range)</h4>
              <div class="grid-3">
                <div class="form-group">
                  <label>高度分布曲线 (Distribution Type)</label>
                  <select class="form-control" onchange="PromptUI.updateVal('distributionType', this.value)">
                    <option value="TRIANGLE" ${c.distributionType === 'TRIANGLE' ? 'selected' : ''}>三角分布 (中间聚集两端递减，推荐)</option>
                    <option value="UNIFORM" ${c.distributionType === 'UNIFORM' ? 'selected' : ''}>均匀分布 (整段高度平权生成)</option>
                    <option value="TRAPEZOID" ${c.distributionType === 'TRAPEZOID' ? 'selected' : ''}>梯形分布 (梯级分布)</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>最低 Y 轴高度 (Min Height: -64 ~ 320)</label>
                  <input type="number" class="form-control" value="${c.minHeight}" step="8"
                         oninput="PromptUI.updateVal('minHeight', parseInt(this.value)||-64)" />
                </div>
                <div class="form-group">
                  <label>最高 Y 轴高度 (Max Height: -64 ~ 320)</label>
                  <input type="number" class="form-control" value="${c.maxHeight}" step="8"
                         oninput="PromptUI.updateVal('maxHeight', parseInt(this.value)||16)" />
                </div>
              </div>
              <div class="grid-2 mt-2">
                <div class="form-group">
                  <label>每区块尝试生成次数 (Veins per Chunk: 1-32)</label>
                  <input type="number" class="form-control" value="${c.veinsPerChunk}" min="1" max="64"
                         oninput="PromptUI.updateVal('veinsPerChunk', parseInt(this.value)||7)" />
                </div>
              </div>
            </div>
          </div>

          <!-- 步骤 2：群系过滤与修饰器 -->
          <div class="config-section-card ${activeStep === 2 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag">步骤 2</span>
              <h3>MCreator 生物群系过滤与 Forge BiomeModifier</h3>
            </div>
            <div class="section-body grid-2">
              <div class="form-group">
                <label>目标群系过滤预设</label>
                <select class="form-control" onchange="PromptUI.updateVal('biomeFilter', this.value)">
                  <option value="OVERWORLD_MINING" ${c.biomeFilter === 'OVERWORLD_MINING' ? 'selected' : ''}>主世界所有可采掘群系 (#is_overworld)</option>
                  <option value="MOUNTAINS_ONLY" ${c.biomeFilter === 'MOUNTAINS_ONLY' ? 'selected' : ''}>仅限山脉与高地 (#is_mountain)</option>
                  <option value="NETHER_WASTES" ${c.biomeFilter === 'NETHER_WASTES' ? 'selected' : ''}>仅限下界荒地与熔岩群系 (#is_nether)</option>
                  <option value="END_HIGHLANDS" ${c.biomeFilter === 'END_HIGHLANDS' ? 'selected' : ''}>仅限末地高地与岛屿 (#is_end)</option>
                </select>
              </div>
              <div class="form-group">
                <label>世界生成阶段 (Generation Step)</label>
                <select class="form-control" onchange="PromptUI.updateVal('generationStep', this.value)">
                  <option value="UNDERGROUND_ORES" ${c.generationStep === 'UNDERGROUND_ORES' ? 'selected' : ''}>UNDERGROUND_ORES (地下矿物阶段)</option>
                  <option value="SURFACE_STRUCTURES" ${c.generationStep === 'SURFACE_STRUCTURES' ? 'selected' : ''}>SURFACE_STRUCTURES (地表建筑结构阶段)</option>
                  <option value="VEGETAL_DECORATION" ${c.generationStep === 'VEGETAL_DECORATION' ? 'selected' : ''}>VEGETAL_DECORATION (地表植被阶段)</option>
                </select>
              </div>
            </div>
          </div>

          <!-- 步骤 3：MCreator 素材地址选择绑定与构建 (高级模式专享) -->
          ${isAdv ? `
          <div class="config-section-card advanced-exclusive ${activeStep === 3 ? 'current' : ''}">
            <div class="section-header">
              <span class="section-tag purple">步骤 3 · 高级模式专享</span>
              <h3>MCreator 素材地址选择与工程构建</h3>
            </div>

            <!-- NBT 结构素材选择 -->
            <div style="margin-bottom:20px;">
              <h4 style="margin:0 0 10px 0; font-size:14px; font-weight:600; color:var(--gemini-text);">
                结构模板 NBT 与数据资产地址绑定
              </h4>
              <div class="grid-2" style="gap:12px;">
                ${PromptUI.renderAssetPicker({
                  key: 'assetStructureNbt',
                  label: '结构模板 NBT 文件路径',
                  hint: 'data/<modId>/structures/<structure>.nbt',
                  value: c.assetStructureNbt || `data/${modId}/structures/${featureId}.nbt`,
                  defaultPath: `data/${modId}/structures/${featureId}.nbt`,
                  accept: '.nbt',
                  prefix: `data/${modId}/structures/`,
                  icon: 'model'
                })}
                ${PromptUI.renderAssetPicker({
                  key: 'assetLootTable',
                  label: '战利品宝箱表 JSON 路径',
                  hint: 'data/<modId>/loot_tables/chests/<structure>_chest.json',
                  value: c.assetLootTable || `data/${modId}/loot_tables/chests/${featureId}_chest.json`,
                  defaultPath: `data/${modId}/loot_tables/chests/${featureId}_chest.json`,
                  accept: '.json',
                  prefix: `data/${modId}/loot_tables/chests/`,
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
                <label>Forge BiomeModifier 注入机制</label>
                <input type="text" class="form-control" value="forge:add_features 数据包动态修饰器" disabled />
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
      const featureId = (c.featureId || 'custom_ore_feature').trim().toLowerCase().replace(/[^a-z0-9_]/g, '_');
      const nameZh = c.nameZh || '自定义世界生成';
      const nameEn = c.nameEn || 'Custom Feature';
      const mountedSkill = skillPath || 'd:/xiangmu/yezi/.agents/skills/moonmod/SKILL.md';

      let md = '';
      md += `# 项目核心规范与执行契约 (External Skill Mounting)\n`;
      md += `> **外部项目规范**：\`${mountedSkill}\`\n`;
      md += `> **执行指令**：开始编写代码前，必须优先完整读取并无条件遵循上述挂载的 Skill 规范文档（包括目标平台版本、Forge/NeoForge API 契约、分包命名结构、前后端分离以及 DataGen 规范）。\n\n`;

      md += `# 目标资产需求定义 (Target WorldGen: ${nameZh})\n`;
      md += `请为模组实现全新世界生成特征：${nameZh} (${modId}:${featureId})，严格对齐 MCreator 核心元素定义与工业级规范。\n\n`;

      md += `## 1. 基础标识与核心配置规格 (Specifications)\n`;
      md += `- 特征标识：Mod ID: \`${modId}\` | 注册名: \`${featureId}\` | 类型: \`${c.featureType}\`\n`;
      md += `- 本地化显示：中文: \`${nameZh}\` | 英文: \`${nameEn}\`\n`;
      if (c.featureType === 'ORE') {
        md += `- 靶体判定：替换目标: \`${c.replaceTarget}\` | 生成目标方块: \`${c.oreBlockId}\` | 矿簇大小: \`${c.clusterSize}\`\n`;
      }
      md += `- 高度分布：曲线算法: \`${c.distributionType}\` | 高度区间: \`Y = ${c.minHeight} ~ ${c.maxHeight}\` | 每区块频次: \`${c.veinsPerChunk}\` 簇\n`;
      md += `- 群系与修饰器：注入阶段: \`GenerationStep.Decoration.${c.generationStep}\` | 群系过滤: \`${c.biomeFilter}\`\n\n`;

      // 高级模式素材绑定规范
      if (isAdv) {
        md += `# 素材与资源资产路径绑定规范 (Assets & Structure Mappings)\n`;
        md += `> **重要约束**：以下模板与战利品素材已在项目中完成绘制或存放，DataGen 或数据包定义**必须严格精准对齐下列相对路径**，严禁擅自修改或使用虚构的文件名：\n\n`;
        if (c.featureType === 'STRUCTURE') {
          md += `- **结构模板 NBT 路径**：\`${c.assetStructureNbt || `data/${modId}/structures/${featureId}.nbt`}\`\n`;
          md += `- **战利品宝箱表 JSON**：\`${c.assetLootTable || `data/${modId}/loot_tables/chests/${featureId}_chest.json`}\`\n`;
        } else {
          md += `- **目标方块资产对齐**：必须引用已注册的 \`${c.oreBlockId}\` 模型与贴图。\n`;
        }
        md += `\n`;
      }

      md += `# 输出文件目录树规范 (Output Files Layout)\n`;
      md += `1. 数据包配置层 (Data Pack Layout)：\n`;
      md += `   - \`data/${modId}/worldgen/configured_feature/${featureId}.json\`\n`;
      md += `   - \`data/${modId}/worldgen/placed_feature/${featureId}.json\`\n`;
      md += `   - \`data/${modId}/forge/biome_modifier/${featureId}_modifier.json\`\n`;
      if (c.featureType === 'STRUCTURE') {
        md += `   - \`data/${modId}/structures/${featureId}.nbt\`\n`;
      }
      md += `2. Java DataGen 提供器：\n`;
      md += `   - \`src/main/java/com/example/${modId}/datagen/ModWorldGenProvider.java\`\n\n`;

      if (c.conventionalCommit) {
        md += `# Git 规范提交信息\n`;
        md += `\`\`\`bash\ngit commit -m "feat(worldgen): add ${nameEn} (${featureId}) with MCreator distribution and BiomeModifier"\n\`\`\`\n`;
      }

      return md;
    }
  };

  global.TypeWorldGen = TypeWorldGen;
  if (global.TypeRegistry) {
    global.TypeRegistry.register('worldgen', TypeWorldGen);
  }
})(window);
