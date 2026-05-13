package com.nutrifit.repository;

import com.nutrifit.entity.Goal;
import com.nutrifit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    // Kullanıcının TÜM hedeflerini yeniden eskiye sıralı getirir (List döner)
    List<Goal> findByUserOrderByGoalIdDesc(User user);

    // Kullanıcı ID'si ile çağırma
    List<Goal> findByUserUserIdOrderByGoalIdDesc(Long userId);

    // Sadece EN SON eklenen tek bir hedefi getirir (Optional döner)
    Optional<Goal> findTopByUserOrderByGoalIdDesc(User user);
}