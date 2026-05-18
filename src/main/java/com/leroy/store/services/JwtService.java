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


    public Jwt generateAccessToken(User user) {
        var userClaims = Map.of("email", user.getEmail(), "name", user.getName(), "role", user.getRole().name());
        return generateToken(user.getId(), userClaims, jwtConfig.getTokenExpiration());
    }

    public Jwt generateRefreshToken(User user) {
        var userClaims = Map.of("email", user.getEmail(), "name", user.getName(), "role", user.getRole().name());
        return generateToken(user.getId(), userClaims, jwtConfig.getRefreshTokenExpiration());
    }

    public Jwt parseToken(String token) {
        try {
            var claims = getClaims(token);
            return new Jwt(claims, jwtConfig.getSecretKey());
        } catch (JwtException e) {
            return null;
        }
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




    private Jwt generateToken(UUID userId, Map<String, String> userClaims, long tokenExpiration) {
        var claims = Jwts.claims()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
                .add(userClaims)
                .build();
        return new Jwt(claims, jwtConfig.getSecretKey());

    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
