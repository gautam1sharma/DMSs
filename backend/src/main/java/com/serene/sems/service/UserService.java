package com.serene.sems.service;

import com.serene.sems.dto.CreateUserRequest;
import com.serene.sems.dto.UpdateUserRequest;
import com.serene.sems.dto.UserResponse;
import com.serene.sems.exception.ResourceNotFoundException;
import com.serene.sems.model.AuditAction;
import com.serene.sems.model.Role;
import com.serene.sems.model.User;
import com.serene.sems.repository.RoleRepository;
import com.serene.sems.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public UserResponse get(Long id) {
        return userRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponse create(CreateUserRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEnabled(req.isEnabled());
        user.setAccountExpiry(req.getAccountExpiry());
        user.setLastLoginAt(Instant.now());
        user.setRoles(resolveRoles(req.getRoleNames()));
        User saved = userRepository.save(user);
        auditService.record(
                AuditAction.USER_CREATED, true, saved.getUsername(), "USER", saved.getId(), null, null);
        return toResponse(saved);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponse update(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (req.getEmail() != null) {
            if (!req.getEmail().equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmail(req.getEmail())) {
                throw new IllegalArgumentException("Email already registered");
            }
            user.setEmail(req.getEmail());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getEnabled() != null) {
            user.setEnabled(req.getEnabled());
        }
        if (req.getAccountExpiry() != null) {
            user.setAccountExpiry(req.getAccountExpiry());
        }
        if (req.getRoleNames() != null) {
            user.setRoles(resolveRoles(req.getRoleNames()));
        }
        User saved = userRepository.save(user);
        auditService.record(AuditAction.USER_UPDATED, true, null, "USER", id, null, null);
        return toResponse(saved);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        auditService.record(AuditAction.USER_DELETED, true, null, "USER", id, null, null);
        userRepository.deleteById(id);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponse unlock(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFailedAttempts(0);
        user.setLockTime(null);
        user.setLastLoginAt(Instant.now());
        User saved = userRepository.save(user);
        auditService.record(AuditAction.USER_UNLOCKED, true, null, "USER", id, null, null);
        return toResponse(saved);
    }

    private Set<Role> resolveRoles(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return new HashSet<>();
        }
        Set<Role> roles = new HashSet<>();
        for (String name : names) {
            Role r = roleRepository.findByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + name));
            roles.add(r);
        }
        return roles;
    }

    private UserResponse toResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setUsername(user.getUsername());
        r.setEmail(user.getEmail());
        r.setEnabled(user.isEnabled());
        r.setFailedAttempts(user.getFailedAttempts());
        r.setLockTime(user.getLockTime());
        r.setAccountExpiry(user.getAccountExpiry());
        r.setLastLoginAt(user.getLastLoginAt());
        r.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        r.setCreatedAt(user.getCreatedAt());
        r.setUpdatedAt(user.getUpdatedAt());
        return r;
    }
}
