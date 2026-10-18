# Mô hình đúng phiên ngay trên trang hoàn thiện — 18/10/2026

## Phạm vi và bản nguồn

Nhánh `integration/fullstack-v2`, đầu lượt `ccf06ee` (source trước sửa `7af2d5f`).
Không thay schema, API cũ, spec công bố, luật lắp hoặc dữ liệu lịch sử.
Không triển khai/restart 8080/8081. Cả hai cổng không lắng nghe lúc khảo sát.

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

Trạng thái: **code và QA viewer đạt; triển khai 8080/8081 còn chờ duyệt**.
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
