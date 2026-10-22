# Robot Assembly Lab

Robot Assembly Lab là website hỗ trợ người học chọn mẫu robot, chuẩn bị linh kiện và tự lắp từng vật thể trong phòng 3D có server kiểm chứng. Sau khi hoàn tất, người học xem lại mô hình đã lưu, quy trình lắp, phiếu kết quả và làm bài kiểm tra kiến thức của mẫu đó.

Đồ án môn **Lập trình Web — Nhóm 4**, xây dựng bằng **Java Servlet/JSP, Tomcat 9 và MySQL**.

## Trạng thái dự án

Phiên bản hiện tại có năm mẫu: Robot dò đường, Robot tránh vật cản, Cánh tay robot mini, Robot dò line kết hợp tránh vật cản và Robot quét hướng tránh vật cản.

- Lắp ráp từng vật thể: kéo-thả hoặc chọn-gắn, xoay hướng, tháo, hoàn tác và lưu bố cục tại server.
- Trang hoàn thiện: mô hình 3D đúng phiên, bản vẽ 2D, nhật ký, phiếu kết quả, phát lại và đường vào quiz.
- Xưởng thiết kế: tự chọn linh kiện, vào phòng 3D ngay sau khi tạo, kiểm tra tương thích và in hồ sơ thiết kế.
- Các chức năng hỗ trợ: nối dây, chẩn đoán, tra cứu lỗi, nhiệm vụ thực hành, bộ linh kiện và cửa hàng mô phỏng.

Các báo cáo trong `docs/` ghi lại kết quả kiểm chứng từng phiên bản; số test và trạng thái triển khai trong báo cáo chỉ áp dụng cho lần kiểm được ghi ở đó.

## Phạm vi nghiệp vụ

### Luồng chức năng chính

1. **Lắp theo mẫu:** đăng nhập → chọn mẫu → chuẩn bị linh kiện → vào phòng 3D → lắp và nhận phản hồi từ server → hoàn tất.
2. **Học sau khi lắp:** xem mô hình đã lưu, đối chiếu bước chuẩn và trình tự đã làm → xem phiếu → làm quiz đúng mẫu.
3. **Tự thiết kế:** chọn linh kiện từ bộ cá nhân hoặc thư viện → tạo thiết kế → vào phòng 3D → lắp, kiểm tra và xem hồ sơ vật tư/bản vẽ.

### Chức năng hỗ trợ

| Nhóm | Nội dung |
| --- | --- |
| Tài khoản và kết quả | Đăng ký, đăng nhập, dashboard, tổng kết học tập và hồ sơ in/PDF |
| Thực hành | Bài nối dây, hỗ trợ Admin–User, chẩn đoán và tra cứu lỗi theo mẫu |
| Nhiệm vụ | Giao nhiệm vụ, thu bằng chứng, nộp bài, phản hồi và đánh giá theo rubric hiện có |
| Tài nguyên | Danh mục linh kiện, thư viện, bộ linh kiện cá nhân và nhập từ đơn đã xác nhận |
| Cửa hàng mô phỏng | Sản phẩm, giỏ hàng, kiểm tồn kho, tạo đơn và lịch sử đơn |
| Quản trị | Quản lý nội dung, quiz, bài thực hành, nhiệm vụ, người dùng và thống kê |

### Giới hạn mô phỏng

- Kiểm tra theo luật lắp ghép và cấu hình đã công bố; không mô phỏng điện, vật lý hoặc firmware.
- Xưởng chỉ kiểm chứng bộ linh kiện được hỗ trợ. Linh kiện chưa có luật được báo rõ, không tự coi là đúng.
- Thiết kế riêng không được tính là hoàn thành mẫu chính thức hoặc bằng chứng nhiệm vụ.
- Bản vẽ và hồ sơ là tài liệu tham khảo, không thay thế kiểm tra thực tế khi lắp robot.
- Cửa hàng không thu tiền, không tích hợp thanh toán hoặc giao hàng thật.

## Kiến trúc kỹ thuật

Ứng dụng dùng **Model 2 (MVC)**, giữ các lớp Servlet, JavaBean và JDBC theo cấu trúc môn học.

| Thành phần | Công nghệ và trách nhiệm |
| --- | --- |
| Giao diện | HTML5, CSS3, JavaScript, JSP/JSTL core/EL; Three.js cục bộ cho 3D |
| Controller | Java 17, Servlet API `javax.servlet`, chạy trên Tomcat 9 |
| Business | JavaBean giữ dữ liệu và luật nghiệp vụ |
| Data | Các lớp `XxxDB` static, JDBC, `PreparedStatement`, `ConnectionPool`, `DBUtil` |
| Database | MySQL 8, khóa ngoại, transaction và khóa khi cập nhật bố cục |
| Xác thực | `HttpSession`, cookie `JSESSIONID`, phân quyền và ownership tại server |
| Bảo vệ thao tác | CSRF, kiểm phiên bản bố cục và `Cache-Control: no-store` cho dữ liệu cá nhân |

