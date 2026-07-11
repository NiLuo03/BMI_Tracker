---
name: bmi-meal-recommend
description: >
  BMI体质评估系统的AI膳食推荐模块。通过JavaFX调用Coze平台智能体API，
  根据用户体质数据生成个性化一日三餐推荐，并以卡片形式展示。
  触发词：膳食推荐、AI推荐、Coze API、饮食建议、营养师、智能推荐、膳食建议、
  三餐推荐、AI营养师、饮食方案、HTTP请求、API调用。
  当用户提到 AI 膳食推荐、Coze API 调用或营养餐推荐相关功能时，使用此 Skill。
---
# AI 膳食推荐模块 (Meal Recommendation)

## 开发目标
调用 Coze 平台发布的营养师智能体 API，根据用户年龄、性别、身高、体重、BMI、健康区间和饮食偏好，生成个性化一日三餐推荐，并以卡片形式美观展示。

## 前置依赖
- 用户管理模块已完成（Session 可用，preferences 字段可用）
- bmi_records 表已有数据（需获取最新 BMI 和健康区间）
- **Coze 平台智能体已创建并发布**（见下方 Coze 配置）
- Java 11+（内置 `java.net.http.HttpClient`）或使用第三方库如 OkHttp

---

## Coze 智能体配置

在 Coze 平台 (https://www.coze.com) 创建智能体：

### 人设（System Prompt）
```
你是一位资深注册营养师，拥有 10 年以上临床营养经验，
擅长根据用户体质数据推荐一日三餐。

规则：
1. 推荐需符合中国居民膳食指南
2. 总热量控制在合理范围（男性 1800-2500kcal，女性 1500-2000kcal）
3. 每餐需标注菜品名称和预估热量
4. 必须考虑用户的饮食偏好（不吃辣、不吃海鲜等）
5. 输出格式必须是 JSON，不要包含额外文字
```

### 输入变量
| 变量名 | 类型 | 说明 |
|--------|------|------|
| age | number | 年龄 |
| sex | string | "男" 或 "女" |
| height | number | 身高(cm) |
| weight | number | 体重(kg) |
| bmi | number | BMI 值 |
| status | string | 健康区间 |
| preferences | string | 偏好标签（逗号分隔） |

### 输出 JSON 格式
```json
{
  "breakfast": "全麦面包2片+水煮蛋1个+牛奶250ml",
  "breakfastCal": "450kcal",
  "lunch": "糙米饭150g+清蒸鲈鱼100g+蒜蓉西兰花200g",
  "lunchCal": "620kcal",
  "dinner": "小米粥1碗+凉拌黄瓜+酱牛肉80g",
  "dinnerCal": "480kcal",
  "totalCal": "1550kcal",
  "tips": "全天饮水量建议2000ml，避免高糖饮料"
}
```

---

## JavaFX 端实现

### 请求封装类 (CozeApiClient.java)

```java
package util;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class CozeApiClient {
    // Coze API 配置
    private static final String API_URL = "https://api.coze.cn/v1/workflow/run"; // 或 bot/chat
    private static final String API_KEY = "Bearer YOUR_COZE_API_TOKEN";
    private static final String BOT_ID = "YOUR_BOT_ID";
    private static final int TIMEOUT_SECONDS = 5;

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();

    /**
     * 调用 Coze 智能体获取膳食推荐
     */
    public static String getMealRecommendation(int age, String sex,
            double height, double weight, double bmi,
            String status, String preferences) throws Exception {

        // 构建请求 JSON
        String requestBody = String.format(
            "{\"bot_id\":\"%s\",\"user_id\":\"bmi_app_user\"," +
            "\"stream\":false,\"auto_save_history\":false," +
            "\"additional_messages\":[{\"role\":\"user\"," +
            "\"content\":\"请根据以下信息推荐一日三餐：年龄=%d, 性别=%s, " +
            "身高=%.1fcm, 体重=%.1fkg, BMI=%.1f, 健康状态=%s, 偏好=%s\"," +
            "\"content_type\":\"text\"}]}",
            BOT_ID, age, sex, height, weight, bmi, status, preferences
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", API_KEY)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("API 返回状态码：" + response.statusCode());
        }
    }
}
```

> Coze API 调用方式因版本而异。如果使用 Bot Chat API：`POST https://api.coze.cn/v3/chat`；如果通过 Workflow 发布：`POST https://api.coze.cn/v1/workflow/run`。请根据实际 Coze 配置调整。

### 推荐结果数据模型 (MealRecommendation.java)

```java
package model;

public class MealRecommendation {
    private String breakfast;
    private String breakfastCal;
    private String lunch;
    private String lunchCal;
    private String dinner;
    private String dinnerCal;
    private String totalCal;
    private String tips;

    // 构造器 + getter/setter

    public static MealRecommendation fromJson(String jsonStr) {
        // 使用 org.json 或 Gson 解析
        JSONObject json = new JSONObject(jsonStr);
        MealRecommendation rec = new MealRecommendation();
        rec.breakfast = json.optString("breakfast");
        rec.breakfastCal = json.optString("breakfastCal");
        rec.lunch = json.optString("lunch");
        rec.lunchCal = json.optString("lunchCal");
        rec.dinner = json.optString("dinner");
        rec.dinnerCal = json.optString("dinnerCal");
        rec.totalCal = json.optString("totalCal");
        rec.tips = json.optString("tips");
        return rec;
    }
}
```

#### 调用逻辑

```java
@FXML
private void handleGetRecommendation() {
    // 显示加载状态
    lblStatus.setText("正在为您生成专属膳食方案...");
    btnRecommend.setDisable(true);

    // 从 Session 和数据库获取用户数据
    Session session = Session.getInstance();

    // 获取最新 BMI 记录
    double latestBmi = getLatestBmi();
    String latestStatus = getLatestStatus();
    String preferences = session.getPreferences() != null
            ? session.getPreferences() : "无特殊偏好";

    // 异步调用 API（避免阻塞 UI 线程）
    new Thread(() -> {
        try {
            String result = CozeApiClient.getMealRecommendation(
                session.getUserAge(),
                session.getSex() == 0 ? "男" : "女",
                session.getHeight(),
                session.getWeight(),
                latestBmi,
                latestStatus,
                preferences
            );

            MealRecommendation meal = MealRecommendation.fromJson(result);

            // 回到 UI 线程更新界面
            Platform.runLater(() -> {
                displayMealCards(meal);
                lblStatus.setText("推荐方案已就绪");
                btnRecommend.setDisable(false);
            });
        } catch (java.net.http.HttpTimeoutException e) {
            Platform.runLater(() -> {
                lblStatus.setText("AI 服务繁忙，请稍后再试");
                btnRecommend.setDisable(false);
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                lblStatus.setText("请求失败：" + e.getMessage());
                btnRecommend.setDisable(false);
            });
        }
    }).start();
}
```

#### 卡片渲染

```java
private VBox createMealCard(String mealType, String emoji,
                             String content, String calories) {
    VBox card = new VBox(10);
    card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0;" +
                  "-fx-border-radius: 10; -fx-background-radius: 10;" +
                  "-fx-padding: 20; -fx-min-width: 200;");

    Label titleLabel = new Label(emoji + " " + mealType);
    titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    Label contentLabel = new Label(content);
    contentLabel.setWrapText(true);

    Label calLabel = new Label(calories);
    calLabel.setStyle("-fx-text-fill: #ff6b35; -fx-font-weight: bold; -fx-font-size: 18px;");

    card.getChildren().addAll(titleLabel, contentLabel, calLabel);
    return card;
}
```

---

## 推荐历史记录

### 数据库表（recommendations）
已在需求文档定义，建表 SQL：

```sql
CREATE TABLE recommendations (
    recId INT PRIMARY KEY AUTO_INCREMENT,
    userId INT NOT NULL,
    breakfast VARCHAR(100),
    lunch VARCHAR(100),
    dinner VARCHAR(100),
    totalCal VARCHAR(20),
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 保存逻辑

```java
@FXML
private void handleSaveRecommendation() {
    if (currentMeal == null) return;

    int userId = Session.getInstance().getUserId();
    String sql = "INSERT INTO recommendations (userId, breakfast, lunch, dinner, totalCal) VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, userId);
        ps.setString(2, currentMeal.getBreakfast());
        ps.setString(3, currentMeal.getLunch());
        ps.setString(4, currentMeal.getDinner());
        ps.setString(5, currentMeal.getTotalCal());
        ps.executeUpdate();
        showInfo("已保存", "膳食方案已保存到历史记录");
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert("保存失败");
    }
}
```

---

## 偏好配置

在 `profile.fxml`（个人信息编辑页）中增加饮食偏好选择：

### CheckBox 选项
- `cbNoSpicy`：不吃辣
- `cbNoSeafood`：不吃海鲜
- `cbLowCal`：低卡偏好
- `cbHighProtein`：高蛋白偏好
- `cbVegetarian`：素食

### 偏好字符串拼接
```java
List<String> selected = new ArrayList<>();
if (cbNoSpicy.isSelected()) selected.add("不吃辣");
if (cbNoSeafood.isSelected()) selected.add("不吃海鲜");
if (cbLowCal.isSelected()) selected.add("低卡");
if (cbHighProtein.isSelected()) selected.add("高蛋白");
if (cbVegetarian.isSelected()) selected.add("素食");
String preferences = String.join(",", selected);
```

---

## 通用约束

- API 调用必须在**后台线程**执行，避免阻塞 UI
- 超时设置为 5 秒，超时后显示"AI 服务繁忙，请稍后再试"
- API Key 建议放在配置文件而非硬编码
- 网络请求使用 try-catch 包裹，异常时友好提示

## 验证清单

- [ ] 点击"生成推荐"后显示加载状态
- [ ] API 返回数据后正确解析为三餐卡片
- [ ] 卡片包含餐食名称和预估热量
- [ ] "换一批推荐"可重新请求 API
- [ ] "保存记录"将推荐写入 recommendations 表
- [ ] API 超时（5秒）时显示"AI 服务繁忙"
- [ ] API 无网络时不会崩溃，有友好提示
- [ ] 偏好选择正确传递到 API 请求中
