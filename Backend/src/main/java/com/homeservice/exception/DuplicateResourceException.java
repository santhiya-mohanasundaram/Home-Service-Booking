package com.homeservice.exception;

/**
 * Exception thrown when an attempt is made to create a duplicate resource in the system.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
public class DuplicateResourceException extends RuntimeException {

    /**
     * Constructs a new DuplicateResourceException with the specified error message.
     *
     * @param message Detailed reason why the resource is duplicated
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
