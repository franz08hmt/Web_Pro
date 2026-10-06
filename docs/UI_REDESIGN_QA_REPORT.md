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
Mọi trang dưới đây đã có theme/shell và được render bằng dữ liệu server thật.
Trang lỗi được kiểm ở 422; task-preview được mở bằng POST preview, không confirm.

| File | Route / action | Vai trò | Layout | Triển khai / QA |
| --- | --- | --- | --- | --- |
| assembly-receipt.jsp | /assembly-receipt?session= | Chủ phiên | Kết quả + bảng | Render + thao tác QA |
| learning-summary.jsp | /learning-summary | Đăng nhập | Tổng quan + bảng | Render + hợp đồng form |
| learning-profile.jsp | /learning-profile | Đăng nhập | Shell + tờ giấy in | Render + hợp đồng form |
| admin-stats.jsp | /admin-stats | ADMIN | Thống kê + bảng | Render + hợp đồng form |
| architecture.jsp | /architecture | Công khai | Thông tin kỹ thuật | Render + hợp đồng form |
| account.jsp | /account | Đăng nhập | Thông tin tài khoản | Render + hợp đồng form |
| robots.jsp | /robots?page= | Công khai | Catalog + bảng | Render + hợp đồng form |
| components.jsp | /components?page= | Công khai | Catalog + bảng | Render + hợp đồng form |
| order-history.jsp | /order-history?page= | Đăng nhập | Lịch sử + bảng | Render + hợp đồng form |
| task-list.jsp | /tasks | USER | Danh sách card | Render + hợp đồng form |
| task-view.jsp | /tasks?action=view; /admin-tasks?action=view/preview | USER sở hữu / ADMIN | Chi tiết + lịch sử | Render + hợp đồng form |
| task-quiz.jsp | /tasks?action=quiz | USER được giao | Form quiz / kết quả | Render + hợp đồng form |
| task-submit.jsp | /tasks?action=submit | USER được giao | Bằng chứng + form | Render + hợp đồng form |
| task-preview.jsp | POST /tasks action=preview | USER được giao | Xem trước + xác nhận | JS-off preview 200; không confirm |
| task-error.jsp | Lỗi 422 các route task/diagnosis | Theo route | Thông báo | Render + hợp đồng form |
| admin-task-list.jsp | /admin-tasks?state= | ADMIN | Bộ lọc + card | Render + hợp đồng form |
| admin-task-form.jsp | /admin-tasks?action=new/edit | ADMIN | Biên soạn + radio | Render + hợp đồng form |
| admin-task-review.jsp | /admin-task-reviews?id= (bài nộp) | ADMIN | Bằng chứng + đánh giá | Render + hợp đồng form |
| diagnosis-list.jsp | /diagnosis | USER | Catalog + đang làm | Render + hợp đồng form |
| diagnosis-play.jsp | /diagnosis?action=play&id= | Chủ lượt | Phép kiểm tra + form | Render + hợp đồng form |
| diagnosis-result.jsp | /diagnosis?action=play&id= (đã nộp) | Chủ lượt | Kết quả + giải thích | Render + hợp đồng form |
| admin-diagnosis-list.jsp | /admin-diagnosis | ADMIN | Danh sách card | Render + hợp đồng form |
| admin-diagnosis-form.jsp | /admin-diagnosis?action=new/edit | ADMIN | Biên soạn | Render + hợp đồng form |
| admin-diagnosis-preview.jsp | /admin-diagnosis?action=view/preview | ADMIN | Xem trước / quản lý | Render + hợp đồng form |
| wiring-list.jsp | /wiring | USER | Catalog + lịch sử | Render + thao tác QA |
| wiring-play.jsp | /wiring?action=play&id= (nháp) | Chủ lượt | Sơ đồ + hướng dẫn + form | Render + thao tác QA |
| wiring-result.jsp | /wiring?action=result/play&id= (đã nộp) | Chủ lượt | Sơ đồ + kết quả | Render + hợp đồng form |
| wiring-error.jsp | Lỗi 422 các route wiring/support | Theo route | Thông báo | Render + hợp đồng form |
| admin-wiring-list.jsp | /admin-wiring | ADMIN | Danh sách card | Render + hợp đồng form |
| admin-wiring-form.jsp | /admin-wiring?action=new/edit | ADMIN | Đầu nối + quy tắc | Render + hợp đồng form |
| admin-wiring-view.jsp | /admin-wiring?action=view/preview | ADMIN | Sơ đồ + quản lý | Render + hợp đồng form |
| wiring-support-list.jsp | /wiring-support; /admin-wiring-support | USER / ADMIN | Danh sách yêu cầu | Render + hợp đồng form |
| wiring-support-new.jsp | /wiring-support?action=new&attemptId= | Chủ lượt | Bản chụp + form | Render + hợp đồng form |
| wiring-support-view.jsp | /wiring-support?action=view; /admin-wiring-support?action=view | Chủ yêu cầu / ADMIN | Lịch sử + bản chụp + form | Render + hợp đồng form |

