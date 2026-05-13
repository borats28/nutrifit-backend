package com.nutrifit.repository;

import com.nutrifit.entity.SportPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SportPlanRepository extends JpaRepository<SportPlan, Long> {
    List<SportPlan> findByUserUserIdOrderByPlanIdDesc(Long userId);
}