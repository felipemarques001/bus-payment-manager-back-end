package com.felipemarquesdev.bus_payment_manager.dtos.payment;

import com.felipemarquesdev.bus_payment_manager.dtos.financialHelp.FinancialHelpResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaymentResponseDTO(
        
        UUID id,

        String month,

        String year,
        
        BigDecimal totalAmount,

        BigDecimal totalToBePaid,

        BigDecimal tuitionAmount,

        List<FinancialHelpResponseDTO> financialHelps
) {
    
    public static PaymentResponseDTO fromPayment(Payment payment) {
        List<FinancialHelpResponseDTO> financialHelps = payment.getFinancialHelps()
                .stream()
                .map(FinancialHelpResponseDTO::fromFinancialHelp)
                .toList();

        return new PaymentResponseDTO(
                payment.getId(),
                payment.getInvoiceMonth(),
                payment.getInvoiceYear(),
                payment.getTotalAmount(),
                payment.getTotalToBePaid(),
                payment.getTuitionAmount(),
                financialHelps
        );
    }
}
