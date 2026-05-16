package com.nutrifit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sport_plan")
public class SportPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "plan_name")
    private String planName;

    @Column(name = "plan_duration")
    private String planDuration;

    @Column(name = "plan_focus")
    private String planFocus;

    @Column(name = "prompt_txt", columnDefinition = "LONGTEXT")
    private String promptTxt;

    @Column(name = "ai_response", columnDefinition = "LONGTEXT")
    private String aiResponse;

    // N:1 İlişki (SportPlan -> User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // N:1 İlişki (SportPlan -> Goal)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    @JsonIgnore
    private Goal goal;

    // 1:N İlişki (SportPlan -> Exercise)
    @OneToMany(mappedBy = "sportPlan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Exercise> exercise;
}
