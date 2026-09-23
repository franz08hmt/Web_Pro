# Quy ước Servlet API

API chạy trong cùng WAR và cùng origin với frontend, dưới `/api`. Servlet nhận
HTTP và kiểm tra dữ liệu; lớp `XxxDB` trong package `data` dùng JDBC truy cập MySQL;
dữ liệu đi giữa các tầng bằng JavaBean trong package `business`.

## Response

Thành công:

```json
{ "data": {} }
```

Danh sách có thể thêm `meta` gồm `page`, `limit` hoặc `pageSize`, `total`.

Lỗi:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Dữ liệu không hợp lệ.",
    "requestId": "uuid"
  }
}
```

`util/ResponseUtil.sendJson()` đặt JSON UTF-8, status code và
`Cache-Control: no-store`; `sendError()` tạo response lỗi có `requestId`.

| Status | Code chính | Ý nghĩa |
| --- | --- | --- |
| 400/422 | `VALIDATION_ERROR` | Path/query/body không hợp lệ |
| 401 | `AUTH_REQUIRED`, `INVALID_CREDENTIALS` | Chưa đăng nhập hoặc sai thông tin |
| 403 | `FORBIDDEN`, `CSRF_REQUIRED` | Thiếu quyền hoặc CSRF token |
| 404 | `NOT_FOUND` | Không có tài nguyên hoặc không thuộc người dùng |
| 409 | `CONFLICT`, `INVALID_STATE_TRANSITION` | Trùng/ràng buộc/trạng thái sai |
| 503 | `DEPENDENCY_NOT_READY` | Thiếu cấu hình hoặc chưa kết nối MySQL |

## Endpoint công khai

| Method | Path | Servlet |
| --- | --- | --- |
| GET | `/api/health` | `HealthServlet` |
| GET | `/api/robots` | `RobotServlet` |
| GET | `/api/robots/{robotId}/components` | `RobotServlet` |
| GET | `/api/robots/{robotId}/steps` | `RobotServlet` |
| GET | `/api/components` | `ComponentServlet` |
| GET | `/api/library-resources` | `LibraryResourceServlet` |

Danh sách hỗ trợ `page` và `limit`; giá trị phải là số nguyên dương trong giới
hạn service.

## Auth và HttpSession

| Method | Path | Body/Kết quả |
| --- | --- | --- |
| POST | `/api/auth/register` | `fullName`, `email`, `password`; tạo user và `HttpSession` |
| POST | `/api/auth/login` | `email`, `password`; tạo `HttpSession` |
| GET | `/api/auth/me` | trả user hiện tại và `csrfToken` |
| POST | `/api/auth/logout` | hủy session, trả 204 |
| PATCH | `/api/auth/profile` | `{ "fullName": "..." }`; sửa tên của chính mình |
| PUT | `/api/auth/password` | `{ "currentPassword": "...", "newPassword": "..." }`; đổi mật khẩu, hủy phiên hiện tại |

`POST /api/auth/logout`, `PATCH /api/auth/profile` và `PUT /api/auth/password`
đều cần `X-CSRF-Token`. Đổi mật khẩu hoặc quyền sẽ tăng `users.session_version`;
mọi phiên cũ bị từ chối khi truy cập lần kế tiếp. `GET /api/auth/me` trả
`createdAt` nhưng không trả password hash hay session version.

Quản lý quyền chỉ dành cho ADMIN:

| Method | Path | Body/Kết quả |
| --- | --- | --- |
| GET | `/api/admin/users?page=1` | danh sách 20 tài khoản/trang, không có hash |
| PATCH | `/api/admin/users/{id}/role` | `{ "role": "USER" }` hoặc `ADMIN`; không được tự đổi quyền, cần CSRF |

Tomcat quản lý cookie `JSESSIONID`. Không lưu mật khẩu hoặc CSRF token trong
database response. Password hash dùng PBKDF2 trong `PasswordUtil`.

## Phiên lắp ráp

Tất cả endpoint sau yêu cầu đăng nhập. Request thay đổi dữ liệu yêu cầu header
`X-CSRF-Token` lấy từ `/api/auth/me`.

| Method | Path | Body |
| --- | --- | --- |
| POST | `/api/assembly-sessions` | `{ "robotId": "line-follower" }` |
| GET | `/api/assembly-sessions` | danh sách của user hiện tại |
| GET | `/api/assembly-sessions/{id}` | chi tiết đúng chủ sở hữu |
| PATCH | `/api/assembly-sessions/{id}` | `{ "status": "IN_PROGRESS" }` |
| PUT | `/api/assembly-sessions/{id}/components/{componentId}` | `{ "isPrepared": true }` |
| PUT | `/api/assembly-sessions/{id}/steps/{stepId}` | `{ "status": "COMPLETED" }` |
| PUT | `/api/assembly-sessions/{id}/visual-parts/{componentId}` | `{ "isAssembled": true }` |
| DELETE | `/api/assembly-sessions/{id}/progress` | Xóa tiến độ chuẩn bị/bước/part, trả phiên về `PREPARING` |

Trạng thái chính: `PREPARING → READY → IN_PROGRESS → COMPLETED`; có thể chuyển
sang `ABANDONED` trước khi hoàn tất. `AssemblySessionServlet` kiểm tra robot,
component, step và quyền sở hữu; luật chuyển trạng thái nằm trong JavaBean
`AssemblySession`; sau đó `AssemblySessionDB` mới ghi dữ liệu.
Reset tiến độ chỉ áp dụng cho phiên `PREPARING`, `READY` hoặc `IN_PROGRESS`; thao
tác cần đăng nhập và CSRF, khóa đúng phiên theo `user_id`, xóa ba bảng tiến độ
trong transaction rồi đặt lại trạng thái. Phiên `COMPLETED`/`ABANDONED` không thể
đặt lại.

Phân biệt UI và API khi trình bày: trang `pages/lap-rap.html` hiện hiển thị quy
trình dưới dạng danh sách hướng dẫn đọc theo thứ tự; phần checkbox và tiến độ
đồng bộ là cho **chuẩn bị linh kiện**. Phòng `pages/lap-rap-3d.html` chỉ hiển thị
danh sách linh kiện/part 3D, không có panel quy trình. Endpoint `/steps` vẫn được
Servlet hỗ trợ, nhưng giao diện hiện tại chưa gọi `setStepStatus()` để lưu trạng
thái từng bước; không nên nói rằng tick từng bước đang được đồng bộ.

## CRUD quản trị

Các route yêu cầu user role `ADMIN` và CSRF token đối với thao tác ghi.

| Resource | Base path | Methods |
| --- | --- | --- |
| Robot | `/api/admin/robots` | GET, POST, PATCH `/{id}`, DELETE `/{id}` |
| Linh kiện | `/api/admin/components` | GET, POST, PATCH `/{id}`, DELETE `/{id}` |
| Bước lắp ráp | `/api/admin/steps` | GET, POST, PATCH `/{id}`, DELETE `/{id}` |
| Thư viện | `/api/admin/library-resources` | GET, POST, PATCH `/{id}`, DELETE `/{id}` |

## Vị trí triển khai

- HTTP/controller: `tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller`
- JavaBean và luật nghiệp vụ: `tomcat-app/src/main/java/vn/edu/webpro/robotlab/business`
- JDBC/SQL: `tomcat-app/src/main/java/vn/edu/webpro/robotlab/data`
- JSON, session, mật khẩu, kiểm tra dữ liệu: `tomcat-app/src/main/java/vn/edu/webpro/robotlab/util`
- Client gọi API: `assets/js/api.js`, `assets/js/content-api.js`
