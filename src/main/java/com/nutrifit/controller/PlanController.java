package com.nutrifit.controller;

import com.nutrifit.entity.*;
import com.nutrifit.repository.*;
import com.nutrifit.security.JwtUtils;
import com.nutrifit.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/plans")
public class PlanController {

    @Autowired
    private AiService aiService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiyetPlanRepository diyetPlanRepository;
    @Autowired
    private SportPlanRepository sportPlanRepository;
    @Autowired
    private GoalRepository goalRepository;
    @Autowired
    private MeasurementRepository measurementRepository;


    @PostMapping("/diet")
    public ResponseEntity<?> createDietPlan(@RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);

            Optional<Goal> goal = goalRepository.findTopByUserOrderByGoalIdDesc(user);

            Optional<Measurement> measure = measurementRepository.findTopByUserOrderByMeasurementDateDesc(user);

            if (goal.isEmpty() || measure.isEmpty()) {
                return ResponseEntity.badRequest().body("Önce hedef ve vücut ölçülerinizi girmelisiniz.");
            }


            DiyetPlan plan = aiService.diyetPlaniOlustur(user);
            return ResponseEntity.ok(plan);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/diet/latest")
    public ResponseEntity<?> getLatestDiet(@RequestHeader("Authorization") String token) {
        User user = getUserFromToken(token);
        List<DiyetPlan> plans = diyetPlanRepository.findByUserUserIdOrderByPlanIdDesc(user.getUserId());

        if (plans.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(plans.get(0));
    }

    @PostMapping("/workout")
    public ResponseEntity<?> createWorkoutPlan(@RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            Optional<Goal> goal = goalRepository.findTopByUserOrderByGoalIdDesc(user);

            Optional<Measurement> measure = measurementRepository.findTopByUserOrderByMeasurementDateDesc(user);

            if (goal.isEmpty() || measure.isEmpty()) {
                return ResponseEntity.badRequest().body("Önce hedef ve vücut ölçülerinizi girmelisiniz.");
            }

            SportPlan plan = aiService.sporPlaniOlustur(user);
            return ResponseEntity.ok(plan);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/workout/latest")
    public ResponseEntity<?> getLatestWorkout(@RequestHeader("Authorization") String token) {
        User user = getUserFromToken(token);
        List<SportPlan> plans = sportPlanRepository.findByUserUserIdOrderByPlanIdDesc(user.getUserId());

        if (plans.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(plans.get(0));
    }

    private User getUserFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String username = jwtUtils.getUserNameFromJwtToken(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }
}