# 更新日志 (Changelog)

本文档记录《月亮计划三部曲 MC 同人模组》(moonmod) 的所有重要迭代与版本变更。
更新记录严格遵守团队技术规范（三铁律）：**拒绝黑盒提交、分类清晰记录、附带核心机制简述**。

---

## [Unreleased] - 2026-11-01

### [规范 / Docs]
- **技术规范落地入库**：
  - 在 `SKILL.md` 中确立并固化「团队研发三大技术铁律」：
    1. **前后端严格分离与美术资产外部化解耦**（服务端严禁调用客户端类，源稿放外部结构）；
    2. **文档先行与规范化更新日志制度**（拒绝模糊黑盒提交，版本与文档实时同步）；
    3. **底层实现逻辑透明化**（必须遵循“三问白盒说明法”，保证考核与 Code Review 可行性）。
  - 创建并规范化本 `CHANGELOG.md`，确立后续团队开发提交流程。

### [新增 / Feat]
- **人格抽取舱产出「人格卡」饰品**（`moonproject:identity_card`）：抽取舱原本吐出的是「附魔书 + 自定义名字」的伪书页，物品本身不可佩戴、不进任何系统，等于一次性消耗品。现在产物统一为真正可佩戴的人格卡。
  - **一个物品装所有人格**：人格身份存在 NBT（`CardId` / `CardGrade`），数据表在 `item/IdentityCardData`（9 张内置人格：0 级 3 张、00 级 3 张、000 级 3 张）。加新人格只需在数据表登记一行，不必新增物品、模型、贴图。
  - **接入 Curios**：`item/IdentityCardItem` 实现 `ICurioItem`，佩戴于「手部」槽（`data/curios/tags/items/hands.json` 登记），支持手持右键佩戴；属性通过 `getAttributeModifiers` 由 Curios 原生命令驱动，不需要自己轮询 tick 增删。
  - **抽取表**：E.G.O 核心 / 下界之星 → 000 级；异想体碎片 / 紫水晶镜面碎片 → 00 级（仍保留 1/4 概率升格出 E.G.O 核心）；书籍 / 经验瓶 → 0 级。
  - **数值口径**：卡面写基础值，实际下发 = 基础值 × `identity.cardPowerScale`（Config，默认 1.0，0..5；设为 0 则卡片只剩外观与文本）。
  - **白盒三问**：①数据在哪——人格身份在物品 NBT，属性表在 `IdentityCardData` 静态登记，倍率在 Config，均不落存档以外的位置；②前后端怎么同步——无需网络包，Curios 在佩戴/卸下的槽位事件里向服务端申请重算属性，客户端读同一份 NBT 与 Config 渲染 tooltip；③边界——NBT 被清空时 `IdentityCardData.of` 回落到 0 级第一张，不会 NPE；`cardPowerScale = 0` 时所有 modifier 直接不下发（`put` 里对 0 值提前 return）。
- **武器简介直接写明技能机制**：阿赖耶识的物品提示（tooltip）新增「技能机制」段——出鞘持续时长、天杀 / 将我抹去的斩缘消耗与射程、斩缘上限、记忆灼烧节奏，全部**从 Config 读取**（`drawDurationTicks`、`heavenlyKillCutbondCost`、`eraseCutbondCost`、`skill3Range`、`cutbondMaxStacks`、`memoryBurnIntervalTicks`、`memoryCostPerBurn`），改配置后简介自动跟着变，不存在写死的数字。
  - 同时删掉原来那句写死「每秒 -1 级经验」的旧提示（`item.moonproject.alaya_vijnana.memory`），避免与真实配置漂移；英文提示里残留的旧键位「N / M」也一并改为 Z / X。
  - **白盒三问**：①数据在哪——简介文本只在 `lang/*.json`，数字全部来自服务端 Config；②前后端怎么同步——`appendHoverText` 在客户端读的是同步后的配置值，无需额外网络包；③边界——配置未加载时 getter 回落默认值，简介不会出现 `%s` 未替换的空洞。
