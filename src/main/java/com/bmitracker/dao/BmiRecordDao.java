package com.bmitracker.dao;

import com.bmitracker.model.BmiRecord;
import com.bmitracker.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BmiRecordDao {

    // 插入 BMI 记录
    public int insert(BmiRecord record) throws SQLException {
        String sql = "INSERT INTO bmi_records (userId, height, weight, bmi, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getUserId());
            ps.setDouble(2, record.getHeight());
            ps.setDouble(3, record.getWeight());
            ps.setDouble(4, record.getBmi());
            ps.setString(5, record.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // 按时间倒序查询（历史列表）
    public List<BmiRecord> findByUserIdDesc(int userId) throws SQLException {
        String sql = "SELECT * FROM bmi_records WHERE userId = ? ORDER BY createTime DESC";
        return queryList(userId, sql);
    }

    // 按时间升序查询（折线图、预测）
    public List<BmiRecord> findByUserIdAsc(int userId) throws SQLException {
        String sql = "SELECT * FROM bmi_records WHERE userId = ? ORDER BY createTime ASC";
        return queryList(userId, sql);
    }

    // 统计记录数
    public int countByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bmi_records WHERE userId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private List<BmiRecord> queryList(int userId, String sql) throws SQLException {
        List<BmiRecord> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRecord(rs));
            }
        }
        return list;
    }

    private BmiRecord mapRecord(ResultSet rs) throws SQLException {
        BmiRecord r = new BmiRecord();
        r.setRecordId(rs.getInt("recordId"));
        r.setUserId(rs.getInt("userId"));
        r.setHeight(rs.getDouble("height"));
        r.setWeight(rs.getDouble("weight"));
        r.setBmi(rs.getDouble("bmi"));
        r.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("createTime");
        if (ts != null) r.setCreateTime(ts.toLocalDateTime());
        return r;
    }
}
