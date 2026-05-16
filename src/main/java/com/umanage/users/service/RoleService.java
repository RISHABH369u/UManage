package com.umanage.users.service;

import com.umanage.common.exception.ConflictException;
import com.umanage.common.exception.ResourceNotFoundException;
import com.umanage.users.dto.CreateRoleRequest;
import com.umanage.users.entity.Permission;
import com.umanage.users.entity.Role;
import com.umanage.users.repository.PermissionRepository;
import com.umanage.users.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public Role createRole(CreateRoleRequest request) {
        roleRepository.findByNameIgnoreCase(request.name()).ifPresent(r -> {
            throw new ConflictException("Role already exists");
        });

        var role = new Role();
        role.setName(request.name().trim().toUpperCase());
        role.setDescription(request.description());

        if (request.permissionNames() != null && !request.permissionNames().isEmpty()) {
            var permissions = new HashSet<Permission>();
            for (String permissionName : request.permissionNames()) {
                Permission permission = permissionRepository.findByNameIgnoreCase(permissionName.trim())
                        .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionName.trim()));
                permissions.add(permission);
            }
            role.setPermissions(permissions);
        }

        return roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public List<Role> listRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Role getByName(String name) {
        return roleRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name));
    }
}
