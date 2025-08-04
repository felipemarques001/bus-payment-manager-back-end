package com.felipemarquesdev.bus_payment_manager.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.felipemarquesdev.bus_payment_manager.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.access.token.secret}")
    private String accessTokenSecret;

    @Value("${api.security.refresh.token.secret}")
    private String refreshTokenSecret;

    @Value("${api.security.token.issuer}")
    private String issuer;

    public String generateAccessToken(User user) {
        try {
            long accessTokenExpirationTimeInMinutes = 15L;
            return generateToken(accessTokenSecret, user, accessTokenExpirationTimeInMinutes);
        } catch (JWTCreationException ex) {
            throw new RuntimeException("Error while creating access token");
        }
    }

    public String generateRefreshToken(User user) {
        try {
            long refreshTokenExpirationTimeInMinutes = 7L * 24L * 60L;
            return generateToken(refreshTokenSecret, user, refreshTokenExpirationTimeInMinutes);
        } catch (JWTCreationException ex) {
            throw new RuntimeException("Error while creating refresh token");
        }
    }

    public String validateAccessToken(String token) {
        return validateToken(accessTokenSecret, token);
    }

    public String validateRefreshToken(String token) {
        return validateToken(refreshTokenSecret, token);
    }

    private String generateToken(String secret, User user, long expirationMinutes) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getEmail())
                .withExpiresAt(generateExpirationDate(expirationMinutes))
                .sign(algorithm);
    }

    private String validateToken(String secret, String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException ex) {
            return null;
        }
    }

    private Instant generateExpirationDate(long minutes){
        return LocalDateTime.now().plusMinutes(minutes).toInstant(ZoneOffset.of("-03:00"));
    }
}
