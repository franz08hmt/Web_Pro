-- New immutable spec only. Rerun does not edit a published spec or old sessions.
-- Motor and wheel are separate physical items, as confirmed by the user.
START TRANSACTION;
SELECT id FROM robots WHERE id = 'obstacle-avoider' FOR UPDATE;
SET @create_assembly_spec = NOT EXISTS (
    SELECT 1 FROM assembly_specs WHERE robot_id = 'obstacle-avoider' AND version_no = 1
);
INSERT INTO assembly_specs (robot_id, version_no, state)
SELECT 'obstacle-avoider', 1, 'PUBLISHED' FROM robots
WHERE id = 'obstacle-avoider' AND @create_assembly_spec;
SET @assembly_spec = (
    SELECT id FROM assembly_specs WHERE robot_id = 'obstacle-avoider' AND version_no = 1
);

INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @assembly_spec, 'chassis', NULL, 'chassis-2wd', 'Khung xe (nền)', TRUE, 0, .42, 0, 0, 0, 0, 1
WHERE @create_assembly_spec AND @assembly_spec IS NOT NULL;
SET @chassis_slot = (SELECT id FROM assembly_slots WHERE spec_id = @assembly_spec AND slot_code = 'chassis');

INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @assembly_spec, 'motor-left', @chassis_slot, 'dc-motor', 'Động cơ DC TT trái', TRUE,
    -1.35, .34, .1, 0, 0, 1.5708, 2 WHERE @create_assembly_spec
UNION ALL SELECT @assembly_spec, 'motor-right', @chassis_slot, 'dc-motor', 'Động cơ DC TT phải', TRUE,
    1.35, .34, .1, 0, 0, 1.5708, 3 WHERE @create_assembly_spec
UNION ALL SELECT @assembly_spec, 'uno', @chassis_slot, 'arduino-uno', 'Mạch Arduino trên khung', TRUE,
    -.58, .78, .22, 0, 0, 0, 6 WHERE @create_assembly_spec
UNION ALL SELECT @assembly_spec, 'driver', @chassis_slot, 'l298n', 'Mạch L298N trên khung', TRUE,
    .6, .8, .28, 0, 0, 0, 7 WHERE @create_assembly_spec
UNION ALL SELECT @assembly_spec, 'battery', @chassis_slot, 'battery-holder', 'Hộp pin phía sau', TRUE,
    0, .72, .82, 0, 1.5708, 0, 8 WHERE @create_assembly_spec
UNION ALL SELECT @assembly_spec, 'caster', @chassis_slot, 'caster-wheel', 'Bánh tự do dưới khung', TRUE,
    0, .18, .88, 0, 0, 0, 9 WHERE @create_assembly_spec
UNION ALL SELECT @assembly_spec, 'sensor', @chassis_slot, 'hc-sr04', 'Cảm biến hướng về trước', TRUE,
    0, 1.02, -.92, 0, 3.1416, 0, 10 WHERE @create_assembly_spec;

INSERT INTO assembly_slots
    (spec_id, slot_code, parent_slot_id, component_id, label, required, x, y, z, rx, ry, rz, display_order)
SELECT @assembly_spec, 'wheel-left', id, 'wheel', 'Bánh trên trục động cơ trái', TRUE,
    -1.72, .43, .1, 0, 0, 1.5708, 4 FROM assembly_slots
WHERE spec_id = @assembly_spec AND slot_code = 'motor-left' AND @create_assembly_spec
UNION ALL SELECT @assembly_spec, 'wheel-right', id, 'wheel', 'Bánh trên trục động cơ phải', TRUE,
    1.72, .43, .1, 0, 0, 1.5708, 5 FROM assembly_slots
WHERE spec_id = @assembly_spec AND slot_code = 'motor-right' AND @create_assembly_spec;

INSERT INTO assembly_slot_orientations (slot_id, orientation_code)
SELECT id, 'Y0' FROM assembly_slots WHERE spec_id = @assembly_spec AND @create_assembly_spec;
COMMIT;
