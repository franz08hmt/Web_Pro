# Prompt triển khai phần việc của Huỳnh Minh Tài đến trạng thái sẵn sàng tích hợp

Bạn là một Senior Backend Engineer kiêm Integration Engineer. Hãy tiếp quản project **Robot Assembly Lab** và hoàn thiện đúng phần việc của **Huỳnh Minh Tài** đến trạng thái có thể chờ dữ liệu/schema của Văn Phạm Thảo Nhi và mô phỏng 2D của Phạm Tuấn Anh để tích hợp. Làm việc dựa trên code thực tế, không suy đoán rằng tài liệu luôn đúng.

## 1. Mục tiêu cuối của lượt triển khai

Hoàn thiện nền backend và các hợp đồng tích hợp thuộc phạm vi của Tài:

1. Củng cố nền Express, cấu hình môi trường, CORS, logging, rate limit, xử lý lỗi và response thống nhất.
2. Chốt hợp đồng REST API cho xác thực và phiên lắp ráp trước khi Nhi/Tuấn Anh tích hợp.
3. Triển khai route/controller/service/validation/auth middleware cho đăng ký, đăng nhập, đăng xuất, người dùng hiện tại và phiên lắp ráp.
4. Tách phụ thuộc dữ liệu bằng repository interface/port và dependency injection để có thể kiểm thử khi MySQL/schema thật chưa được merge.
5. Tạo API client dùng chung cho frontend và kết nối giao diện tài khoản với API; không làm giả đăng nhập bằng `localStorage`.
6. Chuẩn bị tài liệu handoff rõ ràng để Nhi chỉ cần cắm MySQL repositories/schema và Tuấn Anh chỉ cần gọi API phiên lắp ráp từ module 2D.
7. Giữ giao diện hiện tại ổn định; không thiết kế lại UI và không làm thay nghiệp vụ của Nhi hoặc Tuấn Anh.

Kết quả của lượt này **không được tuyên bố là full-stack hoàn chỉnh** nếu chưa có MySQL schema/seed thật và chưa có mô phỏng 2D thật. Trạng thái đúng cần đạt là: **backend và frontend integration của Tài đã sẵn sàng, test được bằng test doubles/in-memory adapter có kiểm soát, còn production persistence và 2D đang chờ hai nhánh của đồng đội**.

## 2. Trạng thái repo đã biết tại thời điểm tạo prompt

- Workspace chính: `D:\Web-Pro\robot-engine-website`
- Nhánh đang làm: `feature/tai-backend-integration`
- Commit nền backend hiện tại: `052451a feat(api): add Express foundation and health endpoint`
- Commit cha chứa UI/sliders: `85021ef feat(ui): add visual robot and component sliders`
- Nhánh backend hiện chưa có upstream remote được xác nhận; không tự push.
- Backend hiện có:
  - Express 5, `dotenv`, `cors`, `helmet`, `express-rate-limit`, `mysql2`.
  - `GET /api/health`.
  - Cấu hình `.env.example`, MySQL pool tùy chọn, request ID, structured logging.
  - Response `{ data, meta? }` và `{ error: { code, message, details?, requestId? } }`.
  - 11 backend tests đang pass.
- Chưa có:
  - `database/`, migration, schema hoặc seed.
  - Route xác thực và phân quyền.
  - Route/service/repository cho phiên lắp ráp.
  - `assets/js/api.js` và bất kỳ `fetch()` nào từ frontend đến backend.
  - MySQL repositories thật cho user/session.
- Giao diện tài khoản hiện mới là preview: `assets/js/account.js` chỉ validate HTML5 và hiển thị thông báo chờ database.
- `assets/js/main.js` vẫn lưu checklist lắp ráp trong `localStorage`; đây là fallback web tĩnh, chưa phải tiến độ theo tài khoản.
- Các ID model đang dùng và phải giữ tương thích: `line-follower`, `obstacle-avoider`, `mini-arm`.
- Hiện có nhiều thay đổi frontend chưa commit và file chưa được track. Tất cả đều là dữ liệu của người dùng, phải bảo vệ tuyệt đối.

Trạng thái trên chỉ là snapshot. Trước khi sửa, phải chạy lại các kiểm tra ở phần tiếp theo và ưu tiên kết quả thực tế.

## 3. Quy tắc bắt buộc trước khi hành động

