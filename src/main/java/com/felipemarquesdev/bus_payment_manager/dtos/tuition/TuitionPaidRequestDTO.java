package com.felipemarquesdev.bus_payment_manager.dtos.tuition;

import com.felipemarquesdev.bus_payment_manager.enums.PaymentType;
import com.felipemarquesdev.bus_payment_manager.validations.enumType.EnumType;
import jakarta.validation.constraints.NotNull;

public record TuitionPaidRequestDTO(

        @EnumType(enumClass = PaymentType.class)
        @NotNull(message = "This field cannot be null")
        String paymentType
) { }
