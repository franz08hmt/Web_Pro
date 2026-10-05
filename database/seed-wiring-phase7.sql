-- Hai bài chính thức UTF-8; seed chỉ thêm bản mới, không sửa bản công bố.
SET NAMES utf8mb4;
START TRANSACTION;
SET @author = (SELECT MIN(id) FROM users WHERE role='ADMIN');
SET @seed_new = NOT EXISTS (SELECT 1 FROM wiring_exercises WHERE code='wiring-line-follower-v2');
INSERT IGNORE INTO wiring_exercises(code,title,robot_id,objective,scope_text,state,created_by,published_at)
SELECT 'wiring-line-follower-v2','Nối dây robot dò đường','line-follower','Nối đúng từng đầu nối theo sơ đồ tham khảo; phân biệt tín hiệu, nguồn và mass chung.','Chỉ chấm tín hiệu D5–D10, tín hiệu cảm biến, VCC cảm biến và GND chung đã khai báo. Không chấm Vs, đầu ra động cơ, jumper L298N hoặc nguồn Arduino; không mô phỏng điện/vận hành robot thật.','PUBLISHED',@author,CURRENT_TIMESTAMP
WHERE @seed_new=1 AND @author IS NOT NULL AND EXISTS (SELECT 1 FROM robots WHERE id='line-follower');
SET @exercise = (SELECT id FROM wiring_exercises WHERE code='wiring-line-follower-v2');
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D5','uno','Arduino Uno','D5',1,50,70 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D6','uno','Arduino Uno','D6',2,50,150 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D7','uno','Arduino Uno','D7',3,50,230 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D8','uno','Arduino Uno','D8',4,50,310 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D9','uno','Arduino Uno','D9',5,50,390 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D10','uno','Arduino Uno','D10',6,50,470 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D2','uno','Arduino Uno','D2',7,50,550 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D3','uno','Arduino Uno','D3',8,50,630 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.5V','uno','Arduino Uno','5V',9,50,710 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.GND','uno','Arduino Uno','GND',10,50,790 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.ENA','l298n','L298N','ENA',11,550,50 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.IN1','l298n','L298N','IN1',12,550,115 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.IN2','l298n','L298N','IN2',13,550,180 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.IN3','l298n','L298N','IN3',14,550,245 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.IN4','l298n','L298N','IN4',15,550,310 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.ENB','l298n','L298N','ENB',16,550,375 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.GND','l298n','L298N','GND',17,550,440 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'line-left.OUT','line-left','Line trái','OUT',18,550,505 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'line-left.VCC','line-left','Line trái','VCC',19,550,570 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'line-left.GND','line-left','Line trái','GND',20,550,635 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'line-right.OUT','line-right','Line phải','OUT',21,550,700 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'line-right.VCC','line-right','Line phải','VCC',22,550,765 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'line-right.GND','line-right','Line phải','GND',23,550,830 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'battery.MINUS','battery','Hộp pin','Cực âm',24,550,895 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Tín hiệu D5 điều khiển ENA của L298N theo đúng sơ đồ mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D5' AND b.code='l298n.ENA' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Tín hiệu D6 điều khiển IN1 của L298N theo đúng sơ đồ mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D6' AND b.code='l298n.IN1' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Tín hiệu D7 điều khiển IN2 của L298N theo đúng sơ đồ mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D7' AND b.code='l298n.IN2' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Tín hiệu D8 điều khiển IN3 của L298N theo đúng sơ đồ mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D8' AND b.code='l298n.IN3' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Tín hiệu D9 điều khiển IN4 của L298N theo đúng sơ đồ mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D9' AND b.code='l298n.IN4' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Tín hiệu D10 điều khiển ENB của L298N theo đúng sơ đồ mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D10' AND b.code='l298n.ENB' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Đọc OUT của cảm biến left tại D2; không dùng chung ID hai cảm biến.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D2' AND b.code='line-left.OUT' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Cấp 5V cho VCC của cảm biến left trong phạm vi bài.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.5V' AND b.code='line-left.VCC' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Nối GND cảm biến left về GND chung Arduino; chân chung cho phép nhiều dây.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.GND' AND b.code='line-left.GND' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Đọc OUT của cảm biến right tại D3; không dùng chung ID hai cảm biến.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D3' AND b.code='line-right.OUT' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Cấp 5V cho VCC của cảm biến right trong phạm vi bài.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.5V' AND b.code='line-right.VCC' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Nối GND cảm biến right về GND chung Arduino; chân chung cho phép nhiều dây.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.GND' AND b.code='line-right.GND' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Nối chung GND Arduino và L298N theo dòng mass chung của sơ đồ.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.GND' AND b.code='l298n.GND' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Khai báo cực âm hộp pin trong mass chung; không chấm đường cấp động cơ.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.GND' AND b.code='battery.MINUS' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'FORBIDDEN','Cặp bị cấm cụ thể: không nối trực tiếp 5V với GND; chỉ chấm cặp này, không suy diễn nguy cơ khác.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.5V' AND b.code='uno.GND' AND @seed_new=1;
SET @seed_new = NOT EXISTS (SELECT 1 FROM wiring_exercises WHERE code='wiring-obstacle-avoider-v2');
INSERT IGNORE INTO wiring_exercises(code,title,robot_id,objective,scope_text,state,created_by,published_at)
SELECT 'wiring-obstacle-avoider-v2','Nối dây robot tránh vật cản','obstacle-avoider','Nối đúng từng đầu nối theo sơ đồ tham khảo; phân biệt tín hiệu, nguồn và mass chung.','Chỉ chấm tín hiệu D5–D10, tín hiệu cảm biến, VCC cảm biến và GND chung đã khai báo. Không chấm Vs, đầu ra động cơ, jumper L298N hoặc nguồn Arduino; không mô phỏng điện/vận hành robot thật.','PUBLISHED',@author,CURRENT_TIMESTAMP
WHERE @seed_new=1 AND @author IS NOT NULL AND EXISTS (SELECT 1 FROM robots WHERE id='obstacle-avoider');
SET @exercise = (SELECT id FROM wiring_exercises WHERE code='wiring-obstacle-avoider-v2');
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D5','uno','Arduino Uno','D5',1,50,70 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D6','uno','Arduino Uno','D6',2,50,150 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D7','uno','Arduino Uno','D7',3,50,230 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D8','uno','Arduino Uno','D8',4,50,310 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D9','uno','Arduino Uno','D9',5,50,390 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D10','uno','Arduino Uno','D10',6,50,470 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D11','uno','Arduino Uno','D11',7,50,550 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.D12','uno','Arduino Uno','D12',8,50,630 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.5V','uno','Arduino Uno','5V',9,50,710 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'uno.GND','uno','Arduino Uno','GND',10,50,790 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.ENA','l298n','L298N','ENA',11,550,50 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.IN1','l298n','L298N','IN1',12,550,115 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.IN2','l298n','L298N','IN2',13,550,180 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.IN3','l298n','L298N','IN3',14,550,245 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.IN4','l298n','L298N','IN4',15,550,310 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.ENB','l298n','L298N','ENB',16,550,375 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'l298n.GND','l298n','L298N','GND',17,550,440 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'hc-sr04.TRIG','hc-sr04','HC-SR04','TRIG',18,550,505 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'hc-sr04.ECHO','hc-sr04','HC-SR04','ECHO',19,550,570 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'hc-sr04.VCC','hc-sr04','HC-SR04','VCC',20,550,635 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'hc-sr04.GND','hc-sr04','HC-SR04','GND',21,550,700 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_terminals(exercise_id,code,device_code,device_label,pin_label,display_order,x,y)
SELECT @exercise,'battery.MINUS','battery','Hộp pin','Cực âm',22,550,765 WHERE @seed_new=1 AND @exercise IS NOT NULL;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Tín hiệu D5 điều khiển ENA của L298N theo đúng sơ đồ mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D5' AND b.code='l298n.ENA' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Tín hiệu D6 điều khiển IN1 của L298N theo đúng sơ đồ mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D6' AND b.code='l298n.IN1' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Tín hiệu D7 điều khiển IN2 của L298N theo đúng sơ đồ mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D7' AND b.code='l298n.IN2' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Tín hiệu D8 điều khiển IN3 của L298N theo đúng sơ đồ mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D8' AND b.code='l298n.IN3' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Tín hiệu D9 điều khiển IN4 của L298N theo đúng sơ đồ mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D9' AND b.code='l298n.IN4' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Tín hiệu D10 điều khiển ENB của L298N theo đúng sơ đồ mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D10' AND b.code='l298n.ENB' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','D11 phát xung kích tại TRIG theo sơ đồ của mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D11' AND b.code='hc-sr04.TRIG' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','D12 đọc ECHO; không hoán đổi hai chân tín hiệu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.D12' AND b.code='hc-sr04.ECHO' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Nối 5V Arduino với VCC HC-SR04 trong phạm vi sơ đồ mẫu.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.5V' AND b.code='hc-sr04.VCC' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Nối mass HC-SR04 về GND chung Arduino.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.GND' AND b.code='hc-sr04.GND' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Nối chung GND Arduino và L298N theo dòng mass chung của sơ đồ.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.GND' AND b.code='l298n.GND' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'REQUIRED','Khai báo cực âm hộp pin trong mass chung; không chấm đường cấp động cơ.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.GND' AND b.code='battery.MINUS' AND @seed_new=1;
INSERT IGNORE INTO wiring_rules(exercise_id,terminal_a,terminal_b,kind,explanation)
SELECT @exercise,LEAST(a.id,b.id),GREATEST(a.id,b.id),'FORBIDDEN','Cặp bị cấm cụ thể: không nối trực tiếp 5V với GND; chỉ chấm cặp này, không suy diễn nguy cơ khác.'
FROM wiring_terminals a JOIN wiring_terminals b ON b.exercise_id=a.exercise_id
WHERE a.exercise_id=@exercise AND a.code='uno.5V' AND b.code='uno.GND' AND @seed_new=1;
COMMIT;
