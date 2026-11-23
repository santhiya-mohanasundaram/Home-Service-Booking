package com.homeservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "services")
public class CleaningService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    @Column(name = "price_per_unit", nullable = false)
    private Double pricePerUnit;

    private String duration;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "image_url")
    private String imageUrl;

    public CleaningService() {
    }

    public CleaningService(String name, String description, Double basePrice, Double pricePerUnit, String duration, boolean active, String imageUrl) {
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.pricePerUnit = pricePerUnit;
        this.duration = duration;
        this.active = active;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(Double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