1. Đọc toàn bộ `README.md`, `docs/API_CONVENTIONS.md`, `.env.example`, `package.json`, `server/`, `tests/server/`, `assets/js/data.js`, phần assembly trong `assets/js/main.js`, `pages/tai-khoan.html` và `assets/js/account.js` nếu các file tồn tại.
2. Đọc và tuân thủ các quy tắc phù hợp trong `D:\Web-Pro\.agents`, tối thiểu các skill về API/interface design, security, test-driven development, incremental implementation, code review và Git workflow.
3. Chạy:

   ```powershell
   git status --short --branch
   git log --oneline --decorate -12
   git worktree list
   git diff --check
   npm test
   ```

4. Không dùng `git reset --hard`, `git clean`, `git checkout -- <file>`, không stash và không ghi đè thay đổi chưa commit.
5. Không checkout `feature/ui-enhancement-phase-2`; nhánh đó có thể đang được checkout ở worktree khác.
6. Không stage, commit, push, merge hoặc mở Pull Request nếu người dùng chưa yêu cầu rõ. Nếu được yêu cầu commit, chỉ stage đúng file của từng lát cắt; tuyệt đối không gom các thay đổi UI không liên quan.
7. Nếu trạng thái thực tế khác snapshot, báo ngắn gọn sự khác biệt rồi điều chỉnh kế hoạch; không tự xóa hoặc lùi code.

## 4. Ranh giới trách nhiệm

### Được phép triển khai trong phần Tài

- Express application composition và dependency injection.
- API conventions, validation, error semantics, auth middleware và authorization.
- Đăng ký, đăng nhập, đăng xuất, `GET /api/auth/me`.
- API tạo/đọc/cập nhật phiên lắp ráp và hợp đồng cập nhật tiến độ.
- Repository contracts/ports và adapters in-memory chỉ dành cho test hoặc development được bật rõ bằng biến môi trường.
- Frontend API client dùng chung.
- Kết nối form tài khoản với API cùng trạng thái loading/success/error accessible.
- Integration tests, contract tests, tài liệu tích hợp, README và cấu hình mẫu.
- Review độ nhất quán header/footer/design system nếu cần cho điểm nối tài khoản, nhưng không redesign.

### Không được làm thay Nhi

- Không tự tạo schema/migration/seed production cho `robots`, `components`, `robot_components`, `assembly_steps`, `library_resources` hoặc trang quản trị.
- Không thay `assets/js/data.js` bằng dữ liệu tự nghĩ ra.
- Không tự triển khai API nội dung `/api/robots`, `/api/components`, `/api/library-resources` nếu chưa có contract/nhánh của Nhi.
- Không đoán tên cột MySQL. Chỉ ghi rõ repository contract và danh sách cột/ràng buộc Tài cần Nhi cung cấp.

### Không được làm thay Tuấn Anh

- Không xây Canvas/SVG, kéo-thả, hit testing, thứ tự thao tác 2D hoặc asset/state machine mô phỏng.
- Không viết logic 2D vào `assets/js/main.js`.
- Chỉ cung cấp API client/hợp đồng cho Tuấn Anh lưu và khôi phục tiến độ.

## 5. Kiến trúc và nguyên tắc cần giữ

- Tiếp tục dùng CommonJS và JavaScript hiện có; không chuyển toàn project sang TypeScript hoặc framework mới.
- Luồng phụ thuộc: `route -> controller -> service -> repository port -> adapter`.
- Route/controller chỉ xử lý HTTP, parse input và gọi service.
- Service chứa nghiệp vụ, không biết Express hoặc SQL.
- Repository là ranh giới dữ liệu; truy vấn SQL thật chỉ được thêm sau khi schema đã chốt.
- Mọi input ngoài hệ thống phải validate tại boundary.
- Mọi list endpoint phải có pagination hoặc giới hạn rõ.
- Không trả password hash, session token, stack trace hoặc thông tin kết nối.
- Giữ response envelope hiện tại; không tạo response shape thứ hai.
- Giữ các model ID hiện có; không dùng tên hiển thị tiếng Việt làm khóa quan hệ.
- Dùng cookie `HttpOnly`, `SameSite=Lax`, `Secure` trong production. Không lưu token nhạy cảm trong `localStorage`.
- Kiểm tra quyền sở hữu phiên để tránh IDOR: user A không được đọc/sửa phiên của user B; admin chỉ có quyền được tài liệu định nghĩa.
- Nếu chưa có MySQL adapter, production phải fail rõ bằng lỗi `503 DEPENDENCY_NOT_READY`; không âm thầm dùng memory store trong production.

