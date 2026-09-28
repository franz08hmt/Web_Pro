-- Run once on an existing database before deploying the updated WAR.
-- Tra cứu lỗi lắp ráp (Chức năng 3). Một bảng mới, không đổi gì ở bảng cũ.
-- robot_id NULL nghĩa là tình huống dùng chung cho nhiều mẫu robot (cùng khuôn
-- với library_resources.robot_id).

SET NAMES utf8mb4;

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
