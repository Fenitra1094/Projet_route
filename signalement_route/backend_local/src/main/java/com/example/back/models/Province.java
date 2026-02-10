package com.example.back.models;

import jakarta.persistence.*;

@Entity
@Table(name = "province")
public class Province {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_province;

    @Column(nullable = false, unique = true)
    private String province;

    public Province() {}

    public Province(String province) {
        this.province = province;
    }

    public Long getId_province() {
        return id_province;
    }

    public void setId_province(Long id_province) {
        this.id_province = id_province;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }
}