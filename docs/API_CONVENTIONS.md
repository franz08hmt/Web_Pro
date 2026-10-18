# Quy ước Servlet API

## Luồng phụ tích hợp ngày 12/10/2026

- `GET /diagnosis?action=history&page=1`: trang JSP USER, chỉ đọc, lấy owner
  từ HttpSession và giữ `Cache-Control: no-store`. Chỉ `mode=PRACTICE` và
  `state=SUBMITTED`; sắp `submitted_at DESC, id DESC`, 10 lượt/trang. Trang
  thiếu dùng 1; sai định dạng, âm, 0 hoặc >100000 trả 422; trang hợp lệ vượt
  dữ liệu được bean giới hạn về trang cuối. Khách redirect đăng nhập, ADMIN
  403; tham số userId/user/email không đổi được chủ sở hữu.
- `GET /diagnosis?action=result&id=...` vẫn dùng kiểm owner hiện có. Lịch sử
  dùng cùng `DiagnosisAttempt.getScoreExact()` và định dạng giờ Việt Nam của
  trang kết quả; không chấm lại quan sát hoặc sửa điểm trong DB/JSP.
- Thư viện dùng API hiện có `/api/library-resources?page=&limit=&robotId=`;
  không thay payload server. Client cũ `loadLibraryResources()` giữ mảng và
  cache; client mới `loadLibraryResourcesPage(options)` trả `{data, meta}`.
- Không thêm POST/migration/schema hoặc thay luật TASK/rubric A/B.

## Chẩn đoán tương tác và mẫu B (Đợt 6, chặng 2)

Các route sau là trang Servlet → JSP/form, không phải JSON API. Mọi trang có
`Cache-Control: no-store`; khách redirect `/pages/tai-khoan.html`. Danh tính lấy
từ HttpSession. Mọi POST kiểm hidden `csrfToken` bằng helper form hiện có.

| Route | Quyền | GET (chỉ đọc) | POST action |
| --- | --- | --- | --- |
| `/diagnosis` | USER | Mặc định: PUBLISHED và lượt đang làm của mình; `play/result&id=<attempt>` | `start&scenarioId`, `check&id&checkId`, `conclude&id&causeOptionId&actionOptionId` |
| `/admin-diagnosis` | ADMIN | Danh sách, `new`, `edit/view/preview&id=<scenario>` | `save`, `publish`, `duplicate`, `archive` |
| `/tasks` | USER | Luồng nhiệm vụ cũ, thêm bằng chứng chẩn đoán cho B | Thêm `diagnosis&id=<task>` để tạo/tiếp tục lượt TASK của vòng |
| `/admin-tasks` | ADMIN | Nháp chọn rubric A/B và tình huống PUBLISHED cùng robot | Công bố kiểm lại rubric/tình huống; khóa sau công bố |
| `/admin-task-reviews` | ADMIN | Thêm quá trình chẩn đoán của bài nộp B | Chấm/sửa chấm theo luật cũ |

422: action/id/field sai hoặc vi phạm luật; 404: không tồn tại/không thuộc quyền
xem; 403: thiếu vai trò hoặc token; 503: SQLException. Thiếu/sai token bị chặn
trước mọi ghi dữ liệu. ADMIN không làm lượt hoặc nộp; USER không xem lượt của
người khác. Không có tham số userId để đổi chủ lượt.

`check` idempotent, chỉ cho phép check của đúng tình huống. Quan sát chưa chọn,
cờ cần thiết/đáp án, phản hồi và giải thích bị loại khỏi dữ liệu trả cho trang
làm bài. `conclude` khóa riêng lượt của mình; kết luận lần hai nhận 422.
`start` chỉ nhận PUBLISHED; lượt TASK của nhiệm vụ đang mở vẫn tiếp tục được
khi tình huống đã ARCHIVED. Bản PUBLISHED bất biến, sửa phải nhân bản.

POST nhiệm vụ `diagnosis` khóa task → recipient → round và trả về cùng lượt
nếu đã gắn. Confirm chỉ đọc lượt chẩn đoán, kiểm mode TASK/user/tình huống/
SUBMITTED và tự chọn ID của vòng hoặc bài trước. Hidden ID không được thay
bằng lượt luyện hay lượt khác. Quiz API và header CSRF cũ giữ nguyên.

