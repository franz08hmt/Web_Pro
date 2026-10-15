# Bản đồ Robot Assembly Lab

## Khảo sát Đợt 11 (15/10/2026)

Nguồn: HEAD 191146b, 42 ánh xạ Servlet và 18 trang HTML (gồm trang chủ).
Baseline JDK17: 197 Node / 127 JUnit, 0 fail / 0 skip; clean package đạt.
Tomcat QA 8081 riêng: 120 GET (khách/USER/ADMIN): 86×200,19×302,10×403,4×404,1×422.
404 ở hồ sơ thiết kế khác chủ là kiểm quyền; 422 ở trang bài cần tham số không phải link menu chết.
Ảnh trước lưu ngoài Git: D:/Web-Pro/qa-evidence/phase11/before/.

Phát hiện: menu JSP và HTML khác nhau; hai phòng 3D không có menu chung;
Xưởng/bộ kit/quiz/tra cứu/đơn hàng không có đường vào thống nhất;
robots/components trùng trang catalog HTML; hồ sơ in xuất hiện như một chức năng riêng;
architecture chưa cho thấy luồng request/response cụ thể. Không phát hiện module cần xóa.

## Quyết định trang chính/phụ và M6

| Cặp trang/chức năng | Quyết định | Lý do |
| --- | --- | --- |
| /robots ↔ /pages/mau-robot.html | HTML là chính; JSP minh họa Model2, giữ route | Cùng dữ liệu, hai cách dựng giao diện |
| /components ↔ /pages/linh-kien.html | HTML là chính; JSP minh họa Model2, giữ route | Không bỏ ví dụ Servlet → JSP |
| /account ↔ /pages/tai-khoan.html | /account thành Bảng điều khiển; HTML giữ đăng nhập/đăng ký/chỉnh hồ sơ/lịch sử | Giữ cả hai hành vi, không chuyển hướng route có dữ liệu |
| /learning-summary ↔ /learning-profile | Menu Kết quả học tập → tổng kết; nút Bản in/PDF → hồ sơ | Bản in là thao tác của kết quả, không sửa file WIP |
| Shop/giỏ/đơn, nhiệm vụ, thư viện, chẩn đoán | Giữ tất cả và kết nối theo ngữ cảnh | Phục vụ học hoặc chuẩn bị linh kiện; không ẩn module có dữ liệu |

M6 gộp quyết định vào M2: không có chức năng lạc đề bị ẩn/xóa; chỉ hai trang minh họa
được bỏ khỏi menu theo yêu cầu, vẫn mở từ Bản đồ môn học.

## Menu đích (M1)

Nhóm mở bằng một lần bấm, chọn chức năng lần thứ hai. Cùng nhãn/thứ tự/URL ở HTML và JSP.
Nguồn kiểm tra duy nhất: tests/fixtures/site-nav.json (sẽ thêm ở M1).

- Học: Mẫu robot; Lắp ráp; Xưởng thiết kế.
- Luyện tập: Kiểm tra kiến thức; Nối dây; Hỗ trợ nối dây; Luyện chẩn đoán; Tra cứu lỗi; Nhiệm vụ.
- Tài nguyên: Linh kiện; Thư viện; Cửa hàng.
- Của tôi (đăng nhập): Bảng điều khiển; Bộ linh kiện; Thiết kế của tôi; Kết quả học tập; Đơn hàng; Giỏ hàng; Tài khoản.
- Giới thiệu: Thành viên; Bản đồ môn học.
- Quản trị (ADMIN): toàn bộ trang admin hiện có.
- Khách: đường vào đăng nhập luôn hiện; trang cần quyền vẫn kiểm ở server.

## Toàn bộ trang/route

Các API wildcard có nhiều resource con; hợp đồng chi tiết giữ tại API_CONVENTIONS.md.
Quyền trong bảng là điều kiện của dữ liệu/thao tác, không dựa vào việc ẩn menu.

