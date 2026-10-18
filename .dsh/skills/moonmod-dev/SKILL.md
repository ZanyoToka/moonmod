---
name: moonmod-dev
description: Project Moon 同人模组 moonmod（Minecraft 1.20.1 + Forge 47）的开发约定。涉及阿赖耶识武器、硬币/理智/沉沦/震颤/呼吸法系统、OBJ 物品模型、Epic Fight 集成时加载本技能。
---

# moonmod 开发约定

## 构建
- JDK 固定在 `C:\jdk-17`；Gradle 8.8、Forge 47.4.10、`official` mappings。
- 代理无法执行 Gradle（沙箱限制），编译由用户在本机执行 `build → classes`，报错再贴回。
- 每次改完资源先跑 `python docs/verify_assets.py`（物品/效果/语言键自检）。
- `run/config/moonproject-common.toml` 会缓存旧默认值，改 Config 后要删掉该文件才生效。

## 资源规则（踩过的坑）
- `forge:obj` 只支持方块模型，物品必须用自定义 `IGeometryLoader`（`client/obj/ObjItemModel`，注册名 `moonproject:obj_item`）。
- 注册几何加载器：`ModelEvent.RegisterGeometryLoaders.register("obj_item", loader)` —— 参数是 String 且**不含 modid**。
- 贴图必须是 **2 的幂**尺寸且**不透明**（透明像素会让部件"消失"）。
- MC 模型 `elements` 不支持 `"name"` 字段，坐标必须在 `-16..32` 内。
- `display` 是**先平移后旋转**，`translation` 用的是模型自身轴：
  - `rotation [90,-90,45]` 下：`translation[0]`=左右、`[1]`=前后、`[2]`=上下；`rotation[2]`=倾斜角。
- 最终持握参数（用户确认）：三称 `rotation[90,-90,45]` / `translation[-6,0,12]` / `scale 0.75`。

## 系统设计
- 状态效果：`StackEffect`（层数 = amplifier+1，命中时读取），`PeriodicDamageEffect`（DoT），`SinkingEffect`（强度+层数双值）。
- 呼吸法：每层 +0.5% 暴击率 / +5% 暴伤，30 层封顶（`PoiseCritEvents`）；**不是所有武器命中都获得**，将来绑定人格卡。
- 震颤：满 10 层原地爆炸（`ExplosionInteraction.NONE`），伤害 = 武器白值（`TremorEvents`）。
- 理智：范围 -45~45；攻击 +1 / 受击 -5 / 死亡归零；≤-10 虚弱、≤-25 恐慌、阿鼻叫唤锁 -44。
- 硬币：`0.2 + 1.8 × (正面/总数)`；正面率 `0.5 + 理智/200`；阿鼻叫唤时正负反转。

## 代码习惯
- 中文注释；一个机制一个类，放在 `event/` 或 `effect/`。
- 修改 Java 前先用 `javap` 对 `~/.gradle/caches/forge_gradle/minecraft_user_repo/.../forge-...-recomp.jar` 核对 API 签名。
- 提交信息用中文，分条列出改动。

---

## 工程结构（来自 docs/工程结构.md）

# 「月亮计划」模组 · 工程结构说明

> 本文档描述本工程的包结构与开发规范，与工作区 `.dsh/skills/moonmod/SKILL.md`（设计总纲）配套。
> 原则：**保留全部现有可用代码**，在此之上对齐规划结构、补齐缺失骨架。

## 1. 基本信息

| 项 | 值 |
|---|---|
| Minecraft / Forge | 1.20.1 / 47.4.x |
| modid | `moonproject`（与 mods.toml、主类常量一致） |
| 包根 | `com.moonproject.moonmod` |
| 主类 | `MoonProjectMod.java` |
| 构建 | `gradlew.bat`，**JAVA_HOME 必须指向 `C:\jdk-17`**（详见 §5） |

## 2. Java 包结构

