package com.nutrifit.repository;

import com.nutrifit.entity.BloodTest;
import com.nutrifit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodTestRepository extends JpaRepository<BloodTest, Long> {

    List<BloodTest> findByUserUserIdOrderByUploadDateDesc(Long userId);

    List<BloodTest> findByUserOrderByUploadDateDesc(User user);
}
