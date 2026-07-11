package com.bmitracker.service;

import com.bmitracker.dao.BmiRecordDao;
import com.bmitracker.model.BmiRecord;
import java.sql.SQLException;
import java.util.List;

public class BmiService {

    private final BmiRecordDao recordDao = new BmiRecordDao();

    // 计算 BMI
    public double calculateBMI(double heightCm, double weightKg) {
        double heightM = heightCm / 100.0;
        return Math.round((weightKg / (heightM * heightM)) * 10.0) / 10.0;
    }

    // 匹配健康区间
    public String getHealthStatus(double bmi) {
        if (bmi < 18.5) return "偏瘦";
        if (bmi < 24) return "正常";
        if (bmi < 28) return "超重";
        return "肥胖";
    }

    // 记录 BMI 并保存
    public String saveRecord(int userId, double heightCm, double weightKg) {
        if (heightCm <= 0 || weightKg <= 0) return "请输入有效的身高和体重";
        double bmi = calculateBMI(heightCm, weightKg);
        String status = getHealthStatus(bmi);
        BmiRecord record = new BmiRecord(userId, heightCm, weightKg, bmi, status);
        try {
            int id = recordDao.insert(record);
            return id > 0 ? null : "保存失败，请稍后再试";
        } catch (SQLException e) {
            return "系统繁忙，请稍后再试";
        }
    }

    // 查询历史记录（倒序）
    public List<BmiRecord> getRecordsDesc(int userId) {
        try {
            return recordDao.findByUserIdDesc(userId);
        } catch (SQLException e) {
            return null;
        }
    }

    // 查询历史记录（升序）
    public List<BmiRecord> getRecordsAsc(int userId) {
        try {
            return recordDao.findByUserIdAsc(userId);
        } catch (SQLException e) {
            return null;
        }
    }

    // 统计记录数
    public int getRecordCount(int userId) {
        try {
            return recordDao.countByUserId(userId);
        } catch (SQLException e) {
            return 0;
        }
    }

    // 获取最近一次 BMI
    public Double getLatestBmi(int userId) {
        List<BmiRecord> records = getRecordsDesc(userId);
        if (records != null && !records.isEmpty()) {
            return records.get(0).getBmi();
        }
        return null;
    }
}
