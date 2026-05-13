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

    // SAĞLIK KAYDI EKLEME
    @PostMapping("/add")
    public ResponseEntity<?> addHealthRecord(@RequestBody Health health, @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        health.setUser(user);
        // Eğer tarih gönderilmediyse bugünü al
        if (health.getDate() == null) {
            health.setDate(java.time.LocalDate.now());
        }
        healthRepository.save(health);

        return ResponseEntity.ok("Sağlık kaydı başarıyla eklendi.");
    }

    // SAĞLIK GEÇMİŞİNİ LİSTELEME
    @GetMapping("/list")
    public ResponseEntity<?> getHealthRecords(@RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        List<Health> records = healthRepository.findByUserUserId(user.getUserId());
        return ResponseEntity.ok(records);
    }

    // SİLME İŞLEMİ
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteHealth(@PathVariable Long id) {
        if (healthRepository.existsById(id)) {
            healthRepository.deleteById(id);
            return ResponseEntity.ok("Sağlık kaydı silindi.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}