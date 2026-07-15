package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.BmiRecord;
import com.bmitracker.service.BmiService;
import com.bmitracker.util.LinearRegression;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import java.util.List;

public class ChartController {

    @FXML private LineChart<Number, Number> chart;
    @FXML private Label infoLabel;

    private final BmiService bmiService = new BmiService();

    @FXML
    void initialize() {
        // 按时间升序获取用户 BMI 记录
        List<BmiRecord> records = bmiService.getRecordsAsc(BMIApplication.currentUserId);
        if (records == null || records.size() < 2) {
            // 不足两条时隐藏图表并提示
            chart.setVisible(false);
            infoLabel.setText("暂无足够数据绘制折线图（至少需要2条记录）");
            return;
        }
        chart.setVisible(true);
        infoLabel.setText("");

        // 以记录索引为 X 轴，BMI 值为 Y 轴构建折线序列
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("BMI历史记录");
        for (int i = 0; i < records.size(); i++) {
            series.getData().add(new XYChart.Data<>(i, records.get(i).getBmi()));
        }
        chart.getData().add(series);
    }
}
