package com.bmitracker.model;

import java.time.LocalDateTime;

public class Recommendation {
    private int recId;
    private int userId;
    private String breakfast;
    private String lunch;
    private String dinner;
    private String totalCal;
    private LocalDateTime createTime;

    public Recommendation() {}

    public Recommendation(int userId, String breakfast, String lunch, String dinner, String totalCal) {
        this.userId = userId;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
        this.totalCal = totalCal;
    }

    public int getRecId() { return recId; }
    public void setRecId(int recId) { this.recId = recId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getBreakfast() { return breakfast; }
    public void setBreakfast(String breakfast) { this.breakfast = breakfast; }
    public String getLunch() { return lunch; }
    public void setLunch(String lunch) { this.lunch = lunch; }
    public String getDinner() { return dinner; }
    public void setDinner(String dinner) { this.dinner = dinner; }
    public String getTotalCal() { return totalCal; }
    public void setTotalCal(String totalCal) { this.totalCal = totalCal; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
