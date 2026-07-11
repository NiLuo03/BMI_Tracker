package com.bmitracker;

import com.bmitracker.service.BmiService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BmiServiceTest {

    private final BmiService service = new BmiService();

    @Test
    public void testCalculateBMI() {
        double bmi = service.calculateBMI(175, 70);
        assertEquals(22.9, bmi, 0.1);
    }

    @Test
    public void testGetHealthStatus() {
        assertEquals("偏瘦", service.getHealthStatus(17.5));
        assertEquals("正常", service.getHealthStatus(21.0));
        assertEquals("超重", service.getHealthStatus(26.0));
        assertEquals("肥胖", service.getHealthStatus(30.0));
    }

    @Test
    public void testBmiBoundary() {
        assertEquals("正常", service.getHealthStatus(18.5));
        assertEquals("超重", service.getHealthStatus(24.0));
        assertEquals("肥胖", service.getHealthStatus(28.0));
    }
}
