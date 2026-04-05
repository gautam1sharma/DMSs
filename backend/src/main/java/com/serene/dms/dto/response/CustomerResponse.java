package com.serene.dms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerResponse {
    private Long id;
    private Long dealerId;
    private String dealerName;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private LocalDate dateOfBirth;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
