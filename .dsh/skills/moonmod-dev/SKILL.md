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