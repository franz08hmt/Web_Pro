# Kiểm chứng Đợt 6b — 05/10/2026

Nhánh integration/fullstack-v2, HEAD trước sửa a1273fe. Chỉ AGENT_HANDOFF.md
untracked; không có file người dùng đang sửa. Không push, không migration,
không tạo tài khoản, không sửa/xoá dữ liệu hoặc nhiệm vụ [QA].

## Phạm vi thay đổi

15 file Java (12 file Đợt 6 và đúng phần mở rộng QuizAttemptDB/UserDB/SessionUtil),
chín JSP. Tách lệnh/dòng, thêm braces và Javadoc ngắn tiếng Việt; giữ chữ ký,
field/action/form/token, literal SQL, thứ tự khóa và commit/rollback. Không sửa
CSS/JS hay cache-busting. Giữ c:set learnerAction để tránh thay HTML.
Inline EL/c:out và các đoạn văn vẫn đi cùng tiền tố/hậu tố để không chèn khoảng
trắng vào mã phiên, tỷ lệ điểm, hidden field hoặc nội dung textarea.

Chỉ đổi hai hành vi: getter showCurrentRoundQuiz cho vòng ACTIVE trong view/submit;
GET action lạ trả task-error422. Trang chấm nhận action rỗng hoặc view để xem.
Lịch sử bài nộp vẫn ghi quiz và nguồn dùng lại theo bản chụp.

Test thêm vào practice-tasks.test.cjs (ba Node) và TaskRubricTest (một JUnit).
Một regex trong quiz.test.cjs được chỉnh nhận block có braces; giữ nguyên
kịch bản/khẳng định chấm ở server. API docs, plan/todo và báo cáo Đợt6 được đồng bộ.

## Test / build / tính tương đương

| Kiểm chứng | Kết quả thực chạy |
| --- | --- |
| Baseline | Node111/111; JUnit51/51 |
| Test-first Node | Ba test mới đỏ trước sửa: định dạng, mã lỗi GET, getter trong JSP |
| JUnit đỏ | Lần đầu bị class69 do IDE; chạy bản sao bean baseline với test mới trên JDK17 xác nhận cannot find symbol isShowCurrentRoundQuiz |
| Sau nhóm Java | clean test52/52; Node còn đỏ các JSP/regex/định dạng chưa sửa |
| Cuối | Node114/114,0 fail/skip; JUnit52/52,0 fail/skip; clean package BUILD SUCCESS |
| Guard slide-style | Xanh; test chèn stream vào bản sao tạm vẫn bắt được vi phạm và dọn bản sao |
| Guard định dạng | Xanh; giới hạn Java120/JSP140; bắt lệnh ghép, thiếu braces và thiếu khoảng trắng control flow |
| So Java trước/sau | 15 file giữ token/biểu thức/literal, bỏ riêng comment/braces/format/import order và đúng hai sửa 3A/3B |
| WAR | Tất cả WEB-INF/classes major61, đúng Java17 |
| Jasper | Biên dịch đủ9 JSP,0 lỗi; chạy JDK17, mã servlet JSP sinh ở target1.8 tương thích runtime |
| DB | SHA256 và số dòng của tám bảng giống trước/sau; chỉ SELECT để đối chiếu |
| Log QA | Không có SEVERE/Exception/ERROR ứng dụng |

IntelliJ tự ghi class Java25 vào target đã làm hai lần Maven test không chạy
được. Không thay IDE; Maven clean với JAVA_HOME=JDK17 dựng lại và WAR được
kiểm bytecode. RED của JUnit được xác nhận riêng trên bản sao baseline để
phân biệt thiếu getter với lỗi môi trường, không dùng lỗi bytecode làm bằng
chứng kiểm hành vi. Công cụ định dạng/parser/Jasper phụ trợ chỉ ở thư mục tạm,
không thêm dependency vào project.

## Độ dài dòng lớn nhất từng file

Đơn vị: số ký tự, không tính CR/LF. Bảng đo toàn file trước/sau; với ba file
mở rộng cũ guard chỉ kiểm phần Đợt6. Các phần ngoài Đợt6 không bị refactor.

