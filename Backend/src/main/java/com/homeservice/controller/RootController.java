package com.homeservice.controller;

import com.homeservice.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Root and Health Controller providing API status and endpoint discovery.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@RestController
public class RootController {

    @GetMapping({"/", "/api", "/api/status"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApiStatus() {
        Map<String, Object> info = Map.of(
                "service", "Home Service Booking Backend API",
                "status", "UP",
                "port", 8080,
                "swaggerUI", "/swagger-ui/index.html",
                "apiDocs", "/v3/api-docs",
                "endpoints", Map.of(
                        "register", "POST /api/auth/register",
                        "login", "POST /api/auth/login",
                        "profile", "GET /api/users/profile [Requires Bearer Token]",
                        "bookings", "GET /api/bookings/my [Requires Bearer Token]",
                        "admin", "GET /api/admin/dashboard [Requires ROLE_ADMIN Token]"
                )
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Home Service Booking Backend API is running successfully on port 8080. Swagger UI is available at /swagger-ui/index.html",
                info
        ));
    }
}
