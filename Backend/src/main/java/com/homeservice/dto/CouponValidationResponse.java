package com.homeservice.dto;

/**
 * Response payload returning coupon validation details and computed discounts.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
public class CouponValidationResponse {

    private boolean valid;
    private String code;
    private Double discountPercent;
    private Double discountAmount;
    private Double finalAmount;
    private String message;

    public CouponValidationResponse() {}

    public CouponValidationResponse(boolean valid, String code, Double discountPercent, Double discountAmount, Double finalAmount, String message) {
        this.valid = valid;
        this.code = code;
        this.discountPercent = discountPercent;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
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

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(Double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
