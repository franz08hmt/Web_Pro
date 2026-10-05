# Implementation Plan: Đợt 2 — Lái thử 3D và Đợt 3 — Hai mẫu robot mới

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