## Bằng chứng hiện có

HTML trước sửa: 303 request của bốn demo, 260 trả 200; lưu ngoài Git tại
D:/Web-Pro/qa-evidence/ui-redesign. CSRF được thay TOKEN trong bằng chứng.
Các ảnh before và final đại diện 16 route/layout ở desktop và 390px;
form biên soạn, preview, lỗi và các luồng QA có ảnh bổ sung.

## Kiểm chứng đã thực chạy

| Kiểm tra | Kết quả / giới hạn |
| --- | --- |
| Node baseline → cuối | 128/128 → 131/131; không skip, không fail |
| JUnit baseline → cuối | 79/79 → 79/79; JavaBeanRulesTest và JspExpressionContractTest xanh |
| Maven JDK 17 | test và clean package BUILD SUCCESS |
| Runtime | Tomcat 9, loopback 8081 riêng; MySQL robot_lab_content_test |
| Render manifest | 34 view độc lập + fragment, gồm cả preview POST và trạng thái lỗi 422 |
| Form trước/sau | 260 trang render, diff control/action/validation/token rỗng trước khi tạo QA mới |
| Guard mới | Giữ hợp đồng source của 35 JSP từ HEAD ban đầu; shell/noindex/SVG/asset/fragment |
| Viewport | 16 route đại diện tại 320/390/768/1024/1440px; không tràn ngang toàn trang |
| Audit | 75 tổ hợp vai trò/view ở 390px: một h1/main, lang vi, labels, skip link, bảng còn header/scope/caption |
| Zoom | CSS zoom 200% headless: đã sửa grid metric gây tràn; kiểm lại không tràn |
| Bảng màu | Bốn cặp chữ/nền chính đạt 6,94–14,38:1; đây không phải audit mọi pixel hay chứng nhận WCAG |
| Console/assets | Kiểm cả console.error, pageerror và response asset; favicon.ico 404 được sửa bằng cpu-chip.svg cục bộ |
| Wiring | Click hai chân, drag, Enter/Space/Escape, chọn/xóa dây; touch emulation 390px; lưu/reload/nộp/luyện lại |
| JS-off | Thêm/xóa dây, lưu/nộp; Admin biên soạn wiring, task A/B, diagnosis; lọc DRAFT; gửi phản hồi hỗ trợ; preview bài nộp |
| Hỗ trợ | Gửi → Admin trả lời → sửa/lưu lượt → snapshot mới → đóng; hai snapshot riêng, không nhận tương tác wiring |
| Quyền | Guest 302 đúng trang tài khoản + no-store ở chín route; USER vào Admin 403, khác owner GET/POST 404, CSRF sai 403 |
| Version | Lưu draft với version cũ trả 422; không ghi đè bản đã lưu |
| SQL | Dòng cũ trong 38 bảng giữ nguyên hash; chỉ thêm/sửa bản ghi QA mới qua ứng dụng |
| PDF | Chrome A4 ba trang, đã render và xem cả ba; shell/toolbar ẩn, giấy trắng, kỹ năng và xác nhận còn đủ, số trang 1/3–3/3 |
| Hồi quy | 10 GET lắp ráp/3D/quiz/tra cứu/shop/cart/tài khoản/tổng kết/profile/orders trả 200; source ngoài phạm vi không đổi |

Test nguồn mới không thay thế QA browser: fixture chỉ lưu biểu thức EL và
thuộc tính form của source ban đầu, không chứa dữ liệu cá nhân hay token thật.
Assertion profile cũ được chuẩn hóa khoảng trắng trong c:out để giữ kiểm tra
escape sau khi xuống dòng 140 ký tự; không nới guard bảo mật/nghiệp vụ.

