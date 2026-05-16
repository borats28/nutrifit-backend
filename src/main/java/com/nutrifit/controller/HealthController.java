package com.nutrifit.controller;

import com.nutrifit.entity.Health;
import com.nutrifit.entity.User;
import com.nutrifit.repository.HealthRepository;
import com.nutrifit.repository.UserRepository;
import com.nutrifit.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    HealthRepository healthRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/add")
    public ResponseEntity<?> addHealthRecord(@RequestBody Health health, @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        health.setUser(user);
        if (health.getDate() == null) {
            health.setDate(java.time.LocalDate.now());
        }
        healthRepository.save(health);

        return ResponseEntity.ok("Sağlık kaydı başarıyla eklendi.");
    }

    @GetMapping("/list")
    public ResponseEntity<?> getHealthRecords(@RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        List<Health> records = healthRepository.findByUserUserId(user.getUserId());
        return ResponseEntity.ok(records);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteHealth(@PathVariable Long id) {
        if (healthRepository.existsById(id)) {
            healthRepository.deleteById(id);
            return ResponseEntity.ok("Sağlık kaydı silindi.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateHealth(@PathVariable Long id, @RequestBody Health healthDetails, @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        return healthRepository.findById(id).map(existingHealth -> {

            if (!existingHealth.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(403).body("Bu kaydı güncelleme yetkiniz yok.");
            }

            existingHealth.setNotes(healthDetails.getNotes());
            healthRepository.save(existingHealth);

            return ResponseEntity.ok("Sağlık kaydı başarıyla güncellendi.");

        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}