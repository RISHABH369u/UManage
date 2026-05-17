package com.umanage.users.service;

import com.umanage.audit.service.AuditLogService;
import com.umanage.common.exception.ConflictException;
import com.umanage.users.dto.CreateUserRequest;
import com.umanage.users.dto.UpdateProfileRequest;
import com.umanage.users.entity.User;
import com.umanage.users.entity.Role;
import com.umanage.security.AuthUserPrincipal;
import com.umanage.users.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleService roleService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditLogService auditLogService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleService, passwordEncoder, auditLogService);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUserShouldFailWhenEmailExists() {
        when(userRepository.existsByEmailIgnoreCase("admin@u.com")).thenReturn(true);
        var request = new CreateUserRequest("admin@u.com", "Password@123", "System", "Admin", Set.of("ADMIN"));
        var servletRequest = mock(jakarta.servlet.http.HttpServletRequest.class);

        assertThatThrownBy(() -> userService.createUser(request, servletRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void createUserShouldSaveEncodedPasswordAndRoles() {
        when(userRepository.existsByEmailIgnoreCase("student@u.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encoded");

        var role = new Role();
        role.setName("ADMIN");
        when(roleService.getByName("ADMIN")).thenReturn(role);

        var request = new CreateUserRequest("student@u.com", "Password@123", "John", "Doe", Set.of("ADMIN"));
        var servletRequest = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(userRepository.save(any(com.umanage.users.entity.User.class))).thenAnswer(invocation -> {
            var user = invocation.getArgument(0, com.umanage.users.entity.User.class);
            user.setId(UUID.randomUUID());
            return user;
        });

        userService.createUser(request, servletRequest);

        var captor = ArgumentCaptor.forClass(com.umanage.users.entity.User.class);
        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded");
        assertThat(captor.getValue().getRoles()).hasSize(1);
        verify(auditLogService).log(any(), eq("CREATE_USER"), eq("User"), any(), any(), any());
    }

    @Test
    void getMyProfileShouldReturnAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        var principal = new AuthUserPrincipal(userId, "user@u.com", "hash", true, Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        var user = new User();
        user.setId(userId);
        user.setEmail("user@u.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var result = userService.getMyProfile();
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    void updateMyProfileShouldPersistFirstAndLastName() {
        UUID userId = UUID.randomUUID();
        var principal = new AuthUserPrincipal(userId, "user@u.com", "hash", true, Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        var user = new User();
        user.setId(userId);
        user.setFirstName("Old");
        user.setLastName("Name");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0, User.class));

        var servletRequest = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        var updated = userService.updateMyProfile(new UpdateProfileRequest(" New ", " User "), servletRequest);
        assertThat(updated.getFirstName()).isEqualTo("New");
        assertThat(updated.getLastName()).isEqualTo("User");
        verify(auditLogService).log(eq(userId), eq("UPDATE_PROFILE"), eq("User"), eq(userId.toString()), any(), eq("127.0.0.1"));
    }
}
