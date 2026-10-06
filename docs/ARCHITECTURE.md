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
| Hidden field mang dữ liệu qua form (Ch7 slide 35-37) | `csrfToken`, `id`, `action`, `expectedVersion` trong form nhiệm vụ, chẩn đoán, nối dây, hỗ trợ |
| `c:out` chống XSS (Ch9 slide 7-8) | Mọi chuỗi từ DB hoặc người dùng trong JSP |
| Include file (Ch6 slide 27-33) | `workspace-header.jspf`, `workspace-footer.jspf`, `wiring-diagram.jsp` bằng `<%@ include file="..." %>` |
| Luật nghiệp vụ trong bean, getter định dạng (Ch9 slide 28-33) | `TaskRubric`, `WiringExercise.grade`, `AssemblySession`; getter `...Display` |
| `sendRedirect` (Ch5 slide 25-26) | Khách về trang tài khoản; PRG sau mọi POST thành công |

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
| Không có filter | `RequestContextFilter` (`@WebFilter`) | Gom việc chung: UTF-8, `X-Request-Id`, bắt lỗi 500; không chứa nghiệp vụ |
| Truy vấn đơn lẻ | `setAutoCommit(false)`, `commit`, `rollback`, `SELECT ... FOR UPDATE` | Nhiều bảng phải cùng thành công; khoá dòng để request đồng thời tuần tự hoá (khái niệm transaction ở Ch13 slide 29-34) |
| `Date`, `DateFormat` | `Date` + `SimpleDateFormat` + `TimeZone` | Cần mẫu `dd/MM/yyyy HH:mm` và múi giờ Việt Nam; cùng họ `java.text` |
| Tiền, điểm bằng `double` | `BigDecimal` + `RoundingMode.HALF_UP` | Làm tròn chính xác một lần ở kết quả cuối |
| Custom tag (Ch10) | Không dùng | Thay bằng include và getter của bean |
| JPA (Ch13), JavaMail (Ch14) | Không dùng | Theo Chapter 12 dùng JDBC; không có email thông báo |

## 7. Database

- `database/schema.sql`: cấu trúc cho database mới (gồm đủ các migration).
- `database/seed.sql`: dữ liệu robot, linh kiện, quan hệ, bước và thư viện.
- `database/migrations/`: thay đổi tăng dần cho database đã tồn tại.

Quan hệ các bảng xem tại [erd.md](erd.md).

## 8. Cửa hàng mô phỏng: request/response và database

```text
pages/cua-hang.html / gio-hang.html
  → assets/js/api.js dùng fetch, cookie same-origin và X-CSRF-Token khi ghi
  → ShopServlet / CartServlet / OrderServlet / AdminShopServlet
  → ShopProduct, CartItem, ShopOrder (JavaBean)
  → ShopProductDB / CartDB / OrderDB (PreparedStatement + ConnectionPool)
  → shop_products, cart_items, orders, order_items trong MySQL
```

Catalog chỉ đọc sản phẩm active; `shop_products.component_id` liên kết tới
`components`, giữ riêng dữ liệu giá/tồn và dữ liệu học tập. Cart và lịch sử được
scope bởi user từ `HttpSession`, không bởi ID client. Admin ghi qua servlet có
`requireAdmin` và CSRF.

Checkout là ví dụ transaction nhiều bảng để trình bày: khóa user và cart/product,
kiểm tra trạng thái và tồn, tính tổng bằng giá DB, giảm kho, insert order cùng
snapshot từng item, xóa cart rồi commit. Nếu bước nào lỗi thì rollback; hai
request dùng chung giỏ được tuần tự hóa. Snapshot tên/giá giữ lịch sử đúng thời
điểm đặt kể cả khi admin đổi giá về sau. Trang `/order-history` do
`OrderHistoryServlet` đọc `OrderDB`, `setAttribute` rồi forward tới JSP/JSTL;
chỉ đọc đơn của phiên hiện tại. Tất cả tiền đều là dữ liệu mô phỏng, không thu
tiền thật.

## 9. Hồ sơ học tập (Đợt 5, 5b)

```text
GET /learning-profile
  → LearningProfileServlet: SessionUtil.getCurrentUser, Cache-Control: no-store
  → RobotDB.selectAllRobots, StatsDB.selectLearningProfile* (mọi câu SQL có user_id = ?)
  → LearningProfile.buildProfile()   luật chọn lượt tốt nhất theo tỷ lệ, điểm trung bình HALF_UP, kỹ năng có căn cứ
  → forward → learning-profile.jsp   (@media print: A4, nền trắng, không menu)
```

Không thêm bảng. Ngày giờ do bean định dạng bằng `SimpleDateFormat` múi giờ `Asia/Ho_Chi_Minh`.
Đợt 5b viết lại bean bằng vòng `for`, `ArrayList`, `HashMap`, `Date` cho đúng kỹ thuật slide.

## 10. Nhiệm vụ thực hành và chẩn đoán (Đợt 6)

Dùng **form POST** thay API JSON để bám Ch5, Ch7 (hidden field), Ch9 (JSTL):

