package com.homeservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing an airline flight route and seat inventory.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Entity
@Table(name = "flights")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Flight number is required")
    @Column(nullable = false, length = 30)
    private String flightNumber;

    @NotBlank(message = "Airline is required")
    @Column(nullable = false, length = 100)
    private String airline;

    @NotBlank(message = "Origin city is required")
    @Column(nullable = false, length = 100)
    private String origin;

    @NotBlank(message = "Destination city is required")
    @Column(nullable = false, length = 100)
    private String destination;

    @NotBlank(message = "Departure time is required")
    @Column(nullable = false, length = 50)
    private String departureTime;

    @NotBlank(message = "Arrival time is required")
    @Column(nullable = false, length = 50)
    private String arrivalTime;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Flight price must be greater than zero")
    @Column(nullable = false)
    private Double price;

    @Min(value = 0, message = "Available seats cannot be negative")
    @Column(nullable = false)
    private Integer availableSeats = 60;

    public Flight() {}

    public Flight(String flightNumber, String airline, String origin, String destination, String departureTime, String arrivalTime, Double price, Integer availableSeats) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.availableSeats = availableSeats;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }
}
