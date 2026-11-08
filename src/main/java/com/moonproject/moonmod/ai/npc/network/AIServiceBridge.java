package com.moonproject.moonmod.ai.npc.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.moonproject.moonmod.ai.npc.data.ChatRequest;
import com.moonproject.moonmod.ai.npc.data.ChatResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class AIServiceBridge {
    private static final Logger LOGGER = LogManager.getLogger(AIServiceBridge.class);
    private static final Gson GSON = new GsonBuilder().create();

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * 异步向 Python AI 中间件发起 HTTP POST 对话请求
     * 严格在工作线程执行，杜绝阻塞 Minecraft 主 Tick 循环
     *
     * @param request 对话与现场环境数据对象
     * @param serviceUrl 目标中间件 URL
     * @param timeoutSeconds 请求超时时间
     * @return CompletableFuture 包装的 ChatResponse
     */
    public static CompletableFuture<ChatResponse> sendChatRequestAsync(ChatRequest request, String serviceUrl, int timeoutSeconds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String requestJson = GSON.toJson(request);
                LOGGER.debug("发送 AI 请求: {}", requestJson);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(serviceUrl))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (response.statusCode() == 200) {
                    String body = response.body();
                    LOGGER.debug("收到 AI 中间件回包: {}", body);
                    ChatResponse chatResponse = GSON.fromJson(body, ChatResponse.class);
                    if (chatResponse != null) {
                        return chatResponse;
                    } else {
                        throw new IllegalStateException("中间件返回了空的响应体");
                    }
                } else {
                    throw new IllegalStateException("中间件 HTTP 状态码异常: " + response.statusCode() + " | " + response.body());
                }
            } catch (Exception e) {
                LOGGER.error("AI 中间件通信异常: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
