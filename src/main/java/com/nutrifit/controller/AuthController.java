package com.nutrifit.controller;

import com.nutrifit.entity.User;
import com.nutrifit.payload.request.LoginRequest;
import com.nutrifit.payload.request.SignupRequest;
import com.nutrifit.payload.response.JwtResponse;
import com.nutrifit.payload.response.MessageResponse;
import com.nutrifit.repository.UserRepository;
import com.nutrifit.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600) // Frontend (React) farklı portta olacağı için izin veriyoruz
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    // Kayıt olma
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        //Kullanıcı adı alınmış mı kontrol et
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Hata: Bu kullanıcı adı zaten kullanımda!"));
        }

        //Email alınmış mı kontrol et
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Hata: Bu email zaten kullanımda!"));
        }

        //Yeni kullanıcı oluştur
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword())); // Şifreyi hash'le (Bcrypt)

        //Veritabanına kaydet
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Kullanıcı başarıyla kaydedildi!"));
    }

    // Giriş yapma
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();

        List<String> hashPermissions = List.of("read_dashboard", "create_plan", "upload_blood_test");

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getUserId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                hashPermissions));
    }
}