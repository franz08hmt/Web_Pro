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

## Rà soát giao diện và chữ viết (UI-2) — 06/10/2026

Phần này ghi riêng kiểm chứng UI-2, không thay thế hoặc mở rộng các kết luận của đợt redesign trên.
Khởi đầu `7d27f18`, nhánh `integration/fullstack-v2`, ahead 8, chỉ handoff untracked.
Chỉ đổi CSS workspace, JSP/fragment trình bày và hai test nguồn mới; không sửa Java/SQL/JS,
schema, fixture hợp đồng hay trang HTML/3D. Header/footer được rà nhưng giữ markup cũ;
menu native được sửa bằng CSS. Cache-busting CSS được đồng bộ `20261006.5` ở cả 34 trang.

### Kết quả thực chạy

| Kiểm tra | Kết quả UI-2 |
| --- | --- |
| Node baseline → cuối | 131/131 → 133/133; 0 fail, 0 skip; thêm 2 test trình bày, không bỏ/nới guard cũ |
| JUnit baseline → cuối | 79/79 → 79/79; JavaBeanRulesTest/JspExpressionContractTest xanh |
| JDK 17 Maven | `test` và `clean package`: BUILD SUCCESS |
| Runtime | Tomcat 9 tại loopback 8081, CATALINA_BASE riêng; ADMIN và cả ba USER đăng nhập được |
| Golden GET | 327 response trước/sau, 283 HTML 200; lỗi/redirect giữ mã cũ |
| Hợp đồng form | 0 khác biệt method/action/name/type/value hidden/required/minlength/maxlength/min/max/checked/disabled/id; CSRF chuẩn hóa TOKEN |
| HTTP/cache | 0 khác biệt status/Location/Cache-Control trên 327 response |
| Số/mã/ngày | Không mất phần tử từ tập hiển thị trước; chỉ bỏ thời điểm lập hồ sơ thay theo đồng hồ khỏi phép so |
| SQL chỉ đọc | Hash nội dung 38 bảng không đổi; không tạo tài khoản/dữ liệu QA hoặc mutation nghiệp vụ |
| Browser server | 32 view × 5 độ rộng = 160 render: 320, 390, 768, 1024, 1440px; 0 tràn ngang toàn trang |
| Hai view tạm | task-preview và diagnosis-play: 10 render mỗi phase từ HTML tĩnh dựng từ JSP; không gọi POST preview/tạo lượt |
| Zoom/nội dung dài | 32 view server dùng CSS zoom 200%; 32 lần thay DOM tạm với tiêu đề dài, chuỗi 180/400 ký tự không khoảng trắng; 0 overflow |
| Bảng/a11y | 75 tổ hợp vai trò/view ở 390px; label, caption, thead/scope, skip link, font, snapshot chỉ đọc đều đạt |
| DOM | 1 h1/main; không id trùng, tabindex dương, EL/scriptlet/null/undefined lộ ra, text bị overflow:hidden cắt trong phạm vi đo |
| Vùng chạm | Không còn button/input/summary hoặc label radio/checkbox nhìn thấy thấp hơn 44px; trước sửa có 25 ô audit chứa summary nhỏ |
| Tương phản nhãn | 6 cặp chữ/nền status-badge: 8,31–12,69:1; không coi là chứng nhận WCAG hay kiểm mọi pixel |
| GET quyền/hồi quy | 40 ca đạt: 9 guest 302 + no-store; 18 USER→Admin 403; 3 khác owner 404; 10 route cũ 200 |
| Wiring không lưu | 6 nhóm: Enter/Space/Escape, tạo dây bằng phím, click hai chân, chọn/xóa dây, pointer drag, reload và JS-off; không gửi POST nghiệp vụ |
| Native details | Enter/Space mở/đóng Khám phá; Enter mở Cách tính điểm; không cần JS điều hướng |
| Nhiều nội dung | 12 tin nhắn nhân bản DOM tạm; 80 dây vẽ DOM tạm tại 390px; không lưu, không tràn toàn trang |
| Console/assets | Không pageerror/JS error hoặc asset 404. Hai console báo HTTP 422 là tài liệu task-error/wiring-error chủ động mở, không phải lỗi asset |
| Log | Runtime cuối: 0 JasperException, 0 SEVERE; không có tham số/mật khẩu DB trong log đã kiểm |

Chỉ POST đăng nhập để có phiên xác thực; mọi thao tác chức năng QA là GET hoặc sửa DOM
trong bộ nhớ. Không thử lưu/nộp/chấm/đồng thời ở UI-2. Không gọi GET 200 là kiểm đủ luồng ghi.
Fixture nguồn vẫn bảo vệ toàn bộ control và binding của 35 JSP gồm fragment.
Hai test mới chỉ kiểm menu/nhãn và chữ giải thích/công thức nằm trong details.

### Lỗi và chữ viết đã xử lý

9 nhóm vấn đề được xử lý; bảng chuỗi dưới có 42 mục đối chiếu, không phải 42 lỗi nghiệp vụ.

| Nhóm | Trang / phạm vi | Trước → sau |
| --- | --- | --- |
| Menu Khám phá | Tất cả 34 trang, CSS chung | summary mặc định lệch chữ/hàng → font/weight/line-height cùng nav, hàng 44px, marker gọn, hover/focus native |
| Nhãn trạng thái | Các trang liệt kê bên dưới | Chữ đậm thường → status-badge có chữ và nền/viền tương phản; lớp chọn bằng getter hiện có |
| Mã enum thô | account, assembly-receipt, admin-stats, admin-wiring-view | ADMIN/USER, trạng thái phiên, 5 enum thống kê, REQUIRED/FORBIDDEN → nhãn Việt; value control giữ nguyên |
| Chữ chấm điểm | task-view, admin-task-form, wiring-result, learning-profile | HALF_UP/score/C-W-M-N ở phần chính → giải thích thông thường; công thức nối dây giữ trong Cách tính điểm |
| Mã chân | fragment và view wiring/support có lựa chọn chân | Mã [uno.D5] lẫn tên → tên/chân chính, mã monospace phụ; aria-label/data/viewBox/ID giữ nguyên |
| Hậu quả hành động | task-view, admin-diagnosis-preview, admin-wiring-view, wiring-support-view | Thêm câu ngắn cạnh nút đóng/lưu trữ/xoá nháp, không thêm hộp thoại hoặc thao tác |
| Chữ mở đầu quá tối | order-history và rule .lead scoped | Màu cũ trên nền tối → màu chữ phụ của workspace; dữ liệu đơn không đổi |
| Chữ giải thích phóng lớn | admin-stats | small trong metric thừa kế cỡ số → 14px/weight400, giữ số liệu |
| Phiếu in gần trống | assembly-receipt | Trang 2 chỉ có link Về trang chủ → các liên kết điều hướng no-print, phiếu còn 1 trang A4 |

Trang có nhãn trạng thái: `account.jsp`, `admin-diagnosis-list.jsp`, `admin-diagnosis-preview.jsp`, `admin-stats.jsp`, `admin-task-list.jsp`, `admin-task-review.jsp`, `admin-wiring-list.jsp`, `admin-wiring-view.jsp`, `assembly-receipt.jsp`, `diagnosis-result.jsp`, `learning-profile.jsp`, `order-history.jsp`, `task-list.jsp`, `task-preview.jsp`, `task-view.jsp`, `wiring-list.jsp`, `wiring-play.jsp`, `wiring-result.jsp`, `wiring-support-list.jsp`, `wiring-support-view.jsp`.

Không thêm dữ liệu chấm vào DOM. Điều kiện `not diagnosisPreview` hiện đáp án quản trị,
điều kiện lượt đã nộp và điều kiện hiển thị quiz vòng hiện tại đều giữ nguyên.
Các chuỗi miễn trừ mô phỏng, không xác nhận robot thật, luyện tập có tài liệu,
điểm tự động /80 khi chờ chấm và hỗ trợ đã kết thúc không đồng nghĩa đạt vẫn còn nguyên.

### Checklist file × viewport

Đ = đạt kiểm layout bằng Chrome với HTML server; T = đạt kiểm layout bằng HTML tĩnh tạm,
không khẳng định đã render trạng thái này trên Tomcat trong UI-2. Cột mobile gồm cả 320 và 390px.
136 ô được kiểm: 128 ô server + 8 ô tạm; 170 render theo năm độ rộng mỗi phase.
Audit bao phủ DOM toàn trang; ảnh full-page được lưu và contact sheet được rà từng view/cỡ màn hình.
Điều này không thay kiểm screen reader, Tab từng control trên mọi form hoặc audit mọi pixel.

| File | 320/390 | 768 | 1024 | 1440 | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| account.jsp | Đ | Đ | Đ | Đ | Ngày ISO cũ giữ nguyên, xem giới hạn |
| admin-diagnosis-form.jsp | Đ | Đ | Đ | Đ |  |
| admin-diagnosis-list.jsp | Đ | Đ | Đ | Đ |  |
| admin-diagnosis-preview.jsp | Đ | Đ | Đ | Đ |  |
| admin-stats.jsp | Đ | Đ | Đ | Đ |  |
| admin-task-form.jsp | Đ | Đ | Đ | Đ |  |
| admin-task-list.jsp | Đ | Đ | Đ | Đ |  |
| admin-task-review.jsp | Đ | Đ | Đ | Đ |  |
| admin-wiring-form.jsp | Đ | Đ | Đ | Đ |  |
| admin-wiring-list.jsp | Đ | Đ | Đ | Đ | Có bản ghi DB cũ lỗi dấu ?, không sửa dữ liệu |
| admin-wiring-view.jsp | Đ | Đ | Đ | Đ | Có bản ghi DB cũ lỗi dấu ?, không sửa dữ liệu; Sơ đồ cuộn nội vùng; mã chân phụ; snapshot chỉ đọc |
| architecture.jsp | Đ | Đ | Đ | Đ |  |
| assembly-receipt.jsp | Đ | Đ | Đ | Đ | Ngày ISO cũ giữ nguyên, xem giới hạn |
| components.jsp | Đ | Đ | Đ | Đ |  |
| diagnosis-list.jsp | Đ | Đ | Đ | Đ |  |
| diagnosis-play.jsp | T | T | T | T | HTML tạm; trạng thái không tới được bằng GET dữ liệu sẵn có |
| diagnosis-result.jsp | Đ | Đ | Đ | Đ |  |
| learning-profile.jsp | Đ | Đ | Đ | Đ |  |
| learning-summary.jsp | Đ | Đ | Đ | Đ |  |
| order-history.jsp | Đ | Đ | Đ | Đ | Ngày ISO cũ giữ nguyên, xem giới hạn |
| robots.jsp | Đ | Đ | Đ | Đ |  |
| task-error.jsp | Đ | Đ | Đ | Đ | GET có chủ đích trả 422 |
| task-list.jsp | Đ | Đ | Đ | Đ |  |
| task-preview.jsp | T | T | T | T | HTML tạm; trạng thái không tới được bằng GET dữ liệu sẵn có |
| task-quiz.jsp | Đ | Đ | Đ | Đ |  |
| task-submit.jsp | Đ | Đ | Đ | Đ |  |
| task-view.jsp | Đ | Đ | Đ | Đ |  |
| wiring-error.jsp | Đ | Đ | Đ | Đ | GET có chủ đích trả 422 |
| wiring-list.jsp | Đ | Đ | Đ | Đ | Có bản ghi DB cũ lỗi dấu ?, không sửa dữ liệu |
| wiring-play.jsp | Đ | Đ | Đ | Đ | Sơ đồ cuộn nội vùng; mã chân phụ; snapshot chỉ đọc |
| wiring-result.jsp | Đ | Đ | Đ | Đ | Sơ đồ cuộn nội vùng; mã chân phụ; snapshot chỉ đọc |
| wiring-support-list.jsp | Đ | Đ | Đ | Đ |  |
| wiring-support-new.jsp | Đ | Đ | Đ | Đ | Sơ đồ cuộn nội vùng; mã chân phụ; snapshot chỉ đọc |
| wiring-support-view.jsp | Đ | Đ | Đ | Đ | Sơ đồ cuộn nội vùng; mã chân phụ; snapshot chỉ đọc |

### Chuỗi cũ → chuỗi mới

Các dấu …/biến chỉ dữ liệu động, không thay dữ liệu DB. Những thay đổi chỉ thêm lớp
nhãn hoặc tách mã chân không đổi lời gốc, được ghi ở bảng vấn đề thay vì liệt kê lặp.

