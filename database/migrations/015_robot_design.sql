-- Additive workshop; never change official assembly tables or published specs.
CREATE TABLE IF NOT EXISTS design_rule_sets (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    version_no INT NOT NULL UNIQUE CHECK (version_no > 0),
    state VARCHAR(12) NOT NULL CHECK (state IN ('DRAFT','PUBLISHED')),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS design_component_roles (
rule_set_id BIGINT UNSIGNED NOT NULL,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    role VARCHAR(24) NOT NULL CHECK (role IN ('BASE','CONTROLLER','MOTOR_DRIVER','DRIVE_MOTOR','WHEEL','SUPPORT_WHEEL','POWER','DISTANCE_SENSOR','LINE_SENSOR','SERVO')),
    driver_channels INT NULL CHECK (driver_channels IS NULL OR driver_channels > 0),
    PRIMARY KEY (rule_set_id, component_id),
    FOREIGN KEY (rule_set_id) REFERENCES design_rule_sets(id) ON DELETE RESTRICT,
    FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS design_mounts (
rule_set_id BIGINT UNSIGNED NOT NULL,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    mount_code VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    mount_type VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    label VARCHAR(160) NOT NULL,
    x DOUBLE NOT NULL CHECK (x BETWEEN -10 AND 10),
    y DOUBLE NOT NULL CHECK (y BETWEEN -10 AND 10),
    z DOUBLE NOT NULL CHECK (z BETWEEN -10 AND 10),
    rx DOUBLE NOT NULL DEFAULT 0 CHECK (rx BETWEEN -6.284 AND 6.284),
    ry DOUBLE NOT NULL DEFAULT 0 CHECK (ry BETWEEN -6.284 AND 6.284),
    rz DOUBLE NOT NULL DEFAULT 0 CHECK (rz BETWEEN -6.284 AND 6.284),
    display_order INT NOT NULL CHECK (display_order BETWEEN 1 AND 40),
    PRIMARY KEY (rule_set_id, component_id, mount_code),
    UNIQUE KEY uq_design_mount_type (rule_set_id, component_id, mount_code, mount_type),
    FOREIGN KEY (rule_set_id, component_id)
        REFERENCES design_component_roles(rule_set_id, component_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS design_fits (
rule_set_id BIGINT UNSIGNED NOT NULL,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    mount_type VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    orientation_code VARCHAR(4) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    PRIMARY KEY (rule_set_id, component_id, mount_type, orientation_code),
    CHECK (orientation_code IN ('Y0','Y90','Y180','Y270')),
    FOREIGN KEY (rule_set_id, component_id)
        REFERENCES design_component_roles(rule_set_id, component_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS design_goals (
code VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(600) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS design_goal_requirements (
rule_set_id BIGINT UNSIGNED NOT NULL,
    goal_code VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    role VARCHAR(24) NOT NULL CHECK (role IN ('BASE','CONTROLLER','MOTOR_DRIVER','DRIVE_MOTOR','WHEEL','SUPPORT_WHEEL','POWER','DISTANCE_SENSOR','LINE_SENSOR','SERVO')),
    min_count INT NOT NULL CHECK (min_count BETWEEN 1 AND 30),
    message VARCHAR(300) NOT NULL,
    PRIMARY KEY (rule_set_id, goal_code, role),
    FOREIGN KEY (rule_set_id) REFERENCES design_rule_sets(id) ON DELETE RESTRICT,
    FOREIGN KEY (goal_code) REFERENCES design_goals(code) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user_kit_items (
user_id BIGINT UNSIGNED NOT NULL,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    quantity INT NOT NULL CHECK (quantity BETWEEN 1 AND 10),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, component_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS robot_designs (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(80) NOT NULL,
    goal_code VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    rule_set_id BIGINT UNSIGNED NOT NULL,
    source VARCHAR(12) NOT NULL CHECK (source IN ('KIT','LIBRARY')),
    status VARCHAR(12) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','CHECKED')),
    layout_version INT NOT NULL DEFAULT 1 CHECK (layout_version > 0),
    last_check_summary VARCHAR(600) NULL,
    last_checked_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_design_rule (id, rule_set_id),
    KEY ix_design_owner (user_id, id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (goal_code) REFERENCES design_goals(code) ON DELETE RESTRICT,
    FOREIGN KEY (rule_set_id) REFERENCES design_rule_sets(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS design_instances (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    design_id BIGINT UNSIGNED NOT NULL,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    instance_no INT NOT NULL CHECK (instance_no BETWEEN 1 AND 10),
    UNIQUE KEY uq_design_instance (design_id, component_id, instance_no),
    UNIQUE KEY uq_design_instance_owner (id, design_id),
    UNIQUE KEY uq_design_instance_type (id, design_id, component_id),
    FOREIGN KEY (design_id) REFERENCES robot_designs(id) ON DELETE CASCADE,
    FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS design_placements (
design_id BIGINT UNSIGNED NOT NULL,
    instance_id BIGINT UNSIGNED PRIMARY KEY,
    host_instance_id BIGINT UNSIGNED NULL,
    rule_set_id BIGINT UNSIGNED NOT NULL,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    host_component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
    mount_code VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    mount_type VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    orientation_code VARCHAR(4) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    placed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    root_design_id BIGINT UNSIGNED GENERATED ALWAYS AS
        (CASE WHEN host_instance_id IS NULL THEN design_id ELSE NULL END) STORED,
    UNIQUE KEY uq_design_root (root_design_id),
    UNIQUE KEY uq_design_mount (design_id, host_instance_id, mount_code),
    CHECK (orientation_code IN ('Y0','Y90','Y180','Y270')),
    CHECK (host_instance_id IS NULL OR host_instance_id <> instance_id),
    CHECK ((host_instance_id IS NULL AND host_component_id IS NULL AND mount_code = 'base' AND mount_type = 'ROOT')
        OR (host_instance_id IS NOT NULL AND host_component_id IS NOT NULL AND mount_type <> 'ROOT')),
    FOREIGN KEY (design_id, rule_set_id) REFERENCES robot_designs(id, rule_set_id) ON DELETE CASCADE,
    FOREIGN KEY (instance_id, design_id, component_id)
        REFERENCES design_instances(id, design_id, component_id) ON DELETE CASCADE,
    FOREIGN KEY (host_instance_id, design_id, host_component_id)
        REFERENCES design_instances(id, design_id, component_id) ON DELETE CASCADE,
    FOREIGN KEY (rule_set_id, host_component_id, mount_code, mount_type)
        REFERENCES design_mounts(rule_set_id, component_id, mount_code, mount_type) ON DELETE RESTRICT,
    FOREIGN KEY (rule_set_id, component_id, mount_type, orientation_code)
        REFERENCES design_fits(rule_set_id, component_id, mount_type, orientation_code) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS design_events (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    design_id BIGINT UNSIGNED NOT NULL,
    action VARCHAR(12) NOT NULL CHECK (action IN ('ATTACH','DETACH','UNDO','CHECK')),
    outcome VARCHAR(12) NOT NULL CHECK (outcome IN ('ACCEPTED','REJECTED')),
    reason_code VARCHAR(64) NULL,
    instance_id BIGINT UNSIGNED NULL,
    host_instance_id BIGINT UNSIGNED NULL,
    mount_code VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
    orientation_code VARCHAR(4) CHARACTER SET ascii COLLATE ascii_bin NULL,
    prev_orientation_code VARCHAR(4) CHARACTER SET ascii COLLATE ascii_bin NULL,
    undo_of_event_id BIGINT UNSIGNED NULL,
    layout_version_after INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_design_event_owner (id, design_id),
    UNIQUE KEY uq_design_undo (undo_of_event_id),
    KEY ix_design_event (design_id, id),
    CHECK (orientation_code IS NULL OR orientation_code IN ('Y0','Y90','Y180','Y270')),
    CHECK (prev_orientation_code IS NULL OR prev_orientation_code IN ('Y0','Y90','Y180','Y270')),
    CHECK (layout_version_after > 0),
    CHECK ((outcome = 'ACCEPTED' AND reason_code IS NULL) OR (outcome = 'REJECTED' AND reason_code IS NOT NULL)),
    CHECK ((action = 'UNDO' AND outcome = 'ACCEPTED' AND undo_of_event_id IS NOT NULL)
        OR ((action <> 'UNDO' OR outcome <> 'ACCEPTED') AND undo_of_event_id IS NULL)),
    FOREIGN KEY (design_id) REFERENCES robot_designs(id) ON DELETE CASCADE,
    FOREIGN KEY (instance_id, design_id) REFERENCES design_instances(id, design_id) ON DELETE CASCADE,
    FOREIGN KEY (host_instance_id, design_id) REFERENCES design_instances(id, design_id) ON DELETE CASCADE,
    FOREIGN KEY (undo_of_event_id, design_id) REFERENCES design_events(id, design_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO schema_migrations (version) VALUES ('015_robot_design');
