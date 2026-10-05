# Đợt 6 chặng 2 — Chẩn đoán và mẫu B

Ngày kiểm chứng: 05/10/2026. Bắt đầu `a1650e0`, branch
`integration/fullstack-v2`, ahead3. Chỉ handoff untracked; không sửa file cá nhân
ngoài phạm vi. Java17/Tomcat9/MySQL thật, CATALINA_BASE riêng8081. ADMIN dùng
mật khẩu chủ tài khoản đã cung cấp trong hội thoại, không ghi vào tài liệu/Git.

## Kết quả và giới hạn nghiệm thu

Code chẩn đoán độc lập và mẫu B đã triển khai, điểm và luồng builder đạt.
Chưa đạt toàn bộ tiêu chí prompt ở hai điểm được ghi rõ: golden HTML ADMIN
khác11 form vì yêu cầu radio; chưa có lần nộp đầy đủ bằng USER thứ hai.
Không diễn giải hai giới hạn này thành “toàn bộ nghiệm thu đạt”.

| Mục | Bằng chứng / kết quả |
| --- | --- |
| Baseline | npm114/114; Maven52/52 trên JDK17; build WAR trước sửa |
| Test-first | Bốn source test chẩn đoán đỏ ENOENT; JUnit đỏ thiếu lớp/method trước code |
| Cuối | npm119/119,0 fail/skip; Maven59/59,0 fail/skip; clean package BUILD SUCCESS |
| Bean tests mới |7 test:4/3/3; B71,4/80,0/30,0; biên HALF_UP; điểm chẩn đoán chưa làm tròn; quan sát; nguồn/round/PRACTICE; seed công bố; draft vượt độ dài; conclude sai kind/lặp |
| Guard | Code ứng dụng mới/sửa qua slide-style; Java tối đa120 ký tự, JSP140; chèn stream và dòng121 vào bản sao tạm bị bắt, bản sao được dọn |
| Cấu trúc | JavaBeanRulesTest và JspExpressionContractTest xanh; Servlet→bean→XxxDB→JSP; không framework/ORM/PDF/JS dependency |
| DB | Migration010 chạy trên DB local .env; schema_migrations=010_diagnosis_practice; chạy lại migration/seed không lỗi |
| Seed |5 tình huống đúng luật công bố được đọc từ SQL trong JUnit; chạy lại seed bản cuối giữ nguyên các hàng PUBLISHED cũ |

Node/JUnit là công cụ kiểm chứng, không phải stack phục vụ trang. Công thức,
quyền, nguồn bằng chứng và trạng thái nằm ở JavaBean/XxxDB, JSP chỉ trình bày.

## File thêm/sửa

- Mới: `business/DiagnosisScenario.java`, `DiagnosisCheck.java`,
  `DiagnosisOption.java`, `DiagnosisAttempt.java`; `data/DiagnosisDB.java`;
  `controller/DiagnosisServlet.java`, `AdminDiagnosisServlet.java`;
  `util/DiagnosisFormUtil.java` (gốc Java là `tomcat-app/src/main/java/vn/edu/webpro/robotlab/`).
- Mở rộng: `PracticeTask`, `TaskRecipient`, `TaskRound`, `TaskRubric`,
  `TaskSubmission`; `PracticeTaskDB`, `TaskSubmissionDB`; `TaskServlet`,
  `AdminTaskServlet`, `AdminTaskReviewServlet`; `TaskFormUtil`.
- View mới trong `WEB-INF/views/`: `diagnosis-list/play/result.jsp`,
  `admin-diagnosis-list/form/preview.jsp` (tên file thực tế có prefix đầy đủ).
  Sửa `task-view/submit/preview.jsp`, `admin-task-form/review.jsp`;
  lối vào `pages/tai-khoan.html`, `assets/js/account.js`, tăng cache-bust JS.
  CSS và các view/task-list cũ không sửa.
- DB: `database/migrations/010_diagnosis_practice.sql`,
  `database/seed-diagnosis-phase6.sql`, `database/schema.sql`; migration009 không sửa.
- Test: `DiagnosisTest.java` mới; `JspExpressionContractTest.java` thêm BEANS;
  `tests/server/diagnosis.test.cjs` mới, `practice-tasks.test.cjs` mở guard;
  `learning-profile.test.cjs` cho phép migration010 của chức năng khác.
