package com.bmitracker.util;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBUtil {

    private static final Logger LOG = Logger.getLogger(DBUtil.class.getName());

    private static final String URL = "jdbc:mysql://172.30.67.202:3306/bmi_db?useSSL=false&serverTimezone=Asia/Shanghai&connectTimeout=3000&socketTimeout=3000";
    private static final String USER = "root";
    private static final String PASS = "23456789";

    /** 设为 true 则跳过数据库连接，前端预览模式专用 */
    public static boolean PREVIEW_MODE = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "MySQL驱动加载失败", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (PREVIEW_MODE) {
            throw new SQLException("前端预览模式，已跳过数据库连接");
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
