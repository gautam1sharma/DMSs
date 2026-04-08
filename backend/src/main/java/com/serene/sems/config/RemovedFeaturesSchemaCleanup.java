package com.serene.sems.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Drops tables left behind after removing idempotency and configurable menu features (entities removed).
 */
@Component
@Order
public class RemovedFeaturesSchemaCleanup implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RemovedFeaturesSchemaCleanup.class);

    private final DataSource dataSource;

    public RemovedFeaturesSchemaCleanup(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        String url;
        try (Connection c = dataSource.getConnection()) {
            url = c.getMetaData().getURL();
        } catch (SQLException e) {
            log.warn("Removed-features schema cleanup skipped: {}", e.getMessage());
            return;
        }
        if (url.contains(":h2:") || url.contains("jdbc:h2:")) {
            execIgnore(jdbc, "DROP TABLE IF EXISTS menu_item_roles");
            execIgnore(jdbc, "DROP TABLE IF EXISTS menu_items");
            execIgnore(jdbc, "DROP TABLE IF EXISTS idempotent_requests");
        } else if (url.contains("mysql") || url.contains("mariadb")) {
            execIgnore(jdbc, "DROP TABLE IF EXISTS menu_item_roles");
            execIgnore(jdbc, "DROP TABLE IF EXISTS menu_items");
            execIgnore(jdbc, "DROP TABLE IF EXISTS idempotent_requests");
        } else {
            execIgnore(jdbc, "DROP TABLE IF EXISTS menu_item_roles CASCADE");
            execIgnore(jdbc, "DROP TABLE IF EXISTS menu_items CASCADE");
            execIgnore(jdbc, "DROP TABLE IF EXISTS idempotent_requests CASCADE");
        }
    }

    private static void execIgnore(JdbcTemplate jdbc, String sql) {
        try {
            jdbc.execute(sql);
            log.info("Removed-features cleanup: {}", sql);
        } catch (Exception e) {
            log.debug("Skipped or failed: {} — {}", sql, e.getMessage());
        }
    }
}
