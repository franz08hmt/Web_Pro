# Đợt 2 — Lái thử mô hình 3D

- [x] Viết regression tests cho điều kiện phiên hoàn tất và các hành vi bàn phím.
- [x] Implement controller lái thử 3D tách biệt; clamp theo footprint và bệ.
- [x] Thêm UI có nút bật/tắt, hướng dẫn phím và thông báo chỉ mô phỏng.
- [x] Gọi cập nhật chuyển động từ render loop hiện có; dọn listeners khi dừng/rời trang.
- [x] Chạy `npm test`, `mvn test`, `mvn package`.
- [x] Deploy WAR lên Tomcat + MySQL cục bộ; kiểm tra trang/API/accessibility tree trong browser và dừng Tomcat test sạch.
- [x] Rà lại diff để bảo đảm không có thay đổi Servlet/API/database, không có panel bước 3D; log chứa DB config từ Tomcat thử đã được xóa cùng runtime.
- [ ] Thử bật/tắt và lái trên một phiên `COMPLETED` đã đăng nhập bằng trình duyệt thật; chưa đăng nhập hay tạo dữ liệu tài khoản/phiên để làm việc này.
- [x] Báo cáo đợt 2; chờ người dùng/điều phối trước đợt kế tiếp.

## Đợt 3 — Hai mẫu robot mới

- [x] Thêm kiểm thử hợp đồng cho hai robot mới; xác nhận đỏ trước triển khai.
- [x] Thêm seed additive cho robot/linh kiện/bước; đồng bộ `ROBOT_MODELS` và wiring.
- [x] Thêm cấu hình 3D cho mọi part quantity; giữ nguyên thư viện geometry và cấm panel bước 3D.
- [x] Thêm 6 câu quiz/robot và ít nhất 3 tình huống troubleshooting/robot trong seed riêng.
- [x] Tạo và thêm hai ảnh robot; kiểm tra asset path và phong cách hiển thị.
- [x] Cập nhật README, API conventions, ERD và demo guide theo luồng kiến thức môn học.
- [x] Chạy seed trên MySQL local theo kiểu an toàn/idempotent; xác nhận API trả dữ liệu.
- [x] Kiểm chứng trọn luồng browser cho cả hai mẫu: chuẩn bị → lắp 3D đủ part → hoàn tất/receipt → quiz. Hai quiz đều được chấm và lưu lịch sử 6/6 trên tài khoản QA local.
- [x] Chạy `npm test`, `mvn test`, `mvn clean package`; review diff/no secrets.
- [x] Commit đợt 3 local: `f582799 feat: add two robotics learning models`.
- [ ] Push đợt 3 lên `origin/integration/fullstack-v2`; đang chờ kết nối GitHub ổn định.

## Đợt 4 — Cửa hàng linh kiện và giỏ hàng mô phỏng

- [x] ERD và contract tests trước migration.
- [x] Migration/schema/seed additive cho shop, cart và order snapshot.
- [x] JavaBean, JDBC `XxxDB`, Servlet API, phân quyền ADMIN/CSRF và transaction checkout.
- [x] Catalog, giỏ, checkout mô phỏng, trang JSP lịch sử đơn và giao diện quản trị.
- [x] Cập nhật hướng dẫn API, kiến trúc, demo guide và README.
- [x] `npm test` 97/97, Java production/test biên dịch bằng JDK 17, JUnit 25/25;
  Maven không có trong PATH. Node syntax và `git diff --check` pass.
- [x] Áp dụng migration 008 và seed idempotent trên MySQL local; có 12 sản phẩm.
- [x] Tomcat QA cổng 8081 + browser guest: health/catalog 200, catalog 12 dòng,
  guest cart/admin 401, HTML/CSS/JS 200; đã dừng QA sạch.
- [ ] Chưa chạy checkout, CSRF ghi và CRUD admin với user đăng nhập thật; không
  tạo tài khoản/đơn thử để tránh thêm dữ liệu không cần thiết vào DB hiện tại.
- [x] Rà soát diff/no secrets và commit Đợt 4 ở local.
- [ ] Push các commit Đợt 3–4 lên `origin/integration/fullstack-v2`; push thường
  lỗi không kết nối được GitHub, lần thử có quyền mạng bị treo và đã dừng, chưa
  xác nhận remote nhận commit. Local hiện ahead 2 commits.

## Đợt 5 — Hồ sơ học tập cá nhân có thể in

- [x] Khảo sát hand-off, trạng thái nhánh, tài liệu, source servlet/JSP/StatsDB,
  hợp đồng JavaBean/JSP và dữ liệu skills của cả năm robot.
- [x] Xác nhận baseline: `npm test` 97/97; Maven cache/JDK 17 hoạt động và
  `mvn test` 25/25.
