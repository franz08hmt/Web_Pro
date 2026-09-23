# Luồng demo và câu hỏi giảng viên

Tài liệu chỉ tập trung vào luồng đã tích hợp: dữ liệu → Servlet → giao diện →
phiên lắp ráp → phòng 3D. Bảng đối chiếu từng lớp với số slide nằm ở
[ARCHITECTURE.md](ARCHITECTURE.md). Đây là ứng dụng Java Servlet/JSP chạy trong
Tomcat và một WAR; JavaScript gọi API cùng origin, không phải serverless/BaaS.

## Kết luận đối chiếu yêu cầu ban đầu

| Nội dung cô hỏi | Mức đáp ứng trong code | Bằng chứng nên mở |
| --- | --- | --- |
| Controller và client/server kết nối | Đạt: `fetch` → URL mapping `@WebServlet` → JSON response | `assets/js/api.js`, `AuthServlet.java`, `ResponseUtil.java` |
| Request/response | Đạt: servlet đọc body/query/session và trả status + JSON; trang JSP dùng forward | `AuthServlet.java`, `RobotServlet.java`, `ComponentCatalogPageServlet.java` |
| Cấu trúc theo kiến thức Servlet/JSP | Đạt về mô hình: JavaBean, Servlet, JSP/EL/JSTL, lớp `XxxDB`, JDBC | [ARCHITECTURE.md](ARCHITECTURE.md) và các source được dẫn dưới đây |
| Database | Đạt: schema/seed, migration, ERD, PreparedStatement, pool và đóng tài nguyên | `database/`, `docs/erd.md`, `ConnectionPool.java`, `DBUtil.java` |
| Đăng nhập/phân quyền | Code có HttpSession, role USER/ADMIN, CSRF, hash mật khẩu; DB hiện tại cần chạy migration `004_account_session_version` trước khi xác nhận demo end-to-end | `AuthServlet.java`, `SessionUtil.java`, `UserDB.java`, `AdminUserServlet.java`, `database/migrations/004_account_session_version.sql` |
| Lắp ráp từng bước | Chưa khép kín UI: trang có danh sách hướng dẫn, nhưng chưa lưu tick từng bước | Backend có API `/steps`; UI chưa gọi `setStepStatus()` |

Lưu ý: repository hiện không chứa bộ slide gốc, nên các số chương/slide trong
`ARCHITECTURE.md` đang đối chiếu theo ghi chú môn học đã có trong dự án/cuộc trao
đổi; trước buổi bảo vệ hãy mở slide của cô để xác nhận lại số slide chính xác.
Đây là giới hạn kiểm chứng tài liệu, không phải thiếu cấu trúc Servlet/JSP trong code.
Database đang dùng trước đó chưa có `users.session_version`; user ứng dụng không có
quyền `ALTER`, nên cần DBA/chủ database chạy migration `004` rồi mới demo xác thực
trên chính database đó. Không chạy lại migration trên DB đã có version này.

## Phân công

| Thành viên | Phần trình bày | File nên mở |
| --- | --- | --- |
| Nhi | Database, dữ liệu robot/linh kiện/bước/thư viện, CRUD nội dung | `database/schema.sql`, `database/seed.sql`, `data/RobotDB.java`, `data/ComponentDB.java`, `Admin*Servlet.java` |
| Tuấn Anh | Phòng 3D, dựng linh kiện, camera và thao tác lắp ráp | `assets/js/assembly-3d-parts.js`, `assets/js/assembly-3d.js`, `pages/lap-rap-3d.html` |
| Tài | Servlet/Tomcat, đăng nhập, session, phân quyền, phiên lắp ráp, tích hợp client-server | `AuthServlet.java`, `AdminUserServlet.java`, `data/UserDB.java`, `util/SessionUtil.java`, `AssemblySessionServlet.java`, `assets/js/api.js` |

## Kịch bản demo ngắn

1. Chạy Tomcat và mở `/api/health`; chỉ `"database":"connected"` — kết nối lấy từ
   connection pool khai báo trong `META-INF/context.xml`.
2. Mở `/components` — trang do servlet dựng sẵn ở server. Xem source trang
   (Ctrl+U) để thấy bảng HTML đã có đủ dữ liệu, không có JavaScript nào gọi API.
   Đây là câu trả lời trực tiếp cho "setAttribute và forward nằm ở đâu".
3. Mở trang robot; DevTools Network cho thấy `GET /api/robots`.
4. Trong code, đi theo `RobotServlet → RobotDB → ConnectionPool → MySQL`.
5. Đăng ký/đăng nhập; mở `/api/auth/me`, giải thích `JSESSIONID`, `HttpSession`
   và CSRF token. `UserDB.insert` ghi cứng role `user` cho mọi tài khoản đăng ký.
