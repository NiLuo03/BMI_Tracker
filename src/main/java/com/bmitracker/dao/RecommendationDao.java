package com.bmitracker.dao;

import com.bmitracker.model.Recommendation;
import com.bmitracker.util.DBUtil;
import java.sql.*;

public class RecommendationDao {

    // 插入膳食推荐记录
    public int insert(Recommendation rec) throws SQLException {
        String sql = "INSERT INTO recommendations (userId, breakfast, lunch, dinner, totalCal) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rec.getUserId());
            ps.setString(2, rec.getBreakfast());
            ps.setString(3, rec.getLunch());
            ps.setString(4, rec.getDinner());
            ps.setString(5, rec.getTotalCal());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // 查询最近一条推荐记录
    public Recommendation findLatestByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM recommendations WHERE userId = ? ORDER BY createTime DESC LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Recommendation rec = new Recommendation();
                    rec.setRecId(rs.getInt("recId"));
                    rec.setUserId(rs.getInt("userId"));
                    rec.setBreakfast(rs.getString("breakfast"));
                    rec.setLunch(rs.getString("lunch"));
                    rec.setDinner(rs.getString("dinner"));
                    rec.setTotalCal(rs.getString("totalCal"));
                    Timestamp ts = rs.getTimestamp("createTime");
                    if (ts != null) rec.setCreateTime(ts.toLocalDateTime());
                    return rec;
                }
            }
        }
        return null;
    }
}
