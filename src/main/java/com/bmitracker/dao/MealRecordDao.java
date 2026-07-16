package com.bmitracker.dao;

import com.bmitracker.model.MealRecord;
import com.bmitracker.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MealRecordDao {

    public void insert(int userId, String mealType, int foodId, double grams, LocalDate date) throws SQLException {
        String sql = "INSERT INTO meal_records (userId, mealType, foodId, grams, recordDate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, mealType);
            ps.setInt(3, foodId);
            ps.setDouble(4, grams);
            ps.setDate(5, Date.valueOf(date));
            ps.executeUpdate();
        }
    }

    public void deleteByUserAndDate(int userId, LocalDate date) throws SQLException {
        String sql = "DELETE FROM meal_records WHERE userId = ? AND recordDate = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(date));
            ps.executeUpdate();
        }
    }

    public List<MealRecord> findByUserAndDate(int userId, LocalDate date) throws SQLException {
        String sql = "SELECT mr.*, f.foodName, f.calories FROM meal_records mr " +
                "JOIN foods f ON mr.foodId = f.foodId " +
                "WHERE mr.userId = ? AND mr.recordDate = ? ORDER BY mr.mealType, mr.recordId";
        List<MealRecord> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private MealRecord map(ResultSet rs) throws SQLException {
        MealRecord r = new MealRecord();
        r.setRecordId(rs.getInt("recordId"));
        r.setUserId(rs.getInt("userId"));
        r.setMealType(rs.getString("mealType"));
        r.setFoodId(rs.getInt("foodId"));
        r.setGrams(rs.getDouble("grams"));
        r.setRecordDate(rs.getDate("recordDate").toLocalDate());
        r.setFoodName(rs.getString("foodName"));
        r.setCalories(rs.getDouble("calories"));
        return r;
    }
}
