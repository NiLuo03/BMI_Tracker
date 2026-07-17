package com.bmitracker.service;

import com.bmitracker.dao.MealRecordDao;
import com.bmitracker.model.MealRecord;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class MealRecordService {

    private final MealRecordDao dao = new MealRecordDao();

    public List<MealRecord> getRecords(int userId, LocalDate date) {
        try {
            return dao.findByUserAndDate(userId, date);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public List<MealRecord> getRecordsInRange(int userId, LocalDate start, LocalDate end) {
        try {
            return dao.findByUserAndDateRange(userId, start, end);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public void saveRecords(int userId, LocalDate date, List<MealRecord> records) {
        try {
            dao.saveRecords(userId, date, records);
        } catch (SQLException e) {
            throw new RuntimeException("保存膳食记录失败", e);
        }
    }
}
