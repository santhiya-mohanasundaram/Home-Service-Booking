package com.homeservice.repository;

import com.homeservice.entity.TravelPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for TravelPackage entities.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Repository
public interface TravelPackageRepository extends JpaRepository<TravelPackage, Long> {

    List<TravelPackage> findByDestinationIgnoreCase(String destination);

    @Query("SELECT p FROM TravelPackage p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.destination) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<TravelPackage> searchPackages(@Param("query") String query);
}
