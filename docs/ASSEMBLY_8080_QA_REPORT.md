# Nghiệm thu kéo-thả đủ 5 mẫu trên localhost:8080

## Kết luận — 17/10/2026

Đã triển khai và kiểm thực tế đúng `http://localhost:8080/`, context `/`,
JDK 17.0.19 / Tomcat 9.0.120 / JNDI `robot_lab_content_test`.
Không lấy kết quả QA8082 để thay cho nghiệm thu8080.

Code ứng dụng từ commit `7af2d5f`, gồm hai sửa chính:

- `875a8af`: đường bắt đầu bình thường chọn DETAILED, không lấy nhầm GROUP cũ;
  URL có session vẫn mở đúng lịch sử. Lỗi server không giả lập tiến độ local.
- `7af2d5f`: bắt điểm thả theo marker chiếu lên màn hình, phân biệt các khớp
  khác độ cao; camera chứa toàn bộ mô hình/khay. Dùng chung placement view.

Lượt tiếp quản này không sửa thêm luật Java/API/rubric hoặc source WIP.
Chỉ bổ sung QA, triển khai sau khi người dùng duyệt, và tài liệu bàn giao.

## Nguyên nhân và phạm vi khắc phục

8080 phục vụ target của IntelliJ, nối DB làm việc; 8081 là artifact/DB demo khác.
DB làm việc thiếu spec của bốn mẫu, trong khi đường mặc định có thể tiếp tục
phiên GROUP. Vì vậy bấm chọn nhóm vẫn xuất hiện dù bản khác đã hỗ trợ DETAILED.
Điểm thả trước đây còn phụ thuộc mặt phẳng ngang, không phù hợp khớp xếp tầng.

Không chuyển đổi/xóa/chấm lại GROUP. Nút mở phiên kéo-thả riêng vẫn có trên
trang lịch sử; vào Mẫu robot → Lắp mẫu này là đường mặc định cho phiên chi tiết.

## Bằng chứng kiểm thử

| Phạm vi | Kết quả |
| --- | --- |
| Node trên snapshot sạch | 253 đạt, 0 fail/skip |
| Node chạy lại trên working tree sau triển khai | 253 đạt, 0 fail/skip |
| Maven JDK17 `test` và `clean package`, snapshot sạch | 159 JUnit, 0 failure/error/skip |
| Ma trận đường vào trên clone đúng DB8080 | 45 ca đạt: fresh/GROUP/DETAILED/cả hai/URL lịch sử/hai tab |
| Kéo-thả chuột thật trên clone | 53 lượt gắn đúng, 15 lượt sai bị server từ chối |
| Kéo-thả chuột thật trên8080 | 53 lượt gắn đúng, 5 lượt thiếu cha trả422 |
| Viewport8080 | 15 ô: mỗi mẫu ở390/768/1440, không tràn ngang |
| Source/artifact/HTTP | 349 file candidate khớp target; 5 tài nguyên HTTP khớp byte |
| Bytecode chính của artifact | 179 class, tất cả major61 (Java17) |

Kiểm chuột dùng mouse down → move → up trên canvas, không gọi API gắn để thay
thao tác người dùng. API GET/layout và dữ liệu MySQL dùng để đối chứng việc lưu.
Trên clone đã kiểm sai linh kiện, sai hướng, thiếu cha; không cộng tiến độ sai.

| Mẫu | Vật thể | Phiên QA8080 | Kết quả |
| --- | ---: | ---: | --- |
| Tránh vật cản | 10 | 44 | Đạt |
| Dò đường | 11 | 45 | Đạt |
| Cánh tay mini, 4 servo riêng | 8 | 46 | Đạt |
| Dò line + tránh vật cản | 12 | 47 | Đạt |
| Quét hướng tránh vật cản | 12 | 48 | Đạt |

Mỗi mẫu8080: vào từ catalog → chuẩn bị kit → kéo từng vật thể/chọn hướng →
reload giữ đủ bố cục → tháo một vật thể → undo → hoàn tất → trang hoàn thiện200.
POST undo thiếu CSRF bị403 trên cả5 phiên. Các phiên QA đều COMPLETED;
không dùng URL này làm phiên lắp mới hoặc coi là phiên dành live của người học.

