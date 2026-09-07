# Robot Assembly Lab

Robot Assembly Lab là website hỗ trợ người học lựa chọn mô hình robot, đối chiếu linh kiện và thực hiện quy trình lắp ráp theo từng bước. Phiên bản đầu tiên được xây dựng bằng HTML, CSS và JavaScript thuần; giai đoạn tiếp theo mở rộng thành ứng dụng full-stack có API, cơ sở dữ liệu, tài khoản người dùng và khả năng lưu tiến độ.

## Trạng thái dự án

### Giai đoạn 1 — Nền tảng web tĩnh

- Hoàn thiện giao diện dùng chung, responsive và nội dung cho các trang chính.
- Cung cấp ba mô hình: Robot dò đường, Robot tránh vật cản và Cánh tay robot mini.
- Hỗ trợ tìm kiếm mô hình, lọc linh kiện và kiểm tra danh sách linh kiện bắt buộc.
- Hiển thị tiến độ chuẩn bị và hướng dẫn lắp ráp theo thứ tự.
- Mô phỏng lắp ráp 2D đang được phát triển riêng.

### Giai đoạn 2 — Ứng dụng full-stack

Mục tiêu của giai đoạn này là chuyển dữ liệu tĩnh sang cơ sở dữ liệu, cung cấp REST API, quản lý tài khoản và lưu lại tiến độ lắp ráp của từng người dùng.

## Phạm vi nghiệp vụ giai đoạn 2

### Chức năng bắt buộc

- Đăng ký, đăng nhập, đăng xuất và xác định người dùng hiện tại.
- Tra cứu mô hình robot, linh kiện, các bước lắp ráp và tài nguyên kỹ thuật từ API.
- Lưu quan hệ giữa mô hình và linh kiện kèm số lượng bắt buộc.
- Tạo một phiên lắp ráp cho mô hình được chọn.
- Lưu trạng thái linh kiện đã chuẩn bị và bước lắp ráp đã hoàn thành.
- Khôi phục tiến độ khi người dùng đăng nhập lại.
- Thực hiện mô phỏng lắp ráp 2D và đồng bộ kết quả với phiên lắp ráp.
- Cung cấp chức năng quản trị nội dung cơ bản cho mô hình, linh kiện, bước lắp ráp và thư viện.

### Chưa đưa vào giai đoạn 2

- Thanh toán và mua bán linh kiện.
- Mô hình robot 3D.
- Chat, mạng xã hội hoặc hệ thống đánh giá công khai.
- Tích hợp phần cứng robot thực tế qua Bluetooth hoặc cổng nối tiếp.

Các chức năng ngoài phạm vi chỉ được bổ sung sau khi nhóm thống nhất và giảng viên xác nhận.

## Kiến trúc kỹ thuật đề xuất

Để tận dụng kiến thức JavaScript hiện có và hạn chế thay đổi giao diện, nhóm ưu tiên kiến trúc sau:

- **Frontend:** HTML5, CSS3 và JavaScript theo mô-đun.
- **Backend:** Node.js 20+ và Express.
- **Database:** MySQL 8+.
- **Giao tiếp:** REST API sử dụng JSON.
- **Xác thực:** cookie `HttpOnly` và session hoặc token được quản lý phía server; không lưu token nhạy cảm trong `localStorage`.
- **Quản lý cấu hình:** biến môi trường, không commit mật khẩu hoặc chuỗi kết nối.

Nếu môn học yêu cầu Java/Spring Boot, nhóm giữ nguyên mô hình dữ liệu và hợp đồng API bên dưới, chỉ thay lớp backend.

```text
Trình duyệt
   |
   | HTTP / JSON
   v
Express REST API
   |
   | Truy vấn có tham số
   v
MySQL
```

## Mô hình dữ liệu dự kiến

