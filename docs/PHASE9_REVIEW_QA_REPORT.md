# Đợt 9 — Sau khi hoàn tất: báo cáo kiểm chứng

## M0 — Khảo sát và nợ kiểm chứng, 14/10/2026

HEAD8da8b15 đồng bộ origin. Baseline173Node/97JUnit, JDK17 clean package xanh.
Sau sửa M0:174Node/97JUnit xanh; guard byte dùng WAR riêng ngoài target của IDE.
Giữ góc khay từ clone trước preview; khi chưa chọn slot, chỉ cộng góc hướng.
Test nguồn đã đỏ trước sửa. Chrome kiểm góc gốc bằng fixture scene tạm, không lưu.
Trên scene thật: kéo vật thể có di chuyển, camera không đổi; kéo vùng trống có
xoay camera;390px không tràn, nút Gắn thấy được, không pageerror.
Script ban đầu chọn vùng trống trùng lớp nhãn trên canvas; đã sửa cách chọn
bằng elementFromPoint, không sửa ứng dụng để ép kết quả kiểm tra.

ADMIN QA mới qua đăng ký: qa-phase9-admin-20261014@robotlab.test, id22;
chỉ tài khoản mới này được nâng admin và tăng session_version theo README.
Không ghi mật khẩu vào báo cáo/file. GET /api/admin/robots, /api/admin/steps,
/admin-stats đều200. Phiên QA mới35 của demo student, v2 IN_PROGRESS, kit đã chuẩn bị;
không sửa các phiên cũ30/31/33/34 hoặc spec đã công bố.
Bằng chứng ngoài Git: D:/Web-Pro/qa-evidence/phase9/m0-results.json, m0-390.png,
baseline/m0 test logs, protected-before.json và database-before.json (hash, không dữ liệu thô).
Không bịa số chương/slide: slide pptx có Word nhúng; tài liệu đợt9 ghi tên chủ đề
Servlet/request/response/JavaBean/EL/JSTL/JDBC/session/transaction khi chưa trích xác minh.

## M1 — Nhật ký transaction

ERD viết trước migration014, thêm một bảng và spec_id phục vụ FK cùng phiên/spec.
DDL014 đã chạy đúng DB local, schema_migrations có014, tổng44 bảng. Không backfill
hoặc sửa spec/session hoàn tất cũ. ACCEPTED và placement/version cùng commit;
REJECTED commit riêng dữ kiện từ chối trong cùng khóa phiên, không đổi mô hình/version.
Không ghi NOT_FOUND/UNCHANGED hoặc thao tác trên phiên COMPLETED đã khóa; cap300 từ chối.
Bean sự kiện giữ hướng trước, nhãn tiếng Việt và Date/formatter giờ Việt Nam.
Node mới đã đỏ trước implementation; cuối177Node/100JUnit, package JDK17 xanh.
Guard hồ sơ chỉ thêm014 vào danh sách migration của chức năng khác, vẫn chặn migration lạ.
Live phiên35: ATTACH ACCEPTED → ATTACH REJECTED sai hướng → DETACH ACCEPTED → ATTACH ACCEPTED.
Retry và NOT_FOUND không thêm dòng; từ chối giữ layout_version. Evidence m1-results.json.
## M2 — Bản vẽ theo phiên

AssemblyDrawing/DrawingItem dựng hai phép chiếu x,z và x,y, khối gợi hình theo loại.
JUnit đã đỏ do thiếu bean, sau đó103/103; Node177/177 xanh. Test10 mục/cả hai góc,
bounds SVG, khung rỗng/một vật thể không chia0, GROUP không đoán tọa độ.
Fragment core JSTL/c:out, tọa độ SVG chỉ từ getter số; có chú giải và giới hạn tỉ lệ.
BEANS map đăng ký hai bean; Jasper/render fragment sẽ kiểm trên trang thật ở M3.
## M3 — Trang hoàn thiện Servlet/JSP

