package com.bmitracker.service;

import com.bmitracker.dao.FoodDao;
import com.bmitracker.model.Food;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class FoodServiceImpl implements FoodService {

    private final FoodDao foodDao = new FoodDao();

    @Override
    public List<Food> getAllFoods() {
        return getTopByCategory(null);
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
        try {
            return foodDao.findAllCategories();
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Food> getTopByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return getAllFoods();
        }
        return getFoodsByCategory(category);
    }
}
