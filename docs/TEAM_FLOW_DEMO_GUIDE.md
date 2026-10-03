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
| Đăng nhập/phân quyền | Đã kiểm tra end-to-end trên DB demo cục bộ: đăng ký/đăng nhập, HttpSession, USER/ADMIN, CSRF và chặn `/admin-stats` bằng 403 với USER | `AuthServlet.java`, `SessionUtil.java`, `UserDB.java`, `AdminUserServlet.java`, `database/migrations/004_account_session_version.sql` |
| Lắp ráp từng bước | Danh sách hướng dẫn đọc theo thứ tự nằm ở `pages/lap-rap.html` (ngoài phòng 3D), có chủ đích không đưa checkbox từng bước vào phòng 3D | Backend vẫn có API `/steps` cho mục đích dạy Servlet/JSP; UI hiện tại không gọi `setStepStatus()` |
| Hoàn tất thực hành + phiếu kết quả | Đạt: server tự đối chiếu `session_visual_parts` với `robot_components` trước khi ghi `COMPLETED`, không tin dữ liệu client | `AssemblySessionDB.completeSession()`, `AssemblyReceiptPageServlet.java` |
| Hiệu ứng ăn mừng | Đạt: chỉ bắn khi vừa chuyển sang `COMPLETED` trong lượt bấm hiện tại, tôn trọng `prefers-reduced-motion` | `assets/js/confetti.js`, `assets/js/assembly-3d.js` |
| Bài kiểm tra kiến thức | Đạt: server chấm từ `quiz_options.is_correct`, lưu bản chụp để không đổi kết quả cũ | `QuizAttemptDB.submitAttempt()`, `QuizServlet.java` |
| Tra cứu lỗi lắp ráp | Seed cơ sở có 14 tình huống; seed đợt 3 thêm 6 tình huống cho hai robot mới (tổng 20 sau khi nạp đủ seed) | `TroubleshootingServlet.java`, `TroubleshootingGuideDB.java`, `database/seed-troubleshooting-phase3.sql` |
| Tổng kết và thống kê | Đạt: số liệu tính bằng `GROUP BY`/`COUNT DISTINCT` trực tiếp trong SQL, có trạng thái "chưa có dữ liệu" | `StatsDB.java`, `LearningSummaryServlet.java`, `AdminStatsServlet.java` |
| Hồ sơ học tập cá nhân có thể in | Chỉ đọc, giới hạn theo `HttpSession`, no-store; JavaBean tính trạng thái, tỷ lệ quiz, giờ Việt Nam và căn cứ kỹ năng; trình duyệt in A4/PDF | `LearningProfileServlet.java` | `LearningProfile`; `RobotDB`; `StatsDB` | `learning-profile.jsp` | Đọc `robots`, `assembly_sessions`, `quiz_attempts`, `robot_components`, `components`; không thêm bảng |
| Cửa hàng mô phỏng | Catalog công khai; giỏ/đơn theo session; admin CRUD; checkout server tính giá và khóa tồn trong transaction; không thanh toán thật | `ShopServlet`, `CartServlet`, `OrderServlet`, `AdminShopServlet`; `ShopProductDB`, `CartDB`, `OrderDB`; migration `008_shop_cart_orders.sql` |

Lưu ý: repository hiện không chứa bộ slide gốc, nên các số chương/slide trong
`ARCHITECTURE.md` đang đối chiếu theo ghi chú môn học đã có trong dự án/cuộc trao
đổi; trước buổi bảo vệ hãy mở slide của cô để xác nhận lại số slide chính xác.
Đây là giới hạn kiểm chứng tài liệu, không phải thiếu cấu trúc Servlet/JSP trong code.

### Database demo đã kiểm tra (28/09/2026)

DB cục bộ được kiểm tra qua `/api/health`: `status=ok`, `database=connected`; có đủ
các migration `001`–`007`, bao gồm cột `users.session_version`. Không chạy lại các
migration đã ghi nhận. Nếu dùng một DB khác, kiểm tra `schema_migrations` trước;
chỉ nhờ chủ DB chạy migration còn thiếu bằng tài khoản được cấp quyền, không dùng
tài khoản ứng dụng để `ALTER`.