/assembly-review đọc owner từ session, no-store, setAttribute và forward. Có ảnh/bản
vẽ, bước chuẩn/trình tự/thống kê lỗi, phiếu/in và quiz đúng model. Điểm quiz hiển thị
score/total đã lưu, best so tỷ lệ câu đúng; không thêm thang điểm. Chưa COMPLETED chỉ
có thông báo/tiếp tục; GROUP có ảnh/bước/phiếu/quiz, không đoán tọa độ hoặc replay.
Điều hướng hoàn tất chuyển sang review, URL phiếu cũ vẫn giữ. Các assertion đường
dẫn cũ cập nhật có chủ đích theo route mới, vẫn kiểm ../ và quyền/escape của phiếu.
Manifest/fixture bổ sung đúng một trang mới (36); không sửa hợp đồng form trang cũ.
Cookie/session/CSRF/owner và các file WIP không đổi. JS trên trang chỉ gọi window.print.

179Node/105JUnit/package xanh. Live51 ca:30/31/33/34, GROUP16, bốn phần,20 SVG items
cho hai góc nhìn/10 hàng chú giải, quiz model đúng, pending35, foreign404, guest302,
no-store,390px không tràn, không browsererror/asset404. Đã export PDF3 trang, render
và xem mọi trang. Runtime/PDF phát hiện fragment thiếu pageEncoding bị Jasper đọc
sai dấu; đã thêm UTF-8 riêng, test đỏ→xanh và kiểm lại HTML thật đúng chữ.
Print áp box-sizing trong namespace riêng; không mang header/menu/theme tối vào PDF.
Evidence m3-results.json, review-old-desktop/390.png, review-old.pdf và review-print-*.png.
## M4 — Events API và replay chỉ đọc

GET events kiểm owner, chỉ trả ACCEPTED theo id, kèm undone. Replay giữ dòng đã
undo để tái hiện quá trình thật rồi áp UNDO; RESET về trống. Không tự chấm hoặc gọi
API ghi. Restore replay rẽ nhánh trước create/resume/autostart, ẩn controls ghi,
timer được dọn pagehide; reduced-motion chỉ bấm từng bước. GROUP/pending không replay.
181Node/105JUnit/package xanh; Node test frames và source-only network/read ownership.
HTTP/Chrome9 ca: events35 chỉ3 ACCEPTED, other404; replay33 cũ báo không có nhật ký,
controls ghi ẩn,390 không tràn; pending35 chỉ thông báo; không network write/pageerror.
Replay đầy đủ trên phiên mới hoàn tất sẽ kiểm ở M5/M6, không lấy ca không nhật ký
làm bằng chứng đã phát lại đủ10 vật thể. Các VM test completion vẫn kiểm normal mode.

## M5 — Hoàn tác có kiểm chứng

182Node/106JUnit xanh. Undo tìm ACCEPTED ATTACH/DETACH gần nhất, chưa undo và sau RESET,
khóa phiên rồi gọi lại validateAttach/validateDetach; UNIQUE và version bảo vệ gửi đôi.
24 ca HTTP/SQL: đảo gắn/tháo/đổi hướng, reset barrier, một200/một409 khi đồng thời, quyền404,
CSRF403, COMPLETED SESSION_LOCKED, GROUP GROUP_MODE, cap300 REJECTED không đổi version.
Chrome5 ca: nút undo, Ctrl+Z từ radio; Ctrl+Z trong ô nhập không gọi API undo, không pageerror.

Phiên35 đã hoàn tất10/10 v2; hai request hoàn tất trả200 nhưng chỉ một transitioned và một
unchanged, SQL chỉ một COMPLETE. Phát lại đầy đủ27 ACCEPTED kể cả UNDO/RESET về đúng10 cặp
vật thể/điểm gắn/hướng cuối; không request ghi và hash phiên/placements/events không đổi.
Đã kiểm trang mới bốn phần,5 bước chuẩn,20 mục SVG/10 hàng chú giải; các trang cũ bằng đúng
chủ tài khoản. Script QA ban đầu dùng student cho phiên của builder nên404 đúng; sửa script
lấy owner từ SQL, không sửa quyền ứng dụng. Evidence m5-results, m5-browser-results,
final-mutations-results và final-live-results (49 kiểm đọc/replay/hồi quy).
PDF phiên mới4 trang đã render/xem mọi trang; thêm giữ caption với bảng và không bẻ chữ Hướng.
Replay bỏ lời hướng dẫn gắn/progress cũ gây hiểu nhầm; chỉ đổi chữ/ẩn vùng thuộc chế độ xem lại.

