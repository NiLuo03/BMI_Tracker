package com.bmitracker.controller;

import com.bmitracker.algorithm.LinearRegression;
import com.bmitracker.model.BmiRecord;
import com.bmitracker.service.BmiRecordService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class TrendPredictionController implements Initializable {

    @FXML private LineChart<Number, Number> trendChart;
    @FXML private Label equationLabel;
    @FXML private Label slopeLabel;
    @FXML private Label predictionLabel;
    @FXML private Label extremeLabel;

    private BmiRecordService bmiRecordService;
    private int currentUserId;
    private LinearRegression regression;

    public void setBmiRecordService(BmiRecordService bmiRecordService) {
        this.bmiRecordService = bmiRecordService;
    }

    public void setUserId(int userId) {
        this.currentUserId = userId;
        loadAndPredict();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        trendChart.setAnimated(false);
        trendChart.setCreateSymbols(true);
        trendChart.setLegendVisible(true);

        NumberAxis xAxis = (NumberAxis) trendChart.getXAxis();
        xAxis.setLabel("天数");
        xAxis.setForceZeroInRange(false);

        NumberAxis yAxis = (NumberAxis) trendChart.getYAxis();
        yAxis.setLabel("BMI");
        yAxis.setForceZeroInRange(false);
    }

    public void loadAndPredict() {
        if (bmiRecordService == null || currentUserId <= 0) return;
        try {
            List<BmiRecord> records = bmiRecordService.getRecordsByUserId(currentUserId);
            records.sort(Comparator.comparing(BmiRecord::getCreateTime));

            if (records.size() < 2) {
                equationLabel.setText("暂无足够数据（至少需要2条记录）");
                trendChart.setData(FXCollections.observableArrayList());
                return;
            }

            regression = new LinearRegression();
            regression.fit(records);

            XYChart.Series<Number, Number> historySeries = buildHistorySeries(records);
            XYChart.Series<Number, Number> regressionSeries = buildRegressionSeries(records);
            XYChart.Series<Number, Number> idealLineSeries = buildIdealLine(records);
            XYChart.Series<Number, Number> predictionSeries = buildPredictionPoint(records);
            XYChart.Series<Number, Number> extremeSeries = buildExtremePoints(records);

            trendChart.setData(FXCollections.observableArrayList(
                    historySeries, regressionSeries, idealLineSeries, predictionSeries, extremeSeries
            ));

            equationLabel.setText("回归方程: " + regression.getEquation());
            slopeLabel.setText(regression.getSlopeInterpretation());

            double nextBmi = regression.predictNext(7);
            predictionLabel.setText(String.format("7天后预测 BMI: %.2f", nextBmi));

            double minBmi = records.stream().mapToDouble(BmiRecord::getBmi).min().orElse(0);
            double maxBmi = records.stream().mapToDouble(BmiRecord::getBmi).max().orElse(0);
            extremeLabel.setText(String.format("最高 BMI: %.2f  /  最低 BMI: %.2f", maxBmi, minBmi));

        } catch (Exception e) {
            showAlert("预测失败", "无法进行趋势预测：" + e.getMessage());
        }
    }

    private XYChart.Series<Number, Number> buildHistorySeries(List<BmiRecord> records) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("BMI历史数据");
        for (int i = 0; i < records.size(); i++) {
            series.getData().add(new XYChart.Data<>(i + 1, records.get(i).getBmi()));
        }
        return series;
    }

    private XYChart.Series<Number, Number> buildRegressionSeries(List<BmiRecord> records) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("回归趋势线");
        int n = records.size();
        series.getData().add(new XYChart.Data<>(1, regression.predict(1)));
        series.getData().add(new XYChart.Data<>(n, regression.predict(n)));
        return series;
    }

    private XYChart.Series<Number, Number> buildIdealLine(List<BmiRecord> records) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("当前水平线");
        double currentBmi = records.get(records.size() - 1).getBmi();
        int n = records.size() + 7;
        series.getData().add(new XYChart.Data<>(1, currentBmi));
        series.getData().add(new XYChart.Data<>(n, currentBmi));
        return series;
    }

    private XYChart.Series<Number, Number> buildPredictionPoint(List<BmiRecord> records) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("7天后预测");
        int n = records.size();
        double pred7 = regression.predictNext(7);
        series.getData().add(new XYChart.Data<>(n + 7, pred7));
        return series;
    }

    private XYChart.Series<Number, Number> buildExtremePoints(List<BmiRecord> records) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("极值标注");

        int minIdx = 0, maxIdx = 0;
        for (int i = 1; i < records.size(); i++) {
            if (records.get(i).getBmi() < records.get(minIdx).getBmi()) minIdx = i;
            if (records.get(i).getBmi() > records.get(maxIdx).getBmi()) maxIdx = i;
        }

        series.getData().add(new XYChart.Data<>(minIdx + 1, records.get(minIdx).getBmi()));
        series.getData().add(new XYChart.Data<>(maxIdx + 1, records.get(maxIdx).getBmi()));

        return series;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
