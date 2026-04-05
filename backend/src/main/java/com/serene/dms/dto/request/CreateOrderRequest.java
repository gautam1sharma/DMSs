package com.serene.dms.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Dealer ID is required")
    private Long dealerId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private Long vehicleId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    @DecimalMin("0.0")
    private BigDecimal discount;

    private String notes;
}