| Trang | Chuỗi cũ | Chuỗi mới |
| --- | --- | --- |
| admin-diagnosis-preview.jsp | Xem trước như User | Xem trước như người học |
| admin-stats.jsp | Tổng số người học (role USER) | Tổng số người học |
| admin-stats.jsp | Đang chuẩn bị (PREPARING) | Đang chuẩn bị |
| admin-stats.jsp | Sẵn sàng (READY) | Sẵn sàng |
| admin-stats.jsp | Đang lắp ráp (IN_PROGRESS) | Đang lắp ráp |
| admin-stats.jsp | Đã hoàn thành (COMPLETED) | Đã hoàn thành |
| admin-stats.jsp | Đã dừng (ABANDONED) | Đã dừng |
| admin-task-form.jsp | Tìm User theo tên | Tìm người học theo tên |
| admin-task-form.jsp | Trang danh sách User | Trang danh sách người học |
| admin-task-form.jsp | Mỗi trang tối đa 20 User. | Mỗi trang tối đa 20 người học. |
| admin-task-form.jsp | Chọn người được giao (chỉ USER) | Chọn người học được giao nhiệm vụ |
| admin-wiring-view.jsp | Xem trước chỉ đọc; không tạo lượt USER. | Xem trước chỉ đọc; không tạo lượt của người học. |
| task-view.jsp | Tìm User theo tên | Tìm người học theo tên |
| task-view.jsp | Trang danh sách User | Trang danh sách người học |
| task-view.jsp | Mỗi trang tối đa 20 User. | Mỗi trang tối đa 20 người học. |
| task-view.jsp | Thêm User | Thêm người học |
| task-view.jsp | Xem trước như User | Xem trước như người học |
| task-view.jsp | Xem trước nội dung cho người học. ADMIN không được nộp bài. | Xem trước nội dung cho người học. Quản trị viên không được nộp bài. |
| wiring-result.jsp | payload gửi lại không được lưu | sơ đồ gửi lại không được lưu |
| wiring-support-view.jsp | User gửi | Người học gửi |
| wiring-support-view.jsp | Admin phản hồi | Quản trị viên phản hồi |
| wiring-support-view.jsp | Nội dung trao đổi (User 20–2000; Admin 10–2000 ký tự) | Nội dung trao đổi (người học 20–2000; quản trị viên 10–2000 ký tự) |
| wiring-support-view.jsp | Đóng hỗ trợ | Đóng yêu cầu |
| task-view.jsp | Mẫu A/B: lắp ráp + quiz + … = 100; làm tròn HALF_UP… | Thang điểm: lắp ráp …, quiz …, [chẩn đoán …,] giải thích 20 — tổng 100. Điểm tự động được làm tròn đến 1 chữ số thập phân; giải thích chi tiết ở Cách tính điểm. |
| admin-task-form.jsp | Nhãn mẫu tiêu chí từ rubric.label | Thang điểm đang chọn: lắp ráp …, quiz …, [chẩn đoán …,] giải thích 20 — tổng 100. |
| admin-task-form.jsp | Mẫu A: lắp ráp 40 + quiz 40 + giải thích 20 = 100 | Mẫu A — lắp ráp 40, quiz 40, giải thích 20; tổng 100. |
| admin-task-form.jsp | Mẫu B: lắp ráp 30 + quiz 25 + chẩn đoán 25 + giải thích 20 = 100 | Mẫu B — lắp ráp 30, quiz 25, chẩn đoán 25, giải thích 20; tổng 100. |
| wiring-result.jsp | Đúng C=…; sai/thừa W=…; thiếu M=…; bắt buộc N=…; công thức và HALF_UP ở phần chính | Dây đúng: … · Dây sai/thừa: … · Còn thiếu: … · Tổng dây bắt buộc: …; công thức đầy đủ trong Cách tính điểm. |
| learning-profile.jsp | làm tròn HALF_UP đến số nguyên. Tốt nhất là tỷ lệ score/tổng câu cao nhất (hòa lấy lượt mới hơn); gần nhất là lượt | làm tròn đến số nguyên (phần thập phân từ 0,5 trở lên được làm tròn lên). Tốt nhất là tỷ lệ câu đúng cao nhất (nếu bằng nhau, lấy lượt mới hơn); gần nhất là lượt |
| assembly-receipt.jsp | Trạng thái hiện tại: mã trạng thái phiên | Trạng thái hiện tại: Đang lắp ráp / Sẵn sàng / Đang chuẩn bị / Đã dừng, theo dữ liệu gốc. |
| admin-diagnosis-preview.jsp | [Chưa có mô tả hậu quả gần nút archive] | Lưu trữ sẽ ẩn nội dung khỏi danh sách mặc định và ngăn bắt đầu lượt mới; lịch sử vẫn được giữ. |
| admin-wiring-view.jsp | [Chưa có mô tả hậu quả gần nút archive] | Lưu trữ sẽ ẩn nội dung khỏi danh sách mặc định và ngăn bắt đầu lượt mới; lịch sử vẫn được giữ. |
| task-view.jsp | [Chưa có mô tả hậu quả gần nút close] | Đóng nhiệm vụ sẽ ngừng nhận bài mới và không thể mở lại. Các bài đang chờ vẫn được chấm. |
| task-view.jsp | [Chưa có mô tả hậu quả gần nút archive] | Lưu trữ sẽ ẩn nhiệm vụ khỏi danh sách mặc định; bài nộp và lịch sử chấm vẫn được giữ. |
| wiring-support-view.jsp | [Chưa có mô tả hậu quả gần nút close] | Đóng yêu cầu sẽ kết thúc trao đổi này. Nếu cần, bạn có thể gửi yêu cầu hỗ trợ mới. |
| admin-stats.jsp | chỉ role &lt;code&gt;ADMIN&lt;/code&gt; | chỉ quản trị viên |
| assembly-receipt.jsp | &lt;code&gt;COMPLETED&lt;/code&gt; và &lt;code&gt;completed_at&lt;/code&gt; | hoàn tất và &lt;code&gt;completed_at&lt;/code&gt; |
| admin-wiring-form.jsp | Mã ổn định (ASCII, số, dấu . _ -) | Mã bài (chữ không dấu, số, dấu . _ -) |
| account.jsp | ADMIN / USER | Quản trị viên / Người học |
| account.jsp | Trang được AccountPageServlet bảo vệ bằng HttpSession… (ở phần đầu) | Thông tin của tài khoản đang đăng nhập. Chi tiết kỹ thuật giữ trong Ghi chú kỹ thuật. |
| admin-wiring-view.jsp | REQUIRED / FORBIDDEN | Bắt buộc / Cặp bị cấm |
| task-view.jsp | [Chưa có mô tả hậu quả cạnh nút xoá nháp] | Xóa nháp sẽ bỏ nội dung chưa công bố này. Hãy kiểm tra trước khi xác nhận. |

### PDF và bằng chứng

- Chrome A4 từ phiên builder thật: hồ sơ trước/sau 3 trang; phiếu lắp ráp trước 2,
  sau 1 trang. Đã render và xem ảnh tất cả trang; dữ liệu, kỹ năng/căn cứ/xác nhận
  và miễn trừ còn đủ. Hồ sơ trắng/đen, không header/menu/toolbar; số trang 1/3–3/3.
  Phiếu giữ bảng và miễn trừ, ẩn link điều hướng khi in, không có trang gần trống.
- thead/CSS in được giữ; dữ liệu hiện có chưa ép bảng nhiều trang để kiểm header lặp
  trực tiếp. Phiếu còn nền panel rất nhạt của CSS in cũ; không mang theme charcoal vào PDF.
- Bằng chứng ngoài Git: `D:/Web-Pro/qa-evidence/ui2/`. `before/` và `after/` chứa
  HTML/token chuẩn hóa và manifest; `comparison.json` chứa kết quả form/dữ liệu/HTTP/SQL.
- `before-screens/`, `after-screens/`: ảnh full-page mọi view ở năm độ rộng, hai PDF
  và PNG từng trang. `contact-sheets/`: ảnh tổng hợp đối chiếu. Ảnh mô phỏng được
  đánh dấu ở tiêu đề; hai ảnh stress nội dung chỉ sửa DOM, không phải dữ liệu đã lưu.
- `before-viewport.json`, `after-viewport.json`, `static-viewport.json`, `audit-report.json`,
  `checks.json`, `pilot-interactions.json`, `extra-layout.json`: kết quả browser/GET.
  Cookie và mật khẩu chỉ ở bộ nhớ, không nằm trong evidence hay commit.

### Giữ nguyên có lý do và phần chưa kiểm

- Java/SQL tuyệt đối không đổi. Những ngày String ISO có sẵn ở tài khoản/phiếu/đơn
  giữ nguyên: không có getter định dạng Việt tương ứng để dùng mà không sửa Java;
  không thêm fmt/JS chuyển giá trị. Các trang mới hơn dùng getter ngày Việt sẵn có.
- `architecture`, `robots`, `components`, ghi chú kỹ thuật trong details giữ Servlet,
  JDBC và tên bảng để phục vụ môn học. fmt/fn cũ ở view thông tin không được nhân rộng.
- Cặp kết quả nối dây không có boolean đúng/thiếu riêng: chỉ chọn màu bằng nhãn kết
  quả đã có (Đúng/Sai-thừa), dùng getter forbidden khi có; không chấm lại ở JSP.
  Phiên READY không có isReady: giữ so sánh trạng thái sẵn có để dịch nhãn, không sửa bean.
- SnapshotText hỗ trợ bất biến là một chuỗi server đã lưu; giữ nguyên mã chân trong
  bản chụp, không parse hoặc đổi lịch sử. Trên sơ đồ và radio, mã đã tách thành chữ phụ.
- Các tiêu đề QA chứa mã thô/HTML dạng chữ là dữ liệu có sẵn, không đổi. Bài nối dây
  #1/#2 có title/objective/scope_text lỗi dấu ?, #7 có scope_text sao chép lỗi này;
  còn lời giải/nhãn của bản cũ cũng có thể lỗi. Đây là tồn đọng dữ liệu trước UI-2,
  không phải do Poppins/JSP mới. Cần quyết định sửa dữ liệu riêng; không tuyên bố đã sửa.
- task-preview không GET tới được; diagnosis-play không có lượt IN_PROGRESS sẵn có.
  Hai trang chỉ kiểm layout tĩnh/contract nguồn trong UI-2; không thực thi JSP bằng
  trạng thái thật hoặc thử nộp/chốt. Admin còn lại đã kiểm bằng server thật.
- CSS zoom 200% headless, chưa kiểm Ctrl+Plus thực; chưa kiểm Firefox/Safari,
  cảm ứng vật lý, screen reader, Tab từng control trên tất cả form hoặc chứng nhận WCAG.
- Không kiểm lại concurrency/vòng nộp/chấm/3D/shop ghi dữ liệu vì QA lần này chỉ đọc.
  JS-off kiểm thấy radio/form đầy đủ, không gửi form; không coi đó là kiểm lại POST.
- Không tạo tài khoản/bản ghi QA mới. Bản ghi cũ giữ nguyên hash; runtime QA riêng
  được dừng và dọn sau kiểm chứng, cổng 8081 đóng; không dừng Tomcat 8080.

Mốc code UI-2: `6b07f1b` (`fix: tidy workspace navigation and learner-facing copy`).
Tài liệu/checklist được lưu bằng commit local tiếp theo; không push, handoff giữ untracked.

Trong lúc thực hiện, Git có thêm năm commit tài liệu đến `d41c5f1`, origin cũng ở mốc này.
Đối chiếu `7d27f18..d41c5f1` gồm README, ARCHITECTURE, TEAM_FLOW_DEMO_GUIDE và hai
tài liệu ôn tập; giữ nguyên, không đưa vào commit UI-2.
Các golden/test ứng dụng vẫn áp dụng vì code ứng dụng của khoảng này không đổi.
Không reset/sửa lịch sử hoặc push để khớp ghi chú ahead ban đầu.

### Danh sách toàn bộ chuỗi tĩnh đã trích để rà

Trích nút văn bản hiển thị và title của 34 JSP + wiring-diagram + hai fragment shell.
EL/c:out thay bằng [dữ liệu]; giữ cả nhánh điều kiện nên hai nhãn cạnh nhau trong danh
sách không có nghĩa cùng xuất hiện lúc chạy. Bỏ comment/directive và không đưa value
ẩn/credential/dữ liệu DB vào danh sách. Đã đọc từng nhóm chuỗi, không dùng bộ sửa DB.
Các mảnh dấu câu riêng được giữ để danh sách không làm mất nội dung nguồn.

<details>
<summary>TRƯỚC: 793 mảnh văn bản tĩnh</summary>

#### account.jsp

1. Tài khoản | Robot Assembly Lab
2. Tài khoản thực hành
3. Trang được
4. AccountPageServlet
5. bảo vệ bằng
6. HttpSession
7. , sau đó controller đặt JavaBean
8. user
9. vào request và forward đến JSP.
10. Họ tên
11. Email
12. Vai trò
13. Ngày tạo
14. Quản lý tài khoản
15. Về trang chủ

#### admin-diagnosis-form.jsp

1. Biên soạn chẩn đoán | Robot Assembly Lab
2. ← Tài khoản
3. Tạo / sửa nháp tình huống
4. Bốn ô cố định cho mỗi nhóm. Ô nhãn trống được bỏ qua. Công bố cần 2–4 phép kiểm tra, ít nhất một phép cần thiết, 3–4 nguyên nhân và 3–4 biện pháp; mỗi nhóm lựa chọn có đúng một đáp án.
5. Tiêu đề
6. Bối cảnh
7. Triệu chứng
8. Giải thích sau khi chốt
9. Robot
10. Hướng dẫn liên quan (cùng robot hoặc dùng chung)
11. Không chọn
12. Phép kiểm tra
13. Ô [dữ liệu]
14. Tên phép kiểm tra
15. Quan sát
16. Cần thiết
17. Nguyên nhân
18. Ô [dữ liệu]
19. Nhãn
20. Phản hồi sau khi chốt
21. Đáp án đúng
22. Biện pháp
23. Ô [dữ liệu]
24. Nhãn
25. Phản hồi sau khi chốt
26. Đáp án đúng
27. Lưu nháp

#### admin-diagnosis-list.jsp

1. Quản lý chẩn đoán | Robot Assembly Lab
2. ← Tài khoản
3. Quản lý tình huống chẩn đoán
4. Tạo nháp tình huống
5. [dữ liệu] · [dữ liệu] · Phiên bản [dữ liệu]

#### admin-diagnosis-preview.jsp