WAR cuối trên runtime sạch: 260 response 200, không overflow, không
console/pageerror/asset 404. Log Tomcat không có JasperException; cổng 8081
đã đóng và CATALINA_BASE QA đã xóa. Không gửi lệnh dừng tới Tomcat 8080.

## Những vấn đề UI đã xử lý

- Giới hạn selector wiring.js/CSS vào đúng diagram; Heroicon ở header và
  các sơ đồ snapshot không bị áp kích thước canvas hay nhận thao tác.
- Snapshot/result/preview và sơ đồ JS-off không tạo nút SVG giả. Chỉ JS
  enhancement của play thêm role/tabindex; radio/form luôn dùng được.
- Giữ thead/table semantics trên mobile, thay rule responsive cũ làm mất
  header bằng cuộn nội vùng có nhãn. Không sửa server-view.css toàn cục.
- Không để xuống dòng JSTL làm thay giá trị expectedReviewId; diff form
  runtime và fixture nguồn đã bắt lỗi này trong quá trình triển khai.
- Include dùng pageEncoding UTF-8; văn bản Việt/Poppins đã kiểm qua browser.
- Nhóm metric dùng cột co được khi zoom; hành động archive/delete/close có
  kiểu cảnh báo; navigation trang lỗi và Admin wiring phù hợp vai trò.
- Hồ sơ giữ thứ tự/nội dung; bỏ avoid trên cả khối kỹ năng để không ép bảng
  dài sang một trang không đủ chỗ; hàng ngắn và heading vẫn có quy tắc ngắt.

## Dữ liệu QA còn trong DB test

Không chạy migration, sửa password/role hay cập nhật dữ liệu cũ.

- Hai tài khoản chỉ kiểm đăng ký/chọn người trong nháp:
  qa-ui-1791267516391-a@robotlab.test và qa-ui-1791267516391-b@robotlab.test.
  Không lưu credential vào bằng chứng/Git. Luồng wiring/hỗ trợ dùng builder
  và practice trên bản ghi UI-QA mới; không sửa lượt/nhiệm vụ cũ của demo.
- Wiring exercise #7: `[UI-QA] Nối dây <b>hiển thị dạng chữ</b> …`, PUBLISHED.
  Attempt #28/#29 SUBMITTED: N=14, C=W=0, M=14, điểm 0,0; #30 DRAFT, điểm NULL.
- Support #7 CLOSED, ba tin #17–19; snapshot ở hai tin USER, tin Admin không
  ghi đè snapshot. Điểm hai lượt đã nộp không đổi.
- Diagnosis practice attempt #13; diagnosis draft #6107, tiêu đề UI-QA.
- Task draft #21 mẫu A và #22 mẫu B, tiêu đề UI-QA; mỗi nháp chọn hai USER QA.
  Không công bố, không tạo vòng nộp hay thay kết quả chấm cũ.

Giữ các bản ghi này để người dùng quyết định dọn; không tự xóa.

## Giới hạn và bàn giao

- Chưa kiểm Firefox/Safari, cảm ứng vật lý, screen reader hoặc chứng nhận
  WCAG. Zoom dùng CSS zoom headless, không phải thao tác Ctrl+Plus desktop.
- Bảng kỹ năng hiện vừa một trang; đã giữ CSS thead lặp nhưng chưa ép dữ
  liệu server thành bảng nhiều trang để quan sát header lặp trực tiếp.
- Không thực hiện lại toàn bộ vòng nộp/chấm A/B, mua hàng, lắp đủ 3D hay
  concurrency nghiệp vụ trong đợt UI. Có source/form contract, test cũ,
  render/GET hồi quy và ca stale version; không gọi GET 200 là kiểm hết luồng.
- Ngày ISO ở các view cũ như phiếu lắp ráp được giữ; không thêm Java getter
  hay thay giá trị nguồn trong đợt UI. fmt/fn cũ ngoài view thực hành không
  được nhân rộng hoặc refactor trong đợt này.
- Runtime QA, cookie/profile browser và cấu hình/log nhạy cảm được dọn sau
  kiểm chứng. Bằng chứng không bí mật giữ ngoài Git; Tomcat 8080 không bị dừng.
- Các mốc local: f5e8446 (plan), 2fc82ce (pilot), ac0feed (info/PDF),
  0b9daaa (practice), ca0ffb7 (Admin/support). Commit cuối chứa guard/báo cáo
  và hoàn thiện focus SVG/favicon; xem git log. Không push.
