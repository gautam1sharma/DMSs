package com.serene.dms.service;

import com.serene.dms.dto.request.CreateUserRequest;
import com.serene.dms.dto.response.UserResponse;
import com.serene.dms.entity.Role;
import com.serene.dms.entity.User;
import com.serene.dms.exception.AppException;
import com.serene.dms.repository.RoleRepository;
import com.serene.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(CreateUserRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw AppException.conflict("Email is already registered");
        }

        Set<Role> roles = req.getRoles().stream()
            .map(name -> roleRepository.findByName(name)
                .orElseThrow(() -> AppException.badRequest("Unknown role: " + name)))
            .collect(Collectors.toSet());

        User user = User.builder()
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .email(req.getEmail().toLowerCase())
            .passwordHash(passwordEncoder.encode(req.getPassword()))
            .phone(req.getPhone())
            .roles(roles)
            .build();

        user = userRepository.save(user);
        log.info("User created: {} with roles: {}", user.getEmail(), req.getRoles());
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> AppException.notFound("User", id));
    }

    @Transactional
    public void toggleLock(Long id, boolean lock) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("User", id));
        user.setAccountLocked(lock);
        if (!lock) user.setFailedAttempts(0);
        userRepository.save(user);
        log.info("User {} {}", user.getEmail(), lock ? "locked" : "unlocked");
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) throw AppException.notFound("User", id);
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
            .id(u.getId())
            .firstName(u.getFirstName())
            .lastName(u.getLastName())
            .email(u.getEmail())
            .phone(u.getPhone())
            .enabled(u.isEnabled())
            .accountLocked(u.isAccountLocked())
            .failedAttempts(u.getFailedAttempts())
            .roles(u.getRoles().stream().map(Role::getName).toList())
            .createdAt(u.getCreatedAt())
            .createdBy(u.getCreatedBy())
            .build();
    }
}
