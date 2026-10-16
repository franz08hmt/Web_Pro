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
|M5|Ban đầu chỉ JSP chi tiết; sau khi được duyệt riêng GET đã hiện trạng thái cá nhân ngay từng thẻ danh sách từ TaskRecipient/query gộp theo owner. Cờ muộn tách riêng; không SQL/suy diễn điểm trong JSP. Xem mục bổ sung M5 và bằng chứng stage hunk ở cuối.|
|M6|Nút bản in ở đầu tổng kết, link review từ phiên COMPLETED theo owner; quiz đúng model có dữ kiện câu hỏi/lượt cũ. Dùng truy vấn gộp có sẵn, không query mỗi dòng. LearningProfile không sửa.|
|M7|Chỉ đọc DB/HEX và trình duyệt; danh sách QA cùng đề xuất bên dưới, không dọn/ẩn dữ liệu.|

Ở lần bàn giao fc81a96, M5 đã gửi câu hỏi về GET TaskServlet và còn chờ duyệt;
chỉ phần JSP an toàn được commit. Bổ sung sau khi được duyệt có kết quả riêng
ở cuối báo cáo, không thay bằng chứng của lần kiểm trước.

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
|Nhiệm vụ→trạng thái cá nhân|Lần fc81a96 chỉ kiểm chi tiết TaskRecipient; mục bổ sung M5 bên dưới kiểm thêm danh sách sau duyệt GET|
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

Commit tài liệu nghiệm thu trước là fc81a96. Tại lần bàn giao đó còn chờ trạng
thái cá nhân trên danh sách nhiệm vụ, quyết định dữ liệu demo và deploy8080.

## Bổ sung M5 sau khi duyệt riêng GET TaskServlet — 16/10/2026

Người dùng đã duyệt bổ sung duy nhất nhánh GET danh sách, giữ nguyên WIP và
không stage cả TaskServlet. Đầu lượt HEADfc81a96, baseline229 Node/144 JUnit,
0fail/skip, package JDK17 riêng đạt. Sao lưu TaskServlet nguyên byte cùng diff
và12 hash đầu lượt mới tại `phase11c/task-status-followup`, ngoài Git.

### Thay đổi và bảo vệ WIP

- TaskServlet chỉ thêm2 dòng `setAttribute`/gọi
  `PracticeTaskDB.selectListRecipients(user.getId(), state)` trước forward danh sách.
  Không đọc userId từ URL, không tính nhãn ở Servlet hoặc JSP.
- Giữ truy vấn danh sách nhiệm vụ cũ và thêm một query metadata trạng thái:
  tổng2 truy vấn cố định, không tăng theo số nhiệm vụ. Các nhóm bài nộp/review
  đều lọc user_id; chọn MAX(submission_no) và MAX(review.id), đúng thứ tự đọc
  bài mới nhất/lần chấm cuối của trang chi tiết. State filter giữ semantics cũ.
- Map theo taskId chứa TaskRecipient với task, bài nộp gần nhất và kết luận
  hiện hành; `TaskRecipient.statusLabel` giữ nguyên luật. Không có bài thì bean
  quyết định Chưa nộp/quá hạn; thiếu review vẫn theo nhãn Chờ chấm có sẵn.
- JSP c:out nhãn cá nhân, tách trạng thái nhiệm vụ và cờ muộn từ `isLate` đã lưu.
  Metadata này chỉ phục vụ danh sách, không dùng cho nộp/chấm; không tải lịch sử
  từng nhiệm vụ hoặc đưa SQL/quy tắc điểm vào JSP.
- Index được dựng bằng patch chỉ2 dòng GET, kiểm `git apply --cached --check`
  và đối chiếu toàn bộ blob với HEAD cộng đúng hunk mới. Không git add TaskServlet.
  Commit source `2b4e5fd` không chứa3 dòng WIP cũ; sau commit TaskServlet vẫn M
  với đúng3 dòng thêm/3 dòng bớt có sẵn. Xóa riêng insertion trong bản đọc kiểm
  thì khôi phục chính xác byte WIP đầu lượt, không thực hiện revert trên file thật.
