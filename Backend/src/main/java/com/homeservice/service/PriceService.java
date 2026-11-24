package com.homeservice.service;

import com.homeservice.dto.PriceRequest;
import com.homeservice.entity.CleaningService;
import com.homeservice.entity.ServicePrice;
import com.homeservice.exception.ResourceNotFoundException;
import com.homeservice.repository.CleaningServiceRepository;
import com.homeservice.repository.ServicePriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PriceService {

    private final ServicePriceRepository priceRepository;
    private final CleaningServiceRepository serviceRepository;

    public PriceService(ServicePriceRepository priceRepository, CleaningServiceRepository serviceRepository) {
        this.priceRepository = priceRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<ServicePrice> getAllPrices() {
        return priceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ServicePrice getPriceByServiceId(Long serviceId) {
        return priceRepository.findByServiceId(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Price configuration not found for service ID: " + serviceId));
    }

    @Transactional
    public ServicePrice addPrice(PriceRequest request) {
        CleaningService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + request.getServiceId()));

        String serviceName = request.getServiceName() != null ? request.getServiceName() : service.getName();

        ServicePrice price = new ServicePrice(
                request.getServiceId(),
                serviceName,
                request.getBasePrice(),
                request.getPricePerRoom(),
                request.getTaxRate() != null ? request.getTaxRate() : 0.05,
                request.getDescription()
        );
        return priceRepository.save(price);
    }

    @Transactional
    public ServicePrice updatePrice(Long id, PriceRequest request) {
        ServicePrice price = priceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Price not found with ID: " + id));

        price.setBasePrice(request.getBasePrice());
        price.setPricePerRoom(request.getPricePerRoom());
        if (request.getTaxRate() != null) {
            price.setTaxRate(request.getTaxRate());
        }
        if (request.getDescription() != null) {
            price.setDescription(request.getDescription());
        }
        return priceRepository.save(price);
    }

    /**
     * Strict Requirement: Calculate booking totals on the backend using trusted database prices.
     * Never trust totals submitted by the frontend.
     */
    @Transactional(readOnly = true)
    public double calculateBookingTotal(String selectedServicesStr, int roomCount) {
        if (selectedServicesStr == null || selectedServicesStr.trim().isEmpty()) {
            return 0.0;
        }

        String[] serviceNames = selectedServicesStr.split(",");
        double baseSum = 0.0;
        int count = Math.max(1, roomCount);

        for (String rawName : serviceNames) {
            String name = rawName.trim();
            if (name.isEmpty()) continue;

            Optional<CleaningService> serviceOpt = serviceRepository.findByNameIgnoreCase(name);
            if (serviceOpt.isPresent()) {
                baseSum += serviceOpt.get().getBasePrice();
            } else {
                Optional<ServicePrice> priceOpt = priceRepository.findByServiceNameIgnoreCase(name);
                if (priceOpt.isPresent()) {
                    baseSum += priceOpt.get().getBasePrice();
                }
            }
        }

        return Math.round(baseSum * count * 100.0) / 100.0;
    }
}