1. Xem trước chẩn đoán | Robot Assembly Lab
2. ← Tài khoản
3. [dữ liệu] · [dữ liệu] · Phiên bản [dữ liệu]
4. Xem trước như User, chỉ đọc. Không tạo lượt hoặc ghi phép kiểm tra.
5. Phép kiểm tra
6. Cần thiết Không liên quan
7. Nguyên nhân / biện pháp
8. Đáp án đúng Đáp án sai
9. Quan sát là dữ liệu mô phỏng của bài tập, không phải phép đo từ robot thật.
10. Giải thích sau khi chốt
11. Xem trước như User
12. Sửa nháp
13. Công bố
14. Lưu trữ
15. Nhân bản thành nháp mới
16. Danh sách tình huống

#### admin-stats.jsp

1. Thống kê quản trị | Robot Assembly Lab
2. Thống kê quản trị
3. Phiên lắp ráp theo trạng thái
4. Tổng [dữ liệu] phiên trên toàn hệ thống
5. Trạng thái
6. Số phiên
7. Đang chuẩn bị (PREPARING)
8. Sẵn sàng (READY)
9. Đang lắp ráp (IN_PROGRESS)
10. Đã hoàn thành (COMPLETED)
11. Đã dừng (ABANDONED)
12. Tổng số người học (role USER)
13. Số người có hoạt động
14. [dữ liệu] (đã có ít nhất một phiên lắp ráp hoặc một lượt làm bài kiểm tra)
15. Mẫu robot được chọn nhiều nhất
16. Chưa có phiên lắp ráp nào trong hệ thống.
17. Xếp hạng theo số phiên lắp ráp đã tạo
18. Mẫu robot
19. Số phiên
20. Kết quả kiểm tra tổng hợp
21. Chưa có lượt làm bài kiểm tra nào trong hệ thống.
22. Điểm trung bình tính theo phần trăm số câu đúng trên mỗi lượt làm bài
23. Mẫu robot
24. Số lượt làm bài
25. Điểm trung bình
26. [dữ liệu] %
27. Câu hỏi có tỷ lệ trả lời sai cao
28. Chưa đủ dữ liệu (cần ít nhất [dữ liệu] lượt trả lời cho một câu hỏi mới đưa vào thống kê, để tránh một lượt làm sai duy nhất bị tính thành &quot;sai 100%&quot;).
29. Chỉ tính câu hỏi đã có từ [dữ liệu] lượt trả lời trở lên
30. Câu hỏi
31. Số lượt trả lời
32. Tỷ lệ sai
33. [dữ liệu] %
34. Về quản trị nội dung
35. ·
36. Về trang chủ
37. Ghi chú kỹ thuật
38. Trang do
39. AdminStatsServlet
40. dựng ở server (chỉ role
41. ADMIN
42. xem được, tài khoản thường bị từ chối với mã 403): mọi số liệu đọc trực tiếp từ MySQL qua
43. StatsDB
44. bằng
45. GROUP BY
46. /
47. COUNT DISTINCT
48. ngay trong câu SQL — không cộng dồn ở tầng Java — để tránh đếm trùng khi có JOIN. Không có số liệu nào ở đây được bịa ra để biểu đồ đẹp hơn.

#### admin-task-form.jsp

1. Biên soạn nhiệm vụ | Robot Assembly Lab
2. ← Tài khoản
3. Tạo / sửa nháp nhiệm vụ
4. Tìm User theo tên
5. Trang danh sách User
6. Tìm
7. Mỗi trang tối đa 20 User. Người đã được giao vẫn được giữ trong nháp; bỏ chọn để gỡ.
8. Mẫu tiêu chí cố định
9. Tình huống chẩn đoán (bắt buộc với mẫu B, cùng robot)
10. Không dùng (mẫu A)
11. [dữ liệu] · [dữ liệu]
12. Tiêu đề
13. Mô tả
14. Mẫu robot
15. Hạn nộp (giờ Việt Nam)
16. Chính sách muộn
17. Không nhận bài quá hạn
18. Nhận và gắn cờ nộp muộn
19. Số lần nộp tối đa
20. Ngưỡng đạt
21. Cho phép phiên lắp ráp hoàn thành trước vòng 1
22. [dữ liệu] .
23. Chọn người được giao (chỉ USER)
24. Lưu nháp

#### admin-task-list.jsp

1. Quản lý nhiệm vụ | Robot Assembly Lab
2. ← Tài khoản
3. Quản lý nhiệm vụ
4. Tạo nháp mới
5. Lọc trạng thái
6. Danh sách mặc định
7. Nháp
8. Đang mở
9. Đã đóng
10. Đã lưu trữ
11. Lọc
12. Chưa có nhiệm vụ.
13. [dữ liệu] · [dữ liệu] · Hạn nộp [dữ liệu]

#### admin-task-review.jsp

1. Đánh giá bài nộp | Robot Assembly Lab
2. ← Tài khoản
3. Chấm bài: [dữ liệu]
4. Người nộp: [dữ liệu] · Đang chấm lần nộp # [dữ liệu]
5. Không điều chỉnh điểm tự động. Mỗi lần chấm lưu một bản ghi mới; sửa chấm phải có lý do và vòng sau chưa có hoạt động.
6. Tổng quan nhiệm vụ
7. Mức giải thích
8. 0 điểm — Không có nội dung hoặc không liên quan
9. 5 điểm — Kể lại thao tác nhưng chưa giải thích
10. 10 điểm — Giải thích đúng một phần, còn thiếu căn cứ
11. 15 điểm — Lập luận đúng, liên hệ với kết quả quan sát
12. 20 điểm — Lập luận rõ, có căn cứ và đề xuất cải thiện hợp lý
13. Kết luận
14. Đạt yêu cầu
15. Cần bổ sung
16. Chưa đạt yêu cầu
17. Cần gia hạn trước nếu muốn yêu cầu bổ sung khi đã quá hạn và chính sách không nhận bài muộn. Nhiệm vụ đã đóng chỉ được kết luận Đạt / Chưa đạt.
18. Điểm mạnh
19. Cần cải thiện (bắt buộc với Cần bổ sung / Chưa đạt)
20. Hướng làm lại (bắt buộc với Cần bổ sung / Chưa đạt)
21. Lý do sửa chấm (bắt buộc khi đã chấm)
22. Lưu lần chấm
23. Quá trình chẩn đoán
24. Điểm thu thập quan sát: [dữ liệu] /4
25. Thu thập đủ quan sát cần thiết: [dữ liệu] / [dữ liệu] phép · Điểm: [dữ liệu] /10
26. Đã kiểm tra lúc [dữ liệu]
27. Phép cần thiết còn thiếu
28. Đã thu thập đủ quan sát cần thiết.
29. Nguyên nhân đã chọn
30. [dữ liệu] — Đúng (3/3) Sai (0/3)
31. Cách xử lý đã chọn
32. [dữ liệu] — Đúng (3/3) Sai (0/3)
33. Giải thích tình huống
34. Quan sát là dữ liệu mô phỏng của bài tập, không phải phép đo từ robot thật.
35. Bằng chứng và lịch sử nộp / chấm
36. Lần nộp # [dữ liệu]
37. · Nộp muộn
38. Phiên lắp ráp # [dữ liệu] · [dữ liệu] · hoàn thành [dữ liệu]
39. Quiz lượt # [dữ liệu] : [dữ liệu] / [dữ liệu] · [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] )
40. Tình huống: [dữ liệu]
41. Chẩn đoán lượt # [dữ liệu] – [dữ liệu] /10 – lúc [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
42. Đóng góp chẩn đoán: [dữ liệu] / [dữ liệu]
43. Lắp ráp: [dữ liệu] / [dữ liệu] · Quiz: [dữ liệu] / [dữ liệu]
44. Điểm tự động: [dữ liệu] / [dữ liệu]
45. — chờ đánh giá phần giải thích
46. Bạn gặp vấn đề gì?
47. Vì sao chọn cách kiểm tra/xử lý đó?
48. Nếu làm lại, bạn sẽ thay đổi điều gì?
49. Chấm / xem lịch sử chấm bài này
50. [dữ liệu] · Tổng: [dữ liệu] /100
51. · Giải thích: [dữ liệu] /20 · [dữ liệu]
52. Người chấm: [dữ liệu] · [dữ liệu]
53. Điểm mạnh: [dữ liệu]
54. Cần cải thiện: [dữ liệu]
55. Hướng làm lại: [dữ liệu]
56. Đã được sửa lúc [dữ liệu] – lý do: [dữ liệu]

#### admin-wiring-form.jsp

1. Biên soạn bài nối dây | Robot Assembly Lab
2. Tài khoản
3. Quản lý bài nối dây
4. Biên soạn bài nối dây nháp
5. Số ô cố định: tối đa 40 đầu nối, 80 quy tắc. Bỏ trống mã đầu nối/cả hai ô quy tắc để bỏ qua ô đó.
6. Mã ổn định (ASCII, số, dấu . _ -)
7. Tên bài
8. Robot
9. Mục tiêu
10. Phạm vi mô phỏng (ghi rõ phần không chấm)
11. Đầu nối
12. Ô đầu nối [dữ liệu]
13. Mã riêng (ví dụ line-left.OUT)
14. Mã thiết bị
15. Tên thiết bị
16. Nhãn chân
17. Tọa độ X (40–960)
18. Tọa độ Y (40–960)
19. Quy tắc (đầu A/B là số ô đầu nối bên trên)
20. Quy tắc [dữ liệu]
21. Ô đầu nối A
22. Ô đầu nối B
23. Bắt buộc
24. Bị cấm
25. Giải thích đúng/thiếu hoặc lý do cấm
26. Lưu nháp

#### admin-wiring-list.jsp

1. Quản lý bài nối dây | Robot Assembly Lab
2. Tài khoản
3. Quản lý bài nối dây
4. Quản lý bài nối dây
5. Tạo bài nháp
6. [dữ liệu] · [dữ liệu]
7. Chi tiết / quản lý

#### admin-wiring-view.jsp

1. Chi tiết bài nối dây | Robot Assembly Lab
2. Tài khoản
3. Quản lý bài nối dây
4. [dữ liệu] · [dữ liệu]
5. Quy tắc biên soạn
6. # [dữ liệu] · # [dữ liệu] · [dữ liệu] : [dữ liệu]
7. Xem trước chỉ đọc; không tạo lượt USER.
8. Xem trước như người học
9. Sửa nháp và đầu nối/quy tắc
10. Công bố và khóa nội dung
11. Lưu trữ
12. Nhân bản thành nháp mới
13. Danh sách bài

#### architecture.jsp

1. Luồng Servlet/JSP | Robot Assembly Lab
2. Luồng Servlet/JSP của Robot Assembly Lab
3. Trang này do
4. đặt dữ liệu vào request rồi
5. forward()
6. sang JSP. JSP chỉ hiển thị, không truy vấn MySQL.
7. Browser (view: HTML/JSP) → [dữ liệu] (controller: servlet) → JavaBean trong package business (model) → [dữ liệu] (data access layer) → MySQL JSON contract: [dữ liệu]
8. Mở response JSON health

#### assembly-receipt.jsp

1. Phiếu kết quả lắp ráp | Robot Assembly Lab
2. Phiếu kết quả lắp ráp
3. Phiên lắp ráp này chưa hoàn tất nên chưa có phiếu kết quả. Trạng thái hiện tại:
4. .
5. Vào phòng lắp ráp 3D để tiếp tục →
6. Đã hoàn tất lắp ráp trong mô hình 3D
7. · [dữ liệu]
8. Mã phiên
9. Mẫu robot
10. Bắt đầu phiên
11. Hoàn tất lắp ráp
12. Kết quả
13. Đã lắp đủ toàn bộ [dữ liệu] nhóm linh kiện bắt buộc trong mô hình 3D.
14. Linh kiện bắt buộc của mẫu robot này (bảng
15. robot_components
16. )
17. Mã linh kiện
18. Tên linh kiện
19. Số lượng yêu cầu
20. Đã lắp trong mô hình 3D
21. ✓ Đã lắp
22. &quot;Hoàn tất&quot; ở đây có nghĩa là đã hoàn tất bài lắp ráp trong phạm vi mô phỏng của website (đủ mọi linh kiện bắt buộc trong mô hình 3D); phiếu này không khẳng định robot thật đã được đấu nối điện hoặc vận hành đúng.
23. Làm bài kiểm tra kiến thức →
24. ·
25. Thực hành lại mẫu này →
26. ·
27. Tra cứu lỗi lắp ráp →
28. ·
29. Xem lịch sử tài khoản
30. Về trang chủ
31. Ghi chú kỹ thuật
32. Trang do
33. AssemblyReceiptPageServlet
34. dựng ở server: đọc phiên theo
35. (id, user_id)
36. qua
37. AssemblySessionDB.selectSession()
38. nên chỉ chủ phiên mới xem được, đặt robot và danh sách linh kiện vào request bằng
39. setAttribute
40. rồi
41. forward()
42. sang JSP này. Trạng thái
43. COMPLETED
44. và
45. completed_at
46. chỉ được
47. AssemblySessionDB.completeSession()
48. ghi sau khi đối chiếu tập hợp linh kiện bắt buộc với dữ liệu thật trong
49. session_visual_parts
50. — trang này không tin số phần trăm do trình duyệt tự tính.

#### components.jsp

1. Danh mục linh kiện (JSP) | Robot Assembly Lab
2. Danh mục linh kiện
3. Tổng số bản ghi trong bảng
4. components
5. :
6. · đang xem trang [dữ liệu]
7. Dữ liệu đọc trực tiếp từ MySQL qua JDBC
8. Ảnh
9. ID
10. Tên linh kiện
11. Danh mục
12. Mô tả
13. ← Trang trước
14. Trang sau →
15. Bảng
16. components
17. chưa có dữ liệu. Hãy chạy
18. database/seed.sql
19. .
20. Danh mục robot (JSP)
21. ·
22. Sơ đồ kiến trúc
23. ·
24. Về trang chủ
25. Ghi chú kỹ thuật
26. Trang do
27. ComponentCatalogPageServlet
28. dựng ở server: tham số
29. ?page=
30. được đọc từ
31. HttpServletRequest
32. ,
33. ComponentDB.selectComponents()
34. lấy kết nối từ
35. ConnectionPool
36. và chạy
37. PreparedStatement
38. trên MySQL, kết quả đi qua
39. setAttribute
40. rồi
41. forward()
42. tới JSP.