- Tài liệu: README, API_CONVENTIONS, erd, TEAM_FLOW_DEMO_GUIDE, plan/todo,
  báo cáo này; AGENT_HANDOFF cập nhật nhưng không commit.

## Hồi quy HTML mẫu A

Golden77 trang GET trước/sau (ba USER và ADMIN), bỏ whitespace giữa thẻ, gộp
whitespace, token CSRF→TOKEN. Chụp after trước khi thêm nhiệm vụ QA-B, tránh
nhầm dữ liệu mới trên danh sách với thay đổi renderer.

- 66/77 trang giống hoàn toàn sau chuẩn hóa; mọi trang USER giống.
- 11 trang khác trong form ADMIN: `/admin-tasks?action=new`,
  `/admin-tasks?action=edit&id=10`, `/admin-task-reviews?id=1` đến `id=9`.
- 77/77 giống khi bỏ khối form; ngoài form không có thay đổi.
- WAR cuối kiểm lại69 trang chi tiết/form A từ manifest, giống after; danh sách
  sau đó có QA-B mới nên không so với dữ liệu cũ. Không sửa golden để làm khớp.

Ngoại lệ được nêu trong lúc triển khai: prompt vừa yêu cầu HTML A nguyên vẹn
vừa cấm select trong file sửa. Hai điều kiện không đồng thời thỏa được cho form
ADMIN vốn có select. Chọn radio theo trọng tâm slide; tên field/giá trị và luật
giữ nguyên, thêm lựa chọn rubric B và tình huống. Đây là khác biệt thật, không
báo diff rỗng toàn bộ. `admin-task-list.jsp` không sửa, còn select lọc trạng thái.

Ví dụ diff cấu trúc (nội dung c:out và giá trị cũ giữ nguyên):

```diff
- <select name="robotId">...<option value="line-follower">...</option>...</select>
+ <input type="radio" name="robotId" value="line-follower" ...>...
- <select name="explanationLevel">...<option value="4">...</option>...</select>
+ <input type="radio" name="explanationLevel" value="4" ...>...
```

Diff cụ thể từng đoạn và HTML thô nằm ở bằng chứng local `golden-diff.json`,
`before/`, `after/`; không commit token/HTML QA vào repo.

## Phase4 — QA chức năng thật

Các script dùng request HTTP/cookie đăng nhập và SQL read-only để đối chiếu.
`live-results.json` có243 kết quả kiểm, gồm các lần kiểm lại; `browser-results`
có12 kiểm bằng Chrome. Hai lỗi script (URL 3D gõ sai, so chữ có newline) đã được
sửa rồi chạy lại đúng route/chuẩn hóa whitespace; không coi là lỗi ứng dụng.

| Nhóm | Kết quả và bằng chứng |
| --- | --- |
| Danh tính/quyền |4 login200; khách năm route302+no-store; USER vào admin GET/POST token hợp lệ403; lượt người khác GET/POST404; nhiệm vụ không được giao GET/POST404 |
| CSRF/mã lỗi | Năm route thiếu/sai token403; action GET lạ422; task id abc/-1/quá lớn422; round lạ404; confirm ngắn422; oversized nháp422 trên WAR cuối |
| Không lộ dữ kiện | HTML play thật không chứa bất kỳ quan sát chưa chọn, feedback/giải thích/cờ đáp án/cần thiết; sau check chỉ hiện quan sát đã ghi; sau conclude mới hiện kết quả |
| Check/kết luận | Check khác tình huống422; cùng check hai lần SQLcount1; sai kind/option khác tình huống422; kết luận lần hai422; không chọn phép vẫn được kết luận và điểm thu thập0 |
| Quản lý tình huống | Nhân bản6103→6106 nháp, save/preview/publish qua form; preview không tạo attempt; USER start draft404; sửa PUBLISHED422; archive ẩn luyện/new task nhưng TASK cũ tiếp tục |
| Tạo nhiệm vụ B | Draft/view/preview/publish hai USER; sai robot hoặc DRAFT/ARCHIVED scenario422; sửa trường cốt lõi OPEN422 |
| Quiz | Thiếu câu422, SQL không thêm lượt; lượt nhiệm vụ chốt; trang luyện/result cũ không thay slot; quiz dùng lại ở vòng sau được giữ |
| Nộp B | PRACTICE id giả422; own TASK còn dở422, không fallback; phiên người khác/không COMPLETED/sai robot/trước mốc422; snapshot80,0 và30,0 lưu đúng; khi chờ chấm hiển thị /80 |
| Vòng bổ sung | Task14 builder vòng2 dùng lại diag/quiz (reused_from1); task15 vòng2 diag mới+quiz cũ; NEEDS_REVISION→PASSED, lịch sử đủ hai vòng/provenance; luyện sau không đổi snapshot |
| Chấm | Admin thấy quá trình/quan sát/thời điểm/đúng sai; PASSED dưới ngưỡng422; sửa không lý do422; bắt đầu diag vòng sau chặn sửa chấm422; hết lượt/CLOSED không NEEDS_REVISION; CLOSED vẫn NOT_PASSED được |
| Hạn | Task18 REJECT quá hạn confirm422; task19 ACCEPT quá hạn lưu is_late1, hiện cờ muộn; ACCEPT vẫn cho bổ sung khi còn lượt |
| Hồi quy |3D, receipt session16, quiz luyện, tra cứu, tổng kết, hồ sơ, shop, API quiz GET, năm trang admin cũ200; toàn bộ test cũ xanh |
| Trình duyệt | Chrome JS-off thực sự start/check/conclude và tạo nháp B qua form;8 trang390px scrollWidth=390; ảnh xem trực tiếp; role link USER/ADMIN đúng |
| Log | Log Tomcat QA không có Exception/SEVERE ứng dụng;6 JSP diagnosis được GET biên dịch; form admin nhiệm vụ cũng GET biên dịch |

