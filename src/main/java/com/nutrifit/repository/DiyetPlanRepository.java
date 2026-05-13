package com.nutrifit.repository;

import com.nutrifit.entity.DiyetPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiyetPlanRepository extends JpaRepository<DiyetPlan, Long> {
    // Metod ismindeki 'PlanId' kısmı Entity'deki 'planId' alanı ile aynı olmalı.
    List<DiyetPlan> findByUserUserIdOrderByPlanIdDesc(Long userId);
}