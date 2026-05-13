package com.nutrifit.payload.request;

import lombok.Data;

@Data
public class MeasurementRequest {
    private Double boy; // cm cinsinden
    private Double kilo; // kg cinsinden
    private Integer yas;
    private String cinsiyet; // "MALE" veya "FEMALE" olarak gelecek
    private String fitnessLevel; // Yeni: "BEGINNER", "INTERMEDIATE", "ADVANCED"
}
