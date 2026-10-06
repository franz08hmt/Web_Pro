# Robot Assembly Lab

## Chẩn đoán tương tác và nhiệm vụ mẫu B (Đợt 6, chặng 2)

USER chọn phép kiểm tra bằng form để mở từng quan sát mô phỏng, rồi chọn nguyên
nhân và biện pháp. Server ghi nhận thao tác, chấm thu thập quan sát /4, nguyên
nhân /3 và xử lý /3; chỉ hiện đáp án/giải thích sau khi kết luận. Lượt PRACTICE
để luyện độc lập; lượt TASK gắn cố định vào vòng nhiệm vụ ngay khi bắt đầu.
ADMIN biên soạn nháp, xem trước, công bố bản bất biến, nhân bản và lưu trữ.

Mẫu B: lắp ráp 30 + quiz 25 + chẩn đoán 25 + giải thích 20. Server dùng điểm
chẩn đoán chính xác để tính tổng tự động, làm tròn HALF_UP một lần tới một chữ
số thập phân và lưu snapshot. Mẫu A giữ luật 40 + 40 + 20. Vòng bổ sung dùng
lượt chẩn đoán mới đã kết luận hoặc dùng lại bài trước nếu chưa bắt đầu lượt mới.

DB đã có migration 009: chạy theo thứ tự, bằng MySQL client trên DB ứng dụng:

```sql
SOURCE database/migrations/010_diagnosis_practice.sql;
SOURCE database/seed-diagnosis-phase6.sql;
```

Migration 010 cần CREATE/ALTER, thêm năm bảng và các liên kết nullable, mở rubric
A/B, đổi hai CHECK điểm tối thiểu từ 40 thành 30. Không sửa dữ liệu cũ. Seed
INSERT IGNORE gồm năm tình huống chính thức; cần catalog robot và ít nhất một
ADMIN có sẵn. Có thể chạy lại; các bản công bố đã tồn tại không bị ghi đè.
Schema cài mới đã gộp thay đổi này trong `database/schema.sql`.

Tài khoản USER có **Luyện chẩn đoán**; ADMIN có **Quản lý tình huống chẩn đoán**.
Trang Servlet/JSP và form hoạt động khi tắt JavaScript. Quan sát là dữ liệu
mô phỏng của bài tập, không phải phép đo robot thật. Đây là đánh giá quá trình
có tài liệu tham khảo và luyện tập, không phải thi kín.

Xem [đối chiếu slide và kịch bản demo](docs/TEAM_FLOW_DEMO_GUIDE.md),
[báo cáo QA chặng 2 và giới hạn](docs/PHASE6C_QA_REPORT.md).

## Nhiệm vụ thực hành và đánh giá (Đợt 6, chặng 1)

ADMIN tạo nháp/giao USER/công bố/gia hạn/đóng/lưu trữ/nhân bản, chấm và sửa chấm
có lịch sử. USER làm quiz tính điểm theo vòng, chọn phiên lắp ráp COMPLETED,
viết giải thích, xem trước rồi nộp; yêu cầu bổ sung mở vòng tiếp theo. Rubric A
cố định: lắp ráp 40, quiz 40, giải thích 20. Chưa chấm hiện điểm tự động /80.

Sau migration 008, chạy `database/migrations/009_practice_tasks.sql` trên đúng
database bằng tài khoản có quyền CREATE/REFERENCES. Kiểm schema_migrations
trước; database mới dùng schema.sql đã chứa năm bảng nhiệm vụ, không chạy lại
các migration đã có. Migration 009 chỉ thêm bảng, không đổi dữ liệu cũ;
IF NOT EXISTS cho phép tiếp tục khi lần chạy DDL trước bị ngắt, không thay thế
việc kiểm cấu trúc các bảng hiện có.

Lối vào ở trang Tài khoản: USER **Nhiệm vụ của tôi**, ADMIN **Quản lý nhiệm vụ**.
Trang mới dùng Servlet → JavaBean → XxxDB → JSP/form, chạy khi tắt JavaScript.
Xem [đối chiếu chương/slide và demo](docs/TEAM_FLOW_DEMO_GUIDE.md) và
[bằng chứng kiểm chứng Đợt 6](docs/PHASE6_QA_REPORT.md).


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

## Phân công nhóm

Nhóm 4 · Web Programming. Mỗi thành viên giữ vai trò ban đầu, các chức năng thêm sau được chia
lại để khối lượng và độ sâu tương đương; mỗi người sở hữu một luồng đầy đủ từ trình duyệt đến
bảng MySQL.

