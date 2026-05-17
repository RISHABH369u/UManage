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
import java.util.HashSet;

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
        var profileView = createPermissionIfMissing("PROFILE_VIEW");
        var profileManage = createPermissionIfMissing("PROFILE_MANAGE");

        var defaultUserPermissions = Set.of(profileView, profileManage);

        var adminPermissions = Set.of(userManage, userView, roleManage, roleView, profileView, profileManage);

        var adminRole = roleRepository.findByNameIgnoreCase("ADMIN").orElseGet(() -> {
            var role = new Role();
            role.setName("ADMIN");
            role.setDescription("System administrator role");
            role.setPermissions(new HashSet<>(adminPermissions));
            return roleRepository.save(role);
        });
        mergePermissionsIfMissing(adminRole, adminPermissions);

        var userRole = roleRepository.findByNameIgnoreCase("USER").orElseGet(() -> {
            var role = new Role();
            role.setName("USER");
            role.setDescription("Default user role");
            role.setPermissions(new HashSet<>(defaultUserPermissions));
            return roleRepository.save(role);
        });
        mergePermissionsIfMissing(userRole, defaultUserPermissions);

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

    private void mergePermissionsIfMissing(Role role, Set<Permission> requiredPermissions) {
        var currentPermissions = new HashSet<>(role.getPermissions());
        if (currentPermissions.addAll(requiredPermissions)) {
            role.setPermissions(currentPermissions);
            roleRepository.save(role);
        }
    }
}
