package com.serene.sems.controller;

import com.serene.sems.dto.CreateDealerRequest;
import com.serene.sems.dto.DealerResponse;
import com.serene.sems.dto.UpdateDealerRequest;
import com.serene.sems.service.DealerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api.base-path}/admin/dealers")
@Tag(name = "Admin Dealers")
@SecurityRequirement(name = "bearerAuth")
public class AdminDealerController {

    private final DealerService dealerService;

    public AdminDealerController(DealerService dealerService) {
        this.dealerService = dealerService;
    }

    @GetMapping
    public Page<DealerResponse> list(@RequestParam(required = false) String q, Pageable pageable) {
        return dealerService.listAdmin(q, pageable);
    }

    @GetMapping("/{id}")
    public DealerResponse get(@PathVariable Long id) {
        return dealerService.getAdmin(id);
    }

    @PostMapping
    public ResponseEntity<DealerResponse> create(@Valid @RequestBody CreateDealerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealerService.create(req));
    }

    @PutMapping("/{id}")
    public DealerResponse update(@PathVariable Long id, @Valid @RequestBody UpdateDealerRequest req) {
        return dealerService.updateAdmin(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dealerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
