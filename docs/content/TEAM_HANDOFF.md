# Hợp đồng Nhi / Tài / Tuấn Anh

Đối chiếu `docs/BACKEND_HANDOFF.md`, `docs/API_CONVENTIONS.md` và repository ports
ở nền `3fda472` ngày 2026-09-16. Đây là câu trả lời bằng schema/code hiện có,
không khẳng định nhóm đã chạy auth và mô phỏng 2D xuyên suốt.

## 10 câu hỏi của Tài

| # | Quyết định đã triển khai |
|---|---|
| 1 | users, auth_sessions, assembly_sessions dùng BIGINT UNSIGNED tự tăng; API/adapter trả chuỗi số thập phân. Robot/component/step dùng slug ASCII binary <=64. Không dùng UUID cho user/session. |
| 2 | users.email UNIQUE, utf8mb4_0900_ai_ci; service chuẩn hóa trim/lowercase, adapter create cũng chuẩn hóa. Email trùng trả EMAIL_ALREADY_EXISTS 409. |
| 3 | users.password_hash VARCHAR(255). Băm/kiểm tra mật khẩu ở service Tài. findPublicById không trả hash. |
| 4 | auth_sessions riêng từ migration 002: token_hash CHAR(64) UNIQUE, expires_at có index, revoked_at nullable. Adapter hỗ trợ revoke một phiên/tất cả, bỏ qua phiên hết hạn/thu hồi. |
| 5 | Giữ line-follower, obstacle-avoider, mini-arm. CRUD có thể tạo robot mới; Tài phải đọc robots thật thay danh sách known-robots tĩnh khi tích hợp. |
| 6 | robot_components(robot_id,component_id,quantity); GET /api/robots/:id/components cung cấp ID và số lượng thật. |
| 7 | assembly_steps.id dạng mini-arm-step-1; GET /api/robots/:id/steps trả id, stepOrder, instruction, illustration. Không gửi step-1 thuần. |
| 8 | PK(session_id,component_id), PK(session_id,step_id). Upsert adapter idempotent về trạng thái, kiểm tra thành phần thuộc robot của phiên. |
| 9 | user/robot -> assembly_sessions RESTRICT. session -> progress CASCADE. Nội dung đã được tham chiếu RESTRICT. user -> auth_sessions CASCADE. |
| 10 | Migration 002 thêm (user_id,status,created_at), (user_id,robot_id,created_at). Adapter list có phân trang và sắp theo created_at/id. |

## Adapter đã cung cấp

`server/content/backend-repositories.cjs` export `createBackendRepositories(pool)`:

| Kết quả | Port tương ứng | Ánh xạ |
|---|---|---|
| users | UserRepositoryPort | display_name -> fullName, user/admin -> USER/ADMIN, ID -> string |
| authSessions | AuthSessionRepositoryPort | token_hash/expires_at/revoked_at -> camelCase |
| assemblySessions | AssemblySessionRepositoryPort | prepared 0/1 -> isPrepared boolean; completed 0/1 -> PENDING/COMPLETED |

Adapter implements đủ method trong ba port; dùng pool mysql2/promise thật, mỗi thay
đổi tiến độ khóa session và kiểm tra userId. createOrResume khóa user để chống tạo
phiên đang dở trùng khi có request đồng thời. findOwnedById trả null khi không tồn tại
hoặc khác chủ; mutation trả 404, không tiết lộ session của người khác. Hợp đồng nhóm
đang ghi 403 ở một đoạn nhưng port trả null cho cả hai trường hợp: Tài cần thống nhất
service HTTP theo port hoặc bổ sung phương thức riêng nếu bắt buộc phân biệt 403/404.

Adapter không băm password, đặt cookie, tính progressPercent, kiểm tra thứ tự bước
hoặc quyết định chuyển PREPARING/READY/IN_PROGRESS/COMPLETED. Đó là service của Tài;
service phải kiểm tra quy tắc trước khi gọi repository và xử lý cạnh tranh giữa
chuyển trạng thái với ghi tiến độ. Nhi không sửa interface để tránh thay đổi ngầm.

`simulation_state` đã có trong DB nhưng port upsertStepProgress hiện chỉ nhận status.
Lưu vị trí kéo-thả để khôi phục toàn cảnh 2D cần Tài/Tuấn Anh chốt payload và mở rộng
port/API cùng lúc. Hiện adapter khôi phục được dấu hoàn thành, chưa trả tọa độ mô phỏng.

## Tuấn Anh lấy dữ liệu thế nào

1. GET `/api/robots/:id`: thông tin robot, không nhúng parts/steps.
2. GET `/api/robots/:id/components`: `{componentId,quantity}`; lấy tên/ảnh qua API components.
3. GET `/api/robots/:id/steps?limit=100`: `{id,robotId,stepOrder,title,instruction,illustration}`.
   Dùng meta.total để lấy tiếp nếu có hơn 100 bước; không mặc định chỉ có 5 bước.
4. Gửi đúng componentId và step.id vào API phiên của Tài; không dùng tên linh kiện,
   chỉ số mảng hoặc ID nội bộ canvas để lưu DB.
5. illustration đang là `{}` trong seed. Mô phỏng phải xử lý khi chưa có dữ liệu;
   thống nhất cấu trúc JSON trước khi Nhi thêm minh họa.

Đếm số dòng linh kiện bắt buộc, không cộng quantity để tính số ô checklist.
API nội dung phân trang `limit`; API phiên phân trang `pageSize`.
Không đổi slug/field sau khi đã có tiến độ.

## Trạng thái ghép nhóm

Đã kiểm tra nội dung, MySQL adapters và test nền Tài trong cùng checkout. Các nhánh
remote của Tài/Tuấn Anh đã được kiểm tra lịch sử; không có backend auth/service hoặc
module mô phỏng 2D hoàn chỉnh để xác nhận end-to-end ở thời điểm này. Một số câu trong
BACKEND_HANDOFF.md mô tả api.js/in-memory adapter nhưng file đó chưa có ở commit nền.
Không coi tài liệu mô tả tính năng là bằng chứng tính năng đã chạy.

Tiêu chí Tài nghiệm thu: inject adapters, gắn content router, auth ADMIN thật,
CSRF, body limit, HTTP error handler, serve frontend cùng origin.
Tiêu chí Tuấn Anh nghiệm thu: mapping ID, tải lại tiến độ, reset/hoàn thành, payload 2D.
