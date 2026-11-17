package com.pragma.powerup.infrastructure.security;

import com.pragma.powerup.domain.model.RoleName;
import com.pragma.powerup.infrastructure.out.security.JwtAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfiguration {
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String OWNERS_URL = "/api/v1/owners";
    private static final String ERROR_URL = "/error";
    private static final String[] SWAGGER_URLS = {"/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAdapter jwtAdapter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.POST, LOGIN_URL).permitAll()
                        .requestMatchers(SWAGGER_URLS).permitAll()
                        .requestMatchers(ERROR_URL).permitAll()
                        .requestMatchers(HttpMethod.POST, OWNERS_URL).hasRole(RoleName.ADMIN.name())
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtAdapter), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
