package com.felipemarquesdev.bus_payment_manager.exceptions;

public class DiscountExceedsTotalException extends RuntimeException {

    public DiscountExceedsTotalException() {
        super("The discount total is greater than the payment amount");
    }
}
