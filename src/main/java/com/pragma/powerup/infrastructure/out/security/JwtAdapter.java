package com.pragma.powerup.infrastructure.out.security;

import com.pragma.powerup.domain.model.UserModel;
import com.pragma.powerup.domain.spi.ITokenPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtAdapter implements ITokenPort {
    public static final String USER_ID_CLAIM = "userId";
    public static final String ROLE_CLAIM = "role";
    private static final long MILLIS_PER_MINUTE = 60_000L;

    private final SecretKey secretKey;
    private final long expirationMinutes;

    public JwtAdapter(String secret, long expirationMinutes) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public String generateToken(UserModel userModel) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userModel.getEmail())
                .claim(USER_ID_CLAIM, userModel.getId())
                .claim(ROLE_CLAIM, userModel.getRole().getName())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMinutes * MILLIS_PER_MINUTE))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
