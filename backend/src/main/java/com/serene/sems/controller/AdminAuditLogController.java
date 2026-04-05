package com.serene.sems.controller;

import com.serene.sems.dto.AuditLogResponse;
import com.serene.sems.model.AuditAction;
import com.serene.sems.service.AuditService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin/audit-logs")
@Tag(name = "Admin Audit Logs")
@SecurityRequirement(name = "bearerAuth")
public class AdminAuditLogController {

    private final AuditService auditService;

    public AdminAuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public Page<AuditLogResponse> list(
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String actorUsername,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {
        return auditService.listAdmin(action, actorUsername, from, to, pageable);
    }
}
