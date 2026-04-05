package com.serene.dms.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateCustomerRequest {

    @NotNull(message = "Dealer ID is required")
    private Long dealerId;

    @NotBlank(message = "First name is required")
    @Size(max = 60)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 60)
    private String lastName;

    @Email(message = "Must be a valid email")
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phone;

    @Size(max = 300)
    private String address;

    @Size(max = 80)
    private String city;

    @Size(max = 80)
    private String state;

    private LocalDate dateOfBirth;

    @Size(max = 2000)
    private String notes;
}
