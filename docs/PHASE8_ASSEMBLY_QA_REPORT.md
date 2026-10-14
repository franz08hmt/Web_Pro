# Đợt 8 — Phòng lắp ráp tương tác có kiểm chứng

## M0 — Khảo sát ngày 12/10/2026

- Nhánh `integration/fullstack-v2`, baseline `ad2a4fc`, đồng bộ origin.
- Baseline thực chạy: Node 160/160, JUnit 85/85, Java 17, không fail/skip.
- Bảo toàn bảy file tracked sửa sẵn: account.js, LearningProfile.java,
  LearningProfileServlet.java, TaskServlet.java, ConnectionPool.java,
  TaskFormUtil.java, learning-profile.jsp; các tài liệu TAI và handoff ngoài commit.
- GROUP lưu theo component_id nên không xác thực được từng bánh/động cơ.
  DETAILED cần dữ liệu riêng, không chuyển đổi hoặc chấm lại phiên cũ.
- Người dùng xác nhận: động cơ và bánh là hai vật thể riêng; tháo cha khi còn
  con bị từ chối; robot chưa có spec giữ GROUP. Không sửa tên components.

## Kế hoạch và tiêu chí

1. M1: prototype riêng, raycaster và mặt phẳng kéo; kéo vật thể không xoay
   camera; chọn vật thể/điểm/hướng vẫn thao tác được bằng bàn phím.
2. M2: sáu JavaBean, unit tests trước luật; migration 013 và seed bất biến.
   Kiểm chuẩn bean, cú pháp, FK cùng phiên/spec, version và từng mã từ chối.
3. M3: AssemblyPlacementDB và nhánh API hiện có, khóa phiên trước đọc layout,
   kiểm owner/state/version trong transaction. Chỉ chạy DDL sau duyệt cụ thể.
4. M4: toàn bộ kit obstacle-avoider, UI chỉ dựng trạng thái server; lỗi mạng
   giữ chưa đồng bộ. GROUP giữ giao diện hiện tại. Không panel bước.
5. M5: check/report và complete nguyên tử, khóa sửa, confetti chuyển thật,
   phiếu JSP riêng nhánh DETAILED; giữ bằng chứng nhiệm vụ A/B hiện có.
6. M6: Tomcat QA riêng 8081, SQL/browser/concurrency/hồi quy và cleanup.

Tái sử dụng Three.js, hình học thủ tục, camera/fullscreen/drive/confetti,
HttpSession/CSRF/JsonUtil/ResponseUtil/ConnectionPool/DBUtil và shell JSP.
File chính mới: business AssemblySpec/Slot/Instance/Placement/Layout/CheckReport,
data AssemblyPlacementDB, util AssemblyPlacementJson, JS assembly-3d-placement,
CSS assembly-placement, migration và seed 013. Chỉ sửa các file lắp ráp liên quan.

Không mô phỏng điện/va chạm/firmware, không thêm điểm hoặc đổi rubric A/B.
Hướng Y0/Y90/Y180/Y270 là góc tương đối so với pose gốc của slot; pose gốc
lấy từ config hiện hành, giữ cả rz của động cơ/bánh và ry của hộp pin/cảm biến.
Không nhật ký events: phản hồi lỗi theo request và báo cáo đọc layout đủ phạm vi.

## Bằng chứng từng mốc

Chưa chạy migration/seed, chưa QA dữ liệu mới hoặc browser Đợt 8 tại M0.
Các mốc chỉ ghi đạt sau khi có kết quả thực chạy.

### M1 — Prototype

Chrome headless với callback fixture (không server/DB): chọn bằng radio và
bàn phím Space → Gắn; kéo vật thể → snap; kéo vật thể không xoay camera,
kéo vùng trống vẫn xoay. Không pageerror. Bằng chứng ngoài Git:
`D:/Web-Pro/qa-evidence/phase8/m1-results.json`, `m1-prototype.png`.
Đây chỉ là kiểm tương tác M1, chưa phải bằng chứng API/persistence.

### M2 — Dữ kiện và luật

