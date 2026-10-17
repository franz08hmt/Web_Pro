# Final hardening — kiểm chứng ngày 17/10/2026

Kết luận: **đủ điều kiện đề nghị triển khai bản chốt** cho F1/F2/F3.
Bản sửa **chưa triển khai lên final8081, chưa push**. Không thay schema, seed, quyền,
rubric, luật/spec đã công bố hoặc giao diện. Không tuyên bố toàn bộ repo đúng mọi slide.

## 1. Bản source và artifact

| Bản | Kết quả đã kiểm |
| --- | --- |
| Baseline sạch `d869e8d` | Archive Git riêng; build JDK17; 234 Node, 149 JUnit đạt, không fail/skip |
| Working tree | Sáu file sửa dở và bốn file chưa theo dõi được giữ nguyên, không dùng dựng WAR |
| Source bàn giao `c9e3a35b724e0fc11daa5c1aaf34ac8ff544c1e4` | Archive Git riêng; `mvn -q test`, `mvn -q clean package`, `npm test` đạt |
| WAR bàn giao | 237 Node, 159 JUnit; 0 fail/error/skip; 179 class ứng dụng có major version 61 |
| QA8082 | Chạy chính WAR bàn giao, DB bản sao riêng; HTTP và trình duyệt kiểm mới; đã dừng/dọn |
| QA8083 | Runtime riêng kiểm launcher, cùng WAR bàn giao; đã dừng/dọn |
| Final8081 | Vẫn PID 32216, health 200 và WAR cũ; không restart, thay WAR hoặc đổi kết nối |

Môi trường thực tế: Temurin **17.0.19**, Maven **3.9.15**, Tomcat **9.0.120**,
Node **24.13.0**, Chrome qua Playwright cục bộ đã có. Không cài/nâng công cụ.
Maven báo platform encoding Cp1252; cấu hình UTF-8 của project giữ nguyên và test
tiếng Việt/JSON/byte tài nguyên đạt. Không biên dịch vào target gốc của repo.

WAR:
`D:/Web-Pro/qa-evidence/final-hardening-20261017/release-source/tomcat-app/target/robot-assembly-lab.war`

SHA256:
`08b51a4371253b0ce3cacd1a964a50edefe690c4f2e0f24d75631e5ab1d1c10d`

WAR final8081 giữ SHA256 cũ:
`7fb5b374ffa668a25f5556c8d838d0145bc7b36fa14c35408df9e2d90d0b4dff`.

## 2. Ba lỗi và bằng chứng trước/sau

| Mục | Nguyên nhân | Sửa tối thiểu | Kiểm chứng |
| --- | --- | --- | --- |
| F1 — đạt | Start cũ đòi thư mục runtime chưa tồn tại, chặn trường hợp Java đã chết | Tách kiểm vòng đời vào script cục bộ; xác minh artifact, cấu hình, PID, command token argfile, thời điểm tạo process và chủ cổng; khóa liên process; cập nhật metadata bằng rename | 21 checkpoint trên QA8083, gồm cold start, hai launcher độc lập cùng mở, reuse, chờ startup, unhealthy, stop/start, orphan, PID/argfile sai, cổng lạ và artifact sai |
| F2 — đạt | Bảy vị trí dùng stream, lambda, method reference, switch hoặc Set.of | Vòng for/if/else và kiểm chuỗi; giữ BigDecimal, thứ tự, null behavior, JSON/code/message | Bảy JUnit mới xanh cả trước và sau refactor; AdminQuiz HTTP trước/sau; AdminStats; bốn nhánh checkout HTTP |
| F3 — đạt | Regex cho 19 chữ số nhưng parseLong có thể overflow | Chỉ bắt NumberFormatException quanh parse ID; dùng nhánh NOT_FOUND hiện hữu trước JDBC | Hai test lỗi trên code cũ, xanh sau sửa; HTTP hai overflow từ 500 thành 404 JSON; owner/guest/collection giữ contract |

F3 trước sửa: bộ `QuizAttemptIdTest` có 3 test, **2 errors** do
NumberFormatException; không phải lỗi DB. Sau sửa cả 3 đạt. Có tái hiện HTTP trên
artifact baseline trước khi đổi sang WAR mới, không chỉ kiểm regex source.