## 6. Hợp đồng API tối thiểu cần chốt

Trước khi viết implementation, bổ sung tài liệu contract với request, success response, error response, status code, cookie và quyền truy cập cho từng endpoint. Có thể điều chỉnh chi tiết nếu code hiện tại đòi hỏi, nhưng phải nhất quán và tương thích ngược.

### Xác thực

| Method | Endpoint | Input chính | Kết quả |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | `fullName`, `email`, `password` | `201`, user công khai và cookie phiên |
| `POST` | `/api/auth/login` | `email`, `password` | `200`, user công khai và cookie phiên |
| `POST` | `/api/auth/logout` | Không có | `204`, thu hồi phiên và xóa cookie |
| `GET` | `/api/auth/me` | Cookie phiên | `200` user hoặc `401` |

Mã lỗi tối thiểu: `VALIDATION_ERROR` (422), `INVALID_CREDENTIALS` (401), `AUTH_REQUIRED` (401), `EMAIL_ALREADY_EXISTS` (409), `FORBIDDEN` (403), `DEPENDENCY_NOT_READY` (503).

User public shape tối thiểu:

```json
{
  "id": "stable-id",
  "fullName": "Nguyễn Văn A",
  "email": "user@example.com",
  "role": "USER",
  "createdAt": "2026-09-16T00:00:00.000Z"
}
```

### Phiên lắp ráp

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| `POST` | `/api/assembly-sessions` | Tạo hoặc tiếp tục phiên cho `robotId` |
| `GET` | `/api/assembly-sessions` | Danh sách phiên của user hiện tại, có pagination/filter |
| `GET` | `/api/assembly-sessions/:sessionId` | Lấy phiên cùng component/step progress |
| `PATCH` | `/api/assembly-sessions/:sessionId` | Cập nhật trạng thái phiên hợp lệ |
| `PUT` | `/api/assembly-sessions/:sessionId/components/:componentId` | Upsert trạng thái chuẩn bị linh kiện |
| `PUT` | `/api/assembly-sessions/:sessionId/steps/:stepId` | Upsert trạng thái bước lắp ráp |

Yêu cầu contract:

- `robotId`, `componentId`, `stepId` là ID ổn định do API nội dung của Nhi cung cấp.
- Session response phải có `id`, `userId`, `robotId`, `status`, `progressPercent`, `components`, `steps`, `createdAt`, `updatedAt`.
- Chốt enum session/step trong tài liệu; không rải chuỗi tùy ý trong code.
- `progressPercent` phải được tính ở service/server từ trạng thái, không tin số phần trăm do client gửi.
- Endpoint ghi phải idempotent khi hợp lý, đặc biệt hai endpoint `PUT` progress.
- Tuấn Anh chỉ cần gọi API client; không biết SQL, pool hoặc Express internals.

## 7. Kế hoạch triển khai theo lát cắt

### Lát cắt 1 — Chốt contract và dependency boundaries

- Cập nhật `docs/API_CONVENTIONS.md` hoặc tạo tài liệu contract chuyên biệt.
- Xác định public DTO, enum, error codes và repository methods.
- Tạo handoff checklist nêu chính xác những gì cần Nhi cung cấp và những hàm Tuấn Anh sẽ gọi.

Tiêu chí:

- Mỗi endpoint có input/output/status/error/auth rule.
- Không có tên cột SQL được đoán thành sự thật.
- Các ID hiện có được giữ nguyên.

### Lát cắt 2 — Validation, auth service và auth HTTP layer

- Viết test trước cho validation và service.
- Triển khai password hashing an toàn; ưu tiên thư viện phổ biến, nhỏ và có lý do rõ. Không tự viết thuật toán mã hóa.
- Triển khai session cookie và auth middleware qua repository port.
- Route/controller không chứa truy vấn hoặc nghiệp vụ.
- Dùng fake/in-memory repositories trong test; runtime memory adapter chỉ bật khi biến môi trường development ghi rõ.

Tiêu chí:

