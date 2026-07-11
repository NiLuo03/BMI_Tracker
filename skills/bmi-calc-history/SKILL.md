---
name: bmi-calc-history
description: >
  BMI体质评估系统的计算、记录存储与历史折线图模块。实现BMI值计算、健康区间自动匹配、
  记录持久化、历史数据表格展示、BMI趋势折线图绘制。
  触发词：BMI计算、BMI记录、历史记录、折线图、BMI折线图、LineChart、bmi_records、
  TableView、健康区间、记录并计算、BMI图表、体重记录。
  当用户提到 BMI 计算、存储记录、历史数据展示或图表相关功能时，使用此 Skill。
---
# BMI 计算与历史记录模块 (Calculation & History)

## 开发目标
实现 BMI 值计算、健康区间自动匹配、记录写入数据库、历史数据表格分页展示、BMI 趋势折线图。

## 前置依赖
- 用户管理模块已完成（`users` 表已创建，Session 单例可用）
- 导航菜单已搭建，本模块对应"BMI记录"菜单项

## 数据库

### 建表 SQL

```sql
USE bmi_system;

CREATE TABLE bmi_records (
    recordId INT PRIMARY KEY AUTO_INCREMENT,
    userId INT NOT NULL,
    height DECIMAL(5,2) NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    bmi DECIMAL(4,1) NOT NULL,
    status VARCHAR(10) NOT NULL COMMENT '偏瘦/正常/超重/肥胖',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 功能实现

### 1. BMI 计算与存储

**位置**：主界面 BMI 记录区（可以是 BorderPane 中心区域的一个面板）

#### 界面元素
- `TextField`：身高 cm (`tfHeight`)、体重 kg (`tfWeight`)
- `Label`：当前 BMI 值显示 (`lblBmiValue`)、健康状态显示 (`lblStatus`)
- `Button`：记录并计算 (`btnRecord`)

#### 核心逻辑 (btnRecord 点击事件)

```java
@FXML
private void handleRecord() {
    // 1. 校验输入
    String heightStr = tfHeight.getText().trim();
    String weightStr = tfWeight.getText().trim();
    if (heightStr.isEmpty() || weightStr.isEmpty()) {
        showAlert("请输入身高和体重");
        return;
    }

    double height = Double.parseDouble(heightStr);
    double weight = Double.parseDouble(weightStr);
    if (height <= 0 || weight <= 0) {
        showAlert("请输入有效的身高和体重数值");
        return;
    }

    // 2. 计算 BMI（保留一位小数）
    double bmi = weight / Math.pow(height / 100.0, 2);
    bmi = Math.round(bmi * 10.0) / 10.0;

    // 3. 匹配健康区间
    String status;
    if (bmi < 18.5)       status = "偏瘦";
    else if (bmi < 24.0)  status = "正常";
    else if (bmi < 28.0)  status = "超重";
    else                  status = "肥胖";

    // 4. 写入数据库
    int userId = Session.getInstance().getUserId();
    String sql = "INSERT INTO bmi_records (userId, height, weight, bmi, status) VALUES (?, ?, ?, ?, ?)";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, userId);
        ps.setDouble(2, height);
        ps.setDouble(3, weight);
        ps.setDouble(4, bmi);
        ps.setString(5, status);
        ps.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert("记录保存失败，请稍后再试");
        return;
    }

    // 5. 更新显示 & 弹窗
    lblBmiValue.setText(String.format("%.1f", bmi));
    lblStatus.setText(status);
    showInfo("BMI 计算结果", String.format("您的 BMI 为 %.1f，状态：%s", bmi, status));

    // 6. 刷新历史记录列表
    refreshHistoryTable();
}
```

> 健康区间边界：`<18.5` 偏瘦，`[18.5, 24)` 正常，`[24, 28)` 超重，`>=28` 肥胖。

---

### 2. 历史数据表格 (TableView)

**位置**：主界面下方或独立标签页

#### 界面结构
- `TableView<BmiRecord>`：展示历史记录
- `TableColumn`：日期、身高(cm)、体重(kg)、BMI、健康状态
- `Pagination` 或手动分页（每页 10 条）

#### 数据模型 (BmiRecord.java)

```java
public class BmiRecord {
    private final IntegerProperty recordId;
    private final DoubleProperty height;
    private final DoubleProperty weight;
    private final DoubleProperty bmi;
    private final StringProperty status;
    private final ObjectProperty<LocalDateTime> createTime;