- **天杀 / 将我抹去改为「乱刀」白刃特效**：两个技能不再只留一道弧光，而是一次斩出成片的纯白刃痕（天杀默认 28 条、抹去默认 36 条），散布在以目标为中心的椭球内（水平 ±1.5 格、竖直 ±1.1 格），每道弧的倾角 0..360° 随机、大小 0.45~1.2 倍随机，看上去是被乱刀切开。寿命仍是 30 秒（`effects.slashScarLifetimeTicks` 600 tick），数量可在 `effects.slashScarHeavenlyCount` / `effects.slashScarEraseCount` 调整（0 = 关闭）。
  - 成本提示：每次施法会生成几十个实体，连续狂按技能可能掉帧，卡的话把那两个数字调小即可。
- **刀光残留（开刃后斩击会在原地留下 30 秒弧光）**：此前斩击特效只是「一闪即逝的粒子」，关掉瞬间空气里什么都没有。新增实体 `entity/SlashScarEntity`（`moonproject:slash_scar`）与客户端 `client/renderer/SlashScarRenderer`：
  - 斩击命中时在目标半高处、沿视线回退 0.45 格生成一条弧光，随机倾斜 ±35°；重击 ×1.4、天杀 ×1.6、概念抹除 ×1.7 且换成冷白色（变体 1）。
  - 外观为始终正对镜头的 billboard 面片（贴图 `textures/entity/slash_scar.png`，程序化生成的纯白月牙弧；概念抹除版本略带冷调），4 tick 淡入、寿命最后 40 tick 淡出，30 秒后由服务端 `discard()`。
  - **为什么用实体而不是粒子**：粒子有总量上限，且离玩家超过 32 格会被剔除，撑不住「原地留 30 秒」；实体是服务端权威的，位置与消失时刻对所有玩家一致，且 `noSave()` 不写存档、不参与碰撞与选中。
  - **白盒三问**：①数据在哪——`life/scale/roll/variant` 存在 `SynchedEntityData`，外观与时长由 Config `effects` 段决定，不落盘；②前后端怎么同步——服务端 `addFreshEntity` 同步生成、到期 `discard` 自动下发移除包，客户端只按 `tickCount` 做插值动画，不做任何「该不该消失」的判定；③边界——区块卸载即消失（`noSave`），配置 `slashScarLifetimeTicks` 下限 20 tick，避免寿命为 0 时实体残留一帧。
- **刀光与碎屑整体放大**：`black_slash` 粒子 quadSize 0.09→0.20、寿命 9+8→12+10、命中爆发 18/10→26/16；`concept_shard` quadSize 0.07→0.13、寿命 18+12→22+14、抹除爆发 40→56。配合新铁律，尺寸参数留在粒子类里（纯视觉常量，不进 Config）。
- **Config 新增 `effects` 段**：`slashScarLifetimeTicks`（600，20..6000）、`slashScarScale`（3.2，0.5..12）。
- **阿赖耶识步骤 3：音效与粒子系统落地**（此前全模组 `sounds.json` 不存在、`assets/moonproject/sounds/` 零文件、粒子类零个，武器是「哑巴」）：
  - **音效**：新建 `assets/moonproject/sounds.json` 与 `init/ModSounds` 的 6 个事件——`alaya_draw`（出鞘）、`alaya_sheath`（归鞘）、`alaya_slash`（挥砍）、`alaya_slash_heavy`（重斩）、`alaya_erase`（概念抹除）、`alaya_memory_burn`（记忆灼烧），配套 `subtitles.moonproject.*` 字幕键（中英双语）。音频为一次性脚本合成（正弦/噪声/包络），**不含任何外部素材，无版权风险**。
  - **粒子**：新建 `init/ModParticles` 与 `client/particle/` 三个 `TextureSheetParticle`——`memory_ember`（记忆余烬，负重力上浮）、`black_slash`（焦黑斩痕碎屑，受重力下落）、`concept_shard`（概念残片，四散冷白），贴图在 `textures/particle/`、帧表在 `assets/moonproject/particles/`。
  - **接入点**：拔刀/收刀播金属音（拔刀额外放一圈 24 颗余烬）；每烧一次记忆播 `alaya_memory_burn` + 4 颗余烬；记忆烧空与限时到点强制收刀时播低音高归鞘音；命中播挥砍音 + 落点碎屑（重击技能用低沉版）；天杀与「将我抹去，也将你抹去」各有专属音效与碎屑。
  - **白盒三问**：①数据在哪——音效只有事件名（`ModSounds`）与文件映射（`sounds.json`），粒子只有类型 id（`ModParticles`），外观由客户端 Provider 决定，都不进存档；②前后端怎么同步——服务端 `playSound(null, …)` 与 `ServerLevel#sendParticles(…)` 广播，客户端自行模拟渲染，不在客户端重复播放（否则单机会听到两遍）；③边界——记忆烧空、倒计时归零、抑制失败等分支不会误播成功音。
