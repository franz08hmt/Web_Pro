# Đợt11c — Hành trình học tập và giao diện phụ

Ngày16/10/2026. Khởi đầu `0280960`, nhánh `integration/fullstack-v2`.
Giữ hai commit cache/menu đã được review; không schema, endpoint mới, đổi quyền,
điểm, rubric, spec/luật công bố hoặc chức năng lắp ráp/checkout.

## Baseline và môi trường

- Node218/218, JUnit138/138,0 fail/skip; clean package JDK17 đạt.
- Đây là working tree có6 WIP sửa dở và4 file chưa theo dõi.12 SHA-256 đầu lượt
  (10 file đó cùng POM/settings workspace) lưu ngoài Git. Không sửa chúng.
- Tomcat8080 dùng target gốc: chỉ GET/đăng nhập tài khoản demo sẵn có khi khảo sát;
  không build/dừng/redeploy. Maven dùng `qa-evidence/phase11c/build` ngoài repo.
- Khảo sát lại17 trang trên8080, menu→quiz thiếu model tái hiện; không JS exception
  hoặc request ghi của trang. Báo cáo review độc lập được giữ nguyên.
- QA bản sửa dùng Tomcat riêng8081, localhost, artifact riêng `phase11c/deploy`.
  Phân biệt với8080 chưa được deploy các sửa11c. Không dùng số test của working
  tree để khẳng định bản commit sạch hoặc8080 đã chạy bản mới.
- .agents cấp workspace mô tả LogiRoute/Python; không áp dụng stack đó cho đồ án.
  Không bịa số slide; giải thích theo Servlet/JavaBean/JSTL/JDBC.

## Các sửa và giới hạn phạm vi

| Mốc | Thực hiện |
| --- | --- |
|M1|Quiz thiếu model thành bộ chọn; GET catalog nhẹ `loadRobotOptions`, GET questions hiện có để xác nhận câu hỏi. Không đáp án, không tạo lượt hoặc lịch sử model rỗng. Khách/503/rỗng/không có bài có thông báo và cách chọn/thử lại.|
|M2|LearningSummary tổng số phiên bằng for; Servlet giữ attribute cũ, chỉ gọi bean. Không đổi StatsDB hoặc công thức điểm.|
|M3|Dashboard3/2/1 cột ở1440/768/390, thẻ học trước, kit/đơn thành nhóm phụ; giữ đủ7 thẻ và đích cũ. CSS riêng, không đổi metric toàn site.|
|M4|Neo bài/lịch sử nối dây; count và LIMIT/OFFSET theo user_id, thứ tự id DESC,20 lượt/trang. Chẩn đoán bỏ khung form phụ, CTA chính trước link tra cứu. Tra cứu dùng details/summary, giữ nguyên văn và filter/context; chặn response cũ khi đổi lọc nhanh.|
|M5|Chỉ JSP: phân biệt nhãn nhiệm vụ với trạng thái cá nhân tại chi tiết; cờ nộp muộn từ bean. Danh sách có link mở trạng thái của em. Trạng thái cá nhân ngay từng thẻ danh sách chưa triển khai: GET TaskServlet chỉ truyền tasks, file WIP chưa được duyệt sửa. Không SQL/suy diễn điểm trong JSP.|
|M6|Nút bản in ở đầu tổng kết, link review từ phiên COMPLETED theo owner; quiz đúng model có dữ kiện câu hỏi/lượt cũ. Dùng truy vấn gộp có sẵn, không query mỗi dòng. LearningProfile không sửa.|
|M7|Chỉ đọc DB/HEX và trình duyệt; danh sách QA cùng đề xuất bên dưới, không dọn/ẩn dữ liệu.|

M5 đã gửi câu hỏi riêng về nhánh GET TaskServlet; chưa có phê duyệt thì giữ nguyên
WIP. Phần JSP an toàn đã commit, không gọi việc gắn trạng thái trên danh sách là xong.

## Dữ liệu QA và định nghĩa số liệu — không tự xử lý

Catalog nối dây dùng chung cho USER; đổi sang tài khoản demo riêng không loại
bài công bố số7 khỏi danh sách. Trình duyệt hiển thị `<b>` như chữ, không phải
lỗi XSS. DB bài7 có `scope_text` bắt đầu `Ch? ch?m...`; HEX bắt đầu
`43683F2063683F6D` xác nhận dấu hỏi ASCII đã nằm trong dữ liệu, không chỉ console
Windows. Mục tiêu bài7 có chuỗi X dài; giữ nguyên, không cắt/xóa bằng UI.

