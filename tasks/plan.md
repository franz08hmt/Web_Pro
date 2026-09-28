# Implementation Plan: Đợt 2 — Lái thử 3D và Đợt 3 — Hai mẫu robot mới

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
