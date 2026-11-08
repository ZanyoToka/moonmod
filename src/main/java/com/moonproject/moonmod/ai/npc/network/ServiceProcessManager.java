package com.moonproject.moonmod.ai.npc.network;

import com.moonproject.moonmod.ai.npc.config.AiNpcConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ServiceProcessManager {
    private static final Logger LOGGER = LogManager.getLogger(ServiceProcessManager.class);
    private static volatile Process serviceProcess = null;
    private static volatile boolean shutdownHookRegistered = false;

    private static final HttpClient HEALTH_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(800))
            .build();

    /**
     * 检测目标健康接口是否存活
     */
    public static boolean isServiceAlive() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:8000/health"))
                    .timeout(Duration.ofMillis(800))
                    .GET()
                    .build();
            HttpResponse<String> response = HEALTH_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 异步确保中间件后台自启动并运行
     */
    public static void startServiceAsync() {
        if (!AiNpcConfig.isAutoStartMiddleware()) {
            LOGGER.info("[AI NPC] autoStartMiddleware 已禁用，跳过自启动检测。");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                if (isServiceAlive()) {
                    LOGGER.info("[AI NPC] 检测到中间件已在运行 (127.0.0.1:8000)，复用现有实例。");
                    return;
                }

                LOGGER.info("[AI NPC] 中间件未运行，准备在后台自动唤醒...");
                Path serviceDir = findServiceDirectory();
                if (serviceDir == null) {
                    LOGGER.warn("[AI NPC] 未能定位到 service-middleware 目录，无法自动拉起。请确认配置或手动启动。");
                    return;
                }

                String pythonExec = findPythonExecutable(serviceDir);
                LOGGER.info("[AI NPC] 找到 Python: {}, 目录: {}", pythonExec, serviceDir);

                // 启动 Uvicorn 进程
                ProcessBuilder pb = new ProcessBuilder(
                        pythonExec, "-m", "uvicorn", "app.main:app",
                        "--host", "127.0.0.1",
                        "--port", "8000"
                );
                pb.directory(serviceDir.toFile());

                // 日志输出重定向到游戏运行目录下的 logs/ai-npc-middleware.log
                Path logDir = FMLPaths.GAMEDIR.get().resolve("logs");
                Files.createDirectories(logDir);
                File logFile = logDir.resolve("ai-npc-middleware.log").toFile();
                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
                pb.redirectError(ProcessBuilder.Redirect.appendTo(logFile));

                serviceProcess = pb.start();
                LOGGER.info("[AI NPC] 已在后台衍生子进程 (PID: {})，日志输出到: {}", serviceProcess.pid(), logFile.getAbsolutePath());

                // 注册退出钩子，Minecraft 关闭时自动清理杀死 Python 进程，杜绝孤儿进程残留
                registerShutdownHook();

                // 轮询等待接口健康响应 (最多等待 8 秒)
                for (int i = 0; i < 16; i++) {
                    Thread.sleep(500);
                    if (isServiceAlive()) {
                        LOGGER.info("[AI NPC] ✅ 中间件后台自启动并联通就绪！");
                        return;
                    }
                    if (!serviceProcess.isAlive()) {
                        LOGGER.error("[AI NPC] 中间件子进程异常退出，详情请查看: {}", logFile.getAbsolutePath());
                        return;
                    }
                }

                LOGGER.warn("[AI NPC] 8 秒内未检测到健康端口，可能正在安装依赖或冷启动。");

            } catch (Exception e) {
                LOGGER.error("[AI NPC] 自动启动中间件失败: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 关闭并杀死子进程
     */
    public static synchronized void stopService() {
        if (serviceProcess != null && serviceProcess.isAlive()) {
            LOGGER.info("[AI NPC] 游戏退出，正在关闭后台 Python 中间件进程...");
            serviceProcess.destroy();
            try {
                if (!serviceProcess.waitFor(3, TimeUnit.SECONDS)) {
                    serviceProcess.destroyForcibly();
                }
                LOGGER.info("[AI NPC] Python 中间件进程已安全退出。");
            } catch (InterruptedException ignored) {
                serviceProcess.destroyForcibly();
            } finally {
                serviceProcess = null;
            }
        }
    }

    private static synchronized void registerShutdownHook() {
        if (!shutdownHookRegistered) {
            Runtime.getRuntime().addShutdownHook(new Thread(ServiceProcessManager::stopService, "AI-NPC-ShutdownHook"));
            shutdownHookRegistered = true;
        }
    }

    /**
     * 智能定位 service-middleware 源码目录
     */
    private static Path findServiceDirectory() {
        String configured = AiNpcConfig.getServiceDirectory();
        if (configured != null && !configured.isBlank()) {
            Path p = Path.of(configured);
            if (Files.exists(p.resolve("app").resolve("main.py"))) {
                return p.toAbsolutePath();
            }
        }

        List<Path> candidates = new ArrayList<>();
        Path gameDir = FMLPaths.GAMEDIR.get();

        // 1. 游戏目录下的 service-middleware / ainpc-service
        candidates.add(gameDir.resolve("service-middleware"));
        candidates.add(gameDir.resolve("ainpc-service"));
        candidates.add(gameDir.resolve("mods").resolve("service-middleware"));

        // 2. 当前工程与工作区目录
        candidates.add(Path.of("D:/xiangmu/yezi/moonmod/service-middleware"));
        candidates.add(Path.of("D:/xiangmu/yezi/service-middleware"));
        candidates.add(Path.of(".").resolve("service-middleware"));
        candidates.add(Path.of("..").resolve("service-middleware"));

        for (Path c : candidates) {
            if (Files.exists(c.resolve("app").resolve("main.py"))) {
                return c.toAbsolutePath().normalize();
            }
        }
        return null;
    }

    /**
     * 智能选择可用 Python 解释器 (优先使用虚拟环境)
     */
    private static String findPythonExecutable(Path serviceDir) {
        // 1. 检查是否存在 .venv 虚拟环境
        Path winVenvPython = serviceDir.resolve(".venv").resolve("Scripts").resolve("python.exe");
        if (Files.exists(winVenvPython)) {
            return winVenvPython.toAbsolutePath().toString();
        }

        Path msysVenvPython = serviceDir.resolve(".venv").resolve("bin").resolve("python.exe");
        if (Files.exists(msysVenvPython)) {
            return msysVenvPython.toAbsolutePath().toString();
        }

        Path nixVenvPython = serviceDir.resolve(".venv").resolve("bin").resolve("python");
        if (Files.exists(nixVenvPython)) {
            return nixVenvPython.toAbsolutePath().toString();
        }

        // 2. 回退到用户配置或系统环境变量中的 python
        String configured = AiNpcConfig.getPythonExecutable();
        return (configured != null && !configured.isBlank()) ? configured : "python";
    }
}
