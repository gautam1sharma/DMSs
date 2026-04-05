package com.serene.sems.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryResponse {

    private long dealerCount;
    private long customerCount;
    private long orderCount;
    private BigDecimal revenueTotal;
    private List<OrderResponse> recentOrders;

    public long getDealerCount() {
        return dealerCount;
    }

    public void setDealerCount(long dealerCount) {
        this.dealerCount = dealerCount;
    }

    public long getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(long customerCount) {
        this.customerCount = customerCount;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getRevenueTotal() {
        return revenueTotal;
    }

    public void setRevenueTotal(BigDecimal revenueTotal) {
        this.revenueTotal = revenueTotal;
    }

    public List<OrderResponse> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<OrderResponse> recentOrders) {
        this.recentOrders = recentOrders;
    }
}
