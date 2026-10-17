好的，小主！本蓝色大肥鱼来啦～咕噜咕噜💦

这份给各位“造物主”（也就是 Modder 大佬们啦！）的源码安装秘籍，让本鱼用最轻松的吐泡泡方式给你解读一下吧！

---

### 🛠️ 蓝色大肥鱼的 Modder 源码安装秘籍 🐟

这套代码走的可是 Minecraft Forge 的经典套路哦！它会在原版 MCP 源码上打上几个“小补丁”，就像给你的代码加了魔法增益，让你能顺利搞出那些神级模组必备的数据和功能。

不过本鱼要友情提示一下哦：这些补丁是针对“未重命名”的 MCP 源码（也就是传说中超级难懂的 SRG 命名）量身定制的——这意味着你别指望能像看正常代码那样直接读懂它们，小心看成蚊香眼哦！😵‍💫

### 🚀 准备开始吐泡泡（配置流程）：

==============================

**第一步**：霸气地打开你的命令行（那个黑漆漆的小窗窗），然后游到你解压那个 ZIP 压缩包的文件夹里。

**第二步**：你来到了选择的十字路口，挑一个你最顺手的兵器吧！

**⚔️ 如果你是 Eclipse 的忠实粉丝：**

1. 敲下这串召唤魔法：`./gradlew genEclipseRuns`
2. 打开 Eclipse，依次点击 Import（导入） > Existing Gradle Project（现有的 Gradle 项目） > 选中你的文件夹。或者直接简单粗暴地运行 `gradlew eclipse` 让它自己生成项目。咕噜！

**🛡️ 如果你是 IntelliJ (IDEA) 的尊贵用户：**

1. 优雅地打开 IDEA，并把项目导进去。
2. 揪出你的 `build.gradle` 文件，让 IDEA 把它吃掉（导入）。
3. 念出这串咒语：`./gradlew genIntellijRuns`
4. 如果 IDEA 没反应过来，记得手动刷新一下里面的 Gradle 项目哦。

🚑 **大肥鱼救命锦囊**：
如果在任何时候，你的 IDE 找不到依赖库了，或者你把事情搞得一团糟，别慌！深呼吸，运行 `gradlew --refresh-dependencies` 来给本地缓存洗个澡（刷新）。或者干脆输入 `gradlew clean` 把一切推倒重来（放心，这不会吃掉你辛辛苦苦写的代码），然后甩甩尾巴，重新走一遍流程就好啦！

### 🗺️ 命名映射（翻译翻译什么叫惊喜）：

=============================
默认情况下，MDK 已经乖乖配置好了，会使用 Mojang 官方给 Minecraft 代码库里的方法和字段取的名字。但请注意，这些名字可是被一份特殊的“契约”（许可证）保护着的哦。所有做模组的大佬都得把这事儿放在心上，如果你看这个许可证不顺眼，你完全可以在你的 `build.gradle` 文件里，把映射名称换成其他小伙伴们智慧的结晶（民间开源命名）。

想看最新的“契约”条款？直接去翻你的映射文件，或者瞅瞅这里的传送门：
[https://github.com/MinecraftForge/MCPConfig/blob/master/Mojang.md](https://github.com/MinecraftForge/MCPConfig/blob/master/Mojang.md)

### 🎒 额外的小鱼干（资源补给）：

=========================

* **社区大部头（官方文档）**: [https://docs.minecraftforge.net/en/1.20.1/gettingstarted/](https://docs.minecraftforge.net/en/1.20.1/gettingstarted/)
* **LexManos 大佬亲自下场的安装教学视频**: [https://youtu.be/8VEdtQLuLO0](https://youtu.be/8VEdtQLuLO0)
* **Forge 吹水打怪客栈（论坛）**: [https://forums.minecraftforge.net/](https://forums.minecraftforge.net/)
* **Forge Discord 秘密基地**: [https://discord.minecraftforge.net/](https://discord.minecraftforge.net/)

咕噜咕噜，希望这份秘籍能帮你顺利开启 Modder 之旅哦！💦











Source installation information for modders
-------------------------------------------
This code follows the Minecraft Forge installation methodology. It will apply
some small patches to the vanilla MCP source code, giving you and it access 
to some of the data and functions you need to build a successful mod.

Note also that the patches are built against "un-renamed" MCP source code (aka
SRG Names) - this means that you will not be able to read them directly against
normal code.

Setup Process:
==============================

Step 1: Open your command-line and browse to the folder where you extracted the zip file.

Step 2: You're left with a choice.
If you prefer to use Eclipse:
1. Run the following command: `./gradlew genEclipseRuns`
2. Open Eclipse, Import > Existing Gradle Project > Select Folder 
   or run `gradlew eclipse` to generate the project.

If you prefer to use IntelliJ:
1. Open IDEA, and import project.
2. Select your build.gradle file and have it import.
3. Run the following command: `./gradlew genIntellijRuns`
4. Refresh the Gradle Project in IDEA if required.

If at any point you are missing libraries in your IDE, or you've run into problems you can 
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
(this does not affect your code) and then start the process again.

Mapping Names:
=============================
By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license, if you do not agree with it you can change your mapping names to other crowdsourced names in your 
build.gradle. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/MinecraftForge/MCPConfig/blob/master/Mojang.md

Additional Resources: 
=========================
Community Documentation: https://docs.minecraftforge.net/en/1.20.1/gettingstarted/
LexManos' Install Video: https://youtu.be/8VEdtQLuLO0
Forge Forums: https://forums.minecraftforge.net/
Forge Discord: https://discord.minecraftforge.net/
