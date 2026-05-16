package com.nutrifit.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "goals")
@Data
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Long goalId;

    @Column(name = "hedef_kilo")
    private Double hedefKilo;

    @Column(name = "hedef_tarih")
    private LocalDate hedefTarih;

    @Column(name = "baslangic_tarihi")
    private LocalDate baslangicTarihi;
    @Column(name = "target_body_fat")
    private Double targetBodyFat;

    // İlişkiler
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    public Goal() {
    }


    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    public Double getHedefKilo() {
        return hedefKilo;
    }

    public void setHedefKilo(Double hedefKilo) {
        this.hedefKilo = hedefKilo;
    }

    public LocalDate getHedefTarih() {
        return hedefTarih;
    }

    public void setHedefTarih(LocalDate hedefTarih) {
        this.hedefTarih = hedefTarih;
    }

    public LocalDate getBaslangicTarihi() {
        return baslangicTarihi;
    }

    public void setBaslangicTarihi(LocalDate baslangicTarihi) {
        this.baslangicTarihi = baslangicTarihi;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getTargetBodyFat() {
        return targetBodyFat;
    }

    public void setTargetBodyFat(Double targetBodyFat) {
        this.targetBodyFat = targetBodyFat;
    }
}