| File | Trước | Sau |
| --- | ---: | ---: |
| PracticeTask.java | 135 | 97 |
| TaskRecipient.java | 110 | 80 |
| TaskReview.java | 128 | 98 |
| TaskRound.java | 82 | 76 |
| TaskRubric.java | 134 | 99 |
| TaskSubmission.java | 120 | 76 |
| TaskServlet.java | 151 | 99 |
| AdminTaskServlet.java | 150 | 97 |
| AdminTaskReviewServlet.java | 164 | 98 |
| PracticeTaskDB.java | 225 | 100 |
| TaskSubmissionDB.java | 438 | 108 |
| TaskFormUtil.java | 148 | 87 |
| QuizAttemptDB.java | 153 | 118 |
| UserDB.java | 121 | 99 |
| SessionUtil.java | 105 | 101 |
| task-list.jsp | 490 | 117 |
| task-view.jsp | 1980 | 140 |
| task-quiz.jsp | 1598 | 136 |
| task-submit.jsp | 1350 | 140 |
| task-preview.jsp | 1060 | 131 |
| task-error.jsp | 490 | 116 |
| admin-task-list.jsp | 740 | 133 |
| admin-task-form.jsp | 755 | 137 |
| admin-task-review.jsp | 944 | 139 |

## Golden HTML trước / sau

Chụp từ WAR trước sửa và WAR cuối, cả ba USER và ADMIN đăng nhập được.
GET danh sách/mục archived, tất cả id1–25 với view/quiz/submit của USER;
ADMIN thêm danh sách/new/edit/view/preview và bài chấm id1–25. Không POST để
chụp trang hoặc tạo dữ liệu. Có77 trang200: builder23, practice11, student8,
admin35; status matrix trước/sau giống hệt.

Chuẩn hoá bỏ whitespace giữa các thẻ, gộp whitespace thành một và thay hidden
csrfToken bằng TOKEN. Không trang nào có thời điểm "hiện tại"/Lập lúc cần loại;
mọi ngày bằng chứng và lịch sử được giữ để đối chiếu.

71 trang giống nhau; sáu trang dưới đây chỉ mất đúng paragraph quiz của vòng
hiện tại. Mọi trang admin, quiz/submit và lịch sử vẫn giống nhau. Các trang
submit của vòng SUBMITTED đã trả422 cả trước/sau nên không có HTML200 để diff.

Lưu ý đối chiếu prompt: #11 là Chờ chấm, không phải kết luận cuối. Vòng của
bài này cũng SUBMITTED, nên công thức getter được chỉ định (chỉ ACTIVE) bắt
buộc ẩn khối quiz ở đầu trang. Đây là tác động của chính mục3A; không đổi
trạng thái/điểm/vòng. Năm trang còn lại có kết luận Đạt/Chưa đạt. Chưa có
khác biệt nào ngoài việc bỏ khối quiz theo getter ACTIVE.

### builder `/tasks?action=view&id=1`

```diff
--- before
+++ after
@@ -21,7 +21,6 @@
 <strong>Đạt yêu cầu</strong>
 </p>
 <p>Vòng 2 · bắt đầu 05/10/2026 10:14</p>
-<p>Quiz lượt #12 – 7/7 – 05/10/2026 10:14 (dùng lại từ lần nộp #2)</p>
 <p>
 <a href="/pages/kiem-tra.html?model=line-follower">Quiz luyện tập (không thay lượt tính điểm)</a>
 </p>
```

### builder `/tasks?action=view&id=2`

```diff
--- before
+++ after
@@ -21,7 +21,6 @@
 <strong>Đạt yêu cầu</strong>
 </p>
 <p>Vòng 2 · bắt đầu 05/10/2026 10:18</p>
-<p>Quiz lượt #14 – 7/7 – 05/10/2026 10:18 (lượt của vòng này)</p>
 <p>
 <a href="/pages/kiem-tra.html?model=line-follower">Quiz luyện tập (không thay lượt tính điểm)</a>
 </p>
```

### builder `/tasks?action=view&id=3`

