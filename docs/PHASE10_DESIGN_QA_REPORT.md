# Đợt10 — Xưởng thiết kế: bằng chứng theo mốc

## M0/M1 — 14/10/2026

HEAD b0bf22c, ahead8; baseline183Node/109JUnit, JDK17 clean package xanh.
Snapshot44 tập hàng cũ và10 file được bảo vệ ngoài Git trước khi ghi dữ liệu.
ERD viết trước code; M0 commit19e344c. M1 luật/reader/015/seed,186Node/110JUnit.
Node/JUnit mới đỏ trước implementation. Guard hồ sơ chỉ thêm015 vào whitelist
migration chức năng khác; vẫn chặn migration lạ, không thêm bảng hồ sơ.

DDL MySQL thật phát hiện FK CASCADE của placements không phù hợp cột sinh root.
Sửa riêng015/schema mới sang RESTRICT (9571e51); xóa design phải xóa placements
trước. Không DROP/ALTER bảng cũ. Seed chạy bằng SQL UTF-8 nguyên file, trong
transaction; thử lệnh SOURCE qua stdin chưa chạy được, không phải lỗi dữ liệu.
Cuối có55 bảng,015 hiện diện,11 role/11 mount, rule v1 PUBLISHED; seed chạy lại
không đổi hash rule/mount/fit. Chưa có seed hay DDL nào sửa spec v1/v2.

## M2 — Bộ linh kiện/form/nhập mua

187Node/111JUnit/package xanh. GET MyKit owner/no-store,13 linh kiện; POST hidden
CSRF/getParameterValues, validation bean, transaction owner và PRG. Kit cho phép0
xóa,1..10 lưu. Import SUM đơn CONFIRMED của owner, bean cap10, GREATEST chống cộng
trùng và giữ số đang có lớn hơn. Không cập nhật order, không giảm kit khi import.

Chrome/HTTP13 kiểm đạt: GET200, hai import302/không tăng lại lượng, quantity11→422
không ghi một phần, thiếu CSRF403, JS-off13 inputnumber,390/1440 không tràn/pageerror.
Demo builder/practice/student không có đơn CONFIRMED nên tạo USER QA23
qa-design-buyer-20261014@robotlab.test qua đăng ký, mua mô phỏng một Arduino bằng
luồng shop hiện có, order2. Stock sản phẩm giảm một theo checkout; ghi metadata
không bí mật ở qa-user.json. Không sửa tài khoản/kit/phiên mẫu cũ để ép QA đạt.

Shop chỉ thêm id anchor trên card để link kit/BOM tới sản phẩm có thật, cache-busting
shop.js cập nhật; không thay luật/cart/checkout. Account HTML chỉ thêm link kit.
Guard redirect được mở rộng kiểm cả @WebServlet và file HTML, loại query khi so
đường dẫn; vẫn chặn trang dang-nhap.html không tồn tại. Không giấu URL để né guard.

Bằng chứng ngoài Git: D:/Web-Pro/qa-evidence/phase10; baseline/test/package logs,
migration-results.json, m2-results.json, kit390/1440.png, hash WIP/DB và metadata QA.
Các mốc tiếp theo chưa được coi đã kiểm hoặc hoàn thành trong mục này.

## M3 — Tạo/quản lý/snapshot

188Node/112JUnit/package xanh;18 HTTP/SQL/Chrome kiểm đạt. KIT vượt số đang có422
trên form, không header/instances mới; LIBRARY và KIT hợp lệ302, snapshot10 vật thể.
Kit đổi bánh về0 không đổi hai instance bánh đã chọn. Đổi tên302, form cũ409;
USER khác GET/POST404. Xóa thiếu xác nhận422, có xác nhận302, cascade snapshot
chỉ của thiết kế đó. Tên chứa chuỗi script hiển thị dạng chữ;390/1440 không tràn.
User QA23 có design1 LIBRARY và design2 KIT; design QA3 dùng kiểm xóa đã xóa qua form.
Không dùng thiết kế riêng làm phiên assembly/nhiệm vụ; không sửa luật mẫu.

## M4–M6 — Tương tác, kiểm tra và hồ sơ

