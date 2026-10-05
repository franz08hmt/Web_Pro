# Kiểm chứng Đợt 6 — Nhiệm vụ thực hành, chặng 1

Ngày 05/10/2026, trên nhánh `integration/fullstack-v2`. HEAD thực tế lúc nhận
việc là `f654840`, đã đồng bộ origin (khác HEAD c4ba31b trong prompt).
Ba file index.html/style.css/personalization.js sạch và không bị sửa trong đợt này.
AGENT_HANDOFF.md tiếp tục ngoài commit. Không push.

## File triển khai

- Business: PracticeTask, TaskRecipient, TaskRound, TaskSubmission, TaskReview,
  TaskRubric. Luật trạng thái/quyền nộp/rubric/điểm/đánh giá nằm trong bean.
- Controller: TaskServlet, AdminTaskServlet, AdminTaskReviewServlet.
- Data: PracticeTaskDB, TaskSubmissionDB; mở rộng QuizAttemptDB (dùng chung
  gradeAnswers/insertAttempt, giữ overload/API cũ), UserDB.selectLearners.
- Util: TaskFormUtil; SessionUtil.hasValidFormCsrfToken, giữ kiểm header API cũ.
- JSP: task-list, task-view, task-quiz, task-submit, task-preview, task-error;
  admin-task-list, admin-task-form, admin-task-review; CSS practice-tasks.css.
- Lối vào: pages/tai-khoan.html, hai câu lệnh ẩn/hiện link trong account.js;
  cache-busting account.js được tăng. Không sửa ba file trang chủ được bảo vệ.
- DB: migration 009 và schema.sql; chỉ bổ sung năm bảng/FK/UNIQUE/CHECK/index.
- Tests: TaskRubricTest, practice-tasks.test.cjs, BEANS map của JSP contract.
  Hai test source cũ được điều chỉnh để vẫn kiểm luật: phát hiện câu hỏi trùng
  bằng ArrayList.contains thay HashSet.add; hồ sơ không thêm migration nhưng
  cho phép migration 009 riêng của Đợt 6. Không xoá kịch bản test cũ.
- Tài liệu: README, API_CONVENTIONS, erd, TEAM_FLOW_DEMO_GUIDE, tasks/plan,
  tasks/todo; báo cáo này; handoff không commit.

## Test và build

| Bước | Kết quả thực chạy |
| --- | --- |
| Baseline npm test | 105/105, không fail |
| Baseline Maven test, JDK 17 | 37/37, không fail |
| Test-first | Node đỏ khi chưa có file mới; JUnit đỏ khi chưa có bean nhiệm vụ |
| Guard slide-style cuối | Xanh cho toàn bộ Java/JSP mới và phần mở rộng cũ; bản sao TaskRubric chèn stream bị guard bắt, file tạm được xoá |
| npm test cuối | 111/111, 0 fail, 0 skip |
| mvn -f tomcat-app/pom.xml clean package | 51/51 JUnit, 0 fail/skip; BUILD SUCCESS |
| Kiểm WAR | Toàn bộ class trong WEB-INF/classes có major version 61 (Java 17) |
| JavaBeanRules / JSP expression contract / course structure | Xanh trong các bộ test trên |

JUnit bao phủ điểm 7/7, 6/7, 0/7; biên 69,95 → 70,0 và 69,94 → 69,9 (cả
phép chia quiz thật và helper làm tròn); thang giải thích; điều kiện đạt/bổ sung/
sửa chấm; trạng thái; nguồn quiz; mốc phiên; giới hạn văn bản; hạn/trạng thái
nhiệm vụ; ngày Việt Nam ở biên nửa đêm.

IntelliJ từng ghi class Java 25 vào target trong lúc sửa; Maven clean package
với JAVA_HOME=JDK17 đã dựng lại, WAR được kiểm bytecode và dùng cho Tomcat QA.
Không sửa cấu hình IDE của người dùng.

## Tomcat / MySQL và ma trận QA

Tomcat 9.0.120, JDK17, QA 8081, CATALINA_BASE tạm; DB local trong .env.
Cả bốn demo đăng nhập được sau khi chủ tài khoản cung cấp mật khẩu ADMIN
hiện hành. Không ghi mật khẩu vào báo cáo/Git, không đổi mật khẩu/role/tài khoản.

Migration 009 đã chạy thành công trên robot_lab_content_test. Lần đầu MySQL
báo CHECK cấp cột tham chiếu cột khác; đã sửa thành CHECK cấp bảng và tiếp tục
CREATE IF NOT EXISTS cho các bảng còn thiếu. Không DROP bảng, không thay dữ
liệu cũ. schema_migrations có 009_practice_tasks và đủ năm bảng.

