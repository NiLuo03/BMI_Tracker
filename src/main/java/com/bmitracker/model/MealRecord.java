package com.bmitracker.model;

import java.time.LocalDate;

public class MealRecord {
    private int recordId;
    private int userId;
    private String mealType;
    private int foodId;
    private double grams;
    private LocalDate recordDate;
    private String foodName;
    private double calories;

    public int getRecordId() { return recordId; }
    public void setRecordId(int v) { recordId = v; }
    public int getUserId() { return userId; }
    public void setUserId(int v) { userId = v; }
    public String getMealType() { return mealType; }
    public void setMealType(String v) { mealType = v; }
    public int getFoodId() { return foodId; }
    public void setFoodId(int v) { foodId = v; }
    public double getGrams() { return grams; }
    public void setGrams(double v) { grams = v; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate v) { recordDate = v; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String v) { foodName = v; }
    public double getCalories() { return calories; }
    public void setCalories(double v) { calories = v; }
}
