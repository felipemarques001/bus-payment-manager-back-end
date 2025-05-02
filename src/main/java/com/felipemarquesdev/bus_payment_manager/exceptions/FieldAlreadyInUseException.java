package com.felipemarquesdev.bus_payment_manager.exceptions;

import lombok.Getter;

@Getter
public class FieldAlreadyInUseException extends RuntimeException {
    private final String fieldError;

    public FieldAlreadyInUseException(String fieldError) {
        super("The value is already in use!");
        this.fieldError = fieldError;
    }
}
