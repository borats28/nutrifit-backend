package com.nutrifit.service;

import com.nutrifit.entity.Calculations;
import com.nutrifit.entity.Gender;
import com.nutrifit.entity.Measurement;
import org.springframework.stereotype.Service;

@Service
public class CalculationService {

    public Calculations performCalculations(Measurement measurement) {
        Calculations calcs = new Calculations();

        // BMI Hesapla
        double bmi = calculateBMI(measurement.getKilo(), measurement.getBoy());
        calcs.setBmiValue(formatDouble(bmi));

        // BMR Hesapla (Mifflin-St Jeor Formülü)
        double bmr = calculateBMR(measurement.getKilo(), measurement.getBoy(), measurement.getYas(), measurement.getCinsiyet());
        calcs.setBmrValue(formatDouble(bmr));

        // Vücut Yağ Oranı Hesapla (Deurenberg Formülü)
        double bodyFat = calculateBodyFat(bmi, measurement.getYas(), measurement.getCinsiyet());
        calcs.setBodyFat(formatDouble(bodyFat));

        // İlişki kur
        calcs.setMeasurement(measurement);

        return calcs;
    }

    private double calculateBMI(Double weight, Double heightCm) {
        if (heightCm == 0) return 0;
        double heightM = heightCm / 100.0;
        return weight / (heightM * heightM);
    }

    private double calculateBMR(Double weight, Double heightCm, Integer age, Gender gender) {
        // Mifflin-St Jeor Denklemi
        // Erkek: (10 x kilo) + (6.25 x boy) - (5 x yaş) + 5
        // Kadın: (10 x kilo) + (6.25 x boy) - (5 x yaş) - 161
        double s = (gender == Gender.MALE) ? 5 : -161;
        return (10 * weight) + (6.25 * heightCm) - (5 * age) + s;
    }

    private double calculateBodyFat(double bmi, Integer age, Gender gender) {
        // Deurenberg Formülü
        int sexValue = (gender == Gender.MALE) ? 1 : 0;
        return (1.20 * bmi) + (0.23 * age) - (10.8 * sexValue) - 5.4;
    }

    private Double formatDouble(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}