- POST byte-identical, TaskFormUtil không đổi;11 hash file bảo vệ/cấu hình còn
  lại giữ nguyên. Không schema, dữ liệu công bố, rubric, quyền hay luật mới.

### Kiểm chứng bổ sung

| Kiểm | Kết quả |
| --- | --- |
|Hai regression Node trước sửa|2 fail tái hiện thiếu dữ kiện/hiển thị; đạt sau sửa|
|Node toàn bộ|229→231,0fail/skip; working tree và archive sạch2b4e5fd cùng đạt|
|JUnit toàn bộ|144→149,0fail/skip; test/clean package17 working tree và commit sạch đạt|
|5 fixture JUnit ResultSet|Chưa nộp, chờ chấm, cần bổ sung+cờ muộn, thiếu review, hai user cùng task; chỉ bộ nhớ, không kết nối DB|
|Hai USER thật|Builder15 nhiệm vụ và practice4;19/19 nhãn danh sách khớp trang chi tiết hiện có|
|390/768/1440|6/6 ô đạt, một H1, không tràn, focus nhìn thấy, Enter mở đúng chi tiết|
|URL userId của người khác|Bị bỏ qua, danh sách vẫn của session hiện tại|
|Lọc OPEN/CLOSED/ARCHIVED/DRAFT|8 ca GET của2 USER đạt; DRAFT không lộ cho USER|
|Quyền cũ|Khách302, ADMIN403, nhiệm vụ không thuộc người dùng404|
|Ghi dữ liệu/lỗi JS|0 request ghi của trang,0 pageerror; chỉ đăng nhập tài khoản có sẵn|
|DB|55/55 hash bảng trước/sau giữ nguyên, không tạo/nộp/chấm dữ liệu thử|
|Artifact|QA8081 có15 nhãn cá nhân của builder;8080 vẫn0 nhãn, chưa deploy bản bổ sung|

39 kiểm browser đạt. Trên dữ liệu thật builder có2 Chờ chấm,2 Cần bổ sung,
3 Chưa nộp,1 Chưa nộp quá hạn,2 Chưa đạt và5 Đạt; practice có4 Chưa nộp.
Đây là nhãn từ dữ liệu/luật cũ, không tính lại điểm. Không chạy POST nộp/chấm/
nộp lại; các ca ghi giữ kiểm thử hiện có. Giới hạn Firefox/Safari, cảm ứng thật,
screen reader và quyết định xử lý dữ liệu demo trước đó giữ nguyên.

Bằng chứng ngoài Git ở `D:/Web-Pro/qa-evidence/phase11c/task-status-followup/`:
backup/diff WIP, `get-only.patch`, `staging-proof.json`, `final-proof.json`,
log Node/Maven/Surefire, `browser-results.json`, `owners.json`, ảnh hai USER
theo viewport và hash DB/file trước/sau. Đã dọn đúng QA8081;8080 PID30024 giữ
nguyên. Chưa push. M5 danh sách đã được triển khai và kiểm; phần còn chờ là
quyết định dữ liệu demo/deploy8080, không còn chờ quyền sửa GET M5.

## Chốt artifact và chuẩn bị demo final — 16/10/2026

### Bốn bản cần phân biệt

Đầu lượt HEAD `2ff67f1cba597a674a9d3e2c405138ce9eed511d`, nhánh
`integration/fullstack-v2`, index rỗng. Không delta source kể từ review độc lập.
Không sửa code/M5/API/schema/quyền/luật trong lượt này. Commit mới chỉ tài liệu.

