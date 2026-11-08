# mod_creater_233 —— 模块化提示词生成台（网页版）

从 `mod_creater_233.apk` 的 `assets/` 抽取出来的静态网页版，纯前端、零依赖、不联网。

## 它做什么

把 Minecraft 模组开发里 8 类资产（方块 / 物品 / 工具 / 护甲 / 实体 / 药水效果 / 方块实体 / 世界生成）的字段
填成表单，拼成一段结构化提示词，再导出成 `.md`，喂给任意 AI 让它按格式写代码。

它**不调用任何 API**，页面里没有硬编码的 key——所谓「生成」就是本地模板拼字符串。

## 怎么打开

方式一（最省事）：直接双击 `index.html`。功能基本都在，只有 PWA 离线缓存（Service Worker）在 `file://` 下注册失败，不影响使用。

方式二（推荐，Service Worker 也能生效）：

```powershell
# 在本目录下起一个静态服务器
& 'C:\Users\23586\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe' -m http.server 8080
# 然后浏览器打开 http://127.0.0.1:8080/index.html
```

## 文件

```
index.html                 入口
manifest.json              PWA 清单（应用名 / 主题色 / 图标）
sw.js                      Service Worker，离线缓存
css/gemini.css             视觉体系
css/prompt-generator.css   提示词工作台样式
js/icons.js                Material 矢量图标
js/types/registry.js       类型注册中心（TypeRegistry）
js/types/type-*.js         8 个资产类型的字段定义与提示词拼装
js/prompt-generator.js     核心调度与数据模型
js/prompt-ui.js            界面联动
```

## 来源

- 原始 APK：`mod_creater_233.apk(1).1`（113,822 字节，包名 `com.gemini.modcreator`，`classes.dex` 仅 1 KB 的 WebView 壳）
- 抽取方式：解 zip 后把 `assets/` 原样复制过来，未改任何字节
- APK 本身已是编译产物，源码（Android 工程 + 前端源文件）不在这台机器上

## 备注

`css/gemini-logo.png` 在 `manifest.json` 里被引用，但 APK 的 assets 里没有这个文件，所以安装成 PWA 时图标会缺失。
补一张 192×192 的 PNG 放到 `css/` 下即可。
