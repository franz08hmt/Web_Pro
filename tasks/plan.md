# Implementation Plan: Đợt 2 — Lái thử 3D và Đợt 3 — Hai mẫu robot mới

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
