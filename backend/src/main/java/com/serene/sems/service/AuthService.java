package com.serene.sems.service;

import com.serene.sems.dto.LoginRequest;
import com.serene.sems.dto.LoginResponse;
import com.serene.sems.dto.RegisterDealerRequest;
import com.serene.sems.model.AuditAction;
import com.serene.sems.model.Dealer;
import com.serene.sems.model.Role;
import com.serene.sems.model.User;
import com.serene.sems.repository.DealerRepository;
import com.serene.sems.repository.RoleRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DealerRepository dealerRepository;
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            DealerRepository dealerRepository,
            JwtProvider jwtProvider,
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.dealerRepository = dealerRepository;
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
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

    public void logoutAudit() {
        auditService.record(AuditAction.LOGOUT, true, null, null, null, null, null);
    }

    @Transactional
    public LoginResponse registerDealer(RegisterDealerRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Role dealerRole = roleRepository.findByName("DEALER")
                .orElseThrow(() -> new IllegalStateException("DEALER role not configured"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.getRoles().add(dealerRole);
        user.setLastLoginAt(Instant.now());
        user = userRepository.save(user);

        Dealer dealer = new Dealer();
        dealer.setUser(user);
        dealer.setCompanyName(request.getCompanyName());
        dealer.setPhone(request.getPhone());
        dealer.setAddress(request.getAddress());
        String cc = request.getCountryCode();
        dealer.setCountryCode(cc == null || cc.isBlank() ? null : cc);
        String sc = request.getStateCode();
        dealer.setStateCode(sc == null || sc.isBlank() ? null : sc);
        String city = request.getCity();
        dealer.setCity(city == null || city.isBlank() ? null : city);
        dealer.setActive(true);
        dealerRepository.save(dealer);

        auditService.record(
                AuditAction.REGISTER_DEALER,
                true,
                "Company: " + dealer.getCompanyName(),
                "USER",
                user.getId(),
                user.getUsername(),
                user.getId());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtProvider.generateToken(userDetails);

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(Set.of("DEALER"))
                .build();
    }
}
