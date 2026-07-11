package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.BmiRecord;
import com.bmitracker.service.BmiService;
import com.bmitracker.util.LinearRegression;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.util.List;

public class PredictionController {

    @FXML private LineChart<Number, Number> chart;
    @FXML private Label equationLabel;
    @FXML private Label warningLabel;
    @FXML private Button predictBtn;
    @FXML private StackPane chartContainer;

    private final BmiService bmiService = new BmiService();

    @FXML
    void initialize() {
        int count = bmiService.getRecordCount(BMIApplication.currentUserId);
        if (count < 4) {
            predictBtn.setDisable(true);
            predictBtn.setTooltip(new javafx.scene.control.Tooltip("至少录入4次数据，趋势预测更准确哦~"));
        }
    }

    @FXML
    void handlePredict() {
        List<BmiRecord> records = bmiService.getRecordsAsc(BMIApplication.currentUserId);
        if (records == null || records.size() < 4) {
            showAlert("至少需要4条记录才能进行预测");
            return;
        }

        LinearRegression lr = new LinearRegression();
        lr.fit(records);
        int n = records.size();
        double futureBmi = lr.predictNextWeek(n);

        // 清除旧数据
        chart.getData().clear();

        // ① 真实历史 + 预测线
        XYChart.Series<Number, Number> historySeries = new XYChart.Series<>();
        historySeries.setName("真实值");
        for (int i = 0; i < n; i++) {
            historySeries.getData().add(new XYChart.Data<>(i, records.get(i).getBmi()));
        }
        // 预测延伸点
        historySeries.getData().add(new XYChart.Data<>(n + 6, futureBmi));
        chart.getData().add(historySeries);

        // ③ 理想平缓线（从当前 BMI 水平延伸）
        double currentBmi = records.get(n - 1).getBmi();
        XYChart.Series<Number, Number> idealSeries = new XYChart.Series<>();
        idealSeries.setName("理想平缓线");
        idealSeries.getData().add(new XYChart.Data<>(0, currentBmi));
        idealSeries.getData().add(new XYChart.Data<>(n + 6, currentBmi));
        chart.getData().add(idealSeries);

        // ① 回归方程展示
        double slope = lr.getSlope();
        String trend = slope >= 0 ? "上升" : "下降";
        equationLabel.setText(String.format("BMI = %.2f×天数 + %.2f      您的BMI平均每天%s %.4f",
                slope, lr.getIntercept(), trend, Math.abs(slope)));

        // ② 健康区间预警
        String status = bmiService.getHealthStatus(futureBmi);
        if (!"正常".equals(status)) {
            warningLabel.setText(String.format("⚠️ 预警：7天后将进入 [%s] 区间！当前预测值：%.1f", status, futureBmi));
            warningLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            warningLabel.setText(String.format("预测7天后BMI：%.1f，保持在正常区间", futureBmi));
            warningLabel.setStyle("-fx-text-fill: green;");
        }

        // ④ 历史极值标注
        double minBmi = Double.MAX_VALUE, maxBmi = Double.MIN_VALUE;
        int minIdx = 0, maxIdx = 0;
        for (int i = 0; i < n; i++) {
            double b = records.get(i).getBmi();
            if (b < minBmi) { minBmi = b; minIdx = i; }
            if (b > maxBmi) { maxBmi = b; maxIdx = i; }
        }
        XYChart.Series<Number, Number> minSeries = new XYChart.Series<>();
        minSeries.setName("最低BMI");
        minSeries.getData().add(new XYChart.Data<>(minIdx, minBmi));
        chart.getData().add(minSeries);

        XYChart.Series<Number, Number> maxSeries = new XYChart.Series<>();
        maxSeries.setName("最高BMI");
        maxSeries.getData().add(new XYChart.Data<>(maxIdx, maxBmi));
        chart.getData().add(maxSeries);

        chart.setCreateSymbols(true);
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("提示"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