| URL | Loại / method | Quyền | Chức năng | Vai trò trang | Đường vào |
| --- | --- | --- | --- | --- | --- |
| /index.html | HTML + API | Khách; dữ liệu riêng cần đăng nhập | Robot Assembly Lab — Hướng dẫn lắp ráp robot | Chính | Menu chung |
| /pages/admin-content.html | HTML + API | ADMIN (API kiểm quyền) | Quản trị nội dung — Robot Assembly Lab | Chính | Quản trị |
| /pages/admin-quiz.html | HTML + API | ADMIN (API kiểm quyền) | Quản trị bài kiểm tra — Robot Assembly Lab | Chính | Quản trị |
| /pages/admin-shop.html | HTML + API | ADMIN (API kiểm quyền) | Quản trị cửa hàng — Robot Assembly Lab | Chính | Quản trị |
| /pages/admin-troubleshooting.html | HTML + API | ADMIN (API kiểm quyền) | Quản trị tra cứu lỗi — Robot Assembly Lab | Chính | Quản trị |
| /pages/admin-users.html | HTML + API | ADMIN (API kiểm quyền) | Quản lý tài khoản — Robot Assembly Lab | Chính | Quản trị |
| /pages/cua-hang.html | HTML + API | Khách; dữ liệu riêng cần đăng nhập | Cửa hàng mô phỏng — Robot Assembly Lab | Chính | Menu chung |
| /pages/gio-hang.html | HTML + API | Khách; dữ liệu riêng cần đăng nhập | Giỏ hàng mô phỏng — Robot Assembly Lab | Chính | Menu chung |
| /pages/kiem-tra.html | HTML + API | Khách; dữ liệu riêng cần đăng nhập | Kiểm tra kiến thức | Chính | Menu chung |
| /pages/lap-rap-3d.html | HTML + API | Khách; dữ liệu riêng cần đăng nhập | Lắp ráp Robot 3D | Phòng tương tác | Lắp ráp / Thiết kế |
| /pages/lap-rap.html | HTML + API | Khách; dữ liệu riêng cần đăng nhập | Quy trình lắp ráp — Robot Assembly Lab | Chính | Menu chung |
| /pages/linh-kien.html | HTML + API | Khách; dữ liệu riêng cần đăng nhập | Danh mục linh kiện — Robot Assembly Lab | Chính | Menu chung |
| /pages/mau-robot.html | HTML + API | Khách; dữ liệu riêng cần đăng nhập | Danh mục robot — Robot Assembly Lab | Chính | Menu chung |
| /pages/tai-khoan.html | HTML + API | Khách; dữ liệu riêng cần đăng nhập | Tài khoản — Robot Assembly Lab | Chính | Menu chung |
| /pages/thanh-vien.html | HTML + API | Khách; dữ liệu riêng cần đăng nhập | Nhóm phát triển — Robot Assembly Lab | Chính | Menu chung |
| /pages/thu-vien.html | HTML + API | Khách; dữ liệu riêng cần đăng nhập | Thư viện kỹ thuật — Robot Assembly Lab | Chính | Menu chung |
| /pages/tra-cuu-loi.html | HTML + API | Khách; dữ liệu riêng cần đăng nhập | Tra cứu lỗi lắp ráp | Chính | Menu chung |
| /pages/xuong-thiet-ke.html | HTML + API | Khách; dữ liệu riêng cần đăng nhập | Xưởng thiết kế robot — Robot Assembly Lab | Phòng tương tác | Lắp ráp / Thiết kế |
| /account | Servlet → JSP; GET | Đăng nhập (owner khi có ID) | AccountPageServlet | Chính | Menu chung |
| /api/admin/components/* | JSON API; GET/POST/DELETE | ADMIN | AdminComponentServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /admin-diagnosis | Servlet → JSP; GET/POST | ADMIN | AdminDiagnosisServlet | Chính | Menu chung |
| /api/admin/library-resources/* | JSON API; GET/POST/DELETE | ADMIN | AdminLibraryResourceServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /api/admin/quiz/questions/* | JSON API; GET/POST/DELETE | ADMIN | AdminQuizServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /api/admin/robots/* | JSON API; GET/POST/DELETE | ADMIN | AdminRobotServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /api/admin/shop/products/* | JSON API; GET/POST/DELETE | ADMIN | AdminShopServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /admin-stats | Servlet → JSP; GET | ADMIN | AdminStatsServlet | Chính | Menu chung |
| /api/admin/steps/* | JSON API; GET/POST/DELETE | ADMIN | AdminStepServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /admin-task-reviews | Servlet → JSP; GET/POST | ADMIN | AdminTaskReviewServlet | Chi tiết | Lịch sử / Bảng điều khiển / kết quả |
| /admin-tasks | Servlet → JSP; GET/POST | ADMIN | AdminTaskServlet | Chính | Menu chung |
| /api/admin/troubleshooting-guides/* | JSON API; GET/POST/DELETE | ADMIN | AdminTroubleshootingServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /api/admin/users/* | JSON API; GET | ADMIN | AdminUserServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /admin-wiring | Servlet → JSP; GET/POST | ADMIN | AdminWiringServlet | Chính | Menu chung |
| /admin-wiring-support | Servlet → JSP; GET/POST | ADMIN | AdminWiringSupportServlet | Chính | Menu chung |
| /architecture | Servlet → JSP; GET | Khách | ArchitectureServlet | Chính | Menu chung |
| /assembly-receipt | Servlet → JSP; GET | Đăng nhập (owner khi có ID) | AssemblyReceiptPageServlet | Chi tiết | Lịch sử / Bảng điều khiển / kết quả |
| /assembly-review | Servlet → JSP; GET | Đăng nhập (owner khi có ID) | AssemblyReviewPageServlet | Chi tiết | Lịch sử / Bảng điều khiển / kết quả |
| /api/assembly-sessions/* | JSON API; POST/GET/PUT/DELETE | Đăng nhập (owner khi có ID) | AssemblySessionServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /api/auth/* | JSON API; GET/POST/PUT | Khách/đăng nhập tùy thao tác | AuthServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /api/cart/* | JSON API; GET/PUT/DELETE | Đăng nhập (owner khi có ID) | CartServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /components | Servlet → JSP; GET | Khách | ComponentCatalogPageServlet | Minh họa | Bản đồ môn học |
| /api/components | JSON API; GET | Khách | ComponentServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /api/designs/* | JSON API; GET/POST/DELETE | Đăng nhập (owner khi có ID) | DesignApiServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /designs | Servlet → JSP; GET/POST | Đăng nhập (owner khi có ID) | DesignPageServlet | Chính | Menu chung |
| /design-sheet | Servlet → JSP; GET | Đăng nhập (owner khi có ID) | DesignSheetServlet | Chi tiết | Lịch sử / Bảng điều khiển / kết quả |
| /diagnosis | Servlet → JSP; GET/POST | Đăng nhập (owner khi có ID) | DiagnosisServlet | Chính | Menu chung |
| /api/health | JSON API; GET | Khách | HealthServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /learning-profile | Servlet → JSP; GET | Đăng nhập (owner khi có ID) | LearningProfileServlet | Chính | Menu chung |
| /learning-summary | Servlet → JSP; GET | Đăng nhập (owner khi có ID) | LearningSummaryServlet | Chính | Menu chung |
| /api/library-resources | JSON API; GET | Khách | LibraryResourceServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /my-kit | Servlet → JSP; GET/POST | Đăng nhập (owner khi có ID) | MyKitServlet | Chính | Menu chung |
| /order-history | Servlet → JSP; GET | Đăng nhập (owner khi có ID) | OrderHistoryServlet | Chính | Menu chung |
| /api/orders | JSON API; GET/POST | Đăng nhập (owner khi có ID) | OrderServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /api/quiz/* | JSON API; GET/POST | Đăng nhập (owner khi có ID) | QuizServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /robots | Servlet → JSP; GET | Khách | RobotCatalogPageServlet | Minh họa | Bản đồ môn học |
| /api/robots/* | JSON API; GET | Khách | RobotServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /api/shop/products | JSON API; GET | Khách | ShopServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /tasks | Servlet → JSP; GET/POST | Đăng nhập (owner khi có ID) | TaskServlet | Chính | Menu chung |
| /api/troubleshooting-guides | JSON API; GET | Khách | TroubleshootingServlet | Hỗ trợ tương tác | Trang tương ứng gọi API |
| /wiring | Servlet → JSP; GET/POST | Đăng nhập (owner khi có ID) | WiringServlet | Chính | Menu chung |
| /wiring-support | Servlet → JSP; GET/POST | Đăng nhập (owner khi có ID) | WiringSupportServlet | Chính | Menu chung |

Các route có tham số (session, id, action…) chỉ mở chi tiết từ bản ghi thuộc người dùng.
Không tạo link menu tới một ID cố định hoặc lấy ID của người khác.
