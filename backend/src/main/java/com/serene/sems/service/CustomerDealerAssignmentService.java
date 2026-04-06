package com.serene.sems.service;

import com.serene.sems.model.Customer;
import com.serene.sems.model.Dealer;
import com.serene.sems.repository.CustomerRepository;
import com.serene.sems.repository.DealerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Assigns customers to dealers by country/state/city. Customers are not owned by a dealer; assignment is
 * derived from location and can change when city changes, when a dealer is removed, or when a dealer is
 * deactivated (customers are moved to another active dealer in the same city or left unassigned).
 */
@Service
public class CustomerDealerAssignmentService {

    private final CustomerRepository customerRepository;
    private final DealerRepository dealerRepository;

    public CustomerDealerAssignmentService(
            CustomerRepository customerRepository, DealerRepository dealerRepository) {
        this.customerRepository = customerRepository;
        this.dealerRepository = dealerRepository;
    }

    /**
     * Reassigns every customer currently on {@code dealerIdBeingRemoved} using their city, excluding that
     * dealer from candidates. Customers become unassigned if no other active dealer serves their city.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void reassignCustomersAwayFromDealer(Long dealerIdBeingRemoved) {
        for (Customer c : customerRepository.findByDealerId(dealerIdBeingRemoved)) {
            assignDealerForLocationExcluding(c, dealerIdBeingRemoved);
            customerRepository.save(c);
        }
        customerRepository.flush();
    }

    /** Picks an active dealer for the customer's location, or clears assignment if none. */
    public void assignDealerForLocation(Customer c) {
        assignDealerForLocationExcluding(c, null);
    }

    private void assignDealerForLocationExcluding(Customer c, Long excludeDealerId) {
        String cc = blankToNull(c.getCountryCode());
        String sc = blankToNull(c.getStateCode());
        String ct = blankToNull(c.getCity());
        if (cc == null || sc == null || ct == null) {
            c.setDealer(null);
            return;
        }
        List<Dealer> dealers =
                dealerRepository.findByCountryCodeAndStateCodeAndCityIgnoreCaseAndActiveTrueOrderByIdAsc(cc, sc, ct);
        if (excludeDealerId != null) {
            dealers = dealers.stream().filter(d -> !d.getId().equals(excludeDealerId)).toList();
        }
        if (dealers.isEmpty()) {
            c.setDealer(null);
            return;
        }
        Dealer picked = dealers.stream()
                .min(Comparator.comparingLong(d -> customerRepository.countByDealerId(d.getId())))
                .orElse(dealers.get(0));
        c.setDealer(picked);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
