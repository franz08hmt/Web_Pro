# Robot Assembly Lab

Ứng dụng Web Java giúp người học chọn mô hình robot, tra cứu linh kiện, thực hiện
các bước lắp ráp và lưu tiến độ. Runtime duy nhất của dự án là **Java Servlet/JSP
trên Tomcat 9**; Node.js không chạy backend.

## Kiến trúc đúng theo học phần

```text
Browser (HTML/CSS/JavaScript hoặc JSP)
        │ HTTP request / JSON
        ▼
Servlet Controller
        ▼
Service (nghiệp vụ, validation, trạng thái)
        ▼
DAO (JDBC + PreparedStatement)
        ▼
MySQL
```

- Java 17, Servlet API 4.0.1 (`javax.servlet`), JSP và Tomcat 9.
- Maven đóng gói WAR; MySQL Connector/J nằm trong `WEB-INF/lib`.
- `HttpSession`/cookie `JSESSIONID` giữ trạng thái đăng nhập; request ghi dùng CSRF.
- `/robots` và `/components` là trang JSP do Servlet dựng sẵn ở server: doGet đọc
  tham số, gọi Service/DAO lấy dữ liệu MySQL, `setAttribute` rồi `forward` sang
  JSP dùng JSTL. `/account` và `/architecture` cũng đi theo luồng này.
- REST API trả JSON cho phần giao diện HTML/JavaScript trong `pages/`.
- Three.js được lưu tại `assets/vendor/three`, không phụ thuộc `node_modules` khi chạy.

Giải thích chi tiết và vị trí code: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Thư mục cần biết

```text
tomcat-app/
  pom.xml
  src/main/java/vn/edu/webpro/robotlab/
    controller/   Servlet nhận request, trả response/forward JSP
    service/      Nghiệp vụ và validation
    dao/          JDBC, SQL và ánh xạ dữ liệu
    model/        Model Java
    filter/       UTF-8, request context, xử lý lỗi chung
  src/main/webapp/WEB-INF/views/   JSP không truy cập trực tiếp
assets/           CSS, JavaScript, ảnh và Three.js cục bộ
pages/            Các view HTML phía client
database/         Schema, seed và migration MySQL
docs/             Kiến trúc, API, ERD và kịch bản thuyết trình
tests/server/     Kiểm tra tĩnh tùy chọn cho frontend/cấu hình WAR
```

## Chạy bằng IntelliJ + Tomcat

Yêu cầu: JDK 17, Tomcat 9 và MySQL 8.

1. Tạo database UTF-8, chọn database đó rồi chạy `database/schema.sql` và
   `database/seed.sql`. Với database cũ, chỉ chạy migration chưa có trong
   `schema_migrations`; trước khi chạy WAR mới phải áp dụng
   `database/migrations/004_account_session_version.sql` nếu chưa có.
2. Trong cấu hình Tomcat, tab **Startup/Connection → Run → Environment Variables**,
   nhập đủ `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` theo
   `.env.example`. Tomcat không tự đọc `.env`.
3. Trong **Deployment**, thêm artifact `robot-assembly-lab-tomcat:war exploded`
   và đặt **Application context** là `/` vì frontend gọi API cùng origin tại `/api`.
4. Chạy cấu hình Tomcat, không chạy `index.html` và không dùng `npm run dev`.
5. Mở `http://localhost:8080/`. Kiểm tra kết nối tại
   `http://localhost:8080/api/health`.

Có thể build ngoài IntelliJ bằng:

```powershell
mvn -f tomcat-app/pom.xml clean package
```

WAR được tạo tại `tomcat-app/target/robot-assembly-lab.war`.

## Tài khoản quản trị

Đăng ký trên giao diện tạo tài khoản thường. Sau đó cập nhật quyền trong MySQL:

```sql
UPDATE users
SET role = 'admin', session_version = session_version + 1
WHERE email = 'admin@example.com';
```

Đây chỉ là bước bootstrap quản trị viên đầu tiên. Các lần phân quyền sau dùng
`/pages/admin-users.html` (yêu cầu role ADMIN và CSRF); các phiên cũ của tài khoản
được đổi quyền sẽ hết hiệu lực. Trang quản trị nội dung là
`/pages/admin-content.html`.

## Kiểm tra

Kiểm tra chính là build Maven:

```powershell
mvn -f tomcat-app/pom.xml clean package
```

Nếu máy có Node.js 20, có thể chạy thêm các kiểm tra tĩnh; đây không phải runtime:

```powershell
npm test
```

## Tài liệu bảo vệ bài

- [Kiến trúc và luồng request/response](docs/ARCHITECTURE.md)
- [Quy ước và danh sách API](docs/API_CONVENTIONS.md)
- [ERD](docs/erd.md)
- [Luồng demo và câu hỏi giảng viên](docs/TEAM_FLOW_DEMO_GUIDE.md)
