# Đợt 11 — Điều hướng và kết nối chức năng

Đối chiếu source và runtime ngày15/10/2026, nhánh integration/fullstack-v2.
Baseline191146b đã đồng bộ origin. Không push trong đợt này. Không migration/seed,
không sửa spec v1/v2, luật thiết kế hoặc rubric/API cũ.

## Kết quả

| Kiểm | Trước | Sau |
| --- | --- | --- |
| npm test | 197 pass /0 fail /0 skip | 205 pass /0 fail /0 skip |
| JDK17 mvn test | 127 pass /0 fail /0 skip | 130 pass /0 fail /0 skip |
| JDK17 clean package | Đạt | Đạt |
| Hash10 file WIP/untracked | Ghi SHA-256 trước | 10/10 nguyên byte |
| Trình duyệt Chrome | Khảo sát,30 ảnh trước | 207/207 ô đạt; thêm6 viewport QA7 |
| HTTP/hành vi cuối | 120 GET khảo sát3vai trò | 81/81 kiểm cuối đạt |
| Bổ sung giỏ | — | 31/31 kiểm HTTP/browser đạt |
| Tương tác/khôi phục | — | 13/13 kiểm đạt |
| Smoke WAR cuối | — | 36/36 kiểm đạt, có header no-store và chặn HTML admin |

Số kiểm HTTP/hành vi gồm assertion đích PRG, mã HTTP, số request ghi; không gọi
chúng là số request duy nhất. Các tập kiểm có ca trùng, không cộng thành số test độc lập.
Node tăng8 (menu2, dashboard1, liên kết/giỏ2, course map1, error1, syntax1); JUnit tăng3
(2dashboard,1giỏ). Build riêng ngoài repo để không ảnh hưởng target của IntelliJ.

## Thay đổi và quyết định

- Menu chung sáu nhóm, fixture duy nhất tests/fixtures/site-nav.json;18 HTML và mọi
  JSP dùng shared include. Native details + Enter/Escape; giữ sidebar thu gọn desktop.
  Các điểm vào module trong menu cần tối đa2bấm (mở nhóm/chọn link), không có lớp
  hamburger ngoài nhóm. Trang chi tiết theo bản ghi vẫn phải chọn phiên/thiết kế.
- /account thành dashboard thật; HTML tài khoản giữ auth/settings/history. /robots
  và /components giữ nguyên route và dữ liệu, thêm dải minh họa, mở từ course map.
  Kết quả học tập là entry duy nhất trong menu; PDF là thao tác trong tổng kết.
- M6 gộp M2: không ẩn/xóa module có dữ liệu. Bốn route luyện tập USER vốn403 với
  ADMIN chỉ hiện cho khách/USER; ADMIN mở bản quản trị ở nhóm riêng. Chấm nhiệm vụ
  mở danh sách chọn bài vì /admin-task-reviews không có ID vốn422. Không đổi mã đó.
- Dashboard dùng XxxDB hiện có, phiên gần nhất LIMIT1 theo owner; bean giữ điểm0
  và các trạng thái rỗng. Đổi login/signup redirect tới /account sau thành công.
- Catalog nối robot, quiz, nối dây có bài PUBLISHED, tra cứu; linh kiện có robot
  sử dụng, lượng kit riêng và SKU. Thông báo sai/tray ở hai phòng có link linh kiện.
  Trang hoàn thiện chỉ gợi ý hướng dẫn nếu tìm thấy liên quan lỗi học tập nhiều nhất.
- Hồ sơ có POST csrfToken/designId; CartDB khóa user/design/kho, dùng lại giới hạn
  giỏ, DesignCartResult chỉ bù thiếu. PRG sang giỏ, từng mục có thông báo, không
  checkout/giảm kho. Bấm hai lần giữ nguyên số lượng; giỏ QA đã dọn về rỗng.
- Course map công khai15 chủ đề, file:dòng đã đối chiếu, in được; không bịa số slide.
  Link minh họa xử lý lỗi cố ý mở404 có tên chủ đề, không phải menu hỏng.
