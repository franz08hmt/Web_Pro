# Ôn tập vấn đáp cuối kỳ — Robot Assembly Lab (một file duy nhất)

File này gom **toàn bộ nội dung để học**: kiến thức chapter, luồng code, phân công từng người,
câu hỏi–đáp và lịch ôn. Chỉ cần đọc file này; các file `.md` khác trong `docs/` là tài liệu của
project, chỉ mở khi muốn tra cứu sâu.

Số liệu lấy từ mã nguồn ngày 06/10/2026 (nhánh `integration/fullstack-v2`). Số slide đối chiếu với
nội dung thật trong `D:\Web-Pro\Chapter NN slides.pptx`; chỗ nào không có slide thì ghi "ngoài slide".

## Lộ trình học

| Thời gian | Học gì |
| --- | --- |
| 30 phút | Mục 1 (giới thiệu), mục 3 (ba luồng), mục 4 (ba khuôn code) |
| 2 giờ | Thêm mục 2 (chapter → code), mục 6 (khái niệm), phần của mình ở mục 7 |
| Đầy đủ | Thêm mục 5, mục 8 (47 câu hỏi–đáp, tự trả lời to trước khi xem đáp án), mục 10–11 |

Khi cần xem sâu hơn trong project: `docs/ARCHITECTURE.md` (đối chiếu kỹ thuật với slide),
`docs/TEAM_FLOW_DEMO_GUIDE.md` (kịch bản demo, tài khoản demo), `docs/erd.md` (quan hệ bảng),
`docs/API_CONVENTIONS.md` (route, quyền, mã lỗi), `docs/PHASE5B_SLIDE_AUDIT.md` (chỗ chưa bám slide).

## 1. Giới thiệu dự án trong 60 giây

> Robot Assembly Lab là website học lắp ráp robot. Người học chọn mẫu robot, chuẩn bị
> linh kiện, lắp ráp trong phòng 3D, làm quiz, luyện chẩn đoán lỗi, nối dây, và nhận
> nhiệm vụ do Admin giao. Điểm, trạng thái và quyền đều do **server** quyết định.
> Server là Tomcat 9 chạy Servlet/JSP, dữ liệu ở MySQL qua JDBC. Code chia bốn tầng
> theo Chapter 2: `business` (JavaBean), `controller` (Servlet), `data` (XxxDB),
> JSP là view.

Con số nên nhớ (06/10/2026):

| Thành phần | Số lượng |
| --- | --- |
| Servlet (`controller/`) | 37 |
| JavaBean (`business/`) | 45 lớp |
| Lớp truy cập dữ liệu (`data/`) | 22 (gồm `ConnectionPool`, `DBUtil`, `DatabaseLifecycleListener`) |
| Lớp tiện ích (`util/`) | 8; thêm 1 filter `RequestContextFilter` |
| JSP dưới `WEB-INF/views` | 35 trang + 2 file include `.jspf` |
| Trang HTML tĩnh `pages/` | 16 |
| Bảng MySQL | 37 (12 migration, từ 001 đến 012) |
| Kiểm thử | 21 file Node (kiểm nguồn) và 12 lớp JUnit |

Công nghệ: Java 17, Tomcat 9 (`javax.servlet`), JSP/JSTL, MySQL 8, JDBC. Không có
Spring, ORM, JPA hay thư viện JSON.

## 2. Chapter → kiến thức → nơi dùng trong dự án

Cột "Slide" là số slide có nội dung đó. Hàng in nghiêng là chapter dự án **không dùng**;
phải biết giải thích vì sao.

| Chapter | Kiến thức trong slide | Slide | Dự án dùng ở đâu |
| --- | --- | --- | --- |
| 2 | Model 1 và Model 2 (MVC); thuật ngữ model/view/controller; lớp `User`; loại file trong MVC | 3–5, 16–20 | Bốn tầng `business`, `controller`, `data`, JSP. Xem `docs/ARCHITECTURE.md` mục 1 |
| 5 | Servlet và `@WebServlet`/`web.xml`; GET/POST, `getParameter`, `getParameterValues`; `setAttribute`; `forward`; `sendRedirect`; validate trong `doPost`; vòng đời servlet và vì sao không dùng biến instance | 6–11, 12–17, 21–26, 30–31, 46–50 | 37 servlet `@WebServlet`; forward bằng `String url = "..."; getRequestDispatcher(url).forward(...)`; `sendRedirect` đưa khách về trang tài khoản và dùng cho PRG; **không servlet nào có biến instance thay đổi** (đã kiểm) |
| 6 | JavaBean (ctor rỗng, get/set, `Serializable`); EL hiển thị attribute/property; JSTL; 5 loại thẻ JSP; thẻ chuẩn `useBean`; **include** (compile-time/run-time) | 4–9, 12–13, 14–16, 23–25, 27–33 | 45 JavaBean (`JavaBeanRulesTest` kiểm cả ba quy tắc); `<%@ include file="workspace-header.jspf" %>` và `wiring-diagram.jsp` dùng chung; không scriptlet |
| 7 | Session tracking, `request.getSession()`, `setAttribute/getAttribute`; truy cập session an toàn luồng (`synchronized`); cookie; URL rewriting và **hidden field** | 4–16, 17–31, 32–37 | `SessionUtil` lưu `user` và `csrfToken` trong `HttpSession`; cookie `JSESSIONID` do Tomcat quản lý (không tự dùng `Cookie`); hidden field mang `csrfToken`, `id`, `action`, `expectedVersion` trong mọi form |
| 8 | EL: truy cập property, phạm vi (scope), `[ ]`, toán tử `== eq`, `empty`, `and or not`, `? :`, implicit object | 3–39 | `${profile.fullName}`, `${empty tasks}`, `${recipient.canSubmit}`, `${sessionScope.user.admin}` trong header; tránh tên biến trùng implicit object (`header`, `param`, `cookie`) |
| 9 | JSTL: `c:out` chống XSS, `c:forEach`, `c:if`, `c:choose/when/otherwise`, `c:set`; ứng dụng giỏ hàng với `Product`, `LineItem`, `Cart` | 3–25, 28–33 | `c:out` cho mọi chuỗi từ DB hoặc người dùng; luật nghiệp vụ nằm trong bean như `Cart.addItem`; getter định dạng sẵn kiểu `getPriceCurrencyFormat()` → `getAutomaticDisplay()`, `getCompletionDate()` |
| *10* | *Custom tag, TLD, `TagSupport`* | 3–42 | **Không dùng.** Thay bằng include + getter của bean. Chỉ mượn ví dụ `DateFormat` ở slide 6 để định dạng ngày |
| 12 | JDBC: driver, URL, `ResultSet`, `executeUpdate`, `PreparedStatement`, connection pool, `context.xml`, `UserDB`, `DBUtil`, `EmailListServlet` | 4–53 | `ConnectionPool` (JNDI `jdbc/robotlab`), `DBUtil`, 22 lớp `XxxDB` method `static`, đóng tài nguyên trong `finally`; seed bằng SQL |
| *13* | *JPA, entity, `EntityManager`, transaction* | 4–39 | **Không dùng JPA** (đã chọn JDBC theo Chapter 12). Chỉ mượn khái niệm transaction (29–34) để giải thích `setAutoCommit(false)/commit/rollback` |
| *14* | *JavaMail* | 3–28 | **Không có chức năng gửi mail** (email thông báo nằm ngoài phạm vi). Ôn riêng theo `D:\Web-Pro\ON_TAP_GIUA_KY_WEB_2026.md` |
| 18 | HTTP request/response, mã trạng thái, header, MIME | cả chapter | Mã 200, 201, 302, 403, 404, 422, 503; header `Cache-Control: no-store`, `Location`, `Content-Type`, `X-CSRF-Token`; PRG |

