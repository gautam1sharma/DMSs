package com.serene.dms.controller;

import com.serene.dms.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
@Tag(name = "Inquiries", description = "Customer inquiry management")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER', 'CUSTOMER')")
    @Operation(summary = "Submit an inquiry")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inquiryService.createInquiry(req));
    }

    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    @Operation(summary = "List inquiries for a dealer")
    public ResponseEntity<Page<Map<String, Object>>> listByDealer(
        @PathVariable Long dealerId,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(inquiryService.getByDealer(dealerId, pageable));
    }

    @PatchMapping("/{id}/respond")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    @Operation(summary = "Respond to an inquiry")
    public ResponseEntity<Map<String, Object>> respond(
        @PathVariable Long id,
        @RequestBody Map<String, String> body
    ) {
        return ResponseEntity.ok(inquiryService.respond(id, body.get("response")));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEALER')")
    @Operation(summary = "Update inquiry status")
    public ResponseEntity<Map<String, Object>> updateStatus(
        @PathVariable Long id,
        @RequestParam String status
    ) {
        return ResponseEntity.ok(inquiryService.updateStatus(id, status));
    }
}
