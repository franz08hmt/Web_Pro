# Luồng demo và câu hỏi giảng viên

## Chẩn đoán và Mẫu B bám slide nào? (Đợt 6, chặng 2)

Node/JUnit là công cụ kiểm chứng lúc phát triển, không thay kiến trúc môn học.
Ứng dụng mới chạy bằng Servlet/JSP/JDBC trên Tomcat, dùng form khi tắt JavaScript.

| Phần triển khai | Chapter / slide | File |
| --- | --- | --- |
| Model 2, Servlet → bean → XxxDB → JSP | Ch2 slide 4–5, 20 | `DiagnosisServlet`, `AdminDiagnosisServlet`, `DiagnosisDB`, các bean `Diagnosis*` |
| `@WebServlet` | Ch5 slide 10–11 | Hai Servlet chẩn đoán |
| GET/POST, getParameter, checkbox getParameterValues | Ch5 slide 12–17 | Hai Servlet, `DiagnosisFormUtil`, `TaskServlet` |
| Hidden action rẽ nhánh | Ch2 slide 9; Ch12 slide 40–44 | Form JSP và dispatcher Servlet |
| setAttribute, String url, forward | Ch5 slide 22–24; Ch12 slide 42–44 | Hai Servlet chẩn đoán |
| Redirect sau POST (PRG) | Ch5 slide 25–26 | start/check/conclude và quản lý tình huống |
| HttpSession lấy user và kiểm token form | Ch7 slide 10–12 | `SessionUtil`, các Servlet |
| JavaBean ctor rỗng, get/set, Serializable | Ch6 slide 4–6 | `DiagnosisScenario/Check/Option/Attempt`, các bean nhiệm vụ |
| Luật trong bean, vòng for/if như Cart | Ch9 slide 32–33 | `DiagnosisScenario`, `DiagnosisAttempt`, `TaskRubric`, `TaskRecipient` |
| Getter định dạng NumberFormat | Ch9 slide 28–31 | Điểm/display trong `DiagnosisAttempt`, `TaskSubmission` |
| Date, SimpleDateFormat/TimeZone | Ch10 slide 6 | `TaskRubric`, ngày lượt và quan sát |
| EL, empty/and/or/not, boolean getter | Ch6 slide 7–9; Ch8 slide 5–11, 32–35 | Các JSP chẩn đoán và nhiệm vụ |
| c:out chống XSS | Ch9 slide 7–8 | Mọi chữ DB và người dùng trong JSP |
| c:forEach, c:if, c:choose/when/otherwise | Ch9 slide 11, 17–19 | Các JSP mới/sửa |
| Hidden/radio/checkbox, textarea | Ch7 slide 35–36; Ch12 slide 24 | Form biên soạn cố định bốn ô, chọn nguyên nhân/biện pháp, chấm |
| PreparedStatement, XxxDB static, pool, DBUtil/finally | Ch12 slide 18–20, 32–38, 45–53 | `DiagnosisDB`, `PracticeTaskDB`, `TaskSubmissionDB` |
| Transaction commit/rollback, khóa dòng | Ch13 slide 29–34 (khái niệm); mẫu `CartDB`/`AssemblySessionDB`/`TaskSubmissionDB` | Bắt đầu TASK, kết luận, confirm bài |

BigDecimal/HALF_UP được prompt cho phép để làm tròn chính xác. FOR UPDATE là
mẫu triển khai đã có trong repo, không khẳng định slide chứa đúng câu SQL này.
Không dùng stream/lambda/switch/java.time trong code ứng dụng mới/sửa.
Guard kiểm cả cú pháp và giới hạn Java 120/JSP 140 ký tự.

### Kịch bản demo chẩn đoán và nhiệm vụ B

1. ADMIN → **Quản lý tình huống chẩn đoán**: mở seed hoặc tạo nháp (bốn ô mỗi
   nhóm, bỏ ô trống), xem trước không tạo lượt, công bố. Muốn sửa bản công bố
   phải nhân bản; bản mới chỉ công bố khi đủ điều kiện.
2. USER → **Luyện chẩn đoán**: chọn tình huống, chọn một phép kiểm tra để thấy
   quan sát vừa được server ghi nhận. Bấm lại không cộng điểm. Chọn nguyên nhân
   và xử lý, kết luận; khi đó mới thấy đúng/sai, giải thích và phép còn thiếu.
3. ADMIN tạo nhiệm vụ B, robot và tình huống PUBLISHED cùng robot, giao USER,
   hạn tương lai. Nếu demo bằng phiên hoàn thành cũ, bật cho phép bằng chứng cũ
   trong nháp. Xem trước rồi công bố.
4. USER làm quiz nhiệm vụ; bấm **Bắt đầu chẩn đoán cho nhiệm vụ này**, kết luận,
   chọn phiên COMPLETED của mình, viết ba đoạn giải thích → xem trước → nộp.
   Điểm tự động hiển thị /80, chưa chấm không hiển thị /100.
5. ADMIN mở bài: xem snapshot và quá trình chẩn đoán, chọn **Cần bổ sung**,
   ghi phản hồi. USER vòng 2 có thể dùng lại lượt cũ nếu chưa bắt đầu lượt mới,
   hoặc bắt đầu và phải kết luận xong lượt mới. Nộp lại → ADMIN chấm đạt nếu đủ
   ngưỡng. Lịch sử giữ cả hai vòng và nguồn dùng lại.
6. Minh họa quyền trên màn hình: USER chỉ có link luyện/nhiệm vụ của tôi; ADMIN
   có link quản lý. USER mở URL quản lý nhận 403; mở lượt người khác nhận 404.
   ADMIN chỉ xem quá trình chẩn đoán của bài nộp, không làm lượt hay nộp bài.

