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
    private final StringProperty mealType = new SimpleStringProperty();
    private final StringProperty foodTexture = new SimpleStringProperty();
    private final StringProperty flavor = new SimpleStringProperty();
    private final StringProperty storage = new SimpleStringProperty();
    private final StringProperty cookingMethod = new SimpleStringProperty();
    private final StringProperty image = new SimpleStringProperty();
    private final StringProperty servingDesc = new SimpleStringProperty();

    public Food() {}

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

    public String getMealType() { return mealType.get(); }
    public StringProperty mealTypeProperty() { return mealType; }
    public void setMealType(String v) { mealType.set(v); }

    public String getFoodTexture() { return foodTexture.get(); }
    public StringProperty foodTextureProperty() { return foodTexture; }
    public void setFoodTexture(String v) { foodTexture.set(v); }

    public String getFlavor() { return flavor.get(); }
    public StringProperty flavorProperty() { return flavor; }
    public void setFlavor(String v) { flavor.set(v); }

    public String getStorage() { return storage.get(); }
    public StringProperty storageProperty() { return storage; }
    public void setStorage(String v) { storage.set(v); }

    public String getCookingMethod() { return cookingMethod.get(); }
    public StringProperty cookingMethodProperty() { return cookingMethod; }
    public void setCookingMethod(String v) { cookingMethod.set(v); }

    public String getImage() { return image.get(); }
    public StringProperty imageProperty() { return image; }
    public void setImage(String v) { image.set(v); }

    public String getServingDesc() { return servingDesc.get(); }
    public StringProperty servingDescProperty() { return servingDesc; }
    public void setServingDesc(String v) { servingDesc.set(v); }

    @Override
    public String toString() {
        return getFoodName();
    }
}
