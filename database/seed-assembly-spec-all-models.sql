-- Spec v1 cho 4 mẫu còn lại; chỉ tạo phiên mới, giữ nguyên GROUP và spec đã có.
-- Tọa độ kế thừa assembly-3d-config.js; hướng rời rạc là quy ước mô hình học tập.
-- Không mô phỏng dung sai cơ khí, động học servo hay an toàn điện của robot thật.
START TRANSACTION;
SELECT id FROM robots WHERE id IN ('line-follower', 'mini-arm', 'line-obstacle', 'servo-scout')
ORDER BY id FOR UPDATE;

-- line-follower: 11 vật thể; số lượng từng loại khớp robot_components.
SET @line_follower_new = NOT EXISTS (
    SELECT 1 FROM assembly_specs WHERE robot_id = 'line-follower' AND version_no = 1
);
INSERT INTO assembly_specs (robot_id, version_no, state)
SELECT 'line-follower', 1, 'PUBLISHED' FROM robots WHERE id = 'line-follower' AND @line_follower_new;
SET @line_follower_spec = (SELECT id FROM assembly_specs WHERE robot_id = 'line-follower' AND version_no = 1);
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_follower_spec, 'chassis-2wd', NULL, 'chassis-2wd', 'Khung xe 2 bánh', TRUE, 0, 0.42, 0, 0, 0, 0, 1 WHERE @line_follower_new;
SET @line_follower_chassis_2wd = (SELECT id FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'chassis-2wd');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_follower_spec, 'arduino-uno', @line_follower_chassis_2wd, 'arduino-uno', 'Arduino Uno', TRUE, -0.55, 0.78, 0.15, 0, 0, 0, 2 WHERE @line_follower_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_follower_spec, 'dc-motor-1', @line_follower_chassis_2wd, 'dc-motor', 'Động cơ DC 1', TRUE, -1.35, 0.34, 0.1, 0, -1.570796326795, 1.570796326795, 3 WHERE @line_follower_new;
SET @line_follower_dc_motor_1 = (SELECT id FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'dc-motor-1');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_follower_spec, 'dc-motor-2', @line_follower_chassis_2wd, 'dc-motor', 'Động cơ DC 2', TRUE, 1.35, 0.34, 0.1, 0, -4.712388980385, 1.570796326795, 4 WHERE @line_follower_new;
SET @line_follower_dc_motor_2 = (SELECT id FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'dc-motor-2');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_follower_spec, 'wheel-1', @line_follower_dc_motor_1, 'wheel', 'Bánh xe 1', TRUE, -1.72, 0.43, 0.1, 0, 0, 1.570796326795, 5 WHERE @line_follower_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_follower_spec, 'wheel-2', @line_follower_dc_motor_2, 'wheel', 'Bánh xe 2', TRUE, 1.72, 0.43, 0.1, 0, 0, 1.570796326795, 6 WHERE @line_follower_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_follower_spec, 'caster-wheel', @line_follower_chassis_2wd, 'caster-wheel', 'Bánh tự do', TRUE, 0, 0.18, 0.88, 0, 0, 0, 7 WHERE @line_follower_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_follower_spec, 'line-sensor-1', @line_follower_chassis_2wd, 'line-sensor', 'Cảm biến dò line 1', TRUE, -0.48, 0.25, -1.18, 0, 0, 0, 8 WHERE @line_follower_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_follower_spec, 'line-sensor-2', @line_follower_chassis_2wd, 'line-sensor', 'Cảm biến dò line 2', TRUE, 0.48, 0.25, -1.18, 0, 0, 0, 9 WHERE @line_follower_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_follower_spec, 'l298n', @line_follower_chassis_2wd, 'l298n', 'Module L298N', TRUE, 0.58, 0.8, 0.22, 0, 0, 0, 10 WHERE @line_follower_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_follower_spec, 'battery-holder', @line_follower_chassis_2wd, 'battery-holder', 'Hộp pin', TRUE, 0, 0.72, 0.78, 0, 0, 0, 11 WHERE @line_follower_new;
INSERT INTO assembly_slot_orientations (slot_id, orientation_code)
SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'chassis-2wd' AND @line_follower_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'arduino-uno' AND @line_follower_new
UNION ALL SELECT id, 'Y90' FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'dc-motor-1' AND @line_follower_new
UNION ALL SELECT id, 'Y270' FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'dc-motor-2' AND @line_follower_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'wheel-1' AND @line_follower_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'wheel-2' AND @line_follower_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'caster-wheel' AND @line_follower_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'line-sensor-1' AND @line_follower_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'line-sensor-2' AND @line_follower_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'l298n' AND @line_follower_new
UNION ALL SELECT id, 'Y90' FROM assembly_slots WHERE spec_id = @line_follower_spec AND slot_code = 'battery-holder' AND @line_follower_new;

