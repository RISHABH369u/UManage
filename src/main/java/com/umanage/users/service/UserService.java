package com.umanage.users.service;

import com.umanage.audit.service.AuditLogService;
import com.umanage.common.exception.ConflictException;
import com.umanage.common.exception.ResourceNotFoundException;
import com.umanage.users.dto.CreateUserRequest;
import com.umanage.users.entity.User;
import com.umanage.security.AuthUserPrincipal;
import com.umanage.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository,
                       RoleService roleService,
                       PasswordEncoder passwordEncoder,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public User createUser(CreateUserRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already exists");
        }

        var user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRoles(request.roles().stream().map(roleService::getByName).collect(Collectors.toSet()));

        var saved = userRepository.save(user);
        auditLogService.log(
                getActorUserId(),
                "CREATE_USER",
                "User",
                saved.getId().toString(),
                "Created user " + saved.getEmail(),
                httpRequest.getRemoteAddr()
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<User> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UUID getActorUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUserPrincipal principal) {
            return principal.getUserId();
        }
        return null;
    }
}
