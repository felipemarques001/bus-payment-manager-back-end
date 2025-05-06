package com.felipemarquesdev.bus_payment_manager.dtos.payment;

import com.felipemarquesdev.bus_payment_manager.dtos.financialHelp.FinancialHelpRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;
import java.util.List;

public record PaymentRequestDTO(

        @NotBlank(message = "This field cannot be empty")
        @Length(max = 9, message = "The month must contain a maximum of {max} characters long")
        String month,

        @NotBlank(message = "This field cannot be empty")
        @Length(max = 4, message = "The year must contain a maximum of {max} characters long")
        String year,

        @Digits(
                integer = 6,
                fraction = 2,
                message = "This field must contain a maximum of 6 integers and 2 fractional digits"
        )
        @Positive(message = "This field must be greater than zero")
        @NotNull(message = "This field cannot be null")
        BigDecimal totalAmount,

        List<@Valid FinancialHelpRequestDTO> financialHelps,

        @NotEmpty
        List<@UUID(message = "This field is not a valid UUID")
             @NotNull(message = "This field cannot be null") String> studentsIds
) { }
