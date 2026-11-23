package com.homeservice.repository;

import com.homeservice.entity.CleaningService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CleaningServiceRepository extends JpaRepository<CleaningService, Long> {
    List<CleaningService> findByActiveTrue();
    Optional<CleaningService> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
