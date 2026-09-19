-- MySQL >= 8.0.16. Run ONCE against an empty, explicitly selected database.
-- DDL implicitly commits in MySQL. Do not wrap this migration in a transaction.

SET NAMES utf8mb4;

CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    display_name VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('user', 'admin') NOT NULL DEFAULT 'user',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE robots (
    id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    level ENUM('Cơ bản', 'Trung bình', 'Nâng cao') NOT NULL,
    summary TEXT NOT NULL,
    image VARCHAR(2048) NOT NULL,
    build_time VARCHAR(100) NOT NULL,
    main_sensor VARCHAR(255) NOT NULL,
    skills TEXT NOT NULL,
    wiring JSON NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE components (
    id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    image VARCHAR(2048) NOT NULL,
    description TEXT NOT NULL,
    specs JSON NOT NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE robot_components (
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    quantity INT NOT NULL CHECK (quantity BETWEEN 1 AND 10000),

    PRIMARY KEY (robot_id, component_id),

    FOREIGN KEY (robot_id)
        REFERENCES robots(id)
        ON DELETE RESTRICT,

    FOREIGN KEY (component_id)
        REFERENCES components(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE assembly_steps (
    id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    step_order INT NOT NULL CHECK (step_order BETWEEN 1 AND 10000),
    title VARCHAR(150) NOT NULL,
    instruction TEXT NOT NULL,
    illustration JSON NOT NULL,

    UNIQUE (robot_id, step_order),
    UNIQUE (robot_id, id),

    FOREIGN KEY (robot_id)
        REFERENCES robots(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE assembly_sessions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    status ENUM('in_progress', 'completed') NOT NULL DEFAULT 'in_progress',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE (id, robot_id),

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    FOREIGN KEY (robot_id)
        REFERENCES robots(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE session_components (
    session_id BIGINT UNSIGNED NOT NULL,
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    prepared BOOLEAN NOT NULL DEFAULT FALSE CHECK (prepared IN (0, 1)),

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


CREATE TABLE session_steps (
    session_id BIGINT UNSIGNED NOT NULL,
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    step_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE CHECK (completed IN (0, 1)),
    simulation_state JSON NULL,

    PRIMARY KEY (session_id, step_id),

    FOREIGN KEY (session_id, robot_id)
        REFERENCES assembly_sessions(id, robot_id)
        ON DELETE CASCADE,

    FOREIGN KEY (robot_id, step_id)
        REFERENCES assembly_steps(robot_id, id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE library_resources (
    id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
    title VARCHAR(150) NOT NULL,
    type ENUM('image', 'document', 'link') NOT NULL,
    url VARCHAR(2048) NOT NULL,
    description TEXT NOT NULL,

    FOREIGN KEY (robot_id)
        REFERENCES robots(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE schema_migrations (
    version VARCHAR(100) PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;


INSERT INTO schema_migrations (version)
VALUES ('001_initial');

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
