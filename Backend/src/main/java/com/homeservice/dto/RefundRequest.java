package com.homeservice.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for processing a safe test refund.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
public class RefundRequest {

    @NotBlank(message = "Refund reason is required")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
