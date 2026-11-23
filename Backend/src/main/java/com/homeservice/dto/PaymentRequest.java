package com.homeservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for processing a booking payment in verified test/sandbox mode.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
public class PaymentRequest {

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
    private Double amount;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String testSecurityPin;

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTestSecurityPin() {
        return testSecurityPin;
    }

    public void setTestSecurityPin(String testSecurityPin) {
        this.testSecurityPin = testSecurityPin;
    }
}
