package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.UserModel;

public interface IUserPersistencePort {
    UserModel saveUser(UserModel userModel);

    boolean existsByEmail(String email);
    boolean existsByIdentityDocument(String identityDocument);
}