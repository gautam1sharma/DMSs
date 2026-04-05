package com.serene.sems.controller;

import com.serene.sems.dto.LoginRequest;
import com.serene.sems.dto.LoginResponse;
import com.serene.sems.dto.RegisterDealerRequest;
import com.serene.sems.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api.base-path}/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Self-register as dealer")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterDealerRequest request) {
        return ResponseEntity.ok(authService.registerDealer(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Sign out (records audit log)")
    public ResponseEntity<Void> logout() {
        authService.logoutAudit();
        return ResponseEntity.noContent().build();
    }
}
