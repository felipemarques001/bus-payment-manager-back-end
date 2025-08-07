package com.felipemarquesdev.bus_payment_manager.services.interfaces;

import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.AccessTokenResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.UserTokensDTO;

public interface AuthService {

    UserTokensDTO login(LoginRequestDTO loginRequestDTO);

    AccessTokenResponseDTO refreshToken(String refreshToken);
}
