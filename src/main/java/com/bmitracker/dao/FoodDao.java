package com.bmitracker.dao;

import com.bmitracker.model.Food;
import com.bmitracker.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodDao {

    // 按分类查询（按热量升序）
    public List<Food> findByCategoryOrderByCalories(String category) throws SQLException {
        String sql = "SELECT * FROM foods WHERE category = ? ORDER BY calories ASC LIMIT 10";
        List<Food> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapFood(rs));
            }
        }
        return list;
    }

    // 根据 ID 列表查询
    public List<Food> findByIds(List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM foods WHERE foodId IN (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append(i > 0 ? ",?" : "?");
        }
        sql.append(")");
        List<Food> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapFood(rs));
            }
        }
        return list;
    }

    // 查询所有分类
    public List<String> findAllCategories() throws SQLException {
        String sql = "SELECT DISTINCT category FROM foods ORDER BY category";
        List<String> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString("category"));
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
        return f;
    }
}
