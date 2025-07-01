package com.felipemarquesdev.bus_payment_manager.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TuitionStatus {

    PAID("PAID"),
    PENDING("PENDING");

    private final String value;
}