```
com.moonproject.moonmod/
├─ MoonProjectMod.java      # 主类：注册入口（物品→方块→BE→菜单→创造页→效果→实体→音效）+ Config
├─ init/                    # 注册器（只负责注册，不含业务逻辑）
│  ├─ ModItems              # 物品：abnormality_fragment / ego_core / regret / ego 装甲×4 / enkephalin / alaya_vijnana
│  ├─ ModBlocks             # 方块：resonance_collector / exp_burner（含 BlockItem）
│  ├─ ModBlockEntities      # 方块实体注册
│  ├─ ModMenus              # 容器菜单注册（GUI 服务端半边）
│  ├─ ModEntities           # 【新】实体注册骨架（异想体/Boss/NPC/弹幕，暂空）
│  └─ ModSounds             # 【新】音效注册骨架（暂空）
├─ item/                    # 物品类：AlayaVijnanaItem / EgoArmorMaterial / ModCreativeTab
├─ block/                   # 方块类：ExpBurnerBlock / ResonanceCollectorBlock
├─ blockentity/             # 方块实体（机器数据与逻辑）
├─ gui/                     # 服务端菜单：ExpBurnerMenu / ResonanceCollectorMenu
├─ client/                  # 仅客户端：HUD（SP/硬币/状态）、按键、Screen、OBJ 模型加载
│  ├─ gui/                  #   ExpBurnerScreen / ResonanceCollectorScreen
│  └─ obj/                  #   ObjItemModel（阿赖耶识的 OBJ 渲染）
├─ entity/                  # 【新】生物包：AbnormalityEntity（异想体基类）+ boss/mob/npc/projectile 子包（规划中）
├─ effect/                  # 状态效果：流血/烧伤/沉沦/震颤/破裂/充能/斩痕/黑痕等 + ModEffects
├─ event/                   # 事件监听：Sanity/Combat/Panic/MobHealth/AbyssCall 等
├─ sanity/                  # SP 精神值系统：Sanity / PanicType
├─ network/                 # ModNetwork（发包）
├─ config/                  # MoonProjectConfig（全部数值集中这里）
└─ datagen/                 # 【新】数据生成：DataGenerators + providers（配方/物品模型/语言）
   └─ providers/            #   ModRecipeProvider / ModItemModelProvider / ModLanguageProvider
```

## 3. 资源结构

```
src/main/resources/
├─ META-INF/mods.toml                 # 模组声明
├─ pack.mcmeta
├─ assets/moonproject/
│  ├─ animmodels/animations/biped/…   # GeckoLib 动画（阿赖耶识）
│  ├─ blockstates/ models/(block|item|obj)/ textures/(block|item|gui|mob_effect|models)/
│  └─ lang/ zh_cn.json · en_us.json
└─ data/moonproject/
   ├─ recipes/                        # 现有 8 条手写配方
   ├─ loot_tables/blocks/             # 掉落表
   └─ capabilities/weapons/           # 武器能力数据
```

## 4. 与设计总纲（skill §4）的对齐状态

| 规划包 | 状态 | 备注 |
|---|---|---|
| PmoonMod 主类 | ✅ 已有 | 对应 MoonProjectMod |
| init/ | ✅ 已补齐 | 新增 ModEntities、ModSounds |
| item/ block/ blockentity/ gui/ effect/ event/ config/ | ✅ 已有 | 内容超出规划（已含 SP、异常状态、GUI） |
| entity/ | ✅ 骨架 | AbnormalityEntity 基类已建，boss/mob/npc/projectile 待实现 |
| datagen/ | ✅ 骨架 | 入口 + 3 个 provider，runData 即用 |
| 音效 sounds | ⏳ 待做 | 注册器已备，需 ogg 文件 + sounds.json |
| GeckoLib / Patchouli / JEI | ⏳ 待接入 | 动画目录已就位；图鉴与配方查看后续加依赖 |
| modid=pmoon（skill 建议） | 🔒 沿用 moonproject | 现有工程已用 moonproject，改动成本高，暂不迁移 |

## 5. 构建与运行

```bat
set JAVA_HOME=C:\jdk-17          :: ⚠️ 必须用工作区外的 JDK 17（本机 PATH 上是 JDK 22）
gradlew build                    :: 编译打包
gradlew runClient                :: 启动游戏客户端
gradlew runData                  :: 运行数据生成（datagen）
```

IDEA 里：Project Structure → SDK 选 `C:\jdk-17`；Gradle JVM 同样选它。

## 6. 新增内容速查

| 想加什么 | 步骤 |
|---|---|
| 新物品 | ModItems 注册 → lang 加名 → models/item 与贴图（或 datagen basicItem） |
| 新方块 | ModBlocks 注册 → 方块类 → BlockItem → blockstates/模型/贴图 → 掉落表 |
| 新异想体 | entity/ 建类（继承 AbnormalityEntity）→ ModEntities 注册 → GeckoLib 动画 → 生成/掉落数据 |
| 新音效 | ModSounds 注册 → sounds/ogg + sounds.json → 事件里播放 |
| 新配方 | datagen ModRecipeProvider（推荐）或手写 data/.../recipes/*.json |
| 新状态效果 | effect/ 建类 → ModEffects 注册 → mob_effect 贴图 + lang |
| 新机制数值 | 一律进 config/MoonProjectConfig，不硬编码 |

## 7. 已知待办（下一步方向）

- [ ] 第一个异想体实体（建议从 TETH 级「焦化少女」起步：自爆 AI 最简单）
- [ ] 音效资源（CC0 素材 + Audacity）
- [ ] datagen 跑通并迁移手写配方
- [ ] GeckoLib 4.2 依赖接入 build.gradle
- [ ] 阿赖耶识等非月亮计划内容与主线内容分区（可选，不影响功能）

