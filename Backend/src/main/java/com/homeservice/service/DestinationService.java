package com.homeservice.service;

import com.homeservice.dto.DestinationRequest;
import com.homeservice.entity.Destination;
import com.homeservice.exception.DuplicateResourceException;
import com.homeservice.exception.ResourceNotFoundException;
import com.homeservice.repository.DestinationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing destination catalogs, searches, and administrative operations.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Service
@Transactional
public class DestinationService {

    private final DestinationRepository destinationRepository;

    public DestinationService(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    /**
     * Adds a new destination after checking for uniqueness.
     *
     * @param request the destination payload
     * @return newly created Destination entity
     * @throws DuplicateResourceException if a destination with same name and country exists
     */
    public Destination addDestination(DestinationRequest request) {
        // Developed by Santhiya Mohanasundaram - Handles unique validation for travel spots
        if (destinationRepository.existsByNameIgnoreCaseAndCountryIgnoreCase(request.getName(), request.getCountry())) {
            throw new DuplicateResourceException("Destination '" + request.getName() + "' in country '" + request.getCountry() + "' already exists.");
        }
        Destination destination = new Destination(
                request.getName(),
                request.getCountry(),
                request.getDescription(),
                request.getImageUrl(),
                request.getCategory(),
                request.isPopular()
        );
        return destinationRepository.save(destination);
    }

    /**
     * Retrieves all available destinations.
     *
     * @return list of destinations
     */
    @Transactional(readOnly = true)
    public List<Destination> getAllDestinations() {
        // Reviewed by Dhanusri D - Verified read-only transaction performance
        return destinationRepository.findAll();
    }

    /**
     * Finds a destination by its database identifier.
     *
     * @param id the destination ID
     * @return Destination if found
     * @throws ResourceNotFoundException if no destination matches the ID
     */
    @Transactional(readOnly = true)
    public Destination getDestinationById(Long id) {
        return destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found with id: " + id));
    }

    /**
     * Searches destinations by name, country, or category keywords.
     *
     * @param query search keyword
     * @return matching destinations list
     */
    @Transactional(readOnly = true)
    public List<Destination> searchDestinations(String query) {
        if (query == null || query.trim().isEmpty()) {
            return destinationRepository.findAll();
        }
        return destinationRepository.searchDestinations(query.trim());
    }

    /**
     * Updates an existing destination's metadata.
     *
     * @param id the target destination ID
     * @param request updated values
     * @return updated Destination entity
     */
    public Destination updateDestination(Long id, DestinationRequest request) {
        Destination destination = getDestinationById(id);
        destination.setName(request.getName());
        destination.setCountry(request.getCountry());
        destination.setDescription(request.getDescription());
        destination.setImageUrl(request.getImageUrl());
        destination.setCategory(request.getCategory());
        destination.setPopular(request.isPopular());
        return destinationRepository.save(destination);
    }

    /**
     * Deletes a destination from the catalog.
     *
     * @param id the target destination ID
     */
    public void deleteDestination(Long id) {
        Destination destination = getDestinationById(id);
        destinationRepository.delete(destination);
    }
}
