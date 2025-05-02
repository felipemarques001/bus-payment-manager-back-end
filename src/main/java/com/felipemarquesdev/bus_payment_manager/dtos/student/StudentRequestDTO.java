package com.felipemarquesdev.bus_payment_manager.dtos.student;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record StudentRequestDTO(

        @NotBlank(message = "This field cannot be empty")
        String name,

        @NotBlank(message = "This field cannot be empty")
        @Length(max = 11, message = "The phone number must contain a maximum of {max} characters long")
        String phoneNumber,

        String major,

        String college
) { }
