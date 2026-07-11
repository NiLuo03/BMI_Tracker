---
name: bmi-trend-predict
description: >
  BMI体质评估系统的趋势预测模块。实现最小二乘法线性回归预测7天后BMI，
  包含4个增强可视化组件：回归方程展示、健康区间预警带、模拟控制线对比、历史极值标注。
  触发词：趋势预测、BMI预测、线性回归、最小二乘法、预测BMI、7天预测、健康预警、预警带、
  控制线、历史极值、回归方程、线性拟合。
  当用户提到 BMI 趋势预测、数据分析或线性回归相关功能时，使用此 Skill。
---
# BMI 趋势预测模块 (Trend Prediction)

## 开发目标
基于用户历史 BMI 记录，使用最小二乘法线性回归预测 7 天后的 BMI 值，并通过 4 个增强组件在折线图上进行可视化展示。

## 前置依赖
- bmi_records 表已有数据
- 折线图基础已实现（来自 bmi-calc-history Skill）
- 需要 JavaFX LineChart 控件

## 触发条件

- 当前用户历史 BMI 记录 **>= 4 条**时，"趋势预测"按钮可点击
- 若 < 4 条：按钮置灰（`setDisable(true)`），鼠标悬停 tooltip 显示"至少录入4次数据，趋势预测更准确哦~"

---

## 核心算法：最小二乘法线性回归

### 数学模型
```
BMI = a × (天数序号) + b
```

### 计算步骤

```java
/**
 * 对历史 BMI 数据执行线性回归
 * @param bmiValues 按时间升序排列的 BMI 值列表
 * @return double[]{斜率a, 截距b, 预测值predictedBmi, R²}
 */
public double[] linearRegression(List<Double> bmiValues) {
    int n = bmiValues.size();
    // x_i = 0, 1, 2, ..., n-1
    double[] x = new double[n];
    double[] y = new double[n];
    for (int i = 0; i < n; i++) {
        x[i] = i;
        y[i] = bmiValues.get(i);
    }

    // 1. 计算均值
    double sumX = 0, sumY = 0;
    for (int i = 0; i < n; i++) {
        sumX += x[i];
        sumY += y[i];
    }
    double meanX = sumX / n;
    double meanY = sumY / n;

    // 2. 计算斜率 a
    double numerator = 0, denominator = 0;
    for (int i = 0; i < n; i++) {
        numerator += (x[i] - meanX) * (y[i] - meanY);
        denominator += (x[i] - meanX) * (x[i] - meanX);
    }
    double a = numerator / denominator;

    // 3. 计算截距 b
    double b = meanY - a * meanX;

    // 4. 预测 7 天后（x_pred = n + 6，因为第 n 条对应 x = n-1）
    double xPred = n + 6;
    double predictedBmi = a * xPred + b;
    predictedBmi = Math.round(predictedBmi * 10.0) / 10.0;

    return new double[]{a, b, predictedBmi};
}
```

### 数值格式要求
- a（斜率）：保留 **4 位**小数
- b（截距）：保留 2 位小数
- 预测 BMI：保留 1 位小数

---

## 四个增强组件

### 组件 ① 回归方程展示

**位置**：折线图下方

**展示内容**：
```
回归方程：BMI = {a} × 天数 + {b}
斜率含义：您的 BMI 平均每天{上升/下降} {|a|} 个单位
```

**实现**：
```java
String trend = a > 0 ? "上升" : "下降";
String equationText = String.format("回归方程：BMI = %.4f × 天数 + %.2f", a, b);
String slopeText = String.format("您的 BMI 平均每天%s %.4f 个单位", trend, Math.abs(a));
lblEquation.setText(equationText + "\n" + slopeText);
```

---

### 组件 ② 健康区间预警带

**需求**：在折线图背景中用半透明色块标识健康区间，并用闪烁红点标注预测值。

#### 色块定义
| 区间 | BMI 范围 | 颜色 | 含义 |
|------|----------|------|------|
| 绿色带 | 18.5 ~ 24 | rgba(0,200,0,0.1) | 正常区间 |
| 黄色带 | <18.5 或 24~28 | rgba(255,200,0,0.15) | 偏瘦/超重 |
| 红色带 | >=28 | rgba(255,0,0,0.1) | 肥胖区间 |

