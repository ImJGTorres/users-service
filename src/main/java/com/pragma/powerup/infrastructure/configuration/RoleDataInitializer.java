package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.model.RoleName;
import com.pragma.powerup.infrastructure.out.jpa.entity.RoleEntity;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleDataInitializer implements ApplicationRunner {
    private final IRoleRepository roleRepository;
    @Override
    public void run(ApplicationArguments args) {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName.name()).isEmpty()) {
                roleRepository.save(new RoleEntity(null, roleName.name(),
                        "Role " + roleName.name()));
            }
        }
    }
}
