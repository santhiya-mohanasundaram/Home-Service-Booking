package com.homeservice.controller;

import com.homeservice.dto.*;
import com.homeservice.entity.BookingStatus;
import com.homeservice.entity.Destination;
import com.homeservice.entity.Flight;
import com.homeservice.entity.Hotel;
import com.homeservice.entity.TravelPackage;
import com.homeservice.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing administrative dashboard KPIs, ecosystem management, and status controls.
 * Guarded strictly by Spring Security @PreAuthorize("hasAuthority('ROLE_ADMIN')").
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final BookingService bookingService;
    private final UserService userService;
    private final DestinationService destinationService;
    private final HotelService hotelService;
    private final FlightService flightService;
    private final TravelPackageService packageService;

    public AdminController(DashboardService dashboardService,
                           BookingService bookingService,
                           UserService userService,
                           DestinationService destinationService,
                           HotelService hotelService,
                           FlightService flightService,
                           TravelPackageService packageService) {
        this.dashboardService = dashboardService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.destinationService = destinationService;
        this.hotelService = hotelService;
        this.flightService = flightService;
        this.packageService = packageService;
    }

    /**
     * Retrieves aggregated administrative dashboard KPIs and status metrics.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getAdminDashboard() {
        DashboardStatsResponse stats = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard statistics retrieved successfully", stats));
    }

    /**
     * Returns total customer count.
     */
    @GetMapping("/total-users")
    public ResponseEntity<ApiResponse<Long>> getTotalUsers() {
        long count = dashboardService.getTotalUsers();
        return ResponseEntity.ok(ApiResponse.success("Total users retrieved", count));
    }

    /**
     * Returns total bookings count.
     */
    @GetMapping("/total-bookings")
    public ResponseEntity<ApiResponse<Long>> getTotalBookings() {
        long count = dashboardService.getTotalBookings();
        return ResponseEntity.ok(ApiResponse.success("Total bookings retrieved", count));
    }

    /**
     * Returns total revenue generated.
     */
    @GetMapping("/total-revenue")
    public ResponseEntity<ApiResponse<Double>> getTotalRevenue() {
        double revenue = dashboardService.getTotalRevenue();
        return ResponseEntity.ok(ApiResponse.success("Total revenue retrieved", revenue));
    }

    /**
     * Returns booking breakdown statistics.
     */
    @GetMapping("/booking-statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingStatistics() {
        Map<String, Object> stats = dashboardService.getBookingStatistics();
        return ResponseEntity.ok(ApiResponse.success("Booking statistics retrieved", stats));
    }

    /**
     * Manages users: retrieves list of all users.
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> manageUsers() {
        List<UserProfileResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("User management list retrieved", users));
    }

    /**
     * Manages destinations: retrieves all destinations.
     */
    @GetMapping("/destinations")
    public ResponseEntity<ApiResponse<List<Destination>>> manageDestinations() {
        List<Destination> list = destinationService.getAllDestinations();
        return ResponseEntity.ok(ApiResponse.success("Destinations management list retrieved", list));
    }

    /**
     * Manages hotels: retrieves all hotels.
     */
    @GetMapping("/hotels")
    public ResponseEntity<ApiResponse<List<Hotel>>> manageHotels() {
        List<Hotel> list = hotelService.getAllHotels();
        return ResponseEntity.ok(ApiResponse.success("Hotels management list retrieved", list));
    }

    /**
     * Manages flights: retrieves all flights.
     */
    @GetMapping("/flights")
    public ResponseEntity<ApiResponse<List<Flight>>> manageFlights() {
        List<Flight> list = flightService.getAllFlights();
        return ResponseEntity.ok(ApiResponse.success("Flights management list retrieved", list));
    }

    /**
     * Manages packages: retrieves all travel packages.
     */
    @GetMapping("/packages")
    public ResponseEntity<ApiResponse<List<TravelPackage>>> managePackages() {
        List<TravelPackage> list = packageService.getAllPackages();
        return ResponseEntity.ok(ApiResponse.success("Packages management list retrieved", list));
    }

    /**
     * Retrieves all bookings for admin review.
     */
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.success("All bookings retrieved successfully", bookings));
    }

    /**
     * Updates booking status by numeric or string reference ID.
     */
    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBookingStatus(
            @PathVariable String id,
            @Valid @RequestBody BookingStatusUpdateRequest request) {
        BookingStatus status = request.getStatus();
        BookingResponse updatedBooking = bookingService.updateBookingStatusByRef(id, status);
        return ResponseEntity.ok(ApiResponse.success("Booking status updated to " + status, updatedBooking));
    }
}