#### 实现方案

由于 JavaFX LineChart 不原生支持背景色块，采用以下方案之一：

**方案 A（推荐）**：在折线图区域叠加自定义 `Canvas` 或 `Pane`，在其上绘制半透明矩形。

**方案 B**：使用 `XYChart.plotArea` 的 CSS 或 `lookup` 方式添加背景节点。

**方案 A 实现关键代码**：

```java
private void addHealthZoneBands() {
    // 在 chart 的 plotArea 上叠加一个 Pane
    Node plotArea = bmiChart.lookup(".chart-plot-background");
    Pane overlay = new Pane();
    overlay.setMouseTransparent(true); // 鼠标事件穿透
    overlay.prefWidthProperty().bind(plotArea.layoutBoundsProperty()
        .map(b -> b.getWidth()));
    overlay.prefHeightProperty().bind(plotArea.layoutBoundsProperty()
        .map(b -> b.getHeight()));

    // 计算 Y 轴映射：将 BMI 值映射为 Pane 内的像素位置
    double totalRange = yAxis.getUpperBound() - yAxis.getLowerBound();
    double plotHeight = plotArea.getLayoutBounds().getHeight();

    // 绿色带 (18.5 ~ 24)
    double greenTop = (yAxis.getUpperBound() - 24) / totalRange * plotHeight;
    double greenHeight = (24 - 18.5) / totalRange * plotHeight;
    Rectangle greenZone = new Rectangle(0, greenTop, plotArea.getLayoutBounds().getWidth(), greenHeight);
    greenZone.setFill(Color.rgb(0, 200, 0, 0.1));
    overlay.getChildren().add(greenZone);

    // 黄色带 (24 ~ 28)
    double yellowTop1 = (yAxis.getUpperBound() - 28) / totalRange * plotHeight;
    double yellowHeight1 = (28 - 24) / totalRange * plotHeight;
    Rectangle yellowZone = new Rectangle(0, yellowTop1, plotArea.getLayoutBounds().getWidth(), yellowHeight1);
    yellowZone.setFill(Color.rgb(255, 200, 0, 0.15));
    overlay.getChildren().add(yellowZone);

    // 红色带 (>=28)
    double redTop = 0;
    double redHeight = (yAxis.getUpperBound() - 28) / totalRange * plotHeight;
    Rectangle redZone = new Rectangle(0, redTop, plotArea.getLayoutBounds().getWidth(), redHeight);
    redZone.setFill(Color.rgb(255, 0, 0, 0.1));
    overlay.getChildren().add(redZone);

    // 闪烁红点标记预测位置
    Circle predictedDot = new Circle(5, Color.RED);
    // 添加闪烁动画 (Timeline 交替显示/隐藏)
    Timeline flicker = new Timeline(
        new KeyFrame(Duration.seconds(0.5), e -> predictedDot.setVisible(!predictedDot.isVisible()))
    );
    flicker.setCycleCount(Timeline.INDEFINITE);
    flicker.play();

    // 计算预测点 X 位置：最后一列 + 1
    double predX = (dataCount + 1) / (double) dataCount * plotArea.getLayoutBounds().getWidth();
    // 计算预测点 Y 位置
    double predY = (yAxis.getUpperBound() - predictedBmi) / totalRange * plotHeight;
    predictedDot.setCenterX(predX);
    predictedDot.setCenterY(predY);
    overlay.getChildren().add(predictedDot);
}
```

#### 预警文字
计算预测值所在区间，如果落在黄色或红色带，显示加粗红色文字：
```java
if (predictedBmi >= 28) {
    lblWarning.setText("⚠ 预警：7天后将进入【肥胖】区间！");
} else if (predictedBmi >= 24) {
    lblWarning.setText("⚠ 预警：7天后将进入【超重】区间！");
} else if (predictedBmi < 18.5) {
    lblWarning.setText("⚠ 预警：7天后将进入【偏瘦】区间！");
}
lblWarning.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
```

---

### 组件 ③ 模拟"控制线"对比

