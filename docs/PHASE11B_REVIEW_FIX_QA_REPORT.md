# Đợt 11b — sửa review và xác minh bản demo

Ngày kiểm: 15/10/2026. Nhánh `integration/fullstack-v2`, baseline `99e8337`.
Không push. Không thay schema, quyền, rubric, published rules hay luồng checkout.
Review độc lập Đợt11 được giữ nguyên; kết quả dưới đây là kiểm lại trong lượt này.

## Năm vấn đề và kết quả

| Vấn đề | Tái hiện/nguyên nhân | Sửa và bằng chứng |
| --- | --- | --- |
| Artifact lệch source/JDK | Repo gốc 204 Node đạt, 1 lỗi byte exploded, 0 skip. Sau một clean build17, package không clean gặp class69 | Clean package bằng17; xác định docBase IDE thật; kiểm chính target qua QA8081. 177 class chính và26 class test đều major61;10 resource HTTP khớp byte source |
| Deep link linh kiện | Tra cứu dùng `#l298n`, context dùng `component-l298n`; focus phụ thuộc API context và lặp mỗi render | Một helper canonical query+hash; nhận hash cũ, chờ catalog/fallback, focus đúng thẻ một lần, báo thiếu/sai URL bằng chữ an toàn. Node và Chrome kiểm tải chậm/lỗi/ký tự đặc biệt |
| Link ngữ cảnh sai quyền | ADMIN/khách được dựng nút luyện tập trong nội dung trước xác minh quyền | Dùng trạng thái auth từ GET server của menu; USER mới hiện nút. JSP kiểm session bằng JSTL. Pending/lỗi đóng quyền; backend giữ403 cũ. Lỗi đọc giỏ không biến người đã xác thực thành khách |
| SKU ngoài trang1 | Browser fixture21SKU: SKU21 không có trong DOM trang1 | GET một SKU đang bán theo khóa chính, không tải tuần tự catalog. Khu vực tập trung khi ngoài trang; không ID trùng, bỏ response cũ, phân biệt hết hàng/không bán/lỗi tải;0 request ghi |
| Nghiệp vụ dashboard ở Servlet | Chọn gợi ý/card/kit trong controller; gọi count quiz trong vòng lặp, tải cả danh sách thiết kế | DashboardSummary quyết định; Servlet đọc dữ kiện và forward. Một query gộp candidates, LIMIT1 thiết kế, COUNT/SUM nhiệm vụ.8 JUnit mới; bảy thẻ USER/ADMIN trước/sau giống nhau |

Không đổi method/action/form/CSRF/API lỗi cũ. Thêm đúng một GET additive; không có
migration/seed/dependency mới, không sửa vendor hay chỉnh artifact bằng tay.

## Source, commit và deploy là ba trạng thái riêng

| Trạng thái | Đã kiểm |
| --- | --- |
| A — working tree | Build17 trong thư mục riêng, sau đó clean package17 tại target gốc;216 Node/138 JUnit,0 lỗi/skip |
| B — HEAD sạch khởi đầu | Archive99e8337 ngoài repo, clean package17 và npm test:205 Node/130 JUnit,0 lỗi/skip; không mang WIP vào bản này |
| B — mã sửa đã commit | Các commit source ở bảng dưới; build sạch snapshot sau commit được ghi trong evidence `committed-build.log` và `committed-node.log` |
| C — artifact IDE được cấu hình | Đọc ROOT.xml của CATALINA_BASE IntelliJ, docBase là `D:/Web-Pro/robot-engine-website/tomcat-app/target/robot-assembly-lab`. Đã rebuild và kiểm HTTP chính docBase này bằng Tomcat QA riêng8081 |
| Phiên IntelliJ8080 của người dùng | Không chạy trong lúc khảo sát/QA; không dừng/khởi động/redeploy hoặc đổi cấu hình của phiên này. Lần Run8080 tiếp theo chưa được quan sát trực tiếp |