| ID/đường vào | Kết quả trên WAR bàn giao |
| --- | --- |
| Lượt thật của chủ tài khoản | 200 JSON |
| Cùng lượt, tài khoản khác | 404 NOT_FOUND |
| ID không tồn tại trong miền Long | 404 NOT_FOUND |
| `0`, số âm, chữ, 20 chữ số | 404 NOT_FOUND |
| `9223372036854775807` | 404 NOT_FOUND |
| `9223372036854775808`, `9999999999999999999` | 404 NOT_FOUND; baseline là 500 |
| Khách chưa đăng nhập | 401 theo contract cũ |
| Collection `/attempts` và `/attempts/` | 200 JSON, không đổi thành detail lỗi |

SQLException vẫn thoát tới nhánh database-unavailable có sẵn; không nằm trong
catch parse và không bị đổi thành NOT_FOUND. Phần này kiểm bằng phạm vi source
và contract; không chủ động làm hỏng DB đang phục vụ quiz để fault-inject SQL.

## 3. Cú pháp Java trước/sau

| File | Trước | Sau, trách nhiệm được giữ |
| --- | --- | --- |
| [AdminQuizServlet](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AdminQuizServlet.java:120) | stream/anyMatch/lambda | Cờ thiếu lựa chọn trong vòng for đọc request; giữ giới hạn 2–6 và đúng một đáp án |
| [AdminStatsServlet](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AdminStatsServlet.java:55) | stream/mapToLong/:: | Gọi instance method `LearningSummary.summarizeSessions`; tổng hợp nằm trong JavaBean |
| [ShopOrder](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/ShopOrder.java:28) | stream/map/reduce | for-each cộng BigDecimal, không đổi scale hoặc null behavior |
| [OrderServlet](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/OrderServlet.java:66) | Hai switch expression | if/else cho code/message, giữ HTTP409 và nhánh mặc định |
| [JsonUtil](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/JsonUtil.java:33) | switch arrow | if/else theo ký tự, giữ escape của toàn bộ 32 control characters |
| [Robot](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/Robot.java:116) | Set.of | equals theo chuỗi, phân biệt hoa/thường; null vẫn ném NPE như trước |
| [LibraryResource](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/LibraryResource.java:81) | Set.of | equals theo chuỗi; giữ tập giá trị và null behavior |

Đã tìm và đọc ngữ cảnh trên Java production còn lại: kết quả `->` còn thấy là
comment trong file WIP, không phải lambda. Guard mới phủ tám file Java sửa,
loại comment/string trước khi xét cú pháp, kiểm giới hạn 120 ký tự/dòng.
Không sửa JDBC, auth hoặc các cú pháp cũ khác chỉ để làm sạch repo.

Test mới: `CourseSyntaxContractTest`, `CheckoutConflictContractTest`,
`JsonQuoteContractTest`, `QuizAttemptIdTest`, và `final-hardening.test.cjs`.
Assertion ownership trong `quiz.test.cjs` được cập nhật theo biến ID đã parse,
đồng thời bổ sung catch overflow; không bỏ kiểm owner hoặc nới điều kiện lỗi.
Lần chạy trung gian ghi nhận 236/237 Node do assertion cũ đòi parse inline;
bản Git sạch cuối đạt **237/237**, không skip.

## 4. Hồi quy trên WAR mới, DB QA riêng

