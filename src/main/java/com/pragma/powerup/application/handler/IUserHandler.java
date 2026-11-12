package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.UserRequestDto;
import com.pragma.powerup.application.dto.response.UserResponseDto;
import com.pragma.powerup.application.dto.response.UserRoleResponseDto;

public interface IUserHandler {
    UserResponseDto saveOwner(UserRequestDto userRequestDto);

    UserRoleResponseDto getUserById(Long id);
}