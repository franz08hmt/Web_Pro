-- Đợt 6 chặng 2: bổ sung chẩn đoán và mẫu B; chạy lại an toàn.
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS diagnosis_scenarios (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    title VARCHAR(150) NOT NULL,
    context_text TEXT NOT NULL,
    symptom_text TEXT NOT NULL,
    guide_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
    explanation_text TEXT NOT NULL,
    state ENUM('DRAFT','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    version_no INT NOT NULL DEFAULT 1 CHECK (version_no > 0),
    parent_scenario_id BIGINT UNSIGNED NULL,
    created_by BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL,
    published_at DATETIME NULL,
    INDEX idx_diagnosis_catalog (state, robot_id),
    FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE RESTRICT,
    FOREIGN KEY (guide_id) REFERENCES troubleshooting_guides(id) ON DELETE RESTRICT,
    FOREIGN KEY (parent_scenario_id) REFERENCES diagnosis_scenarios(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS diagnosis_checks (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    scenario_id BIGINT UNSIGNED NOT NULL,
    label VARCHAR(200) NOT NULL,
    observation_text TEXT NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE CHECK (is_required IN (0,1)),
    display_order INT NOT NULL CHECK (display_order BETWEEN 1 AND 4),
    UNIQUE (scenario_id, display_order),
    FOREIGN KEY (scenario_id) REFERENCES diagnosis_scenarios(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS diagnosis_options (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    scenario_id BIGINT UNSIGNED NOT NULL,
    kind ENUM('CAUSE','ACTION') NOT NULL,
    label VARCHAR(200) NOT NULL,
    feedback_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE CHECK (is_correct IN (0,1)),
    display_order INT NOT NULL CHECK (display_order BETWEEN 1 AND 4),
    UNIQUE (scenario_id, kind, display_order),
    FOREIGN KEY (scenario_id) REFERENCES diagnosis_scenarios(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS diagnosis_attempts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    scenario_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    mode ENUM('PRACTICE','TASK') NOT NULL,
    state ENUM('IN_PROGRESS','SUBMITTED') NOT NULL DEFAULT 'IN_PROGRESS',
    started_at DATETIME NOT NULL,
    submitted_at DATETIME NULL,
    cause_option_id BIGINT UNSIGNED NULL,
    action_option_id BIGINT UNSIGNED NULL,
    required_done INT NULL,
    required_total INT NULL,
    cause_correct BOOLEAN NULL,
    action_correct BOOLEAN NULL,
    CHECK (required_total IS NULL OR (required_total > 0 AND required_done BETWEEN 0 AND required_total)),
    CHECK (cause_correct IS NULL OR cause_correct IN (0,1)),
    CHECK (action_correct IS NULL OR action_correct IN (0,1)),
    INDEX idx_diagnosis_owner (user_id, state, id),
    INDEX idx_diagnosis_scenario (scenario_id, mode),
    FOREIGN KEY (scenario_id) REFERENCES diagnosis_scenarios(id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (cause_option_id) REFERENCES diagnosis_options(id) ON DELETE RESTRICT,
    FOREIGN KEY (action_option_id) REFERENCES diagnosis_options(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS diagnosis_attempt_checks (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT UNSIGNED NOT NULL,
    check_id BIGINT UNSIGNED NOT NULL,
    chosen_at DATETIME NOT NULL,
    UNIQUE (attempt_id, check_id),
    FOREIGN KEY (attempt_id) REFERENCES diagnosis_attempts(id) ON DELETE RESTRICT,
    FOREIGN KEY (check_id) REFERENCES diagnosis_checks(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='practice_tasks' AND COLUMN_NAME='diagnosis_scenario_id'), 'SELECT 1', 'ALTER TABLE practice_tasks ADD COLUMN diagnosis_scenario_id BIGINT UNSIGNED NULL');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='task_rounds' AND COLUMN_NAME='diagnosis_attempt_id'), 'SELECT 1', 'ALTER TABLE task_rounds ADD COLUMN diagnosis_attempt_id BIGINT UNSIGNED NULL UNIQUE');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='task_submissions' AND COLUMN_NAME='diagnosis_attempt_id'), 'SELECT 1', 'ALTER TABLE task_submissions ADD COLUMN diagnosis_attempt_id BIGINT UNSIGNED NULL');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='task_submissions' AND COLUMN_NAME='diagnosis_title'), 'SELECT 1', 'ALTER TABLE task_submissions ADD COLUMN diagnosis_title VARCHAR(150) NULL');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='task_submissions' AND COLUMN_NAME='diag_required_done'), 'SELECT 1', 'ALTER TABLE task_submissions ADD COLUMN diag_required_done INT NULL');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='task_submissions' AND COLUMN_NAME='diag_required_total'), 'SELECT 1', 'ALTER TABLE task_submissions ADD COLUMN diag_required_total INT NULL');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='task_submissions' AND COLUMN_NAME='diag_cause_correct'), 'SELECT 1', 'ALTER TABLE task_submissions ADD COLUMN diag_cause_correct BOOLEAN NULL');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='task_submissions' AND COLUMN_NAME='diag_action_correct'), 'SELECT 1', 'ALTER TABLE task_submissions ADD COLUMN diag_action_correct BOOLEAN NULL');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='task_submissions' AND COLUMN_NAME='diagnosis_score'), 'SELECT 1', 'ALTER TABLE task_submissions ADD COLUMN diagnosis_score DECIMAL(4,1) NULL');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='task_submissions' AND COLUMN_NAME='diagnosis_points'), 'SELECT 1', 'ALTER TABLE task_submissions ADD COLUMN diagnosis_points DECIMAL(4,1) NULL');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='task_submissions' AND COLUMN_NAME='diagnosis_reused_from'), 'SELECT 1', 'ALTER TABLE task_submissions ADD COLUMN diagnosis_reused_from INT NOT NULL DEFAULT 0');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