- Register/login/logout/me có test happy path và error path.
- Duplicate email không tạo user thứ hai.
- Login trả lỗi chung, không tiết lộ email có tồn tại hay không.
- Response không chứa password/hash/token.
- Cookie có cờ bảo mật theo môi trường.

### Lát cắt 3 — Assembly session service và HTTP layer

- Viết contract tests bằng fake repositories.
- Kiểm tra user ownership cho mọi thao tác.
- Tính progress ở server và kiểm tra transition trạng thái hợp lệ.
- Không cần mô phỏng 2D để test API.

Tiêu chí:

- User chỉ xem/sửa phiên của mình.
- Cập nhật component/step lặp lại không tạo bản ghi trùng về mặt nghiệp vụ.
- ID hoặc trạng thái không hợp lệ trả lỗi chuẩn.
- Thiếu data adapter thật trả `503`, không crash và không fake thành công.

### Lát cắt 4 — Frontend API client và trang tài khoản

- Tạo `assets/js/api.js` làm điểm duy nhất gọi backend.
- Dùng `fetch`, JSON, `credentials: "include"`, timeout/abort hợp lý và chuyển API error thành object dễ dùng.
- Kết nối `assets/js/account.js` với auth API, giữ HTML5 validation hiện có.
- Thêm trạng thái submitting, vô hiệu hóa nút khi đang gửi, hiển thị lỗi/success bằng vùng `role="status"` hoặc `aria-live` phù hợp, khôi phục nút sau lỗi.
- Không render dữ liệu API bằng `innerHTML` nếu có thể dùng `textContent`.
- Không chuyển các trang nội dung sang API của Nhi trong lát cắt này.

Tiêu chí:

- Form gửi đúng field contract (`fullName`, không dùng `name` mơ hồ ở API boundary).
- Không có giả lập đăng nhập bằng localStorage.
- Lỗi 401/409/422/503 có thông báo tiếng Việt rõ và không lộ chi tiết nội bộ.
- Khi API chưa chạy, UI báo không kết nối được và vẫn dùng được bằng bàn phím.

### Lát cắt 5 — Handoff và integration readiness

- Cập nhật README với cách chạy frontend + API, biến môi trường, test và chế độ development adapter.
- Tạo tài liệu handoff cho Nhi và Tuấn Anh.
- Cung cấp ví dụ request/response hoặc `curl`/PowerShell không chứa secret.
- Ghi rõ phần nào là temporary adapter và điều kiện xóa nó sau khi merge schema thật.

Tiêu chí:

- Một thành viên khác có thể chạy test trên máy mới theo README.
- Nhi biết chính xác repository methods, bảng/ràng buộc/index cần cung cấp.
- Tuấn Anh biết chính xác API client methods và dữ liệu tiến độ cần gửi.
- Không có TODO chung chung kiểu “làm database sau”; mọi dependency chờ phải có owner và acceptance criteria.

## 8. Repository contracts cần chuẩn bị cho Nhi

Tên method có thể điều chỉnh theo convention của code, nhưng contract phải bao phủ tối thiểu:

```text
UserRepository
- findByEmail(email)
- findPublicById(userId)
- create({ fullName, email, passwordHash, role })

AuthSessionRepository
- create({ userId, tokenHash, expiresAt })
- findActiveByTokenHash(tokenHash)
- revokeByTokenHash(tokenHash)
- revokeAllForUser(userId) [nếu contract chốt cần]

RobotRepository
- existsById(robotId)

AssemblySessionRepository
- createOrResume({ userId, robotId })
- listByUser({ userId, page, pageSize, status })
- findOwnedById({ sessionId, userId })
- updateStatus({ sessionId, userId, status })
- upsertComponentProgress({ sessionId, userId, componentId, isPrepared })
- upsertStepProgress({ sessionId, userId, stepId, status })
```

Tài liệu handoff phải yêu cầu Nhi chốt:

- Primary key type và serialization ra JSON.
- Unique index email dạng normalized/case-insensitive.
- Cách lưu password hash.
- Bảng hoặc cơ chế lưu auth session/token hash và expiry.
- Unique constraints cho `(session_id, component_id)` và `(session_id, step_id)`.
- Foreign keys, cascade behavior và transaction boundaries.
- Index cho user/session/robot/status và query danh sách.

Không tự tạo SQL production trước khi các điểm trên được Nhi xác nhận.

