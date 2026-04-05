package com.serene.sems.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Hibernate {@code ddl-auto: update} does not widen legacy {@code CHECK} constraints on {@code audit_logs.action}.
 * New {@link com.serene.sems.model.AuditAction} values then fail with {@code Check constraint violation}.
 * Drops {@code CHECK} constraints on {@code audit_logs} only.
 */
@Component
@Order(1)
public class AuditLogsCheckConstraintMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AuditLogsCheckConstraintMigration.class);

    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    public AuditLogsCheckConstraintMigration(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String url = environment.getProperty("spring.datasource.url", "");
        if (url.contains(":h2:")) {
            dropH2CheckConstraintsOnAuditLogs();
        } else if (url.contains("mysql") || url.contains("mariadb")) {
            dropMysqlCheckConstraintsOnAuditLogs();
        }
    }

    private void dropH2CheckConstraintsOnAuditLogs() {
        Set<String> names = new LinkedHashSet<>();
        for (String table : List.of("AUDIT_LOGS", "audit_logs")) {
            try {
                names.addAll(
                        jdbcTemplate.queryForList(
                                "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS "
                                        + "WHERE TABLE_SCHEMA = SCHEMA() AND TABLE_NAME = ? AND CONSTRAINT_TYPE = 'CHECK'",
                                String.class,
                                table));
            } catch (Exception e) {
                log.debug("H2 TABLE_CONSTRAINTS lookup failed for {}: {}", table, e.getMessage());
            }
        }
        if (names.isEmpty()) {
            names.add("CONSTRAINT_6");
        }
        for (String name : names) {
            try {
                jdbcTemplate.update("ALTER TABLE audit_logs DROP CONSTRAINT IF EXISTS \"" + name + "\"");
                log.info("Dropped H2 CHECK constraint \"{}\" on audit_logs (if it existed)", name);
            } catch (Exception e) {
                log.debug("Could not drop H2 constraint \"{}\": {}", name, e.getMessage());
            }
        }
    }

    private void dropMysqlCheckConstraintsOnAuditLogs() {
        try {
            List<String> names =
                    jdbcTemplate.queryForList(
                            "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS "
                                    + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'audit_logs' "
                                    + "AND CONSTRAINT_TYPE = 'CHECK'",
                            String.class);
            for (String raw : names) {
                String name = raw.replace("`", "");
                try {
                    jdbcTemplate.execute("ALTER TABLE audit_logs DROP CHECK `" + name + "`");
                    log.info("Dropped MySQL CHECK `{}` on audit_logs", name);
                } catch (Exception e) {
                    log.debug("Could not drop MySQL CHECK `{}`: {}", name, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("Could not list MySQL CHECK constraints on audit_logs: {}", e.getMessage());
        }
    }
}