6. Mở `/account` — servlet đọc `User` từ `HttpSession`, đặt vào request rồi
   forward sang `account.jsp`. `SessionUtil.getCurrentUser` đối chiếu database
   trước khi tin role trong phiên; chưa đăng nhập thì bị chuyển về trang đăng nhập.
7. Ở `/pages/lap-rap.html`, chọn robot và tick linh kiện; tiến độ chuẩn bị được lưu
   trong phiên tài khoản qua API (hoặc local storage khi chưa đăng nhập/API lỗi).
   Quy trình lắp ráp bên dưới là danh sách hướng dẫn đọc theo thứ tự, không phải
   checkbox tiến độ từng bước.
8. Vào phòng 3D, lắp/gỡ part; panel chỉ chứa linh kiện. Part được đồng bộ qua
   `visual-parts`; reload trang để chứng minh trạng thái 3D được khôi phục.
   Quay lại trang chuẩn bị và bấm **Đặt lại tiến độ** để xóa checklist, trạng thái
   part 3D và đưa phiên đang làm về `PREPARING` trong một thao tác.
9. Đăng nhập admin, CRUD một nội dung rồi mở lại `/components` để thấy dữ liệu
   mới xuất hiện ngay trong HTML do server dựng. Mở `/pages/admin-users.html`
   để chỉ cách admin đổi role người khác; tài khoản thường bị API từ chối 403.
10. Mở `/architecture` để tóm tắt lại toàn bộ luồng.

## Câu hỏi thường gặp

### "Dự án chia tầng thế nào?"

Đúng Model 2 của Chapter 2 slide 5: model là business object (`business/`), view
là HTML/JSP, controller là servlet (`controller/`), data access layer là các lớp
`XxxDB` (`data/`). Tên lớp `UserDB`, `ConnectionPool`, `DBUtil` giữ giống slide
Chapter 12.

### "Sao không có tầng Service hay DAO?"

Vì slide không dùng hai tầng đó. Giống `EmailListServlet` (Chapter 12 slide
42-44), servlet tự lấy tham số, kiểm tra hợp lệ rồi gọi `UserDB`. Luật nghiệp vụ
nằm trong chính business object — ví dụ `AssemblySession.canChangeStatusTo()`,
`User.canChangeRoleOf()` — như lớp `Cart` của sách tự có `addItem/removeItem`.

### "User có phải JavaBean không?"

Có, đủ ba quy tắc ở Chapter 6 slide 6: constructor không tham số, get/set cho mọi
biến private, `implements Serializable`. `JavaBeanRulesTest` kiểm tra tự động mọi
lớp trong `business/`, thiếu một quy tắc là build báo lỗi.

### "Controller ở đâu?"

Các lớp có `@WebServlet` trong `tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller`.
Ví dụ `RobotServlet` cho `/api/robots`; `AccountPageServlet` forward tới JSP.

### "Client và server kết nối như nào?"

`assets/js/content-api.js` và `assets/js/api.js` dùng `fetch()` gửi HTTP tới
`/api`. Tomcat ánh xạ URL tới servlet qua `@WebServlet`. Servlet trả JSON UTF-8;
JavaScript đọc `response.json()` và cập nhật trang. Cùng một WAR trên Tomcat phục
vụ các JSP/HTML và endpoint API; trình duyệt không kết nối MySQL.

Ví dụ dễ chỉ trên màn hình: `api.js → POST /api/auth/login` gửi JSON
`{email,password}`; `AuthServlet.doPost()` xác thực qua `UserDB` rồi trả HTTP 200
với `{data:{user}}` và cookie `JSESSIONID`. Request `/api/auth/me` tiếp theo gửi
cookie đó; response có `{data:{user},csrfToken}`. Với thao tác ghi, client gửi
thêm `X-CSRF-Token`; status và JSON response được tạo ở Servlet qua `ResponseUtil`.

### "Request và response nằm ở đâu?"

Trong `doGet`, `doPost`, `doPut`, `doDelete` của servlet. `HttpServletRequest`
cung cấp tham số, đường dẫn, thân request, header và session;
`HttpServletResponse` nhận mã trạng thái, content type và nội dung qua `ResponseUtil`.
Ví dụ mở `AuthServlet.doPost()` cho login/register và `ResponseUtil.sendJson()` /
`sendError()` để chỉ đúng chỗ tạo response thành công/lỗi.

### "Database đi qua lớp nào?"

