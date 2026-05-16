package com.nutrifit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "diyet_plan")
public class DiyetPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "plan_name")
    private String planName;

    @Column(name = "prompt_txt", columnDefinition = "LONGTEXT")
    private String promptTxt;

    @Column(name = "ai_response", columnDefinition = "LONGTEXT")
    private String aiResponse;

    // N:1 İlişki (DiyetPlan -> User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // N:1 İlişki (DiyetPlan -> Goal)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    @JsonIgnore
    private Goal goal;

    // 1:N İlişki (DiyetPlan -> Meal)
    @OneToMany(
            mappedBy = "diyetPlan",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private List<Meal> meals;
}
