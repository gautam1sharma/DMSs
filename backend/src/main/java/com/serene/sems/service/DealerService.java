package com.serene.sems.service;

import com.serene.sems.dto.CreateDealerRequest;
import com.serene.sems.dto.DealerResponse;
import com.serene.sems.dto.UpdateDealerRequest;
import com.serene.sems.exception.ResourceNotFoundException;
import com.serene.sems.model.AuditAction;
import com.serene.sems.model.Dealer;
import com.serene.sems.model.Role;
import com.serene.sems.model.User;
import com.serene.sems.repository.DealerRepository;
import com.serene.sems.repository.OrderRepository;
import com.serene.sems.repository.RoleRepository;
import com.serene.sems.repository.UserRepository;
import com.serene.sems.util.SecurityUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DealerService {

    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final CustomerDealerAssignmentService customerDealerAssignmentService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public DealerService(
            DealerRepository dealerRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            OrderRepository orderRepository,
            CustomerDealerAssignmentService customerDealerAssignmentService,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.dealerRepository = dealerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.orderRepository = orderRepository;
        this.customerDealerAssignmentService = customerDealerAssignmentService;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<DealerResponse> listAdmin(String q, Pageable pageable) {
        if (q != null && !q.isBlank()) {
            return dealerRepository.findByCompanyNameContainingIgnoreCase(q.trim(), pageable).map(this::toResponse);
        }
        return dealerRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public DealerResponse getAdmin(Long id) {
        return dealerRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DealerResponse create(CreateDealerRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        Role dealerRole = roleRepository.findByName("DEALER")
                .orElseThrow(() -> new IllegalStateException("DEALER role not configured"));

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEnabled(true);
        user.getRoles().add(dealerRole);
        user = userRepository.save(user);

        Dealer dealer = new Dealer();
        dealer.setUser(user);
        dealer.setCompanyName(req.getCompanyName());
        dealer.setPhone(req.getPhone());
        dealer.setAddress(req.getAddress());
        dealer.setCountryCode(blankToNull(req.getCountryCode()));
        dealer.setStateCode(blankToNull(req.getStateCode()));
        dealer.setCity(blankToNull(req.getCity()));
        dealer.setActive(req.isActive());
        Dealer saved = dealerRepository.save(dealer);
        auditService.record(
                AuditAction.DEALER_CREATED,
                true,
                saved.getCompanyName(),
                "DEALER",
                saved.getId(),
                null,
                null);
        return toResponse(saved);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DealerResponse updateAdmin(Long id, UpdateDealerRequest req) {
        Dealer dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
        User user = dealer.getUser();
        if (req.getEmail() != null && !req.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new IllegalArgumentException("Email already registered");
            }
            user.setEmail(req.getEmail());
            userRepository.save(user);
        }
        if (req.getCompanyName() != null) {
            dealer.setCompanyName(req.getCompanyName());
        }
        if (req.getPhone() != null) {
            dealer.setPhone(req.getPhone());
        }
        if (req.getAddress() != null) {
            dealer.setAddress(req.getAddress());
        }
        if (req.getCountryCode() != null) {
            dealer.setCountryCode(blankToNull(req.getCountryCode()));
        }
        if (req.getStateCode() != null) {
            dealer.setStateCode(blankToNull(req.getStateCode()));
        }
        if (req.getCity() != null) {
            dealer.setCity(blankToNull(req.getCity()));
        }
        if (req.getActive() != null) {
            dealer.setActive(req.getActive());
        }
        Dealer saved = dealerRepository.save(dealer);
        auditService.record(AuditAction.DEALER_UPDATED, true, null, "DEALER", id, null, null);
        return toResponse(saved);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void delete(Long id) {
        Dealer dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
        User user = dealer.getUser();
        deleteDealerRowAndDependents(dealer);
        userRepository.delete(user);
    }

    /**
     * Removes the dealer profile: customers stay and are reassigned by city; past orders keep history with
     * dealer unlinked.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void purgeDealerProfileIfExists(User user) {
        dealerRepository.findByUser(user).ifPresent(this::deleteDealerRowAndDependents);
    }

    private void deleteDealerRowAndDependents(Dealer dealer) {
        Long dealerId = dealer.getId();
        customerDealerAssignmentService.reassignCustomersAwayFromDealer(dealerId);
        orderRepository.unlinkDealerFromOrders(dealerId);
        orderRepository.flush();
        auditService.record(
                AuditAction.DEALER_DELETED, true, dealer.getCompanyName(), "DEALER", dealerId, null, null);
        dealerRepository.delete(dealer);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public DealerResponse currentProfile() {
        String username = SecurityUtils.currentUsername();
        Dealer dealer = dealerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer profile not found"));
        return toResponse(dealer);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DealerResponse updateProfile(UpdateDealerRequest req) {
        String username = SecurityUtils.currentUsername();
        Dealer dealer = dealerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer profile not found"));
        User user = dealer.getUser();
        if (req.getEmail() != null && !req.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new IllegalArgumentException("Email already registered");
            }
            user.setEmail(req.getEmail());
            userRepository.save(user);
        }
        if (req.getCompanyName() != null) {
            dealer.setCompanyName(req.getCompanyName());
        }
        if (req.getPhone() != null) {
            dealer.setPhone(req.getPhone());
        }
        if (req.getAddress() != null) {
            dealer.setAddress(req.getAddress());
        }
        if (req.getCountryCode() != null) {
            dealer.setCountryCode(blankToNull(req.getCountryCode()));
        }
        if (req.getStateCode() != null) {
            dealer.setStateCode(blankToNull(req.getStateCode()));
        }
        if (req.getCity() != null) {
            dealer.setCity(blankToNull(req.getCity()));
        }
        Dealer saved = dealerRepository.save(dealer);
        auditService.record(
                AuditAction.DEALER_PROFILE_UPDATED, true, null, "DEALER", saved.getId(), null, null);
        return toResponse(saved);
    }

    public Dealer requireDealerForCurrentUser() {
        String username = SecurityUtils.currentUsername();
        return dealerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer profile not found"));
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<Dealer> findDealerForUser(User user) {
        return dealerRepository.findByUser(user);
    }

    /**
     * Creates a {@link Dealer} row for a persisted user that has the DEALER role. No-op if a profile already exists.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void ensureDealerProfileForUser(
            User user,
            String companyName,
            String phone,
            String address,
            String countryCode,
            String stateCode,
            String city,
            boolean active) {
        if (user.getId() == null) {
            throw new IllegalStateException("User must be persisted before attaching a dealer profile");
        }
        if (dealerRepository.findByUser(user).isPresent()) {
            return;
        }
        boolean hasDealerRole =
                user.getRoles().stream().anyMatch(r -> "DEALER".equals(r.getName()));
        if (!hasDealerRole) {
            throw new IllegalArgumentException("User must have the DEALER role to attach a dealer profile");
        }
        Dealer dealer = new Dealer();
        dealer.setUser(user);
        dealer.setCompanyName(companyName.trim());
        dealer.setPhone(phone);
        dealer.setAddress(address);
        dealer.setCountryCode(blankToNull(countryCode));
        dealer.setStateCode(blankToNull(stateCode));
        dealer.setCity(blankToNull(city));
        dealer.setActive(active);
        Dealer saved = dealerRepository.save(dealer);
        auditService.record(
                AuditAction.DEALER_CREATED,
                true,
                saved.getCompanyName(),
                "DEALER",
                saved.getId(),
                null,
                null);
    }

    private DealerResponse toResponse(Dealer d) {
        User u = d.getUser();
        DealerResponse r = new DealerResponse();
        r.setId(d.getId());
        r.setUserId(u.getId());
        r.setUsername(u.getUsername());
        r.setEmail(u.getEmail());
        r.setCompanyName(d.getCompanyName());
        r.setPhone(d.getPhone());
        r.setAddress(d.getAddress());
        r.setCountryCode(d.getCountryCode());
        r.setStateCode(d.getStateCode());
        r.setCity(d.getCity());
        r.setActive(d.isActive());
        r.setCreatedAt(d.getCreatedAt());
        return r;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
