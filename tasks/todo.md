# Đợt 2 — Lái thử mô hình 3D

## Redesign JSP học tập — 06/10/2026

- [x] Git/HEAD thực tế, đọc tài liệu/code/guard; manifest34 trang + fragment.
- [x] Baseline thực chạy Node128/128, JUnit79/79; WAR JDK17 thành công.
- [x] Lưu HTML trước sửa và ảnh đại diện bằng Chrome, credentials ngoài bằng chứng.
- [x] Pilot3 trang được kiểm runtime/form/SVG và commit local 2fc82ce.
- [x] Nhóm thông tin/tổng kết/hồ sơ giấy được kiểm render/PDF; commit ac0feed.
- [x] Nhóm nhiệm vụ/chẩn đoán được kiểm form/EL/không lộ thêm đáp án; commit 0b9daaa.
- [x] Admin/hỗ trợ được kiểm bản chụp/chỉ đọc/role; commit ca0ffb7.
- [x] Toàn manifest, viewport/JS-off/hồi quy/test cuối; runtime 8081 đã dọn, log không có JasperException.
- [x] Báo cáo đúng giới hạn, commit đúng file từng mốc; không push, handoff ngoài commit.

## Đợt 6d — Sửa bộ lọc nháp và phủ guard JSP

- [x] HEAD d608401/ahead4; chỉ handoff untracked, không có file người dùng đang sửa.
- [x] Baseline Node119/JUnit59; WAR trước sửa bằng JDK17; bốn tài khoản đăng nhập được.
- [x] Golden239 GET trước/sau,106 trang200; chỉ POST đăng nhập, không POST chức năng.
- [x] Guard/source test chạy đỏ đúng admin-task-list.jsp trước khi sửa.
- [x] Radio state đủ năm giá trị/nhãn; checked theo taskStateFilter đã chuẩn hóa từ request.
- [x] Rà15 JSP Đợt6; không phát hiện lỗi đóng/cân bằng thẻ khác; chỉ sửa khối bộ lọc.
- [x] Guard tự liệt kê bốn họ JSP, chặn select/option/closing attributes và cân bằng11 thẻ khối.
- [x] Bản sao JSP thật chèn select và closing option sai đều bị bắt; kiểm future JSP/EL/void.
- [x] Node121/JUnit59, clean package xanh; JspExpressionContract và guard slide/định dạng xanh.
- [x] Golden233 GET giống,6 admin list chỉ đổi bộ lọc; toàn bộ204 USER GET giữ nguyên.
- [x] Admin lọc đủ trạng thái đúng SQL/checked; zzz200 trước/sau; khách302/no-store, ba USER403.
- [x] Chrome tắt JS chọn Nháp→GET→đúng #20/#10; ảnh390px đã xem, scrollWidth390.
- [x] Hash16 bảng không đổi sau toàn bộ QA; không migration, không sửa dữ liệu hay tài khoản.
- [x] Dừng8081/dọn config/log/work/npm; lưu492 file bằng chứng ở target/phase6d-qa (gitignored).
- [x] Báo cáo6d cuối PHASE6C_QA_REPORT và handoff cập nhật; README/API/ERD không cần đổi.
- [ ] Firefox/Safari; hai USER nộp mẫu B vẫn cần hoàn tất lắp ráp thật, ngoài QA chỉ đọc đợt này.

Bàn giao một commit local `fix: repair admin task state filter and extend practice view guards`, không push.

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

## Đợt 7 — Phòng nối dây, chặng 1

