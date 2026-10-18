# Mô hình đúng phiên ngay trên trang hoàn thiện — 18/10/2026

## Phạm vi và bản nguồn

Nhánh `integration/fullstack-v2`, đầu lượt `ccf06ee` (source trước sửa `7af2d5f`).
Không thay schema, API cũ, spec công bố, luật lắp hoặc dữ liệu lịch sử.
Vòng QA ban đầu không triển khai/restart 8080/8081; cả hai cổng không lắng nghe
lúc khảo sát. Lần triển khai 8080 được duyệt sau đó ghi ở phụ lục cuối báo cáo.

Trước sửa, `/assembly-review` có ảnh catalog, SVG và link replay nhưng chưa có
canvas kết quả. Bốn test Node mới thất bại trước khi thêm viewer; log giữ ngoài Git.
Sau sửa, phần Mẫu hoàn chỉnh có mô hình cuối đã lưu, mã phiên/spec, điều khiển
camera; ảnh catalog ghi rõ **Ảnh tham khảo**. SVG/nhật ký/phiếu/quiz/replay vẫn giữ.

## Luồng Model 2 và nguồn dữ kiện

1. `AssemblyReviewPageServlet` lấy user từ session, đọc phiên theo `(id,user_id)`,
   trả 404 khi không phải chủ và giữ `Cache-Control: no-store`.
2. `AssemblyPlacementDB.selectLayout` đọc `spec_id` đã lưu của phiên cùng instances/
   placements. Servlet dùng lại đúng một lần đọc layout, kể cả link tra cứu lỗi.
3. `AssemblyReview.canDisplayLayout` kiểm phiên hoàn tất, đúng session/spec và bố
   cục có đủ dữ kiện. Không cập nhật trạng thái hoặc sửa bố cục khi xem.
4. `AssemblyPlacementJson.reviewLayout` dùng serializer hiện có, bỏ metadata sửa/
   báo cáo chấm của API. Request attribute đi qua `c:out` trong `<div hidden>`;
   trình duyệt đọc `textContent` rồi `JSON.parse`, không chèn JSON vào mã JavaScript.
5. `assembly-result-viewer.js` dùng Three.js cục bộ và `createAssemblyPart`. Mỗi
   placement tạo một object riêng theo instanceId; position là tọa độ tuyệt đối
   của slot, rotation là `(rx, ry + góc đã lưu, rz)`. Không cộng lại biến đổi cha.

Viewer không fetch API, không đọc localStorage, không đăng ký thao tác gắn/tháo/
undo/reset/complete. Chỉ camera thay đổi. Render theo nhu cầu; dispose listener,
ResizeObserver, geometry, material và renderer khi rời trang/khởi tạo lại.
`pageshow` khôi phục viewer khi trình duyệt quay lại bằng bfcache.

## Build và kiểm tự động

| Bản | Node | JUnit | Build |
|---|---:|---:|---|
| Baseline archive `ccf06ee` | 253 đạt, 0 fail/skip | 159 đạt, 0 fail/skip | clean package đạt |
| Source `92f7954` | 259 đạt, 0 fail/skip | 163 đạt, 0 fail/skip | test và clean package đạt |

JDK Eclipse Adoptium 17.0.19.10, Maven 3.9.15, Tomcat 9.0.120, Node 24.13.0.
Baseline chạy Node trước package có 1 skip artifact chưa dựng; đã package và chạy
lại đầy đủ 253/253. Không sửa test/skip để né lỗi. Snapshot source sạch lấy từ Git,
không mang sáu WIP vào build, không đụng target IDE.

Node mới kiểm pose tuyệt đối, orientation, pinned spec v1/v2, instance trùng loại,
không tự điền linh kiện trong khay, dữ kiện hỏng, lifecycle/dispose/bfcache,
bốn servo riêng, payload qua c:out và không có API ghi trong renderer.
JUnit mới kiểm điều kiện hiển thị, nhầm phiên/spec, GROUP/chưa hoàn tất/null,
tham chiếu hỏng/góc không hợp lệ/NaN/trùng, serializer và metadata API cũ.