### Năm tình huống chính thức

| ID / Robot | Phép cần thiết | Nguyên nhân đúng | Biện pháp đúng |
| --- | --- | --- | --- |
| 6101 / obstacle-avoider | Đầu nối động cơ trái; tín hiệu/nguồn L298N | Đầu nối động cơ trái lỏng | Ngắt nguồn, siết lại đầu nối và thử hai bánh |
| 6102 / obstacle-avoider | Số đo/chân HC-SR04; điều kiện ngưỡng trong chương trình | Ngưỡng 100 cm thay vì yêu cầu 20 cm | Sửa ngưỡng, thử vật gần và xa hơn 20 cm |
| 6103 / line-follower | Vị trí cảm biến; dây OUT/nguồn | Cảm biến lắp ngoài vùng đọc | Chỉnh độ cao, thử cả vạch và nền |
| 6104 / mini-arm | Nguồn servo; tín hiệu/GND | Đầu nối VCC servo bị tuột | Ngắt nguồn, nối lại nguồn riêng và thử góc nhỏ |
| 6105 / line-obstacle | Hai tín hiệu cảm biến; thứ tự nhánh điều khiển | Dò line được ưu tiên trước tránh vật cản | Ưu tiên xử lý vật cản, kiểm lại các tình huống phối hợp |

Mỗi seed có ba phép (hai cần thiết), ba nguyên nhân, ba biện pháp và đúng một
đáp án mỗi nhóm. `DiagnosisTest` đọc chính file seed rồi kiểm luật công bố.
guide_id chỉ gắn nếu guide tương ứng tồn tại và cùng robot hoặc chung; chạy lại
INSERT IGNORE không sửa guide hay nội dung bản đã có.

### Vì sao chỉ đo “Thu thập đủ quan sát cần thiết”?

Điểm quá trình = 4 × số phép cần thiết đã chọn / tổng phép cần thiết. Phép không
liên quan và chọn lặp không cộng. Chỉ số này không đánh giá số thao tác ít nhất
hay thứ tự tối ưu, nên không gọi là “chọn phép kiểm tra tối ưu”.

### Vì sao lượt chẩn đoán gắn ngay khi bắt đầu, còn quiz khi nộp?

Chọn phép đã tạo hoạt động và mở dữ kiện. Gắn ngay giữ một quá trình cố định
của vòng, không cho bỏ lượt để đổi lượt khác. Quiz chỉ lưu khi đủ câu trả lời
hợp lệ; mở rồi bỏ dở không tạo lượt. Vòng sau đã bắt đầu chẩn đoán thì phải kết
luận lượt đó, không được quay lại dùng bài trước để bỏ qua quá trình đang dở.

### Quan sát có phải phép đo robot thật? Đây có phải thi kín?

Quan sát là dữ liệu mô phỏng của tình huống, không lấy telemetry robot. Hướng
dẫn công khai và lượt luyện cho thấy đáp án sau kết luận; đây là đánh giá quá
trình có tài liệu tham khảo, không phải thi kín. Lượt PRACTICE không dùng để
nộp bài; lượt TASK và snapshot xác định quá trình nào đã được đánh giá.
Đề quiz có thể thay đổi giữa đợt như chặng 1; lượt và snapshot cũ giữ nguyên.

### Vì sao dùng radio thay select?

Bảng kỹ thuật chặng 2 cho phép radio/checkbox, không có select. Các form mới và
form nhiệm vụ được sửa dùng radio, chạy không cần JS. Đây là ngoại lệ HTML mẫu
A ở form ADMIN: nội dung, giá trị field, điểm và luật vẫn giữ nguyên; xem bảng
diff cụ thể trong [báo cáo QA](PHASE6C_QA_REPORT.md).

### Transaction và khóa dòng tránh tranh chấp thế nào?

Bắt đầu TASK khóa task → recipient → round; nếu đã có ID thì trả đúng lượt đó.
Conclude chỉ khóa lượt của mình. Confirm theo thứ tự nhiệm vụ cũ và đọc lượt
chẩn đoán thường, không khóa ngược. UNIQUE slot/log/submission bổ sung bảo vệ.
Hai request thật đã kiểm cho ba thao tác: chỉ một lượt/một kết luận/một bài.

### Vì sao phải dùng điểm chẩn đoán chưa làm tròn để tính B?

Ví dụ quiz 6/7 và chẩn đoán 8/10: 30 + 6/7 × 25 + 8/10 × 25 = 71,4 sau một
lần HALF_UP. Điểm /10 trên màn hình chỉ định dạng; nếu dùng số đã làm tròn
này để nhân trọng số, tổng có thể lệch 0,1. Điểm tự động đã lưu dùng cho chấm
và so ngưỡng; điểm thành phần có getter riêng, JSP không tự tính.

## Nhiệm vụ thực hành bám slide nào? (Đợt 6, chặng 1)