#### diagnosis-list.jsp

1. Luyện chẩn đoán | Robot Assembly Lab
2. ← Tài khoản
3. Luyện chẩn đoán
4. Chọn phép kiểm tra để thu thập quan sát mô phỏng, rồi kết luận nguyên nhân và cách xử lý.
5. Lượt luyện không tính vào nhiệm vụ. Muốn làm bài được giao, hãy bắt đầu từ trang nhiệm vụ.
6. Đang làm dở
7. Chưa có lượt đang làm.
8. Lượt # [dữ liệu] · [dữ liệu]
9. Tình huống đang công bố
10. [dữ liệu] · Phiên bản [dữ liệu]
11. Bắt đầu luyện

#### diagnosis-play.jsp

1. Thực hành chẩn đoán | Robot Assembly Lab
2. ← Tài khoản
3. Lượt # [dữ liệu] · [dữ liệu]
4. Quan sát là dữ liệu mô phỏng của bài tập, không phải phép đo từ robot thật.
5. Chọn phép kiểm tra
6. Đã kiểm tra lúc [dữ liệu]
7. Thực hiện phép kiểm tra
8. Kết luận
9. Thu thập đủ quan sát cần thiết: 4 điểm; xác định nguyên nhân: 3; chọn cách xử lý: 3.
10. Có thể kết luận khi chưa kiểm tra; điểm thu thập quan sát khi đó bằng 0. Kết luận chỉ được chốt một lần.
11. Nguyên nhân
12. Cách xử lý
13. Chốt kết luận
14. Nhiệm vụ của tôi
15. ·
16. Danh sách luyện

#### diagnosis-result.jsp

1. Kết quả chẩn đoán | Robot Assembly Lab
2. ← Tài khoản
3. Kết quả: [dữ liệu]
4. Lượt # [dữ liệu] · Chốt lúc [dữ liệu]
5. Quá trình chẩn đoán
6. Thu thập đủ quan sát cần thiết: [dữ liệu] / [dữ liệu] phép · [dữ liệu] /4
7. Tổng điểm chẩn đoán: [dữ liệu] /10
8. Đã kiểm tra lúc [dữ liệu]
9. Phép cần thiết còn thiếu
10. Đã thu thập đủ quan sát cần thiết.
11. Nguyên nhân đã chọn
12. [dữ liệu] — Đúng (3/3) Sai (0/3)
13. Cách xử lý đã chọn
14. [dữ liệu] — Đúng (3/3) Sai (0/3)
15. Giải thích tình huống
16. Quan sát là dữ liệu mô phỏng của bài tập, không phải phép đo từ robot thật.
17. Xem hướng dẫn tra cứu liên quan
18. Tra cứu hướng dẫn theo mẫu robot
19. Nhiệm vụ của tôi
20. ·
21. Danh sách luyện

#### learning-profile.jsp

1. Hồ sơ học tập - [dữ liệu] | Robot Assembly Lab
2. ← Quay lại tài khoản
3. In / Lưu PDF
4. Trong hộp thoại in, chọn Lưu dưới dạng PDF, khổ A4; nên tắt Đầu trang và chân trang để tránh thêm ngày giờ và tiêu đề của trình duyệt.
5. Bạn có thể dùng Ctrl+P (Windows/Linux) hoặc Command+P (macOS) để in hoặc lưu PDF.
6. Robot Assembly Lab
7. Hồ sơ học tập Robot Assembly Lab
8. Họ tên:
9. Lập lúc [dữ liệu]
10. Lớp/MSSV: ........
11. Tổng quan kết quả
12. Mẫu robot
13. Lượt làm bài
14. Mẫu đã làm bài
15. [dữ liệu] / [dữ liệu]
16. Điểm trung bình
17. Tình trạng chung
18. Điểm trung bình là trung bình cộng tỷ lệ phần trăm của lượt tốt nhất ở từng mẫu đã làm bài; làm tròn HALF_UP đến số nguyên. Tốt nhất là tỷ lệ score/tổng câu cao nhất (hòa lấy lượt mới hơn); gần nhất là lượt có thời điểm nộp mới nhất (hòa lấy mã lượt lớn hơn).
19. Kết quả theo từng mẫu robot
20. Chưa có dữ liệu trong danh mục robot.
21. Trạng thái
22. Hoàn thành
23. [dữ liệu] · phiên # [dữ liệu] · [dữ liệu] lần
24. Phiên gần nhất
25. [dữ liệu] · cập nhật [dữ liệu]
26. Phiên lắp ráp
27. Chưa có dữ liệu
28. Quiz tốt nhất
29. [dữ liệu] / [dữ liệu] · [dữ liệu]
30. Quiz gần nhất
31. [dữ liệu] / [dữ liệu] · [dữ liệu]
32. Số lượt quiz
33. Quiz
34. Chưa có dữ liệu
35. Kỹ năng và kiến thức đã thực hành
36. Các nội dung được ghi nhận từ mẫu đã hoàn thành và lượt quiz hiện có
37. Nội dung đã thực hành
38. Căn cứ ghi nhận
39. Nhận xét/xác nhận của giảng viên
40. Họ tên giảng viên:
41. Ngày:
42. Chữ ký:
43. Hoàn tất/kết quả trong hồ sơ này là kết quả mô phỏng trong phạm vi website, không khẳng định robot thật đã được đấu nối hay vận hành đúng, và không thay thế đánh giá của giảng viên.
44. Thời điểm lập: [dữ liệu]
45. Luồng trong mã:
46. LearningProfileServlet → StatsDB/RobotDB → LearningProfile → JSP
47. . Bộ đếm trang ở chân giấy phụ thuộc hỗ trợ margin box của trình duyệt. Nên tắt Đầu trang và chân trang; nếu trình duyệt không hiện số trang do CSS tạo và bạn cần số trang, có thể bật lại tùy chọn này, nhưng bản in sẽ có thêm ngày giờ, tiêu đề hoặc URL do trình duyệt tạo, theo định dạng của trình duyệt.

#### learning-summary.jsp

1. Tổng kết học tập | Robot Assembly Lab
2. Tổng kết học tập
3. Tài khoản
4. [dữ liệu] ( [dữ liệu] )
5. Tổng số phiên lắp ráp
6. Số phiên đã hoàn thành
7. Đang chuẩn bị / sẵn sàng
8. Đang lắp ráp
9. Đã dừng
10. Mẫu robot đã hoàn tất
11. Bạn chưa hoàn tất mẫu robot nào. Hãy vào phòng lắp ráp 3D và hoàn tất một phiên để xem tại đây.
12. Mẫu robot đã từng lắp ráp xong ít nhất một lần
13. Mẫu robot
14. Độ khó
15. Mô tả
16. Điểm kiểm tra kiến thức
17. Bạn chưa làm bài kiểm tra nào. Vào trang lắp ráp của một mẫu robot để bắt đầu.
18. Điểm tốt nhất và gần nhất theo từng mẫu robot đã từng làm bài
19. Mẫu robot
20. Điểm tốt nhất
21. Điểm gần nhất
22. Số lần làm bài
23. [dữ liệu] / [dữ liệu]
24. [dữ liệu] / [dữ liệu]
25. Hồ sơ học tập (in/PDF)
26. ·
27. Xem lịch sử phiên và phiếu kết quả
28. ·
29. Tra cứu lỗi lắp ráp
30. ·
31. Về trang chủ
32. Ghi chú kỹ thuật
33. Trang do
34. LearningSummaryServlet
35. dựng ở server: đọc số liệu của đúng tài khoản đang đăng nhập qua
36. StatsDB
37. (dùng
38. GROUP BY
39. và
40. COUNT DISTINCT
41. ngay trong SQL để không đếm trùng), đặt vào request bằng
42. setAttribute
43. rồi
44. forward()
45. sang JSP này. Không có tài khoản nào xem được số liệu của tài khoản khác.

#### order-history.jsp

1. Lịch sử đơn mô phỏng | Robot Assembly Lab
2. ← Quay lại cửa hàng
3. Lịch sử đơn mô phỏng
4. Đây là các đơn học tập của [dữ liệu] . Đơn chỉ ghi nhận dữ liệu trong bài, không có thanh toán hoặc giao hàng thật.
5. Bạn chưa có đơn nào. Hãy xem giá tham khảo của linh kiện trong cửa hàng.
6. Đơn # [dữ liệu]
7. Trạng thái: [dữ liệu] · [dữ liệu]
8. Các mặt hàng đã được chụp thông tin tại lúc xác nhận
9. Linh kiện
10. Đơn giá
11. Số lượng
12. Thành tiền
13. đ
14. đ
15. Tổng mô phỏng:
16. đ
17. ← Mới hơn
18. Trang [dữ liệu] / [dữ liệu]
19. Cũ hơn →
20. Giỏ hàng
21. ·
22. Trang chủ

#### robots.jsp

1. Danh mục robot (JSP) | Robot Assembly Lab
2. Danh mục mô hình robot
3. Tổng số bản ghi trong bảng
4. robots
5. :
6. · đang xem trang [dữ liệu]
7. Dữ liệu đọc trực tiếp từ MySQL qua JDBC
8. ID
9. Tên mô hình
10. Độ khó
11. Cảm biến chính
12. Thời gian lắp
13. Bước lắp ráp
14. Xem JSON
15. ← Trang trước
16. Trang sau →
17. Bảng
18. robots
19. chưa có dữ liệu. Hãy chạy
20. database/seed.sql
21. .
22. Danh mục linh kiện (JSP)
23. ·
24. Sơ đồ kiến trúc
25. ·
26. Về trang chủ
27. Ghi chú kỹ thuật
28. Trang do
29. RobotCatalogPageServlet
30. dựng ở server:
31. doGet()
32. gọi
33. RobotDB.selectRobots()
34. →
35. ConnectionPool
36. → MySQL, đặt danh sách vào request bằng
37. setAttribute(&quot;robots&quot;, ...)
38. rồi
39. forward()
40. sang JSP này. JSP chỉ hiển thị, không mở kết nối database.

#### task-error.jsp

1. Thông báo nhiệm vụ | Robot Assembly Lab
2. ← Tài khoản
3. Chưa thực hiện được thao tác
4. Vui lòng quay lại nhiệm vụ để kiểm tra vòng hiện tại và điều kiện nộp.
5. Quản lý nhiệm vụ
6. Nhiệm vụ của tôi

#### task-list.jsp

1. Nhiệm vụ của tôi | Robot Assembly Lab
2. ← Tài khoản
3. Nhiệm vụ của tôi
4. Chỉ hiển thị nhiệm vụ được giao cho tài khoản hiện tại.
5. Chưa có nhiệm vụ.
6. [dữ liệu] · [dữ liệu] · Hạn nộp [dữ liệu]

#### task-preview.jsp

1. Xem trước bài nộp | Robot Assembly Lab
2. ← Tài khoản
3. Xem trước bài nộp
4. Phiên lắp ráp # [dữ liệu] , hoàn thành lúc [dữ liệu]
5. Quiz lượt # [dữ liệu] – [dữ liệu] / [dữ liệu] – [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] )
6. Tình huống: [dữ liệu]
7. Chẩn đoán lượt # [dữ liệu] – [dữ liệu] /10 – lúc [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
8. Điểm tự động dự kiến: [dữ liệu] / [dữ liệu] — chờ đánh giá phần giải thích
9. Nộp muộn
10. Giải thích
11. Bạn gặp vấn đề gì?
12. Vì sao chọn cách kiểm tra/xử lý đó?
13. Nếu làm lại, bạn sẽ thay đổi điều gì?
14. Tình huống: [dữ liệu]
15. Xác nhận nộp bài
16. Quay lại chọn bằng chứng

#### task-quiz.jsp

1. Quiz nhiệm vụ | Robot Assembly Lab
2. ← Tài khoản
3. Quiz nhiệm vụ: [dữ liệu]
4. Chỉ lượt hợp lệ đầu tiên của mỗi vòng được chốt tính điểm. Gửi thiếu câu hoặc lựa chọn không thuộc câu sẽ không lưu lượt.
5. Chốt lượt tính điểm của vòng này
6. Lượt tính điểm của vòng này đã chốt hoặc vòng không còn nhận quiz.
7. Quiz lượt # [dữ liệu] – [dữ liệu] / [dữ liệu] – [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
8. Kết quả lượt đã chốt
9. Bạn chọn: [dữ liệu]
10. Đáp án: [dữ liệu]
11. Trở về nhiệm vụ
12. Làm quiz luyện tập

#### task-submit.jsp

1. Nộp bài nhiệm vụ | Robot Assembly Lab
2. ← Tài khoản
3. Nộp bài: [dữ liệu]
4. Quiz lượt # [dữ liệu] – [dữ liệu] / [dữ liệu] – [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
5. Chẩn đoán lượt # [dữ liệu] – [dữ liệu] /10 – lúc [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
6. Tiếp tục chẩn đoán Bắt đầu chẩn đoán cho nhiệm vụ này
7. Chọn một phiên của chính bạn đã hoàn thành đúng mẫu. Mốc bằng chứng là lúc bắt đầu vòng 1; các vòng sau có thể dùng lại phiên này nếu hợp lệ.
8. Chưa có phiên lắp ráp phù hợp. Hãy hoàn thành mẫu robot rồi quay lại.
9. Bằng chứng lắp ráp
10. Phiên # [dữ liệu] · [dữ liệu] · hoàn thành [dữ liệu]
11. Bạn gặp vấn đề gì?
12. Vì sao chọn cách kiểm tra/xử lý đó?
13. Nếu làm lại, bạn sẽ thay đổi điều gì?
14. Xem trước bài nộp

