package com.felipemarquesdev.bus_payment_manager.dtos.financialHelp;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record FinancialHelpRequestDTO(

        @NotBlank(message = "This field cannot be empty")
        String name,

        @Digits(
                integer = 6,
                fraction = 2,
                message = "This field must contain a maximum of 6 integers and 2 fractional digits"
        )
        @Positive(message = "This field must be greater than zero")
        @NotNull(message = "This field cannot be null")
        BigDecimal amount
) { }