| Mốc | Node/JUnit (không fail/skip) | Bằng chứng thật |
| --- | --- | --- |
| M4 |189/114|32 ca HTTP/SQL/Chrome; gắn/tháo/undo/retry/version/owner/reload,10 placement |
| M5 |190/120|15 ca HTTP/Chrome; đủ bộ đạt, bỏ driver cần sửa, chưa hỗ trợ, check đồng thời200/409 |
| M6 |191/124|18 ca hồ sơ/JS-off/Chrome390/1440/PDF;3 trang PDF đã render/xem toàn bộ |

Maven package JDK17 đạt. Bốn view mới thêm hợp đồng form/binding, BEANS map;
hợp đồng/guard cũ giữ nguyên, room không có panel bước, server quyết định kết quả.

| Kịch bản HTTP | Kết quả |
| --- | --- |
| Gắn/tháo hợp lệ, undo gắn mới/đổi hướng/tháo |200, SQL/reload đúng |
| Retry đúng cặp/hướng, kể cả version cũ |200 unchanged, không ghi thêm |
| Bánh vào mặt trên / HC-SR04 sai hướng |422 MOUNT_INCOMPATIBLE / ORIENTATION_NOT_ALLOWED, message có tên |
| Cha chưa gắn / tháo cha còn con |422 HOST_MISSING / HAS_DEPENDENTS |
| arm-frame gửi gắn / kiểm tra |422 NOT_SUPPORTED / Chưa hỗ trợ kiểm chứng |
| Thiếu CSRF / ID khác chủ |403 /404 cả GET và POST |
| Version cũ / hai check đồng thời |409 / một200,một409 |
| Đủ obstacle-avoider / tháo L298N |CHECK200 đạt / CHECK200 không đạt, câu hướng dẫn motor-driver |
| Đổi bố cục đã CHECKED |DRAFT và xóa dữ kiện kiểm cũ |
| Xóa design có placement/lịch sử undo |POST302, không orphan, chỉ fixture QA |
| /design-sheet owner / khác chủ / khách |200 no-store /404 /302 |

Browser Xưởng thêm13 ca: radio→gắn thật, Q/E, Ctrl+Z, kéo vật thể có di chuyển nhưng
camera không xoay, kéo nền camera xoay, thả vào root POST200, reload có vật thể,
390/768/1440 không tràn,0pageerror. Hồ sơ JS-off có hai SVG/10 chú giải/BOM/10 bước,
ba lớp kết quả và XSS hiển thị chữ. PDF A4 trắng/đen, navigation ẩn,3 trang đủ dữ kiện,
đã xem ảnh từng trang. M6 crowded30 vật thể/khung160×160 không ném lỗi, đủ chú giải,
tọa độ trong khung; test cũ40 nhãn tách nhau trong khung mặc định vẫn đạt.
Hết chỗ giữ tâm, không cam kết khoảng cách44. Thống kê dùng chung isLearningError
và bốn mã Xưởng; lỗi tab, khóa, hoàn tác trống, request sai không tính lỗi học tập.

## M7 — Phát hiện trước khi duyệt luật sửa

GET bằng ba USER demo: tasks/account/learning-profile/wiring/diagnosis/robots/
components/architecture/quiz/shop/order-history200. Phiên16,30,31,33,34,35,37 mở
review/receipt/layout200. ADMIN QA22 từ Đợt9 login200, robot/steps/stats200;
không tạo hoặc nâng quyền ADMIN mới trong Đợt10.

Đối chiếu cuối phát hiện góc pin/cảm biến trong luật Xưởng v1 không khớp mẫu:
rear-bay/front-top bù góc hai lần. Góc nền battery/sensor trong spec v2 vốn đã≈0
(0.0000036732/0.0000073464), view đã cộng Y90/Y180; v1 lại trừ thêm90°/180°.
Đây là lỗi seed đợt này. Đã dừng nghiệm thu hình và hỏi người dùng trước khi
tạo phiên bản sửa; người dùng duyệt v2 chỉ đổi hai ry về0, giữ nguyên v1.
Ảnh ngoài Git:pose-workshop-v1.png/pose-template-v2.png trong phase10.

