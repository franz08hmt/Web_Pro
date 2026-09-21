# Luồng demo và câu hỏi giảng viên

Tài liệu chỉ tập trung vào luồng đã tích hợp: dữ liệu → Servlet API → giao diện →
phiên lắp ráp → phòng 3D.

## Phân công

| Thành viên | Phần trình bày | File nên mở |
| --- | --- | --- |
| Nhi | Database, dữ liệu robot/linh kiện/bước/thư viện, CRUD nội dung | `database/schema.sql`, `database/seed.sql`, `dao/`, `Admin*Servlet.java` |
| Tuấn Anh | Phòng 3D, dựng linh kiện, camera và thao tác lắp ráp | `assets/js/assembly-3d-parts.js`, `assets/js/assembly-3d.js`, `pages/lap-rap-3d.html` |
| Tài | Servlet/Tomcat, auth, session, API phiên, tích hợp client-server | `AuthServlet.java`, `AssemblySessionServlet.java`, `AssemblySessionService.java`, `assets/js/api.js` |

## Kịch bản demo ngắn

1. Chạy Tomcat và mở `/api/health`; chỉ ra response JSON và trạng thái database.
2. Mở trang robot; DevTools Network cho thấy `GET /api/robots`.
3. Trong code, đi theo `RobotServlet → RobotService → RobotDao → MySQL`.
4. Đăng ký/đăng nhập; mở `/api/auth/me`, giải thích `JSESSIONID`, `HttpSession`
   và CSRF token.
5. Chọn robot, tick linh kiện; mỗi thay đổi gọi API phiên và lưu MySQL.
6. Vào phòng 3D, lắp/gỡ part; reload trang để chứng minh trạng thái được khôi phục.
7. Đăng nhập admin, CRUD một nội dung rồi quay lại trang công khai để kiểm tra.
8. Mở `/account` hoặc `/architecture` để minh họa Servlet forward tới JSP.

## Câu hỏi thường gặp

### “Controller ở đâu?”

Các class có `@WebServlet` trong `tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller`.
Ví dụ `RobotServlet` là controller cho `/api/robots`; `AccountPageServlet` là
controller forward tới JSP.

### “Client và server kết nối như nào?”

`assets/js/content-api.js` và `assets/js/api.js` dùng `fetch()` gửi HTTP tới
`/api`. Tomcat ánh xạ URL tới Servlet qua `@WebServlet`. Servlet trả JSON UTF-8;
JavaScript đọc `response.json()` và render DOM. Frontend không kết nối MySQL.

### “Request và response nằm ở đâu?”

Trong các phương thức `doGet`, `doPost`, `doPut`, `doDelete` của Servlet.
`HttpServletRequest` cung cấp path/query/body/header/session;
`HttpServletResponse` nhận status, content type và JSON qua `ApiResponses`.

### “Database đi qua lớp nào?”

Chỉ DAO mở JDBC connection và viết SQL. Chuỗi chuẩn là
`Servlet → Service → DAO → MySQL`. Mở `RobotDao` hoặc `AssemblySessionDao` để chỉ
`PreparedStatement`, tham số `?` và try-with-resources.

### “JSP được dùng ở đâu?”

`AccountPageServlet` forward tới `WEB-INF/views/account.jsp`;
`ArchitectureServlet` forward tới `architecture.jsp`. Đặt JSP dưới `WEB-INF` để
mọi request phải qua controller.

### “Đăng nhập lưu ở đâu?”

`AuthServlet` tạo `HttpSession`; Tomcat gửi cookie `JSESSIONID`. Session lưu model
`User` và CSRF token. Mật khẩu trong bảng `users` là hash PBKDF2, không lưu plain text.

### “Vì sao có service?”

Service giữ luật nghiệp vụ độc lập với HTTP và SQL. Ví dụ
`AssemblySessionService` kiểm tra component có thuộc robot, phiên có thuộc user và
trạng thái có cho phép sửa trước khi gọi DAO.

### “Dữ liệu khởi nguồn từ đâu?”

`database/seed.sql` là dữ liệu mẫu cho MySQL. Khi chạy đầy đủ, nguồn chính là
MySQL qua DAO/Servlet API. `assets/js/data.js` chỉ là snapshot dự phòng để giao
diện vẫn có nội dung minh họa khi API chưa sẵn sàng.

### “Phòng 3D lấy mô hình ở đâu?”

Three.js nằm cục bộ tại `assets/vendor/three`. Các hình học được dựng bằng code
trong `assembly-3d-parts.js`, không tải GLB bên ngoài. `assembly-3d.js` đồng bộ
part đã lắp với `/api/assembly-sessions/{id}/visual-parts/{componentId}`.

### “Tại sao không chạy index.html trực tiếp?”

File HTML chỉ là view. Auth, HttpSession, Servlet và JDBC chỉ hoạt động khi WAR
được deploy trên Tomcat. Cấu hình Tomcat nên dùng application context `/` vì
frontend gọi `/api` cùng origin.

## Chuỗi file nên mở khi bảo vệ

```text
assets/js/content-api.js
→ RobotServlet.java
→ RobotService.java
→ RobotDao.java
→ database/schema.sql
→ database/seed.sql
```

Với phiên người dùng:

```text
assets/js/api.js
→ AuthServlet.java / AssemblySessionServlet.java
→ AssemblySessionService.java
→ UserDao.java / AssemblySessionDao.java
→ HttpSession + MySQL
```