| Bản | Nguồn / kết quả / giới hạn |
| --- | --- |
| Commit sạch | Archive2ff67f1, gồm hunk GET đã duyệt trong2b4e5fd, không có WIP. Build/test mới bên dưới áp dụng cho bản này. |
| Working tree | Sáu tracked WIP và bốn untracked vẫn riêng; không được coi là phần bàn giao. Không clean package vào target gốc. |
| Artifact QA/bàn giao | WAR từ archive sạch, build JDK17 ngoài repo; chạy Tomcat riêng8081 và kiểm HTTP/browser. Không mang .env/cấu hình DB local/tài liệu cá nhân. Template JNDI DB_* của repo giữ nguyên, cấu hình thực nằm ngoài WAR. |
| IntelliJ/Tomcat8080 | PID30024, Java17; ROOT.xml trỏ `D:/Web-Pro/robot-engine-website/tomcat-app/target/robot-assembly-lab`. Không tự dừng/sửa/redeploy. Bản HTTP còn cũ, chưa nghiệm thu làm bản final. |

Bằng chứng mới: `D:/Web-Pro/qa-evidence/final-demo-11c-20261016/`.
Artifact: `clean-source/tomcat-app/target/robot-assembly-lab.war`.
SHA-256 WAR: `9281fdb1eec4cc12ec5e61472c04d7a7c2512704047866e3708aa37729420024`.
Manifest chứa hash archive, class major, số test và đối chiếu HTTP; không chứa bí mật.
Tài liệu commit sau lượt này không thay đổi byte ứng dụng của artifact2ff67f1.

### Kiểm mới trên bản bàn giao

| Kiểm / nguồn | Kết quả thực hiện lần chốt |
| --- | --- |
| Maven clean package JDK17 / archive2ff67f1 | BUILD SUCCESS;149 JUnit,0fail/error/skip; không ghi target repo gốc. |
| npm test / cùng archive sau build |231 đạt,0fail/skip. So với mốc review231/149: số lượng không đổi. |
| Bytecode/tài nguyên |179 application class đều major61;166 public/webapp resources khớp byte archive, gồm JSP; không kiểm dependency class như application class. |
| Cache-busting | site-nav20261015.5 ở HTML/JSP; quiz/tra cứu/dashboard/practice-index20261016.1 tại nơi nạp tương ứng. Không đổi JS/CSS trong lượt này. |
| Chrome thật, profile QA riêng |31 kiểm, trong đó15 ô390/768/1440 có keyboard menu/focus, dashboard3/2/1;0pageerror,0asset lỗi,0request ghi từ trang. |
| Menu → quiz | Bấm menu thật → chọn Robot tránh vật cản → đúng model/câu hỏi; không tự nộp/tạo lượt. |
| Dashboard/tổng kết | Dashboard mở đúng review30 của builder; tổng kết mở review/quiz đúng ngữ cảnh và hồ sơ in. |
| Nhiệm vụ | Hai USER có15/4 thẻ; mỗi user mở một nhãn đối chiếu chi tiết bằng Enter; URL userId không đổi danh sách. Không lặp lại19 so khớp độc lập đã đạt khi source không đổi. |
| Nối dây/tra cứu | Neo lịch sử → lượt của chủ; tra cứu theo robot, Space mở chi tiết → chẩn đoán giữ model. Không bắt đầu bài luyện. |
| Quyền | Khách vào admin302; hai USER403; ADMIN200. ADMIN vào tasks403, review khác chủ404. Không mở rộng backend. |
| Smoke GET |27 kiểm đạt: các module cũ,6 byte tài nguyên, replay37 của student và Xưởng thiết kế10 của practice chỉ đọc;0request ghi/JS exception. Có1 DOM fixture dashboard rỗng, không coi là user thật rỗng. |
| Fixture quiz |10 ca rỗng/lỗi catalog/auth/câu hỏi, tải chậm, model không hợp lệ, truy cập thẳng; API chặn/giả lập trong browser, không dữ liệu thật. |
|8080 so archive | quiz.js, troubleshooting.js, site-nav.js và trang quiz khác byte; dashboard.css/practice-index.css404. Chưa cập nhật8080, không suy từ200 thành đúng phiên bản. |

