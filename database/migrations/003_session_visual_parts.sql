-- Apply once after 002_backend_contract. Existing sessions and progress are preserved.
-- MySQL DDL implicitly commits; inspect schema_migrations before running.
CREATE TABLE session_visual_parts (
    session_id BIGINT UNSIGNED NOT NULL,
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,

    PRIMARY KEY (session_id, component_id),

    FOREIGN KEY (session_id, robot_id)
        REFERENCES assembly_sessions(id, robot_id)
        ON DELETE CASCADE,

    FOREIGN KEY (robot_id, component_id)
        REFERENCES robot_components(robot_id, component_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO schema_migrations (version)
VALUES ('003_session_visual_parts');
