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
| GET | `/api/troubleshooting-guides?robotId=&componentGroup=&search=` | `TroubleshootingServlet` |

Danh sách hỗ trợ `page` và `limit`; giá trị phải là số nguyên dương trong giới
hạn service.

Hai mẫu `line-obstacle` và `servo-scout` dùng nguyên các endpoint danh mục này,
không thêm API: `GET /api/robots` trả danh sách; `GET
/api/robots/{robotId}/components` đọc quan hệ linh kiện; `GET
/api/robots/{robotId}/steps` trả năm bước theo thứ tự. Dữ liệu được seed vào
`robots`, `robot_components` và `assembly_steps`; `wiring` là JSON trong bản ghi
robot. Trang `pages/lap-rap.html` hiển thị hướng dẫn bước ở ngoài phòng 3D.
Quiz và tra cứu lỗi cũng đi qua endpoint hiện hữu, với seed bổ sung riêng.

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

Phiên đã `COMPLETED` không thể quay lại `IN_PROGRESS` hay đặt lại tiến độ.
`completed_at` chỉ được ghi bởi `AssemblySessionDB.completeSession()`, sau khi
đối chiếu (trong cùng một câu UPDATE) rằng mọi `component_id` bắt buộc của robot
đều có mặt trong `session_visual_parts` — so theo tập hợp mã linh kiện, không
so số lượng vật thể; client không tự gửi phần trăm hay cờ hoàn thành.

## Bài kiểm tra kiến thức

Tất cả endpoint sau yêu cầu đăng nhập; `POST` cần `X-CSRF-Token`.

| Method | Path | Body/Ghi chú |
| --- | --- | --- |
| GET | `/api/quiz/questions?robotId=X` | Đề bài — **không có** đáp án đúng |
| POST | `/api/quiz/attempts` | `{ "robotId": "...", "answers": [{"questionId":"...","optionId":"..."}] }` |
| GET | `/api/quiz/attempts?robotId=X&page=1` | Lịch sử làm bài của chính mình |
| GET | `/api/quiz/attempts/{id}` | Xem lại một lượt đã làm (kèm giải thích) |

`QuizServlet` chỉ nhận mã câu hỏi + mã lựa chọn từ client; điểm số và đúng/sai
luôn do `QuizAttemptDB.submitAttempt()` tính từ `quiz_options.is_correct` đọc
tươi từ database ngay trong lúc chấm. Request nộp bài bị từ chối (422) nếu
thiếu câu trả lời cho một câu hỏi, `questionId` xuất hiện quá một lần, hoặc
`optionId` không thuộc đúng câu hỏi của nó. Mỗi lần nộp tạo một dòng
`quiz_attempts` mới (không có khái niệm "sửa lại điểm cũ"); lịch sử được đọc
từ **bản chụp** trong `quiz_attempt_answers`
(`question_prompt_snapshot`/`selected_option_label_snapshot`/…), không JOIN
lại `quiz_questions`/`quiz_options`, nên admin sửa câu hỏi sau đó không làm
đổi kết quả cũ. Người dùng chỉ xem được lượt làm bài của chính mình
(`user_id` trong mọi WHERE).

Tra cứu lỗi lắp ráp là nội dung công khai (không cần đăng nhập), lọc theo
`robotId` (khớp cả tình huống dùng chung có `robot_id NULL`), `componentGroup`
và `search` (tìm trong `symptom`). Đây là "hướng dẫn kiểm tra" do đội ngũ biên
soạn — trang không đọc tín hiệu từ robot thật.

## CRUD quản trị

Các route yêu cầu user role `ADMIN` và CSRF token đối với thao tác ghi.

| Resource | Base path | Methods |
| --- | --- | --- |
| Robot | `/api/admin/robots` | GET, POST, PATCH `/{id}`, DELETE `/{id}` |
| Linh kiện | `/api/admin/components` | GET, POST, PATCH `/{id}`, DELETE `/{id}` |
| Bước lắp ráp | `/api/admin/steps` | GET, POST, PATCH `/{id}`, DELETE `/{id}` |
| Thư viện | `/api/admin/library-resources` | GET, POST, PATCH `/{id}`, DELETE `/{id}` |
| Câu hỏi kiểm tra | `/api/admin/quiz/questions?robotId=X` hoặc `/{id}` | GET (danh sách theo robot hoặc 1 câu), POST, PATCH `/{id}`, DELETE `/{id}` |
| Tra cứu lỗi | `/api/admin/troubleshooting-guides` | GET, POST, PATCH `/{id}`, DELETE `/{id}` |
| Cửa hàng mô phỏng | `/api/admin/shop/products` | GET, POST, PATCH `/{id}`, DELETE `/{id}` (ngừng bán) |

Một request thêm/sửa câu hỏi kiểm tra gửi kèm toàn bộ mảng `options` (2–6 lựa
chọn, đúng một `isCorrect: true`) trong cùng body; server xóa hết lựa chọn cũ
rồi chèn lại theo danh sách mới trong một transaction. Xóa một câu hỏi đã có
người làm bài bị chặn bởi khóa ngoại `ON DELETE RESTRICT` (409 `RELATION_CONFLICT`).

