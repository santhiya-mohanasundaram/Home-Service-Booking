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
     * Retrieves personal bookings of the logged-in customer.
     *
     * @return list of bookings
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUserBookings() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        List<BookingResponse> bookings = bookingService.getUserBookings(currentUser);
        return ResponseEntity.ok(ApiResponse.success("User bookings retrieved successfully", bookings));
    }

    /**
     * Alias for getUserBookings representing booking history.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingHistory() {
        return getUserBookings();
    }

    /**
     * Retrieves all bookings in the system (Admin only).
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.success("All bookings retrieved successfully", bookings));
    }

    /**
     * Retrieves a booking by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        BookingResponse booking = bookingService.getBookingById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", booking));
    }

    /**
     * Updates an existing booking.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(@PathVariable Long id,
                                                                      @Valid @RequestBody BookingRequest request) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        BookingResponse updated = bookingService.updateBooking(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Booking updated successfully", updated));
    }

    /**
     * Cancels an eligible booking.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long id) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        BookingResponse cancelled = bookingService.cancelBooking(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", cancelled));
    }

    /**
     * Calculates dynamic booking price for given services and room count.
     */
    @PostMapping("/calculate-price")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateBookingPrice(@RequestBody Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        List<String> services = (List<String>) payload.getOrDefault("services", List.of());
        int rooms = Integer.parseInt(payload.getOrDefault("rooms", 1).toString());
        double price = bookingService.calculateBookingPrice(services, rooms);
        return ResponseEntity.ok(ApiResponse.success("Price calculated successfully", Map.of("rooms", rooms, "services", services, "totalPrice", price)));
    }

    /**
     * Validates a promotional discount coupon.
     */
    @GetMapping("/coupon/validate")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> validateCoupon(@RequestParam String code,
                                                                                @RequestParam(defaultValue = "1000.0") Double amount) {
        CouponValidationResponse response = bookingService.validateCoupon(code, amount);
        return ResponseEntity.ok(ApiResponse.success("Coupon checked", response));
    }

    /**
     * Applies a valid coupon to an existing booking.
     */
    @PostMapping("/{ref}/coupon")
    public ResponseEntity<ApiResponse<BookingResponse>> applyCoupon(@PathVariable String ref,
                                                                    @RequestParam String code) {
        BookingResponse response = bookingService.applyCoupon(ref, code);
        return ResponseEntity.ok(ApiResponse.success("Coupon applied to booking", response));
    }
}
