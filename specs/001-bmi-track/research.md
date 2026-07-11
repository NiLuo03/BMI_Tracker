# Research: BMI体质评估与预测系统

## 技术选型确认

### JavaFX 21 + SceneBuilder
- **JavaFX 21** 为最新 LTS 版本，通过 Maven `javafx-maven-plugin` 运行
- `module-info.java` 需声明 `requires javafx.controls; requires javafx.fxml;`
- SceneBuilder 拖拽生成 FXML → Controller 通过 `@FXML` 注解绑定
- LineChart 使用 `NumberAxis` 作为 X/Y 轴，数据通过 `XYChart.Series` 填充

### MySQL 8.0 + JDBC
- MySQL Connector/J 8.4.0 通过 Maven 引入
- module-info.java 需声明 `requires java.sql;`
- 连接 URL: `jdbc:mysql://localhost:3306/bmi_track?useSSL=false&serverTimezone=Asia/Shanghai`
- 预编译: `PreparedStatement` 防 SQL 注入

### 最小二乘法（纯 Java 实现）
- 无需第三方数学库，纯 `double` 计算
- 注意处理除零异常（当所有 x_i 相等时）
- 斜率 a = Σ[(x_i - x̄)(y_i - ȳ)] / Σ[(x_i - x̄)²]
- 截距 b = ȳ - a × x̄

### Coze API
- HTTP POST 到 Coze 智能体 API
- 超时 5 秒（`HttpURLConnection.setConnectTimeout(5000)`）
- JSON 解析：使用 `javax.json` 或手动解析（建议手动，避免加依赖）
- 返回格式：`{ "breakfast": "...", "lunch": "...", "dinner": "...", "totalCal": "..." }`

## 关键决策点

1. **密码存储**：明文存储（实训作业要求，无需加密）
2. **数据初始化**：MySQL 建表 SQL 放在 `src/main/resources/sql/schema.sql`，50 种食物种子数据放在 `seed.sql`
3. **FXML 目录**：统一 `src/main/resources/fxml/`，按模块命名（`login.fxml`, `register.fxml`, `main.fxml`, `bmi_record.fxml`, `history.fxml`, `prediction.fxml`, `diet.fxml`, `food.fxml`）
4. **Coze API Key**：硬编码在配置类中（实训项目，无需安全存储）
5. **折线图颜色**：历史数据蓝色实线，预测数据红色虚线，预警带使用半透明色块叠加

## 风险

- WSL 环境无法直接运行 JavaFX（需 Windows 原生 JDK），建议在 Windows 上开发
- MySQL 需确保局域网内三台机器都能连接：检查 `bind-address` 和防火墙
- Coze API 可能因网络问题超时：添加 5 秒超时 + 友好提示
