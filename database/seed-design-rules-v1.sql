-- Luật v1 độc lập; chạy lại không sửa bản PUBLISHED hoặc thiết kế cũ.
-- MOTOR_MOUNT_L/R và rx/rz giữ quy ước pose giản lược v2, không mô phỏng chiều trục thật.
START TRANSACTION;
SELECT id FROM components WHERE id = 'chassis-2wd' FOR UPDATE;
SET @create_design_rules_v1 = NOT EXISTS (SELECT 1 FROM design_rule_sets WHERE version_no = 1);
INSERT INTO design_rule_sets (version_no, state) SELECT 1, 'PUBLISHED' WHERE @create_design_rules_v1;
SET @design_rules_v1 = (SELECT id FROM design_rule_sets WHERE version_no = 1);

INSERT INTO design_component_roles (rule_set_id, component_id, role, driver_channels)
SELECT @design_rules_v1, 'chassis-2wd', 'BASE', NULL WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'arduino-uno', 'CONTROLLER', NULL WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'l298n', 'MOTOR_DRIVER', 2 WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'dc-motor', 'DRIVE_MOTOR', NULL WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'wheel', 'WHEEL', NULL WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'caster-wheel', 'SUPPORT_WHEEL', NULL WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'battery-holder', 'POWER', NULL WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'power-5v', 'POWER', NULL WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'hc-sr04', 'DISTANCE_SENSOR', NULL WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'line-sensor', 'LINE_SENSOR', NULL WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'sg90', 'SERVO', NULL WHERE @create_design_rules_v1;

INSERT INTO design_mounts (rule_set_id, component_id, mount_code, mount_type, label, x, y, z, rx, ry, rz, display_order)
SELECT @design_rules_v1, 'chassis-2wd', 'motor-left', 'MOTOR_MOUNT_L', 'Vị trí động cơ trái', -1.35, -0.08, 0.1, 0, -1.5707963267948966, 1.5708, 1 WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'chassis-2wd', 'motor-right', 'MOTOR_MOUNT_R', 'Vị trí động cơ phải', 1.35, -0.08, 0.1, 0, -4.71238898038469, 1.5708, 2 WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'chassis-2wd', 'deck-left', 'BOARD_DECK', 'Mặt trên khung trái', -0.58, 0.36, 0.22, 0, 0, 0, 3 WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'chassis-2wd', 'deck-right', 'BOARD_DECK', 'Mặt trên khung phải', 0.6, 0.38, 0.28, 0, 0, 0, 4 WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'chassis-2wd', 'rear-bay', 'POWER_BAY', 'Khoang pin phía sau', 0, 0.3, 0.82, 0, -1.5707963267948966, 0, 5 WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'chassis-2wd', 'caster-point', 'CASTER_POINT', 'Điểm bánh tự do', 0, -0.24, 0.88, 0, 0, 0, 6 WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'chassis-2wd', 'front-top', 'FRONT_TOP', 'Mặt trước phía trên', 0, 0.6, -0.92, 0, -3.141592653589793, 0, 7 WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'chassis-2wd', 'under-front-left', 'UNDER_FRONT', 'Mặt dưới phía trước trái', -0.75, -0.24, -0.76, 0, 0, 0, 8 WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'chassis-2wd', 'under-front-right', 'UNDER_FRONT', 'Mặt dưới phía trước phải', 0.75, -0.24, -0.76, 0, 0, 0, 9 WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'dc-motor', 'axle', 'AXLE', 'Trục động cơ', 0, 0.09, -0.37, 0, 0, 1.5708, 10 WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'sg90', 'horn', 'SERVO_HORN', 'Tay quay servo', 0, 0.75, 0, 0, 0, 0, 11 WHERE @create_design_rules_v1;

INSERT INTO design_fits (rule_set_id, component_id, mount_type, orientation_code)
SELECT @design_rules_v1, 'chassis-2wd', 'ROOT', 'Y0' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'dc-motor', 'MOTOR_MOUNT_L', 'Y90' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'dc-motor', 'MOTOR_MOUNT_R', 'Y270' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'wheel', 'AXLE', 'Y0' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'wheel', 'AXLE', 'Y180' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'caster-wheel', 'CASTER_POINT', 'Y0' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'caster-wheel', 'CASTER_POINT', 'Y90' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'caster-wheel', 'CASTER_POINT', 'Y180' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'caster-wheel', 'CASTER_POINT', 'Y270' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'arduino-uno', 'BOARD_DECK', 'Y0' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'arduino-uno', 'BOARD_DECK', 'Y180' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'l298n', 'BOARD_DECK', 'Y0' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'l298n', 'BOARD_DECK', 'Y180' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'battery-holder', 'POWER_BAY', 'Y90' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'battery-holder', 'POWER_BAY', 'Y270' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'battery-holder', 'BOARD_DECK', 'Y90' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'battery-holder', 'BOARD_DECK', 'Y270' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'power-5v', 'POWER_BAY', 'Y90' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'power-5v', 'POWER_BAY', 'Y270' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'power-5v', 'BOARD_DECK', 'Y90' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'power-5v', 'BOARD_DECK', 'Y270' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'hc-sr04', 'FRONT_TOP', 'Y180' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'hc-sr04', 'SERVO_HORN', 'Y180' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'sg90', 'FRONT_TOP', 'Y0' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'line-sensor', 'UNDER_FRONT', 'Y0' WHERE @create_design_rules_v1;

