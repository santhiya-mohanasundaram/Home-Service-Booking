package com.homeservice.controller;

import com.homeservice.dto.ApiResponse;
import com.homeservice.dto.ServiceRequest;
import com.homeservice.entity.CleaningService;
import com.homeservice.exception.BadRequestException;
import com.homeservice.service.CleaningServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final CleaningServiceService serviceService;

    public ServiceController(CleaningServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CleaningService>>> getAllServices() {
        List<CleaningService> services = serviceService.getAllActiveServices();
        return ResponseEntity.ok(ApiResponse.success("Active services retrieved successfully", services));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CleaningService>> getServiceById(@PathVariable Long id) {
        CleaningService service = serviceService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.success("Service retrieved successfully", service));
    }

    @PutMapping("/{id}/price")
    public ResponseEntity<ApiResponse<CleaningService>> updateServicePrice(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Double price = null;
        if (body.containsKey("price")) {
            price = Double.valueOf(body.get("price").toString());
        } else if (body.containsKey("basePrice")) {
            price = Double.valueOf(body.get("basePrice").toString());
        }
        if (price == null || price <= 0) {
            throw new BadRequestException("Invalid price specified");
        }
        CleaningService updated = serviceService.updateServicePrice(id, price);
        return ResponseEntity.ok(ApiResponse.success("Service price updated successfully", updated));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CleaningService>> updateService(
            @PathVariable Long id,
            @RequestBody ServiceRequest request) {
        CleaningService updated = serviceService.updateService(id, request);
        return ResponseEntity.ok(ApiResponse.success("Service updated successfully", updated));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CleaningService>> addService(@Valid @RequestBody ServiceRequest request) {
        CleaningService created = serviceService.addService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Service created successfully", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id) {
        serviceService.deleteOrDeactivateService(id);
        return ResponseEntity.ok(ApiResponse.success("Service deactivated successfully", null));
    }
}