-- mini-arm: 8 vật thể; số lượng từng loại khớp robot_components.
SET @mini_arm_new = NOT EXISTS (
    SELECT 1 FROM assembly_specs WHERE robot_id = 'mini-arm' AND version_no = 1
);
INSERT INTO assembly_specs (robot_id, version_no, state)
SELECT 'mini-arm', 1, 'PUBLISHED' FROM robots WHERE id = 'mini-arm' AND @mini_arm_new;
SET @mini_arm_spec = (SELECT id FROM assembly_specs WHERE robot_id = 'mini-arm' AND version_no = 1);
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @mini_arm_spec, 'arm-frame', NULL, 'arm-frame', 'Bộ khung cánh tay', TRUE, 0, 0.12, 0, 0, 0, 0, 1 WHERE @mini_arm_new;
SET @mini_arm_arm_frame = (SELECT id FROM assembly_slots WHERE spec_id = @mini_arm_spec AND slot_code = 'arm-frame');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @mini_arm_spec, 'arduino-uno', @mini_arm_arm_frame, 'arduino-uno', 'Arduino Uno', TRUE, -1.65, 0.24, 0.55, 0, 0, 0, 2 WHERE @mini_arm_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @mini_arm_spec, 'sg90-1', @mini_arm_arm_frame, 'sg90', 'Động cơ servo 1', TRUE, 0, 0.34, 0, 0, 0, 0, 3 WHERE @mini_arm_new;
SET @mini_arm_sg90_1 = (SELECT id FROM assembly_slots WHERE spec_id = @mini_arm_spec AND slot_code = 'sg90-1');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @mini_arm_spec, 'sg90-2', @mini_arm_sg90_1, 'sg90', 'Động cơ servo 2', TRUE, 0, 1.02, 0, 0, 0, 0, 4 WHERE @mini_arm_new;
SET @mini_arm_sg90_2 = (SELECT id FROM assembly_slots WHERE spec_id = @mini_arm_spec AND slot_code = 'sg90-2');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @mini_arm_spec, 'sg90-3', @mini_arm_sg90_2, 'sg90', 'Động cơ servo 3', TRUE, 0.72, 1.88, 0, 0, 0, -0.62, 5 WHERE @mini_arm_new;
SET @mini_arm_sg90_3 = (SELECT id FROM assembly_slots WHERE spec_id = @mini_arm_spec AND slot_code = 'sg90-3');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @mini_arm_spec, 'sg90-4', @mini_arm_sg90_3, 'sg90', 'Động cơ servo 4', TRUE, 1.45, 2.4, 0, 0, 0, -0.92, 6 WHERE @mini_arm_new;
SET @mini_arm_sg90_4 = (SELECT id FROM assembly_slots WHERE spec_id = @mini_arm_spec AND slot_code = 'sg90-4');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @mini_arm_spec, 'mini-gripper', @mini_arm_sg90_4, 'mini-gripper', 'Bộ kẹp mini', TRUE, 2.04, 2.62, 0, 0, 0, -0.25, 7 WHERE @mini_arm_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @mini_arm_spec, 'power-5v', @mini_arm_arm_frame, 'power-5v', 'Nguồn 5V phù hợp', TRUE, 1.45, 0.28, 0.72, 0, 0, 0, 8 WHERE @mini_arm_new;
INSERT INTO assembly_slot_orientations (slot_id, orientation_code)
SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @mini_arm_spec AND slot_code = 'arm-frame' AND @mini_arm_new
UNION ALL SELECT id, 'Y90' FROM assembly_slots WHERE spec_id = @mini_arm_spec AND slot_code = 'arduino-uno' AND @mini_arm_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @mini_arm_spec AND slot_code = 'sg90-1' AND @mini_arm_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @mini_arm_spec AND slot_code = 'sg90-2' AND @mini_arm_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @mini_arm_spec AND slot_code = 'sg90-3' AND @mini_arm_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @mini_arm_spec AND slot_code = 'sg90-4' AND @mini_arm_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @mini_arm_spec AND slot_code = 'mini-gripper' AND @mini_arm_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @mini_arm_spec AND slot_code = 'power-5v' AND @mini_arm_new;

