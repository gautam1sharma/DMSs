package com.serene.sems.service;

import com.serene.sems.dto.CreateDealerRequest;
import com.serene.sems.dto.DealerResponse;
import com.serene.sems.dto.UpdateDealerRequest;
import com.serene.sems.dto.UpdateUserRequest;
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
import org.springframework.security.access.AccessDeniedException;
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
        assertNoOtherActiveDealerSharesLocation(null, dealer.getCountryCode(), dealer.getStateCode(), dealer.getCity(), dealer.isActive());
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
        boolean activeBefore = dealer.isActive();
        String cityBefore = blankToNull(dealer.getCity());
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
        assertNoOtherActiveDealerSharesLocation(
                dealer.getId(),
                dealer.getCountryCode(),
                dealer.getStateCode(),
                dealer.getCity(),
                dealer.isActive());
        Dealer saved = dealerRepository.save(dealer);
        boolean cityChanged = !sameCity(cityBefore, blankToNull(saved.getCity()));
        if (activeBefore && !saved.isActive()) {
            dealerRepository.flush();
            customerDealerAssignmentService.reassignCustomersAwayFromDealer(saved.getId());
        } else if (cityChanged && saved.isActive()) {
            // Dealer moved to a new city — reassign old-city customers to the correct local dealer
            dealerRepository.flush();
            customerDealerAssignmentService.reassignCustomersAwayFromDealer(saved.getId());
        }
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
        Dealer dealer = requireDealerForCurrentUser();
        return toResponse(dealer);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DealerResponse updateProfile(UpdateDealerRequest req) {
        Dealer dealer = requireDealerForCurrentUser();
        String cityBefore = blankToNull(dealer.getCity());
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
        assertNoOtherActiveDealerSharesLocation(
                dealer.getId(),
                dealer.getCountryCode(),
                dealer.getStateCode(),
                dealer.getCity(),
                dealer.isActive());
        Dealer saved = dealerRepository.save(dealer);
        boolean cityChanged = !sameCity(cityBefore, blankToNull(saved.getCity()));
        if (cityChanged && saved.isActive()) {
            // Dealer moved to a new city — reassign old-city customers to the correct local dealer
            dealerRepository.flush();
            customerDealerAssignmentService.reassignCustomersAwayFromDealer(saved.getId());
        }
        auditService.record(
                AuditAction.DEALER_PROFILE_UPDATED, true, null, "DEALER", saved.getId(), null, null);
        return toResponse(saved);
    }

    public Dealer requireDealerForCurrentUser() {
        String username = SecurityUtils.currentUsername();
        Dealer dealer = dealerRepository
                .findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer profile not found"));
        if (!dealer.isActive()) {
            throw new AccessDeniedException("Dealer account is inactive");
        }
        return dealer;
    }

    /**
     * Applies Manage Users dealer fields to an existing profile when the user still has the DEALER role.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void syncDealerProfileFromManageUsers(User user, UpdateUserRequest req) {
        if (req == null) {
            return;
        }
        boolean touched = req.getCompanyName() != null
                || req.getPhone() != null
                || req.getAddress() != null
                || req.getCountryCode() != null
                || req.getStateCode() != null
                || req.getCity() != null
                || req.getDealerActive() != null;
        if (!touched) {
            return;
        }
        Dealer dealer = dealerRepository.findByUser(user).orElse(null);
        if (dealer == null) {
            return;
        }
        boolean activeBefore = dealer.isActive();
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
        if (req.getDealerActive() != null) {
            dealer.setActive(req.getDealerActive());
        }
        assertNoOtherActiveDealerSharesLocation(
                dealer.getId(),
                dealer.getCountryCode(),
                dealer.getStateCode(),
                dealer.getCity(),
                dealer.isActive());
        dealerRepository.save(dealer);
        if (activeBefore && !dealer.isActive()) {
            dealerRepository.flush();
            customerDealerAssignmentService.reassignCustomersAwayFromDealer(dealer.getId());
        }
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
        assertNoOtherActiveDealerSharesLocation(null, dealer.getCountryCode(), dealer.getStateCode(), dealer.getCity(), dealer.isActive());
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

    /**
     * For public registration: rejects when an active dealer already occupies this country/state/city.
     */
    public void validateNewActiveDealerLocation(String countryCode, String stateCode, String city) {
        assertNoOtherActiveDealerSharesLocation(null, countryCode, stateCode, city, true);
    }

    /**
     * At most one <strong>active</strong> dealer may use the same country, state, and city (city compared
     * case-insensitive), matching customer assignment rules. Applies when creating an active dealer,
     * updating location while active, or <strong>reactivating</strong> a dealer (inactive → active): if
     * another active dealer already serves that city, the save is rejected.
     *
     * @param excludeDealerId dealer row to ignore (null when creating a new profile)
     */
    private void assertNoOtherActiveDealerSharesLocation(
            Long excludeDealerId, String countryCode, String stateCode, String city, boolean willBeActive) {
        if (!willBeActive) {
            return;
        }
        String cc = blankToNull(countryCode);
        String sc = blankToNull(stateCode);
        String ct = blankToNull(city);
        if (cc == null || sc == null || ct == null) {
            throw new IllegalArgumentException(
                    "Country, state, and city are required for an active dealer. Set a full service area before activating (only one active dealer is allowed per city).");
        }
        boolean conflict =
                excludeDealerId == null
                        ? dealerRepository.existsByCountryCodeAndStateCodeAndCityIgnoreCaseAndActiveTrue(cc, sc, ct)
                        : dealerRepository.existsByCountryCodeAndStateCodeAndCityIgnoreCaseAndActiveTrueAndIdNot(
                                cc, sc, ct, excludeDealerId);
        if (conflict) {
            throw new IllegalArgumentException(
                    "An active dealer already serves this city. Choose a different location, keep this dealer inactive, or deactivate the other dealer first.");
        }
    }

    private static boolean sameCity(String before, String after) {
        if (before == null && after == null) {
            return true;
        }
        if (before == null || after == null) {
            return false;
        }
        return before.equalsIgnoreCase(after);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