Các tài khoản dưới đây dành riêng cho buổi demo trên DB cục bộ. Cả bốn được tạo
qua `POST /api/auth/register`; role ADMIN được cấp sau đó bằng một câu `UPDATE`
có điều kiện và tăng `session_version` để vô hiệu hóa phiên cũ. Đăng ký công khai
luôn tạo role USER, không gửi role từ trình duyệt.

| Email | Quyền | Dữ liệu lịch sử để trình bày |
| --- | --- | --- |
| `ral-demo-admin@robotlab.test` | ADMIN | Đăng nhập được; xem `/admin-stats`; không có dữ liệu thực hành riêng |
| `ral-demo-builder@robotlab.test` | USER | Đã hoàn tất Robot dò đường; có 3 lượt kiểm tra, mỗi mẫu robot một lượt (7/7) |
| `ral-demo-practice@robotlab.test` | USER | Phiên Robot tránh vật cản ở `PREPARING`, đã chuẩn bị 4/8 nhóm linh kiện |
| `ral-demo-student@robotlab.test` | USER | Có một lượt kiểm tra Cánh tay robot mini (7/7) |

Mật khẩu demo chung: `RobotLabDemo#2026`. Đây là thông tin đăng nhập thử nghiệm,
không dùng ngoài demo/không dùng cho người thật; đổi hoặc xóa các tài khoản này
trước khi đưa một bản sao database lên môi trường công khai. Mật khẩu DB thật
không nằm trong tài liệu này hay trong Git.

## Bảng đối chiếu: chức năng → kiến thức môn học → code

Trả lời trực tiếp câu "chức năng này xây từ đâu trong code?" cho năm chức năng
mới thêm vào đồ án.

| Chức năng | Kiến thức môn học minh họa | Servlet | JavaBean / XxxDB | JSP | Bảng database |
| --- | --- | --- | --- | --- | --- |
| 1. Hoàn tất thực hành + phiếu kết quả | Server-side validation (không tin client); atomic UPDATE tránh race condition; Servlet→JSP forward | `AssemblySessionServlet` (đổi trạng thái), `AssemblyReceiptPageServlet` (phiếu kết quả) | `AssemblySession.canCompleteAssembly()`; `AssemblySessionDB.completeSession()` | `assembly-receipt.jsp` | `assembly_sessions` (cột `completed_at`), `session_visual_parts`, `robot_components` |
| 2. Bài kiểm tra kiến thức | Chấm điểm ở server, không tin client; snapshot dữ liệu lịch sử; JDBC transaction nhiều bảng | `QuizServlet` (làm bài), `AdminQuizServlet` (quản trị) | `QuizQuestion.isCorrectOption()`; `QuizAttemptDB.submitAttempt()` | *(API JSON, JS dựng giao diện — không dùng JSP)* | `quiz_questions`, `quiz_options`, `quiz_attempts`, `quiz_attempt_answers` |
| 3. Tra cứu lỗi lắp ráp | Nội dung công khai đọc từ MySQL; lọc/tìm bằng `PreparedStatement` tham số hóa | `TroubleshootingServlet` (công khai), `AdminTroubleshootingServlet` (quản trị) | `TroubleshootingGuide`; `TroubleshootingGuideDB` | *(API JSON)* | `troubleshooting_guides` |
| 4. Hiệu ứng ăn mừng | Sự kiện phía client gắn với xác nhận server, không phải hiệu ứng trang trí độc lập | *(không có endpoint riêng — dùng lại `PATCH /api/assembly-sessions/{id}`)* | — | — | — |
| 5. Tổng kết và thống kê | `GROUP BY`/`COUNT DISTINCT` để tránh đếm trùng; phân quyền xem dữ liệu tổng hợp | `LearningSummaryServlet` (cá nhân), `AdminStatsServlet` (quản trị, chỉ ADMIN) | `QuizRobotScore`, `RobotPopularity`, `QuizRobotAggregate`, `QuestionMissRate`; `StatsDB` | `learning-summary.jsp`, `admin-stats.jsp` | Đọc tổng hợp từ `assembly_sessions`, `quiz_attempts`, `quiz_attempt_answers`, `users` |
| 6. Hồ sơ học tập cá nhân có thể in | Servlet → JavaBean → JDBC/PreparedStatement → JSP; session scope, escape dữ liệu, quy tắc điểm/trạng thái/thời gian nằm trong business object | `LearningProfileServlet` | `LearningProfile`, `ProfileRobotEntry`, `ProfileSkill`; `RobotDB`, `StatsDB` | `learning-profile.jsp` | `robots`, `assembly_sessions`, `quiz_attempts`, `robot_components`, `components` (chỉ đọc) |

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
3. Mở trang robot; DevTools Network cho thấy `GET /api/robots`. Chọn
   `line-obstacle` hoặc `servo-scout`, mở sơ đồ dây và danh sách linh kiện để
   đối chiếu dữ liệu DB. Hai mẫu mới được thêm bằng seed, không cần controller
   hoặc endpoint mới.
