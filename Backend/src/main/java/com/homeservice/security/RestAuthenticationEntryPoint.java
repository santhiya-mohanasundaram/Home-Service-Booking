package com.homeservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homeservice.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message = authException.getMessage();
        if (message == null || message.trim().isEmpty() || message.equalsIgnoreCase("Bad credentials")) {
            message = "Full authentication is required to access this resource. Please provide a valid Bearer token in the Authorization header.";
        }
        ApiResponse<Void> body = ApiResponse.error(message);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
