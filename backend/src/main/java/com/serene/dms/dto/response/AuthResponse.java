package com.serene.dms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private List<String> roles;
        private Long dealerId;   // present if role is DEALER
    }
}