ALTER TABLE practice_tasks MODIFY rubric_template ENUM('A','B') NOT NULL DEFAULT 'A';
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='practice_tasks' AND CONSTRAINT_NAME='fk_task_diagnosis'), 'SELECT 1', 'ALTER TABLE practice_tasks ADD CONSTRAINT fk_task_diagnosis FOREIGN KEY (diagnosis_scenario_id) REFERENCES diagnosis_scenarios(id) ON DELETE RESTRICT');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='task_rounds' AND CONSTRAINT_NAME='fk_round_diagnosis'), 'SELECT 1', 'ALTER TABLE task_rounds ADD CONSTRAINT fk_round_diagnosis FOREIGN KEY (diagnosis_attempt_id) REFERENCES diagnosis_attempts(id) ON DELETE RESTRICT');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='task_submissions' AND CONSTRAINT_NAME='fk_submission_diagnosis'), 'SELECT 1', 'ALTER TABLE task_submissions ADD CONSTRAINT fk_submission_diagnosis FOREIGN KEY (diagnosis_attempt_id) REFERENCES diagnosis_attempts(id) ON DELETE RESTRICT');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
-- Tra tên CHECK thực tế do MySQL tạo, không giả định số thứ tự.
SET @old_check = (
    SELECT cc.CONSTRAINT_NAME FROM information_schema.CHECK_CONSTRAINTS cc
    JOIN information_schema.TABLE_CONSTRAINTS tc
      ON tc.CONSTRAINT_SCHEMA=cc.CONSTRAINT_SCHEMA AND tc.CONSTRAINT_NAME=cc.CONSTRAINT_NAME
    WHERE tc.TABLE_SCHEMA=DATABASE() AND tc.TABLE_NAME='task_submissions'
      AND cc.CHECK_CLAUSE LIKE '%automatic_points%' AND cc.CONSTRAINT_NAME <> 'chk_submission_auto_b' LIMIT 1
);
SET @ddl = IF(@old_check IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE task_submissions DROP CHECK `', REPLACE(@old_check, '`', '``'), '`'));
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='task_submissions' AND CONSTRAINT_NAME='chk_submission_auto_b'), 'SELECT 1', 'ALTER TABLE task_submissions ADD CONSTRAINT chk_submission_auto_b CHECK (automatic_points BETWEEN 30 AND 80)');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
-- Tra tên CHECK thực tế do MySQL tạo, không giả định số thứ tự.
SET @old_check = (
    SELECT cc.CONSTRAINT_NAME FROM information_schema.CHECK_CONSTRAINTS cc
    JOIN information_schema.TABLE_CONSTRAINTS tc
      ON tc.CONSTRAINT_SCHEMA=cc.CONSTRAINT_SCHEMA AND tc.CONSTRAINT_NAME=cc.CONSTRAINT_NAME
    WHERE tc.TABLE_SCHEMA=DATABASE() AND tc.TABLE_NAME='task_reviews'
      AND cc.CHECK_CLAUSE LIKE '%total_points%' AND cc.CONSTRAINT_NAME <> 'chk_review_total_b' LIMIT 1
);
SET @ddl = IF(@old_check IS NULL, 'SELECT 1',
    CONCAT('ALTER TABLE task_reviews DROP CHECK `', REPLACE(@old_check, '`', '``'), '`'));
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
SET @ddl = IF(EXISTS (SELECT 1 FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='task_reviews' AND CONSTRAINT_NAME='chk_review_total_b'), 'SELECT 1', 'ALTER TABLE task_reviews ADD CONSTRAINT chk_review_total_b CHECK (total_points BETWEEN 30 AND 100)');
PREPARE phase6c_stmt FROM @ddl;
EXECUTE phase6c_stmt;
DEALLOCATE PREPARE phase6c_stmt;
INSERT IGNORE INTO schema_migrations (version) VALUES ('010_diagnosis_practice');
