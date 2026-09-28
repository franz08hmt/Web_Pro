-- Quiz bổ sung cho hai robot đợt 3; chạy sau seed-robots-phase3.sql.
-- Nội dung chỉ INSERT IGNORE, có thể chạy lại mà không sửa lịch sử bài làm.
SET NAMES utf8mb4;
START TRANSACTION;

INSERT IGNORE INTO quiz_questions (`id`, `robot_id`, `prompt`, `explanation`, `question_order`) VALUES ('line-obstacle-q1', 'line-obstacle', 'Khi cảm biến line đang thấy vạch nhưng HC-SR04 báo vật cản gần hơn ngưỡng an toàn, chương trình nên ưu tiên điều gì?', 'Nên dừng hoặc giảm tốc để né vật cản trước, sau đó dùng hai cảm biến line tìm lại quỹ đạo. Hai nhiệm vụ cần có thứ tự ưu tiên rõ ràng.', 1);
INSERT IGNORE INTO quiz_questions (`id`, `robot_id`, `prompt`, `explanation`, `question_order`) VALUES ('line-obstacle-q2', 'line-obstacle', 'HC-SR04 ước lượng khoảng cách dựa trên đại lượng nào?', 'Cảm biến đo thời gian từ lúc phát xung TRIG đến lúc nhận xung ECHO. Quãng đường âm thanh đi-về được chia đôi để ra khoảng cách một chiều.', 2);
INSERT IGNORE INTO quiz_questions (`id`, `robot_id`, `prompt`, `explanation`, `question_order`) VALUES ('line-obstacle-q3', 'line-obstacle', 'Hai cảm biến dò line đặt gần mặt sàn có vai trò chính nào?', 'Hai cảm biến trái/phải so sánh phản xạ hồng ngoại để nhận biết vị trí vạch tương đối với robot; khoảng cách lắp cần nằm trong vùng đọc của module.', 3);
INSERT IGNORE INTO quiz_questions (`id`, `robot_id`, `prompt`, `explanation`, `question_order`) VALUES ('line-obstacle-q4', 'line-obstacle', 'Trên L298N, ENA và ENB thường được dùng để làm gì khi nối với chân PWM Arduino?', 'ENA/ENB cho phép và điều chỉnh tốc độ hai kênh động cơ bằng PWM. IN1–IN4 chọn chiều quay.', 4);
INSERT IGNORE INTO quiz_questions (`id`, `robot_id`, `prompt`, `explanation`, `question_order`) VALUES ('line-obstacle-q5', 'line-obstacle', 'Vì sao Arduino, cảm biến, L298N và nguồn động cơ phải nối chung GND?', 'Các tín hiệu cần một mốc điện áp tham chiếu chung. Không nối chung mass có thể làm mức HIGH/LOW hoặc xung ECHO bị hiểu sai.', 5);
INSERT IGNORE INTO quiz_questions (`id`, `robot_id`, `prompt`, `explanation`, `question_order`) VALUES ('line-obstacle-q6', 'line-obstacle', 'Sau khi né vật cản, cách nào giúp robot quay lại vạch an toàn hơn?', 'Robot nên giảm tốc, quét nhẹ theo hướng dự kiến và dùng lại tín hiệu hai cảm biến line để xác nhận đã gặp vạch, thay vì chạy tiếp theo thời gian cố định.', 6);

INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q1-a', 'line-obstacle-q1', 'Ưu tiên dừng hoặc né vật cản, rồi tìm lại vạch bằng cảm biến line', 1, 1);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q1-b', 'line-obstacle-q1', 'Bỏ qua HC-SR04 vì robot đang nhìn thấy vạch', 0, 2);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q1-c', 'line-obstacle-q1', 'Tăng PWM tối đa để vượt qua chướng ngại', 0, 3);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q1-d', 'line-obstacle-q1', 'Tắt cả hai cảm biến rồi giữ nguyên chiều chạy', 0, 4);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q2-a', 'line-obstacle-q2', 'Thời gian xung ECHO và tốc độ truyền âm trong không khí', 1, 1);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q2-b', 'line-obstacle-q2', 'Điện áp pin và số vòng quay bánh xe', 0, 2);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q2-c', 'line-obstacle-q2', 'Độ rộng xung PWM trên ENA', 0, 3);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q2-d', 'line-obstacle-q2', 'Số lần cảm biến line đổi trạng thái', 0, 4);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q3-a', 'line-obstacle-q3', 'So sánh phản xạ để xác định vạch lệch về bên trái hay bên phải', 1, 1);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q3-b', 'line-obstacle-q3', 'Đo nhiệt độ của bề mặt đường', 0, 2);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q3-c', 'line-obstacle-q3', 'Đo khoảng cách tới vật cản ở xa', 0, 3);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q3-d', 'line-obstacle-q3', 'Điều khiển trực tiếp dòng điện động cơ', 0, 4);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q4-a', 'line-obstacle-q4', 'Điều chỉnh tốc độ động cơ; IN1–IN4 chọn chiều', 1, 1);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q4-b', 'line-obstacle-q4', 'Đo thời gian xung ECHO', 0, 2);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q4-c', 'line-obstacle-q4', 'Cấp trực tiếp 6V cho Arduino 5V', 0, 3);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q4-d', 'line-obstacle-q4', 'Chọn ngưỡng phản xạ cho cảm biến line', 0, 4);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q5-a', 'line-obstacle-q5', 'Để các mức tín hiệu dùng cùng mốc điện áp tham chiếu', 1, 1);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q5-b', 'line-obstacle-q5', 'Để tăng điện áp hộp pin lên 12V', 0, 2);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q5-c', 'line-obstacle-q5', 'Để thay thế dây tín hiệu TRIG và ECHO', 0, 3);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q5-d', 'line-obstacle-q5', 'Để Arduino tính được số vòng quay động cơ', 0, 4);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q6-a', 'line-obstacle-q6', 'Giảm tốc và xác nhận vạch bằng tín hiệu cảm biến trước khi chạy tiếp', 1, 1);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q6-b', 'line-obstacle-q6', 'Chạy thẳng một khoảng thời gian cố định mà không đọc cảm biến', 0, 2);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q6-c', 'line-obstacle-q6', 'Tắt L298N và giữ động cơ quay tự do', 0, 3);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('line-obstacle-q6-d', 'line-obstacle-q6', 'Xoay cảm biến siêu âm thay cho cảm biến line', 0, 4);

INSERT IGNORE INTO quiz_questions (`id`, `robot_id`, `prompt`, `explanation`, `question_order`) VALUES ('servo-scout-q1', 'servo-scout', 'Vì sao cần chờ servo ổn định trước khi đọc HC-SR04 ở góc quét mới?', 'Trong lúc servo còn chuyển động, hướng đo thay đổi và rung cơ khí có thể làm kết quả không đại diện cho góc đã chọn. Chờ ngắn giúp gắn phép đo với đúng hướng.', 1);
INSERT IGNORE INTO quiz_questions (`id`, `robot_id`, `prompt`, `explanation`, `question_order`) VALUES ('servo-scout-q2', 'servo-scout', 'Chân D3 nối với dây nào của servo SG90 trong mô hình này?', 'D3 là tín hiệu điều khiển PWM; dây nguồn VCC và GND của servo cần nối theo sơ đồ nguồn riêng.', 2);
INSERT IGNORE INTO quiz_questions (`id`, `robot_id`, `prompt`, `explanation`, `question_order`) VALUES ('servo-scout-q3', 'servo-scout', 'Công thức gần đúng để đổi thời gian ECHO tính bằng micro giây sang khoảng cách một chiều tính bằng centimet là gì?', 'Âm thanh đi từ cảm biến tới vật rồi quay về, nên lấy thời gian nhân tốc độ âm thanh xấp xỉ 0.0343 cm/µs và chia đôi.', 3);
INSERT IGNORE INTO quiz_questions (`id`, `robot_id`, `prompt`, `explanation`, `question_order`) VALUES ('servo-scout-q4', 'servo-scout', 'Khi quét servo SG90, nguyên tắc nào bảo vệ cơ cấu khỏi kẹt?', 'Chọn các góc trong giới hạn cơ khí của servo và giá đỡ; không ép càng servo va vào chặn cứng ở hai đầu.', 4);
INSERT IGNORE INTO quiz_questions (`id`, `robot_id`, `prompt`, `explanation`, `question_order`) VALUES ('servo-scout-q5', 'servo-scout', 'Robot nên chọn hướng rẽ từ các số đo trái, giữa, phải như thế nào?', 'So sánh các khoảng cách hợp lệ, chọn vùng đủ rộng và giữ ngưỡng an toàn; nên xác nhận lại trước khi tăng tốc.', 5);
INSERT IGNORE INTO quiz_questions (`id`, `robot_id`, `prompt`, `explanation`, `question_order`) VALUES ('servo-scout-q6', 'servo-scout', 'Cách cấp nguồn nào phù hợp hơn khi SG90 làm Arduino bị reset lúc quay?', 'Servo có dòng đỉnh khi khởi động hoặc chịu tải; dùng nguồn 5V ổn định riêng đủ dòng, nối chung GND và không cấp đồng thời vào chân 5V từ USB.', 6);

INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q1-a', 'servo-scout-q1', 'Để cảm biến đứng yên theo hướng đã chọn trước khi lấy số đo', 1, 1);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q1-b', 'servo-scout-q1', 'Để tăng điện áp hộp pin', 0, 2);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q1-c', 'servo-scout-q1', 'Để đổi chân TRIG thành chân PWM', 0, 3);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q1-d', 'servo-scout-q1', 'Để servo quay liên tục không cần đọc cảm biến', 0, 4);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q2-a', 'servo-scout-q2', 'Dây Signal màu cam hoặc vàng', 1, 1);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q2-b', 'servo-scout-q2', 'Dây GND màu nâu', 0, 2);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q2-c', 'servo-scout-q2', 'Dây VCC màu đỏ', 0, 3);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q2-d', 'servo-scout-q2', 'Dây nguồn động cơ nối vào Vs L298N', 0, 4);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q3-a', 'servo-scout-q3', 'Khoảng cách ≈ thời gian ECHO × 0.0343 ÷ 2', 1, 1);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q3-b', 'servo-scout-q3', 'Khoảng cách = điện áp ECHO × dòng servo', 0, 2);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q3-c', 'servo-scout-q3', 'Khoảng cách = thời gian ECHO ÷ số vòng bánh xe', 0, 3);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q3-d', 'servo-scout-q3', 'Khoảng cách = góc servo × độ rộng xung TRIG', 0, 4);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q4-a', 'servo-scout-q4', 'Giới hạn góc trong hành trình cơ khí, không ép servo vào chặn cứng', 1, 1);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q4-b', 'servo-scout-q4', 'Quay liên tục nhiều vòng để tìm giới hạn', 0, 2);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q4-c', 'servo-scout-q4', 'Cấp trực tiếp 6V vào dây Signal', 0, 3);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q4-d', 'servo-scout-q4', 'Bỏ càng servo để cảm biến tự quay', 0, 4);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q5-a', 'servo-scout-q5', 'So sánh số đo hợp lệ, giữ ngưỡng an toàn và xác nhận hướng trước khi tăng tốc', 1, 1);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q5-b', 'servo-scout-q5', 'Luôn rẽ về hướng có số đo nhỏ nhất', 0, 2);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q5-c', 'servo-scout-q5', 'Chỉ dùng lần đọc đầu tiên dù cảm biến báo lỗi', 0, 3);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q5-d', 'servo-scout-q5', 'Chọn hướng dựa trên màu dây jumper', 0, 4);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q6-a', 'servo-scout-q6', 'Dùng 5V ổn định đủ dòng cho servo, nối chung GND và tránh cấp trùng chân 5V', 1, 1);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q6-b', 'servo-scout-q6', 'Cấp nguồn servo từ chân D3', 0, 2);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q6-c', 'servo-scout-q6', 'Nối cực dương pin 6V vào chân ECHO', 0, 3);
INSERT IGNORE INTO quiz_options (`id`, `question_id`, `label`, `is_correct`, `option_order`) VALUES ('servo-scout-q6-d', 'servo-scout-q6', 'Tách GND servo khỏi GND Arduino', 0, 4);

COMMIT;