## M6 — Kết quả cuối và bàn giao

Baseline thực173Node/97JUnit → cuối182Node/106JUnit, đều0fail/0skip. Đã chạy
`mvn -q -f tomcat-app/pom.xml test`, `npm test`, `mvn … clean package` bằng JDK17.
Build riêng ngoài target của IDE; Maven property phải được quote trong PowerShell.
Một lệnh ban đầu thiếu quote làm PowerShell tách tham số, Maven chưa build; đã sửa
lệnh rồi clean package thành công, không đổi pom/test để che lỗi. Node chạy glob
hiện có trong npm script, không thay backend thành Node.

### Ma trận HTTP/SQL trên Tomcat9 + DB local thật

| Ca kiểm | Kết quả thực |
| --- | --- |
| ADMIN QA mới đọc robot/bước/thống kê | GET200 cả ba; đăng ký/nâng role chỉ tài khoản mới22 |
| Gắn/tháo đúng, nhật ký cùng commit |200; ACCEPTED và layout_version_after đúng thứ tự |
| Sai hướng động cơ |422 ORIENTATION_NOT_ALLOWED; REJECTED, placement/version giữ nguyên |
| Gửi lại đúng placement / tài nguyên không thuộc quyền |200 unchanged /404; không thêm nhật ký |
| Undo gắn mới |200; vật thể trở lại khay |
| Undo đổi hướng L298N180° |200; hướng trước0° được phục hồi |
| Undo tháo |200; đúng instance/slot/hướng được gắn lại |
| Reset rồi undo khi không còn thao tác |422 NOTHING_TO_UNDO; không vượt rào RESET |
| Hai request undo cùng version | Một200, một409 LAYOUT_CONFLICT; version tăng đúng một |
| UNIQUE undo_of_event_id | SQL không có tham chiếu undo trùng |
| GROUP17 / COMPLETED33,35 undo |422 GROUP_MODE /422 SESSION_LOCKED |
| USER khác undo / thiếu CSRF |404 /403; không ghi placement hoặc lịch sử |
| Flood305 lần sai loại |422; REJECTED dừng ở300, version không đổi |
| Hoàn tất khi còn thiếu |422 ASSEMBLY_INCOMPLETE |
| Gắn đủ phiên35 v2 | Report/SQL10/10, mười placement với hướng hợp lệ |
| Hai request hoàn tất đồng thời | Cả hai200; một transitioned, một unchanged; chỉ một COMPLETE |
| Review35 / phiếu35 |200 no-store; bốn phần,5 bước chuẩn,10 hàng chú giải, quiz model đúng |
| Review/events của người khác |404; khách review302 tài khoản, khách events401 |
| Review lúc35 chưa hoàn tất (M3) |200; chỉ thông báo/tiếp tục, không lộ phần hoàn thiện |
| GROUP16 đã hoàn tất | Review/phiếu200; ảnh/bước/quiz, không SVG/replay giả |
| Phiên30/31/33/34 trước Đợt9 | Review/phiếu200 theo đúng owner;10/10 và thông báo trước nhật ký |
| GET replay35 |27 ACCEPTED được phát lại; trạng thái cuối đúng SQL10/10,0 request ghi |
| Hash dữ liệu cũ |43/43 tập hàng có sẵn không đổi, gồm spec v1/v2 và phiên đã COMPLETED |

M1 có4 sự kiện đầu của phiên35. Cuối phiên35 có327 sự kiện:27ACCEPTED/300REJECTED.
Con số từ chối lớn do kiểm cap, không đại diện quá trình học bình thường; thống kê
trang chỉ đếm dòng đã ghi, không hứa đếm mọi request sau khi chạm giới hạn.

