-- Spec v2 độc lập; chạy lại không sửa v1, bản đã công bố hoặc phiên cũ.
-- Góc nền bù hướng đầu tiên để tư thế đúng trùng v1; CHECK cho phép [-6.284, 6.284].
START TRANSACTION;
SELECT id FROM robots WHERE id = 'obstacle-avoider' FOR UPDATE;
SET @create_assembly_spec_v2 = NOT EXISTS (
    SELECT 1 FROM assembly_specs WHERE robot_id = 'obstacle-avoider' AND version_no = 2
);
INSERT INTO assembly_specs (robot_id, version_no, state)
SELECT 'obstacle-avoider', 2, 'PUBLISHED' FROM robots
WHERE id = 'obstacle-avoider' AND @create_assembly_spec_v2;
SET @assembly_spec_v2 = (
    SELECT id FROM assembly_specs WHERE robot_id = 'obstacle-avoider' AND version_no = 2
);

INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @assembly_spec_v2, 'chassis', NULL, 'chassis-2wd', 'Khung xe (nền)', TRUE, 0, .42, 0, 0, 0, 0, 1
WHERE @create_assembly_spec_v2 AND @assembly_spec_v2 IS NOT NULL;
SET @chassis_slot_v2 = (SELECT id FROM assembly_slots WHERE spec_id = @assembly_spec_v2 AND slot_code = 'chassis');

INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @assembly_spec_v2, 'motor-left', @chassis_slot_v2, 'dc-motor', 'Động cơ DC TT trái', TRUE,
    -1.35, .34, .1, 0, -1.5707963267948966, 1.5708, 2 WHERE @create_assembly_spec_v2
UNION ALL SELECT @assembly_spec_v2, 'motor-right', @chassis_slot_v2, 'dc-motor', 'Động cơ DC TT phải', TRUE,
    1.35, .34, .1, 0, -4.71238898038469, 1.5708, 3 WHERE @create_assembly_spec_v2
UNION ALL SELECT @assembly_spec_v2, 'uno', @chassis_slot_v2, 'arduino-uno', 'Mạch Arduino trên khung', TRUE,
    -.58, .78, .22, 0, 0, 0, 6 WHERE @create_assembly_spec_v2
UNION ALL SELECT @assembly_spec_v2, 'driver', @chassis_slot_v2, 'l298n', 'Mạch L298N trên khung', TRUE,
    .6, .8, .28, 0, 0, 0, 7 WHERE @create_assembly_spec_v2
UNION ALL SELECT @assembly_spec_v2, 'battery', @chassis_slot_v2, 'battery-holder', 'Hộp pin phía sau', TRUE,
    0, .72, .82, 0, 0.0000036732051034, 0, 8 WHERE @create_assembly_spec_v2
UNION ALL SELECT @assembly_spec_v2, 'caster', @chassis_slot_v2, 'caster-wheel', 'Bánh tự do dưới khung', TRUE,
    0, .18, .88, 0, 0, 0, 9 WHERE @create_assembly_spec_v2
UNION ALL SELECT @assembly_spec_v2, 'sensor', @chassis_slot_v2, 'hc-sr04', 'Cảm biến hướng về trước', TRUE,
    0, 1.02, -.92, 0, 0.0000073464102068, 0, 10 WHERE @create_assembly_spec_v2;

INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @assembly_spec_v2, 'wheel-left', id, 'wheel', 'Bánh trên trục động cơ trái', TRUE,
    -1.72, .43, .1, 0, 0, 1.5708, 4 FROM assembly_slots
WHERE spec_id = @assembly_spec_v2 AND slot_code = 'motor-left' AND @create_assembly_spec_v2
UNION ALL SELECT @assembly_spec_v2, 'wheel-right', id, 'wheel', 'Bánh trên trục động cơ phải', TRUE,
    1.72, .43, .1, 0, 0, 1.5708, 5 FROM assembly_slots
WHERE spec_id = @assembly_spec_v2 AND slot_code = 'motor-right' AND @create_assembly_spec_v2;

-- Mỗi hàng là một hướng chấp nhận, không gộp danh sách vào một cột.
INSERT INTO assembly_slot_orientations (slot_id, orientation_code)
SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @assembly_spec_v2 AND @create_assembly_spec_v2
    AND slot_code IN ('chassis', 'wheel-left', 'wheel-right', 'uno', 'driver', 'caster')
UNION ALL SELECT id, 'Y90' FROM assembly_slots
WHERE spec_id = @assembly_spec_v2 AND @create_assembly_spec_v2
    AND slot_code IN ('motor-left', 'battery', 'caster')
UNION ALL SELECT id, 'Y180' FROM assembly_slots
WHERE spec_id = @assembly_spec_v2 AND @create_assembly_spec_v2
    AND slot_code IN ('wheel-left', 'wheel-right', 'driver', 'caster', 'sensor')
UNION ALL SELECT id, 'Y270' FROM assembly_slots
WHERE spec_id = @assembly_spec_v2 AND @create_assembly_spec_v2
    AND slot_code IN ('motor-right', 'battery', 'caster');
COMMIT;
