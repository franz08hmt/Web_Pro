# Redesign không gian học tập — 06/10/2026

## Phạm vi và nguồn sự thật

Khởi đầu HEAD e65ef11 trên integration/fullstack-v2 (ahead 2); chỉ
AGENT_HANDOFF.md untracked. Baseline thực chạy: Node 128/128, JUnit 79/79.
Không đổi Java, SQL, schema, nghiệp vụ hay các trang HTML/3D ngoài phạm vi.

Ảnh 4 dùng cho phân cấp nội dung, card bài và vùng chính/phụ. Ảnh 5 dùng
cho charcoal, lớp panel, cam ở hành động chính và chi tiết mạch điện.
Không thêm số liệu, biểu đồ, menu hoặc chức năng giả. Poppins cục bộ và
Heroicons outline dùng lại, giữ giấy phép assets/icons/LICENSE.txt.

## Hệ thiết kế và bảo vệ hợp đồng

- learning-workspace.css được nạp riêng ở JSP, scope .learning-workspace;
  không import style.css, không đổi server-view.css hoặc phòng 3D.
- workspace-header.jspf và workspace-footer.jspf là fragment đơn giản,
  không sinh html/body/main; điều hướng thật theo vai trò từ session.
- SVG tương tác chỉ chọn trong #wiring-interactive-diagram .wiring-diagram;
  icon và sơ đồ bản chụp không trở thành mục tiêu tương tác.
- Giữ action/field/token/version/validation và điều kiện EL; bảng giữ ngữ
  nghĩa, cuộn nội vùng. Ghi chú kiến trúc được giữ trong details.
- Hồ sơ giữ tờ giấy trắng; theme chung chỉ áp dụng màn hình; navigation
  no-print. Không thêm biểu thức tính điểm ở client hay JSP.

## Manifest

34 trang độc lập; wiring-diagram.jsp là fragment, không có route riêng.
Các route dưới đây được đối chiếu Servlet hiện tại; id dùng đúng loại dữ liệu.
Trạng thái ban đầu: khảo sát; kiểm chứng cập nhật theo từng mốc.

| File | Route / action | Vai trò | Layout | Triển khai / QA |
| --- | --- | --- | --- | --- |
| assembly-receipt.jsp | /assembly-receipt?session= | Chủ phiên | Kết quả + bảng | Pilot đang kiểm |
| learning-summary.jsp | /learning-summary | Đăng nhập | Tổng quan + bảng | Khảo sát |
| learning-profile.jsp | /learning-profile | Đăng nhập | Shell + tờ giấy in | Khảo sát |
| admin-stats.jsp | /admin-stats | ADMIN | Thống kê + bảng | Khảo sát |
| architecture.jsp | /architecture | Công khai | Thông tin kỹ thuật | Khảo sát |
| account.jsp | /account | Đăng nhập | Thông tin tài khoản | Khảo sát |
| robots.jsp | /robots?page= | Công khai | Catalog + bảng | Khảo sát |
| components.jsp | /components?page= | Công khai | Catalog + bảng | Khảo sát |
| order-history.jsp | /order-history?page= | Đăng nhập | Lịch sử + bảng | Khảo sát |
| task-list.jsp | /tasks | USER | Danh sách card | Khảo sát |
| task-view.jsp | /tasks?action=view; /admin-tasks?action=view/preview | USER sở hữu / ADMIN | Chi tiết + lịch sử | Khảo sát |
| task-quiz.jsp | /tasks?action=quiz | USER được giao | Form quiz / kết quả | Khảo sát |
| task-submit.jsp | /tasks?action=submit | USER được giao | Bằng chứng + form | Khảo sát |
| task-preview.jsp | POST /tasks action=preview | USER được giao | Xem trước + xác nhận | Khảo sát |
| task-error.jsp | Lỗi 422 các route task/diagnosis | Theo route | Thông báo | Khảo sát |
| admin-task-list.jsp | /admin-tasks?state= | ADMIN | Bộ lọc + card | Khảo sát |
| admin-task-form.jsp | /admin-tasks?action=new/edit | ADMIN | Biên soạn + radio | Khảo sát |
| admin-task-review.jsp | /admin-task-reviews?id= (bài nộp) | ADMIN | Bằng chứng + đánh giá | Khảo sát |
| diagnosis-list.jsp | /diagnosis | USER | Catalog + đang làm | Khảo sát |
| diagnosis-play.jsp | /diagnosis?action=play&id= | Chủ lượt | Phép kiểm tra + form | Khảo sát |
| diagnosis-result.jsp | /diagnosis?action=play&id= (đã nộp) | Chủ lượt | Kết quả + giải thích | Khảo sát |
| admin-diagnosis-list.jsp | /admin-diagnosis | ADMIN | Danh sách card | Khảo sát |
| admin-diagnosis-form.jsp | /admin-diagnosis?action=new/edit | ADMIN | Biên soạn | Khảo sát |
| admin-diagnosis-preview.jsp | /admin-diagnosis?action=view/preview | ADMIN | Xem trước / quản lý | Khảo sát |
| wiring-list.jsp | /wiring | USER | Catalog + lịch sử | Pilot đang kiểm |
| wiring-play.jsp | /wiring?action=play&id= (nháp) | Chủ lượt | Sơ đồ + hướng dẫn + form | Pilot đang kiểm |
| wiring-result.jsp | /wiring?action=result/play&id= (đã nộp) | Chủ lượt | Sơ đồ + kết quả | Khảo sát |
| wiring-error.jsp | Lỗi 422 các route wiring/support | Theo route | Thông báo | Khảo sát |
| admin-wiring-list.jsp | /admin-wiring | ADMIN | Danh sách card | Khảo sát |
| admin-wiring-form.jsp | /admin-wiring?action=new/edit | ADMIN | Đầu nối + quy tắc | Khảo sát |
| admin-wiring-view.jsp | /admin-wiring?action=view/preview | ADMIN | Sơ đồ + quản lý | Khảo sát |
| wiring-support-list.jsp | /wiring-support; /admin-wiring-support | USER / ADMIN | Danh sách yêu cầu | Khảo sát |
| wiring-support-new.jsp | /wiring-support?action=new&attemptId= | Chủ lượt | Bản chụp + form | Khảo sát |
| wiring-support-view.jsp | /wiring-support?action=view; /admin-wiring-support?action=view | Chủ yêu cầu / ADMIN | Lịch sử + bản chụp + form | Khảo sát |

## Bằng chứng hiện có

HTML trước sửa: 303 request của bốn demo, 258 trả 200; lưu ngoài Git tại
D:/Web-Pro/qa-evidence/ui-redesign. CSRF được thay TOKEN trong bằng chứng.
Ảnh/browser/PDF và kết quả cuối sẽ bổ sung sau khi thực chạy; chưa nghiệm thu.
