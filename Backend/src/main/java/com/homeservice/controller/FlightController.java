package com.homeservice.controller;

import com.homeservice.dto.ApiResponse;
import com.homeservice.dto.FlightRequest;
import com.homeservice.entity.Flight;
import com.homeservice.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing flight search, availability, and scheduling APIs.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    /**
     * Retrieves all scheduled flights.
     *
     * @return list of flights
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Flight>>> getAllFlights() {
        List<Flight> flights = flightService.getAllFlights();
        return ResponseEntity.ok(ApiResponse.success("Flights retrieved successfully", flights));
    }

    /**
     * Retrieves flight by ID.
     *
     * @param id flight identifier
     * @return Flight details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Flight>> getFlightById(@PathVariable Long id) {
        Flight flight = flightService.getFlightById(id);
        return ResponseEntity.ok(ApiResponse.success("Flight retrieved successfully", flight));
    }

    /**
     * Searches flights by route or airline.
     *
     * @param query search keyword
     * @return matching flights
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Flight>>> searchFlights(@RequestParam(required = false, defaultValue = "") String query) {
        List<Flight> results = flightService.searchFlights(query);
        return ResponseEntity.ok(ApiResponse.success("Flights search completed", results));
    }

    /**
     * Checks seat availability for a flight.
     *
     * @param id flight ID
     * @param seats required seats
     * @return availability status
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkFlightAvailability(@PathVariable Long id, @RequestParam(defaultValue = "1") Integer seats) {
        boolean available = flightService.checkFlightAvailability(id, seats);
        return ResponseEntity.ok(ApiResponse.success("Flight availability check completed", Map.of("flightId", id, "requiredSeats", seats, "available", available)));
    }

    /**
     * Adds a new flight schedule (Admin only).
     *
     * @param request flight payload
     * @return persisted Flight
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Flight>> addFlight(@Valid @RequestBody FlightRequest request) {
        Flight flight = flightService.addFlight(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Flight added successfully", flight));
    }

    /**
     * Updates an existing flight (Admin only).
     *
     * @param id flight ID
     * @param request updated flight details
     * @return updated Flight
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Flight>> updateFlight(@PathVariable Long id, @Valid @RequestBody FlightRequest request) {
        Flight updated = flightService.updateFlight(id, request);
        return ResponseEntity.ok(ApiResponse.success("Flight updated successfully", updated));
    }

    /**
     * Deletes a flight (Admin only).
     *
     * @param id flight ID
     * @return deletion acknowledgment
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.ok(ApiResponse.success("Flight deleted successfully", null));
    }
}