| Phần triển khai | Chapter / slide | File |
| --- | --- | --- |
| Model 2: Servlet → JavaBean → XxxDB → JSP | Ch2 slide 4–5, 20 | `TaskServlet`, `AdminTaskServlet`, `AdminTaskReviewServlet`, `PracticeTaskDB`, `TaskSubmissionDB` |
| Mapping `@WebServlet` | Ch5 slide 10–11 | Ba Servlet nhiệm vụ |
| Form GET/POST, `getParameter`, checkbox `getParameterValues` | Ch5 slide 12–17 | `AdminTaskServlet`, `TaskServlet`, `TaskFormUtil` |
| Hidden `action` để rẽ nhánh | Ch2 slide 9; Ch12 slide 40–44 | Ba Servlet và form JSP |
| `setAttribute`, `String url`, forward | Ch5 slide 22–24; Ch12 slide 42–44 | Ba Servlet |
| Redirect sau POST (PRG) | Ch5 slide 25–26 | Ba Servlet |
| Danh tính/CSRF từ `HttpSession` | Ch7 slide 10–12 | `SessionUtil` |
| JavaBean có ctor rỗng, getter/setter, Serializable | Ch6 slide 4–6 | `PracticeTask`, `TaskRecipient`, `TaskRound`, `TaskSubmission`, `TaskReview`, `TaskRubric` |
| Luật nghiệp vụ trong bean, vòng for/if như Cart | Ch9 slide 32–33 | `PracticeTask`, `TaskRecipient`, `TaskReview`, `TaskRubric` |
| Getter định dạng điểm bằng NumberFormat | Ch9 slide 28–31 | Getter display trong `TaskSubmission`, `TaskReview`; formatter `TaskRubric` |
| Date + SimpleDateFormat thuộc họ DateFormat | Ch10 slide 6 | `TaskRubric`, `TaskFormUtil` |
| EL, empty/and/or/not, getter boolean | Ch6 slide 7–9; Ch8 slide 5–11, 32–35 | `task-*.jsp`, `admin-task-*.jsp` |
| c:out, c:forEach, c:if, c:choose | Ch9 slide 7–8, 11, 17–19 | Các JSP nhiệm vụ |
| Hidden field, textarea | Ch7 slide 35–36; Ch12 slide 24 | Form quiz/nộp/xem trước/chấm |
| PreparedStatement, ConnectionPool, DBUtil, XxxDB static/finally | Ch12 slide 18–20, 32–38, 45–53 | `PracticeTaskDB`, `TaskSubmissionDB`, phần mở rộng `QuizAttemptDB`/`UserDB` |
| Transaction commit/rollback và khóa dòng | Ch13 slide 29–34 (khái niệm transaction), mẫu repo `CartDB`/`AssemblySessionDB` | Lưu quiz của vòng, xác nhận nộp, lưu chấm |

Số slide lấy từ bảng đối chiếu trong prompt Đợt 6. `SELECT ... FOR UPDATE` là
cách triển khai khóa dòng theo mẫu đã có trong repo, không khẳng định slide có
đúng câu SQL này. BigDecimal/HALF_UP được prompt cho phép để lưu và so ngưỡng
chính xác. Node/JUnit chỉ kiểm tra trong lúc phát triển; ứng dụng chạy trên Tomcat.

### Kịch bản demo nhiệm vụ

1. Đăng nhập ADMIN → Tài khoản → **Quản lý nhiệm vụ** → tạo nháp mẫu A, chọn
   robot có quiz, hạn tương lai, chọn hai USER bằng checkbox; xem trước rồi công bố.
2. USER → Tài khoản → **Nhiệm vụ của tôi** → mở nhiệm vụ; làm quiz nhiệm vụ.
   Lượt hợp lệ đầu tiên của vòng được chốt. Quiz luyện tập cũ không thay lượt này.
3. Hoàn tất robot qua luồng lắp ráp hiện có; chọn phiên của mình, viết ba đoạn
   giải thích → xem trước (điểm tự động /80) → xác nhận.
4. ADMIN mở bài nộp, chọn mức giải thích và **Cần bổ sung**, ghi cần cải thiện /
   hướng làm lại. USER thấy vòng 2; dùng lại phiên hợp lệ, dùng quiz vòng trước
   hoặc làm một lượt cho vòng mới, xem trước rồi nộp lại.
5. ADMIN chấm **Đạt yêu cầu** khi tổng đủ ngưỡng; xem điểm từng vòng và lịch sử.
   Thử sửa một lần chấm: phải có lý do; vòng kế tiếp đã hoạt động thì bị chặn.
6. Minh họa quyền: USER không có link quản lý; mở `/admin-tasks` nhận 403.
   USER không được giao nhiệm vụ mở id đó nhận 404. ADMIN không có form nộp.

Mặc định không dùng phiên trước mốc vòng 1. Nếu dùng dữ liệu demo cũ để bảo vệ,
ADMIN cần bật **Cho phép phiên lắp ráp hoàn thành trước vòng 1** ngay khi tạo nháp.
Mỗi nhiệm vụ có chính sách muộn riêng; không tự thay đổi khi đã công bố.

### Vì sao chỉ tính lượt quiz đầu tiên của vòng?

Để bài nộp gắn với một lần kiểm tra xác định. `task_rounds.quiz_attempt_id` được
chốt trong transaction, chỉ khi toàn bộ câu trả lời hợp lệ. Mở rồi bỏ dở hoặc
gửi thiếu câu không tạo lượt. Vòng sau có thể làm lượt mới hoặc dùng lại lượt
của bài nộp trước; trang xem trước ghi nguồn rõ ràng.

### Transaction và FOR UPDATE dựa vào đâu?

Ch13 slide 29–34 giải thích transaction; repo đã có mẫu commit/rollback trong
CartDB và AssemblySessionDB. Ở đây khóa nhiệm vụ, người được giao và vòng giúp
kiểm điều kiện và ghi kết quả cùng một transaction. UNIQUE(round_id) là lớp
bảo vệ bổ sung: hai request xác nhận chỉ tạo một bài, request còn lại có thông báo.

### Vì sao dùng form và hidden token thay API JSON?