Sơ suất QA được phát hiện ở cuối: VersionLoggerListener mặc định ghi tham số
`-D`, gồm cấu hình DB, vào log riêng tạm. Đã dừng QA, xóa các log này, cấu hình
`logArgs=false/logEnv=false`, chạy lại và xác nhận0 dòng `DB_PASSWORD=`. Không
in giá trị vào hội thoại, không đưa log/config này vào Git hoặc bằng chứng.
Việc dọn toàn bộ thư mục QA cuối cùng loại bỏ các cấu hình tạm còn lại.

### Ba tranh chấp bằng hai luồng thật

| Thao tác | HTTP | SQL đối chiếu |
| --- | --- | --- |
| Hai conclude cùng lượt1 |302 và422 |1 lượt SUBMITTED, required_done1/total2, điểm8,0 |
| Hai start diagnosis cùng vòng task14 | Hai302 tới cùng ID3 | Slot task_rounds trỏ3, đúng1 lượt TASK mới |
| Hai confirm B cùng vòng task14 |302 và422 | Đúng1 submission #10, automatic80,0 |

## Dữ liệu mới còn trong DB để người dùng kiểm/dọn qua chức năng

Không xóa QA cũ, không sửa tài khoản/role/password, không ghi/sửa phiên lắp ráp
cũ. Không tạo user. Seed6101–6105 là dữ liệu chính thức.

| ID nhiệm vụ | Tiêu đề | Trạng thái / kết quả |
| --- | --- | --- |
|14 |[QA-B] Hai người học và chẩn đoán dùng lại | OPEN; builder đạt vòng2, practice chốt quiz+diag chưa nộp |
|15 |[QA-B] Chẩn đoán mới ở vòng bổ sung | OPEN; builder đạt vòng2 với diag mới |
|16 |[QA-B] Điểm thấp và giới hạn kết luận | CLOSED; tự động30,0, NOT_PASSED tổng50,0 |
|17 |[QA-B] Kiểm mốc bằng chứng vòng đầu | OPEN; không nộp vì phiên cũ trước mốc |
|18 |[QA-B] Không nhận muộn | OPEN; quá hạn REJECT, quiz/diag có, chưa nộp |
|19 |[QA-B] Nhận bài có cờ muộn | OPEN; nộp muộn, NEEDS_REVISION, vòng2 ACTIVE |
|20 |[QA-B] Form không JavaScript trên màn hình nhỏ | DRAFT; tạo bằng browser JS-off |

Tình huống QA6106 `[QA-B] Chẩn đoán line và bằng chứng nhiệm vụ`, bản2/parent6103,
đã ARCHIVED. Diagnosis attempts1–11 (USER13/14/15;4 PRACTICE,7 TASK), tất cả
SUBMITTED; bài nộp10–15; bản chấm11–16. Bằng chứng bảng cụ thể ở final-results.
Quiz mới tạo qua chức năng nhiệm vụ; không POST quiz luyện cũ trong đợt này.

