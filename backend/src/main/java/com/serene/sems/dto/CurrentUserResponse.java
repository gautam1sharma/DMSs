package com.serene.sems.dto;

import java.util.Set;

public class CurrentUserResponse {

    private Long userId;
    private String username;
    private String email;
    private Set<String> roles;
    private boolean hasAvatar;

    public CurrentUserResponse() {
    }

    public CurrentUserResponse(Long userId, String username, String email, Set<String> roles, boolean hasAvatar) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.hasAvatar = hasAvatar;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isHasAvatar() {
        return hasAvatar;
    }

    public void setHasAvatar(boolean hasAvatar) {
        this.hasAvatar = hasAvatar;
    }
}
