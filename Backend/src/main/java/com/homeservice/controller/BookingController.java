package com.homeservice.controller;

import com.homeservice.dto.ApiResponse;
import com.homeservice.dto.BookingRequest;
import com.homeservice.dto.BookingResponse;
import com.homeservice.dto.CouponValidationResponse;
import com.homeservice.entity.User;
import com.homeservice.service.BookingService;
import com.homeservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing booking creation, history, cancellations, pricing, and coupon operations.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    public BookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    /**
     * Creates a new booking.
     *
     * @param request booking payload
     * @return newly created BookingResponse
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest request) {
        User currentUser = null;
        try {
            currentUser = userService.getCurrentAuthenticatedUser();
        } catch (Exception ignored) {
            // Guest or public checkout mode allowed
        }
        BookingResponse response = bookingService.createBooking(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully with status PENDING", response));
    }

    /**
}