| Bài nối dây | Trạng thái | Ảnh hưởng |
| --- | --- | --- |
|5, qa-wiring-1791208067, [QA] Nối dây kiểm tra|ARCHIVED|Lịch sử/ADMIN; không phải bài công bố hiện tại|
|6, copy-5-1791208068485, [QA] Chỉnh nháp qua trình duyệt|DRAFT|Nháp ADMIN|
|7, copy-1-1791267517016, [UI-QA] Nối dây hiển thị dạng chữ|PUBLISHED|Catalog USER: HTML dạng chữ, chuỗi dài và scope mất dấu|

Nhiệm vụ không phải catalog công khai: người học chỉ thấy phần được giao;
ADMIN có nội dung và lịch sử chấm. Có21 nhiệm vụ tên QA tại thời điểm đọc:

| ID | Tiêu đề nhận diện | Trạng thái | Số người được giao |
| --- | --- | --- | --- |
|1|[QA] vòng nộp và chấm|OPEN|2|
|2|[QA] quiz, sửa chấm và tranh chấp|OPEN|3|
|3|[QA] quiz vòng1 bắt buộc|OPEN|1|
|4|[QA] mốc vòng1|OPEN|1|
|5|[QA] sai mẫu và chưa hoàn tất|OPEN|2|
|6|[QA] ngưỡng, đóng và lưu trữ|ARCHIVED|1|
|9|[QA] quyền riêng|OPEN|1|
|10|[QA] Escape (có thẻ HTML dạng chữ)|DRAFT|1|
|11|[QA] REJECT quá hạn|OPEN|1|
|12|[QA] REJECT chấm quá hạn|OPEN|1|
|13|[QA] ACCEPT nộp muộn|OPEN|1|
|14|[QA-B] Hai người học và chẩn đoán dùng lại|OPEN|2|
|15|[QA-B] Chẩn đoán mới ở vòng bổ sung|OPEN|1|
|16|[QA-B] Điểm thấp và giới hạn kết luận|CLOSED|1|
|17|[QA-B] Kiểm mốc bằng chứng vòng đầu|OPEN|1|
|18|[QA-B] Không nhận muộn|OPEN|1|
|19|[QA-B] Nhận bài có cờ muộn|OPEN|1|
|20|[QA-B] Form không JavaScript trên màn hình nhỏ|DRAFT|1|
|21|[UI-QA] Biên soạn mẫu A (HTML dạng chữ)|DRAFT|2|
|22|[UI-QA] Biên soạn mẫu B (HTML dạng chữ)|DRAFT|2|
|23|[QA] Phase8 detailed assembly evidence 1791796063296|OPEN|1|

Đề xuất chờ chủ dự án quyết định: DB demo riêng từ bản sao được duyệt, bộ tài
khoản/bài mẫu rõ ràng; dùng luồng nháp/nhân bản/đóng/lưu trữ phù hợp để giữ lịch
sử. Bài đã PUBLISHED không sửa snapshot; bản sửa cần đi theo luồng biên soạn
hiện có sau khi được duyệt. Không DELETE/UPDATE, ẩn bằng regex hay tự chạy seed.

StatsDB vẫn dùng MAX(score)/MAX(total_questions). Truy vấn chỉ đọc theo user và
robot không thấy nhóm nào có COUNT(DISTINCT total_questions)>1; chưa tái hiện
sai số trên dữ liệu hiện tại. Đây vẫn là nguy cơ khi số câu thay đổi; chưa thống
nhất lại định nghĩa tỷ lệ tốt nhất/gần nhất, không đổi công thức ở đợt UI.

## Cú pháp cũ ngoài phạm vi đã ghi nhận

| File:dòng | Dấu hiệu | Quyết định |
| --- | --- | --- |
|business/ShopOrder.java:29,30|stream/method reference tính tổng|Chưa refactor luồng đơn hàng|
|controller/AdminQuizServlet.java:136|stream/lambda kiểm form|Chưa refactor quản trị quiz|
|controller/AdminStatsServlet.java:55|stream tổng phiên|Chưa mở rộng sang trang ADMIN|
|controller/OrderServlet.java:67,73|switch mã/thông báo|Giữ contract checkout|
|util/JsonUtil.java:39|switch escape JSON|Giữ tiện ích JSON dùng toàn site|