- web.xml404/500/Throwable → JSP; ERROR filter giữ API JSON. AdminPageFilter bảo
  vệ cả5 HTML quản trị ở server. Breadcrumb lấy route gốc trước forward.
  WorkspaceCacheFilter bổ sung no-store cho tổng kết, đơn hàng và thống kê cũ
  (baseline chưa có), cùng hai catalog có menu theo session. Không sửa file WIP.

## Bảng kiểm HTTP và bảo mật

| Tình huống | Kết quả thật |
| --- | --- |
| Mục menu theo khách/USER/ADMIN | 200 hoặc302 về tài khoản; tất cả đích tồn tại |
| USER vào /pages/admin-users.html, /admin-tasks, /admin-wiring, /admin-stats | 403 ở server |
| /account khách | 302 /pages/tai-khoan.html |
| /design-sheet?id=7 của người khác, /assembly-review?session=31 của người khác | 404 |
| POST/design-cart thiếu CSRF | 403 |
| POST/design-cart thiết kế khác chủ | 404 |
| POST/design-cart đúng chủ hai lần | 302 sang giỏ; lượng không cộng trùng, không vượt kho hiện tại |
| /robots và /components | 200, dải minh họa và link trang chính |
| /learning-profile | 200; file WIP nguyên byte |
| /learning-summary, /order-history, /admin-stats, /robots, /components | no-store, kiểm HTTP trên WAR cuối |
| URL HTML không tồn tại | 404, tiếng Việt, không exception/stack |
| Fault JSP chỉ trong runtime QA, đã xóa | 500, trang thân thiện, không tên class/stack |
| /api/robots?limit=0 | 422 JSON |
| /api/no-such-resource | 404 JSON |
| Replay phiên30 cũ và37 có nhật ký thật | GET-only;37 bước trước/sau/phát/tạm dừng;0 request ghi |
| QA7 tháo bánh → gắn vào vị trí Arduino | Bị từ chối, có tên và link đúng component=wheel |
| QA7 gắn lại đúng → check | Đạt10/10; phục hồi bố cục |

Trang404/500 được thử bằng lỗi container thật, không chỉ render HTML tĩnh.
Fixture gây500 chỉ tồn tại trong CATALINA_BASE QA, không có trong WAR/source/Git.
Các trường hợp expected401/403/422 là phản hồi quyền/luật, không phải asset404.

## Checklist trang × viewport (Chrome, server thật)

Mỗi ô đạt: không tràn ngang toàn trang,1h1, không trùng ID, menu đúng fixture theo
vai trò. 0 lỗi JS chưa xử lý (pageerror), 0 asset404. Không tuyên bố đã đọc từng
console warning: thư viện Three.js cũ có thể có cảnh báo; kiểm âm HTTP là có chủ đích.
Trang Xưởng không có design ID kiểm trạng thái thiếu thiết kế; QA7 có kiểm riêng
ở390/768/1440 và thao tác thật. Phòng mẫu dùng session30 đã hoàn tất để không sửa nó.

