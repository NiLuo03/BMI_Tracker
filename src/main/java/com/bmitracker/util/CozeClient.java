package com.bmitracker.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CozeClient {

    private static final String API_URL = "https://api.coze.cn/v1/chat/completions";
    private static final String API_KEY = "your_coze_api_key";
    private static final int TIMEOUT = 5000;

    // 调用 Coze API 生成膳食推荐
    public static String getDietRecommendation(int age, int sex, double height, double weight,
                                                double bmi, String status, String preferences) {
        try {
            String prompt = buildPrompt(age, sex, height, weight, bmi, status, preferences);

            URI uri = new URI(API_URL);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setDoOutput(true);

            String jsonBody = "{\"messages\":[{\"role\":\"user\",\"content\":\"" +
                    URLEncoder.encode(prompt, StandardCharsets.UTF_8) + "\"}]}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder resp = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) resp.append(line);
                    return resp.toString();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String buildPrompt(int age, int sex, double height, double weight,
                                       double bmi, String status, String preferences) {
        String gender = sex == 0 ? "男" : "女";
        return String.format(
                "你是一位资深注册营养师，请根据以下用户体质数据推荐一日三餐。" +
                "用户年龄：%d，性别：%s，身高：%.1fcm，体重：%.1fkg，" +
                "BMI：%.1f，健康状态：%s，用户偏好：%s。" +
                "请以JSON格式返回：{\"breakfast\":\"...\",\"lunch\":\"...\",\"dinner\":\"...\",\"totalCal\":\"...\"}",
                age, gender, height, weight, bmi, status,
                preferences == null || preferences.isEmpty() ? "无特殊偏好" : preferences);
    }
}
