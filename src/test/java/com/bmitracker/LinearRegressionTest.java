package com.bmitracker;

import com.bmitracker.model.BmiRecord;
import com.bmitracker.util.LinearRegression;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class LinearRegressionTest {

    @Test
    public void testPerfectFit() {
        List<BmiRecord> records = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            BmiRecord r = new BmiRecord();
            r.setBmi(20.0 + i);
            records.add(r);
        }
        LinearRegression lr = new LinearRegression();
        lr.fit(records);
        assertEquals(1.0, lr.getSlope(), 0.001);
        assertEquals(20.0, lr.getIntercept(), 0.001);
    }

    @Test
    public void testPredictNextWeek() {
        List<BmiRecord> records = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            BmiRecord r = new BmiRecord();
            r.setBmi(22 + i * 0.5);
            records.add(r);
        }
        LinearRegression lr = new LinearRegression();
        lr.fit(records);
        double predicted = lr.predictNextWeek(4);
        assertTrue(predicted > 0);
    }
}
