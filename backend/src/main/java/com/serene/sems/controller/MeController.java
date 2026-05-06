package com.serene.sems.controller;

import com.serene.sems.dto.CurrentUserResponse;
import com.serene.sems.model.Role;
import com.serene.sems.model.User;
import com.serene.sems.repository.UserRepository;
import com.serene.sems.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@RestController
@RequestMapping("${app.api.base-path}/me")
@Tag(name = "Current user")
public class MeController {

    private final UserRepository userRepository;
    private final UserProfileService userProfileService;

    public MeController(UserRepository userRepository, UserProfileService userProfileService) {
        this.userRepository = userRepository;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    @Operation(summary = "Current user profile (no image bytes)")
    public CurrentUserResponse getMe(@AuthenticationPrincipal UserDetails principal) {
        return toResponse(loadUser(principal.getUsername()));
    }

    @GetMapping("/avatar")
    @Transactional(readOnly = true)
    @Operation(summary = "Profile photo bytes")
    public ResponseEntity<byte[]> getAvatar(@AuthenticationPrincipal UserDetails principal) {
        User user = loadUser(principal.getUsername());
        if (!user.isHasAvatar()) {
            return ResponseEntity.notFound().build();
        }
        byte[] data = user.getAvatarData();
        if (data == null || data.length == 0) {
            return ResponseEntity.notFound().build();
        }
        MediaType mt = MediaType.APPLICATION_OCTET_STREAM;
        if (user.getAvatarContentType() != null && !user.getAvatarContentType().isBlank()) {
            try {
                mt = MediaType.parseMediaType(user.getAvatarContentType());
            } catch (Exception ignored) {
                // keep octet-stream
            }
        }
        return ResponseEntity.ok().contentType(mt).body(data);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile photo (multipart field \"file\")")
    public CurrentUserResponse uploadAvatar(
            @AuthenticationPrincipal UserDetails principal, @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file uploaded");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Could not read uploaded file");
        }
        userProfileService.updateAvatar(principal.getUsername(), bytes, file.getContentType());
        return toResponse(loadUser(principal.getUsername()));
    }

    @DeleteMapping("/avatar")
    @Operation(summary = "Remove profile photo")
    public ResponseEntity<Void> deleteAvatar(@AuthenticationPrincipal UserDetails principal) {
        userProfileService.clearAvatar(principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    private static CurrentUserResponse toResponse(User user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.isHasAvatar());
    }
}
