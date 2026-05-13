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
@Table(name = "meals")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_id")
    private Long mealId;

    @Column(name = "gun_sayisi")
    private int gunSayisi;

    @Enumerated(EnumType.STRING) // Enum'ı veritabanında 'BREAKFAST' gibi metin olarak saklaması için
    @Column(name = "meal_type", nullable = false)
    private MealType mealType; // Kahvaltı, Öğle Yemeği

    @Column(name = "meal_contents")
    private String mealContents;

    @Column(name = "estimated_calori")
    private int estimatedCalori;

    // N:1 İlişki (Meal -> DiyetPlan)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @JsonIgnore
    private DiyetPlan diyetPlan;
}
