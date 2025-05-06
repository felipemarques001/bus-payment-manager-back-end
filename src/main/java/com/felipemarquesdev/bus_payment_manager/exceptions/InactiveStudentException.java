package com.felipemarquesdev.bus_payment_manager.exceptions;

import java.util.UUID;

public class InactiveStudentException extends RuntimeException {

    public InactiveStudentException(UUID id) {
        super(String.format("The student with '%s' id is inactive", id));
    }
}
