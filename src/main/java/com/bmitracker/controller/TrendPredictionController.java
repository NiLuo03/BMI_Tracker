package com.bmitracker.controller;

import com.bmitracker.algorithm.LinearRegression;
import com.bmitracker.model.BmiRecord;
import com.bmitracker.service.BmiRecordService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class TrendPredictionController implements Initializable {

    private static final double BMI_UNDERWEIGHT = 18.5;
    private static final double BMI_NORMAL = 24.0;
    private static final double BMI_OVERWEIGHT = 28.0;

    @FXML private StackPane chartContainer;
    @FXML private LineChart<Number, Number> trendChart;
    @FXML private Label equationLabel;
    @FXML private Label slopeLabel;
    @FXML private Label predictionLabel;
    @FXML private Label extremeLabel;
    @FXML private Label rSquaredLabel;

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
        setupChart();
    }

    private void setupChart() {
        trendChart.setAnimated(false);
        trendChart.setCreateSymbols(true);
        trendChart.setLegendVisible(true);

        NumberAxis xAxis = (NumberAxis) trendChart.getXAxis();
        xAxis.setLabel("天数 (从第1条记录开始)");
        xAxis.setForceZeroInRange(false);
        xAxis.setTickLabelFont(javafx.scene.text.Font.font(11));

        NumberAxis yAxis = (NumberAxis) trendChart.getYAxis();
        yAxis.setLabel("BMI");
        yAxis.setForceZeroInRange(false);
        yAxis.setLowerBound(10);
        yAxis.setUpperBound(40);
        yAxis.setTickUnit(2);
        yAxis.setAutoRanging(false);
    }

    public void loadAndPredict() {
        if (bmiRecordService == null || currentUserId <= 0) return;
        try {
            List<BmiRecord> records = bmiRecordService.getRecordsByUserId(currentUserId);
            records.sort(Comparator.comparing(BmiRecord::getCreateTime));

            int n = records.size();
            if (n < 4) {
                equationLabel.setText("暂无足够数据，至少需要4条记录才能预测");
                trendChart.setData(FXCollections.observableArrayList());
                return;
            }

            regression = new LinearRegression();
            regression.fit(records);

            addHealthZoneBands();

            ObservableList<XYChart.Series<Number, Number>> chartData = FXCollections.observableArrayList();
            chartData.add(buildHistorySeries(records));
            chartData.add(buildRegressionSeries(records));
            chartData.add(buildIdealLineSeries(records));
            chartData.add(buildPredictionPoint(records));
            chartData.add(buildExtremePoints(records));
            trendChart.setData(chartData);

            applyExtremePointStyles();

            equationLabel.setText("回归方程: " + regression.getEquation());
            rSquaredLabel.setText(regression.getRSquaredText());
            slopeLabel.setText(regression.getSlopeInterpretation());

            double nextBmi = regression.predictNext(7);
            predictionLabel.setText(String.format("7天后预测 BMI: %.2f  %s",
                    nextBmi, getBmiStatusEmoji(nextBmi)));

            double minBmi = Double.MAX_VALUE, maxBmi = Double.MIN_VALUE;
            int minIdx = 0, maxIdx = 0;
            for (int i = 0; i < n; i++) {
                double bmi = records.get(i).getBmi();
                if (bmi < minBmi) { minBmi = bmi; minIdx = i; }
                if (bmi > maxBmi) { maxBmi = bmi; maxIdx = i; }
            }
            double minDate = minIdx + 1;
            double maxDate = maxIdx + 1;
            extremeLabel.setText(String.format("最高 BMI: %.2f (第%.0f天)  /  最低 BMI: %.2f (第%.0f天)",
                    maxBmi, maxDate, minBmi, minDate));

        } catch (Exception e) {
            showAlert("预测失败", "无法进行趋势预测：" + e.getMessage());
        }
    }

    private void addHealthZoneBands() {
        Pane plotArea = (Pane) trendChart.lookup(".plot-content");
        if (plotArea == null) return;

        plotArea.getChildren().removeIf(node ->
                node instanceof Rectangle && "healthZone".equals(node.getId()));

        double chartWidth = plotArea.getWidth();
        if (chartWidth <= 0) chartWidth = 700;

        NumberAxis yAxis = (NumberAxis) trendChart.getYAxis();
        double yMin = yAxis.getLowerBound();
        double yMax = yAxis.getUpperBound();
        double yRange = yMax - yMin;
        if (yRange <= 0) return;

        double plotHeight = plotArea.getHeight();
        if (plotHeight <= 0) plotHeight = 400;

        double toPixel = plotHeight / yRange;

        double underweightTop = plotHeight - (BMI_UNDERWEIGHT - yMin) * toPixel;
        double normalTop = plotHeight - (BMI_NORMAL - yMin) * toPixel;
        double overweightTop = plotHeight - (BMI_OVERWEIGHT - yMin) * toPixel;

        Rectangle obeseZone = new Rectangle(0, 0, chartWidth, underweightTop);
        obeseZone.setId("healthZone");
        obeseZone.setFill(Color.rgb(255, 80, 80, 0.08));

        Rectangle overweightZone = new Rectangle(0, underweightTop, chartWidth, normalTop - underweightTop);
        overweightZone.setId("healthZone");
        overweightZone.setFill(Color.rgb(255, 200, 50, 0.08));

        Rectangle normalZone = new Rectangle(0, normalTop, chartWidth, overweightTop - normalTop);
        normalZone.setId("healthZone");
        normalZone.setFill(Color.rgb(80, 200, 80, 0.08));

        Rectangle underweightZone = new Rectangle(0, overweightTop, chartWidth, plotHeight - overweightTop);
        underweightZone.setId("healthZone");
        underweightZone.setFill(Color.rgb(80, 180, 255, 0.08));

        Runnable addBands = () -> {
            Pane p = (Pane) trendChart.lookup(".plot-content");
            if (p == null) return;
            double w = p.getWidth();
            double h = p.getHeight();
            if (w <= 0 || h <= 0) return;
            double yMin2 = yAxis.getLowerBound();
            double yMax2 = yAxis.getUpperBound();
            double range = yMax2 - yMin2;
            if (range <= 0) return;
            double px = h / range;
            double ut = h - (BMI_UNDERWEIGHT - yMin2) * px;
            double nt = h - (BMI_NORMAL - yMin2) * px;
            double ot = h - (BMI_OVERWEIGHT - yMin2) * px;

            p.getChildren().removeIf(node ->
                    node instanceof Rectangle && "healthZone".equals(node.getId()));

            Rectangle o = new Rectangle(0, 0, w, ut); o.setId("healthZone"); o.setFill(Color.rgb(220, 60, 60, 0.10));
            Rectangle ov = new Rectangle(0, ut, w, nt - ut); ov.setId("healthZone"); ov.setFill(Color.rgb(240, 180, 40, 0.10));
            Rectangle nz = new Rectangle(0, nt, w, ot - nt); nz.setId("healthZone"); nz.setFill(Color.rgb(60, 180, 60, 0.10));
            Rectangle uz = new Rectangle(0, ot, w, h - ot); uz.setId("healthZone"); uz.setFill(Color.rgb(60, 140, 220, 0.10));
            p.getChildren().addAll(o, ov, nz, uz);
        };

        trendChart.needsLayoutProperty().addListener((obs, ov, nv) -> {
            if (nv) addBands.run();
        });
        javafx.application.Platform.runLater(addBands);
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
        int totalPoints = n + 7;
        for (int i = 1; i <= totalPoints; i++) {
            series.getData().add(new XYChart.Data<>(i, regression.predict(i)));
        }
        return series;
    }

    private XYChart.Series<Number, Number> buildIdealLineSeries(List<BmiRecord> records) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("当前BMI水平线");
        double currentBmi = records.get(records.size() - 1).getBmi();
        int endX = records.size() + 7;
        series.getData().add(new XYChart.Data<>(1, currentBmi));
        series.getData().add(new XYChart.Data<>(endX, currentBmi));
        return series;
    }

    private XYChart.Series<Number, Number> buildPredictionPoint(List<BmiRecord> records) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("7天后预测值");
        int n = records.size();
        double pred7 = regression.predictNext(7);
        series.getData().add(new XYChart.Data<>(n + 7, pred7));
        return series;
    }

    private XYChart.Series<Number, Number> buildExtremePoints(List<BmiRecord> records) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("极值标注");

        int minIdx = 0, maxIdx = 0;
        double minBmi = records.get(0).getBmi();
        double maxBmi = records.get(0).getBmi();
        for (int i = 1; i < records.size(); i++) {
            double bmi = records.get(i).getBmi();
            if (bmi < minBmi) { minBmi = bmi; minIdx = i; }
            if (bmi > maxBmi) { maxBmi = bmi; maxIdx = i; }
        }

        series.getData().add(new XYChart.Data<>(minIdx + 1, minBmi));
        series.getData().add(new XYChart.Data<>(maxIdx + 1, maxBmi));
        return series;
    }

    @SuppressWarnings("unchecked")
    private void applyExtremePointStyles() {
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Series<Number, Number> s : trendChart.getData()) {
                if (!"极值标注".equals(s.getName())) continue;
                for (XYChart.Data<Number, Number> d : s.getData()) {
                    Node node = d.getNode();
                    if (node != null) {
                        node.setStyle("-fx-background-color: #ff4444, white; -fx-background-radius: 6px; -fx-padding: 6px;");
                        Tooltip tip = new Tooltip(String.format("%.2f  (第%.0f天)", d.getYValue().doubleValue(), d.getXValue().doubleValue()));
                        Tooltip.install(node, tip);
                    }
                }
            }
            for (XYChart.Series<Number, Number> s : trendChart.getData()) {
                if (!"7天后预测值".equals(s.getName())) continue;
                for (XYChart.Data<Number, Number> d : s.getData()) {
                    Node node = d.getNode();
                    if (node != null) {
                        node.setStyle("-fx-background-color: #ff6f00, white; -fx-background-radius: 8px; -fx-padding: 8px;");
                        Tooltip tip = new Tooltip(String.format("预测 BMI: %.2f", d.getYValue().doubleValue()));
                        Tooltip.install(node, tip);
                    }
                }
            }
        });
    }

    private String getBmiStatusEmoji(double bmi) {
        if (bmi < BMI_UNDERWEIGHT) return "(偏瘦)";
        if (bmi < BMI_NORMAL) return "(正常)";
        if (bmi < BMI_OVERWEIGHT) return "(超重)";
        return "(肥胖)";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
