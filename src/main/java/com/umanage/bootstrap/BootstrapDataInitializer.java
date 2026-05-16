package com.umanage.bootstrap;

import com.umanage.users.entity.Permission;
import com.umanage.users.entity.Role;
import com.umanage.users.entity.User;
import com.umanage.users.repository.PermissionRepository;
import com.umanage.users.repository.RoleRepository;
import com.umanage.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class BootstrapDataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.email:admin@umanage.local}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:Admin@123}")
    private String adminPassword;

    public BootstrapDataInitializer(PermissionRepository permissionRepository,
                                    RoleRepository roleRepository,
                                    UserRepository userRepository,
                                    PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        var userManage = createPermissionIfMissing("USER_MANAGE");
        var userView = createPermissionIfMissing("USER_VIEW");
        var roleManage = createPermissionIfMissing("ROLE_MANAGE");
        var roleView = createPermissionIfMissing("ROLE_VIEW");

        var adminRole = roleRepository.findByNameIgnoreCase("ADMIN").orElseGet(() -> {
            var role = new Role();
            role.setName("ADMIN");
            role.setDescription("System administrator role");
            role.setPermissions(Set.of(userManage, userView, roleManage, roleView));
            return roleRepository.save(role);
        });

        if (!userRepository.existsByEmailIgnoreCase(adminEmail)) {
            var admin = new User();
            admin.setEmail(adminEmail.toLowerCase());
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
        }
    }

    private Permission createPermissionIfMissing(String name) {
        return permissionRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            var permission = new Permission();
            permission.setName(name);
            permission.setDescription("System permission " + name);
            return permissionRepository.save(permission);
        });
    }
}
