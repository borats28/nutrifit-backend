package com.nutrifit.controller;

import com.nutrifit.entity.BloodTest;
import com.nutrifit.entity.User;
import com.nutrifit.repository.BloodTestRepository;
import com.nutrifit.repository.UserRepository;
import com.nutrifit.security.JwtUtils;
import com.nutrifit.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/blood-test")
public class BloodTestController {

    @Autowired
    AiService aiService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BloodTestRepository bloodTestRepository;
    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadBloodTest(@RequestParam("image") MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        BloodTest result = aiService.kanTahliliAnalizEt(file, user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<BloodTest>> getBloodTestHistory(@RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        List<BloodTest> history = bloodTestRepository.findByUserOrderByUploadDateDesc(user);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestTest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        List<BloodTest> tests = bloodTestRepository.findByUserOrderByUploadDateDesc(user);
        return tests.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(tests.get(0));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateBloodTest(@PathVariable Long id, @RequestBody BloodTest updatedTest) {
        BloodTest existingTest = bloodTestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tahlil bulunamadı"));

        existingTest.setAnalysisResult(updatedTest.getAnalysisResult());

        bloodTestRepository.save(existingTest);
        return ResponseEntity.ok("Güncellendi.");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBloodTest(@PathVariable Long id) {
        bloodTestRepository.deleteById(id);
        return ResponseEntity.ok("Silindi.");
    }
}