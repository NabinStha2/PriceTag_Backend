package com.example.pricetag.features.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Creates and reads JWTs.
 * <p>
 * jwt.secret MUST be set via an environment variable or
 * application.properties (e.g. jwt.secret=${JWT_SECRET}) - never hardcode
 * it in source. The @PostConstruct check below makes the app refuse to
 * start if it's missing or too short, which is exactly what you want:
 * fail loudly at startup, not silently at 3am in production.
 * <p>
 * To generate a good secret: 32+ random bytes, Base64-encoded, e.g.
 * openssl rand -base64 32
 */
@Component
public class JwtService {

    // 10 days. Consider shortening this and leaning more on the refresh
    // token (see TokenService) if you want tighter blast-radius on a
    // stolen access token.
    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 24 * 10;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key signKey;

    @PostConstruct
    void init() {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be set and at least 32 bytes (256 bits) long");
        }
        this.signKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(signKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // Throws io.jsonwebtoken.JwtException (or a subclass) for any
        // invalid token: expired, bad signature, malformed, etc.
        // JwtFilter is responsible for catching these.
        return Jwts
                .parserBuilder()
                .setSigningKey(signKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}