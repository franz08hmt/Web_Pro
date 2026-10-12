-- Bổ sung thư viện cho đủ 5 mẫu robot và có 12 mục để kiểm tra phân trang.
-- Chạy SAU database/seed.sql và database/seed-robots-phase3.sql.
SET NAMES utf8mb4;
START TRANSACTION;

INSERT IGNORE INTO library_resources (id, robot_id, title, type, url, description) VALUES
('line-follower-uno-guide', 'line-follower', 'Hướng dẫn Arduino Uno cho robot dò đường', 'link', 'https://docs.arduino.cc/hardware/uno-rev3/', 'Tài liệu chính thức để kiểm tra bo mạch Arduino Uno trước khi nối cảm biến dò line.'),
('line-follower-line-sensor', 'line-follower', 'Gợi ý đặt cảm biến dò line', 'image', '/assets/images/components/ir-sensor.png', 'Ảnh cảm biến hồng ngoại dùng để đối chiếu vị trí lắp phía trước robot dò đường.'),
('obstacle-avoider-ultrasonic', 'obstacle-avoider', 'Cảm biến siêu âm cho robot tránh vật cản', 'image', '/assets/images/components/ultrasonic.png', 'Ảnh HC-SR04 để đối chiếu cảm biến đo khoảng cách của robot tránh vật cản.'),
('obstacle-avoider-reference', 'obstacle-avoider', 'Tài liệu Arduino Robot tránh vật cản', 'link', 'https://docs.arduino.cc/retired/getting-started-guides/Robot/', 'Tư liệu chính thức về mô hình Arduino Robot để tham khảo cấu hình cảm biến và di chuyển.'),
('mini-arm-servo', 'mini-arm', 'Động cơ servo cho cánh tay robot mini', 'image', '/assets/images/components/servo-motor.png', 'Ảnh servo SG90 để đối chiếu các khớp và bộ kẹp của cánh tay robot mini.'),
('mini-arm-servo-guide', 'mini-arm', 'Hướng dẫn điều khiển servo', 'link', 'https://docs.arduino.cc/learn/electronics/servo-motors/', 'Tài liệu chính thức về cách dùng servo trong mô hình cánh tay robot.'),
('line-obstacle-image', 'line-obstacle', 'Robot dò line kết hợp tránh vật cản', 'image', '/assets/images/robots/robot-line-obstacle.png', 'Ảnh mô hình kết hợp cảm biến dò line và cảm biến siêu âm.'),
('line-obstacle-checklist', 'line-obstacle', 'Điều khiển hai động cơ cho robot kết hợp', 'link', 'https://docs.arduino.cc/hardware/motor-shield-rev3', 'Tài liệu về điều khiển tốc độ và chiều quay động cơ DC trước khi thử mô hình kết hợp.'),
('servo-scout-image', 'servo-scout', 'Robot quét hướng tránh vật cản', 'image', '/assets/images/robots/robot-servo-scout.png', 'Ảnh mô hình dùng servo để quét hướng cảm biến siêu âm.'),
('servo-scout-servo-guide', 'servo-scout', 'Tài liệu servo quét hướng', 'link', 'https://docs.arduino.cc/learn/electronics/servo-motors/', 'Tư liệu về servo SG90 khi quét các góc để chọn hướng di chuyển.');

COMMIT;
