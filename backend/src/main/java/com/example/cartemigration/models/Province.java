package com.example.cartemigration.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "province")
public class Province {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProvince;

    private String province;

    // 🔴 Obligatoire JPA
    protected Province() {
    }

    // ✅ Constructeur métier principal
    public Province(String province) {
        this.province = province;
    }

    // Constructeur complet avec id (pour tests)
    public Province(Long idProvince, String province) {
        this.idProvince = idProvince;
        this.province = province;
    }

    // Getters & Setters
    public Long getIdProvince() {
        return idProvince;
    }

    public void setIdProvince(Long idProvince) {
        this.idProvince = idProvince;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}
