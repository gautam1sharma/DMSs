package com.serene.sems.security;

/**
 * Request-scoped copy of the authenticated principal for HTTP audit (survives if {@code SecurityContext} is cleared
 * before the audit filter's {@code finally} runs).
 */
public final class RequestAuditAttributes {

    public static final String USERNAME = "com.serene.sems.requestAudit.username";

    /** Set of {@code GrantedAuthority#getAuthority()} strings, e.g. {@code ROLE_DEALER}. */
    public static final String ROLES = "com.serene.sems.requestAudit.roles";

    private RequestAuditAttributes() {
    }
}