Đường dẫn tương đối từ package vn/edu/webpro/robotlab. Đây là vi phạm phong cách
đã chốt, không tự kết luận lỗi chạy. Guard11c phủ các file Java/JSP mới sửa;
không tuyên bố toàn bộ source đã đúng giáo trình vì test xanh.

## Liên hệ kiến thức môn học

Request GET→Servlet lấy user/tham số→XxxDB dùng PreparedStatement theo owner
→JavaBean tổng hợp số phiên/chọn hành động/phân trang→setAttribute→JSP dùng
EL/JSTL/c:url/c:out/static include. Quiz/tra cứu giữ HTML+API; JS chỉ dựng DOM
và điều hướng, server vẫn quyết định quyền, kết quả và dữ liệu riêng tư.

Chi tiết test/hành trình, commit, artifact và giới hạn nghiệm thu được chốt
bên dưới sau vòng QA cuối. Bằng chứng ngoài Git: `D:/Web-Pro/qa-evidence/phase11c/`.

## Kiểm chứng cuối và bàn giao

| Bản được kiểm | Node | JUnit | Build |
| --- | --- | --- | --- |
|Working tree baseline0280960 có WIP|218|138|JDK17 clean package đạt, thư mục riêng|
|Working tree bản sửa11c có WIP|229|144|JDK17 test/clean package đạt, thư mục riêng|
|Archive commit sạchafa6ef2, không WIP|229|144|JDK17 clean package đạt tại clean-source|

Tất cả0 fail/skip. Thêm11 test Node (8 hành vi quiz/tra cứu và3 hợp đồng/guard),
6 JUnit (tổng hợp/actions, nhóm thẻ, phân trang). Test dashboard cũ vẫn kiểm
c:out nhưng theo static include sang dashboard-card.jspf, đồng thời kiểm include,
c:url và cấm scriptlet ở fragment; không bỏ/nới guard để ép xanh.

| Hành trình/ca kiểm | Bằng chứng thực tế |
| --- | --- |
|Menu→quiz không model→chọn mẫu→câu hỏi|USER; đúng obstacle-avoider, chưa có kết quả/đáp án trước nộp|
|Dashboard→quiz và mẫu hoàn thiện|CTA giữ context, GET review trả200 của chủ hiện tại|
|Nối dây→neo lịch sử→lượt đã có|USER lượt30 trả200; URL page quá cao vẫn có đường về trang hợp lệ|
|Tra cứu theo mẫu→Space mở details→chẩn đoán|Giữ robotId=obstacle-avoider; chọn mục có robot riêng, không gán robot cho mục dùng chung|
|Tổng kết→review/ôn tập/bản in|GET đúng mẫu/chủ; hồ sơ in vẫn /learning-profile, file WIP giữ hash|
|Nhiệm vụ→trạng thái cá nhân|Trang chi tiết dùng TaskRecipient; không kiểm POST hoặc coi danh sách đã có trạng thái riêng|
|Quyền quản trị tại server|Khách302, USER403, ADMIN200; không đổi role/backend|
|Chrome390/768/1440|57/57 ô: khách6, builder21, student21, ADMIN9; một H1, ID không trùng, menu Tab/Enter/Escape|
|Dashboard|3/2/1 cột, đủ7 thẻ; nội dung dài/cỡ chữ lớn DOM fixture không tràn|
|Zoom200% thật|Profile Chrome QA riêng: cửa sổ1440→720 CSS px,2 cột, đủ7 thẻ; profile đã dọn, không đổi Chrome người dùng|
|Quiz ca biên browser fixture|10 ca: rỗng/503/auth lỗi/không câu hỏi/chậm/model sai/direct hợp lệ;0 request ghi|
|Hồi quy đọc|27 kiểm gồm16 GET module,6 HTTP asset khớp source, replay37/Xưởng10 tải layout200, QA text/empty DOM/foreign owner404|
|Form nối dây/chẩn đoán/quiz|3 trang,0 khác biệt method/action/name/type/hidden/required/maxlength/minlength giữa8080 cũ và QA8081; CSRF chuẩn hóa|