| Servlet | JavaBean / XxxDB | JSP mới |
| --- | --- | --- |
| DiagnosisServlet | DiagnosisAttempt/Scenario/Check/Option, DiagnosisDB | diagnosis-list/play/result |
| AdminDiagnosisServlet | DiagnosisScenario, DiagnosisDB | admin-diagnosis-list/form/preview |

## Nhiệm vụ thực hành và đánh giá (Đợt 6, chặng 1)

| Route | GET | POST action | Quyền |
| --- | --- | --- | --- |
| `/tasks` | Danh sách; `action=view/quiz/submit&id=` | `quiz`, `preview`, `confirm` | USER được giao nhiệm vụ; dữ liệu riêng của mình |
| `/admin-tasks` | Danh sách lọc state; `new/edit/view/preview` | `saveDraft/publish/addRecipients/extend/close/archive/duplicate/deleteDraft` | ADMIN |
| `/admin-task-reviews?id=` | Bằng chứng, lịch sử nộp/chấm, form chấm | `review` | ADMIN |

Tất cả trang đặt `Cache-Control: no-store`; khách redirect tới
`/pages/tai-khoan.html`, USER vào trang ADMIN trả 403, ADMIN vào luồng nộp USER
trả 403. USER mở nhiệm vụ không được giao (hoặc nháp) trả 404. Danh tính lấy từ
SessionUtil/HttpSession, không nhận userId trong luồng USER. SQLException trả
503; điều kiện nghiệp vụ sai trả trang JSP 422 với thông báo, không tạo dữ liệu.

Đợt 6b thống nhất mã lỗi cho ba route nhiệm vụ (không đổi API JSON cũ):

| Trường hợp | HTTP / nội dung |
| --- | --- |
| ID sai định dạng, action không hỗ trợ, thiếu/sai field hoặc vi phạm luật | 422, forward `task-error.jsp` |
| Không tồn tại hoặc không thuộc quyền xem | 404 |
| Thiếu quyền vai trò | 403 |
| Thiếu/sai hidden `csrfToken` | 403 |
| SQLException / database không sẵn sàng | 503 |

Kiểm đăng nhập/vai trò và token vẫn chạy trước xử lý form. GET trang chấm dùng
action rỗng (URL đang có) hoặc `view`; action lạ trả 422. Các ID và mã lỗi khác
giữ nguyên. Khối quiz của vòng hiện tại chỉ hiện khi
`TaskRecipient.isShowCurrentRoundQuiz()` trả true, tức vòng ACTIVE; vòng
SUBMITTED (kể cả đang chờ chấm), CANCELLED hoặc thiếu vòng không hiện khối này.
Lịch sử bài nộp vẫn giữ nguồn quiz và nhãn dùng lại theo bản chụp.

Mọi POST dùng form URL encoded và hidden `csrfToken` so với HttpSession bằng
`SessionUtil.hasValidFormCsrfToken`; thiếu/sai trả 403. Token trong header
`X-CSRF-Token` của API cũ không đổi. Thành công dùng PRG; riêng preview forward
200 và không lưu bài. Hạn nhập datetime-local được parse strict bằng
SimpleDateFormat theo Asia/Ho_Chi_Minh, hiển thị dd/MM/yyyy HH:mm.

`confirm` nhận id nhiệm vụ, roundId, sessionId và ba đoạn problem/reasoning/
improvement. Server đọc lại bằng chứng theo user_id, khóa task/recipient/round,
kiểm hạn, trạng thái, số lượt, quiz của vòng và mốc phiên lắp ráp; không nhận điểm
từ hidden field. Lưu snapshot và cập nhật vòng trong cùng transaction. Quiz
nhiệm vụ gọi overload bốn tham số của QuizAttemptDB; overload ba tham số và
`/api/quiz` giữ hợp đồng cũ, dùng chung luật chấm.