### Trình duyệt và bản in

Chrome headless với phiên đăng nhập thật: M0 kéo vật thể có di chuyển nhưng camera
không xoay, kéo vùng trống xoay camera;390px không tràn/nút gắn không bị che.
M5 kiểm nút Hoàn tác/Ctrl+Z và không can thiệp Ctrl+Z khi gõ. Review/replay tại
390/768/1440px đều scrollWidth≤innerWidth. Tiêu đề160 ký tự liền và chuỗi HTML
dùng DOM tạm, không lưu; không gây tràn hoặc chạy mã. Đây là fixture trình bày,
không phải phép thử ghi chuỗi độc hại vào DB. Escape thật được guard JSP/c:out kiểm.

Phát/Tạm dừng/Bước trước/Bước sau hoạt động, khung đầu trống và khung cuối đúng
mười vật thể/hướng. Network không POST/PUT/PATCH/DELETE; SQL hash phiên35/placements/
events trước-sau bằng nhau. Reduced-motion vô hiệu Phát tự động, vẫn bước thủ công.
Không pageerror hoặc asset404 trên các trang review/replay đã quan sát.
Replay bỏ hướng dẫn gắn và thanh tiến độ giả từ giao diện GROUP; không có panel bước.

PDF A4 Chrome: phiên33 cũ3 trang, phiên35 mới4 trang; đã render/xem mọi trang.
Ảnh/SVG/bảng/bước/lịch sử/phiếu/quiz giữ đủ, nền trắng chữ đen; menu/toolbars ẩn,
thead lặp, hàng ngắn không bị cắt. Caption đi cùng bảng, chữ Hướng không bẻ lẻ.
Không cam kết số trang hoặc phân trang giống nhau giữa trình duyệt.

Hồi quy GET200: /tasks, /account, /learning-profile, /wiring, /diagnosis, /robots,
quiz đúng model, cửa hàng và giỏ hàng. Browser xác nhận link phiên35 từ tài khoản
tới review, quiz đúng Robot tránh vật cản, cửa hàng render; Admin robot/bước/stats200.
Không chạy lại POST nộp nhiệm vụ/quiz/đơn hàng trong Đợt9; guard/rubric và dữ liệu
cũ giữ nguyên. Fixture giao diện chỉ thêm trang mới, không đổi hợp đồng form cũ.

### Findings và giới hạn

Đã sửa: góc khay mất khi xoay chưa chọn slot; UTF-8 fragment include do Jasper;
print box-sizing/caption/chữ Hướng; hướng dẫn/progress ghi dữ liệu cũ trong replay.
Rà nguồn transaction/owner/CSRF/escape/version, guard môn học và kiểm hành vi thật;
không dùng review độc lập/subagent trong đợt này. Không còn lỗi được tái hiện chưa sửa.

Chưa kiểm Firefox/Safari, cảm ứng vật lý, screen reader, toàn bộ zoom200%; không
tuyên bố chứng nhận accessibility. Không kiểm mọi form ghi của các chức năng cũ.
Không có tọa độ server cho GROUP nên chỉ ảnh/bước/phiếu/quiz; phiên cũ không được
bịa trình tự. Bản vẽ và hình khối giản lược không theo tỉ lệ kỹ thuật, không mô
phỏng điện/firmware/chiều trục động cơ thật. Không sửa spec v1/v2 hoặc rubric A/B.
Không ghi số slide chưa xác minh từ đối tượng Word nhúng trong pptx.

### Dữ liệu, runtime và Git

Giữ dữ liệu QA để người dùng quản lý: ADMIN mới22
`qa-phase9-admin-20261014@robotlab.test`; phiên35 của demo student, spec v2,
COMPLETED10/10;327 events. Chỉ thêm migration014/bảng assembly_events, không seed
nhật ký, không backfill hay UPDATE hàng cũ. Không ghi mật khẩu/cookie/DB config.