Trong ma trận, student là USER ít dữ liệu, vẫn có6 phiên lắp; không gọi đây là
tài khoản hoàn toàn rỗng. Rỗng được kiểm bằng unit test và DOM fixture có đánh
dấu riêng. Ca21/41 lượt phân trang dùng JUnit cùng guard SQL owner→LIMIT/OFFSET;
chưa có lịch sử>20 của tài khoản demo để kiểm trang2 trực tiếp trên DB thật.
Không tạo lượt mới chỉ để đạt ca biên.

57 ô và16 kiểm hành trình/quyền có0 pageerror/asset404/request ghi của trang.
Login demo qua HTTP client là POST xác thực duy nhất. Replay/Xưởng chỉ tải dữ
liệu và xem, không kiểm lại toàn bộ phát timeline/thao tác ghi. Không nộp quiz,
nối dây, chẩn đoán, nhiệm vụ, lắp ráp hoặc checkout live; hồi quy ghi dựa vào
test hiện có, không gọi fixture là dữ liệu thật. Không kiểm Firefox/Safari,
cảm ứng vật lý hoặc screen reader. Không in/PDF lại toàn bộ hồ sơ; file và CSS
in của hồ sơ không sửa, GET/hành trình tới bản in đạt.

### Artifact, an toàn và dọn runtime

- QA8081 thực sự phục vụ artifact build từ working tree bản sửa trong thư mục
  riêng;6 resource HTTP khớp byte source. Version quiz.js/troubleshooting.js
  là20261016.1; CSS mới dashboard/practice-index cũng có version tại mọi nơi nạp.
  Bản đồ môn học trỏ DashboardSummary.java:56, đã kiểm qua HTTP thật.
- Archiveafa6ef2 được build/test riêng, không dùng kết quả working tree để gọi
  commit sạch đã đạt. Chưa chạy runtime archive sạch với DB thật.
-8080 PID30024 không bị dừng/restart/redeploy. GET quiz.js và troubleshooting.js
  vẫn200 nhưng chưa khớp source11c; không tuyên bố bản demo người dùng đã cập nhật.
  Cần người dùng tự Run/redeploy hoặc duyệt phối hợp riêng sau này.
-12/12 hash (10 WIP/untracked và POM/settings) giữ nguyên;55/55 hash bảng DB
  trước/sau giữ nguyên. Không sửa role/đơn/giỏ/tồn kho/phiên/nhật ký QA cũ.
- Đã dừng đúng PID QA tự tạo, xác minh runtime trước khi dừng, dọn conf/log/work/
  temp; không đụng8080. Browser QA/profile tạm đã đóng/dọn. git diff --check đạt
  trong phần sửa; không stage WIP/cấu hình/bằng chứng. Không push.

Bằng chứng: `browser-results.json`, ảnh trước `USER-_account-*` và sau
`after-USER-_account-*` (tương tự các trang), `fixture-results.json`,
`smoke-results.json`, `zoom-results.json`, `form-contracts.json`,
`db-before/after.json`, `data-findings.json`, `protected-before/after.json`,
`final-audit.json`, log Node/Maven và Surefire XML ở build/clean-source.
Các lần runner cần sửa chỉ là đồng bộ navigation, bỏ qua mục tra cứu dùng chung,
chuẩn hóa DOM form.action bị field action che; không sửa sản phẩm để né ca kiểm.

### Commit local

| Mốc | Hash | Message |
| --- | --- | --- |
|M0|c79d022|docs(qa): plan the phase11c learning journey review|
|M1|bab4b8c|fix(quiz): provide a valid quiz selection entry|
|M2|3dd3818|refactor(stats): use course-compliant session aggregation|
|M3|6e84bf2|fix(ux): prioritize dashboard learning actions|
|M4|b1ea9fa|fix(ux): clarify practice catalogs and paginate wiring history|
|M5 phần JSP|525b2d6|fix(ux): distinguish task and learner status in task views|
|M6|1e8fe59|fix(ux): link learning results to owned reviews and model quizzes|
|M7|2d03773|docs(qa): record shared QA content and unchanged score semantics|
|M8 source/guard|afa6ef2|fix(ux): guard learning journey syntax and refresh source references|

Commit tài liệu nghiệm thu cuối được báo hash khi bàn giao. Phần chờ: trạng thái
cá nhân trên danh sách nhiệm vụ (WIP), quyết định dữ liệu demo và deploy8080;
không gọi toàn bộ đề xuất đã hoàn tất.
