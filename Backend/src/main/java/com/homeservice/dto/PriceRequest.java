package com.homeservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PriceRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    private String serviceName;

    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private Double basePrice;

    @NotNull(message = "Price per room is required")
    @Positive(message = "Price per room must be positive")
    private Double pricePerRoom;

    private Double taxRate = 0.05;
    private String description;

    public PriceRequest() {
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
