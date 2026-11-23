package com.homeservice.service;

import com.homeservice.dto.ServiceRequest;
import com.homeservice.entity.CleaningService;
import com.homeservice.exception.BadRequestException;
import com.homeservice.exception.ResourceNotFoundException;
import com.homeservice.repository.CleaningServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CleaningServiceService {

    private final CleaningServiceRepository serviceRepository;

    public CleaningServiceService(CleaningServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
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
        return serviceRepository.save(service);
    }

    @Transactional
    public CleaningService updateService(Long id, ServiceRequest request) {
        CleaningService service = getServiceById(id);
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setBasePrice(request.getBasePrice());
        service.setPricePerUnit(request.getPricePerUnit());
        service.setDuration(request.getDuration());
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
