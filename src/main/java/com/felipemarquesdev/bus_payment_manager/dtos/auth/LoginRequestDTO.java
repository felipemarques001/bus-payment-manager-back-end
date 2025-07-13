package com.felipemarquesdev.bus_payment_manager.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @Email(message = "Invalid e-mail")
        @NotBlank(message = "This field cannot be empty")
        String email,

        @NotBlank(message = "This field cannot be empty")
        String password
) { }
