package com.felipemarquesdev.bus_payment_manager.dtos.student;

import jakarta.validation.constraints.NotNull;

public record StudentActiveRequestDTO (

        @NotNull(message = "This field cannot be null")
        Boolean active
) { }
