package com.nutrifit.repository;

import com.nutrifit.entity.Health;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthRepository extends JpaRepository<Health, Long> {
    //Kullanıcının tüm sağlık kayıtlarını getir.
    List<Health> findByUserUserId(Long userId);
}