| Bảng | Mục đích |
|---|---|
| `users` | Tài khoản, mật khẩu đã băm và vai trò người dùng |
| `robots` | Thông tin mô hình, độ khó, mô tả và hình ảnh |
| `components` | Danh mục linh kiện và thông tin kỹ thuật |
| `robot_components` | Số lượng linh kiện bắt buộc của từng mô hình |
| `assembly_steps` | Các bước lắp ráp có thứ tự và dữ liệu minh họa 2D |
| `assembly_sessions` | Phiên thực hành của một người dùng với một mô hình |
| `session_components` | Trạng thái linh kiện đã chuẩn bị trong từng phiên |
| `session_steps` | Trạng thái hoàn thành của từng bước lắp ráp |
| `library_resources` | Hình ảnh, tài liệu và đường dẫn tham khảo |

Mọi thay đổi schema phải có migration và dữ liệu mẫu tương ứng. Dữ liệu trong `assets/js/data.js` sẽ được dùng làm nguồn seed ban đầu trước khi ngừng sử dụng như nguồn dữ liệu chính.

## REST API dự kiến

| Nhóm | Endpoint chính | Phụ trách |
|---|---|---|
| Xác thực | `/api/auth/register`, `/login`, `/logout`, `/me` | Tài |
| Mô hình | `/api/robots`, `/api/robots/:id` | Nhi |
| Linh kiện | `/api/components`, `/api/components/:id` | Nhi |
| Thư viện | `/api/library-resources` | Nhi |
| Quản trị nội dung | `/api/admin/robots`, `/components`, `/steps`, `/library-resources` | Nhi, Tài review |
| Phiên lắp ráp | `/api/assembly-sessions` | Tài |
| Tiến độ linh kiện | `/api/assembly-sessions/:id/components` | Tuấn Anh tích hợp |
| Tiến độ từng bước | `/api/assembly-sessions/:id/steps` | Tuấn Anh tích hợp |

API phải trả về định dạng lỗi thống nhất, kiểm tra dữ liệu đầu vào và sử dụng đúng mã trạng thái HTTP.

## Phân công giai đoạn 2

### Huỳnh Minh Tài — Backend nền tảng và tích hợp

Nhánh đề xuất: `feature/tai-backend-integration`

- Khởi tạo Express, cấu hình môi trường và cấu trúc backend.
- Xây dựng middleware lỗi, logging, CORS và quy ước response chung.
- Phát triển đăng ký, đăng nhập, đăng xuất và phân quyền người dùng/quản trị viên.
- Xây dựng API phiên lắp ráp và kết nối frontend với API dùng chung.
- Tích hợp các nhánh, review Pull Request, kiểm thử luồng tổng thể và chuẩn bị triển khai.
- Duy trì design system, header, footer và trải nghiệm nhất quán trên toàn website.

### Văn Phạm Thảo Nhi — Cơ sở dữ liệu, API nội dung và trang quản trị

Nhánh đề xuất: `feature/nhi-database-content-api`

- Thiết kế ERD, data dictionary, khóa chính/khóa ngoại và ràng buộc dữ liệu.
- Viết migration, `schema.sql`, `seed.sql` và chuyển dữ liệu mẫu từ `data.js` vào MySQL.
- Phát triển API mô hình, linh kiện, quan hệ linh kiện, bước lắp ráp và thư viện.
- Thực hiện CRUD quản trị nội dung và kiểm tra dữ liệu đầu vào.
- Chuẩn hóa mô tả kỹ thuật, đường dẫn hình ảnh và nội dung hướng dẫn.
- Viết tài liệu API và bộ dữ liệu kiểm thử cho các trường hợp hợp lệ/không hợp lệ.

Đây là nhóm công việc độc lập và có khối lượng lớn hơn giai đoạn trước, giúp cân bằng nhiệm vụ trong nhóm.

### Phạm Tuấn Anh — Tương tác lắp ráp 2D và lưu tiến độ

Nhánh đề xuất: `feature/tuananh-assembly-2d`

- Hoàn thiện vùng lắp ráp 2D bằng Canvas hoặc SVG theo thiết kế đã thống nhất.
- Xử lý kéo-thả, vị trí hợp lệ, thứ tự bước và phản hồi đúng/sai.
- Tách logic mô phỏng khỏi `main.js` để giảm conflict khi tích hợp.
- Kết nối mô phỏng với API phiên lắp ráp để lưu và khôi phục tiến độ.
- Hoàn thiện thao tác chuột, bàn phím, cảm ứng và responsive.
- Viết kiểm thử cho trạng thái bước, reset, tiếp tục và hoàn thành mô hình.

