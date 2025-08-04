package com.felipemarquesdev.bus_payment_manager.services.interfaces;

import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.RefreshTokenRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.RefreshTokenResponseDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

    RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO);
}
