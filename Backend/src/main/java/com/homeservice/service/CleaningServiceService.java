package com.homeservice.service;

import com.homeservice.dto.ServiceRequest;
import com.homeservice.entity.CleaningService;
import com.homeservice.entity.ServicePrice;
import com.homeservice.exception.BadRequestException;
import com.homeservice.exception.ResourceNotFoundException;
import com.homeservice.repository.CleaningServiceRepository;
import com.homeservice.repository.ServicePriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CleaningServiceService {

    private final CleaningServiceRepository serviceRepository;
    private final ServicePriceRepository servicePriceRepository;

    public CleaningServiceService(CleaningServiceRepository serviceRepository,
                                  ServicePriceRepository servicePriceRepository) {
        this.serviceRepository = serviceRepository;
        this.servicePriceRepository = servicePriceRepository;
    }

    @Transactional(readOnly = true)
    public List<CleaningService> getAllActiveServices() {
        return serviceRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<CleaningService> getAllServices() {
        return serviceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CleaningService getServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cleaning service not found with id: " + id));
    }

    @Transactional
    public CleaningService addService(ServiceRequest request) {
        if (serviceRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Service already exists with name: " + request.getName());
        }
        CleaningService service = new CleaningService(
                request.getName(),
                request.getDescription(),
                request.getBasePrice(),
                request.getPricePerUnit(),
                request.getDuration(),
                request.getActive() != null ? request.getActive() : true,
                request.getImageUrl()
        );
        CleaningService saved = serviceRepository.save(service);

        // Sync with prices repository
        ServicePrice price = new ServicePrice(
                saved.getId(),
                saved.getName(),
                saved.getBasePrice(),
                saved.getPricePerUnit() != null ? saved.getPricePerUnit() : 200.0,
                0.05,
                "Price configuration for " + saved.getName()
        );
        servicePriceRepository.save(price);

        return saved;
    }

    @Transactional
    public CleaningService updateServicePrice(Long id, Double newPrice) {
        CleaningService service = getServiceById(id);
        service.setBasePrice(newPrice);
        CleaningService saved = serviceRepository.save(service);

        // Synchronize corresponding record in prices table
        servicePriceRepository.findByServiceId(id).ifPresentOrElse(
                sp -> {
                    sp.setBasePrice(newPrice);
                    servicePriceRepository.save(sp);
                },
                () -> {
                    ServicePrice sp = new ServicePrice(
                            id,
                            service.getName(),
                            newPrice,
                            service.getPricePerUnit() != null ? service.getPricePerUnit() : 200.0,
                            0.05,
                            "Standard base package for " + service.getName()
                    );
                    servicePriceRepository.save(sp);
                }
        );

        return saved;
    }

    @Transactional
    public CleaningService updateService(Long id, ServiceRequest request) {
        CleaningService service = getServiceById(id);
        if (request.getName() != null) service.setName(request.getName());
        if (request.getDescription() != null) service.setDescription(request.getDescription());
        if (request.getBasePrice() != null) {
            service.setBasePrice(request.getBasePrice());
            servicePriceRepository.findByServiceId(id).ifPresent(sp -> {
                sp.setBasePrice(request.getBasePrice());
                servicePriceRepository.save(sp);
            });
        }
        if (request.getPricePerUnit() != null) service.setPricePerUnit(request.getPricePerUnit());
        if (request.getDuration() != null) service.setDuration(request.getDuration());
        if (request.getActive() != null) {
            service.setActive(request.getActive());
        }
        if (request.getImageUrl() != null) {
            service.setImageUrl(request.getImageUrl());
        }
        return serviceRepository.save(service);
    }

    @Transactional
    public void deleteOrDeactivateService(Long id) {
        CleaningService service = getServiceById(id);
        // Soft delete / deactivate service to maintain historical booking reference integrity
        service.setActive(false);
        serviceRepository.save(service);
    }
}
