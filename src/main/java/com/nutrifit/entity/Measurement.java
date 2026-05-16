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
@Table(name = "measurements")
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "measurement_id")
    private Long measurementId;

    @Column(name = "measurement_date")
    private java.time.LocalDate measurementDate;

    @Column(name = "boy")
    private Double boy;

    @Column(name = "kilo")
    private Double kilo;

    @Column(name = "yas")
    private int yas;

    @Enumerated(EnumType.STRING)
    @Column(name = "cinsiyet")
    private Gender cinsiyet;

    @Column(name = "resim")
    private String resim; // resmin sunucuda ki URL'si

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "fitness_level")
    private String fitnessLevel;

    @OneToOne(
            mappedBy = "measurement",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private Calculations calculations;
}