Ban đầu XML artifact duy nhất trong `.idea/artifacts` định nghĩa `war exploded2`
tại `out/artifacts`. Đây không phải bằng chứng artifact đang deploy: cấu hình
Run chọn `war exploded`, và ROOT.xml thực tế xác định target như trên. Không sửa
XML IDE để giải quyết nhầm đường dẫn. Bản QA ban đầu dùng WAR riêng; vòng cuối
dùng Context docBase trỏ thẳng target gốc, work/log/config nằm trong runtime QA.

### Vì sao class Java25 xuất hiện lại?

POM đặt `maven.compiler.release=17`, IntelliJ đặt languageLevel/JDK17. Tuy nhiên
metadata VS Code của chính dự án có compiler source/compliance/targetPlatform25,
classpath `JavaSE-25`, output `target/classes`; language server chạy bằng JRE25
đi kèm extension. Đây là compiler IDE khác Maven đang cùng ghi vào target.
Không kết luận chỉ từ phiên bản JRE của language server: đã kiểm cả preferences
và classpath dự án, cùng bytecode thực tế69.

Người dùng cho phép tạm dừng đúng language server. Đã xác minh PID41060 theo
executable của extension và launcher JDT, kiểm không phải Tomcat rồi mới dừng.
VS Code tự khởi động lại language server; không khẳng định nó còn dừng. Sau
clean build cuối, toàn bộ class đã kiểm đều major61 và các test gốc đạt.
Không đổi source/WIP để né lỗi, không nâng project lên25.

Đã trình xin phép cấu hình runtime JavaSE-17 cho workspace và nhập lại Maven.
Nếu chưa có quyết định riêng, không tự sửa settings/compiler cache/IDE. Cần
xác nhận lại compiler17 sau bước này để ngăn tái diễn; không thể cam kết chỉ
một lần clean sẽ ngăn IDE ghi lại class25 trong tương lai. Không ép JRE chạy
language server xuống17 nếu extension cần JRE mới; cần phân biệt JRE của công
cụ với target Java17 của project.

## Test và build thực chạy

JDK: Eclipse Adoptium17.0.19.10; Tomcat9.0.120; Maven3.9.15 hiện có. JAVA_HOME
đặt trong phiên lệnh, không đổi cấu hình hệ thống.

| Lần kiểm | Node | JUnit | Ghi chú |
| --- | --- | --- | --- |
| Repo gốc trước sửa |204 đạt/1 lỗi/0 skip|—|tomcat-encoding kiểm byte exploded thất bại |
| Working tree build riêng trước sửa |—|130 đạt/0 lỗi/skip|clean package17 đạt |
| Archive HEAD99e8337 |205 đạt/0 lỗi/skip|130 đạt/0 lỗi/skip|Bản commit sạch độc lập |
| M1 |208 đạt/0 lỗi/skip|130 đạt/0 lỗi/skip|Root target được clean build khi không bị dùng |
| M2 |209 đạt/0 lỗi/skip|130 đạt/0 lỗi/skip|Node dùng exploded riêng; không tuyên bố root đã sửa từ lần này |
| M3 |214 đạt/0 lỗi/skip|130 đạt/0 lỗi/skip|GET additive và ca SKU21 |
| M4 |215 đạt/0 lỗi/skip|138 đạt/0 lỗi/skip|8 JUnit mới; dashboard trước/sau giống nhau |
| Chốt source/root target |216 đạt/0 lỗi/skip|138 đạt/0 lỗi/skip|npm test mặc định tại repo; clean package17 đạt |

Có một lần `package` không clean thất bại vì class69. Log giữ trong evidence;
không xóa/nới test, không thêm skip hoặc đổi testbyte để che lỗi này. Kiểm Node
trong bản build riêng có đặt biến exploded chỉ nhằm kiểm bản riêng; số chốt
root dùng đường dẫn mặc định, không override.11 Node và8 JUnit được bổ sung.

## Kiểm HTTP/browser

Bằng chứng ngoài Git: `D:/Web-Pro/qa-evidence/phase11b/`.

