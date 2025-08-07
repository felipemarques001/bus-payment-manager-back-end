package com.felipemarquesdev.bus_payment_manager.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.felipemarquesdev.bus_payment_manager.entities.User;
import com.felipemarquesdev.bus_payment_manager.exceptions.InvalidAuthTokenException;
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

    @Value("${api.security.access.token.max.seconds}")
    private long accessTokenMaxSeconds;

    @Value("${api.security.refresh.token.max.seconds}")
    private long refreshTokenMaxSeconds;

    public String generateAccessToken(User user) {
        try {
            return generateToken(accessTokenSecret, user, accessTokenMaxSeconds);
        } catch (JWTCreationException ex) {
            throw new RuntimeException("Error while creating access token");
        }
    }

    public String generateRefreshToken(User user) {
        try {
            return generateToken(refreshTokenSecret, user, refreshTokenMaxSeconds);
        } catch (JWTCreationException ex) {
            throw new RuntimeException("Error while creating refresh token");
        }
    }

    public String validateAccessToken(String token) {
        try {
            return validateToken(accessTokenSecret, token);
        } catch (JWTVerificationException ex) {
            return null;
        }
    }

    public String validateRefreshToken(String token) {
        try {
            return validateToken(refreshTokenSecret, token);
        } catch (JWTVerificationException ex) {
            throw new InvalidAuthTokenException("refresh");
        }
    }

    private String generateToken(String secret, User user, long expirationMinutes) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getEmail())
                .withExpiresAt(generateExpirationDate(expirationMinutes))
                .sign(algorithm);
    }

    private String validateToken(String secret, String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
                .verify(token)
                .getSubject();
    }

    private Instant generateExpirationDate(long maxSeconds){
        return LocalDateTime.now().plusSeconds(maxSeconds).toInstant(ZoneOffset.of("-03:00"));
    }
}
