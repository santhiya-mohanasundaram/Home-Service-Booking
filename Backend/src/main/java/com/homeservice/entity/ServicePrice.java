package com.homeservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "prices")
public class ServicePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    @Column(name = "price_per_room", nullable = false)
    private Double pricePerRoom;

    @Column(name = "tax_rate", nullable = false)
    private Double taxRate = 0.05; // 5% default

    private String description;

    public ServicePrice() {
    }

    public ServicePrice(Long serviceId, String serviceName, Double basePrice, Double pricePerRoom, Double taxRate, String description) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.basePrice = basePrice;
        this.pricePerRoom = pricePerRoom;
        this.taxRate = taxRate;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Double getPricePerRoom() {
        return pricePerRoom;
    }

    public void setPricePerRoom(Double pricePerRoom) {
        this.pricePerRoom = pricePerRoom;
    }

    public Double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Double taxRate) {
        this.taxRate = taxRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
