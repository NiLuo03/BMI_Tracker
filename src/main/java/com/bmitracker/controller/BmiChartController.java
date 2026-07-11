package com.bmitracker.controller;

import com.bmitracker.model.BmiRecord;
import com.bmitracker.service.BmiRecordService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class BmiChartController implements Initializable {

    @FXML private LineChart<String, Number> bmiChart;
    @FXML private Label hintLabel;

    private BmiRecordService bmiRecordService;
    private int currentUserId;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");

    public void setBmiRecordService(BmiRecordService bmiRecordService) {
        this.bmiRecordService = bmiRecordService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bmiChart.setAnimated(false);
        bmiChart.setCreateSymbols(true);
    }

    public void setUserId(int userId) {
        this.currentUserId = userId;
        loadChart();
    }

    public void loadChart() {
        if (bmiRecordService == null || currentUserId <= 0) return;
        try {
            List<BmiRecord> records = bmiRecordService.getRecordsByUserId(currentUserId);
            records.sort(Comparator.comparing(BmiRecord::getCreateTime));

            if (records.size() < 2) {
                hintLabel.setText("暂无足够数据绘制折线图（至少需要2条记录）");
                bmiChart.setData(FXCollections.observableArrayList());
                return;
            }

            hintLabel.setText("");

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("BMI变化");

            for (BmiRecord record : records) {
                String dateLabel = record.getCreateTime() != null
                        ? record.getCreateTime().format(DATE_FMT)
                        : "";
                series.getData().add(new XYChart.Data<>(dateLabel, record.getBmi()));
            }

            bmiChart.setData(FXCollections.singletonObservableList(series));
        } catch (Exception e) {
            showAlert("加载失败", "无法加载图表数据：" + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
