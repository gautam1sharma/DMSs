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
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final DealerService dealerService;
    private final CustomerService customerService;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            DealerService dealerService,
            CustomerService customerService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.dealerService = dealerService;
        this.customerService = customerService;
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
        validateRoleCombination(req.getRoleNames());
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEnabled(req.isEnabled());
        user.setAccountExpiry(req.getAccountExpiry());
        user.setLastLoginAt(Instant.now());
        user.setRoles(resolveRoles(req.getRoleNames()));
        if (roleNamesContainDealer(req.getRoleNames())) {
            requireDealerCompanyName(req.getCompanyName());
        }
        if (roleNamesContainCustomer(req.getRoleNames())) {
            requireCustomerDisplayName(req.getCustomerFullName());
        }
        if (roleNamesContainDealer(req.getRoleNames()) || roleNamesContainCustomer(req.getRoleNames())) {
            requireSharedLocation(req.getCountryCode(), req.getStateCode(), req.getCity());
        }
        User saved = userRepository.save(user);
        if (roleNamesContainDealer(req.getRoleNames())) {
            dealerService.ensureDealerProfileForUser(
                    saved,
                    req.getCompanyName(),
                    req.getPhone(),
                    req.getAddress(),
                    req.getCountryCode(),
                    req.getStateCode(),
                    req.getCity(),
                    req.isDealerActive());
        }
        if (roleNamesContainCustomer(req.getRoleNames())) {
            customerService.ensureCustomerProfileForUser(
                    saved,
                    req.getCustomerFullName().trim(),
                    req.getPhone(),
                    req.getAddress(),
                    req.getCountryCode(),
                    req.getStateCode(),
                    req.getCity(),
                    req.isCustomerActive());
        }
        auditService.record(
                AuditAction.USER_CREATED, true, saved.getUsername(), "USER", saved.getId(), null, null);
        return toResponse(saved);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UserResponse update(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        boolean hadDealerRole = userHasDealerRole(user);
        boolean hadCustomerRole = userHasCustomerRole(user);
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
            validateRoleCombination(req.getRoleNames());
            user.setRoles(resolveRoles(req.getRoleNames()));
        }
        User saved = userRepository.save(user);
        if (req.getRoleNames() != null) {
            if (hadDealerRole && !userHasDealerRole(saved)) {
                dealerService.purgeDealerProfileIfExists(saved);
            }
            if (hadCustomerRole && !userHasCustomerRole(saved)) {
                customerService.detachCustomerProfileForUser(saved);
            }
        }
        if (userHasDealerRole(saved) && dealerService.findDealerForUser(saved).isEmpty()) {
            requireDealerCompanyName(req.getCompanyName());
            requireSharedLocation(req.getCountryCode(), req.getStateCode(), req.getCity());
            boolean dealerActive = req.getDealerActive() != null ? req.getDealerActive() : true;
            dealerService.ensureDealerProfileForUser(
                    saved,
                    req.getCompanyName(),
                    req.getPhone(),
                    req.getAddress(),
                    req.getCountryCode(),
                    req.getStateCode(),
                    req.getCity(),
                    dealerActive);
        }
        if (userHasCustomerRole(saved) && customerService.findCustomerForUser(saved).isEmpty()) {
            requireCustomerDisplayName(req.getCustomerFullName());
            requireSharedLocation(req.getCountryCode(), req.getStateCode(), req.getCity());
            boolean customerActive = req.getCustomerActive() != null ? req.getCustomerActive() : true;
            customerService.ensureCustomerProfileForUser(
                    saved,
                    req.getCustomerFullName().trim(),
                    req.getPhone(),
                    req.getAddress(),
                    req.getCountryCode(),
                    req.getStateCode(),
                    req.getCity(),
                    customerActive);
        }
        if (userHasDealerRole(saved) && dealerService.findDealerForUser(saved).isPresent()) {
            dealerService.syncDealerProfileFromManageUsers(saved, req);
        }
        if (userHasCustomerRole(saved) && customerService.findCustomerForUser(saved).isPresent()) {
            customerService.syncCustomerProfileFromManageUsers(saved, req);
        }
        auditService.record(AuditAction.USER_UPDATED, true, null, "USER", id, null, null);
        return toResponse(saved);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (isProtectedAdministrator(user)) {
            throw new IllegalArgumentException("Administrator accounts cannot be deleted");
        }
        customerService.detachCustomerProfileForUser(user);
        dealerService.purgeDealerProfileIfExists(user);
        auditService.record(AuditAction.USER_DELETED, true, null, "USER", id, null, null);
        userRepository.delete(user);
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

    private static boolean isProtectedAdministrator(User user) {
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            return true;
        }
        return user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(r -> r != null && "ADMIN".equalsIgnoreCase(r));
    }

    private Set<Role> resolveRoles(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return new HashSet<>();
        }
        Set<Role> roles = new HashSet<>();
        for (String name : names) {
            String key = name == null ? "" : name.trim().toUpperCase(Locale.ROOT);
            Role r = roleRepository.findByName(key)
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
        dealerService
                .findDealerForUser(user)
                .ifPresent(
                        d -> {
                            r.setDealerId(d.getId());
                            r.setDealerCompanyName(d.getCompanyName());
                            r.setDealerPhone(d.getPhone());
                            r.setDealerAddress(d.getAddress());
                            r.setDealerCountryCode(d.getCountryCode());
                            r.setDealerStateCode(d.getStateCode());
                            r.setDealerCity(d.getCity());
                            r.setDealerActive(d.isActive());
                        });
        customerService
                .findCustomerForUser(user)
                .ifPresent(
                        c -> {
                            r.setCustomerId(c.getId());
                            r.setCustomerFullName(c.getFullName());
                            r.setCustomerPhone(c.getPhone());
                            r.setCustomerAddress(c.getAddress());
                            r.setCustomerCountryCode(c.getCountryCode());
                            r.setCustomerStateCode(c.getStateCode());
                            r.setCustomerCity(c.getCity());
                            r.setCustomerActive(c.isActive());
                        });
        return r;
    }

    /**
     * Customer portal accounts must not share a login with dealer or administrator
     * roles.
     */
    private static void validateRoleCombination(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return;
        }
        if (!roleNamesContainCustomer(roleNames)) {
            return;
        }
        if (roleNamesContainDealer(roleNames)) {
            throw new IllegalArgumentException("A user cannot have both DEALER and CUSTOMER roles");
        }
        if (roleNamesContainAdmin(roleNames)) {
            throw new IllegalArgumentException("A user cannot have both ADMIN and CUSTOMER roles");
        }
    }

    private static boolean roleNamesContainDealer(Set<String> roleNames) {
        return containsRoleIgnoreCase(roleNames, "DEALER");
    }

    private static boolean roleNamesContainCustomer(Set<String> roleNames) {
        return containsRoleIgnoreCase(roleNames, "CUSTOMER");
    }

    private static boolean roleNamesContainAdmin(Set<String> roleNames) {
        return containsRoleIgnoreCase(roleNames, "ADMIN");
    }

    private static boolean containsRoleIgnoreCase(Set<String> roleNames, String role) {
        if (roleNames == null) {
            return false;
        }
        for (String n : roleNames) {
            if (n != null && role.equalsIgnoreCase(n.trim())) {
                return true;
            }
        }
        return false;
    }

    private static boolean userHasDealerRole(User user) {
        return user.getRoles().stream().anyMatch(r -> "DEALER".equals(r.getName()));
    }

    private static boolean userHasCustomerRole(User user) {
        return user.getRoles().stream().anyMatch(r -> "CUSTOMER".equals(r.getName()));
    }

    private static void requireDealerCompanyName(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException(
                    "Dealer outlet name is required for users with the DEALER role");
        }
    }

    private static void requireCustomerDisplayName(String customerFullName) {
        if (customerFullName == null || customerFullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer full name is required for users with the CUSTOMER role");
        }
    }

    private static void requireSharedLocation(String countryCode, String stateCode, String city) {
        if (blankToNull(countryCode) == null
                || blankToNull(stateCode) == null
                || blankToNull(city) == null) {
            throw new IllegalArgumentException(
                    "Country, state, and city are required when creating a dealer or customer portal profile");
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
