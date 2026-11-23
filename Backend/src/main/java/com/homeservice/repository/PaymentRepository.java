package com.homeservice.repository;

import com.homeservice.entity.Payment;
import com.homeservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Payment transaction entities.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByBookingId(String bookingId);

    List<Payment> findByUserEmailOrderByPaymentDateDesc(String userEmail);

    List<Payment> findByStatus(PaymentStatus status);
}
