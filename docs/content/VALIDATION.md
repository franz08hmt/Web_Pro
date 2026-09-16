# Kết quả kiểm thử phần Thảo Nhi

Ngày kiểm tra: 2026-09-16. Nền backend nhóm: `3fda472`.

## Kiểm tra sau sửa và đồng bộ hợp đồng

- `node --test tests/content/*.test.cjs tests/server/*.test.cjs`: 27/27 đạt.
- `mysql-smoke.cjs`: CRUD cả robot/component/step/library, lọc/phân trang,
  duplicate, FK, sai robot, chặn xóa tiến độ, cascade và schema 002 đạt; rollback.
- `backend-mysql.cjs`: đủ ba repository ports; ID chuỗi, user public không hash,
  email trùng, hết hạn/thu hồi auth, chủ sở hữu phiên, lưu/khôi phục tiến độ đạt.
  Bốn request createOrResume đồng thời trả cùng một phiên. Dữ liệu test riêng đã dọn.
- `migrations-mysql.cjs`: tạo database tạm, nạp 001/seed, tạo tiến độ cũ rồi áp dụng
  002; trạng thái IN_PROGRESS/COMPLETED và tiến độ được giữ, 11 bảng; xóa DB tạm.
- `browser-check.cjs` bằng Playwright/Edge với MySQL thật: tạo/sửa/xóa đủ bốn loại,
  thêm/gỡ linh kiện bắt buộc, FK 409, chưa auth 401, CSRF 403, validation 422,
  JSON 400/413, private path 404 đều đạt.
- Kiểm thử regression trên trình duyệt: chuyển robot xóa nút Gỡ cũ ngay, tải lỗi
  không giữ danh sách cũ, chuyển danh mục lỗi không còn hàng cũ để sửa nhầm,
  xóa mục cuối trang 2 tự về trang 1.
- Ảnh quản trị tại 375/768/1024/1440px: không tràn ngang; đã xem ảnh mobile/desktop.
  Không có JavaScript exception. Ảnh kiểm thử nằm trong thư mục artifacts bị Git bỏ qua.
- Kiểm tra cú pháp JavaScript/CJS đạt.

Demo sử dụng fixture admin cục bộ. Chưa xác nhận đăng nhập production/CSRF thật,
frontend công khai đọc API hoặc vị trí kéo-thả 2D xuyên suốt. Các điểm cần nhóm nối
đã ghi cụ thể trong TEAM_HANDOFF.md và INTEGRATION.md.

## Lịch sử kiểm tra 2026-09-14

## MySQL thực tế

- Kết nối thành công MySQL 8.0.46 trên máy phát triển.
- Tạo database kiểm thử riêng `robot_lab_content_test`; không sửa database Robot Lab đang dùng.
- Chạy `database/migrations/001_initial.sql` và `database/seed.sql` thành công trên database mới.
- `node --env-file=server/content/.env tests/content/mysql-smoke.cjs`: đạt.
- Xác nhận seed có 3 robot, 14 linh kiện, 15 bước.
- Kiểm tra tạo, đọc, sửa, xóa linh kiện và thêm/gỡ quan hệ linh kiện–robot.
- MySQL chặn ID trùng, tham chiếu robot không tồn tại, xóa linh kiện đang được tham chiếu, lưu bước thuộc robot khác và xóa bước đã có tiến độ.
- Dữ liệu phát sinh trong smoke test được rollback. Database kiểm thử được giữ lại với schema và seed để chạy lại.

## Bộ kiểm thử tự động

`npm test --prefix server/content`: 7 kiểm thử đạt, gồm validation, seed, repository và HTTP với Express thật/repository giả.

Kết quả MySQL ở trên kiểm tra repository và constraint trên database thật; bộ HTTP dùng middleware phân quyền/CSRF giả chỉ trong test. Chưa xác nhận luồng đăng nhập, CSRF, giao diện quản trị và lưu tiến độ xuyên suốt với backend tích hợp của Tài/Tuấn Anh.

Cấu hình kết nối nằm trong `server/content/.env` được Git bỏ qua. Không đưa mật khẩu vào tài liệu hoặc commit.
