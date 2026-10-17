# 阿赖耶识 · 招式动画目录（对齐 Epic Fight / epicmoonmod 的写法）

参照 `epicmoonmod-1.2.1.jar` 的结构：

```
assets/epicmoonmod/animmodels/animations/biped/{武器类型}/*.json
```

本模组对应：`assets/moonproject/animmodels/animations/biped/alaya/*.json`

## 需要放进去的文件（照抄参考 mod 的命名）

| 文件名 | 用途 |
|---|---|
| `alaya_auto1.json` / `alaya_auto2.json` / `alaya_auto3.json` | 左键普攻连段第 1/2/3 段 |
| `alaya_skill.json` / `alaya_skill2.json` … `alaya_skill13.json` | 各技能（我们目前用 6 个：挥砍/涂抹/泼洒/描绘/天杀/抹去） |
| `alaya_dash.json` | 突进（自动追击） |
| `alaya_guard.json` / `alaya_parry.json` | 格挡 / 弹反 |
| `alaya_idle.json` / `alaya_run.json` / `alaya_walk.json` / `alaya_air.json` | 持刀姿态 |
| `alaya_airslash.json` | 空中斩 |

## 怎么产出

1. 用 **Blockbench + Epic Fight 动画插件**（或用 EF 自带的 Weapon Type Editor 导出模板）
2. 导出为 Epic Fight 的 `.json` 动画，文件名按上表
3. 直接放进本目录即可，重启游戏生效

## 还需要配的两件事

1. **自定义武器类型**：用 EF 的 **Weapon Type Editor**（游戏内 `/efweapontypeeditor` 或配置界面）
   新建类型 `moonproject:alaya`，把上面的动画逐个挂上去，导出后放进
   `src/main/resources/data/moonproject/capabilities/weapon_types/`
2. 然后把 `capabilities/weapons/alaya_vijnana.json` 的 `"type"` 从 `epicfight:tachi`
   改成 `moonproject:alaya`

在武器类型就绪之前，武器会以 EF 的太刀（tachi）默认动作运行，不影响游玩。