package com.bmitracker.algorithm;

import com.bmitracker.model.BmiRecord;

import java.util.ArrayList;
import java.util.List;

public class LinearRegression {

    private double slope;
    private double intercept;
    private double rSquared;
    private boolean fitted;
    private int sampleSize;

    public void fit(List<BmiRecord> records) {
        if (records == null || records.size() < 2) {
            throw new IllegalArgumentException("至少需要2条记录进行线性回归");
        }

        int n = records.size();
        sampleSize = n;

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        double cX = 0, cY = 0, cXY = 0, cX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = records.get(i).getBmi();
            double yX = x * y;
            double x2 = x * x;

            double tX = sumX + x;
            double tY = sumY + y;
            double tXY = sumXY + yX;
            double tX2 = sumX2 + x2;

            cX += (x - (tX - cX)) - (sumX - cX);
            cY += (y - (tY - cY)) - (sumY - cY);
            cXY += (yX - (tXY - cXY)) - (sumXY - cXY);
            cX2 += (x2 - (tX2 - cX2)) - (sumX2 - cX2);

            sumX = tX;
            sumY = tY;
            sumXY = tXY;
            sumX2 = tX2;
        }

        double correctedSumX = sumX - cX;
        double correctedSumY = sumY - cY;
        double correctedSumXY = sumXY - cXY;
        double correctedSumX2 = sumX2 - cX2;

        double denominator = n * correctedSumX2 - correctedSumX * correctedSumX;
        if (Math.abs(denominator) < 1e-12) {
            throw new ArithmeticException("数据无法进行线性回归（所有x值相同）");
        }

        slope = (n * correctedSumXY - correctedSumX * correctedSumY) / denominator;
        intercept = (correctedSumY - slope * correctedSumX) / n;

        double meanY = correctedSumY / n;
        double ssRes = 0, ssTot = 0;
        for (int i = 0; i < n; i++) {
            double y = records.get(i).getBmi();
            double yPred = slope * (i + 1) + intercept;
            ssRes += (y - yPred) * (y - yPred);
            ssTot += (y - meanY) * (y - meanY);
        }
        rSquared = ssTot > 1e-12 ? 1 - ssRes / ssTot : 0;

        fitted = true;
    }

    public double predict(double x) {
        checkFitted();
        return slope * x + intercept;
    }

    public List<double[]> getFittedLinePoints() {
        checkFitted();
        List<double[]> points = new ArrayList<>(sampleSize);
        for (int i = 0; i < sampleSize; i++) {
            points.add(new double[]{i + 1, slope * (i + 1) + intercept});
        }
        return points;
    }

    public double predictNext(int daysAhead) {
        return predict(sampleSize + daysAhead);
    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }

    public double getRSquared() {
        return rSquared;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public String getEquation() {
        return String.format("BMI = %.4f × 天数 + %.4f", slope, intercept);
    }

    public String getRSquaredText() {
        return String.format("拟合优度 R² = %.4f", rSquared);
    }

    public String getSlopeInterpretation() {
        if (slope > 0.005) {
            return String.format("⬆ 趋势上升：每天 BMI 增加 %.4f，需要注意控制体重", slope);
        } else if (slope < -0.005) {
            return String.format("⬇ 趋势下降：每天 BMI 减少 %.4f，继续保持", Math.abs(slope));
        } else {
            return "➡ 趋势平稳：BMI 基本保持不变";
        }
    }

    private void checkFitted() {
        if (!fitted) {
            throw new IllegalStateException("请先调用 fit() 方法");
        }
    }
}
