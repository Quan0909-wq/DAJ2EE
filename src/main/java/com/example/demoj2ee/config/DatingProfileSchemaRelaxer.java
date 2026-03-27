package com.example.demoj2ee.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Hibernate ddl-auto=update không luôn bỏ NOT NULL trên MySQL/MariaDB khi đổi entity.
 * Hồ sơ dating mới cần INSERT với nhiều cột null → ALTER một lần khi chạy app.
 */
@Component
@Order(Integer.MAX_VALUE)
public class DatingProfileSchemaRelaxer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatingProfileSchemaRelaxer.class);

    private final JdbcTemplate jdbcTemplate;

    public DatingProfileSchemaRelaxer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        relax("ALTER TABLE dating_profiles MODIFY COLUMN display_name VARCHAR(255) NULL");
        relax("ALTER TABLE dating_profiles MODIFY COLUMN age INT NULL");
        relax("ALTER TABLE dating_profiles MODIFY COLUMN height DOUBLE NULL");
        relax("ALTER TABLE dating_profiles MODIFY COLUMN hometown VARCHAR(255) NULL");
        relax("ALTER TABLE dating_profiles MODIFY COLUMN marital_status VARCHAR(255) NULL");
        relax("ALTER TABLE dating_profiles MODIFY COLUMN avatar_url LONGTEXT NULL");
    }

    private void relax(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            log.debug("Skip dating_profiles relax ({}): {}", sql, e.getMessage());
        }
    }
}
