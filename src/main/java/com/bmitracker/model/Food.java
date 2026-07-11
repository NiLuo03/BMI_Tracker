package com.bmitracker.model;

public class Food {
    private int foodId;
    private String foodName;
    private String category;
    private double calories;
    private double protein;
    private double fat;
    private double carb;

    public Food() {}

    public int getFoodId() { return foodId; }
    public void setFoodId(int foodId) { this.foodId = foodId; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }
    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }
    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }
    public double getCarb() { return carb; }
    public void setCarb(double carb) { this.carb = carb; }
}
