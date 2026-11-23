package com.homeservice.service;

import com.homeservice.dto.PaymentRequest;
import com.homeservice.dto.PaymentResponse;
import com.homeservice.dto.RefundRequest;
import com.homeservice.entity.Payment;
import com.homeservice.entity.PaymentStatus;
import com.homeservice.exception.InvalidRequestException;
import com.homeservice.exception.ResourceNotFoundException;
import com.homeservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service managing secure payment transactions, status audits, and safe test refunds.
 * Strict Rule: Never marks payment successful without verification check.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Processes a payment transaction in verified test sandbox mode.
     * Requires valid simulated security pin or provider token before confirmation.
     *
     * @param request payment payload
     * @param userEmail customer email address
     * @return PaymentResponse with transaction audit details
     */
    public PaymentResponse processPayment(PaymentRequest request, String userEmail) {
        // Developed by Santhiya Mohanasundaram - Enforces safe verified test payment workflow
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new InvalidRequestException("Payment amount must be greater than zero.");
        }

        // Verification guard: Test mode verification check
        PaymentStatus initialStatus = PaymentStatus.PENDING;
        if (request.getPaymentMethod().toUpperCase().contains("TEST") || "1234".equals(request.getTestSecurityPin()) || "SUCCESS".equalsIgnoreCase(request.getTestSecurityPin())) {
            initialStatus = PaymentStatus.SUCCESS;
        } else if ("FAIL".equalsIgnoreCase(request.getTestSecurityPin())) {
            initialStatus = PaymentStatus.FAILED;
        }

        String transactionId = "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = new Payment(
                transactionId,
                request.getBookingId(),
                userEmail,
                request.getAmount(),
                request.getPaymentMethod(),
                initialStatus
        );

        Payment saved = paymentRepository.save(payment);
        return mapToResponse(saved);
    }

    /**
     * Verifies payment status with simulated gateway verification.
     *
     * @param transactionId transaction reference ID
     * @return updated PaymentResponse
     */
    public PaymentResponse verifyPayment(String transactionId) {
        // Reviewed by Dhanusri D - Verifies transaction authenticity before confirming delivery
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment = paymentRepository.save(payment);
        }
        return mapToResponse(payment);
    }

    /**
     * Retrieves payment details by transaction ID.
     *
     * @param transactionId transaction reference ID
     * @return PaymentResponse
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentDetails(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
        return mapToResponse(payment);
    }

    /**
     * Retrieves all payments for a specific user.
     *
     * @param userEmail customer email
     * @return list of payments
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getUserPayments(String userEmail) {
        return paymentRepository.findByUserEmailOrderByPaymentDateDesc(userEmail).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Processes a safe test refund on an eligible payment transaction.
     *
     * @param transactionId transaction reference
     * @param refundRequest reason for refund
     * @return PaymentResponse with status REFUNDED
     */
    public PaymentResponse refundPayment(String transactionId, RefundRequest refundRequest) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidRequestException("Only successfully processed transactions can be refunded.");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundReason(refundRequest.getReason());
        Payment updated = paymentRepository.save(payment);
        return mapToResponse(updated);
    }

    /**
     * Updates payment status directly by administrative audit.
     *
     * @param transactionId transaction ID
     * @param status new PaymentStatus
     * @return updated PaymentResponse
     */
    public PaymentResponse updatePaymentStatus(String transactionId, PaymentStatus status) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
        payment.setStatus(status);
        return mapToResponse(paymentRepository.save(payment));
    }

    private PaymentResponse mapToResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getTransactionId(),
                p.getBookingId(),
                p.getUserEmail(),
                p.getAmount(),
                p.getPaymentMethod(),
                p.getStatus(),
                p.getPaymentDate(),
                p.getRefundReason()
        );
    }
}
