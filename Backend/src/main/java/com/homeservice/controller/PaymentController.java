package com.homeservice.controller;

import com.homeservice.dto.ApiResponse;
import com.homeservice.dto.PaymentRequest;
import com.homeservice.dto.PaymentResponse;
import com.homeservice.dto.RefundRequest;
import com.homeservice.entity.PaymentStatus;
import com.homeservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing payment verification, transaction audit, and refund APIs.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Processes a payment transaction in verified test/sandbox mode.
     *
     * @param request payment payload
     * @param authentication active user context
     * @return PaymentResponse
     */
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@Valid @RequestBody PaymentRequest request,
                                                                      Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "customer@example.com";
        PaymentResponse response = paymentService.processPayment(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment processed successfully in verified mode", response));
    }

    /**
     * Verifies payment status with gateway reconciliation.
     *
     * @param transactionId transaction ID
     * @return updated PaymentResponse
     */
    @PostMapping("/verify/{transactionId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(@PathVariable String transactionId) {
        PaymentResponse response = paymentService.verifyPayment(transactionId);
        return ResponseEntity.ok(ApiResponse.success("Payment verification completed", response));
    }

    /**
     * Retrieves payment details by transaction ID.
     *
     * @param transactionId transaction ID
     * @return PaymentResponse
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentDetails(@PathVariable String transactionId) {
        PaymentResponse response = paymentService.getPaymentDetails(transactionId);
        return ResponseEntity.ok(ApiResponse.success("Payment details retrieved successfully", response));
    }

    /**
     * Retrieves all payments for the logged-in customer.
     *
     * @param authentication active user context
     * @return list of PaymentResponse
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "";
        List<PaymentResponse> list = paymentService.getUserPayments(email);
        return ResponseEntity.ok(ApiResponse.success("User payments retrieved successfully", list));
    }

    /**
     * Processes a safe test refund on an eligible payment (Admin or customer).
     *
     * @param transactionId transaction ID
     * @param refundRequest reason payload
     * @return updated PaymentResponse
     */
    @PostMapping("/{transactionId}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(@PathVariable String transactionId,
                                                                      @Valid @RequestBody RefundRequest refundRequest) {
        PaymentResponse response = paymentService.refundPayment(transactionId, refundRequest);
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", response));
    }

    /**
     * Updates payment status directly (Admin only).
     *
     * @param transactionId transaction ID
     * @param status target PaymentStatus
     * @return updated PaymentResponse
     */
    @PutMapping("/{transactionId}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(@PathVariable String transactionId,
                                                                            @RequestParam PaymentStatus status) {
        PaymentResponse response = paymentService.updatePaymentStatus(transactionId, status);
        return ResponseEntity.ok(ApiResponse.success("Payment status updated successfully", response));
    }
}