10/10 hash file được bảo vệ giữ nguyên (sáu WIP, AGENT_HANDOFF và ba file TAI).
AGENT_HANDOFF không được cập nhật vì prompt Đợt9 yêu cầu không đụng. Bằng chứng
không bí mật ở D:/Web-Pro/qa-evidence/phase9, ngoài Git: m0/m1/m3/m4/m5-results,
m5-browser-results, final-mutations-results, final-live-results49ca,
audit-results10ca+hash, final-smoke-results5ca, ảnh/PDF và test logs.
Script QA sai owner đã được sửa theo dữ liệu SQL; ứng dụng404 đúng, không nới quyền.

Đã dừng đúng process QA riêng, xóa CATALINA_BASE/config/log tạm;8081 đóng, không
dừng8080 của người dùng. Log runtime không có exception ứng dụng. Browser contexts
đã đóng; không lưu cookie/password. Build/evidence không bí mật giữ ngoài repo.
Commit local từng M0–M6, không push; xem git log để lấy hash bàn giao cuối.

## Đợt 9b — Thống kê lỗi học tập và nhãn bản vẽ, 14/10/2026

Baseline thực182Node/106JUnit; sau sửa183Node/109JUnit,0fail/0skip. Đã chạy
Maven test và clean package JDK17 trong build riêng; npm test dùng exploded WAR
cùng build. Test mới đã đỏ: summary nhận6 loại thay vì1 loại lỗi học tập; sau sửa xanh.
Ba JUnit mới kiểm lọc lỗi kỹ thuật/đếm sai hướng,40 nhãn chồng vị trí và hướng/lớp hình;
Node mới kiểm JSP dùng tọa độ/ghi chú/lớp nhãn từ getter Java, không tính ở client.
JspExpressionContractTest/JavaBeanRulesTest và guard Java120/JSP140 vẫn xanh.

`AssemblyReview.isLearningError(code)` là nguồn duy nhất của sáu mã thống kê:
WRONG_COMPONENT, SLOT_OCCUPIED, ORIENTATION_NOT_ALLOWED, PARENT_MISSING,
ALREADY_PLACED, HAS_DEPENDENTS. Không xóa hay sửa nhật ký, không thay DB/API/quyền.
LAYOUT_CONFLICT/NOTHING_TO_UNDO/SESSION_LOCKED/GROUP_MODE/VALIDATION_ERROR không
bị quy thành lỗi học tập. JUnit còn kiểm null/mã lạ không được nhận và summary rỗng
khi chỉ có lỗi kỹ thuật; toàn bộ sự kiện đầu vào vẫn giữ nguyên.

Bản vẽ: phía trước suy từ z của slot HC-SR04 so với khung trong spec, không hardcode
chiều âm. Spec v2 hiện có cảm biến ở z âm: hình trên đặt trước ở trên; hình ngang
đổi phép chiếu x,y cũ thành z,y để phía trước nằm bên phải. Có “▲ Phía trước”/mũi
tên phải và ghi chú ngay dưới hình; thiếu căn cứ cảm biến thì không bịa hướng.
Tọa độ vật thể vẫn lấy từ placement/spec đã lưu. Đây vẫn là hình minh họa giản lược.

Hình lớn vẽ trước theo diện tích hộp, hình nhỏ sau; STT và thứ tự bảng chú giải
giữ nguyên. Vẽ nhãn ở lớp riêng sau hình: tâm cách nhau tối thiểu44 đơn vị SVG;
nếu gần hơn, Java chọn vị trí ngoài hộp gần nhất trên lưới44 và nối về tâm vật thể.
Khung720×360 có vùng trống cho hướng/nhãn. JUnit kiểm40 vật thể đồng vị trí cả hai
phép chiếu (kể cả40 khung cùng loại), khoảng cách nhãn và bounds; test hình rỗng,
một vật thể, GROUP và bounds hình cũ giữ nguyên. CSS chỉ namespace assembly-review,
có nền số/đường nối và màu in; cache-busting tăng20261014.4.

### Kiểm thật trước/sau