## Trình duyệt thật trên QA riêng

Runtime tạm `127.0.0.1:8082/`, DB clone sẵn có
`robot_lab_assembly_entry_20261017`, login DML giới hạn DB này.
Không seed/tạo tài khoản/tạo phiên/hoàn tất thêm dữ liệu trong lượt này.
Dùng năm tài khoản QA đã có, đăng nhập xong mới theo dõi chặn request ghi.

| Mẫu | Phiên QA | Spec | Vật thể thực tế | So với phòng lắp, reload, camera, viewport |
|---|---:|---|---:|---|
| Tránh vật cản | 92 | id 2 / v2 | 10 | Đạt |
| Dò đường | 93 | id 7 / v1 | 11 | Đạt |
| Cánh tay mini | 94 | id 8 / v1 | 8, gồm 4 servo | Đạt |
| Dò line + tránh vật cản | 95 | id 9 / v1 | 12 | Đạt |
| Quét hướng tránh vật cản | 96 | id 10 / v1 | 12 | Đạt |

Spec ID/version đối chiếu từ payload của chính phiên và GET layout, không suy từ
robotId. Năm canvas có geometry/triangle thật; không phải ảnh hoặc canvas trống.
Đối chiếu toàn bộ instanceId, slotId, orientation, vị trí và Euler rotation giữa
payload, viewer và root của phòng lắp. Vị trí/góc không đổi sau xoay/zoom/căn giữa.

Vòng kiểm WAR cuối có **74 kiểm browser/HTTP đạt**, thêm health QA 200.
Không cộng vòng kiểm artifact trung gian thành số ca mới. Các nhóm kiểm:

| Ca | Kết quả |
|---|---|
| GET review chủ phiên / no-store / payload escape | 200; đạt cả 5 mẫu |
| USER khác mở phiên 93 khi đang là chủ phiên 92 | 404, không có payload |
| Khách mở review | 302 về đăng nhập |
| ID 0, âm, chữ, quá dài, Long.MAX_VALUE, overflow | 404, không 500 |
| Xoay chuột; Enter/Space trên nút; mũi tên/Home trên canvas | Camera đổi, pose vật thể giữ nguyên |
| 390/768/1440 px | 15 ca không tràn ngang, mô hình nằm trong khung |
| Reload; khởi tạo 3 lần; không tự animate | Đúng pose, chỉ 1 renderer còn hoạt động |
| Context mất; thiếu Three; WebGL không có; JSON lỗi | Có fallback, không exception JS chưa xử lý |
| JavaScript tắt | SVG, nhật ký, phiếu vẫn đọc được |
| Fixture nhãn chứa thẻ HTML/quote/ampersand | Còn là dữ liệu JSON, không thực thi script |
| In review / hồ sơ thiết kế | Canvas/công cụ ẩn, SVG còn; PDF lưu ngoài repo |
| Replay / phòng lắp completed / Xưởng design 11 / phiếu | Mở được, không phát sinh request ghi |
| Dashboard / tasks / tổng kết / quiz đúng model | Smoke GET 200, không lỗi JS |
| JS/CSS/parts HTTP so byte với source | Khớp; viewer `v=20261018.2`, review JS/CSS `v=20261018.1` |

## Dữ liệu, bằng chứng và giới hạn

Bằng chứng: `D:/Web-Pro/qa-evidence/assembly-review-3d-20261018/`.
`results.json` giữ dữ kiện từng model/pose/network/snapshot; `results-first-build.json`
giữ vòng trung gian. Có ảnh năm mẫu `*-1440.png`, `*-390.png`, `review-print.pdf`
và `design-sheet-regression.pdf`. Ảnh nằm ngoài Git.

