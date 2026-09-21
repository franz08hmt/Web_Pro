# Quy ước Servlet API

API chạy trong cùng WAR và cùng origin với frontend, dưới `/api`. Servlet nhận
HTTP; service xử lý nghiệp vụ; DAO dùng JDBC truy cập MySQL.

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

`ApiResponses` đặt JSON UTF-8, status code và `Cache-Control: no-store`.

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

Tomcat quản lý cookie `JSESSIONID`. Không lưu mật khẩu hoặc CSRF token trong
database response. Password hash dùng PBKDF2 trong `PasswordService`.

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

Trạng thái chính: `PREPARING → READY → IN_PROGRESS → COMPLETED`; có thể chuyển
sang `ABANDONED` trước khi hoàn tất. `AssemblySessionService` kiểm tra robot,
component, step, quyền sở hữu và chuyển trạng thái trước khi DAO ghi dữ liệu.

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
- Nghiệp vụ: `tomcat-app/src/main/java/vn/edu/webpro/robotlab/service`
- JDBC/SQL: `tomcat-app/src/main/java/vn/edu/webpro/robotlab/dao`
- JSON/error: `tomcat-app/src/main/java/vn/edu/webpro/robotlab/web`
- Client gọi API: `assets/js/api.js`, `assets/js/content-api.js`
