package com.umanage.users.mapper;

import com.umanage.users.dto.RoleResponse;
import com.umanage.users.dto.UserResponse;
import com.umanage.users.entity.Role;
import com.umanage.users.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );
    }

    public RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getPermissions().stream().map(permission -> permission.getName()).collect(Collectors.toSet())
        );
    }
}
