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
| `GET` | `/api/auth/me` | Cookie phiên | `200`, `{ data: { user } }` hoặc `401 AUTH_REQUIRED` | — |

**Validation (`VALIDATION_ERROR` nếu vi phạm):**

- `fullName`: chuỗi, sau khi trim dài 2–100 ký tự.
- `email`: chuỗi hợp lệ theo định dạng email; được chuẩn hóa (`trim` + `toLowerCase`)
  trước khi so sánh/lưu, để so khớp không phân biệt hoa/thường.
- `password`: chuỗi, dài 8–72 ký tự (72 là giới hạn kỹ thuật của bcrypt).

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

`role` hiện chỉ có giá trị `"USER"`. `"ADMIN"` được để dành cho trang quản trị
nội dung của Nhi ở giai đoạn sau — **chưa route nào trong phần Tài kiểm tra vai
trò `ADMIN`.**

**Cookie phiên:** tên `ral_session`, `HttpOnly`, `SameSite=Lax`, `Secure` khi
`NODE_ENV=production`, `Path=/api`, hết hạn theo `expiresAt` của phiên (mặc định
7 ngày). Server lưu **hash** của token, không lưu token gốc.

### Phiên lắp ráp

Yêu cầu cookie phiên hợp lệ cho tất cả; thiếu → `401 AUTH_REQUIRED`.

| Method | Path | Mục đích |
| --- | --- | --- |
| `POST` | `/api/assembly-sessions` | Tạo phiên mới hoặc tiếp tục phiên `PREPARING`/`READY`/`IN_PROGRESS` gần nhất cho `robotId` của user hiện tại. |
| `GET` | `/api/assembly-sessions` | Danh sách phiên của user hiện tại. Hỗ trợ `?status=`, `?page=`, `?pageSize=` (mặc định 1/20, tối đa 100). |
| `GET` | `/api/assembly-sessions/:sessionId` | Lấy một phiên cùng toàn bộ `components` và `steps`. |
| `PATCH` | `/api/assembly-sessions/:sessionId` | Đổi `status` của phiên theo enum bên dưới. |
| `PUT` | `/api/assembly-sessions/:sessionId/components/:componentId` | Upsert `isPrepared` cho một linh kiện. Idempotent. |
| `PUT` | `/api/assembly-sessions/:sessionId/steps/:stepId` | Upsert `status` cho một bước lắp ráp. Idempotent. |

Mọi thao tác đọc/ghi phải kiểm tra `session.userId === request.user.id`; sai chủ
sở hữu → `403 FORBIDDEN` (không phải `404`, để phân biệt rõ với "không tồn tại"
trong log nội bộ dù response trả cùng cấu trúc lỗi).

**`robotId`:** phải khớp một trong các ID hiện có của Nhi: `line-follower`,
`obstacle-avoider`, `mini-arm`. ID khác → `422 VALIDATION_ERROR`. Danh sách này
đọc từ `server/config/known-robots.js` (xem `docs/BACKEND_HANDOFF.md`) — **không
phải** từ MySQL, vì bảng `robots` của Nhi chưa tồn tại.

**`componentId` / `stepId`:** theo hợp đồng, đây là ID ổn định do API nội dung
của Nhi cấp phát. **Tại thời điểm viết tài liệu này, `assets/js/data.js` chưa có
ID này** — `ROBOT_MODELS[].parts[]` chỉ có `{ name, quantity }` và
`ROBOT_MODELS[].steps[]` là mảng chuỗi thuần. Vì vậy ở lát cắt hiện tại, hai
endpoint `PUT` chấp nhận **bất kỳ chuỗi non-empty nào** làm `componentId`/`stepId`
(validate định dạng, không đối chiếu với danh mục thật) và lưu opaque. Xem mục
"Cần Nhi xác nhận" trong `docs/BACKEND_HANDOFF.md`.

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

**`progressPercent`:** tính ở service, làm tròn xuống, bằng
`round(số component có isPrepared=true / tổng số component bắt buộc của robotId × 100)`.
Danh sách "tổng số component bắt buộc" đọc từ `server/config/known-robots.js`
(cùng nguồn với validate `robotId`), **không** tin số phần trăm client gửi lên —
không endpoint nào nhận `progressPercent` làm input.

**Assembly session response shape:**

```json
{
  "id": "session-uuid",
  "userId": "user-uuid",
  "robotId": "line-follower",
  "status": "PREPARING",
  "progressPercent": 25,
  "components": [
    { "componentId": "chassis-2wd", "isPrepared": true, "updatedAt": "2026-09-16T00:00:00.000Z" }
  ],
  "steps": [
    { "stepId": "step-1", "status": "PENDING", "updatedAt": "2026-09-16T00:00:00.000Z" }
  ],
  "createdAt": "2026-09-16T00:00:00.000Z",
  "updatedAt": "2026-09-16T00:00:00.000Z"
}
```

`GET /api/assembly-sessions` (danh sách) trả từng phần tử ở dạng rút gọn — bỏ
`components`/`steps` — kèm `meta: { page, pageSize, total }`.

## Adapter dữ liệu chưa sẵn sàng

Nếu `NODE_ENV=production` và chưa cấu hình đủ 5 biến MySQL (`DB_HOST`, `DB_PORT`,
`DB_NAME`, `DB_USER`, `DB_PASSWORD`), mọi route dưới `/api/auth` và
`/api/assembly-sessions` trả `503 DEPENDENCY_NOT_READY` thay vì âm thầm dùng bộ
nhớ trong tiến trình. Bộ nhớ trong tiến trình (`InMemory*Repository`) chỉ được
dùng khi `NODE_ENV` là `development` hoặc `test`, **và** chỉ khi biến
`ALLOW_IN_MEMORY_STORE=true` được đặt rõ ràng — mặc định là tắt để tránh bật
nhầm ở môi trường không phải máy dev.

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
