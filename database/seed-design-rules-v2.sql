-- Chạy sau v1; bản PUBLISHED và thiết kế cũ giữ nguyên dữ liệu đã pin.
-- v1 bù góc hai lần ở rear-bay/front-top: spec v2 của phòng mẫu đã có góc nền xấp xỉ 0.
-- battery=0.0000036732, sensor=0.0000073464: chuẩn hóa nền về 0, chỉ cộng hướng Y90/Y180 một lần.
-- Sao chép nguyên roles/mounts/fits/requirements của v1, chỉ hai mount của chassis đổi ry.
START TRANSACTION;
SELECT id FROM components WHERE id = 'chassis-2wd' FOR UPDATE;
SET @source_design_rules_v1 = (SELECT id FROM design_rule_sets WHERE version_no = 1 AND state = 'PUBLISHED');
SET @create_design_rules_v2 = @source_design_rules_v1 IS NOT NULL
    AND NOT EXISTS (SELECT 1 FROM design_rule_sets WHERE version_no = 2);
INSERT INTO design_rule_sets (version_no, state)
SELECT 2, 'PUBLISHED' WHERE @create_design_rules_v2;
SET @design_rules_v2 = (SELECT id FROM design_rule_sets WHERE version_no = 2);

INSERT INTO design_component_roles (rule_set_id, component_id, role, driver_channels)
SELECT @design_rules_v2, component_id, role, driver_channels
FROM design_component_roles WHERE rule_set_id = @source_design_rules_v1 AND @create_design_rules_v2;

INSERT INTO design_mounts
    (rule_set_id, component_id, mount_code, mount_type, label, x, y, z, rx, ry, rz, display_order)
SELECT @design_rules_v2, component_id, mount_code, mount_type, label, x, y, z, rx,
    CASE WHEN component_id = 'chassis-2wd' AND mount_code IN ('rear-bay', 'front-top') THEN 0 ELSE ry END,
    rz, display_order
FROM design_mounts WHERE rule_set_id = @source_design_rules_v1 AND @create_design_rules_v2;

INSERT INTO design_fits (rule_set_id, component_id, mount_type, orientation_code)
SELECT @design_rules_v2, component_id, mount_type, orientation_code
FROM design_fits WHERE rule_set_id = @source_design_rules_v1 AND @create_design_rules_v2;

INSERT INTO design_goal_requirements (rule_set_id, goal_code, role, min_count, message)
SELECT @design_rules_v2, goal_code, role, min_count, message
FROM design_goal_requirements WHERE rule_set_id = @source_design_rules_v1 AND @create_design_rules_v2;
COMMIT;