4. Trong code, đi theo `RobotServlet → RobotDB → ConnectionPool → MySQL`.
5. Đăng ký/đăng nhập; mở `/api/auth/me`, giải thích `JSESSIONID`, `HttpSession`
   và CSRF token. `UserDB.insert` ghi cứng role `user` cho mọi tài khoản đăng ký.
   Để demo nhanh, dùng các tài khoản trong bảng **Database demo đã kiểm tra**.
6. Mở `/account` — servlet đọc `User` từ `HttpSession`, đặt vào request rồi
   forward sang `account.jsp`. `SessionUtil.getCurrentUser` đối chiếu database
   trước khi tin role trong phiên; chưa đăng nhập thì bị chuyển về trang đăng nhập.
7. Ở `/pages/lap-rap.html`, chọn robot và tick linh kiện; tiến độ chuẩn bị được lưu
   trong phiên tài khoản qua API (hoặc local storage khi chưa đăng nhập/API lỗi).
   Quy trình lắp ráp bên dưới là danh sách hướng dẫn đọc theo thứ tự, không phải
   checkbox tiến độ từng bước.
8. Vào phòng 3D, lắp/gỡ part; panel chỉ chứa linh kiện. Part được đồng bộ qua
   `visual-parts`; reload trang để chứng minh trạng thái 3D được khôi phục. Nút
   toàn màn hình nằm trên thanh công cụ của phòng 3D; bấm lại hoặc dùng `Esc` để
   thoát. Checklist quy trình chỉ còn ở trang lắp ráp bên ngoài, không đưa vào 3D.
   Quay lại trang chuẩn bị và bấm **Đặt lại tiến độ** để xóa checklist, trạng thái
   part 3D và đưa phiên đang làm về `PREPARING` trong một thao tác.
9. Đăng nhập admin, CRUD một nội dung rồi mở lại `/components` để thấy dữ liệu
   mới xuất hiện ngay trong HTML do server dựng. Mở `/pages/admin-users.html`
   để chỉ cách admin đổi role người khác; tài khoản thường bị API từ chối 403.
10. Mở `/architecture` để tóm tắt lại toàn bộ luồng.

### Demo năm chức năng mới (Chức năng 1–5)

11. Vào phòng 3D, lắp đủ mọi nhóm linh kiện tới 100% — nút **Hoàn tất lắp ráp**
    tự mở khóa. Mở DevTools Network trước khi bấm để thấy `PATCH
    /api/assembly-sessions/{id}` với body `{"status":"COMPLETED"}`. Bấm nút, xem
    hiệu ứng confetti (Chức năng 4) và bấm **Xem kết quả** để mở `/assembly-receipt`
    — trang JSP hiển thị đúng robot, thời điểm bắt đầu/hoàn tất và danh sách linh
    kiện đã lắp, không có JavaScript nào tính lại các số liệu này.
