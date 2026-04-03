package com.adminplus.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 数据库健康检查
 *
 * @author AdminPlus
 * @since 2026-02-07
 */
@Slf4j
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("url", connection.getMetaData().getURL())
                        .withDetail("username", connection.getMetaData().getUserName())
                        .build();
            }
            log.error("Database health check failed: Connection not valid");
            return Health.down()
                    .withDetail("database", "Connection not valid")
                    .build();
        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("database", e.getMessage())
                    .build();
        }
    }
}