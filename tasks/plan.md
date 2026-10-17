# Implementation Plan: Đợt 2 — Lái thử 3D và Đợt 3 — Hai mẫu robot mới

## Redesign JSP học tập — 06/10/2026

34 trang độc lập + fragment wiring-diagram; manifest và bằng chứng trong
docs/UI_REDESIGN_QA_REPORT.md. Giữ Servlet/bean/JDBC, mọi form/token/version,
rubric và điểm. Không schema, không đổi trang HTML/3D, không push.

1. Khảo sát/manifest/baseline: Node128, JUnit79; local Poppins/Heroicons.
2. Pilot receipt + wiring list/play, kiểm runtime rồi mới nhân rộng.
3. Tổng kết/thông tin/profile: shell tối, giấy và PDF trắng, bảng ngữ nghĩa.
4. Nhiệm vụ/chẩn đoán: card, nhóm form, không thay điều kiện lộ đáp án.
5. Admin/hỗ trợ: biên soạn rõ nhóm, snapshot chỉ đọc, lịch sử bất biến.
6. Manifest đầy đủ, form contract trước/sau, viewport/JS-off/PDF/hồi quy.

CSS riêng .learning-workspace, include JSP đơn giản; selector SVG giới hạn
đúng sơ đồ. Bằng chứng ngoài Git, runtime riêng8081 và dọn bí mật khi xong.
Commit local từng mốc đã kiểm; bảo toàn AGENT_HANDOFF.md ngoài commit.

Các mốc UI đã có f5e8446/2fc82ce/ac0feed/0b9daaa/ca0ffb7. Hoàn thiện guard
giữ form/source ban đầu, focus SVG chỉ bật cùng JS và favicon cục bộ; chạy
WAR cuối trên runtime sạch. Báo cáo ghi rõ CSS zoom/touch emulation, giới
hạn Firefox/Safari/thead nhiều trang và các luồng nghiệp vụ không lặp lại.

## Đợt 6 chặng 2 — Chẩn đoán tương tác và mẫu B

- Khảo sát HEAD a1650e0, baseline Node114/JUnit52; đăng nhập bốn demo và lưu
  golden77 trang trước sửa. Giữ mọi dữ liệu cũ và handoff ngoài commit.
- Test trước: bean/nguồn đỏ vì lớp và view chưa có. Mở rộng guard slide-style
  và định dạng, giữ toàn bộ test cũ.
- Migration010 thêm năm bảng và liên kết nullable; chỉ đổi hai CHECK điểm
  tối thiểu và mở ENUM rubric. Seed năm tình huống chính thức INSERT IGNORE.
- Bean chứa luật công bố, chọn quan sát, kết luận 4/3/3, nguồn bằng chứng và
  điểm B. JDBC/transaction giữ thứ tự task → recipient → round; conclude khóa
  riêng lượt. JSP/form POST thuần, radio, role/CSRF/no-store như môn học.
- Kiểm chứng điểm/nguồn/ẩn đáp án/quyền/ba tranh chấp thật; Chrome JS-off và
  390px. So golden A:66/77 nguyên HTML;11 form ADMIN khác vì radio thay select,
  ngoài form cả77 giống. Bản cuối69 trang chi tiết A giống bản after.
- USER practice chưa có COMPLETED: phần nộp đầy đủ hai USER chờ quyết định;
  không tự tạo/sửa phiên cũ. Hai cách vòng bổ sung đã chạy bằng builder.
- Tài liệu slide/FAQ/seed/demo và báo cáo PHASE6C_QA_REPORT; một commit local
  do diagnosis và B phụ thuộc nhau. Không push, dọn Tomcat8081 và bí mật tạm.

## Đợt 6b — Định dạng code và hai sửa nhỏ

- Giữ SQL, chữ ký hàm, luật điểm/vòng/chấm và thứ tự transaction của Đợt 6.
  Tách câu lệnh, bọc control flow bằng ngoặc nhọn, giới hạn Java120/JSP140;
  thêm Javadoc ngắn và guard định dạng bên cạnh guard slide-style đang có.
- Chỉ đổi hai hành vi: getter quiz của vòng hiện tại chỉ cho ACTIVE; GET
  action không hỗ trợ trả task-error422, không dùng404 cho nhánh dispatcher.
- Baseline111 Node/51 JUnit; chụp 77 trang của ba USER và ADMIN trước/sau.
  Đối chiếu SQL/mã Java và digest tám bảng để kiểm không đổi dữ liệu.
- Không migration hoặc POST có thể ghi dữ liệu. Chỉ GET/POST chắc chắn bị
  từ chối; biên dịch chín JSP bằng Jasper; build JDK17 và dọn QA8081.
- Báo cáo chi tiết ở docs/PHASE6B_QA_REPORT.md; một commit local, không push.
  AGENT_HANDOFF.md nằm ngoài commit; không đụng file người dùng đang sửa.

## Đợt 6 — Nhiệm vụ thực hành và đánh giá, chặng 1

Mục tiêu: luồng ADMIN giao → USER quiz/bằng chứng/giải thích → xem trước/nộp →
ADMIN chấm → bổ sung/nộp lại, mẫu A cố định 40/40/20. Không triển khai mẫu B
hay chẩn đoán (chặng 2), không sửa phòng 3D/hồ sơ/tổng kết/shop.

- Model 2: ba Servlet, sáu JavaBean, hai XxxDB; mở rộng QuizAttemptDB dùng
  chung luật chấm, thêm USER lookup và CSRF hidden form. JSP chạy khi tắt JS.
- Migration 009 bổ sung năm bảng, FK RESTRICT/UNIQUE/CHECK/index; nguồn phiên/
  quiz từ bảng cũ, bản chụp và đánh giá bất biến. Khóa task/recipient/round,
  commit/rollback; điểm lưu BigDecimal HALF_UP một chữ số rồi dùng để so ngưỡng.
- Luật bean và guard slide-style được viết test trước. Baseline 105 Node,
  37 JUnit; cuối 111 Node, 51 JUnit, Maven clean package JDK17 thành công.
- QA Tomcat8081/MySQL thật qua bốn demo, có concurrency confirm/quiz, quyền/
  CSRF/bằng chứng/hạn/lượt/đánh giá, Chrome desktop/mobile/NoJS. Chỉ builder có
  phiên hoàn tất; người dùng chọn hai nhiệm vụ builder cho hai cách quiz vòng2.
- Bảng chương/slide, Hỏi–Đáp và kịch bản demo ở TEAM_FLOW_DEMO_GUIDE;
  ma trận chi tiết/dữ liệu/giới hạn ở PHASE6_QA_REPORT.
- Chỉ stage file thuộc chặng1. Giữ ba file trang chủ; AGENT_HANDOFF ngoài commit.
  Hai commit local (docs ghi chú 5b và feat chặng1); không push.

Rủi ro đã kiểm: ID/điểm hidden bị sửa, quiz chốt bị thay, nộp/chấm đồng thời,
sửa chấm sau hoạt động, phân trang người được giao, đề quiz hiện hành thay đổi.
Quiz không phải thi kín; chặng1 chưa đóng băng đề cho cả nhiệm vụ.

## Đợt 4 — Cửa hàng linh kiện và giỏ hàng mô phỏng

### Mục tiêu và giới hạn

- Cho người học xem linh kiện có giá tham khảo/tồn kho, thêm vào giỏ đăng nhập,
  xác nhận đơn hàng mô phỏng và xem lịch sử của chính mình.
- Admin được tạo, sửa, bật/tắt sản phẩm. Xóa sản phẩm là ngừng kinh doanh
  (`is_active = false`), không xóa cứng dữ liệu lịch sử.
- Không tích hợp thanh toán, ví, thông tin thẻ hay dịch vụ giao hàng thật. Giá
  hiển thị bằng VND và do server đọc/tính từ MySQL.
- Giữ Java Servlet/JSP Model 2: HTML/JS → Servlet kiểm tra request/session/CSRF
  → JavaBean → `XxxDB` dùng `PreparedStatement`/ConnectionPool → MySQL.

### ERD và quy tắc dữ liệu (phải duyệt trước migration)

- `shop_products`: mã sản phẩm, liên kết duy nhất tới `components`, giá VND,
  số lượng tồn, cờ hoạt động và timestamps. Dữ liệu kỹ thuật vẫn ở `components`.
- `cart_items`: khóa ghép `(user_id, product_id)`, số lượng dương; không có
  `user_id` trong body — Servlet lấy từ `HttpSession`.
- `orders`: chủ sở hữu, trạng thái `CONFIRMED`/`CANCELLED`, tổng tiền server
  tính và thời điểm tạo.
