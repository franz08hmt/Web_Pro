-- Phòng nối dây: chỉ thêm bảng, không thay đổi dữ liệu/cấu trúc cũ.
CREATE TABLE IF NOT EXISTS wiring_exercises (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    objective TEXT NOT NULL,
    scope_text TEXT NOT NULL,
    state ENUM('DRAFT','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT UNSIGNED NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    INDEX(state, id),
    FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wiring_terminals (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    exercise_id BIGINT UNSIGNED NOT NULL,
    code VARCHAR(64) NOT NULL,
    device_code VARCHAR(64) NOT NULL,
    device_label VARCHAR(100) NOT NULL,
    pin_label VARCHAR(50) NOT NULL,
    display_order INT NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    UNIQUE (exercise_id, id),
    UNIQUE (exercise_id, code),
    CHECK (x BETWEEN 40 AND 960 AND y BETWEEN 40 AND 960),
    CHECK (display_order BETWEEN 0 AND 40),
    FOREIGN KEY (exercise_id) REFERENCES wiring_exercises(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wiring_rules (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    exercise_id BIGINT UNSIGNED NOT NULL,
    terminal_a BIGINT UNSIGNED NOT NULL,
    terminal_b BIGINT UNSIGNED NOT NULL,
    kind ENUM('REQUIRED','FORBIDDEN') NOT NULL,
    explanation TEXT NOT NULL,
    UNIQUE (exercise_id, terminal_a, terminal_b),
    CHECK (terminal_a < terminal_b),
    FOREIGN KEY (exercise_id) REFERENCES wiring_exercises(id) ON DELETE RESTRICT,
    FOREIGN KEY (exercise_id, terminal_a) REFERENCES wiring_terminals(exercise_id, id) ON DELETE RESTRICT,
    FOREIGN KEY (exercise_id, terminal_b) REFERENCES wiring_terminals(exercise_id, id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wiring_attempts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    exercise_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    state ENUM('DRAFT','SUBMITTED') NOT NULL DEFAULT 'DRAFT',
    version INT NOT NULL DEFAULT 1,
    required_count INT NULL,
    correct_count INT NULL,
    wrong_count INT NULL,
    missing_count INT NULL,
    score DECIMAL(4,1) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMP NULL,
    UNIQUE (id, exercise_id),
    INDEX (user_id, id),
    CHECK (version >= 1),
    CHECK ((state='DRAFT' AND score IS NULL AND required_count IS NULL AND correct_count IS NULL
        AND wrong_count IS NULL AND missing_count IS NULL AND submitted_at IS NULL)
        OR (state='SUBMITTED' AND score IS NOT NULL AND required_count IS NOT NULL
        AND correct_count IS NOT NULL AND wrong_count IS NOT NULL AND missing_count IS NOT NULL
        AND submitted_at IS NOT NULL AND score BETWEEN 0 AND 100 AND required_count > 0
        AND correct_count BETWEEN 0 AND required_count AND wrong_count BETWEEN 0 AND 80
        AND missing_count = required_count - correct_count)),
    FOREIGN KEY (exercise_id) REFERENCES wiring_exercises(id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wiring_attempt_connections (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT UNSIGNED NOT NULL,
    exercise_id BIGINT UNSIGNED NOT NULL,
    terminal_a BIGINT UNSIGNED NOT NULL,
    terminal_b BIGINT UNSIGNED NOT NULL,
    UNIQUE (attempt_id, terminal_a, terminal_b),
    CHECK (terminal_a < terminal_b),
    FOREIGN KEY (attempt_id, exercise_id) REFERENCES wiring_attempts(id, exercise_id) ON DELETE RESTRICT,
    FOREIGN KEY (exercise_id, terminal_a) REFERENCES wiring_terminals(exercise_id, id) ON DELETE RESTRICT,
    FOREIGN KEY (exercise_id, terminal_b) REFERENCES wiring_terminals(exercise_id, id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO schema_migrations(version) VALUES ('011_wiring_practice');
