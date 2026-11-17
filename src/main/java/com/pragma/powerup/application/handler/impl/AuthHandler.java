package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.LoginRequestDto;
import com.pragma.powerup.application.dto.response.LoginResponseDto;
import com.pragma.powerup.application.handler.IAuthHandler;
import com.pragma.powerup.domain.api.IAuthServicePort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthHandler implements IAuthHandler {
    private final IAuthServicePort authServicePort;

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        String token = authServicePort.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        return new LoginResponseDto(token);
    }
}
