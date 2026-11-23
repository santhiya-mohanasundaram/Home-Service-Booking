package com.homeservice.controller;

import com.homeservice.dto.ApiResponse;
import com.homeservice.entity.CleaningService;
import com.homeservice.service.CleaningServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