| Kiểm tra | Kết quả / bằng chứng |
| --- | --- |
| ADMIN tạo nháp, xem trước, chọn nhiều USER, công bố | Đạt; nhiệm vụ #1/#2 có builder và practice; vòng 1 được tạo khi công bố |
| Sửa nháp / thay người được giao / preview chỉ đọc | #10 sửa qua saveDraft thành công, gỡ builder và chọn practice; GET form xác nhận checkbox; preview ADMIN không có form nộp hay quản lý |
| USER chỉ thấy dữ liệu mình | Đạt; student chưa được giao #1 nhận 404; builder không được giao #9 nhận 404; query userId/email không đổi chủ; bài được truy vấn với user_id |
| Khách vào ba route | 302 về /pages/tai-khoan.html, Cache-Control: no-store |
| USER vào hai route ADMIN; ADMIN vào luồng nộp USER | 403 |
| CSRF thiếu/sai trên cả ba POST route | 403; không ghi dữ liệu |
| Thiếu câu / option không thuộc câu | 422; form quiz vẫn còn, không lưu lượt |
| Vòng đã chốt quiz | POST tiếp bị 422; nội dung điểm/lượt chốt không đổi |
| Mã vòng của nhiệm vụ khác cùng người học | POST quiz #1 mang roundId của #13 trả404; #13 vẫn còn form làm bài, không tạo lượt |
| Đáp án trước khi chốt vòng mới | GET quiz vòng2 #13 có radio nhưng không có phần kết quả/đáp án của lượt cũ; vòng đã có lượt riêng vẫn hiện kết quả |
| Hai POST quiz đồng thời | Chrome request context: một 302, một 422; đúng một lượt được chốt |
| Quiz luyện qua /api/quiz/attempts cũ | 201; lượt luyện mới không thay bài/bằng chứng đã chốt |
| Bằng chứng của người khác / không tồn tại | 422 |
| Phiên sai robot | Builder chọn phiên #16 cho robot tránh vật cản bị 422 |
| Phiên của mình chưa COMPLETED | Practice chọn phiên #17 của chính mình bị 422 |
| Phiên trước mốc vòng 1 | #4 không cho bằng chứng cũ, chọn #16 bị 422 |
| Vòng 1 chưa làm quiz nhiệm vụ | #3 preview bị 422 trước khi quiz được chốt |
| Preview → confirm | Preview 200, điểm /80; confirm PRG 302, lưu snapshot từ nguồn DB |
| Hai confirm đồng thời | Hai luồng HTTP trả 302/422; SQL xác nhận vòng 1 builder của #2 có đúng một bài |
| Nộp khi đang chờ chấm | 422; vòng SUBMITTED không nhận bài khác |
| Nộp sau kết luận/hết lượt | Form không mở; server kiểm vòng ACTIVE và số bài; NEEDS_REVISION ở lần 2/max2 bị 422; JUnit kiểm giới hạn nộp |
| NEEDS_REVISION → vòng 2 | #1 dùng lại phiên #16 và quiz #12 từ lần 1; #2 dùng lại phiên #16, quiz mới #14 của vòng 2 |
| Lịch sử hai vòng, feedback, điểm | Hiển thị đủ; điểm tự động lưu 80,0 và tổng đạt theo mức giải thích; không thay bản chụp vòng trước |
| PASSED dưới ngưỡng | #6 ngưỡng100, mức giải thích0, tổng80 bị 422 |
| Không có phản hồi cụ thể khi chưa đạt/cần bổ sung | Bean test từ chối; form lưu yêu cầu hai nội dung |
| Sửa chấm không lý do | 422 |
| Sửa khi vòng sau có quiz | 422 |
| Sửa NEEDS_REVISION → PASSED → NEEDS_REVISION | #2 lưu bản chấm mới, huỷ rồi kích hoạt lại vòng chưa dùng; lịch sử hiện thời điểm sửa/lý do |
| CLOSED vẫn chấm bài chờ; không NEEDS_REVISION | #6 đóng, NEEDS_REVISION bị 422; NOT_PASSED lưu thành công |
| Không mở lại CLOSED / ARCHIVED | publish không hợp lệ bị 422; không có action mở lại |
| Gia hạn | Hạn sớm hơn bị 422; hạn muộn hơn thành công |
| REJECT_LATE quá hạn | #11 confirm bị 422; #12 NEEDS_REVISION sau hạn bị 422; NOT_PASSED vẫn lưu |
| ACCEPT_LATE_FLAGGED | #13 nhận bài sau hạn, SQL is_late=1 và giao diện Nộp muộn; NEEDS_REVISION được lưu |
| Gia hạn REJECT đang OPEN | #11 gia hạn rồi confirm thành công, hiển thị Chờ chấm |
| Không sửa nội dung OPEN | POST saveDraft bị 422 |
| Không giao ADMIN | recipientId ADMIN bị 422, transaction rollback |
| Thêm USER vào OPEN | Student được thêm #2, thấy nhiệm vụ và vòng1 |
| Nhân bản không sao chép người được giao | Bản nháp #7 chưa có recipient nên publish bị từ chối; xoá nháp qua luồng mới thành công |
| Lưu trữ | #6 ẩn khỏi danh sách mặc định |
| Văn bản HTML / đầu vào sai | #10 tên/mô tả chứa HTML được escape; ngày 30/02, số vượt int, robot không có, tiêu đề ngắn, rubric B đều bị 422 |
| Lối vào phân quyền trên màn hình | Chrome đăng nhập giao diện; ADMIN chỉ thấy Quản lý nhiệm vụ, USER chỉ thấy Nhiệm vụ của tôi; bấm link mở đúng JSP |
| Tắt JavaScript | Chrome dùng session đã đăng nhập, thực hiện chọn bằng chứng → preview → confirm #3; ADMIN chấm đạt và USER thấy kết quả, không cần JS |
| Mobile | Chrome 390px, không tràn ngang; đã xem ảnh form và trang chấm |
| Hồi quy trang cũ | GET phòng3D, phiếu kết quả #16, quiz, tra cứu lỗi, tổng kết, hồ sơ, shop, admin-users/stats/shop/quiz đều 200; API quiz/troubleshooting/shop đáp ứng |
| Log ứng dụng | Không thấy SEVERE/Exception/ERROR trong log QA cuối |
| Cleanup | Xem checklist cuối tasks/todo.md; QA được dừng và thư mục cấu hình/log/cookie/mật khẩu tạm được xoá trước bàn giao |

