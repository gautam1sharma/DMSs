package com.serene.sems.controller;

import com.serene.sems.dto.DealerResponse;
import com.serene.sems.dto.UpdateDealerRequest;
import com.serene.sems.service.DealerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api.base-path}/dealer/profile")
@Tag(name = "Dealer Profile")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('DEALER')")
public class DealerProfileController {

    private final DealerService dealerService;

    public DealerProfileController(DealerService dealerService) {
        this.dealerService = dealerService;
    }

    @GetMapping
    public DealerResponse get() {
        return dealerService.currentProfile();
    }

    @PutMapping
    public DealerResponse update(@Valid @RequestBody UpdateDealerRequest req) {
        return dealerService.updateProfile(req);
    }
}
