package com.bmitracker.util;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBUtil {

    private static final Logger LOG = Logger.getLogger(DBUtil.class.getName());

    /** true=前端预览(不走DB)， false=正常模式 */
    public static boolean PREVIEW_MODE = false;

    /** H2本地文件数据库，MySQL兼容模式 */
    private static final String H2_URL = "jdbc:h2:file:./bmi_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE";
    /** 队友的MySQL地址 */
    private static final String MYSQL_URL = "jdbc:mysql://172.30.67.202:3306/bmi_db?useSSL=false&serverTimezone=Asia/Shanghai&connectTimeout=3000&socketTimeout=3000";
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
                    "foodName VARCHAR(30) NOT NULL UNIQUE," +
                    "category VARCHAR(10) NOT NULL," +
                    "calories DECIMAL(6,1) NOT NULL," +
                    "protein DECIMAL(5,1)," +
                    "fat DECIMAL(5,1)," +
                    "carb DECIMAL(5,1))");

            stmt.execute("CREATE TABLE IF NOT EXISTS recommendations (" +
                    "recId INT AUTO_INCREMENT PRIMARY KEY," +
                    "userId INT NOT NULL," +
                    "breakfast VARCHAR(200)," +
                    "lunch VARCHAR(200)," +
                    "dinner VARCHAR(200)," +
                    "totalCal VARCHAR(20)," +
                    "createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (userId) REFERENCES users(userId))");

            // 检查是否已有食物数据，没有则插入
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM foods");
            rs.next();
            if (rs.getInt(1) == 0) {
                seedFoods(stmt);
            }
            rs.close();
        }
    }

    private static void seedFoods(Statement stmt) throws SQLException {
        String[][] foods = {
                // 主食
                {"米饭", "主食", "116", "2.6", "0.3", "25.9"},
                {"馒头", "主食", "223", "7.0", "1.1", "44.2"},
                {"面条(煮)", "主食", "110", "3.9", "0.3", "23.3"},
                {"全麦面包", "主食", "246", "9.6", "3.4", "41.3"},
                {"小米粥", "主食", "46", "1.4", "0.7", "8.4"},
                {"玉米", "主食", "112", "4.0", "1.2", "22.8"},
                {"红薯", "主食", "99", "1.1", "0.2", "23.1"},
                {"燕麦片", "主食", "367", "13.5", "6.7", "61.6"},
                {"土豆", "主食", "76", "2.0", "0.2", "17.5"},
                {"糙米饭", "主食", "111", "2.5", "0.9", "23.0"},
                // 肉类
                {"鸡胸肉", "肉类", "133", "22.5", "4.0", "2.5"},
                {"猪肉(瘦)", "肉类", "143", "20.3", "6.2", "1.5"},
                {"牛肉(瘦)", "肉类", "106", "20.2", "2.3", "1.2"},
                {"羊肉", "肉类", "203", "19.0", "14.1", "0"},
                {"鸡蛋", "肉类", "144", "13.3", "8.8", "2.8"},
                {"鲈鱼", "肉类", "105", "18.6", "3.4", "0"},
                {"虾", "肉类", "93", "18.6", "0.8", "2.8"},
                {"鸭肉", "肉类", "240", "15.5", "19.7", "0.2"},
                {"三文鱼", "肉类", "139", "17.2", "7.8", "0"},
                {"猪肝", "肉类", "129", "19.3", "3.5", "5.0"},
                // 蔬菜
                {"西兰花", "蔬菜", "34", "2.8", "0.4", "6.6"},
                {"菠菜", "蔬菜", "23", "2.6", "0.3", "4.5"},
                {"番茄", "蔬菜", "18", "0.9", "0.2", "4.0"},
                {"黄瓜", "蔬菜", "15", "0.7", "0.1", "2.9"},
                {"胡萝卜", "蔬菜", "41", "0.9", "0.2", "10.0"},
                {"生菜", "蔬菜", "15", "1.4", "0.2", "2.8"},
                {"白菜", "蔬菜", "13", "1.5", "0.2", "2.2"},
                {"芹菜", "蔬菜", "14", "0.7", "0.1", "3.9"},
                {"茄子", "蔬菜", "25", "1.0", "0.2", "5.9"},
                {"南瓜", "蔬菜", "26", "1.0", "0.1", "6.5"},
                // 水果
                {"苹果", "水果", "52", "0.3", "0.2", "13.5"},
                {"香蕉", "水果", "89", "1.1", "0.3", "22.8"},
                {"橙子", "水果", "47", "0.9", "0.2", "11.8"},
                {"葡萄", "水果", "69", "0.7", "0.2", "18.1"},
                {"西瓜", "水果", "30", "0.6", "0.1", "6.8"},
                {"草莓", "水果", "32", "0.7", "0.3", "7.7"},
                {"猕猴桃", "水果", "61", "1.1", "0.5", "14.7"},
                {"芒果", "水果", "60", "0.8", "0.4", "15.0"},
                {"梨", "水果", "57", "0.4", "0.1", "15.5"},
                {"火龙果", "水果", "55", "1.1", "0.4", "13.0"},
                // 饮品
                {"牛奶", "饮品", "65", "3.2", "3.6", "4.9"},
                {"豆浆", "饮品", "31", "2.9", "1.5", "1.8"},
                {"酸奶(原味)", "饮品", "61", "3.5", "3.1", "4.6"},
                {"橙汁", "饮品", "45", "0.7", "0.2", "10.4"},
                {"绿茶", "饮品", "1", "0.1", "0", "0"},
                {"咖啡(黑)", "饮品", "2", "0.1", "0", "0"},
                {"可乐", "饮品", "42", "0", "0", "10.6"},
                {"椰汁", "饮品", "19", "0.4", "0.2", "3.7"},
                {"运动饮料", "饮品", "27", "0", "0", "6.4"},
                {"蜂蜜水", "饮品", "32", "0.1", "0", "8.0"},
        };

        for (String[] f : foods) {
            stmt.execute(String.format(
                    "INSERT INTO foods (foodName, category, calories, protein, fat, carb) " +
                            "VALUES ('%s','%s',%s,%s,%s,%s)",
                    f[0], f[1], f[2], f[3], f[4], f[5]));
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
