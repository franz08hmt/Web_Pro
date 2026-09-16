# Hợp đồng API nội dung v1 (đề xuất để nhóm review)

Base `/api`. JSON thành công: `{data: object}`; danh sách `{data: [], meta:{page,limit,total}}`. List mặc định page=1, limit=20; tối đa limit=100; bước sắp theo stepOrder, các danh sách khác theo id. Tham số page/limit là số nguyên dương dạng chuỗi URL. ID slug chữ thường/số/dấu gạch ngang, dài tối đa 64; không đổi ID qua PATCH. Lỗi validation dùng 422 theo `docs/API_CONVENTIONS.md`; lỗi có `requestId` nếu middleware chung đã cấp. API nội dung dùng `limit`, API phiên của Tài dùng `pageSize`: không dùng lẫn hai tham số.

| Method | Path | Kết quả |
|---|---|---|
| GET | `/robots`, `/components`, `/steps`, `/library-resources` | Danh sách phân trang |
| GET | `/<loại>/:id` | Chi tiết một bản ghi |
| GET | `/steps?robotId=line-follower` | Lọc bước theo robot |
| GET | `/library-resources?robotId=line-follower` | Lọc thư viện theo robot |
| GET | `/robots/:id/components` | `{data:[{componentId,quantity}]}` |
| GET | `/robots/:id/steps` | Bước theo robot, phân trang |
| GET | `/admin/<loại>` hoặc `/admin/<loại>/:id` | Đọc dành cho admin |
| POST | `/admin/<loại>` | Tạo, 201 |
| PATCH | `/admin/<loại>/:id` | Sửa một phần, 200 |
| DELETE | `/admin/<loại>/:id` | Xóa, 204 không body |
| PUT | `/admin/robots/:id/components/:componentId` | Thêm/sửa quantity, 200 |
| DELETE | `/admin/robots/:id/components/:componentId` | Gỡ quan hệ, 204 |

`<loại>` là robots, components, steps hoặc library-resources. Chi tiết robot không nhúng parts/steps: frontend lấy thêm hai endpoint con. Quan hệ linh kiện không xóa cascade khi xóa robot; gỡ quan hệ và bước trước, nếu đang có tiến độ tham chiếu thì trả 409 để giữ dữ liệu học.

POST bắt buộc đầy đủ field liệt kê dưới đây; PATCH ít nhất một field, không nhận field lạ. Text không được rỗng. image/url chỉ nhận `/assets/...` không có `..` hoặc URL HTTPS không chứa thông tin đăng nhập. Không tải URL phía server.

| Loại | Field |
|---|---|
| robots | id, name (150), level (Cơ bản/Trung bình/Nâng cao), summary (5000), image (2048), buildTime (100), mainSensor (255), skills (2000), wiring (JSON array) |
| components | id, name (150), category (100), image (2048), description (5000), specs (JSON object) |
| steps | id, robotId, stepOrder (integer 1–10000), title (150), instruction (10000), illustration (JSON object) |
| library-resources | id, robotId (slug hoặc null), title (150), type (image/document/link), url (2048), description (5000) |

JSON wiring/specs/illustration tối đa 20000 ký tự serialized. Nội dung bên trong JSON là dữ liệu linh hoạt, chưa có hợp đồng mô phỏng 2D; bên dùng phải kiểm tra cấu trúc cần thiết. Quantity là integer 1–10000, body PUT chỉ `{ "quantity": 4 }`.

Ví dụ POST `/api/admin/steps`:

```json
{"id":"mini-arm-step-6","robotId":"mini-arm","stepOrder":6,"title":"Kiểm tra","instruction":"Kiểm tra chuyển động từng khớp.","illustration":{}}
```

Ví dụ PATCH `/api/admin/components/sg90`: `{"description":"Servo cho bộ kit cánh tay mini."}`.

| HTTP | Code | Tình huống |
|---|---|---|
| 422 | VALIDATION_ERROR | Thiếu field, ID/URL sai, quantity=0, order không nguyên, patch id |
| 400/413 | INVALID_JSON / PAYLOAD_TOO_LARGE | Middleware chung xử lý JSON sai cú pháp / body quá lớn |
| 401/403 | Do middleware chung quy định | Chưa login/không có quyền/CSRF thất bại |
| 404 | NOT_FOUND | Bản ghi hoặc quan hệ không tồn tại |
| 409 | CONFLICT | Trùng ID hoặc (robotId,stepOrder) |
| 409 | RELATION_CONFLICT | Khóa ngoại thiếu hoặc bản ghi đang được tham chiếu |
| 500 | Do middleware lỗi chung quy định | Lỗi ngoài dự kiến; không gửi SQL/stack cho client |

Fixtures hợp lệ: `tests/content/seed-fixture.json`. Trường hợp không hợp lệ và status mong đợi: `tests/content/content.test.cjs` và `http.test.cjs`.