**需求**：在图上画两条线
- **真实线**：实线（历史）+ 虚线（预测段）
- **理想线**：从当前 BMI 水平延伸的平直线

**实现**：

```java
// 真实历史线（实线）
XYChart.Series<Number, Number> realSeries = new XYChart.Series<>();
realSeries.setName("真实 BMI");
for (int i = 0; i < bmiValues.size(); i++) {
    realSeries.getData().add(new XYChart.Data<>(i, bmiValues.get(i)));
}

// 预测连接线（虚线 + 样式区分）
XYChart.Series<Number, Number> predSeries = new XYChart.Series<>();
predSeries.setName("预测趋势");
// 从最后一点到预测点
double lastBmi = bmiValues.get(bmiValues.size() - 1);
predSeries.getData().add(new XYChart.Data<>(bmiValues.size() - 1, lastBmi));
predSeries.getData().add(new XYChart.Data<>(bmiValues.size() + 6, predictedBmi));
// 设置虚线样式通过 CSS

// 理想平直线（当前 BMI 水平延伸）
XYChart.Series<Number, Number> idealSeries = new XYChart.Series<>();
idealSeries.setName("理想控制线");
idealSeries.getData().add(new XYChart.Data<>(0, lastBmi));
idealSeries.getData().add(new XYChart.Data<>(bmiValues.size() + 6, lastBmi));

bmiChart.getData().addAll(realSeries, predSeries, idealSeries);
```

使用 CSS 设置虚线样式：
```css
.default-color1.chart-series-line { -fx-stroke-dash-array: 8 4; }  /* 预测线虚线 */
.default-color2.chart-series-line { -fx-stroke-dash-array: 4 4; }  /* 理想线虚线 */
```

---

### 组件 ④ 历史极值标注

**需求**：红色圆点标最高 BMI（附带日期），绿色圆点标最低 BMI（附带日期）。

**实现**：在折线图中每个 Data 点添加自定义 Node。

```java
private void markExtremes(XYChart.Series<Number, Number> series,
                           List<Double> values, List<String> dates) {
    // 找极值
    int maxIdx = 0, minIdx = 0;
    double maxVal = values.get(0), minVal = values.get(0);
    for (int i = 1; i < values.size(); i++) {
        if (values.get(i) > maxVal) { maxVal = values.get(i); maxIdx = i; }
        if (values.get(i) < minVal) { minVal = values.get(i); minIdx = i; }
    }

    // 给每个 Data 点设置 node（只有极值点有特殊样式）
    for (int i = 0; i < series.getData().size(); i++) {
        XYChart.Data<Number, Number> data = series.getData().get(i);
        if (i == maxIdx) {
            Circle dot = new Circle(6, Color.RED);
            Tooltip.install(dot, new Tooltip(String.format("最高 BMI: %.1f (%s)", maxVal, dates.get(i))));
            data.setNode(dot);
        } else if (i == minIdx) {
            Circle dot = new Circle(6, Color.GREEN);
            Tooltip.install(dot, new Tooltip(String.format("最低 BMI: %.1f (%s)", minVal, dates.get(i))));
            data.setNode(dot);
        }
    }
}
```

---

## 通用约束

- 所有数据库操作包裹 try-catch，异常时弹窗中文提示
- 预测计算用 `double` 类型，避免整数除法精度丢失
- 除数为零检查：`denominator == 0` 时提示"数据不足以拟合趋势线"
- 按钮状态随数据量动态更新：监听 bmi_records 变化

## 验证清单

- [ ] < 4 条记录时按钮置灰，tooltip 正确显示
- [ ] >= 4 条时按钮可用，点击后正确计算回归方程
- [ ] 回归方程公式和斜率含义文字正确显示
- [ ] 折线图上有绿/黄/红色块背景
- [ ] 预测 7 天后的 BMI 值用闪烁红点标记
- [ ] 预测值落在黄色/红色区间时，预警文字变红加粗
- [ ] 图表上有"真实线"和"理想控制线"两条线
- [ ] 历史最高 BMI 用红色圆点标注，最低用绿色圆点
- [ ] 鼠标悬停极值点显示日期和 BMI 值