Hash toàn bộ dòng/bảng trước/sau trên DB clone QA, `robot_lab_content_test` và
`robot_lab_final_demo_20261016` không đổi. Mở review/room completed/replay chỉ đọc;
không có request POST/PUT/PATCH/DELETE nghiệp vụ sau đăng nhập. Không reset mật khẩu,
không thay giỏ/kho/điểm/sự kiện/version/completed_at. Mười file bảo vệ giữ SHA256.

Số bảng tương ứng 55/55/54; network vòng cuối có 640 GET, 0 request ghi nghiệp vụ.
179 class production có major61 (Java17). Sáu file nguồn bảo vệ trong archive
khớp commit sau chuẩn hóa CRLF/LF của Git `core.autocrlf=true`, không lấy WIP.
Runtime QA8082 đã dừng đúng process do lượt này tạo, xác minh argfile trước khi
dừng; conf/log/work/temp/argfile đã xóa. 8080/8081 giữ nguyên trạng thái không chạy.
Manifest độc lập: `ACCEPTANCE.json`. Runner đã sửa hai giả định kiểm thử: c:out
dùng entity số cho dấu quote; đường dẫn argfile cần chuẩn hóa dấu phân cách ở cả
hai phía trước khi xác minh process. Không coi đây là lỗi ứng dụng hoặc bỏ guard.

Giới hạn phân biệt rõ:
- Hai phiên cùng robot/spec v1-v2 được kiểm bằng fixture Node/JUnit; không giả
  làm đã đăng nhập chủ các phiên lịch sử 30/31 trong DB thật.
- GROUP chưa hoàn tất được mở thật và giữ link tiếp tục; nhánh GROUP COMPLETED
  và layout null/hỏng được kiểm ở bean/hợp đồng JSP. Chưa kiểm live nhánh này do
  không có credential chủ phiên cũ; không sửa mật khẩu/dữ liệu để tạo ca.
- Lỗi JSON/nhãn HTML/thiếu asset/WebGL là fixture trình duyệt; không phá dữ liệu DB.
- Không chạy lại nghiệp vụ ghi (lắp/nộp/chấm/checkout) trong đợt viewer chỉ đọc.
- Chưa nghiệm thu runtime người dùng 8080/8081 với WAR mới. Không gọi QA đạt là
  đã triển khai. Không khẳng định toàn repo đúng mọi nội dung slide.

## Artifact và gói triển khai chờ duyệt

Source ứng dụng `92f7954` (hai commit source `5044c28`, `92f7954`). WAR sạch:
`D:/Web-Pro/qa-evidence/assembly-review-3d-20261018/accepted-source/tomcat-app/target/robot-assembly-lab.war`.

SHA256: `610440ea6656890768d90ee4f3838a7d4b5ba5ef3d0f85b8f798a0bbdbc1e386`.

Đích đề nghị: IntelliJ/Tomcat `localhost:8080`, context `/`, giữ nguyên kết nối
JNDI hiện tại; 8081 là môi trường demo riêng, không đổi cùng lúc.
Trước triển khai phải xác minh lại runtime/docBase/PID vì hiện không có listener.
Backup artifact/exploded và descriptor thực tế ngoài repo, lưu hash, không sửa WIP.
Dùng WAR sạch này theo External Source hoặc quy trình artifact sạch đã duyệt;
không build cây WIP vào target IDE. Không cần SQL, migration, seed hay chuyển DB.

Chỉ sau khi được duyệt mới phối hợp Stop/Run/redeploy đúng runtime với người dùng.
Sau Run: kiểm byte HTTP JS/CSS, cache version, owner/404/no-store, review năm phiên
đã có và xoay/zoom/reload không ghi. Nếu lỗi, dừng đúng runtime được phép và chọn lại
artifact/descriptor đã backup; không restore DB vì thay đổi này không ghi DB.
Rollback cũng phải phối hợp nếu người dùng đang chạy IDE. Không sửa IDE/global.

