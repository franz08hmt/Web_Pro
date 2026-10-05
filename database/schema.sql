-- MySQL >= 8.0.16. Run ONCE against an empty, explicitly selected database.
-- DDL implicitly commits in MySQL. Do not wrap this migration in a transaction.

SET NAMES utf8mb4;

CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    display_name VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('user', 'admin') NOT NULL DEFAULT 'user',
    session_version INT UNSIGNED NOT NULL DEFAULT 0,
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
    completed_at TIMESTAMP NULL DEFAULT NULL,
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

INSERT INTO schema_migrations (version)
VALUES ('004_account_session_version');

INSERT INTO schema_migrations (version)
VALUES ('005_assembly_completion');

-- Bài kiểm tra kiến thức theo robot (006_quiz).
CREATE TABLE quiz_questions (
    id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    prompt TEXT NOT NULL,
    explanation TEXT NOT NULL,
    question_order INT NOT NULL CHECK (question_order BETWEEN 1 AND 10000),

    UNIQUE (robot_id, question_order),

    FOREIGN KEY (robot_id)
        REFERENCES robots(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE quiz_options (
    id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    question_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    label TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE CHECK (is_correct IN (0, 1)),
    option_order INT NOT NULL CHECK (option_order BETWEEN 1 AND 100),

    UNIQUE (question_id, option_order),

    FOREIGN KEY (question_id)
        REFERENCES quiz_questions(id)
        ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE quiz_attempts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    score INT UNSIGNED NOT NULL,
    total_questions INT UNSIGNED NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    FOREIGN KEY (robot_id)
        REFERENCES robots(id)
        ON DELETE RESTRICT,

    INDEX idx_quiz_attempts_user_robot (user_id, robot_id, submitted_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE quiz_attempt_answers (
    attempt_id BIGINT UNSIGNED NOT NULL,
    question_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    question_prompt_snapshot TEXT NOT NULL,
    selected_option_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    selected_option_label_snapshot TEXT NOT NULL,
    correct_option_label_snapshot TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL CHECK (is_correct IN (0, 1)),
    explanation_snapshot TEXT NOT NULL,

    PRIMARY KEY (attempt_id, question_id),

    FOREIGN KEY (attempt_id)
        REFERENCES quiz_attempts(id)
        ON DELETE CASCADE,

    FOREIGN KEY (question_id)
        REFERENCES quiz_questions(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO schema_migrations (version)
VALUES ('006_quiz');

-- Tra cứu lỗi lắp ráp (007_troubleshooting).
CREATE TABLE troubleshooting_guides (
    id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
    component_group VARCHAR(100) NOT NULL,
    symptom VARCHAR(255) NOT NULL,
    possible_causes TEXT NOT NULL,
    resolution_steps TEXT NOT NULL,
    related_component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
    display_order INT NOT NULL CHECK (display_order BETWEEN 1 AND 10000),

    FOREIGN KEY (robot_id)
        REFERENCES robots(id)
        ON DELETE RESTRICT,

    FOREIGN KEY (related_component_id)
        REFERENCES components(id)
        ON DELETE SET NULL,

    INDEX idx_troubleshooting_robot (robot_id),
    INDEX idx_troubleshooting_group (component_group)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO schema_migrations (version)
VALUES ('007_troubleshooting');

-- Cửa hàng linh kiện và đơn hàng mô phỏng (008_shop_cart_orders).
CREATE TABLE shop_products (
    id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    component_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL UNIQUE,
    price_vnd DECIMAL(12, 0) UNSIGNED NOT NULL CHECK (price_vnd > 0),
    stock_quantity INT UNSIGNED NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE CHECK (is_active IN (0, 1)),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (component_id)
        REFERENCES components(id)
        ON DELETE RESTRICT,

    INDEX idx_shop_products_active (is_active, id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE cart_items (
    user_id BIGINT UNSIGNED NOT NULL,
    product_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    quantity INT UNSIGNED NOT NULL CHECK (quantity BETWEEN 1 AND 10000),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, product_id),
    INDEX idx_cart_items_product (product_id),

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    FOREIGN KEY (product_id)
        REFERENCES shop_products(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE orders (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    status ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    total_vnd DECIMAL(14, 0) UNSIGNED NOT NULL CHECK (total_vnd >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_orders_user_created (user_id, created_at),

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE order_items (
    order_id BIGINT UNSIGNED NOT NULL,
    product_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    product_name_snapshot VARCHAR(150) NOT NULL,
    unit_price_vnd DECIMAL(12, 0) UNSIGNED NOT NULL CHECK (unit_price_vnd > 0),
    quantity INT UNSIGNED NOT NULL CHECK (quantity BETWEEN 1 AND 10000),

    PRIMARY KEY (order_id, product_id),
    INDEX idx_order_items_product (product_id),

    FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE RESTRICT,

    FOREIGN KEY (product_id)
        REFERENCES shop_products(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


INSERT INTO schema_migrations (version)
VALUES ('008_shop_cart_orders');

-- Đợt 6 chặng 1 và 2: schema cài mới đã gộp migration 009/010.
SET NAMES utf8mb4;
CREATE TABLE diagnosis_scenarios (
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
CREATE TABLE diagnosis_checks (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    scenario_id BIGINT UNSIGNED NOT NULL,
    label VARCHAR(200) NOT NULL,
    observation_text TEXT NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE CHECK (is_required IN (0,1)),
    display_order INT NOT NULL CHECK (display_order BETWEEN 1 AND 4),
    UNIQUE (scenario_id, display_order),
    FOREIGN KEY (scenario_id) REFERENCES diagnosis_scenarios(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE diagnosis_options (
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
CREATE TABLE diagnosis_attempts (
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
CREATE TABLE diagnosis_attempt_checks (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT UNSIGNED NOT NULL,
    check_id BIGINT UNSIGNED NOT NULL,
    chosen_at DATETIME NOT NULL,
    UNIQUE (attempt_id, check_id),
    FOREIGN KEY (attempt_id) REFERENCES diagnosis_attempts(id) ON DELETE RESTRICT,
    FOREIGN KEY (check_id) REFERENCES diagnosis_checks(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
 rubric_template ENUM('A','B') NOT NULL DEFAULT 'A',
 state ENUM('DRAFT','OPEN','CLOSED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
 published_at DATETIME NULL,
 created_at DATETIME NOT NULL,
 INDEX idx_tasks_state (state, due_at),
 FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE RESTRICT,
 FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE RESTRICT,
 diagnosis_scenario_id BIGINT UNSIGNED NULL,
 CONSTRAINT fk_task_diagnosis FOREIGN KEY (diagnosis_scenario_id) REFERENCES diagnosis_scenarios(id) ON DELETE RESTRICT
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
 FOREIGN KEY (quiz_attempt_id) REFERENCES quiz_attempts(id) ON DELETE RESTRICT,
 diagnosis_attempt_id BIGINT UNSIGNED NULL UNIQUE,
 CONSTRAINT fk_round_diagnosis FOREIGN KEY (diagnosis_attempt_id) REFERENCES diagnosis_attempts(id) ON DELETE RESTRICT
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
 automatic_points DECIMAL(4,1) NOT NULL CHECK (automatic_points BETWEEN 30 AND 80),
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
 FOREIGN KEY (quiz_attempt_id) REFERENCES quiz_attempts(id) ON DELETE RESTRICT,
 diagnosis_attempt_id BIGINT UNSIGNED NULL,
 diagnosis_title VARCHAR(150) NULL,
 diag_required_done INT NULL,
 diag_required_total INT NULL,
 diag_cause_correct BOOLEAN NULL,
 diag_action_correct BOOLEAN NULL,
 diagnosis_score DECIMAL(4,1) NULL,
 diagnosis_points DECIMAL(4,1) NULL,
 diagnosis_reused_from INT NOT NULL DEFAULT 0,
 CONSTRAINT fk_submission_diagnosis FOREIGN KEY (diagnosis_attempt_id) REFERENCES diagnosis_attempts(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS task_reviews (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 submission_id BIGINT UNSIGNED NOT NULL,
 reviewer_id BIGINT UNSIGNED NOT NULL,
 explanation_level INT NOT NULL CHECK (explanation_level BETWEEN 0 AND 4),
 explanation_points INT NOT NULL CHECK (explanation_points IN (0,5,10,15,20)),
 total_points DECIMAL(4,1) NOT NULL CHECK (total_points BETWEEN 30 AND 100),
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

INSERT IGNORE INTO schema_migrations (version) VALUES ('010_diagnosis_practice');

-- Phòng nối dây: chỉ thêm bảng, không thay đổi dữ liệu/cấu trúc cũ.
CREATE TABLE IF NOT EXISTS wiring_exercises (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    robot_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    objective TEXT NOT NULL,
    scope_text TEXT NOT NULL,
    state ENUM('DRAFT','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT UNSIGNED NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP NULL,
    INDEX(state, id),
    FOREIGN KEY (robot_id) REFERENCES robots(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wiring_terminals (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    exercise_id BIGINT UNSIGNED NOT NULL,
    code VARCHAR(64) NOT NULL,
    device_code VARCHAR(64) NOT NULL,
    device_label VARCHAR(100) NOT NULL,
    pin_label VARCHAR(50) NOT NULL,
    display_order INT NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    UNIQUE (exercise_id, id),
    UNIQUE (exercise_id, code),
    CHECK (x BETWEEN 40 AND 960 AND y BETWEEN 40 AND 960),
    CHECK (display_order BETWEEN 0 AND 40),
    FOREIGN KEY (exercise_id) REFERENCES wiring_exercises(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wiring_rules (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    exercise_id BIGINT UNSIGNED NOT NULL,
    terminal_a BIGINT UNSIGNED NOT NULL,
    terminal_b BIGINT UNSIGNED NOT NULL,
    kind ENUM('REQUIRED','FORBIDDEN') NOT NULL,
    explanation TEXT NOT NULL,
    UNIQUE (exercise_id, terminal_a, terminal_b),
    CHECK (terminal_a < terminal_b),
    FOREIGN KEY (exercise_id) REFERENCES wiring_exercises(id) ON DELETE RESTRICT,
    FOREIGN KEY (exercise_id, terminal_a) REFERENCES wiring_terminals(exercise_id, id) ON DELETE RESTRICT,
    FOREIGN KEY (exercise_id, terminal_b) REFERENCES wiring_terminals(exercise_id, id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wiring_attempts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    exercise_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    state ENUM('DRAFT','SUBMITTED') NOT NULL DEFAULT 'DRAFT',
    version INT NOT NULL DEFAULT 1,
    required_count INT NULL,
    correct_count INT NULL,
    wrong_count INT NULL,
    missing_count INT NULL,
    score DECIMAL(4,1) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMP NULL,
    UNIQUE (id, exercise_id),
    INDEX (user_id, id),
    CHECK (version >= 1),
    CHECK ((state='DRAFT' AND score IS NULL AND required_count IS NULL AND correct_count IS NULL
        AND wrong_count IS NULL AND missing_count IS NULL AND submitted_at IS NULL)
        OR (state='SUBMITTED' AND score IS NOT NULL AND required_count IS NOT NULL
        AND correct_count IS NOT NULL AND wrong_count IS NOT NULL AND missing_count IS NOT NULL
        AND submitted_at IS NOT NULL AND score BETWEEN 0 AND 100 AND required_count > 0
        AND correct_count BETWEEN 0 AND required_count AND wrong_count BETWEEN 0 AND 80
        AND missing_count = required_count - correct_count)),
    FOREIGN KEY (exercise_id) REFERENCES wiring_exercises(id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wiring_attempt_connections (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT UNSIGNED NOT NULL,
    exercise_id BIGINT UNSIGNED NOT NULL,
    terminal_a BIGINT UNSIGNED NOT NULL,
    terminal_b BIGINT UNSIGNED NOT NULL,
    UNIQUE (attempt_id, terminal_a, terminal_b),
    CHECK (terminal_a < terminal_b),
    FOREIGN KEY (attempt_id, exercise_id) REFERENCES wiring_attempts(id, exercise_id) ON DELETE RESTRICT,
    FOREIGN KEY (exercise_id, terminal_a) REFERENCES wiring_terminals(exercise_id, id) ON DELETE RESTRICT,
    FOREIGN KEY (exercise_id, terminal_b) REFERENCES wiring_terminals(exercise_id, id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO schema_migrations(version) VALUES ('011_wiring_practice');