Form GET/POST, hidden action và forward/redirect là luồng cô dạy. Hidden
csrfToken được so với token trong HttpSession; thiếu/sai trả 403. Token API qua
header X-CSRF-Token vẫn giữ nguyên. Hidden field không đáng tin: server chỉ
nhận ID nguồn và nội dung giải thích, tự đọc lại bằng chứng và tính điểm.

### Đây có phải thi kín không? Nếu đổi đề giữa đợt thì sao?

Đây là đánh giá quá trình có tài liệu tham khảo, không phải thi kín. Người học
vẫn có thể xem quiz luyện tập. Nếu ADMIN đổi câu hỏi giữa đợt thì vòng mới đọc đề
hiện hành; lượt đã chốt và bài đã nộp giữ snapshot cũ. Chặng 1 chưa đóng băng đề
cho toàn bộ nhiệm vụ.

### Mẫu B và chẩn đoán lỗi ở đâu?

Chặng 2 đã triển khai ở phần đầu tài liệu. Chặng 1 giữ rubric A cố định
40/40/20; B dùng 30/25/25/20. Hai mẫu có cùng năm mức giải thích 0/5/10/15/20
và không có màn hình sửa trọng số.

Bằng chứng và giới hạn QA: [PHASE6_QA_REPORT.md](PHASE6_QA_REPORT.md).


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
| Hồ sơ học tập cá nhân có thể in | Model 2 (Ch2/4–5, 20); Servlet forward (Ch5/22–24); `HttpSession` (Ch7/10–12); JavaBean và vòng for (Ch6/4–6, Ch9/28–33); Date/DateFormat (Ch10/6); EL/JSTL chống XSS (Ch8/5–8, 32–35; Ch9/7–8, 11, 17–19); JDBC (Ch12/18–20, 32–38, 45–53); trang chỉ đọc/no-store, trình duyệt in A4/PDF | `LearningProfileServlet.java` | `LearningProfile`; `RobotDB`; `StatsDB` | `learning-profile.jsp` | Đọc `robots`, `assembly_sessions`, `quiz_attempts`, `robot_components`, `components`; không thêm bảng |
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

Các tài khoản dưới đây là dữ liệu demo đã tạo trên một DB cục bộ, không có seed
khôi phục tài khoản trong repo và không bảo đảm đăng nhập được trên DB khác.
Lần QA Đợt 5 trước ghi nhận API từ chối mật khẩu được ghi ở đây; lần kiểm chứng
ngày 04/10/2026 đã đăng nhập thành công cả bốn trên Tomcat QA/MySQL hiện tại.
Trước buổi bảo vệ vẫn phải kiểm tra trên đúng instance đang dùng. Cả bốn được tạo
qua `POST /api/auth/register`; role ADMIN được cấp sau đó bằng một câu `UPDATE`
có điều kiện và tăng `session_version` để vô hiệu hóa phiên cũ. Đăng ký công khai
luôn tạo role USER, không gửi role từ trình duyệt.

| Email | Quyền | Dữ liệu lịch sử để trình bày |
| --- | --- | --- |
| `ral-demo-admin@robotlab.test` | ADMIN | Dự kiến xem `/admin-stats`; không có dữ liệu thực hành riêng |
| `ral-demo-builder@robotlab.test` | USER | Đã hoàn tất Robot dò đường; có 3 lượt kiểm tra, mỗi mẫu robot một lượt (7/7) |
| `ral-demo-practice@robotlab.test` | USER | Phiên Robot tránh vật cản ở `PREPARING`, đã chuẩn bị 4/8 nhóm linh kiện |
| `ral-demo-student@robotlab.test` | USER | Có một lượt kiểm tra Cánh tay robot mini (7/7) |

Mật khẩu demo chung: `RobotLabDemo#2026` (chỉ áp dụng ba USER; ADMIN đã được chủ
tài khoản đổi mật khẩu và được kiểm chứng riêng ngày 05/10/2026). Đây là thông tin đăng nhập thử nghiệm,
không dùng ngoài demo/không dùng cho người thật; đổi hoặc xóa các tài khoản này
trước khi đưa một bản sao database lên môi trường công khai. Mật khẩu DB thật
không nằm trong tài liệu này hay trong Git.

## Bảng đối chiếu: chức năng → kiến thức môn học → code

Trả lời trực tiếp câu "chức năng này xây từ đâu trong code?" cho năm chức năng
mới thêm vào đồ án.

