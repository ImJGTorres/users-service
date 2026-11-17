package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.LoginRequestDto;
import com.pragma.powerup.application.dto.response.LoginResponseDto;
import com.pragma.powerup.domain.api.IAuthServicePort;
import com.pragma.powerup.domain.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthHandlerTest {
    private static final String EMAIL = "ana.gomez@correo.com";
    private static final String PASSWORD = "Clave123";

    @Mock
    private IAuthServicePort authServicePort;

    @InjectMocks
    private AuthHandler authHandler;

    private LoginRequestDto buildRequest() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        return request;
    }

    @Test
    void loginPassesTheCredentialsToTheServicePortAndReturnsTheTokenInTheResponse() {
        when(authServicePort.login(EMAIL, PASSWORD)).thenReturn("token");

        LoginResponseDto result = authHandler.login(buildRequest());

        assertEquals("token", result.getToken());
    }

    @Test
    void loginWhenServicePortRejectsTheCredentialsPropagatesTheException() {
        when(authServicePort.login(EMAIL, PASSWORD))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        assertThrows(InvalidCredentialsException.class, () -> authHandler.login(buildRequest()));
    }
}