10 file WIP/untracked nguyên byte;43/44 tập hàng cũ giữ hash. shop_products đổi
stock−1/timestamp đúng checkout QA order2, không SQL sửa dữ liệu để dựng ca.
Mọi bảng assembly/spec/events/quiz/task/history cũ nguyên hash. Dữ liệu mới:
USER23 qa-design-buyer-20261014@robotlab.test, đơn2 mua một Arduino, kit USER23;
design1 LIBRARY đủ bộ, design2 KIT cho tương tác, design4 arm-frame chưa hỗ trợ.
Các fixture xóa đã xóa qua form xác nhận; metadata ngoài Git, không mật khẩu/cookie.

Chưa kiểm Firefox/Safari, cảm ứng vật lý, screen reader hoặc robot thật;390px là
Chrome mô phỏng viewport. Bản vẽ khối giản lược, không theo tỉ lệ kỹ thuật;
không kiểm điện/dòng/va chạm/firmware, không xác nhận chiều trục động cơ thật.
Bằng chứng D:/Web-Pro/qa-evidence/phase10: m4/m5/m6-results.json, browser-results.json,
audit-results.json, sheet-*.png/design-sheet.pdf/sheet-print-*.png và test/build logs.

Kiểm cuối trước điểm chờ: mvn -q test, mvn clean package với JDK17 và npm test đều
đạt124 JUnit/191 Node,0 fail/skip. Dừng đúng process QA, xóa CATALINA_BASE/config/
log tạm;8081 không còn listener,8080 không bị can thiệp. Cookie chỉ ở browser context
đã đóng, không ghi ra file. Đây là kiểm chứng trước khi chốt bổ sung v2 dưới đây.

Commit local M0–M6:19e344c,667d433,9571e51 (bổ sung FK),bb91aa8,0fa6e33,
d6a6784,d2c2feb,25c0acb. Chưa push; nhánh ahead16, gồm8 commit đã có trước đợt này.

## M7 — Đã chốt luật v2 và nghiệm thu lại

User duyệt seed-design-rules-v2.sql: khóa chassis/transaction, chỉ tạo version2
PUBLISHED khi có v1 PUBLISHED và chưa có v2. INSERT SELECT sao chép nguyên role,
mount/fit/goal requirements; CASE chỉ đổi ry của chassis rear-bay/front-top bằng0.
Không UPDATE/DELETE v1, không đổi schema, spec hoặc bảng phòng mẫu. DB diff đầy đủ
xác nhận đúng hai ô khác; chạy lại seed không đổi hash v1/v2. Snapshot design1/2/4
nguyên hash, pin v1; phiên bản/nhắc lỗi v1 ở danh sách/hồ sơ không sửa dữ kiện cũ.

| Kiểm bổ sung thật | Kết quả |
| --- | --- |
| Baseline vòng v2 |191 Node/124 JUnit,0 fail/skip |
| Sau sửa |192 Node/126 JUnit,0 fail/skip, JDK17 package đạt |
| Node seed v2 / JUnit pose / nhãn v1-v2 |guard copy/2 mount; pin Y90→π/2, sensor Y180→π; cảnh báo không đổi FK |
| Thiết kế QA7 OBSTACLE_AVOIDER mới |rule v2,10/10 placement, check200 đạt |
| Thiết kế QA8 SCANNING_AVOIDER mới |rule v2,11/11, sensor trên horn/180°, check200 đạt kèm lưu ý nguồn servo |
| Sensor sai hướng ở FRONT_TOP và SERVO_HORN |422 ORIENTATION_NOT_ALLOWED; sửa Y180→200 |
| Khác chủ / thiếu CSRF |404 /403 |
| Bản vẽ/hồ sơ7/8 |200, v2, BOM/trình tự/kiểm đầy đủ, không cảnh báo v1 |
| Thiết kế1/2/4 |200, v1 và đúng câu cảnh báo tạo mới; dữ liệu không đổi |
| Chrome1440/390 phòng lắp mới |không tràn,0pageerror/asset404, chụp/xem ảnh so sánh |
| Hồi quy/quyền cuối |68 GET/security checks và15 viewport/render/JS-off checks đạt |
| PDF mới |mỗi thiết kế3 trang A4; render/xem cả6 trang, đủ bốn phần, v2, navigation ẩn |