12. Để chứng minh server không tin client: mở Console, gọi thẳng
    `RobotAssemblyApi.assemblySessions.updateStatus(idPhienChuaLapDu, "COMPLETED")`
    trên một phiên mới lắp một phần — nhận lỗi `409 ASSEMBLY_INCOMPLETE`.
13. Mở `/pages/kiem-tra.html?model=line-follower`, làm bài và nộp — điểm hiện ra
    ngay kèm giải thích từng câu. Vào `/pages/admin-quiz.html`, sửa nội dung một
    câu hỏi vừa làm, quay lại lịch sử bài kiểm tra cũ để chỉ ra điểm/giải thích cũ
    không đổi (đọc từ bản chụp `quiz_attempt_answers`, không JOIN ngược
    `quiz_questions`).
14. Mở `/pages/tra-cuu-loi.html`, lọc theo mẫu robot và từ khóa triệu chứng —
    không cần đăng nhập. Mở `/pages/admin-troubleshooting.html` để chỉ cách
    admin thêm/sửa một tình huống.
15. Đăng nhập tài khoản có lịch sử, mở `/learning-summary` — chỉ số phiên/điểm
    kiểm tra của đúng tài khoản này. Đăng nhập admin, mở `/admin-stats` — số
    liệu toàn hệ thống tính bằng `GROUP BY`; thử truy cập bằng tài khoản thường
    để nhận `403`.
16. Chọn `line-obstacle`: giải thích hai cảm biến line đọc trạng thái trái/phải,
    HC-SR04 đo khoảng cách, rồi chương trình ưu tiên dừng/né trước khi tìm lại
    vạch. Mở tab linh kiện/bước và lần lượt xem hai endpoint
    `GET /api/robots/{id}/components` và `/steps` trong Network.
