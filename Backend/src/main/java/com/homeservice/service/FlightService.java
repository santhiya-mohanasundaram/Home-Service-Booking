package com.homeservice.service;

import com.homeservice.dto.FlightRequest;
import com.homeservice.entity.Flight;
import com.homeservice.exception.DuplicateResourceException;
import com.homeservice.exception.InvalidRequestException;
import com.homeservice.exception.ResourceNotFoundException;
import com.homeservice.repository.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing flight schedules, seat allocations, and route searches.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Service
@Transactional
public class FlightService {

    private final FlightRepository flightRepository;

    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    /**
     * Adds a new flight schedule.
     *
     * @param request flight parameters
     * @return persisted Flight entity
     */
    public Flight addFlight(FlightRequest request) {
        // Developed by Santhiya Mohanasundaram - Prevents duplicate flight numbers
        if (flightRepository.findByFlightNumberIgnoreCase(request.getFlightNumber()).isPresent()) {
            throw new DuplicateResourceException("Flight number '" + request.getFlightNumber() + "' is already registered.");
        }
        Flight flight = new Flight(
                request.getFlightNumber().toUpperCase(),
                request.getAirline(),
                request.getOrigin(),
                request.getDestination(),
                request.getDepartureTime(),
                request.getArrivalTime(),
                request.getPrice(),
                request.getAvailableSeats() != null ? request.getAvailableSeats() : 60
        );
        return flightRepository.save(flight);
    }

    /**
     * Retrieves all available flights.
     *
     * @return list of flights
     */
    @Transactional(readOnly = true)
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    /**
     * Finds a flight by ID.
     *
     * @param id flight identifier
     * @return Flight entity
     */
    @Transactional(readOnly = true)
    public Flight getFlightById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
    }

    /**
     * Searches flights by origin, destination, airline or flight number.
     *
     * @param query search query
     * @return matching flights
     */
    @Transactional(readOnly = true)
    public List<Flight> searchFlights(String query) {
        if (query == null || query.trim().isEmpty()) {
            return flightRepository.findAll();
        }
        return flightRepository.searchFlights(query.trim());
    }

    /**
     * Checks if sufficient seats are available for booking.
     *
     * @param id flight ID
     * @param requiredSeats number of passenger seats
     * @return true if available
     */
    @Transactional(readOnly = true)
    public boolean checkFlightAvailability(Long id, Integer requiredSeats) {
        // Reviewed by Dhanusri D - Guarantees seat constraint verification
        Flight flight = getFlightById(id);
        if (requiredSeats == null || requiredSeats <= 0) {
            throw new InvalidRequestException("Required seats must be at least 1");
        }
        return flight.getAvailableSeats() >= requiredSeats;
    }

    /**
     * Updates an existing flight schedule or price.
     *
     * @param id flight ID
     * @param request updated flight details
     * @return updated Flight
     */
    public Flight updateFlight(Long id, FlightRequest request) {
        Flight flight = getFlightById(id);
        flight.setAirline(request.getAirline());
        flight.setOrigin(request.getOrigin());
        flight.setDestination(request.getDestination());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setPrice(request.getPrice());
        if (request.getAvailableSeats() != null) {
            flight.setAvailableSeats(request.getAvailableSeats());
        }
        return flightRepository.save(flight);
    }

    /**
     * Deletes a flight from the schedule.
     *
     * @param id flight ID
     */
    public void deleteFlight(Long id) {
        Flight flight = getFlightById(id);
        flightRepository.delete(flight);
    }
}