| Thành viên | Mảng phụ trách | Chức năng chính | Chuỗi file nên mở |
| --- | --- | --- | --- |
| Huỳnh Minh Tài | Nền tảng và phiên học tập (giao diện dùng chung, tích hợp, Servlet/Tomcat, phân quyền) | Đăng nhập, session, CSRF; phiên lắp ráp và phiếu kết quả; nhiệm vụ thực hành (rubric A/B); hồ sơ học tập in A4; khung giao diện JSP | `AuthServlet` → `SessionUtil` → `UserDB`; `AssemblySessionServlet`; `TaskServlet`; `LearningProfileServlet` |
| Văn Phạm Thảo Nhi | Dữ liệu và nội dung (database, nội dung mô hình/linh kiện/thư viện) | Lớp dữ liệu (`ConnectionPool`, schema, migration, seed); catalog robot/linh kiện/thư viện và CRUD Admin; quiz; tra cứu lỗi; tổng kết và thống kê | `ConnectionPool` → `RobotDB`/`ComponentDB`; `QuizServlet` → `QuizAttemptDB`; `StatsDB`; `database/schema.sql` |
| Phạm Tuấn Anh | Tương tác và thực hành (luồng lắp ráp, JavaScript, đa thiết bị) | Phòng 3D, hiệu ứng ăn mừng và lái thử; chẩn đoán lỗi tương tác; phòng nối dây có chấm và hỗ trợ Admin–User; cửa hàng mô phỏng; giao diện đáp ứng | `assembly-3d.js`; `WiringServlet` → `WiringExercise.grade` → `WiringDB`; `DiagnosisServlet`; `CartServlet`/`OrderServlet` |

Bảng chi tiết kèm số liệu cân bằng, slide phải thuộc và câu hỏi dự kiến theo từng người:
[mục 7 của file ôn tập cuối kỳ](docs/ON_TAP_VAN_DAP_CUOI_KY.md#7-phân-công-theo-thành-viên).

## Tài liệu bảo vệ bài

- [Kiến trúc và luồng request/response](docs/ARCHITECTURE.md)
- [Quy ước và danh sách API](docs/API_CONVENTIONS.md)
- [ERD](docs/erd.md)
- [Luồng demo và câu hỏi giảng viên](docs/TEAM_FLOW_DEMO_GUIDE.md)
- [Ôn tập vấn đáp cuối kỳ (một file duy nhất: kiến thức chapter, luồng code, phân công, câu hỏi–đáp)](docs/ON_TAP_VAN_DAP_CUOI_KY.md)
- [Ôn kiểm tra code giữa kỳ (MVC, JavaMail Chapter 14)](docs/ON_TAP_GIUA_KY_WEB_2026.md)

## Phòng thực hành nối dây (Đợt 7, chặng 1)

USER vào `/wiring`: tạo nháp, nối chân bằng SVG hoặc form radio, lưu/tiếp tục,
nộp/xem giải thích/luyện lại. Nhấn hai chân, kéo thả và bàn phím là tiện ích
view; form vẫn chạy khi tắt JavaScript. ADMIN `/admin-wiring`: biên soạn nháp,
xem trước, công bố bản bất biến, nhân bản và lưu trữ.

Sau migration 010, chạy bằng tài khoản DDL trên đúng database:

```sql
SOURCE database/migrations/011_wiring_practice.sql;
SOURCE database/seed-wiring-phase7.sql;
```

Hai bài chính thức: `wiring-line-follower-v2`, `wiring-obstacle-avoider-v2`.
Seed chỉ thêm, chạy lại không sửa bài đã công bố. Chỉ chấm cặp trực tiếp được
khai báo; không mô phỏng điện hay xác nhận robot thật. Điểm =
HALF_UP(100 × max(C − W, 0) / N, 1), chỉ đúng toàn bộ khi C=N và W=0.
Không đổi nhiệm vụ/rubric A/B. Xem [thiết kế](docs/WIRING_DESIGN.md),
[kiểm chứng chặng 1](docs/PHASE7_WIRING_QA_REPORT.md).

### Hỗ trợ phiên nối dây Admin–User (Đợt 7, chặng 2)

USER gửi câu hỏi từ lượt đã lưu, xem phản hồi, gửi cập nhật/bản chụp mới và đánh dấu
giải quyết. ADMIN xem đúng bản chụp tại lúc gửi, phản hồi hoặc đóng hỗ trợ; không sửa
dây, nộp thay hay đổi điểm. Tin nhắn/bản chụp cũ bất biến, hỗ trợ độc lập với Mẫu A/B.

Sau migration 011, chạy `database/migrations/012_wiring_support.sql` trên đúng schema.
Migration chỉ thêm hai bảng, chạy lại không xóa/ghi đè lịch sử. Route USER:
`/wiring-support`; ADMIN: `/admin-wiring-support`. Lối vào theo vai trò ở trang tài khoản
và từ lượt nối dây. Báo cáo: [QA hỗ trợ](docs/PHASE7_WIRING_SUPPORT_QA_REPORT.md).