Việc nên biết khi bị hỏi "ngoài slide":

| Thứ dự án dùng | Có trong slide? | Cách nói |
| --- | --- | --- |
| `RequestContextFilter` (`@WebFilter`) | Không | "Gom việc chung: encoding UTF-8, mã `X-Request-Id`, bắt lỗi 500; không chứa nghiệp vụ." |
| API JSON và `JsonUtil` tự viết | Không | "Phòng 3D, tick linh kiện cần dữ liệu không tải lại trang. Dùng chung tầng `data/` và `business/`, chỉ khác view." |
| `setAutoCommit(false)`, `FOR UPDATE` | Khái niệm ở Ch13/29–34 | "Nhiều bảng phải cùng thành công hoặc cùng huỷ; khoá dòng để hai request cùng lúc không tạo trùng." |
| `BigDecimal`, `SimpleDateFormat` | `Date`, `DateFormat`, `NumberFormat` có (Ch9/28–31, Ch10/6) | "Cùng họ `java.text`; cần mẫu `dd/MM/yyyy` và múi giờ Việt Nam; `BigDecimal` để làm tròn HALF_UP chính xác." |
| Lambda, stream, `switch ->` ở ~11 chỗ code cũ | Không | Phải nói thật; danh sách ở `docs/PHASE5B_SLIDE_AUDIT.md` (trong project). Code mới từ Đợt 5 đã bám slide |

## 3. Ba luồng phải vẽ được trên bảng

### 3.1 Trang dựng ở server (Model 2 chuẩn)

```text
GET /learning-profile
  → LearningProfileServlet.doGet                                 controller
      ├─ SessionUtil.getCurrentUser(request)    chưa đăng nhập → sendRedirect(/pages/tai-khoan.html)
      ├─ RobotDB.selectAllRobots()                               data
      ├─ StatsDB.selectLearningProfileSessionStats(user.getId()) mọi câu SQL có user_id = ?
      ├─ profile.buildProfile()                                  business (luật nằm trong bean)
      ├─ request.setAttribute("profile", profile)
      └─ String url = "/WEB-INF/views/learning-profile.jsp";
         getServletContext().getRequestDispatcher(url).forward(request, response)
  → learning-profile.jsp: ${profile.fullName}, <c:forEach>, <c:out>   view
```

### 3.2 API JSON cho JavaScript

```text
assets/js/api.js  fetch("/api/auth/login", {method: "POST", body: JSON})
  → RequestContextFilter (UTF-8, X-Request-Id)
  → AuthServlet.doPost  path "/login"
      ├─ JsonUtil.readBody / stringField           đọc JSON thủ công
      ├─ UserDB.selectUser(email) + PasswordUtil.verifyPassword
      ├─ SessionUtil.startSession → HttpSession (user, csrfToken) + cookie JSESSIONID
      └─ ResponseUtil.sendJson(response, 200, ...)
```

Mọi request ghi của API gửi header `X-CSRF-Token`; `SessionUtil.hasValidCsrfToken` so với
token trong session.

### 3.3 Form POST có chống gian lận (nhiệm vụ, nối dây, chẩn đoán)

```text
GET  /wiring?action=play&id=7        hiện form, hidden: csrfToken, id, expectedVersion, pair…
POST /wiring  action=save|submit
  → WiringServlet.doPost
      ├─ SessionUtil.hasValidFormCsrfToken      thiếu/sai → 403
      ├─ WiringDB.mutate(id, user.getId(), expectedVersion, ...)    một transaction
      │     ├─ khoá bài rồi lượt FOR UPDATE; kiểm chủ lượt, trạng thái, version
      │     ├─ WiringExercise.grade(...)        luật chấm nằm ở JavaBean
      │     └─ UPDATE ... SET version = version + 1, state = 'SUBMITTED' ; commit
      └─ sendRedirect(/wiring?action=play&id=...)    PRG: F5 không gửi lại form
```

Ý để nói: **client chỉ gửi các cặp dây; điểm, quyền, trạng thái luôn do server tính.**

## 4. Mẫu code phải đọc ra được

Ba khuôn dưới đây lặp lại trong toàn dự án. Hiểu một lần là đọc được hầu hết file.

### 4.1 Khuôn `XxxDB` (Ch12 slide 45–53)

```java
public static User selectUser(String email) throws SQLException {
    ConnectionPool pool = ConnectionPool.getInstance();
    Connection connection = pool.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;
    String query = "SELECT " + FIELDS + " FROM users WHERE email = ?";
    try {
        ps = connection.prepareStatement(query);
        ps.setString(1, email);
        rs = ps.executeQuery();
        User user = null;
        if (rs.next()) {
            user = readUser(rs);
        }
        return user;
    } finally {
        DBUtil.closeResultSet(rs);
        DBUtil.closePreparedStatement(ps);
        pool.freeConnection(connection);
    }
}
```

Giải thích: `static` như slide; tham số `?` chống SQL injection; `finally` luôn trả kết nối;
khác slide ở chỗ **ném `SQLException`** để servlet trả 503 thay vì báo nhầm "không tìm thấy".

