package com.bmitracker.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class CozeClient {

    private final String apiKey;
    private final String apiUrl;
    private final String botId;
    private final HttpClient client;
    private final int maxRetries;

    public CozeClient(String apiKey, String botId) {
        this(apiKey, botId, "https://api.coze.cn/open_api/v2/chat", 3);
    }

    public CozeClient(String apiKey, String botId, String apiUrl, int maxRetries) {
        this.apiKey = apiKey;
        this.botId = botId;
        this.apiUrl = apiUrl;
        this.maxRetries = Math.max(1, maxRetries);
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String sendMessage(String userMessage) {
        String json = buildJson(userMessage);
        RuntimeException lastEx = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .timeout(Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return extractMessage(response.body());
                }

                if (response.statusCode() >= 500 && attempt < maxRetries) {
                    Thread.sleep(1000L * attempt);
                    continue;
                }

                throw new CozeApiException("Coze API 请求失败，状态码: " + response.statusCode()
                        + ", 响应: " + truncate(response.body(), 200));

            } catch (CozeApiException e) {
                throw e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CozeApiException("请求被中断", e);
            } catch (Exception e) {
                lastEx = new CozeApiException("Coze API 请求异常 (尝试 " + attempt + "/" + maxRetries + ")", e);
                if (attempt < maxRetries) {
                    try { Thread.sleep(1000L * attempt); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw lastEx != null ? lastEx : new CozeApiException("Coze API 请求失败，已达最大重试次数");
    }

    private String buildJson(String userMessage) {
        return "{\"bot_id\":\"" + escapeJson(botId)
                + "\",\"user\":\"user\",\"query\":\"" + escapeJson(userMessage)
                + "\",\"stream\":false}";
    }

    private String extractMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        try {
            String msgKey = "\"content\":\"";
            int start = responseBody.indexOf(msgKey);
            if (start < 0) {
                msgKey = "\"msg\":\"";
                start = responseBody.indexOf(msgKey);
            }
            if (start < 0) {
                return responseBody;
            }
            start += msgKey.length();
            StringBuilder result = new StringBuilder();
            for (int i = start; i < responseBody.length(); i++) {
                char c = responseBody.charAt(i);
                if (c == '\\' && i + 1 < responseBody.length()) {
                    char next = responseBody.charAt(i + 1);
                    if (next == '"') { result.append('"'); i++; }
                    else if (next == 'n') { result.append('\n'); i++; }
                    else if (next == 'r') { result.append('\r'); i++; }
                    else if (next == 't') { result.append('\t'); i++; }
                    else if (next == '\\') { result.append('\\'); i++; }
                    else { result.append(c); }
                } else if (c == '"') {
                    break;
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        } catch (Exception e) {
            return responseBody;
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private String truncate(String s, int maxLen) {
        return s != null && s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }

    public static class CozeApiException extends RuntimeException {
        public CozeApiException(String message) { super(message); }
        public CozeApiException(String message, Throwable cause) { super(message, cause); }
    }
}
