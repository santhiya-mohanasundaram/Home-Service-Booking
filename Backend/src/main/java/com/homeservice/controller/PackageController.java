package com.homeservice.controller;

import com.homeservice.dto.ApiResponse;
import com.homeservice.dto.TravelPackageRequest;
import com.homeservice.entity.TravelPackage;
import com.homeservice.service.TravelPackageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing service and travel package APIs.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@RestController
@RequestMapping("/api/packages")
public class PackageController {

    private final TravelPackageService packageService;

    public PackageController(TravelPackageService packageService) {
        this.packageService = packageService;
    }

    /**
     * Retrieves all available packages.
     *
     * @return list of packages
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TravelPackage>>> getAllPackages() {
        List<TravelPackage> packages = packageService.getAllPackages();
        return ResponseEntity.ok(ApiResponse.success("Packages retrieved successfully", packages));
    }

    /**
     * Retrieves package by ID.
     *
     * @param id package identifier
     * @return TravelPackage details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TravelPackage>> getPackageById(@PathVariable Long id) {
        TravelPackage pkg = packageService.getPackageById(id);
        return ResponseEntity.ok(ApiResponse.success("Package retrieved successfully", pkg));
    }

    /**
     * Searches packages by title or destination keyword.
     *
     * @param query search keyword
     * @return matching packages
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TravelPackage>>> searchPackages(@RequestParam(required = false, defaultValue = "") String query) {
        List<TravelPackage> results = packageService.searchPackages(query);
        return ResponseEntity.ok(ApiResponse.success("Packages search completed", results));
    }

    /**
     * Adds a new package offering (Admin only).
     *
     * @param request package payload
     * @return persisted TravelPackage
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TravelPackage>> addPackage(@Valid @RequestBody TravelPackageRequest request) {
        TravelPackage created = packageService.addPackage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Package created successfully", created));
    }

    /**
     * Updates an existing package (Admin only).
     *
     * @param id package ID
     * @param request updated values
     * @return updated TravelPackage
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TravelPackage>> updatePackage(@PathVariable Long id, @Valid @RequestBody TravelPackageRequest request) {
        TravelPackage updated = packageService.updatePackage(id, request);
        return ResponseEntity.ok(ApiResponse.success("Package updated successfully", updated));
    }

    /**
     * Deletes a package (Admin only).
     *
     * @param id package ID
     * @return deletion acknowledgment
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
        return ResponseEntity.ok(ApiResponse.success("Package deleted successfully", null));
    }
}
