package com.felipemarquesdev.bus_payment_manager.dtos.payment;

import com.felipemarquesdev.bus_payment_manager.entities.Payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentSummaryResponseDTO(

        UUID id,

        String invoiceMonth,

        String invoiceYear,

        BigDecimal totalAmount,

        BigDecimal tuitionAmount
) {

    public static PaymentSummaryResponseDTO fromPayment(Payment payment) {
        return new PaymentSummaryResponseDTO(
                payment.getId(),
                payment.getInvoiceMonth(),
                payment.getInvoiceYear(),
                payment.getTotalAmount(),
                payment.getTuitionAmount()
        );
    }
}
