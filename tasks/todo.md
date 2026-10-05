# Đợt 2 — Lái thử mô hình 3D

## Đợt 6 chặng 2 — Chẩn đoán tương tác và mẫu B

- [x] HEAD a1650e0; chỉ handoff untracked; baseline Node114/JUnit52.
- [x] Bốn tài khoản login;77 trang golden trước sửa, QA8081 riêng.
- [x] Test bean/nguồn chạy đỏ trước triển khai; giữ test cũ.
- [x] Migration010/seed additive chạy lại được; schema_migrations có010.
- [x] Bốn bean diagnosis, DB/Servlet/JSP/form/role/CSRF/ẩn dữ kiện chưa được mở.
- [x] Rubric B/slot TASK/snapshot/nguồn dùng lại/chấm, A giữ điểm và luật cũ.
- [x] Node119/JUnit59, clean package JDK17; guard bắt stream và dòng quá dài trên bản sao.
- [x] Golden77:66 giống nguyên HTML,11 form ADMIN đổi radio; ngoài form77 giống.
- [x] Bản WAR cuối69 trang chi tiết A giống bản after; nhãn/điểm USER không đổi.
- [x] Builder nộp/chấm/bổ sung/dùng lại hoặc lượt chẩn đoán mới; practice quiz/chẩn đoán.
- [x] Kiểm âm quyền/CSRF/nguồn/điểm/hạn/trạng thái; ba tranh chấp bằng hai luồng và SQL.
- [x] Chrome JS-off form thật,390px không tràn; role link USER/ADMIN.
- [x] README/API/ERD/slide/FAQ/demo/seed/report và handoff cập nhật.
- [ ] Nộp đầy đủ bằng USER practice: thiếu phiên COMPLETED, chưa được phép tạo qua luồng cũ.
- [ ] Firefox/Safari và việc bảo vệ bài thi kín (ngoài phạm vi).
- [x] Dừng/dọn QA8081; giữ175 file bằng chứng không bí mật ở target, không stage target/handoff.

Bàn giao bằng một commit local chẩn đoán và mẫu B, không push. Các giới hạn
chưa tick ở trên vẫn còn; không coi bộ test xanh là full-flow hai USER đạt.

## Đợt 6b — Định dạng code và hai sửa nhỏ

- [x] Xác nhận HEAD a1273fe; chỉ handoff untracked, không có file lạ đang sửa.
- [x] Baseline Node111/JUnit51 trên JDK17; build WAR trước sửa.
- [x] Đăng nhập ba USER và ADMIN; chụp77 trang GET trước sửa, dừng QA.
- [x] Node guard định dạng/mã lỗi/getter chạy đỏ; JUnit mới xác nhận đỏ với
  bean baseline trên bản sao tạm, tránh class Java25 do IDE ghi vào target.
- [x] Định dạng 15 file Java (các phần mở rộng cũ đúng phạm vi) và chín JSP;
  Javadoc ngắn, braces, giữ literal SQL và chữ ký/thứ tự transaction.
- [x] Getter showCurrentRoundQuiz chỉ ACTIVE; hai view dùng getter;
  GET action lạ trả task-error422; lịch sử nộp/chấm giữ nguyên.
- [x] Node114/JUnit52 xanh; guard slide-style bắt stream chèn vào bản sao.
- [x] Golden77 trang:71 giống nhau, sáu view builder chỉ bỏ khối quiz hiện tại;
  nhiệm vụ11 đang chờ chấm cũng SUBMITTED nên bị ẩn theo đúng getter ACTIVE.
- [x] 60 kiểm âm đạt; digest/số dòng tám bảng giống trước, không ghi DB.
- [x] Jasper biên dịch chín JSP,0 lỗi; log QA không có exception ứng dụng.
- [x] Tài liệu API/plan/todo/báo cáo và handoff được cập nhật, handoff ngoài Git.
- [x] Kiểm lại WAR cuối, dừng/dọn QA8081; kiểm diff và stage đúng file cho commit local.
- [ ] Firefox/Safari và hai USER cùng nộp (không kiểm trong đợt chỉ đọc này).

Bàn giao một commit local `refactor: format practice task code and tidy task
status labels`; không push. Chi tiết diff và độ dài file ở PHASE6B_QA_REPORT.

## Đợt 6 chặng 1 — Nhiệm vụ thực hành

- [x] Khảo sát repo/code/slide guard; HEAD thực tế f654840, ba file trang chủ sạch.
- [x] Baseline Node105/JUnit37; DB có quyền CREATE; bốn demo đăng nhập được sau
  khi chủ tài khoản cung cấp mật khẩu ADMIN hiện hành; không đổi tài khoản.
- [x] Sửa audit 5b và tạo commit docs riêng 6018d7b.
- [x] Test-first đỏ; triển khai bean/DB/Servlet/JSP/form/CSRF, không dùng kỹ thuật cấm.
- [x] Migration009 chỉ thêm năm bảng; fold schema; đã chạy trên DB local.
- [x] Node111/JUnit51, clean package JDK17 thành công; WAR bytecode61.
- [x] Guard xanh; stream chèn vào bản sao tạm bị guard bắt; bản sao được dọn.
- [x] QA giao/công bố/quiz/preview/nộp/bổ sung/nộp lại/chấm, lịch sử sửa;
  hai cách quiz vòng2 ở builder theo lựa chọn người dùng.
- [x] QA quyền/CSRF/bằng chứng/hạn/lượt/ngưỡng/feedback/OPEN core/sửa chấm;
  confirm và quiz đồng thời mỗi cặp đúng một thành công, còn lại422.
- [x] Smoke các trang/API cũ, API quiz luyện201 không đổi lượt chốt.
- [x] Chrome giao diện đăng nhập, link đúng role, mobile390px và full luồng
  nộp/chấm với JavaScript tắt; đã xem ảnh.
- [x] Docs bảng chapter/slide + Hỏi–Đáp/demo/API/ERD/README/QA report.
- [x] Dừng Tomcat QA8081, dọn cấu hình/log/cookie/password/npm tạm.
- [x] Kiểm diff, stage file cụ thể; cập nhật handoff ngoài Git.
- [ ] Hai USER khác nhau đều hoàn tất/nộp (chưa có dữ liệu; đã thống nhất QA builder).
- [ ] Firefox/Safari và toàn bộ thao tác lái3D/checkout cũ (chưa kiểm chứng lại).

Danh sách [QA] còn trong DB và giới hạn ở docs/PHASE6_QA_REPORT.md; không xoá
dữ liệu đã nộp. File AGENT_HANDOFF.md vẫn untracked, không stage.
Bàn giao bằng commit feat local sau khi review/stage; không push. Hash xem git log.

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
