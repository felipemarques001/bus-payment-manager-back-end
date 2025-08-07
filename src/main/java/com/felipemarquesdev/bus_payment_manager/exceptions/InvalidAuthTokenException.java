package com.felipemarquesdev.bus_payment_manager.exceptions;

public class InvalidAuthTokenException extends RuntimeException {

    public InvalidAuthTokenException(String tokenType) {
        super(String.format("Invalid %s token", tokenType));
    }
}