```diff
--- before
+++ after
@@ -21,7 +21,6 @@
 <strong>Đạt yêu cầu</strong>
 </p>
 <p>Vòng 1 · bắt đầu 05/10/2026 10:18</p>
-<p>Quiz lượt #23 – 7/7 – 05/10/2026 10:28 (lượt của vòng này)</p>
 <p>
 <a href="/pages/kiem-tra.html?model=line-follower">Quiz luyện tập (không thay lượt tính điểm)</a>
 </p>
```

### builder `/tasks?action=view&id=6`

```diff
--- before
+++ after
@@ -21,7 +21,6 @@
 <strong>Chưa đạt yêu cầu</strong>
 </p>
 <p>Vòng 1 · bắt đầu 05/10/2026 10:18</p>
-<p>Quiz lượt #18 – 7/7 – 05/10/2026 10:18 (lượt của vòng này)</p>
 <p>
 <a href="/pages/kiem-tra.html?model=line-follower">Quiz luyện tập (không thay lượt tính điểm)</a>
 </p>
```

### builder `/tasks?action=view&id=11`

```diff
--- before
+++ after
@@ -21,7 +21,6 @@
 <strong>Chờ chấm</strong>
 </p>
 <p>Vòng 1 · bắt đầu 05/10/2026 10:21</p>
-<p>Quiz lượt #20 – 7/7 – 05/10/2026 10:21 (lượt của vòng này)</p>
 <p>
 <a href="/pages/kiem-tra.html?model=line-follower">Quiz luyện tập (không thay lượt tính điểm)</a>
 </p>
```

### builder `/tasks?action=view&id=12`

```diff
--- before
+++ after
@@ -21,7 +21,6 @@
 <strong>Chưa đạt yêu cầu</strong>
 </p>
 <p>Vòng 1 · bắt đầu 05/10/2026 10:21</p>
-<p>Quiz lượt #21 – 7/7 – 05/10/2026 10:21 (lượt của vòng này)</p>
 <p>
 <a href="/pages/kiem-tra.html?model=line-follower">Quiz luyện tập (không thay lượt tính điểm)</a>
 </p>
```

## Kiểm âm — 60 request đều đạt

Các POST chỉ mang token sai, thiếu field, id không được giao, round không
thuộc mình, bằng chứng0 hoặc nội dung ngắn; không request nào có thể lưu bài,
quiz hoặc chấm mới. Ba USER đều được kiểm riêng từng trường hợp.

| Kiểm tra | Kết quả |
| --- | --- |
| Khách vào3 route | 302 về /pages/tai-khoan.html và no-store |
| USER vào2 route ADMIN, cả GET/POST token đúng | 403 |
| USER POST /tasks thiếu/sai token | 403 |
| ADMIN POST2 route thiếu/sai token | 403 |
| USER nhiệm vụ không được giao, GET/POST | 404 |
| USER id abc/-1/vượt long | 422 |
| ADMIN id abc/-1/vượt long trên2 route | 422 |
| GET action lạ trên USER và2 route ADMIN | 422, task-error |
| USER quiz roundId không thuộc vòng | 404, không lưu lượt |
| USER confirm sai số hoặc bằng chứng0/nội dung ngắn | 422, không lưu bài |

Các bảng được so digest: practice_tasks, task_recipients, task_rounds,
task_submissions, task_reviews, quiz_attempts, quiz_attempt_answers,
assembly_sessions. Không thay mật khẩu/role, không dọn nhiệm vụ QA cũ.

## Giới hạn / bàn giao

- ADMIN đăng nhập được và đã so35 trang thật; không dùng mức kiểm yếu chỉ WAR.
- Không thử Firefox/Safari; không nộp bằng hai USER trong đợt chỉ đọc này.
- Không thực hiện lại luồng tạo/nộp/chấm hay các chức năng3D/shop cũ: phạm vi
  đợt này giữ hành vi, so mã/HTML và toàn bộ test cũ vẫn xanh.
- Bằng chứng HTML/manifest/diff/digest/log test/Jasper ở target/phase6b-qa,
  gitignored; Maven clean sẽ xoá. Thư mục QA tạm, credentials và log runtime
  được dọn trước commit; kiểm8081 không còn listener.
- Một commit local theo tên prompt; hash xem git log/bàn giao cuối, không push.
  AGENT_HANDOFF.md tiếp tục untracked, ngoài commit.