Seed local6101–6105 hiện guide_id NULL vì lượt seed đầu dùng NULL. Source cuối
chọn guide tương thích nếu có; INSERT IGNORE không ghi đè hàng PUBLISHED. Link
kết quả vẫn dẫn tra cứu theo robot; QA6106 có guide line-not-detected. Đây là
chủ ý giữ bất biến/dữ liệu đã có, không UPDATE seed để ép khớp source mới.

## Phần còn thiếu / lệch phạm vi

- Chưa có full submission bằng USER practice: phiên17 là PREPARING robot
  obstacle-avoider; builder là người duy nhất có COMPLETED line-follower16.
  Đã hỏi quyết định tạo phiên QA mới qua luồng cũ/tự hoàn tất/ghi giới hạn, chưa
  nhận lựa chọn. Không vượt phép ghi DB “chỉ chức năng mới”. Hai USER đều đã
  quiz và chẩn đoán TASK; cả hai cách vòng bổ sung kiểm bằng builder trên hai
  nhiệm vụ, chưa thay thế yêu cầu full-flow hai người.
- Không kiểm Firefox/Safari. Không POST API quiz cũ vì phép QA ghi chỉ dành
  chức năng mới; GET và test cũ xanh, QuizAttemptDB không sửa trong chặng2.
- Không lặp toàn bộ kịch bản chặng1 (gia hạn/nhân bản nhiệm vụ/sửa chấm thành
  NEEDS_REVISION rồi kích hoạt lại CANCELLED); test cũ vẫn có. Đợt này kiểm lại
  các nhánh bị tác động bởi diagnosis/mẫu B như bảng trên.
- Draft có thể thiếu nội dung; chặn vượt độ dài trước SQL. Feedback lựa chọn
  khi công bố phải có1–2000 ký tự để kết quả có lời giải thích (quyết định chặt).
- Một commit local vì schema/bean/renderer của diagnosis và B phụ thuộc nhau;
  không push. Handoff nằm ngoài commit.

## Bằng chứng local

Sau clean package cuối, giữ bản không bí mật ở
`tomcat-app/target/phase6c-qa/`: golden trước/sau, diff JSON, kết quả live/browser/
final, ảnh390px, độ dài dòng, log test. Target bị gitignore và sẽ mất khi Maven
clean. Không giữ mysql.cnf, Tomcat conf/log/work, cookie, mật khẩu hay npm tạm.
Đã lưu175 file bằng chứng, dừng QA và xác nhận8081 đóng; thư mục riêng chứa
config/log/work/npm được dọn hoàn toàn. Instance8080 không nhận lệnh stop/start.
Hash commit local ghi trong handoff và báo cáo cuối; không push.

## Đợt 6d — Bộ lọc nháp và guard JSP (05/10/2026)

Lỗi duy nhất tìm thấy khi rà15 JSP Đợt6:
`WEB-INF/views/admin-task-list.jsp:17` ở HEAD d608401 có
`</option value="DRAFT">` thay vì mở option; ADMIN không chọn được Nháp qua UI.
Cùng khối còn select/option ngoài kỹ thuật cho phép. Nay dòng17 bắt đầu
fieldset, năm radio ở20/25/30/35/40 giữ `name=state`, giá trị rỗng/DRAFT/OPEN/
CLOSED/ARCHIVED, nhãn và nút Lọc. Không thấy lỗi đóng/cân bằng form/table/
section/fieldset/label/ul/ol/div/thead/tbody/tr khác trong phạm vi; không sửa
HTML ở chỗ không lỗi.

`AdminTaskServlet.java:81` chỉ thêm request attribute `taskStateFilter` bằng
`TaskFormUtil.text(request,"state")`, cùng chuẩn hóa đang dùng khi gọi DB.
Nhờ vậy radio phản ánh cả URL có khoảng trắng như `%20DRAFT%20`. Không đổi
SQL, luật, tên tham số hay mã lỗi. JSP chỉ chọn checked bằng EL/c:if.

