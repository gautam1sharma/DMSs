package com.serene.sems.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.serene.sems.config.properties.ApiProperties;
import com.serene.sems.config.properties.IdempotencyProperties;
import com.serene.sems.exception.ErrorResponse;
import com.serene.sems.model.IdempotentRequest;
import com.serene.sems.service.IdempotentRequestService;
import com.serene.sems.util.HashUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Replays prior successful POST responses when the same {@code Idempotency-Key} is reused
 * for the same principal and path with the same body hash.
 */
@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyProperties idempotencyProperties;
    private final ApiProperties apiProperties;
    private final IdempotentRequestService idempotentRequestService;
    private final ObjectMapper objectMapper;

    public IdempotencyFilter(
            IdempotencyProperties idempotencyProperties,
            ApiProperties apiProperties,
            IdempotentRequestService idempotentRequestService,
            ObjectMapper objectMapper) {
        this.idempotencyProperties = idempotencyProperties;
        this.apiProperties = apiProperties;
        this.idempotentRequestService = idempotentRequestService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!shouldHandle(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper cr = (ContentCachingRequestWrapper) request;
        byte[] body = StreamUtils.copyToByteArray(cr.getInputStream());
        if (body.length > idempotencyProperties.getMaxBodyBytes()) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader(ContentCachingRequestFilter.IDEMPOTENCY_HEADER).trim();
        if (idempotencyKey.length() > 255) {
            filterChain.doFilter(request, response);
            return;
        }

        String bodyHash = HashUtils.sha256Hex(body);
        String pathFingerprint = HashUtils.sha256Hex(request.getMethod() + "\n" + request.getRequestURI());
        String principal = resolvePrincipal();

        Optional<IdempotentRequest> existing =
                idempotentRequestService.find(idempotencyKey, principal, pathFingerprint);
        if (existing.isPresent()) {
            if (!existing.get().getRequestBodyHash().equals(bodyHash)) {
                writeJson(
                        response,
                        HttpServletResponse.SC_CONFLICT,
                        ErrorResponse.of(
                                HttpServletResponse.SC_CONFLICT,
                                "Idempotency-Key reused with a different request body",
                                request.getRequestURI(),
                                null));
                return;
            }
            replay(response, existing.get());
            return;
        }

        ContentCachingResponseWrapper wrapped = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrapped);

        int status = wrapped.getStatus();
        byte[] respBody = wrapped.getContentAsByteArray();
        if (status >= 200 && status < 300 && respBody.length > 0) {
            String ct = wrapped.getContentType() != null ? wrapped.getContentType() : MediaType.APPLICATION_JSON_VALUE;
            idempotentRequestService.saveReplay(
                    idempotencyKey, principal, pathFingerprint, bodyHash, status, ct, respBody);
        }
        wrapped.copyBodyToResponse();
    }

    private boolean shouldHandle(HttpServletRequest request) {
        if (!idempotencyProperties.isEnabled() || !"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String key = request.getHeader(ContentCachingRequestFilter.IDEMPOTENCY_HEADER);
        if (!StringUtils.hasText(key)) {
            return false;
        }
        if (request.getRequestURI().contains("/auth/login")) {
            return false;
        }
        if (!request.getRequestURI().startsWith(apiProperties.getBasePath())) {
            return false;
        }
        return request instanceof ContentCachingRequestWrapper;
    }

    private static String resolvePrincipal() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }
        return a.getName();
    }

    private void replay(HttpServletResponse response, IdempotentRequest record) throws IOException {
        response.setStatus(record.getHttpStatus());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String ct = record.getContentType();
        response.setContentType(ct != null ? ct : MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().write(record.getResponseBody());
    }

    private void writeJson(HttpServletResponse response, int status, ErrorResponse body) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