#### task-view.jsp

1. Chi tiết nhiệm vụ | Robot Assembly Lab
2. ← Tài khoản
3. Mẫu: [dữ liệu] · [dữ liệu]
4. Hạn nộp: [dữ liệu] · [dữ liệu]
5. Tối đa [dữ liệu] lần nộp · Ngưỡng đạt [dữ liệu] /100.
6. [dữ liệu] . Điểm tự động làm tròn HALF_UP đến 1 chữ số thập phân; tổng điểm dùng điểm tự động đã lưu.
7. Xem trước nội dung cho người học. ADMIN không được nộp bài.
8. Xem trước như User
9. ·
10. Danh sách nhiệm vụ
11. Sửa nháp và người được giao
12. Công bố
13. Xoá nháp
14. Tìm User theo tên
15. Trang danh sách User
16. Tìm
17. Mỗi trang tối đa 20 User. Người đã được giao vẫn được giữ trong nháp; bỏ chọn để gỡ.
18. Thêm người được giao
19. Thêm User
20. Hạn mới (giờ Việt Nam)
21. Gia hạn
22. Đóng nhiệm vụ
23. Lưu trữ
24. Nhân bản thành nháp mới
25. Người được giao và kết quả mới nhất
26. Họ tên
27. Trạng thái
28. Điểm
29. [dữ liệu] · Nộp muộn
30. [dữ liệu] /100 [dữ liệu] / [dữ liệu] — chờ đánh giá phần giải thích
31. Vòng [dữ liệu] · bắt đầu [dữ liệu]
32. Quiz lượt # [dữ liệu] – [dữ liệu] / [dữ liệu] – [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
33. Chẩn đoán lượt # [dữ liệu] – [dữ liệu] /10 – lúc [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
34. Tiếp tục chẩn đoán Bắt đầu chẩn đoán cho nhiệm vụ này
35. Làm quiz cho nhiệm vụ này
36. Chọn bằng chứng và nộp bài
37. Quiz luyện tập (không thay lượt tính điểm)
38. Lịch sử các vòng nộp và chấm
39. Chưa có bài nộp.
40. Lần nộp # [dữ liệu]
41. · Nộp muộn
42. Phiên lắp ráp # [dữ liệu] · [dữ liệu] · hoàn thành [dữ liệu]
43. Quiz lượt # [dữ liệu] : [dữ liệu] / [dữ liệu] · [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] )
44. Tình huống: [dữ liệu]
45. Chẩn đoán lượt # [dữ liệu] – [dữ liệu] /10 – lúc [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
46. Đóng góp chẩn đoán: [dữ liệu] / [dữ liệu]
47. Lắp ráp: [dữ liệu] / [dữ liệu] · Quiz: [dữ liệu] / [dữ liệu]
48. Điểm tự động: [dữ liệu] / [dữ liệu]
49. — chờ đánh giá phần giải thích
50. Bạn gặp vấn đề gì?
51. Vì sao chọn cách kiểm tra/xử lý đó?
52. Nếu làm lại, bạn sẽ thay đổi điều gì?
53. Chấm / xem lịch sử chấm bài này
54. [dữ liệu] · Tổng: [dữ liệu] /100
55. · Giải thích: [dữ liệu] /20 · [dữ liệu]
56. Người chấm: [dữ liệu] · [dữ liệu]
57. Điểm mạnh: [dữ liệu]
58. Cần cải thiện: [dữ liệu]
59. Hướng làm lại: [dữ liệu]
60. Đã được sửa lúc [dữ liệu] – lý do: [dữ liệu]

#### wiring-diagram.jsp

1. [dữ liệu] · [dữ liệu]

#### wiring-error.jsp

1. Thông báo nối dây | Robot Assembly Lab
2. Tài khoản
3. Quản lý bài nối dây
4. Thực hành nối dây
5. Chưa thực hiện được thao tác
6. Hãy mở lại lượt đã lưu nếu bản nháp thay đổi ở tab khác.

#### wiring-list.jsp

1. Phòng thực hành nối dây | Robot Assembly Lab
2. Tài khoản
3. Thực hành nối dây
4. Phòng thực hành nối dây
5. Luyện tập có tài liệu: server chấm các cặp trực tiếp của bài mẫu, không mô phỏng điện hay xác nhận robot thật.
6. Bài đang công bố
7. Bắt đầu lượt mới
8. Lượt đã lưu và lịch sử của tôi
9. Chưa có lượt thực hành.
10. [dữ liệu] · lượt # [dữ liệu]
11. [dữ liệu] · [dữ liệu]
12. [dữ liệu] /100 — [dữ liệu]
13. Tiếp tục / xem kết quả

#### wiring-play.jsp

1. Thực hành nối dây | Robot Assembly Lab
2. Tài khoản
3. Thực hành nối dây
4. Lượt # [dữ liệu] · phiên bản [dữ liệu] · chưa chấm.
5. Gửi câu hỏi hỗ trợ (lưu nháp trước)
6. ·
7. Xem lịch sử hỗ trợ
8. Sơ đồ thực hành
9. Sơ đồ đang hiển thị các dây đã lưu.
10. Hủy chân đang chọn
11. Xóa dây đang chọn
12. JavaScript đang tắt: chọn hai chân bên dưới rồi bấm Thêm dây. Lưu và nộp vẫn hoạt động.
13. Mục tiêu và thao tác
14. Nhấn hai chân hoặc kéo chân A sang B. Dùng Enter/Space để chọn, Escape để hủy; chọn dây rồi nhấn Xóa dây.
15. Trên màn hình nhỏ có thể cuộn sơ đồ; các nhóm chọn chân bằng văn bản luôn dùng được.
16. Chân A
17. Chân B
18. Thêm dây và lưu nháp
19. Danh sách dây bằng văn bản
20. [dữ liệu] · [dữ liệu]
21. Xóa dây đã chọn và lưu nháp
22. Lưu nháp
23. Nộp để chấm (chốt sơ đồ)
24. Lưu nháp không chấm. Nộp sẽ khóa sơ đồ; muốn sửa sau đó hãy tạo lượt luyện lại.
25. Sơ đồ tham khảo hiện có

#### wiring-result.jsp

1. Kết quả nối dây | Robot Assembly Lab
2. Tài khoản
3. Thực hành nối dây
4. Kết quả nối dây
5. Lượt đã nộp trước đó. Đây là kết quả cũ; payload gửi lại không được lưu.
6. Lượt # [dữ liệu] · [dữ liệu]
7. [dữ liệu] · Nộp lúc [dữ liệu]
8. Điểm: [dữ liệu] /100
9. Đúng C= [dữ liệu] ; sai/thừa W= [dữ liệu] ; thiếu M= [dữ liệu] ; bắt buộc N= [dữ liệu] .
10. Điểm = 100 × max(C − W, 0) / N, làm tròn HALF_UP đến 1 chữ số thập phân.
11. Kết quả chỉ ghi nhận cặp trực tiếp trong bài mô phỏng, không xác nhận robot thật đấu nối/vận hành an toàn.
12. Chú giải: sơ đồ hiện dây đã nộp; nhãn Đúng, Sai/thừa, Cặp bị cấm và Thiếu trong bảng giải thích từng cặp.
13. Kết quả từng cặp nối
14. Hai đầu nối
15. Kết quả
16. Giải thích
17. [dữ liệu] · [dữ liệu]
18. Luyện lại (sao chép dây sang nháp mới)
19. Sơ đồ tham khảo hiện có
20. Gửi câu hỏi về kết quả đã nộp
21. ·
22. Xem lịch sử hỗ trợ

#### wiring-support-list.jsp

1. Hỗ trợ thực hành nối dây | Robot Assembly Lab
2. Tài khoản
3. Danh sách hỗ trợ
4. Hỗ trợ thực hành nối dây
5. Hỗ trợ trao đổi về lượt đã lưu; kết thúc hỗ trợ không có nghĩa sơ đồ đúng toàn bộ.
6. Mở lượt nối dây để gửi câu hỏi
7. Chưa có yêu cầu hỗ trợ.
8. Yêu cầu # [dữ liệu] · [dữ liệu]
9. [dữ liệu] · lượt # [dữ liệu]
10. Người hỏi: [dữ liệu]
11. Xem trao đổi

#### wiring-support-new.jsp

1. Gửi yêu cầu hỗ trợ | Robot Assembly Lab
2. Tài khoản
3. Danh sách hỗ trợ
4. Gửi yêu cầu hỗ trợ
5. Chỉ gửi dữ liệu đã lưu trên server. Hãy lưu nháp trước; thao tác đang kéo/chưa lưu không nằm trong bản chụp.
6. Lượt # [dữ liệu] · phiên bản [dữ liệu]
7. Câu hỏi (20–2000 ký tự)
8. Đầu nối liên quan (tùy chọn)
9. Không chọn đầu nối riêng
10. Gửi câu hỏi và bản chụp đã lưu

#### wiring-support-view.jsp

1. Trao đổi hỗ trợ nối dây | Robot Assembly Lab
2. Tài khoản
3. Danh sách hỗ trợ
4. Trao đổi hỗ trợ nối dây
5. Yêu cầu # [dữ liệu] · [dữ liệu]
6. Lượt gốc # [dữ liệu] · [dữ liệu]
7. Đã giải quyết hỗ trợ và sơ đồ đúng toàn bộ là hai kết quả riêng biệt.
8. Mở lượt gốc
9. Tin # [dữ liệu] · [dữ liệu]
10. Admin phản hồi User gửi · [dữ liệu]
11. Đầu nối liên quan: [dữ liệu]
12. Bản chụp tại lúc gửi tin # [dữ liệu]
13. Nội dung trao đổi (User 20–2000; Admin 10–2000 ký tự)
14. Gửi bản chụp mới từ lượt gốc đã lưu
15. Đầu nối liên quan (tùy chọn)
16. Không chọn đầu nối riêng
17. Gửi nội dung
18. Đóng hỗ trợ Đánh dấu đã giải quyết hỗ trợ

#### workspace-footer.jspf

1. Robot Assembly Lab · Không gian học tập và thực hành mô phỏng

#### workspace-header.jspf

1. Đi đến nội dung chính
2. ROBOT
3. ASSEMBLY LAB
4. Khu vực Admin Không gian học tập Thư viện học tập
5. Tài khoản
6. Nhiệm vụ
7. Bài nối dây
8. Nhiệm vụ
9. Nối dây
10. Khám phá
11. Danh mục robot
12. Linh kiện
13. Tình huống chẩn đoán
14. Yêu cầu hỗ trợ
15. Thống kê
16. Luyện chẩn đoán
17. Hỗ trợ nối dây
18. Tổng kết học tập
19. Hồ sơ in / PDF
20. Kiến trúc môn học

</details>

<details>
<summary>SAU: 843 mảnh văn bản tĩnh</summary>

#### account.jsp

1. Tài khoản | Robot Assembly Lab
2. Tài khoản thực hành
3. Thông tin của tài khoản đang đăng nhập.
4. Họ tên
5. Email
6. Vai trò
7. Quản trị viên Người học
8. Ngày tạo
9. Quản lý tài khoản
10. Về trang chủ
11. Ghi chú kỹ thuật
12. Trang được
13. AccountPageServlet
14. bảo vệ bằng
15. HttpSession
16. , sau đó controller đặt JavaBean
17. user
18. vào request và forward đến JSP.

#### admin-diagnosis-form.jsp

1. Biên soạn chẩn đoán | Robot Assembly Lab
2. ← Tài khoản
3. Tạo / sửa nháp tình huống
4. Bốn ô cố định cho mỗi nhóm. Ô nhãn trống được bỏ qua. Công bố cần 2–4 phép kiểm tra, ít nhất một phép cần thiết, 3–4 nguyên nhân và 3–4 biện pháp; mỗi nhóm lựa chọn có đúng một đáp án.
5. Tiêu đề
6. Bối cảnh
7. Triệu chứng
8. Giải thích sau khi chốt
9. Robot
10. Hướng dẫn liên quan (cùng robot hoặc dùng chung)
11. Không chọn
12. Phép kiểm tra
13. Ô [dữ liệu]
14. Tên phép kiểm tra
15. Quan sát
16. Cần thiết
17. Nguyên nhân
18. Ô [dữ liệu]
19. Nhãn
20. Phản hồi sau khi chốt
21. Đáp án đúng
22. Biện pháp
23. Ô [dữ liệu]
24. Nhãn
25. Phản hồi sau khi chốt
26. Đáp án đúng
27. Lưu nháp

#### admin-diagnosis-list.jsp

1. Quản lý chẩn đoán | Robot Assembly Lab
2. ← Tài khoản
3. Quản lý tình huống chẩn đoán
4. Tạo nháp tình huống
5. [dữ liệu] ·
6. · Phiên bản [dữ liệu]

#### admin-diagnosis-preview.jsp

