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
- [x] Commit đợt 3 local: `ed9f30d feat: add two robotics learning models`.
- [ ] Push đợt 3 lên `origin/integration/fullstack-v2`; kết nối HTTPS tới GitHub đang thất bại/treo.
