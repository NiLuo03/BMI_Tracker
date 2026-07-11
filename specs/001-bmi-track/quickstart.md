# Quickstart: BMI体质评估与预测系统

## 启动前准备

### 1. 数据库
```sql
-- 在 MySQL 中执行
CREATE DATABASE bmi_track CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bmi_track;
SOURCE src/main/resources/sql/schema.sql;   -- 建4张表
SOURCE src/main/resources/sql/seed.sql;     -- 插入50种食物
```

### 2. 配置数据库连接
修改 `DBUtil.java` 中的连接参数：
```java
private static final String URL = "jdbc:mysql://localhost:3306/bmi_track?useSSL=false&serverTimezone=Asia/Shanghai";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

### 3. 配置 Coze API
修改 `CozeClient.java` 中的 API Key 和 URL：
```java
private static final String API_URL = "https://api.coze.cn/v1/chat/completions";
private static final String API_KEY = "your_coze_api_key";
```

### 4. 运行
```bash
mvn clean javafx:run
```

## 开发环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0
- SceneBuilder（编辑 FXML）
- IntelliJ IDEA（推荐）
