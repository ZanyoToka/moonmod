package com.moonproject.moonmod.ai.npc.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AiNpcConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> SERVICE_URL;
    public static final ForgeConfigSpec.IntValue TIMEOUT_SECONDS;
    public static final ForgeConfigSpec.IntValue COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_VILLAGERS;
    public static final ForgeConfigSpec.ConfigValue<String> NPC_TAG;

    // 自动托管中间件生命周期配置
    public static final ForgeConfigSpec.BooleanValue AUTO_START_MIDDLEWARE;
    public static final ForgeConfigSpec.ConfigValue<String> PYTHON_EXECUTABLE;
    public static final ForgeConfigSpec.ConfigValue<String> SERVICE_DIRECTORY;

    static {
        BUILDER.comment("AI NPC 智能对话与具身动作系统配置").push("ai_npc");

        SERVICE_URL = BUILDER.comment("AI 中间件服务 POST 接口地址")
                .define("serviceUrl", "http://127.0.0.1:8000/api/npc/chat");

        TIMEOUT_SECONDS = BUILDER.comment("异步 HTTP 请求超时时间（秒）")
                .defineInRange("timeoutSeconds", 10, 1, 60);

        COOLDOWN_SECONDS = BUILDER.comment("玩家与 NPC 交互防刷冷却时间（秒）")
                .defineInRange("cooldownSeconds", 3, 0, 60);

        ALLOW_VILLAGERS = BUILDER.comment("是否默认允许对所有原版村民触发 AI 对话")
                .define("allowVillagers", true);

        NPC_TAG = BUILDER.comment("具备 AI 对话能力的生物 Tag 标识")
                .define("npcTag", "ai_npc");

        BUILDER.comment("中间件自动化托管设置 (无需手动开黑窗口运行)").push("auto_process");

        AUTO_START_MIDDLEWARE = BUILDER.comment("是否在游戏启动时由模组在后台自动启动 Python 中间件进程")
                .define("autoStartMiddleware", true);

        PYTHON_EXECUTABLE = BUILDER.comment("Python 解释器路径或命令 (默认为系统 python)")
                .define("pythonExecutable", "python");

        SERVICE_DIRECTORY = BUILDER.comment("Python 中间件代码所在目录 (留空将自动在游戏目录或工程目录中智能探测)")
                .define("serviceDirectory", "");

        BUILDER.pop(); // pop auto_process
        BUILDER.pop(); // pop ai_npc
        SPEC = BUILDER.build();
    }

    private AiNpcConfig() {}

    public static String getServiceUrl() {
        return SPEC.isLoaded() ? SERVICE_URL.get() : "http://127.0.0.1:8000/api/npc/chat";
    }

    public static int getTimeoutSeconds() {
        return SPEC.isLoaded() ? TIMEOUT_SECONDS.get() : 10;
    }

    public static int getCooldownSeconds() {
        return SPEC.isLoaded() ? COOLDOWN_SECONDS.get() : 3;
    }

    public static boolean isAllowVillagers() {
        return SPEC.isLoaded() ? ALLOW_VILLAGERS.get() : true;
    }

    public static String getNpcTag() {
        return SPEC.isLoaded() ? NPC_TAG.get() : "ai_npc";
    }

    public static boolean isAutoStartMiddleware() {
        return SPEC.isLoaded() ? AUTO_START_MIDDLEWARE.get() : true;
    }

    public static String getPythonExecutable() {
        return SPEC.isLoaded() ? PYTHON_EXECUTABLE.get() : "python";
    }

    public static String getServiceDirectory() {
        return SPEC.isLoaded() ? SERVICE_DIRECTORY.get() : "";
    }
}
