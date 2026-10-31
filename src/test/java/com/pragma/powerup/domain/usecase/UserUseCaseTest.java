package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.RoleNotFoundException;
import com.pragma.powerup.domain.exception.UnderageUserException;
import com.pragma.powerup.domain.exception.UserAlreadyExistsException;
import com.pragma.powerup.domain.model.RoleModel;
import com.pragma.powerup.domain.model.RoleName;
import com.pragma.powerup.domain.model.UserModel;
import com.pragma.powerup.domain.spi.IPasswordEncoderPort;
import com.pragma.powerup.domain.spi.IRolePersistencePort;
import com.pragma.powerup.domain.spi.IUserPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {
    private static final String RAW_PASSWORD = "Clave12345";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPassword";
    private static final int LEGAL_AGE = 18;

    @Mock
    private IUserPersistencePort  userPersistencePort;

    @Mock
    private IRolePersistencePort rolePersistencePort;

    @Mock
    private IPasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private UserUseCase  userUseCase;

    private UserModel buildUserBornOn(LocalDate birthDate) {
        UserModel user = new UserModel();
        user.setName("Alejandro");
        user.setLastName("Ovallos");
        user.setIdentityDocument("1234567890");
        user.setPhone("+573209090908");
        user.setBirthDate(birthDate);
        user.setEmail("alejo@correo.com");
        user.setPassword(RAW_PASSWORD);
        return user;
    }


    @Test
    void saveOwnerWithValidDataAssignsOwnerRoleAndEncodesPassword() {
        UserModel user = buildUserBornOn(LocalDate.now().minusYears(30));
        RoleModel ownerRole = new RoleModel(1L, "OWNER", "Role OWNER");
        when(userPersistencePort.existsByEmail(user.getEmail())).thenReturn(false);
        when(userPersistencePort.existsByIdentityDocument(user.getIdentityDocument())).thenReturn(false);
        when(rolePersistencePort.getRoleByName(RoleName.OWNER)).thenReturn(ownerRole);
        when(passwordEncoderPort.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userPersistencePort.saveUser(user)).thenReturn(user);

        UserModel result = userUseCase.saveOwner(user);

        assertAll(
                () -> assertSame(ownerRole, result.getRole()),
                () -> assertEquals(ENCODED_PASSWORD, result.getPassword())
        );
        verify(userPersistencePort).saveUser(user);
    }

    @Test
    void saveOwnerTurningExactlyLegalAgeIsAccepted() {
        UserModel user = buildUserBornOn(LocalDate.now().minusYears(LEGAL_AGE));
        when(rolePersistencePort.getRoleByName(RoleName.OWNER))
                .thenReturn(new RoleModel(1L, "OWNER", "Role OWNER"));
        when(passwordEncoderPort.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userPersistencePort.saveUser(user)).thenReturn(user);

        UserModel result = userUseCase.saveOwner(user);

        assertSame(user, result);
        verify(userPersistencePort).saveUser(user);
    }

    @Test
    void saveOwnerOneDayBeforeLegalAgeThrowsUnderageUserException() {
        UserModel user = buildUserBornOn(LocalDate.now().minusYears(LEGAL_AGE).plusDays(1));

        assertThrows(UnderageUserException.class, () -> userUseCase.saveOwner(user));
        verify(userPersistencePort, never()).saveUser(any());
        verifyNoInteractions(rolePersistencePort, passwordEncoderPort);
    }

    @Test
    void saveOwnerWithExistingEmailThrowsUserAlreadyExistsException() {
        UserModel user = buildUserBornOn(LocalDate.now().minusYears(30));
        when(userPersistencePort.existsByEmail(user.getEmail())).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class, () -> userUseCase.saveOwner(user));

        assertEquals("A user with this email already exists", exception.getMessage());
        verify(userPersistencePort, never()).saveUser(any());
        verifyNoInteractions(rolePersistencePort, passwordEncoderPort);
    }

    @Test
    void saveOwnerWithExistingIdentityDocumentThrowsUserAlreadyExistsException() {
        UserModel user = buildUserBornOn(LocalDate.now().minusYears(30));
        when(userPersistencePort.existsByEmail(user.getEmail())).thenReturn(false);
        when(userPersistencePort.existsByIdentityDocument(user.getIdentityDocument())).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class, () -> userUseCase.saveOwner(user));

        assertEquals("A user with this identity document already exists", exception.getMessage());
        verify(userPersistencePort, never()).saveUser(any());
        verifyNoInteractions(rolePersistencePort, passwordEncoderPort);
    }

    @Test
    void saveOwnerWhenOwnerRoleDoesNotExistThrowsRoleNotFoundException() {
        UserModel user = buildUserBornOn(LocalDate.now().minusYears(30));
        when(rolePersistencePort.getRoleByName(RoleName.OWNER)).thenThrow(new RoleNotFoundException());

        assertThrows(RoleNotFoundException.class, () -> userUseCase.saveOwner(user));

        verify(userPersistencePort, never()).saveUser(any());
        verifyNoInteractions(passwordEncoderPort);
    }
















}