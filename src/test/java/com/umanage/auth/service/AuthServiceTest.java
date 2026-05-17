package com.umanage.auth.service;

import com.umanage.audit.service.AuditLogService;
import com.umanage.auth.dto.LoginRequest;
import com.umanage.auth.dto.RegisterRequest;
import com.umanage.auth.entity.RefreshToken;
import com.umanage.auth.repository.RefreshTokenRepository;
import com.umanage.security.AuthUserPrincipal;
import com.umanage.security.JwtProperties;
import com.umanage.security.JwtService;
import com.umanage.users.entity.Role;
import com.umanage.users.entity.User;
import com.umanage.users.repository.RoleRepository;
import com.umanage.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditLogService auditLogService;

    private JwtProperties jwtProperties;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties("umanage", "dGVzdC1zZWNyZXQtd2l0aC1lbm91Z2gtbGVuZ3RoLWRhdGE=", 30, 7);
        authService = new AuthService(authenticationManager, jwtService, jwtProperties, refreshTokenRepository, userRepository, roleRepository, passwordEncoder, auditLogService);
        ReflectionTestUtils.setField(authService, "defaultRegistrationRole", "USER");
    }

    @Test
    void loginShouldIssueAccessAndRefreshTokens() {
        UUID userId = UUID.randomUUID();
        var principal = new AuthUserPrincipal(userId, "admin@u.com", "hash", true, Set.of());
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        var user = new User();
        user.setId(userId);
        user.setEmail("admin@u.com");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmailIgnoreCase("admin@u.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(eq(userId), eq("admin@u.com"), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(userId)).thenReturn("refresh-token");

        var request = new LoginRequest("admin@u.com", "Password@123");
        var servletRequest = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        var response = authService.login(request, servletRequest);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getToken()).isEqualTo("refresh-token");

        verify(auditLogService).log(eq(userId), eq("LOGIN"), eq("User"), eq(userId.toString()), any(), eq("127.0.0.1"));
    }

    @Test
    void registerShouldPersistEncodedPasswordWithDefaultRole() {
        var userRole = new Role();
        userRole.setName("USER");
        when(userRepository.existsByEmailIgnoreCase("student@u.com")).thenReturn(false);
        when(roleRepository.findByNameIgnoreCase("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Password@123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            var user = invocation.getArgument(0, User.class);
            user.setId(UUID.randomUUID());
            return user;
        });

        var servletRequest = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        var saved = authService.register(new RegisterRequest("Student@U.com", "Password@123", "John", "Doe"), servletRequest);

        assertThat(saved.getEmail()).isEqualTo("student@u.com");
        assertThat(saved.getRoles()).containsExactly(userRole);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded");
        verify(auditLogService).log(eq(saved.getId()), eq("REGISTER"), eq("User"), eq(saved.getId().toString()), any(), eq("127.0.0.1"));
    }
}