- `order_items`: khóa ghép `(order_id, product_id)`, tên sản phẩm và giá đơn vị
  được chụp tại thời điểm đặt, số lượng; FK giữ lịch sử và ngăn xóa sản phẩm đã
  được đặt. Sản phẩm bị ngừng bán vẫn xem được trên đơn cũ.
- Checkout khóa các dòng giỏ của user trước; sau đó khóa sản phẩm theo thứ tự
  ID ổn định (`SELECT ... FOR UPDATE`), kiểm tra trạng thái/tồn kho, tính lại giá,
  trừ kho, ghi đơn và snapshot, xóa giỏ, rồi commit một transaction. Bất kỳ lỗi
  nào đều rollback toàn bộ. Hai lần bấm đồng thời không được tạo hai đơn từ cùng
  một giỏ.
- Không tin `price`, `total`, `userId`, `stock` hoặc trạng thái do client gửi.
  Mọi truy vấn giỏ/đơn đều scope theo user đăng nhập. Request ghi cần CSRF;
  `/api/admin/*` yêu cầu role ADMIN và CSRF.

### Lát triển khai

1. **ERD/hợp đồng:** kiểm tra quan hệ, PK/FK, snapshot, xóa mềm và khóa giao dịch;
   cập nhật `docs/erd.md`, plan, rồi thêm contract tests đỏ cho ERD, thứ tự
   migration, API/auth/CSRF và seed additive trước khi triển khai.
2. **Persistence và nghiệp vụ:** migration `008_shop_cart_orders.sql`, cập nhật
   `database/schema.sql`, JavaBean trong `business/`, `ShopProductDB`, `CartDB`,
   `OrderDB` trong `data/`, transaction checkout; seed 8–12 sản phẩm chỉ INSERT.
3. **Servlet/API:** catalog công khai; cart/order yêu cầu đăng nhập và CSRF cho
   ghi; admin shop CRUD theo khuôn Servlet hiện có; cập nhật `API_CONVENTIONS`.
4. **Giao diện/tài liệu:** catalog cửa hàng, giỏ, JSP lịch sử đơn qua Servlet →
   JSP/JSTL, trang admin, liên kết điều hướng; cập nhật README/demo guide để giải
   thích request → response, phân quyền và snapshot/transaction.
5. **Kiểm thử/runtime:** unit/contract test, Maven + npm test, áp dụng migration
   và seed trên DB local nếu quyền cho phép, kiểm thử browser guest/user/admin,
   lỗi tồn kho, chỉnh giá client, CSRF, ownership và checkout; review diff, commit
   sau khi đợt này qua kiểm chứng. Push thử lên `integration/fullstack-v2`.

### Tiêu chí chấp nhận

- [x] ERD được cập nhật trước migration và nêu rõ PK/FK/cardinality/delete rules.
- [x] GET catalog chỉ công khai sản phẩm active; giá/tồn kho lấy từ DB.
- [x] Giỏ và lịch sử được cô lập theo user; guest/thiếu CSRF/sai role bị chặn.
- [x] Server validate quantity/active/stock; checkout tính lại giá và transaction
  không để kho/đơn/giỏ ở trạng thái dở dang khi lỗi.
- [x] Đơn giữ snapshot bất biến; admin ngừng bán không phá lịch sử.
- [x] Không có thanh toán thật hoặc bí mật trong seed/tài liệu; seed chạy lại an toàn.
- [x] Node/JUnit tests, biên dịch JDK, MySQL migration/seed, Tomcat QA và browser
  smoke test guest đã pass; Maven không cài trong môi trường. Checkout/admin bằng
  user đăng nhập chưa chạy end-to-end để tránh tạo đơn/tài khoản thử trong DB.

### Kết quả kiểm chứng

- `npm test`: 97/97; `node --check` cho các client shop và `git diff --check` pass.
- Maven không có trong PATH; thay bằng biên dịch production/test với JDK 17 và
  chạy 25/25 JUnit tests qua JUnit Platform Launcher.
- Migration `008_shop_cart_orders` đã áp dụng vào MySQL local; seed idempotent
  tạo đúng 12 sản phẩm. Không sửa/xóa dữ liệu user, phiên lắp ráp hoặc đơn cũ.
- Tomcat QA riêng trên cổng 8081 trả `/api/health` và catalog 200, 12 sản phẩm;
  guest gọi cart/admin nhận 401; trang, CSS và JS đều 200. Trình duyệt thật đã
  xác nhận nội dung catalog guest. Instance QA được dừng sau smoke test.
- Chưa kiểm thử checkout, CSRF ghi, ownership theo hai user và CRUD admin qua
  phiên đăng nhập thật; các hợp đồng tương ứng mới được kiểm tra bằng test/mã.
- Tomcat người dùng trên 8080 không bị restart/redeploy; khi kết thúc QA hiện
  không có listener ở 8080/8081/8006.

### Rủi ro cần kiểm soát

| Rủi ro | Giảm thiểu |
|---|---|
| Client sửa giá/tổng tiền hoặc đặt quá tồn | Chỉ nhận product ID + quantity; server đọc lại DB và khóa hàng khi checkout. |
| Checkout cạnh tranh tạo oversell/đơn trùng | Transaction, khóa cart và product, xác nhận stock rồi commit; rollback khi thiếu. |
| User xem/sửa giỏ hay đơn người khác | User ID chỉ lấy từ `SessionUtil`; mọi câu SQL sở hữu đều có `user_id = ?`. |
| Admin xóa sản phẩm làm hỏng đơn cũ | DELETE là soft-deactivate; FK lịch sử RESTRICT; snapshot tên/giá. |
| Nhầm cửa hàng demo với thanh toán thực | Copy UI/tài liệu ghi rõ “giá tham khảo/đơn mô phỏng, không thanh toán”. |

## Overview

Cho phép người dùng tự bật chế độ lái thử mô hình 3D sau khi Servlet xác nhận phiên lắp ráp `COMPLETED`. Đây chỉ là mô phỏng trên mô hình Three.js, không gửi lệnh tới robot thật và không thay đổi dữ liệu phiên hoặc tiến độ lắp ráp.

## Architecture Decisions

- Giữ nguyên kiến trúc Java Servlet/JSP Model 2; không thêm endpoint, database, migration, framework hoặc server mới.
- Tách trạng thái phím và chuyển động thành controller JavaScript nhỏ, được trang 3D nạp tường minh và kiểm thử độc lập.
- UI chỉ hiện sau trạng thái `COMPLETED` từ API; người dùng phải chủ động bật/tắt. Controller cũng nhận điều kiện cho phép và tự từ chối thao tác khi điều kiện không còn đúng.
- Tích hợp cập nhật chuyển động vào `animate()` hiện có; không tạo animation loop thứ hai. Phím mũi tên/WASD di chuyển và xoay, giới hạn mô hình trong bán kính bệ 3.25 có trừ kích thước mô hình.
- Với `prefers-reduced-motion`, một lần nhấn tạo một bước rời rạc; không lặp khi giữ phím. Bỏ qua nhập liệu đang focus và dọn listeners khi dừng, mất focus, ẩn trang hoặc rời trang.
- Không khôi phục panel danh sách bước/checkbox đã được gỡ khỏi phòng 3D.

## Task List

### Phase 1: Hợp đồng và regression tests
- [x] Thêm test hành vi cho gate `COMPLETED`, bật/tắt chủ động, điều khiển phím, reduced motion, giới hạn bệ, vùng nhập liệu và dọn listeners.
- [x] Bổ sung test cấu trúc trang: disclosure mô phỏng, bộ phím, panel tiến độ lắp ráp 3D vẫn không có danh sách bước.

### Phase 2: Controller và tích hợp giao diện
- [x] Tạo keyboard-drive controller nhỏ, có API nội bộ rõ ràng và không tự tạo render loop.
- [x] Thêm UI chỉ hiện cho phiên hoàn tất, thông báo không điều khiển phần cứng, nút bật/tắt và hướng dẫn truy cập.
- [x] Gắn controller với `robotGroup`, xác nhận trạng thái session ở ranh giới trang, gọi cập nhật trong `animate()`.
- [x] Tạo kiểu responsive theo design system hiện có; không thay đổi panel tiến độ linh kiện hoặc panel bước vốn đã bị xóa.

### Checkpoint: Unit/build
- [x] `npm test` pass.
- [x] `mvn test` và `mvn package` pass.

### Phase 3: Runtime verification
- [x] Deploy WAR hiện tại vào Tomcat base tạm tại context `/` cùng MySQL; `/api/health` trả `connected`, `/api/robots` và `/api/components` trả HTTP 200.
- [x] Trên trình duyệt thật xác nhận dữ liệu API hiển thị, phiên chưa đăng nhập không có nút lái thử và danh sách bước 3D vẫn vắng mặt; hành vi bàn phím/reduced-motion/giới hạn bệ được kiểm tra bằng test controller.
- [x] Kiểm tra tài nguyên CSS/JS/API trả HTTP 200, đọc accessibility tree của trang và dừng Tomcat test sạch.
- [ ] Thử nút bật/tắt và lái bằng phím trên phiên `COMPLETED` đã đăng nhập trong trình duyệt thật.

