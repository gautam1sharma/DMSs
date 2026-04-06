package com.serene.sems.security;

import com.serene.sems.model.Role;
import com.serene.sems.model.User;
import com.serene.sems.repository.DealerRepository;
import com.serene.sems.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
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
    private final DealerRepository dealerRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, DealerRepository dealerRepository) {
        this.userRepository = userRepository;
        this.dealerRepository = dealerRepository;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
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

        assertDealerActiveIfDealerPortalUser(user);

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

    /**
     * Inactive dealer profiles cannot authenticate as dealers. Users who also have ADMIN may still sign in
     * (admin console) even if their dealer row is inactive.
     */
    private void assertDealerActiveIfDealerPortalUser(User user) {
        boolean hasDealer =
                user.getRoles().stream().anyMatch(r -> r != null && "DEALER".equals(r.getName()));
        boolean hasAdmin =
                user.getRoles().stream().anyMatch(r -> r != null && "ADMIN".equals(r.getName()));
        if (!hasDealer || hasAdmin) {
            return;
        }
        dealerRepository
                .findByUser(user)
                .ifPresent(
                        d -> {
                            if (!d.isActive()) {
                                throw new org.springframework.security.authentication.DisabledException(
                                        "Dealer account is inactive");
                            }
                        });
    }
}
