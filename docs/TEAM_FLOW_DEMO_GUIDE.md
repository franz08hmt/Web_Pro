# Robot Assembly Lab — Sổ tay vấn đáp theo luồng tích hợp

Tài liệu này chỉ ghi phần nhóm đã làm trong luồng: **dữ liệu nội dung → API → web công khai → đăng nhập → chuẩn bị linh kiện → lắp ráp 3D → lưu/khôi phục → tài khoản và trang chủ**.

Mục tiêu khi vấn đáp: không nói chung chung. Với mỗi câu hỏi, mở đúng file trong cột **Bằng chứng** và tìm đúng hàm/endpoint nêu ở đó.

## 1. Bức tranh chung trong 30 giây

```text
data.js (nguồn seed ban đầu)
        ↓ build-seed.cjs
MySQL: robots/components/steps/relations/library
        ↓ createContentRouter, cùng Express :3000
Public pages gọi /api/... ── lỗi/mất DB → fallback dữ liệu tĩnh có kiểm soát
        ↓
Đăng nhập (cookie HttpOnly + CSRF) → Assembly Session của từng user
        ↓
Chuẩn bị linh kiện → READY → phòng 3D → bước/bộ phận 3D
        ↓                                      ↓
session_components / session_steps / session_visual_parts
        ↓
Trang Tài khoản + khối tiến độ Trang chủ mở lại đúng ?model=...&session=...
```

**Cách chạy để demo:** dùng `npm run dev`, mở `http://127.0.0.1:3000`. Không dùng preview tĩnh của IntelliJ cho luồng đầy đủ vì preview đó không chạy Express/MySQL/API.

**Điều kiện dữ liệu:** database phải đã có migration `001`, `002`, `003`. Với DB đã có `002`, chỉ chạy `database/migrations/003_session_visual_parts.sql`; không chạy lại toàn bộ `schema.sql` hay `seed.sql` trên DB đang dùng.

## 2. Phân công và ranh giới chịu trách nhiệm

| Người | Chịu trách nhiệm chính trong luồng | Không nhận nhầm công |
| --- | --- | --- |
| **Huỳnh Minh Tài** | Express dùng chung, auth, API phiên lắp ráp, gắn Content API của Nhi, frontend gọi API/fallback, lưu & mở lại tiến độ, tích hợp 3D, tài khoản/trang chủ. | Không tự nhận đã thiết kế schema/CRUD nội dung của Nhi hoặc dựng hình học Three.js gốc của Tuấn Anh. |
| **Văn Phạm Thảo Nhi** | ERD, MySQL schema/migration/seed, Content API, repository adapter, CRUD quản trị nội dung, ràng buộc dữ liệu. | Không nhận auth, state-machine phiên hoặc UI/lưu trạng thái 3D tích hợp cuối cùng. |
| **Phạm Tuấn Anh** | Phòng lắp ráp Three.js: cấu hình mô hình, factory hình học linh kiện, camera/controls, thao tác lắp và responsive. | Không gọi MySQL trực tiếp, không làm auth/CRUD. Lớp nối 3D với API phiên là phần tích hợp của Tài. |

Nguồn phân công gốc: `README.md` phần **Phân công giai đoạn 2**; ranh giới bàn giao: `docs/BACKEND_HANDOFF.md` và `docs/content/TEAM_HANDOFF.md`.

## 3. Luồng end-to-end — nói theo thứ tự này

