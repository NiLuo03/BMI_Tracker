package com.bmitracker.dao;

import com.bmitracker.model.QuizResult;
import com.bmitracker.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizResultDao {

    public void insert(QuizResult r) throws SQLException {
        String sql = "INSERT INTO quiz_results (userId, score, total, answers) VALUES (?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getUserId());
            ps.setInt(2, r.getScore());
            ps.setInt(3, r.getTotal());
            ps.setString(4, r.getAnswers());
            ps.executeUpdate();
        }
    }

    public List<QuizResult> findByUser(int userId) throws SQLException {
        List<QuizResult> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_results WHERE userId = ? ORDER BY createTime DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<LeaderboardEntry> getLeaderboard() throws SQLException {
        return getLeaderboard(null);
    }

    public List<LeaderboardEntry> getLeaderboard(java.time.LocalDateTime since) throws SQLException {
        List<LeaderboardEntry> list = new ArrayList<>();
        String sql = "SELECT u.userId, u.userName, COALESCE(MAX(q.score), 0) AS maxScore " +
                     "FROM users u INNER JOIN quiz_results q ON u.userId = q.userId";
        if (since != null) sql += " WHERE q.createTime >= ?";
        sql += " GROUP BY u.userId, u.userName ORDER BY maxScore DESC, u.userName ASC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (since != null) ps.setTimestamp(1, java.sql.Timestamp.valueOf(since));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new LeaderboardEntry(
                    rs.getInt("userId"),
                    rs.getString("userName"),
                    rs.getInt("maxScore")
                ));
            }
        }
        return list;
    }

    public static class LeaderboardEntry {
        private final int userId;
        private final String userName;
        private final int maxScore;

        public LeaderboardEntry(int userId, String userName, int maxScore) {
            this.userId = userId;
            this.userName = userName;
            this.maxScore = maxScore;
        }

        public int getUserId() { return userId; }
        public String getUserName() { return userName; }
        public int getMaxScore() { return maxScore; }
    }

    public List<QuizResult> getTodayResults(int userId) throws SQLException {
        List<QuizResult> list = new ArrayList<>();
        String sql = "SELECT * FROM quiz_results WHERE userId = ? AND createTime >= CURRENT_DATE ORDER BY createTime DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private QuizResult map(ResultSet rs) throws SQLException {
        QuizResult r = new QuizResult();
        r.setResultId(rs.getInt("resultId"));
        r.setUserId(rs.getInt("userId"));
        r.setScore(rs.getInt("score"));
        r.setTotal(rs.getInt("total"));
        r.setAnswers(rs.getString("answers"));
        r.setCreateTime(rs.getTimestamp("createTime").toLocalDateTime());
        return r;
    }
}