### 4.2 Khuôn JavaBean (Ch6 slide 6; Ch9 slide 28–33)

Lớp `implements Serializable`, constructor rỗng, mọi field `private` có getter/setter.
Khác một class dữ liệu thông thường ở chỗ **có luật nghiệp vụ**, ví dụ
`AssemblySession.canChangeStatusTo(...)`, `TaskRubric.automaticPoints(...)`,
`WiringExercise.grade(...)`, `User.canChangeRoleOf(...)`. Servlet chỉ hỏi bean rồi gọi
`XxxDB`; JSP chỉ hiển thị.

### 4.3 Khuôn Servlet trang (Ch5 slide 21–24; Ch12 slide 42–44)

Lấy user từ session → đọc dữ liệu qua `XxxDB` → `setAttribute` → `String url` → `forward`.
Trường hợp lỗi: chưa đăng nhập `sendRedirect`, sai quyền 403, không thuộc quyền 404,
dữ liệu sai 422, database lỗi 503.

## 5. Bản đồ chức năng theo đợt

| Đợt | Chức năng | Route chính | Bảng chính | Slide chính |
| --- | --- | --- | --- | --- |
| Nền | Đăng ký/đăng nhập, phân quyền, CSRF | `/api/auth/*`, `/api/admin/users/*` | `users` | Ch7, Ch12 |
| Nền | Catalog robot/linh kiện/thư viện, CRUD Admin | `/robots`, `/components`, `/api/robots/*`… | `robots`, `components`, `robot_components`, `assembly_steps`, `library_resources` | Ch2, Ch5, Ch6, Ch12 |
| Nền | Phiên lắp ráp, phòng 3D | `/api/assembly-sessions/*`, `pages/lap-rap-3d.html` | `assembly_sessions`, `session_*` | Ch7, Ch12 |
| 1 | Hoàn tất lắp ráp, phiếu kết quả, ăn mừng, quiz, tra cứu lỗi, tổng kết/thống kê | `/assembly-receipt`, `/api/quiz/*`, `/learning-summary`, `/admin-stats` | `quiz_*`, `troubleshooting_guides` | Ch5, Ch6, Ch9, Ch12 |
| 2 | Lái thử mô hình sau khi hoàn tất | `assembly-3d-drive.js` | không đổi | (JavaScript) |
| 3 | Hai mẫu robot mới | seed | `robots`… | Ch12 |
| 4 | Cửa hàng, giỏ, đơn mô phỏng | `/api/shop/*`, `/api/cart/*`, `/api/orders`, `/order-history` | `shop_products`, `cart_items`, `orders`, `order_items` | Ch9 (Cart), Ch12 |
| 5, 5b | Hồ sơ học tập in A4, bám slide | `/learning-profile` | đọc bảng sẵn có | Ch6, Ch8, Ch9, Ch10/6 |
| 6 | Nhiệm vụ thực hành, vòng nộp, rubric A/B, chẩn đoán lỗi | `/tasks`, `/admin-tasks`, `/admin-task-reviews`, `/diagnosis`, `/admin-diagnosis` | `practice_tasks`, `task_*`, `diagnosis_*` | Ch5, Ch7 (hidden), Ch9, Ch13/29–34 |
| 7 | Phòng nối dây có chấm, hỗ trợ Admin–User | `/wiring`, `/admin-wiring`, `/wiring-support`, `/admin-wiring-support` | `wiring_*`, `wiring_support_*` | Ch5, Ch6, Ch9 |
| UI | Giao diện học tập dùng chung (charcoal/cam, Poppins, Heroicons) | các JSP, `learning-workspace.css` | không đổi | Ch6 (include) |

Bản chi tiết kèm kịch bản demo và Hỏi–Đáp theo từng đợt nằm trong `docs/TEAM_FLOW_DEMO_GUIDE.md` của project.

## 6. Khái niệm nâng cao hơn slide và cách giải thích

| Khái niệm | Giải thích ngắn | Ví dụ trong code |
| --- | --- | --- |
| Server-authoritative | Client chỉ gửi dữ kiện thô; server tính điểm, kiểm quyền | điểm nối dây, phần trăm hoàn thành phiên lắp ráp, tổng đơn hàng |
| IDOR (đổi ID trên URL) | Mọi truy vấn của USER kèm `user_id` lấy từ session; ID của người khác trả 404 | `selectAttempt(id, user.getId())` |
| CSRF | Trang lạ tự gửi form thay người dùng; chặn bằng token trong session so với token trong request | header `X-CSRF-Token` (API), hidden `csrfToken` (form) |
| XSS | Chèn HTML/JS vào dữ liệu; chặn bằng `c:out` (Ch9/7–8) | mọi chuỗi từ DB trong JSP |
| PRG (Post/Redirect/Get) | Sau POST thành công chuyển hướng bằng 302; F5 không gửi lại | mọi servlet form |
| Transaction | Nhiều câu SQL cùng thành công hoặc cùng huỷ | checkout, nộp bài, tạo vòng mới |
| `SELECT … FOR UPDATE` | Khoá dòng để hai request đồng thời tuần tự hoá | nộp bài nhiệm vụ, bắt đầu chẩn đoán |
| Optimistic version | Form mang `expectedVersion`; lệch thì 422, không ghi đè bản mới | lưu nháp nối dây, trả lời hỗ trợ |
| Snapshot | Lưu bản chụp dữ liệu lúc xảy ra để lịch sử không đổi khi nội dung gốc đổi | `quiz_attempt_answers`, `order_items`, bài nộp nhiệm vụ |
| `session_version` | Đổi mật khẩu/quyền thì tăng; phiên cũ mất hiệu lực ngay | `SessionUtil.isCurrent` |
| PBKDF2 | Băm mật khẩu có salt, 210 000 vòng, SHA-256 | `PasswordUtil` |
| `Cache-Control: no-store` | Trang cá nhân không bị cache | mọi servlet trang |

## 7. Phân công theo thành viên

**Mục đích:** mỗi thành viên **làm chủ** một mảng code (đọc được từng dòng, mở đúng file khi
được hỏi), đồng thời nắm phần chung để trả lời khi giảng viên hỏi chéo.

Cơ sở phân công:

1. Giữ nguyên vai trò ban đầu của từng người (trang `pages/thanh-vien.html`
   và bảng "Phân công" trong `docs/TEAM_FLOW_DEMO_GUIDE.md`).
