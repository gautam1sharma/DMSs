package com.serene.sems.service;

import com.serene.sems.dto.LoginRequest;
import com.serene.sems.dto.LoginResponse;
import com.serene.sems.model.AuditAction;
import com.serene.sems.model.Role;
import com.serene.sems.model.User;
import com.serene.sems.repository.UserRepository;
import com.serene.sems.security.JwtProvider;
import com.serene.sems.security.UserDetailsServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds {@link Transactional} login work so {@link AuthService#login} can apply timing mitigation
 * without keeping a DB transaction open during {@link Thread#sleep}.
 */
@Service
public class AuthLoginTransactionService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final AuditService auditService;

    public AuthLoginTransactionService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtProvider jwtProvider,
            UserDetailsService userDetailsService,
            AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public LoginResponse performLogin(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (LockedException ex) {
            auditService.record(
                    AuditAction.LOGIN_FAILED,
                    false,
                    "Locked account sign-in attempt",
                    null,
                    null,
                    request.getUsername(),
                    null);
            throw ex;
        } catch (DisabledException ex) {
            auditService.record(
                    AuditAction.LOGIN_FAILED,
                    false,
                    "Disabled or expired account sign-in attempt",
                    null,
                    null,
                    request.getUsername(),
                    null);
            throw ex;
        } catch (BadCredentialsException ex) {
            userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
                int attempts = user.getFailedAttempts() + 1;
                user.setFailedAttempts(attempts);
                if (attempts >= UserDetailsServiceImpl.MAX_FAILED_ATTEMPTS) {
                    user.setLockTime(Instant.now());
                }
                userRepository.save(user);
            });
            auditService.record(
                    AuditAction.LOGIN_FAILED,
                    false,
                    "Invalid password or unknown user",
                    null,
                    null,
                    request.getUsername(),
                    null);
            throw new BadCredentialsException("Invalid credentials");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        user.setFailedAttempts(0);
        user.setLockTime(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtProvider.generateToken(userDetails);
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        auditService.record(
                AuditAction.LOGIN_SUCCESS,
                true,
                null,
                "USER",
                user.getId(),
                user.getUsername(),
                user.getId());

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }
}