- [x] Push năm commit Đợt6 theo yêu cầu trực tiếp, fetch xác nhận c2afc9b.
- [x] Khảo sát, baseline121 Node/59JUnit; sao lưu bằng chứng QA cũ ngoài repo.
- [x] ERD trước migration011; khóa ngoại ghép, version, nullable kết quả, FK RESTRICT.
- [x] Test-first đỏ rồi xanh; JavaBean/grade/owner/CSRF/slide/format/HTML guards.
- [x] Servlet/JSP + SVG/JS; form radio đầy đủ khi JS tắt; account links đúng vai trò.
- [x] Migration/seed local; đối chiếu wiring DB/code; seed chạy lại không ghi đè.
- [x] Live hai USER/Admin, SQL điểm, stale version, submit đồng thời, archive/continue.
- [x] Chrome click/drag/keyboard/touch/JSoff/390; ảnh đã xem, console không lỗi ứng dụng.
- [x] Review độc lập và sửa UTF-8/nhãn lưu/collation/guard/label SVG/công thức tie.
- [x] Build cuối72JUnit/127Node; QA WAR cuối19Chrome xanh, runtime8081 đã dọn; commit local chặng1.
- [x] Mở chặng 2 sau gate chặng 1 (review đạt, build/QA cuối xanh, commit a1822fd).

## Đợt 7 — hỗ trợ Admin–User, chặng 2

- [x] Gate chặng 1 đạt, commit local a1822fd trước khi triển khai.
- [x] Test-first Node/JUnit đỏ do chưa có implementation, sau đó xanh.
- [x] ERD trước migration 012; hai bảng với FK, UNIQUE active, CHECK snapshot/version.
- [x] Snapshot server, tin bất biến, trạng thái và version trong bean; form không JS.
- [x] USER/Admin session/CSRF/owner, radio đầu nối, account links/cache-busting.
- [x] Live 85 ca API/SQL; hai USER/Admin; create/reply đồng thời, stale, snapshot/score bất biến.
- [x] Chrome 12 ca; JS-off, 390px, bản chụp/SVG trước-sau, ảnh đã xem, XSS chữ.
- [x] 14 HTML/JSON cũ diff rỗng (token/giờ lập được chuẩn hóa), 5 trang admin cũ 200.
- [x] Review độc lập; sửa MEDIUMTEXT, CHECK NULL và thụt lề; worst-case snapshot test.
- [x] Tài liệu/API/ERD/demo chapter-slide và QA/handoff đồng bộ.
- [x] Build cuối79JUnit/128Node, QA Unicode+GET cuối, log sạch; runtime đã dọn; bàn giao commit local.

## UI-2 — Rà soát giao diện và chữ viết, 06/10/2026

- [x] Xác nhận Git/đọc skill áp dụng, manifest 34 và guards/fixture; baseline131Node/79JUnit.
- [x] Golden327GET bằng bốn demo, 283HTML200; hợp đồng form/HTTP/cache/dữ liệu hiển thị và hash38bảng.
- [x] Chrome32view server × 5widths; 2view HTML tạm × 5widths; matrix136ô ghi rõ128server/8tạm.
- [x] Rà menu, badges, enum/copy, mã chân, màu lead, chữ metric; đọc inventory793→843mảnh văn bản.
- [x] Zoom200%, chuỗi dài/HTML dạng chữ,12tin và80dây bằng DOM tạm; không gửi mutation.
- [x] Wiring6nhóm không lưu: click/drag/keyboard/delete/reload/JS-off; menu details bàn phím.
- [x] 327response diff:0form/HTTP/cache/mất số-mã-ngày;38bảng không đổi;40GETquyền/hồi quy đạt.
- [x] Node133/133, JUnit79/79; Maven test/clean package JDK17 thành công, guard cũ không nới.
- [x] PDF hồ sơ3trang và phiếu1trang, render/xem mọi trang; ẩn link in gây trang gần trống.
- [x] Báo cáo trong file UI hiện có, bảng chuỗi/matrix/inventory/giới hạn; handoff ngoài commit.
- [ ] Kiểm task-preview/diagnosis-play với trạng thái server thật trong đợt cho phép ghi.
- [ ] Firefox/Safari, cảm ứng vật lý, screen reader, Ctrl+Plus và Tab mọi control.
- [ ] Sửa dữ liệu bài nối dây cũ lỗi dấu ? / getter ngày ISO: cần phạm vi riêng, chưa làm.