-- line-obstacle: 12 vật thể; số lượng từng loại khớp robot_components.
SET @line_obstacle_new = NOT EXISTS (
    SELECT 1 FROM assembly_specs WHERE robot_id = 'line-obstacle' AND version_no = 1
);
INSERT INTO assembly_specs (robot_id, version_no, state)
SELECT 'line-obstacle', 1, 'PUBLISHED' FROM robots WHERE id = 'line-obstacle' AND @line_obstacle_new;
SET @line_obstacle_spec = (SELECT id FROM assembly_specs WHERE robot_id = 'line-obstacle' AND version_no = 1);
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_obstacle_spec, 'chassis-2wd', NULL, 'chassis-2wd', 'Khung xe 2 bánh', TRUE, 0, 0.42, 0, 0, 0, 0, 1 WHERE @line_obstacle_new;
SET @line_obstacle_chassis_2wd = (SELECT id FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'chassis-2wd');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_obstacle_spec, 'arduino-uno', @line_obstacle_chassis_2wd, 'arduino-uno', 'Arduino Uno', TRUE, -0.62, 0.78, 0.15, 0, 0, 0, 2 WHERE @line_obstacle_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_obstacle_spec, 'dc-motor-1', @line_obstacle_chassis_2wd, 'dc-motor', 'Động cơ DC 1', TRUE, -1.35, 0.34, 0.1, 0, -1.570796326795, 1.570796326795, 3 WHERE @line_obstacle_new;
SET @line_obstacle_dc_motor_1 = (SELECT id FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'dc-motor-1');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_obstacle_spec, 'dc-motor-2', @line_obstacle_chassis_2wd, 'dc-motor', 'Động cơ DC 2', TRUE, 1.35, 0.34, 0.1, 0, -4.712388980385, 1.570796326795, 4 WHERE @line_obstacle_new;
SET @line_obstacle_dc_motor_2 = (SELECT id FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'dc-motor-2');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_obstacle_spec, 'wheel-1', @line_obstacle_dc_motor_1, 'wheel', 'Bánh xe 1', TRUE, -1.72, 0.43, 0.1, 0, 0, 1.570796326795, 5 WHERE @line_obstacle_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_obstacle_spec, 'wheel-2', @line_obstacle_dc_motor_2, 'wheel', 'Bánh xe 2', TRUE, 1.72, 0.43, 0.1, 0, 0, 1.570796326795, 6 WHERE @line_obstacle_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_obstacle_spec, 'caster-wheel', @line_obstacle_chassis_2wd, 'caster-wheel', 'Bánh tự do', TRUE, 0, 0.18, 0.88, 0, 0, 0, 7 WHERE @line_obstacle_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_obstacle_spec, 'line-sensor-1', @line_obstacle_chassis_2wd, 'line-sensor', 'Cảm biến dò line 1', TRUE, -0.48, 0.25, -1.18, 0, 0, 0, 8 WHERE @line_obstacle_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_obstacle_spec, 'line-sensor-2', @line_obstacle_chassis_2wd, 'line-sensor', 'Cảm biến dò line 2', TRUE, 0.48, 0.25, -1.18, 0, 0, 0, 9 WHERE @line_obstacle_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_obstacle_spec, 'hc-sr04', @line_obstacle_chassis_2wd, 'hc-sr04', 'Cảm biến siêu âm HC-SR04', TRUE, 0, 1.02, -0.98, 0, 0, 0, 10 WHERE @line_obstacle_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_obstacle_spec, 'l298n', @line_obstacle_chassis_2wd, 'l298n', 'Module L298N', TRUE, 0.62, 0.8, 0.2, 0, 0, 0, 11 WHERE @line_obstacle_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @line_obstacle_spec, 'battery-holder', @line_obstacle_chassis_2wd, 'battery-holder', 'Hộp pin', TRUE, 0, 0.72, 0.82, 0, 0, 0, 12 WHERE @line_obstacle_new;
INSERT INTO assembly_slot_orientations (slot_id, orientation_code)
SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'chassis-2wd' AND @line_obstacle_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'arduino-uno' AND @line_obstacle_new
UNION ALL SELECT id, 'Y90' FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'dc-motor-1' AND @line_obstacle_new
UNION ALL SELECT id, 'Y270' FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'dc-motor-2' AND @line_obstacle_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'wheel-1' AND @line_obstacle_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'wheel-2' AND @line_obstacle_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'caster-wheel' AND @line_obstacle_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'line-sensor-1' AND @line_obstacle_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'line-sensor-2' AND @line_obstacle_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'hc-sr04' AND @line_obstacle_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'l298n' AND @line_obstacle_new
UNION ALL SELECT id, 'Y90' FROM assembly_slots WHERE spec_id = @line_obstacle_spec AND slot_code = 'battery-holder' AND @line_obstacle_new;