| Chức năng | Kiến thức môn học minh họa | Servlet | JavaBean / XxxDB | JSP | Bảng database |
| --- | --- | --- | --- | --- | --- |
| Nhiệm vụ thực hành và đánh giá (Đợt 6 chặng 1) | Form/action/PRG Ch5/12–26; session Ch7/10–12; bean Ch6/4–6, Ch9/28–33; EL/JSTL Ch8/5–11, 32–35, Ch9/7–8, 11, 17–19; JDBC Ch12/18–20, 32–38, 45–53; transaction Ch13/29–34 | `TaskServlet`, `AdminTaskServlet`, `AdminTaskReviewServlet` | Sáu bean nhiệm vụ; `PracticeTaskDB`, `TaskSubmissionDB`, overload `QuizAttemptDB` | `task-*.jsp`, `admin-task-*.jsp` | Năm bảng migration 009, nguồn `assembly_sessions`/`quiz_attempts` |
| Chẩn đoán tương tác (Đợt 6 chặng 2) | Model 2 Ch2/4–5,20; form/action/session Ch5/10–26, Ch7/10–12; bean/Cart Ch6/4–6, Ch9/32–33; EL/JSTL Ch8/32–35, Ch9/7–8,11,17–19; JDBC Ch12/18–20,32–38,45–53 | `DiagnosisServlet`, `AdminDiagnosisServlet` | Bốn bean `Diagnosis*`, `DiagnosisDB` | `diagnosis-*.jsp`, `admin-diagnosis-*.jsp` | Năm bảng diagnosis của migration 010 |
| Nhiệm vụ mẫu B và bằng chứng chẩn đoán | Getter định dạng Ch9/28–31; form/radio Ch5/12–17, Ch7/35–36; transaction Ch13/29–34 + mẫu repo | Các Servlet nhiệm vụ | `TaskRubric`, `TaskSubmission`, `TaskRound`, `TaskRecipient`; `PracticeTaskDB`, `TaskSubmissionDB` | Các JSP nhiệm vụ | Liên kết/snapshot nullable trên ba bảng 009; nguồn diagnosis_attempts |
| 1. Hoàn tất thực hành + phiếu kết quả | Server-side validation (không tin client); atomic UPDATE tránh race condition; Servlet→JSP forward | `AssemblySessionServlet` (đổi trạng thái), `AssemblyReceiptPageServlet` (phiếu kết quả) | `AssemblySession.canCompleteAssembly()`; `AssemblySessionDB.completeSession()` | `assembly-receipt.jsp` | `assembly_sessions` (cột `completed_at`), `session_visual_parts`, `robot_components` |
| 2. Bài kiểm tra kiến thức | Chấm điểm ở server, không tin client; snapshot dữ liệu lịch sử; JDBC transaction nhiều bảng | `QuizServlet` (làm bài), `AdminQuizServlet` (quản trị) | `QuizQuestion.isCorrectOption()`; `QuizAttemptDB.submitAttempt()` | *(API JSON, JS dựng giao diện — không dùng JSP)* | `quiz_questions`, `quiz_options`, `quiz_attempts`, `quiz_attempt_answers` |
| 3. Tra cứu lỗi lắp ráp | Nội dung công khai đọc từ MySQL; lọc/tìm bằng `PreparedStatement` tham số hóa | `TroubleshootingServlet` (công khai), `AdminTroubleshootingServlet` (quản trị) | `TroubleshootingGuide`; `TroubleshootingGuideDB` | *(API JSON)* | `troubleshooting_guides` |
| 4. Hiệu ứng ăn mừng | Sự kiện phía client gắn với xác nhận server, không phải hiệu ứng trang trí độc lập | *(không có endpoint riêng — dùng lại `PATCH /api/assembly-sessions/{id}`)* | — | — | — |
| 5. Tổng kết và thống kê | `GROUP BY`/`COUNT DISTINCT` để tránh đếm trùng; phân quyền xem dữ liệu tổng hợp | `LearningSummaryServlet` (cá nhân), `AdminStatsServlet` (quản trị, chỉ ADMIN) | `QuizRobotScore`, `RobotPopularity`, `QuizRobotAggregate`, `QuestionMissRate`; `StatsDB` | `learning-summary.jsp`, `admin-stats.jsp` | Đọc tổng hợp từ `assembly_sessions`, `quiz_attempts`, `quiz_attempt_answers`, `users` |
| 6. Hồ sơ học tập cá nhân có thể in | Model 2 (Ch2/4–5, 20); request attribute/forward (Ch5/22–24, Ch12/42–44); session (Ch7/10–12); bean có luật nghiệp vụ và getter định dạng (Ch6/4–6, Ch9/28–33); ngày giờ (Ch10/6); EL/JSTL (Ch8/5–8, 32–35; Ch9/7–8, 11, 17–19); PreparedStatement/pool/DBUtil (Ch12/18–20, 32–38, 45–53) | `LearningProfileServlet` | `LearningProfile`, `ProfileRobotEntry`, `ProfileSkill`; `RobotDB`, `StatsDB` | `learning-profile.jsp` | `robots`, `assembly_sessions`, `quiz_attempts`, `robot_components`, `components` (chỉ đọc) |

## Hồ sơ học tập bám slide nào?

Bảng dưới đã đối chiếu với nội dung slide Chapter 2, 5, 6, 7, 8, 9, 10, 12.
Mở code theo cột cuối để giải thích mẫu đã học.
Thứ tự thực thi cụ thể: Servlet lấy user từ session, đọc dữ kiện qua XxxDB,
đặt dữ kiện vào JavaBean, gọi `buildProfile()`, setAttribute và forward tới JSP.

| Phần trong Đợt 5 | Chapter / slide | File |
| --- | --- | --- |
| Model 2: servlet → bean → XxxDB → JSP | Ch2 slide 4–5, 20 | LearningProfileServlet, LearningProfile, StatsDB/RobotDB, learning-profile.jsp |
| `@WebServlet("/learning-profile")` | Ch5 slide 10–11 | LearningProfileServlet |
| `setAttribute` + `String url` + `getRequestDispatcher(url).forward` | Ch5 slide 22–24; Ch12 slide 42–44 | LearningProfileServlet |
| Khách chưa đăng nhập → `sendRedirect` | Ch5 slide 25–26 | LearningProfileServlet |
| Lấy user từ `HttpSession` | Ch7 slide 10–12 | SessionUtil |
| JavaBean (ctor rỗng, get/set, Serializable) | Ch6 slide 4–6 | LearningProfile, ProfileRobotEntry, ProfileSessionStat, ProfileQuizAttempt, ProfileSkill (5 bean) |
| Getter định dạng sẵn trong bean (`getXxxDisplay`) | Ch9 slide 28–31 | LearningProfile, ProfileRobotEntry |
| Luật nghiệp vụ trong lớp business, duyệt bằng vòng `for` | Ch9 slide 32–33 (Cart) | LearningProfile |
| Ngày giờ: `Date` + `DateFormat`/`SimpleDateFormat` | Ch10 slide 6 | LearningProfile |
| `StringBuilder` | Ch12 slide 29 | LearningProfile |
| EL `${profile.fullName}`, thuộc tính bean | Ch6 slide 7–9; Ch8 slide 5–8 | learning-profile.jsp |
| EL `empty`, `or`, `==/eq` | Ch8 slide 32–35 | learning-profile.jsp |
| `c:out` chống XSS | Ch9 slide 7–8 | learning-profile.jsp |
| `c:forEach` / `c:if` / `c:choose` | Ch9 slide 11, 17–19 | learning-profile.jsp |
| `PreparedStatement`, ConnectionPool, XxxDB static, `DBUtil` | Ch12 slide 18–20, 32–38, 45–53 | StatsDB, RobotDB |

