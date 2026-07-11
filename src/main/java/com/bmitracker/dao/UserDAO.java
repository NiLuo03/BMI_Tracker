package com.bmitracker.dao;

import com.bmitracker.model.User;
import com.bmitracker.util.DBUtil;
import java.sql.*;
import java.time.LocalDateTime;

public class UserDao {

    public User findByUserName(String userName) throws SQLException {
        String sql = "SELECT * FROM users WHERE userName = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
                return null;
            }
        }
    }

    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO users (userName, password, userAge, sex) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getUserAge());
            ps.setInt(4, user.getSex());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public int login(String userName, String password) throws SQLException {
        String sql = "SELECT userId FROM users WHERE userName = ? AND password = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userName);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("userId");
            }
        }
        return -1;
    }

    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE userId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        }
        return null;
    }

    public int update(User user) throws SQLException {
        String sql = "UPDATE users SET userAge = ?, sex = ?, height = ?, weight = ?, preferences = ? WHERE userId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user.getUserAge());
            ps.setInt(2, user.getSex());
            ps.setDouble(3, user.getHeight());
            ps.setDouble(4, user.getWeight());
            ps.setString(5, user.getPreferences());
            ps.setInt(6, user.getUserId());
            return ps.executeUpdate();
        }
    }

    public int updatePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE userId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("userId"));
        user.setUserName(rs.getString("userName"));
        user.setPassword(rs.getString("password"));
        user.setUserAge(rs.getInt("userAge"));
        user.setSex(rs.getInt("sex"));
        user.setHeight(rs.getDouble("height"));
        user.setWeight(rs.getDouble("weight"));
        user.setPreferences(rs.getString("preferences"));
        Timestamp ts = rs.getTimestamp("createTime");
        if (ts != null) user.setCreateTime(ts.toLocalDateTime());
        return user;
    }
}
