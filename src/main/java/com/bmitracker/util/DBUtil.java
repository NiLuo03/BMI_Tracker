package com.bmitracker.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("SqlDialectInspection")
public class DBUtil {

    private static final Logger LOG = Logger.getLogger(DBUtil.class.getName());

    public static boolean PREVIEW_MODE = false;

    private static final String H2_URL = "jdbc:h2:file:./bmi_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE";

    static {
        try {
            Class.forName("org.h2.Driver");
            initH2Database();
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
            for (String col : new String[]{"meal_type","food_texture","flavor","storage","cooking_method","image"}) {
                try { stmt.execute("ALTER TABLE foods ADD COLUMN IF NOT EXISTS " + col + " VARCHAR(10)"); } catch (SQLException ignored) {}
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS meal_records (" +
                    "recordId INT AUTO_INCREMENT PRIMARY KEY," +
                    "userId INT NOT NULL," +
                    "mealType VARCHAR(10) NOT NULL," +
                    "foodId INT NOT NULL," +
                    "grams DECIMAL(6,1) NOT NULL DEFAULT 100," +
                    "recordDate DATE NOT NULL," +
                    "createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (userId) REFERENCES users(userId))");

            stmt.execute("CREATE TABLE IF NOT EXISTS recommendations (" +
                    "recId INT AUTO_INCREMENT PRIMARY KEY," +
                    "userId INT NOT NULL," +
                    "breakfast VARCHAR(200)," +
                    "lunch VARCHAR(200)," +
                    "dinner VARCHAR(200)," +
                    "totalCal VARCHAR(20)," +
                    "createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (userId) REFERENCES users(userId))");

            boolean needsSeed;
            try {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM foods WHERE meal_type IS NOT NULL");
                rs.next();
                needsSeed = rs.getInt(1) == 0;
                rs.close();
            } catch (SQLException e) {
                needsSeed = true;
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
        return DriverManager.getConnection(H2_URL, "sa", "");
    }
}
