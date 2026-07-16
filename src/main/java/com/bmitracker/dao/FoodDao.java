package com.bmitracker.dao;

import com.bmitracker.model.Food;
import com.bmitracker.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodDao {

    public List<Food> findByCategoryOrderByCalories(String category) throws SQLException {
        String sql = "SELECT * FROM foods WHERE category = ? ORDER BY calories ASC LIMIT 10";
        return queryList(sql, ps -> ps.setString(1, category));
    }

    public List<Food> findRankByCategory(String category, String orderColumn, boolean asc) throws SQLException {
        String dir = asc ? "ASC" : "DESC";
        String sql = "SELECT * FROM foods WHERE category = ? ORDER BY " + orderColumn + " " + dir + " LIMIT 10";
        return queryList(sql, ps -> ps.setString(1, category));
    }

    public List<Food> findByIds(List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM foods WHERE foodId IN (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append(i > 0 ? ",?" : "?");
        }
        sql.append(")");
        return queryList(sql.toString(), ps -> {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
        });
    }

    public List<String> findAllCategories() throws SQLException {
        return queryDistinct("SELECT DISTINCT category FROM foods ORDER BY category", "category");
    }

    public List<String> findAllMealTypes() throws SQLException {
        return queryDistinct("SELECT DISTINCT meal_type FROM foods WHERE meal_type IS NOT NULL ORDER BY meal_type", "meal_type");
    }

    public List<String> findAllTextures() throws SQLException {
        return queryDistinct("SELECT DISTINCT food_texture FROM foods WHERE food_texture IS NOT NULL ORDER BY food_texture", "food_texture");
    }

    public List<String> findAllFlavors() throws SQLException {
        return queryDistinct("SELECT DISTINCT flavor FROM foods WHERE flavor IS NOT NULL ORDER BY flavor", "flavor");
    }

    public List<String> findAllStorages() throws SQLException {
        return queryDistinct("SELECT DISTINCT storage FROM foods WHERE storage IS NOT NULL ORDER BY storage", "storage");
    }

    public List<String> findAllCookingMethods() throws SQLException {
        return queryDistinct("SELECT DISTINCT cooking_method FROM foods WHERE cooking_method IS NOT NULL ORDER BY cooking_method", "cooking_method");
    }

    public List<Food> findFiltered(String category, String mealType, String texture, String flavor, String storage, String cooking) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM foods WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendParam(sql, params, category, "category");
        appendParam(sql, params, mealType, "meal_type");
        appendParam(sql, params, texture, "food_texture");
        appendParam(sql, params, flavor, "flavor");
        appendParam(sql, params, storage, "storage");
        appendParam(sql, params, cooking, "cooking_method");
        sql.append(" ORDER BY category, calories");
        return queryList(sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, (String) params.get(i));
            }
        });
    }

    private void appendParam(StringBuilder sql, List<Object> params, String value, String column) {
        if (value != null && !value.isEmpty()) {
            sql.append(" AND ").append(column).append(" = ?");
            params.add(value);
        }
    }

    @FunctionalInterface
    private interface ParamBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private List<Food> queryList(String sql, ParamBinder binder) throws SQLException {
        List<Food> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapFood(rs));
            }
        }
        return list;
    }

    private List<String> queryDistinct(String sql, String column) throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(column));
        }
        return list;
    }

    private Food mapFood(ResultSet rs) throws SQLException {
        Food f = new Food();
        f.setFoodId(rs.getInt("foodId"));
        f.setFoodName(rs.getString("foodName"));
        f.setCategory(rs.getString("category"));
        f.setCalories(rs.getDouble("calories"));
        f.setProtein(rs.getDouble("protein"));
        f.setFat(rs.getDouble("fat"));
        f.setCarb(rs.getDouble("carb"));
        f.setMealType(rs.getString("meal_type"));
        f.setFoodTexture(rs.getString("food_texture"));
        f.setFlavor(rs.getString("flavor"));
        f.setStorage(rs.getString("storage"));
        f.setCookingMethod(rs.getString("cooking_method"));
        f.setImage(rs.getString("image"));
        return f;
    }
}