| Nhóm | Bằng chứng vừa chạy |
| --- | --- |
| Auth/quyền/CSRF/owner | Đăng nhập UI; khách 401 API; USER 403 admin; owner khác 404; CSRF thiếu bị chặn |
| Cả năm mẫu DETAILED | Gắn khi thiếu cha bị từ chối; lắp đủ, tháo lá, undo, hoàn tất server, mở review; owner khác 404 |
| Phòng 3D | Cả năm mẫu: kéo vật thể giữ nguyên camera, kéo vùng trống đổi camera; Q/E, Space, Enter gắn, Ctrl+Z hoàn tác |
| Replay | Bước sau/Phát/Tạm dừng hoạt động; listener network không ghi nhận request ghi; phiếu/in PDF mở được |
| Quiz | Năm mẫu, đáp án không lộ trong public API; server chấm; lịch sử và F3 đúng owner |
| Nối dây/chẩn đoán | Hai bài nối dây: nháp, lưu, stale version 422, nộp, retry; năm tình huống chẩn đoán, kết luận và lịch sử |
| Xưởng/giỏ | Tạo, gắn, check, undo, hồ sơ; thêm thiếu hai lần idempotent, CSRF/owner đúng và không giữ chỗ tồn kho |
| Checkout | Thành công mô phỏng trên QA; lịch sử theo owner; tiền/tồn kho đúng; CART_EMPTY, PRODUCT_UNAVAILABLE, STOCK_UNAVAILABLE, ORDER_TOTAL_LIMIT đều 409 với nguyên thông báo |
| Nhiệm vụ | Công bố → phiên mới → quiz đúng vòng → xem trước → nộp → chấm; user khác vẫn Chưa nộp và không đọc phiên của chủ |
| AdminQuiz/Stats | Valid tạo/sửa/xóa; rỗng, thiếu ID/nhãn, 1/7 lựa chọn, 0/2 đáp án đúng và order sai bị 422; tổng phiên trên JSP khớp COUNT DB |
| Giao diện/link | 103 HTTP theo ba vai trò đúng kỳ vọng; 108 ô viewport khảo sát không tràn; 67 kiểm chuyên biệt trình duyệt đạt; 103 URL nội bộ không link chết ngoài 404 minh họa |
| Console/tài nguyên | Không pageerror; audit link cuối không lỗi asset; 7 tài nguyên HTTP khớp byte artifact; không đổi JS/CSS nên giữ cache version |

Không tính 403 đúng quyền là link chết. Route `/admin-task-reviews` thiếu ID trả
422 đúng contract; có ID bài nộp thật trả 200. Harness ban đầu giả định static
admin mở công khai và review không cần ID; đã đối chiếu filter/controller và
chạy lại ma trận đúng quyền, không sửa backend để chiều test.

Các hiệu chỉnh harness khác được giữ lại trong bằng chứng: health HTTP200 chứa
trạng thái DB (không mặc định 503); điểm kéo phải thực sự nằm trên canvas;
parent ID chuỗi `"0"` phải đọc đúng; chuẩn hóa dấu phân cách đường dẫn Windows.
Ảnh 404 từng phát sinh từ URL sai của **component fixture mới trên QA**, đã sửa
qua API admin trên chính fixture và audit lại. Không phải lỗi catalog final.
Một lần ghi báo cáo song song làm metadata QA bị cũ; đã xác minh lại process/
argfile, chạy ma trận cuối tuần tự và lưu log riêng. Không dùng số kiểm từ báo
cáo cũ hoặc các lần harness sai làm kết quả đạt của bản sửa.

## 5. Launcher ngoài Git và hướng dẫn phục hồi

Đã sửa **ngoài repo**, không nằm trong các commit ứng dụng:

- `D:/Web-Pro/FINAL.ps1`: thao tác Mở đi qua cùng guard vòng đời, bỏ kiểm PID trùng lặp yếu.
- `D:/Web-Pro/qa-evidence/final-deploy-all-models-20261017/deploy.cjs`: start/stop gọi guard.
- Cùng thư mục: thêm `runtime-lifecycle.cjs`, dùng built-in Node, không thêm dependency.

Backup trước sửa: `D:/Web-Pro/qa-evidence/final-hardening-20261017/launcher-backup/`.
Có bản gốc FINAL.ps1, deploy.cjs, run.ps1; run.ps1 triển khai hiện hành không đổi.
Checksum đầy đủ của script mới trong `HANDOFF-MANIFEST.json` ngoài repo.
Không đưa argfile, credential hoặc bản sao DB vào Git.

Lệnh vận hành vẫn là:

```powershell
& 'D:\Web-Pro\FINAL.ps1' -Lenh Mo
& 'D:\Web-Pro\FINAL.ps1' -Lenh Dung
```