2. Các chức năng thêm sau (Đợt 1–7 và giao diện) được xếp về mảng gần nhất với vai trò
   gốc, rồi cân bằng khối lượng để ba người có độ sâu tương đương.
3. Mỗi người sở hữu **một luồng đầy đủ từ trình duyệt đến bảng MySQL**, vì giảng viên hỏi
   theo chuỗi `trang → servlet → bean → XxxDB → bảng`, không hỏi riêng một tầng.

Đây là đề xuất; nhóm có thể đổi qua lại, miễn là mọi file có đúng một người đứng tên.
Kiến thức chung nằm ở các mục 1–6 và 8 của file này.

### 7.1 Bảng tổng quan

| | Huỳnh Minh Tài | Văn Phạm Thảo Nhi | Phạm Tuấn Anh |
| --- | --- | --- | --- |
| Vai trò gốc | Giao diện dùng chung, tích hợp; Servlet/Tomcat, đăng nhập, session, phân quyền | Database, nội dung robot/linh kiện/thư viện, CRUD nội dung | Phòng 3D, luồng lắp ráp, tương tác JavaScript, đa thiết bị |
| Mảng sở hữu | **Nền tảng và phiên học tập** | **Dữ liệu và nội dung** | **Tương tác và thực hành** |
| Servlet | 11 | 15 | 11 |
| Java (dòng, xấp xỉ) | 7 200 | 4 900 | 6 000 |
| JSP (dòng, xấp xỉ) | 1 400 | 450 | 1 400 |
| JavaScript (dòng, xấp xỉ) | 1 100 | 2 450 | 2 100 |
| Khác | CSS giao diện dùng chung 870 dòng | SQL (schema, seed, migration) 2 270 dòng | CSS `style.css` 2 200 dòng (3D, đáp ứng màn hình) |
| Bảng MySQL | 10 | 11 (kể cả `schema_migrations`) | 16 |
| Chapter nặng nhất | 2, 5, 7 | 12 (và 2, 6) | 5, 6, 9 (và JavaScript) |

Số dòng chỉ để cân khối lượng, đếm ngày 06/10/2026; không dùng để so hơn kém.

### 7.2 Huỳnh Minh Tài — Nền tảng và phiên học tập

**Câu chốt:** "Em phụ trách khung chạy của ứng dụng: Tomcat/Servlet, đăng nhập, session,
CSRF, phiên lắp ráp, nhiệm vụ thực hành và hồ sơ học tập; mọi thứ đều do server quyết định."

| Phần | Route | File nên mở (theo chuỗi) | Bảng |
| --- | --- | --- | --- |
| Đăng ký, đăng nhập, phân quyền, CSRF | `/api/auth/*`, `/api/admin/users/*` | `api.js` → `AuthServlet` → `SessionUtil`, `PasswordUtil`, `ValidationUtil` → `UserDB` → `User` | `users` |
| Khung chung | mọi `/api/*`, `/account`, `/architecture` | `RequestContextFilter`, `ResponseUtil`, `JsonUtil`, `HealthServlet` → `HealthDB` | — |
| Phiên lắp ráp, hoàn tất, phiếu kết quả | `/api/assembly-sessions/*`, `/assembly-receipt` | `AssemblySessionServlet` → `AssemblySession` (luật) → `AssemblySessionDB` → `assembly-receipt.jsp` | `assembly_sessions`, `session_components`, `session_steps`, `session_visual_parts` |
| Nhiệm vụ thực hành, vòng nộp, rubric A/B | `/tasks`, `/admin-tasks`, `/admin-task-reviews` | `TaskServlet`, `AdminTaskServlet` → `PracticeTask`, `TaskRecipient`, `TaskRound`, `TaskRubric`, `TaskSubmission`, `TaskReview` → `PracticeTaskDB`, `TaskSubmissionDB` | `practice_tasks`, `task_recipients`, `task_rounds`, `task_submissions`, `task_reviews` |
| Hồ sơ học tập in A4 | `/learning-profile` | `LearningProfileServlet` → `LearningProfile`, `Profile*` → `StatsDB` → `learning-profile.jsp` + `learning-profile.css` | đọc bảng sẵn có |
| Giao diện dùng chung | tất cả JSP | `workspace-header.jspf`, `workspace-footer.jspf`, `learning-workspace.css` | — |

**Slide phải thuộc:** Ch2 slide 3–5, 16–20 (Model 2); Ch5 slide 6–26, 46–50; Ch7 slide 4–16, 35–37
(session, hidden field); Ch6 slide 27–33 (include); Ch12 slide 34–38 (pool, `context.xml`).

**Câu hỏi dự kiến (ngoài 43 câu chung):**

1. Vẽ luồng đăng nhập từ `api.js` đến cookie `JSESSIONID`. Session lưu những gì?
2. Vì sao `getCurrentUser` đọc lại user từ database thay vì tin bản trong session?
3. Một phiên lắp ráp chuyển trạng thái như thế nào, ai kiểm tra? (`canChangeStatusTo`,
   `getExpectedPreparationStatus`.) Vì sao không tin phần trăm hoàn thành từ trình duyệt?
4. Nộp bài nhiệm vụ làm những bước nào trong một transaction? Thứ tự khoá để tránh deadlock?
5. "Chỉ tính lượt quiz đầu trong vòng nộp" nhằm chống điều gì? Còn hạn chế gì?
6. Hồ sơ dùng `Date` và `SimpleDateFormat` thay `java.time` vì sao? `BigDecimal` dùng ở đâu?
7. Vì sao hồ sơ có `@media print` mà không có dịch vụ xuất PDF?
8. `include` trong JSP khác custom tag ở đâu? Dự án dùng cái nào, vì sao?

**Demo 3 phút:** đăng nhập → mở DevTools thấy `JSESSIONID` → `/learning-profile` (xem source
Ctrl+U thấy dữ liệu đã dựng sẵn) → in PDF → mở `TaskServlet` chỉ `hasValidFormCsrfToken` và PRG.

### 7.3 Văn Phạm Thảo Nhi — Dữ liệu và nội dung

**Câu chốt:** "Em phụ trách lớp dữ liệu: thiết kế bảng, migration, seed, `ConnectionPool` và
các lớp `XxxDB`, cùng nội dung robot, linh kiện, quiz, hướng dẫn xử lý lỗi và thống kê."

