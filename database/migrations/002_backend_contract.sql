-- Apply after 001_initial. Existing progress is preserved.
-- MySQL DDL implicitly commits; run once and inspect state if interrupted.
CREATE TABLE auth_sessions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    token_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL UNIQUE,
    expires_at DATETIME(3) NOT NULL,
    revoked_at DATETIME(3) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    INDEX idx_auth_sessions_expiry (expires_at),

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


-- Normalize old status values before changing the ENUM.
ALTER TABLE assembly_sessions
    MODIFY status VARCHAR(20) NOT NULL DEFAULT 'PREPARING';

UPDATE assembly_sessions
SET status = UPPER(status);

ALTER TABLE assembly_sessions
    MODIFY status ENUM(
        'PREPARING',
        'READY',
        'IN_PROGRESS',
        'COMPLETED',
        'ABANDONED'
    ) NOT NULL DEFAULT 'PREPARING',

    ADD INDEX idx_sessions_user_status_created (
        user_id,
        status,
        created_at
    ),

    ADD INDEX idx_sessions_user_robot_created (
        user_id,
        robot_id,
        created_at
    );


ALTER TABLE session_components
    ADD COLUMN updated_at TIMESTAMP(3)
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3);


ALTER TABLE session_steps
    ADD COLUMN updated_at TIMESTAMP(3)
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3);


INSERT INTO schema_migrations (version)
VALUES ('002_backend_contract');
