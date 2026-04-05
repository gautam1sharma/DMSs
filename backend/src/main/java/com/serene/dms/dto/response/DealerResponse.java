package com.serene.dms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DealerResponse {
    private Long id;
    private String name;
    private String code;
    private String address;
    private String city;
    private String state;
    private String phone;
    private String email;
    private String status;
    private Long userId;
    private String ownerName;
    private long customerCount;
    private long vehicleCount;
    private LocalDateTime createdAt;
    private String createdBy;
}
