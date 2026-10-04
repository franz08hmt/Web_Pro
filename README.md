# Robot Assembly Lab

Ứng dụng Web Java giúp người học chọn mô hình robot, tra cứu linh kiện, thực hiện
các bước lắp ráp và lưu tiến độ. Runtime duy nhất của dự án là **Java Servlet/JSP
trên Tomcat 9**; Node.js không chạy backend.

## Kiến trúc theo Model 2 (MVC) của học phần

Chia tầng đúng như Chapter 2 slide 5 và Chapter 12 (sách *Murach's Java Servlets and JSP*):

```text
Browser — view: HTML/JSP
        │ HTTP request
        ▼
Servlet — controller (package controller)
        │ kiểm tra dữ liệu, gọi lớp XxxDB
        ▼
XxxDB + ConnectionPool + DBUtil — data access layer (package data)
        │ PreparedStatement
        ▼
MySQL
```

Dữ liệu đi giữa các tầng bằng JavaBean trong package `business` (model).

- Java 17, Servlet API 4.0.1 (`javax.servlet`), JSP, JSTL và Tomcat 9.
- Connection pool khai báo trong `META-INF/context.xml` như Chapter 12 slide 34.
- `HttpSession`/cookie `JSESSIONID` giữ trạng thái đăng nhập; request ghi dùng CSRF.
- `/robots` và `/components` là trang JSP do servlet dựng sẵn ở server: `doGet` đọc
  tham số, gọi `RobotDB`/`ComponentDB`, `setAttribute` rồi `forward` sang JSP dùng
  JSTL. `/account` và `/architecture` cũng đi theo luồng này.
- REST API trả JSON cho phần giao diện HTML/JavaScript trong `pages/`.
- Three.js được lưu tại `assets/vendor/three`, không phụ thuộc `node_modules` khi chạy.

Giải thích chi tiết và vị trí code: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Thư mục cần biết

```text
tomcat-app/
  pom.xml
  src/main/java/vn/edu/webpro/robotlab/
    business/     JavaBean: User, Robot, Component, AssemblySession...
    controller/   Servlet nhận request, trả JSON hoặc forward sang JSP
    data/         ConnectionPool, DBUtil và UserDB, RobotDB... (JDBC + SQL)
    util/         PasswordUtil, SessionUtil, JsonUtil, ValidationUtil...
    filter/       UTF-8, request context, xử lý lỗi chung
  src/main/webapp/META-INF/context.xml   Connection pool jdbc/robotlab
  src/main/webapp/WEB-INF/views/         JSP không truy cập trực tiếp
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
   `schema_migrations` (`database/migrations/00N_*.sql`, theo đúng thứ tự số).
   Migration là lệnh DDL nên phải chạy bằng tài khoản MySQL có quyền
   `CREATE`/`ALTER` (ví dụ `root`), không phải tài khoản ứng dụng. Sau khi đã có
   migration `006_quiz`, chạy thêm `database/seed-quiz.sql`; sau migration
   `007_troubleshooting`, chạy thêm `database/seed-troubleshooting.sql`. Cả hai
   đều an toàn chạy trên database đang dùng — chỉ INSERT bảng `quiz_*` hoặc
   `troubleshooting_guides`, không đụng dữ liệu người dùng. Để thêm hai mẫu
   `line-obstacle` và `servo-scout`, sau `seed.sql` chạy lần lượt
   `database/seed-robots-phase3.sql`, `database/seed-quiz-phase3.sql` (sau khi
   có bảng quiz từ migration `006_quiz`) và
   `database/seed-troubleshooting-phase3.sql` (sau migration
   `007_troubleshooting`). Các seed đợt 3 chỉ dùng `INSERT IGNORE`, không thêm
   bảng/cột và có thể chạy lại an toàn. Với database cũ, sau `007_troubleshooting`
   chạy migration `008_shop_cart_orders.sql`; sau đó chạy `database/seed-shop.sql`.
   Database mới nhận bốn bảng cửa hàng từ `schema.sql`, rồi chạy cùng seed cửa hàng.
   Seed chỉ thêm 12 sản phẩm mô phỏng gắn với `components`, không đụng giỏ, tài
   khoản hay lịch sử đơn đã có.
2. Trong cấu hình Tomcat của IntelliJ, tab **Server**, ô **VM options**, nhập:

   ```text
   -DDB_HOST=127.0.0.1 -DDB_PORT=3306 -DDB_NAME=ten_database -DDB_USER=ten_user -DDB_PASSWORD=mat_khau
   ```

   `META-INF/context.xml` đọc năm giá trị này qua `${DB_HOST}`…, nên mật khẩu
   không nằm trong Git. Tomcat không tự đọc file `.env`.
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
`/pages/admin-content.html`; quản trị sản phẩm demo tại `/pages/admin-shop.html`.

## Cửa hàng mô phỏng

- `GET /pages/cua-hang.html`: catalog công khai với giá tham khảo/tồn kho.
- `/pages/gio-hang.html`: xem và chỉnh giỏ cần đăng nhập; checkout tạo đơn
  `CONFIRMED` trong database sau khi server kiểm tra tồn kho và tính lại tổng.
- `/order-history`: Servlet forward sang JSP, chỉ hiển thị đơn của tài khoản hiện
  tại và tên/giá được chụp tại lúc đặt.
- Không có cổng thanh toán, dữ liệu thẻ, thu tiền hoặc giao hàng thật.

## Hồ sơ học tập cá nhân có thể in

- `/learning-profile`: hồ sơ chỉ đọc của tài khoản đăng nhập, gồm tiến độ năm
  mẫu robot, lượt quiz và kỹ năng có căn cứ từ các bảng hiện có.
- `LearningProfileServlet` lấy user từ `HttpSession`, gọi `RobotDB`/`StatsDB`,
  dựng JavaBean rồi forward sang JSP. Response dùng `Cache-Control: no-store`;
  không nhận chủ sở hữu từ query string.
- Nút **In / Lưu PDF** gọi hộp thoại in của trình duyệt. Chọn khổ A4 và
  **Lưu dưới dạng PDF**, nên tắt **Đầu trang và chân trang** để tránh thêm ngày
  giờ/tiêu đề của trình duyệt. Số trang do CSS tạo phụ thuộc trình duyệt;
  không có dịch vụ hoặc thư viện xuất PDF riêng.
- Đợt 5b giữ kết quả/giao diện/SQL và viết luật bean bằng vòng `for`, `if/else`,
  `ArrayList`/`HashMap`; ngày giờ dùng `Date` và `SimpleDateFormat` tạo riêng mỗi
  lần theo giờ Việt Nam. JSP nhận các getter boolean trạng thái, Servlet dùng
  `String url` + forward theo slide. Xem bảng chương/slide và Hỏi–Đáp trong
  [hướng dẫn demo](docs/TEAM_FLOW_DEMO_GUIDE.md#hồ-sơ-học-tập-bám-slide-nào).

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