Điểm tự động lưu DECIMAL(4,1) = HALF_UP(40 + score/total × 40, 1). Tổng dùng
chính điểm tự động đã lưu cộng điểm giải thích. Chưa chấm hiển thị /80; đã chấm
hiển thị /100. Lần chấm mới dùng expectedReviewId chống lưu từ form cũ, tạo bản
ghi bất biến với supersedes_review_id/change_reason. Sửa bị chặn nếu vòng kế
tiếp đã có quiz hoặc bài nộp; đổi kết luận có thể huỷ/kích hoạt lại vòng chưa dùng.


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
| `/tasks` | `TaskServlet` | USER được giao; no-store; POST kiểm CSRF form |
| `/admin-tasks` | `AdminTaskServlet` | ADMIN; no-store; POST kiểm CSRF form |
| `/admin-task-reviews` | `AdminTaskReviewServlet` | ADMIN; no-store; POST kiểm CSRF form |
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

Đợt 5b không đổi route/response hay câu SQL. Thời điểm trong bean dùng
`java.util.Date` (dữ kiện JDBC từ `Timestamp.getTime()`), được định dạng bằng
`SimpleDateFormat` tạo riêng mỗi lần theo `Asia/Ho_Chi_Minh`: `dd/MM/yyyy`,
hoặc `dd/MM/yyyy HH:mm` cho lúc lập hồ sơ. Thiếu Date hiển thị "Chưa có dữ
liệu". Servlet dùng `String url` + `getRequestDispatcher(url).forward(...)`;
JSP chỉ trình bày các getter `completed`, `inProgress`, `stopped` của bean.

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

## Phòng nối dây — form Servlet/JSP (Đợt 7, chặng 1)

| Route | Quyền | GET | POST (PRG khi thành công) |
| --- | --- | --- | --- |
| `/wiring` | USER | Catalog/lịch sử; action=play/result&id= của chính mình | start, retry, add, remove, save, submit |
| `/admin-wiring` | ADMIN | Danh sách; new/edit/view/preview&id= | saveDraft, publish, duplicate, archive |

Identity từ HttpSession; tất cả POST hidden `csrfToken` kiểm bằng
SessionUtil.hasValidFormCsrfToken. Giữ nguyên token header của API cũ.
GET không ghi; Cache-Control:no-store. Khách redirect `/pages/tai-khoan.html`;
sai vai trò/token 403, không tồn tại/khác owner 404, field/id/action/luật 422,
SQL lỗi 503. Form POST có action URL riêng để query action của GET không lấn
action trong body. Không nhận score/owner/state từ client.

Nháp gửi expectedVersion; phiên bản cũ bị từ chối 422, không ghi một phần.
pair=A:B lặp/đảo chiều được chuẩn hóa một lần. add dùng terminalA/B, remove
dùng removePair; tối đa 80 dây thô. Bài công bố bất biến, lưu trữ chỉ chặn lượt
mới. Submit lặp dẫn tới kết quả cũ có thông báo payload mới không được lưu.

## Đợt 7 — hỗ trợ nối dây (chặng 2)

| Route | Quyền | GET | POST form |
| --- | --- | --- | --- |
| `/wiring-support` | USER | Danh sách; `action=new&attemptId=…`; `action=view&id=…` | `create`, `message`, `close` |
| `/admin-wiring-support` | ADMIN | Danh sách; `action=view&id=…` | `message`, `close` |

Mọi trang `Cache-Control: no-store`; khách redirect `/pages/tai-khoan.html`. Vai trò
không phù hợp hoặc token form thiếu/sai: 403. Tài nguyên không tồn tại/khác owner: 404.
ID/action/field/luật sai, version cũ, yêu cầu đã đóng: 422; lỗi JDBC: 503.
POST URL encoded gửi hidden `action`, `csrfToken`, `expectedVersion`; thành công PRG.
Không thay cơ chế header CSRF của API cũ. GET không tạo tin hay đổi trạng thái.

`create` nhận `attemptId`, version lượt đã lưu, `content`, `terminalId` tùy chọn.
`message` nhận id yêu cầu, version yêu cầu, content, terminalId tùy chọn; USER có
checkbox `sendSnapshot=on`. Bản chụp luôn từ lượt gốc do server đọc sau khóa, không
nhận JSON/dây/điểm của client. ADMIN không gửi bản chụp nguồn mới.
`close` đóng theo version yêu cầu. Nội dung USER 20–2000, ADMIN 10–2000 ký tự sau trim.
OPEN → Admin phản hồi ANSWERED; USER gửi tiếp → OPEN; USER giải quyết/Admin đóng → CLOSED.
CLOSED không nhận thêm tin, có thể tạo yêu cầu mới. Một lượt chỉ có một OPEN/ANSWERED.

