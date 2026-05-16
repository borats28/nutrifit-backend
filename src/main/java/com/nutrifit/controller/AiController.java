package com.nutrifit.controller;

import com.nutrifit.entity.User;
import com.nutrifit.repository.UserRepository;
import com.nutrifit.security.JwtUtils;
import com.nutrifit.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/chat")
public class AiController {

    @Autowired
    private AiService aiService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/send")
    public ResponseEntity<?> chatWithAi(@RequestBody Map<String, String> payload,
                                        @RequestHeader("Authorization") String token) {
        try {

            User user = getUserFromToken(token);
            String userMessage = payload.get("message");
            String aiResponse = aiService.kocIleSohbetEt(user, userMessage);

            return ResponseEntity.ok(Map.of("response", aiResponse));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Yapay zeka yanıt veremedi: " + e.getMessage());
        }
    }

    private User getUserFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String username = jwtUtils.getUserNameFromJwtToken(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));
    }

    @PostMapping("/analyze-body")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public ResponseEntity<?> analyzeBody(@RequestParam("image") MultipartFile file,
                                         @RequestHeader("Authorization") String token) {
        try {
            User user = getUserFromToken(token);
            String analysis = aiService.vucutAnaliziYap(file, user);
            return ResponseEntity.ok(Map.of("analysis", analysis));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Analiz hatası: " + e.getMessage());
        }
    }
}