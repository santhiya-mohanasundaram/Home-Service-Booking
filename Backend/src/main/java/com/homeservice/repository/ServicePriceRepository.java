package com.homeservice.repository;

import com.homeservice.entity.ServicePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServicePriceRepository extends JpaRepository<ServicePrice, Long> {
    Optional<ServicePrice> findByServiceId(Long serviceId);
    Optional<ServicePrice> findByServiceNameIgnoreCase(String serviceName);
}
