package com.pragma.powerup.infrastructure.security;

import com.pragma.powerup.application.dto.response.UserRoleResponseDto;
import com.pragma.powerup.application.handler.IAuthHandler;
import com.pragma.powerup.application.handler.IUserHandler;
import com.pragma.powerup.domain.model.RoleModel;
import com.pragma.powerup.domain.model.UserModel;
import com.pragma.powerup.infrastructure.input.rest.AuthRestController;
import com.pragma.powerup.infrastructure.input.rest.UserRestController;
import com.pragma.powerup.infrastructure.out.security.JwtAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserRestController.class, AuthRestController.class})
@Import({SecurityConfiguration.class, SecurityConfigurationTest.JwtTestConfiguration.class})
class SecurityConfigurationTest {
    private static final String OWNERS_URL = "/api/v1/owners";
    private static final String USER_URL = "/api/v1/users/1";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String AUTHORIZATION = "Authorization";

    @TestConfiguration
    static class JwtTestConfiguration {
        @Bean
        JwtAdapter jwtAdapter() {
            String secret = Base64.getEncoder().encodeToString(
                    "unit-test-secret-key-with-more-than-32-bytes".getBytes(StandardCharsets.UTF_8));
            return new JwtAdapter(secret, 60);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtAdapter jwtAdapter;

    @MockitoBean
    private IUserHandler userHandler;

    @MockitoBean
    private IAuthHandler authHandler;

    private String bearerTokenFor(String roleName) {
        RoleModel role = new RoleModel();
        role.setName(roleName);
        UserModel user = new UserModel();
        user.setId(1L);
        user.setEmail("user@correo.com");
        user.setRole(role);
        return "Bearer " + jwtAdapter.generateToken(user);
    }

    @Test
    void postOwnersWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post(OWNERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postOwnersWithOwnerTokenReturns403() throws Exception {
        mockMvc.perform(post(OWNERS_URL)
                        .header(AUTHORIZATION, bearerTokenFor("OWNER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postOwnersWithAdminTokenPassesSecurityAndReachesTheValidation() throws Exception {
        mockMvc.perform(post(OWNERS_URL)
                        .header(AUTHORIZATION, bearerTokenFor("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserByIdWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get(USER_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserByIdWithAnyValidTokenReturns200() throws Exception {
        UserRoleResponseDto response = new UserRoleResponseDto();
        response.setId(1L);
        response.setRole("OWNER");
        when(userHandler.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get(USER_URL).header(AUTHORIZATION, bearerTokenFor("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    void getUserByIdWithAnInvalidTokenReturns401() throws Exception {
        mockMvc.perform(get(USER_URL).header(AUTHORIZATION, "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginIsPublicAndReachesTheValidationWithoutToken() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
