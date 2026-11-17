package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.InvalidCredentialsException;
import com.pragma.powerup.domain.model.UserModel;
import com.pragma.powerup.domain.spi.IPasswordEncoderPort;
import com.pragma.powerup.domain.spi.ITokenPort;
import com.pragma.powerup.domain.spi.IUserPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {
    private static final String EMAIL = "ana.gomez@correo.com";
    private static final String RAW_PASSWORD = "Clave123";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded";
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    @Mock
    private IUserPersistencePort userPersistencePort;

    @Mock
    private IPasswordEncoderPort passwordEncoderPort;

    @Mock
    private ITokenPort tokenPort;

    @InjectMocks
    private AuthUseCase authUseCase;

    private UserModel buildUser() {
        UserModel user = new UserModel();
        user.setEmail(EMAIL);
        user.setPassword(ENCODED_PASSWORD);
        return user;
    }

    @Test
    void loginWithValidCredentialsReturnsTheGeneratedToken() {
        UserModel user = buildUser();
        when(userPersistencePort.findUserByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(tokenPort.generateToken(user)).thenReturn("token");

        String result = authUseCase.login(EMAIL, RAW_PASSWORD);

        assertEquals("token", result);
    }

    @Test
    void loginWithUnknownEmailThrowsInvalidCredentialsExceptionWithoutGeneratingToken() {
        when(userPersistencePort.findUserByEmail(EMAIL)).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class, () -> authUseCase.login(EMAIL, RAW_PASSWORD));

        assertEquals(INVALID_CREDENTIALS_MESSAGE, exception.getMessage());
        verifyNoInteractions(passwordEncoderPort, tokenPort);
    }

    @Test
    void loginWithWrongPasswordThrowsTheSameExceptionAndMessageWithoutGeneratingToken() {
        when(userPersistencePort.findUserByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(passwordEncoderPort.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class, () -> authUseCase.login(EMAIL, RAW_PASSWORD));

        assertEquals(INVALID_CREDENTIALS_MESSAGE, exception.getMessage());
        verifyNoInteractions(tokenPort);
    }
}
