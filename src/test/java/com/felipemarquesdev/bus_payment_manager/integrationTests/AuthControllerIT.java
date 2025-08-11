package com.felipemarquesdev.bus_payment_manager.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginRequestDTO;
import com.felipemarquesdev.bus_payment_manager.entities.User;
import com.felipemarquesdev.bus_payment_manager.enums.ErrorType;
import com.felipemarquesdev.bus_payment_manager.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${api.security.refresh.token.max.seconds}")
    private int cookieMaxAge;

    private final String ENDPOINT = "/api/auth";
    private final String LOGIN_ENDPOINT = ENDPOINT + "/login";
    private final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private final String USER_EMAIL = "test@gmail.com";
    private final String USER_PASSWORD = "123";

    @BeforeEach
    void setUp() {
        saveUser();
    }

    @AfterEach
    void shutDown() {
        deleteUser();
    }

    @Test
    @DisplayName("Given valid user, when login(), then return 200 with accessToken and refreshToken's cookie")
    void loginSuccessCase() throws Exception {
        // Given
        LoginRequestDTO requestBody = new LoginRequestDTO(USER_EMAIL, USER_PASSWORD);

        // When and Then
        mockMvc.perform(post(LOGIN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.accessToken", not("")))
                .andExpect(cookie().value(REFRESH_TOKEN_COOKIE_NAME, notNullValue()))
                .andExpect(cookie().value(REFRESH_TOKEN_COOKIE_NAME, not("")))
                .andExpect(cookie().httpOnly(REFRESH_TOKEN_COOKIE_NAME, true))
                .andExpect(cookie().maxAge(REFRESH_TOKEN_COOKIE_NAME, cookieMaxAge));
    }

    @Test
    @DisplayName("Given invalid e-mail, when login, then return 401 and error data")
    void loginFailCaseByInvalidEmail() throws Exception {
        // Given
        LoginRequestDTO requestBody = new LoginRequestDTO("invalid-email@gmail.com", USER_PASSWORD);

        // When and Then
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorType").value(ErrorType.BAD_CREDENTIALS.getValue()))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("Given invalid e-mail, when login, then return 401 and error data")
    void loginFailCaseByInvalidPassword() throws Exception {
        // Given
        LoginRequestDTO requestBody = new LoginRequestDTO(USER_EMAIL, "invalid password");

        // When and Then
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorType").value(ErrorType.BAD_CREDENTIALS.getValue()))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("Given valid refreshToken's cookie, when refreshToken(), then return 200 and accessToken")
    void refreshTokenSuccessCase() throws Exception {
        // Given
        Cookie refreshTokenCookie = getRefreshTokenCookie();
        String url = ENDPOINT + "/refresh-token";

        // When and Then
        mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.accessToken", not("")));
    }

    @Test
    @DisplayName("Given invalid refreshToken's cookie, when refreshToken(), then return 401 and error data")
    void refreshTokenFailCase() throws Exception {
        // Given
        Cookie refreshTokenCookie = getRefreshTokenCookie();
        refreshTokenCookie.setValue("invalid refresh token");
        String url = ENDPOINT + "/refresh-token";

        // When and Then
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorType").value(ErrorType.INVALID_AUTH_TOKEN.getValue()))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    private Cookie getRefreshTokenCookie() throws Exception {
        LoginRequestDTO requestBody = new LoginRequestDTO(USER_EMAIL, USER_PASSWORD);

        MvcResult result = mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andReturn();

        return result.getResponse().getCookie(REFRESH_TOKEN_COOKIE_NAME);
    }

    private void saveUser() {
        User user = new User();
        user.setEmail(USER_EMAIL);
        user.setPassword(passwordEncoder.encode(USER_PASSWORD));
        userRepository.save(user);
    }

    private void deleteUser() {
        userRepository.deleteAll();
    }
}
