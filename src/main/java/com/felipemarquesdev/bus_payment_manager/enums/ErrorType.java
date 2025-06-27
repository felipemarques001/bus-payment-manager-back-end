package com.felipemarquesdev.bus_payment_manager.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorType {

    BAD_REQUEST_VALUE("BAD_REQUEST_VALUE"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND"),
    STUDENT_NOT_ACTIVE("STUDENT_NOT_ACTIVE"),
    FIELD_ALREADY_IN_USE("FIELD_ALREADY_IN_USE"),
    DISCOUNT_EXCEEDS_TOTAL("DISCOUNT_EXCEEDS_TOTAL");

    private final String value;
}