## Lắp ráp từng vật thể — Đợt 8

Giữ `/api/assembly-sessions/*`, danh tính từ HttpSession và no-store.
Mutation dùng JSON, header X-CSRF-Token; không đổi CSRF của form JSP/API cũ.

| Method/path dưới `/api/assembly-sessions/{id}` | Nội dung |
| --- | --- |
| GET `/layout` | mode, spec/slots/hướng, instances, placements, layoutVersion, status, canEdit, report |
| POST `/placements` | instanceId, slotId, orientation, expectedLayoutVersion |
| DELETE `/placements/{instanceId}?expectedLayoutVersion=…` | Tháo vật thể, từ chối khi còn con |
| POST `/check` | Báo cáo X/N, khay, vị trí thiếu/lỗi; không ghi dữ liệu |
| PATCH phiên | status=COMPLETED và expectedLayoutVersion; server kiểm lại trong khóa phiên |
| DELETE `/progress?expectedLayoutVersion=…` | Reset DETAILED trước hoàn tất; giữ instances và kit chuẩn bị |

Phiên cũ GROUP tiếp tục route hoàn tất/reset/visual-parts cũ. DETAILED từ chối
PUT visual-parts; không dùng component_id hoặc phần trăm client để hoàn tất.
Robot có spec PUBLISHED dùng DETAILED khi tạo mới; mode/spec cố định sau đó.
Spec archived không dùng để tạo phiên mới; phiên đã tham chiếu vẫn đọc/lắp tiếp.

17/10/2026: POST `/api/assembly-sessions` nhận thêm boolean tùy chọn
`newDetailedSession`. Bỏ qua hoặc false: giữ nguyên hành vi create-or-resume cũ
(có thể trả GROUP). True: mở/tiếp tục DETAILED riêng của user trong HttpSession,
không đổi bất kỳ phiên GROUP nào. Khóa dòng users trong transaction để hai yêu
cầu true đồng thời dùng một phiên DETAILED đang mở; không lấy user_id từ client.
Nếu chưa có spec PUBLISHED và không có DETAILED đang mở thì422 VALIDATION_ERROR,
không tạo phiên GROUP để giả thành chi tiết. Trả201 cùng cấu trúc session hiện có;
ID có thể là phiên được tiếp tục, không được suy số bản ghi mới từ HTTP201.
Vẫn bắt buộc USER + header CSRF, no-store; không đổi endpoint/mã lỗi cũ.

Seed `seed-assembly-spec-all-models.sql` thêm v1 cho4 mẫu còn lại:11/8/12/12 slot,
không tự chuyển đổi phiên cũ và không ảnh hưởng spec v1/v2 obstacle-avoider.

200 accepted:true có data layout và unchanged/transitioned. Retry đúng vị trí/hướng
trả unchanged:true, không tăng version; hai complete chỉ một transitioned:true.
422 accepted:false có code/message/instanceId/slotId và error theo API cũ.
Mã: WRONG_COMPONENT, SLOT_OCCUPIED, ORIENTATION_NOT_ALLOWED, PARENT_MISSING,
ALREADY_PLACED, HAS_DEPENDENTS, SESSION_LOCKED, ASSEMBLY_INCOMPLETE, VALIDATION_ERROR.
409 LAYOUT_CONFLICT yêu cầu tải lại; 404 NOT_FOUND cho phiên/vật thể/slot khác quyền;
403 CSRF_REQUIRED, khách401 theo API; JDBC503 không lộ SQL/stack trace.
Không nhận owner, điểm, percent, isCorrect hay pose client làm căn cứ xác thực.

### Đợt 8b — hướng có gợi ý và lời giải thích

GET layout trả cho mỗi slot loại linh kiện và danh sách hướng chấp nhận.
Đây là luyện tập có gợi ý; radio/nút xoay chỉ gửi ý định, JavaBean vẫn quyết định.
Spec v2 chỉ áp dụng phiên mới; phiên chi tiết cũ giữ spec_id v1. Không có migration mới.
Y90/Y270 là quy ước hướng lắp đối xứng trái/phải của mô hình 3D giản lược;
hình khối động cơ không mô phỏng chính xác chiều trục. Giữ tư thế hình ảnh v1,
không sửa dữ liệu v2 đã công bố và không tạo v3 trong đợt này.
JSON phiên thêm placedSlotCount/requiredSlotCount; progressPercent cũ giữ nguyên.
Client DETAILED dùng X/N điểm gắn, GROUP dùng phần trăm chuẩn bị linh kiện.

