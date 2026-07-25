package com.bmitracker.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class CozeClient {

    private static final String DEFAULT_ENDPOINT = "ep-20260713112535-75rjx";
    private static final int TIMEOUT_SECONDS = 30;
    private static final int MAX_RETRIES = 2;

    private static String loadApiKey() {
        return loadConfig(3);
    }
    private static String loadApiUrl() {
        String url = loadConfig(1);
        return url.isEmpty() ? "https://ark.cn-beijing.volces.com/api/v3/chat/completions" : url;
    }
    private static String loadModel() {
        String model = loadConfig(2);
        return model.isEmpty() ? DEFAULT_ENDPOINT : model;
    }

    private static String loadConfig(int field) {
        try {
            java.sql.Connection conn = com.bmitracker.util.DBUtil.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement(
                "SELECT api_url, api_model, api_key FROM users WHERE userId=?");
            ps.setInt(1, com.bmitracker.BMIApplication.currentUserId);
            java.sql.ResultSet rs = ps.executeQuery();
            String val = "";
            if (rs.next()) {
                val = switch (field) { case 1 -> rs.getString("api_url"); case 2 -> rs.getString("api_model"); default -> rs.getString("api_key"); };
            }
            rs.close(); ps.close(); conn.close();
            return val != null ? val : "";
        } catch (Exception ex) { return ""; }
    }

    public static String getDietRecommendation(int age, int sex, double height, double weight,
                                                double bmi, String status, String preferences,
                                                String allergens, String chronicDiseases) {
        String genderStr = sex == 0 ? "男" : "女";
        String message = String.format(
                "请为以下用户推荐一日三餐营养膳食：年龄%d岁，性别%s，身高%.1fcm，体重%.1fkg，BMI%.1f(%s)",
                age, genderStr, height, weight, bmi, status);
        if (preferences != null && !preferences.isEmpty()) {
            message += "，偏好：" + preferences;
        }
        if (allergens != null && !allergens.isEmpty()) {
            message += "。用户过敏原：" + allergens + "，请确保推荐的食物不含这些过敏原";
        }
        if (chronicDiseases != null && !chronicDiseases.isEmpty()) {
            message += "。用户慢性病史：" + chronicDiseases + "，请避免推荐对这些慢性病不利的食物";
        }
        message += "。请按照格式返回：{\"breakfast\":\"...\",\"lunch\":\"...\",\"dinner\":\"...\",\"totalCal\":\"...\"}";

        try {
            CozeClient client = new CozeClient(loadApiKey(), loadModel(), loadApiUrl(), MAX_RETRIES);
            return client.sendMessage(message);
        } catch (Exception e) {
            return null;
        }
    }

    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final HttpClient client;
    private final int maxRetries;

    public CozeClient(String apiKey, String model) {
        this(apiKey, model, loadApiUrl(), MAX_RETRIES);
    }

    public CozeClient(String apiKey, String model, String apiUrl, int maxRetries) {
        this.apiKey = apiKey;
        this.model = model;
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
                        .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
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

                throw new CozeApiException("API 请求失败，状态码: " + response.statusCode()
                        + ", 响应: " + truncate(response.body(), 200));

            } catch (CozeApiException e) {
                throw e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CozeApiException("请求被中断", e);
            } catch (Exception e) {
                lastEx = new CozeApiException("API 请求异常 (尝试 " + attempt + "/" + maxRetries + ")", e);
                if (attempt < maxRetries) {
                    try { Thread.sleep(1000L * attempt); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw lastEx != null ? lastEx : new CozeApiException("API 请求失败，已达最大重试次数");
    }

    private String buildJson(String userMessage) {
        return "{\"model\":\"" + escapeJson(model) + "\",\"messages\":["
                + "{\"role\":\"system\",\"content\":\"你是一位专业的营养师，根据用户的身体数据推荐健康膳食。\"},"
                + "{\"role\":\"user\",\"content\":\"" + escapeJson(userMessage) + "\"}"
                + "],\"stream\":false}";
    }

    private String extractMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        try {
            String contentKey = "\"message\":{\"content\":\"";
            int start = responseBody.indexOf(contentKey);
            if (start < 0) return responseBody;
            start += contentKey.length();
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
