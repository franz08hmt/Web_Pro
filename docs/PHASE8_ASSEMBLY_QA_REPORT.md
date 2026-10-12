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
