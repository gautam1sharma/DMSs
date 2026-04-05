package com.serene.dms.controller;

import com.serene.dms.dto.request.CreateCustomerRequest;
import com.serene.dms.dto.response.CustomerResponse;
import com.serene.dms.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management — directly linked to dealers")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    @Operation(summary = "Create a customer (linked directly to a dealer)")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(req));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all customers (admin only)")
    public ResponseEntity<Page<CustomerResponse>> listAll(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(customerService.getAllCustomers(pageable));
    }

    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    @Operation(summary = "List customers by dealer")
    public ResponseEntity<Page<CustomerResponse>> listByDealer(
        @PathVariable Long dealerId,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(customerService.getCustomersByDealer(dealerId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER', 'CUSTOMER')")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<CustomerResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    @Operation(summary = "Update a customer")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CreateCustomerRequest req) {
        return ResponseEntity.ok(customerService.updateCustomer(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    @Operation(summary = "Delete a customer")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
