# Chuyển Robot Assembly Lab về Servlet/JSP trên Tomcat

## Mục đích

Phiên bản nộp môn học chạy trên Java 17 và Tomcat 9 theo MVC/Model 2 đã học:
Servlet là controller, Java service và DAO xử lý nghiệp vụ/dữ liệu, JSP là view
server-side, MySQL là nơi lưu dữ liệu. Bản Node/Express trước đây được giữ lại
trong giai đoạn chuyển đổi để đối chiếu chức năng, không còn là runtime mục tiêu.

## Quyết định

- Thêm module Maven WAR `tomcat-app/`; không xóa `server/`, `assets/`, `pages/`
  hoặc dữ liệu MySQL hiện có.
- Dùng `javax.servlet` để tương thích Tomcat 9, theo đúng template Chapter 02
  và Chapter 18 của học phần.
- Giữ response envelope hiện tại: thành công là `{ "data": ... }`, lỗi là
  `{ "error": { "code", "message", "requestId" } }`.
- Đưa truy vấn MySQL sang DAO JDBC có `PreparedStatement`. Không đặt SQL trong
  Servlet/JSP; database schema chỉ thay đổi theo migration bổ sung, không đổi
  tên/xóa bảng trong đợt chuyển đổi.
- Giao diện HTML/CSS/Three.js được đóng gói cùng WAR để browser và API cùng
  origin. JSP dùng cho các màn hình cần chứng minh luồng MVC; JavaScript vẫn có
  thể gọi `/api/...` cho trải nghiệm lắp ráp 3D.

## Luồng cần trình bày với giảng viên

```text
Browser
  | GET /architecture                    | GET /api/components
  v                                      v
ArchitectureServlet                  ComponentServlet
  | request.setAttribute(...)             | ComponentService
  v                                      v
WEB-INF/views/architecture.jsp       ComponentDao (PreparedStatement)
                                         |
                                         v
                                       MySQL
```

`GET /api/health` là lát cắt đầu tiên: `HealthServlet` gọi service kiểm tra
`SELECT 1`; response dùng JSON envelope. Vì vậy có thể chỉ thẳng controller,
service, data access và HTTP response khi được hỏi.

## Lộ trình chuyển dần

1. **Khung Tomcat:** WAR Maven, servlet health, JSP kiến trúc, đóng gói tài nguyên
   tĩnh; Java compiler và Tomcat 9 phải chạy được.
2. **Dữ liệu đọc:** robots, components, steps, library từ DAO JDBC; giao diện
   công khai đọc cùng contract `/api`.
3. **Tài khoản và phiên:** đăng ký/đăng nhập, HttpSession/cookie, DAO users và
   assembly sessions; không đưa mật khẩu/session token vào JSP hay response.
4. **Phòng 3D và quản trị:** JavaScript giữ vai trò client; Servlet cung cấp
   state phiên và CRUD admin có kiểm tra quyền/CSRF.
5. **Nghiệm thu:** Tomcat là runtime chính; chỉ sau khi API parity và kiểm thử
   xong mới đánh dấu Node là legacy, không xóa nếu chưa được nhóm chấp thuận.

## Chạy khi hoàn tất lát cắt đầu

Trong IntelliJ, import Maven project `tomcat-app/pom.xml`, tạo cấu hình
Tomcat Server > Local với Tomcat ở `C:\WebTools\apache-tomcat-9.0.120`, deploy
artifact `robot-assembly-lab-tomcat:war exploded` tại context `/`. Context gốc là
cần thiết ở lát cắt đầu vì giao diện hiện có gọi các URL tuyệt đối như `/api` và
`/vendor/three`.

Ở **Run > Edit Configurations > Tomcat Server > Local > Environment variables**,
điền năm biến `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` nếu cần
kiểm tra MySQL. Tomcat không tự đọc file `.env`; không dán mật khẩu vào source,
JSP hoặc Git.

- Kiểm tra JSON: `http://localhost:8080/api/health`
- Xem luồng MVC: `http://localhost:8080/architecture`

## Không làm trong đợt này

- Không xóa hoặc ghi đè Node/Express.
- Không chạy lại `database/schema.sql`, seed hoặc migration trên database đang
  có dữ liệu.
- Không commit mật khẩu hay file `.env`.
