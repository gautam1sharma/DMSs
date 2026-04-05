package com.serene.sems.service;

import com.serene.sems.dto.CreateCustomerRequest;
import com.serene.sems.dto.CustomerResponse;
import com.serene.sems.dto.UpdateCustomerRequest;
import com.serene.sems.exception.ResourceNotFoundException;
import com.serene.sems.model.AuditAction;
import com.serene.sems.model.Customer;
import com.serene.sems.model.Dealer;
import com.serene.sems.repository.CustomerRepository;
import com.serene.sems.repository.DealerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final DealerRepository dealerRepository;
    private final DealerService dealerService;
    private final AuditService auditService;

    public CustomerService(
            CustomerRepository customerRepository,
            DealerRepository dealerRepository,
            DealerService dealerService,
            AuditService auditService) {
        this.customerRepository = customerRepository;
        this.dealerRepository = dealerRepository;
        this.dealerService = dealerService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> listAdmin(Long dealerId, String q, Pageable pageable) {
        String nameQ = (q != null && !q.isBlank()) ? q.trim() : null;
        return customerRepository.findCustomers(dealerId, nameQ, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getAdmin(Long id) {
        return customerRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Transactional
    public CustomerResponse createAdmin(CreateCustomerRequest req) {
        Dealer dealer;
        if (req.getDealerId() != null) {
            dealer = dealerRepository.findById(req.getDealerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
        } else {
            dealer = resolveDealerForLocation(req.getCountryCode(), req.getStateCode(), req.getCity());
        }
        Customer saved = customerRepository.save(buildCustomer(req, dealer));
        auditService.record(
                AuditAction.CUSTOMER_CREATED,
                true,
                saved.getFullName(),
                "CUSTOMER",
                saved.getId(),
                null,
                null);
        return toResponse(saved);
    }

    /**
     * Picks an active dealer for the customer's country / state / city. If several serve the same city,
     * chooses the one with the fewest customers (simple load balance).
     */
    private Dealer resolveDealerForLocation(String countryCode, String stateCode, String city) {
        String cc = blankToNull(countryCode);
        String sc = blankToNull(stateCode);
        String ct = blankToNull(city);
        if (cc == null || sc == null || ct == null) {
            throw new IllegalArgumentException(
                    "Either dealerId, or country, state, and city are required to assign a dealer");
        }
        List<Dealer> dealers =
                dealerRepository.findByCountryCodeAndStateCodeAndCityIgnoreCaseAndActiveTrueOrderByIdAsc(cc, sc, ct);
        if (dealers.isEmpty()) {
            throw new IllegalArgumentException("No active dealer found for this city: " + ct + ", " + sc + ", " + cc);
        }
        return dealers.stream()
                .min(Comparator.comparingLong(d -> customerRepository.countByDealerId(d.getId())))
                .orElse(dealers.get(0));
    }

    @Transactional
    public CustomerResponse updateAdmin(Long id, UpdateCustomerRequest req) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        applyUpdate(c, req);
        if (req.getDealerId() != null) {
            Dealer d = dealerRepository.findById(req.getDealerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
            c.setDealer(d);
        }
        Customer saved = customerRepository.save(c);
        auditService.record(AuditAction.CUSTOMER_UPDATED, true, null, "CUSTOMER", id, null, null);
        return toResponse(saved);
    }

    @Transactional
    public void deleteAdmin(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found");
        }
        auditService.record(AuditAction.CUSTOMER_DELETED, true, null, "CUSTOMER", id, null, null);
        customerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> listDealer(String q, Pageable pageable) {
        Dealer dealer = dealerService.requireDealerForCurrentUser();
        String nameQ = (q != null && !q.isBlank()) ? q.trim() : null;
        return customerRepository.findCustomers(dealer.getId(), nameQ, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getDealer(Long id) {
        Dealer dealer = dealerService.requireDealerForCurrentUser();
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (!c.getDealer().getId().equals(dealer.getId())) {
            throw new ResourceNotFoundException("Customer not found");
        }
        return toResponse(c);
    }

    @Transactional
    public CustomerResponse createDealer(CreateCustomerRequest req) {
        Dealer dealer = dealerService.requireDealerForCurrentUser();
        Customer saved = customerRepository.save(buildCustomer(req, dealer));
        auditService.record(
                AuditAction.CUSTOMER_CREATED,
                true,
                saved.getFullName(),
                "CUSTOMER",
                saved.getId(),
                null,
                null);
        return toResponse(saved);
    }

    @Transactional
    public CustomerResponse updateDealer(Long id, UpdateCustomerRequest req) {
        Dealer dealer = dealerService.requireDealerForCurrentUser();
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (!c.getDealer().getId().equals(dealer.getId())) {
            throw new ResourceNotFoundException("Customer not found");
        }
        applyUpdate(c, req);
        Customer saved = customerRepository.save(c);
        auditService.record(AuditAction.CUSTOMER_UPDATED, true, null, "CUSTOMER", id, null, null);
        return toResponse(saved);
    }

    @Transactional
    public void deleteDealer(Long id) {
        Dealer dealer = dealerService.requireDealerForCurrentUser();
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (!c.getDealer().getId().equals(dealer.getId())) {
            throw new ResourceNotFoundException("Customer not found");
        }
        auditService.record(AuditAction.CUSTOMER_DELETED, true, null, "CUSTOMER", id, null, null);
        customerRepository.delete(c);
    }

    private Customer buildCustomer(CreateCustomerRequest req, Dealer dealer) {
        Customer c = new Customer();
        c.setDealer(dealer);
        c.setFullName(req.getFullName());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        c.setCountryCode(req.getCountryCode());
        c.setStateCode(req.getStateCode());
        c.setCity(req.getCity());
        c.setActive(req.isActive());
        return c;
    }

    private void applyUpdate(Customer c, UpdateCustomerRequest req) {
        if (req.getFullName() != null) {
            c.setFullName(req.getFullName());
        }
        if (req.getPhone() != null) {
            c.setPhone(req.getPhone());
        }
        if (req.getAddress() != null) {
            c.setAddress(req.getAddress());
        }
        if (req.getCountryCode() != null) {
            c.setCountryCode(blankToNull(req.getCountryCode()));
        }
        if (req.getStateCode() != null) {
            c.setStateCode(blankToNull(req.getStateCode()));
        }
        if (req.getCity() != null) {
            c.setCity(blankToNull(req.getCity()));
        }
        if (req.getActive() != null) {
            c.setActive(req.getActive());
        }
    }

    private CustomerResponse toResponse(Customer c) {
        CustomerResponse r = new CustomerResponse();
        r.setId(c.getId());
        r.setDealerId(c.getDealer().getId());
        r.setDealerCompanyName(c.getDealer().getCompanyName());
        r.setUserId(c.getUser() != null ? c.getUser().getId() : null);
        r.setFullName(c.getFullName());
        r.setPhone(c.getPhone());
        r.setAddress(c.getAddress());
        r.setCountryCode(c.getCountryCode());
        r.setStateCode(c.getStateCode());
        r.setCity(c.getCity());
        r.setActive(c.isActive());
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
