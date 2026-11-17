package com.pragma.powerup.infrastructure.out.security;

import com.pragma.powerup.domain.model.RoleModel;
import com.pragma.powerup.domain.model.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtAdapterTest {
    private static final String SECRET = Base64.getEncoder()
            .encodeToString("unit-test-secret-key-with-more-than-32-bytes".getBytes(StandardCharsets.UTF_8));
    private static final String OTHER_SECRET = Base64.getEncoder()
            .encodeToString("another-unit-test-secret-key-with-32-bytes+".getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION_MINUTES = 60;

    private UserModel buildUser() {
        RoleModel role = new RoleModel();
        role.setName("OWNER");
        UserModel user = new UserModel();
        user.setId(5L);
        user.setEmail("ana.gomez@correo.com");
        user.setRole(role);
        return user;
    }

    @Test
    void generateTokenContainsTheEmailTheUserIdAndTheRole() {
        JwtAdapter jwtAdapter = new JwtAdapter(SECRET, EXPIRATION_MINUTES);

        Claims claims = jwtAdapter.parseClaims(jwtAdapter.generateToken(buildUser()));

        assertEquals("ana.gomez@correo.com", claims.getSubject());
        assertEquals(5L, ((Number) claims.get(JwtAdapter.USER_ID_CLAIM)).longValue());
        assertEquals("OWNER", claims.get(JwtAdapter.ROLE_CLAIM, String.class));
    }

    @Test
    void parseClaimsWithATokenSignedWithAnotherSecretThrowsJwtException() {
        String foreignToken = new JwtAdapter(OTHER_SECRET, EXPIRATION_MINUTES).generateToken(buildUser());
        JwtAdapter jwtAdapter = new JwtAdapter(SECRET, EXPIRATION_MINUTES);

        assertThrows(JwtException.class, () -> jwtAdapter.parseClaims(foreignToken));
    }

    @Test
    void parseClaimsWithAnExpiredTokenThrowsJwtException() {
        JwtAdapter jwtAdapter = new JwtAdapter(SECRET, -1);
        String expiredToken = jwtAdapter.generateToken(buildUser());

        assertThrows(JwtException.class, () -> jwtAdapter.parseClaims(expiredToken));
    }

    @Test
    void parseClaimsWithAMalformedTokenThrowsJwtException() {
        JwtAdapter jwtAdapter = new JwtAdapter(SECRET, EXPIRATION_MINUTES);

        assertThrows(JwtException.class, () -> jwtAdapter.parseClaims("not-a-token"));
    }
}
