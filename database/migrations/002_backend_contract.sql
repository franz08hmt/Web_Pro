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
