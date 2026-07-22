package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.BmiRecord;
import com.bmitracker.service.BmiService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.Comparator;
import java.util.List;

public class HistoryController {

    // 预览卡片
    @FXML private VBox chartCard, tableCard;
    @FXML private VBox chartDetail, tableDetail;
    @FXML private LineChart<Number, Number> previewChart, chart;
    @FXML private TableView<BmiRecord> previewTable, tableView;
    @FXML private TableColumn<BmiRecord, Integer> colId, preColId;
    @FXML private TableColumn<BmiRecord, Double> colHeight, colWeight, colBmi;
    @FXML private TableColumn<BmiRecord, String> colStatus, colDate;
    @FXML private TableColumn<BmiRecord, Double> preColBmi;
    @FXML private TableColumn<BmiRecord, String> preColDate;
    @FXML private Label infoLabel, chartAnalysis, tableAnalysis, chartFooter, tableFooter;

    private final BmiService bmiService = new BmiService();

    @FXML
    void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("recordId"));
        colHeight.setCellValueFactory(new PropertyValueFactory<>("height"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colBmi.setCellValueFactory(new PropertyValueFactory<>("bmi"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(cell -> {
            BmiRecord r = cell.getValue();
            if (r.getCreateTime() == null) return null;
            return new javafx.beans.property.SimpleStringProperty(
                    r.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });
        preColId.setCellValueFactory(new PropertyValueFactory<>("recordId"));
        preColBmi.setCellValueFactory(new PropertyValueFactory<>("bmi"));
        preColDate.setCellValueFactory(cell -> {
            BmiRecord r = cell.getValue();
            if (r.getCreateTime() == null) return null;
            return new javafx.beans.property.SimpleStringProperty(
                    r.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
        });
        loadPreviews();
    }

    private void loadPreviews() {
        new Thread(() -> {
            List<BmiRecord> records = bmiService.getRecordsDesc(BMIApplication.currentUserId);
            javafx.application.Platform.runLater(() -> {
                if (records == null || records.isEmpty()) {
                    chartAnalysis.setText("暂无数据，快去记录您的第一条数据吧");
                    tableAnalysis.setText("暂无记录");
                    return;
                }
                List<BmiRecord> asc = new java.util.ArrayList<>(records);
                asc.sort(Comparator.comparing(BmiRecord::getCreateTime));
                // 分析趋势
                double first = asc.get(0).getBmi();
                double last = asc.get(asc.size() - 1).getBmi();
                double diff = last - first;
                String trend;
                if (Math.abs(diff) < 0.3) trend = "保持平稳";
                else if (diff > 0) trend = "上升 " + String.format("%.1f", diff) + "，请注意控制";
                else trend = "下降 " + String.format("%.1f", Math.abs(diff)) + "，继续保持";

                BmiRecord latest = asc.get(asc.size() - 1);
                String latestBmi = String.format("%.1f", latest.getBmi());
                String latestDate = latest.getCreateTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("MM月dd日"));

                chartAnalysis.setText(String.format("共 %d 条记录 · 最近 %s BMI %.1f\n趋势：%s",
                    records.size(), latestDate, latest.getBmi(), trend));
                chartFooter.setText(String.format("%.1f → %.1f", first, last));

                // 预览图表
                XYChart.Series<Number, Number> s = new XYChart.Series<>();
                for (int i = 0; i < asc.size(); i++) s.getData().add(new XYChart.Data<>(i, asc.get(i).getBmi()));
                previewChart.setData(FXCollections.singletonObservableList(s));

                // 分析表格数据
                double max = asc.stream().mapToDouble(BmiRecord::getBmi).max().orElse(0);
                double min = asc.stream().mapToDouble(BmiRecord::getBmi).min().orElse(0);
                double avg = asc.stream().mapToDouble(BmiRecord::getBmi).average().orElse(0);
                tableAnalysis.setText(String.format("共 %d 条记录 · 最高 %.1f · 最低 %.1f · 平均 %.1f",
                    records.size(), max, min, avg));
                tableFooter.setText("点击查看全部");

                // 预览表格(最近5条)
                previewTable.setItems(FXCollections.observableArrayList(
                    records.size() > 5 ? records.subList(0, 5) : records));
            });
        }).start();
    }

    @FXML
    void openChartDetail(MouseEvent e) {
        chartCard.setVisible(false); chartCard.setManaged(false);
        tableCard.setVisible(false); tableCard.setManaged(false);
        chartDetail.setVisible(true); chartDetail.setManaged(true);
        new Thread(() -> {
            List<BmiRecord> records = bmiService.getRecordsAsc(BMIApplication.currentUserId);
            javafx.application.Platform.runLater(() -> {
                if (records == null || records.size() < 2) return;
                records.sort(Comparator.comparing(BmiRecord::getCreateTime));
                XYChart.Series<Number, Number> s = new XYChart.Series<>();
                s.setName("BMI");
                for (int i = 0; i < records.size(); i++) s.getData().add(new XYChart.Data<>(i + 1, records.get(i).getBmi()));
                chart.setData(FXCollections.singletonObservableList(s));
            });
        }).start();
    }

    @FXML
    void openTableDetail(MouseEvent e) {
        chartCard.setVisible(false); chartCard.setManaged(false);
        tableCard.setVisible(false); tableCard.setManaged(false);
        tableDetail.setVisible(true); tableDetail.setManaged(true);
        new Thread(() -> {
            List<BmiRecord> records = bmiService.getRecordsDesc(BMIApplication.currentUserId);
            javafx.application.Platform.runLater(() -> {
                if (records != null) tableView.setItems(FXCollections.observableArrayList(records));
            });
        }).start();
    }

    @FXML
    void closeDetail() {
        chartDetail.setVisible(false); chartDetail.setManaged(false);
        tableDetail.setVisible(false); tableDetail.setManaged(false);
        chartCard.setVisible(true); chartCard.setManaged(true);
        tableCard.setVisible(true); tableCard.setManaged(true);
    }
}