### Checkpoint: Complete
- [ ] Tất cả acceptance criteria của đợt 2 đạt và không ảnh hưởng luồng lưu phiên hiện tại.
- [ ] Báo cáo kết quả đợt này trước khi chuyển sang đợt kế tiếp.

## Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Mô hình 3D vượt mép bệ khi di chuyển hoặc xoay | Cao | Tính footprint 3D thực tế và clamp theo bán kính bệ trước khi render. |
| Phím bị kẹt sau blur/đổi tab | Trung bình | Xóa trạng thái phím và gỡ listeners khi dừng, blur, visibilitychange, pagehide. |
| Người xem hiểu nhầm là điều khiển robot thật | Cao | Disclosure hiển thị rõ trong chế độ lái thử; không gọi API phần cứng. |
| Chuyển động gây khó chịu | Trung bình | Tôn trọng `prefers-reduced-motion` bằng nấc di chuyển theo keydown không lặp. |

## Open Questions

- Không có; yêu cầu và ràng buộc kiến trúc đã được nêu trong prompt gốc.

## Verification Limitation

- Chưa đăng nhập hoặc tạo phiên để chạy thao tác lái trên một tài khoản thật. Vì vậy chế độ `COMPLETED` được chứng minh bằng regression/behavior tests và điều kiện tích hợp trong mã; kiểm thử browser trực tiếp hiện dừng ở trạng thái chưa hoàn tất.
- Lúc chạy Tomcat test, script của Tomcat ghi `CATALINA_OPTS` ra log stdout. Giá trị cấu hình DB không được đưa ra hội thoại; đã dừng instance và xóa chính xác thư mục runtime thử, gồm các log đó.

## Đợt 3 — Bổ sung hai mẫu robot giáo dục

### Mục tiêu và quyết định kiến trúc

- Bổ sung `line-obstacle` (dò line kết hợp ưu tiên tránh vật cản bằng HC-SR04) và `servo-scout` (servo quét HC-SR04 để quan sát nhiều hướng), tổng cộng năm mẫu.
- Tái sử dụng các component đã có: khung 2WD, Arduino Uno, động cơ/bánh xe, caster, L298N, hộp pin, line sensor, HC-SR04, SG90 và bộ nguồn 5V. Không thêm component, bảng, Servlet, API hay migration.
- Robot/catalog/linh kiện/bước lắp ráp được seed bổ sung theo kiểu additive; quiz và troubleshooting nằm trong hai seed riêng. Mã client `ROBOT_MODELS`, cấu hình 3D, asset ảnh và các ID trong MySQL phải đồng nhất.
- Các bước có thứ tự chỉ hiển thị tại trang lắp ráp ngoài 3D. Phòng 3D tiếp tục chỉ có thao tác lắp linh kiện; giữ nguyên test cấm panel bước.

### Các lát triển khai

1. **Hợp đồng trước:** thêm test đỏ cho hai ID, part mapping, năm bước, quiz/troubleshooting coverage, config target count, geometry factory support, asset path và chính sách additive-only; giữ test hợp đồng ba mẫu cũ.
2. **Catalog và lắp ráp:** thêm seed `robots`/`robot_components`/`assembly_steps`, dữ liệu JS, wiring có kiểm tra kỹ thuật, cấu hình 3D; chạy test tập trung và API smoke check.
3. **Học tập và tra cứu lỗi:** thêm 6 câu quiz mỗi robot cùng lựa chọn/đáp án/giải thích và ba tình huống xử lý lỗi mỗi robot; thêm seed riêng và test nội dung/khóa ngoại/idempotency.
4. **Ảnh, tài liệu, tích hợp:** thêm hai ảnh sản phẩm không placeholder, cập nhật hướng dẫn cài seed/ma trận kiến thức, kiểm tra seed trên MySQL local khi khả dụng, kiểm tra end-to-end trên Tomcat/trình duyệt, chạy toàn bộ test/build, review diff, commit và push.

### Tiêu chí hoàn tất

- [x] API MySQL trả đủ năm robot; hai robot mới có đủ part, đúng số lượng, wiring và năm bước ngoài 3D.
- [x] Mỗi robot mới có 6 câu hỏi bám nguyên lý thật và ít nhất 3 tình huống khắc phục lỗi; seed không sửa/xóa dữ liệu đang có, chạy lại không tạo trùng.
- [x] Mọi component mới dùng được factory 3D có sẵn; mỗi instance có target riêng; phòng 3D không có danh sách/checkbox bước.
- [x] Asset path tồn tại và ảnh hiển thị; tài liệu nêu rõ kiến thức, request/API → Servlet → `XxxDB` → bảng dữ liệu theo kiến trúc môn học.
- [x] `npm test`, `mvn test`, `mvn clean package`, Tomcat/MySQL và luồng browser được kiểm chứng; cả `line-obstacle` và `servo-scout` đã chạy trọn chuẩn bị → lắp 3D → receipt → quiz trên phiên QA, mỗi quiz đạt 6/6.
- [x] Diff đã qua rà soát và commit local `ed9f30d` trên `integration/fullstack-v2`.
- [ ] Push `ed9f30d` lên origin; GitHub HTTPS hiện không kết nối được trong môi trường này.

### Rủi ro kỹ thuật

| Rủi ro | Giảm thiểu |
|---|---|
| Tên/ID part lệch giữa SQL, API và dữ liệu client | Test đối chiếu ID/quantity và GET API sau seed. |
| Nhiều component dùng chung chân Arduino hoặc nguồn không phù hợp | Bố trí pin duy nhất cho từng tín hiệu; với servo-scout dùng nguồn bàn DC 9V đủ dòng (ngoài BOM tối thiểu) cấp VIN/DC jack Uno và đầu vào XL4015, hộp 4AA 6V chỉ cấp L298N Vs; đo XL4015 đúng 5V cho servo, không nối song song nguồn 5V, nối chung GND. |
| Thêm cấu hình 3D nhưng instance chồng lấp/thiếu target | Tái sử dụng factory; test target count và kiểm tra trực quan trên room 3D. |
| Seed gây ảnh hưởng dữ liệu hiện có | File seed mới, `INSERT IGNORE`, không migration/UPDATE/DELETE; kiểm tra ID trước và sau. |

# Đợt 5 — Hồ sơ học tập cá nhân có thể in

## Mục tiêu và giới hạn

- Tạo `GET /learning-profile`, chỉ đọc dữ liệu của tài khoản trong `HttpSession`;
  guest được chuyển về trang tài khoản, database lỗi trả 503 và response no-store.
- Dựng bản xem trước A4 có tiến độ đủ catalog, kết quả quiz, kỹ năng kèm căn cứ,
  vùng xác nhận viết tay và nút in/lưu PDF của trình duyệt.
- Không sửa logic tổng kết hiện có, không đọc dữ liệu shop, không thêm bảng,
  migration, thư viện PDF, API ghi hoặc chức năng ký/chấm điện tử.

## Thiết kế

- Dùng `RobotDB.selectAllRobots()` cho catalog; `StatsDB` đọc dữ kiện phiên đã
  gom theo robot/trạng thái, lượt quiz và DISTINCT linh kiện theo `user_id`.
- `LearningProfile` và các JavaBean con tính bốn trạng thái, lượt quiz tốt nhất
  theo tỷ lệ, lượt gần nhất theo thời gian/id, trung bình HALF_UP, giờ
  `Asia/Ho_Chi_Minh` và dòng kỹ năng nguyên văn có căn cứ hoàn thành.
  Từ Đợt 5b, thời điểm là `Date`, formatter `SimpleDateFormat` được tạo riêng
  mỗi lần; collection xử lý bằng vòng for/if theo slide, kết quả không đổi.
- `LearningProfileServlet` đặt bean vào request rồi forward sang JSP dưới
  `WEB-INF/views`; CSS riêng scope dưới `.learning-profile`, JS gọi `window.print()`.

## Lát triển khai

1. Viết unit/contract tests cho trạng thái, quiz, ngày giờ, kỹ năng, quyền sở hữu,
   escape, no-store và CSS in trước phần chạy tương ứng.
2. Thêm JDBC, JavaBean và Servlet theo Model 2; giữ riêng truy vấn phiên/quiz để
   tránh nhân dòng; không dùng dữ liệu request để chọn owner.
3. Tạo JSP/CSS/JS và thêm lối vào từ trang tài khoản, trang tổng kết.
4. Cập nhật API conventions, demo guide, ERD, README, checklist; chạy Node/Maven,
   kiểm tra Tomcat/MySQL và bản in A4; review diff, commit và push nhánh hiện tại.