`ArrayList` theo Ch9 slide 32; `Map`/`HashMap` theo Ch8 slide 8 và Ch18 slide 21.
Không gọi collection factory hay xử lý collection bằng stream/lambda trong
code ứng dụng Đợt 5. SQL vẫn giữ nguyên; `getTimestamp()` chuyển sang `Date`
bằng `getTime()`, bean so sánh `Date` rồi định dạng theo `Asia/Ho_Chi_Minh`.
Giá trị Date thiếu là null, hiển thị "Chưa có dữ liệu" và xếp cũ nhất khi so
sánh; cùng ngày giờ vẫn dùng mã lượt/mã phiên để phân định như trước.

CSS A4 và `window.print()` giữ nguyên từ Đợt 5, là phần giao diện/in của trình
duyệt; không gán cho chúng một số slide Java không có trong bảng đối chiếu.
Node/JUnit là công cụ kiểm tra lúc phát triển, không phải runtime backend.
Các chỗ code cũ ngoài Đợt 5 còn dùng kỹ thuật chưa có trong bảng được liệt kê
ở [báo cáo quét Đợt 5b](PHASE5B_SLIDE_AUDIT.md); chưa refactor chúng.

## Phân công

Từ Đợt 1 đến Đợt 7 và phần giao diện, các chức năng thêm vào được chia lại theo vai trò
ban đầu để ba người có khối lượng và độ sâu tương đương (bảng dưới). Mỗi người sở hữu một
luồng đầy đủ từ trình duyệt đến bảng MySQL.

| Thành viên | Vai trò ban đầu | Mảng sở hữu hiện tại | File nên mở |
| --- | --- | --- | --- |
| Nhi | Database, dữ liệu robot/linh kiện/bước/thư viện, CRUD nội dung | **Dữ liệu và nội dung**: lớp dữ liệu, schema/migration/seed, catalog, quiz, tra cứu lỗi, tổng kết và thống kê | `database/schema.sql`, `database/seed.sql`, `ConnectionPool`, `RobotDB`, `ComponentDB`, `QuizAttemptDB`, `StatsDB`, `Admin*Servlet.java` |
| Tuấn Anh | Phòng 3D, dựng linh kiện, camera và thao tác lắp ráp | **Tương tác và thực hành**: phòng 3D, chẩn đoán lỗi, phòng nối dây và hỗ trợ, cửa hàng mô phỏng, giao diện đáp ứng | `assembly-3d*.js`, `lap-rap-3d.html`, `wiring.js`, `WiringServlet`, `DiagnosisServlet`, `CartDB`, `OrderDB` |
| Tài | Servlet/Tomcat, đăng nhập, session, phân quyền, phiên lắp ráp, tích hợp client-server, giao diện dùng chung | **Nền tảng và phiên học tập**: auth/CSRF/filter, phiên lắp ráp và phiếu kết quả, nhiệm vụ thực hành, hồ sơ học tập, khung giao diện JSP | `AuthServlet`, `SessionUtil`, `AssemblySessionServlet`, `TaskServlet`, `LearningProfileServlet`, `workspace-header.jspf` |

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
19. Chuẩn bị trước một tài khoản đăng nhập được trên DB hiện tại. Nếu tài khoản
    demo cũ bị từ chối, đăng ký tài khoản mới ở `/pages/tai-khoan.html` bằng mật
    khẩu tự chọn; hoàn tất ít nhất một mẫu qua luồng chuẩn bị → lắp ráp 3D →
    phiếu kết quả, rồi nộp một bài quiz. Không sửa dữ liệu hay mật khẩu tài khoản
    demo cũ chỉ để buổi trình bày chạy được. Đăng ký mới chỉ có quyền USER;
    phần demo ADMIN cần một tài khoản ADMIN đã được xác thực riêng.
    Mở `/learning-profile` bằng tài khoản đã chuẩn bị: chỉ ra danh mục đủ năm mẫu,
    số liệu của session hiện tại, trạng thái/quiz/kỹ năng có căn cứ; dùng In / Lưu
    PDF, chọn A4 và nên tắt **Đầu trang và chân trang**. Kiểm tra mục Kỹ năng
    giữ tiêu đề/caption cùng bảng; số trang CSS tùy hỗ trợ trình duyệt. Nếu cần
    bật lại đầu/chân trang để có số trang, chấp nhận ngày giờ/tiêu đề/URL do
    trình duyệt thêm vào. So sánh cùng tài khoản ở `/learning-summary`; nếu lịch sử
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
tổng hợp `MAX(score)` và `MAX(total_questions)` độc lập; dữ liệu lịch sử có mẫu
số thay đổi có thể làm hai cách hiển thị
khác nhau. Hồ sơ cũng là tài liệu in tĩnh, không hiện email hay giải thích kiến
trúc dài như trang tổng kết.

