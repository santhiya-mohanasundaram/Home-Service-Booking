package com.homeservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing an accommodation or partner hotel.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Hotel name is required")
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Destination is required")
    @Column(nullable = false, length = 120)
    private String destination;

    @Column(length = 255)
    private String address;

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price per night must be greater than zero")
    @Column(nullable = false)
    private Double pricePerNight;

    @Column(nullable = false)
    private Double rating = 4.5;

    @Min(value = 0, message = "Available rooms cannot be negative")
    @Column(nullable = false)
    private Integer availableRooms = 10;

    @Column(length = 500)
    private String amenities;

    public Hotel() {}

    public Hotel(String name, String destination, String address, Double pricePerNight, Double rating, Integer availableRooms, String amenities) {
        this.name = name;
        this.destination = destination;
        this.address = address;
        this.pricePerNight = pricePerNight;
        this.rating = rating;
        this.availableRooms = availableRooms;
        this.amenities = amenities;
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

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(Double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getAvailableRooms() {
        return availableRooms;
    }

    public void setAvailableRooms(Integer availableRooms) {
        this.availableRooms = availableRooms;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }
}
