package com.umanage.users.controller;

import com.umanage.common.api.ApiResponse;
import com.umanage.users.dto.CreateUserRequest;
import com.umanage.users.dto.UpdateProfileRequest;
import com.umanage.users.dto.UserResponse;
import com.umanage.users.mapper.UserMapper;
import com.umanage.users.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request,
                                                            HttpServletRequest httpRequest) {
        var created = userService.createUser(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(userMapper.toUserResponse(created)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        var sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        var result = userService.listUsers(PageRequest.of(page, size, Sort.by(sortDirection, sortBy)));

        var response = Map.<String, Object>of(
                "content", result.getContent().stream().map(userMapper::toUserResponse).toList(),
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages()
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(userMapper.toUserResponse(userService.getUser(id))));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PROFILE_VIEW')")
    public ResponseEntity<ApiResponse<UserResponse>> myProfile() {
        return ResponseEntity.ok(ApiResponse.ok(userMapper.toUserResponse(userService.getMyProfile())));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('PROFILE_MANAGE')")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request,
                                                                     HttpServletRequest httpRequest) {
        return ResponseEntity.ok(ApiResponse.ok(userMapper.toUserResponse(userService.updateMyProfile(request, httpRequest))));
    }
}