| Nhóm | Kết quả | Nguồn dữ liệu/bằng chứng |
| --- | --- | --- |
| HTTP ba vai trò |69 kiểm mã HTTP đạt:49×200,11×302,5×403,4×404|DB thật, `real-qa.json` |
| Asset/deploy |10/10 file HTTP khớp từng byte source; menu và cache version mới|`real-qa.json`, QA dùng docBase target gốc |
| Các trang sửa, ba vai trò |42/42 ô 390/768/1440 không tràn ngang;0 pageerror/asset404|Catalog linh kiện/robot, tra cứu, shop, dashboard; `real-qa.json` |
| Fixture browser |37/37 ca đạt, không request ghi|21SKU, legacy/canonical, fallback/slow/error/special ID, auth pending/503, race/reload/stock/ID trùng, menu Enter/Escape; `final-original-browser.json` |
| Link phòng mẫu/Xưởng |4/4 ca USER/ADMIN đạt; bấm tài liệu tới đúng thẻ|Layout từng phần chỉ sửa trong response browser; `room-context.json` |
| Dashboard USER/ADMIN |Bảy thẻ trước/sau refactor giống nhau|DB thật; `before-dashboard.json`, `after-dashboard.json` |
| Replay phiên37 |Bước sau/Phát/Tạm dừng hoạt động,0 request ghi|Phiên cũ của student; `replay-readonly.json` |
| Dữ liệu DB |55/55 bảng giữ hash trong toàn bộ QA|Snapshot SELECT chỉ lưu SHA-256, không lưu dữ liệu/bí mật |

HTTP cụ thể: khách `/account` và route cá nhân302; USER mở admin403; ADMIN mở
`/tasks`, `/wiring`, `/diagnosis`403 đúng luật cũ. Review16/30 của builder200,
review31 khác chủ404. GET SKU đang bán200 và data giống sản phẩm trong list;
SKU không tồn tại404 JSON. Đã bấm link cửa hàng từ hồ sơ thiết kế7 tới thẻ
sản phẩm thật. Không bấm GET trang chuẩn bị thiếu session để tránh tạo phiên.

Ảnh trước là trang từ bản HEAD sạch99e8337 trên QA8082, không phải ảnh của
exploded cũ bị lệch byte. Ảnh `before-390-*`, `before-1440-*` và ảnh sau
`final-original-390-*`, `final-original-1440-*`, `real-*` nằm ngoài repo. Đã xem
ảnh shop mobile, catalog desktop và dashboard mobile. Không commit ảnh/token.

## Hồi quy, giới hạn và an toàn

- Smoke GET thật: tasks, account, learning-profile, wiring, diagnosis, robots,
  components, summary, kit, designs, orders, admin-stats/admin-shop. Phòng mẫu
  hoàn tất, Xưởng7, review và replay dùng dữ liệu có sẵn, không tạo phiên mới.
- Quiz/checkout/ghi nối dây/nhiệm vụ và POST bổ sung giỏ: giữ test cũ, kiểm source;
  không chạy thao tác ghi thật. Không tuyên bố đã kiểm E2E ghi các module này.
- SKU21/hết hàng/ngừng bán/503/race dùng fixture browser/Node; không seed hoặc
  đổi sản phẩm/tồn kho để tạo tình huống. Backend mới được kiểm contract source
  và GET thật cho SKU hiện có/không tồn tại; không giả lập lỗi DB thật bằng cách
  làm gián đoạn DB. Không kiểm500 bằng cách phá runtime.
- Không kiểm Firefox/Safari, thiết bị cảm ứng vật lý hoặc screen reader.
- Không sửa/xóa QA7, phiên39/40 hay bất kỳ dữ liệu học tập/đơn/giỏ nào. Chỉ POST
  đăng nhập tài khoản đã có; không tạo tài khoản, không đổi role/mật khẩu.