Hồi quy bổ sung trên clone: Xưởng tạo qua form Servlet, kéo khung/Arduino thật,
reload/undo/kiểm tra; khác chủ404, thiếu CSRF403. Replay Bước sau/Phát/Tạm dừng
không có request ghi; trang hoàn thiện và biên nhận200. Quiz server chấm7/7;
hai ID vượt Long trả404 JSON, khác chủ404 và thiếu CSRF403. Đã sửa fixture của
harness dùng nhầm mật khẩu tài khoản thứ hai; không reset mật khẩu người dùng.

Một lượt Node tại repo phát hiện `index.html` có newline LF/CRLF trộn trong
working tree, còn candidate checkout là CRLF. Đã chỉ định dạng newline của
index theo checkout Git, giữ nguyên nội dung UTF-8/Git; test byte đạt lại.
Không bỏ/skip/nới test. Không sửa newline các file WIP.

## Triển khai, sao lưu và bảo toàn

Người dùng duyệt riêng hai phạm vi: triển khai DB/artifact/restart8080, sau đó
tạo 1 tài khoản QA và 5 phiên mới để nghiệm thu. Không dùng tài khoản cũ để thử.

WAR sạch:

```text
commit: 7af2d5fa065ee3371cc87f9bf1365f44fc65565c
SHA256: fba465cfed2d0c67874714a5ae30a1f57137a209c8e6ef3323d765b898c30cde
```

- Backup DB mới trước seed đã restore vào `robot_lab_assembly_restore_20261017`;
  đủ55 bảng và khớp hash. Không restore đè DB làm việc.
- Seed chỉ thêm4 spec/43 slot/43 orientation; chạy lại không đổi dữ liệu.
  Mọi dòng PUBLISHED đã có giữ nguyên, không đổi schema.
- Sau cả seed và QA: tất cả dòng cũ của55 bảng còn nguyên; so với DB restore,
  không chỉ so số dòng. QA mới thêm user24, 5 phiên,53 instance/placement,
  73 event và40 session_component; không checkout/đổi kho hoặc tạo điểm nhiệm vụ.
- 44/44 hash file bảo vệ khớp, gồm10 WIP/cá nhân, .env, IDE và cấu hình Tomcat.
- 8081 giữ PID/artifact,54/54 bảng DB demo giữ nguyên hash; health200.
- QA8082 được dừng/dọn riêng sau nghiệm thu; giữ DB clone và bằng chứng.

Lần triển khai gặp lỗi helper chuyển cache C→D và một lỗi biến in kết quả sau
health; đã bảo toàn artifact cũ, sửa parser tham số Windows/cache cùng ổ rồi
khởi động candidate lại. Không che các lần thử này bằng kết quả health đầu tiên:
chỉ nghiệm thu sau khi candidate chạy lại và cả5 mẫu qua thao tác thật.

Runtime8080 được khởi động lại bằng Bootstrap với chính CATALINA_BASE/docBase/
JNDI đã xác minh của IntelliJ; tiến trình Run cũ đã kết thúc. Không sửa cấu hình
IDE. Log khởi động dùng bản logging private không in DB system properties;
không sửa logging.properties gốc. Không kỳ vọng nút Stop của Run cũ quản lý PID mới.

## Sử dụng và khởi động lại

Để dùng luồng mới đã kiểm: mở `http://localhost:8080/pages/mau-robot.html`, đăng
nhập USER của DB làm việc → Lắp mẫu này → chuẩn bị → vào3D. Không dùng thông
tin tài khoản demo8081 cho8080 trừ khi chính bạn đã tạo tài khoản tương ứng.

```powershell
& 'D:\Web-Pro\qa-evidence\assembly-8080-20261017\manage-8080.ps1' -Action Status
& 'D:\Web-Pro\qa-evidence\assembly-8080-20261017\manage-8080.ps1' -Action Stop
& 'D:\Web-Pro\qa-evidence\assembly-8080-20261017\manage-8080.ps1' -Action Start
```

