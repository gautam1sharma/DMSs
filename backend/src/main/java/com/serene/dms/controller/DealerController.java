package com.serene.dms.controller;

import com.serene.dms.dto.request.CreateDealerRequest;
import com.serene.dms.dto.response.DealerResponse;
import com.serene.dms.service.DealerService;
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
@RequestMapping("/api/v1/dealers")
@RequiredArgsConstructor
@Tag(name = "Dealers", description = "Dealer management")
public class DealerController {

    private final DealerService dealerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new dealer")
    public ResponseEntity<DealerResponse> create(@Valid @RequestBody CreateDealerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealerService.createDealer(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    @Operation(summary = "List all dealers")
    public ResponseEntity<Page<DealerResponse>> list(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(dealerService.getAllDealers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    @Operation(summary = "Get dealer by ID")
    public ResponseEntity<DealerResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(dealerService.getDealerById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a dealer")
    public ResponseEntity<DealerResponse> update(@PathVariable Long id, @Valid @RequestBody CreateDealerRequest req) {
        return ResponseEntity.ok(dealerService.updateDealer(id, req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle dealer status (ACTIVE/INACTIVE/SUSPENDED)")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        dealerService.toggleStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a dealer")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dealerService.deleteDealer(id);
        return ResponseEntity.noContent().build();
    }
}