Guard độ dài/core EL cũ thực tế đã quét15 JSP; khoảng trống là kiểm select ở
diagnosis.test loại admin-task-list và chưa có kiểm closing attributes/balance.
Nay practice-tasks.test dùng danh sách tự liệt kê bốn họ `task-*`,
`admin-task-*`, `diagnosis-*`, `admin-diagnosis-*` kết thúc `.jsp` cho guard
HTML/định dạng/slide/form; diagnosis.test cũng quét đủ bốn họ. Guard mới bỏ
directive/comment/EL/JSTL khi kiểm stack11 thẻ khối, bỏ void tags. Test giả lập
file diagnosis-future được quét, order-history cũ không bị kéo vào.

| Kiểm chứng | Kết quả |
| --- | --- |
| Baseline → cuối | Node119→121,0 fail/skip; JUnit59→59,0 fail/skip; JDK17 clean package BUILD SUCCESS |
| Test-first |3 test đỏ, đều ở admin-task-list: select, HTML guard, thiếu năm radio; sau sửa xanh |
| Thử chèn | Bản sao admin-task-list thật chèn `<select>` và `</option value="x">` bị bắt đúng từng loại; lệch/chưa đóng thẻ cũng bị bắt; temp được dọn |
| Golden |239 GET,106 trả200;233 giống sau chuẩn hóa whitespace/CSRF;6 chỉ đổi filter; mọi status giữ nguyên |
| USER |204 GET (builder/practice/student mỗi68), trong đó73 trang200, HTML/status đều giống trước |
| ADMIN khác biệt | Chỉ `/admin-tasks`, `?state=DRAFT`, `OPEN`, `CLOSED`, `ARCHIVED`, `zzz`; bỏ đúng form filter thì cả sáu giống; admin view/edit/preview/review/diagnosis không đổi |
| Bộ lọc so SQL | DRAFT #20,#10; OPEN14 nhiệm vụ; CLOSED #16; ARCHIVED #6; mặc định17 nhiệm vụ, ẩn lưu trữ; radio checked đúng |
| Invalid state | zzz200 và không có nhiệm vụ trước/sau; không thêm validation/mã lỗi mới |
| Quyền GET | Khách302 `/pages/tai-khoan.html`+no-store; cả ba USER vào admin-tasks403 |
| Chrome JS-off | Chọn Nháp→Lọc gửi GET, URL state=DRAFT, đúng hai ID; screenshot đã xem; viewport/scrollWidth390px |
| DB chỉ đọc | SHA256 nội dung16 bảng trước/sau và sau browser giống; không migration/POST chức năng/ghi tài khoản; chỉ POST authentication để đăng nhập |
| Log và dọn | VersionLogger logArgs/logEnv=false ngay từ đầu; không DB argument records/Exception/SEVERE;8081 tắt, thư mục config/log/work/npm đã dọn |

Diff khối lọc (phần còn lại của trang giữ nguyên):

```diff
- <select name="state"><option value="">Danh sách mặc định</option value="DRAFT">Nháp</option>...
+ <fieldset><legend>Lọc trạng thái</legend>
+ <label><input type="radio" name="state" value="" ...>Danh sách mặc định</label>
+ <label><input type="radio" name="state" value="DRAFT" ...>Nháp</label>
+ ... radio OPEN/CLOSED/ARCHIVED có checked theo trạng thái ...
+ </fieldset>
```

Chi tiết HTML/diff từng route và492 file bằng chứng local ở
`tomcat-app/target/phase6d-qa/` (Maven clean sẽ xóa): before/after, results JSON,
hash DB, screenshot390px và log test đỏ/xanh. Không commit token/config/log QA.
Đợt6d sửa sáu file tracked: JSP, một dòng Servlet, hai file Node test, todo và
phần báo cáo này. Handoff ngoài commit; README/API/ERD không đổi vì không đổi
route/quyền/SQL/dữ liệu hay luật nghiệp vụ. Commit local, không push.

Ngoài phạm vi gặp khi rà, chỉ ghi nhận và không sửa:
`pages/tra-cuu-loi.html:44,50`, `admin-content.html:112`, `admin-shop.html:40`,
`lap-rap.html:90`, `admin-quiz.html:60`, `admin-troubleshooting.html:75` còn select;
`WEB-INF/views/order-history.jsp:36,38,43` còn fmt:formatNumber.
Chưa kiểm Firefox/Safari. Full-flow nộp B của hai USER vẫn chưa kiểm vì practice
cần hoàn tất lắp ráp thật; đợt này chỉ đọc, không tự tạo/hoàn tất phiên.
