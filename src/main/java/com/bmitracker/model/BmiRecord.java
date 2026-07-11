package com.bmitracker.model;

import java.time.LocalDateTime;

public class BmiRecord {
    private int recordId;
    private int userId;
    private double height;
    private double weight;
    private double bmi;
    private String status;
    private LocalDateTime createTime;

    public BmiRecord() {}

    public BmiRecord(int userId, double height, double weight, double bmi, String status) {
        this.userId = userId;
        this.height = height;
        this.weight = weight;
        this.bmi = bmi;
        this.status = status;
    }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