Trạng thái tại commit `087aeab`: **code và QA viewer đạt; triển khai còn chờ duyệt**.
Các kiểm live lịch sử nêu trên còn giới hạn, không đánh dấu “đạt toàn bộ”. Chưa push.

## Git bàn giao

- `743e940 docs(assembly): plan session-specific inline result viewer`
- `5044c28 feat(assembly): show the saved session model in completed review`
- `92f7954 fix(assembly): preserve readable results when the 3D view is unavailable`
- Commit tài liệu nghiệm thu riêng sau ba mốc trên; không thay source của WAR.

Sau khi commit tài liệu, phần chưa commit chỉ còn đúng WIP/untracked đầu lượt:

```text
 M tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/LearningProfile.java
 M tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/LearningProfileServlet.java
 M tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java
 M tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ConnectionPool.java
 M tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/TaskFormUtil.java
 M tomcat-app/src/main/webapp/WEB-INF/views/learning-profile.jsp
?? AGENT_HANDOFF.md
?? docs/TAI.md
?? docs/TAI_.docx
?? docs/TAI_.pdf
```

## Triển khai 8080 được duyệt — 18/10/2026

Người dùng đã duyệt phối hợp triển khai riêng lên 8080. Không thay source ứng
dụng, không build lại từ WIP; dùng nguyên WAR `92f7954` và checksum đã kiểm ở trên.
259 Node/163 JUnit là kết quả của đúng artifact này ở vòng build trước; lượt
triển khai không chạy lại suite để tạo số kiểm mới, chỉ kiểm runtime/HTTP/browser.

| Mục | Kết quả vừa xác minh |
|---|---|
| URL/context | `http://localhost:8080/`, context `/`, health 200 |
| Runtime | JDK17, Tomcat9, CATALINA_BASE cấu hình IntelliJ hiện có; PID 20012 lúc nghiệm thu |
| JNDI/DB | Giữ nguyên ROOT.xml và DB `robot_lab_content_test`; kiểm URL JNDI hiệu lực và property JVM |
| Artifact | 351 file khớp candidate sạch; WAR SHA256 `610440ea6656890768d90ee4f3838a7d4b5ba5ef3d0f85b8f798a0bbdbc1e386` |
| Backup | 350 file artifact trước đối chiếu hash; WAR trước SHA256 `fba465cfed2d0c67874714a5ae30a1f57137a209c8e6ef3323d765b898c30cde` |
| Cấu hình | ROOT.xml/server.xml giữ nguyên byte; không sửa IDE/global/.env hoặc DB connection |
| Tài nguyên HTTP | JS viewer/review/parts và CSS review khớp byte candidate; trang JSP mới hiện đúng khung |
| Quyền | Chủ phiên 200/no-store; đổi sang phiên 30 của người khác 404, không payload; khách 302 đăng nhập; overflow 404 |
| Không ghi khi xem | 150 GET sau đăng nhập, 0 request ghi nghiệp vụ, 0 lỗi JS chưa xử lý |
| Dữ liệu/WIP | Toàn bộ 55 bảng DB làm việc và 54 bảng DB demo cùng 10 file bảo vệ giữ hash |
| 8081 | Không có listener trước/sau; không khởi động, dừng hay đổi artifact/config |

| Mẫu trên 8080 | Phiên đã có | Spec thực tế | Vật thể | Camera/reload/390–768–1440 |
|---|---:|---|---:|---|
| Tránh vật cản | 44 | id2/v2 | 10 | Đạt |
| Dò đường | 45 | id3/v1 | 11 | Đạt |
| Cánh tay mini | 46 | id4/v1 | 8, gồm 4 servo riêng | Đạt |
| Dò line + tránh vật cản | 47 | id5/v1 | 12 | Đạt |
| Quét hướng tránh vật cản | 48 | id6/v1 | 12 | Đạt |

