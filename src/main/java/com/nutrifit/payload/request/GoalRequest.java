package com.nutrifit.payload.request;

import java.time.LocalDate;

public class GoalRequest {
    private Double hedefKilo;
    private LocalDate hedefTarih;
    private LocalDate baslangicTarihi;

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
}