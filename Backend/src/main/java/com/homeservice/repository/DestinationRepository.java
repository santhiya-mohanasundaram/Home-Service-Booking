package com.homeservice.repository;

import com.homeservice.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Destination entities.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {

    List<Destination> findByPopularTrue();

    @Query("SELECT d FROM Destination d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.country) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Destination> searchDestinations(@Param("query") String query);

    boolean existsByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);
}
