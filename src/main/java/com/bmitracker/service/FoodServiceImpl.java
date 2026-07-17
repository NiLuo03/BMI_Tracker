package com.bmitracker.service;

import com.bmitracker.dao.FoodDao;
import com.bmitracker.model.Food;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FoodServiceImpl implements FoodService {

    private static final FoodDao foodDao = new FoodDao();

    private static List<Food> cachedAllFoods;
    private static List<String> cachedCategories;
    private static List<String> cachedMealTypes;
    private static List<String> cachedTextures;
    private static List<String> cachedFlavors;
    private static List<String> cachedStorages;
    private static List<String> cachedCookings;

    private static synchronized void ensureCache() {
        if (cachedAllFoods != null) return;
        try {
            cachedAllFoods = foodDao.findFiltered(null, null, null, null, null, null);
            cachedCategories = cachedAllFoods.stream().map(Food::getCategory).distinct().sorted().collect(Collectors.toList());
            cachedMealTypes = cachedAllFoods.stream().map(Food::getMealType).filter(java.util.Objects::nonNull).distinct().sorted().collect(Collectors.toList());
            cachedTextures = cachedAllFoods.stream().map(Food::getFoodTexture).filter(java.util.Objects::nonNull).distinct().sorted().collect(Collectors.toList());
            cachedFlavors = cachedAllFoods.stream().map(Food::getFlavor).filter(java.util.Objects::nonNull).distinct().sorted().collect(Collectors.toList());
            cachedStorages = cachedAllFoods.stream().map(Food::getStorage).filter(java.util.Objects::nonNull).distinct().sorted().collect(Collectors.toList());
            cachedCookings = cachedAllFoods.stream().map(Food::getCookingMethod).filter(java.util.Objects::nonNull).distinct().sorted().collect(Collectors.toList());
        } catch (SQLException e) {
            cachedAllFoods = Collections.emptyList();
            cachedCategories = Collections.emptyList();
            cachedMealTypes = Collections.emptyList();
            cachedTextures = Collections.emptyList();
            cachedFlavors = Collections.emptyList();
            cachedStorages = Collections.emptyList();
            cachedCookings = Collections.emptyList();
        }
    }

    @Override
    public List<Food> getAllFoods() {
        ensureCache();
        return cachedAllFoods;
    }

    @Override
    public List<Food> getFoodsByCategory(String category) {
        ensureCache();
        return cachedAllFoods.stream()
                .filter(f -> category.equals(f.getCategory()))
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllCategories() {
        ensureCache();
        return cachedCategories;
    }

    @Override
    public List<Food> getTopByCategory(String category) {
        return category == null || category.isEmpty() ? getAllFoods() : getFoodsByCategory(category);
    }

    @Override
    public List<Food> getRankByCategoryAndNutrient(String category, String nutrient, boolean asc) {
        List<String> valid = Arrays.asList("calories", "protein", "fat", "carb", "foodName");
        if (!valid.contains(nutrient)) return Collections.emptyList();
        ensureCache();
        if ("foodName".equals(nutrient)) {
            return cachedAllFoods.stream()
                    .filter(f -> category.equals(f.getCategory()))
                    .sorted((a, b) -> asc ? a.getFoodName().compareTo(b.getFoodName()) : b.getFoodName().compareTo(a.getFoodName()))
                    .limit(10)
                    .collect(Collectors.toList());
        }
        return cachedAllFoods.stream()
                .filter(f -> category.equals(f.getCategory()))
                .sorted((a, b) -> {
                    double va = getNutrient(a, nutrient);
                    double vb = getNutrient(b, nutrient);
                    return asc ? Double.compare(va, vb) : Double.compare(vb, va);
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    private double getNutrient(Food f, String nutrient) {
        return switch (nutrient) {
            case "calories" -> f.getCalories();
            case "protein" -> f.getProtein();
            case "fat" -> f.getFat();
            case "carb" -> f.getCarb();
            default -> 0;
        };
    }

    @Override
    public List<String> getAllMealTypes() { ensureCache(); return cachedMealTypes; }
    @Override
    public List<String> getAllTextures() { ensureCache(); return cachedTextures; }
    @Override
    public List<String> getAllFlavors() { ensureCache(); return cachedFlavors; }
    @Override
    public List<String> getAllStorages() { ensureCache(); return cachedStorages; }
    @Override
    public List<String> getAllCookingMethods() { ensureCache(); return cachedCookings; }

    @Override
    public List<Food> findFiltered(String category, String mealType, String texture, String flavor, String storage, String cooking) {
        ensureCache();
        return cachedAllFoods.stream()
                .filter(f -> category == null || category.equals(f.getCategory()))
                .filter(f -> mealType == null || mealType.equals(f.getMealType()))
                .filter(f -> texture == null || texture.equals(f.getFoodTexture()))
                .filter(f -> flavor == null || flavor.equals(f.getFlavor()))
                .filter(f -> storage == null || storage.equals(f.getStorage()))
                .filter(f -> cooking == null || cooking.equals(f.getCookingMethod()))
                .collect(Collectors.toList());
    }
}