Đã kiểm Stop/Start: cùng artifact, health200, không dừng8081. Helper từ chối
PID không thuộc runtime này và không mở trùng cổng. Sau restart cần đăng nhập lại.
Nếu muốn dùng nút Run IntelliJ trở lại: Stop runtime này trước. Build từ working
tree có thể đóng gói cả WIP của bạn; khi đó checksum không còn là candidate này,
cần kiểm lại artifact. Helper không tự ghi đè một bản bạn vừa build.

`FINAL.ps1` vẫn quản lý **8081**, không phải bản8080 vừa nghiệm thu. Không lấy
việc8080 đạt để tuyên bố8081 đã nhận hai sửa đường vào/marker của lượt này.

### Quay lui khi cần

Backup trong thư mục private ngoài repo ở
`D:/Web-Pro/qa-evidence/assembly-8080-20261017/private/deploy-8080/`:
`before.sql`, `original.war`, `original-exploded/`, `conf-backup/` và metadata.
Không in/nộp thư mục này vì có dữ liệu/kết nối riêng.

Chỉ quay lui khi chủ động yêu cầu: dừng đúng runtime bằng helper, giữ candidate
ở đường riêng, đưa original WAR/exploded về đúng docBase rồi khởi động với cấu
hình8080 đã sao lưu. Không khởi động helper Start candidate khi WAR đã quay lui.
Giữ seed cộng thêm và các phiên mới; không xóa spec đã được tham chiếu. Không
restore toàn DB lên dữ liệu phát sinh sau backup. Restore dữ liệu chỉ làm trong
quy trình riêng có xác nhận, đối chiếu và thời gian dừng phù hợp.

## Đối chiếu nội dung giảng viên và giới hạn

Không thêm framework/thư viện/Service/DAO; vẫn Java17, javax.servlet, Model2,
JavaBean, static XxxDB/JDBC, PreparedStatement/finally và transaction/khóa.
JSP dùng EL/JSTL; JavaScript chỉ hiển thị/nhận tương tác. Không sửa luật chấm,
API contract, phân quyền, session_version hoặc cơ chế CSRF hiện có.

Khi vấn đáp, chỉ các điểm thật trong code, không bịa số chương/slide:

- `assets/js/main.js:347`, `assets/js/assembly-3d.js:223`: tạo/tiếp tục phiên
  chi tiết mặc định; URL có ID vẫn gọi get đúng phiên.
- `assets/js/assembly-3d-placement.js:305`: chọn marker trên màn hình; snap
  chỉ là preview. Server mới quyết định component/slot/parent/orientation hợp lệ.
- `AssemblySessionServlet.java:79` (thư mục controller): nhận cờ boolean tạo
  DETAILED; nhánh tạo/tiếp tục cũ không bị thay contract.
- `AssemblyPlacementDB.java:39,63` (thư mục data): khóa người học, chọn spec
  PUBLISHED để tạo phiên theo server; các transaction giữ dữ liệu nhất quán.

Mô hình3D là minh họa học tập, không mô phỏng dung sai cơ khí, điện, firmware
hay góc điều khiển servo thật. Chưa thử Firefox/Safari, màn hình cảm ứng hoặc
screen reader; viewport nhỏ là kiểm bố cục, không phải chứng nhận touch drag.
Không chạy lại toàn bộ POST cửa hàng/nhiệm vụ/chẩn đoán trong lượt này; dựa vào
hồi quy hiện có cho phần không sửa. Không tuyên bố mọi chức năng toàn site đã
được nghiệm thu lại trên8080.

Bằng chứng: `D:/Web-Pro/qa-evidence/assembly-8080-20261017/`, gồm matrix45 ca,
drag/rejection JSON,8080-acceptance.json, deployment-8080-result.json, ảnh từng
mẫu/viewport, log Node/Maven và snapshot riêng. Mật khẩu QA8080 lưu DPAPI ngoài Git.