Chỉ các lớp trong `data/` viết SQL. Mỗi method làm đúng bốn bước như slide 45-51:
lấy `Connection` từ `ConnectionPool`, tạo `PreparedStatement` với tham số `?`,
chạy câu lệnh, rồi trong `finally` gọi `DBUtil.closeResultSet`,
`DBUtil.closePreparedStatement` và `pool.freeConnection`. Mở `UserDB` hoặc
`RobotDB` để chỉ.

### "Connection pool cấu hình ở đâu?"

`tomcat-app/src/main/webapp/META-INF/context.xml`, giống slide 34. Tomcat tạo pool
tên `jdbc/robotlab`; `ConnectionPool` tra JNDI `java:/comp/env/jdbc/robotlab`.
User/password lấy từ VM options (`-DDB_USER=...`) nên không bị commit lên Git.

### "JSP được dùng ở đâu?"

Bốn trang, đều đặt JSP dưới `WEB-INF` để mọi request bắt buộc đi qua servlet:

| URL | Servlet | Dữ liệu đưa sang JSP |
| --- | --- | --- |
| `/robots` | `RobotCatalogPageServlet` | `List<Robot>` đọc qua `RobotDB` |
| `/components` | `ComponentCatalogPageServlet` | `List<Component>`, có phân trang |
| `/account` | `AccountPageServlet` | `User` lấy từ `HttpSession` |
| `/architecture` | `ArchitectureServlet` | Vài chuỗi minh họa kiến trúc |

### "Sao vừa có /components vừa có /api/components?"

Cùng dùng `ComponentDB` và JavaBean `Component`, chỉ khác view: `/components` trả
HTML dựng sẵn ở server theo kiểu Servlet/JSP truyền thống; `/api/components` trả
JSON cho JavaScript của các trang trong `pages/`.

### "Đăng nhập lưu ở đâu? Mật khẩu lưu thế nào?"

`SessionUtil.startSession` tạo `HttpSession`; Tomcat gửi cookie `JSESSIONID`.
Session lưu JavaBean `User` và CSRF token. Cột `users.password_hash` chỉ chứa hash
PBKDF2 có salt do `PasswordUtil` tạo, không lưu mật khẩu gốc.

### "Dữ liệu khởi nguồn từ đâu?"

`database/seed.sql` là dữ liệu mẫu cho MySQL. Khi chạy đầy đủ, nguồn chính là
MySQL qua servlet. `assets/js/data.js` chỉ là bản dự phòng để giao diện vẫn có nội
dung minh họa khi API chưa sẵn sàng.

### "Quy trình bước lắp ráp đang được lưu thế nào?"

Trên trang chuẩn bị, `main.js` dựng danh sách `model.steps` thành hướng dẫn tĩnh;
checkbox được đồng bộ hiện tại là checkbox linh kiện. Backend đã có
`PUT /api/assembly-sessions/{id}/steps/{stepId}`, nhưng giao diện chưa gọi endpoint
này. Trong phòng 3D, panel hiển thị part và tiến độ lắp/gỡ part, không hiển thị
danh sách bước. Nói đúng ranh giới này để không khẳng định tính năng UI chưa có.

### "Phòng 3D lấy mô hình ở đâu?"

Three.js nằm cục bộ tại `assets/vendor/three`. Hình học được dựng bằng code trong
`assembly-3d-parts.js`, không tải file mô hình bên ngoài. `assembly-3d.js` lưu part
đã lắp qua `/api/assembly-sessions/{id}/visual-parts/{componentId}`.

### "Tại sao không chạy index.html trực tiếp?"

File HTML chỉ là view. Đăng nhập, `HttpSession`, servlet và JDBC chỉ hoạt động khi
WAR được deploy trên Tomcat. Cấu hình Tomcat dùng application context `/` vì giao
diện gọi `/api` cùng origin.

## Chuỗi file nên mở khi bảo vệ

```text
assets/js/content-api.js
→ controller/RobotServlet.java
→ data/RobotDB.java  (ConnectionPool, PreparedStatement, DBUtil)
→ business/Robot.java  (JavaBean)
→ database/schema.sql
```

Với tài khoản và phiên lắp ráp:

```text
assets/js/api.js
→ controller/AuthServlet.java / controller/AssemblySessionServlet.java
→ util/SessionUtil.java  (HttpSession)
→ data/UserDB.java / data/AssemblySessionDB.java
→ business/User.java / business/AssemblySession.java
→ MySQL
```

Khi cô hỏi request/response, mở theo chuỗi ngắn này:

```text
assets/js/api.js (fetch + JSON + cookie/CSRF)
→ controller/AuthServlet.java (doPost/doGet)
→ util/ResponseUtil.java (HTTP status + JSON)
→ util/SessionUtil.java (HttpSession/JSESSIONID)
```