- [x] Dừng đúng runtime QA, xác nhận 8081 đóng; xóa CATALINA_BASE/log/cookie tạm, giữ evidence ngoài Git.

## Tích hợp Nhi/Tuấn Anh — 12/10/2026

- [x] Fetch đúng hai nhánh, backup fb07191 và giữ riêng mọi sửa dở của người dùng.
- [x] Baseline sạch JDK17; review source trước merge, tái hiện lỗi guard raw merge.
- [x] Sửa tương thích client/paging/fallback/race/CSS scope cho thư viện.
- [x] Lịch sử PRACTICE owner-scoped, điểm/giờ/phân trang trong JavaBean; JSP core/shell.
- [x] Bỏ feedback lặp; giữ form POST, rubric và toàn bộ source lắp ráp/shop.
- [x] Node160/160, JUnit85/85, clean package JDK17; review độc lập không còn blocker.
- [x] Live79 + extra17 kiểm đạt; hash38/38bảng không đổi, không chạy seed/migration.
- [x] Cập nhật README/API/demo/report; bằng chứng QA ngoài Git.
- [x] Runtime QA đã dọn, cổng8081 đóng; merge commit và fast-forward là bước bàn giao sau gate.
- [ ] ADMIN live (credential không đăng nhập được), Firefox/Safari/cảm ứng vật lý.
- [ ] Push merge lên GitHub: chưa có yêu cầu riêng trong lượt này.

## Đợt 8 — Phòng lắp ráp có kiểm chứng

- [x] M0: kiểm Git, baseline thực chạy và thiết kế ERD trước migration.
- [x] M1: prototype kéo/chọn/gắn và tách điều khiển camera.
- [x] M2: migration/seed và JavaBean có test từng luật.
- [x] M3: duyệt DDL, API/persistence owner/version/transaction.
- [x] M4: kit obstacle-avoider, khay, phản hồi và khôi phục.
- [x] M5: report/complete/khóa/phiếu JSP.
- [x] M6: QA thật/browser/concurrency/hồi quy/cleanup (giới hạn ghi trong báo cáo Đợt8).

## Đợt 8b — vòng sửa nhỏ sau review

- [x] M1: spec v2 có hướng, preview/snap dùng góc tuyệt đối, README và test.
- [x] M2: lời giải thích nêu tên từ layout, câu chung không lộ tên, quote/textContent.
- [x] M3: tìm nguyên nhân parse trước khóa và chuẩn hóa SESSION_LOCKED, giữ retry.
- [x] M4: count owner/spec trong SELECT, JSON và DETAILED X/N, GROUP%, cache-busting.
- [x] M5: dọn chữ phiếu, API/ERD/kiến trúc/demo/vấn đáp theo code.
- [x] M6 kiểm chứng:172 Node/97 JUnit/package, HTTP/SQL/Chrome, hash và cleanup8081.
- [x] M6 chốt: người dùng chọn giữ tư thế v1/v2, không tạo v3; ghi rõ giới hạn hình khối động cơ.
- [x] M6 bàn giao: test xanh, file WIP giữ nguyên; commit local đúng phạm vi, không push.

## Đợt 9 — Sau khi hoàn tất

- [x] M0: baseline, kế hoạch, ADMIN QA riêng, camera/mobile và góc khay; build/test xanh.
- [x] M1: nhật ký cùng transaction, ERD/migration014, bean/DB/test và HTTP thứ tự.
- [x] M2: bản vẽ SVG theo dữ liệu phiên và test tỉ lệ; render thật nối ở M3.
- [x] M3: trang hoàn thiện Servlet/JSP bốn phần, điều hướng/in/quiz;51 ca runtime, PDF3 trang.
- [x] M4: events API owner và replay chỉ đọc; playback đầy đủ kiểm sau M5 hoàn tất.
- [x] M5: undo có bean/version/khóa, không Redo; HTTP/concurrency/Ctrl+Z và replay đầy đủ đã kiểm.
- [x] M6: HTTP/SQL/Chrome/PDF/hồi quy, tài liệu/vấn đáp và cleanup8081; không push.

