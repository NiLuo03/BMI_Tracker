package com.bmitracker.algorithm;

import com.bmitracker.model.BmiRecord;

import java.util.List;

public class LinearRegression {

    private double slope;
    private double intercept;
    private boolean fitted;

    public void fit(List<BmiRecord> records) {
        if (records == null || records.size() < 2) {
            throw new IllegalArgumentException("至少需要2条记录进行线性回归");
        }

        int n = records.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = records.get(i).getBmi();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denominator = n * sumX2 - sumX * sumX;
        if (Math.abs(denominator) < 1e-10) {
            throw new ArithmeticException("数据无法进行线性回归（所有x值相同）");
        }

        slope = (n * sumXY - sumX * sumY) / denominator;
        intercept = (sumY - slope * sumX) / n;
        fitted = true;
    }

    public double predict(double x) {
        if (!fitted) {
            throw new IllegalStateException("请先调用 fit() 方法");
        }
        return slope * x + intercept;
    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }

    public String getEquation() {
        return String.format("BMI = %.4f × 天数 + %.4f", slope, intercept);
    }

    public String getSlopeInterpretation() {
        if (slope > 0.01) {
            return String.format("趋势上升：每天 BMI 增加 %.4f，需要注意控制体重", slope);
        } else if (slope < -0.01) {
            return String.format("趋势下降：每天 BMI 减少 %.4f，继续保持", Math.abs(slope));
        } else {
            return "趋势平稳：BMI 基本保持不变";
        }
    }

    public double predictNext(int daysAhead) {
        return predict(daysAhead + 1);
    }
}
