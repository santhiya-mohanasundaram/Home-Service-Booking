package com.homeservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing promotional discount coupons.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Coupon code is required")
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "1.0", message = "Discount percentage must be at least 1%")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100%")
    @Column(nullable = false)
    private Double discountPercent;

    @DecimalMin(value = "0.0", message = "Maximum discount cannot be negative")
    @Column(nullable = false)
    private Double maxDiscount = 500.0;

    @DecimalMin(value = "0.0", message = "Minimum order amount cannot be negative")
    @Column(nullable = false)
    private Double minAmount = 500.0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 30)
    private String expiryDate;

    public Coupon() {}

    public Coupon(String code, Double discountPercent, Double maxDiscount, Double minAmount, boolean active, String expiryDate) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.maxDiscount = maxDiscount;
        this.minAmount = minAmount;
        this.active = active;
        this.expiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Double getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(Double maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public Double getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Double minAmount) {
        this.minAmount = minAmount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
