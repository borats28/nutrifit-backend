package com.nutrifit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "calculations")
public class Calculations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calculations_id")
    private Long CalculationsId;

    @Column(name = "bmi_value")
    private Double bmiValue; // Vucut kitle indeksi

    @Column(name = "bmr_value")
    private Double bmrValue; // Vucut bazal metobolizma hızı

    @Column(name = "body_fat")
    private Double bodyFat; // Vucut yağ oranı

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "measurement_id", nullable = false, unique = true)
    @JsonIgnore
    private Measurement measurement;
}
