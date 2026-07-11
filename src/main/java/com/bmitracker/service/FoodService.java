package com.bmitracker.service;

import com.bmitracker.dao.FoodDao;
import com.bmitracker.model.Food;
import java.sql.SQLException;
import java.util.List;

public class FoodService {

    private final FoodDao foodDao = new FoodDao();

    public List<Food> getTopByCategory(String category) {
        try {
            return foodDao.findByCategoryOrderByCalories(category);
        } catch (SQLException e) {
            return null;
        }
    }

    public List<Food> getFoodsByIds(List<Integer> ids) {
        try {
            return foodDao.findByIds(ids);
        } catch (SQLException e) {
            return null;
        }
    }

    public List<String> getAllCategories() {
        try {
            return foodDao.findAllCategories();
        } catch (SQLException e) {
            return null;
        }
    }
}
