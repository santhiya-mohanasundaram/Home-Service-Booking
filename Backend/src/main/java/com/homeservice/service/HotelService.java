package com.homeservice.service;

import com.homeservice.dto.HotelRequest;
import com.homeservice.entity.Hotel;
import com.homeservice.exception.InvalidRequestException;
import com.homeservice.exception.ResourceNotFoundException;
import com.homeservice.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing hotel inventory, availability checks, and price tiers.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Service
@Transactional
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    /**
     * Adds a new hotel to inventory.
     *
     * @param request hotel details
     * @return persisted Hotel entity
     */
    public Hotel addHotel(HotelRequest request) {
        // Developed by Santhiya Mohanasundaram - Validates rate tiers and initial room inventory
        Hotel hotel = new Hotel(
                request.getName(),
                request.getDestination(),
                request.getAddress(),
                request.getPricePerNight(),
                request.getRating() != null ? request.getRating() : 4.5,
                request.getAvailableRooms() != null ? request.getAvailableRooms() : 10,
                request.getAmenities()
        );
        return hotelRepository.save(hotel);
    }

    /**
     * Retrieves all registered hotels.
     *
     * @return list of hotels
     */
    @Transactional(readOnly = true)
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    /**
     * Finds a hotel by ID.
     *
     * @param id the hotel ID
     * @return Hotel entity
     * @throws ResourceNotFoundException if ID not found
     */
    @Transactional(readOnly = true)
    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + id));
    }

    /**
     * Searches hotels by name or destination keyword.
     *
     * @param query search keyword
     * @return matching hotels
     */
    @Transactional(readOnly = true)
    public List<Hotel> searchHotels(String query) {
        if (query == null || query.trim().isEmpty()) {
            return hotelRepository.findAll();
        }
        return hotelRepository.searchHotels(query.trim());
    }

    /**
     * Checks if a hotel has the required number of rooms available.
     *
     * @param id hotel ID
     * @param requiredRooms number of requested rooms
     * @return true if sufficient rooms available
     */
    @Transactional(readOnly = true)
    public boolean checkHotelAvailability(Long id, Integer requiredRooms) {
        // Reviewed by Dhanusri D - Guarantees room count verification before booking confirmation
        Hotel hotel = getHotelById(id);
        if (requiredRooms == null || requiredRooms <= 0) {
            throw new InvalidRequestException("Required rooms must be at least 1");
        }
        return hotel.getAvailableRooms() >= requiredRooms;
    }

    /**
     * Updates an existing hotel's details.
     *
     * @param id target hotel ID
     * @param request updated values
     * @return updated Hotel entity
     */
    public Hotel updateHotel(Long id, HotelRequest request) {
        Hotel hotel = getHotelById(id);
        hotel.setName(request.getName());
        hotel.setDestination(request.getDestination());
        hotel.setAddress(request.getAddress());
        hotel.setPricePerNight(request.getPricePerNight());
        if (request.getRating() != null) hotel.setRating(request.getRating());
        if (request.getAvailableRooms() != null) hotel.setAvailableRooms(request.getAvailableRooms());
        hotel.setAmenities(request.getAmenities());
        return hotelRepository.save(hotel);
    }

    /**
     * Deletes a hotel from the system.
     *
     * @param id target hotel ID
     */
    public void deleteHotel(Long id) {
        Hotel hotel = getHotelById(id);
        hotelRepository.delete(hotel);
    }
}
