package com.serene.dms.controller;

import com.serene.dms.dto.response.DashboardStatsResponse;
import com.serene.dms.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregate metrics and KPI stats")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin dashboard stats — all dealers, users, and revenue")
    public ResponseEntity<DashboardStatsResponse> adminStats() {
        return ResponseEntity.ok(dashboardService.getAdminStats());
    }

    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    @Operation(summary = "Dealer dashboard stats — customers, vehicles, orders, inquiries")
    public ResponseEntity<DashboardStatsResponse> dealerStats(@PathVariable Long dealerId) {
        return ResponseEntity.ok(dashboardService.getDealerStats(dealerId));
    }
}
