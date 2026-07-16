package com.bmitracker.service;

import com.bmitracker.model.Food;
import java.util.List;

public interface FoodService {
    List<Food> getAllFoods();
    List<Food> getFoodsByCategory(String category);
    List<String> getAllCategories();
    List<Food> getTopByCategory(String category);
    List<Food> getRankByCategoryAndNutrient(String category, String nutrient, boolean asc);

    List<String> getAllMealTypes();
    List<String> getAllTextures();
    List<String> getAllFlavors();
    List<String> getAllStorages();
    List<String> getAllCookingMethods();
    List<Food> findFiltered(String category, String mealType, String texture, String flavor, String storage, String cooking);
}
