package com.felipemarquesdev.bus_payment_manager.services;

import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginResponseDTO;
import com.felipemarquesdev.bus_payment_manager.entities.User;
import com.felipemarquesdev.bus_payment_manager.exceptions.UserNotFoundException;
import com.felipemarquesdev.bus_payment_manager.infra.security.TokenService;
import com.felipemarquesdev.bus_payment_manager.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final String USER_EMAIL = "email@email.com";
    private final String USER_PASSWORD = "password";
    private final String ERROR_MESSAGE = "Invalid credentials";

    private User user;
    private LoginRequestDTO loginRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setEmail(USER_EMAIL);
        user.setPassword(USER_PASSWORD);

        loginRequestDTO = new LoginRequestDTO(USER_EMAIL, USER_PASSWORD);
    }

    @Test
    @DisplayName("Given valid user, when login(), then return DTO with token")
    void loginSuccessCase() {
        // Given
        String accessToken = "valid_access_token";
        String refreshToken = "valid_refresh_token";
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenService.generateAccessToken(user)).thenReturn(accessToken);
        when(tokenService.generateRefreshToken(user)).thenReturn(refreshToken);

        // When
        LoginResponseDTO response = authService.login(loginRequestDTO);

        // Then
        assertEquals(accessToken, response.accessToken());
        assertEquals(refreshToken, response.refreshToken());
    }

    @Test
    @DisplayName("Given invalid e-mail, when login(), then throw UserNotFoundException")
    void loginFirstFailCase() {
        // Given
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        try {
            // When
            authService.login(loginRequestDTO);
        } catch (RuntimeException ex) {
            // Then
            assertEquals(UserNotFoundException.class, ex.getClass());
            assertEquals(ERROR_MESSAGE, ex.getMessage());
        }
    }

    @Test
    @DisplayName("Given invalid password, when login(), then throw BadCredentialsException")
    void loginSecondFailCase() {
        // Given
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        try {
            // When
            authService.login(loginRequestDTO);
        } catch (RuntimeException ex) {
            // Then
            assertEquals(BadCredentialsException.class, ex.getClass());
            assertEquals(ERROR_MESSAGE, ex.getMessage());
        }
    }
}