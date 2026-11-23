package com.homeservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entity representing a travel destination or service region.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Entity
@Table(name = "destinations")
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Destination name is required")
    @Size(max = 120, message = "Destination name must not exceed 120 characters")
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank(message = "Country is required")
    @Size(max = 80, message = "Country must not exceed 80 characters")
    @Column(nullable = false, length = 80)
    private String country;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 60)
    private String category;

    @Column(nullable = false)
    private boolean popular = false;

    public Destination() {}

    public Destination(String name, String country, String description, String imageUrl, String category, boolean popular) {
        this.name = name;
        this.country = country;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.popular = popular;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isPopular() {
        return popular;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }
}
