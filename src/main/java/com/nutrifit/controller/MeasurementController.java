package com.nutrifit.controller;

import com.nutrifit.entity.Calculations;
import com.nutrifit.entity.Gender;
import com.nutrifit.entity.Measurement;
import com.nutrifit.entity.User;
import com.nutrifit.payload.request.MeasurementRequest;
import com.nutrifit.payload.response.MessageResponse;
import com.nutrifit.repository.CalculationRepository;
import com.nutrifit.repository.MeasurementRepository;
import com.nutrifit.repository.UserRepository;
import com.nutrifit.service.CalculationService;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/measurements")
public class MeasurementController {

    @Autowired
    MeasurementRepository measurementRepository;

    @Autowired
    CalculationRepository calculationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CalculationService calculationService;

    @PostMapping("/add")
    public ResponseEntity<?> addMeasurement(@RequestBody MeasurementRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı"));

        Measurement measurement = getMeasurement(request, user);
        Measurement savedMeasurement = measurementRepository.save(measurement);
        Calculations calculations = calculationService.performCalculations(savedMeasurement);
        calculationRepository.save(calculations);

        return ResponseEntity.ok(new MessageResponse("Ölçüm ve hesaplamalar başarıyla kaydedildi!"));
    }

    @Nonnull
    private static Measurement getMeasurement(MeasurementRequest request, User user) {
        Measurement measurement = new Measurement();
        measurement.setBoy(request.getBoy());
        measurement.setKilo(request.getKilo());
        measurement.setYas(request.getYas());
        measurement.setUser(user);
        measurement.setFitnessLevel(request.getFitnessLevel());

        if ("MALE".equalsIgnoreCase(request.getCinsiyet())) {
            measurement.setCinsiyet(Gender.MALE);
        } else {
            measurement.setCinsiyet(Gender.FEMALE);
        }
        return measurement;
    }

    @GetMapping("/history")
    public ResponseEntity<List<Measurement>> getUserHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        List<Measurement> history = measurementRepository.findByUserUserIdOrderByMeasurementIdDesc(user.getUserId());

        return ResponseEntity.ok(history);
    }
}