package com.serene.sems.service;

import com.serene.sems.dto.CreateDealerRequest;
import com.serene.sems.dto.DealerResponse;
import com.serene.sems.dto.UpdateDealerRequest;
import com.serene.sems.exception.ResourceNotFoundException;
import com.serene.sems.model.AuditAction;
import com.serene.sems.model.Dealer;
import com.serene.sems.model.Role;
import com.serene.sems.model.User;
import com.serene.sems.repository.CustomerRepository;
import com.serene.sems.repository.DealerRepository;
import com.serene.sems.repository.RoleRepository;
import com.serene.sems.repository.UserRepository;
import com.serene.sems.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DealerService {

    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public DealerService(
            DealerRepository dealerRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.dealerRepository = dealerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<DealerResponse> listAdmin(String q, Pageable pageable) {
        if (q != null && !q.isBlank()) {
            return dealerRepository.findByCompanyNameContainingIgnoreCase(q.trim(), pageable).map(this::toResponse);
        }
        return dealerRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DealerResponse getAdmin(Long id) {
        return dealerRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
    }

    @Transactional
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

    @Transactional
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

    @Transactional
    public void delete(Long id) {
        Dealer dealer = dealerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
        if (customerRepository.countByDealerId(dealer.getId()) > 0) {
            throw new IllegalArgumentException("Cannot delete dealer with existing customers");
        }
        auditService.record(AuditAction.DEALER_DELETED, true, dealer.getCompanyName(), "DEALER", id, null, null);
        dealerRepository.delete(dealer);
        userRepository.delete(dealer.getUser());
    }

    @Transactional(readOnly = true)
    public DealerResponse currentProfile() {
        String username = SecurityUtils.currentUsername();
        Dealer dealer = dealerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer profile not found"));
        return toResponse(dealer);
    }

    @Transactional
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
