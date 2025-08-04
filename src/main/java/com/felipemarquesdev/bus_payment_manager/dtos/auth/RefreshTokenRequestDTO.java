package com.felipemarquesdev.bus_payment_manager.dtos.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(

        @NotBlank(message = "This field cannot be empty")
        String refreshToken
) { }