39 ca v2-live gồm nghiệm thu pose từng bộ phận của obstacle theo spec v2; x/y/z,
rx/rz khớp và yaw khớp trong sai số. Pin90° nằm ngang, HC-SR04 quay180° giống mẫu.
Ảnh mẫu là preview chỉ đọc, dựng từ spec v2/assembly-3d-config và hình thủ tục
hiện có trong cùng góc camera; không tạo/sửa phiên mẫu để chụp ảnh. Ảnh Xưởng lấy
layout server của7/8; có marker điểm gắn, ảnh tham chiếu không có marker.

SCANNING đối chiếu servo-scout có khác biệt rõ: target HC-SR04 của servo-scout
không khai ry nên0, còn Xưởng theo obstacle dùng180°. User đã chỉ định theo
obstacle-avoider đã kiểm8b; không sửa servo-scout. Giữ nguyên tọa độ luật v1 vì
v2 chỉ được sửa hai ry: servo Xưởng(0,1.02,−.92), sensor(0,1.77,−.92); servo-scout
servo(0,.82,−.92), sensor(0,1.42,−1.14). Không tuyên bố trùng toàn bộ hình servo-scout
hoặc kiểm cơ khí thật; mục tiêu quét được kiểm cây sensor→SERVO_HORN và vai trò.

Đơn QA2: rà OrderServlet chỉ có GET lịch sử/POST checkout; OrderDB không có hàm
hủy/hoàn kho. CANCELLED chỉ là hằng trạng thái, không có luồng hủy dùng được.
Theo chỉ định user giữ CONFIRMED, tồn kho Arduino giảm1. Không UPDATE bằng tay,
không thêm chức năng hủy. Audit vẫn10 file nguyên byte,43/44 tập hàng cũ giữ hash,
ngoại lệ shop_products là checkout QA này; spec/phiên mẫu cũ không đổi.

Ảnh mới ngoài Git ở D:/Web-Pro/qa-evidence/phase10:
v2-OBSTACLE_AVOIDER-workshop.png / v2-OBSTACLE_AVOIDER-template.png;
v2-SCANNING_AVOIDER-workshop.png / v2-SCANNING_AVOIDER-template.png;
v2-*-390.png, v2-*-sheet.pdf và v2-*-print-{1,2,3}.png.
Kết quả: v2-seed-results.json, v2-live-results.json (39), v2-final-results.json
(68 HTTP +15 browser), v2-pdf-results.json, audit-results.json. Không commit ảnh,
cookie/password hoặc cấu hình DB. Commit sửa v2:a93c44e; tài liệu M7 commit riêng.

Kiểm chốt M7 sau toàn bộ sửa: mvn -q test, mvn clean package bằng JDK17 và npm
test đạt192 Node/126 JUnit,0 fail/error/skip. Dừng đúng QA PID, dọn CATALINA_BASE/
config/log,8081 đóng; không can thiệp8080. Hash10 file bảo vệ kiểm lại trước commit
vẫn nguyên byte. Phần chưa kiểm Firefox/Safari/cảm ứng vật lý/screen reader/robot
thật giữ nguyên; đối chiếu servo-scout là preview cấu hình chỉ đọc, không phiên mới.

| Mốc | Commit | Message |
| --- | --- | --- |
| M0 |19e344c|feat(design): plan versioned robot workshop and isolated data model |
| M1 |667d433|feat(design): add versioned workshop rules and isolated schema |
| M1 FK |9571e51|feat(design): verify workshop foreign keys against MySQL |
| M2 |bb91aa8|feat(design): manage personal kits with idempotent purchase imports |
| M3 |0fa6e33|feat(design): create owned designs with inventory snapshots and form validation |
| M4 |d6a6784|feat(design): validate owned placements and undo with versioned transactions |
| M5 |d2c2feb|feat(design): check mechanical chains goal roles and supported configuration rules |
| M6 |25c0acb|feat(design): render printable design sheets with safe crowded drawings |
| V2 |a93c44e|feat(design): publish corrected v2 mount angles and preserve legacy rule notices |
| M7 |xem git log cuối|feat(design): document verified v2 workshop flows and QA evidence |

Không push. Tài khoản QA23, đơn2, kit và design1/2/4/7/8 giữ lại cho người dùng
quản lý; không có dữ liệu mới trong bảng phòng lắp mẫu. Bằng chứng QA giữ ngoài Git.
