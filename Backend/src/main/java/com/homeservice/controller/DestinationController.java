package com.homeservice.controller;

import com.homeservice.dto.ApiResponse;
import com.homeservice.dto.DestinationRequest;
import com.homeservice.entity.Destination;
import com.homeservice.service.DestinationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing destination management APIs.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    /**
     * Retrieves all registered destinations.
     *
     * @return list of destinations
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Destination>>> getAllDestinations() {
        List<Destination> destinations = destinationService.getAllDestinations();
        return ResponseEntity.ok(ApiResponse.success("Destinations retrieved successfully", destinations));
    }

    /**
     * Retrieves a destination by ID.
     *
     * @param id destination identifier
     * @return Destination details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Destination>> getDestinationById(@PathVariable Long id) {
        Destination destination = destinationService.getDestinationById(id);
        return ResponseEntity.ok(ApiResponse.success("Destination retrieved successfully", destination));
    }

    /**
     * Searches destinations by keyword query.
     *
     * @param query search query
     * @return matching destinations
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Destination>>> searchDestinations(@RequestParam(required = false, defaultValue = "") String query) {
        List<Destination> results = destinationService.searchDestinations(query);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", results));
    }

    /**
     * Adds a new destination (Admin only).
     *
     * @param request destination payload
     * @return newly created Destination
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Destination>> addDestination(@Valid @RequestBody DestinationRequest request) {
        Destination destination = destinationService.addDestination(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Destination added successfully", destination));
    }

    /**
     * Updates an existing destination (Admin only).
     *
     * @param id destination ID
     * @param request updated values
     * @return updated Destination
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Destination>> updateDestination(@PathVariable Long id, @Valid @RequestBody DestinationRequest request) {
        Destination updated = destinationService.updateDestination(id, request);
        return ResponseEntity.ok(ApiResponse.success("Destination updated successfully", updated));
    }

    /**
     * Deletes a destination (Admin only).
     *
     * @param id destination ID
     * @return deletion acknowledgment
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDestination(@PathVariable Long id) {
        destinationService.deleteDestination(id);
        return ResponseEntity.ok(ApiResponse.success("Destination deleted successfully", null));
    }
}
