package com.felipemarquesdev.bus_payment_manager.dtos.payment;

import com.felipemarquesdev.bus_payment_manager.entities.Payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentSummaryResponseDTO(

        UUID id,

        String month,

        String year,

        BigDecimal totalAmount,

        BigDecimal tuitionAmount
) {

    public static PaymentSummaryResponseDTO fromPayment(Payment payment) {
        return new PaymentSummaryResponseDTO(
                payment.getId(),
                payment.getMonth(),
                payment.getYear(),
                payment.getTotalAmount(),
                payment.getTuitionAmount()
        );
    }
}
