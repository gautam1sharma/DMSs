package com.serene.sems.web;

import com.serene.sems.config.properties.ApiProperties;
import com.serene.sems.config.properties.IdempotencyProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * Wraps POST requests that carry an {@code Idempotency-Key} so the body can be read twice
 * (filter + controller) without losing bytes.
 */
@Component
public class ContentCachingRequestFilter extends OncePerRequestFilter {

    public static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final IdempotencyProperties idempotencyProperties;
    private final ApiProperties apiProperties;

    public ContentCachingRequestFilter(IdempotencyProperties idempotencyProperties, ApiProperties apiProperties) {
        this.idempotencyProperties = idempotencyProperties;
        this.apiProperties = apiProperties;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest reqToUse = request;
        if (shouldWrap(request) && !(request instanceof ContentCachingRequestWrapper)) {
            reqToUse = new ContentCachingRequestWrapper(request);
        }
        filterChain.doFilter(reqToUse, response);
    }

    private boolean shouldWrap(HttpServletRequest request) {
        if (!idempotencyProperties.isEnabled()
                || !"POST".equalsIgnoreCase(request.getMethod())
                || !StringUtils.hasText(request.getHeader(IDEMPOTENCY_HEADER))) {
            return false;
        }
        if (request.getRequestURI().contains("/auth/login")) {
            return false;
        }
        String base = apiProperties.getBasePath();
        return request.getRequestURI().startsWith(base);
    }
}
