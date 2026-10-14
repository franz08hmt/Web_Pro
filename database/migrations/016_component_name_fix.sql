-- Tên catalog là nguồn nhãn; không đổi tên admin đã sửa hoặc nhãn spec đã công bố.
SET NAMES utf8mb4;
START TRANSACTION;
UPDATE components SET name = 'Động cơ DC TT (hộp số)'
WHERE id = 'dc-motor' AND name = 'Động cơ DC TT & Bánh xe';
INSERT IGNORE INTO schema_migrations (version) VALUES ('016_component_name_fix');
COMMIT;