Mỗi object được đối chiếu instanceId/slotId/orientation và pose với layout server
của đúng phiên. Canvas có triangle thật, không phải ảnh thay thế. Xoay chuột,
Enter/Space trên nút zoom, căn giữa, reload và phiếu đều hoạt động; không tạo phiên.
Không chạy lại seed, không sửa hoặc hoàn tất thêm dữ liệu để kiểm.

Bằng chứng mới, tách khỏi QA8082:
`D:/Web-Pro/qa-evidence/assembly-review-3d-deploy-8080-20261018/`.
`ACCEPTANCE.json`/`results.json` có thông số, hash và network; ảnh năm mẫu desktop/
mobile là `*-1440.png`, `*-390.png`. Backup artifact/descriptor và argfile nằm
trong `private/` giới hạn ACL, ngoài Git; cache JSP cũ cũng được giữ để quay lui.
Không xóa backup trước khi người dùng chốt. Không restore DB khi quay lui artifact.

**Cách đang chạy:** khởi động bằng argfile từ cấu hình runtime IntelliJ đã xác
minh, không bấm nút Run của IDE. 8080 được giữ chạy sau bàn giao. Khi muốn dùng
Run IntelliJ, dừng đúng runtime hiện tại trước để tránh hai process tranh cổng:

```powershell
& 'D:\Web-Pro\qa-evidence\assembly-review-3d-deploy-8080-20261018\run.ps1' -Operation status
& 'D:\Web-Pro\qa-evidence\assembly-review-3d-deploy-8080-20261018\run.ps1' -Operation stop
```

Helper xác minh PID/executable/creation time/argfile/chủ cổng shutdown; không kill
process lạ. `-Operation start` chỉ dùng khi runtime này đã dừng và checksum/config
còn đúng. Không chạy Start đồng thời Run IntelliJ. Không dùng helper ngày17 với
checksum cũ. Hướng dẫn backup/quay lui chi tiết ở `OPERATIONS.md` cùng thư mục.
Run IDE có thể build từ cây WIP nên phải kiểm lại artifact/HTTP sau Run; lượt này
không tuyên bố đã kiểm thao tác bấm Run IDE hoặc sửa cấu hình IDE.

**Trạng thái hiện tại: đã triển khai và kiểm mô hình kết quả năm mẫu trên
localhost:8080.** Các giới hạn lịch sử GROUP/spec cũ ở vòng QA vẫn được giữ nguyên,
không biến thành tuyên bố đã kiểm live. Không push; Git chỉ còn WIP/untracked cũ.

## Xác minh sau khi người dùng Run IntelliJ — 18/10/2026

Runtime helper PID20012 đã dừng an toàn để giải phóng 8080 và JMX1099. Người dùng
sau đó bấm Run thành công; PID32144 giữ cả hai cổng, không dùng argfile helper.
Kiểm JVM/JNDI: Java17.0.19, đúng CATALINA_BASE/docBase trên và DB làm việc cũ.
179 class production đều major61, không có class Java25 trong artifact đang chạy.

Năm review44–48 hiện canvas và10/11/8/12/12 placements của đúng phiên, no-store;
không lỗi JS/request ghi nghiệp vụ sau đăng nhập. Health200. JS/CSS viewer khớp
nội dung candidate khi chuẩn hóa CRLF/LF; byte khác do kiểu xuống dòng.

**Phân biệt artifact:** file WAR vẫn giữ checksum sạch nhưng thư mục exploded
đã được IntelliJ build lại. Đối chiếu `WEB-INF/views/learning-profile.jsp` cho thấy
khớp WIP hiện tại, khác bản sạch; nhiều class cũng khác byte. Vì vậy không gọi
runtime Run IDE này là WAR sạch đã nghiệm thu, dù viewer chạy đúng.
Run configuration đang dùng `robot-assembly-lab-tomcat:war exploded`, context `/`,
Before launch bật BuildArtifacts cho chính artifact này. Không sửa cấu hình đó
trong lượt xác minh; muốn pin External Source WAR sạch cần người dùng duyệt riêng.
Mười file bảo vệ vẫn giữ hash. Bằng chứng: `INTELLIJ_RUN_VERIFICATION.json` ở thư
mục triển khai ngoài repo. Không dùng helper Stop cũ để can thiệp PID của IDE.