Log mới: `maven-clean-package.log`, `node-test.log`, `artifact-manifest.json`,
`browser-results.json`, `smoke-results.json`, `fixture-results.json` và ảnh PNG.
Assertion smoke đầu tiên so nhầm CSS CRLF của working tree với LF của archive;
đối chiếu chuẩn hóa chứng minh cùng nội dung. Runner ngoài repo đã chuyển so
byte với archive sạch, chạy lại đạt27; không sửa source hoặc nới test dự án.

Review độc lập được dùng làm bằng chứng lịch sử riêng:
`D:/Web-Pro/qa-evidence/review-m5-20261016/REVIEW.md`, `results.json`.
44 kiểm/19 nhãn khớp từ artifact sạch2ff67f1 là kết quả reviewer, không phải số
kiểm mới của lượt này. Không tìm thấy lỗi chặn mới trong phạm vi vừa kiểm.

### Dữ liệu demo và phần còn chờ

GET/browser và SELECT/HEX chỉ đọc xác nhận wiring exercise7 vẫn PUBLISHED,
tên `[UI-QA]`, scope có dấu `?` ngay trong DB. HTML dạng chữ được escape đúng.
Đọc danh sách thấy21 nhiệm vụ có QA trong tên (ID1–6,9–23); quyền xem/nội dung
trạng thái cá nhân theo người được giao. Đây là inventory để quyết định demo,
không phải regex ẩn bài. Bài nối dây công bố dùng chung không biến mất khi đổi user.
Kết quả truy vấn mới ở `data-readonly.json`, không sửa dữ liệu/snapshot.

Hai lựa chọn đã gửi người dùng, hiện chờ phản hồi:

1. Tự deploy WAR sạch trong IntelliJ context `/` rồi báo để kiểm8080; hoặc giữ
   nguyên8080 và nhận artifact trước. Hướng dẫn cụ thể trong TEAM_FLOW_DEMO_GUIDE.
   Chưa có quyền tự dừng/redeploy/sửa IDE hoặc ghi đè docBase.
2. Giữ DB hiện tại và chọn luồng demo có kiểm soát; hoặc chuẩn bị phương án
   DB demo riêng để duyệt tiếp. Chưa tạo/copy DB, không sửa/xóa/ẩn QA.

Lượt này chỉ đăng nhập tài khoản có sẵn và đọc GET/SELECT, không tạo phiên,
thiết kế, bài nộp, chấm, đơn hàng hay thay giỏ/tồn kho. Không tuyên bố kiểm lại
mọi POST bằng dữ liệu thật. Script thao tác ghi là hướng dẫn cho người demo,
không phải agent đã tự thực hiện. Firefox/Safari, cảm ứng thật, screen reader
và xuất PDF thủ công không kiểm mới; kiểm load/error bằng fixture đã ghi rõ.
Legacy syntax ngoài11c vẫn theo danh sách cũ, không kết luận toàn repo tuân thủ100%.

Hash đầu/cuối của10 WIP/untracked và POM/settings (12/12) giữ nguyên; bằng
chứng `protected-before.json`, `protected-after.json`, `release-final-proof.json`.
24 link file:dòng mới trong kịch bản đều có file/dòng tồn tại. `git diff --check`
phần tài liệu sửa không có lỗi. Đã dừng đúng QA runtime tự tạo và dọn config,
log/work/temp; không có lỗi ứng dụng trong log.8081 không còn listener,8080
vẫn PID30024. Không build vào target gốc hay sửa cấu hình IDE.

Ảnh sau ở thư mục bằng chứng mới: `USER-_account-1440.png`,
`USER-_account-390.png`, `USER-_tasks-390.png`; ảnh trước11c ở
`D:/Web-Pro/qa-evidence/phase11c/USER-_account-1440.png`.
Đây là ảnh khác thời điểm/bản phục vụ, không dùng để kết luận8080 đã cập nhật.

Kịch bản final3–5 phút và7 câu vấn đáp/file:dòng nằm trong TEAM_FLOW_DEMO_GUIDE,
không tạo bộ tài liệu học trùng. Chưa push; ahead28 đầu lượt chỉ so với ref
origin cục bộ, chưa fetch nên không xác nhận trạng thái remote mới nhất.
