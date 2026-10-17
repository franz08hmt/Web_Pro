# Lắp chi tiết toàn bộ mẫu — 17/10/2026

## Phạm vi và kết quả

Yêu cầu: ngoài obstacle-avoider,4 mẫu còn lại cũng kéo-thả từng vật thể và chọn
hướng; không chỉ tick nhóm. Giữ Java17, Tomcat9/javax.servlet, Model2 và JDBC.
Không thêm framework, thư viện, bảng, DAO hay Service. Luật kiểm vẫn ở JavaBean
AssemblyLayout; Servlet nhận request/kiểm session-CSRF, XxxDB khóa và lưu.

| Mẫu | Vật thể | Kết quả QA |
| --- | ---: | --- |
| line-follower | 11 | DETAILED, COMPLETED |
| mini-arm | 8 | DETAILED, COMPLETED;4 servo riêng |
| line-obstacle | 12 | DETAILED, COMPLETED |
| servo-scout | 12 | DETAILED, COMPLETED |
| obstacle-avoider | 10 | Hồi quy v2 COMPLETED |

## Thay đổi

- Seed cộng thêm4 spec v1,43 slot và43 hướng hợp lệ; tọa độ lấy từ config3D
  hiện có, bù góc nền để cùng tư thế cuối. Cha của bánh là động cơ; cảm biến
  của servo-scout gắn sau servo; cánh tay theo chuỗi4 servo rồi bộ kẹp.
- API tùy chọn newDetailedSession=true; giữ create-or-resume cũ khi không truyền.
  Transaction khóa user trước khi tìm/tạo DETAILED đang mở. Hai request đồng thời
  cùng user/mẫu trả cùng ID, không sửa phiên GROUP đã có.
- Module UI nhỏ assembly-mode.js: thông báo dữ liệu GROUP cũ, nút mở phiên
  kéo-thả riêng rồi chuyển về chuẩn bị kit. Dùng button/status/textContent,
  không div click, không HTML từ DB, không tin localStorage để cấp quyền.
- Cache-busting mới tại hai trang thực hành; cơ chế pointer/keyboard/undo/replay
  có sẵn được dùng lại, không viết một engine mới cho mỗi mẫu.

## Bằng chứng mới

- Test RED trước thay đổi:3 ca thiếu seed/API/UI thất bại; sau thay đổi test
  tập trung16/16 đạt. Toàn bộ npm:234/234,0 fail/skip (trước231).
- Build từ archive HEAD + đúng10 file feature, loại toàn bộ WIP người dùng;
  Maven JDK17 clean package,149 JUnit,0 fail/error/skip. Không build vào target gốc.
- DB thử riêng robot_lab_all_models_qa_20261017, runtime127.0.0.1:8082;
 2 tài khoản USER QA đăng ký qua UI, runtime DB login chỉ DML trên DB QA.
  Không seed DB cũ hoặc DB demo final. Không đổi .env/IDE/Tomcat8080.
- Cả5 mẫu kiểm thật HTTP: tạo DETAILED, bấm lại/2 request đồng thời cùng ID,
  owner khác404, thiếu CSRF403, thiếu cha/sai loại/sai hướng422,
  gắn đủ N/N, tháo1 vật thể, undo, GET phục hồi version/layout,
  server xác nhận COMPLETED và mở replay.
- Chuột thật:5/5 mẫu kéo vật thể khay tới vị trí gắn, server200 và DB lưu đúng
  vật thể. Undo rồi chọn radio/điểm gắn/hướng, Enter trên nút Gắn thành công;
  reload hiển thị dữ liệu đã lưu. Không gọi hàm mutate nội bộ bằng evaluate.
-15 ô Chrome390/768/1440 không tràn ngang; screenshot đầy đủ. Bốn phiên GROUP
  tạo trước seed giữ nguyên hash dữ kiện sau khi công bố spec và thực hành mới.
- Runtime QA8082 đã dừng/dọn, không SEVERE/JasperException.44 file bảo vệ và
  dữ liệu DB demo final khớp mốc sau tập dượt (12 nhóm live/dự phòng không đổi).
- DB cũ robot_lab_content_test:52/55 bảng khớp mốc14:56. Ba bảng phiên tăng:
  sessions32→35, components213→245, visual_parts118→124; USER7 có phiên41/42/43
  lúc16:19 và cập nhật phiên24. Hoạt động này trước lúc tạo DB QA mới;
 0 tài khoản qa-all-models-a/b trong DB cũ. Không suy là lỗi feature hoặc tự
  rollback hoạt động có thể của người dùng; chưa có audit log xác định người thao tác.

Đối chiếu cuối sau commit:43/44 hash còn khớp, riêng `.idea/workspace.xml`
đã đổi trong lúc làm việc. File này không nằm trong patch, không được runner
ghi/sao chép vào repo, không stage/commit; chưa đủ dữ kiện quy kết nguồn thay đổi.
Không khôi phục file cấu hình IDE vì có thể ghi đè trạng thái hiện tại của người dùng.
Sáu file WIP và bốn tài liệu cá nhân vẫn khớp10/10; DB demo final giữ nguyên.

