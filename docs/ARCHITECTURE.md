# Kiến trúc Servlet/JSP và đối chiếu với slide môn học

Dự án đi theo đúng **Model 2 (MVC)** của Chapter 2 và cách truy cập database của
Chapter 12 (sách *Murach's Java Servlets and JSP*). Tên package và tên lớp được giữ
giống slide để khi mở code có thể chỉ ngay slide tương ứng.

Đường dẫn Java bắt đầu từ `tomcat-app/src/main/java/vn/edu/webpro/robotlab/`.

## 1. Bốn tầng theo Chapter 2 slide 5

> *"The model consists of business objects like the User object. The view consists
> of HTML pages and JSPs. The controller consists of servlets. The data access layer
> consists of classes like the UserDB class..."*

| Tầng trên slide | Package trong dự án | Nội dung | Slide mẫu |
| --- | --- | --- | --- |
| Model — business objects | `business/` | JavaBean `User`, `Robot`, `Component`, `AssemblySession`… | Ch2 slide 16, Ch6 slide 6 |
| View | `pages/*.html`, `WEB-INF/views/*.jsp` | HTML/JSP, JSTL, EL | Ch2 slide 6-8, Ch6 |
| Controller | `controller/` | `XxxServlet` với `doGet/doPost/doPut/doDelete` | Ch2 slide 11-13, Ch5 |
| Data access layer | `data/` | `ConnectionPool`, `DBUtil`, `UserDB`, `RobotDB`… | Ch12 slide 35-53 |
| Lớp tiện ích | `util/` | `PasswordUtil`, `SessionUtil`, `JsonUtil`… | Ch7 (`murach.util.CookieUtil`) |

Dự án **không có tầng service**: giống `EmailListServlet`, servlet tự kiểm tra dữ
liệu rồi gọi lớp `XxxDB`; luật nghiệp vụ nằm trong chính business object (giống lớp
`Cart` của sách tự có `addItem/removeItem`).

## 2. Đối chiếu chi tiết từng kỹ thuật

| Kỹ thuật trên slide | Trong dự án |
| --- | --- |
| JavaBean: constructor rỗng, get/set, `Serializable` (Ch6 slide 6) | Mọi lớp trong `business/`. `JavaBeanRulesTest` kiểm tra tự động cả ba quy tắc |
| `@WebServlet` hoặc `web.xml` (Ch5) | `@WebServlet("/api/robots/*")`… trên từng servlet |
| `request.getParameter`, `setAttribute`, `forward` (Ch2, Ch12 slide 42-44) | `RobotCatalogPageServlet`, `ComponentCatalogPageServlet`, `AccountPageServlet` |
| EL `${user.email}` và JSTL `<c:forEach>` (Ch6, Ch12 slide 23) | `WEB-INF/views/robots.jsp`, `components.jsp`, `account.jsp` |
| `HttpSession` (Ch7) | `util/SessionUtil`: lưu `user` và `csrfToken` trong session |
| `context.xml` khai báo connection pool (Ch12 slide 34) | `tomcat-app/src/main/webapp/META-INF/context.xml` |
| Lớp `ConnectionPool` (Ch12 slide 35-37) | `data/ConnectionPool`: `getInstance()`, `getConnection()`, `freeConnection()` |
| `PreparedStatement` với `?` chống SQL injection (Ch12 slide 17-20) | Mọi câu SQL trong `data/`. Không có câu nào nối chuỗi từ dữ liệu người dùng |
| Lớp `UserDB` với method `static` (Ch12 slide 45-51) | `data/UserDB`: `insert`, `update`, `emailExists`, `selectUser`… cùng tên với slide |
| Lớp `DBUtil` đóng tài nguyên trong `finally` (Ch12 slide 52-53) | `data/DBUtil`, dùng trong khối `finally` của mọi method `XxxDB` |

## 3. Một request đi qua các tầng thế nào

### Trang dựng ở server (luồng truyền thống)

```text
Trình duyệt gửi GET /components?page=2
  → ComponentCatalogPageServlet.doGet(request, response)        controller
  → request.getParameter("page")
  → ComponentDB.selectComponents(limit, offset)                 data access layer
      → ConnectionPool.getInstance().getConnection()
      → PreparedStatement "... LIMIT ? OFFSET ?" → ResultSet
      → mỗi dòng tạo một JavaBean Component bằng setter         model
      → finally: DBUtil.closeResultSet, closePreparedStatement, freeConnection
  → request.setAttribute("components", components)
  → getServletContext().getRequestDispatcher(url).forward(...)
  → components.jsp dùng <c:forEach> và ${component.name}        view
```

### API cho JavaScript

```text
assets/js/content-api.js gửi GET /api/robots?page=1&limit=20
  → RobotServlet.doGet → RobotDB.selectRobots → List<Robot>
  → robot.toJson() → HttpServletResponse 200 application/json
```

Hai kiểu dùng chung tầng `data/` và `business/`, chỉ khác view: `/components` trả
**HTML dựng ở server**, `/api/components` trả **JSON** cho các trang trong `pages/`.

### Đăng ký — theo đúng thứ tự của EmailListServlet (Ch12 slide 43-44)

```text
AuthServlet.register
  → // get parameters from the request       fullName, email, password
  → // validate the parameters               ValidationUtil, UserDB.emailExists(email)
  → // store data in User object and save    new User(); UserDB.insert(user, hash)
  → SessionUtil.startSession                 session.setAttribute("user", user)
```

Mật khẩu được băm bằng `PasswordUtil.hashPassword` (PBKDF2, có salt) trước khi lưu.
Tài khoản đăng ký luôn có role `user`: câu `INSERT` trong `UserDB.insert` ghi cứng
giá trị này, không nhận role từ request.

## 4. Phiên đăng nhập và phân quyền

1. Đăng nhập thành công, `SessionUtil.startSession` tạo `HttpSession` mới, lưu
   JavaBean `User` và một CSRF token. Tomcat gửi cookie `JSESSIONID`.
2. Mỗi request cần đăng nhập gọi `SessionUtil.getCurrentUser`, đọc lại `User` từ
   `UserDB` và so `session_version`. Đổi mật khẩu hoặc đổi quyền làm tăng
   `session_version`, nên phiên cũ hết hiệu lực ngay.
3. Trang quản trị gọi `SessionUtil.requireAdmin`; thao tác ghi còn phải gửi header
   `X-CSRF-Token` khớp với token trong session.
4. `User.canChangeRoleOf(target)` giữ luật: chỉ ADMIN được đổi quyền, và không được
   tự đổi quyền của mình.

## 5. Phiên lắp ráp

`AssemblySession` là JavaBean tự giữ luật của mình:

- `canChangeStatusTo(target)`: chỉ cho `READY → IN_PROGRESS → COMPLETED`, và
  `ABANDONED` khi chưa hoàn thành.
- `getExpectedPreparationStatus()`: đủ mọi linh kiện bắt buộc thì `READY`.
  Trình duyệt chỉ gửi "tick linh kiện nào", không tự khai báo được là đã đủ.
- `getProgressPercent()`: tính lại từ dữ liệu thật mỗi lần trả về.

`AssemblySessionServlet` hỏi các method trên rồi gọi `AssemblySessionDB`. Mọi câu
SQL của phiên đều kèm `user_id`, nên không đọc hay sửa được phiên của người khác.

## 6. Những chỗ cố ý khác slide

| Slide | Dự án | Lý do |
| --- | --- | --- |
| `UserDB` bắt `SQLException`, in ra rồi trả `null`/`0` | Method khai báo `throws SQLException` | Servlet trả mã 503 "database chưa sẵn sàng" thay vì báo nhầm "không tìm thấy" hay "sai mật khẩu" |
| `ConnectionPool.getConnection()` trả `null` khi lỗi | Ném `SQLException` | Tránh `NullPointerException` ở `connection.prepareStatement(...)` |
| `context.xml` ghi thẳng user/password | Dùng `${DB_USER}`… lấy từ VM options | Không commit mật khẩu database lên Git |
| `maxActive`, `maxWait` | `maxTotal`, `maxWaitMillis` | Tomcat 9 dùng DBCP2; đây là tên mới của cùng thuộc tính |
| Driver `com.mysql.jdbc.Driver` | `com.mysql.cj.jdbc.Driver` | Tên lớp driver của MySQL Connector/J 8 trở lên |
| Servlet trả trang HTML | Nhiều servlet trong `/api` trả JSON | Các trang trong `pages/` dùng JavaScript (phòng 3D, tick linh kiện) nên cần dữ liệu JSON |

## 7. Database

- `database/schema.sql`: cấu trúc cho database mới (gồm đủ các migration).
- `database/seed.sql`: dữ liệu robot, linh kiện, quan hệ, bước và thư viện.
- `database/migrations/`: thay đổi tăng dần cho database đã tồn tại.

Quan hệ các bảng xem tại [erd.md](erd.md).