| Bước | Chuyện gì xảy ra | Bằng chứng phải chỉ ra |
| --- | --- | --- |
| 1. Chuẩn hóa dữ liệu | `assets/js/data.js` là snapshot ban đầu. Script build chuyển snapshot thành seed/fixture, giữ slug ổn định như `line-follower`, `arduino-uno`. | `database/build-seed.cjs`; `database/seed.sql`; `docs/content/INTEGRATION.md` mục **Áp dụng database**. |
| 2. Lưu quan hệ đúng bản chất | MySQL tách robot, linh kiện, quan hệ robot-linh kiện, bước lắp, tài nguyên; không nhét tất cả vào một JSON. | `database/schema.sql`; `docs/erd.md`; `docs/content/DATABASE.md`. |
| 3. Mở API cùng origin | Express mount auth, session API rồi mount `createContentRouter` dưới `/api`; frontend và API cùng `127.0.0.1:3000`. | `server/routes/index.js`, hàm `createApiRouter` (khoảng dòng 33–80); `server/app.js` (serve `assets`, `pages`). |
| 4. Trang công khai lấy dữ liệu thật | Trang robot/linh kiện/thư viện gọi `/api/robots`, `/api/components`, `/api/library-resources`. Khi API chưa sẵn sàng, UI giữ fallback tĩnh thay vì trắng trang. | `assets/js/content-api.js`, các hàm `loadRobots`, `loadComponents`, `loadLibraryResources`; `assets/js/robot-catalog-api.js`, `component-catalog-api.js`, `library-api.js`. |
| 5. Đăng nhập tạo danh tính | Backend băm mật khẩu, phát cookie `ral_session` là `HttpOnly`; các request ghi lấy CSRF token từ `/api/auth/me` và gửi header `X-CSRF-Token`. | `server/services/auth.service.js`; `server/middleware/auth.js`; `assets/js/api.js`, các hàm `request`, `refreshSession`. |
| 6. Bắt đầu chuẩn bị | Người dùng chọn robot. Server kiểm tra robot có trong Content Repository rồi tạo/tiếp tục session của chính user đó. | `assets/js/main.js`, `setupAssembly`; `assets/js/api.js`, `assemblySessions.createOrResume`; `server/services/assembly-session.service.js`, `createOrResume`. |
| 7. Tick linh kiện | Mỗi tick gửi `componentId` thật. Service kiểm tra component có thuộc robot của session; server tự tính phần trăm và tự chuyển `PREPARING ↔ READY`. | `assets/js/main.js`, `applySession` và phần gọi `setComponentPrepared`; `server/services/assembly-session.service.js`, `setComponentPrepared`, `progressPercent`, `synchronizePreparation`. |
| 8. Sang phòng 3D | URL mang `?model=<robotId>&session=<id>`. Nếu có `session`, trang lấy đúng session đó, không tạo nhầm session mới. | `assets/js/main.js` khoảng dòng 220–225; `assets/js/account.js` khoảng dòng 102; `assets/js/assembly-3d.js` phần nạp `activeSession`. |
| 9. Lắp 3D và hoàn tất bước | Three.js tạo từng part theo ID. Việc bật/tắt part được lưu ở `session_visual_parts`; hoàn thành step được lưu ở `session_steps`; server chỉ nhận khi session `IN_PROGRESS`. | `assets/js/assembly-3d-parts.js`, `createAssemblyPart`; `assets/js/assembly-3d.js`, `restoreVisualAssembly`, `setVisualPart`, `setStepStatus`; `server/services/assembly-session.service.js`, `setVisualPart`, `setStepStatus`. |
| 10. Quay lại sau reload | API trả detail session gồm `components`, `steps`, `assembledPartIds`; UI dựng lại checklist, bước và các part 3D đã lắp. | `server/content/backend-repositories.cjs` phần đọc `session_visual_parts`; `assets/js/assembly-3d.js`, `restoreVisualAssembly`; `assets/js/account.js`, `loadHistory`. |
| 11. Hiển thị tiến độ cá nhân | Trang tài khoản liệt kê phiên theo thời gian cập nhật; trang chủ lấy phiên mới nhất và link thẳng về chính session đó. | `server/content/backend-repositories.cjs` dòng khoảng 381 (`ORDER BY updated_at DESC, id DESC`); `assets/js/account.js`; `assets/js/personalization.js`; `index.html` `#home-progress`. |