INSERT IGNORE INTO design_goals (code,name,description) VALUES
('BASIC_2WD','Xe hai bánh cơ bản','Lắp ghép trong bộ linh kiện được hỗ trợ; kiểm theo luật giản lược.'),
('LINE_FOLLOWER','Robot dò line','Lắp ghép trong bộ linh kiện được hỗ trợ; kiểm theo luật giản lược.'),
('OBSTACLE_AVOIDER','Robot tránh vật cản','Lắp ghép trong bộ linh kiện được hỗ trợ; kiểm theo luật giản lược.'),
('SCANNING_AVOIDER','Tránh vật cản có quét','Lắp ghép trong bộ linh kiện được hỗ trợ; kiểm theo luật giản lược.'),
('COMBO','Dò line và tránh vật cản','Lắp ghép trong bộ linh kiện được hỗ trợ; kiểm theo luật giản lược.');

INSERT INTO design_goal_requirements (rule_set_id, goal_code, role, min_count, message)
SELECT @design_rules_v1, 'BASIC_2WD', 'BASE', 1, 'Cần ít nhất 1 khung đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'BASIC_2WD', 'CONTROLLER', 1, 'Cần ít nhất 1 mạch điều khiển đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'BASIC_2WD', 'MOTOR_DRIVER', 1, 'Cần ít nhất 1 mạch điều khiển động cơ đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'BASIC_2WD', 'DRIVE_MOTOR', 2, 'Cần ít nhất 2 động cơ DC đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'BASIC_2WD', 'WHEEL', 2, 'Cần ít nhất 2 bánh xe đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'BASIC_2WD', 'SUPPORT_WHEEL', 1, 'Cần ít nhất 1 bánh tự do đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'BASIC_2WD', 'POWER', 1, 'Cần ít nhất 1 nguồn đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'LINE_FOLLOWER', 'BASE', 1, 'Cần ít nhất 1 khung đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'LINE_FOLLOWER', 'CONTROLLER', 1, 'Cần ít nhất 1 mạch điều khiển đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'LINE_FOLLOWER', 'MOTOR_DRIVER', 1, 'Cần ít nhất 1 mạch điều khiển động cơ đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'LINE_FOLLOWER', 'DRIVE_MOTOR', 2, 'Cần ít nhất 2 động cơ DC đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'LINE_FOLLOWER', 'WHEEL', 2, 'Cần ít nhất 2 bánh xe đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'LINE_FOLLOWER', 'SUPPORT_WHEEL', 1, 'Cần ít nhất 1 bánh tự do đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'LINE_FOLLOWER', 'POWER', 1, 'Cần ít nhất 1 nguồn đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'LINE_FOLLOWER', 'LINE_SENSOR', 2, 'Cần ít nhất 2 cảm biến dò line đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'OBSTACLE_AVOIDER', 'BASE', 1, 'Cần ít nhất 1 khung đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'OBSTACLE_AVOIDER', 'CONTROLLER', 1, 'Cần ít nhất 1 mạch điều khiển đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'OBSTACLE_AVOIDER', 'MOTOR_DRIVER', 1, 'Cần ít nhất 1 mạch điều khiển động cơ đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'OBSTACLE_AVOIDER', 'DRIVE_MOTOR', 2, 'Cần ít nhất 2 động cơ DC đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'OBSTACLE_AVOIDER', 'WHEEL', 2, 'Cần ít nhất 2 bánh xe đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'OBSTACLE_AVOIDER', 'SUPPORT_WHEEL', 1, 'Cần ít nhất 1 bánh tự do đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'OBSTACLE_AVOIDER', 'POWER', 1, 'Cần ít nhất 1 nguồn đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'OBSTACLE_AVOIDER', 'DISTANCE_SENSOR', 1, 'Cần ít nhất 1 cảm biến khoảng cách đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'SCANNING_AVOIDER', 'BASE', 1, 'Cần ít nhất 1 khung đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'SCANNING_AVOIDER', 'CONTROLLER', 1, 'Cần ít nhất 1 mạch điều khiển đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'SCANNING_AVOIDER', 'MOTOR_DRIVER', 1, 'Cần ít nhất 1 mạch điều khiển động cơ đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'SCANNING_AVOIDER', 'DRIVE_MOTOR', 2, 'Cần ít nhất 2 động cơ DC đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'SCANNING_AVOIDER', 'WHEEL', 2, 'Cần ít nhất 2 bánh xe đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'SCANNING_AVOIDER', 'SUPPORT_WHEEL', 1, 'Cần ít nhất 1 bánh tự do đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'SCANNING_AVOIDER', 'POWER', 1, 'Cần ít nhất 1 nguồn đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'SCANNING_AVOIDER', 'DISTANCE_SENSOR', 1, 'Cần ít nhất 1 cảm biến khoảng cách đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'SCANNING_AVOIDER', 'SERVO', 1, 'Cần ít nhất 1 servo đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'COMBO', 'BASE', 1, 'Cần ít nhất 1 khung đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'COMBO', 'CONTROLLER', 1, 'Cần ít nhất 1 mạch điều khiển đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'COMBO', 'MOTOR_DRIVER', 1, 'Cần ít nhất 1 mạch điều khiển động cơ đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'COMBO', 'DRIVE_MOTOR', 2, 'Cần ít nhất 2 động cơ DC đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'COMBO', 'WHEEL', 2, 'Cần ít nhất 2 bánh xe đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'COMBO', 'SUPPORT_WHEEL', 1, 'Cần ít nhất 1 bánh tự do đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'COMBO', 'POWER', 1, 'Cần ít nhất 1 nguồn đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'COMBO', 'LINE_SENSOR', 2, 'Cần ít nhất 2 cảm biến dò line đã gắn.' WHERE @create_design_rules_v1
UNION ALL SELECT @design_rules_v1, 'COMBO', 'DISTANCE_SENSOR', 1, 'Cần ít nhất 1 cảm biến khoảng cách đã gắn.' WHERE @create_design_rules_v1;

COMMIT;
