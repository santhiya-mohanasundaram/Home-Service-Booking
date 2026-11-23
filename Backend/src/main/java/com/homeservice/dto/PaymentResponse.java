package com.homeservice.dto;

import com.homeservice.entity.PaymentStatus;
import java.time.LocalDateTime;

/**
 * Detailed payment receipt and status response.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
public class PaymentResponse {

    private Long id;
    private String transactionId;
    private String bookingId;
    private String userEmail;
    private Double amount;
    private String paymentMethod;
    private PaymentStatus status;
    private LocalDateTime paymentDate;
    private String refundReason;

    public PaymentResponse() {}

    public PaymentResponse(Long id, String transactionId, String bookingId, String userEmail, Double amount, String paymentMethod, PaymentStatus status, LocalDateTime paymentDate, String refundReason) {
        this.id = id;
        this.transactionId = transactionId;
        this.bookingId = bookingId;
        this.userEmail = userEmail;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paymentDate = paymentDate;
        this.refundReason = refundReason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }
}
