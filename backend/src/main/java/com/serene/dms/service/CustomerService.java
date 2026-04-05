package com.serene.dms.service;

import com.serene.dms.dto.request.CreateCustomerRequest;
import com.serene.dms.dto.response.CustomerResponse;
import com.serene.dms.entity.Customer;
import com.serene.dms.entity.Dealer;
import com.serene.dms.exception.AppException;
import com.serene.dms.repository.CustomerRepository;
import com.serene.dms.repository.DealerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final DealerRepository dealerRepository;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest req) {
        Dealer dealer = dealerRepository.findById(req.getDealerId())
            .orElseThrow(() -> AppException.notFound("Dealer", req.getDealerId()));

        Customer customer = Customer.builder()
            .dealer(dealer)
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .email(req.getEmail())
            .phone(req.getPhone())
            .address(req.getAddress())
            .city(req.getCity())
            .state(req.getState())
            .dateOfBirth(req.getDateOfBirth())
            .notes(req.getNotes())
            .build();

        customer = customerRepository.save(customer);
        log.info("Customer created: {} {} under dealer {}", customer.getFirstName(), customer.getLastName(), dealer.getCode());
        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> getCustomersByDealer(Long dealerId, Pageable pageable) {
        if (!dealerRepository.existsById(dealerId)) throw AppException.notFound("Dealer", dealerId);
        return customerRepository.findByDealerId(dealerId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        return customerRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> AppException.notFound("Customer", id));
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CreateCustomerRequest req) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Customer", id));

        customer.setFirstName(req.getFirstName());
        customer.setLastName(req.getLastName());
        customer.setEmail(req.getEmail());
        customer.setPhone(req.getPhone());
        customer.setAddress(req.getAddress());
        customer.setCity(req.getCity());
        customer.setState(req.getState());
        customer.setDateOfBirth(req.getDateOfBirth());
        customer.setNotes(req.getNotes());

        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) throw AppException.notFound("Customer", id);
        customerRepository.deleteById(id);
    }

    public CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
            .id(c.getId())
            .dealerId(c.getDealer().getId())
            .dealerName(c.getDealer().getName())
            .firstName(c.getFirstName())
            .lastName(c.getLastName())
            .email(c.getEmail())
            .phone(c.getPhone())
            .address(c.getAddress())
            .city(c.getCity())
            .state(c.getState())
            .dateOfBirth(c.getDateOfBirth())
            .status(c.getStatus().name())
            .notes(c.getNotes())
            .createdAt(c.getCreatedAt())
            .build();
    }
}
