package com.felipemarquesdev.bus_payment_manager.dtos.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.felipemarquesdev.bus_payment_manager.dtos.financialHelp.FinancialHelpRequestDTO;

import java.math.BigDecimal;
import java.util.List;

public record PaymentAmountsRequestDTO(

        @Digits(
                integer = 6,
                fraction = 2,
                message = "This field must contain a maximum of 6 integers and 2 fractional digits"
        )
        @Positive(message = "This field must be greater than zero")
        @NotNull(message = "This field cannot be null")
        BigDecimal totalAmount,

        List<@Valid FinancialHelpRequestDTO> financialHelps,

        @Digits(
                integer = 5,
                fraction = 0,
                message = "This field must contain a maximum of 6 integers digits"
        )
        @NotNull(message = "This field cannot be null")
        Integer studentsQuantity
) { }
