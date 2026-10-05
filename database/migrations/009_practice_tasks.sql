-- Đợt 6 chặng 1. Chạy một lần sau migration 008; chỉ bổ sung bảng.
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS practice_tasks (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 creator_id BIGINT UNSIGNED NOT NULL,
 title VARCHAR(150) NOT NULL,
 description TEXT NOT NULL,
 robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
 due_at DATETIME NOT NULL,
 late_policy ENUM('REJECT_LATE','ACCEPT_LATE_FLAGGED') NOT NULL DEFAULT 'REJECT_LATE',
 max_submissions INT NOT NULL DEFAULT 2 CHECK (max_submissions BETWEEN 1 AND 3),
 pass_threshold INT NOT NULL DEFAULT 70 CHECK (pass_threshold BETWEEN 50 AND 100),
 allow_prior_evidence BOOLEAN NOT NULL DEFAULT FALSE,
 rubric_template ENUM('A') NOT NULL DEFAULT 'A',
 state ENUM('DRAFT','OPEN','CLOSED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
 published_at DATETIME NULL,
 created_at DATETIME NOT NULL,
 INDEX idx_tasks_state (state, due_at),
 FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE RESTRICT,
 FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS task_recipients (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 task_id BIGINT UNSIGNED NOT NULL,
 user_id BIGINT UNSIGNED NOT NULL,
 UNIQUE (task_id, user_id),
 INDEX idx_recipients_user (user_id, task_id),
 FOREIGN KEY (task_id) REFERENCES practice_tasks(id) ON DELETE RESTRICT,
 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS task_rounds (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 recipient_id BIGINT UNSIGNED NOT NULL,
 round_no INT NOT NULL CHECK (round_no BETWEEN 1 AND 3),
 started_at DATETIME NOT NULL,
 state ENUM('ACTIVE','SUBMITTED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
 quiz_attempt_id BIGINT UNSIGNED NULL UNIQUE,
 UNIQUE (recipient_id, round_no),
 FOREIGN KEY (recipient_id) REFERENCES task_recipients(id) ON DELETE RESTRICT,
 FOREIGN KEY (quiz_attempt_id) REFERENCES quiz_attempts(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS task_submissions (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 recipient_id BIGINT UNSIGNED NOT NULL,
 round_id BIGINT UNSIGNED NOT NULL,
 submission_no INT NOT NULL CHECK (submission_no BETWEEN 1 AND 3),
 session_id BIGINT UNSIGNED NOT NULL,
 quiz_attempt_id BIGINT UNSIGNED NOT NULL,
 robot_name VARCHAR(150) NOT NULL,
 assembly_completed_at DATETIME NOT NULL,
 quiz_score INT NOT NULL CHECK (quiz_score >= 0),
 quiz_total INT NOT NULL,
 CHECK (quiz_total > 0 AND quiz_score <= quiz_total),
 quiz_submitted_at DATETIME NOT NULL,
 quiz_reused_from INT NOT NULL DEFAULT 0,
 assembly_points DECIMAL(4,1) NOT NULL,
 quiz_points DECIMAL(4,1) NOT NULL,
 automatic_points DECIMAL(4,1) NOT NULL CHECK (automatic_points BETWEEN 40 AND 80),
 is_late BOOLEAN NOT NULL,
 problem TEXT NOT NULL,
 reasoning TEXT NOT NULL,
 improvement TEXT NOT NULL,
 submitted_at DATETIME NOT NULL,
 state ENUM('SUBMITTED','REVIEWED') NOT NULL DEFAULT 'SUBMITTED',
 UNIQUE (round_id),
 UNIQUE (recipient_id, submission_no),
 FOREIGN KEY (recipient_id) REFERENCES task_recipients(id) ON DELETE RESTRICT,
 FOREIGN KEY (round_id) REFERENCES task_rounds(id) ON DELETE RESTRICT,
 FOREIGN KEY (session_id) REFERENCES assembly_sessions(id) ON DELETE RESTRICT,
 FOREIGN KEY (quiz_attempt_id) REFERENCES quiz_attempts(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS task_reviews (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 submission_id BIGINT UNSIGNED NOT NULL,
 reviewer_id BIGINT UNSIGNED NOT NULL,
 explanation_level INT NOT NULL CHECK (explanation_level BETWEEN 0 AND 4),
 explanation_points INT NOT NULL CHECK (explanation_points IN (0,5,10,15,20)),
 total_points DECIMAL(4,1) NOT NULL CHECK (total_points BETWEEN 40 AND 100),
 conclusion ENUM('PASSED','NEEDS_REVISION','NOT_PASSED') NOT NULL,
 strengths TEXT NOT NULL,
 improvements TEXT NOT NULL,
 retry_guidance TEXT NOT NULL,
 reviewed_at DATETIME NOT NULL,
 supersedes_review_id BIGINT UNSIGNED NULL UNIQUE,
 change_reason TEXT NOT NULL,
 INDEX idx_reviews_submission (submission_id, id),
 FOREIGN KEY (submission_id) REFERENCES task_submissions(id) ON DELETE RESTRICT,
 FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE RESTRICT,
 FOREIGN KEY (supersedes_review_id) REFERENCES task_reviews(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
INSERT IGNORE INTO schema_migrations (version) VALUES ('009_practice_tasks');