## Đợt10 — Xưởng thiết kế riêng

- [x] M0: baseline, ERD trước code, bảo vệ file và dữ liệu cũ.
- [x] M1:015/seed v1/rule bean/reader, MySQL FK/cột sinh kiểm thật.
- [x] M2: kit Servlet/JSP/POST/PRG, nhập đơn hai lần không cộng trùng.
- [x] M3: tạo/đổi/xóa owner, KIT validation/snapshot, CSRF/version.
- [x] M4: API/transaction/placement/undo,3D adapter, HTTP/SQL/Chrome.
- [x] M5: ba lớp JavaBean, CHECK dưới khóa, xóa kết luận khi đổi bố cục.
- [x] M6: hồ sơ/BOM/cây/bản vẽ/in, crowded30, thống kê lỗi học tập dùng chung.
- [x] M7: user duyệt v2, seed/diff/idempotence, pose7/8, final192 Node/126 JUnit và cleanup8081.

## Đợt10b

- [x] M1: migration016/seed/catalog, tên từ DB, test và14 ca nhãn thật.
- [x] M2: bounds/FOV camera, nút focus/bệ/sàn,16 camera và4 motion/keyboard checks.
- [x] M3: tài liệu/vấn đáp,197 Node/127 JUnit và clean package JDK17, WAR smoke/reload, dọn QA8081.


## Đợt11 — Điều hướng và kết nối

- [x] M0: baseline,120GET ba vai trò, SITE_MAP và kế hoạch,10 hash WIP.
- [x] M1: fixture/menu18HTML +JSP, role từserver, bànphím/mobile/thu gọn.
- [x] M2/M6: trangchính/minhhọa và hồsơin; giữroute/module dữliệu.
- [x] M3: dashboard Servlet/bean/XxxDB/JSP, empty/zero test.
- [x] M4: context theo robot/component; formCSRF/owner/transaction/idempotent bổsunggiỏ.
- [x] M5: coursemap15chủđề, sourcefile:dòng đãđối chiếu, không bịa slide.
- [x] M7: breadcrumb,404/500/Throwable, APIJSON, filterHTMLadmin.
- [x] M8:205Node/130JUnit, cleanpackage17,207ôChrome/HTTP3vai trò/PDF/ảnh và cleanup.

## Đợt 11b — sửa review

- [x] Khảo sát Git, hash10 file, review và baseline Node/JUnit JDK17.
- [x] M0: source/HEAD/artifact độc lập; target gốc JDK17, HTTP đúng docBase IDE.
- [x] M1: canonical/hash cũ, tải chậm/fallback/focus không giật, ID đặc biệt.
- [x] M2: nội dung đúng vai trò, auth chậm/lỗi đóng quyền, JSTL giữ kiểm server.
- [x] M3: SKU trang2, GET additive, race/error/stock/ID trùng, không ghi dữ liệu.
- [x] M4: nghiệp vụ dashboard trong bean, dữ kiện gộp/LIMIT,8 JUnit bổ sung.
- [x] Test/build/HTTP/browser và đối chiếu byte, hash WIP/DB, tài liệu UTF-8.
- [x] Dọn đúng runtime QA8081/8082; kiểm Maven test và npm test lại tại repo gốc.
- [x] Xác nhận compiler17 sau Reload Projects/restart language server/Full Compilation; xem evidence ide-java17.
- [x] Người dùng Run IntelliJ8080; kiểm JDK17, docBase và byte HTTP ngày16/10; không tự can thiệp server.

### Chốt16/10/2026

