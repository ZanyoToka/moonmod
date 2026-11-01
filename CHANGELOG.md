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

### [修复 / Fixed]
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