## Chốt Run riêng “Tomcat Final WAR” — 18/10/2026

Người dùng duyệt chọn cách chạy phù hợp cho final. Đã tạo cấu hình cục bộ
`.idea/runConfigurations/Tomcat_Final_WAR.xml`, Git bỏ qua; không commit XML IDE.
Giữ nguyên Run phát triển `Tomcat 9.0.120`, server/JDK17 và năm DB properties.
Cấu hình Final dùng External Source là WAR `92f7954`, context `/`, Before launch
không Make/BuildArtifacts. Runtime base riêng; thêm logging riêng để không in
VM arguments chứa bí mật. Workspace/config cũ được backup private trước khi tạo.

Sau khi người dùng Stop Run cũ và Run đúng Final, kiểm thực tế:

| Mục | Kết quả |
|---|---|
| URL/PID lúc kiểm | `http://localhost:8080/`, PID39896; 1099 cùng process của IDE |
| Runtime | Java17.0.19; base riêng `e2c73c89-cee7-4367-a318-e5ff279fbe23` |
| Descriptor | ROOT.xml trỏ thẳng WAR sạch đã chốt, không trỏ target working tree |
| WAR | SHA256 `610440ea6656890768d90ee4f3838a7d4b5ba5ef3d0f85b8f798a0bbdbc1e386` |
| Artifact Tomcat giải nén | 351 file khớp candidate từng byte; 179 class đều major61 |
| WIP không vào bản phục vụ | Learning-profile JSP và toàn bộ class/resource khớp bản sạch, khác exploded working tree trước đó |
| HTTP | JS viewer/review và CSS khớp byte candidate; health200; review no-store |
| Năm mô hình | Phiên44–48 có10/11/8/12/12 placements, khớp spec/layout của phiên và có geometry thật |
| Tương tác xem | Xoay chuột, zoom bằng Enter, căn giữa, reload; 15 viewport390/768/1440 đạt |
| Quyền | Đổi sang phiên30 của người khác404; khách302 đăng nhập |
| Không ghi khi xem | 0 request ghi nghiệp vụ sau đăng nhập, 0 lỗi JS chưa xử lý |
| Bảo vệ | 10 file giữ hash; không sửa DB/schema/seed, không tác động8081 |

Bằng chứng mới: `D:/Web-Pro/qa-evidence/final-war-run-config-20261018/`, gồm
`CONFIGURATION.json`, `FINAL_RUN_ACCEPTANCE.json`, ảnh năm mẫu desktop/mobile;
backup cấu hình và logging nằm trong private giới hạn ACL. Không chạy lại full
suite vì source/WAR không đổi: kết quả259 Node/163 JUnit thuộc chính WAR này.
Đây là kiểm mới bằng Run IDE thật, không kế thừa kết luận từ helper hay QA8082.

Dùng **Tomcat Final WAR khi demo**, Run cũ khi phát triển. Chỉ chạy một cấu hình
cùng lúc vì cùng cổng8080/JMX1099. Chỉnh source/WIP không tự cập nhật WAR Final;
muốn đổi bản final cần build sạch, kiểm và thay artifact có kiểm soát.
Hoàn tác cấu hình: Stop Final, chọn Run phát triển cũ; file Final có thể bỏ riêng
nếu không dùng nữa, không cần phục hồi toàn workspace hoặc DB. Không dùng helper
ngoài IDE để dừng process này. Không sửa global settings.

**Trạng thái chốt: IntelliJ Run Final đã chạy đúng WAR sạch và kiểm đạt trên
localhost:8080.** Giữ server của IDE chạy, không còn chờ bước Run này. Các giới hạn
kiểm live GROUP/spec lịch sử ở phần trước vẫn được ghi đúng phạm vi. Chưa push.
