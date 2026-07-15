package com.bmitracker.controller;

import com.bmitracker.BMIApplication;
import com.bmitracker.model.BmiRecord;
import com.bmitracker.service.BmiService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.format.DateTimeFormatter;
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
                    r.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });

        loadData();
    }

    private void loadData() {
        List<BmiRecord> records = bmiService.getRecordsDesc(BMIApplication.currentUserId);
        if (records != null && !records.isEmpty()) {
            tableView.getItems().setAll(records);
            infoLabel.setText("");
        } else {
            infoLabel.setText("暂无记录");
        }
    }
}