| Mã từ chối | Câu cho người học (tên lấy trong layout của transaction) |
| --- | --- |
| WRONG_COMPONENT | Không thể gắn {vật thể} vào {điểm gắn}: điểm này dành cho loại bộ phận khác. |
| SLOT_OCCUPIED | {điểm gắn} đã có {vật thể đang gắn}. Hãy tháo vật thể đó trước. |
| ORIENTATION_NOT_ALLOWED | {vật thể} chưa đúng hướng tại {điểm gắn}. Hãy xoay rồi thử lại. |
| PARENT_MISSING | Cần gắn {điểm gắn cha} trước khi gắn {vật thể}. |
| ALREADY_PLACED | {vật thể} đang ở {điểm gắn cũ}. Hãy tháo trước khi chuyển. |
| HAS_DEPENDENTS | Cần tháo {vật thể con} trước khi tháo {vật thể}. |

NOT_FOUND/404, LAYOUT_CONFLICT/409, SESSION_LOCKED/422 và GROUP_MODE/422 giữ câu chung,
không nêu tên. Thiếu nhãn dùng câu dự phòng. JSON quote, giao diện textContent, không HTML.
Phiên đã khóa trả SESSION_LOCKED khi sửa/đổi hướng/tháo/reset, kể cả field sai định dạng;
phiên còn sửa được trả VALIDATION_ERROR cho hướng ngoài Y0/Y90/Y180/Y270 hoặc thiếu field.
Request lặp đúng hệt placement đã lưu vẫn trả unchanged, không sửa dữ liệu đã hoàn tất.

## Đợt 9 — xem lại, nhật ký và hoàn tác

| Route | Request | Kết quả |
| --- | --- | --- |
| GET `/assembly-review?session=…` | HttpSession, id phiên | Servlet setAttribute/forward JSP; chỉ chủ phiên; không ghi |
| GET `/api/assembly-sessions/{id}/events` | HttpSession | `{data:[…]}` ACCEPTED theo id, gồm action/nhãn/vật thể/slot/hướng/hướng trước/undoOfEventId/undone/thời điểm |
| POST `/api/assembly-sessions/{id}/undo` | JSON `{expectedLayoutVersion}`, header X-CSRF-Token | Layout đã lưu, version mới; không có Redo |

Trang tổng kết và các API có `Cache-Control: no-store`. Khách vào trang tổng kết
redirect tài khoản; API giữ401. Khác chủ/không tồn tại404; CSRF403; JDBC503 không
lộ SQL. Phiên chưa hoàn tất vẫn200 nhưng chỉ có thông báo và link tiếp tục.
GET tổng kết đọc quiz theo owner và robot, không tạo lượt quiz hoặc tính điểm mới.

Nhật ký ghi ACCEPTED cùng transaction placement/version. Từ chối do luật bean
ghi REJECTED (tối đa300/phiên), commit nhật ký nhưng không đổi placement/version.
Không ghi retry unchanged, lỗi quyền/NOT_FOUND hoặc thao tác trên phiên COMPLETED.
JSON events chỉ trả ACCEPTED; REJECTED được tổng hợp thành nhãn lỗi trên JSP.
UNDO giữ lịch sử gốc, dùng UNIQUE undo_of_event_id và FK cùng phiên.

Undo khóa phiên FOR UPDATE, kiểm version, lấy ATTACH/DETACH gần nhất chưa undo
sau RESET cuối rồi gọi lại luật gắn/tháo. Hướng cũ lưu trong prev_orientation_code.
Không còn thao tác:422 `NOTHING_TO_UNDO`, câu “Không còn thao tác để hoàn tác sau
lần đặt lại gần nhất.” GROUP422 `GROUP_MODE`, đã khóa422 `SESSION_LOCKED`,
version cũ409 `LAYOUT_CONFLICT`; các mã/HTTP cũ khác giữ nguyên.

