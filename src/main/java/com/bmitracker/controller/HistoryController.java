package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.BmiRecord;
import com.bmitracker.service.BmiService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import java.util.Comparator;
import java.util.List;

public class HistoryController {

    @FXML private TableView<BmiRecord> tableView;
    @FXML private TableColumn<BmiRecord, Integer> colId;
    @FXML private TableColumn<BmiRecord, Double> colHeight;
    @FXML private TableColumn<BmiRecord, Double> colWeight;
    @FXML private TableColumn<BmiRecord, Double> colBmi;
    @FXML private TableColumn<BmiRecord, String> colStatus;
    @FXML private TableColumn<BmiRecord, String> colDate;
    @FXML private Label infoLabel;
    @FXML private LineChart<Number, Number> chart;
    @FXML private Label chartHint, tableHint;
    @FXML private Label chartTitle, tableTitle;

    private final BmiService bmiService = new BmiService();
    private boolean chartOpen, tableOpen;

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
        new Thread(this::loadData).start();
    }

    private void loadData() {
        List<BmiRecord> records = bmiService.getRecordsDesc(BMIApplication.currentUserId);
        javafx.application.Platform.runLater(() -> {
            if (records == null || records.isEmpty()) {
                if (infoLabel != null) infoLabel.setText("暂无记录");
                return;
            }
            if (infoLabel != null) infoLabel.setText("共 " + records.size() + " 条记录");
        });
    }

    @FXML
    void toggleChart(MouseEvent e) {
        chartOpen = !chartOpen;
        if (chartOpen) {
            chartHint.setVisible(false);
            chartHint.setManaged(false);
            chart.setVisible(true);
            chart.setManaged(true);
            chartTitle.setText("BMI 变化趋势（已展开）");
            loadChart();
        } else {
            chartHint.setVisible(true);
            chartHint.setManaged(true);
            chart.setVisible(false);
            chart.setManaged(false);
            chartTitle.setText("BMI 变化趋势");
        }
    }

    @FXML
    void toggleTable(MouseEvent e) {
        tableOpen = !tableOpen;
        if (tableOpen) {
            tableHint.setVisible(false);
            tableHint.setManaged(false);
            tableView.setVisible(true);
            tableView.setManaged(true);
            tableTitle.setText("历史记录（已展开）");
            loadTable();
        } else {
            tableHint.setVisible(true);
            tableHint.setManaged(true);
            tableView.setVisible(false);
            tableView.setManaged(false);
            tableTitle.setText("历史记录");
        }
    }

    private void loadChart() {
        List<BmiRecord> records = bmiService.getRecordsAsc(BMIApplication.currentUserId);
        if (records == null || records.size() < 2) {
            chartHint.setText("至少需要2条记录");
            chartHint.setManaged(true);
            return;
        }
        records.sort(Comparator.comparing(BmiRecord::getCreateTime));
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("BMI");
        for (int i = 0; i < records.size(); i++) {
            series.getData().add(new XYChart.Data<>(i + 1, records.get(i).getBmi()));
        }
        chart.setData(FXCollections.singletonObservableList(series));
    }

    private void loadTable() {
        List<BmiRecord> records = bmiService.getRecordsDesc(BMIApplication.currentUserId);
        if (records != null) {
            tableView.setItems(FXCollections.observableArrayList(records));
        }
    }
}
