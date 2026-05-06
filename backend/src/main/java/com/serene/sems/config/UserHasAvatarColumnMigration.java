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
 * Ensures {@code users.has_avatar} is safe for legacy rows: relax NOT NULL if Hibernate added it too
 * strictly, then backfill nulls so login {@code save(user)} never hits constraint errors.
 */
@Component
@Order
public class UserHasAvatarColumnMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserHasAvatarColumnMigration.class);

    private final DataSource dataSource;

    public UserHasAvatarColumnMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        String url;
        try (Connection c = dataSource.getConnection()) {
            url = c.getMetaData().getURL();
        } catch (SQLException e) {
            log.warn("User has_avatar migration skipped: {}", e.getMessage());
            return;
        }

        if (url.contains(":h2:") || url.contains("jdbc:h2:")) {
            execIgnore(jdbc, "ALTER TABLE users ALTER COLUMN has_avatar DROP NOT NULL");
        } else if (url.contains("mysql") || url.contains("mariadb")) {
            execIgnore(jdbc, "ALTER TABLE users MODIFY COLUMN has_avatar TINYINT(1) NULL");
            execIgnore(jdbc, "ALTER TABLE users MODIFY COLUMN has_avatar BIT(1) NULL");
        } else {
            execIgnore(jdbc, "ALTER TABLE users ALTER COLUMN has_avatar DROP NOT NULL");
        }

        try {
            int n = jdbc.update("UPDATE users SET has_avatar = FALSE WHERE has_avatar IS NULL");
            if (n > 0) {
                log.info("Backfilled has_avatar for {} user row(s)", n);
            }
        } catch (Exception e) {
            log.debug("has_avatar backfill skipped (column may not exist yet): {}", e.getMessage());
        }
    }

    private static void execIgnore(JdbcTemplate jdbc, String sql) {
        try {
            jdbc.execute(sql);
            log.debug("Applied: {}", sql);
        } catch (Exception e) {
            log.debug("Skipped: {} — {}", sql, e.getMessage());
        }
    }
}
