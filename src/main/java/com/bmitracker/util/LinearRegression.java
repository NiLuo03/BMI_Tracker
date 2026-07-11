package com.bmitracker.util;

import com.bmitracker.model.BmiRecord;
import java.util.List;

public class LinearRegression {

    private double slope;
    private double intercept;
    private boolean fitted = false;

    // 最小二乘法拟合：BMI = slope * x + intercept
    public void fit(List<BmiRecord> records) {
        int n = records.size();
        if (n < 2) throw new IllegalArgumentException("至少需要2条记录进行回归");

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = records.get(i).getBmi();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double xBar = sumX / n;
        double yBar = sumY / n;
        double numerator = sumXY - n * xBar * yBar;
        double denominator = sumX2 - n * xBar * xBar;

        if (Math.abs(denominator) < 1e-10) {
            slope = 0;
        } else {
            slope = numerator / denominator;
        }
        intercept = yBar - slope * xBar;
        fitted = true;
    }

    // 预测：传入天数序号（0-based），返回预测 BMI 值
    public double predict(double x) {
        if (!fitted) throw new IllegalStateException("请先调用 fit()");
        return slope * x + intercept;
    }

    // 预测 7 天后的 BMI（基于当前记录数 n）
    public double predictNextWeek(int recordCount) {
        return predict(recordCount + 6);
    }

    public double getSlope() { return slope; }
    public double getIntercept() { return intercept; }
    public boolean isFitted() { return fitted; }
}
