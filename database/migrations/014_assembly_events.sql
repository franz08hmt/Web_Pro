-- Additive journal; keep published specs and old session results unchanged.
CREATE TABLE IF NOT EXISTS assembly_events (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT UNSIGNED NOT NULL,
    spec_id BIGINT UNSIGNED NULL,
    action VARCHAR(12) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    outcome VARCHAR(10) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    reason_code VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
    instance_id BIGINT UNSIGNED NULL,
    slot_id BIGINT UNSIGNED NULL,
    orientation_code VARCHAR(4) CHARACTER SET ascii COLLATE ascii_bin NULL,
    prev_orientation_code VARCHAR(4) CHARACTER SET ascii COLLATE ascii_bin NULL,
    undo_of_event_id BIGINT UNSIGNED NULL,
    layout_version_after INT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_event_session (id, session_id),
    UNIQUE KEY uq_event_undo (undo_of_event_id),
    KEY ix_event_session (session_id, id),
    FOREIGN KEY (session_id) REFERENCES assembly_sessions(id) ON DELETE RESTRICT,
    FOREIGN KEY (session_id, spec_id) REFERENCES assembly_sessions(id, spec_id) ON DELETE RESTRICT,
    FOREIGN KEY (instance_id, session_id) REFERENCES assembly_instances(id, session_id) ON DELETE RESTRICT,
    FOREIGN KEY (slot_id, spec_id) REFERENCES assembly_slots(id, spec_id) ON DELETE RESTRICT,
    FOREIGN KEY (undo_of_event_id, session_id) REFERENCES assembly_events(id, session_id) ON DELETE RESTRICT,
    CHECK (action IN ('ATTACH','DETACH','RESET','COMPLETE','UNDO')),
    CHECK (outcome IN ('ACCEPTED','REJECTED')),
    CHECK (orientation_code IS NULL OR orientation_code IN ('Y0','Y90','Y180','Y270')),
    CHECK (prev_orientation_code IS NULL OR prev_orientation_code IN ('Y0','Y90','Y180','Y270')),
    CHECK (layout_version_after IS NULL OR layout_version_after >= 1),
    CHECK ((outcome = 'ACCEPTED' AND reason_code IS NULL)
        OR (outcome = 'REJECTED' AND reason_code IS NOT NULL)),
    CHECK ((action = 'UNDO' AND outcome = 'ACCEPTED' AND undo_of_event_id IS NOT NULL)
        OR ((action <> 'UNDO' OR outcome <> 'ACCEPTED') AND undo_of_event_id IS NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO schema_migrations (version) VALUES ('014_assembly_events');