### "Tình trạng chung được xác định thế nào?"

Ưu tiên mẫu đã hoàn thành, rồi phiên đang thực hiện, rồi phiên đã dừng. Nếu chỉ
có lượt quiz thì ghi "Chưa hoàn thành mẫu nào"; chỉ ghi "Chưa có dữ liệu" khi
không có phiên hay lượt quiz. Điểm và căn cứ kỹ năng vẫn tuân theo dữ liệu thực
tế, không suy ra đã lắp ráp từ việc làm quiz.

### "Vì sao bản in giữ mã phiên?"

Mã phiên lắp ráp dùng để đối chiếu căn cứ hoàn thành với phiếu kết quả, đúng
đặc tả hồ sơ. Đây không phải ID người dùng; hồ sơ không in email, quyền hay
thông tin xác thực của tài khoản.

### "Vì sao dùng SimpleDateFormat mà không phải DateFormat.getDateInstance?"

`SimpleDateFormat` là lớp con của `DateFormat`, cùng họ định dạng ngày giờ ở
Ch10 slide 6. Hồ sơ cần đúng mẫu `dd/MM/yyyy` hoặc `dd/MM/yyyy HH:mm`, cùng múi
giờ Việt Nam; `getDateInstance()` dùng kiểu định dạng theo locale nên không
chốt được mẫu này. Bean tạo formatter mới mỗi lần gọi hàm định dạng, giống
cách tạo `NumberFormat` trong getter ở Ch9 slides 28–31, rồi đặt TimeZone
`Asia/Ho_Chi_Minh`. Không lưu formatter trong static field vì formatter này
không an toàn khi nhiều request dùng chung.

### "Vì sao có BigDecimal?"

`BigDecimal` là lớp Java cổ điển để giữ phép tính thập phân và làm tròn
`RoundingMode.HALF_UP` chính xác. Bean duyệt từng robot bằng vòng for, cộng
bằng `.add(...)`, chia số mẫu có quiz, không dùng reduce/stream. Test kiểm cả
trường hợp 12.5% phải thành 13%, trung bình nhiều mẫu và không có quiz. Đây là
lựa chọn độ chính xác được prompt Đợt 5b cho phép; không khẳng định slide có
ví dụ riêng về BigDecimal.

### "Vì sao JSP không tự so sánh trạng thái?"

Luật nằm trong business object theo tinh thần Cart/LineItem ở Ch9 slides
28–33: `ProfileRobotEntry.isCompleted()`, `isInProgress()`, `isStopped()` trả
kết quả boolean. JSP chỉ dùng `${profileRobot.completed}` hoặc `or` để chọn
khối trình bày bằng `c:choose`, không biết chuỗi trạng thái trong database.
`getStatusKey()` và `getStatusLabel()` vẫn được giữ cho code hiện có.

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

## Phòng nối dây bám slide nào? (Đợt 7, chặng 1)

| Phần | Chapter/slide | Code |
| --- | --- | --- |
| Model 2 request → Servlet → XxxDB → JavaBean → JSP | Ch2/4–5,20 | WiringServlet, AdminWiringServlet, WiringDB, Wiring* |
| JavaBean ctor rỗng/get/set/Serializable | Ch6/4–6 | Sáu bean Wiring* |
| Luật chuẩn hóa/chấm/công bố trong bean; vòng for/if | Ch9/32–33 Cart | WiringExercise, WiringGrade, WiringAttempt |
| @WebServlet/getParameter/setAttribute/String url/forward/PRG | Ch5/10–17,22–26; Ch12/42–44 | Hai Servlet nối dây |
| HttpSession và token form hidden | Ch7/10–12,35–36 | SessionUtil; wiring forms |
| EL/JSTL c:out, if, choose, forEach | Ch8/5–11,32–35; Ch9/7–8,11,17–19 | wiring-*.jsp/admin-wiring-*.jsp |
| JDBC PreparedStatement/pool/DBUtil/finally | Ch12/18–20,32–38,45–53 | WiringDB |
| Transaction và khóa dòng | Ch13/29–34 khái niệm; mẫu CartDB | Khóa bài → lượt, version, atomic submit |
| Getter NumberFormat/SimpleDateFormat riêng mỗi lần | Ch9/28–31; Ch10/6 DateFormat | WiringGrade/WiringAttempt |

BigDecimal/HALF_UP được yêu cầu để tránh sai số điểm; không tuyên bố slide
có ví dụ riêng về công thức nối dây. SVG và kéo thả chỉ là view phụ trợ;
giảng viên xem luồng form POST → Servlet → WiringDB → WiringExercise.grade
→ WiringGrade.calculate → lưu → JSP c:out. Tắt JS vẫn trình bày đủ luồng này.

| Chức năng | Kiến thức | Servlet | Bean/XxxDB | JSP | Bảng |
| --- | --- | --- | --- | --- | --- |
| Lưu/nộp/chấm nối dây | Model2/session/form/JDBC/transaction | WiringServlet | WiringAttempt/Exercise/Grade, WiringDB | wiring-play/result/list | wiring_attempts, wiring_attempt_connections và bài |
| Biên soạn/công bố/lưu trữ bài | Quyền ADMIN/form/JavaBean/JDBC | AdminWiringServlet | WiringExercise, WiringDB | admin-wiring-form/view/list | wiring_exercises, wiring_terminals, wiring_rules |

### Demo nối sai → giải thích → luyện lại

