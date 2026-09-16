# Demo phần Thảo Nhi

## Chạy ngay trên máy hiện tại

Trong terminal VS Code tại thư mục gốc dự án:

```powershell
node --env-file=server/content/.env tests/content/demo-server.cjs
```

Mở đường dẫn `http://127.0.0.1:<port>/__demo/start?key=...` được in trong terminal.
Port được chọn tự động để không trùng server khác. Giữ terminal mở khi demo;
Ctrl+C kết thúc và rollback mọi thay đổi demo. Nếu tiến trình bị tắt đột ngột,
MySQL rollback transaction khi kết nối đóng. Dừng demo trước khi chạy các test DB khác
để tránh giữ khóa trên dữ liệu đang sửa.

Đây là demo CRUD dùng Express và MySQL thật. Quyền admin là fixture cục bộ bảo vệ
bằng cookie HttpOnly/token ngẫu nhiên; không phải luồng đăng nhập production của Tài.
Chỉ bind 127.0.0.1 và chỉ chấp nhận database `_test`. Không deploy script này.
Các thay đổi demo nằm trong transaction nên Workbench ở kết nối khác không nhìn thấy
trước khi commit; demo không commit. Dùng API/trang admin để xem thay đổi tức thời.

## Máy mới

1. Cài Node >=20.6, MySQL >=8.0.16.
2. Chạy `npm ci` và `npm ci --prefix server/content`.
3. Tạo database kiểm thử riêng, áp dụng schema + seed một lần:

```sql
CREATE DATABASE robot_lab_content_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE robot_lab_content_test;
SOURCE E:/Java/web/Web_Pro/database/schema.sql;
SOURCE E:/Java/web/Web_Pro/database/seed.sql;
```

Sửa đường dẫn SOURCE theo máy. Nếu đã có migration 001, chỉ SOURCE migration 002;
không nạp lại schema/seed vào DB hiện có. Cấu hình `.env` theo `.env.example`, dùng
tài khoản được cấp quyền trên DB test. Không đưa mật khẩu vào Git.

## Kịch bản 5–7 phút

1. Giới thiệu: “Em phụ trách cơ sở dữ liệu, API nội dung, CRUD quản trị và lớp
   truy cập MySQL để backend của nhóm dùng chung.”
2. Mở `docs/erd.md`: giải thích robot_components là quan hệ nhiều-nhiều có quantity;
   session_steps/session_components dùng FK kép để không lưu tiến độ sai robot.
   Schema hiện tại có 11 bảng: 10 nghiệp vụ và schema_migrations.
3. Trên trang admin, chọn robot và chỉ ra danh sách linh kiện bắt buộc. Mở
   `/api/robots`, `/api/robots/mini-arm/components`, `/api/robots/mini-arm/steps`
   trên cùng origin của demo để cho thấy JSON đến từ database.
4. Chọn “Linh kiện” -> “Thêm mới”, nhập mẫu dưới đây; lưu, đổi tên, lưu lại,
   chọn lại từ danh sách để chứng minh dữ liệu được đọc lại từ API.
5. Chọn “Mô hình robot” -> Cánh tay robot mini; nhập component-id `demo-servo`,
   quantity `2`, lưu. Quay lại linh kiện và thử xóa demo-servo: API từ chối 409
   vì đang được tham chiếu. Gỡ quan hệ ở robot, sau đó xóa linh kiện thành công.
6. Cho thấy bộ kiểm thử validation chặn quantity=0, field lạ, ID trùng; không cần
   sửa dữ liệu seed để minh họa. Chạy lệnh test ở phần dưới sau khi dừng demo.
7. Kết luận: “Phần nội dung/MySQL chạy được độc lập. Backend xác thực thật và mô
   phỏng 2D được ghép theo hợp đồng đã ghi trong TEAM_HANDOFF.md.”

| Field | Giá trị mẫu |
|---|---|
| ID | demo-servo |
| Tên | Servo demo |
| Danh mục | Động cơ |
| Đường dẫn ảnh | /assets/images/components/servo-motor.png |
| Mô tả | Linh kiện thử nghiệm cho buổi demo. |
| Thông số JSON | {"Điện áp":"5V","Ghi chú":"Demo"} |

Các loại Robot/Bước/Thư viện cũng có tạo/sửa/xóa. Khi tạo bước, dùng robotId có thật
và stepOrder chưa trùng, ví dụ mini-arm/6. illustration nhập `{}`. Thư viện có thể
để trống robotId cho tài nguyên dùng chung. Mọi ô JSON phải là JSON hợp lệ.

## Lệnh kiểm thử

```powershell
node --test tests/content/*.test.cjs tests/server/*.test.cjs
node --env-file=server/content/.env tests/content/mysql-smoke.cjs
node --env-file=server/content/.env tests/content/backend-mysql.cjs
node --env-file=server/content/.env tests/content/migrations-mysql.cjs
```

Browser check tùy chọn dùng Playwright. Nếu đã có Playwright ở nơi khác, đặt
`CONTENT_PLAYWRIGHT_PATH` bằng đường dẫn module; có thể đặt `CONTENT_BROWSER_CHANNEL=msedge`
để dùng Edge đã cài. Nếu không, cài Playwright tạm ngoài repo bằng npm --prefix rồi trỏ
biến vào module đó. Chạy:

```powershell
node --env-file=server/content/.env tests/content/browser-check.cjs
```

Script tự mở server demo, chạy CRUD/MySQL, lỗi tải, phân trang, kiểm tra 4 viewport;
lưu ảnh ở `tests/content/artifacts/` (Git bỏ qua), đóng trình duyệt và rollback.

## Khi có lỗi

- Trang báo chưa đăng nhập: mở đúng link có key từ terminal, không mở file HTML trực tiếp.
- ECONNREFUSED: kiểm tra dịch vụ MySQL và CONTENT_TEST_DB_PORT.
- Thiếu migration 002: kiểm tra schema_migrations rồi áp dụng đúng migration còn thiếu.
- 409 khi xóa: gỡ các quan hệ trước; bản ghi có tiến độ được giữ lại theo thiết kế.
- Thay đổi admin chưa hiện ngoài trang công khai: frontend công khai vẫn dùng data.js,
  cần Tài nối API; demo này tập trung vào phần quản trị và database.
