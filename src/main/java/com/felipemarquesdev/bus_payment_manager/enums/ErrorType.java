package com.felipemarquesdev.bus_payment_manager.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorType {

    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND"),
    FIELD_ALREADY_IN_USE("FIELD_ALREADY_IN_USE");

    private String value;
}
