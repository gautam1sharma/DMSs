package com.serene.dms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long dealerId;
    private String dealerName;
    private Long customerId;
    private String customerName;
    private Long vehicleId;
    private String vehicleInfo;
    private BigDecimal amount;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;
}