## Tiêu chí hoàn tất

- [x] Năm robot đều xuất hiện; hồ sơ chỉ hiện dữ liệu của user hiện tại và không
  cache; thiếu dữ liệu không sinh điểm 0 giả.
- [x] Công thức quiz, trạng thái, thời gian Việt Nam và kỹ năng có căn cứ được
  kiểm thử trong JavaBean.
- [x] JSP escape mọi chuỗi động; CSS in A4 ẩn công cụ, giữ khối/hàng và lặp thead.
- [x] `npm test`, `mvn test`, `mvn clean package`, live Tomcat/MySQL và khả năng
  in PDF được kiểm chứng; giới hạn trình duyệt được ghi trung thực. Bộ test và
  bản in A4 đã đạt. Kiểm chứng bổ sung ngày 04/10/2026 đã đăng nhập cả bốn tài
  khoản demo, đối chiếu SQL/tổng kết và thử query đổi chủ hồ sơ thành công.
- [x] Không thêm migration; tổng kết và chức năng cũ không đổi; Đợt 5 ban đầu
  đã commit/push tại `adec65f`. Lượt sửa sau review chỉ commit local theo yêu
  cầu mới, không push.

## Sửa sau review Đợt 5 — 04/10/2026

- Giữ mục Kỹ năng cùng tiêu đề/caption/bảng khi đủ chỗ trên một trang bằng
  `break-inside: avoid`/`page-break-inside: avoid` ở section trong CSS in. Bảng
  quá dài vẫn được ngắt trang; giữ từng hàng và lặp `thead`.
- Bổ sung nhãn tổng quan cho phiên ABANDONED và tài khoản chỉ làm quiz; kiểm
  tra ưu tiên hoàn thành → phiên mở → đã dừng → chỉ quiz → không có dữ liệu.
- Sửa gợi ý tắt đầu/chân trang mặc định của trình duyệt, nêu rõ cách dự phòng
  khi trình duyệt không hỗ trợ số trang CSS; bump phiên bản CSS riêng.
- Giữ mã phiên lắp ráp để truy vết theo đặc tả. Giữ logic tổng kết; ghi rõ
  khác biệt giữa MAX độc lập và tỷ lệ của đúng lượt quiz trong demo guide.
- Tài khoản demo không có seed trong repo. Cả bốn đăng nhập được trong lần QA
  này; bổ sung cách chuẩn bị một tài khoản USER mới nếu DB khác không có demo.
- Baseline Node: 104 test, 103 pass/1 skip vì chưa có exploded WAR; Maven ban
  đầu 32/32. Target cũ còn một số class Java 25 gây lỗi khi biên dịch test mới
  trên JDK 17; `clean package` dựng lại đúng Java 17, không đổi SDK/pom nguồn.
- Sau sửa: Node 104/104, JUnit 35/35; `mvn clean package` thành công với JDK 17.
  Ba test mới kiểm tra quiz 0/7 thật, phiên đã dừng và thứ tự ưu tiên trạng thái.
- Tomcat 9 QA chỉ nghe loopback cổng 8081, MySQL connected. Guest nhận 302 và
  no-store; cả bốn demo nhận hồ sơ 200. Số mẫu/lượt/điểm/kỹ năng khớp JDBC đọc
  trực tiếp và `/learning-summary`; query userId/user/email không đổi nội dung.
- Smoke hồi quy: tổng kết, phiếu kết quả của builder, trang 3D/quiz/shop, API
  robots/components/shop và lịch sử đơn đều 200. Không làm lại thao tác ghi
  lắp ráp/quiz/checkout trong lượt này.
- Chrome headless: PDF live builder 3 trang; fixture qua JSP thật với 3 hoặc
  5 mẫu hoàn thành đều 3 trang, tiêu đề Kỹ năng cùng trang với đầu bảng. Fixture
  kỹ năng dài 6 trang lặp `thead` trên trang 3–6; hàng không cắt ngang. Đã xem
  ảnh PDF và màn hình 390px không tràn ngang. Tên HTML ở fixture được escape;
  giờ biên UTC hiển thị 25/09/2026 00:30. Fixture nằm trong runtime tạm, không
  vào repo/WAR chính, không ghi DB. Firefox/Safari chưa kiểm chứng.
- Không tạo tài khoản mới, không sửa dữ liệu/tài khoản demo. Artifact PDF/ảnh
  QA giữ ở `tomcat-app/target/phase5-qa/` (gitignored); runtime/log/cookie tạm
  được dọn sau khi kiểm chứng. Chỉ stage các file sửa của Đợt 5 và commit local.

# Đợt 5b — bám slide

## Phạm vi và nguồn đối chiếu

- Chỉ refactor code Đợt 5, giữ nguyên hành vi/giao diện/SQL và cập nhật tài liệu.
  Bảng được phép/cấm và số slide lấy từ prompt đã đối chiếu của người dùng;
  không suy ra số slide từ thư viện hoặc test.
- Giữ Java 17/Tomcat 9/JDBC/Servlet/JSP, không thêm tầng DAO/Service/ORM. Chỉ
  đăng nhập bốn demo và GET; POST/PUT vào route chỉ đọc để kiểm 405. Không ghi
  DB hay tạo user. Một commit local cuối cùng, không push; handoff ngoài commit.

## Các bước và bằng chứng

- [x] HEAD ban đầu `3f0992b`, origin tracking `adec65f`, chỉ handoff untracked;
  đọc source/bean/JSP/tests/ba hàm StatsDB và tài liệu liên quan.
- [x] Baseline Node 104/104, JUnit 35/35. Tomcat 9 QA 8081 từ WAR trước sửa:
  bốn tài khoản đăng nhập được, lưu `golden-before` và PDF builder A4 3 trang.
- [x] Thêm guard trước refactor; chạy đỏ vì cú pháp bị cấm, gồm các nhóm stream,
  lambda/method reference, switch, collection factory, java.time. Sau refactor
  xanh, guard còn chặn toInstant ở ba hàm StatsDB và JSP fmt/fn/scriptlet.
- [x] Dùng for/if, ArrayList/HashMap, StringBuilder; Date null-safe và formatter
  riêng mỗi lần. Giữ chính xác phép BigDecimal/HALF_UP và luật hòa ngày/id.
- [x] Bean có getter boolean trạng thái; JSP chỉ trình bày; Servlet forward
  bằng String url. Không đổi CSS/JS hay cache-busting.
- [x] Giữ 10 JUnit kịch bản cũ, đổi input sang Date, thêm 2 test cho getter
  trạng thái và Date thiếu/thứ tự thời gian/hòa theo id. Tổng JUnit 37/37;
  `mvn clean package` Java 17 BUILD SUCCESS. Target từng bị ghi class Java 25
  trong lúc sửa; clean dựng lại đúng Java 17, không đổi pom/config người dùng.
- [x] Node 105/105, gồm guard kiến thức slide. Contract JSP đọc được các getter
  mới bằng BEANS map hiện có, không cần sửa JspExpressionContractTest.
- [x] Tomcat QA từ WAR mới: golden-after của cả 4 demo diff rỗng; đã loại đúng
  hai dòng giờ lập khỏi cả hai bản, không sửa baseline để làm khớp. Guest
  302/no-store, POST/PUT 405; query userId/user/email không đổi hồ sơ; HTML không
  lộ EL/scriptlet/null; giờ lập khớp giờ Việt Nam; không thấy exception ứng dụng.
- [x] PDF builder sau sửa vẫn A4 3 trang, Kỹ năng cùng bảng ở trang 3, số trang
  1/3–3/3. Text và pixel trước/sau giống nhau sau khi bỏ hai dòng giờ lập.
  Bằng chứng ở target/phase5b-qa (gitignored; Maven clean sẽ xóa).
- [x] Bổ sung hai hàng đối chiếu, bảng 15 mục chapter/slide, 3 Hỏi–Đáp và đồng
  bộ README/API/ERD/handoff; quét code cũ, liệt kê riêng để người dùng quyết định.
- [x] Dừng/dọn QA và log/cookie/cấu hình tạm; cổng 8081/9224 đóng. Review diff:
  chỉ code Đợt 5, test và tài liệu; StatsDB không đổi chuỗi SQL, CSS/JS nguyên vẹn.

Chính sách bàn giao: đúng một commit local `refactor: align learning profile
with course slides`, không push; handoff không stage. Hash xem `git log -1`.

Firefox/Safari chưa kiểm chứng; không làm lại các luồng ghi 3D/quiz/shop vì
phạm vi lần này chỉ refactor trang đọc và tuyệt đối không ghi database.

## Đợt 7 — Phòng nối dây, chặng 1

