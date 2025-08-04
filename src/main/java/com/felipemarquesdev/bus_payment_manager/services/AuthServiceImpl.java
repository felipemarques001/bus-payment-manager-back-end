package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.RefreshTokenRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.RefreshTokenResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.User;
import com.felipemarquesdev.bus_payment_manager.exceptions.InvalidRefreshTokenException;
import com.felipemarquesdev.bus_payment_manager.exceptions.UserNotFoundException;
import com.felipemarquesdev.bus_payment_manager.infra.security.TokenService;
import com.felipemarquesdev.bus_payment_manager.repositories.UserRepository;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.AuthService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UserRepository userRepository,
            TokenService tokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByEmail(loginRequestDTO.email())
                .orElseThrow(() -> new UserNotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(loginRequestDTO.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);
        return new LoginResponseDTO(accessToken, refreshToken);
    }

    @Override
    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO) {
        String userEmail = tokenService.validateRefreshToken(refreshTokenRequestDTO.refreshToken());
        if (userEmail == null) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with the e-mail provided in the refresh token"));

        String accessToken = tokenService.generateAccessToken(user);
        return new RefreshTokenResponseDTO(accessToken);
    }
}