Runner, state, ảnh và artifact nằm ngoài Git:
`D:/Web-Pro/qa-evidence/all-models-20261017/`.
Chưa kiểm Firefox/Safari, cảm ứng thật, screen reader hoặc vật lý robot thật.
Không báo các phần đó là PASS. Bản khách là preview cục bộ, không chứng minh server.

## Sai lệch runner đã xử lý trung thực

Lượt đầu runner gửi ID dạng chuỗi thay vì số nên server422 trước nhánh thiếu
cha; đó là lỗi runner, không đổi API hay assertion nghiệp vụ để né. Sau đối chiếu
payload, sửa runner gửi Number(id), tiếp tục đúng phiên QA đã có. Mọi ca cuối đạt.
Một lệnh Maven PowerShell chưa quote tham số -D bị tách sai trước khi compile;
đã quote và build từ archive sạch. Không thay POM hoặc Java để chữa môi trường.

## Cách áp dụng cho máy trình bày

1. Backup và chọn đúng DB; chạy seed all-models sau các seed spec obstacle v1/v2.
2. Build/deploy WAR mới bằng JDK17 theo hướng dẫn README.
3. Đăng nhập, tạo phiên mới; GROUP cũ dùng nút rõ ràng để mở phiên DETAILED riêng.
4. Kiểm cả N/N từng mẫu và trang hoàn thiện. Không chuyển mode phiên cũ bằng SQL.

Đây là kiểm tra ràng buộc tư thế lắp giản lược; không đo dung sai, kiểm đấu điện,
mô phỏng động học hay bảo đảm robot thật vận hành an toàn. Không đổi rubric điểm.

## Triển khai đã duyệt lên DB demo final — 17/10/2026

Người dùng giao chọn phương án tốt nhất cho final. Chọn DB demo riêng thay vì
DB làm việc; bản final mới đang chạy localhost8081, không đổi8080/.env/IDE.
Không sửa source ứng dụng trong lượt triển khai này; dùng nguyên artifact đã QA.

- Backup MySQL trong private ACL ngoài Git (198821 byte), restore thử vào
  robot_lab_final_restorecheck_20261017 mới. Hash dữ liệu54/54 bảng khớp DB nguồn.
  Không restore đè, không xóa DB cũ hoặc DB restore-check.
- Nạp đúng seed từ commit a5e5409 bằng runtime DB login hiện có: thêm4 spec,
 43 slot và43 hướng. Chỉ3 bảng này đổi;51 bảng khác khớp hash trước/sau seed.
- WAR SHA256 `7fb5b374ffa668a25f5556c8d838d0145bc7b36fa14c35408df9e2d90d0b4dff`.
  Giải nén chính WAR vào thư mục artifact riêng;352 file khớp byte archive.
  Thư mục Maven exploded ban đầu không có META-INF/MANIFEST.MF (WAR tự sinh);
  đã giữ thư mục đó làm đầu vào đóng gói và dùng bản giải nén WAR, không vá ứng dụng.
- Tomcat9/JDK17 riêng127.0.0.1:8081; runtime DB login chỉ DML trên DB demo;
  không cấp thêm quyền hoặc tạo tài khoản website mới. Credential vẫn trong DPAPI.
-44 smoke/check mới đạt: guest/USER/ADMIN, kiểm quyền server, phiếu dự phòng,
  dữ liệu live, quiz/nhiệm vụ/tổng kết/nối dây/chẩn đoán,6 tài nguyên HTTP khớp byte,
 4 giao diện model có khay vật thể/4 lựa chọn hướng;12 ô390/768/1440 không tràn ngang.
- USER3 được chuẩn bị4 phiên hữu ích cho trình bày qua API/UI:4 dò đường0/11,
 5 cánh tay0/8,6 kết hợp0/12,7 quét hướng0/12. Không placement/event/quiz/bài nộp/
  lần chấm mới. Bước chuẩn bị này thêm4 sessions,32 session_components và43 instances.
-14 nhóm snapshot phiên1–3, task/recipient/round/submission/review/quiz/answers
  khớp trước triển khai. Tài vẫn9/10, backup10/10, task1 DRAFT; rehearsal95/100 giữ nguyên.
-44 file bảo vệ khớp mốc đầu lượt triển khai, kể cả10 WIP/cá nhân. Đây là mốc
  mới sau thay đổi workspace.xml đã báo ở lượt trước, không phủ nhận sai lệch lịch sử đó.
- Stop/Start runtime riêng đã thử thành công; log không SEVERE/JasperException.
  Runtime mới được để chạy cho người dùng; không tự khởi động cùng Windows.

Hướng dẫn mở/đăng nhập/dừng và URL từng phiên ở đầu TEAM_FLOW_DEMO_GUIDE.
Lệnh gọn ngoài Git: `D:/Web-Pro/FINAL.ps1`. Script không tự chép mật khẩu khi mở
web; chỉ các lệnh MatKhau... do người dùng chủ động chạy mới dùng clipboard.
Evidence/state/manifest/backup/artifact: `D:/Web-Pro/qa-evidence/final-deploy-all-models-20261017/`.
Chỉ smoke khi đưa bản mới lên demo, không nhận là kiểm lại Firefox/Safari, cảm ứng,
screen reader hay toàn bộ POST cũ.234 Node/149 JUnit và kiểm kéo thật kế thừa
từ đúng artifact QA trước đó; không cộng vào số44 như kiểm mới.