- **收容失效闭环：破门对象由核心决定**：
  - 过去 `ContainmentUnitBlockEntity.triggerBreach` 无论放什么核心，都生成一只凋灵骷髅占位；现在破门时先读取槽内核心的 `coreId`（`AbnormalityCoreItem.getCoreId`，必须在清空槽位之前读），`scorched_girl` 直接放出**焦化少女本体**（`ModEntities.SCORCHED_GIRL`，带 `setPersistenceRequired` 与「§4[收容失效] 焦化少女」名牌），其它身份回落到「狂暴化身」占位怪，保证任何物品都有破门表现。
  - 收容核心槽改为只接受 `AbnormalityCoreItem` 与通用「异想体核心」（后者没有 `coreId`，破门时走回落分支）。
  - 收容单元 GUI 新增「收容对象：<异想体名>」读数；抑制按钮加了悬浮提示（显示恢复量与两种代价）。
  - 破门警报文本带上异想体名：`[脑叶公司警报] 收容室 (x, y, z) 逆卡巴拉计数归零！<名称> 已突破收容！`
- **焦化少女生成蛋**（`moonproject:scorched_girl_spawn_egg`）：此前生存里根本没有办法遇到异想体，收容单元等于摆设。现在它自爆后有概率掉落「终末火柴」（焦化少女核心），形成「引怪 → 击杀 → 取核心 → 装机 → 等破门/做抑制」的最小闭环。
- **9 个方块补齐合成配方**：`containment_unit`、`reinforced_plating`（一次 4 个）、`meltdown_siren`、`enkephalin_extractor`、`identity_extractor`、`bookbinding_table`、`mephistopheles_engine`、`kcorp_injector`、`golden_bough_shrine`。此前 12 个方块只有 3 个有配方，其余只能在创造模式里拿。

### [变更 / Changed]
- **技能键位调整**：天杀 `N → Z`、将我抹去 `M → X`。Z / X 在本模组内无冲突（模组已占用 J = 饰品栏），原版也不默认占用这两个键；`R` 仍留给 Epic Fight。物品提示文本同步更新。
  - 注意：已存档的 `options.txt` 若保存过旧绑定，需要到「选项 → 控制」手动重绑，或删掉那两行让它回落到新默认。

### [平衡 / Balance]
- **焦化少女数值改为可配置（默认值保持原作者手调）**：原 `TRIGGER_RANGE=1.0`、`FUSE_TICKS=1`、`EXPLODE_RADIUS=200.0F`、固定 100 伤害、`ExplosionInteraction.MOB` 全部硬编码。现改为 `scorched_girl` 配置段——`triggerRange` 1.0、`fuseTicks` 1、`explodeRadius` 200.0、`explodeDamage` 100、`griefing` true、`coreDropChance` 0.35，**默认行为与原来完全一致**（1 格点燃、几乎立即自爆、200 格半径、破坏地形、100 伤害）。
  - 新增 `griefing` 开关：设为 false 时改用 `ExplosionInteraction.NONE`，只造成伤害不破坏方块——半径 200 且破坏地形会瞬间清空大片区域并让服务端卡死，想调大半径做测试时建议先关掉。
  - 注：曾短暂把默认值改成「4 格 / 3 秒 / 不破坏方块」，属于擅自改动原作者设定，已按作者要求还原。
- **焦化少女改为「苦力怕式」纯自爆单位**：删掉 `MeleeAttackGoal`（不再挥刀）、`ATTACK_DAMAGE` 置 0，改由 `MoveTowardsTargetGoal`（判定 32 格）负责靠过去；伤害全部来自爆炸。移速改为配置项 `scorched_girl.moveSpeed`（默认 0.07 ≈ 3.0 格/秒，玩家步行 0.1 ≈ 4.3、冲刺 ≈ 5.6，所以它追不上跑动中的玩家，但会一路跟过来）。引线时长仍为原作者设定的 1 tick（几乎立即爆炸），未改动。
- **紧急抑制实装代价**：原来点按钮白拿 +25 安定度。现在优先扣 `containment.suppressXpLevels`（3 级经验），经验不足改扣 `containment.suppressSanityCost`（10 点理智），两者都不够则抑制失败且不扣任何代价；安定度已满时直接拒绝。
- **收容单元衰减节拍进 Config**：`80 + 40×防爆壁` 改为 `containment.decayBaseTicks`（80）+ `containment.decayPlatingBonusTicks`（40）× 相邻防爆壁数。