Tuấn Anh không nhận thêm CRUD hoặc xác thực trong giai đoạn đầu để tập trung vào phần tương tác khó nhất.

## Thứ tự triển khai và phụ thuộc

1. **Chốt bản web tĩnh:** merge `feature/ui-enhancement-phase-1` vào `integration/group-website`, kiểm thử và merge nhánh tích hợp vào `main`. Gắn tag `v1.0-static` để có điểm khôi phục.
2. **Chốt hợp đồng dữ liệu:** Nhi hoàn thành ERD và dữ liệu seed; cả nhóm review trước khi viết API.
3. **Khởi tạo backend:** Tài tạo Express, kết nối MySQL, middleware và cấu hình môi trường.
4. **API nội dung:** Nhi triển khai API đọc trước, sau đó bổ sung CRUD quản trị.
5. **Xác thực và phiên lắp ráp:** Tài triển khai tài khoản, phân quyền và API lưu phiên.
6. **Tích hợp mô phỏng 2D:** Tuấn Anh phát triển bằng dữ liệu mock song song, sau đó chuyển sang API thật khi hợp đồng ổn định.
7. **Kiểm thử và phát hành:** kiểm tra bảo mật, responsive, dữ liệu, luồng người dùng và tài liệu cài đặt.

Nhi và Tài phải thống nhất schema/response trước khi Tuấn Anh tích hợp lưu tiến độ. Không đổi tên field API sau khi đã chốt nếu chưa thông báo và cập nhật tài liệu.

## Cấu trúc thư mục mục tiêu

```text
robot-engine-website/
|-- index.html
|-- pages/
|-- assets/
|   |-- css/
|   |-- images/
|   `-- js/
|       |-- api.js
|       |-- main.js
|       `-- assembly-2d.js
|-- server/
|   |-- app.js
|   |-- routes/
|   |-- controllers/
|   |-- services/
|   |-- repositories/
|   `-- middleware/
|-- database/
|   |-- migrations/
|   |-- schema.sql
|   `-- seed.sql
|-- tests/
|-- .env.example
|-- package.json
`-- README.md
```

Đây là cấu trúc mục tiêu, chưa phải toàn bộ thư mục đã tồn tại ở thời điểm hiện tại.

## Quy trình Git

- Không phát triển tính năng trực tiếp trên `main`.
- Giai đoạn full-stack sử dụng nhánh tích hợp `integration/fullstack-v2`.
- Mỗi thành viên tạo nhánh riêng từ cùng một commit nền đã được chốt trên `main`.
- Mỗi Pull Request chỉ tập trung vào một mô-đun và phải mô tả schema/API bị ảnh hưởng.
- Trước khi merge phải chạy kiểm tra cú pháp, test liên quan và `git diff --check`.
- Không commit `.env`, mật khẩu, token, dữ liệu cá nhân hoặc file database cục bộ.

## Tiêu chí hoàn thành giai đoạn 2

- Project chạy được từ hướng dẫn trong README trên một máy mới.
- Database có migration và seed tái tạo được dữ liệu mẫu.
- API có validation, xử lý lỗi và không trả về mật khẩu hoặc thông tin nhạy cảm.
- Người dùng có thể đăng nhập, chọn mô hình và lưu/khôi phục tiến độ.
- Quản trị viên có thể quản lý nội dung cốt lõi.
- Mô phỏng 2D hoạt động với chuột, bàn phím và màn hình cảm ứng cơ bản.
- Giao diện hoạt động ổn định tại các mốc 375px, 768px, 1024px và 1440px.
- Không có liên kết hỏng, conflict marker hoặc lỗi JavaScript nghiêm trọng.
- Pull Request được ít nhất một thành viên khác review trước khi merge.

## Chạy phiên bản hiện tại

Mở `index.html` bằng trình duyệt hoặc dùng tính năng preview của IntelliJ IDEA. Phiên bản full-stack chưa được khởi tạo; lệnh cài đặt backend và database sẽ được bổ sung sau khi nhóm hoàn thành bước chốt kiến trúc.
