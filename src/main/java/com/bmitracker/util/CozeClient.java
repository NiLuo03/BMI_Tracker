package com.bmitracker.util;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class CozeClient {

    private final String apiKey;
    private final String apiUrl;
    private final String botId;
    private final HttpClient client;

    public CozeClient(String apiKey, String botId) {
        this(apiKey, botId, "https://api.coze.cn/open_api/v2/chat");
    }

    public CozeClient(String apiKey, String botId, String apiUrl) {
        this.apiKey = apiKey;
        this.botId = botId;
        this.apiUrl = apiUrl;
        this.client = HttpClient.newHttpClient();
    }

    public String sendMessage(String userMessage) throws Exception {
        String json = buildJson(userMessage);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Coze API 请求失败，状态码: " + response.statusCode()
                    + ", 响应: " + response.body());
        }

        return extractMessage(response.body());
    }

    private String buildJson(String userMessage) {
        return String.format("""
                {
                    "bot_id": "%s",
                    "user": "user",
                    "query": "%s",
                    "stream": false
                }""", botId, escapeJson(userMessage));
    }

    private String extractMessage(String responseBody) {
        return responseBody;
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