Khảo sát → baseline → ERD trước migration011 → test-first → bean/JDBC/
Servlet/JSP/SVG/form + JS enhancement → migration/seed → live SQL/two USER/
concurrency → Chrome keyboard/touch/JSoff/390 → review độc lập → local commit.
Chặng hỗ trợ chỉ mở sau cổng review; không push commit mới.

## Đợt 7 — hỗ trợ Admin–User, chặng 2

Sau gate chặng 1 (commit a1822fd), viết test trước → ERD hai bảng → migration 012 →
bean trạng thái/bản chụp → JDBC khóa bài/lượt/yêu cầu → Servlet/JSP form thuần →
QA hai USER/Admin, version/concurrency/SQL/snapshot/JS-off/mobile → review độc lập →
build/cleanup/commit local. Không sửa migration 011 hay rubric A/B.

## UI-2 — Rà soát giao diện và chữ viết, 06/10/2026

Khởi đầu 7d27f18/ahead8, giữ mọi backend và hợp đồng. Baseline thực 131Node/79JUnit.
Khảo sát 34 view + fragment → golden GET/token/form/SQL → Chrome từng viewport →
menu/badges/copy/CSS và PDF → contract/GET hồi quy → báo cáo cùng inventory chuỗi →
cleanup runtime → commit local, không push. Kết quả và giới hạn từng view ở
docs/UI_REDESIGN_QA_REPORT.md mục UI-2. Hai trạng thái chỉ dựng HTML tạm vì cấm POST.

## Tích hợp luồng phụ Nhi/Tuấn Anh — 12/10/2026

Fetch → backup/bảo vệ sửa dở → checkout riêng từ fb07191 → baseline JDK17 →
review hai nhánh → merge không commit ngay → test đỏ/sửa lỗi API/guard/bean →
review độc lập → build + QA Tomcat riêng/SQL hash38bảng → tài liệu/cleanup →
merge commit và fast-forward về integration/fullstack-v2. Không seed/migration,
không stage file cá nhân; không tự push. Chi tiết ở TEAM_INTEGRATION_QA_20261012.md.

## Đợt 8 — Phòng lắp ráp có kiểm chứng

M0: baseline ad2a4fc; Node160/JUnit85 xanh Java17. Thiết kế ERD trước
migration013, giữ GROUP cũ và bảy file người dùng. Các mốc M1–M6 và bằng chứng
ở docs/PHASE8_ASSEMBLY_QA_REPORT.md. Chỉ commit local, không push.

M1–M6 đã triển khai obstacle-avoider chi tiết, migration013/seed được duyệt và chạy.
Kết quả thực:168 Node/93 JUnit, Maven test/clean package Java17 xanh;155 kiểm tra
API/SQL/JSP/Chrome, kéo chuột/cảm ứng mô phỏng/bàn phím,390/768/1440 và concurrency.
Phiên DETAILED COMPLETED được nộp làm bằng chứng nhiệm vụ mẫuA; GROUP cũ giữ nguyên.
Review chính có các findings reset/version/touch/tách hình đã sửa và chạy lại.
QA8081 đã dọn; còn giới hạn Firefox/Safari, cảm ứng thật, screen reader, nộp mẫuB mới
và spec archived trên server thật. Không thực hiện các phần mở rộng tùy chọn.

## Đợt 8b — sửa hướng, thông báo và tiến độ

M1–M5 đã commit local: seed v2 additive (không migration), preview góc tuyệt đối,
lời giải thích từ layout transaction, SESSION_LOCKED nhất quán, X/N chi tiết và chữ phiếu.
Baseline168/93 →172Node/97JUnit; clean package JDK17 xanh. QA phiên mới33 hoàn tất10/10,
Chrome Q/E/nút/radio/390, owner/CSRF/version/complete đồng thời và hash43 bảng cũ giữ nguyên.
M6 đã chốt theo quyết định người dùng: giữ tư thế v1, v2 không đổi, không tạo v3;
Y90/Y270 là quy ước trái/phải của mô hình giản lược, hình khối không mô phỏng chính xác
chiều trục động cơ. Bằng chứng/giới hạn ở PHASE8_ASSEMBLY_QA_REPORT.md mục8b.

## Đợt 9 — Sau khi hoàn tất

Baseline8da8b15 đã đồng bộ origin;173Node/97JUnit JDK17. Có ảnh robots.image,
assembly_steps theo thứ tự, phiếu JSP và quiz theo model. Thiếu nhật ký/bản vẽ theo
phiên/trang hoàn thiện/replay/undo. Giữ sáu WIP và bốn file untracked nguyên byte.
M0 sửa góc khay, kiểm camera/mobile và ADMIN QA mới qua đăng ký/nâng quyền riêng.
M1 ERD trước migration014: assembly_events cùng transaction, không backfill phiên cũ.
M2 bean bản vẽ hai góc nhìn; M3 Servlet/JSP bốn phần, điều hướng và in;
M4 events API/replay chỉ đọc; M5 undo có khóa/version/bean, không Redo;
M6 test/HTTP/Chrome/PDF/hồi quy/tài liệu. Commit từng mốc, không push.
Tái sử dụng AssemblySessionServlet/AssemblyPlacementDB, RobotDB/AssemblyStepDB,
quiz aggregates và shell JSP. JavaScript chỉ tương tác; không đổi rubric hoặc panel3D.
Chỉ ghi tên chủ đề môn học khi chưa xác minh nội dung Word nhúng trong slide.

Đã triển khai M0–M5 và kiểm thật với phiên QA35, ADMIN QA22. Migration014 additive
đã chạy, không có seed mới; 43 tập hàng cũ và10 file WIP/untracked giữ nguyên hash.
M6 chốt tài liệu/demo/vấn đáp,182Node/106JUnit, clean package JDK17; kiểm HTTP,
Chrome390/768/1440, replay không ghi và PDF thật (phiên cũ3 trang, phiên mới4 trang).
Các giới hạn/ca chưa kiểm ghi trong PHASE9_REVIEW_QA_REPORT.md, không tuyên bố đã
kiểm Firefox/Safari/cảm ứng vật lý/screen reader. Giữ các dữ liệu QA để người dùng quản lý.

## Đợt10 — Xưởng thiết kế, M0

HEAD b0bf22c, integration/fullstack-v2 ahead8; giữ10 file WIP/untracked nguyên byte.
Baseline thực183Node/109JUnit, JDK17 clean package xanh. DB44 bảng có001–014,
015 chưa dùng, quyền CREATE có sẵn; catalog có đủ13 renderer. ERD đã viết trước code.

