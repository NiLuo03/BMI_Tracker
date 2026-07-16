package com.bmitracker.dao;

import com.bmitracker.model.MealRecord;
import com.bmitracker.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MealRecordDao {

    public List<MealRecord> findByUserAndDate(int userId, LocalDate date) throws SQLException {
        String sql = "SELECT mr.*, f.foodName, f.calories FROM meal_records mr " +
                     "JOIN foods f ON mr.foodId = f.foodId " +
                     "WHERE mr.userId = ? AND mr.recordDate = ? ORDER BY mr.mealType, mr.recordId";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            List<MealRecord> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    public List<MealRecord> findByUserAndDateRange(int userId, LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT mr.*, f.foodName, f.calories FROM meal_records mr " +
                     "JOIN foods f ON mr.foodId = f.foodId " +
                     "WHERE mr.userId = ? AND mr.recordDate BETWEEN ? AND ? ORDER BY mr.recordDate, mr.mealType";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            List<MealRecord> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    public void deleteByUserAndDate(int userId, LocalDate date) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM meal_records WHERE userId = ? AND recordDate = ?")) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(date));
            ps.executeUpdate();
        }
    }

    public void batchInsert(int userId, LocalDate date, List<MealRecord> records) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO meal_records (userId, foodId, mealType, grams, recordDate) VALUES (?,?,?,?,?)")) {
            for (MealRecord r : records) {
                ps.setInt(1, userId);
                ps.setInt(2, r.getFoodId());
                ps.setString(3, r.getMealType());
                ps.setDouble(4, r.getGrams());
                ps.setDate(5, Date.valueOf(date));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void saveRecords(int userId, LocalDate date, List<MealRecord> records) throws SQLException {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM meal_records WHERE userId = ? AND recordDate = ?")) {
                del.setInt(1, userId);
                del.setDate(2, Date.valueOf(date));
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO meal_records (userId, foodId, mealType, grams, recordDate) VALUES (?,?,?,?,?)")) {
                for (MealRecord r : records) {
                    ins.setInt(1, userId);
                    ins.setInt(2, r.getFoodId());
                    ins.setString(3, r.getMealType());
                    ins.setDouble(4, r.getGrams());
                    ins.setDate(5, Date.valueOf(date));
                    ins.addBatch();
                }
                ins.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    private MealRecord map(ResultSet rs) throws SQLException {
        MealRecord r = new MealRecord();
        r.setRecordId(rs.getInt("recordId"));
        r.setUserId(rs.getInt("userId"));
        r.setFoodId(rs.getInt("foodId"));
        r.setMealType(rs.getString("mealType"));
        r.setGrams(rs.getDouble("grams"));
        r.setRecordDate(rs.getDate("recordDate").toLocalDate());
        r.setFoodName(rs.getString("foodName"));
        r.setFoodCalories(rs.getDouble("calories"));
        return r;
    }
}
