package com.homeservice.controller;

import com.homeservice.dto.ApiResponse;
import com.homeservice.entity.ServicePrice;
import com.homeservice.service.PriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicePrice>>> getAllPrices() {
        List<ServicePrice> prices = priceService.getAllPrices();
        return ResponseEntity.ok(ApiResponse.success("Prices retrieved successfully", prices));
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ServicePrice>> getPriceByService(@PathVariable Long serviceId) {
        ServicePrice price = priceService.getPriceByServiceId(serviceId);
        return ResponseEntity.ok(ApiResponse.success("Price for service retrieved successfully", price));
    }
}