- [x] Viết unit/contract tests trước phần triển khai; kiểm tra trạng thái,
  chọn quiz theo tỷ lệ/thời gian/id, HALF_UP, giờ Việt Nam, skill evidence,
  scope user, no-store, escape và print CSS.
- [x] Thêm JavaBean hồ sơ, truy vấn JDBC user-scoped, Servlet GET và JSP/JSTL.
- [x] Thêm CSS A4 riêng, nút in bằng `window.print()`, link tài khoản/tổng kết.
- [x] Cập nhật API conventions, demo guide, ERD, README và plan.
- [x] Chạy lại toàn bộ `npm test` (104/104), `mvn clean test` (32/32),
  `mvn clean package` (32/32, JDK 17).
- [x] Live Tomcat/MySQL: health, guest redirect/no-store, hai hồ sơ QA rỗng,
  escape tên HTML và số liệu demo đọc trực tiếp bằng JDBC đã kiểm tra. Đăng nhập
  bốn tài khoản demo bằng mật khẩu trong `TEAM_FLOW_DEMO_GUIDE.md` và giá trị
  xác thực người dùng cung cấp đều bị từ chối;
  lần bổ sung ngày 04/10/2026 đăng nhập thành công cả bốn demo, đối chiếu
  JDBC/`/learning-summary` và owner-query tampering trong phiên đăng nhập.
- [x] Xuất PDF A4 bằng Chrome headless, xác nhận no-print, tiêu đề, chân hồ sơ,
  ngày giờ Việt Nam và 2 trang; đã render và xem trực quan cả hai trang. Tài khoản
  QA rỗng nên bảng kỹ năng có thead chưa xuất hiện trong PDF live.
- [x] Rà diff/no secrets, commit đúng file Đợt 5 và push nhánh hiện tại.

### Sửa sau review — 04/10/2026 (chỉ commit local, không push)

- [x] CSS in giữ mục Kỹ năng cùng tiêu đề/caption/bảng; bảng quá dài vẫn lặp thead.
- [x] Tổng quan phản ánh phiên đã dừng và quiz-only; thêm ba JUnit test cho luật
  và thứ tự ưu tiên, gồm 0/7 thật và nhiều trạng thái phiên mở.
- [x] Chỉnh gợi ý in, cache-busting CSS, hướng dẫn chuẩn bị demo và giải thích mã phiên.
- [x] Node 104/104, JUnit 35/35, `mvn clean package` JDK 17 thành công.
- [x] Tomcat/MySQL thật: guest 302/no-store, bốn demo đăng nhập/hồ sơ 200;
  đối chiếu JDBC/tổng kết; query owner không đổi dữ liệu. Smoke trang/API cũ 200.
- [x] Chrome PDF live builder và fixture JSP đủ dữ liệu 3 trang; mục Kỹ năng
  liền nhau. Fixture dài 6 trang lặp thead ở trang 3–6. Đã xem ảnh PDF/mobile;
  no-print bị ẩn, tên HTML được escape. Chưa kiểm tra Firefox/Safari.
- [x] Không thêm schema/migration/tài khoản hay ghi dữ liệu QA vào MySQL.
- [x] Dọn Tomcat QA/log/cookie tạm; giữ bằng chứng trong target/phase5-qa (không stage).
- [x] Review diff, commit đúng file sửa; không push theo yêu cầu hiện tại.

## Đợt 5b — bám slide

- [x] Xác nhận HEAD/nhánh, đọc code/test/tài liệu Đợt 5; baseline Node 104 và JUnit 35.
- [x] Tomcat QA WAR trước sửa; lưu golden-before cho bốn demo và PDF builder.
- [x] Viết guard cú pháp trước, chạy đỏ, sau refactor chạy xanh.
- [x] Refactor collection/luật bằng for/if, Date/SimpleDateFormat, getter boolean,
  String url + forward; không đổi SQL, giao diện hay CSS/JS.
- [x] Giữ 10 test Java cũ, đổi Date; thêm 2 test. Node 105/105, JUnit 37/37,
  `mvn clean package` Java 17 thành công.
- [x] Golden-after/diff của cả bốn demo rỗng; guest 302/no-store, POST/PUT 405,
  owner query không đổi, HTML không lộ template/null, giờ Việt Nam đúng/log sạch.
- [x] PDF A4 builder vẫn 3 trang; text/pixel giống trước khi loại giờ lập;
  Kỹ năng đi cùng bảng, có số trang CSS. Chưa kiểm chứng Firefox/Safari.
- [x] Tài liệu chapter/slide + 3 Hỏi–Đáp, README/API/ERD/handoff được đồng bộ;
  code cũ ngoài phạm vi chỉ liệt kê, không sửa.
- [x] Dừng/dọn QA/log/cookie/cấu hình tạm, kiểm cổng 8081/9224 đóng; review diff.

Bàn giao bằng 1 commit local `refactor: align learning profile with course
slides`, không push; AGENT_HANDOFF.md tiếp tục untracked.