### [修复 / Fixed]
- **技能按键「没反应」的反馈缺失**：`castUltimateByKey` 原先在「没拿阿赖耶识」「没出鞘」两种情况下直接 `return`，一句话都不给，玩家完全无法判断失败原因。现在三种情况都会在动作栏给出明确提示（未手持 / 未出鞘 / 斩缚不足 X 层、当前 Y 层），只有「范围内没有目标」沿用原提示。
- **补齐「场谐振能源采集单元」缺失的语言键**：
  - `en_us.json` 补 `block.moonproject.resonance_collector` / `item.moonproject.resonance_collector`（此前英文客户端会直接显示原始 key）。
  - `zh_cn.json` 补 `item.moonproject.resonance_collector`。
  - `python docs/verify_assets.py` 由「1 个问题」恢复为全绿。
- **修复「阿赖耶识」3D OBJ 模型被覆盖成平面 2D 的问题**：
  - 现象：鞘/拔刀两种形态在物品栏与手持时都退化为一张平面贴图，Blender 高模消失。
  - 根因：`0dcee77 加入方块` 提交批量新增方块时，把 `alaya_vijnana.json` 与 `alaya_vijnana_drawn.json` 的 `"loader": "moonproject:obj_item"` 误改成 `"parent": "minecraft:item/handheld"`（同类误伤第二次，此前 `3114524` 已修过一次）。
  - 修复：两份模型 JSON 按 `3114524` 内容还原为 OBJ 加载器定义（保留材质映射 `BladeSteel`/`TsubaGold`/`GoldFit`/`HiltMat`/`SheathMat`、`flip_v`、完整 `display` 变换表与鞘态 `moonproject:drawn` 谓词 overrides）。
  - 说明：Java 侧 `client/obj/ObjItemModel`、`client/ObjModelRegistration`、`AlayaClientSetup:25` 的谓词注册与五张材质贴图均在，未改动；OBJ/MTL 资产完好。
- **恢复「阿赖耶识」出鞘烧血机制**：
  - 现象：拔刀后不再持续掉血，「以血为代价换强度」的核心代价机制消失。
  - 根因：同样是 `0dcee77 加入方块` 用较旧的文件覆盖了 `event/AbyssCallEvents.java`，删掉了 `DRAIN_INTERVAL`（20 tick）、`DRAIN_DIVISOR`（30.0F）、拔刀态判定与 `setHealth` 扣血逻辑；同时把「阿鼻叫唤」触发条件从「拔刀态」放宽成「手持即触发」。
  - 修复：重写 `AbyssCallEvents`——`holding = held.is(ModItems.ALAYA_VIJNANA)`，`drawn = holding && AlayaVijnanaItem.isDrawnBlade(held)`；`Sanity.setAbyssCall/setFixedPanic(player, holding)`；持械时理智固定为 `-44`；`drawn` 时每 `interval` tick 扣 `maxHealth / divisor` 血量（纯服务端 `setHealth`，不走 `hurt`，无无敌帧与抖动），血量下限 0，`divisor <= 0` 即关闭。
  - 新增可调配置（遵循“数值必须进 Config”铁律）：`MoonProjectConfig.ALAYA_BLOOD_DRAIN_DIVISOR`（key `bloodDrainDivisor`，默认 `30.0`，范围 0–1000，0 = 关闭）与 `ALAYA_BLOOD_DRAIN_INTERVAL_TICKS`（key `bloodDrainIntervalTicks`，默认 `20`，范围 1–1200），并提供 `bloodDrainDivisor()` / `bloodDrainIntervalTicks()` 读取器。
  - 验证：`gradlew compileJava` → `BUILD SUCCESSFUL`；`python docs/verify_assets.py` 全绿（Config 方法 55 / Config 键 58）。

---

## [v0.1.3] - 2026-10-31

