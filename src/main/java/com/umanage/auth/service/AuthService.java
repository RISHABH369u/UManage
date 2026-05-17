package com.umanage.auth.service;

import com.umanage.audit.service.AuditLogService;
import com.umanage.auth.dto.LoginRequest;
import com.umanage.auth.dto.RegisterRequest;
import com.umanage.auth.dto.RefreshRequest;
import com.umanage.auth.dto.TokenResponse;
import com.umanage.auth.entity.RefreshToken;
import com.umanage.auth.repository.RefreshTokenRepository;
import com.umanage.common.exception.ConflictException;
import com.umanage.common.exception.ResourceNotFoundException;
import com.umanage.common.exception.UnauthorizedException;
import com.umanage.security.AuthUserPrincipal;
import com.umanage.security.JwtProperties;
import com.umanage.security.JwtService;
import com.umanage.users.entity.User;
import com.umanage.users.repository.RoleRepository;
import com.umanage.users.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.StreamSupport;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Value("${app.auth.registration.default-role:USER}")
    private String defaultRegistrationRole;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       JwtProperties jwtProperties,
                       RefreshTokenRepository refreshTokenRepository,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuditLogService auditLogService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public User register(RegisterRequest request, HttpServletRequest httpRequest) {
        var normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email already exists");
        }

        var defaultRole = roleRepository.findByNameIgnoreCase(defaultRegistrationRole)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found: " + defaultRegistrationRole));

        var user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRoles(Set.of(defaultRole));

        var saved = userRepository.save(user);
        auditLogService.log(saved.getId(), "REGISTER", "User", saved.getId().toString(), "User self-registered", httpRequest.getRemoteAddr());
        return saved;
    }

    @Transactional
    public TokenResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var principal = (AuthUserPrincipal) authentication.getPrincipal();
        var user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        var scope = buildScopeFromAuthorities(principal.getAuthorities());

        var accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), scope);
        var refreshTokenValue = jwtService.generateRefreshToken(user.getId());

        var refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(OffsetDateTime.now().plusDays(jwtProperties.refreshTokenExpiryDays()));
        refreshTokenRepository.save(refreshToken);

        auditLogService.log(user.getId(), "LOGIN", "User", user.getId().toString(), "User logged in", httpRequest.getRemoteAddr());
        return new TokenResponse(accessToken, refreshTokenValue, "Bearer", jwtProperties.accessTokenExpiryMinutes() * 60);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request, HttpServletRequest httpRequest) {
        var dbToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (dbToken.isRevoked() || dbToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        try {
            var claims = jwtService.extractClaims(request.refreshToken());
            if (!"refresh".equals(claims.get("typ", String.class))) {
                throw new UnauthorizedException("Invalid token type");
            }
        } catch (JwtException e) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        dbToken.setRevoked(true);
        refreshTokenRepository.save(dbToken);

        var user = dbToken.getUser();
        var scope = buildScopeFromAuthorities(
                user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(permission -> (GrantedAuthority) () -> permission.getName().toUpperCase())
                        .toList()
        );

        var accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), scope);
        var refreshTokenValue = jwtService.generateRefreshToken(user.getId());

        var newToken = new RefreshToken();
        newToken.setToken(refreshTokenValue);
        newToken.setUser(user);
        newToken.setExpiresAt(OffsetDateTime.now().plusDays(jwtProperties.refreshTokenExpiryDays()));
        refreshTokenRepository.save(newToken);

        auditLogService.log(user.getId(), "REFRESH_TOKEN", "User", user.getId().toString(), "Refreshed access token", httpRequest.getRemoteAddr());
        return new TokenResponse(accessToken, refreshTokenValue, "Bearer", jwtProperties.accessTokenExpiryMinutes() * 60);
    }

    private String buildScopeFromAuthorities(Iterable<? extends GrantedAuthority> authorities) {
        return StreamSupport.stream(authorities.spliterator(), false)
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority != null && !authority.isBlank())
                .distinct()
                .sorted()
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }
}
