package com.felipemarquesdev.bus_payment_manager.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, String fieldName) {
        super(String.format("%s not found with the %s provided", resourceName, fieldName));
    }
}
