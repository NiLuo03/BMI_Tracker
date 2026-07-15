package com.bmitracker.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// BMI 测量记录：每次记录的身高体重 BMI 值及健康评级
public class BmiRecord {
    private final IntegerProperty recordId = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final DoubleProperty height = new SimpleDoubleProperty();
    private final DoubleProperty weight = new SimpleDoubleProperty();
    private final DoubleProperty bmi = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createTime = new SimpleObjectProperty<>();

    public BmiRecord() {}

    public BmiRecord(int userId, double height, double weight, double bmi, String status) {
        this.userId.set(userId);
        this.height.set(height);
        this.weight.set(weight);
        this.bmi.set(bmi);
        this.status.set(status);
    }

    public BmiRecord(int recordId, int userId, double height, double weight, double bmi, String status, LocalDateTime createTime) {
        this.recordId.set(recordId);
        this.userId.set(userId);
        this.height.set(height);
        this.weight.set(weight);
        this.bmi.set(bmi);
        this.status.set(status);
        this.createTime.set(createTime);
    }

    public int getRecordId() { return recordId.get(); }
    public IntegerProperty recordIdProperty() { return recordId; }
    public void setRecordId(int recordId) { this.recordId.set(recordId); }

    public int getUserId() { return userId.get(); }
    public IntegerProperty userIdProperty() { return userId; }
    public void setUserId(int userId) { this.userId.set(userId); }

    public double getHeight() { return height.get(); }
    public DoubleProperty heightProperty() { return height; }
    public void setHeight(double height) { this.height.set(height); }

    public double getWeight() { return weight.get(); }
    public DoubleProperty weightProperty() { return weight; }
    public void setWeight(double weight) { this.weight.set(weight); }

    public double getBmi() { return bmi.get(); }
    public DoubleProperty bmiProperty() { return bmi; }
    public void setBmi(double bmi) { this.bmi.set(bmi); }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String status) { this.status.set(status); }

    public LocalDateTime getCreateTime() { return createTime.get(); }
    public ObjectProperty<LocalDateTime> createTimeProperty() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime.set(createTime); }

    public String getFormattedDate() {
        if (getCreateTime() == null) return "";
        return getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