JUnit mới đỏ do chưa có bean, sau triển khai đạt 7/7; tổng 92/92.
Node mới 5/5; tổng 165/165. Có kiểm guard trên bản sao chứa stream/dòng121.
Migration013 chưa chạy. DB đọc thực xác nhận migration001–012, kit có8 nhóm,
10 vật thể (2 động cơ,2 bánh). Seed10 slots theo config, hướng Y0 tương đối.
Guard hồ sơ Đợt5 bổ sung đúng migration013 vào whitelist các chức năng khác,
vẫn từ chối migration lạ; không nới bảo mật/luật hồ sơ.
Build riêng Java17 ngoài repo tránh IDE tự ghi class69(Java25) vào target.
M1 đã commit trước khi phát hiện guard whitelist đỏ; được sửa ở M2,
không sửa lịch sử commit. Không tuyên bố baseline mới xanh ở lần đỏ đó.

### M3 — API và lưu dữ liệu

Người dùng đã duyệt migration013 và seed. Đã chạy đúng DB trong `.env`;
10 slots/10 vật thể, chạy lại seed không đổi spec v1. Đối chiếu dữ liệu
37 bảng cũ không thay đổi; schema_migrations chỉ thêm bản ghi013.
82 kiểm tra Tomcat9/Java17/MySQL thật đã qua: ADMIN và hai USER đăng nhập,
owner/CSRF, từng mã từ chối, version cũ, retry, reset giữ instances,
reload, một bánh không tính hai, hai attach cùng version chỉ một ghi,
hai complete chỉ một chuyển trạng thái; SQL xác nhận10 placements.
Phiên QA mới #30 (builder), #31 (student); không tạo tài khoản mới.
Chi tiết không bí mật: `D:/Web-Pro/qa-evidence/phase8/live-results.json`.
JUnit bổ sung kiểm kit và complete/reset nằm trong bean: tổng93.
Node hiện166/166. Kiểm byte exploded hỗ trợ `ROBOTLAB_EXPLODED_DIR`
để đối chiếu bản build Java17 riêng; vẫn giữ assertion byte và UTF-8,
mặc định dùng target cũ. Không sửa/dừng Tomcat8080 của người dùng.
Assertion nguồn hoàn tất được cập nhật theo if/braces và nhánh GROUP;
guard mới kiểm bean validateChange, không đặt luật hoàn tất trong XxxDB.

### M4 — Giao diện toàn bộ kit

Chrome headless server thật: kéo động cơ được lưu, Space/Enter chọn-gắn,
Escape hủy cả khi focus radio, reload khôi phục, lỗi mạng giữ chưa đồng bộ
và khóa Hoàn tất, tải lại xóa trạng thái chưa đồng bộ. Có nút xoay90° và Q/E;
hướng/loại vẫn do JavaBean xác thực. Viewport390/768/1440 không tràn toàn trang.
Prototype kiểm kéo vật thể không xoay camera, kéo vùng trống vẫn xoay.
Touch là mô phỏng Chrome, chưa thử màn hình cảm ứng thật. 23 ca Chrome thật
(gồm complete và drive của M5) xanh, không pageerror/asset404. Bằng chứng:
`browser-results.json`, `detailed-390/768/1440.png`, `completed-desktop/mobile.png`.
Không đưa panel bước/checkbox bước vào3D; GROUP giữ cách tick nhóm.
api.js cache-busting được tăng tại mọi HTML dùng chung, không đổi các luồng đó.

### M5 — Hoàn tất và phiếu kết quả

Check/complete chỉ dùng layout server trong khóa phiên. Hai complete đồng thời
trả một transitioned:true và một unchanged:true, timestamp/version chỉ tăng một lần.
Confetti chỉ khi transitioned; reload không ăn mừng lại. Phiếu JSP có phiên bản,
10 vật thể/slot/hướng/ngày gắn và ba phần cơ khí/nối dây/chạy thử mô phỏng.
Không xác nhận nối dây hoặc firmware thật. GET phiếu owner và no-store.
Phiên GROUP cũ của builder vẫn mở được layout và phiếu; không chấm lại.
Nhiệm vụ mới #23 `[QA] Phase8 detailed assembly evidence ...` được tạo/công bố
qua form ADMIN, USER quiz→preview→confirm với session30. SQL xác nhận một
bài nộp tham chiếu30, assembly_points40.0, không đổi rubric hoặc TaskServlet.
Mẫu B được giữ bởi test hồi quy; chưa nộp bài B mới riêng trong QA này.
Fixture UI chỉ đổi hai binding tương đương ở assembly-receipt:
status != COMPLETED → getter completed; fn:length → count đã set từ danh sách.
Không đổi bất kỳ control/form contract nào, bỏ fn khỏi JSP theo kiến thức môn học.
JspExpressionContractTest đăng ký ba biến bean mới. Tổng167 Node/93 JUnit xanh.

