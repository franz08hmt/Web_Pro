-- Run once on an existing database before deploying the updated WAR.
-- Incremented when a password or role changes to invalidate old HttpSessions.
ALTER TABLE users ADD COLUMN session_version INT UNSIGNED NOT NULL DEFAULT 0 AFTER role;

INSERT INTO schema_migrations (version)
VALUES ('004_account_session_version');
