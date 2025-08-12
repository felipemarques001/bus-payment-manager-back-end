package com.felipemarquesdev.bus_payment_manager.controllers;

import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.AccessTokenResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.UserTokensDTO;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "AuthController", description = "Handles all endpoints for managing of authentication requests")
public class AuthController {

    private final AuthService authService;

    @Value("${api.security.refresh.token.max.seconds}")
    private long refreshTokenMaxSeconds;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user by login")
    @ApiResponse(responseCode = "200", description = "User authenticated with success")
    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content())
    public ResponseEntity<AccessTokenResponseDTO> login(
            @RequestBody @Valid LoginRequestDTO requestBody,
            HttpServletResponse response
    ) {
        UserTokensDTO tokens = authService.login(requestBody);
        ResponseCookie accessTokenCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/refresh-token")
                .maxAge(refreshTokenMaxSeconds)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        AccessTokenResponseDTO accessTokenResponseDTO = new AccessTokenResponseDTO(tokens.accessToken());
        return ResponseEntity.status(HttpStatus.OK).body(accessTokenResponseDTO);
    }

    @GetMapping("/refresh-token")
    @Operation(summary = "Authenticate user by refresh token sent in a cookie")
    @ApiResponse(responseCode = "200", description = "User authenticated with success")
    @ApiResponse(responseCode = "401", description = "Invalid refresh token", content = @Content())
    public ResponseEntity<AccessTokenResponseDTO> refreshToken(@CookieValue("refreshToken") String refreshToken) {
        AccessTokenResponseDTO responseBody = authService.refreshToken(refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }
}
