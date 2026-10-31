package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.UserRequestDto;
import com.pragma.powerup.application.dto.response.UserResponseDto;
import com.pragma.powerup.application.mapper.IUserRequestMapper;
import com.pragma.powerup.application.mapper.IUserResponseMapper;
import com.pragma.powerup.domain.api.IUserServicePort;
import com.pragma.powerup.domain.exception.UserAlreadyExistsException;
import com.pragma.powerup.domain.model.UserModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHandlerTest {

    @Mock
    private IUserServicePort userServicePort;

    @Mock
    private IUserRequestMapper userRequestMapper;

    @Mock
    private IUserResponseMapper userResponseMapper;

    @InjectMocks
    private UserHandler userHandler;


    @Test
    void saveOwnerMapsRequestCallsServicePortAndReturnsMappedResponse() {
        UserRequestDto request = new UserRequestDto();
        UserModel modelToSave = new UserModel();
        UserModel savedModel = new UserModel();
        UserResponseDto expectedResponse = new UserResponseDto();
        when(userRequestMapper.toUser(request)).thenReturn(modelToSave);
        when(userServicePort.saveOwner(modelToSave)).thenReturn(savedModel);
        when(userResponseMapper.toResponse(savedModel)).thenReturn(expectedResponse);

        UserResponseDto result = userHandler.saveOwner(request);

        assertSame(expectedResponse, result);
        verify(userServicePort).saveOwner(modelToSave);
    }

    @Test
    void saveOwnerWhenServicePortThrowsPropagatesExceptionWithoutMappingResponse() {
        UserRequestDto request = new UserRequestDto();
        UserModel modelToSave = new UserModel();
        when(userRequestMapper.toUser(request)).thenReturn(modelToSave);
        when(userServicePort.saveOwner(modelToSave))
                .thenThrow(new UserAlreadyExistsException("A user with this email already exists"));

        assertThrows(UserAlreadyExistsException.class, () -> userHandler.saveOwner(request));
        verifyNoInteractions(userResponseMapper);
    }













}