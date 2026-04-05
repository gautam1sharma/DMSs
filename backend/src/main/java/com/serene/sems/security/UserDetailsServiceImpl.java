package com.serene.sems.security;

import com.serene.sems.model.Role;
import com.serene.sems.model.User;
import com.serene.sems.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int LOCK_MINUTES = 5;
    public static final int MAX_DAYS_SINCE_LAST_LOGIN = 365;

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (user.getLockTime() != null) {
            Instant unlockAt = user.getLockTime().plus(LOCK_MINUTES, ChronoUnit.MINUTES);
            if (Instant.now().isBefore(unlockAt)) {
                throw new org.springframework.security.authentication.LockedException("Account locked");
            }
            user.setLockTime(null);
            user.setFailedAttempts(0);
            userRepository.save(user);
        }

        if (user.getLastLoginAt() != null) {
            Instant inactiveDeadline = user.getLastLoginAt().plus(MAX_DAYS_SINCE_LAST_LOGIN, ChronoUnit.DAYS);
            if (Instant.now().isAfter(inactiveDeadline)) {
                throw new org.springframework.security.authentication.DisabledException("Account expired");
            }
        }

        if (user.getAccountExpiry() != null && Instant.now().isAfter(user.getAccountExpiry())) {
            throw new org.springframework.security.authentication.DisabledException("Account expired");
        }

        if (!user.isEnabled()) {
            throw new org.springframework.security.authentication.DisabledException("Account disabled");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .authorities(mapRoles(user))
                .build();
    }

    private Collection<? extends GrantedAuthority> mapRoles(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .map(r -> "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