Đã xác nhận 8081 không còn listener và thư mục CATALINA_BASE QA không còn.
Lệnh shutdown chỉ dùng cấu hình QA (shutdown8006/HTTP8081). Lúc kiểm tra cuối,
8080 không phản hồi; agent không gửi lệnh dừng hay khởi động instance đó.
Người dùng cần build/deploy WAR mới và Run Tomcat từ IntelliJ để dùng chức năng.

Một lần smoke ban đầu gọi nhầm /troubleshooting (không có route) nhận 404;
đã kiểm lại đúng pages/tra-cuu-loi.html và /api/troubleshooting-guides, đều 200.

## Dữ liệu QA đã tạo

Mọi nhiệm vụ có tiền tố [QA], tạo qua form mới; quiz/nộp/chấm qua đúng luồng.
Không tạo tài khoản, không sửa dữ liệu lắp ráp cũ. SQL chỉ đọc để đối chiếu.

| ID | Tiêu đề còn trong DB |
| --- | --- |
| 1 | [QA] vòng nộp và chấm |
| 2 | [QA] quiz, sửa chấm và tranh chấp |
| 3 | [QA] quiz vòng 1 bắt buộc |
| 4 | [QA] mốc vòng 1 |
| 5 | [QA] sai mẫu và chưa hoàn tất |
| 6 | [QA] ngưỡng, đóng và lưu trữ (ARCHIVED) |
| 9 | [QA] quyền riêng |
| 10 | [QA] &lt;b&gt;Escape&lt;/b&gt; (DRAFT, kiểm escape) |
| 11 | [QA] REJECT quá hạn |
| 12 | [QA] REJECT chấm quá hạn |
| 13 | [QA] ACCEPT nộp muộn |

#7 là nháp nhân bản được xoá bằng chức năng mới; #8 là ID bị tiêu thụ trong
transaction tạo nháp bị rollback, không có bản ghi. Tới cuối QA có 9 bài nộp
(#1–#9), các lượt quiz nhiệm vụ #12–#18, #20–#23 và một lượt luyện API #19.
Các bản chấm được giữ làm lịch sử. Không tự dọn dữ liệu nhiệm vụ đã nộp; có thể
lưu trữ các nhiệm vụ QA còn OPEN bằng giao diện quản trị.

## Giới hạn và khác biệt được chấp thuận

- Chỉ builder có phiên COMPLETED; practice chỉ PREPARING, student chưa có phiên.
  Người dùng đã chọn dùng builder ở hai nhiệm vụ và ghi giới hạn. #1 kiểm dùng
  lại quiz ở vòng2, #2 kiểm quiz riêng vòng2. Không giả lập hai USER đều có phiên
  hoàn tất, không tạo phiên hay sửa dữ liệu lắp ráp để lấp QA.
- Hồi quy 3D/shop/quiz cũ gồm GET/smoke API và một lượt quiz luyện; chưa lặp lại
  lái robot 3D, lắp lại robot hay checkout mua sắm đầy đủ.
- Trình duyệt thực chứng là Chrome desktop/mobile và NoJS; Firefox/Safari chưa
  kiểm chứng. Mẫu B/chẩn đoán lỗi thuộc chặng2, chưa triển khai.
- Không có test giả “đã chạy”: số test, HTTP/concurrency và SQL nêu trên là
  kết quả đã chạy. Ảnh Chrome và browser-checks.json ở target/phase6-qa
  (gitignored; Maven clean sẽ xoá), báo cáo này giữ nội dung kiểm chứng.

Commit tài liệu sửa ghi chú Đợt5b: `6018d7b` — `docs: correct phase 5b audit notes`.
Commit chức năng: `feat: add practice tasks with submission and review`;
hash xem git log hoặc bàn giao cuối. Không push.
