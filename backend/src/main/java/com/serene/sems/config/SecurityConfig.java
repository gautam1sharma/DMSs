package com.serene.sems.config;

import com.serene.sems.config.properties.ApiProperties;
import com.serene.sems.security.JwtAuthFilter;
import com.serene.sems.service.AuditService;
import com.serene.sems.web.PortalHttpAuditFilter;
import com.serene.sems.web.ContentCachingRequestFilter;
import com.serene.sems.web.IdempotencyFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final ApiProperties apiProperties;
    private final ContentCachingRequestFilter contentCachingRequestFilter;
    private final IdempotencyFilter idempotencyFilter;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            UserDetailsService userDetailsService,
            ApiProperties apiProperties,
            ContentCachingRequestFilter contentCachingRequestFilter,
            IdempotencyFilter idempotencyFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.apiProperties = apiProperties;
        this.contentCachingRequestFilter = contentCachingRequestFilter;
        this.idempotencyFilter = idempotencyFilter;
    }

    /**
     * BCrypt's {@code matches} uses a constant-time path over the hash, reducing timing leaks on password checks.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, PasswordEncoder passwordEncoder, PortalHttpAuditFilter portalHttpAuditFilter)
            throws Exception {
        String base = apiProperties.getBasePath();
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, base + "/auth/login", base + "/auth/register")
                        .permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, base + "/products", base + "/products/**")
                        .hasAnyRole("ADMIN", "DEALER", "CUSTOMER")
                        .requestMatchers(base + "/admin/**").hasRole("ADMIN")
                        .requestMatchers(base + "/dealer/**").hasRole("DEALER")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider(passwordEncoder))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(contentCachingRequestFilter, JwtAuthFilter.class)
                .addFilterAfter(idempotencyFilter, JwtAuthFilter.class)
                // After authorization so the response status is finalized on the same response we observe,
                // and immediately before dispatch to controllers.
                .addFilterAfter(portalHttpAuditFilter, AuthorizationFilter.class);

        return http.build();
    }

    @Bean
    public PortalHttpAuditFilter portalHttpAuditFilter(AuditService auditService) {
        return new PortalHttpAuditFilter(auditService, apiProperties);
    }
}