## 4. Kiến thức và bằng chứng của Tài

### Tài xây gì?

1. **Backend chung và tích hợp module của Nhi.**
   - Nói: “Tôi không copy API nội dung vào frontend. Tôi gắn router nội dung mà Nhi bàn giao vào Express chung, sau auth/session, để tất cả dùng cùng origin `/api`.”
   - Mở: `server/routes/index.js` → `createApiRouter` → `router.use(createContentRouter(...))`.

2. **Xác thực và biên bảo vệ dữ liệu người dùng.**
   - Nói: “Cookie chỉ là token ngẫu nhiên; server lưu hash token. Cookie là HttpOnly, thao tác ghi cần CSRF, session khác chủ trả 404 để không lộ ID.”
   - Mở: `server/middleware/auth.js` → `SESSION_COOKIE`, `protectMutation`; `server/services/auth.service.js` → `csrfTokenFor`; `server/services/assembly-session.service.js` → `owned`.

3. **State machine của phiên, không tin phần trăm từ trình duyệt.**
   - Nói: “Client chỉ gửi trạng thái của từng component/step. Backend đối chiếu database, tự tính `progressPercent`, rồi mới đổi `PREPARING`, `READY`, `IN_PROGRESS`, `COMPLETED`.”
   - Mở: `server/services/assembly-session.service.js` → `progressPercent`, `synchronizePreparation`, `updateStatus`, `setComponentPrepared`, `setStepStatus`.

4. **Frontend dùng API có fallback có chủ đích.**
   - Nói: “Ở trình duyệt không có backend hoặc lỗi mạng, web vẫn có dữ liệu minh họa tĩnh và báo rõ. Khi chạy ở cổng Express, dữ liệu ưu tiên là MySQL/API.”
   - Mở: `assets/js/content-api.js`; `assets/js/main.js` → `RobotContentApi.loadRobots()`.

5. **Nối chuẩn bị 2D, 3D, tài khoản và trang chủ bằng cùng `sessionId`.**
   - Nói: “`sessionId` là khóa xuyên suốt, không dùng tên robot để suy luận tiến độ. Vì vậy có thể mở lại đúng một lần thực hành.”
   - Mở: `assets/js/api.js` → `assemblySessions`; `assets/js/main.js`; `assets/js/assembly-3d.js`; `assets/js/account.js`; `assets/js/personalization.js`.

6. **Kiểm thử phần tích hợp.**
   - Mở: `tests/server/assembly-sessions.test.cjs` (authorization/state transitions); `tests/ui-session-flow.cjs` (mở lại đúng session); `tests/ui-personalization.cjs` (trang chủ/tài khoản); `tests/server/frontend-api-client.test.cjs`.

### Khi bị hỏi “Tài giải quyết vấn đề gì?”

| Vấn đề thực tế | Cách giải quyết | Bằng chứng |
| --- | --- | --- |
| Dữ liệu trang tĩnh và dữ liệu admin cập nhật bị lệch | Đổi đường đọc chính sang Content API; `data.js` chỉ còn fallback/nguồn seed. | `assets/js/content-api.js`, `database/build-seed.cjs`. |
| Người dùng reload mất bài đang làm | Lưu session và tiến độ theo `userId + sessionId` trong MySQL. | `server/content/backend-repositories.cjs`; `assets/js/account.js`. |
| 3D lắp xong nhưng reload không còn mô hình | Bổ sung `session_visual_parts` + endpoint visual parts + restore bằng `assembledPartIds`. | `database/migrations/003_session_visual_parts.sql`; `assets/js/assembly-3d.js`. |
| Client có thể tick sai linh kiện/đi tắt trạng thái | Backend xác minh relation robot-component/step và state transition trước khi ghi. | `server/services/assembly-session.service.js`. |
| Endpoint viết dữ liệu bị CSRF hoặc đọc session người khác | Cookie HttpOnly, CSRF header, `findOwnedById`; không phân biệt 404 khác chủ/không tồn tại. | `server/middleware/auth.js`; `server/services/assembly-session.service.js`. |