## Trang Servlet → JSP (không phải JSON API)

Một số màn hình forward thẳng sang JSP thay vì trả JSON, theo đúng khuôn
`ComponentCatalogPageServlet`/`AccountPageServlet` đã có từ trước:

| URL | Servlet | Yêu cầu đăng nhập |
| --- | --- | --- |
| `/assembly-receipt?session={id}` | `AssemblyReceiptPageServlet` | Có — chỉ chủ phiên xem được |
| `/learning-summary` | `LearningSummaryServlet` | Có — chỉ số liệu của chính mình |
| `/learning-profile` | `LearningProfileServlet` | Có — chỉ dữ liệu của chính mình; `Cache-Control: no-store` |
| `/admin-stats` | `AdminStatsServlet` | Có, và phải role `ADMIN` (403 nếu không) |
| `/order-history` | `OrderHistoryServlet` | Có — chỉ lịch sử của user hiện tại |

`GET /learning-profile` lấy user qua `HttpSession`/`SessionUtil`; không nhận
`userId`, email hay chủ sở hữu từ request. Khách được chuyển về
`/pages/tai-khoan.html`, lỗi database trả `503`; response đặt
`Cache-Control: no-store`. Servlet đọc catalog bằng `RobotDB` và dữ kiện cá nhân
bằng các truy vấn `StatsDB` riêng theo `user_id`, dựng `LearningProfile`, rồi
forward sang `WEB-INF/views/learning-profile.jsp`. Đây là trang chỉ đọc, không
ghi dữ liệu và không thêm bảng.

Tình trạng chung ưu tiên: có mẫu hoàn thành → "Đã hoàn thành n mẫu"; nếu chưa
có mẫu hoàn thành nhưng có phiên mở → "Đang thực hiện"; nếu chỉ còn phiên đã
dừng → "Đã dừng"; nếu chỉ có lượt quiz → "Chưa hoàn thành mẫu nào"; không có
phiên/lượt quiz → "Chưa có dữ liệu". Đây là luật trong JavaBean, không suy ra
đã hoàn thành lắp ráp từ điểm quiz.

## Cửa hàng và đơn hàng mô phỏng (đợt 4)

`GET /api/shop/products?page=1&limit=20` là catalog công khai, chỉ trả sản phẩm
đang hoạt động. Giá/tồn kho thuộc `shop_products`; tên, ảnh và thông số kỹ thuật
được JOIN từ `components`, tránh nhân bản dữ liệu học tập.

| Method | Path | Body / quyền |
| --- | --- | --- |
| GET | `/api/components?page=1&limit=100` | Công khai; linh kiện kỹ thuật cho select admin |
| GET | `/api/cart` | Đăng nhập; chỉ giỏ của `HttpSession` hiện tại |
| PUT | `/api/cart/items/{productId}` | Đăng nhập + CSRF; `{ "quantity": 2 }`; server kiểm tra trạng thái/tồn |
| DELETE | `/api/cart/items/{productId}` | Đăng nhập + CSRF; chỉ xóa dòng thuộc user hiện tại |
| POST | `/api/orders` | Đăng nhập + CSRF; body rỗng, server tự lấy giỏ và tính tổng |
| GET | `/api/orders?page=1&limit=20` | Đăng nhập; lịch sử của user hiện tại |
| GET | `/api/admin/shop/products?page=1&limit=100` | Chỉ ADMIN; gồm cả sản phẩm đã ngừng bán |
| POST | `/api/admin/shop/products` | ADMIN + CSRF; `{ "id", "componentId", "priceVnd", "stockQuantity", "active" }` |
| PATCH | `/api/admin/shop/products/{id}` | ADMIN + CSRF; cập nhật linh kiện, giá, tồn, trạng thái |
| DELETE | `/api/admin/shop/products/{id}` | ADMIN + CSRF; đặt `is_active=false`, không xóa vật lý |

Các API cart/order lấy `user_id` từ `SessionUtil`, không nhận chủ tài khoản từ
request. `OrderDB.checkout()` dùng transaction và `SELECT ... FOR UPDATE`, kiểm
tra active/tồn kho, lấy giá mới nhất từ DB, giảm kho, chụp tên/đơn giá/số lượng
vào `order_items`, ghi tổng rồi xóa giỏ. Bất kỳ lỗi nào rollback toàn bộ. Client
không gửi `price`, `total`, user ID hay số tồn. API chỉ tạo đơn mô phỏng; không
có tích hợp thanh toán hoặc giao hàng.

## Vị trí triển khai

- HTTP/controller: `tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller`
- JavaBean và luật nghiệp vụ: `tomcat-app/src/main/java/vn/edu/webpro/robotlab/business`
- JDBC/SQL: `tomcat-app/src/main/java/vn/edu/webpro/robotlab/data`
- JSON, session, mật khẩu, kiểm tra dữ liệu: `tomcat-app/src/main/java/vn/edu/webpro/robotlab/util`
- Client gọi API: `assets/js/api.js`, `assets/js/content-api.js`
