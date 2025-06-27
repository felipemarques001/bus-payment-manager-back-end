package com.felipemarquesdev.bus_payment_manager.exceptions;

public class BadRequestValueException extends RuntimeException {

    public BadRequestValueException(String message) {
        super(message);
    }
}
