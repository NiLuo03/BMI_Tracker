package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.BmiRecord;
import com.bmitracker.service.BmiService;
import com.bmitracker.util.LinearRegression;
import javafx.application.Platform;
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
        chart.setVisible(false);
        infoLabel.setText("加载中…");

        new Thread(() -> {
            List<BmiRecord> records = bmiService.getRecordsAsc(BMIApplication.currentUserId);
            Platform.runLater(() -> {
                if (records == null || records.size() < 2) {
                    infoLabel.setText("暂无足够数据绘制折线图（至少需要2条记录）");
                    return;
                }
                chart.setVisible(true);
                infoLabel.setText("");

                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName("BMI历史记录");
                for (int i = 0; i < records.size(); i++) {
                    series.getData().add(new XYChart.Data<>(i, records.get(i).getBmi()));
                }
                chart.getData().add(series);
            });
        }).start();
    }
}
