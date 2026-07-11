# Implementation Plan: BMI体质评估与预测系统

**Branch**: `001-bmi-track` | **Date**: 2026-07-11 | **Spec**: `specs/001-bmi-track/spec.md`

**Input**: 项目需求分析文档

## Summary

开发一款单机 JavaFX 桌面应用，实现 6 大模块：用户管理、BMI 计算与存储、历史记录与折线图、趋势预测（最小二乘法线性回归 + 4 个增强组件）、AI 膳食推荐（Coze API）、食物选择参考。系统为大一实训项目，3 人团队协作开发。

## Technical Context

**Language/Version**: Java 25 (JDK 17+ 兼容)

**Primary Dependencies**: JavaFX 21 (controls + fxml)、MySQL Connector/J 8.4.0、JUnit 5.12

**Storage**: MySQL 8.0（4 张表：users, bmi_records, foods, recommendations）

**Testing**: JUnit 5（每个模块 ≥ 3 个测试用例）

**Target Platform**: Windows 桌面应用（JDK 17+）

**Project Type**: 桌面应用（JavaFX + SceneBuilder）

**Performance Goals**: 界面切换和按钮响应 ≤ 1 秒

**Constraints**: 
- 所有数据库操作必须 try-catch，异常弹出中文友好提示
- FXML 文件存放在 `src/main/resources/fxml/`
- 主界面 BorderPane 布局，左侧导航右侧内容区
- Coze API 超时限制 5 秒
- 密码明文存储（实训作业无需加密）
- 关键逻辑加中文注释

**Scale/Scope**: 单机桌面应用，3 人团队，6 大模块约 20 个工作项

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- ✅ 界面与交互分离: 使用 FXML + Controller + Service 三层架构
- ✅ 数据库操作安全: 统一 DBUtil + PreparedStatement + try-catch
- ✅ 代码可读性优先: Java 命名规范 + 中文注释 + 单文件 ≤ 200 行
- ✅ 模块独立开发: 按依赖顺序分 6 阶段开发
- ✅ 最少依赖原则: 仅 JavaFX + MySQL Connector + JUnit
- ✅ 技术栈锁定: 符合 JDK 17+ / JavaFX 21 / Coze API 规范

## Project Structure

### Documentation (this feature)

```text
specs/001-bmi-track/
├── spec.md              # 需求分析文档
├── plan.md              # 本文件（实施计划）
├── research.md          # 技术调研文档
├── data-model.md        # 数据模型设计
├── quickstart.md        # 快速上手指南
├── contracts/           # API 契约定义
└── tasks.md             # 任务分解（由 /speckit.tasks 生成）
```

### Source Code (repository root)

```text
src/main/
├── java/com/example/test/
│   ├── HelloApplication.java       # 主入口
│   ├── HelloController.java        # 主控制器
│   ├── model/                      # 实体类（User, BmiRecord, Food, Recommendation）
│   ├── dao/                        # 数据访问层（UserDao, BmiRecordDao, FoodDao, RecommendationDao）
│   ├── service/                    # 业务逻辑层（UserService, BmiService, PredictionService, CozeService, FoodService）
│   ├── util/                       # 工具类（DBUtil, LinearRegression, CozeClient）
│   └── controller/                 # FXML 控制器（LoginController, RegisterController, MainController, ...）
├── resources/
│   ├── fxml/                       # FXML 布局文件（login.fxml, register.fxml, main.fxml, ...）
│   ├── sql/                        # SQL 脚本（schema.sql, seed.sql）
│   └── images/                     # 图片资源
└── module-info.java                # Java 模块配置

src/test/java/com/example/test/
├── model/                          # 实体类测试
├── service/                        # 业务逻辑测试（BmiServiceTest, LinearRegressionTest）
└── dao/                            # DAO 测试
```

**Structure Decision**: 采用标准 Maven + JavaFX 项目结构，按三层架构（Controller/Service/DAO）分层组织代码。

## Complexity Tracking

无违规项，项目技术架构符合宪法要求。

## 阶段划分

| 阶段 | 模块 | 工作项 | 依赖 |
|------|------|--------|------|
| Phase 1 | 用户管理 | M1-M4, M19, M20 | 无 |
| Phase 2 | BMI 计算与存储 | M5 | Phase 1 |
| Phase 3 | 历史记录与折线图 | M6, M7 | Phase 2 |
| Phase 4 | 趋势预测 | M8-M13 | Phase 3 |
| Phase 5 | AI 膳食推荐 | M14-M16 | Phase 1 |
| Phase 6 | 食物参考 | M17, M18, T2 | 无 |

详见 `data-model.md`、`research.md`、`contracts/`。