- [x] Đồng bộ version site-nav.js giữa JSP và mọi HTML, có guard hồi quy.
- [x] Ba trang shop chỉ có một nav chính, nav cửa hàng có tên riêng nhìn thấy được.
- [x] Link/form/control/id/data không đổi;218 Node/138 JUnit và21 ô browser/keyboard đạt.
- [x] Hash WIP/DB giữ nguyên, dọn QA8081; không tự can thiệp IDE/Tomcat8080.
- [x] Sau xác nhận mở VS Code: compiler/runtime17,203 class target major61; PID language server giữ nguyên.
- [x] Người dùng Run IntelliJ8080:177 class deploy major61, byte source/menu/version HTTP khớp;6 ô Chrome đạt.
- [x] Sau Run:218 Node/138 JUnit,0 fail/skip; clean package17 ở thư mục riêng, giữ nguyên target đang phục vụ.

## Đợt11c — Hành trình học tập

- [x] M0: Git/hash/review/source,218 Node/138 JUnit/package17 riêng,17 trang Chrome baseline.
- [x] M1: menu/dashboard→bộ chọn quiz→đúng bài; lỗi/rỗng/quyền, không ghi dữ liệu.
- [x] M2: cộng số phiên trong JavaBean bằng for, guard và JUnit giữ số liệu.
- [x] M3: dashboard ưu tiên học, layout riêng, mobile/zoom200/empty DOM/long.
- [x] M4: bài/lịch sử nối dây, chẩn đoán/tra cứu gọn, giữ dữ liệu và context.
- [x] M5 phần JSP: chi tiết dùng trạng thái bean/cờ muộn; danh sách có link mở trạng thái.
- [x] M5 phần danh sách: sau duyệt riêng GET, map TaskRecipient theo user, một query gộp;
  19 nhãn khớp chi tiết của2 USER,6 ô viewport, không ghi DB. Chỉ stage hunk2 dòng GET.
- [x] M6: tổng kết→review/quiz đúng mẫu/bản in; không đổi định nghĩa điểm.
- [x] M7: browser/DB read-only/HEX, danh sách QA và đề xuất chờ quyết định; không dọn DB.
- [x] M8:229 Node/144 JUnit ở working tree và commit sạch,57 ô Chrome/16 kiểm hành trình-quyền,
  10 ca browser fixture,27 smoke,3 form contract không đổi; hash12 file/55 bảng giữ nguyên.
- [x] M8 runtime: chỉ chạy bản mới trên QA8081 rồi dọn;8080 không redeploy, không push.
- [x] Bổ sung M5:231 Node/149 JUnit,0 fail/skip, build17 working tree/commit sạch;
  WIP cũ/POST giữ nguyên byte khi bỏ2 dòng GET mới;11 hash còn lại và55 bảng giữ nguyên.

## Chốt nghiệm thu11c và demo final — 16/10/2026

- [x] Xác minh HEAD2ff67f1/branch/index, không delta source; snapshot12 hash mới ngoài repo.
- [x] Archive commit sạch, JDK17 clean package;231 Node/149 JUnit,0fail/skip.
- [x] Manifest WAR/checksum,179 class major61,166 tài nguyên và HTTP QA8081 khớp archive.
- [x]31 kiểm browser,27 smoke,10 fixture quiz;390/768/1440, keyboard/quyền/owner; không ghi dữ liệu thử.
- [x] Xác nhận bài nối dây công bố7 và21 nhiệm vụ tên QA còn tồn tại; chỉ báo cáo, không dọn.
- [x] Kịch bản3–5 phút +7 câu vấn đáp + file:dòng + cờ ghi DB, hướng dẫn Run/redeploy.
- [ ] Người dùng cập nhật8080; sau xác nhận đối chiếu asset/quiz/dashboard/nhãn nhiệm vụ trên8080.
- [ ] Người dùng chốt dữ liệu demo hiện tại có kiểm soát hoặc phương án DB riêng.

## Tập dượt riêng và ôn vấn đáp — 17/10/2026