```text
Trình duyệt
    | HTTP request
    v
Servlet (controller)
    | gọi XxxDB và JavaBean
    v
XxxDB + ConnectionPool + DBUtil <--> MySQL
    | dữ liệu trong JavaBean
    v
request attribute --> JSP/JSTL --> HTML
hoặc JSON response --> JavaScript --> giao diện tương tác
```

Các trang tổng hợp và form dùng Servlet → JSP; catalog HTML và phòng 3D gọi API cùng origin. JavaScript dựng hình và điều khiển tương tác; server quyết định đúng/sai, điểm và trạng thái lưu.

**Node.js chỉ dùng chạy kiểm thử nguồn, không chạy backend.** Dự án không dùng Express, Spring hoặc ORM.

## Mô hình dữ liệu

| Nhóm bảng | Dữ liệu chính |
| --- | --- |
| `users` | Tài khoản, mật khẩu băm, vai trò và phiên bản đăng nhập |
| `robots`, `components`, `robot_components`, `assembly_steps` | Mẫu robot, linh kiện, số lượng và bước chuẩn |
| `assembly_sessions`, `session_components`, `session_steps` | Phiên học và tiến độ chuẩn bị theo người dùng |
| `assembly_specs`, `assembly_slots`, `assembly_slot_orientations` | Phiên bản cấu hình lắp và hướng hợp lệ |
| `assembly_instances`, `assembly_placements`, `assembly_events` | Vật thể, bố cục đã lưu và nhật ký thao tác |
| `quiz_*`, `troubleshooting_guides`, `library_resources` | Kiểm tra kiến thức và tài nguyên học tập |
| Các bảng nhiệm vụ, nối dây và chẩn đoán | Bài thực hành, lượt làm, bằng chứng và đánh giá |
| `design_*`, `robot_designs`, `user_kit_items` | Luật Xưởng, thiết kế cá nhân và bộ linh kiện |
| `shop_products`, `cart_items`, `orders`, `order_items` | Sản phẩm, giỏ hàng và đơn mô phỏng |

Schema chi tiết và quan hệ: [docs/erd.md](docs/erd.md). Phiên cũ giữ spec/luật đã gắn tại lúc tạo; không lấy bản mới nhất để chấm lại lịch sử.

## Trang và API chính

| Chức năng | Trang người dùng | API chính |
| --- | --- | --- |
| Tài khoản | `/pages/tai-khoan.html`, `/account` | `/api/auth/*` |
| Mẫu và linh kiện | `/pages/mau-robot.html`, `/pages/linh-kien.html` | `/api/robots`, `/api/components` |
| Lắp theo mẫu | `/pages/lap-rap.html`, `/pages/lap-rap-3d.html` | `/api/assembly-sessions/*` |
| Kết quả lắp | `/assembly-review`, `/assembly-receipt` | Dữ liệu chủ phiên do Servlet đọc và forward JSP |
| Quiz | `/pages/kiem-tra.html` | `/api/quiz/*` |
| Xưởng và bộ linh kiện | `/designs`, `/my-kit`, `/design-sheet` | `/api/designs/*` |
| Thực hành | `/wiring`, `/diagnosis`, `/tasks` | Form Servlet và các luồng hiện có |
| Cửa hàng | `/pages/cua-hang.html`, `/pages/gio-hang.html`, `/order-history` | `/api/shop/products`, `/api/cart`, `/api/orders` |
| Kết quả và bản đồ môn học | `/learning-summary`, `/learning-profile`, `/architecture` | Servlet → JSP |

`/robots` và `/components` vẫn được giữ làm ví dụ catalog render bằng Servlet/JSP. Menu, quyền truy cập và toàn bộ route được mô tả tại [SITE_MAP](docs/SITE_MAP.md); hợp đồng JSON tại [API_CONVENTIONS](docs/API_CONVENTIONS.md).

## Phân công nhóm

| Thành viên | Mảng phụ trách | File tiêu biểu |
| --- | --- | --- |
| Huỳnh Minh Tài | Nền tảng Servlet/Tomcat, xác thực và phân quyền, phiên học, nhiệm vụ, hồ sơ, cửa hàng và tích hợp | `AuthServlet`, `AssemblySessionServlet`, `TaskServlet`, `LearningProfileServlet`, `CartDB`, `OrderDB` |
| Văn Phạm Thảo Nhi | Cơ sở dữ liệu, nội dung robot/linh kiện/thư viện, quản trị nội dung, quiz và thống kê | `RobotDB`, `ComponentDB`, `QuizServlet`, `StatsDB`, `database/` |
| Phạm Tuấn Anh | Tương tác lắp ráp 3D, thực hành nối dây, chẩn đoán, hỗ trợ và responsive | `assembly-3d.js`, `WiringServlet`, `WiringExercise`, `DiagnosisServlet` |

