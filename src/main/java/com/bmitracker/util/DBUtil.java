package com.bmitracker.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("SqlDialectInspection")
public class DBUtil {

    private static final Logger LOG = Logger.getLogger(DBUtil.class.getName());

    /** true=前端预览(不走DB)， false=正常模式 */
    public static boolean PREVIEW_MODE = false;

    /** H2本地文件数据库，MySQL兼容模式 */
    private static final String H2_URL = "jdbc:h2:file:./bmi_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE";
    /** 队友的MySQL地址 */
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/bmi_db?useSSL=false&serverTimezone=Asia/Shanghai&connectTimeout=3000&socketTimeout=3000";
    private static final String USER = "root";
    private static final String PASS = "23456789";

    /** true=使用内嵌H2， false=连队友MySQL */
    public static boolean USE_H2 = true;

    static {
        try {
            Class.forName("org.h2.Driver");
            if (USE_H2) {
                initH2Database();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "数据库初始化失败", e);
        }
    }

    private static void initH2Database() throws SQLException {
        try (Connection conn = DriverManager.getConnection(H2_URL, "sa", "");
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "userId INT AUTO_INCREMENT PRIMARY KEY," +
                    "userName VARCHAR(20) NOT NULL UNIQUE," +
                    "password VARCHAR(64) NOT NULL," +
                    "userAge INT NOT NULL," +
                    "sex TINYINT NOT NULL DEFAULT 0," +
                    "height DECIMAL(5,2)," +
                    "weight DECIMAL(5,2)," +
                    "preferences VARCHAR(200)," +
                    "createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 兼容旧表：添加健康档案字段
            for (String col : new String[]{"allergens VARCHAR(200)", "chronic_diseases VARCHAR(200)"}) {
                try { stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS " + col); } catch (SQLException ignored) {}
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS bmi_records (" +
                    "recordId INT AUTO_INCREMENT PRIMARY KEY," +
                    "userId INT NOT NULL," +
                    "height DECIMAL(5,2) NOT NULL," +
                    "weight DECIMAL(5,2) NOT NULL," +
                    "bmi DECIMAL(4,1) NOT NULL," +
                    "status VARCHAR(10) NOT NULL," +
                    "createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (userId) REFERENCES users(userId))");

            stmt.execute("CREATE TABLE IF NOT EXISTS foods (" +
                    "foodId INT AUTO_INCREMENT PRIMARY KEY," +
                    "foodName VARCHAR(50) NOT NULL UNIQUE," +
                    "category VARCHAR(10) NOT NULL," +
                    "calories DECIMAL(6,1) NOT NULL," +
                    "protein DECIMAL(5,1)," +
                    "fat DECIMAL(5,1)," +
                    "carb DECIMAL(5,1)," +
                    "meal_type VARCHAR(10)," +
                    "food_texture VARCHAR(10)," +
                    "flavor VARCHAR(10)," +
                    "storage VARCHAR(10)," +
                    "cooking_method VARCHAR(10)," +
                    "image VARCHAR(50))");
            // 兼容旧表：尝试添加新列（表已存在时会跳过）
            for (String col : new String[]{"meal_type","food_texture","flavor","storage","cooking_method","image"}) {
                try { stmt.execute("ALTER TABLE foods ADD COLUMN IF NOT EXISTS " + col + " VARCHAR(10)"); } catch (SQLException ignored) {}
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS recommendations (" +
                    "recId INT AUTO_INCREMENT PRIMARY KEY," +
                    "userId INT NOT NULL," +
                    "breakfast VARCHAR(200)," +
                    "lunch VARCHAR(200)," +
                    "dinner VARCHAR(200)," +
                    "totalCal VARCHAR(20)," +
                    "createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (userId) REFERENCES users(userId))");

            stmt.execute("CREATE TABLE IF NOT EXISTS meal_records (" +
                    "recordId INT AUTO_INCREMENT PRIMARY KEY," +
                    "userId INT NOT NULL," +
                    "foodId INT NOT NULL," +
                    "mealType VARCHAR(10) NOT NULL," +
                    "grams DECIMAL(7,1) NOT NULL," +
                    "recordDate DATE NOT NULL," +
                    "createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (userId) REFERENCES users(userId)," +
                    "FOREIGN KEY (foodId) REFERENCES foods(foodId))");

            // 检查是否需要初始化食物数据（空表或旧表缺标签）
            boolean needsSeed;
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM foods WHERE meal_type IS NOT NULL");
                rs.next();
                needsSeed = rs.getInt(1) == 0;
                rs.close();
            } catch (SQLException e) {
                needsSeed = true; // 表不存在或列缺失时执行 seed
            }
            if (needsSeed) {
                stmt.execute("DELETE FROM foods");
                seedFoods(stmt);
            }
        }
    }

    private static void seedFoods(Statement stmt) throws SQLException {
        String dataFile = "/data/foods.txt";
        try (InputStream is = DBUtil.class.getResourceAsStream(dataFile);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 11) continue;
                String img = parts.length >= 12 ? parts[11] : "";
                stmt.execute(String.format(
                    "INSERT INTO foods (foodName, category, calories, protein, fat, carb, meal_type, food_texture, flavor, storage, cooking_method, image) " +
                    "VALUES ('%s','%s',%s,%s,%s,%s,'%s','%s','%s','%s','%s','%s')",
                    parts[0], parts[1], parts[2], parts[3], parts[4], parts[5],
                    parts[6], parts[7], parts[8], parts[9], parts[10], img));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "无法读取食物数据文件: " + dataFile, e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (PREVIEW_MODE) {
            throw new SQLException("前端预览模式，已跳过数据库连接");
        }
        if (USE_H2) {
            return DriverManager.getConnection(H2_URL, "sa", "");
        }
        return DriverManager.getConnection(MYSQL_URL, USER, PASS);
    }
}