| Phần | Route | File nên mở (theo chuỗi) | Bảng |
| --- | --- | --- | --- |
| Lớp dữ liệu nền | — | `context.xml` → `ConnectionPool` → `DBUtil` → `DatabaseLifecycleListener`; `schema.sql`, `migrations/001–012`, `erd.md` | 37 bảng |
| Catalog robot, linh kiện, bước, thư viện (đọc và CRUD Admin) | `/robots`, `/components`, `/api/robots/*`, `/api/admin/*` | `RobotCatalogPageServlet`, `ComponentCatalogPageServlet` → `RobotDB`, `ComponentDB`, `AssemblyStepDB`, `LibraryResourceDB` → `Robot`, `Component`, `AssemblyStep` → `robots.jsp`, `components.jsp` | `robots`, `components`, `robot_components`, `assembly_steps`, `library_resources` |
| Quiz | `/api/quiz/*`, `/api/admin/quiz/*` | `QuizServlet`, `AdminQuizServlet` → `QuizAttemptDB`, `QuizQuestionDB` → `Quiz*` | `quiz_questions`, `quiz_options`, `quiz_attempts`, `quiz_attempt_answers` |
| Tra cứu lỗi | `/api/troubleshooting-guides`, `/api/admin/troubleshooting-guides/*` | `TroubleshootingServlet` → `TroubleshootingGuideDB` → `TroubleshootingGuide` | `troubleshooting_guides` |
| Tổng kết và thống kê | `/learning-summary`, `/admin-stats` | `LearningSummaryServlet`, `AdminStatsServlet` → `StatsDB` (`GROUP BY`, `COUNT DISTINCT`) → `QuizRobotScore`, `RobotPopularity`, `QuizRobotAggregate`, `QuestionMissRate` | đọc nhiều bảng |

**Slide phải thuộc:** Ch12 slide 4–53 (toàn bộ JDBC, trọng tâm 18–20, 32–38, 45–53); Ch13 slide 29–34
(khái niệm transaction); Ch2 slide 5, 16 (model và data access layer); Ch9 slide 28–33 (`Product`,
`LineItem`, `Cart`, tham khảo cách bean giữ luật).

**Câu hỏi dự kiến:**

1. Giải thích từng dòng `UserDB.selectUser`: vì sao có `finally`, vì sao `static`?
2. `ConnectionPool` lấy kết nối từ đâu? `context.xml` khai báo gì (`maxTotal`, `maxWaitMillis`)?
3. Migration khác `schema.sql` thế nào? Vì sao có 12 migration thay vì sửa thẳng schema?
4. Quan hệ `robots — robot_components — components` là loại quan hệ gì? Khoá chính, khoá ngoại?
5. Quiz lưu "bản chụp" câu trả lời để làm gì? Admin sửa câu hỏi thì điểm cũ có đổi không?
6. Vì sao `quiz_attempt_answers` lưu cả nhãn lựa chọn và đáp án đúng tại thời điểm làm bài?
7. Số liệu thống kê tránh đếm trùng bằng cách nào (`COUNT(DISTINCT …)`)?
8. Vì sao `ON DELETE RESTRICT` được dùng cho dữ liệu lịch sử?
9. Vì sao không dùng JPA mà dùng JDBC? (Ch12 so với Ch13.)

**Demo 3 phút:** `/api/health` → `/components` (xem source) → chạy một truy vấn trong
MySQL Workbench cho `robots` ↔ `components` → thêm một câu hỏi quiz qua Admin → làm quiz →
mở `quiz_attempt_answers` thấy bản chụp câu trả lời → mở `/admin-stats` thấy số liệu `GROUP BY`.

### 7.4 Phạm Tuấn Anh — Tương tác và thực hành

**Câu chốt:** "Em phụ trách phần người dùng thao tác trực tiếp: phòng 3D, lắp ráp, chẩn đoán lỗi
tương tác và phòng nối dây; JavaScript chỉ quản lý thao tác, còn chấm điểm và quyền nằm ở server."

| Phần | Route | File nên mở (theo chuỗi) | Bảng |
| --- | --- | --- | --- |
| Phòng 3D, dựng linh kiện, camera, hiệu ứng ăn mừng, toàn màn hình, lái thử | `pages/lap-rap-3d.html` | `assembly-3d-parts.js`, `assembly-3d-config.js`, `assembly-3d.js`, `assembly-3d-drive.js`, `confetti.js` (dữ liệu từ `data.js` / API) | `session_visual_parts` (qua phiên) |
| Chẩn đoán lỗi tương tác | `/diagnosis`, `/admin-diagnosis` | `DiagnosisServlet` → `DiagnosisAttempt` (điểm 4/3/3) → `DiagnosisDB` → `diagnosis-play.jsp` | `diagnosis_scenarios`, `_checks`, `_options`, `_attempts`, `_attempt_checks` |
| Phòng nối dây có chấm | `/wiring`, `/admin-wiring` | `wiring.js` (nhấn/kéo/bàn phím) → `WiringServlet` → `WiringExercise.grade` → `WiringDB` → `wiring-play.jsp`, `wiring-diagram.jsp` | `wiring_exercises`, `_terminals`, `_rules`, `_attempts`, `_attempt_connections` |
| Hỗ trợ Admin–User | `/wiring-support`, `/admin-wiring-support` | `WiringSupportServlet` → `WiringSupportRequest`, `WiringSupportMessage` → `WiringSupportDB` | `wiring_support_requests`, `wiring_support_messages` |
| Cửa hàng, giỏ, đơn mô phỏng (giao diện giỏ bằng JS, checkout là transaction nhiều bảng) | `/api/shop/*`, `/api/cart/*`, `/api/orders`, `/order-history` | `shop.js`, `cart.js` → `CartServlet`, `OrderServlet` → `CartDB`, `OrderDB`, `ShopProductDB` → `ShopOrder`, `CartItem`, `OrderItem` → `order-history.jsp` | `shop_products`, `cart_items`, `orders`, `order_items` |
| Giao diện đáp ứng màn hình | tất cả trang | `style.css`, `wiring.css`, `wiring-support.css`, kiểm 390/768/1024/1440 px | — |

**Slide phải thuộc:** Ch5 slide 12–17, 21–26 (form, `getParameterValues`, forward, redirect); Ch6 slide 4–9
(bean, EL); Ch9 slide 11, 17–19 (`c:forEach`, `c:if`, `c:choose`); Ch7 slide 35–36 (hidden field).
JavaScript, SVG và Three.js nằm ngoài slide nên phải nắm chắc để giải thích bằng lời của mình.