| Ca | Kết quả |
| --- | --- |
| Phiên QA mới37 của demo student, v2 |201; chuẩn bị/lắp/hoàn tất10/10 bằng API hiện có |
| Hoàn tác khi trống |422 NOTHING_TO_UNDO, vẫn có REJECTED trong nhật ký |
| Hai tab cùng version, gắn hai bộ phận khác nhau | Một200, một409 LAYOUT_CONFLICT; vẫn ghi lý do truy vết |
| Hai lần gắn động cơ sai hướng |422 ORIENTATION_NOT_ALLOWED cả hai |
| Trang trước sửa | GET200, summary hiện cả lỗi tab/hoàn tác trống và sai hướng |
| Trang sau sửa | GET200, “Những lần cần sửa” chỉ còn “Sai hướng: 2 lần” |
| Hash nhật ký37 trước/sau sửa | Giống hệt, không cập nhật dữ liệu phiên đã COMPLETED |
| Hai SVG thật |20 nhãn, khoảng cách≥44, tọa độ trong khung, có đường nối và hai dấu trước |
| Chrome1440px/390px | Không tràn ngang, đã chụp/xem ảnh trước/sau;0pageerror/asset404 |
| PDF Chrome A4 |4 trang, render/xem mọi trang, ghi chú/nhãn/bảng và thống kê đúng |
| Bảo toàn |44 tập hàng có trước QA giữ nguyên hash;10 file được bảo vệ nguyên byte |

Bằng chứng ngoài Git: D:/Web-Pro/qa-evidence/phase9b, before-results.json và after-results.json
(10 kiểm dựng ca trước,12 kiểm sau), before/after-1440.png, before/after-390.png,
before/after-drawing-*.png, after.pdf, print-*.png, baseline/red/test/package logs,
protected-before.json và database-before.json (hash, không dữ liệu thô/bí mật).

Không tạo tài khoản hoặc chạy migration/seed; chỉ thêm phiên QA37 và dữ kiện của
chính phiên này qua luồng hiện có. Giữ spec v1/v2, phiên cũ và mọi dữ liệu của người
dùng khác. Không sửa Servlet/XxxDB, không đổi điểm, CSRF, version hay HTTP status.
File ôn tập cập nhật phép chiếu/dòng code và câu hỏi phân biệt lỗi học tập với lỗi kỹ thuật.
Chưa kiểm Firefox/Safari, cảm ứng vật lý hoặc screen reader trong9b.

Đã dừng đúng process QA, xóa CATALINA_BASE/config/log tạm,8081 đóng; không đụng8080.
Runtime không có exception ứng dụng; không lưu cookie/password. Một commit local
`fix(assembly): count only learning errors and clarify drawing labels`, không push.

### Bổ sung M6 Đợt 10 — tái sử dụng an toàn

`AssemblyDrawing.placeLabels` hết chỗ sẽ giữ nhãn tại tâm, không ném lỗi làm trang 500;
bảng chú giải vẫn đủ mọi vật thể. JUnit dựng 30 vật thể trong khung 160×160 xác nhận
hai hình có đủ 30 mục, tọa độ hình/nhãn nằm trong khung. Test cũ 40 vật thể tách nhãn
trong khung mặc định vẫn giữ nguyên và đạt. Khi khung quá chật, không cam kết nhãn
đều cách nhau 44 đơn vị; bảng chú giải là phương án đọc đầy đủ.

`AssemblyReview.isLearningError` dùng chung cho Xưởng, thêm MOUNT_INCOMPATIBLE,
MOUNT_OCCUPIED, HOST_MISSING, NOT_SUPPORTED. Test và hồ sơ HTTP thật xác nhận lỗi tab,
hoàn tác trống, khóa phiên và request sai không được tính là lỗi lắp ghép.
Không sửa dữ liệu phiên lắp mẫu. Bộ test tại M6:191 Node/124 JUnit, package Java17 đạt.
PDF hồ sơ Xưởng 3 trang đã render/xem, bằng chứng ngoài Git:qa-evidence/phase10.