1. Xem trước chẩn đoán | Robot Assembly Lab
2. ← Tài khoản
3. [dữ liệu] ·
4. · Phiên bản [dữ liệu]
5. Xem trước như người học, chỉ đọc. Không tạo lượt hoặc ghi phép kiểm tra.
6. Phép kiểm tra
7. Cần thiết
8. Không liên quan
9. Nguyên nhân / biện pháp
10. Đáp án đúng
11. Đáp án sai
12. Quan sát là dữ liệu mô phỏng của bài tập, không phải phép đo từ robot thật.
13. Giải thích sau khi chốt
14. Xem trước như người học
15. Sửa nháp
16. Công bố
17. Lưu trữ
18. Lưu trữ sẽ ẩn nội dung khỏi danh sách mặc định và ngăn bắt đầu lượt mới; lịch sử vẫn được giữ.
19. Nhân bản thành nháp mới
20. Danh sách tình huống

#### admin-stats.jsp

1. Thống kê quản trị | Robot Assembly Lab
2. Thống kê quản trị
3. Phiên lắp ráp theo trạng thái
4. Tổng [dữ liệu] phiên trên toàn hệ thống
5. Trạng thái
6. Số phiên
7. Đang chuẩn bị
8. Sẵn sàng
9. Đang lắp ráp
10. Đã hoàn thành
11. Đã dừng
12. Tổng số người học
13. Số người có hoạt động
14. (đã có ít nhất một phiên lắp ráp hoặc một lượt làm bài kiểm tra)
15. Mẫu robot được chọn nhiều nhất
16. Chưa có phiên lắp ráp nào trong hệ thống.
17. Xếp hạng theo số phiên lắp ráp đã tạo
18. Mẫu robot
19. Số phiên
20. Kết quả kiểm tra tổng hợp
21. Chưa có lượt làm bài kiểm tra nào trong hệ thống.
22. Điểm trung bình tính theo phần trăm số câu đúng trên mỗi lượt làm bài
23. Mẫu robot
24. Số lượt làm bài
25. Điểm trung bình
26. [dữ liệu] %
27. Câu hỏi có tỷ lệ trả lời sai cao
28. Chưa đủ dữ liệu (cần ít nhất [dữ liệu] lượt trả lời cho một câu hỏi mới đưa vào thống kê, để tránh một lượt làm sai duy nhất bị tính thành &quot;sai 100%&quot;).
29. Chỉ tính câu hỏi đã có từ [dữ liệu] lượt trả lời trở lên
30. Câu hỏi
31. Số lượt trả lời
32. Tỷ lệ sai
33. [dữ liệu] %
34. Về quản trị nội dung
35. ·
36. Về trang chủ
37. Ghi chú kỹ thuật
38. Trang do
39. AdminStatsServlet
40. dựng ở server (chỉ quản trị viên xem được, tài khoản thường bị từ chối với mã 403): mọi số liệu đọc trực tiếp từ MySQL qua
41. StatsDB
42. bằng
43. GROUP BY
44. /
45. COUNT DISTINCT
46. ngay trong câu SQL — không cộng dồn ở tầng Java — để tránh đếm trùng khi có JOIN. Không có số liệu nào ở đây được bịa ra để biểu đồ đẹp hơn.

#### admin-task-form.jsp

1. Biên soạn nhiệm vụ | Robot Assembly Lab
2. ← Tài khoản
3. Tạo / sửa nháp nhiệm vụ
4. Tìm người học theo tên
5. Trang danh sách người học
6. Tìm
7. Mỗi trang tối đa 20 người học. Người đã được giao vẫn được giữ trong nháp; bỏ chọn để gỡ.
8. Mẫu tiêu chí cố định
9. Mẫu A — lắp ráp 40, quiz 40, giải thích 20; tổng 100.
10. Mẫu B — lắp ráp 30, quiz 25, chẩn đoán 25, giải thích 20; tổng 100.
11. Tình huống chẩn đoán (bắt buộc với mẫu B, cùng robot)
12. Không dùng (mẫu A)
13. [dữ liệu] · [dữ liệu]
14. Tiêu đề
15. Mô tả
16. Mẫu robot
17. Hạn nộp (giờ Việt Nam)
18. Chính sách muộn
19. Không nhận bài quá hạn
20. Nhận và gắn cờ nộp muộn
21. Số lần nộp tối đa
22. Ngưỡng đạt
23. Cho phép phiên lắp ráp hoàn thành trước vòng 1
24. Thang điểm đang chọn: lắp ráp [dữ liệu] , quiz [dữ liệu] , chẩn đoán [dữ liệu] , giải thích 20 — tổng 100.
25. Chọn người học được giao nhiệm vụ
26. Lưu nháp

#### admin-task-list.jsp

1. Quản lý nhiệm vụ | Robot Assembly Lab
2. ← Tài khoản
3. Quản lý nhiệm vụ
4. Tạo nháp mới
5. Lọc trạng thái
6. Danh sách mặc định
7. Nháp
8. Đang mở
9. Đã đóng
10. Đã lưu trữ
11. Lọc
12. Chưa có nhiệm vụ.
13. [dữ liệu] ·
14. · Hạn nộp [dữ liệu]

#### admin-task-review.jsp

1. Đánh giá bài nộp | Robot Assembly Lab
2. ← Tài khoản
3. Chấm bài: [dữ liệu]
4. Người nộp: [dữ liệu] · Đang chấm lần nộp # [dữ liệu]
5. Không điều chỉnh điểm tự động. Mỗi lần chấm lưu một bản ghi mới; sửa chấm phải có lý do và vòng sau chưa có hoạt động.
6. Tổng quan nhiệm vụ
7. Mức giải thích
8. 0 điểm — Không có nội dung hoặc không liên quan
9. 5 điểm — Kể lại thao tác nhưng chưa giải thích
10. 10 điểm — Giải thích đúng một phần, còn thiếu căn cứ
11. 15 điểm — Lập luận đúng, liên hệ với kết quả quan sát
12. 20 điểm — Lập luận rõ, có căn cứ và đề xuất cải thiện hợp lý
13. Kết luận
14. Đạt yêu cầu
15. Cần bổ sung
16. Chưa đạt yêu cầu
17. Cần gia hạn trước nếu muốn yêu cầu bổ sung khi đã quá hạn và chính sách không nhận bài muộn. Nhiệm vụ đã đóng chỉ được kết luận Đạt / Chưa đạt.
18. Điểm mạnh
19. Cần cải thiện (bắt buộc với Cần bổ sung / Chưa đạt)
20. Hướng làm lại (bắt buộc với Cần bổ sung / Chưa đạt)
21. Lý do sửa chấm (bắt buộc khi đã chấm)
22. Lưu lần chấm
23. Quá trình chẩn đoán
24. Điểm thu thập quan sát: [dữ liệu] /4
25. Thu thập đủ quan sát cần thiết: [dữ liệu] / [dữ liệu] phép · Điểm: [dữ liệu] /10
26. Đã kiểm tra lúc [dữ liệu]
27. Phép cần thiết còn thiếu
28. Đã thu thập đủ quan sát cần thiết.
29. Nguyên nhân đã chọn
30. [dữ liệu] —
31. Đúng (3/3)
32. Sai (0/3)
33. Cách xử lý đã chọn
34. [dữ liệu] —
35. Đúng (3/3)
36. Sai (0/3)
37. Giải thích tình huống
38. Quan sát là dữ liệu mô phỏng của bài tập, không phải phép đo từ robot thật.
39. Bằng chứng và lịch sử nộp / chấm
40. Lần nộp # [dữ liệu]
41. · Nộp muộn
42. Phiên lắp ráp # [dữ liệu] · [dữ liệu] · hoàn thành [dữ liệu]
43. Quiz lượt # [dữ liệu] : [dữ liệu] / [dữ liệu] · [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] )
44. Tình huống: [dữ liệu]
45. Chẩn đoán lượt # [dữ liệu] – [dữ liệu] /10 – lúc [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
46. Đóng góp chẩn đoán: [dữ liệu] / [dữ liệu]
47. Lắp ráp: [dữ liệu] / [dữ liệu] · Quiz: [dữ liệu] / [dữ liệu]
48. Điểm tự động: [dữ liệu] / [dữ liệu]
49. — chờ đánh giá phần giải thích
50. Bạn gặp vấn đề gì?
51. Vì sao chọn cách kiểm tra/xử lý đó?
52. Nếu làm lại, bạn sẽ thay đổi điều gì?
53. Chấm / xem lịch sử chấm bài này
54. · Tổng: [dữ liệu] /100
55. · Giải thích: [dữ liệu] /20 · [dữ liệu]
56. Người chấm: [dữ liệu] · [dữ liệu]
57. Điểm mạnh: [dữ liệu]
58. Cần cải thiện: [dữ liệu]
59. Hướng làm lại: [dữ liệu]
60. Đã được sửa lúc [dữ liệu] – lý do: [dữ liệu]

#### admin-wiring-form.jsp

1. Biên soạn bài nối dây | Robot Assembly Lab
2. Tài khoản
3. Quản lý bài nối dây
4. Biên soạn bài nối dây nháp
5. Số ô cố định: tối đa 40 đầu nối, 80 quy tắc. Bỏ trống mã đầu nối/cả hai ô quy tắc để bỏ qua ô đó.
6. Mã bài (chữ không dấu, số, dấu . _ -)
7. Tên bài
8. Robot
9. Mục tiêu
10. Phạm vi mô phỏng (ghi rõ phần không chấm)
11. Đầu nối
12. Ô đầu nối [dữ liệu]
13. Mã riêng (ví dụ line-left.OUT)
14. Mã thiết bị
15. Tên thiết bị
16. Nhãn chân
17. Tọa độ X (40–960)
18. Tọa độ Y (40–960)
19. Quy tắc (đầu A/B là số ô đầu nối bên trên)
20. Quy tắc [dữ liệu]
21. Ô đầu nối A
22. Ô đầu nối B
23. Bắt buộc
24. Bị cấm
25. Giải thích đúng/thiếu hoặc lý do cấm
26. Lưu nháp

#### admin-wiring-list.jsp

1. Quản lý bài nối dây | Robot Assembly Lab
2. Tài khoản
3. Quản lý bài nối dây
4. Quản lý bài nối dây
5. Tạo bài nháp
6. [dữ liệu] ·
7. Chi tiết / quản lý

#### admin-wiring-view.jsp

1. Chi tiết bài nối dây | Robot Assembly Lab
2. Tài khoản
3. Quản lý bài nối dây
4. · [dữ liệu]
5. Quy tắc biên soạn
6. # [dữ liệu] · # [dữ liệu] ·
7. Bắt buộc
8. Cặp bị cấm
9. : [dữ liệu]
10. Xem trước chỉ đọc; không tạo lượt của người học.
11. Xem trước như người học
12. Sửa nháp và đầu nối/quy tắc
13. Công bố và khóa nội dung
14. Lưu trữ
15. Lưu trữ sẽ ẩn nội dung khỏi danh sách mặc định và ngăn bắt đầu lượt mới; lịch sử vẫn được giữ.
16. Nhân bản thành nháp mới
17. Danh sách bài

#### architecture.jsp

1. Luồng Servlet/JSP | Robot Assembly Lab
2. Luồng Servlet/JSP của Robot Assembly Lab
3. Trang này do
4. đặt dữ liệu vào request rồi
5. forward()
6. sang JSP. JSP chỉ hiển thị, không truy vấn MySQL.
7. Browser (view: HTML/JSP) → [dữ liệu] (controller: servlet) → JavaBean trong package business (model) → [dữ liệu] (data access layer) → MySQL JSON contract: [dữ liệu]
8. Mở response JSON health

#### assembly-receipt.jsp

1. Phiếu kết quả lắp ráp | Robot Assembly Lab
2. Phiếu kết quả lắp ráp
3. Phiên lắp ráp này chưa hoàn tất nên chưa có phiếu kết quả. Trạng thái hiện tại:
4. Đang lắp ráp Sẵn sàng Đang chuẩn bị Đã dừng
5. .
6. Vào phòng lắp ráp 3D để tiếp tục →
7. Đã hoàn tất lắp ráp trong mô hình 3D
8. · [dữ liệu]
9. Mã phiên
10. Mẫu robot
11. Bắt đầu phiên
12. Hoàn tất lắp ráp
13. Kết quả
14. Đã lắp đủ toàn bộ [dữ liệu] nhóm linh kiện bắt buộc trong mô hình 3D.
15. Linh kiện bắt buộc của mẫu robot này (bảng
16. robot_components
17. )
18. Mã linh kiện
19. Tên linh kiện
20. Số lượng yêu cầu
21. Đã lắp trong mô hình 3D
22. ✓ Đã lắp
23. &quot;Hoàn tất&quot; ở đây có nghĩa là đã hoàn tất bài lắp ráp trong phạm vi mô phỏng của website (đủ mọi linh kiện bắt buộc trong mô hình 3D); phiếu này không khẳng định robot thật đã được đấu nối điện hoặc vận hành đúng.
24. Làm bài kiểm tra kiến thức →
25. ·
26. Thực hành lại mẫu này →
27. ·
28. Tra cứu lỗi lắp ráp →
29. ·
30. Xem lịch sử tài khoản
31. Về trang chủ
32. Ghi chú kỹ thuật
33. Trang do
34. AssemblyReceiptPageServlet
35. dựng ở server: đọc phiên theo
36. (id, user_id)
37. qua
38. AssemblySessionDB.selectSession()
39. nên chỉ chủ phiên mới xem được, đặt robot và danh sách linh kiện vào request bằng
40. setAttribute
41. rồi
42. forward()
43. sang JSP này. Trạng thái hoàn tất và
44. completed_at
45. chỉ được
46. AssemblySessionDB.completeSession()
47. ghi sau khi đối chiếu tập hợp linh kiện bắt buộc với dữ liệu thật trong
48. session_visual_parts
49. — trang này không tin số phần trăm do trình duyệt tự tính.

#### components.jsp

