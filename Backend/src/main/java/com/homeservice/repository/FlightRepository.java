package com.homeservice.repository;

import com.homeservice.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Flight entities.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumberIgnoreCase(String flightNumber);

    List<Flight> findByOriginIgnoreCaseAndDestinationIgnoreCase(String origin, String destination);

    @Query("SELECT f FROM Flight f WHERE LOWER(f.origin) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(f.destination) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(f.airline) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(f.flightNumber) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Flight> searchFlights(@Param("query") String query);
}
