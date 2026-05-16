package com.nutrifit.service;

import com.nutrifit.entity.*;
import com.nutrifit.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiServiceTest {

    @Mock
    private DiyetPlanRepository diyetPlanRepository;
    @Mock
    private SportPlanRepository sportPlanRepository;
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private MeasurementRepository measurementRepository;
    @Mock
    private HealthRepository healthRepository;
    @Mock
    private BloodTestRepository bloodTestRepository;
    @Mock
    private CalculationService calculationService;
    @Mock
    private RestTemplate restTemplate;

    private AiService aiService;

    @BeforeEach
    public void setup() {
        aiService = new AiService(restTemplate);
        ReflectionTestUtils.setField(aiService, "diyetPlanRepository", diyetPlanRepository);
        ReflectionTestUtils.setField(aiService, "sportPlanRepository", sportPlanRepository);
        ReflectionTestUtils.setField(aiService, "goalRepository", goalRepository);
        ReflectionTestUtils.setField(aiService, "measurementRepository", measurementRepository);
        ReflectionTestUtils.setField(aiService, "healthRepository", healthRepository);
        ReflectionTestUtils.setField(aiService, "bloodTestRepository", bloodTestRepository);
        ReflectionTestUtils.setField(aiService, "calculationService", calculationService);
        ReflectionTestUtils.setField(aiService, "geminiApiKey", "test-key");
    }

    @Test
    public void testDiyetPlaniOlustur_Success() {
        User user = new User();
        user.setUserId(1L);
        Goal goal = new Goal();
        Measurement measurement = new Measurement();
        Calculations calcs = new Calculations();
        
        when(goalRepository.findTopByUserOrderByGoalIdDesc(user)).thenReturn(Optional.of(goal));
        when(measurementRepository.findTopByUserOrderByMeasurementDateDesc(user)).thenReturn(Optional.of(measurement));
        when(calculationService.performCalculations(measurement)).thenReturn(calcs);
        when(healthRepository.findByUserUserId(1L)).thenReturn(Collections.emptyList());
        when(bloodTestRepository.findByUserUserIdOrderByUploadDateDesc(1L)).thenReturn(Collections.emptyList());
        when(diyetPlanRepository.findByUserUserIdOrderByPlanIdDesc(1L)).thenReturn(Collections.emptyList());

        String mockResponse = "{\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Diyet Planı İçeriği\"}]}}]}";
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(ResponseEntity.ok(mockResponse));
        when(diyetPlanRepository.save(any(DiyetPlan.class))).thenAnswer(i -> i.getArguments()[0]);

        DiyetPlan result = aiService.diyetPlaniOlustur(user);

        assertNotNull(result);
        assertEquals("Diyet Planı İçeriği", result.getAiResponse());
        verify(diyetPlanRepository).save(any(DiyetPlan.class));
    }

    @Test
    public void testSporPlaniOlustur_Success() {
        User user = new User();
        user.setUserId(1L);
        Goal goal = new Goal();
        goal.setHedefKilo(70.0);
        Measurement measurement = new Measurement();
        measurement.setKilo(80.0);
        
        when(goalRepository.findTopByUserOrderByGoalIdDesc(user)).thenReturn(Optional.of(goal));
        when(measurementRepository.findTopByUserOrderByMeasurementDateDesc(user)).thenReturn(Optional.of(measurement));
        when(healthRepository.findByUserUserId(1L)).thenReturn(Collections.emptyList());

        String mockResponse = "{\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Spor Planı İçeriği\"}]}}]}";
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(ResponseEntity.ok(mockResponse));
        when(sportPlanRepository.save(any(SportPlan.class))).thenAnswer(i -> i.getArguments()[0]);

        SportPlan result = aiService.sporPlaniOlustur(user);

        assertNotNull(result);
        assertEquals("Spor Planı İçeriği", result.getAiResponse());
        assertEquals("Yağ Yakımı & Kondisyon", result.getPlanFocus());
    }

    @Test
    public void testKocIleSohbetEt_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setUserId(1L);
        
        when(goalRepository.findTopByUserOrderByGoalIdDesc(user)).thenReturn(Optional.of(new Goal()));
        when(diyetPlanRepository.findByUserUserIdOrderByPlanIdDesc(1L)).thenReturn(Collections.emptyList());

        String mockResponse = "{\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Koç Yanıtı\"}]}}]}";
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(ResponseEntity.ok(mockResponse));

        String response = aiService.kocIleSohbetEt(user, "Merhaba");

        assertEquals("Koç Yanıtı", response);
    }

    @Test
    public void testVucutAnaliziYap_Success() throws Exception {
        User user = new User();
        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "image content".getBytes());
        
        when(goalRepository.findTopByUserOrderByGoalIdDesc(user)).thenReturn(Optional.empty());
        when(measurementRepository.findTopByUserOrderByMeasurementDateDesc(user)).thenReturn(Optional.empty());

        String mockResponse = "{\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Vücut Analizi\"}]}}]}";
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(ResponseEntity.ok(mockResponse));

        String result = aiService.vucutAnaliziYap(file, user);

        assertEquals("Vücut Analizi", result);
    }

    @Test
    public void testKanTahliliAnalizEt_Success() throws Exception {
        User user = new User();
        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "image content".getBytes());

        String mockResponse = "{\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Kan Analizi\"}]}}]}";
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(ResponseEntity.ok(mockResponse));
        when(bloodTestRepository.save(any(BloodTest.class))).thenAnswer(i -> i.getArguments()[0]);

        BloodTest result = aiService.kanTahliliAnalizEt(file, user);

        assertNotNull(result);
        assertEquals("Kan Analizi", result.getAnalysisResult());
    }

    @Test
    public void testDiyetPlaniOlustur_NoGoal() {
        User user = new User();
        when(goalRepository.findTopByUserOrderByGoalIdDesc(user)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> aiService.diyetPlaniOlustur(user));
    }

    @Test
    public void testDiyetPlaniOlustur_NoMeasurement() {
        User user = new User();
        when(goalRepository.findTopByUserOrderByGoalIdDesc(user)).thenReturn(Optional.of(new Goal()));
        when(measurementRepository.findTopByUserOrderByMeasurementDateDesc(user)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> aiService.diyetPlaniOlustur(user));
    }

    @Test
    public void testSporPlaniOlustur_NoGoal() {
        User user = new User();
        when(goalRepository.findTopByUserOrderByGoalIdDesc(user)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> aiService.sporPlaniOlustur(user));
    }

    @Test
    public void testSporPlaniOlustur_NoMeasurement() {
        User user = new User();
        when(goalRepository.findTopByUserOrderByGoalIdDesc(user)).thenReturn(Optional.of(new Goal()));
        when(measurementRepository.findTopByUserOrderByMeasurementDateDesc(user)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> aiService.sporPlaniOlustur(user));
    }
}
