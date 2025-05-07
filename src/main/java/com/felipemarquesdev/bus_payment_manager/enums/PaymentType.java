package com.felipemarquesdev.bus_payment_manager.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentType {

    PIX("PIX"),
    CARD("CARD"),
    BILLET("BILLET"),
    CASH_PAYMENT("CASH_PAYMENT");

    private final String value;
}