### M6 — Findings đã xử lý và kiểm chứng cuối

- Trang chuẩn bị còn dùng reset GROUP thiếu version: nhánh DETAILED giờ xác nhận,
  gửi version và giữ kit; reset PREPARING/READY/IN_PROGRESS có luật bean và test.
  main.js chỉ thêm nhánh này; GROUP giữ request và quy trình bên ngoài3D.
- Touch bị cuộn trang làm hủy pointer: touch-action:none chỉ canvas DETAILED.
  Chạy lại CDP touchStart/move/end tại390px đã gắn và lưu khung thật (#32).
- Escape khi focus radio và hướng xem trước: hủy/reset về0°; Q/E và nút xoay
  được kiểm. Chọn hướng trước vật thể không để control và ý định lệch nhau.
- Connection có lỗi getAutoCommit vẫn trả pool; rollback giữ exception gốc.
  Response từ chối có code/message/instanceId/slotId, giữ error envelope API cũ.
- Gắn khi đang tách hình tự ghép về vị trí: đồng bộ trạng thái/nút và aria-pressed,
  tránh nút vẫn báo ghép dù mô hình đã ghép. Đã kiểm gắn rồi tháo trên phiên QA32.

38 kiểm bổ sung: reset lúc chuẩn bị và sẵn sàng, kit giữ nguyên, ID sai/số lớn,
field sai, thiếu/sai CSRF trên mọi route mới, version không đổi khi request sai,
xoay/preview không lưu, kéo cảm ứng mô phỏng và hash dữ liệu cũ. 12 smoke browser
cuối: Q/E tại radio, Escape, thoát fullscreen bằng nút app, reset hoàn tất khóa,
lái thử làm mô hình thật đổi trên canvas, phiếu3 phần/mobile, GROUP17 còn8 checkbox.
Thêm ba smoke: orientation không phải chuỗi bị từ chối; attach retry giống hệt
sau COMPLETED trả unchanged và không ghi (mọi thay đổi khác vẫn bị khóa).
Ca còn lại kiểm nút tách/ghép đồng bộ sau khi gắn vật thể.
82 API/SQL/JSP +23 Chrome +38 bổ sung +12 smoke =155 assertion thực chạy,
ngoài luồng nhiệm vụ mới23: chọn bằng chứng30 → preview → confirm → SQL40.0.

Baseline160 Node/85 JUnit → cuối168 Node/93 JUnit, không skip/fail/error.
`mvn -f tomcat-app/pom.xml test` và `clean package` chạy JDK17 với override
robotlab.buildDirectory ra ngoài repo:93/93, BUILD SUCCESS. POM cho phép override
directory, mặc định target giữ nguyên. Không clean target đang bị IDE ghi classJava25.
Node dùng ROBOTLAB_EXPLODED_DIR của WAR Java17 vừa build; guard vẫn so byte nguồn.
WAR cuối được deploy lại Tomcat9 riêng8081 và smoke12 ca trên đúng WAR đó.

Đối chiếu SHA256 các cột cũ của những dòng đã có trước migration (prefix theo
khóa tăng dần; QA chỉ INSERT ID mới) giữ nguyên trong cả38 bảng, kể cả users,
robot/kit, phiên GROUP, quiz/nhiệm vụ cũ và dữ liệu Nhi/Tuấn Anh.
Hồi quy runtime: GET các trang quiz/shop/thư viện/các catalog, nhiệm vụ/chẩn đoán,
nối dây/hỗ trợ/tổng kết/hồ sơ/đơn hàng/Admin; source/unit cũ đều còn và xanh.
Không thực hiện mua hàng hay nộp/chấm mẫuB mới trong vòng QA này.

Review do agent chính rà code cùng guard/SQL/concurrency/browser, không có reviewer
độc lập. Các findings trên đã sửa và chạy lại; không còn lỗi bắt buộc đã biết.
Chưa kiểm Firefox/Safari, màn hình cảm ứng thật, screen reader; spec archived
kiểm bằng bean/source (không UPDATE seed chính thức để dựng ca runtime).
Không làm mẫu thứ hai/cụm giá đỡ/hình tham khảo tùy chọn, không thêm tính năng ngoài phạm vi.

Dữ liệu QA giữ để người dùng quản lý: phiên30 và31 COMPLETED10/10; phiên32
IN_PROGRESS với khung đã gắn; nhiệm vụ23 mẫuA `[QA] Phase8 detailed assembly evidence …`,
một quiz vòng và một bài nộp tham chiếu30. Không có tài khoản mới.
Bằng chứng không bí mật ngoài Git ở `D:/Web-Pro/qa-evidence/phase8/`:
maven-test/package.txt, node-final.txt, migration/live/browser/extra/final-browser-results.json,
ảnh desktop/mobile, receipt-final-desktop/mobile.png. Không commit token/cookie/ảnh.

Đã dừng đúng process QA, kiểm cổng8081 không còn LISTEN và xóa CATALINA_BASE,
cấu hình/log tạm. Context trình duyệt đã đóng, không lưu cookie/mật khẩu ra file.
Log runtime QA cuối không có exception ứng dụng. Không dừng Tomcat8080 của người dùng.

## Đợt 8b — sửa nhỏ sau review, 13/10/2026

Baseline HEAD6d8a152, nhánh integration/fullstack-v2 ahead8. Đã kiểm account.js
không có diff nội dung trước khi sửa theo quyền riêng trong prompt. Bảo vệ bằng
SHA256 sáu file WIP còn lại và bốn file untracked; không sửa/stage chúng.

### Quyết định dữ liệu và tư thế

Không có migration mới, không sửa schema/migration013 hoặc seed v1. Seed v2 thêm
version_no2 PUBLISHED trong transaction, khóa robot, NOT EXISTS và cờ tạo mới.
Chạy hai lần trên DB local:10 slot,17 hàng hướng, lần hai hash toàn DB không đổi.
Các slot giữ code/label/parent/component/order/xyz/rx/rz của v1, chỉ bù ry theo
hướng đầu tiên. CHECK góc [-6.284,6.284] cho phép ry âm; không cần cộng2π.

| Slot | Hướng cuối cùng | Lý do/giới hạn |
| --- | --- | --- |
| chassis | Y0 | Nền cố định |
| motor-left | Y90 | Mã hướng trái; ry bù90° để giữ tư thế v1 |
| motor-right | Y270 | Mã hướng phải; ry bù270° để giữ tư thế v1 |
| wheel-left, wheel-right | Y0,Y180 | Bánh đối xứng |
| uno | Y0 | Giữ tư thế USB của v1 |
| driver | Y0,Y180 | Hai phía hàng ốc |
| battery | Y90,Y270 | Hộp pin ngang đuôi xe |
| caster | Y0,Y90,Y180,Y270 | Bánh tự do |
| sensor | Y180 | Giữ hai mắt cảm biến theo tư thế trước v1 |

Góc lưu DECIMAL(8,4) theo schema013, nên tư thế bù có sai số lượng tử dưới0,00005
radian so với v1; preview/snap/reload khớp chính xác slot.ry + góc đã chọn.
Không tăng độ chính xác schema hoặc sửa bản công bố để loại sai số không nhìn thấy.

### Giới hạn mô hình

Y90/Y270 là quy ước hướng lắp đối xứng trái/phải của mô hình 3D giản lược;
hình khối động cơ không mô phỏng chính xác chiều trục. Trong hình khối v1/v2,
hai trục đều gần−X theo phép kiểm vector trong Chrome. Không dùng hình này
để xác nhận chiều lắp động cơ thật hoặc kết luận robot thật vận hành an toàn.

Người dùng đã chốt giữ tư thế hình ảnh giống v1, không sửa dữ liệu v2 đã PUBLISHED
và không tạo v3. Chỉ sửa comment/lời giải thích cho đúng giới hạn. Ảnh đối chiếu:
`qa-evidence/phase8b/v2-completed-pose.png` (đường dẫn thực ngoài repo:
`D:/Web-Pro/qa-evidence/phase8b/v2-completed-pose.png`); số liệu ở pose-results.json.
Thư mục qa-evidence không được stage hoặc commit. Finding đã được chốt theo
quyết định sản phẩm này; M6 được tiếp tục, không thay đổi tư thế hoặc luật chấm.
Sau quyết định, chạy lại Node172/172 và JUnit97/97 JDK17, không fail/skip.
Đối chiếu seed sau khi bỏ comment: SQL giữ nguyên; không chạy lại ghi DB.
Hash kiểm lại dữ liệu cũ43 bảng và10 file được bảo vệ đều giữ nguyên.

### Lỗi và cách sửa

- M1: Y0 mặc định không còn đúng cho mọi slot; preview dùng góc tuyệt đối, không xoay cộng dồn.
- M2: messageFor có tên vật thể/điểm/cha/con từ chính layout kiểm trong transaction.
  Exception nhỏ ở util mang dữ kiện bị từ chối; Servlet quote, JS textContent.
  NOT_FOUND/version/khóa/GROUP giữ câu chung; thiếu label dùng câu dự phòng.
- M3: tái hiện WAR cũ: đổi hướng với ID số trả422 SESSION_LOCKED, ID chuỗi trả422
  VALIDATION_ERROR vì parser chạy trước luật khóa. Đã chuẩn hóa lỗi parse khi layout
  khóa; phiên còn sửa được vẫn kiểm định dạng. Retry đúng hệt vẫn unchanged và không ghi.
- M4: COUNT placement/slot bắt buộc trong SELECT phiên đã lọc user_id; thêm getter/setter
  và hai field JSON. DETAILED hiện X/N, GROUP giữ phần trăm chuẩn bị. Ẩn dòng bước cũ
  riêng ở DETAILED; cache-busting cập nhật mọi nơi nạp account/personalization.
- M5: phiếu bỏ tên bảng/enum, giữ giải thích server và giới hạn mô phỏng; tài liệu
  API/ERD/kiến trúc/demo/vấn đáp cập nhật. Không đổi field/form/route hay rubric.

### Bằng chứng thực chạy

| Ca | Kết quả | HTTP |
| --- | --- | --- |
| Tạo phiên mới33, spec v2,0/10 | Đạt | 201/200 |
| Khung Y0, chi tiết/danh sách1/10 | Đạt | 200 |
| Động cơ trái Y0, thông báo có tên | Đạt, ORIENTATION_NOT_ALLOWED | 422 |
| Động cơ trái Y90 bằng E/nút | Đạt, preview và snap cùng góc | 200 |
| Bánh tại Arduino, thông báo nêu hai tên | Đạt, WRONG_COMPONENT | 422 |
| Cảm biến Y0/Y180 | Từ chối/được lưu qua radio | 422/200 |
| Thiếu cha, chiếm chỗ, đã gắn, tháo cha | Đạt, lời giải thích có tên | 422 |
| Mọi hướng trong slot nhiều hướng | Được lưu trên phiên QA chưa hoàn tất | 200 |
| Hướng Y45/null trên phiên đang sửa | VALIDATION_ERROR | 422 |
| CSRF thiếu, USER khác GET/POST, version cũ | Đạt | 403/404/409 |
| Kiểm10/10 rồi hai complete đồng thời | Một transitioned, một unchanged | 200 |
| Đổi hướng/tháo/reset sau hoàn tất, kể cả field lỗi | SESSION_LOCKED | 422 |
| Retry placement đúng hệt sau hoàn tất | Không đổi version/dữ liệu | 200 |
| Phiếu33:10/10, hướng90/180/270°, chữ mới | Đạt, no-store | 200 |
| Phiếu30/31 và layout v1:10/10 | Đạt, không chấm lại | 200 |
| Account/index:1/10; GROUP tài khoản practice giữ% | Đạt trên Chrome thật | 200 |
| GET tasks/account/learning-profile/wiring/diagnosis/robots | Đạt | 200 |

Baseline168 Node/93 JUnit →172 Node/97 JUnit,0 fail/error/skip.
JUnit mới có hướng đơn/nhiều, sáu mã lỗi có tên, không lộ label cho lỗi chung,
hai count JSON và phần trăm GROUP không đổi. Node test-first đã đỏ ở M1/M3/M4;
JUnit M2 đỏ do chưa có overload. Guard Java120/JSP140/course/CSRF/owner giữ nguyên.
Maven test và clean package JDK17 xanh, dùng robotlab.buildDirectory ngoài repo
để không clean target mà IntelliJ đang dùng. Quiet test cuối97/97 qua surefire XML.
Lệnh node --test tests/server trên Node24.13.0/Windows báo MODULE_NOT_FOUND cho thư mục;
đã chạy đủ test bằng glob chuẩn của npm: node --test tests/server/**/*.test.cjs,
172/172, không skip hay nới guard. Lỗi CLI được giữ riêng trong evidence.

Chrome headless thật18 kiểm tra: Q/E/nút/radio, từ chối/snap/reload, đo góc từ scene,
account/index/GROUP,390px không tràn, không pageerror/asset404. API/SQL vòng đầu
24 ca và vòng cuối68 ca ghi riêng từng ca: tổng110 kiểm tra HTTP/SQL/Chrome đã qua.
Script QA từng nhầm vị trí mã CSRF ở envelope,
và thử stale trên request idempotent. Đã sửa script để kiểm đúng envelope và
stale trên ý định khác; không sửa ứng dụng hoặc nới test để khớp các nhầm lẫn đó.
Không tính assertion version complete viết quá rộng ở bản script đầu thành bằng chứng.
Sau QA, hash các dòng đã có trước seed trong43 bảng giữ nguyên; spec v1 và toàn bộ
phiên/placements30/31 có hash riêng giữ nguyên. Sáu WIP+bốn untracked giữ nguyên byte.

Dữ liệu mới: spec v2,10 slot/18 hướng chính thức; phiên QA33 của demo student COMPLETED10/10.
Không tạo tài khoản, nhiệm vụ, quiz hay đơn hàng. Không sửa phiên32 hoặc dữ liệu cũ.
Runtime QA8081 đã dừng đúng PID, cổng không LISTEN, CATALINA_BASE/config/log tạm đã xóa;
context trình duyệt đóng, không lưu cookie/mật khẩu. Log không có exception ứng dụng.
Evidence ngoài Git: D:/Web-Pro/qa-evidence/phase8b/ gồm log test, JSON hash/HTTP,
account-1-of-10.png, v2-desktop/390.png, receipt-v2-desktop/390.png và ảnh tư thế.
Chưa kiểm Firefox/Safari, cảm ứng vật lý/screen reader; hồi quy cũ chỉ GET và test,
không tạo bài nộp mới hay mua hàng. Giới hạn tư thế động cơ đã được người dùng chốt ở trên.

| Mốc | Commit |
| --- | --- |
| M1 | 7047f1a fix(assembly): add immutable v2 spec with meaningful orientations |
| M2 | d3f88a3 fix(assembly): explain rejected placements with local part labels |
| M3 | 13e4e9b fix(assembly): report locked layout mutations consistently |
| M4 | 2a80c82 fix(assembly): show verified slot counts in session overviews |
| M5 | c1e65d9 fix(assembly): clarify receipt copy and document v2 orientation flow |
| M6 | Hoàn tất; fix(assembly): finalize v2 QA and model limits (hash xem git log) |

### Đợt 8b-fix — giữ mã lỗi GROUP, 14/10/2026

Catch chỉ đổi lỗi thành SESSION_LOCKED khi layout DETAILED không còn sửa được;
không đổi NOT_FOUND/GROUP_MODE. Route placement kiểm GROUP sau khi kiểm chủ phiên,
trước khi đọc field thao tác. AssemblyPlacementDB dùng import AssemblyPlacementException.

| Ca HTTP thật trên Tomcat9/Java17/MySQL | Kết quả |
| --- | --- |
| GROUP17 của practice: POST placements / DELETE placements | 422 GROUP_MODE; câu “Phiên này dùng cách lắp theo nhóm linh kiện, không dùng điểm gắn riêng.” |
| DETAILED33 COMPLETED: POST với Y45 / DELETE placement | 422 SESSION_LOCKED |
| DETAILED32 IN_PROGRESS: POST với Y45 | 422 VALIDATION_ERROR |
| Phiên khác chủ: POST / DELETE placement | 404 NOT_FOUND |

Test Node mới đỏ đúng do thiếu điều kiện DETAILED, sau sửa xanh. npm test173/173,
mvn test97/97 và clean package JDK17 BUILD SUCCESS, không fail/error/skip.
Giữ build directory riêng và guard byte đối chiếu WAR mới, không clean target của IDE.
Không tách hàm static vì chỉ thêm điều kiện trong catch; có source guard và HTTP thật.
Ba USER demo,9 request thao tác đều bị từ chối, kể cả GROUP có field thiếu/sai.
Hash toàn bộ dữ liệu sáu bảng lắp ráp trước/sau giống nhau; không tạo/sửa phiên,
spec hoặc tài khoản. Các file WIP/untracked được bảo vệ giữ nguyên byte.
Tomcat QA8081 đã dừng đúng PID, cổng đóng, cấu hình/log tạm dọn và browser context đóng.
Bằng chứng ngoài Git: D:/Web-Pro/qa-evidence/phase8b-fix/ (test-red, test/build logs,
http-results.json và hash file được bảo vệ). Không stage qa-evidence hoặc bí mật.
