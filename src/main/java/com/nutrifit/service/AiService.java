package com.nutrifit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrifit.entity.*;
import com.nutrifit.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class AiService {

    @Autowired
    private DiyetPlanRepository diyetPlanRepository;

    @Autowired
    private SportPlanRepository sportPlanRepository;

    @Autowired
    private HealthRepository healthRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private MeasurementRepository measurementRepository;

    @Autowired
    private BloodTestRepository bloodTestRepository;

    @Autowired
    private CalculationService calculationService;

    @Value("${nutrifit.app.geminiApiKey}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    // DİYET PLANI OLUŞTURMA (Otomatik Veri Çekme)
    public DiyetPlan diyetPlaniOlustur(User user) {
        // EN GÜNCEL VERİLERİ ÇEK
        Goal goal = goalRepository.findTopByUserOrderByGoalIdDesc(user)
                .orElseThrow(() -> new RuntimeException("Aktif bir hedefiniz yok. Lütfen önce hedef belirleyin."));

        Measurement measurement = measurementRepository.findTopByUserOrderByMeasurementDateDesc(user)
                .orElseThrow(() -> new RuntimeException("Hiç vücut ölçümü girmemişsiniz. Profil sayfasından ekleyiniz."));

        Calculations calcs = calculationService.performCalculations(measurement);
        List<Health> healthRecords = healthRepository.findByUserUserId(user.getUserId());

        // Kan Tahlili (En son yüklenen)
        List<BloodTest> bloodTests = bloodTestRepository.findByUserUserIdOrderByUploadDateDesc(user.getUserId());
        BloodTest latestBloodTest = bloodTests.isEmpty() ? null : bloodTests.get(0);

        // Eski Diyet Özeti (AI hafızası için)
        List<DiyetPlan> oldPlans = diyetPlanRepository.findByUserUserIdOrderByPlanIdDesc(user.getUserId());
        String oldDietSummary = oldPlans.isEmpty() ? "" : oldPlans.get(0).getAiResponse();

        // PROMPT HAZIRLA
        String prompt = prepareDietPrompt(user, goal, measurement, calcs, healthRecords, latestBloodTest, oldDietSummary);

        // AI ÇAĞRISI
        String aiResponse = callGeminiApi(prompt);

        // KAYDET
        DiyetPlan plan = new DiyetPlan();
        plan.setPlanName("Akıllı Beslenme Planı - " + java.time.LocalDate.now());
        plan.setUser(user);
        plan.setGoal(goal);
        plan.setPromptTxt(prompt);
        plan.setAiResponse(aiResponse);

        return diyetPlanRepository.save(plan);
    }
    // SPOR PLANI OLUŞTURMA (Otomatik Veri Çekme)

    public SportPlan sporPlaniOlustur(User user) {
        // VERİLERİ ÇEK
        Goal goal = goalRepository.findTopByUserOrderByGoalIdDesc(user)
                .orElseThrow(() -> new RuntimeException("Spor planı için önce hedef belirlemelisiniz."));

        Measurement measurement = measurementRepository.findTopByUserOrderByMeasurementDateDesc(user)
                .orElseThrow(() -> new RuntimeException("Vücut ölçülerinizi girmeden size uygun program yazamayız."));

        List<Health> healthRecords = healthRepository.findByUserUserId(user.getUserId());

        // PROMPT HAZIRLA
        String prompt = prepareWorkoutPrompt(user, goal, measurement, healthRecords);

        // AI ÇAĞRISI
        String aiResponse = callGeminiApi(prompt);

        // KAYDET
        SportPlan plan = new SportPlan();
        plan.setPlanName("Kişisel Antrenman Programı");

        // Arayüz ve veritabanı için Türkçe metin
        String focus = (goal.getHedefKilo() < measurement.getKilo()) ? "Yağ Yakımı & Kondisyon" : "Kas Kütlesi & Güç";
        plan.setPlanFocus(focus);
        plan.setPlanDuration("1 Haftalık Döngü");
        plan.setUser(user);
        plan.setGoal(goal);
        plan.setPromptTxt(prompt);
        plan.setAiResponse(aiResponse); // JSON formatında dönecek cevabı buraya basıyoruz

        return sportPlanRepository.save(plan);
    }

    // ============================================================================================
    // KOÇ İLE SOHBET (Mevcut Planları Bilerek Cevap Verir)
    // ============================================================================================
    public String kocIleSohbetEt(User user, String userMessage) {
        StringBuilder context = new StringBuilder();
        context.append("KULLANICI: ").append(user.getUsername()).append("\n");

        // Son verileri hafızaya al
        Optional<Goal> goal = goalRepository.findTopByUserOrderByGoalIdDesc(user);
        goal.ifPresent(g -> context.append("- Hedef: ").append(g.getHedefKilo()).append("kg.\n"));

        List<DiyetPlan> dietPlans = diyetPlanRepository.findByUserUserIdOrderByPlanIdDesc(user.getUserId());
        if (!dietPlans.isEmpty()) {
            String summary = dietPlans.get(0).getAiResponse();
            if (summary.length() > 1000) summary = summary.substring(0, 1000) + "..."; // Çok uzunsa kes
            context.append("- Mevcut Diyeti: ").append(summary).append("\n");
        }

        String prompt = "ROL: Sen NutriFit uygulamasının yapay zeka koçusun.\n" + "BAĞLAM (Context): " + context.toString() + "\n" + "KULLANICI SORUSU: \"" + userMessage + "\"\n" + "GÖREV: Kullanıcının verilerini ve mevcut planını bilerek, samimi ve motive edici bir cevap ver.";

        return callGeminiApi(prompt);
    }

    // ============================================================================================
    // ANALİZ METODLARI (Resim Yükleme)
    // ============================================================================================

    public String vucutAnaliziYap(MultipartFile imageFile, User user) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());

            // ✅ goal'u veritabanından çekiyoruz
            String hedefBilgisi = goalRepository.findTopByUserOrderByGoalIdDesc(user)
                    .map(g -> g.getHedefKilo() + "kg")
                    .orElse("belirtilmemiş");

            // ✅ Ölçüm bilgisi de ekleyelim
            String olcumBilgisi = measurementRepository.findTopByUserOrderByMeasurementDateDesc(user)
                    .map(m -> m.getKilo() + "kg, " + m.getFitnessLevel() + " seviye")
                    .orElse("belirtilmemiş");

            String prompt = "Bu vücut fotoğrafını analiz et.\n" +
                    "Kullanıcı bilgileri: Mevcut durum: " + olcumBilgisi + ", Hedef: " + hedefBilgisi + "\n" +
                    "Şunları analiz et:\n" +
                    "1. Tahmini vücut yağ oranı\n" +
                    "2. Hangi bölgelere odaklanmalı\n" +
                    "3. Genel vücut tipi değerlendirmesi\n" +
                    "4. Kişisel öneriler\n" +
                    "Kısa, net ve motive edici maddeler halinde Türkçe yaz.";

            return callGeminiApiWithImage(prompt, base64Image);
        } catch (Exception e) {
            return "Resim analizinde hata: " + e.getMessage();
        }
    }

    public BloodTest kanTahliliAnalizEt(MultipartFile imageFile, User user) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            String prompt = "Bu bir kan tahlili sonucudur. Lütfen referans dışı (Yüksek/Düşük) değerleri bul " + "ve bunlar için beslenme tavsiyesi ver. JSON formatı kullanma, düzgün bir metin olarak yaz.";

            String analysis = callGeminiApiWithImage(prompt, base64Image);

            BloodTest bloodTest = new BloodTest();
            bloodTest.setUploadDate(java.time.LocalDate.now());
            bloodTest.setUser(user);
            bloodTest.setAnalysisResult(analysis);

            return bloodTestRepository.save(bloodTest);
        } catch (Exception e) {
            throw new RuntimeException("Tahlil analizi hatası: " + e.getMessage());
        }
    }

    // YARDIMCI METODLAR (PROMPT MÜHENDİSLİĞİ)

    private String prepareDietPrompt(User user, Goal goal, Measurement measurement, Calculations calcs, List<Health> healthRecords, BloodTest bloodTest, String oldDietSummary) {
        StringBuilder sb = new StringBuilder();
        sb.append("ROL: Sen uzman bir diyetisyensin. Türk mutfağına hakimsin.\n");
        sb.append("GÖREV: Aşağıdaki danışan için **1 HAFTALIK (Pazartesi-Pazar)** detaylı bir beslenme planı oluştur. Ayrıca, hiçbir resim kullanma.\n\n");

        sb.append("DANIŞAN PROFİLİ:\n");
        sb.append("- Cinsiyet/Yaş: ").append(measurement.getCinsiyet()).append(", ").append(measurement.getYas()).append("\n");
        sb.append("- Mevcut Kilo: ").append(measurement.getKilo()).append(" kg\n");
        sb.append("- Hedef Kilo: ").append(goal.getHedefKilo()).append(" kg\n");
        sb.append("- BMR (Bazal Metabolizma): ").append(calcs.getBmrValue()).append(" kalori\n");
        sb.append("- Aktivite/Fitness Seviyesi: ").append(measurement.getFitnessLevel()).append("\n");

        // --- KAN TAHLİLİ MANTIĞI ---
        if (bloodTest != null && bloodTest.getAnalysisResult() != null) {
            sb.append("\n⚠️ ÖNEMLİ - KAN TAHLİLİ BULGULARI:\n");
            sb.append(bloodTest.getAnalysisResult()).append("\n");
            sb.append("LÜTFEN BU BULGULARA GÖRE YASAKLI VEYA ÖNERİLEN GIDALARI SEÇ.\n");
        }

        // --- SAĞLIK GEÇMİŞİ ---
        if (!healthRecords.isEmpty()) {
            sb.append("\n⚠️ SAĞLIK NOTLARI / HASTALIKLAR:\n");
            for (Health h : healthRecords) sb.append("- ").append(h.getNotes()).append("\n");
        }

        sb.append("\nÇIKTI FORMATI:\n");
        sb.append("Lütfen cevabı JSON formatında verme. Markdown kullanarak, Gün Gün, Öğün Öğün (Kahvaltı, Öğle, Akşam, Ara) listele.\n");
        sb.append("Her yemeğin yanına yaklaşık kalorisini yaz.\n");

        return sb.toString();
    }

    private String prepareWorkoutPrompt(User user, Goal goal, Measurement measurement, List<Health> healthRecords) {
        StringBuilder sb = new StringBuilder();
        sb.append("ROL: Sen profesyonel bir fitness antrenörüsün.\n");
        sb.append("GÖREV: Aşağıdaki kişi için 1 haftalık antrenman programı yaz.\n\n");

        sb.append("- Seviye: ").append(measurement.getFitnessLevel()).append("\n");
        sb.append("- Mevcut Durum: ").append(measurement.getKilo()).append("kg\n");
        sb.append("- Hedef: ").append(goal.getHedefKilo()).append("kg\n");

        if (!healthRecords.isEmpty()) {
            sb.append("\n⚠️ DİKKAT EDİLMESİ GEREKEN SAĞLIK DURUMLARI:\n");
            for (Health h : healthRecords) sb.append("- ").append(h.getNotes()).append("\n");
            sb.append("Bu sakatlıklara/durumlara zarar verecek hareketleri ASLA yazma.\n");
        }

        sb.append("\nLütfen Hareket Adı, Set Sayısı ve Tekrar Sayısını net belirt.");
        return sb.toString();
    }

    // ============================================================================================
    // API ÇAĞRI MERKEZİ (Requests)
    // ============================================================================================

    private String callGeminiApi(String promptText) {
        try {
            // --- DEDEKTİF KODU: Bellekteki API anahtarını kontrol edelim ---
            String maskedKey = (geminiApiKey != null && geminiApiKey.length() > 10)
                    ? geminiApiKey.substring(0, 10) + "..."
                    : "Eksik_veya_Okunamadi";
            logger.info("🔑 ŞU AN OKUNAN API KEY: " + maskedKey);
            // ----------------------------------------------------------------

            String maskedUrl = GEMINI_URL + maskedKey;
            logger.info("🔗 GİDEN URL: " + maskedUrl); // Log
            final String finalUrl = GEMINI_URL + geminiApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> part = new HashMap<>();
            part.put("text", promptText);
            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part));
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(finalUrl, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        } catch (Exception e) {
            logger.warn("🔴 API HATASI: " + e.getMessage());
            e.printStackTrace();
            return "Üzgünüm, şu an plan oluşturulamıyor. Lütfen daha sonra tekrar deneyin.";
        }
    }


    private String callGeminiApiWithImage(String promptText, String base64Image) {
        try {
            String finalUrl = GEMINI_URL + geminiApiKey;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", promptText);

            Map<String, Object> imagePart = new HashMap<>();
            Map<String, String> inlineData = new HashMap<>();
            inlineData.put("mime_type", "image/jpeg");
            inlineData.put("data", base64Image);
            imagePart.put("inline_data", inlineData);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(textPart, imagePart));
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(finalUrl, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            return "Görsel analiz hatası: " + e.getMessage();
        }
    }
}