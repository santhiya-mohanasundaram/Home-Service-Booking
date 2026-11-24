package com.homeservice.controller;

import com.homeservice.dto.ApiResponse;
import com.homeservice.entity.ServicePrice;
import com.homeservice.exception.BadRequestException;
import com.homeservice.service.PriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PutMapping("/{serviceId}")
    public ResponseEntity<ApiResponse<ServicePrice>> updatePrice(
            @PathVariable Long serviceId,
            @RequestBody Map<String, Object> body) {
        Double basePrice = null;
        if (body.containsKey("basePrice")) {
            basePrice = Double.valueOf(body.get("basePrice").toString());
        } else if (body.containsKey("price")) {
            basePrice = Double.valueOf(body.get("price").toString());
        }
        if (basePrice == null || basePrice <= 0) {
            throw new BadRequestException("Invalid price specified");
        }
        ServicePrice updated = priceService.updateBasePriceByServiceId(serviceId, basePrice);
        return ResponseEntity.ok(ApiResponse.success("Price updated successfully", updated));
    }
}