1. Danh mục linh kiện (JSP) | Robot Assembly Lab
2. Danh mục linh kiện
3. Tổng số bản ghi trong bảng
4. components
5. :
6. · đang xem trang [dữ liệu]
7. Dữ liệu đọc trực tiếp từ MySQL qua JDBC
8. Ảnh
9. ID
10. Tên linh kiện
11. Danh mục
12. Mô tả
13. ← Trang trước
14. Trang sau →
15. Bảng
16. components
17. chưa có dữ liệu. Hãy chạy
18. database/seed.sql
19. .
20. Danh mục robot (JSP)
21. ·
22. Sơ đồ kiến trúc
23. ·
24. Về trang chủ
25. Ghi chú kỹ thuật
26. Trang do
27. ComponentCatalogPageServlet
28. dựng ở server: tham số
29. ?page=
30. được đọc từ
31. HttpServletRequest
32. ,
33. ComponentDB.selectComponents()
34. lấy kết nối từ
35. ConnectionPool
36. và chạy
37. PreparedStatement
38. trên MySQL, kết quả đi qua
39. setAttribute
40. rồi
41. forward()
42. tới JSP.

#### diagnosis-list.jsp

1. Luyện chẩn đoán | Robot Assembly Lab
2. ← Tài khoản
3. Luyện chẩn đoán
4. Chọn phép kiểm tra để thu thập quan sát mô phỏng, rồi kết luận nguyên nhân và cách xử lý.
5. Lượt luyện không tính vào nhiệm vụ. Muốn làm bài được giao, hãy bắt đầu từ trang nhiệm vụ.
6. Đang làm dở
7. Chưa có lượt đang làm.
8. Lượt # [dữ liệu] · [dữ liệu]
9. Tình huống đang công bố
10. [dữ liệu] · Phiên bản [dữ liệu]
11. Bắt đầu luyện

#### diagnosis-play.jsp

1. Thực hành chẩn đoán | Robot Assembly Lab
2. ← Tài khoản
3. Lượt # [dữ liệu] · [dữ liệu]
4. Quan sát là dữ liệu mô phỏng của bài tập, không phải phép đo từ robot thật.
5. Chọn phép kiểm tra
6. Đã kiểm tra lúc [dữ liệu]
7. Thực hiện phép kiểm tra
8. Kết luận
9. Thu thập đủ quan sát cần thiết: 4 điểm; xác định nguyên nhân: 3; chọn cách xử lý: 3.
10. Có thể kết luận khi chưa kiểm tra; điểm thu thập quan sát khi đó bằng 0. Kết luận chỉ được chốt một lần.
11. Nguyên nhân
12. Cách xử lý
13. Chốt kết luận
14. Nhiệm vụ của tôi
15. ·
16. Danh sách luyện

#### diagnosis-result.jsp

1. Kết quả chẩn đoán | Robot Assembly Lab
2. ← Tài khoản
3. Kết quả: [dữ liệu]
4. Lượt # [dữ liệu] · Chốt lúc [dữ liệu]
5. Quá trình chẩn đoán
6. Thu thập đủ quan sát cần thiết: [dữ liệu] / [dữ liệu] phép · [dữ liệu] /4
7. Tổng điểm chẩn đoán: [dữ liệu] /10
8. Đã kiểm tra lúc [dữ liệu]
9. Phép cần thiết còn thiếu
10. Đã thu thập đủ quan sát cần thiết.
11. Nguyên nhân đã chọn
12. [dữ liệu] —
13. Đúng (3/3)
14. Sai (0/3)
15. Cách xử lý đã chọn
16. [dữ liệu] —
17. Đúng (3/3)
18. Sai (0/3)
19. Giải thích tình huống
20. Quan sát là dữ liệu mô phỏng của bài tập, không phải phép đo từ robot thật.
21. Xem hướng dẫn tra cứu liên quan
22. Tra cứu hướng dẫn theo mẫu robot
23. Nhiệm vụ của tôi
24. ·
25. Danh sách luyện

#### learning-profile.jsp

1. Hồ sơ học tập - [dữ liệu] | Robot Assembly Lab
2. ← Quay lại tài khoản
3. In / Lưu PDF
4. Trong hộp thoại in, chọn Lưu dưới dạng PDF, khổ A4; nên tắt Đầu trang và chân trang để tránh thêm ngày giờ và tiêu đề của trình duyệt.
5. Bạn có thể dùng Ctrl+P (Windows/Linux) hoặc Command+P (macOS) để in hoặc lưu PDF.
6. Robot Assembly Lab
7. Hồ sơ học tập Robot Assembly Lab
8. Họ tên:
9. Lập lúc [dữ liệu]
10. Lớp/MSSV: ........
11. Tổng quan kết quả
12. Mẫu robot
13. Lượt làm bài
14. Mẫu đã làm bài
15. [dữ liệu] / [dữ liệu]
16. Điểm trung bình
17. Tình trạng chung
18. Điểm trung bình là trung bình cộng tỷ lệ phần trăm của lượt tốt nhất ở từng mẫu đã làm bài; làm tròn đến số nguyên (phần thập phân từ 0,5 trở lên được làm tròn lên). Tốt nhất là tỷ lệ câu đúng cao nhất (nếu bằng nhau, lấy lượt mới hơn); gần nhất là lượt có thời điểm nộp mới nhất (hòa lấy mã lượt lớn hơn).
19. Kết quả theo từng mẫu robot
20. Chưa có dữ liệu trong danh mục robot.
21. Trạng thái
22. Hoàn thành
23. [dữ liệu] · phiên # [dữ liệu] · [dữ liệu] lần
24. Phiên gần nhất
25. · cập nhật [dữ liệu]
26. Phiên lắp ráp
27. Chưa có dữ liệu
28. Quiz tốt nhất
29. [dữ liệu] / [dữ liệu] · [dữ liệu]
30. Quiz gần nhất
31. [dữ liệu] / [dữ liệu] · [dữ liệu]
32. Số lượt quiz
33. Quiz
34. Chưa có dữ liệu
35. Kỹ năng và kiến thức đã thực hành
36. Các nội dung được ghi nhận từ mẫu đã hoàn thành và lượt quiz hiện có
37. Nội dung đã thực hành
38. Căn cứ ghi nhận
39. Nhận xét/xác nhận của giảng viên
40. Họ tên giảng viên:
41. Ngày:
42. Chữ ký:
43. Hoàn tất/kết quả trong hồ sơ này là kết quả mô phỏng trong phạm vi website, không khẳng định robot thật đã được đấu nối hay vận hành đúng, và không thay thế đánh giá của giảng viên.
44. Thời điểm lập: [dữ liệu]
45. Luồng trong mã:
46. LearningProfileServlet → StatsDB/RobotDB → LearningProfile → JSP
47. . Bộ đếm trang ở chân giấy phụ thuộc hỗ trợ margin box của trình duyệt. Nên tắt Đầu trang và chân trang; nếu trình duyệt không hiện số trang do CSS tạo và bạn cần số trang, có thể bật lại tùy chọn này, nhưng bản in sẽ có thêm ngày giờ, tiêu đề hoặc URL do trình duyệt tạo, theo định dạng của trình duyệt.

#### learning-summary.jsp

1. Tổng kết học tập | Robot Assembly Lab
2. Tổng kết học tập
3. Tài khoản
4. [dữ liệu] ( [dữ liệu] )
5. Tổng số phiên lắp ráp
6. Số phiên đã hoàn thành
7. Đang chuẩn bị / sẵn sàng
8. Đang lắp ráp
9. Đã dừng
10. Mẫu robot đã hoàn tất
11. Bạn chưa hoàn tất mẫu robot nào. Hãy vào phòng lắp ráp 3D và hoàn tất một phiên để xem tại đây.
12. Mẫu robot đã từng lắp ráp xong ít nhất một lần
13. Mẫu robot
14. Độ khó
15. Mô tả
16. Điểm kiểm tra kiến thức
17. Bạn chưa làm bài kiểm tra nào. Vào trang lắp ráp của một mẫu robot để bắt đầu.
18. Điểm tốt nhất và gần nhất theo từng mẫu robot đã từng làm bài
19. Mẫu robot
20. Điểm tốt nhất
21. Điểm gần nhất
22. Số lần làm bài
23. [dữ liệu] / [dữ liệu]
24. [dữ liệu] / [dữ liệu]
25. Hồ sơ học tập (in/PDF)
26. ·
27. Xem lịch sử phiên và phiếu kết quả
28. ·
29. Tra cứu lỗi lắp ráp
30. ·
31. Về trang chủ
32. Ghi chú kỹ thuật
33. Trang do
34. LearningSummaryServlet
35. dựng ở server: đọc số liệu của đúng tài khoản đang đăng nhập qua
36. StatsDB
37. (dùng
38. GROUP BY
39. và
40. COUNT DISTINCT
41. ngay trong SQL để không đếm trùng), đặt vào request bằng
42. setAttribute
43. rồi
44. forward()
45. sang JSP này. Không có tài khoản nào xem được số liệu của tài khoản khác.

#### order-history.jsp

1. Lịch sử đơn mô phỏng | Robot Assembly Lab
2. ← Quay lại cửa hàng
3. Lịch sử đơn mô phỏng
4. Đây là các đơn học tập của [dữ liệu] . Đơn chỉ ghi nhận dữ liệu trong bài, không có thanh toán hoặc giao hàng thật.
5. Bạn chưa có đơn nào. Hãy xem giá tham khảo của linh kiện trong cửa hàng.
6. Đơn # [dữ liệu]
7. Trạng thái:
8. · [dữ liệu]
9. Các mặt hàng đã được chụp thông tin tại lúc xác nhận
10. Linh kiện
11. Đơn giá
12. Số lượng
13. Thành tiền
14. đ
15. đ
16. Tổng mô phỏng:
17. đ
18. ← Mới hơn
19. Trang [dữ liệu] / [dữ liệu]
20. Cũ hơn →
21. Giỏ hàng
22. ·
23. Trang chủ

#### robots.jsp

1. Danh mục robot (JSP) | Robot Assembly Lab
2. Danh mục mô hình robot
3. Tổng số bản ghi trong bảng
4. robots
5. :
6. · đang xem trang [dữ liệu]
7. Dữ liệu đọc trực tiếp từ MySQL qua JDBC
8. ID
9. Tên mô hình
10. Độ khó
11. Cảm biến chính
12. Thời gian lắp
13. Bước lắp ráp
14. Xem JSON
15. ← Trang trước
16. Trang sau →
17. Bảng
18. robots
19. chưa có dữ liệu. Hãy chạy
20. database/seed.sql
21. .
22. Danh mục linh kiện (JSP)
23. ·
24. Sơ đồ kiến trúc
25. ·
26. Về trang chủ
27. Ghi chú kỹ thuật
28. Trang do
29. RobotCatalogPageServlet
30. dựng ở server:
31. doGet()
32. gọi
33. RobotDB.selectRobots()
34. →
35. ConnectionPool
36. → MySQL, đặt danh sách vào request bằng
37. setAttribute(&quot;robots&quot;, ...)
38. rồi
39. forward()
40. sang JSP này. JSP chỉ hiển thị, không mở kết nối database.

#### task-error.jsp

1. Thông báo nhiệm vụ | Robot Assembly Lab
2. ← Tài khoản
3. Chưa thực hiện được thao tác
4. Vui lòng quay lại nhiệm vụ để kiểm tra vòng hiện tại và điều kiện nộp.
5. Quản lý nhiệm vụ
6. Nhiệm vụ của tôi

#### task-list.jsp

1. Nhiệm vụ của tôi | Robot Assembly Lab
2. ← Tài khoản
3. Nhiệm vụ của tôi
4. Chỉ hiển thị nhiệm vụ được giao cho tài khoản hiện tại.
5. Chưa có nhiệm vụ.
6. [dữ liệu] ·
7. · Hạn nộp [dữ liệu]

#### task-preview.jsp

1. Xem trước bài nộp | Robot Assembly Lab
2. ← Tài khoản
3. Xem trước bài nộp
4. Phiên lắp ráp # [dữ liệu] , hoàn thành lúc [dữ liệu]
5. Quiz lượt # [dữ liệu] – [dữ liệu] / [dữ liệu] – [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] )
6. Tình huống: [dữ liệu]
7. Chẩn đoán lượt # [dữ liệu] – [dữ liệu] /10 – lúc [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
8. Điểm tự động dự kiến: [dữ liệu] / [dữ liệu] — chờ đánh giá phần giải thích
9. Nộp muộn
10. Giải thích
11. Bạn gặp vấn đề gì?
12. Vì sao chọn cách kiểm tra/xử lý đó?
13. Nếu làm lại, bạn sẽ thay đổi điều gì?
14. Tình huống: [dữ liệu]
15. Xác nhận nộp bài
16. Quay lại chọn bằng chứng

#### task-quiz.jsp

1. Quiz nhiệm vụ | Robot Assembly Lab
2. ← Tài khoản
3. Quiz nhiệm vụ: [dữ liệu]
4. Chỉ lượt hợp lệ đầu tiên của mỗi vòng được chốt tính điểm. Gửi thiếu câu hoặc lựa chọn không thuộc câu sẽ không lưu lượt.
5. Chốt lượt tính điểm của vòng này
6. Lượt tính điểm của vòng này đã chốt hoặc vòng không còn nhận quiz.
7. Quiz lượt # [dữ liệu] – [dữ liệu] / [dữ liệu] – [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
8. Kết quả lượt đã chốt
9. Bạn chọn: [dữ liệu]
10. Đáp án: [dữ liệu]
11. Trở về nhiệm vụ
12. Làm quiz luyện tập

#### task-submit.jsp