Phân công và luồng code chi tiết nằm trong [tài liệu vấn đáp](docs/ON_TAP_VAN_DAP_CUOI_KY.md).

## Cấu trúc thư mục

```text
robot-engine-website/
|-- index.html
|-- pages/                         Trang HTML và giao diện tương tác
|-- assets/                        CSS, JavaScript, ảnh và Three.js cục bộ
|-- tomcat-app/
|   |-- pom.xml
|   `-- src/
|       |-- main/java/vn/edu/webpro/robotlab/
|       |   |-- controller/         Servlet điều phối request
|       |   |-- business/           JavaBean và luật nghiệp vụ
|       |   |-- data/               XxxDB, ConnectionPool và DBUtil
|       |   |-- util/               Tiện ích kỹ thuật
|       |   `-- filter/             Bộ lọc request, quyền và lỗi
|       |-- main/webapp/
|       |   |-- META-INF/context.xml
|       |   `-- WEB-INF/views/      JSP và các phần dùng chung .jspf
|       `-- test/java/              JUnit
|-- database/                      Schema, migration và seed
|-- tests/server/                  Kiểm thử nguồn và hành vi JavaScript
|-- docs/                          Kiến trúc, API, ERD, demo và báo cáo QA
|-- .env.example                   Mẫu thông số kết nối, không chứa bí mật thật
|-- package.json                   Lệnh kiểm thử Node.js
`-- README.md
```

## Quy trình Git

- `main` là bản tích hợp để bàn giao; phát triển và review trên nhánh riêng.
- Không đưa thay đổi đang làm dở vào commit phát hành.
- Trước khi tích hợp, chạy kiểm tra phù hợp và `git diff --check`.
- Không commit `.env`, cấu hình IDE cá nhân, mật khẩu, token, DB backup hoặc artifact build.
- Lịch sử triển khai và kiểm chứng được lưu trong các báo cáo QA, không dùng README làm nhật ký từng đợt.

## Chạy trên máy mới (Windows / IntelliJ)

Yêu cầu: **JDK 17, Maven, Tomcat 9 và MySQL 8**. Node.js 20+ chỉ cần khi chạy bộ kiểm thử Node.

### 1. Tạo database và nạp danh mục

Chỉ làm phần này với **database mới, rỗng**. Dùng tài khoản cài đặt có quyền DDL; tài khoản chạy ứng dụng chỉ cần quyền DML phù hợp trên database ứng dụng.

Mở MySQL CLI từ thư mục gốc repo, kết nối UTF-8 và nhập mật khẩu khi được hỏi:

```powershell
mysql --default-character-set=utf8mb4 -u tai_khoan_cai_dat -p
```

Trong MySQL, tạo và chọn database mới rồi chạy theo thứ tự:

```sql
CREATE DATABASE robot_assembly_lab
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE robot_assembly_lab;

SOURCE database/schema.sql;
SOURCE database/seed.sql;
SOURCE database/seed-robots-phase3.sql;
SOURCE database/seed-quiz.sql;
SOURCE database/seed-quiz-phase3.sql;
SOURCE database/seed-troubleshooting.sql;
SOURCE database/seed-troubleshooting-phase3.sql;
SOURCE database/seed-shop.sql;
SOURCE database/seed-library-resources-phase3.sql;
SOURCE database/seed-assembly-spec-obstacle-avoider.sql;
SOURCE database/seed-assembly-spec-obstacle-avoider-v2.sql;
SOURCE database/seed-assembly-spec-all-models.sql;
SOURCE database/seed-design-rules-v1.sql;
SOURCE database/seed-design-rules-v2.sql;
```

`schema.sql` đã gộp cấu trúc hiện tại; **không chạy lại migration 001–016 trên database vừa tạo bằng schema này**. Hai seed chẩn đoán/nối dây thực hiện sau khi có ADMIN, ở bước 4.

Với database đang có dữ liệu: backup, kiểm `schema_migrations`, đọc migration và chỉ áp dụng phần còn thiếu theo thứ tự. Ví dụ `008_shop_cart_orders.sql` thêm cửa hàng, `013_assembly_placement.sql` thêm lắp chi tiết, `014_assembly_events.sql` thêm nhật ký, `015_robot_design.sql` thêm Xưởng và `016_component_name_fix.sql` sửa tên catalog. Không chạy lại schema, không sửa spec PUBLISHED hoặc tiến độ cũ để nâng cấp.

### 2. Cấu hình Tomcat trong IntelliJ

1. Chọn Project SDK **JDK 17** và Tomcat **9**; giữ target biên dịch Java 17.
2. Tạo một cấu hình Tomcat Local. Trong **Deployment**, thêm WAR exploded của module `robot-assembly-lab-tomcat`, đặt **Application context** là `/`.
3. Trong **Server → VM options**, điền các giá trị kết nối của máy:

   ```text
   -DDB_HOST=127.0.0.1 -DDB_PORT=3306 -DDB_NAME=robot_assembly_lab -DDB_USER=ten_user_ung_dung -DDB_PASSWORD=mat_khau_cuc_bo
   ```

4. `META-INF/context.xml` dùng các system property này để tạo JNDI DataSource `jdbc/robotlab`. **Tomcat không tự đọc `.env`**; `.env.example` chỉ là mẫu thông số.
5. Bấm Run Tomcat rồi mở `http://localhost:8080/`. Không mở `index.html` trực tiếp hoặc dùng Live Server để thay luồng backend.

