package com.bmitracker.model;

import javafx.beans.property.*;

public class Food {
    private final IntegerProperty foodId = new SimpleIntegerProperty();
    private final StringProperty foodName = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final DoubleProperty calories = new SimpleDoubleProperty();
    private final DoubleProperty protein = new SimpleDoubleProperty();
    private final DoubleProperty fat = new SimpleDoubleProperty();
    private final DoubleProperty carb = new SimpleDoubleProperty();

    public Food() {}

    public Food(int foodId, String foodName, String category, double calories, double protein, double fat, double carb) {
        this.foodId.set(foodId);
        this.foodName.set(foodName);
        this.category.set(category);
        this.calories.set(calories);
        this.protein.set(protein);
        this.fat.set(fat);
        this.carb.set(carb);
    }

    public int getFoodId() { return foodId.get(); }
    public IntegerProperty foodIdProperty() { return foodId; }
    public void setFoodId(int foodId) { this.foodId.set(foodId); }

    public String getFoodName() { return foodName.get(); }
    public StringProperty foodNameProperty() { return foodName; }
    public void setFoodName(String foodName) { this.foodName.set(foodName); }

    public String getCategory() { return category.get(); }
    public StringProperty categoryProperty() { return category; }
    public void setCategory(String category) { this.category.set(category); }

    public double getCalories() { return calories.get(); }
    public DoubleProperty caloriesProperty() { return calories; }
    public void setCalories(double calories) { this.calories.set(calories); }

    public double getProtein() { return protein.get(); }
    public DoubleProperty proteinProperty() { return protein; }
    public void setProtein(double protein) { this.protein.set(protein); }

    public double getFat() { return fat.get(); }
    public DoubleProperty fatProperty() { return fat; }
    public void setFat(double fat) { this.fat.set(fat); }

    public double getCarb() { return carb.get(); }
    public DoubleProperty carbProperty() { return carb; }
    public void setCarb(double carb) { this.carb.set(carb); }

    @Override
    public String toString() {
        return getFoodName();
    }
}