- 10/10 file WIP/chưa theo dõi giữ nguyên SHA-256 đầu lượt. Không stage chúng.
  Không đổi cấu hình IDE khi chưa được duyệt. QA runtime8081/8082, cấu hình/log/
  work/temp chứa thông tin nhạy cảm được dọn bằng đúng PID của runtime tự tạo;
 8080 không bị tác động. Kết quả cleanup nằm ngoài Git.
- Tài liệu mới từng bị pipeline PowerShell mất dấu; đã viết lại UTF-8, đọc kiểm
  các heading/nội dung Đợt11b trước commit cuối. Không sửa lịch sử review cũ.

## Commit

| Hash | Message |
| --- | --- |
|55af8c0|fix(ux): resolve component deep links after catalog loading|
|f315a70|fix(ux): align contextual learning links with server roles|
|7f3f0eb|fix(shop): resolve product deep links beyond the first page|
|18a1c6b|refactor(ux): move dashboard decisions into JavaBeans|
|be2df6d|fix(shop): distinguish unavailable authentication from guest access|

Commit tài liệu/QA cuối dùng message `docs(qa): record phase11b verification and deployment status`;
hash được báo khi bàn giao. Không amend/rebase/reset, không push.

## Đoạn giải thích để trình bày

Trình duyệt gửi HTTP request; Servlet đọc tham số và danh tính từ session.
Servlet gọi XxxDB đọc dữ kiện đúng chủ bằng PreparedStatement có giới hạn.
JavaBean quyết định gợi ý, trạng thái và nội dung thẻ rồi Servlet đặt bean vào
request bằng setAttribute. JSP dùng EL/JSTL hiển thị sau forward, không truy vấn
DB hay quyết định nghiệp vụ. Link theo ngữ cảnh giữ đúng linh kiện/robot, nhưng
quyền vẫn được kiểm ở server kể cả khi giao diện đã ẩn nút. Source đúng cần được
build bằng Java17 và deploy đúng artifact; kiểm byte HTTP giúp chứng minh bản
demo đang phục vụ đúng phiên bản.

## Chốt nghiệm thu — 16/10/2026

Phạm vi chỉ gồm cache-busting và tên điều hướng; không sửa Java/JS/CSS nghiệp vụ,
route, schema, phân quyền hay dữ liệu. HEAD đầu lượt24819e3, đúng nhánh tích hợp.
Hash10 WIP/chưa theo dõi cùng POM/settings workspace được chụp lại đầu lượt này.

- JSP từng nạp site-nav.js v20261015.3, HTML nạp v20261015.5. Đồng bộ fragment
  JSP về20261015.5; giữ nguyên JS. Test mới đối chiếu version giữa fragment và
  mọi HTML để bắt lệch sau này.
- Ba trang cua-hang/gio-hang/admin-shop có hai nav cùng tên “Điều hướng chính”.
  Giữ menu site-nav làm điều hướng chính duy nhất; nav cũ trong shop mang tên
  “Liên kết cửa hàng”, có chữ hiển thị cùng tên. Không xóa/chuyển link, ID hoặc
  hook hiện có. So inventory trước/sau:0 khác biệt ở link, control/form và id/data.
- Hai test mới thất bại trước sửa, đạt sau sửa. Toàn bộ Node218/218 và
  JUnit138/138 đạt,0 lỗi/skip; Maven test và clean package bằng17 thành công.
- Chrome thật:21/21 ô ở390/768/1440 trên ba trang shop theo vai trò hợp lệ;
  kiểm menu Enter/Tab/Escape, focus nhìn thấy và trở về summary, Enter vào link
  cửa hàng, ID không trùng, không tràn ngang.0 pageerror/asset404/request ghi.
  JSP/account của USER và ADMIN thực sự trả script v20261015.5. Quyền trang
  admin-shop giữ khách302, USER403, ADMIN200. Hash55 bảng DB trước/sau giữ nguyên.
