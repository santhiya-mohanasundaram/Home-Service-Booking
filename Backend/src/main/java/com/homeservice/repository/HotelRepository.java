package com.homeservice.repository;

import com.homeservice.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Hotel entities.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByDestinationIgnoreCase(String destination);

    @Query("SELECT h FROM Hotel h WHERE LOWER(h.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(h.destination) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Hotel> searchHotels(@Param("query") String query);

    List<Hotel> findByDestinationIgnoreCaseAndAvailableRoomsGreaterThan(String destination, Integer minRooms);
}