### [新增 / Feat]
- **核心工作方块与设施系统落地**：
  - 新增 **收容单元 (`ContainmentUnitBlock`)**：用于放置异想体核心，内部维护安定度（Qliphoth Counter），安定度归零联动警报与收容失效出逃。
  - 新增 **脑啡肽提取机 (`EnkephalinExtractorBlock`)**：自动判定相邻收容单元，消耗燃料提取脑啡肽，并按异想体等级结算产出与安定度损耗。
  - 新增 **人格提取装置 (`IdentityExtractorBlock`)**：支持书页 + 脑啡肽作为原料，进行人格卡提取。
  - 新增 **装订台 (`BookbindingTableBlock`)**：用于书页残片与书本装订专属工作台。
  - 新增 **经验焚烧炉 (`ExpBurnerBlock`)**：支持消耗经验/书籍转换为余烬与特化产物。
  - 新增 **梅菲斯特号发动机 (`MephistophelesEngineBlock`)** 与 **梅菲斯特座椅 (`MephistophelesSeatBlock`)**：载具核心动力单元与可骑乘交互座椅实体。
  - 新增 **K公司注射装置 (`KCorpInjectorBlock`)**、**金枝祭坛 (`GoldenBoughShrineBlock`)**、**出逃警报器 (`MeltdownSirenBlock`)**、**强化装甲板 (`ReinforcedPlatingBlock`)**。
  - 配套完成所有方块的 `BlockEntity`、`Menu`（网络容器）与客户端 `Screen`（GUI）实现。
- **世界生成结构支持**：
  - 新增边狱分支结构生成框架（`LimbusBranchStructure` / `LimbusBranchPiece`）。

### [底层实现逻辑简述]
> - **方块驱动**：提取机每 20 tick 检索水平相邻 4 格收容单元，使用服务端状态机控制生产周期与安定度扣除。
> - **网络同步**：所有机器通过 `ContainerData` 数组向客户端 Menu 同步进度与燃料数值，保证客户端 GUI 平滑渲染。

---

## [v0.1.2] - 2026-10-31

### [新增 / Feat]
- **自研 4 槽饰品系统 (Trinket System)**：
  - 基于 Forge `ICapabilitySerializable` 实现玩家实体饰品栏扩展（核心槽、胸饰、腕饰、眼罩 4 个独立槽位）。
  - 新增饰品专属 GUI 界面与快捷键呼出支持，数据完全 NBT 持久化存储。
  - 接入 Curios 5.7.2 兼容层，支持无缝联动 Curios 饰品体系。
- **异想体饰品被动触发体系**：
  - 核心饰品装备时，在 `LivingHurtEvent` / `LivingDamageEvent` 事件监听中自动提取词条与伤害增幅。

### [底层实现逻辑简述]
> - **数据存储**：饰品数据通过 `Player` 的 Capability 附加 `ItemStackHandler`，死亡保持或掉落按游戏规则结算。
> - **前后端同步**：按键发包呼出 Menu，穿脱装备时通过 S2C 数据包实时向客户端广播外观与属性加成。

---

## [v0.1.1] - 2026-10-19 ~ 2026-10-22

### [新增 / Feat]
- **TETH 级异想体「焦化少女 (Scorched Girl)」实装**：
  - 独立实体生物 AI：具备随机锁定仇恨、靠近目标蓄力点燃火柴自爆机制。
  - 掉落物体系：掉落焦化火柴与专属异想体核心。
- **场谐振能源采集单元**：
  - 完工界面操作、采集按钮交互、能量进度与不同品质产出概率计算。

### [修复 / Fixed]
- 修正 `mineable/pickaxe` 标签路径配置（原路径拼写错误导致铁镐无法正常挖掘方块）。
- 恢复 OBJ 加载器配置与刀鞘模型定义，修复出鞘烧血代价机制。

---

## [v0.1.0] - 2026-10-17 ~ 2026-10-18

### [新增 / Feat]
- **神备觉醒武器「阿赖耶识 (Alaya-Vijnana)」首版灰模**：
  - 实现鞘击（面板 14）与拔刀（面板 35 @ 5次/秒）双形态平滑切换与动态属性分配。
  - 战斗机制：快慢刀延迟结算系统（`SlashScheduler`）、自动追击突进、横扫范围伤害。
  - 专属状态效果体系：流血、烧伤、呼吸法、破裂、震颤、斩缘、焦黑斩痕。
  - 状态可见性：`StatusHudOverlay` 准星旁敌对生物状态层数 HUD 实时绘制。
