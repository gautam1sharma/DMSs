package com.serene.sems.web;

import com.serene.sems.config.properties.ApiProperties;
import com.serene.sems.model.AuditAction;
import com.serene.sems.security.RequestAuditAttributes;
import com.serene.sems.service.AuditService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Records HTTP method, path, and status for authenticated {@code /admin/**} (admin role) and {@code /dealer/**}
 * (dealer role) API calls. Uses {@link RequestAuditAttributes} when {@link SecurityContextHolder} was already cleared.
 */
public class PortalHttpAuditFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(PortalHttpAuditFilter.class);

    private final AuditService auditService;
    private final String adminApiPrefix;
    private final String dealerApiPrefix;
    private final String auditLogsPath;

    public PortalHttpAuditFilter(AuditService auditService, ApiProperties apiProperties) {
        this.auditService = auditService;
        String base = apiProperties.getBasePath();
        this.adminApiPrefix = base + "/admin";
        this.dealerApiPrefix = base + "/dealer";
        this.auditLogsPath = base + "/admin/audit-logs";
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            try {
                maybeRecord(request, response);
            } catch (Exception e) {
                log.warn("Portal HTTP audit failed for {} {}", request.getMethod(), request.getRequestURI(), e);
            }
        }
    }

    private void maybeRecord(HttpServletRequest request, HttpServletResponse response) {
        if (request.getDispatcherType() != DispatcherType.REQUEST) {
            return;
        }
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return;
        }
        String path = pathWithoutContextPath(request);
        String username = resolveUsername(request);
        if (username == null) {
            return;
        }

        boolean admin = hasRole(request, "ROLE_ADMIN");
        boolean dealer = hasRole(request, "ROLE_DEALER");

        AuditAction action = null;
        if (path.startsWith(adminApiPrefix) && admin) {
            if ("GET".equalsIgnoreCase(method) && path.equals(auditLogsPath)) {
                return;
            }
            action = AuditAction.ADMIN_API_REQUEST;
        } else if (path.startsWith(dealerApiPrefix) && dealer) {
            action = AuditAction.DEALER_API_REQUEST;
        }

        if (action == null) {
            return;
        }

        int status = response.getStatus();
        if (status == 0) {
            status = HttpServletResponse.SC_OK;
        }
        auditService.recordPortalHttpRequest(action, method, path, status, username);
    }

    private static String resolveUsername(HttpServletRequest request) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken)) {
            return a.getName();
        }
        Object u = request.getAttribute(RequestAuditAttributes.USERNAME);
        return u instanceof String s && !s.isBlank() ? s : null;
    }

    private static boolean hasRole(HttpServletRequest request, String role) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken)) {
            for (GrantedAuthority ga : a.getAuthorities()) {
                if (role.equals(ga.getAuthority())) {
                    return true;
                }
            }
        }
        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) request.getAttribute(RequestAuditAttributes.ROLES);
        return roles != null && roles.contains(role);
    }

    /** Servlet path for matching API prefixes (strips {@code contextPath} from {@code getRequestURI()}). */
    private static String pathWithoutContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());
        }
        if (uri.isEmpty()) {
            uri = "/";
        }
        return stripTrailingSlash(uri);
    }

    private static String stripTrailingSlash(String path) {
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}
