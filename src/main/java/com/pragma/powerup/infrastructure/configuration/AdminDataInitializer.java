package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.model.RoleName;
import com.pragma.powerup.infrastructure.out.jpa.entity.RoleEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.UserEntity;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRoleRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Order(2)
public class AdminDataInitializer implements ApplicationRunner {
    private static final String ADMIN_NAME = "Admin";
    private static final String ADMIN_LAST_NAME = "Plazoleta";
    private static final String ADMIN_IDENTITY_DOCUMENT = "0000000000";
    private static final String ADMIN_PHONE = "+570000000000";
    private static final LocalDate ADMIN_BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String ADMIN_ROLE_MISSING_MESSAGE = "The ADMIN role must exist before seeding the admin user";

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminDataInitializer(IUserRepository userRepository,
                                IRoleRepository roleRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${admin.email}") String adminEmail,
                                @Value("${admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        RoleEntity adminRole = roleRepository.findByName(RoleName.ADMIN.name())
                .orElseThrow(() -> new IllegalStateException(ADMIN_ROLE_MISSING_MESSAGE));
        userRepository.save(new UserEntity(null, ADMIN_NAME, ADMIN_LAST_NAME, ADMIN_IDENTITY_DOCUMENT,
                ADMIN_PHONE, ADMIN_BIRTH_DATE, adminEmail, passwordEncoder.encode(adminPassword), adminRole));
    }
}
