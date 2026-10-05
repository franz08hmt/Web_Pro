# Đợt 7 — QA hỗ trợ nối dây, chặng 2

Ngày 05/10/2026, nhánh integration/fullstack-v2. Chặng 1 đã review đạt và commit
a1822fd trước khi mở chặng 2. Các commit Đợt 6 đã push c2afc9b theo yêu cầu trực tiếp;
hai commit Đợt 7 chỉ lưu local, không push.

## Phạm vi và thiết kế

Thêm hai bean WiringSupportRequest/Message, WiringSupportDB, hai Servlet hỗ trợ,
ba JSP form thuần, CSS scope riêng, lối vào account và lượt nối dây. WiringDB chỉ
thêm đọc trên kết nối caller và khóa bài, không sửa công thức/luật lưu-nộp.
Migration 012 thêm hai bảng request/message và fold schema; không sửa 011 đã chạy.
Snapshot MEDIUMTEXT + cặp ID do server dựng; SVG lấy bài công bố bất biến và dây
của từng tin. FK RESTRICT, UNIQUE active_attempt_id; không UPDATE/xóa lịch sử tin.
Khóa bài → lượt → request, form version; Admin không sửa điểm/dây/owner/nộp thay.

## Test và kiểm chứng thật

- Baseline chặng 2: Node 127, JUnit 72. Test mới viết trước implementation:
  Node đỏ vì route/DB chưa có, JUnit đỏ vì bean chưa có; bằng chứng giữ ngoài Git.
- Sau triển khai: Node 128/128, JUnit 79/79; bao gồm JavaBean/EL, guard course/format/HTML.
  Bảy test hỗ trợ kiểm trạng thái, stale, trim/độ dài, snapshot độc lập, điểm SUBMITTED,
  độ dài Unicode đồng nhất MySQL (test đỏ rồi sửa), trường hợp 40 chân/80 dây với nhãn UTF-8 vượt 65KB.
- Tomcat 9 + MySQL thật, loopback 8081, ADMIN + builder + practice (+ student login):
  **85 ca API/SQL đạt**. Khách redirect/no-store; quyền/CSRF 403; khác owner GET/POST404;
  ID/action/độ dài/version/đầu nối khác bài422. Tạo đồng thời đúng một302/một422 và SQL
  đúng một active; phản hồi đồng thời đúng một302/một422, version tăng một lần.
- USER gửi câu hỏi/bản chụp, Admin ANSWERED, USER cập nhật OPEN, USER/Admin CLOSED;
  CLOSED không nhận thêm tin, tạo yêu cầu mới được. Admin không chụp nguồn thay User.
  Client gửi snapshotText/score/userId giả không đổi bản chụp do server dựng.
- SQL: snapshot cũ không đổi khi nháp nguồn được lưu, bản cập nhật có version/cặp mới;
  mọi điểm/state/version của lượt SUBMITTED giữ nguyên sau hỗ trợ; toàn bộ bảng cũ
  trước wiring có hash không đổi. Cột snapshot_text trên DB thực tế là MEDIUMTEXT.
- Chrome thật **12 ca đạt**: JS-off lưu → hỏi → Admin xem cùng bản chụp/SVG → phản hồi →
  sửa/lưu → gửi snapshot mới → giải quyết. Hai SVG cũ/mới có một/hai dây riêng, snapshot
  cũ giữ nguyên. Hai USER khác nhau, Admin; 390px không tràn ngang toàn trang; nội dung
  HTML hiển thị chữ; account links đúng vai trò; không có pageerror. Đã xem hai ảnh.
- Hồi quy: so 14 trang/API cũ với bản chặng 1, diff rỗng sau chuẩn hóa token CSRF và
  thời điểm lập hồ sơ: tasks, diagnosis, summary/profile, order-history, lắp ráp/3D,
  quiz/tra cứu/shop/cart, receipt, API cart/quiz questions. Năm trang admin cũ 200.
  Đây là GET smoke/so HTML và bộ test; không tuyên bố thực hiện lại toàn bộ mua hàng,
  lắp ráp 3D hay nộp nhiệm vụ A/B trong đợt này.

## Review và xử lý

Reviewer độc lập đọc source, chạy guard 7/7 và đọc bằng chứng 85API/12Chrome/ảnh;
không tự chạy lại server/browser. Finding MEDIUM snapshot TEXT có thể vượt 65KB đã
sửa thành MEDIUMTEXT, thêm worst-case test trước khi apply 012; CHECK NULL và indent
đã sửa. Review cuối không còn finding nghiêm trọng. QA ban đầu cũng bắt cột tên User
phải là display_name (đã sửa), rollback trước transaction làm foreign404 thành503
(đã guard autoCommit, chạy lại âm tính đạt); IDE class69 được khắc phục bằng clean
package JDK17. Log runtime kiểm cuối không có SEVERE/exception ứng dụng.

## Dữ liệu và giới hạn

Không tạo tài khoản, không đổi role/password hay dữ liệu học tập cũ. QA chỉ thao tác
chức năng mới: thêm các lượt #15–18 (15/16 là nháp của lần QA dừng sớm); hỗ trợ #1–5,
tin #1–15. #2 còn OPEN trên lượt17, các yêu cầu khác CLOSED; nội dung có tiền tố [QA].
Không tự xóa/dọn dữ liệu DB; người dùng quyết định quản lý. Các dữ liệu chặng 1 #1–14,
bài QA#5/#6 và hai bản seed v1 ARCHIVED vẫn giữ nguyên như báo cáo chặng 1.

Chưa kiểm Firefox/Safari hoặc thiết bị cảm ứng vật lý; Chrome mobile/touch được
kiểm trong chặng 1 và viewport390/JS-off chặng 2. Đây là hỗ trợ văn bản và dữ liệu
mô phỏng, không phải chat realtime, điều khiển robot hoặc đồng chỉnh sửa.
Bằng chứng không bí mật ở D:/Web-Pro/qa-evidence/phase7-wiring/support, ngoài Git.
WAR cuối clean package JDK17 thành công: 79/79 JUnit, Node128/128; GET chín trang
USER/Admin mới từ WAR cuối đều200/EL đúng. Kiểm thêm bốn biên Unicode 19/20/2000/2001
ký tự: 422/302/302/422, SQL lưu đúng2000 ký tự; tổng hỗ trợ85+4 ca API và12+2Chrome.
String.codePointCount là method cổ điển của String, giúp giới hạn bean và CHAR_LENGTH
khớp nhau; không thay kiến trúc hay dùng cú pháp hiện đại. #2 hiện OPEN, version4.
Textarea cho tối đa4000 đơn vị UTF-16 để không chặn2000 ký tự có surrogate pair;
bean vẫn kiểm đúng tối đa2000 ký tự Unicode, mọi nội dung vượt ngưỡng bị422.
Tomcat QA đã dừng; cổng8081 không còn listener, process riêng đã thoát; cấu hình,
log/cookie/npm tạm đã dọn, bằng chứng không bí mật giữ ngoài Git. Không đụng8080.
Đủ điều kiện commit local chặng2, không push; hash ghi trong bàn giao cuối.

Hai ca Chrome JS-off bổ sung: textarea nhận đủ2000 ký tự Unicode (3995 đơn vị UTF-16), gửi thành công; 2001 ký tự bị server422. Bản WAR cuối và log được kiểm lại, runtime đã dọn.
