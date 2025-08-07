package com.felipemarquesdev.bus_payment_manager.controllers;

import com.felipemarquesdev.bus_payment_manager.dtos.auth.LoginRequestDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.AccessTokenResponseDTO;
import com.felipemarquesdev.bus_payment_manager.dtos.auth.UserTokensDTO;
import com.felipemarquesdev.bus_payment_manager.services.interfaces.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${api.security.refresh.token.max.seconds}")
    private long refreshTokenMaxSeconds;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
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
    public ResponseEntity<AccessTokenResponseDTO> refreshToken(@CookieValue("refreshToken") String refreshToken) {
        AccessTokenResponseDTO responseBody = authService.refreshToken(refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }
}