- Tomcat QA8081 đã dọn; không dừng/khởi động Tomcat8080 hoặc đóng IDE của người dùng.
  Mục mở lại VS Code và Run IntelliJ8080 đang chờ người dùng thực hiện/xác nhận;
  không dùng kiểm QA8081 để thay cho bước này.

Bằng chứng: `D:/Web-Pro/qa-evidence/phase11b/acceptance-20261016/` gồm baseline,
hai regression test trước sửa, build/test cuối, browser-results.json, ảnh theo
vai trò/viewport, markup-contracts.json và hash DB/WIP. Cấu hình Java17 cục bộ
đã được sửa và kiểm bằng restart language server ở lượt trước; chưa sửa thêm
cấu hình IDE trong lượt chốt này.

### Xác nhận môi trường người dùng — 16/10/2026, sau khi Run IntelliJ

Người dùng xác nhận đã mở lại VS Code và Run Tomcat8080. Kiểm tra trực tiếp
bản đang chạy, chỉ GET/đọc file; không build vào target gốc, không đóng IDE,
không dừng/redeploy server. Commit source được đối chiếu là `4fb451d`.

| Hạng mục | Kết quả thực tế |
| --- | --- |
| Compiler dự án trong VS Code | source/compliance/target đều17; classpath JavaSE-17 |
| Bytecode target/classes và test-classes |177 và26 class, toàn bộ major61 (Java17) |
| Bytecode WEB-INF/classes đang deploy |177 class, toàn bộ major61 |
| IntelliJ/Tomcat8080 |JDK17; ROOT.xml trỏ target/robot-assembly-lab |
| HTTP index, cửa hàng, giỏ hàng, site-nav.js |4/4 trả200, byte khớp source |
| JSP /architecture |200; script site-nav.js?v=20261015.5 |
| Fragment JSP và HTML admin-shop đã deploy |Khớp byte source; không cần đăng nhập để đọc artifact cục bộ |
| Chrome8080, khách, shop/giỏ |6/6 ô390/768/1440; catalog tải được, menu Enter/Tab/Escape đạt |
| Điều hướng và lỗi trình duyệt |Một nav chính, nav cửa hàng riêng; không tràn ngang, không lỗi JS/asset404/request ghi |
| Test sau Run |218/218 Node;138/138 JUnit,0 fail/skip; clean package JDK17 đạt |
| Hash bảo vệ |10 WIP/chưa theo dõi cùng POM/settings workspace:12/12 giữ nguyên |

Maven chạy vào thư mục `reopened-build` ngoài repo để không tác động artifact
đang được8080 phục vụ. Node kiểm exploded tại target gốc vẫn đạt. Sau kiểm,
Tomcat của người dùng tiếp tục chạy; không tạo runtime QA mới ở lượt xác nhận.

Language server dùng JRE25 của extension để chạy công cụ; target của project
vẫn17. PID language server không đổi so với trước xác nhận: đã kiểm được trạng
thái sau khi người dùng mở cửa sổ, không coi đây là bằng chứng một lần khởi động
lại hoàn toàn tiến trình language server. Việc restart/full compilation trước
đó đã có bằng chứng riêng trong `ide-java17`. Lượt8080 này chỉ kiểm khách và
JSP công khai; USER/ADMIN đã kiểm ở21 ô QA8081 nêu trên, không đăng nhập/ghi DB
lại để mở rộng phạm vi. Giới hạn Firefox/Safari/cảm ứng thật/screen reader giữ nguyên.

Bằng chứng bổ sung trong cùng thư mục nghiệm thu: `manual-verification.json`,
ảnh `manual-8080-*`, `node-after-user-run.log`, `build-after-user-run.log` và
Surefire XML trong `reopened-build/surefire-reports`. Không lưu cookie/token
hay cấu hình DB trong báo cáo. Hai lỗi của bộ kiểm tra ngoài repo (chuẩn hóa
URI workspace Windows và nhận URL có phần đường dẫn phiên do Tomcat thêm)
đã được sửa ở runner; không thay source hoặc test dự án để bỏ qua kiểm chứng.
