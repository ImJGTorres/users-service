package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.AccountRequestDto;
import com.pragma.powerup.application.dto.request.UserRequestDto;
import com.pragma.powerup.application.dto.response.UserResponseDto;
import com.pragma.powerup.application.dto.response.UserRoleResponseDto;

public interface IUserHandler {
    UserResponseDto saveOwner(UserRequestDto userRequestDto);

    UserResponseDto saveEmployee(AccountRequestDto accountRequestDto);

    UserResponseDto saveClient(AccountRequestDto accountRequestDto);

    UserRoleResponseDto getUserById(Long id);
}