`pages/lap-rap-3d.html?model=…&session=…&replay=1` chỉ xem phiên DETAILED COMPLETED.
Client gọi GET layout/phiên/events, dựng từ trống rồi áp ATTACH/DETACH/RESET/UNDO.
Cả thao tác gốc đã undo vẫn được phát lại trước sự kiện đảo; không gọi API ghi.
Không có nhật ký thì báo thiếu dữ liệu, không bịa lịch sử. Phát/Tạm dừng/Bước
trước/Bước sau là tương tác trình bày; reduced-motion chỉ bấm từng bước.

## Đợt10 — Xưởng thiết kế

| Route | Input | Kết quả |
| --- | --- | --- |
| GET /my-kit | HttpSession | JSP13 linh kiện, lượng owner, hỗ trợ và link shop |
| POST /my-kit | action=save/import, csrfToken; componentId lặp, quantity_{id} | lỗi422 trên form, thành công302 PRG |
| GET /designs | action=new/rename/delete tùy form; id khi đổi/xóa | JSP danh sách/form owner |
| POST /designs | action=create/rename/delete, csrfToken; name/goalCode/source, componentId/quantity | create commit rồi302 tới phòng3D bằng ID vừa tạo; rename/delete về danh sách; validation422 giữ form |
| GET /design-sheet?id=… | HttpSession | JSP bản vẽ/BOM/trình tự/kết quả, không ghi |
| GET /api/designs/{id}/layout | HttpSession | data layout, luật phiên bản, instances/placements/version, check hoặc null |
| POST /api/designs/{id}/placements | instanceId, slotId dạng host:mount, orientation, expectedLayoutVersion | data layout đã lưu |
| DELETE /api/designs/{id}/placements/{instanceId} | query expectedLayoutVersion | data layout sau tháo |
| POST /api/designs/{id}/undo | expectedLayoutVersion | đảo sự kiện gần nhất, kiểm lại bằng bean |
| POST /api/designs/{id}/check | expectedLayoutVersion | kiểm ba lớp, lưu summary/ngày/version; không có thang điểm |

Mọi API ghi có X-CSRF-Token, owner từ session. Trang/API riêng no-store, noindex
cho JSP/HTML có dữ liệu riêng. Khách vào JSP302 tài khoản, API401; ID khác chủ404
NOT_FOUND; CSRF403; version cũ409 LAYOUT_CONFLICT; JDBC503 không SQL/stack trace.
Đổi tên/xóa dùng expectedLayoutVersion và checkbox confirmDelete=yes khi xóa.
GET không tạo/xóa/ghi kiểm. CHECKED không đồng nghĩa đạt mục tiêu.

422 do luật: NOT_SUPPORTED, MOUNT_INCOMPATIBLE, MOUNT_OCCUPIED,
ORIENTATION_NOT_ALLOWED, HOST_MISSING, ALREADY_PLACED, HAS_DEPENDENTS,
NOTHING_TO_UNDO; LOCKED là nhánh phòng vệ nếu trạng thái không sửa được.
VALIDATION_ERROR dành cho request sai định dạng. Message tiếng Việt nêu tên từ
roster/layout của đúng owner; NOT_FOUND/LOCKED/LAYOUT_CONFLICT dùng câu chung.
Retry gắn đúng cặp/hướng đang có trả unchanged:true, không tăng version hoặc event.
REJECTED do bean ghi truy vết cùng khóa, chỉ lỗi học tập được đếm trong hồ sơ.

Slots trả nhãn điểm gắn và các hướng gợi ý của loại đó, có tọa độ từ server; item
componentId có trong snapshot. Client chỉ chọn/gửi ý định, server kiểm fit cụ thể.
`check.items` có layer/label/message cho người học; không hiển thị enum. Kết quả
chỉ có khi bố cục đã CHECKED; mọi thao tác đổi bố cục xóa check cũ. Thiết kế lưu
rule_set_id, không lấy luật mới nhất để tính lại thiết kế cũ.

POST tạo thiết kế lấy ID do `DesignDB.create` trả về sau commit rồi chuyển thẳng
đến `/pages/xuong-thiet-ke.html?design=<id>`. Phòng3D chỉ tải layout đã lưu qua
GET owner-scoped; reload không tạo thiết kế mới. Validation thất bại rollback
transaction, trả 422 và render lại tên, mục tiêu, nguồn cùng số lượng đã nhập.

