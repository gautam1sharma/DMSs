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
        return r;
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
