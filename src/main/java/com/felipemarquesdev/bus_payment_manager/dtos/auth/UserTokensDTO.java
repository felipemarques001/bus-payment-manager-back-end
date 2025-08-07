package com.felipemarquesdev.bus_payment_manager.dtos.auth;

public record UserTokensDTO(

        String accessToken,
        String refreshToken
) { }