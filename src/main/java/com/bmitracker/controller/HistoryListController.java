package com.bmitracker.controller;

import com.bmitracker.model.BmiRecord;
import com.bmitracker.service.BmiRecordService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class HistoryListController implements Initializable {

    @FXML private TableView<BmiRecord> historyTable;
    @FXML private TableColumn<BmiRecord, String> dateColumn;
    @FXML private TableColumn<BmiRecord, Number> heightColumn;
    @FXML private TableColumn<BmiRecord, Number> weightColumn;
    @FXML private TableColumn<BmiRecord, Number> bmiColumn;
    @FXML private TableColumn<BmiRecord, String> statusColumn;

    private BmiRecordService bmiRecordService;
    private int currentUserId;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void setBmiRecordService(BmiRecordService bmiRecordService) {
        this.bmiRecordService = bmiRecordService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().createTimeProperty().asString(DATE_FMT));
        heightColumn.setCellValueFactory(new PropertyValueFactory<>("height"));
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));
        bmiColumn.setCellValueFactory(new PropertyValueFactory<>("bmi"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                setStyle(switch (item) {
                    case "偏瘦" -> "-fx-text-fill: #f0ad4e; -fx-font-weight: bold;";
                    case "正常" -> "-fx-text-fill: #5cb85c; -fx-font-weight: bold;";
                    case "超重" -> "-fx-text-fill: #f0ad4e; -fx-font-weight: bold;";
                    case "肥胖" -> "-fx-text-fill: #d9534f; -fx-font-weight: bold;";
                    default -> "";
                });
            }
        });
    }

    public void setUserId(int userId) {
        this.currentUserId = userId;
        loadRecords();
    }

    public void loadRecords() {
        if (bmiRecordService == null || currentUserId <= 0) return;
        try {
            List<BmiRecord> records = bmiRecordService.getRecordsByUserId(currentUserId);
            records.sort(Comparator.comparing(BmiRecord::getCreateTime).reversed());
            historyTable.setItems(FXCollections.observableArrayList(records));
        } catch (Exception e) {
            showAlert("加载失败", "无法加载历史记录：" + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