-- servo-scout: 12 vật thể; số lượng từng loại khớp robot_components.
SET @servo_scout_new = NOT EXISTS (
    SELECT 1 FROM assembly_specs WHERE robot_id = 'servo-scout' AND version_no = 1
);
INSERT INTO assembly_specs (robot_id, version_no, state)
SELECT 'servo-scout', 1, 'PUBLISHED' FROM robots WHERE id = 'servo-scout' AND @servo_scout_new;
SET @servo_scout_spec = (SELECT id FROM assembly_specs WHERE robot_id = 'servo-scout' AND version_no = 1);
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @servo_scout_spec, 'chassis-2wd', NULL, 'chassis-2wd', 'Khung xe 2 bánh', TRUE, 0, 0.42, 0, 0, 0, 0, 1 WHERE @servo_scout_new;
SET @servo_scout_chassis_2wd = (SELECT id FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'chassis-2wd');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @servo_scout_spec, 'arduino-uno', @servo_scout_chassis_2wd, 'arduino-uno', 'Arduino Uno', TRUE, -0.72, 0.78, 0.05, 0, 0, 0, 2 WHERE @servo_scout_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @servo_scout_spec, 'dc-motor-1', @servo_scout_chassis_2wd, 'dc-motor', 'Động cơ DC 1', TRUE, -1.35, 0.34, 0.1, 0, -1.570796326795, 1.570796326795, 3 WHERE @servo_scout_new;
SET @servo_scout_dc_motor_1 = (SELECT id FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'dc-motor-1');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @servo_scout_spec, 'dc-motor-2', @servo_scout_chassis_2wd, 'dc-motor', 'Động cơ DC 2', TRUE, 1.35, 0.34, 0.1, 0, -4.712388980385, 1.570796326795, 4 WHERE @servo_scout_new;
SET @servo_scout_dc_motor_2 = (SELECT id FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'dc-motor-2');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @servo_scout_spec, 'wheel-1', @servo_scout_dc_motor_1, 'wheel', 'Bánh xe 1', TRUE, -1.72, 0.43, 0.1, 0, 0, 1.570796326795, 5 WHERE @servo_scout_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @servo_scout_spec, 'wheel-2', @servo_scout_dc_motor_2, 'wheel', 'Bánh xe 2', TRUE, 1.72, 0.43, 0.1, 0, 0, 1.570796326795, 6 WHERE @servo_scout_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @servo_scout_spec, 'caster-wheel', @servo_scout_chassis_2wd, 'caster-wheel', 'Bánh tự do', TRUE, 0, 0.18, 0.88, 0, 0, 0, 7 WHERE @servo_scout_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @servo_scout_spec, 'sg90', @servo_scout_chassis_2wd, 'sg90', 'Động cơ servo SG90', TRUE, 0, 0.82, -0.92, 0, 0, 0, 8 WHERE @servo_scout_new;
SET @servo_scout_sg90 = (SELECT id FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'sg90');
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @servo_scout_spec, 'hc-sr04', @servo_scout_sg90, 'hc-sr04', 'Cảm biến siêu âm HC-SR04', TRUE, 0, 1.42, -1.14, 0, 0, 0, 9 WHERE @servo_scout_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @servo_scout_spec, 'l298n', @servo_scout_chassis_2wd, 'l298n', 'Module L298N', TRUE, 0.68, 0.8, 0.16, 0, 0, 0, 10 WHERE @servo_scout_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @servo_scout_spec, 'battery-holder', @servo_scout_chassis_2wd, 'battery-holder', 'Hộp pin', TRUE, -0.62, 0.72, 0.78, 0, 0, 0, 11 WHERE @servo_scout_new;
INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @servo_scout_spec, 'power-5v', @servo_scout_chassis_2wd, 'power-5v', 'Module nguồn DC-DC 5V', TRUE, 0.9, 0.74, 0.76, 0, 0, 0, 12 WHERE @servo_scout_new;
INSERT INTO assembly_slot_orientations (slot_id, orientation_code)
SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'chassis-2wd' AND @servo_scout_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'arduino-uno' AND @servo_scout_new
UNION ALL SELECT id, 'Y90' FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'dc-motor-1' AND @servo_scout_new
UNION ALL SELECT id, 'Y270' FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'dc-motor-2' AND @servo_scout_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'wheel-1' AND @servo_scout_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'wheel-2' AND @servo_scout_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'caster-wheel' AND @servo_scout_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'sg90' AND @servo_scout_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'hc-sr04' AND @servo_scout_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'l298n' AND @servo_scout_new
UNION ALL SELECT id, 'Y90' FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'battery-holder' AND @servo_scout_new
UNION ALL SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @servo_scout_spec AND slot_code = 'power-5v' AND @servo_scout_new;
COMMIT;