**Câu hỏi dự kiến:**

1. Khi tắt JavaScript, phòng nối dây còn dùng được không? Bằng cách nào? (Hai nhóm radio, form POST.)
2. Kéo thả có quyết định điểm không? Dữ liệu nào thật sự gửi lên server? (`pair`, `expectedVersion`.)
3. Công thức điểm nối dây, ví dụ 4 dây đúng, 2 sai, 14 bắt buộc? (14,3.) Vì sao đúng hết cộng một dây thừa không được 100?
4. Vì sao đầu nối `line-left.OUT` và `line-right.OUT` phải có ID khác nhau?
5. `expectedVersion` giải quyết tình huống nào? Hai tab cùng lưu thì kết quả ra sao?
6. Bài chẩn đoán: vì sao quan sát chỉ hiện sau khi bấm kiểm tra? Điểm quá trình tính thế nào (4/3/3)?
7. Hỗ trợ Admin–User: vì sao bản chụp do server dựng, không nhận sơ đồ từ trình duyệt? Mỗi lượt chỉ một yêu cầu mở được bảo đảm bằng gì?
8. Phòng 3D: ai quyết định phiên "hoàn tất"? (Server kiểm linh kiện bắt buộc.)
9. Heroicons SVG ở header có thể làm hỏng sơ đồ nối dây không? (Selector đã giới hạn vào đúng SVG sơ đồ.)
10. Checkout làm những bước nào, rollback khi nào? Giá lấy từ đâu, vì sao không tin giá từ trình duyệt? `order_items` giữ snapshot để làm gì?

**Demo 3 phút:** lắp robot trong phòng 3D → hoàn tất → ăn mừng → `/wiring`: nối đúng, nối sai và một
cặp bị cấm 5V–GND → nộp → xem điểm và giải thích → luyện lại → gửi câu hỏi hỗ trợ → mở `pages/cua-hang.html` đặt một đơn mô phỏng và xem `order_items`.

### 7.5 Kiến thức chung cả ba người phải biết

- Ba luồng ở mục 3 của file này (trang JSP, API JSON, form POST).
- Ba khuôn code: `XxxDB`, JavaBean, Servlet trang.
- Chapter nào dự án **không** dùng (10 custom tag, 13 JPA, 14 JavaMail) và vì sao.
- Danh sách chỗ ngoài slide (filter, API JSON, `FOR UPDATE`) và cách giải thích.
- Chạy được dự án trên máy mình: JDK 17, Tomcat 9, biến `DB_*`, `npm test`, `mvn test`.

## 8. Câu hỏi–đáp ngắn (đọc to được trong 30 giây mỗi câu)

### A. Kiến trúc và slide

1. **Dự án theo mô hình nào?** Model 2 (MVC) của Chapter 2 slide 4–5: servlet là controller,
   JavaBean là model, JSP là view, `XxxDB` là tầng truy cập dữ liệu.
2. **Controller ở đâu?** `controller/`, 37 lớp `@WebServlet`. Mỗi servlet nhận request,
   kiểm quyền, gọi `XxxDB` và bean, rồi forward hoặc trả JSON.
3. **Vì sao không có Service hay DAO?** Slide không dạy hai tầng đó. Giống
   `EmailListServlet` (Ch12/42–44), servlet gọi thẳng `UserDB`; luật nghiệp vụ nằm trong bean
   như lớp `Cart` (Ch9/32–33).
4. **forward khác redirect thế nào?** `forward` chuyển nội bộ trong cùng request, URL không
   đổi, giữ được `setAttribute` (Ch5/24). `sendRedirect` bảo trình duyệt gọi URL mới
   (Ch5/26); dùng cho khách chưa đăng nhập và cho PRG sau POST.
5. **request attribute khác session attribute?** `profile` đặt vào request chỉ sống trong
   một lượt; `user` đặt vào session sống suốt phiên đăng nhập (Ch7/10).
6. **Vì sao servlet không có biến instance?** Một servlet dùng chung cho nhiều luồng
   (Ch5/48–50); biến instance gây tranh chấp. Dự án chỉ có hằng `static final`.
7. **`@WebServlet` hay `web.xml`?** Cả hai có trong Ch5 (slide 7–11). Dự án dùng annotation;
   `web.xml` chỉ khai báo trang chào.

### B. JDBC

8. **PreparedStatement khác Statement?** Tham số `?` gán bằng `setXxx`, nên dữ liệu người
   dùng không thành mã SQL (chống SQL injection) (Ch12/18–20).
9. **Connection pool là gì, cấu hình ở đâu?** Giữ sẵn một tập kết nối để dùng lại. Khai báo
   trong `META-INF/context.xml` (Ch12/34); `ConnectionPool` lấy qua JNDI `java:/comp/env/jdbc/robotlab`
   (Ch12/35–37). `freeConnection` trả kết nối về pool trong `finally`.
10. **Vì sao ném `SQLException` thay vì in lỗi như slide?** Để servlet trả 503 "database chưa
    sẵn sàng"; slide in lỗi rồi trả `null` khiến sai mật khẩu và database hỏng khó phân biệt.
11. **Transaction là gì? Ví dụ?** Nhóm câu SQL cùng thành công hoặc cùng huỷ
    (`setAutoCommit(false)`, `commit`, `rollback`). Ví dụ checkout: trừ kho, tạo đơn, tạo
    từng dòng đơn, xoá giỏ; lỗi bước nào cũng quay về như cũ.
12. **`FOR UPDATE` để làm gì?** Khoá dòng đang đọc; request đồng thời phải đợi nhau. Nhờ vậy
    4 request nộp cùng một lượt chỉ tạo 1 kết quả.
13. **Snapshot là gì, vì sao cần?** Lưu bản chụp tại thời điểm xảy ra (tên, giá, câu trả lời,
    điểm). Admin đổi giá hay sửa câu hỏi sau đó không làm lịch sử cũ sai.
14. **Vì sao điểm dùng `DECIMAL` và `BigDecimal`?** Số thực nhị phân sai số; điểm cần làm tròn
    HALF_UP chính xác một lần ở kết quả cuối.

### C. Session và bảo mật

15. **Đăng nhập lưu ở đâu?** Trong `HttpSession` ở server (`user` và `csrfToken`); trình duyệt
    chỉ giữ cookie `JSESSIONID`.