Tái sử dụng placement view (adapter slot host:mount), hình thủ tục, drawing/JSP
fragment, SessionUtil/CSRF và cơ chế transaction/nhật ký. Các route JSP mới:
/my-kit, /designs, /design-sheet; API /api/designs/* cho tương tác3D riêng.
Tạo11 bảng qua015, seed luật v1 chỉ INSERT có khóa/guard version, không sửa mẫu.
M1 bean luật/DDL/seed/reader → M2 kit/form/import → M3 thiết kế/snapshot/PRG →
M4 layout/động tác/undo/API/3D → M5 check ba lớp → M6 hồ sơ/BOM/cây/bản vẽ/in →
M7 QA/demo/vấn đáp/tài liệu. Commit local từng mốc, không push.

CHECKED vẫn sửa, mutation xóa kết quả kiểm cũ và trở về DRAFT. Xóa design chỉ
own POST có xác nhận, con CASCADE; không xóa đơn/kit/mẫu. Tách MOTOR_MOUNT_L/R,
thêm rx/rz và fit ROOT giải thích trong ERD để FK/pose đúng, không thay schema mẫu.
M6 trả nợ drawing: không ném lỗi khi hết chỗ nhãn; thêm ca30 vật thể và dùng lại
isLearningError, thêm bốn mã Xưởng. Không tính design làm bằng chứng nhiệm vụ.
QA8081 riêng, snapshot44 tập hàng cũ, hai USER, import idempotent, version409,
Chrome390/1440, đối chiếu SQL/bản vẽ/PDF và hình obstacle giống mẫu chuẩn.
Giới hạn điện/servo tham khảo nguồn chính thức Arduino/ST, không xác nhận an toàn thật.

M1–M6 đã triển khai/commit:015/schema/seed, kit+nhập đơn, snapshot thiết kế,
API placement/undo, check ba lớp, hồ sơ/in. Node183→191/JUnit109→124, build đạt.
10 file WIP nguyên byte,43/44 tập hàng cũ giữ hash; shop_products đổi do một đơn QA.
M7 phát hiện bù góc hai lần; user duyệt luật v2 chỉ sửa hai góc nền về0. Seed đã
chạy/rerun/diff đúng hai ô, không đổi v1/schema/spec. QA7 obstacle10/10 khớp mẫu,
QA8 scanning11/11 giữ hướng180° theo obstacle và ghi khác biệt với servo-scout.
Node192/JUnit126;39 ca v2 +68 HTTP cuối +15 browser/JS-off, PDF mới3 trang mỗi mẫu.
Đơn QA2 giữ nguyên vì không có luồng hủy hoàn kho; không sửa tồn kho bằng tay.
M7 hoàn tất: mvn -q test, clean package JDK17 và npm test đều xanh192/126,0skip;
PDF v2 mỗi mẫu3 trang đã xem, runtime QA/config/log đã dọn và8081 đóng.
Commit tài liệu riêng, không push; các giới hạn servo-scout/thiết bị thật ghi rõ.

## Đợt10b — Tên catalog và khung nhìn

M1 migration016 có điều kiện tên cũ, seed/catalog mới đúng tên, bỏ ba overrideJava;
nhãn spec và snapshot đơn cũ giữ nguyên. M2 camera theo bounds/FOV/aspect, focus
native và bệ/sàn theo phòng mẫu. M3 QA/docs: baseline192/126→197/127, Chrome thật
1440/768/390 và gắn/tháo/undo/camera.10 file được bảo vệ nguyên byte, chỉ QA7 đổi
bố cục/version/ngày kiểm qua API và phục hồi10/10; không checkout thêm hoặc sửa kho.

## Đợt11 — Điều hướng và kết nối chức năng (M0)

HEAD191146b đồng bộ origin; bảo toàn hash10 file WIP/untracked. Baseline197Node/127JUnit,
JDK17 clean package. Khảo sát120GET ba vai trò, ADMIN QA đăng nhập được. SITE_MAP.md
liệt kê42 ánh xạServlet +18HTML, quyết định giữ/account làm dashboard, HTML tài khoản
giữ auth/settings, robots/components minh họa. M6 không ẩn module có dữ liệu.

M1 fixture/menu chung → M2 trang chính/phụ → M3 dashboard dữ liệu owner → M4 link
ngữ cảnh + POST thiếu vật tư vào giỏ (transaction/idempotent, không checkout) →
M5 bản đồ môn học → M7 lỗi/breadcrumb → M8 kiểm link, HTTP3vai trò, Chrome390/768/1440,
ảnh ngoàiGit và tài liệu. Không schema, không sửa luật/spec/API cũ hoặc file WIP.
Commit local từng mốc, không push.

### Đợt11 — M1–M8 đã kiểm chứng

Menu18HTML +sharedJSP, trang chính/phụ, dashboard owner, context và BOM→cart,
course map15 chủ đề, error404/500/JSON và serverfilterHTMLadmin đã triển khai.
M6 gộpM2, không ẩn module dữ liệu. Baseline197/127→205Node/130JUnit,0fail/skip;
JDK17 test+cleanpackage xanh. HTTPcuối81, giỏ/browser31, tương tác13;207ôChrome
390/768/1440 đạt. PDFcourse4trang vàhồsơ3trang đã xem; bằng chứng ngoàiGit.
QA39/40 tựtạo bởi luồngcũ; QA7 phục hồi10/10 nhưngnhậtký/versionđổi; cart23cuốirỗng.
Khôngorder/checkout/stock/spec/schema thay đổi.10fileWIP giữhash. RuntimeQA8081
đãdọn, khôngđụng8080; giới hạnbrowser/POSThồi quy ghi rõ trong PHASE11_UX_QA_REPORT.
WAR cuối có thêm36 kiểm smoke/header/quyền; WorkspaceCacheFilter bảo vệ no-store
ở các trang cũ, không sửa controller hoặc JSP WIP của người dùng.

## Đợt 11b — sửa review và xác minh deploy (15/10/2026)

Khởi đầu HEAD99e8337, đúng integration/fullstack-v2, 10 file bảo vệ đã lưu SHA-256.
Node tại repo:204 đạt/1 lỗi exploded byte/0skip; working tree build riêng JDK17:
130JUnit đạt/0skip. Bản HEAD sạch archive:205Node/130JUnit đạt, không có skip.
Không có Tomcat đang dùng target gốc khi bắt đầu rebuild.

Kế hoạch trước khi code endpoint M3: thêm GET `/api/shop/products/{id}`, công khai,
chỉ SKU đang bán;200 `{data: product}` kể cả hết hàng,404 JSON nếu không có/ngừng
bán,503 khi DB lỗi. Request danh sách cũ giữ nguyên. Một GET theo SKU, không tải
mọi trang, không ghi giỏ/đơn/kho, không đổi schema. Đã triển khai đúng contract.

- M1: URL linh kiện canonical query+hash, nhận hash cũ, chờ catalog/fallback,
  focus một lần độc lập API context; link từ tra cứu/khay/lỗi/thông số dùng chung.
- M2: trạng thái xác thực từ menu/server, đóng quyền khi pending/lỗi; JSP dùng
  JSTL/session. Không mở quyền backend USER cho ADMIN.
- M3: GET SKU có giới hạn, khu vực tập trung khi ngoài trang hiện tại; chống
  response cũ/ID trùng. Auth lỗi khác khách; giỏ lỗi không mất danh tính.
- M4: DashboardSummary quyết định thẻ/gợi ý/kit; Servlet đọc dữ kiện và forward.
  Candidates quiz một query, thiết kế LIMIT1, nhiệm vụ COUNT/SUM theo owner.
- M0 chốt: ROOT.xml IntelliJ thật trỏ target/robot-assembly-lab; đã build lại
  target bằng17 và phục vụ chính docBase này bằng Tomcat QA riêng8081.
  POM/IntelliJ17, nhưng metadata VS Code compiler/JRE25, output target/classes;
  đã xin phép và xác minh đúng language server trước khi dừng. Không sửa cấu hình
  IDE khi chưa được duyệt; không khởi động/dừng Tomcat người dùng8080.
- QA:216Node/138JUnit,37 ca fixture browser,4 ca link phòng dùng layout tạm,
  69HTTP thật và42 ô viewport,10 resource HTTP khớp byte source. Hash55 bảng DB
  không đổi; chỉ đăng nhập/GET thật. Báo cáo ghi rõ giới hạn deploy/IDE và hồi quy.

### Chốt nghiệm thu Đợt11b — 16/10/2026

Chỉ sửa fragment cache site-nav.js3→5 và đặt tên riêng “Liên kết cửa hàng” cho
nav phụ ở ba trang shop. Giữ menu chung làm điều hướng chính duy nhất; không
đổi link, control, ID/data/hook, Java hoặc luật. Hai regression test mới tái hiện
và bắt được lệch version/nav trùng tên trước sửa. Kết quả218 Node/138 JUnit,
0 lỗi/skip;21 ô Chrome qua390/768/1440 và bàn phím đạt;55 hash DB giữ nguyên.
Đã dọn QA8081, phối hợp người dùng mở lại VS Code và Run IntelliJ8080 thật;
chỉ đọc compiler/bytecode/HTTP sau khi người dùng thực hiện, không tự đóng IDE
hoặc dừng Tomcat.

Người dùng đã xác nhận mở VS Code/Run IntelliJ8080 ngày16/10. Đã kiểm thực tế:
compiler17/JavaSE-17;177+26 class target và177 class deploy đều major61;
Tomcat8080 dùng JDK17, ROOT.xml đúng docBase.4 HTTP asset khớp byte source,
JSP/architecture trả version20261015.5;6 ô Chrome khách390/768/1440 của shop/giỏ
và bàn phím đạt. Node218/JUnit138,0 fail/skip và clean package17 chạy lại đạt;
Maven dùng buildDirectory ngoài repo để không đụng target đang phục vụ.12 hash
bảo vệ giữ nguyên; không ghi DB, không tự dừng server. Language server giữ PID
cũ: kiểm được trạng thái sau mở cửa sổ, không tuyên bố vừa restart tiến trình.
Xem mục xác nhận môi trường trong PHASE11B_REVIEW_FIX_QA_REPORT.md và evidence
acceptance-20261016/manual-verification.json. Chưa push.

## Đợt11c — Hành trình học tập và giao diện phụ (16/10/2026)

M0: HEAD0280960 đúng nhánh tích hợp,6 WIP sửa dở và4 file chưa theo dõi;
12 hash bảo vệ gồm POM/settings lưu ngoài Git tại qa-evidence/phase11c.
Baseline218 Node,138 JUnit,0fail/skip; clean package17 dùng buildDirectory riêng.
Tomcat8080 đang phục vụ target gốc: không build/redeploy vào đó. Tái hiện17 trang
bằng Chrome/GET, menu→quiz thiếu model, dashboard7 thẻ;0 lỗi JS/request ghi.
Hướng dẫn .agents cấp workspace thuộc LogiRoute/Python, không áp dụng công nghệ
cho Robot Assembly Lab. Review là bằng chứng lịch sử, đã đọc lại source.

- M1: quiz không model dùng loadRobotOptions (GET catalog nhẹ), kiểm bài bằng
  GET questions hiện có, chỉ JSON công khai không đáp án. Không endpoint mới,
  không tự tạo lượt, không lịch sử model rỗng; trạng thái khách/lỗi/rỗng rõ ràng.
- M2: bean LearningSummary gom count bằng for, giữ attribute và dữ kiện cũ;
  JUnit rỗng/nhiều trạng thái và guard cú pháp phủ file đã sửa.
- M3: CSS riêng dashboard,2–3 cột/1 cột mobile; phân nhóm thẻ học và kit/đơn,
  giữ hành động/dữ kiện từ DashboardSummary, không đổi CSS metric toàn cục.
- M4: nối dây có neo bài/lịch sử, paging server theo owner/thứ tự ổn định;
  chẩn đoán bớt khung và CTA rõ; tra cứu details giữ nguyên văn/filter/context.
- M5: đọc TaskServlet/TaskRecipient; chi tiết có recipient nhưng danh sách chỉ
  có tasks. Không suy diễn trạng thái bằng JSP hay chạm WIP; ghi giới hạn hoặc
  xin duyệt phần GET WIP nếu cần trạng thái cá nhân ngay trên danh sách.
- M6: giữ số liệu StatsDB; bean tổng kết chọn phiên hoàn tất theo owner từ
  dữ kiện gộp hiện có, link review/quiz đúng model và bản in dễ thấy. Ghi nhận
  nguy cơ MAX(score)/MAX(total_questions), không đổi định nghĩa điểm.
- M7: chỉ đọc DB/HEX và browser để liệt kê dữ liệu QA công bố/cá nhân; không dọn.
- M8: QA8081 riêng từ artifact mới, fixture ca biên không ghi DB;390/768/1440,
  bàn phím và hành trình, cache/version, hash WIP/DB, hồi quy GET và build cuối.
  Phân biệt commit sạch/working tree có WIP/artifact8080 cũ. Commit từng mốc;
  không push. Báo cáo PHASE11C_LEARNING_JOURNEY_QA_REPORT.md ghi phần chờ duyệt.

### Kết quả11c

M1–M4/M6 đã sửa và kiểm; M5 chỉ JSP an toàn, nhánh GET danh sách WIP vẫn chờ duyệt.
M7 đọc55 bảng: bài nối dây7 công bố có scope mất dấu ngay trong DB,21 nhiệm vụ QA;
không dọn/ẩn. Không thấy nhóm quiz hiện tại có nhiều mẫu số, giữ công thức cũ.
Working tree và archive commit sạchafa6ef2 đều229 Node/144 JUnit,0fail/skip,
clean package17. Chrome57 ô390/768/1440,16 kiểm hành trình/quyền,10 fixture quiz,
27 smoke, zoom Chrome200% thật trong profile QA riêng,3 form contracts0 khác biệt.
12 hash bảo vệ/55 hash bảng giữ nguyên. QA8081 phục vụ artifact riêng và đã dọn;
HTTP quiz/tra cứu trên8080 vẫn là bản cũ, không tự redeploy. Không tạo/nộp lượt,
checkout hoặc sửa dữ liệu. Giới hạn thao tác ghi/live lịch sử>20/Firefox/Safari
và M5 được ghi rõ trong PHASE11C_LEARNING_JOURNEY_QA_REPORT.md. Chưa push.

### Bổ sung M5 sau duyệt riêng GET TaskServlet

Đã sao lưu file/diff WIP và12 hash mới đầu lượt. TaskServlet chỉ thêm2 dòng
setAttribute/gọi PracticeTaskDB.selectListRecipients(user.getId(), state).
Query gộp theo owner lấy MAX(submission_no)/MAX(review.id), khớp thứ tự trang
chi tiết; metadata chuyển vào TaskRecipient để dùng luật statusLabel hiện có.
JSP chỉ c:out nhãn và cờ muộn, không suy từ điểm, không sửa POST/TaskFormUtil.

39 kiểm browser đạt:2USER,19 nhãn khớp chi tiết,6 ô390/768/1440, filter/quyền/
URL userId bị bỏ qua. Fixture JUnit có chưa nộp/chờ chấm/cần bổ sung/thiếu review/
hai người cùng task.231 Node/149 JUnit,0 fail/skip, package17 đạt ở working tree
và archive sạch. Commit source2b4e5fd: TaskServlet chỉ có2 dòng GET trong index,
WIP3 dòng cũ còn ngoài commit, POST byte-identical.11 hash bảo vệ khác và55 hash
bảng DB giữ nguyên. QA8081 riêng; không deploy hoặc dừng8080. Chưa push.

## Chốt nghiệm thu11c và artifact demo final — 16/10/2026

HEAD đầu lượt2ff67f1, source M5 là2b4e5fd, không delta source sau review.
Index rỗng;12 hash đầu lượt mới (10 WIP/untracked và POM/settings) lưu ngoài
Git tại `qa-evidence/final-demo-11c-20261016/protected-before.json`.

Không sửa chức năng. Archive HEAD sạch, clean package JDK17 ngoài repo; chạy
Node trên archive sau build. Kiểm bytecode, tài nguyên đóng gói/HTTP, cache
version; QA8081 riêng, giữ nguyên target/IDE/Tomcat8080. Kiểm các hành trình
GET bằng tài khoản có sẵn, fixture lỗi/rỗng/tải chậm không ghi DB.

Kết quả mới:231 Node/149 JUnit,0fail/skip;179 application class major61,
166 tài nguyên khớp archive;31 kiểm browser (15 ô viewport/keyboard),27 smoke,
10 fixture quiz đạt. Các byte HTTP quan trọng QA8081 khớp archive;8080 cũ,
dashboard.css/practice-index.css trả404. Không suy từ QA sang deploy người dùng.
Review độc lập44 kiểm/19 nhãn ngày16/10 được kế thừa với nguồn rõ, không cộng
thành kiểm mới. Một assertion smoke ban đầu so CSS CRLF của working tree với
LF của archive; sửa đúng nguồn đối chiếu trong runner ngoài repo, không sửa app/test dự án.

Bổ sung kịch bản final3–5 phút,7 câu vấn đáp/file:dòng và hướng dẫn redeploy
WAR sạch trong TEAM_FLOW_DEMO_GUIDE; cập nhật báo cáo11c/todo. Chỉ commit tài
liệu, không gom WIP. Chờ người dùng tự deploy8080 hoặc quyết định giữ bản cũ;
chờ chọn dữ liệu demo hiện tại có kiểm soát hoặc phương án DB riêng.
Không tạo DB/dữ liệu, không dọn QA, không sửa M5. Không push.

## Tập dượt riêng và ôn vấn đáp — 17/10/2026

Người dùng yêu cầu thực hiện bước tiếp theo trên DB demo đã dựng ở Chặng 1.
Giữ WAR sạch đã nghiệm thu, Java17/Servlet-JSP Model 2 và toàn bộ WIP;
không sửa source, IDE, .env hay Tomcat8080. Chỉ chạy QA8081 do runner sở hữu.

1. Chụp hash dữ liệu phiên1/2, nhiệm vụ1, quiz luyện tập1 và file được bảo vệ.
2. Tạo tối đa một nhiệm vụ A giao hai USER và một phiên lắp mới của Tài.
   Công bố → hoàn tất phiên mới → quiz TASK đúng vòng → nộp → ADMIN phản hồi.
   Chỉ ghi qua UI/API sẵn có; SQL chỉ đọc. Không lặp POST khi kết quả chưa rõ.
3. Đối chiếu trạng thái hai người, dữ liệu dành live, DB cũ và WIP; dọn QA riêng.
4. Cập nhật tài liệu demo/ôn tập bằng file:dòng đã đọc; phân biệt JSON API với
   Servlet forward JSP. Chốt phát triển, chuyển sang hỏi từng câu vấn đáp.
   Không push; không tạo thêm chức năng hoặc đổi luật chấm.

Kết quả tạm17/10: đã tạo/công bố nhiệm vụ2. POST tạo phiên cho Tài trả lại
phiên2 đang mở, runner dừng trước mọi thao tác vào phiên live.12 snapshot
dành live/dự phòng và44 hash bảo vệ khớp; DB cũ55/55 bảng khớp. Chưa hoàn tất
tập dượt; cần duyệt chuyển người thực hiện sang USER3 trên nhiệm vụ2 đã có,
giữ Tài làm người đối chiếu chưa nộp. Không sửa luật create-or-resume hay API.
Bằng chứng ngoài Git: qa-evidence/final-rehearsal-stage2-20261017/RESULTS.md.

## Đồng nhất lắp chi tiết cả 5 mẫu — 17/10/2026

Yêu cầu mới của người dùng: bốn mẫu còn lại cũng kéo thả từng vật thể như
obstacle-avoider. Chặng tập dượt đã hoàn tất trên USER3 (phiên3, nhiệm vụ2,
quiz2:7/7, bài nộp1,95/100); không lặp hoặc dùng phiên live để QA tính năng mới.

### Đặc tả và ranh giới

- Thêm spec v1 cho line-follower, mini-arm, line-obstacle, servo-scout qua seed
  cộng thêm; không sửa schema/spec obstacle-avoider/phiên GROUP cũ/điểm.
- Từng vật thể có slot/cha/hướng rời rạc; quantity4 servo là4 vật thể riêng.
  Tọa độ lấy từ ASSEMBLY_3D_CONFIG hiện có, góc nền bù hướng để không quay hai lần.
  Đây là mô hình học tập giản lược, không khẳng định lắp cơ khí/điện thực tế.
- Dùng nguyên AssemblyLayout/AssemblyPlacementDB, CSRF/owner/version/transaction.
  Java17/Servlet/JSP Model2/JDBC; không thêm thư viện, DAO hay Service.
- Phiên mới chọn spec PUBLISHED. Phiên GROUP đang có hiển thị hành động rõ
  tạo/tiếp tục phiên DETAILED riêng; không tự đổi/xóa/dừng phiên cũ.
  POST mở rộng tùy chọn newDetailedSession=true; mặc định giữ create-or-resume cũ.
  Server khóa user, dùng lại phiên DETAILED đang mở để không tạo trùng khi bấm lại.
- Chỉ chạy seed/QA trên DB thử cô lập mới; không seed DB cũ hoặc DB demo final,
  không đổi .env/IDE/8080 hay sáu file WIP và bốn tài liệu cá nhân.

### Thứ tự và kiểm chứng

1. Test RED: tất cả seed khớp kit, parent không chu kỳ, tọa độ/góc khớp config.
2. Seed4 mẫu + test GREEN; API tạo phiên riêng + UI chuyển tiếp phiên cũ + test.
3. JDK17 Maven/npm toàn bộ; WAR/source sạch trong thư mục QA riêng, không target gốc.
4. DB QA riêng: cả5 mẫu tạo DETAILED; sai loại/hướng/thiếu cha bị từ chối;
   gắn/tháo/undo/refresh/hoàn tất và replay; pointer kéo thực + keyboard, mobile.
5. Tài liệu cài đặt/giới hạn, báo cáo bằng chứng, commit từng nhóm rõ ràng;
   không push hoặc chuyển8080 khi chưa được yêu cầu.

Lệnh: npm test; mvn -f tomcat-app/pom.xml
 -Drobotlab.buildDirectory=D:/Web-Pro/qa-evidence/all-models-20261017/build clean package
(JAVA_HOME trỏ JDK17 trong tiến trình). Chạy test tập trung bằng node --test
tests/server/all-models-placement.test.cjs. Dùng apply_patch, chỉ stage đường dẫn riêng.

Kết quả:234 Node/149 JUnit,0 fail/skip, build archive sạch không WIP. QA8082
trên DB riêng xác minh cả5 mẫu và4 phiên GROUP cũ,15 ô responsive,5 kéo chuột
thật +5 gắn bằng Enter/reload. Runtime riêng đã dừng/dọn, không lỗi runtime.
DB demo final và44 hash file bảo vệ khớp. DB cũ hiện52/55 bảng khớp mốc14:56;
3 bảng phiên tăng bởi hoạt động USER7 lúc16:19 (phiên41/42/43), trước lúc dựng
DB QA mới. Không có tài khoản QA mới trong DB cũ. Chỉ đọc để báo cáo, không
hoàn nguyên dữ liệu này hoặc quy kết người thao tác khi chưa có audit log.
Chưa seed/deploy vào môi trường trình bày; xem README và báo cáo toàn bộ mẫu.

Audit cuối sau commit:43/44 hash bảo vệ khớp, `.idea/workspace.xml` thay đổi
ngoài patch; không stage/restore cấu hình IDE.10/10 file WIP/cá nhân vẫn nguyên.

## Áp dụng bản toàn bộ mẫu vào demo final — 17/10/2026

Người dùng giao chọn phương án tốt nhất cho final: chọn DB demo riêng,
không đụng DB làm việc. Không thay Java17/Servlet-JSP Model2/JDBC hoặc luật chấm.

1. Chụp hash54 bảng demo + phiên1/2/nhiệm vụ1/quiz đã chuẩn bị và file bảo vệ.
2. Backup trong thư mục private ngoài Git, khôi phục thử vào DB tên riêng mới;
   đối chiếu54 bảng trước khi áp dụng. Không restore đè DB đang có.
3. Nạp đúng seed cộng thêm đã commit bằng runtime DB login hiện có. Chỉ3 bảng
   spec/slot/orientation được thêm4/43/43;51 bảng còn lại phải giữ nguyên hash.
4. Dùng WAR Java17 đã QA có checksum; copy thành artifact demo bất biến riêng,
   chạy Tomcat riêng127.0.0.1:8081. Không đổi8080/.env/IDE/target gốc.
5. Smoke guest/USER/ADMIN, tài nguyên khớp artifact và dữ liệu live không đổi.
   Chuẩn bị4 phiên0/N cho USER B qua API/UI thật để mở được các mẫu mới;
   không hoàn tất hoặc ghi điểm, không dùng tài khoản QA mới trong DB demo.
6. Ghi URL, Start/Stop và tài khoản demo (không mật khẩu) vào tài liệu hiện có.
   Giữ backup/WAR cũ để quay lại; không drop/restore dữ liệu hay push tự động.

Kết quả: backup198821 byte restore54/54 bảng khớp; seed chỉ3 bảng với4/43/43
dòng mới,51 bảng còn lại giữ nguyên. WAR nguyên checksum,352 file giải nén khớp.
Demo mới8081 có44 kiểm/12 viewport đạt. USER3 có4 phiên4–7 ở0/N;14 nhóm dữ liệu
cũ/live/rehearsal và44 file bảo vệ khớp mốc đầu lượt. Stop/Start đã thử; để runtime
chạy phục vụ người dùng, không thay8080 hoặc tự khởi động cùng Windows.
Lệnh mở nhanh D:/Web-Pro/FINAL.ps1 -Lenh Mo; tài liệu hiện hành ở đầu hướng dẫn demo.

## Sửa đường vào và kéo-thả đúng môi trường 8080 — 17/10/2026

Baseline 01b1f2b, nhánh integration/fullstack-v2; giữ hardening và 10 file WIP/cá nhân.
8080 JDK17/context / dùng target của IntelliJ, JNDI nối robot_lab_content_test;
8081 dùng artifact riêng và robot_lab_final_demo_20261016. Không suy DB từ .env.
DB 8080 chỉ có spec obstacle v1/v2; bốn mẫu còn lại chưa được triển khai spec.
Đường mặc định createOrResume có thể lấy GROUP cũ; lỗi server còn fallback local.

1. Hash bảo vệ, snapshot chỉ đọc, baseline archive sạch/JDK17/Node. Clone đúng
   DB nguồn thành robot_lab_assembly_entry_20261017, login runtime chỉ DML trên clone.
2. Kiểm seed all-models trên clone: rollback khi lỗi, chạy hai lần, đối chiếu kit/
   cha/hướng; không sửa spec đã công bố hoặc tự seed lúc mở trang.
3. Chỉ main.js/assembly-3d.js đổi đường bắt đầu mặc định sang createDetailed;
   giữ get(session) cho URL lịch sử, contract createOrResume cũ và nút GROUP.
   API lỗi phải dừng rõ, chỉ khách AUTH_REQUIRED được xem trước local.
4. Kiểm/sửa điểm thả theo hình chiếu marker thay vì chỉ mặt phẳng x/z, căn camera
   chứa khay và điểm gắn; dùng chung placement view, không đổi luật server.
5. Test trước/sau và UI năm mẫu: fresh/GROUP/DETAILED/cả hai/session cũ/catalog/
   chuẩn bị/direct3D/nút GROUP. Thả chuột thật → placements → layout → reload;
   kiểm sai/đúng, undo/hoàn tất, owner/CSRF/version, 390/768/1440 và hồi quy hardening.
6. Build snapshot commit sạch; trình gói duyệt DB/artifact/backup/rollback/fixture
   và redeploy đúng 8080 một lần. Chưa có duyệt thì chỉ báo QA đạt/chờ triển khai.

Bằng chứng ngoài Git: D:/Web-Pro/qa-evidence/assembly-8080-20261017/.
Không dừng 8080/8081, không đổi kết nối/IDE, không ghi DB nguồn trước gói duyệt.
