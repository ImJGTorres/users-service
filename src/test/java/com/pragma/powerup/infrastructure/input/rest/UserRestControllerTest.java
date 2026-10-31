package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.UserRequestDto;
import com.pragma.powerup.application.dto.response.UserResponseDto;
import com.pragma.powerup.application.handler.IUserHandler;
import com.pragma.powerup.domain.exception.RoleNotFoundException;
import com.pragma.powerup.domain.exception.UnderageUserException;
import com.pragma.powerup.domain.exception.UserAlreadyExistsException;
import com.pragma.powerup.infrastructure.exceptionhandler.ControllerAdvisor;
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
class UserRestControllerTest {

    private static final String OWNERS_URL = "/api/v1/owners";
    private static final String VALID_DOCUMENT = "1234567890";
    private static final String VALID_PHONE = "+573005698325";
    private static final String VALID_EMAIL = "ana.gomez@correo.com";

    @Mock
    private IUserHandler userHandler;

    @InjectMocks
    private UserRestController userRestController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userRestController)
                .setControllerAdvice(new ControllerAdvisor())
                .build();
    }

    @Test
    void saveOwnerWithValidRequestReturns201AndTheCreatedOwnerWithoutPassword() throws Exception {
        UserResponseDto response = new UserResponseDto();
        response.setId(1L);
        response.setName("Ana");
        response.setLastName("Gomez");
        response.setEmail(VALID_EMAIL);
        response.setRole("OWNER");
        when(userHandler.saveOwner(any(UserRequestDto.class))).thenReturn(response);

        mockMvc.perform(post(OWNERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson(VALID_DOCUMENT, VALID_PHONE, VALID_EMAIL)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value(VALID_EMAIL))
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void saveOwnerWithNonNumericIdentityDocumentReturns400() throws Exception {
        mockMvc.perform(post(OWNERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson("12345ABC", VALID_PHONE, VALID_EMAIL)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.identityDocument").value("must contain only digits"));

        verifyNoInteractions(userHandler);
    }

    @Test
    void saveOwnerWithPhoneLongerThan13CharactersReturns400() throws Exception {
        mockMvc.perform(post(OWNERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson(VALID_DOCUMENT, "+5730056983251", VALID_EMAIL)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phone").exists());

        verifyNoInteractions(userHandler);
    }

    @Test
    void saveOwnerWithMalformedEmailReturns400() throws Exception {
        mockMvc.perform(post(OWNERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson(VALID_DOCUMENT, VALID_PHONE, "not-an-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());

        verifyNoInteractions(userHandler);
    }

    @Test
    void saveOwnerWithEmptyBodyReturns400ReportingEveryRequiredField() throws Exception {
        mockMvc.perform(post(OWNERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.lastName").exists())
                .andExpect(jsonPath("$.identityDocument").exists())
                .andExpect(jsonPath("$.phone").exists())
                .andExpect(jsonPath("$.birthDate").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.password").exists());

        verifyNoInteractions(userHandler);
    }

    @Test
    void saveOwnerWhenUserIsUnderageReturns400WithTheDomainMessage() throws Exception {
        when(userHandler.saveOwner(any(UserRequestDto.class))).thenThrow(new UnderageUserException());

        mockMvc.perform(post(OWNERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson(VALID_DOCUMENT, VALID_PHONE, VALID_EMAIL)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The user must be of legal age"));
    }

    @Test
    void saveOwnerWhenUserAlreadyExistsReturns409WithTheDomainMessage() throws Exception {
        when(userHandler.saveOwner(any(UserRequestDto.class)))
                .thenThrow(new UserAlreadyExistsException("A user with this email already exists"));

        mockMvc.perform(post(OWNERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson(VALID_DOCUMENT, VALID_PHONE, VALID_EMAIL)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A user with this email already exists"));
    }

    @Test
    void saveOwnerWhenOwnerRoleIsMissingReturns404() throws Exception {
        when(userHandler.saveOwner(any(UserRequestDto.class))).thenThrow(new RoleNotFoundException());

        mockMvc.perform(post(OWNERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRequestJson(VALID_DOCUMENT, VALID_PHONE, VALID_EMAIL)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The requested role does not exist"));
    }

    private String buildRequestJson(String identityDocument, String phone, String email) {
        return """
                {
                  "name": "Ana",
                  "lastName": "Gomez",
                  "identityDocument": "%s",
                  "phone": "%s",
                  "birthDate": "1990-05-20",
                  "email": "%s",
                  "password": "Clave12345"
                }
                """.formatted(identityDocument, phone, email);
    }
}