- [x] Chụp mốc dữ liệu dành live, WIP/cấu hình và DB cũ trước tập dượt.
- [x] Một nhiệm vụ A riêng, một phiên mới, quiz TASK đúng vòng, nộp và chấm một lần.
- [x] Xác minh hai USER, responsive, dữ liệu dự phòng/live và WIP giữ nguyên.
- [ ] Cập nhật hướng dẫn demo, bản đồ code và câu hỏi vấn đáp đúng Model 2.
- [x] Dừng/dọn QA8081 riêng; không đổi8080, không sửa source, không push.
- [x] Tạo/công bố nhiệm vụ2 riêng; dừng khi API trả lại phiên2 dành live.
- [x] Đối chiếu12 snapshot dành live,44 hash file,51 bảng demo ngoài phạm vi ghi;
  DB cũ55/55 bảng khớp. Chưa tạo phiên/quiz TASK/bài nộp/lần chấm mới.
- [x] Người dùng duyệt dùng USER3 tiếp tục nhiệm vụ2, giữ nguyên phiên1/2 của Tài.

## Kéo thả toàn bộ mẫu — 17/10/2026

- [x] Đặc tả/test RED seed4 mẫu, kit/cha/góc tương thích config.
- [x] Seed cộng thêm + kiểm GREEN; không đổi spec/phiên cũ.
- [x] API mở phiên DETAILED riêng, chống tạo trùng; UI hướng dẫn GROUP cũ.
- [x] npm/Maven JDK17; QA DB cô lập, cả5 mẫu, sai/đúng/undo/refresh/replay.
- [x] Đối chiếu WIP/DB thật/DB demo; cập nhật README/API/demo/report; không push.
- [x] DB demo final đã backup/restore thử → seed cộng thêm → WAR mới riêng8081;
  không áp dụng vào DB làm việc hoặc cấu hình8080 của người dùng.

## Triển khai vào demo final — 17/10/2026

- [x] Backup private + restore thử54 bảng vào DB mới, đối chiếu hash.
- [x] Seed đúng DB demo, chỉ4/43/43 dòng mới ở3 bảng; dữ liệu cũ/live nguyên vẹn.
- [x] WAR bất biến + Tomcat riêng8081, smoke3 vai trò; không đổi8080/.env/IDE.
- [x] Chuẩn bị4 phiên USER B0/N, không điểm/bài nộp/lần chấm mới.
- [x] Hướng dẫn mở/dừng/demo, backup/rollback, WIP hash và commit tài liệu; không push.

## Sửa kéo-thả và triển khai đúng 8080 — 17/10/2026

- [x] Xác minh runtime/JNDI, snapshot WIP; baseline sạch 237 Node/159 JUnit.
- [x] Clone đúng DB nguồn 55 bảng; tái hiện UI GROUP và lỗi 422 thiếu spec bốn mẫu.
- [x] Seed trên clone: rollback dòng dữ liệu khi lỗi; chạy lại không đổi dữ liệu;
  chỉ thêm ba bảng spec/slot/orientation, không đổi bản PUBLISHED trước đó.
- [x] Đường vào DETAILED mặc định, giữ URL GROUP cũ và lỗi server rõ ràng.
- [x] Kéo-thả thật năm mẫu, camera/marker chiều cao, reload/undo/complete.
- [x] Ma trận UI45 ca, build sạch253 Node/159 JUnit; hồi quy Xưởng/replay/quiz.
- [x] Người dùng duyệt DB/artifact/restart8080 và riêng1 QA USER/5 phiên mới.
- [x] Backup restore55 bảng, seed4/43/43; WAR sạch vào đúng8080, Stop/Start đạt.
- [x] Nghiệm thu8080:53 gắn thật/5 từ chối/15 viewport; mọi dòng cũ và WIP nguyên.
- [x] Báo cáo/hướng dẫn phân biệt8080/8081; dọn riêng QA8082, không push.
