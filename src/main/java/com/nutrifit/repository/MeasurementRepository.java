package com.nutrifit.repository;

import com.nutrifit.entity.Measurement;
import com.nutrifit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    // Entity'de "measurementId" olduğu için "OrderByMeasurementIdDesc" yazmalıyız.
    List<Measurement> findByUserUserIdOrderByMeasurementIdDesc(Long userId);

    Optional<Measurement> findTopByUserOrderByMeasurementDateDesc(User user);
}