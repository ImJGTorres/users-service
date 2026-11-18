package com.pragma.powerup.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonAuthenticationEntryPointTest {
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint = new JsonAuthenticationEntryPoint();

    @Test
    void commenceAnswers401WithAJsonMessageAndWithoutTheInternalErrorDetails() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        jsonAuthenticationEntryPoint.commence(new MockHttpServletRequest(), response,
                new InsufficientAuthenticationException("internal detail"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals("{\"message\":\"Authentication is required: send a valid token\"}",
                response.getContentAsString());
        assertFalse(response.getContentAsString().contains("internal detail"));
    }
}
