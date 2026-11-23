package com.homeservice.exception;

/**
 * Exception thrown when a client request fails business validation rules.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
public class InvalidRequestException extends RuntimeException {

    /**
     * Constructs a new InvalidRequestException with the specified error message.
     *
     * @param message Detailed explanation of the invalid request
     */
    public InvalidRequestException(String message) {
        super(message);
    }
}
