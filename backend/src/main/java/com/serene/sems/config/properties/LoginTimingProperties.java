package com.serene.sems.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class LoginTimingProperties {

    /**
     * Minimum time spent in login path to reduce timing-based user enumeration (milliseconds).
     */
    private long loginMinDurationMs = 200;

    public long getLoginMinDurationMs() {
        return loginMinDurationMs;
    }

    public void setLoginMinDurationMs(long loginMinDurationMs) {
        this.loginMinDurationMs = loginMinDurationMs;
    }
}
