package com.serene.dms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardStatsResponse {
    // Admin view
    private long totalDealers;
    private long activeDealers;
    private long totalUsers;
    private BigDecimal totalRevenue;

    // Dealer view (populated when dealerId is provided)
    private long totalCustomers;
    private long totalVehicles;
    private long availableVehicles;
    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private long openInquiries;
    private BigDecimal dealerRevenue;
}
