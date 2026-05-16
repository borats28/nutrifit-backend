package com.nutrifit.service;

import com.nutrifit.entity.Gender;
import com.nutrifit.entity.Measurement;
import com.nutrifit.entity.Calculations;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CalculationServiceTest {

    private final CalculationService calculationService = new CalculationService();

    @Test
    public void testPerformCalculations_Male() {
        Measurement measurement = new Measurement();
        measurement.setKilo(80.0);
        measurement.setBoy(180.0);
        measurement.setYas(25);
        measurement.setCinsiyet(Gender.MALE);

        Calculations result = calculationService.performCalculations(measurement);

        assertNotNull(result);
        assertEquals(24.69, result.getBmiValue()); // 80 / (1.8 * 1.8) = 24.6913...
        assertEquals(1805.0, result.getBmrValue()); // (10*80) + (6.25*180) - (5*25) + 5 = 800 + 1125 - 125 + 5 = 1805
        assertEquals(19.18, result.getBodyFat()); // (1.20 * 24.69) + (0.23 * 25) - (10.8 * 1) - 5.4 = 29.628 + 5.75 - 10.8 - 5.4 = 19.178
    }

    @Test
    public void testPerformCalculations_Female() {
        Measurement measurement = new Measurement();
        measurement.setKilo(60.0);
        measurement.setBoy(165.0);
        measurement.setYas(30);
        measurement.setCinsiyet(Gender.FEMALE);

        Calculations result = calculationService.performCalculations(measurement);

        assertNotNull(result);
        assertEquals(22.04, result.getBmiValue()); // 60 / (1.65 * 1.65) = 22.038...
        assertEquals(1320.25, result.getBmrValue()); // (10*60) + (6.25*165) - (5*30) - 161 = 600 + 1031.25 - 150 - 161 = 1320.25
        assertEquals(27.95, result.getBodyFat()); // (1.20 * 22.04) + (0.23 * 30) - (10.8 * 0) - 5.4 = 26.448 + 6.9 - 5.4 = 27.948
    }
}
