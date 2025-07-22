package com.felipemarquesdev.bus_payment_manager.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginResponseDTO;
import com.felipemarquesdev.bus_payment_manager.enums.ErrorType;
import com.felipemarquesdev.bus_payment_manager.exceptions.UserNotFoundException;
import com.felipemarquesdev.bus_payment_manager.infra.security.TokenService;
import com.felipemarquesdev.bus_payment_manager.repositories.UserRepository;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // We need to define these beans to prevent errors in auth token creation
    @MockitoBean
    private TokenService tokenService;
    @MockitoBean
    private UserRepository userRepository;

    private final String ENDPOINT = "/api/auth/login";
    private final String USER_EMAIL = "test@email.com";
    private final String USER_PASSWORD = "123";

    @Test
    @DisplayName("Given valid credentials, when POST to login, then return 200 and token")
    void loginSuccessCase() throws Exception {
        // Given
        String token = "valid_token";
        LoginRequestDTO requestBody = new LoginRequestDTO(USER_EMAIL, USER_PASSWORD);
        LoginResponseDTO responseBody = new LoginResponseDTO(token);
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(responseBody);

        // when and then
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token));
    }

    @Test
    @DisplayName("Given invalid credentials, when POST to login, then return 401 and error data")
    void loginFailCase() throws Exception {
        // Given
        String errorMessage = "Invalid credentials";
        LoginRequestDTO requestBody = new LoginRequestDTO(USER_EMAIL, USER_PASSWORD);
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new UserNotFoundException(errorMessage));

        // when and then
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorType").value(ErrorType.BAD_CREDENTIALS.getValue()))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("Given empty fields, when POST to login, then return 400 and error data")
    void loginFailCaseByEmptyFields() throws Exception {
        // Given
        String errorMessage = "This field cannot be empty";
        LoginRequestDTO requestBody = new LoginRequestDTO("", "");

        // when and then
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value(errorMessage))
                .andExpect(jsonPath("$.password").value(errorMessage));
    }

    @Test
    @DisplayName("Given invalid e-mail, when POST to login, then return 400 and error data")
    void loginFailCaseByInvalidEmail() throws Exception {
        // Given
        LoginRequestDTO requestBody = new LoginRequestDTO("test-email", USER_PASSWORD);

        // when and then
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid e-mail"));
    }
}