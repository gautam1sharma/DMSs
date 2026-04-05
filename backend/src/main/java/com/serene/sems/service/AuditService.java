package com.serene.sems.service;

import com.serene.sems.dto.AuditLogResponse;
import com.serene.sems.model.AuditAction;
import com.serene.sems.model.AuditLog;
import com.serene.sems.model.User;
import com.serene.sems.repository.AuditLogRepository;
import com.serene.sems.repository.UserRepository;
import com.serene.sems.util.ClientIpUtils;
import com.serene.sems.util.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Persists in a new transaction so failures in the caller do not roll back the audit row
     * (e.g. failed login still leaves LOGIN_FAILED).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void record(
            AuditAction action,
            boolean success,
            String detail,
            String targetType,
            Long targetId,
            String actorUsernameOverride,
            Long actorUserIdOverride) {
        String username =
                actorUsernameOverride != null ? actorUsernameOverride : SecurityUtils.currentUsername();
        if (username == null) {
            username = "anonymous";
        }
        Long userId = actorUserIdOverride;
        if (userId == null && !"anonymous".equals(username)) {
            userId = userRepository.findByUsername(username).map(User::getId).orElse(null);
        }

        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setSuccess(success);
        log.setDetail(truncate(detail, 4000));
        log.setTargetType(truncate(targetType, 64));
        log.setTargetId(targetId);
        log.setActorUsername(truncate(username, 80));
        log.setActorUserId(userId);
        log.setIpAddress(truncate(ClientIpUtils.currentClientIp(), 64));
        auditLogRepository.save(log);
    }

    /**
     * HTTP-level audit for admin ({@code /admin/**}) or dealer ({@code /dealer/**}) API calls.
     *
     * @param actorUsername resolved principal (never rely on {@link SecurityContextHolder} here).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void recordPortalHttpRequest(
            AuditAction action, String httpMethod, String requestPath, int httpStatus, String actorUsername) {
        if (action != AuditAction.ADMIN_API_REQUEST && action != AuditAction.DEALER_API_REQUEST) {
            throw new IllegalArgumentException("Unsupported HTTP audit action: " + action);
        }
        String username = actorUsername != null && !actorUsername.isBlank() ? actorUsername : "anonymous";
        Long userId =
                "anonymous".equals(username) ? null : userRepository.findByUsername(username).map(User::getId).orElse(null);

        boolean success = httpStatus >= 200 && httpStatus < 400;

        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setSuccess(success);
        log.setHttpMethod(truncate(httpMethod, 16));
        log.setRequestPath(truncate(requestPath, 1024));
        log.setHttpStatus(httpStatus);
        log.setActorUsername(truncate(username, 80));
        log.setActorUserId(userId);
        log.setIpAddress(truncate(ClientIpUtils.currentClientIp(), 64));
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<AuditLogResponse> listAdmin(
            AuditAction action,
            String actorUsername,
            Instant from,
            Instant to,
            Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (action != null) {
                p.add(cb.equal(root.get("action"), action));
            }
            if (actorUsername != null && !actorUsername.isBlank()) {
                p.add(cb.like(cb.lower(root.get("actorUsername")), "%" + actorUsername.trim().toLowerCase() + "%"));
            }
            if (from != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                p.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        return auditLogRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog a) {
        AuditLogResponse r = new AuditLogResponse();
        r.setId(a.getId());
        r.setCreatedAt(a.getCreatedAt());
        r.setAction(a.getAction().name());
        r.setActorUsername(a.getActorUsername());
        r.setActorUserId(a.getActorUserId());
        r.setTargetType(a.getTargetType());
        r.setTargetId(a.getTargetId());
        r.setDetail(a.getDetail());
        r.setSuccess(a.isSuccess());
        r.setIpAddress(a.getIpAddress());
        r.setHttpMethod(a.getHttpMethod());
        r.setRequestPath(a.getRequestPath());
        r.setHttpStatus(a.getHttpStatus());
        return r;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