Không chạy thêm launcher/server khác trên cùng cổng với IntelliJ. Các launcher demo ngoài repo phụ thuộc môi trường riêng, không phải điều kiện để chạy dự án trên máy mới.

### 3. Kiểm tra kết nối

Mở `http://localhost:8080/api/health`; trường `database` cần là `"connected"`.

| Hiện tượng | Kiểm tra |
| --- | --- |
| Không truy cập được hoặc 404 | Tomcat đã chạy chưa, deployment và context có đúng `/` không |
| `database` là `"unavailable"` | MySQL, năm giá trị `DB_*`, quyền tài khoản và database đã chọn |
| Đăng nhập báo dữ liệu chưa sẵn sàng | Kết nối, cấu trúc bảng và log Tomcat; không tự chạy lại schema trên DB có dữ liệu |
| Class version `69.0`, yêu cầu `61.0` | Có class Java 25 trong output; kiểm compiler target và rebuild bằng JDK 17 khi output không bị runtime sử dụng |
| `localhost:1099` hoặc `8080` đã được dùng | Kiểm đúng tiến trình/cấu hình đang chạy; không mở hai Tomcat và không dừng tiến trình chưa xác minh |

### 4. Tài khoản quản trị và bài thực hành

Đăng ký qua website tạo tài khoản USER. Để bootstrap ADMIN đầu tiên, dùng tài khoản cài đặt trên đúng database, với email vừa đăng ký:

```sql
UPDATE users
SET role = 'admin', session_version = session_version + 1
WHERE email = 'admin@example.com';
```

Kiểm đúng một tài khoản được cập nhật, rồi đăng xuất/đăng nhập lại. Không chỉnh `password_hash`. Những lần quản lý quyền sau dùng `/pages/admin-users.html` theo quyền server hiện có.

Khi đã có ADMIN, dùng tài khoản cài đặt chạy:

```sql
SOURCE database/seed-diagnosis-phase6.sql;
SOURCE database/seed-wiring-phase7.sql;
```

Hai bài nối dây chính thức là `wiring-line-follower-v2` và `wiring-obstacle-avoider-v2`. Seed không tạo sẵn mật khẩu hoặc kết quả học tập; không import DB cá nhân của thành viên để thay việc cài đặt.

## Build và kiểm thử

Xác minh Maven thực sự dùng Java 17, sau đó chạy từ thư mục gốc repo:

```powershell
java -version
mvn -version
mvn -q -f tomcat-app/pom.xml test
mvn -q -f tomcat-app/pom.xml clean package
npm test
```

WAR đầu ra: `tomcat-app/target/robot-assembly-lab.war`. Tài nguyên HTML/CSS/JS ở repo được Maven đóng gói vào WAR; chỉnh source không tự đồng nghĩa Tomcat đã nhận artifact mới.

Nếu IntelliJ/Tomcat đang phục vụ `target`, build vào thư mục riêng để tránh ghi đè:

```powershell
mvn -q -f tomcat-app/pom.xml -Drobotlab.buildDirectory=D:/robotlab-build clean package
$env:ROBOTLAB_EXPLODED_DIR = 'D:/robotlab-build/robot-assembly-lab'
npm test
Remove-Item Env:ROBOTLAB_EXPLODED_DIR
```

Chọn thư mục build riêng chưa được runtime khác sử dụng. Bộ kiểm tra UTF-8 sẽ đối chiếu artifact tại `ROBOTLAB_EXPLODED_DIR`; kiểm thử nguồn không thay thế nghiệm thu trình duyệt trên đúng bản đang triển khai.

