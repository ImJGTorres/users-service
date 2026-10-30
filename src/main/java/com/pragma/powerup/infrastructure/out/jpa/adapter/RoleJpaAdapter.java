package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.exception.RoleNotFoundException;
import com.pragma.powerup.domain.model.RoleModel;
import com.pragma.powerup.domain.model.RoleName;
import com.pragma.powerup.domain.spi.IRolePersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRoleEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRoleRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoleJpaAdapter implements IRolePersistencePort {
    private final IRoleRepository roleRepository;
    private final IRoleEntityMapper roleEntityMapper;

    @Override
    public RoleModel getRoleByName(RoleName roleName) {
        return roleEntityMapper.toModel(roleRepository.findByName(roleName.name())
                .orElseThrow(RoleNotFoundException::new));
    }
}
