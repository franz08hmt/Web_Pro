# Rà soát bảo mật full-stack

Ngày rà soát: 2026-09-18. Phạm vi: xác thực, Content API, phiên lắp ráp,
frontend API client và adapter MySQL trên `integration/fullstack-v2`.

## Biên tin cậy và tài sản cần bảo vệ

- Request HTTP và dữ liệu từ Content API là dữ liệu không tin cậy; controller
  kiểm tra kiểu, field cho phép, slug, enum và giới hạn phân trang.
- Cookie phiên, CSRF token, password hash và thông tin kết nối MySQL là dữ liệu
  nhạy cảm. Token gốc chỉ nằm trong cookie `HttpOnly`; database chỉ lưu hash.
- Phiên lắp ráp thuộc về một user. Truy vấn phiên không phân biệt "không tồn
  tại" với "thuộc user khác" để tránh dò ID.
- Truy vấn MySQL sử dụng placeholder; JSON nội dung không được ghép thành SQL.

## Kiểm soát đã xác nhận

- Password dùng `scrypt` với salt ngẫu nhiên; đăng nhập sai vẫn thực hiện dummy
  verify để giảm rò rỉ timing.
- Cookie dùng `HttpOnly`, `SameSite=Lax`, `Secure` trong production và có hạn.
- Mọi route phiên lắp ráp yêu cầu đăng nhập; thao tác ghi yêu cầu CSRF.
- Route quản trị yêu cầu role `ADMIN` và CSRF.
- CORS chỉ cho same-origin hoặc danh sách `CORS_ORIGINS`; body JSON tối đa 256 KB.
- Helmet, rate limit, lỗi an toàn và request ID được bật trên Express chung.
- Chỉ sửa chuẩn bị linh kiện ở `PREPARING`/`READY`; chỉ sửa bước ở
  `IN_PROGRESS`; phiên kết thúc không thể bị thay đổi.
- Không có `.env`, token hay mật khẩu thật trong danh sách file được Git theo
  dõi. Các file `.env.example` chỉ chứa placeholder.

## Bằng chứng kiểm thử

- `npm test`: kiểm tra auth, quyền ADMIN, CSRF, CORS, validation, ownership và
  state machine của phiên.
- Content tests và MySQL smoke/adapter tests: kiểm tra truy vấn tham số, rollback,
  concurrency, ownership, expiry và revocation.
- `npm run test:ui`: 32 lượt responsive trên Chrome, gồm dữ liệu Content API,
  trang tài khoản, chuẩn bị và phòng 3D.
- `npm audit --omit=dev --audit-level=high`: không phát hiện vulnerability.

## Ghi chú vận hành

- Production phải chạy qua HTTPS để cookie `Secure` có hiệu lực.
- Không dùng database production cho test. Cấu hình test phải có tên kết thúc
  `_test` như guard trong các script.
- Lịch sử cũ từng có chuỗi mật khẩu trong file mẫu content. File hiện đã đổi sang
  placeholder; nếu chuỗi đó từng được dùng làm mật khẩu thật, phải đổi mật khẩu
  MySQL tương ứng trước khi chia sẻ hoặc triển khai repository.
