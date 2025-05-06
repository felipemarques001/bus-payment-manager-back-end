package com.felipemarquesdev.bus_payment_manager.dtos.financialHelp;

import com.felipemarquesdev.bus_payment_manager.entities.FinancialHelp;

import java.math.BigDecimal;
import java.util.UUID;

public record FinancialHelpResponseDTO (

        UUID id,

        String name,

        BigDecimal amount
){

    public static FinancialHelpResponseDTO fromFinancialHelp(FinancialHelp financialHelp) {
        return new FinancialHelpResponseDTO(
                financialHelp.getId(),
                financialHelp.getName(),
                financialHelp.getAmount()
        );
    }
}
