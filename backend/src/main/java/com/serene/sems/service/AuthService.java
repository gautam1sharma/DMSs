package com.serene.sems.service;

import com.serene.sems.config.properties.LoginTimingProperties;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DealerRepository dealerRepository;
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final LoginTimingProperties loginTimingProperties;
    private final AuthLoginTransactionService authLoginTransactionService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            DealerRepository dealerRepository,
            JwtProvider jwtProvider,
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            LoginTimingProperties loginTimingProperties,
            AuthLoginTransactionService authLoginTransactionService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.dealerRepository = dealerRepository;
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.loginTimingProperties = loginTimingProperties;
        this.authLoginTransactionService = authLoginTransactionService;
    }

    public LoginResponse login(LoginRequest request) {
        long startNanos = System.nanoTime();
        try {
            return authLoginTransactionService.performLogin(request);
        } finally {
            long minNanos = TimeUnit.MILLISECONDS.toNanos(loginTimingProperties.getLoginMinDurationMs());
            long elapsed = System.nanoTime() - startNanos;
            if (elapsed < minNanos) {
                long sleepMs = TimeUnit.NANOSECONDS.toMillis(minNanos - elapsed);
                if (sleepMs > 0) {
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    public void logoutAudit() {
        auditService.record(AuditAction.LOGOUT, true, null, null, null, null, null);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
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
