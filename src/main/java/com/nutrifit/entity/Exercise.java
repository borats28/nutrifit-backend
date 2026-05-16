package com.nutrifit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "exercises")
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_id")
    private Long exerciseId;

    @Column(name = "day_week")
    private String dayweek;

    @Column(name = "exercise_name")
    private String exerciseName;

    @Column(name = "set_count")
    private int setCount;

    @Column(name = "number_repetitions")
    private String numberRepetitions;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // N:1 İlişki (Exercise -> SportPlan)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @JsonIgnore
    private SportPlan sportPlan;

}
