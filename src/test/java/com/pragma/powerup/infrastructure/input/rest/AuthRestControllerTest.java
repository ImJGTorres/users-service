package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.LoginRequestDto;
import com.pragma.powerup.application.dto.response.LoginResponseDto;
import com.pragma.powerup.application.handler.IAuthHandler;
import com.pragma.powerup.domain.exception.InvalidCredentialsException;
import com.pragma.powerup.infrastructure.exceptionhandler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthRestControllerTest {
    private static final String LOGIN_URL = "/api/v1/auth/login";

    @Mock
    private IAuthHandler authHandler;

    @InjectMocks
    private AuthRestController authRestController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authRestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginWithValidCredentialsReturns200AndTheToken() throws Exception {
        when(authHandler.login(any(LoginRequestDto.class))).thenReturn(new LoginResponseDto("token"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson("ana.gomez@correo.com", "Clave123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void loginWithMalformedEmailReturns400() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson("not-an-email", "Clave123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());

        verifyNoInteractions(authHandler);
    }

    @Test
    void loginWithEmptyBodyReturns400ReportingEveryRequiredField() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.password").exists());

        verifyNoInteractions(authHandler);
    }

    @Test
    void loginWithWrongCredentialsReturns401WithTheDomainMessage() throws Exception {
        when(authHandler.login(any(LoginRequestDto.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson("ana.gomez@correo.com", "wrong")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    private String buildRequestJson(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }
}