Luật Xưởng v2 sửa riêng góc nền rear-bay/front-top về0 do v1 bù góc hai lần;
battery/sensor của spec v2 có nền≈0, hướng Y90/Y180 được view cộng đúng một lần.
Không đổi route/schema/fit hoặc luật phòng mẫu. Tạo mới chọn PUBLISHED mới nhất;
GET cũ vẫn đọc FK v1. Danh sách/hồ sơ hiển thị v1/v2; v1 có câu cảnh báo tạo mới,
không tự chuyển version. SCANNING_AVOIDER giữ cảm biến SERVO_HORN/Y180 theo
obstacle-avoider; servo-scout hiện có target sensor không ry (0), không sửa mẫu đó.

## Đợt 11 — Điều hướng theo hành trình

Các hợp đồng API cũ, CSRF/version/mã lỗi vẫn giữ nguyên. Bổ sung chỉ đọc:

| Route | Quyền/dữ liệu | Kết quả |
| --- | --- | --- |
| GET /api/learning-links | Công khai; kit của user trong session nếu đăng nhập | data[]: componentId, owned (null với khách), productId, robots[{id,name,hasWiring,hasGuide}] |
| POST /design-cart | Form đăng nhập; csrfToken, designId; chỉ chủ thiết kế | 302 PRG tới /pages/gio-hang.html?designNotice=1; thiếu token403, khác chủ404, DB503 |
| GET /api/design-cart-notice | Đăng nhập; thông báo trong chính session | data[] thông báo tiếng Việt, no-store; không ghi DB |
| GET /design-cart | Không có thao tác ghi | 302 tới trang giỏ |

CartDB khóa user/design/kho bằng cơ chế giỏ hiện có. Lượng cần = BOM trừ kit;
lượng giỏ đích = max(giỏ hiện có, min(phần thiếu, kho, giới hạn giỏ)). Không giảm
mục đã có; nếu giỏ cũ đang vượt kho thì báo cần kiểm lại, không thêm vượt kho.
Không thêm sản phẩm ngừng bán/hết hàng; giải thích từng mục, không checkout.
Bấm lại không cộng trùng. Mọi tên/chuỗi JSON qua JsonUtil.quote, DOM dùng textContent.

/wiring?robotId= và /diagnosis?robotId= chỉ lọc danh sách đã được phép đọc; không
thay quyền, dữ liệu bài, đáp án hoặc request POST. /pages/tra-cuu-loi.html dùng
robotId/componentGroup/search là các bộ lọc sẵn có. Catalog có link đúng component
và sản phẩm; chỉ hiện đường nối dây khi DB có bài PUBLISHED của robot đó.

404/500 HTML có trang lỗi; lỗi đã xử lý của /api/* giữ ResponseUtil JSON.
ERROR dispatch404/500 của container dưới /api/* cũng trả JSON, không lộ stack/SQL.
Năm trang HTML quản trị giờ kiểm ở server; các API quản trị không đổi hợp đồng.
Trang tổng kết, lịch sử đơn, thống kê và catalog SSR có no-store qua WorkspaceCacheFilter;
không thay mã HTTP, query hoặc quyền của các trang này.

### Đợt 11b — Deep link sản phẩm (GET additive)

`GET /api/shop/products/{id}` trả `200 {data: product}` cho một SKU đang bán,
kể cả khi tồn kho bằng0 (nút thêm bị vô hiệu hóa). Không có/ngừng bán cùng trả
404 NOT_FOUND, thông báo không tiết lộ sản phẩm quản trị; DB lỗi trả503 JSON.
Response dùng no-store và JSON theo ResponseUtil. Không ghi giỏ hay tồn kho.
Query PreparedStatement theo khóa chính, tối đa một dòng.

GET danh sách `/api/shop/products?page=1&limit=20` giữ nguyên response/phân trang.
Deep link `pages/cua-hang.html#shop-product-ID` vẫn dùng được: đọc một SKU độc lập
với trang hiện tại, chống response cũ và không tạo ID trùng khi SKU nằm trên trang.
Lỗi xác thực không được diễn giải là khách; lỗi đọc giỏ không xóa danh tính đã
xác nhận, và không cho cập nhật số lượng dựa trên giỏ chưa tải được.