1. ADMIN xem bài mẫu, xem trước chỉ đọc; nhân bản để sửa, bản công bố bị khóa.
2. USER mở Thực hành nối dây, bắt đầu line-follower. Nối đúng D5–ENA và nối
   sai một cặp; lưu, tải lại để thấy dây đã lưu và phiên bản mới.
3. Nộp để chấm: chỉ server tính C/W/M/N và điểm, bảng chỉ rõ cặp sai/thiếu.
4. Luyện lại tạo nháp mới, dùng sơ đồ tham khảo bên ngoài, sửa/lưu/nộp.
5. Hai tab cùng nháp: lưu tab thứ nhất; tab cũ bị yêu cầu tải lại, không mất dây.

### Vì sao đây không phải mô phỏng điện hay thi kín?

Bài chỉ so cặp trực tiếp đã biên soạn; không tính dòng/điện áp/jumper/robot thật,
không chấm mọi sơ đồ điện tương đương. Tài liệu tham khảo vẫn công khai. Nguồn
GND dùng chung có nhiều dây và hai cảm biến cùng loại có ID riêng.
Hai mẫu không bổ sung Vs/đầu ra động cơ còn thiếu trong robots.wiring cũ;
phạm vi mô phỏng in rõ trên bài. Căn cứ chân tại WIRING_DESIGN.md.

### Vì sao không phòng nhóm hoặc đổi Mẫu A/B?

Lượt nối dây có một owner; hỗ trợ Admin–User là chặng riêng sau review, không
chia quyền sửa sơ đồ. Điểm nối dây độc lập với nhiệm vụ/rubric A/B để không
đổi hành vi đã nghiệm thu. Hỗ trợ không đồng nghĩa đạt yêu cầu nối dây.

## Hỗ trợ nối dây bám slide nào? (Đợt 7, chặng 2)

| Phần | Chapter/slide | Code |
| --- | --- | --- |
| Model 2 Servlet → XxxDB → JavaBean → JSP | Ch2/4–5,20 | WiringSupportServlet, AdminWiringSupportServlet, WiringSupportDB |
| JavaBean ctor rỗng, get/set, Serializable | Ch6/4–6 | WiringSupportRequest, WiringSupportMessage |
| Luật trạng thái/version trong bean, vòng for/if | Ch9/32–33 Cart | Request.requireCurrent/applyMessage/close; Message.capture |
| @WebServlet, form/action/getParameter/PRG/forward | Ch5/10–17,22–26; Ch12/42–44 | Hai Servlet hỗ trợ |
| HttpSession, hidden token | Ch7/10–12,35–36 | SessionUtil, wiring-support forms |
| EL/JSTL c:out/if/choose/forEach | Ch8/5–11,32–35; Ch9/7–8,11,17–19 | wiring-support-list/new/view.jsp |
| StringBuilder dựng bản chụp | Ch12/29 | WiringSupportMessage.capture |
| Date/SimpleDateFormat (cùng họ DateFormat) | Ch10/6 | WiringSupportMessage.getCreatedAtDisplay |
| PreparedStatement, pool, DBUtil/finally | Ch12/18–20,32–38,45–53 | WiringSupportDB; overload WiringDB.selectAttempt |
| Transaction/khoá dòng theo mẫu có sẵn | Ch13/29–34 khái niệm; CartDB | Khóa bài → lượt → yêu cầu; version + UNIQUE |

| Chức năng | Kiến thức | Servlet | Bean/XxxDB | JSP | Bảng |
| --- | --- | --- | --- | --- | --- |
| Hỏi/phản hồi/cập nhật/giải quyết hỗ trợ | Model2/session/form/bean/JDBC/transaction | WiringSupportServlet, AdminWiringSupportServlet | WiringSupportRequest/Message, WiringSupportDB | wiring-support-list/new/view | wiring_support_requests, wiring_support_messages |

### Demo User hỏi → Admin phản hồi → cập nhật → đóng

1. USER lưu nháp nối dây, mở “Gửi câu hỏi hỗ trợ”; bản xem trước là dữ liệu đã lưu.
2. Viết câu hỏi, chọn đầu nối liên quan nếu cần, gửi; server chụp version/nhãn/dây/trạng thái.
3. ADMIN mở Quản lý hỗ trợ nối dây, xem bản chụp và SVG, phản hồi; không có nút sửa dây/điểm.
4. USER sửa và lưu lượt gốc, gửi tiếp với checkbox bản chụp mới. Tin/bản chụp cũ vẫn hiện.
5. USER đánh dấu đã giải quyết hỗ trợ; không đổi kết quả chấm. Form cũ của Admin bị từ chối.
6. Có thể tắt JavaScript và thực hiện cùng các form trên, không cần chat realtime.

**Vì sao bản chụp không lấy sơ đồ client?** Sơ đồ đang kéo chưa lưu không phải dữ liệu
đã ghi nhận. Server khóa lượt, đọc trên cùng kết nối và INSERT bản chụp riêng cho từng tin.

**Vì sao dùng version và UNIQUE?** Version từ chối phản hồi/đóng khi đã có tin mới;
UNIQUE cột sinh cùng transaction ngăn hai yêu cầu hoạt động/lượt, kể cả bấm đồng thời.
Cột sinh là ràng buộc MySQL hỗ trợ tính toàn vẹn; không chứa luật chấm.

**Đã giải quyết hỗ trợ có đồng nghĩa đạt bài không?** Không. Hỗ trợ và chấm là hai trạng
thái riêng. ADMIN không sửa dây/điểm và không nộp thay. Không có phòng nhóm, không đổi Mẫu A/B.
