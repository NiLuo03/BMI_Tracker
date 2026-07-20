package com.bmitracker.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class MealRecord {
    private final IntegerProperty recordId = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final IntegerProperty foodId = new SimpleIntegerProperty();
    private final StringProperty mealType = new SimpleStringProperty();
    private final DoubleProperty grams = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> recordDate = new SimpleObjectProperty<>();

    private String foodName;
    private double foodCalories;
    private double foodProtein;
    private double foodFat;
    private double foodCarb;

    public MealRecord() {}

    public int getRecordId() { return recordId.get(); }
    public IntegerProperty recordIdProperty() { return recordId; }
    public void setRecordId(int v) { recordId.set(v); }

    public int getUserId() { return userId.get(); }
    public IntegerProperty userIdProperty() { return userId; }
    public void setUserId(int v) { userId.set(v); }

    public int getFoodId() { return foodId.get(); }
    public IntegerProperty foodIdProperty() { return foodId; }
    public void setFoodId(int v) { foodId.set(v); }

    public String getMealType() { return mealType.get(); }
    public StringProperty mealTypeProperty() { return mealType; }
    public void setMealType(String v) { mealType.set(v); }

    public double getGrams() { return grams.get(); }
    public DoubleProperty gramsProperty() { return grams; }
    public void setGrams(double v) { grams.set(v); }

    public LocalDate getRecordDate() { return recordDate.get(); }
    public ObjectProperty<LocalDate> recordDateProperty() { return recordDate; }
    public void setRecordDate(LocalDate v) { recordDate.set(v); }

    public String getFoodName() { return foodName; }
    public void setFoodName(String v) { foodName = v; }

    public double getFoodCalories() { return foodCalories; }
    public void setFoodCalories(double v) { foodCalories = v; }

    public double getFoodProtein() { return foodProtein; }
    public void setFoodProtein(double v) { foodProtein = v; }

    public double getFoodFat() { return foodFat; }
    public void setFoodFat(double v) { foodFat = v; }

    public double getFoodCarb() { return foodCarb; }
    public void setFoodCarb(double v) { foodCarb = v; }

    public double getCalories() { return foodCalories * getGrams() / 100.0; }
    public double getProtein() { return foodProtein * getGrams() / 100.0; }
    public double getFat() { return foodFat * getGrams() / 100.0; }
    public double getCarb() { return foodCarb * getGrams() / 100.0; }
}
