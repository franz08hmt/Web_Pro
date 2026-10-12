-- Additive: preserve every GROUP session and existing progress/evidence.
-- Run once after inspecting schema_migrations, with explicit DDL approval.
CREATE TABLE IF NOT EXISTS assembly_specs (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    version_no INT NOT NULL,
    state ENUM('PUBLISHED','ARCHIVED') NOT NULL,
    published_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_assembly_spec_version (robot_id, version_no),
    UNIQUE KEY uq_assembly_spec_robot (id, robot_id),
    CONSTRAINT fk_assembly_spec_robot FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE RESTRICT,
    CHECK (version_no >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS assembly_slots (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    spec_id BIGINT UNSIGNED NOT NULL,
    slot_code VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    parent_slot_id BIGINT UNSIGNED NULL,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    label VARCHAR(150) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    x DECIMAL(8,4) NOT NULL,
    y DECIMAL(8,4) NOT NULL,
    z DECIMAL(8,4) NOT NULL,
    rx DECIMAL(8,4) NOT NULL DEFAULT 0,
    ry DECIMAL(8,4) NOT NULL DEFAULT 0,
    rz DECIMAL(8,4) NOT NULL DEFAULT 0,
    display_order INT NOT NULL,
    UNIQUE KEY uq_assembly_slot_code (spec_id, slot_code),
    UNIQUE KEY uq_assembly_slot_spec (id, spec_id),
    UNIQUE KEY uq_assembly_slot_component (id, spec_id, component_id),
    FOREIGN KEY (spec_id) REFERENCES assembly_specs(id) ON DELETE RESTRICT,
    FOREIGN KEY (parent_slot_id, spec_id) REFERENCES assembly_slots(id, spec_id) ON DELETE RESTRICT,
    FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE RESTRICT,
    CHECK (x BETWEEN -10 AND 10 AND y BETWEEN -10 AND 10 AND z BETWEEN -10 AND 10),
    CHECK (rx BETWEEN -6.284 AND 6.284 AND ry BETWEEN -6.284 AND 6.284 AND rz BETWEEN -6.284 AND 6.284),
    CHECK (display_order BETWEEN 1 AND 40)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS assembly_slot_orientations (
    slot_id BIGINT UNSIGNED NOT NULL,
    orientation_code VARCHAR(4) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    PRIMARY KEY (slot_id, orientation_code),
    FOREIGN KEY (slot_id) REFERENCES assembly_slots(id) ON DELETE RESTRICT,
    CHECK (orientation_code IN ('Y0','Y90','Y180','Y270'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE assembly_sessions
    ADD COLUMN assembly_mode ENUM('GROUP','DETAILED') NOT NULL DEFAULT 'GROUP',
    ADD COLUMN spec_id BIGINT UNSIGNED NULL,
    ADD COLUMN layout_version INT NOT NULL DEFAULT 1,
    ADD UNIQUE KEY uq_session_spec (id, spec_id),
    ADD CONSTRAINT fk_session_assembly_spec FOREIGN KEY (spec_id, robot_id)
        REFERENCES assembly_specs(id, robot_id) ON DELETE RESTRICT,
    ADD CONSTRAINT chk_session_mode_spec CHECK (
        (assembly_mode = 'GROUP' AND spec_id IS NULL) OR (assembly_mode = 'DETAILED' AND spec_id IS NOT NULL)),
    ADD CONSTRAINT chk_layout_version CHECK (layout_version >= 1);

CREATE TABLE IF NOT EXISTS assembly_instances (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT UNSIGNED NOT NULL,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    instance_no INT NOT NULL,
    UNIQUE KEY uq_instance_number (session_id, component_id, instance_no),
    UNIQUE KEY uq_instance_session (id, session_id),
    UNIQUE KEY uq_instance_component (id, session_id, component_id),
    FOREIGN KEY (session_id) REFERENCES assembly_sessions(id) ON DELETE RESTRICT,
    FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE RESTRICT,
    CHECK (instance_no BETWEEN 1 AND 40)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS assembly_placements (
    instance_id BIGINT UNSIGNED PRIMARY KEY,
    session_id BIGINT UNSIGNED NOT NULL,
    spec_id BIGINT UNSIGNED NOT NULL,
    slot_id BIGINT UNSIGNED NOT NULL,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    orientation_code VARCHAR(4) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    placed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_placement_session_slot (session_id, slot_id),
    FOREIGN KEY (instance_id, session_id, component_id)
        REFERENCES assembly_instances(id, session_id, component_id) ON DELETE RESTRICT,
    FOREIGN KEY (session_id, spec_id) REFERENCES assembly_sessions(id, spec_id) ON DELETE RESTRICT,
    FOREIGN KEY (slot_id, spec_id, component_id)
        REFERENCES assembly_slots(id, spec_id, component_id) ON DELETE RESTRICT,
    FOREIGN KEY (slot_id, orientation_code)
        REFERENCES assembly_slot_orientations(slot_id, orientation_code) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO schema_migrations (version) VALUES ('013_assembly_placement');
