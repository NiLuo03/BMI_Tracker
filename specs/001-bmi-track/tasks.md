# Tasks: BMI体质评估与预测系统

**Input**: Design documents from `specs/001-bmi-track/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Organization**: 按 6 阶段 + 团队分工（A: FXML / B: DAO+DB / C: Controller）组织

## 格式: `[ID] [P] [负责人] 描述 [文件路径]`

- **[P]**: 可并行（不同文件，无依赖）
- **A/B/C**: 团队角色（A=界面布局, B=数据库+工具, C=Controller逻辑）

---

## Phase 1: 基础设施（Shared Infrastructure）

- [ ] T001 [B] 重命名包结构 `com.example.test` → `com.bmitracker`，修改 module-info.java 和 pom.xml，统一包名
- [ ] T002 [B] 完善 `DBUtil.java`：连接池管理、资源自动关闭、异常统一处理 → `src/main/java/com/bmitracker/util/DBUtil.java`
- [ ] T003 [B] 创建 SQL 目录和建表脚本 → `src/main/resources/sql/schema.sql`（4张表DDL）
- [ ] T004 [B] [P] 创建实体类：`User.java`, `BmiRecord.java`, `Food.java`, `Recommendation.java` → `src/main/java/com/bmitracker/model/`
- [ ] T005 [A] [P] 搭建主界面框架：BorderPane 布局，左侧6个导航按钮，右侧 StackPane 内容区 → `src/main/resources/fxml/main.fxml`
- [ ] T006 [C] [P] 创建 `MainController.java`：导航按钮切换右侧内容面板 → `src/main/java/com/bmitracker/controller/MainController.java`

**Checkpoint**: 主界面框架跑通，导航按钮可切换空白面板

---

## Phase 2: 用户管理（Module 1）

### 注册功能
- [ ] T007 [A] 拖注册界面 FXML：账号、密码、年龄、性别(单选框)、注册按钮、跳转登录链接 → `src/main/resources/fxml/register.fxml`
- [ ] T008 [B] 实现 `UserDao.java`：`insertUser()`, `findByUserName()` → `src/main/java/com/bmitracker/dao/UserDao.java`
- [ ] T009 [B] 实现 `UserService.java`：注册校验（空字段、账号重复）→ `src/main/java/com/bmitracker/service/UserService.java`
- [ ] T010 [C] 实现 `RegisterController.java`：表单校验 → 调 Service → 成功跳转登录 / 失败弹窗提示 → `src/main/java/com/bmitracker/controller/RegisterController.java`

### 登录功能
- [ ] T011 [A] 拖登录界面 FXML：账号、密码输入框、登录按钮、跳转注册链接 → `src/main/resources/fxml/login.fxml`
- [ ] T012 [B] `UserDao.java` 加 `findByUserNameAndPassword()` 方法
- [ ] T013 [C] 实现 `LoginController.java`：校验 → 调 Service → 成功跳转主界面 / 失败弹窗 → `src/main/java/com/bmitracker/controller/LoginController.java`

### 密码修改
- [ ] T014 [A] 拖密码修改界面 FXML：旧密码、新密码、确认新密码、修改按钮 → `src/main/resources/fxml/change_password.fxml`
- [ ] T015 [B] `UserDao.java` 加 `updatePassword()` 方法
- [ ] T016 [C] 实现 `ChangePasswordController.java`：旧密码校验 → 两次新密码一致检查 → 更新 → 提示 → `src/main/java/com/bmitracker/controller/ChangePasswordController.java`

### 个人信息编辑
- [ ] T017 [A] 拖个人信息界面 FXML：展示 userId/userName/年龄/性别/身高/体重/偏好多选框 + 编辑保存按钮 → `src/main/resources/fxml/profile.fxml`
- [ ] T018 [B] `UserDao.java` 加 `updateUser()` 方法，`UserService.java` 加更新逻辑
- [ ] T019 [C] 实现 `ProfileController.java`：加载用户数据 → 编辑 → 保存 → `src/main/java/com/bmitracker/controller/ProfileController.java`

**Checkpoint**: 完整用户管理流程跑通：注册 → 登录 → 修改密码 → 编辑个人信息

---

## Phase 3: BMI 计算与历史记录（Module 2 & 3）

- [ ] T020 [A] 拖 BMI 记录界面 FXML：身高输入、体重输入、"记录并计算"按钮、结果显示标签 → `src/main/resources/fxml/bmi_record.fxml`
- [ ] T021 [B] 实现 `BmiRecordDao.java`：`insertRecord()`, `findByUserIdDesc()`, `findByUserIdAsc()`, `countByUserId()` → `src/main/java/com/bmitracker/dao/BmiRecordDao.java`
- [ ] T022 [B] 实现 `BmiService.java`：`calculateBMI()`, `getHealthStatus()`, `saveRecord()` → `src/main/java/com/bmitracker/service/BmiService.java`
- [ ] T023 [C] 实现 `BmiController.java`：输入校验 → 计算 BMI → 匹配区间 → 存储 → 弹窗显示 → `src/main/java/com/bmitracker/controller/BmiController.java`
- [ ] T024 [A] 拖历史记录界面 FXML：TableView（日期/身高/体重/BMI/区间）+ 分页控制 → `src/main/resources/fxml/history.fxml`
- [ ] T025 [C] 实现 `HistoryController.java`：查数据库 → 填充 TableView（倒序，每页10条）→ `src/main/java/com/bmitracker/controller/HistoryController.java`

**Checkpoint**: 录 BMI → 存数据库 → 历史列表展示，数据准确

---

## Phase 4: 折线图（Module 3 续）

- [ ] T026 [A] 拖折线图界面 FXML：LineChart 控件 + 提示标签（<2条记录时显示）→ `src/main/resources/fxml/chart.fxml`
- [ ] T027 [C] 实现 `ChartController.java`：判断记录数 → ≥2条时绘制 LineChart（日期X轴，BMI Y轴）→ `src/main/java/com/bmitracker/controller/ChartController.java`

**Checkpoint**: BMI折线图正确显示，<2条时显示提示

---

## Phase 5: 趋势预测（Module 4 + 4个增强组件）

- [ ] T028 [B] 实现 `LinearRegression.java`：`fit(List<BmiRecord>)` 计算斜率a和截距b，`predict(x)` 返回预测值 → `src/main/java/com/bmitracker/util/LinearRegression.java`
- [ ] T029 [A] 拖趋势预测界面 FXML：折线图 + 方程文字区 + 预测按钮 + 预警标签 + 控制线 → `src/main/resources/fxml/prediction.fxml`
- [ ] T030 [C] ① 回归方程展示：显示 `BMI = a×天数 + b` + 斜率含义文字 → `src/main/java/com/bmitracker/controller/PredictionController.java`
- [ ] T031 [C] ② 健康区间预警带：折线图背景叠加半透明绿/黄/红色块，预测7天后值用闪烁红点标出
- [ ] T032 [C] ③ 控制线对比：画真实历史+预测线（实线+虚线）+ 理想平缓线（水平延伸）
- [ ] T033 [C] ④ 历史极值标注：红点标最高BMI+日期，绿点标最低BMI+日期

**Checkpoint**: 记录≥4条时按钮可点击，预测结果正确，4个增强组件全部展示

---

## Phase 6: AI 膳食推荐（Module 5）

- [ ] T034 [B] 实现 `CozeClient.java`：HTTP POST 封装，5秒超时，JSON 解析 → `src/main/java/com/bmitracker/util/CozeClient.java`
- [ ] T035 [B] 实现 `RecommendationDao.java`：`insertRecommendation()`, `findLatestByUserId()` → `src/main/java/com/bmitracker/dao/RecommendationDao.java`
- [ ] T036 [A] 拖膳食推荐界面 FXML：生成推荐按钮 + 早餐/午餐/晚餐/热量卡片展示区 → `src/main/resources/fxml/diet.fxml`
- [ ] T037 [C] 实现 `DietController.java`：读取用户数据 → 调 Coze API → 解析返回 → 卡片展示 → `src/main/java/com/bmitracker/controller/DietController.java`

**Checkpoint**: AI 推荐成功返回并展示，超时时显示友好提示

---

## Phase 7: 食物选择参考（Module 6）

- [ ] T038 [B] 创建食物种子数据 SQL（50种食物）→ `src/main/resources/sql/seed.sql`
- [ ] T039 [B] 实现 `FoodDao.java`：`findByCategoryOrderByCalories()`, `findByIds()`, `findAllCategories()` → `src/main/java/com/bmitracker/dao/FoodDao.java`
- [ ] T040 [A] 拖食物对比界面 FXML：2~3个食物下拉选择框 + 横向对比表格 → `src/main/resources/fxml/food_compare.fxml`
- [ ] T041 [C] 实现 `FoodCompareController.java`：选择食物 → 查数据 → 表格对比 → 高亮热量最低/蛋白质最高 → `src/main/java/com/bmitracker/controller/FoodCompareController.java`
- [ ] T042 [A] 拖食物榜单界面 FXML：分类下拉框 + 排序 TableView → `src/main/resources/fxml/food_rank.fxml`
- [ ] T043 [C] 实现 `FoodRankController.java`：选分类 → 查数据 → 前10名 → "低卡推荐"徽章 → `src/main/java/com/bmitracker/controller/FoodRankController.java`

**Checkpoint**: 食物对比和榜单功能正常，50种食物数据已录入

---

## Phase 8: 集成与收尾

- [ ] T044 [All] 模块联调：完整跑通注册→登录→录BMI→历史→趋势→推荐→食物全流程
- [ ] T045 [B] 测试：`BmiServiceTest`（计算/区间匹配）、`LinearRegressionTest`（拟合/预测）→ `src/test/java/com/bmitracker/`
- [ ] T046 [C] 测试：Controller 边界测试（空输入、异常数据）
- [ ] T047 [A] 检查所有 FXML 在 SceneBuilder 中正常打开
- [ ] T048 [All] 对照需求文档逐条验收，确保6大模块全部覆盖

**Checkpoint**: 全流程跑通，验收通过

---

## 依赖关系

| 阶段 | 前置依赖 | 可并行 |
|------|----------|--------|
| Phase 1 | 无 | T004+T005+T006 |
| Phase 2 | Phase 1 | T007+T008+T009(串行) |
| Phase 3 | Phase 2 | 无 |
| Phase 4 | Phase 3 | 无 |
| Phase 5 | Phase 4 | T028+T029(并行) |
| Phase 6 | Phase 2 | T034+T035+T036(并行) |
| Phase 7 | Phase 1 | T038+T039+T040(并行) |
| Phase 8 | All | T045+T046+T047(并行) |

### 团队并行策略（3人）

```
Phase 1: A→T005(FXML), B→T001-T004(DB+model), C→T006(Controller)
Phase 2: A→拖FXML, B→DAO+Service, C→Controller
Phase 3-4: 同上
Phase 5: A→T029(FXML), B→T028(LinearRegression), C→T030-T033(预测逻辑)
Phase 6: A→T036(FXML), B→T034-T035(Coze+DAO), C→T037(Controller)
Phase 7: A→T040+T042(FXML), B→T038-T039(种子数据+DAO), C→T041+T043(Controller)
Phase 8: 三人合作联调
```
