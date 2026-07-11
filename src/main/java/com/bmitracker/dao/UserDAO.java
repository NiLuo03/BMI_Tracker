package com.bmitracker.dao;

import com.bmitracker.model.User;
import com.bmitracker.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;

public class UserDAO {

    // 注册用户，插入 users 表
    public boolean register(User user) {
        String sql = "INSERT INTO users (userName, password, userAge, sex) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getUserAge());
            ps.setInt(4, user.getSex());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, ps);
        }
    }

    // 校验账号密码，登录成功返回 userId，失败返回 -1
    public int login(String userName, String password) {
        String sql = "SELECT userId FROM users WHERE userName = ? AND password = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("userId");
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    // 根据 userId 查询用户信息
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE userId = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
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
                if (ts != null) {
                    user.setCreateTime(ts.toLocalDateTime());
                }
                return user;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    // 更新用户信息（年龄、性别、身高、体重、偏好）
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET userAge = ?, sex = ?, height = ?, weight = ?, preferences = ? WHERE userId = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getUserAge());
            ps.setInt(2, user.getSex());
            ps.setDouble(3, user.getHeight());
            ps.setDouble(4, user.getWeight());
            ps.setString(5, user.getPreferences());
            ps.setInt(6, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, ps);
        }
    }
}