1. Nộp bài nhiệm vụ | Robot Assembly Lab
2. ← Tài khoản
3. Nộp bài: [dữ liệu]
4. Quiz lượt # [dữ liệu] – [dữ liệu] / [dữ liệu] – [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
5. Chẩn đoán lượt # [dữ liệu] – [dữ liệu] /10 – lúc [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
6. Tiếp tục chẩn đoán Bắt đầu chẩn đoán cho nhiệm vụ này
7. Chọn một phiên của chính bạn đã hoàn thành đúng mẫu. Mốc bằng chứng là lúc bắt đầu vòng 1; các vòng sau có thể dùng lại phiên này nếu hợp lệ.
8. Chưa có phiên lắp ráp phù hợp. Hãy hoàn thành mẫu robot rồi quay lại.
9. Bằng chứng lắp ráp
10. Phiên # [dữ liệu] · [dữ liệu] · hoàn thành [dữ liệu]
11. Bạn gặp vấn đề gì?
12. Vì sao chọn cách kiểm tra/xử lý đó?
13. Nếu làm lại, bạn sẽ thay đổi điều gì?
14. Xem trước bài nộp

#### task-view.jsp

1. Chi tiết nhiệm vụ | Robot Assembly Lab
2. ← Tài khoản
3. Mẫu: [dữ liệu] ·
4. Hạn nộp: [dữ liệu] · [dữ liệu]
5. Tối đa [dữ liệu] lần nộp · Ngưỡng đạt [dữ liệu] /100.
6. Thang điểm: lắp ráp [dữ liệu] , quiz [dữ liệu] , chẩn đoán [dữ liệu] , giải thích 20 — tổng 100.
7. Điểm tự động được làm tròn đến 1 chữ số thập phân. Tổng điểm gồm điểm tự động và điểm giải thích.
8. Cách tính điểm
9. Điểm tự động được làm tròn một lần: chữ số tiếp theo từ 5 trở lên thì tăng chữ số cuối. Điểm giải thích được cộng vào điểm tự động đã lưu; không làm tròn lại tổng điểm.
10. Xem trước nội dung cho người học. Quản trị viên không được nộp bài.
11. Xem trước như người học
12. ·
13. Danh sách nhiệm vụ
14. Sửa nháp và người được giao
15. Công bố
16. Xoá nháp
17. Xóa nháp sẽ bỏ nội dung chưa công bố này. Hãy kiểm tra trước khi xác nhận.
18. Tìm người học theo tên
19. Trang danh sách người học
20. Tìm
21. Mỗi trang tối đa 20 người học. Người đã được giao vẫn được giữ trong nháp; bỏ chọn để gỡ.
22. Thêm người được giao
23. Thêm người học
24. Hạn mới (giờ Việt Nam)
25. Gia hạn
26. Đóng nhiệm vụ
27. Đóng nhiệm vụ sẽ ngừng nhận bài mới và không thể mở lại. Các bài đang chờ vẫn được chấm.
28. Lưu trữ
29. Lưu trữ sẽ ẩn nhiệm vụ khỏi danh sách mặc định; bài nộp và lịch sử chấm vẫn được giữ.
30. Nhân bản thành nháp mới
31. Người được giao và kết quả mới nhất
32. Họ tên
33. Trạng thái
34. Điểm
35. · Nộp muộn
36. [dữ liệu] /100 [dữ liệu] / [dữ liệu] — chờ đánh giá phần giải thích
37. Vòng [dữ liệu] · bắt đầu [dữ liệu]
38. Quiz lượt # [dữ liệu] – [dữ liệu] / [dữ liệu] – [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
39. Chẩn đoán lượt # [dữ liệu] – [dữ liệu] /10 – lúc [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
40. Tiếp tục chẩn đoán Bắt đầu chẩn đoán cho nhiệm vụ này
41. Làm quiz cho nhiệm vụ này
42. Chọn bằng chứng và nộp bài
43. Quiz luyện tập (không thay lượt tính điểm)
44. Lịch sử các vòng nộp và chấm
45. Chưa có bài nộp.
46. Lần nộp # [dữ liệu]
47. · Nộp muộn
48. Phiên lắp ráp # [dữ liệu] · [dữ liệu] · hoàn thành [dữ liệu]
49. Quiz lượt # [dữ liệu] : [dữ liệu] / [dữ liệu] · [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] )
50. Tình huống: [dữ liệu]
51. Chẩn đoán lượt # [dữ liệu] – [dữ liệu] /10 – lúc [dữ liệu] (dùng lại từ lần nộp # [dữ liệu] ) (lượt của vòng này)
52. Đóng góp chẩn đoán: [dữ liệu] / [dữ liệu]
53. Lắp ráp: [dữ liệu] / [dữ liệu] · Quiz: [dữ liệu] / [dữ liệu]
54. Điểm tự động: [dữ liệu] / [dữ liệu]
55. — chờ đánh giá phần giải thích
56. Bạn gặp vấn đề gì?
57. Vì sao chọn cách kiểm tra/xử lý đó?
58. Nếu làm lại, bạn sẽ thay đổi điều gì?
59. Chấm / xem lịch sử chấm bài này
60. · Tổng: [dữ liệu] /100
61. · Giải thích: [dữ liệu] /20 · [dữ liệu]
62. Người chấm: [dữ liệu] · [dữ liệu]
63. Điểm mạnh: [dữ liệu]
64. Cần cải thiện: [dữ liệu]
65. Hướng làm lại: [dữ liệu]
66. Đã được sửa lúc [dữ liệu] – lý do: [dữ liệu]

#### wiring-diagram.jsp

1. [dữ liệu] · [dữ liệu]
2. [dữ liệu] · [dữ liệu]
3. [ [dữ liệu] ]

#### wiring-error.jsp

1. Thông báo nối dây | Robot Assembly Lab
2. Tài khoản
3. Quản lý bài nối dây
4. Thực hành nối dây
5. Chưa thực hiện được thao tác
6. Hãy mở lại lượt đã lưu nếu bản nháp thay đổi ở tab khác.

#### wiring-list.jsp

1. Phòng thực hành nối dây | Robot Assembly Lab
2. Tài khoản
3. Thực hành nối dây
4. Phòng thực hành nối dây
5. Luyện tập có tài liệu: server chấm các cặp trực tiếp của bài mẫu, không mô phỏng điện hay xác nhận robot thật.
6. Bài đang công bố
7. Bắt đầu lượt mới
8. Lượt đã lưu và lịch sử của tôi
9. Chưa có lượt thực hành.
10. [dữ liệu] · lượt # [dữ liệu]
11. · [dữ liệu]
12. [dữ liệu] /100 —
13. Tiếp tục / xem kết quả

#### wiring-play.jsp

1. Thực hành nối dây | Robot Assembly Lab
2. Tài khoản
3. Thực hành nối dây
4. Lượt # [dữ liệu] · phiên bản [dữ liệu] ·
5. chưa chấm
6. .
7. Gửi câu hỏi hỗ trợ (lưu nháp trước)
8. ·
9. Xem lịch sử hỗ trợ
10. Sơ đồ thực hành
11. Sơ đồ đang hiển thị các dây đã lưu.
12. Hủy chân đang chọn
13. Xóa dây đang chọn
14. JavaScript đang tắt: chọn hai chân bên dưới rồi bấm Thêm dây. Lưu và nộp vẫn hoạt động.
15. Mục tiêu và thao tác
16. Nhấn hai chân hoặc kéo chân A sang B. Dùng Enter/Space để chọn, Escape để hủy; chọn dây rồi nhấn Xóa dây.
17. Trên màn hình nhỏ có thể cuộn sơ đồ; các nhóm chọn chân bằng văn bản luôn dùng được.
18. Chân A
19. [dữ liệu] · [dữ liệu]
20. [ [dữ liệu] ]
21. Chân B
22. [dữ liệu] · [dữ liệu]
23. [ [dữ liệu] ]
24. Thêm dây và lưu nháp
25. Danh sách dây bằng văn bản
26. [dữ liệu] · [dữ liệu]
27. [ [dữ liệu] ]
28. ·
29. [dữ liệu] · [dữ liệu]
30. [ [dữ liệu] ]
31. Xóa dây đã chọn và lưu nháp
32. Lưu nháp
33. Nộp để chấm (chốt sơ đồ)
34. Lưu nháp không chấm. Nộp sẽ khóa sơ đồ; muốn sửa sau đó hãy tạo lượt luyện lại.
35. Sơ đồ tham khảo hiện có

#### wiring-result.jsp

1. Kết quả nối dây | Robot Assembly Lab
2. Tài khoản
3. Thực hành nối dây
4. Kết quả nối dây
5. Lượt đã nộp trước đó. Đây là kết quả cũ; sơ đồ gửi lại không được lưu.
6. Lượt # [dữ liệu] · [dữ liệu]
7. [dữ liệu] · Nộp lúc [dữ liệu]
8. Điểm: [dữ liệu] /100
9. Dây đúng: [dữ liệu] · Dây sai/thừa: [dữ liệu] · Còn thiếu: [dữ liệu] · Tổng dây bắt buộc: [dữ liệu] .
10. Cách tính điểm
11. Điểm = 100 × max(C − W, 0) / N; C là số dây đúng, W là số dây sai/thừa, N là tổng dây bắt buộc. Làm tròn đến 1 chữ số thập phân: chữ số tiếp theo từ 5 trở lên thì tăng chữ số cuối.
12. Kết quả chỉ ghi nhận cặp trực tiếp trong bài mô phỏng, không xác nhận robot thật đấu nối/vận hành an toàn.
13. Chú giải: sơ đồ hiện dây đã nộp; nhãn Đúng, Sai/thừa, Cặp bị cấm và Thiếu trong bảng giải thích từng cặp.
14. Kết quả từng cặp nối
15. Hai đầu nối
16. Kết quả
17. Giải thích
18. [dữ liệu] · [dữ liệu]
19. [ [dữ liệu] ]
20. ·
21. [dữ liệu] · [dữ liệu]
22. [ [dữ liệu] ]
23. Luyện lại (sao chép dây sang nháp mới)
24. Sơ đồ tham khảo hiện có
25. Gửi câu hỏi về kết quả đã nộp
26. ·
27. Xem lịch sử hỗ trợ

#### wiring-support-list.jsp

1. Hỗ trợ thực hành nối dây | Robot Assembly Lab
2. Tài khoản
3. Danh sách hỗ trợ
4. Hỗ trợ thực hành nối dây
5. Hỗ trợ trao đổi về lượt đã lưu; kết thúc hỗ trợ không có nghĩa sơ đồ đúng toàn bộ.
6. Mở lượt nối dây để gửi câu hỏi
7. Chưa có yêu cầu hỗ trợ.
8. Yêu cầu # [dữ liệu] ·
9. [dữ liệu] · lượt # [dữ liệu]
10. Người hỏi: [dữ liệu]
11. Xem trao đổi

#### wiring-support-new.jsp

1. Gửi yêu cầu hỗ trợ | Robot Assembly Lab
2. Tài khoản
3. Danh sách hỗ trợ
4. Gửi yêu cầu hỗ trợ
5. Chỉ gửi dữ liệu đã lưu trên server. Hãy lưu nháp trước; thao tác đang kéo/chưa lưu không nằm trong bản chụp.
6. Lượt # [dữ liệu] · phiên bản [dữ liệu]
7. Câu hỏi (20–2000 ký tự)
8. Đầu nối liên quan (tùy chọn)
9. Không chọn đầu nối riêng
10. [dữ liệu] · [dữ liệu]
11. [ [dữ liệu] ]
12. Gửi câu hỏi và bản chụp đã lưu

#### wiring-support-view.jsp

1. Trao đổi hỗ trợ nối dây | Robot Assembly Lab
2. Tài khoản
3. Danh sách hỗ trợ
4. Trao đổi hỗ trợ nối dây
5. Yêu cầu # [dữ liệu] ·
6. Lượt gốc # [dữ liệu] · [dữ liệu]
7. Đã giải quyết hỗ trợ và sơ đồ đúng toàn bộ là hai kết quả riêng biệt.
8. Mở lượt gốc
9. Tin # [dữ liệu] · [dữ liệu]
10. Quản trị viên phản hồi Người học gửi · [dữ liệu]
11. Đầu nối liên quan: [dữ liệu]
12. Bản chụp tại lúc gửi tin # [dữ liệu]
13. Nội dung trao đổi (người học 20–2000; quản trị viên 10–2000 ký tự)
14. Gửi bản chụp mới từ lượt gốc đã lưu
15. Đầu nối liên quan (tùy chọn)
16. Không chọn đầu nối riêng
17. [dữ liệu] · [dữ liệu]
18. [ [dữ liệu] ]
19. Gửi nội dung
20. Đóng yêu cầu Đánh dấu đã giải quyết hỗ trợ
21. Đóng yêu cầu sẽ kết thúc trao đổi này. Nếu cần, bạn có thể gửi yêu cầu hỗ trợ mới.

#### workspace-footer.jspf

1. Robot Assembly Lab · Không gian học tập và thực hành mô phỏng

#### workspace-header.jspf

1. Đi đến nội dung chính
2. ROBOT
3. ASSEMBLY LAB
4. Khu vực Admin Không gian học tập Thư viện học tập
5. Tài khoản
6. Nhiệm vụ
7. Bài nối dây
8. Nhiệm vụ
9. Nối dây
10. Khám phá
11. Danh mục robot
12. Linh kiện
13. Tình huống chẩn đoán
14. Yêu cầu hỗ trợ
15. Thống kê
16. Luyện chẩn đoán
17. Hỗ trợ nối dây
18. Tổng kết học tập
19. Hồ sơ in / PDF
20. Kiến trúc môn học

</details>