Lệnh trên là hướng dẫn cho người dùng, **không được chạy trên final trong đợt QA này**.
Mở khi healthy dùng lại PID; đang khởi động chờ có giới hạn. Process đã chết chỉ
được phục hồi nếu thư mục, argfile, context, port và artifact đều được xác minh.
Process còn sống nhưng DB/health lỗi thì báo và giữ process, không tự restart.
Cổng/PID/metadata lạ, thiếu WAR, checksum sai hoặc exploded khác WAR đều dừng an toàn.
Không tự chạy prepare, seed, đổi DB hoặc mật khẩu khi mở web.

Nếu guard từ chối: kiểm thông báo, PID/chủ cổng và manifest cục bộ; không xóa
runtime/lock mù quáng. Chỉ dùng Stop khi xác minh đúng runtime và có quyền dừng.
Nếu cần quay lại script cũ, đối chiếu backup rồi sao chép thủ công đúng hai file;
module mới có thể giữ không được gọi. **Script cũ vẫn có lỗi F1**, rollback script
không phải cách chữa dữ liệu. Không rollback DB hoặc thay vault mật khẩu.

QA kiểm cold start và hai lệnh mở độc lập đồng thời chỉ sinh một PID; stop/start;
kill đúng Java QA để tạo orphan; cổng do process khác chiếm; PID/creation/argfile
sai; WAR thiếu/sai checksum; exploded sửa nội dung hoặc thêm class lạ. Ca process
sống nhưng unhealthy được kiểm cả probe fixture và runtime thật dùng thông tin
kết nối QA sai có chủ đích: health HTTP200 nhưng DB không connected, PID không
đổi và không bị launcher giết. Cấu hình nhạy cảm của runtime này đã dọn.

Không đóng gói/phát hành launcher vào Git trong đợt này. Nếu cần launcher có
version trong repo, cần duyệt riêng vị trí và cách tách cấu hình/secret.

## 6. Dữ liệu, WIP và cleanup

- DB `robot_lab_hardening_20261017` được tạo mới sau kiểm không tồn tại, từ bản
  chụp nhất quán của final; không nhập/seed lại vào final. Giữ 54 bảng, không đổi schema.
- Login ứng dụng `ral_hardening_20261017` chỉ SELECT/INSERT/UPDATE/DELETE trên đúng DB
  QA, host 127.0.0.1; không DDL hoặc quyền trên DB final.
- Đăng ký USER QA mới `hardening-learner@robotlab.test` (ID4) qua API. Dùng ADMIN/Tài
  đã có trong bản sao; không tạo admin credential hoặc reset mật khẩu final/Học viên.
- Các phiên QA mới ID8–24; lượt quiz ID3–20; thiết kế ID1–3; đơn ID1–3;
  nhiệm vụ QA ID3–4, bài nộp gần nhất ID3. Đây là dữ liệu **DB QA**, không phải ID
  của final. Có component/product `hardening-checkout-part`/`hardening-checkout`
  để kiểm lỗi checkout, sản phẩm fixture đã ngừng bán bằng API.
- Inventory đầy đủ: `QA-inventory.json`; điểm/trạng thái đều sinh qua server.
  Không nói đã khôi phục DB QA: nhật ký, version, timestamp và dữ liệu thử vẫn còn.
- Hash chỉ đọc trước/sau **54/54 bảng final khớp**; không có bảng đổi.
- **44/44 hash file bảo vệ khớp**, gồm 10 WIP/untracked; không stage WIP.
  Sáu file tracked bảo vệ được build từ bản commit, không lấy phần sửa dở;
  bốn file untracked không nằm trong snapshot/WAR.
- QA8082/8083 đã dừng đúng process tự tạo; conf/log/work/temp/argfile runtime đã dọn.
  DB QA, inventory, backup và vault DPAPI vẫn giữ trong private ACL ngoài repo.
- Final8081 vẫn PID32216/health200, WAR cũ; 8080 không có listener tại lần kiểm cuối,
  không bị can thiệp. Không sửa .env, IDE/global config hay đọc clipboard.

