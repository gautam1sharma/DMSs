package com.serene.dms.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateVehicleRequest {

    @NotNull(message = "Dealer ID is required")
    private Long dealerId;

    @NotBlank(message = "Model is required")
    @Size(max = 100)
    private String model;

    @Size(max = 80)
    private String variant;

    @Size(max = 17, message = "VIN must be max 17 characters")
    private String vin;

    @NotNull(message = "Year is required")
    @Min(value = 2000) @Max(value = 2100)
    private Integer year;

    @Size(max = 50)
    private String color;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @Size(max = 30)
    private String mileage;

    @Size(max = 30)
    private String fuelType;

    @Size(max = 30)
    private String transmission;

    private String description;
}
