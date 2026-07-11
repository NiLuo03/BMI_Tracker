# BMI_Track Constitution

## Core Principles

### I. 界面与交互分离
所有 UI 布局使用 SceneBuilder + FXML 文件描述，存放在 `src/main/resources/fxml/`。业务逻辑不写在 Controller 中，抽取为独立的 Service 类。

### II. 数据库操作安全
所有数据库操作（增删改查）必须包裹在 `try-catch` 中，异常时弹出友好中文提示（不显示堆栈信息）。DAO 层统一使用 JDBC + 预编译 `PreparedStatement`，禁止拼接 SQL。数据库连接统一管理，用完后及时关闭。

### III. 代码可读性优先
遵循 Java 命名规范（camelCase 变量/方法、PascalCase 类名），关键逻辑和复杂算法（如最小二乘法、BMI 计算）必须加中文注释。类/方法职责单一，单文件不超过 200 行。

### IV. 模块独立开发
6 个模块按依赖顺序分阶段开发（用户管理 → BMI 计算 → 历史趋势 → 预测 → AI 推荐 → 食物参考）。每个阶段产生的代码不影响上一阶段功能，提交粒度保持阶段独立。

### V. 最少依赖原则
仅使用 Maven 管理的必需依赖：JavaFX（controls + fxml）、MySQL Connector/J、JUnit（测试），不引入额外框架或第三方库。Coze API 调用使用 JDK 内置 `HttpURLConnection`，不引入 HTTP 客户端库。

## 技术约束

### 技术栈锁定
- **语言**：Java 25（JDK 17+ 兼容）
- **UI**：JavaFX 21 + SceneBuilder（FXML）
- **数据库**：MySQL 8.0 + JDBC
- **构建**：Maven（`javafx-maven-plugin` 运行）
- **AI**：Coze API（HTTP POST JSON，5s 超时）
- **算法**：最小二乘法线性回归（纯 Java 实现，不依赖第三方数学库）
- **密码**：明文存储

### 数据库规范
4 张表：`users`、`bmi_records`、`foods`、`recommendations`。建表 SQL 放在 `src/main/resources/sql/` 目录下，附带初始数据（至少 50 种食物）。字段名使用 camelCase。

## 开发工作流

### 阶段顺序
1. 用户管理（注册/登录/个人信息编辑）
2. BMI 计算与存储
3. 历史记录列表 + 折线图
4. 趋势预测（含 4 个增强组件）
5. AI 膳食推荐（Coze API 调用）
6. 食物选择参考（对比 + 榜单）

### 质量门禁
- 每个模块完成需通过人工验收（对照需求文档逐条检查）
- 所有 FXML 文件必须能在 SceneBuilder 中正常打开
- 界面切换和按钮响应须在 1 秒内完成
- 进入下一阶段前确保上一阶段无 Bug

## Governance
本宪法是项目开发的最高准则，所有代码生成和修改必须遵守。如因需求变更需要修改宪法，需记录修改原因并经团队确认。每次实现前检查宪法确保方向一致。

**Version**: 1.0.0 | **Ratified**: 2026-07-11 | **Last Amended**: 2026-07-11
