package com.homeservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing a comprehensive service or travel package.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Entity
@Table(name = "travel_packages")
public class TravelPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Package title is required")
    @Column(nullable = false, length = 150)
    private String title;

    @NotBlank(message = "Destination is required")
    @Column(nullable = false, length = 120)
    private String destination;

    @Min(value = 1, message = "Duration must be at least 1 day")
    @Column(nullable = false)
    private Integer durationDays;

    @NotNull(message = "Package price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    @Column(nullable = false)
    private Double price;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String includedServices;

    public TravelPackage() {}

    public TravelPackage(String title, String destination, Integer durationDays, Double price, String description, String includedServices) {
        this.title = title;
        this.destination = destination;
        this.durationDays = durationDays;
        this.price = price;
        this.description = description;
        this.includedServices = includedServices;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIncludedServices() {
        return includedServices;
    }

    public void setIncludedServices(String includedServices) {
        this.includedServices = includedServices;
    }
}