## 7. Bằng chứng và giới hạn bàn giao

Thư mục: `D:/Web-Pro/qa-evidence/final-hardening-20261017/`.

| Bằng chứng | Nội dung |
| --- | --- |
| `HANDOFF-MANIFEST.json` | Commit, artifact/checksum, môi trường, số kiểm, script checksum, backup |
| `baseline-node.log`, `f2-before-tests.log`, `f3-before-tests.log` | Baseline và khóa hành vi/tái hiện trước sửa |
| `release-node.log`, `release-test-totals.json`, `release-package.log` | 237 Node, 159 JUnit, build sạch, bytecode17 |
| `targeted-before.json`, `targeted-after-release.json` | ID overflow và AdminQuiz HTTP trước/sau |
| `lifecycle-results.json` | 21 checkpoint cuối trên WAR bàn giao; raw harness trước hiệu chỉnh vẫn giữ riêng |
| `release-functional-flows.log`, `release-task-flow.log`, `checkout-branches-results.json` | Hồi quy ghi trên DB QA cô lập |
| `browser-hardening-results.json`, `http-release-results.json`, `results.json` | Chuột/phím, viewport, quyền, hash DB, link/asset |
| `room-*-390.png`, `room-*-768.png`, `room-*-1440.png`, `assembly-receipt.pdf` | Ảnh Chrome và bản in thực tế |
| `protected-before.json`, `protected-after.json`, `QA-inventory.json` | Bảo vệ WIP và dữ liệu QA thực tế |

Không kiểm trực tiếp nút Mở/Stop trên final8081 vì chưa được duyệt thao tác vòng
đời final; dùng cùng guard/module trên QA để nghiệm thu F1. Không reset hoặc
thử lại mật khẩu Học viên final đang có khả năng khác vault. Không fault-inject
lỗi SQL vào DB final. Kiểm trình duyệt là Chrome trên máy này; không khẳng định
mọi trình duyệt/thiết bị. Không phát hiện blocker mới trong phạm vi F1/F2/F3.

Để triển khai sau khi được duyệt: dùng đúng WAR/checksum trên; phối hợp dừng/
thay artifact final có kiểm soát; cập nhật manifest/checksum kỳ vọng của launcher
tương ứng WAR mới, giữ DB/vault đã chốt; khởi động và đối chiếu HTTP/F3/luồng đọc.
**Chỉ thay WAR mà không đồng bộ checksum thì guard phải từ chối.** Chưa thực hiện
các bước triển khai này và chưa gọi final8081 là bản hardening mới.

## 8. Commit và vài câu vấn đáp

- `381c9bd` — `refactor(java): replace advanced syntax without changing behavior`.
- `2f38281` — `fix(quiz): return not found for overflowing attempt IDs`.
- `c9e3a35` — `test(quiz): retain owner guard after bounded ID parsing`.
- Commit tài liệu nghiệm thu sau ba commit trên không thay production/WAR.

Không push; không force/reset/rebase/tag/merge. Các script ngoài repo không được
gọi là đã commit. Trạng thái cuối chỉ còn sáu M và bốn untracked ban đầu.

**Vì sao regex chưa đủ bảo vệ ID?** Chuỗi có đúng 19 chữ số vẫn có thể lớn hơn
Long.MAX_VALUE. Servlet bắt NumberFormatException đúng chỗ parse; dữ kiện hợp lệ
mới tới XxxDB cùng user lấy từ session; lỗi JDBC không bị nuốt thành 404.

**Vì sao tổng tiền/tổng phiên vẫn ở JavaBean?** ShopOrder cộng BigDecimal bằng
vòng for; AdminStats gọi LearningSummary để tổng hợp dữ kiện. Servlet giữ vai trò
điều phối và JSP chỉ trình bày request attribute, không tự tính nghiệp vụ.

**Vì sao launcher không chỉ kiểm PID?** PID có thể được dùng lại. Cần đối chiếu
command/argfile, executable, thời điểm tạo, cổng, cấu hình và checksum artifact;
trường hợp chưa rõ phải từ chối, không tự giết process hoặc seed dữ liệu.
