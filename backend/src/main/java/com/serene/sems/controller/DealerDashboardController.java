package com.serene.sems.controller;

import com.serene.sems.dto.DashboardSummaryResponse;
import com.serene.sems.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dealer/dashboard")
@Tag(name = "Dealer Dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DealerDashboardController {

    private final DashboardService dashboardService;

    public DealerDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        return dashboardService.dealerSummary();
    }
}