## 5. Kiến thức và bằng chứng của Thảo Nhi

### Nhi xây gì?

1. **Mô hình dữ liệu quan hệ và migration/seed.**
   - Nói: “Dữ liệu robot không chỉ là danh sách card. Quan hệ `robot_components` cung cấp BOM và quantity; `assembly_steps` có ID ổn định; các bảng progress dùng khóa ngoại kép để không gắn nhầm robot.”
   - Mở: `docs/erd.md`; `database/schema.sql`; `docs/content/DATABASE.md`.

2. **Content Repository và Content API.**
   - Nói: “API nội dung tách ra thành router có thể mount. API trả envelope thống nhất, phân trang, validation và CRUD admin.”
   - Mở: `server/content/index.cjs` → `createContentRouter`; `server/content/repository.cjs`; `docs/content/API.md`.

3. **Adapter MySQL cho lớp backend.**
   - Nói: “Adapter biến hàng MySQL thành contract camelCase cho backend; đồng thời kiểm tra ownership ở lớp truy cập session và lưu `session_visual_parts`.”
   - Mở: `server/content/backend-repositories.cjs` → `createBackendRepositories`, phần `listByUser`, `setVisualPart`.

4. **Quản trị nội dung.**
   - Nói: “CRUD nội dung nằm sau `/api/admin`, yêu cầu ADMIN và CSRF, không phải API public mở.”
   - Mở: `server/content/index.cjs` → `router.use('/admin', requireAdmin)`; `assets/js/admin-content.js`; `pages/admin-content.html`.

5. **Kiểm thử data/API.**
   - Mở: `tests/content/content.test.cjs`; `tests/content/http.test.cjs`; `tests/content/backend-contract.test.cjs`; `docs/content/VALIDATION.md`.

### Khi bị hỏi “Vì sao phải dùng database/ERD?”

Trả lời ngắn: “Vì một linh kiện dùng được cho nhiều robot, mỗi robot cần quantity khác nhau, và tiến độ phải gắn vào đúng robot/session. Nếu chỉ để mảng JSON trong frontend, không kiểm soát được foreign key, admin không cập nhật tập trung được và không chứng minh được dữ liệu của user là đúng. ERD tách bảng quan hệ để backend kiểm tra được điều đó.”

Sau đó chỉ: `docs/erd.md` → `robot_components`, `session_components`, `session_steps`, `session_visual_parts`; rồi `database/schema.sql` → các FK/unique key tương ứng.

## 6. Kiến thức và bằng chứng của Phạm Tuấn Anh

### Tuấn Anh xây gì?

1. **Cấu hình riêng cho từng robot.**
   - Nói: “Mỗi robot có danh sách part và vị trí theo một config; render không hard-code vào HTML.”
   - Mở: `assets/js/assembly-3d-config.js` → `window.ASSEMBLY_3D_CONFIG`.

2. **Factory tạo hình học linh kiện bằng Three.js.**
   - Nói: “Mô hình là procedural 3D: tạo hình từ box/cylinder/shape, không phải ảnh PNG đặt trong canvas. Từng component dùng `componentId` để factory trả đúng object.”
   - Mở: `assets/js/assembly-3d-parts.js` → `window.createAssemblyPart` (khoảng dòng 227).

3. **Phòng 3D và tương tác.**
   - Nói: “Module 3D tách khỏi `main.js`, dựng scene/camera/grid/control và render các part theo config; nhờ tách module mà phần backend/tài khoản không đụng trực tiếp render.”
   - Mở: `assets/js/assembly-3d.js` → phần khởi tạo scene và đoạn gọi `createAssemblyPart(THREE, part.id)` (khoảng dòng 382); `pages/lap-rap-3d.html`.

