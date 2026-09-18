# Handoff tích hợp backend

Tài liệu này ghi chính xác **ranh giới bàn giao** giữa ba phần việc: backend/tích
hợp (Tài), nội dung/dữ liệu MySQL (Nhi), mô phỏng lắp ráp 2D (Tuấn Anh). Xem hợp
đồng API đầy đủ tại `docs/API_CONVENTIONS.md`.

Nguyên tắc: **không có TODO chung chung.** Mỗi mục chờ dưới đây có chủ sở hữu rõ
và tiêu chí nghiệm thu rõ.

## Trạng thái hiện tại (2026-09-18)

- Content Router, Content Repository và adapter MySQL cho user, auth session và
  assembly session của Nhi đã được gắn vào Express chung.
- Auth session thật đã có register/login/me/logout, cookie `HttpOnly`, phân quyền
  ADMIN và CSRF cho thao tác ghi quản trị.
- API phiên lắp ráp đọc robot, linh kiện và bước thật từ MySQL; không còn dùng
  `server/config/known-robots.js` hay chấp nhận ID tùy ý.
- Mô phỏng 3D của Tuấn Anh đã được tích hợp. Phần còn lại của Tài là nối
  `assets/js/api.js`, trang tài khoản và trạng thái mô phỏng với các endpoint
  phiên thật, rồi kiểm thử end-to-end.

## 1. Hợp đồng MySQL đã được Nhi bàn giao

### 1.1. Repository contract phải cắm được

```text
UserRepository
- findByEmail(email)              // email đã chuẩn hóa (lowercase, trim)
- findPublicById(userId)
- create({ fullName, email, passwordHash, role })

AuthSessionRepository
- create({ userId, tokenHash, expiresAt })
- findActiveByTokenHash(tokenHash)   // trả null nếu hết hạn hoặc đã thu hồi
- revokeByTokenHash(tokenHash)
- revokeAllForUser(userId)           // dùng khi cần "đăng xuất mọi thiết bị"; chưa có route gọi, để sẵn

AssemblySessionRepository
- createOrResume({ userId, robotId })
- listByUser({ userId, page, pageSize, status })
- findOwnedById({ sessionId, userId })   // null nếu không tồn tại HOẶC không thuộc user này
- updateStatus({ sessionId, userId, status })
- upsertComponentProgress({ sessionId, userId, componentId, isPrepared })
- upsertStepProgress({ sessionId, userId, stepId, status })
```

Interface đầy đủ (dùng để viết adapter MySQL cắm thay thế in-memory) nằm tại
`server/repositories/ports/`.

### 1.2. Các quyết định schema đã chốt

| # | Câu hỏi | Vì sao Tài cần |
| --- | --- | --- |
| 1 | Kiểu khóa chính (`BIGINT AUTO_INCREMENT`? `CHAR(36)` UUID? `VARCHAR` slug?) và cách serialize ra JSON (số hay chuỗi)? | `User.id`, `AssemblySession.id` phải ổn định qua API; đổi kiểu sau khi frontend đã lưu là breaking change. |
| 2 | Unique index trên `email` có `LOWER(email)`/collation `_ci` không, hay backend phải tự chuẩn hóa trước khi ghi? | Hiện backend đang tự chuẩn hóa (`trim` + `toLowerCase`) trước khi gọi `findByEmail`/`create`. Nếu MySQL cũng áp constraint hoa/thường thì cần xác nhận hai lớp không xung đột. |
| 3 | Cột lưu password hash tên gì, độ dài tối thiểu (bcrypt hash dài 60 ký tự)? | Để viết migration khớp, không đoán tên cột. |
| 4 | Bảng lưu `AuthSession` (token hash + expiry) là bảng riêng hay cột trên `users`? Có cần index theo `expiresAt` để dọn phiên hết hạn không? | Ảnh hưởng cách `revokeByTokenHash`/dọn dẹp định kỳ được viết. |
| 5 | `robots` table: `id` có khớp đúng ba chuỗi hiện dùng (`line-follower`, `obstacle-avoider`, `mini-arm`) không, hay Nhi đổi sang khóa khác? | Đây là **breaking change** nếu đổi — toàn bộ `assets/js/data.js`, `main.js`, URL query `?model=` đang dùng đúng ba chuỗi này. |
| 6 | `components` mỗi robot: cấp `componentId` ổn định cho từng dòng trong `ROBOT_MODELS[].parts[]` bằng cách nào — thêm cột `component_id` tham chiếu `COMPONENTS_DATA[].id`, hay một bảng nối `robot_components(robot_id, component_id, quantity)`? | `AssemblySessionRepository.upsertComponentProgress` cần `componentId` thật để đối chiếu "đủ chưa"; hiện `parts[]` chỉ có `name`, không có id. |
| 7 | `assembly_steps` mỗi robot: cấp `stepId` ổn định cho từng phần tử `ROBOT_MODELS[].steps[]` bằng cách nào? | Tương tự mục 6, cho `upsertStepProgress`. |
| 8 | Unique constraint `(session_id, component_id)` và `(session_id, step_id)` — đã có trong thiết kế bảng progress chưa? | Đảm bảo upsert không tạo bản ghi trùng. |
| 9 | Foreign key `assembly_sessions.user_id -> users.id`, `assembly_sessions.robot_id -> robots.id` dùng `ON DELETE` gì (`CASCADE`/`RESTRICT`)? | Ảnh hưởng hành vi khi xóa user/robot; cần nhất quán với nghiệp vụ thật (không tự quyết định thay Nhi). |
| 10 | Index phục vụ `listByUser({ userId, status, page })` — có index `(user_id, status, created_at)` không? | Query danh sách phiên sẽ chậm nếu thiếu; Tài không tự thêm index vào schema của Nhi. |

