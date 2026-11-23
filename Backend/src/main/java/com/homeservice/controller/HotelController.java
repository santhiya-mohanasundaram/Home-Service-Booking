package com.homeservice.controller;

import com.homeservice.dto.ApiResponse;
import com.homeservice.dto.HotelRequest;
import com.homeservice.entity.Hotel;
import com.homeservice.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing hotel accommodation APIs.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    /**
     * Retrieves all registered hotels.
     *
     * @return list of hotels
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Hotel>>> getAllHotels() {
        List<Hotel> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(ApiResponse.success("Hotels retrieved successfully", hotels));
    }

    /**
     * Retrieves a hotel by ID.
     *
     * @param id hotel identifier
     * @return Hotel details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Hotel>> getHotelById(@PathVariable Long id) {
        Hotel hotel = hotelService.getHotelById(id);
        return ResponseEntity.ok(ApiResponse.success("Hotel retrieved successfully", hotel));
    }

    /**
     * Searches hotels by name or destination keyword.
     *
     * @param query search query
     * @return matching hotels
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Hotel>>> searchHotels(@RequestParam(required = false, defaultValue = "") String query) {
        List<Hotel> results = hotelService.searchHotels(query);
        return ResponseEntity.ok(ApiResponse.success("Hotels search completed", results));
    }

    /**
     * Verifies room availability for a hotel.
     *
     * @param id hotel ID
     * @param rooms required rooms count
     * @return availability status
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHotelAvailability(@PathVariable Long id, @RequestParam(defaultValue = "1") Integer rooms) {
        boolean available = hotelService.checkHotelAvailability(id, rooms);
        return ResponseEntity.ok(ApiResponse.success("Availability check completed", Map.of("hotelId", id, "requiredRooms", rooms, "available", available)));
    }

    /**
     * Adds a new hotel (Admin only).
     *
     * @param request hotel payload
     * @return persisted Hotel
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Hotel>> addHotel(@Valid @RequestBody HotelRequest request) {
        Hotel hotel = hotelService.addHotel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hotel added successfully", hotel));
    }

    /**
     * Updates an existing hotel (Admin only).
     *
     * @param id hotel ID
     * @param request updated values
     * @return updated Hotel
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Hotel>> updateHotel(@PathVariable Long id, @Valid @RequestBody HotelRequest request) {
        Hotel updated = hotelService.updateHotel(id, request);
        return ResponseEntity.ok(ApiResponse.success("Hotel updated successfully", updated));
    }

    /**
     * Deletes a hotel (Admin only).
     *
     * @param id hotel ID
     * @return deletion acknowledgment
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.ok(ApiResponse.success("Hotel deleted successfully", null));
    }
}
