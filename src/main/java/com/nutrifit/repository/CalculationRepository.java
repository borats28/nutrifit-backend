package com.nutrifit.repository;

import com.nutrifit.entity.Calculations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalculationRepository extends JpaRepository<Calculations, Long> {

}