Mười mục trên đã được trả lời và triển khai trong migrations, seed, Content
Repository và `server/content/backend-repositories.cjs`. Chi tiết đối chiếu nằm
tại `docs/content/TEAM_HANDOFF.md`.

## 2. Cần Tuấn Anh biết để gọi API phiên lắp ráp

Namespace `assemblySessionsApi` trong `assets/js/api.js` (đã export ở
`window.RobotAssemblyApi.assemblySessions`, xem `docs/API_CONVENTIONS.md` cho
response shape đầy đủ):

```text
assemblySessionsApi.createOrResume(robotId)
  -> Promise<AssemblySession>
  Gọi khi người dùng bắt đầu/mở lại một mẫu robot trong mô-đun 2D.

assemblySessionsApi.get(sessionId)
  -> Promise<AssemblySession>
  Gọi khi mô-đun 2D cần load lại trạng thái đã lưu (ví dụ sau khi tải lại trang).

assemblySessionsApi.list(params?)
  -> Promise<{ items: AssemblySessionSummary[], meta }>
  params: { status?, page?, pageSize? }

assemblySessionsApi.updateStatus(sessionId, status)
  -> Promise<AssemblySession>
  status thuộc { "IN_PROGRESS", "COMPLETED", "ABANDONED" } — xem bảng transition
  hợp lệ trong docs/API_CONVENTIONS.md. Gọi "IN_PROGRESS" khi người dùng bắt đầu
  thao tác 2D, "COMPLETED" khi mô phỏng xác nhận lắp xong.

assemblySessionsApi.setComponentPrepared(sessionId, componentId, isPrepared)
  -> Promise<AssemblySession>
  Gọi mỗi khi người dùng tick/bỏ tick một linh kiện. Idempotent — gọi lại nhiều
  lần với cùng giá trị không tạo lỗi.

assemblySessionsApi.setStepStatus(sessionId, stepId, status)
  -> Promise<AssemblySession>
  status thuộc { "PENDING", "COMPLETED" }. Gọi mỗi khi một bước lắp ráp trong mô
  phỏng 2D được đánh dấu xong.
```

Mọi method trả về Promise, reject bằng object lỗi có `{ code, message, status }`
(xem chi tiết ở mục "Hình dạng lỗi trả cho frontend" bên dưới) — **không** ném
lỗi thô của `fetch`. Không có method nào biết về Canvas/SVG hay state machine
2D; module 2D tự quyết định khi nào gọi các hàm trên.

Mô-đun lắp ráp phải gửi đúng `componentId` và `stepId` mà Content API trả về.
Backend kiểm tra hai ID này thuộc đúng robot của phiên và từ chối ID nội bộ hoặc
ID không tồn tại bằng `422 VALIDATION_ERROR`.

## 3. Hình dạng lỗi trả cho frontend

`assets/js/api.js` chuyển mọi lỗi HTTP (kể cả lỗi mạng/timeout) thành:

```json
{ "code": "VALIDATION_ERROR", "message": "Dữ liệu không hợp lệ.", "status": 422 }
```

`code`/`status` lấy từ response envelope lỗi khi có; nếu request thất bại trước
khi có response (mất mạng, timeout, CORS), `code` là `"NETWORK_ERROR"` và
`status` là `0`. `message` luôn là chuỗi tiếng Việt an toàn để hiển thị thẳng
cho người dùng — không bao giờ chứa stack trace hay chi tiết nội bộ.

## 4. Biến môi trường liên quan tới handoff

| Biến | Ý nghĩa | Ai cần biết |
| --- | --- | --- |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Bật MySQL pool thật. Phải điền đủ cả 5. | Nhi, khi schema đã migrate xong trên máy/CI. |
| `SESSION_TOKEN_TTL_MS` | Thời gian sống của cookie phiên (mặc định 7 ngày, tính bằng mili-giây). | Tài. |

## 5. Điều kiện coi một mục "đã xong" và có thể gỡ khỏi danh sách chờ

- Phần dữ liệu Nhi: hoàn thành khi migration/seed chạy, Content API trả đúng ID
  và adapter MySQL vượt kiểm thử tích hợp.
- Phần mô phỏng Tuấn Anh: hoàn thành khi 3D tải được, mapping đúng ID Content API,
  khôi phục tiến độ và lưu mọi thay đổi qua API phiên của Tài.
- Phần Tài: hoàn thành khi auth, tài khoản, frontend API client và phiên lắp ráp
  chạy xuyên suốt trên cùng origin, có trạng thái tải/lỗi và toàn bộ test xanh.
