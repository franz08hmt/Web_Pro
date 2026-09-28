# Implementation Plan: Đợt 2 — Lái thử mô hình 3D

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
