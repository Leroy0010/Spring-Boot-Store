package com.leroy.store.services;

import com.leroy.store.config.JwtConfig;
import com.leroy.store.entities.Role;
import com.leroy.store.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtConfig jwtConfig;


    public String generateAccessToken(User user) {
        var userClaims = Map.of("email", user.getEmail(), "name", user.getName(), "role", user.getRole().name());
        return generateToken(user.getId(), userClaims, jwtConfig.getTokenExpiration());
    }

    public String generateRefreshToken(User user) {
        var userClaims = Map.of("email", user.getEmail(), "name", user.getName(), "role", user.getRole().name());
        return generateToken(user.getId(), userClaims, jwtConfig.getRefreshTokenExpiration());
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean validateToken(String token){
        try {
            var claims = getClaims(token);

            return claims.getExpiration().after(new Date());
        } catch (JwtException e) {
            return false;
        }
    }


    public UUID getUserIdFromToken(String token) {
        return UUID.fromString(getClaims(token).getSubject());
    }

    public Role getRoleFromToken(String token) {
        return Role.valueOf(getClaims(token).get("role", String.class));
    }

    private String generateToken(UUID userId, Map<String, String> userClaims, long tokenExpiration) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
                .signWith(jwtConfig.getSecretKey())
                .claims(userClaims)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