4. **Sau khi tích hợp với API của Tài.**
   - Nói chính xác: “Tuấn Anh cung cấp mô phỏng. Lớp tích hợp sau này gọi `RobotAssemblyApi.assemblySessions` để lưu/bật lại part và step là phần Tài thêm vào để mô hình trở thành tiến độ theo tài khoản.”
   - Mở: `assets/js/assembly-3d.js` → `restoreVisualAssembly`, `activeSession`, `setVisualPart`, `setStepStatus`; `assets/js/api.js`.

### Khi bị hỏi “Model 3D được xây dựng từ đâu?”

Trả lời: “Từ JavaScript Three.js cục bộ. File HTML nạp Three.js qua `/vendor/three`; `assembly-3d-config.js` nói robot cần những part nào; `assembly-3d-parts.js` là factory hình học; `assembly-3d.js` đưa các object đó vào scene. Vì vậy không phụ thuộc ảnh card và không cần gửi file 3D từ server để render.”

Chỉ lần lượt: `server/app.js` (route `/vendor/three`) → `pages/lap-rap-3d.html` (script) → `assets/js/assembly-3d-config.js` → `assets/js/assembly-3d-parts.js` → `assets/js/assembly-3d.js`.

## 7. Bộ câu hỏi giảng viên thường hỏi — trả lời và chỉ code

| Câu hỏi | Trả lời gọn | Mở để chứng minh |
| --- | --- | --- |
| “Dữ liệu khởi nguồn từ đâu?” | Từ `data.js` được build thành seed ban đầu; khi full-stack chạy, nguồn đọc chính là MySQL qua Content API. | `database/build-seed.cjs`, `database/seed.sql`, `assets/js/content-api.js`. |
| “Admin sửa tên/ảnh linh kiện thì trang public đổi thế nào?” | Admin ghi MySQL qua `/api/admin`; trang public lần tải sau gọi `/api/components`, nên lấy bản đã đổi. | `server/content/index.cjs`, `assets/js/admin-content.js`, `assets/js/component-catalog-api.js`. |
| “Vì sao không truyền `progressPercent` từ frontend?” | Client không đáng tin. Server lấy component đã tick, đếm group bắt buộc và tự tính floor phần trăm. | `server/services/assembly-session.service.js` → `progressPercent`. |
| “Vì sao không thể tick linh kiện của robot khác?” | Service đối chiếu `componentId` với relations của `robotId`; schema có FK kép cho progress. | `server/services/assembly-session.service.js` → `setComponentPrepared`; `docs/erd.md`. |
| “Làm sao biết session thuộc đúng người?” | Mỗi API session dùng `request.auth.user.id`; service gọi `findOwnedById`; khác chủ cũng trả 404. | `server/routes/assembly-session.routes.js`; `server/services/assembly-session.service.js` → `owned`. |
| “Reload hay đổi máy có mất 3D không?” | Không, part đã lắp nằm trong `session_visual_parts` và endpoint trả `assembledPartIds`; trang dựng lại scene từ đó. | migration `003`; `server/content/backend-repositories.cjs`; `assets/js/assembly-3d.js` → `restoreVisualAssembly`. |
| “Sao không mở trực tiếp file HTML bằng IntelliJ?” | Vì API/auth/MySQL chạy trong Express. Preview tĩnh không mount `/api`, nên chỉ có fallback hoặc báo lỗi, không phải full-stack demo. | `server/app.js`; `server/routes/index.js`; `README.md` phần chạy ứng dụng. |
| “Lỗi API hiển thị thế nào?” | Client chuẩn hóa lỗi HTTP/network thành `{ code, message, status }`; UI không hiển thị stack trace. | `assets/js/api.js`; `docs/BACKEND_HANDOFF.md` mục hình dạng lỗi. |
| “Trạng thái có thể nhảy thẳng hoàn tất không?” | Không. Transition chỉ cho `READY → IN_PROGRESS → COMPLETED`; server tự hoàn tất khi mọi step completed. | `server/services/assembly-session.service.js` → `updateStatus`, `setStepStatus`. |
| “Phần 3D là asset ảnh hay model?” | Là procedural model Three.js bằng factory hình học. Ảnh trong Content API chỉ phục vụ catalog/card. | `assets/js/assembly-3d-parts.js`; `assets/js/assembly-3d.js`; `assets/js/content-api.js`. |

