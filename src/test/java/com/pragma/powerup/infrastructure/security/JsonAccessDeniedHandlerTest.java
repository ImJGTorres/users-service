package com.pragma.powerup.infrastructure.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonAccessDeniedHandlerTest {
    private final JsonAccessDeniedHandler jsonAccessDeniedHandler = new JsonAccessDeniedHandler();

    @Test
    void handleAnswers403WithAJsonMessageAndWithoutTheInternalErrorDetails()
            throws IOException, ServletException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        jsonAccessDeniedHandler.handle(new MockHttpServletRequest(), response,
                new AccessDeniedException("internal detail"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals("{\"message\":\"You do not have permission to perform this action\"}",
                response.getContentAsString());
        assertFalse(response.getContentAsString().contains("internal detail"));
    }
}
