package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IUserServicePort;
import com.pragma.powerup.domain.exception.UnderageUserException;
import com.pragma.powerup.domain.exception.UserAlreadyExistsException;
import com.pragma.powerup.domain.model.RoleName;
import com.pragma.powerup.domain.model.UserModel;
import com.pragma.powerup.domain.spi.IPasswordEncoderPort;
import com.pragma.powerup.domain.spi.IRolePersistencePort;
import com.pragma.powerup.domain.spi.IUserPersistencePort;

import java.time.LocalDate;
import java.time.Period;

public class UserUseCase implements IUserServicePort {
    private static final int LEGAL_AGE = 18;
    private final IUserPersistencePort userPersistencePort;
    private final IRolePersistencePort rolePersistencePort;
    private final IPasswordEncoderPort passwordEncoderPort;


    public UserUseCase(IUserPersistencePort userPersistencePort,
                       IRolePersistencePort rolePersistencePort,
                       IPasswordEncoderPort passwordEncoderPort) {
        this.userPersistencePort = userPersistencePort;
        this.rolePersistencePort = rolePersistencePort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public UserModel saveOwner(UserModel userModel) {
        validateLegalAge(userModel.getBirthDate());
        validateUniqueness(userModel);
        userModel.setRole(rolePersistencePort.getRoleByName(RoleName.OWNER));
        userModel.setPassword(passwordEncoderPort.encode(userModel.getPassword()));
        return userPersistencePort.saveUser(userModel);
    }

    private void validateLegalAge(LocalDate birthDate) {
        if (Period.between(birthDate, LocalDate.now()).getYears() < LEGAL_AGE) {
            throw new UnderageUserException();
        }
    }

    //se valida unicidad para que no existan dos propietarios iguales
    private void validateUniqueness(UserModel userModel) {
        if (userPersistencePort.existsByEmail(userModel.getEmail())) {
            throw new UserAlreadyExistsException("A user with this email already exists");
        }
        if (userPersistencePort.existsByIdentityDocument(userModel.getIdentityDocument())) {
            throw new UserAlreadyExistsException("A user with this identity document already exists");
        }
    }
}