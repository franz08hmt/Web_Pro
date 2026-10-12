# Tích hợp thư viện và lịch sử chẩn đoán — 12/10/2026

## Nguồn và cách bảo toàn

- Đích: `integration/fullstack-v2`, baseline `fb07191`.
- Nhi: `origin/feature/nhi-library`, `6cff9d8`.
- Tuấn Anh: `origin/feature/practice-diagnosis-history`, `bc689cd`.
- Merge thực, giữ lịch sử hai thành viên; không reset/force-push/cherry-pick
  đè toàn bộ cây nguồn. Backup local: `backup/fullstack-v2-before-team-20261012`.
- Dùng checkout tích hợp riêng; giữ nguyên bảy đường dẫn sửa dở và bốn file
  untracked của người dùng. `.env`/IDE/config/log/target không thuộc commit.
- Không đổi Java 17/Tomcat 9/Servlet-JSP-JavaBean/JDBC/ConnectionPool; không
  đổi schema/migration, form ghi, thuật toán chấm, lắp ráp 3D hoặc shop.
- Seed thư viện bổ sung được giữ trong Git nhưng **không chạy** trên DB hiện tại.

## Review và các sửa tích hợp

1. Giữ API client thư viện cũ trả mảng/cache; tách hàm phân trang trả data/meta.
   Lọc/phân trang giữ trạng thái khi tải lỗi, không nói dữ liệu cũ là fallback;
   khóa cả pager để ngăn click nhanh/race. Lỗi tải danh mục robot được thông báo.
2. Thêm CSS scope riêng `library.css`, không sửa CSS chung; giữ layout, font,
   nhãn bàn phím/mobile và cache-busting. Xác nhận các asset local trong seed tồn tại.
3. Lịch sử dùng shell JSP chung/Poppins/Heroicons, core JSTL và `c:out`; bỏ
   fmt/CSS inline của trang mới, không đưa logic chấm vào JSP.
4. `DiagnosisHistoryItem.useAttempt` gọi luật duy nhất `DiagnosisAttempt`;
   DB chỉ hydrate dữ kiện, không tự tạo công thức/điểm 0 giả cho dữ liệu sai.
   Ngày Việt Nam và điểm hiển thị bằng getter; bean phân trang giới hạn trang.
5. Giữ null-guard kết quả nhưng loại phần nguyên nhân/cách xử lý bị lặp.
   Danh sách luyện độc lập chỉ lấy PRACTICE; không thay phương thức POST TASK.
6. Thêm BEANS map cho hai biến JSP mới. Guard shell cập nhật số trang 34→35
   vì trang history mới; vẫn kiểm tất cả trang, không bỏ assertion hay skip.

## Test và QA

- Baseline checkout sạch: Node 135 đạt + 1 skip (chưa có exploded WAR),
  JUnit 79/79 với JDK 17. Không dùng target Java 25 của IDE ở checkout người dùng.
- Raw merge tái hiện 4 lỗi guard; test business mới đỏ trước triển khai.
- Sau sửa: **Node 160/160, không skip; JUnit 85/85**; Maven clean package JDK17
  thành công. JavaBean, EL, form contract, UTF-8, slide/120/140 và guard 3D giữ xanh.
- 79 kiểm live + 17 kiểm bổ sung = **96 kiểm đạt**, Chrome/Tomcat9/MySQL,
  QA riêng loopback8081. Lịch sử ba USER đối chiếu count và điểm với SQL,
  mở lại kết quả, foreign result404, owner-query không đổi dữ liệu,
  page sai422/trang lớn giới hạn; trạng thái rỗng và JS-off.
- Thư viện live: options/filter/page size/count; mobile320/390/768/1440 không
  tràn toàn trang. Chuyển trang và lỗi API dùng thêm response fixture được
  đánh dấu QA, không thêm tài nguyên vào database để ép đủ số trang.
- Hồi quy GET ba USER: learning-summary/profile/tasks/wiring/3D/shop200.
  Core source lắp ráp/config/drive/API/CSS chung/schema/migration/pom không đổi.
- SHA-256 và số dòng **38/38 bảng trước/sau giống nhau**. Không tạo tài khoản,
  không seed/migration, không viết điểm/lượt/bài nộp hay sửa dữ liệu người dùng.
- Review độc lập snapshot code: không còn blocker; 42 Node checks chọn lọc
  và guard Java120/JSP140 đạt, không sửa source bởi reviewer.
- Bằng chứng không bí mật: `D:/Web-Pro/qa-evidence/team-merge-20261012/`;
  ảnh desktop/mobile đã xem. Cookie/mật khẩu chỉ ở bộ nhớ, không nằm trong report.
- Tomcat QA đã dừng, cổng8081 đóng; thư mục runtime/config/log tạm đã xóa,
  không có SEVERE/JasperException. WAR kiểm chứng được giữ cùng bằng chứng.

## Giới hạn

- Không đăng nhập được ADMIN demo bằng thông tin có sẵn; không đổi/reset mật
  khẩu để QA. Luật cấm ADMIN và kiểm role có source/guard cũ nhưng chưa thực
  hiện lại trực tiếp bằng phiên ADMIN trong lần này.
- Chưa Firefox/Safari, screen reader hoặc cảm ứng vật lý. Hồi quy cũ là GET
  và test, không thực hiện lại toàn bộ lắp 3D/checkout/nộp-chấm nhiệm vụ.
- Bản build QA dùng JDK17; cấu hình IDE/Maven và target đang có của người dùng
  được giữ nguyên. Nếu IDE vẫn dùng Java25, cần chọn lại JDK17 khi rebuild.
- Nhánh tích hợp hoàn tất local; không tự push khi yêu cầu hiện tại chỉ merge.
