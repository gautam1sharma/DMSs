package com.serene.sems.dto;

import com.serene.sems.model.OrderStatus;

import jakarta.validation.constraints.NotNull;

public class UpdateOrderStatusRequest {

    @NotNull
    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
