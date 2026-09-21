# Kiến trúc Servlet/JSP và vị trí code

Tài liệu này là bản đối chiếu ngắn giữa nội dung học phần và mã nguồn. Dự án giữ
cấu trúc Java Web truyền thống: Servlet làm controller, JSP/HTML làm view, model
Java biểu diễn dữ liệu, DAO dùng JDBC truy cập MySQL.

## 1. Ánh xạ kiến thức đã học

| Nội dung | Cách áp dụng | Vị trí code |
| --- | --- | --- |
| MVC / Model 2 | Controller không viết SQL; JSP không truy cập database | `controller/`, `service/`, `dao/`, `model/`, `WEB-INF/views/` |
| Servlet | `@WebServlet`, `doGet`, `doPost`, `doPut`, `doDelete`, override `service` cho PATCH | `tomcat-app/src/main/java/.../controller/` |
| Request/Response | Đọc query/path/body từ `HttpServletRequest`; đặt status/content type và JSON vào `HttpServletResponse` | `controller/*Servlet.java`, `web/ApiResponses.java` |
| JSP/EL | Servlet đặt request attribute rồi forward vào JSP nằm dưới `WEB-INF` | `AccountPageServlet`, `ArchitectureServlet`, `WEB-INF/views/` |
| Session/Cookie | Tomcat tạo `JSESSIONID`; `HttpSession` lưu `User` và CSRF token | `AuthServlet`, `AssemblySessionServlet`, `AdminAccess` |
| Filter | Chuẩn hóa UTF-8, gắn request ID và xử lý lỗi chung | `filter/RequestContextFilter.java` |
| JDBC | Mở `Connection`, dùng `PreparedStatement`, `ResultSet`, đóng bằng try-with-resources | `dao/` |
| Maven/Tomcat | Đóng gói WAR, Servlet API do Tomcat cung cấp | `tomcat-app/pom.xml`, `WEB-INF/web.xml` |

Đường dẫn Java đầy đủ trong bảng bắt đầu từ
`tomcat-app/src/main/java/vn/edu/webpro/robotlab/`.

## 2. Luồng request/response điển hình

Ví dụ trình duyệt lấy danh sách robot:

```text
assets/js/content-api.js
  → GET /api/robots?page=1&limit=20
  → RobotServlet.doGet(request, response)
  → RobotService.list(page, limit)
  → RobotDao.list(limit, offset)
  → JDBC PreparedStatement → MySQL
  → Robot model → JSON
  → HttpServletResponse 200 application/json;charset=UTF-8
  → JavaScript render giao diện
```

Mỗi lớp chỉ giữ một trách nhiệm:

- **Controller/Servlet:** nhận HTTP, gọi service, chọn status code và response.
- **Service:** validation và quy tắc nghiệp vụ, ví dụ trạng thái phiên lắp ráp.
- **DAO:** câu SQL và ánh xạ `ResultSet` sang model.
- **Model:** dữ liệu Java và biểu diễn JSON.
- **View:** HTML/JSP/CSS/JavaScript hiển thị dữ liệu, không kết nối MySQL.

## 3. Luồng JSP

`GET /account` đi vào `AccountPageServlet`. Servlet đọc `HttpSession`; nếu chưa
đăng nhập thì redirect tới trang đăng nhập. Nếu đã đăng nhập, Servlet đặt thuộc
tính request rồi dùng `RequestDispatcher.forward()` tới
`/WEB-INF/views/account.jsp`. Vì JSP nằm dưới `WEB-INF`, người dùng không thể mở
trực tiếp file JSP và bỏ qua controller.

`GET /architecture` dùng cùng kiểu luồng qua `ArchitectureServlet` và
`architecture.jsp`; đây là trang minh họa kiến trúc ngay trong ứng dụng.

## 4. Xác thực và phiên lắp ráp

1. `AuthServlet` nhận đăng ký/đăng nhập và gọi `AuthService`.
2. `AuthService` gọi `UserDao`; mật khẩu được băm PBKDF2-HMAC-SHA256 trong
   `PasswordService`.
3. Khi thành công, `AuthServlet` tạo `HttpSession`, lưu `User` và CSRF token.
4. Trình duyệt tự gửi cookie `JSESSIONID` trong các request sau.
5. `AssemblySessionServlet` lấy user từ session; service luôn truyền `userId` vào
   DAO nên chỉ đọc/ghi phiên thuộc đúng người dùng.
6. Các request PATCH/PUT và CRUD admin phải gửi `X-CSRF-Token`.

## 5. Database

- `database/schema.sql`: tạo cấu trúc hoàn chỉnh cho database mới.
- `database/seed.sql`: dữ liệu robot, linh kiện, quan hệ, bước và thư viện.
- `database/migrations/`: thay đổi tăng dần cho database đã tồn tại.
- `DatabaseConnectionFactory`: đọc đúng năm biến `DB_*` và tạo JDBC URL UTF-8.
- Mỗi DAO dùng tham số `?` trong `PreparedStatement`, không ghép input người dùng
  trực tiếp vào SQL.

Quan hệ chính xem tại [erd.md](erd.md).

## 6. Frontend và phòng 3D

Các trang công khai vẫn là HTML/JavaScript để giữ giao diện của nhóm. JavaScript
gọi Servlet API cùng origin qua `assets/js/api.js` và `content-api.js`. Nếu API
chưa sẵn sàng, một số trang đọc snapshot `assets/js/data.js` để còn hiển thị dữ
liệu minh họa; khi chạy Tomcat/MySQL, API là nguồn chính.

Phòng 3D dùng Three.js cục bộ tại `assets/vendor/three`. Hình học robot được dựng
bằng code trong `assets/js/assembly-3d-parts.js`, còn
`assets/js/assembly-3d.js` điều khiển scene và đồng bộ trạng thái với Servlet API.

## 7. Phân công có thể trình bày

- **Nhi:** schema/seed/migration, dữ liệu robot-linh kiện-bước-thư viện, DAO và
  CRUD nội dung.
- **Tuấn Anh:** giao diện và hình học phòng lắp ráp 3D, thao tác camera/linh kiện.
- **Tài:** Servlet nền tảng, auth/HttpSession, phiên lắp ráp, tích hợp API-client,
  Tomcat và luồng tổng thể.

Khi bảo vệ, mở lần lượt `RobotServlet` → `RobotService` → `RobotDao` →
`database/schema.sql`, sau đó mở `content-api.js`. Chuỗi đó trả lời trực tiếp câu
hỏi “client và server kết nối thế nào, controller ở đâu, database đi qua đâu”.
