package com.serene.dms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean enabled;
    private boolean accountLocked;
    private int failedAttempts;
    private List<String> roles;
    private LocalDateTime createdAt;
    private String createdBy;
}
