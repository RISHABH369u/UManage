package com.umanage.users.controller;

import com.umanage.common.api.ApiResponse;
import com.umanage.users.dto.CreateRoleRequest;
import com.umanage.users.dto.RoleResponse;
import com.umanage.users.mapper.UserMapper;
import com.umanage.users.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;
    private final UserMapper userMapper;

    public RoleController(RoleService roleService, UserMapper userMapper) {
        this.roleService = roleService;
        this.userMapper = userMapper;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody CreateRoleRequest request) {
        var created = roleService.createRole(request);
        return ResponseEntity.ok(ApiResponse.ok(userMapper.toRoleResponse(created)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> list() {
        var responses = roleService.listRoles().stream().map(userMapper::toRoleResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
