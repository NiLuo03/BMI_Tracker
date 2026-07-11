package com.bmitracker.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MySQL 数据库连接工具类
 */
public class DBUtil {

    private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());

    // 数据库连接地址
    private static final String URL = "jdbc:mysql://localhost:3306/bmi_db?useSSL=false&serverTimezone=Asia/Shanghai";
    // 数据库用户名
    private static final String USERNAME = "root";
    // 数据库密码
    private static final String PASSWORD = "23456789";

    /**
     * 获取数据库连接
     * @return Connection 对象
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database operation failed", e);
        }
        return conn;
    }

    /**
     * 关闭数据库连接
     * @param conn 数据库连接对象
     */
    public static void close(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database operation failed", e);
        }
    }

    /**
     * 关闭数据库连接和语句对象
     * @param conn 数据库连接对象
     * @param stmt 语句对象
     */
    public static void close(Connection conn, Statement stmt) {
        try {
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database operation failed", e);
        }
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database operation failed", e);
        }
    }

    /**
     * 关闭数据库连接、语句对象和结果集
     * @param conn 数据库连接对象
     * @param stmt 语句对象
     * @param rs   结果集对象
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database operation failed", e);
        }
        try {
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database operation failed", e);
        }
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database operation failed", e);
        }
    }
}