| Vai trò | Trang | 390 | 768 | 1440 |
| --- | --- | --- | --- | --- |
| guest | /index.html | Đạt | Đạt | Đạt |
| guest | /pages/cua-hang.html | Đạt | Đạt | Đạt |
| guest | /pages/gio-hang.html | Đạt | Đạt | Đạt |
| guest | /pages/kiem-tra.html | Đạt | Đạt | Đạt |
| guest | /pages/lap-rap-3d.html | Đạt | Đạt | Đạt |
| guest | /pages/lap-rap.html | Đạt | Đạt | Đạt |
| guest | /pages/linh-kien.html | Đạt | Đạt | Đạt |
| guest | /pages/mau-robot.html | Đạt | Đạt | Đạt |
| guest | /pages/tai-khoan.html | Đạt | Đạt | Đạt |
| guest | /pages/thanh-vien.html | Đạt | Đạt | Đạt |
| guest | /pages/thu-vien.html | Đạt | Đạt | Đạt |
| guest | /pages/tra-cuu-loi.html | Đạt | Đạt | Đạt |
| guest | /pages/xuong-thiet-ke.html | Đạt | Đạt | Đạt |
| guest | /architecture | Đạt | Đạt | Đạt |
| guest | /robots | Đạt | Đạt | Đạt |
| guest | /components | Đạt | Đạt | Đạt |
| user | /index.html | Đạt | Đạt | Đạt |
| user | /pages/cua-hang.html | Đạt | Đạt | Đạt |
| user | /pages/gio-hang.html | Đạt | Đạt | Đạt |
| user | /pages/kiem-tra.html | Đạt | Đạt | Đạt |
| user | /pages/lap-rap-3d.html | Đạt | Đạt | Đạt |
| user | /pages/lap-rap.html | Đạt | Đạt | Đạt |
| user | /pages/linh-kien.html | Đạt | Đạt | Đạt |
| user | /pages/mau-robot.html | Đạt | Đạt | Đạt |
| user | /pages/tai-khoan.html | Đạt | Đạt | Đạt |
| user | /pages/thanh-vien.html | Đạt | Đạt | Đạt |
| user | /pages/thu-vien.html | Đạt | Đạt | Đạt |
| user | /pages/tra-cuu-loi.html | Đạt | Đạt | Đạt |
| user | /pages/xuong-thiet-ke.html | Đạt | Đạt | Đạt |
| user | /account | Đạt | Đạt | Đạt |
| user | /architecture | Đạt | Đạt | Đạt |
| user | /robots | Đạt | Đạt | Đạt |
| user | /components | Đạt | Đạt | Đạt |
| user | /learning-summary | Đạt | Đạt | Đạt |
| user | /learning-profile | Đạt | Đạt | Đạt |
| user | /tasks | Đạt | Đạt | Đạt |
| user | /wiring | Đạt | Đạt | Đạt |
| user | /wiring-support | Đạt | Đạt | Đạt |
| user | /diagnosis | Đạt | Đạt | Đạt |
| user | /my-kit | Đạt | Đạt | Đạt |
| user | /designs | Đạt | Đạt | Đạt |
| user | /order-history | Đạt | Đạt | Đạt |
| user | /assembly-review?session=30 | Đạt | Đạt | Đạt |
| user | /assembly-receipt?session=30 | Đạt | Đạt | Đạt |
| admin | /index.html | Đạt | Đạt | Đạt |
| admin | /pages/admin-content.html | Đạt | Đạt | Đạt |
| admin | /pages/admin-quiz.html | Đạt | Đạt | Đạt |
| admin | /pages/admin-shop.html | Đạt | Đạt | Đạt |
| admin | /pages/admin-troubleshooting.html | Đạt | Đạt | Đạt |
| admin | /pages/admin-users.html | Đạt | Đạt | Đạt |
| admin | /pages/cua-hang.html | Đạt | Đạt | Đạt |
| admin | /pages/gio-hang.html | Đạt | Đạt | Đạt |
| admin | /pages/kiem-tra.html | Đạt | Đạt | Đạt |
| admin | /pages/lap-rap-3d.html | Đạt | Đạt | Đạt |
| admin | /pages/lap-rap.html | Đạt | Đạt | Đạt |
| admin | /pages/linh-kien.html | Đạt | Đạt | Đạt |
| admin | /pages/mau-robot.html | Đạt | Đạt | Đạt |
| admin | /pages/tai-khoan.html | Đạt | Đạt | Đạt |
| admin | /pages/thanh-vien.html | Đạt | Đạt | Đạt |
| admin | /pages/thu-vien.html | Đạt | Đạt | Đạt |
| admin | /pages/tra-cuu-loi.html | Đạt | Đạt | Đạt |
| admin | /pages/xuong-thiet-ke.html | Đạt | Đạt | Đạt |
| admin | /account | Đạt | Đạt | Đạt |
| admin | /architecture | Đạt | Đạt | Đạt |
| admin | /admin-tasks | Đạt | Đạt | Đạt |
| admin | /admin-wiring | Đạt | Đạt | Đạt |
| admin | /admin-wiring-support | Đạt | Đạt | Đạt |
| admin | /admin-diagnosis | Đạt | Đạt | Đạt |
| admin | /admin-stats | Đạt | Đạt | Đạt |

Kiểm thêm: menu sidebar thu gọn, Enter mở nhóm, Escape đóng và trả focus,
link44px; aria-current của /account đúng sau forward. Không sửa layout phòng3D
hoặc thêm panel bước; không đổi style.css/font/bảng màu chung.

## Ảnh và bản in

