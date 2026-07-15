package com.bmitracker.service;

import com.bmitracker.dao.FoodDao;
import com.bmitracker.model.Food;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class FoodServiceImpl implements FoodService {

    private final FoodDao foodDao = new FoodDao();

    // 传 null 等于查全部，复用 findFiltered 省个 DAO 方法
    @Override
    public List<Food> getAllFoods() {
        try {
            return foodDao.findFiltered(null, null, null, null, null, null);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Food> getFoodsByCategory(String category) {
        try {
            return foodDao.findByCategoryOrderByCalories(category);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getAllCategories() {
        try { return foodDao.findAllCategories(); } catch (SQLException e) { return Collections.emptyList(); }
    }

    // 按热量排序返回，category 为空就直接返回全部食品
    @Override
    public List<Food> getTopByCategory(String category) {
        if (category == null || category.isEmpty()) return getAllFoods();
        return getFoodsByCategory(category);
    }

    @Override
    public List<String> getAllMealTypes() {
        try { return foodDao.findAllMealTypes(); } catch (SQLException e) { return Collections.emptyList(); }
    }

    @Override
    public List<String> getAllTextures() {
        try { return foodDao.findAllTextures(); } catch (SQLException e) { return Collections.emptyList(); }
    }

    @Override
    public List<String> getAllFlavors() {
        try { return foodDao.findAllFlavors(); } catch (SQLException e) { return Collections.emptyList(); }
    }

    @Override
    public List<String> getAllStorages() {
        try { return foodDao.findAllStorages(); } catch (SQLException e) { return Collections.emptyList(); }
    }

    @Override
    public List<String> getAllCookingMethods() {
        try { return foodDao.findAllCookingMethods(); } catch (SQLException e) { return Collections.emptyList(); }
    }

    @Override
    public List<Food> findFiltered(String category, String mealType, String texture, String flavor, String storage, String cooking) {
        try {
            return foodDao.findFiltered(category, mealType, texture, flavor, storage, cooking);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }
}
