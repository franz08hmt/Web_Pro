# Bàn giao phần Nhi

Đã đối chiếu nền nhóm `origin/feature/ui-enhancement-phase-2` tại commit `3fda472`
ngày 2026-09-16. Nhánh làm việc: `feature/thao-nhi`.

- [DEMO.md](DEMO.md): chạy demo và kịch bản thuyết trình.
- [TEAM_HANDOFF.md](TEAM_HANDOFF.md): trả lời 10 câu hỏi của Tài, hợp đồng với Tuấn Anh.
- [API.md](API.md), [DATABASE.md](DATABASE.md): hợp đồng nội dung và schema.
- [VALIDATION.md](VALIDATION.md): kết quả kiểm thử thực tế và giới hạn.

## Cài và kiểm thử

Chạy từ thư mục gốc bằng Node.js >= 20.6 (để dùng `--env-file`):

```powershell
npm ci
npm ci --prefix server/content
node --test tests/content/*.test.cjs tests/server/*.test.cjs
node --env-file=server/content/.env tests/content/mysql-smoke.cjs
node --env-file=server/content/.env tests/content/backend-mysql.cjs
node --env-file=server/content/.env tests/content/migrations-mysql.cjs
```

`.env` dùng database riêng kết thúc `_test`; mẫu ở `server/content/.env.example`.
Smoke test rollback; backend adapter test tạo tài khoản ngẫu nhiên và xóa đúng dữ liệu
của lần chạy; migration test tạo/xóa database tạm tên riêng, cần quyền CREATE/DROP DATABASE.
Không chạy các test này bằng cấu hình production. Không commit `.env` hoặc node_modules.

## Áp dụng database

Cài mới: tạo DB utf8mb4 rồi chạy **một** trong hai cách:

1. `database/schema.sql`, sau đó `database/seed.sql`.
2. Các migration 001, 002 theo thứ tự, sau đó `database/seed.sql`.

DB đã có 001: chỉ chạy `database/migrations/002_backend_contract.sql`.
Đọc `schema_migrations` trước khi áp dụng. DDL MySQL tự commit; nếu lỗi giữa chừng,
kiểm tra trạng thái từng thay đổi trước khi chạy tiếp. Seed chỉ nạp một lần trên DB mới.
Khi lỗi seed, ROLLBACK trong cùng kết nối; không dùng `--force`.

`node database/build-seed.cjs` tái tạo snapshot, seed và fixture từ data.js.
Giữ slug gốc, đổi ảnh sang `/assets/...`; bổ sung 4 linh kiện thiếu trong danh mục.
Seed có 3 robot, 14 linh kiện, 15 bước, 3 tài nguyên ảnh; không có tài khoản mặc định.
Thông số nguồn/khung minh họa và BOM động cơ kèm bánh cần đối chiếu bộ kit thật.

## Tài nối backend

Module nội dung không tự mở port hoặc cung cấp xác thực production. Đặt sau session
middleware và trước 404/error middleware trong backend của Tài:

```js
const { createContentRouter } = require('./content/index.cjs');
const { createBackendRepositories } = require('./content/backend-repositories.cjs');

const repositories = createBackendRepositories(pool);
// Inject repositories.users/authSessions/assemblySessions vào service tương ứng.
app.use('/api', createContentRouter({
  Router: express.Router, pool, requireAdmin, protectMutation
}));
```

Điều kiện tích hợp cần Tài thực hiện:

- MySQL đã có migration 002; pool dùng UTC, `timezone: 'Z'`, khuyến nghị
  `supportBigNumbers: true, bigNumberStrings: true`. Adapter CAST ID sang chuỗi.
- `requireAdmin` xác minh phiên server, kiểm tra role `ADMIN`, trả 401/403.
- `protectMutation` kiểm tra CSRF/Origin; không dùng middleware demo trong production.
- `/api/auth/me` trả `{data:{user:{id,fullName,role:"ADMIN"}},csrfToken:"..."}`.
  Cấu trúc user/role khớp hợp đồng nhóm; `csrfToken` là phần bổ sung cần Tài cấp nếu
  dùng CSRF token. Frontend gửi `X-CSRF-Token` khi có token.
- Parser hiện tại của Tài giới hạn 16kb: cần nâng giới hạn cho request nội dung lên
  256kb **trước** router, không thêm parser sau parser 16kb đã từ chối request.
  Error middleware chung cần trả 413 cho `entity.too.large` (hiện có thể thành 500).
- Serve `/assets` và các HTML cần thiết có chọn lọc, cùng origin; không expose repo.
  Backend hiện tại chỉ cung cấp API health, chưa serve trang quản trị hoặc auth.

Lỗi nội dung trả `{error:{code,message,requestId?}}`, validation 422, FK/duplicate 409.
Lỗi DB bất ngờ chuyển cho error middleware chung. Adapter auth/session dùng `ApiError`
của Tài; không tự cài service đăng nhập hoặc quy tắc chuyển trạng thái.

## Git và merge

Phần Nhi nằm trong `server/content`, `database`, `tests/content`, `docs/content`,
`docs/erd.md` và ba file admin. Không sửa app.js, repository ports, main.js hoặc
module 2D của thành viên khác. Tách thư mục giảm conflict văn bản; vẫn phải kiểm tra
hợp đồng và luồng end-to-end khi tích hợp.

Tại lần kiểm tra này chưa có `origin/integration/fullstack-v2`; không chạy lệnh merge
một nhánh chưa tồn tại. Nhóm chọn nhánh đích thống nhất, ưu tiên nền backend đã kiểm tra.
Các nhánh riêng cũ của Tài/Tuấn Anh có thể thiếu commit nền mới.

Sau khi commit phần Nhi, kiểm tra merge không thay worktree:

```powershell
git fetch origin
git merge-tree --write-tree HEAD origin/feature/ui-enhancement-phase-2
git push -u origin feature/thao-nhi
```

Nếu nhóm chọn nhánh khác, thay đúng tên nhánh và chạy lại kiểm tra.
Không force-push, không tự merge vào main. Trước khi phát hành, Tài/Tuấn Anh cần chạy
luồng đăng nhập thật -> chọn robot -> chuẩn bị -> lưu bước -> tải lại -> hoàn thành.
