package com.nutrifit.controller;

import com.nutrifit.entity.Goal;
import com.nutrifit.entity.User;
import com.nutrifit.repository.GoalRepository;
import com.nutrifit.repository.UserRepository;
import com.nutrifit.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    @Autowired
    GoalRepository goalRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/add")
    public ResponseEntity<?> addGoal(@RequestBody Goal goal, @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        goal.setUser(user);
        goalRepository.save(goal);
        return ResponseEntity.ok("Hedef başarıyla eklendi.");
    }

    @GetMapping("/my-goals")
    public ResponseEntity<?> getMyGoals(@RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        List<Goal> goals = goalRepository.findByUserUserIdOrderByGoalIdDesc(user.getUserId());
        return ResponseEntity.ok(goals);
    }
}