## 8. Kịch bản demo an toàn trong 3–5 phút

1. Mở `/api/health` để xác nhận `database: "connected"`.
2. Mở **Linh kiện**; nói dữ liệu đang đi qua `content-api.js` và chỉ một linh kiện từ catalog.
3. Đăng ký/đăng nhập user thường. Mở **Mẫu robot** → chọn một robot → vào **Chuẩn bị linh kiện**.
4. Tick lần lượt các nhóm linh kiện. Khi đủ, chỉ `READY`/100%; giải thích đây là server tính.
5. Chọn sang **Lắp ráp 3D**, URL phải có `session=<id>`. Bật một part và đánh dấu bước; reload trang để chứng minh part/bước vẫn còn.
6. Mở **Tài khoản** rồi **Trang chủ**, bấm **Tiếp tục** để chứng minh link mở đúng ID phiên.
7. Vào `pages/admin-content.html` bằng tài khoản ADMIN, sửa một field không nhạy cảm, rồi bấm **Kiểm chứng trên trang công khai** để mở tab đối chiếu — chứng minh dữ liệu đi thẳng từ MySQL ra trang công khai.

### Chuẩn bị tài khoản ADMIN trước buổi demo

Đăng ký trên website luôn tạo role `USER` (`server/services/auth.service.js`), nên
khu vực quản trị **không thể** mở bằng tài khoản vừa đăng ký. Chuẩn bị trước bằng một
trong hai lệnh sau, chạy một lần trên máy demo:

```powershell
npm run create-admin -- --email=admin@robotlab.local --name="Quản trị nội dung"
npm run create-admin -- --email=tai-khoan-da-co@example.com --promote
```

Lệnh thứ nhất tạo tài khoản quản trị mới và hỏi mật khẩu trực tiếp (không hiện trên
màn hình, không lưu vào lịch sử shell). Lệnh thứ hai nâng quyền cho một tài khoản đã
đăng ký sẵn. Giữ lại **cả hai** tài khoản khi demo: một `USER` để diễn luồng sinh
viên, một `ADMIN` để diễn luồng quản trị — đó là cách cho thấy phân quyền có thật.

Nếu mở trang quản trị mà chưa đăng nhập hoặc đang dùng tài khoản `USER`, trang sẽ
hiện đúng lý do kèm lối sang trang đăng nhập, không phải màn hình trắng.

## 9. Điều cần nói trung thực nếu bị hỏi sâu

- Cần chạy migration `003_session_visual_parts.sql` trên database đích trước khi demo lưu trạng thái 3D. Migration là DDL, không tự chạy ngầm từ app.
- `data.js` vẫn tồn tại làm fallback và nguồn seed; nó không còn là nguồn dữ liệu chính khi MySQL/API đã cấu hình.
- Three.js model hiện là mô hình procedural minh họa theo component, không tuyên bố là bản CAD/GLB có kích thước cơ khí chính xác.
- Không nêu mật khẩu, chuỗi kết nối DB hay nội dung `.env` khi trình bày.

## 10. Lệnh kiểm chứng trước khi bảo vệ

```powershell
npm run dev
npm test
npm run test:ui
npm run test:ui:sessions
npm run test:ui:personalization
npm run test:ui:phase3
```

Nếu MySQL chưa kết nối, kiểm tra `/api/health` trước. Nếu trả `not_configured`, kiểm tra đủ năm biến `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` trong môi trường chạy server; không đưa file `.env` vào Git.