16. **Mật khẩu lưu thế nào?** Băm PBKDF2-HMAC-SHA256, salt ngẫu nhiên 16 byte, 210 000 vòng;
    cột `users.password_hash` không chứa mật khẩu gốc.
17. **`session_version` để làm gì?** Đổi mật khẩu hoặc quyền làm tăng số này; mỗi request
    `getCurrentUser` đọc lại từ database và so, nên phiên cũ hết hiệu lực ngay.
18. **CSRF là gì, chống thế nào?** Trang lạ lợi dụng cookie đang đăng nhập để gửi request thay
    bạn. Mỗi phiên có `csrfToken`; API gửi trong header `X-CSRF-Token`, form HTML gửi trong
    hidden field (Ch7/35–36); thiếu hoặc sai thì 403.
19. **XSS là gì, chống thế nào?** Chèn mã vào dữ liệu hiển thị. JSP bọc mọi chuỗi từ DB bằng
    `c:out` (Ch9/7–8) để `<` thành văn bản.
20. **Vì sao đổi `?id=` trên URL không xem được bài người khác?** Mọi truy vấn của USER kèm
    `user_id` từ session; id lạ trả 404.
21. **Vì sao client không tự gửi điểm?** Server tính lại từ dữ liệu gốc; trường `score`,
    `userId` trong request bị bỏ qua (đã kiểm bằng test).

### D. JSP, EL, JSTL

22. **EL tìm biến ở đâu?** Lần lượt page, request, session, application (Ch6/9, Ch8/7). Vì vậy
    không đặt tên vòng lặp trùng implicit object như `header`, `param`, `cookie`.
23. **Dự án dùng `c:forEach`, `c:if`, `c:choose` ở đâu?** Các JSP trong `WEB-INF/views`, ví dụ
    danh sách bài trong `wiring-list.jsp`, trạng thái nhiệm vụ trong `task-view.jsp`.
24. **include dùng để làm gì?** Gắn header/footer chung: `<%@ include file="workspace-header.jspf" %>`
    (compile-time include, Ch6/31) để 34 trang có cùng khung.
25. **JavaBean cần ba quy tắc nào?** Constructor không tham số, get/set cho mọi field private,
    `Serializable` (Ch6/6). `JavaBeanRulesTest` quét mọi lớp trong `business/`.
26. **Vì sao JSP không có `if (status.equals("COMPLETED"))`?** Luật thuộc bean:
    `profileRobot.completed` là getter boolean; JSP chỉ hiển thị.
27. **Dự án có dùng custom tag không?** Không (Ch10). Phần dùng chung làm bằng include và
    getter của bean.

### E. HTTP

28. **Mã trạng thái nào được dùng?** 200 OK, 201 tạo mới, 302 chuyển hướng, 403 sai vai trò
    hoặc sai token, 404 không có/không thuộc quyền, 422 dữ liệu không hợp lệ, 503 database lỗi.
29. **PRG là gì?** POST xử lý xong thì trả 302 sang trang GET; tải lại trang không gửi lại form.
30. **Vì sao vừa có JSP vừa có API JSON?** Trang tĩnh trong `pages/` (phòng 3D, tick linh kiện)
    cần dữ liệu không tải lại trang; trang cá nhân dựng ở server cho dễ kiểm quyền. Cả hai dùng
    chung `data/` và `business/`.
31. **`RequestContextFilter` làm gì?** Ngoài slide: đặt UTF-8, gắn `X-Request-Id`, bắt lỗi 500 để
    trả thông báo an toàn; không chứa nghiệp vụ.
32. **`Cache-Control: no-store` để làm gì?** Trình duyệt không lưu trang cá nhân; đăng xuất rồi bấm
    Back không xem lại được dữ liệu.

### F. Tính năng

33. **Chấm nối dây thế nào?** Cặp dây là không hướng, chuẩn hoá theo ID. `N` cặp bắt buộc, `C`
    cặp đúng, `W` cặp sai/thừa/cấm. Điểm = `100 × max(C − W, 0) / N`, làm tròn HALF_UP một chữ số.
    Chỉ ghi "đúng toàn bộ" khi `C = N` và `W = 0`. Luật ở `WiringExercise.grade` và `WiringGrade`.
34. **Hai tab cùng sửa một lượt thì sao?** Mỗi form mang `expectedVersion`; tab lưu sau bị 422,
    không ghi đè.
35. **Quiz chấm ở đâu?** Server (`QuizAttemptDB` + bean), lưu bản chụp câu trả lời. Trình duyệt chỉ
    gửi lựa chọn, không gửi đáp án đúng.
36. **Nhiệm vụ có vòng nộp thế nào?** Mỗi người được giao có các vòng; chỉ tính lượt quiz/chẩn
    đoán đầu của vòng; Admin chấm, "cần bổ sung" mở vòng mới. Mẫu A: lắp ráp 40, quiz 40, giải
    thích 20. Mẫu B: lắp ráp 30, quiz 25, chẩn đoán 25, giải thích 20.
37. **Vì sao quan sát chẩn đoán không có sẵn trong HTML?** Chỉ trả sau khi server ghi nhận lượt
    chọn phép kiểm tra đó; nếu có sẵn thì xem mã nguồn là biết đáp án.
38. **Hồ sơ học tập khác tổng kết ở đâu?** Hồ sơ là tài liệu in A4 có chỗ cho giảng viên nhận xét,
    chọn "tốt nhất" theo tỷ lệ điểm; tổng kết là bảng thống kê trên màn hình.
39. **Phòng 3D lấy dữ liệu từ đâu?** JavaScript đọc mô hình từ catalog (API/seed); việc coi phiên là
    hoàn tất do server kiểm các linh kiện bắt buộc, không tin phần trăm client.
40. **Dữ liệu nối dây có phải mô phỏng điện không?** Không. Chỉ chấm các cặp nối trực tiếp trong
    bài mẫu; không mô phỏng dòng điện, không xác nhận robot thật.

### G. Câu hỏi bẫy

41. **"Dự án có dùng JPA, JavaMail, custom tag không?"** Không. JDBC thay JPA vì theo Chapter 12;
    chưa có email thông báo; custom tag (Ch10) được thay bằng include và getter của bean. Biết
    nội dung các chapter đó vẫn phải trả lời được.
