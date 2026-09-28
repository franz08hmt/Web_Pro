-- Run once on an existing database before deploying the updated WAR.
-- Bài kiểm tra kiến thức theo robot (Chức năng 2). Bốn bảng mới, không đổi gì
-- ở bảng cũ. quiz_attempt_answers lưu snapshot câu hỏi/lựa chọn tại thời điểm
-- nộp bài, nên admin sửa câu hỏi sau đó không làm đổi kết quả đã có.

SET NAMES utf8mb4;

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


-- Mỗi lần nộp bài tạo đúng một dòng, chấm điểm ngay bằng dữ liệu server đọc
-- được lúc nộp; không có trạng thái "đang làm dở" lưu ở server.
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