    // 构造器 + getter/setter ...
}
```

#### 查询逻辑

```java
private void refreshHistoryTable() {
    int userId = Session.getInstance().getUserId();
    // 按时间倒序查询
    String sql = "SELECT recordId, height, weight, bmi, status, createTime FROM bmi_records WHERE userId = ? ORDER BY createTime DESC";

    List<BmiRecord> records = new ArrayList<>();
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            records.add(new BmiRecord(
                rs.getInt("recordId"),
                rs.getDouble("height"),
                rs.getDouble("weight"),
                rs.getDouble("bmi"),
                rs.getString("status"),
                rs.getTimestamp("createTime").toLocalDateTime()
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert("数据加载失败");
        return;
    }

    ObservableList<BmiRecord> data = FXCollections.observableArrayList(records);
    tableView.setItems(data);
    refreshChart(); // 同步刷新折线图
}
```

#### 分页实现
- 维护 `currentPage` 和 `pageSize = 10` 变量
- 使用 `subList` 截取当前页数据
- "上一页"/"下一页"按钮控制翻页，边界禁用

---

### 3. BMI 折线图 (LineChart)

**位置**：表格上方或独立面板

#### 实现要求
使用 JavaFX 内置 `LineChart<Number, Number>`，X 轴为日期，Y 轴为 BMI 值。

#### 关键实现

```java
@FXML
private LineChart<Number, Number> bmiChart;
@FXML
private NumberAxis xAxis;
@FXML
private NumberAxis yAxis;

private void refreshChart() {
    bmiChart.getData().clear();

    // 按时间升序查询（用于图表）
    String sql = "SELECT bmi, createTime FROM bmi_records WHERE userId = ? ORDER BY createTime ASC";
    List<Double> bmiValues = new ArrayList<>();
    List<String> dates = new ArrayList<>();

    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, Session.getInstance().getUserId());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            bmiValues.add(rs.getDouble("bmi"));
            // 日期格式化为 MM-dd
            dates.add(rs.getTimestamp("createTime").toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("MM-dd")));
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return;
    }

    // 不足 2 条时显示提示
    if (bmiValues.size() < 2) {
        lblChartHint.setText("暂无足够数据绘制折线图（需要至少 2 条记录）");
        lblChartHint.setVisible(true);
        bmiChart.setVisible(false);
        return;
    }

    lblChartHint.setVisible(false);
    bmiChart.setVisible(true);

    // 构建数据系列
    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    series.setName("BMI 趋势");
    for (int i = 0; i < bmiValues.size(); i++) {
        series.getData().add(new XYChart.Data<>(i, bmiValues.get(i)));
    }

    bmiChart.getData().add(series);

    // 配置 X 轴显示日期标签（自定义 CategoryAxis 或 StringConverter）
    xAxis.setLabel("日期");
    yAxis.setLabel("BMI 值");
    yAxis.setAutoRanging(false);
    yAxis.setLowerBound(10.0);
    yAxis.setUpperBound(40.0);
    yAxis.setTickUnit(2.0);
}
```

#### X 轴日期显示方案

由于 `NumberAxis` 不支持直接显示字符串，推荐使用 `CategoryAxis` 替代：

```xml
<!-- FXML 中 -->
<CategoryAxis fx:id="xAxis" label="日期" />
<NumberAxis fx:id="yAxis" label="BMI" />
```

或者保留 NumberAxis，在每个 Data 点使用 `Node` 设置 tooltip 显示日期。

---

## 通用约束

- 所有数据库操作包裹在 try-catch 中，异常弹窗显示中文提示
- 弹窗统一使用 `Alert`，禁止输出堆栈信息
- 页面切换和数据刷新响应时间应在 1 秒内

## 验证清单

- [ ] 输入身高 170cm、体重 65kg，BMI 显示 22.5，状态"正常"
- [ ] 输入身高 170cm、体重 50kg，BMI 显示 17.3，状态"偏瘦"
- [ ] 点击"记录并计算"后，数据写入 bmi_records 表
- [ ] 历史表格按时间倒序显示
- [ ] 超过 10 条记录时，分页功能正常
- [ ] 记录 >= 2 条时，折线图正确渲染
- [ ] 记录 < 2 条时，显示"暂无足够数据"提示
- [ ] 空输入或非法数值输入有校验提示
