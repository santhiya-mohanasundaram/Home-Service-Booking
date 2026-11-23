package com.homeservice.service;

import com.homeservice.dto.TravelPackageRequest;
import com.homeservice.entity.TravelPackage;
import com.homeservice.exception.ResourceNotFoundException;
import com.homeservice.repository.TravelPackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing bundled service and travel packages.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Service
@Transactional
public class TravelPackageService {

    private final TravelPackageRepository packageRepository;

    public TravelPackageService(TravelPackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    /**
     * Adds a new package offering.
     *
     * @param request package details
     * @return persisted TravelPackage
     */
    public TravelPackage addPackage(TravelPackageRequest request) {
        // Developed by Santhiya Mohanasundaram - Assembles package items and durations
        TravelPackage pkg = new TravelPackage(
                request.getTitle(),
                request.getDestination(),
                request.getDurationDays(),
                request.getPrice(),
                request.getDescription(),
                request.getIncludedServices()
        );
        return packageRepository.save(pkg);
    }

    /**
     * Retrieves all packages.
     *
     * @return list of packages
     */
    @Transactional(readOnly = true)
    public List<TravelPackage> getAllPackages() {
        return packageRepository.findAll();
    }

    /**
     * Finds a package by ID.
     *
     * @param id package identifier
     * @return TravelPackage
     */
    @Transactional(readOnly = true)
    public TravelPackage getPackageById(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id));
    }

    /**
     * Searches packages by title or destination keyword.
     *
     * @param query search query
     * @return matching packages
     */
    @Transactional(readOnly = true)
    public List<TravelPackage> searchPackages(String query) {
        if (query == null || query.trim().isEmpty()) {
            return packageRepository.findAll();
        }
        return packageRepository.searchPackages(query.trim());
    }

    /**
     * Updates an existing package.
     *
     * @param id package ID
     * @param request updated values
     * @return updated TravelPackage
     */
    public TravelPackage updatePackage(Long id, TravelPackageRequest request) {
        // Reviewed by Dhanusri D - Verified price integrity and service lists
        TravelPackage pkg = getPackageById(id);
        pkg.setTitle(request.getTitle());
        pkg.setDestination(request.getDestination());
        pkg.setDurationDays(request.getDurationDays());
        pkg.setPrice(request.getPrice());
        pkg.setDescription(request.getDescription());
        pkg.setIncludedServices(request.getIncludedServices());
        return packageRepository.save(pkg);
    }

    /**
     * Deletes a package.
     *
     * @param id package ID
     */
    public void deletePackage(Long id) {
        TravelPackage pkg = getPackageById(id);
        packageRepository.delete(pkg);
    }
}
