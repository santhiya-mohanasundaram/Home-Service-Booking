package com.homeservice.repository;

import com.homeservice.entity.Booking;
import com.homeservice.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingId(String bookingId);
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findByEmailOrderByCreatedAtDesc(String email);
    List<Booking> findAllByOrderByCreatedAtDesc();
    long countByBookingStatus(BookingStatus status);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0.0) FROM Booking b WHERE b.bookingStatus != 'CANCELLED'")
    Double calculateTotalRevenue();
}