```text
POST /tasks  action=confirm
  → TaskServlet.doPost
      ├─ SessionUtil.hasValidFormCsrfToken      hidden csrfToken, sai → 403
      ├─ TaskSubmissionDB.submit                setAutoCommit(false)
      │     khoá theo thứ tự: nhiệm vụ → người được giao → vòng → phiên lắp ráp  (FOR UPDATE)
      │     kiểm: được giao, còn lượt, đúng robot, phiên đã COMPLETED, bằng chứng đúng mốc
      │     TaskRubric tính điểm tự động (BigDecimal, HALF_UP)  → INSERT bài nộp (snapshot) → commit
      └─ sendRedirect (PRG)
```

- **Vòng nộp:** mỗi người được giao có vòng 1, 2…; chỉ tính lượt quiz/chẩn đoán đầu của vòng; kết luận
  "cần bổ sung" mở vòng mới. Lần chấm là bản ghi mới, không sửa bản cũ.
- **Rubric:** mẫu A (lắp ráp 40, quiz 40, giải thích 20) và mẫu B (30, 25, 25, 20). Hằng số nằm trong
  `TaskRubric`; bài đã công bố bất biến, muốn đổi thì nhân bản.
- **Chẩn đoán:** quan sát chỉ trả sau khi server ghi nhận lượt chọn phép kiểm tra
  (`diagnosis_attempt_checks`); điểm quá trình `4 × số phép cần thiết đã làm / tổng` + nguyên nhân 3 + xử lý 3.
- **Vì sao snapshot:** bài nộp giữ id nguồn **và** bản chụp (tên robot, điểm quiz, điểm chẩn đoán) nên
  lịch sử không đổi khi nội dung gốc đổi.

## 11. Phòng nối dây và hỗ trợ (Đợt 7)

```text
wiring.js (nhấn hai chân / kéo thả / bàn phím)  chỉ tạo danh sách cặp "pair=idA:idB" trong form
  → POST /wiring  action=save | submit  (kèm csrfToken, expectedVersion)
  → WiringServlet → WiringDB.mutate     khoá bài rồi lượt FOR UPDATE; kiểm chủ lượt, trạng thái, version
  → WiringExercise.normalize            cặp không hướng: A–B = B–A, bỏ dây trùng, tối đa 80 dây
  → WiringExercise.grade → WiringGrade  N, C, W, M; điểm = HALF_UP(100 × max(C − W, 0) / N, 1)
  → UPDATE ... version = version + 1, state = 'SUBMITTED' ; commit ; sendRedirect
```

- Tắt JavaScript vẫn dùng được (hai nhóm radio chọn chân, nút thêm/xoá/lưu/nộp).
- Hai tab cùng lưu một version: tab đến sau nhận 422, không ghi đè.
- Hỗ trợ Admin–User: `WiringSupportDB` dựng **bản chụp từ lượt đã lưu trên server**; mỗi lượt chỉ có một
  yêu cầu OPEN/ANSWERED; tin nhắn đã lưu bất biến. ADMIN không sửa dây, điểm hay nộp thay.
- Đây là luyện tập có tài liệu, chỉ chấm cặp nối trực tiếp trong bài mẫu; không mô phỏng điện.

## 12. Khung giao diện dùng chung

Mọi JSP trong `WEB-INF/views` (34 trang) gắn `workspace-header.jspf` và `workspace-footer.jspf`
bằng include directive; kiểu dáng ở `assets/css/learning-workspace.css` (nền charcoal, nhấn cam,
Poppins, Heroicons cục bộ). Header chỉ hiện liên kết theo vai trò bằng `c:choose` trên
`sessionScope.user.admin`. Trang hồ sơ học tập vẫn in A4 nền trắng bằng `@media print`.

## 13. Kiểm thử (06/10/2026)

| Loại | Số lượng | Vai trò |
| --- | --- | --- |
| Node (`tests/server/*.test.cjs`) | 21 file, 131 test | Kiểm nguồn: cấu trúc môn học, cấm kỹ thuật ngoài slide, form/CSRF/no-store, hợp đồng giao diện |
| JUnit | 12 lớp, 79 test | Luật nghiệp vụ (rubric, nối dây, công bố), `JavaBeanRulesTest`, `JspExpressionContractTest` |

Node và JUnit chỉ là công cụ kiểm tra khi phát triển, không phải runtime của website.

## 14. Số lớp theo package

| Package | Số file | Ghi chú |
| --- | --- | --- |
| `business/` | 45 | JavaBean + luật nghiệp vụ |
| `controller/` | 37 | `@WebServlet`; 18 forward sang JSP, 19 trả JSON |
| `data/` | 22 | `ConnectionPool`, `DBUtil`, `DatabaseLifecycleListener` và các `XxxDB` |
| `util/` | 8 | `SessionUtil`, `PasswordUtil`, `JsonUtil`, `ResponseUtil`, `ValidationUtil`, các `...FormUtil` |
| `filter/` | 1 | `RequestContextFilter` (ngoài slide) |
