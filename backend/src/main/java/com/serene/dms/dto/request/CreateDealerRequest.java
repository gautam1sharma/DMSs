package com.serene.dms.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateDealerRequest {

    @NotBlank(message = "Dealer name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Dealer code is required")
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must be uppercase alphanumeric")
    private String code;

    @Size(max = 300)
    private String address;

    @Size(max = 80)
    private String city;

    @Size(max = 80)
    private String state;

    @Size(max = 20)
    private String phone;

    @Email(message = "Must be a valid email")
    @Size(max = 150)
    private String email;

    // Optional: link to existing user as the dealer owner
    private Long userId;
}
