package com.bmitracker.service;

import com.bmitracker.model.Food;
import java.util.List;

public interface FoodService {
    // 查询全部食品
    List<Food> getAllFoods();
    List<Food> getFoodsByCategory(String category);
    List<String> getAllCategories();
    // 取分类下最低卡的 Top 推荐
    List<Food> getTopByCategory(String category);

    List<String> getAllMealTypes();
    List<String> getAllTextures();
    List<String> getAllFlavors();
    List<String> getAllStorages();
    List<String> getAllCookingMethods();
    // 多条件筛选食品
    List<Food> findFiltered(String category, String mealType, String texture, String flavor, String storage, String cooking);
}