## 9. API client contract cần chuẩn bị cho Tuấn Anh

Module frontend cần export hoặc cung cấp namespace ổn định tương đương:

```text
authApi.register(payload)
authApi.login(payload)
authApi.logout()
authApi.me()

assemblySessionsApi.createOrResume(robotId)
assemblySessionsApi.list(params)
assemblySessionsApi.get(sessionId)
assemblySessionsApi.updateStatus(sessionId, status)
assemblySessionsApi.setComponentPrepared(sessionId, componentId, isPrepared)
assemblySessionsApi.setStepStatus(sessionId, stepId, status)
```

Không gắn các hàm này trực tiếp vào logic Canvas/SVG. Tuấn Anh sẽ import/gọi từ module riêng của mô phỏng.

## 10. Kiểm thử và quality gates

Áp dụng TDD cho logic mới. Tối thiểu phải có:

- Unit tests cho validation, auth service, session service và progress calculation.
- Integration tests cho auth routes, cookie, lỗi chuẩn, ownership, malformed JSON, rate limit/CORS không bị hồi quy.
- Tests chứng minh password/hash/token không xuất hiện trong JSON hoặc log.
- Tests cho `503 DEPENDENCY_NOT_READY` khi adapter thật chưa sẵn sàng.
- Syntax checks cho JavaScript frontend được sửa.
- Nếu môi trường browser test có sẵn, chạy smoke test trang tài khoản và kiểm tra không có console error.

Trước khi kết thúc, chạy:

```powershell
npm test
node --check server/app.js
node --check server/index.js
node --check assets/js/api.js
node --check assets/js/account.js
git diff --check
git status --short --branch
```

Nếu có `npm audit`, chỉ báo cáo kết quả; không tự nâng cấp hàng loạt dependencies ngoài phạm vi.

## 11. Definition of Done cho phần Tài trước khi chờ đồng đội

- [ ] Nền Express hiện có không hồi quy và toàn bộ backend tests pass.
- [ ] Auth/session API contract được tài liệu hóa đầy đủ.
- [ ] Route/controller/service/validation/auth middleware đã có và test bằng repository doubles.
- [ ] Production không âm thầm chạy bằng memory persistence.
- [ ] Frontend có API client chung và form tài khoản gọi API thật.
- [ ] Không còn code giả lập auth bằng localStorage.
- [ ] API phiên lắp ráp có contract/service/controller/test và kiểm tra ownership.
- [ ] Có handoff rõ cho MySQL adapter của Nhi.
- [ ] Có handoff rõ cho module 2D của Tuấn Anh.
- [ ] README đủ để thành viên khác chạy và test.
- [ ] Không đụng vào API nội dung/admin của Nhi hoặc logic 2D của Tuấn Anh.
- [ ] Không mất hoặc vô tình stage thay đổi chưa commit của người dùng.

## 12. Điều kiện dừng đúng

Dừng và báo `WAITING_FOR_NHI` khi implementation tiếp theo cần tên bảng/cột, foreign key, migration hoặc seed thật chưa được thống nhất.

Dừng và báo `WAITING_FOR_TUAN_ANH` khi API client và contract đã sẵn sàng nhưng bước tiếp theo là gắn vào Canvas/SVG hoặc state machine 2D.

Không gọi đó là blocker nếu vẫn còn test, contract, service, controller, frontend API client hoặc tài liệu thuộc phần Tài có thể hoàn thiện an toàn.

## 13. Cách báo cáo sau mỗi lát cắt

Trả lời bằng tiếng Việt, dẫn chứng file và test. Mỗi lần hoàn thành một lát cắt, báo:

1. **Đã làm:** file và hành vi thay đổi.
2. **Đã kiểm tra:** lệnh đã chạy và kết quả pass/fail.
3. **Không đụng tới:** phần của Nhi, phần của Tuấn Anh và thay đổi người dùng được bảo vệ.
4. **Đang chờ:** contract/data cụ thể, owner và lý do.
5. **Bước kế tiếp:** đúng một lát cắt nhỏ, không gom thay đổi quá lớn.

Nếu phát hiện contract hiện tại mâu thuẫn với code, ưu tiên bảo toàn dữ liệu và tương thích; trình bày bằng chứng rồi đề xuất phương án trước khi thực hiện thay đổi breaking.
