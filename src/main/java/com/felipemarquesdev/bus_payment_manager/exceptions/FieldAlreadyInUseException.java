package com.felipemarquesdev.bus_payment_manager.exceptions;

import lombok.Getter;

@Getter
public class FieldAlreadyInUseException extends RuntimeException {

    public FieldAlreadyInUseException(String fieldError) {
        super(String.format("The %s is already in use!", fieldError));
    }
}
