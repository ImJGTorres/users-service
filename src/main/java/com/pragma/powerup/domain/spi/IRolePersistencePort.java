package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.RoleModel;
import com.pragma.powerup.domain.model.RoleName;

public interface IRolePersistencePort {
    RoleModel getRoleByName(RoleName roleName);
}