Ảnh trước/sau ngoài Git: D:/Web-Pro/qa-evidence/phase11/before/ (30 ảnh), after/
(29 ảnh), m4/ và m1/. Có cặp1440/390 cho trang chủ/dashboard/bản đồ/catalog/tổng kết
đại diện; có ảnh QA7 và trạng thái từ chối. Ảnh chỉ là bằng chứng UI, không thay
cho assertion HTTP hoặc kiểm quyền.

PDF Chrome thật: course-map.pdf 4 trang; design-sheet.pdf 3 trang. Đã raster và xem
ảnh toàn bộ7 trang; nền trắng/chữ đen, không menu, bảng/chú giải còn đủ, form bổ sung
giỏ không xuất hiện khi in. File PDF/ảnh không commit. Không tạo/sửa bản PDF WIP của Tài.

## Dữ liệu QA và giới hạn

- Không tạo tài khoản/đơn hàng/checkout mới. Order2 cũ và tồn kho giữ nguyên.
- Luồng chuẩn bị cũ tự tạo hai phiên nháp GROUP39(USERdemo13) và40(ADMIN QA22),
  line-follower; không đổi phiên đã hoàn tất16/30/31/33/34/35/37. Để lại39/40 cho
  người dùng quản lý, không xóa bằng SQL. Phiên38 của user7 không đụng.
- QA7: tháo/gắn lại một bánh, thêm một lần từ chối, kiểm lại đạt; placement cuối
  cùng như trước, layout_version/nhật ký/thời điểm kiểm đổi do API bình thường.
  CartQA23 thêm/thử lại rồi xóa chính dòng thử qua API, cuối rỗng; không sửa kit/kho.
- Kiểm ràng buộc giỏ thiếu kho/giới hạn bằng JUnit; HTTP thực với tồn kho hiện có.
  Không đổi sản phẩm thật sang hết hàng/ngừng bán để ép trạng thái.
- Hồi quy GET/render/menu và các hành vi đại diện; không chạy lại mọi POST nghiệp
  vụ (quiz/nhiệm vụ/chẩn đoán/nối dây/admin CRUD/checkout) vì không đổi chúng.
- Chưa kiểm Firefox/Safari, cảm ứng vật lý, screen reader hoặc hệ điều hành khác.
  Không khẳng định đã kiểm mọi tổ hợp focus/zoom hoặc mọi dữ liệu tùy ý.

## Hợp đồng và test cũ

Mọi fixture form cũ giữ nguyên. Chỉ thêm đúng một form vào design-sheet.jsp, giữ
nguyên nút In và các binding trước. Guard thư viện nay nhận đường Servlet có thật
ngoài file tĩnh; không chấp nhận href không tồn tại. Guard JSTL của bài luyện thêm
c:url/c:param theo yêu cầu Đợt11, vẫn chặn scriptlet/fmt/fn/select và cú pháp lạ.
Guard shell nhận asset URL qua c:url/c:param và thêm đúng3 view; mọi view cũ vẫn
được quét. Test greeting chuyển sang nhãn menu thống nhất, không nới kiểm quyền.
Các guard nghiệp vụ/CSRF/course-style/phòng3D/replay cũ vẫn xanh.

## Commit

| Mốc | Commit |
| --- | --- |
| M0 | 4767a86 — map existing routes and plan connected navigation |
| M1 | e374ed7 — unify role-aware navigation across static and JSP pages |
| M2 + M6 | e406f32 — clarify canonical pages and retain Model 2 examples |
| M3 | ef703d8 — show owned learning and design activity on the dashboard |
| M4 | c1856b8 — connect lessons and add missing design materials to the cart |
| M5 | 3e25cdd — map course concepts to working features and source locations |
| M7 | 45db37b — align role navigation breadcrumbs and friendly error pages |
| M8 | Commit chứa báo cáo này, chốt bằng chứng và chỉnh nhãn/đường đi cuối |

Không push. CATALINA_BASE8081 riêng được dừng đúng PID và dọn config/log/cookie
context; không can thiệp Tomcat8080. Bằng chứng test/HTML/PDF/ảnh ở ngoài repo.
10 file WIP/untracked không stage; hash cuối so baseline10/10.

Các file người dùng còn lại sau khi commit phần Đợt11 (đã đối chiếu nguyên byte):

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
