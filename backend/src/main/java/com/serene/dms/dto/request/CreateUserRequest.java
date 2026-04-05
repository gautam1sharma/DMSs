package com.serene.dms.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
public class CreateUserRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 60)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 60)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
             message = "Password must contain uppercase, lowercase, digit and special character")
    private String password;

    @Size(max = 20)
    private String phone;

    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;
}