42. **"Chỗ nào chưa đúng slide?"** Trả lời thật: filter, API JSON, `FOR UPDATE`, và khoảng 11 chỗ
    code cũ dùng stream/`switch ->`/`fmt:`; có danh sách trong `PHASE5B_SLIDE_AUDIT.md`.
43. **"Phần này em làm hay công cụ làm?"** Trả lời trung thực về cách làm việc; quan trọng hơn là
    giải thích được từng dòng và chỉ ra file, hàm, bảng liên quan.

### H. Câu hỏi bổ sung (từ phần demo)

44. **Dữ liệu khởi nguồn từ đâu?** `database/seed.sql` là dữ liệu mẫu cho MySQL; nguồn chính khi chạy
    đầy đủ là MySQL qua servlet. `assets/js/data.js` chỉ là bản dự phòng để giao diện vẫn có nội dung khi
    API chưa sẵn sàng.
45. **Vì sao không chạy `index.html` trực tiếp?** HTML chỉ là view. Đăng nhập, `HttpSession`, servlet và
    JDBC chỉ hoạt động khi WAR được deploy trên Tomcat; giao diện gọi `/api` cùng origin nên cần context `/`.
46. **Vì sao không tin phần trăm hoàn thành do trình duyệt tính?** Người dùng sửa được JavaScript hoặc gọi
    thẳng API. `AssemblySessionDB.completeSession()` chỉ ghi `COMPLETED` khi chính câu `UPDATE` đối chiếu
    `session_visual_parts` với `robot_components` và thấy đủ; điều kiện nằm ngay trong `WHERE` nên kiểm tra và
    ghi xảy ra nguyên tử, hai request gần đồng thời không tạo hai kết quả khác nhau.
47. **Sao vừa có `/components` vừa có `/api/components`?** Hai view của cùng dữ liệu: `/components` trả HTML dựng ở
    server (xem source thấy bảng đã đầy đủ), `/api/components` trả JSON cho JavaScript; cùng dùng `ComponentDB`.

## 9. Kiểm thử và chạy dự án

| Loại | Công cụ | Kiểm gì |
| --- | --- | --- |
| Kiểm nguồn | Node (`npm test`, 21 file trong `tests/server/`) | cấu trúc môn học (`course-structure`), cấm kỹ thuật ngoài slide, form/CSRF/no-store, hợp đồng giao diện |
| Luật nghiệp vụ | JUnit (`mvn -f tomcat-app/pom.xml test`) | điểm rubric, chấm nối dây, luật công bố, `JavaBeanRulesTest`, `JspExpressionContractTest` |
| Chạy thật | Tomcat + MySQL (cổng 8080) | `/api/health` phải trả `"database":"connected"` |

Số liệu gần nhất: 131 Node (130 đạt + 1 bỏ qua nếu chưa build exploded WAR) và 79 JUnit,
đều xanh. Node và JUnit chỉ là công cụ kiểm tra lúc phát triển, **không phải runtime**.

Nhớ: IntelliJ phải dùng **JDK 17** (Project SDK); Tomcat truyền
`-DDB_HOST -DDB_PORT -DDB_NAME -DDB_USER -DDB_PASSWORD` qua VM options vì `context.xml`
đọc các giá trị đó; Tomcat không tự đọc `.env`.

## 10. Cách luyện chéo và lịch ôn

1. **Vòng 1 — kể phần mình (10 phút/người):** vẽ luồng của mảng mình trên bảng, không nhìn tài liệu.
2. **Vòng 2 — hỏi chéo (15 phút/người):** người khác chọn ngẫu nhiên 5 câu trong mục 8 của file này
   và 3 câu trong mục của bạn, yêu cầu "mở file nào, dòng nào".
3. **Vòng 3 — đổi chỗ:** mỗi người trình bày một luồng của người khác (ví dụ Tài giải thích checkout của Tuấn Anh,
   Nhi giải thích chấm nối dây, Tuấn Anh giải thích đăng nhập của Tài). Ai bị vấp thì ghi lại để ôn.
4. **Vòng cuối — mô phỏng:** giả lập giảng viên, mỗi người trả lời liên tục 10 phút, chỉ được dùng
   chuỗi file làm gợi ý.

Quy tắc khi trả lời: nói tên file và hàm trước, giải thích tác dụng sau; nếu không chắc thì
nói "em kiểm tra lại trong code" và mở file, không đoán.

### Lịch gợi ý

| Mốc | Việc |
| --- | --- |
| Còn 7 ngày | Mỗi người đọc phần của mình và mục 1–6 và mục 8 của file này |
| Còn 5 ngày | Vẽ luồng không nhìn tài liệu; chạy lại dự án trên máy từng người |
| Còn 3 ngày | Hỏi chéo vòng 2; chốt dữ liệu demo |
| Còn 1 ngày | Mô phỏng vòng cuối; kiểm `npm test`, `mvn test`; kiểm tài khoản demo |
| Ngày kiểm tra | Mang bản chạy được; không thay đổi code mới ngay trước giờ trình bày |

## 11. Khi không nhớ và việc trước ngày kiểm tra

Đừng đoán. Đi theo chuỗi file, đọc to điều thấy:

```text
trang/JS → Servlet (doGet/doPost) → bean (luật) → XxxDB (SQL, transaction) → bảng → JSP
```

Mở mục 3 của file này cho luồng mẫu; khi cần xem code thật thì mở `docs/ARCHITECTURE.md`.

### Danh sách việc trước ngày kiểm tra

- [ ] Chạy được Tomcat 8080; `/api/health` báo `connected`; đăng nhập USER và ADMIN.
- [ ] IntelliJ dùng JDK 17; Rebuild Project trước khi chạy.
- [ ] Tài khoản demo đăng nhập được (bảng ở `docs/TEAM_FLOW_DEMO_GUIDE.md` trong project); nếu không, tạo
      tài khoản mới và ghi lại mật khẩu ở nơi riêng, **không** ghi vào Git.
- [ ] Dữ liệu demo: ít nhất một phiên đã hoàn tất, một lượt quiz, một nhiệm vụ đang mở.
- [ ] `npm test` và `mvn -f tomcat-app/pom.xml test` đều xanh trên máy sẽ trình bày.
- [ ] Mỗi người tự vẽ lại ba luồng ở mục 3 không nhìn tài liệu.
- [ ] Mỗi người trả lời được các câu của mình ở mục 7 và 43+ câu ở mục 8.
- [ ] Không mang `.env`, `.idea/workspace.xml` (chứa mật khẩu DB) khi nộp file nén.
