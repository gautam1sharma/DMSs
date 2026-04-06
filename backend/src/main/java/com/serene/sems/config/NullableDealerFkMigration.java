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
 * Hibernate {@code ddl-auto: update} often does not change an existing column from NOT NULL to nullable.
 * Customers and orders may store {@code null} dealer_id after city-based unassignment or dealer removal.
 * Customer rows may keep order history after the portal user is removed ({@code customers.user_id} nullable).
 */
@Component
@Order
public class NullableDealerFkMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(NullableDealerFkMigration.class);

    private final DataSource dataSource;

    public NullableDealerFkMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        String url;
        try (Connection c = dataSource.getConnection()) {
            url = c.getMetaData().getURL();
        } catch (SQLException e) {
            log.warn("Could not open connection for dealer FK migration: {}", e.getMessage());
            return;
        }
        if (url.contains(":h2:") || url.contains("jdbc:h2:")) {
            relaxH2(jdbc);
        } else if (url.contains("mysql") || url.contains("mariadb")) {
            relaxMysql(jdbc);
        } else {
            relaxPostgresStyle(jdbc);
        }
    }

    private static void relaxH2(JdbcTemplate jdbc) {
        // Native H2 syntax
        execIgnore(jdbc, "ALTER TABLE customers ALTER COLUMN dealer_id DROP NOT NULL");
        execIgnore(jdbc, "ALTER TABLE customers ALTER COLUMN user_id DROP NOT NULL");
        execIgnore(jdbc, "ALTER TABLE orders ALTER COLUMN dealer_id DROP NOT NULL");
        // H2 MODE=MySQL often accepts MODIFY if ALTER COLUMN is unsupported for this build
        execIgnore(jdbc, "ALTER TABLE customers MODIFY dealer_id BIGINT NULL");
        execIgnore(jdbc, "ALTER TABLE customers MODIFY user_id BIGINT NULL");
        execIgnore(jdbc, "ALTER TABLE orders MODIFY dealer_id BIGINT NULL");
    }

    private static void relaxMysql(JdbcTemplate jdbc) {
        execIgnore(jdbc, "ALTER TABLE customers MODIFY COLUMN dealer_id BIGINT NULL");
        execIgnore(jdbc, "ALTER TABLE customers MODIFY COLUMN user_id BIGINT NULL");
        execIgnore(jdbc, "ALTER TABLE orders MODIFY COLUMN dealer_id BIGINT NULL");
    }

    private static void relaxPostgresStyle(JdbcTemplate jdbc) {
        execIgnore(jdbc, "ALTER TABLE customers ALTER COLUMN dealer_id DROP NOT NULL");
        execIgnore(jdbc, "ALTER TABLE customers ALTER COLUMN user_id DROP NOT NULL");
        execIgnore(jdbc, "ALTER TABLE orders ALTER COLUMN dealer_id DROP NOT NULL");
    }

    private static void execIgnore(JdbcTemplate jdbc, String sql) {
        try {
            jdbc.execute(sql);
            log.info("Applied schema fix: {}", sql);
        } catch (Exception e) {
            log.debug("Skipped or failed (may already be nullable): {} — {}", sql, e.getMessage());
        }
    }
}
