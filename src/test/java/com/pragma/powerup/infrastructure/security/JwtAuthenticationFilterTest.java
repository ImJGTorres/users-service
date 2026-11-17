package com.pragma.powerup.infrastructure.security;

import com.pragma.powerup.domain.model.RoleModel;
import com.pragma.powerup.domain.model.UserModel;
import com.pragma.powerup.infrastructure.out.security.JwtAdapter;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationFilterTest {
    private static final String SECRET = Base64.getEncoder()
            .encodeToString("unit-test-secret-key-with-more-than-32-bytes".getBytes(StandardCharsets.UTF_8));
    private static final String OTHER_SECRET = Base64.getEncoder()
            .encodeToString("another-unit-test-secret-key-with-32-bytes+".getBytes(StandardCharsets.UTF_8));
    private static final String AUTHORIZATION = "Authorization";

    private JwtAdapter jwtAdapter;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtAdapter = new JwtAdapter(SECRET, 60);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtAdapter);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private String buildTokenWith(JwtAdapter adapter) {
        RoleModel role = new RoleModel();
        role.setName("OWNER");
        UserModel user = new UserModel();
        user.setId(5L);
        user.setEmail("ana.gomez@correo.com");
        user.setRole(role);
        return adapter.generateToken(user);
    }

    @Test
    void doFilterWithAValidTokenAuthenticatesWithTheUserIdAndTheRoleAuthority()
            throws ServletException, IOException {
        request.addHeader(AUTHORIZATION, "Bearer " + buildTokenWith(jwtAdapter));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(5L, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_OWNER".equals(authority.getAuthority())));
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterWithoutAuthorizationHeaderLeavesTheRequestUnauthenticatedAndContinues()
            throws ServletException, IOException {
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterWithAnInvalidTokenLeavesTheRequestUnauthenticatedAndContinues()
            throws ServletException, IOException {
        request.addHeader(AUTHORIZATION, "Bearer not-a-token");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterWithATokenSignedWithAnotherSecretLeavesTheRequestUnauthenticatedAndContinues()
            throws ServletException, IOException {
        request.addHeader(AUTHORIZATION, "Bearer " + buildTokenWith(new JwtAdapter(OTHER_SECRET, 60)));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterWithAnEmptyBearerTokenLeavesTheRequestUnauthenticatedAndContinues()
            throws ServletException, IOException {
        request.addHeader(AUTHORIZATION, "Bearer ");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterWithANonBearerSchemeIgnoresTheHeaderAndContinues() throws ServletException, IOException {
        request.addHeader(AUTHORIZATION, "Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }
}
