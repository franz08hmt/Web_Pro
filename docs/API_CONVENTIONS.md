# Quy ước REST API

Nền API đặt tại `/api`. Lớp route chỉ nhận HTTP và kiểm tra input; logic nghiệp vụ
nằm trong `server/services`, còn truy vấn MySQL nằm trong `server/repositories`.
Cách tách này giúp dữ liệu do Nhi phụ trách và mô phỏng 2D của Tuấn Anh không phụ
thuộc trực tiếp vào Express.

Luồng phụ thuộc bắt buộc: `route -> controller -> service -> repository port -> adapter`.
Route/controller không chứa truy vấn hoặc nghiệp vụ. Service không biết Express
hoặc SQL. Repository là ranh giới dữ liệu; adapter MySQL thật chỉ được thêm sau
khi schema của Nhi được chốt (xem `docs/BACKEND_HANDOFF.md`).

## Response thành công

```json
{
  "data": {}
}
```

Danh sách có thể bổ sung `meta` mà không thay đổi `data`. Tên field dùng
camelCase; endpoint là danh từ số nhiều, ví dụ `/api/robots`, `/api/assembly-sessions`.

## Response lỗi

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Dữ liệu không hợp lệ.",
    "requestId": "...",
    "details": {}
  }
}
```

Không trả stack trace, mật khẩu, password hash, session token, chuỗi kết nối
hoặc thông tin nội bộ khác trong bất kỳ response hoặc log nào.

### Mã lỗi

| Code | Status | Dùng khi |
| --- | --- | --- |
| `INVALID_JSON` | 400 | Body không parse được thành JSON. |
| `VALIDATION_ERROR` | 422 | Input đúng định dạng JSON nhưng sai kiểu/thiếu field/vi phạm ràng buộc. |
| `AUTH_REQUIRED` | 401 | Endpoint cần đăng nhập nhưng không có cookie phiên hợp lệ. |
| `INVALID_CREDENTIALS` | 401 | Sai email hoặc mật khẩu khi đăng nhập. Không tiết lộ email nào tồn tại. |
| `EMAIL_ALREADY_EXISTS` | 409 | Đăng ký với email đã tồn tại (so khớp dạng chuẩn hóa, không phân biệt hoa/thường). |
| `FORBIDDEN` | 403 | Đã đăng nhập nhưng không có quyền trên tài nguyên (không phải chủ sở hữu). |
| `NOT_FOUND` | 404 | Tài nguyên hoặc route không tồn tại. |
| `INVALID_STATE_TRANSITION` | 409 | Yêu cầu chuyển trạng thái phiên lắp ráp không hợp lệ. |
| `CORS_ORIGIN_DENIED` | 403 | Origin không nằm trong `CORS_ORIGINS`. |
| `RATE_LIMITED` | 429 | Vượt giới hạn request. |
| `DEPENDENCY_NOT_READY` | 503 | Endpoint cần một adapter dữ liệu thật (MySQL) nhưng chưa được cấu hình ở production. |
| `INTERNAL_SERVER_ERROR` | 500 | Lỗi không xác định; chi tiết chỉ vào log server, không vào response. |

## Endpoint hiện có

### Hạ tầng

| Method | Path | Mục đích |
| --- | --- | --- |
| `GET` | `/api/health` | Kiểm tra API và trạng thái pool MySQL. |

`GET /api/health` trả `database: "not_configured"` khi máy local chưa điền đủ
biến MySQL, và `"connected"` khi query `SELECT 1` thành công.

### Xác thực

| Method | Path | Input | Kết quả thành công | Cookie |
| --- | --- | --- | --- | --- |
| `POST` | `/api/auth/register` | `fullName`, `email`, `password` | `201`, `{ data: { user } }` | Đặt cookie phiên |
| `POST` | `/api/auth/login` | `email`, `password` | `200`, `{ data: { user } }` | Đặt cookie phiên |
| `POST` | `/api/auth/logout` | Không có | `204`, không body | Xóa cookie, thu hồi phiên |
| `GET` | `/api/auth/me` | Cookie phiên | `200`, `{ data: { user }, csrfToken }` hoặc `401 AUTH_REQUIRED` | — |

**Validation (`VALIDATION_ERROR` nếu vi phạm):**

- `fullName`: chuỗi, sau khi trim dài 2–100 ký tự.
- `email`: chuỗi hợp lệ theo định dạng email; được chuẩn hóa (`trim` + `toLowerCase`)
  trước khi so sánh/lưu, để so khớp không phân biệt hoa/thường.
- `password`: chuỗi, dài 8–72 ký tự; backend băm bằng `scrypt` với salt ngẫu nhiên.

**User public shape** (không bao giờ có `passwordHash`):

```json
{
  "id": "stable-id",
  "fullName": "Nguyễn Văn A",
  "email": "user@example.com",
  "role": "USER",
  "createdAt": "2026-09-16T00:00:00.000Z"
}
```

`role` có giá trị `"USER"` hoặc `"ADMIN"`. Các route ghi dưới `/api/admin/*`
yêu cầu phiên `ADMIN` và header `X-CSRF-Token` hợp lệ.

**Cookie phiên:** tên `ral_session`, `HttpOnly`, `SameSite=Lax`, `Secure` khi
`NODE_ENV=production`, `Path=/api`, hết hạn theo `expiresAt` của phiên (mặc định
7 ngày). Server lưu **hash** của token, không lưu token gốc.

### Phiên lắp ráp

Yêu cầu cookie phiên hợp lệ cho tất cả; thiếu → `401 AUTH_REQUIRED`.

| Method | Path | Mục đích |
| --- | --- | --- |
| `POST` | `/api/assembly-sessions` | Tạo phiên mới hoặc tiếp tục phiên `PREPARING`/`READY`/`IN_PROGRESS` gần nhất cho `robotId` của user hiện tại. |
| `GET` | `/api/assembly-sessions` | Danh sách phiên của user hiện tại. Hỗ trợ `?status=`, `?page=`, `?pageSize=` (mặc định 1/20, tối đa 100). |
| `GET` | `/api/assembly-sessions/:sessionId` | Lấy một phiên cùng toàn bộ `components`, `steps` và `assembledPartIds`. |
| `PATCH` | `/api/assembly-sessions/:sessionId` | Đổi `status` của phiên theo enum bên dưới. |
| `PUT` | `/api/assembly-sessions/:sessionId/components/:componentId` | Upsert `isPrepared` cho một linh kiện. Idempotent. |
| `PUT` | `/api/assembly-sessions/:sessionId/steps/:stepId` | Upsert `status` cho một bước lắp ráp. Idempotent. |
| `PUT` | `/api/assembly-sessions/:sessionId/visual-parts/:componentId` | Body `{ "isAssembled": boolean }`. Lưu/gỡ bộ phận trên mô hình 3D của đúng phiên; chỉ khi `IN_PROGRESS`. Idempotent, yêu cầu CSRF. |

Mọi thao tác đọc/ghi phải kiểm tra quyền sở hữu bằng
`assemblySessions.findOwnedById({ sessionId, userId })`. Phiên không tồn tại và
phiên thuộc người dùng khác đều trả `404 NOT_FOUND`; không phân biệt hai trường
hợp này ở HTTP để tránh dò ID phiên của người dùng khác.

**`robotId`:** phải tồn tại trong bảng `robots`, được kiểm tra qua Content
Repository của Nhi. ID không tồn tại → `422 VALIDATION_ERROR`. Backend không duy
trì thêm danh sách robot tĩnh.

**`componentId`:** phải thuộc danh sách trả bởi
`GET /api/robots/:robotId/components`. **`stepId`:** phải thuộc danh sách trả bởi
`GET /api/robots/:robotId/steps`. ID không thuộc mẫu robot của phiên →
`422 VALIDATION_ERROR`. Backend không chấp nhận ID tùy ý hoặc suy luận ID từ tên
hiển thị.

**Enum `AssemblySession.status`:**

| Giá trị | Ý nghĩa | Ai đặt |
| --- | --- | --- |
| `PREPARING` | Đang chuẩn bị linh kiện, `progressPercent < 100`. | Server tự tính, mặc định khi tạo phiên. |
| `READY` | Đã đủ linh kiện, `progressPercent === 100`, chưa bắt đầu các bước. | Server tự chuyển khi `progressPercent` đạt 100. |
| `IN_PROGRESS` | Đang thực hiện các bước lắp ráp. | Client gọi `PATCH` để chuyển từ `READY`. |
| `COMPLETED` | Đã hoàn tất phiên lắp ráp. | Client gọi `PATCH` từ `IN_PROGRESS`, hoặc server tự chuyển khi mọi step `COMPLETED`. |
| `ABANDONED` | Người dùng hủy phiên. | Client gọi `PATCH` từ bất kỳ trạng thái nào trừ `COMPLETED`. |

Transition hợp lệ: `PREPARING <-> READY` (tự động theo `progressPercent`,
không nhận qua `PATCH`); `READY -> IN_PROGRESS`; `IN_PROGRESS -> COMPLETED`;
bất kỳ trạng thái nào (trừ `COMPLETED`) `-> ABANDONED`. `PATCH` với target không
nằm trong danh sách trên trả `409 INVALID_STATE_TRANSITION`. Gọi `PATCH` với
đúng `status` hiện tại là no-op hợp lệ, trả `200` (idempotent).

**Enum `StepProgress.status`:** `PENDING` (mặc định) | `COMPLETED`.

Chỉ được sửa `session_components` khi phiên đang `PREPARING` hoặc `READY`. Chỉ
được sửa `session_steps` và `session_visual_parts` khi phiên đang `IN_PROGRESS`. Phiên `COMPLETED` và
`ABANDONED` là bất biến; thao tác ghi không phù hợp trả
`409 INVALID_STATE_TRANSITION`.

**`progressPercent`:** tính ở service, làm tròn xuống, bằng
`floor(số nhóm component có isPrepared=true / tổng số nhóm component bắt buộc của robotId × 100)`.
Tổng số nhóm lấy từ bảng nối `robot_components` qua Content Repository. `quantity`
không nhân thêm số ô checklist. Server không tin số phần trăm client gửi lên —
không endpoint nào nhận `progressPercent` làm input.

**Assembly session response shape:**

```json
{
  "id": "42",
  "userId": "7",
  "robotId": "line-follower",
  "status": "PREPARING",
  "progressPercent": 25,
  "components": [
    { "componentId": "chassis-2wd", "isPrepared": true, "updatedAt": "2026-09-16T00:00:00.000Z" }
  ],
  "steps": [
    { "stepId": "step-1", "status": "PENDING", "updatedAt": "2026-09-16T00:00:00.000Z" }
  ],
  "assembledPartIds": ["chassis-2wd"],
  "createdAt": "2026-09-16T00:00:00.000Z",
  "updatedAt": "2026-09-16T00:00:00.000Z"
}
```

`GET /api/assembly-sessions` (danh sách) trả từng phần tử ở dạng rút gọn — bỏ
`components`/`steps`/`assembledPartIds`, thêm `completedStepCount` và
`totalStepCount` tính theo các bước hiện có của robot — kèm
`meta: { page, pageSize, total }`. `progressPercent` chỉ là tiến độ chuẩn bị,
không phải tỷ lệ bước hoặc tỷ lệ bộ phận 3D.

## Trạng thái phụ thuộc dữ liệu

Auth và phiên lắp ráp dùng trực tiếp adapter MySQL tại
`server/content/backend-repositories.cjs`; dữ liệu robot/linh kiện/bước dùng
Content Repository tại `server/content/repository.cjs`. Nếu chưa cấu hình đủ
năm biến MySQL (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`), các
route phụ thuộc database trả `503 DEPENDENCY_NOT_READY`; server không âm thầm
chuyển sang dữ liệu bộ nhớ hoặc danh sách robot tĩnh.

## Hợp đồng cần chốt trước tích hợp

Xem đầy đủ trong `docs/BACKEND_HANDOFF.md`. Tóm tắt:

- Nhi cung cấp schema MySQL thật khớp repository contract trong mục "Repository
  contracts" của tài liệu handoff, cộng với việc cấp `componentId`/`stepId` ổn
  định cho `assets/js/data.js` (hoặc API nội dung thay thế nó).
- Tuấn Anh chỉ gọi API phiên lắp ráp qua `assets/js/api.js`
  (namespace `assemblySessionsApi`), không gọi MySQL hay thao tác trực tiếp state
  của Express.
- Mọi endpoint ghi dữ liệu có validation tại route/controller và kiểm tra quyền
  sở hữu tại service trước khi chạm vào repository.
