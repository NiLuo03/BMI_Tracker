package com.bmitracker.service;

import com.bmitracker.model.Food;
import java.util.List;

public interface FoodService {
    List<Food> getAllFoods();
    List<Food> getFoodsByCategory(String category);
}
