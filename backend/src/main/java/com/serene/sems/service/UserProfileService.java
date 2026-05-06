package com.serene.sems.service;

import com.serene.sems.model.User;
import com.serene.sems.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
public class UserProfileService {

    private static final long MAX_AVATAR_BYTES = 2 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void updateAvatar(String username, byte[] data, String contentType) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Empty file");
        }
        if (data.length > MAX_AVATAR_BYTES) {
            throw new IllegalArgumentException("Image must be 2 MB or smaller");
        }
        String ct = normalizeContentType(contentType);
        if (!ALLOWED_TYPES.contains(ct)) {
            throw new IllegalArgumentException("Only JPEG, PNG, WebP, or GIF images are allowed");
        }
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setAvatarData(data);
        user.setAvatarContentType(ct);
        user.setHasAvatar(true);
        userRepository.save(user);
    }

    @Transactional
    public void clearAvatar(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setAvatarData(null);
        user.setAvatarContentType(null);
        user.setHasAvatar(false);
        userRepository.save(user);
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "";
        }
        String lower = contentType.toLowerCase(Locale.ROOT).trim();
        int semi = lower.indexOf(';');
        return semi >= 0 ? lower.substring(0, semi).trim() : lower;
    }
}
