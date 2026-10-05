-- Chặng 2: bản chụp server và tin nhắn bất biến; không sửa bảng/lượt cũ.
CREATE TABLE IF NOT EXISTS wiring_support_requests (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT UNSIGNED NOT NULL,
    exercise_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    state ENUM('OPEN','ANSWERED','CLOSED') NOT NULL DEFAULT 'OPEN',
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP NULL,
    active_attempt_id BIGINT UNSIGNED GENERATED ALWAYS AS
        (CASE WHEN state IN ('OPEN','ANSWERED') THEN attempt_id ELSE NULL END) STORED,
    UNIQUE (active_attempt_id),
    UNIQUE (id, exercise_id),
    INDEX (user_id, id),
    INDEX (state, id),
    CHECK (version >= 1),
    CHECK ((state='CLOSED' AND closed_at IS NOT NULL) OR (state<>'CLOSED' AND closed_at IS NULL)),
    FOREIGN KEY (attempt_id, exercise_id) REFERENCES wiring_attempts(id, exercise_id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wiring_support_messages (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT UNSIGNED NOT NULL,
    exercise_id BIGINT UNSIGNED NOT NULL,
    author_id BIGINT UNSIGNED NOT NULL,
    is_admin BOOLEAN NOT NULL,
    content TEXT NOT NULL,
    terminal_id BIGINT UNSIGNED NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    snapshot_text MEDIUMTEXT NULL,
    snapshot_pairs VARCHAR(4000) NULL,
    snapshot_version INT NULL,
    snapshot_state ENUM('DRAFT','SUBMITTED') NULL,
    snapshot_score DECIMAL(4,1) NULL,
    snapshot_at TIMESTAMP NULL,
    INDEX (request_id, id),
    CHECK (CHAR_LENGTH(content) BETWEEN 10 AND 2000 AND (is_admin OR CHAR_LENGTH(content) >= 20)),
    CHECK ((snapshot_text IS NULL AND snapshot_pairs IS NULL AND snapshot_version IS NULL
        AND snapshot_state IS NULL AND snapshot_score IS NULL AND snapshot_at IS NULL)
        OR (snapshot_text IS NOT NULL AND snapshot_pairs IS NOT NULL AND snapshot_version IS NOT NULL
        AND snapshot_version >= 1
        AND snapshot_at IS NOT NULL AND snapshot_state IS NOT NULL AND ((snapshot_state='DRAFT' AND snapshot_score IS NULL)
        OR (snapshot_state='SUBMITTED' AND snapshot_score IS NOT NULL AND snapshot_score BETWEEN 0 AND 100)))),
    FOREIGN KEY (request_id, exercise_id) REFERENCES wiring_support_requests(id, exercise_id) ON DELETE RESTRICT,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (exercise_id, terminal_id) REFERENCES wiring_terminals(exercise_id, id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO schema_migrations(version) VALUES ('012_wiring_support');
