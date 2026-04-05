package com.serene.sems.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.api")
public class ApiProperties {

    /**
     * Versioned REST base path (e.g. {@code /api/v1}).
     */
    private String basePath = "/api/v1";

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
