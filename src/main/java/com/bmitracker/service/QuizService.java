package com.bmitracker.service;

import com.bmitracker.dao.QuizResultDao;
import com.bmitracker.model.QuizResult;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class QuizService {

    private final QuizResultDao dao = new QuizResultDao();
    private List<QuizQuestion> allQuestions;

    public static class QuizQuestion {
        private final int id;
        private final String question;
        private final String[] options = new String[4];
        private final String answer;
        private final String explanation;

        public QuizQuestion(int id, String q, String a, String b, String c, String d, String ans, String exp) {
            this.id = id;
            this.question = q;
            options[0] = a;
            options[1] = b;
            options[2] = c;
            options[3] = d;
            this.answer = ans;
            this.explanation = exp;
        }

        public int getId() { return id; }
        public String getQuestion() { return question; }
        public String getOption(int i) { return options[i]; }
        public String getAnswer() { return answer; }
        public String[] getOptions() { return options; }
        public int getAnswerIndex() { return answer.charAt(0) - 'A'; }
        public String getExplanation() { return explanation; }
    }

    public List<QuizQuestion> loadAllQuestions() {
        if (allQuestions != null) return allQuestions;
        allQuestions = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/data/quiz_questions.txt")) {
            if (is == null) return allQuestions;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|", 8);
                    if (parts.length < 7) continue;
                    int id = Integer.parseInt(parts[0]);
                    String exp = parts.length >= 8 ? parts[7] : "";
                    allQuestions.add(new QuizQuestion(id, parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], exp));
                }
            }
        } catch (Exception e) {
            allQuestions = Collections.emptyList();
        }
        return allQuestions;
    }

    public List<QuizQuestion> pickRandom(int count) {
        List<QuizQuestion> pool = new ArrayList<>(loadAllQuestions());
        Collections.shuffle(pool);
        return pool.subList(0, Math.min(count, pool.size()));
    }

    public void saveResult(int userId, int score, int total, String answers) {
        QuizResult r = new QuizResult();
        r.setUserId(userId);
        r.setScore(score);
        r.setTotal(total);
        r.setAnswers(answers);
        try { dao.insert(r); }
        catch (SQLException e) { throw new RuntimeException("保存答题记录失败", e); }
    }

    public List<QuizResult> getHistory(int userId) {
        try { return dao.findByUser(userId); }
        catch (SQLException e) { return Collections.emptyList(); }
    }

    public List<QuizResultDao.LeaderboardEntry> getLeaderboard() {
        try { return dao.getLeaderboard(); }
        catch (SQLException e) { return Collections.emptyList(); }
    }

    public List<QuizResultDao.LeaderboardEntry> getLeaderboardWeekly() {
        LocalDateTime now = LocalDateTime.now();
        int daysFromSat = (now.getDayOfWeek().getValue() - DayOfWeek.SATURDAY.getValue() + 7) % 7;
        LocalDateTime since = now.toLocalDate().minusDays(daysFromSat).atStartOfDay();
        try { return dao.getLeaderboard(since); }
        catch (SQLException e) { return Collections.emptyList(); }
    }

    public static class TodayStats {
        public int totalQuizzes;
        public int totalCorrect;
        public int totalQuestions;
        public int totalScore;
        public int bestScore;
    }

    public TodayStats getTodayStats(int userId) {
        TodayStats stats = new TodayStats();
        try {
            List<QuizResult> results = dao.getTodayResults(userId);
            stats.totalQuizzes = results.size();
            for (QuizResult r : results) {
                stats.totalCorrect += r.getScore();
                stats.totalQuestions += r.getTotal();
                stats.totalScore += r.getScore() * 5;
                if (r.getScore() * 5 > stats.bestScore) stats.bestScore = r.getScore() * 5;
            }
        } catch (SQLException e) { /* return empty */ }
        return stats;
    }
}
