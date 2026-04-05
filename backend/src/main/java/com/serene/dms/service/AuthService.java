package com.serene.dms.service;

import com.serene.dms.dto.request.LoginRequest;
import com.serene.dms.dto.response.AuthResponse;
import com.serene.dms.entity.User;
import com.serene.dms.exception.AppException;
import com.serene.dms.repository.DealerRepository;
import com.serene.dms.repository.UserRepository;
import com.serene.dms.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepository;
    private final DealerRepository dealerRepository;

    @Value("${application.security.max-failed-attempts}")
    private int maxFailedAttempts;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        if (user.isAccountLocked()) {
            log.warn("Locked account login attempt: {}", request.getEmail());
            throw new AppException("Account is locked. Please contact support.", HttpStatus.UNAUTHORIZED);
        }

        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException | AuthenticationException ex) {
            handleFailedAttempt(user);
            // Generic message to prevent user enumeration
            throw new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        // Reset failed attempts on successful login
        userRepository.resetFailedAttempts(user.getEmail());

        List<String> roles = user.getRoles().stream()
            .map(r -> r.getName())
            .toList();

        String accessToken = jwtProvider.generateToken(user.getEmail(), roles, user.getId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        // Resolve dealer ID if user is a DEALER
        Long dealerId = null;
        if (roles.contains("DEALER")) {
            dealerId = dealerRepository.findByUserId(user.getId())
                .map(d -> d.getId())
                .orElse(null);
        }

        log.info("User logged in: {} with roles: {}", user.getEmail(), roles);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(86400)
            .user(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(roles)
                .dealerId(dealerId)
                .build())
            .build();
    }

    private void handleFailedAttempt(User user) {
        userRepository.incrementFailedAttempts(user.getEmail());
        int newAttempts = user.getFailedAttempts() + 1;
        if (newAttempts >= maxFailedAttempts) {
            userRepository.lockAccount(user.getEmail());
            log.warn("Account locked after {} failed attempts: {}", maxFailedAttempts, user.getEmail());
        }
    }
}