17. Chọn `servo-scout`: chỉ ra SG90 quay đầu HC-SR04 để so sánh khoảng trống;
    đối chiếu D3 (tín hiệu servo), D11/D12 (TRIG/ECHO), D5–D10 (L298N) và
    GND chung. Bản build thực tế cần nguồn bàn DC 9V đủ dòng (ngoài BOM): giao
    dải theo datasheet XL4015 (8–36V) và Uno (7–12V); hộp pin 4AA 6V chỉ cấp
    `Vs` động cơ, không cấp XL4015 hay `VIN` Arduino. Đo đầu ra 5.0V và kiểm tra
    jumper 5V-EN theo đúng module trước khi nối. Hai mẫu này có quiz và ba tình
    huống tra cứu lỗi riêng. Tham khảo [datasheet XL4015 của XLSEMI](https://www.xlsemi.com/datasheet/XL4015-EN.pdf),
    [đặc tính nguồn Arduino Uno R3](https://store.arduino.cc/products/arduino-uno-rev3)
    và [datasheet L298 của ST](https://www.st.com/resource/en/datasheet/cd00000240.pdf).
18. Cửa hàng mô phỏng: đăng nhập USER, mở `/pages/cua-hang.html`, thêm linh kiện
    vào giỏ, đổi số lượng rồi xem tổng tạm tính. Trong Network chỉ
    `PUT /api/cart/items/{id}` gửi quantity + CSRF; `POST /api/orders` không gửi
    giá/tổng/user ID. Response trả mã đơn và tổng do server tính; mở
    `/order-history` xem snapshot. Nói rõ đây không phải thanh toán thật. Với
    ADMIN, mở `/pages/admin-shop.html`, sửa giá demo rồi ngừng bán; catalog ẩn
    sản phẩm nhưng đơn cũ vẫn giữ tên/giá tại thời điểm đặt.
19. Mở `/learning-profile` bằng một tài khoản demo: chỉ ra danh mục đủ năm mẫu,
    số liệu của session hiện tại, trạng thái/quiz/kỹ năng có căn cứ; dùng In / Lưu
    PDF và chọn A4. So sánh cùng tài khoản ở `/learning-summary`; nếu lịch sử
    quiz có mẫu số thay đổi, giải thích rằng hồ sơ chọn đúng lượt theo tỷ lệ
    `score/total_questions`, còn trang tổng kết cũ giữ nguyên cách tổng hợp trước.

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
| `/order-history` | `OrderHistoryServlet` | Danh sách `ShopOrder` của user, gồm snapshot `OrderItem` |

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

### "Sao không tin luôn phần trăm hoàn thành mà trình duyệt tính sẵn?"

Vì trình duyệt là thứ người dùng kiểm soát được — họ có thể sửa JavaScript hoặc
gọi thẳng API bằng tay. `AssemblySessionDB.completeSession()` chỉ ghi
`COMPLETED` khi chính câu `UPDATE` đó tự đối chiếu `session_visual_parts` với
`robot_components` và thấy đủ; điều kiện nằm ngay trong `WHERE` nên việc kiểm
tra và ghi xảy ra atomic, hai request gần đồng thời (double-click) không thể
tạo hai kết quả khác nhau.

### "Sao bài kiểm tra không lưu thẳng câu hỏi mà phải lưu 'bản chụp'?"

Vì `quiz_questions`/`quiz_options` là nội dung admin có thể sửa sau này. Nếu
lịch sử đọc trực tiếp từ hai bảng đó, sửa một câu hỏi sẽ vô tình đổi cả điểm số
và giải thích của những người đã làm bài trước đó. `quiz_attempt_answers` lưu
nguyên văn câu hỏi/lựa chọn tại đúng thời điểm nộp bài, nên lịch sử luôn đúng
với những gì người dùng thực sự đã thấy lúc làm bài.

### "Số liệu ở trang thống kê có đáng tin không?"

Có — mọi con số đọc trực tiếp bằng `GROUP BY`/`COUNT DISTINCT` ngay trong câu
SQL của `StatsDB`, không cộng dồn thủ công ở Java (tránh đếm trùng khi có
JOIN). Không có số liệu giả nào được chèn vào để biểu đồ đẹp hơn; khi chưa đủ
dữ liệu, trang hiện rõ thông báo "chưa có dữ liệu" thay vì bảng trống hoặc số 0
gây hiểu nhầm.

### "Giá và tổng đơn có thể bị sửa ở trình duyệt không?"

Không. Browser chỉ gửi mã sản phẩm và số lượng cho cart; checkout gửi body rỗng.
`OrderDB.checkout()` đọc giá/tồn từ MySQL trong transaction, khóa hàng cần
thiết, tự tính tổng rồi giảm kho và tạo snapshot. Sửa con số trên DevTools không
đổi được giá lưu vào đơn. Đây là đơn mô phỏng, không kết nối cổng thanh toán.

### "Sao hồ sơ không lưu thành bảng riêng?"

Hồ sơ là bản xem trước chỉ đọc, dựng từ `robots`, `assembly_sessions`,
`quiz_attempts` và quan hệ linh kiện đã có. Không lưu thêm bản sao có thể lệch
với tiến độ thật; nút in dùng chức năng in/lưu PDF sẵn có của trình duyệt.

### "Sao hồ sơ có thể khác trang tổng kết?"

Hai trang đọc cùng các bảng của đúng tài khoản. Hồ sơ áp dụng định nghĩa đã công
bố: lượt tốt nhất là tỷ lệ `score/total_questions` cao nhất, hòa thì lấy lượt
mới hơn và giữ đúng tổng câu của lượt đó. Trang tổng kết cũ vẫn giữ nguyên cách
tổng hợp trước; dữ liệu lịch sử có mẫu số thay đổi có thể làm hai cách hiển thị
khác nhau. Hồ sơ cũng là tài liệu in tĩnh, không hiện email hay giải thích kiến
trúc dài như trang tổng kết.

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
