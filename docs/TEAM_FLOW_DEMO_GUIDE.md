# Kịch bản demo và vấn đáp Robot Assembly Lab — 15–20 phút

Đây là lời trình bày theo hành trình người học, không phải danh sách tính năng.
Mỗi chặng có **mở trang → thao tác → lời nói → kết quả cần chỉ → cô hỏi → mở code**.
Chỉ đọc lời nói; câu vấn đáp dùng ngay tại chặng đó khi cô hỏi. Không đọc toàn bộ câu hỏi dự phòng.

## 0. Chuẩn bị để demo không bị đứt luồng

### Môi trường và tài khoản

- Dùng cấu hình duy nhất **Tomcat 9.0.120**, Java17, context `/`, tại **http://localhost:8080/**.
  Không mở helper Start hoặc đổi sang8081 giữa buổi. DB/tài khoản ở hai cổng có thể khác nhau.
- Một cửa sổ trình duyệt đăng nhập USER để thực hành; một cửa sổ riêng/context riêng đăng nhập ADMIN.
  Hai tab thường cùng profile dùng chung cookie: đăng nhập ADMIN ở tab thứ hai có thể đổi cả tab USER.
- Mở IDE sẵn các file trong từng chặng. Liên kết dưới đây ghi `file:dòng`; nếu không nhảy dòng,
  dùng `Ctrl+G` rồi tìm tên method. Số dòng đã cập nhật sau khi thêm comment ngày18/10/2026, không phải số slide.
- Không hiện mật khẩu, token, `.env`, VM options hoặc toàn bộ cấu hình kết nối lên màn hình trình bày.

### Dữ liệu người trình bày cần chuẩn bị bằng luồng thật

| Chuẩn bị | Để làm gì | Điều kiện |
|---|---|---|
| Một phiên Tránh vật cản đang thực hành | Kéo-thả sai/đúng và hoàn tất live | Phiên riêng của USER, còn sửa được; dùng spec v2 cho bảng hướng bên dưới |
| Một kết quả hoàn tất dự phòng của cùng USER | Không mất phần kết quả nếu thao tác live chậm | Lấy link từ lịch sử/tổng kết, không đoán ID hoặc sửa DB giả hoàn tất |
| Một thiết kế Xưởng đã lắp đủ bằng luồng thật | Kiểm và mở hồ sơ trong2–3 phút | Đúng owner; nếu có thiết kế đang làm thiếu driver thì dùng minh họa FAIL |
| Một bài nối dây và một tình huống chẩn đoán chính thức | Chỉ một thao tác và một kết quả | Dùng lượt luyện riêng, không dùng lượt nhiệm vụ đang tính điểm làm bản thử |
| Một nhiệm vụ và bài nộp/phản hồi thật đã chuẩn bị | Thấy đủ ADMIN → USER → phản hồi | Giữ bài nộp/vòng/owner hợp lệ; không hứa điểm tối đa hoặc kết luận Đạt |

Đây là checklist cho người trình bày, **không khẳng định các bản ghi đó đã được tạo ở lần viết script này**.
Không reset phiên live/dự phòng. Không dùng SQL đặt điểm hoặc trạng thái. Nếu thiếu dữ liệu,
dùng kết quả có sẵn và nói rõ phần nào là đọc lại, phần nào đang thao tác trực tiếp.
DB8080 còn bài công bố và nhiệm vụ mang tên QA: chọn đúng nội dung chính thức;
không tự xóa/ẩn. Đổi tài khoản không làm sạch danh mục công bố dùng chung.

### Chia thời gian

| Phút | Chặng | Cô cần thấy |
|---|---|---|
| 0:00–0:50 | 1. Mục tiêu website | Phục vụ ai, giải quyết việc gì |
| 0:50–2:00 | 2. Chọn mẫu, hiểu linh kiện | Người mới biết bắt đầu từ đâu |
| 2:00–3:00 | 3. Tài khoản và chuẩn bị | Dữ liệu riêng, chuẩn bị khác lắp xong |
| 3:00–7:00 | 4. Lắp có kiểm chứng | Kéo-thả thật, sai/đúng, hướng/cha, undo, lưu |
| 7:00–9:30 | 5. Kết quả và quiz | Đủ bốn yêu cầu sau hoàn tất, mô hình đúng phiên |
| 9:30–12:00 | 6. Xưởng, vật tư và hồ sơ | Tự chọn linh kiện, kiểm tương thích, tham khảo lắp thật |
| 12:00–14:00 | 7. Nối dây, tra cứu, chẩn đoán | Các bài phụ giúp hiểu/sửa vấn đề sau lắp |
| 14:00–16:00 | 8. Nhiệm vụ và phản hồi | Giảng viên giao, USER nộp, ADMIN đánh giá |
| 16:00–17:00 | 9. Tổng kết, tài nguyên, quản trị | Học tiếp, xem kết quả, duy trì nội dung |
| 17:00–18:00 | 10. Chỉ một vòng code và chốt | Kiến thức Servlet/JSP/JavaBean/JDBC có trong chức năng thật |
| 18:00–20:00 | Vấn đáp và dự phòng thao tác | Mở code đúng câu cô hỏi |

Nếu chỉ có15 phút: giữ chặng1–5 khoảng9 phút; Xưởng2 phút; bài luyện+nhiệm vụ2 phút;
tổng kết/code2 phút. Dùng lượt/bài nộp đã có, không làm lại cả quiz hoặc chấm cả bài nối dây.
Không cắt phần mô hình kết quả, quy trình, phiếu và quiz sau hoàn tất.

## 1. Mở đầu: website chính xác phục vụ việc gì? — 0:00–0:50

**Mở:** Trang chủ `/`. Chỉ đọc, chưa thao tác ghi.

**Nói nguyên đoạn:**

> Thưa cô, nhóm em trình bày Robot Assembly Lab, website hỗ trợ người mới học lắp ráp robot.
> Vấn đề nhóm em giải quyết là: người học có danh sách linh kiện nhưng chưa biết chọn bộ nào,
> lắp vào đâu, cần xoay hướng nào và làm sao xem lại kết quả của mình.
> Website có hai hướng chính. Thứ nhất là lắp theo mẫu có sẵn và được server kiểm tra từng thao tác.
> Thứ hai là tự chọn linh kiện trong Xưởng thiết kế, kiểm tra tương thích theo bộ luật được hỗ trợ.
> Sau khi hoàn thành mẫu, người học xem lại sản phẩm, quy trình đã làm, phiếu kết quả và làm quiz của mẫu đó.
> Em sẽ đi theo hành trình của một người học, từ chọn mẫu đến kết quả, rồi chỉ cách các chức năng phụ hỗ trợ hành trình này.

**Chỉ trên màn hình:** Nhóm Học, hai đường Mẫu robot và Xưởng thiết kế. Chưa đi vòng qua mọi menu.

**Cô hỏi: “Chức năng chính là bán hàng hay làm quiz?”**

> Dạ, chính là học lắp ráp có kiểm chứng và xem lại kết quả. Quiz kiểm tra kiến thức sau khi lắp.
> Cửa hàng giúp tham khảo/chuẩn bị vật tư và hiện là cửa hàng mô phỏng.

**Cô hỏi: “Robot lắp đúng trên đây thì ngoài đời chạy được chưa?”**

> Dạ chưa thể kết luận. Hệ thống kiểm luật lắp ghép và cấu hình cơ bản,
> không mô phỏng điện, vật lý hay firmware. Khi lắp thật vẫn phải kiểm nguồn, dây và các điều kiện thực tế.

**Chuyển:** “Đầu tiên em đứng ở vai trò người chưa biết chọn mẫu nào để học.”

## 2. Chọn mẫu và biết linh kiện dùng để làm gì — 0:50–2:00

**Bấm lần lượt:**

1. Học → **Mẫu robot**, `/pages/mau-robot.html`.
2. Chỉ năm mẫu; chọn **Robot tránh vật cản** làm ví dụ xuyên suốt.
3. Tài nguyên → **Linh kiện**, `/pages/linh-kien.html`; xem một động cơ, bánh xe hoặc cảm biến.
4. Chỉ mô tả/thông số và “Dùng trong các mẫu robot”; quay lại mẫu và bấm **Lắp mẫu này**.

**Nói khi bấm:**

> Người học bắt đầu từ danh mục mẫu, thay vì phải tự đoán cần mua và ghép những gì.
> Website hiện có năm mẫu: tránh vật cản, dò đường, cánh tay mini, dò line kết hợp tránh vật cản và quét hướng tránh vật cản.
> Em chọn tránh vật cản vì thấy rõ khung, hai động cơ, hai bánh và cảm biến khoảng cách.
> Khi chưa hiểu một chi tiết, người học có thể mở danh mục linh kiện để đọc chức năng và biết chi tiết đó dùng trong mẫu nào.
> Động cơ và bánh xe là hai vật thể riêng; có đủ linh kiện trong danh sách chưa có nghĩa là đã lắp thành công.
> Từ mẫu đã chọn, em bắt đầu luồng lắp ráp.

**Cô cần thấy:** Có điểm bắt đầu rõ ràng; link từ mẫu giữ đúng `model`, không chuyển sang mẫu mặc định khác.

**Cô hỏi: “Danh sách mẫu viết cứng ở giao diện à?”**

> Giao diện lấy dữ liệu catalog từ API. Servlet gọi lớp XxxDB để đọc MySQL rồi trả JSON.
> Phần tương tác HTML dùng JavaScript dựng giao diện; phía server còn có bản Servlet render JSP để minh họa Model2.

**Mở code nếu hỏi:**

- [RobotCatalogPageServlet.java:24](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/RobotCatalogPageServlet.java#L24)
  là luồng `/robots`; tìm `doGet`, `setAttribute`, `forward`.
- [RobotDB.java:126](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/RobotDB.java#L126), `selectRobots`:
  SQL ở tầng data, không ở JSP.
- [learning-context.js:17](../assets/js/learning-context.js#L17), `componentUrl`:
  link đi đúng linh kiện, mã hóa tham số URL.

**Chuyển:** “Để lưu bài của từng người và mở lại được, em đăng nhập bằng tài khoản người học.”

## 3. Đăng nhập, bảng điều khiển và chuẩn bị bộ linh kiện — 2:00–3:00

**Bấm:** Đăng nhập USER nếu chưa đăng nhập → `/account` → quay lại mẫu → trang chuẩn bị
`/pages/lap-rap.html?model=obstacle-avoider`. Chuẩn bị linh kiện theo UI và vào phòng3D.
Đăng nhập/chuẩn bị/bắt đầu phiên là thao tác ghi; nếu đã chuẩn bị trước, chỉ lại trạng thái và nói rõ.

**Nói:**

> Sau khi đăng nhập, bảng điều khiển cho em thấy phiên đang làm để tiếp tục và kết quả gần nhất để xem lại.
> Tiến độ được lưu riêng theo tài khoản. Nếu máy tải lại trang thì em vẫn đọc lại dữ liệu đã lưu ở server.
> Ở phần chuẩn bị, em xác nhận bộ linh kiện của mẫu. Đây là bước kiểm bộ đồ dùng trước khi thực hành,
> không phải đánh dấu hoàn thành lắp ráp. Khi vào phòng3D, em vẫn phải gắn từng vật thể.

**Cô hỏi: “Đổi userId trên URL có xem được bài người khác không?”**

> Dạ không. Server lấy user từ HttpSession; các truy vấn dữ liệu riêng có điều kiện user_id.
> ID trong URL chỉ xác định tài nguyên muốn mở, không chứng minh quyền sở hữu.

**Mở code:**

- [SessionUtil.java:47](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java#L47), `getCurrentUser`:
  đọc session và kiểm người dùng/session_version còn hợp lệ.
- [AccountPageServlet.java:45](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AccountPageServlet.java#L45):
  lấy dữ kiện theo user, gọi bean để dựng thẻ.
- [DashboardSummary.java:56](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/DashboardSummary.java#L56):
  `addAssemblyCards` quyết định nội dung/hành động thẻ theo dữ kiện.

**Cô hỏi: “Bấm lại có sinh phiên mới liên tục không?”**

> Đường vào thông thường ưu tiên phiên chi tiết đang mở của đúng người và mẫu.
> Server dùng cơ chế đồng bộ hiện có khi tạo/dùng lại phiên. Refresh không được tự nhân phiên vô hạn.

**Chuyển:** “Điểm chính của website nằm ở phần này: em phải lắp bằng thao tác thật và server có thể từ chối.”

## 4. Phòng lắp ráp: phải chứng minh được sai, đúng và lưu — 3:00–7:00

**Mở:** `/pages/lap-rap-3d.html?model=obstacle-avoider&session=...` từ nút của UI.
Giữ nguyên session do server trả về. Chỉ dùng phiên thực hành của mình, không reset phiên live/dự phòng.

### 4.1. Giới thiệu thao tác — khoảng20 giây

**Bấm:** Chỉ khay, điểm gắn và tiến độ. Kéo vùng trống để xoay camera; chỉ một vật thể có thể chọn/kéo.

> Đây là phòng lắp ráp chi tiết. Em nhìn thấy các vật thể trong khay và các điểm gắn của mẫu.
> Mỗi bánh và mỗi động cơ là một instance riêng. Kéo nền thì xoay góc nhìn;
> kéo vật thể là ý định lắp vật thể đó. Bản xem trước chỉ giúp thao tác,
> còn trạng thái đã lưu phải chờ phản hồi của server.

### 4.2. Minh họa thiếu cha — khoảng25 giây

**Bấm:** Khi động cơ trái chưa gắn, thử kéo bánh vào đúng điểm bánh trên động cơ trái.
Quan sát thông báo; chỉ tiến độ và thấy bánh vẫn chưa được lưu vào điểm đó.
Nếu phiên đã có động cơ, không tháo cha còn con để cố dựng ca này; chuyển sang hai ca dưới.

> Trường hợp này em chọn đúng loại bánh nhưng chưa có bộ phận cha là động cơ.
> Server từ chối nên không tăng tiến độ. Như vậy kiểm chứng không chỉ là thả gần một tọa độ.
> Em phải lắp theo quan hệ phụ thuộc phù hợp.

### 4.3. Minh họa sai loại — khoảng20 giây

**Bấm:** Sau khi có khung, thử đưa bánh vào điểm dành cho Arduino. Đọc câu báo có tên linh kiện/điểm gắn.

> Ở đây em cố đặt bánh vào chỗ của mạch Arduino. Server nhận ra sai loại linh kiện,
> giải thích chỗ sai và giữ nguyên bố cục đã lưu. Client không được tự coi thao tác kéo xong là lắp đúng.

### 4.4. Minh họa hướng và thao tác kéo-thả thành công — khoảng40 giây

**Bấm:** Gắn khung hướng0° nếu chưa có. Chọn động cơ trái hướng0° rồi kéo-thả vào điểm trái → bị từ chối.
Xoay90° bằng nút hoặc Q/E; **mouse down trên vật thể → kéo tới điểm → mouse up**.
Chờ xác nhận đã lưu, chỉ tiến độ thay đổi.

> Lần này loại linh kiện và điểm gắn đã đúng, nhưng hướng chưa đúng luật của mẫu.
> Em xoay động cơ sang90° rồi thả lại. Khi server chấp nhận, vật thể mới được lưu và tiến độ mới tăng.
> Cùng một giao diện nhưng ba lỗi khác nhau được giải thích riêng: sai loại, sai hướng và thiếu bộ phận cha.

**Cô hỏi: “Kéo xong thì JavaScript tự kiểm được rồi, cần Java làm gì?”**

> JavaScript gửi ý định gắn gồm phiên, instance, slot, hướng và phiên bản bố cục.
> Servlet kiểm đăng nhập, CSRF và tham số. Trong transaction, XxxDB đọc bố cục có khóa,
> gọi JavaBean kiểm luật rồi mới ghi. Người dùng sửa JavaScript hoặc request cũng không tự cấp kết quả đúng.

**Mở code theo đúng đường đi, chỉ mở khi cô hỏi:**

1. [assembly-3d.js:886](../assets/js/assembly-3d.js#L886): callback `onAttach` gọi API, không tự chấm.
2. [AssemblySessionServlet.java:434](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblySessionServlet.java#L434):
   CSRF; [dòng467](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblySessionServlet.java#L467): đọc ý định gắn.
3. [AssemblyPlacementDB.java:169](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblyPlacementDB.java#L169), `change`:
   một connection/transaction, đọc layout và gọi bean.
4. [AssemblyLayout.java:233](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyLayout.java#L233), `validateAttach`:
   loại linh kiện, hướng, chỗ đã bị chiếm và cha.

### 4.5. Lắp phần còn lại — khoảng60–90 giây

Không đọc thuộc từng tọa độ. Bảng dưới để người thao tác tránh chọn sai khi demo,
chỉ áp dụng **Tránh vật cản spec v2**; không coi là luật chung cho năm mẫu hoặc phiên v1.

| Thứ tự thuận tiện | Vật thể → điểm gắn | Một hướng hợp lệ | Phụ thuộc |
|---|---|---|---|
| 1 | Khung → nền | 0° | Không có cha |
| 2 | Động cơ thứ nhất → bên trái | 90° | Khung |
| 3 | Động cơ thứ hai → bên phải | 270° | Khung |
| 4 | Bánh thứ nhất → trục trái | 0° | Động cơ trái |
| 5 | Bánh thứ hai → trục phải | 180° | Động cơ phải |
| 6 | Arduino → mặt khung dành cho Arduino | 0° | Khung |
| 7 | L298N → điểm của mạch điều khiển động cơ | 0° | Khung |
| 8 | Hộp pin → phía sau | 90° | Khung |
| 9 | Bánh tự do → dưới khung | 0° | Khung |
| 10 | HC-SR04 → phía trước | 180° | Khung |

> Em tiếp tục gắn những bộ phận còn lại. Luật của mẫu cho phép người học chủ động thao tác,
> nhưng vẫn phải tôn trọng những bộ phận phụ thuộc. Số điểm cần lắp lấy từ spec của phiên,
> không có con số cố định10 áp dụng cho tất cả các robot.

### 4.6. Undo, tải lại và hoàn tất — khoảng35 giây

**Bấm:** Gắn cảm biến hoặc một chi tiết lá → **Hoàn tác** → gắn lại. Tải lại để thấy placements còn.
Bấm **Hoàn tất** sau khi đủ; nếu đang thiếu thì chỉ thông báo server chưa chấp nhận.

> Hoàn tác cũng là yêu cầu server kiểm lại, không chỉ xóa hình trên màn hình.
> Sau khi tải lại, bố cục đọc từ dữ liệu đã lưu vẫn còn. Khi bấm hoàn tất,
> server kiểm đủ các điểm bắt buộc và tính hợp lệ của toàn bố cục rồi mới xác nhận.
> Phiên đã hoàn tất được giữ để xem kết quả, không tiếp tục sửa như phiên đang lắp.

**Cô hỏi: “Hai tab sửa cùng lúc thì sao?”**

> Client gửi expectedLayoutVersion. Server so với version hiện tại dưới khóa;
> tab cũ nhận409 xung đột để tải lại, không âm thầm ghi đè thao tác mới.

**Code:** [AssemblyLayout.java:247](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyLayout.java#L247),
kiểm version; [AssemblyPlacementDB.java:311](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblyPlacementDB.java#L311),
lọc user và `FOR UPDATE`; [AssemblyLayout.java:316](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyLayout.java#L316), `check`.

**Cô hỏi: “Sao không tháo động cơ trước rồi tháo bánh sau?”**

> Hệ thống chặn tháo cha khi còn con để bố cục không bị treo bộ phận.
> `validateDetach` ở [AssemblyLayout.java:271](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyLayout.java#L271).

**Cô hỏi: “Hướng trái/phải có phải mô phỏng chính xác trục động cơ không?”**

> Dạ không.90°/270° là quy ước đối xứng trái/phải của mô hình giản lược;
> hình khối động cơ không mô phỏng chính xác chiều trục. Hướng lắp servo cũng không phải góc điều khiển servo thật.

**Chuyển:** “Hoàn tất chưa phải điểm dừng. Phần tiếp theo cho người học xem lại mình đã làm gì và học tiếp.”

## 5. Trang hoàn thiện đáp ứng đủ bốn yêu cầu, rồi quiz — 7:00–9:30

**Bấm:** **Xem kết quả** → `/assembly-review?session=...`.
Nếu dùng kết quả dự phòng, nói “Em mở một phiên đã hoàn tất trước để minh họa phần xem lại”.
Không nhận kết quả cũ là vừa tạo trong lượt live.

### 5.1. Mẫu hoàn chỉnh

**Bấm:** Xoay mô hình, phóng to, căn giữa; chỉ mã phiên/spec và bản vẽ2D bên dưới.

> Phần đầu hiển thị mô hình3D của chính phiên em đã lắp, dựa trên các vật thể và hướng đã lưu.
> Em có thể xoay góc nhìn và phóng to nhưng không gắn, tháo hay thay kết quả ở đây.
> Ảnh catalog vẫn giữ nhưng ghi rõ là ảnh tham khảo; bản vẽ2D dùng để đọc/in khi cần.

**Cô hỏi: “Có phải chỉ dựng mô hình mẫu theo robotId không?”**

> Không. Servlet đọc phiên theo id và user từ session; layout lấy spec_id gắn với phiên,
> cùng instances và final placements. Một phiên lịch sử không bị dựng lại bằng spec mới nhất.

**Code:** [AssemblyReviewPageServlet.java:40](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblyReviewPageServlet.java#L40),
user; dòng47 đọc phiên theo owner, dòng78 đọc layout;
[AssemblyReview.java:36](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyReview.java#L36), kiểm dữ kiện hiển thị.

### 5.2. Quy trình chuẩn và quá trình thực sự đã làm

**Bấm:** Mục **Quy trình lắp ráp**; chỉ bước chuẩn, nhật ký gắn và lỗi lắp ghép. Mở replay,
bấm **Bước sau**, rồi quay về kết quả, không cần chờ phát toàn bộ.

> Người học xem hai nội dung khác nhau: quy trình chuẩn của mẫu để ôn lại,
> và trình tự mình đã thực hiện để biết đã gắn gì, ở đâu và theo hướng nào.
> Các lần cần sửa chỉ đếm lỗi lắp ghép của người học; xung đột tab hoặc lỗi request không coi là lỗi kiến thức.
> Phát lại cho xem quá trình; còn mô hình vừa thấy ở đầu trang là bố cục cuối đã lưu.

**Cô hỏi: “Tại sao ghi thao tác cùng transaction?”**

> Để placement và sự kiện được chấp nhận cùng thành công hoặc cùng rollback,
> tránh nhật ký nói đã gắn nhưng bố cục chưa lưu. Thao tác bị từ chối có thể được ghi riêng để truy vết.

**Code:** [AssemblyPlacementDB.java:278](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblyPlacementDB.java#L278),
insert event cùng connection rồi commit;
[AssemblyReview.java:211](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyReview.java#L211), `summarizeEvents`.

### 5.3. Phiếu kết quả

**Bấm:** Mục **Phiếu kết quả** → mở `/assembly-receipt?session=...`; chỉ số điểm hợp lệ,
thời điểm hoàn tất, rồi mở xem trước bản in nếu thời gian cho phép.

> Phiếu là kết quả của một phiên cụ thể, có các thông tin để đối chiếu và in lại.
> Đây không phải một ảnh bất kỳ hoặc điểm do trình duyệt tự gửi lên.
> Bản vẽ là giản lược theo mô phỏng, không phải bản vẽ kỹ thuật chính xác để chế tạo.

### 5.4. Kiểm tra kiến thức

**Bấm:** Từ trang hoàn thiện chọn **Làm bài kiểm tra**, kiểm đúng tên mẫu trên đề.
Chọn một vài đáp án để chỉ cách làm; nếu nộp live thì làm đầy đủ và chấp nhận điểm server chấm.
Nếu không đủ thời gian, mở lịch sử thật và nói rõ không nộp lượt mới.

> Sau thao tác lắp, người học làm quiz đúng mẫu vừa học, để kiểm tra hiểu chức năng và cách sử dụng linh kiện.
> Server đọc đáp án hợp lệ và chấm; client không được gửi một con số điểm rồi yêu cầu lưu.
> Lịch sử giúp người học xem lại lần làm và điểm của mình.

**Cô hỏi: “Quiz trong menu không có model thì sao?”**

> Nó là trang chọn bài. Người học chọn mẫu có bài hiện có rồi mới vào đề;
> không tự chọn mẫu đầu tiên hoặc tạo lượt làm khi chỉ mở trang.

**Code:** [QuizServlet.java:62](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/QuizServlet.java#L62), `doPost`;
[QuizAttemptDB.java:40](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/QuizAttemptDB.java#L40), `submitAttempt`;
[QuizQuestion.java:95](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/QuizQuestion.java#L95), `isCorrectOption`.
Mở đề không phải nộp bài; quiz PRACTICE không tự thành điểm của nhiệm vụ.

**Chuyển:** “Đó là luồng theo mẫu. Nếu người học có bộ linh kiện riêng thì website có hướng thứ hai.”

## 6. Xưởng thiết kế → kiểm tương thích → hồ sơ và vật tư — 9:30–12:00

### 6.1. Chọn linh kiện theo nhu cầu

**Bấm:** Học → **Xưởng thiết kế**, `/designs` → **Tạo thiết kế** để chỉ form.
Chọn mục tiêu **Robot tránh vật cản**; chỉ hai nguồn linh kiện và ô số lượng.
Nếu đã có thiết kế chuẩn bị sẵn, quay danh sách và mở nó; nói rõ không tạo mới lần này.

> Với Xưởng, người học không bắt buộc dùng nguyên bộ của một mẫu.
> Em chọn mục tiêu và số lượng linh kiện từ thư viện hoặc từ Bộ linh kiện của tôi.
> Nếu chọn từ bộ của tôi thì không được chọn vượt số đang có.
> Thiết kế lưu một bản chụp số lượng, nên đổi bộ linh kiện sau đó không làm thay đổi thiết kế cũ.
> Phạm vi là tự thiết kế trong bộ linh kiện được hỗ trợ, không phải mọi linh kiện bất kỳ ngoài thị trường.

**Cô hỏi: “Bộ linh kiện của tôi lấy từ đâu?”**

> Người dùng tự khai báo số lượng hoặc nhập từ các đơn mô phỏng đã xác nhận của chính mình.
> Khi nhập lại từ đơn, hệ thống dùng số lượng lớn hơn giữa dữ liệu đang có và số tổng từ đơn,
> để bấm lại không cộng trùng vô hạn.

**Code:** [MyKitServlet.java:58](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/MyKitServlet.java#L58),
nhận action; [UserKitDB.java:132](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/UserKitDB.java#L132), `GREATEST`.

### 6.2. Lắp và kiểm thiết kế

**Bấm:** Mở phòng Xưởng của thiết kế; chỉ điểm gắn của khung và vật thể con.
Nếu dùng phiên thử đã chuẩn bị, minh họa bánh vào mặt khung bị từ chối.
Bấm **Kiểm tra thiết kế**; chỉ cả thông báo PASS/WARN/FAIL thực tế, không đọc trước kết luận mong muốn.

> Luồng này vẫn kiểm từng vật thể, nhưng điểm gắn đến từ các linh kiện đã được gắn.
> Một bánh phải gắn vào trục phù hợp, không thể đặt lên mặt khung rồi coi là hợp lệ.
> Kiểm tra thiết kế có ba lớp: lắp ghép cơ khí, đủ vai trò theo mục tiêu và tương thích cấu hình cơ bản.
> Ví dụ có động cơ DC mà thiếu mạch điều khiển động cơ thì bị báo thiếu;
> số động cơ vượt số kênh điều khiển cũng không đạt.
> Linh kiện chưa có luật được ghi Chưa hỗ trợ kiểm chứng, không được tự coi là đúng.

**Cô hỏi: “Đạt mục tiêu nghĩa là chạy được robot thật chưa?”**

> Chưa. Đó là đạt bộ luật giản lược đang hỗ trợ. Hệ thống không mô phỏng dòng điện,
> điện áp tải, va chạm hoặc firmware. Thiết kế riêng cũng không được tính là hoàn thành một mẫu chính thức.

**Code:** [DesignLayout.java:91](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/DesignLayout.java#L91), `validateAttach`;
[DesignCheck.java:65](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/DesignCheck.java#L65), `evaluate`.

### 6.3. Hồ sơ để tham khảo lắp thật, nối sang vật tư

**Bấm:** **Hồ sơ thiết kế**, `/design-sheet?id=...`; chỉ lần lượt bản vẽ, BOM,
phần đang có/còn thiếu, trình tự đề xuất và kết quả kiểm. Chỉ nút **Thêm phần còn thiếu vào giỏ**;
không bắt buộc checkout live. Mở Của tôi → Bộ linh kiện hoặc Tài nguyên → Cửa hàng nếu cần đối chiếu.

> Khi đã có thiết kế, người học xuất hồ sơ để tham khảo: hình chiếu2D, danh sách vật tư,
> số đang có và còn thiếu, trình tự lắp theo cha trước con, kết quả kiểm và lưu ý an toàn.
> Phần vật tư thiếu có thể liên kết với cửa hàng mô phỏng, nên chức năng mua không đứng riêng khỏi việc thiết kế.
> Khi lắp ngoài đời cần ngắt nguồn trước khi đổi dây và kiểm lại theo thông số thực tế.

**Cô hỏi: “Giá và tổng tiền có lấy theo client không?”**

> Không. Checkout đọc giỏ và giá từ DB; server kiểm tồn kho và tính bằng BigDecimal.
> Đây là checkout mô phỏng, không phải thanh toán thật.

**Code:** [DesignSheetServlet.java:21](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/DesignSheetServlet.java#L21),
lọc owner và forward JSP;
[OrderServlet.java:55](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/OrderServlet.java#L55), không đọc giá/user từ body;
[ShopOrder.java:28](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/ShopOrder.java#L28), tổng tiền BigDecimal.

**Chuyển:** “Ngoài việc đặt linh kiện lên mô hình, người học còn cần hiểu cách nối và cách tìm lỗi.”

## 7. Chức năng phụ: học nối dây, tra cứu và luyện chẩn đoán — 12:00–14:00

### 7.1. Bài nối dây — khoảng50 giây

**Bấm:** Từ mẫu/kết quả chọn **Bài nối dây của mẫu này**, hoặc `/wiring` rồi chọn đúng mẫu.
Chỉ bài chính thức. Mở lượt luyện riêng: chọn hai chân để nối hoặc dùng form;
lưu nháp nếu đang demo thao tác, rồi mở kết quả thật đã chuẩn bị để giải thích chấm.
Không nói đã nộp live nếu chỉ mở kết quả cũ.

> Lắp hình khối chưa đủ để hiểu cấu hình, nên website có bài nối dây theo sơ đồ tham khảo.
> Người học thực hiện các cặp chân, có thể lưu nháp để làm tiếp và nộp để server chấm theo bài.
> Khi sai, kết quả giúp đối chiếu phần đúng, phần thiếu hoặc phần thừa.
> Phần này chấm sơ đồ/các cặp theo bài mẫu, không mô phỏng điện hoặc khẳng định robot thật an toàn.

**Cô hỏi: “Tắt JavaScript thì trang này còn hoạt động không?”**

> Trang có form gửi về Servlet; JavaScript bổ sung tương tác SVG cho thuận tiện.
> Server vẫn nhận dữ kiện và gọi luật chấm, không phụ thuộc client tự cho điểm.

**Code:** [WiringServlet.java:85](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/WiringServlet.java#L85), `doPost`;
[WiringExercise.java:314](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/WiringExercise.java#L314), `grade`.

### 7.2. Tra cứu rồi luyện chẩn đoán — khoảng60 giây

**Bấm:** `/pages/tra-cuu-loi.html`, lọc đúng robot bằng bộ lọc đang có → mở chi tiết triệu chứng.
Theo link chẩn đoán cùng mẫu nếu có, hoặc `/diagnosis` rồi chọn tình huống phù hợp.
Ở lượt luyện riêng, thực hiện một phép kiểm tra để thấy quan sát;
chỉ kết luận sau khi có căn cứ. Nếu thời gian ngắn, mở lịch sử kết quả đã làm thật.

> Khi gặp vấn đề, người học dùng tra cứu để đọc hướng dẫn theo triệu chứng và mẫu robot.
> Chẩn đoán khác ở chỗ người học phải chọn phép kiểm tra, đọc quan sát rồi chọn nguyên nhân và cách xử lý.
> Server ghi nhận việc đã kiểm gì và đánh giá kết luận theo luật của tình huống.
> Như vậy người học không chỉ bấm đoán một đáp án, mà có quá trình thu thập thông tin.

**Cô hỏi: “Sao đã cho đọc hướng dẫn mà còn chấm chẩn đoán?”**

> Đây là luyện tập có tài liệu, không phải thi kín. Tra cứu hỗ trợ học;
> chẩn đoán đánh giá cách thực hiện kiểm tra và kết luận trong tình huống cụ thể.
> Quan sát là dữ kiện mô phỏng của bài, không phải số đo từ một robot thật đang kết nối.

**Code:** [DiagnosisServlet.java:94](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/DiagnosisServlet.java#L94),
`doPost`; [DiagnosisAttempt.java:32](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/DiagnosisAttempt.java#L32), `conclude`.

**Nếu cô hỏi hỗ trợ người học:** Mở `/wiring-support`, nói người học gửi câu hỏi kèm thông tin lượt,
ADMIN trả lời, người gửi xem phản hồi. Không cần tạo yêu cầu mới chỉ để lấp đầy màn hình.

**Chuyển:** “Các hoạt động vừa rồi có thể luyện tự do. Khi giảng viên muốn giao bài và nhận minh chứng, nhóm dùng nhiệm vụ.”

## 8. Nhiệm vụ: giao → làm đúng vòng → nộp → nhận phản hồi — 14:00–16:00

### 8.1. ADMIN nêu yêu cầu, USER biết cần làm gì

**Bấm:** Cửa sổ ADMIN → `/admin-tasks` → mở nhiệm vụ đã chuẩn bị.
Chỉ mẫu, người được giao, hạn, tối đa lần nộp, ngưỡng đạt và rubric.
Chuyển cửa sổ USER → `/tasks` → chi tiết cùng nhiệm vụ.
Không công bố hoặc chấm bản ghi dành live chỉ để tập trình bày.

> Với nhiệm vụ, giảng viên giao một mẫu cụ thể cho từng người, có hạn và tiêu chí đánh giá.
> USER mở chi tiết để biết phải hoàn thành gì và trạng thái cá nhân hiện tại.
> “Đang mở” là trạng thái nhiệm vụ; “Chưa nộp”, “Chờ chấm”, “Cần bổ sung”, “Đạt” hoặc “Chưa đạt” là trạng thái của người học.
> Hai người cùng được giao một nhiệm vụ có thể có kết quả hoàn toàn khác nhau.

### 8.2. Chỉ bài nộp và phản hồi thật, không gom nhầm bằng chứng

**Bấm:** USER mở bài nộp/lịch sử đã chuẩn bị: chỉ phiên hoàn tất, quiz đúng vòng,
ba phần giải thích; ADMIN mở bài đó và phản hồi. Nếu muốn nộp/chấm trực tiếp,
chỉ dùng nhiệm vụ thử riêng đã chuẩn bị hợp lệ, không nhận là đang tạo lại toàn chuỗi trong2 phút.

> Người học không chỉ gửi một số điểm. Bài nộp gắn với bằng chứng lắp và quiz hợp lệ của đúng người,
> kèm giải thích vấn đề gặp phải, lý do xử lý và điều sẽ cải thiện.
> Với rubric A, lắp ráp40, quiz40 và giải thích20. Phần giải thích cần giảng viên đánh giá;
> có điểm tự động chưa có nghĩa là đã được kết luận Đạt.
> ADMIN có thể phản hồi và yêu cầu bổ sung theo giới hạn của nhiệm vụ, người học xem trạng thái và vòng tiếp theo.

**Cô hỏi: “Quiz vừa luyện ở trang hoàn thiện đem nộp luôn được à?”**

> Không mặc định như vậy. Quiz tính cho nhiệm vụ phải đi từ chi tiết nhiệm vụ và gắn đúng vòng.
> Phiên lắp cũng phải đáp ứng điều kiện bằng chứng. Nếu không cho dùng bằng chứng cũ,
> phải công bố nhiệm vụ trước rồi mới hoàn thành phiên dùng để nộp.

**Lưu ý điều phối:** Nếu muốn dùng phiên lắp live ở chặng4 làm bằng chứng,
chuẩn bị/công bố nhiệm vụ đúng luật trước khi hoàn tất phiên; quiz phải mở từ nhiệm vụ,
không lấy lượt PRACTICE ở chặng5 để giả là lượt TASK. Mặc định script đọc bài nộp đã có ở chặng8.

**Cô hỏi: “Rubric B khác gì? Có tự đổi thang điểm không?”**

> Rubric B có lắp ráp30, quiz25, chẩn đoán25 và giải thích20.
> Hai rubric dùng luật cố định đã có; nhóm không tự thêm thang điểm theo giao diện.

**Mở code:**

- [AdminTaskServlet.java:52](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AdminTaskServlet.java#L52):
  `getParameterValues` lấy nhiều người được giao; dòng166 là `doPost`, dòng174 kiểm form CSRF.
- [TaskServlet.java:139](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L139):
  nhận form; chỉ đọc minh họa, không sửa file WIP.
- [task-submit.jsp:61](../tomcat-app/src/main/webapp/WEB-INF/views/task-submit.jsp#L61):
  form POST; dòng74–78 là các textarea giải thích.
- [TaskRecipient.java:106](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/TaskRecipient.java#L106):
  `statusLabel` đọc bài nộp/lần chấm để trả trạng thái cá nhân.
- [TaskSubmissionDB.java:315](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/TaskSubmissionDB.java#L315):
  transaction nộp bài; [TaskRubric.java:29](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/TaskRubric.java#L29): tính điểm tự động.

**Chuyển:** “Những kết quả này quay lại tài khoản để người học biết đã làm gì và nên mở hoạt động nào tiếp.”

## 9. Tổng kết, tài nguyên và quản trị — 16:00–17:00

**Bấm:** USER `/learning-summary` → mở kết quả của một mẫu → quay lại → **Bản in/PDF** (`/learning-profile`).
Chỉ link ôn tập đúng mẫu. Nếu còn thời gian, mở Thư viện và dùng bộ lọc robot;
ADMIN chỉ danh sách quản trị nội dung/thống kê, không tạo/sửa dữ liệu công bố trong buổi demo.

> Bảng điều khiển giúp người học chọn việc làm tiếp; tổng kết cho biết đã có kết quả gì;
> hồ sơ học tập dùng để đọc và in. Ba trang có vai trò khác nhau, không phải ba cách tính điểm mới.
> Từ tổng kết người học quay lại kết quả hoặc ôn quiz đúng mẫu.
> Thư viện và danh mục linh kiện bổ sung tài liệu theo robot.
> Phía ADMIN quản lý nội dung và xem thống kê để duy trì các bài học, không tham gia bài luyện thay USER.

**Cô hỏi: “Ẩn menu quản trị thì đã đủ bảo mật chưa?”**

> Chưa. Menu chỉ giúp giao diện rõ ràng; server phải kiểm role cho từng route/thao tác.
> USER gõ URL quản trị vẫn bị chặn. Dữ liệu riêng còn phải kiểm owner, không chỉ kiểm đã đăng nhập.

**Code:** [SessionUtil.java:84](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java#L84), `requireAdmin`;
[LearningSummaryServlet.java:53](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/LearningSummaryServlet.java#L53),
đưa dữ kiện vào request rồi forward.

**Chuyển:** “Cuối cùng em chỉ một vòng request thật để làm rõ phần kiến thức Web nhóm đã áp dụng.”

## 10. Một vòng code có đầu vào và đầu ra — 17:00–18:00

**Mở hai file cạnh nhau:** `AssemblyReviewPageServlet.java` và `assembly-result-viewer.jspf`.
Nếu cô hỏi form/PRG, chuyển sang `MyKitServlet.java` và `my-kit.jsp`.
Không mở cả project rồi kể tên package mà không chỉ request cụ thể.

**Nói:**

> Ví dụ khi em mở trang kết quả, trình duyệt gửi GET kèm mã phiên.
> Tomcat chuyển request tới Servlet theo mapping. Servlet lấy user từ session,
> gọi XxxDB đọc dữ kiện đúng owner; JavaBean kiểm và tổng hợp dữ kiện.
> Servlet đặt dữ liệu vào request bằng setAttribute, rồi forward vào JSP trong WEB-INF.
> JSP dùng EL và JSTL để hiển thị. Phần mô hình3D dùng JavaScript dựng hình từ bố cục server đã cung cấp,
> không tự quyết định hoàn tất hoặc thay đổi DB khi chỉ xem.
> Với thao tác tương tác trong phòng lắp, Servlet trả JSON thay vì forward JSP;
> server vẫn giữ phần xác thực, kiểm luật và ghi dữ liệu.

**Kết lời:**

> Như vậy website đưa người học từ chọn mẫu, thực hành có phản hồi, xem lại sản phẩm và học tiếp.
> Xưởng mở rộng sang tự chọn cấu hình trong phạm vi được hỗ trợ; các phần nối dây, chẩn đoán,
> nhiệm vụ và vật tư phục vụ hành trình đó. Nhóm giữ rõ giới hạn mô hình học tập,
> không khẳng định đây là công cụ mô phỏng điện hay chứng nhận robot thật hoạt động.

## 11. Khi cô dừng ở code: trả lời ngay chỗ đang demo — dự phòng18:00–20:00

Không đọc mục này thành bài lý thuyết riêng. Chọn đúng câu cô hỏi; nói dữ liệu vào, xử lý, dữ liệu ra.

### “Servlet nhận GET/POST ở đâu? action trong form có phải doPost không?”

> HTTP method quyết định vào doGet/doPost. Thuộc tính action của form là URL nhận;
> input tên action là tham số để code chọn nhánh bên trong method.
> getParameter lấy một giá trị; getParameterValues lấy nhiều giá trị cùng tên, ví dụ danh sách người được giao.

**Mở:** [MyKitServlet.java:32](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/MyKitServlet.java#L32), `doPost`;
[my-kit.jsp:28](../tomcat-app/src/main/webapp/WEB-INF/views/my-kit.jsp#L28), hidden token/action;
[AdminTaskServlet.java:52](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AdminTaskServlet.java#L52), nhiều checkbox.

### “forward, redirect và PRG khác gì?”

> Forward chuyển xử lý ở server trong cùng request nên JSP đọc được request attribute.
> Redirect trả chỉ dẫn để trình duyệt gửi request mới. Sau POST lưu thành công,
> redirect về GET là PRG, giúp refresh không gửi lại chính POST đó.
> PRG không thay việc khóa/version hoặc kiểm request lặp ở nghiệp vụ.

**Mở:** [AssemblyReviewPageServlet.java:67](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblyReviewPageServlet.java#L67), forward;
[MyKitServlet.java:67](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/MyKitServlet.java#L67), redirect sau lưu.

### “JavaBean chỉ có getter/setter thôi hay làm gì nữa?”

> Bean có field private, constructor rỗng, getter/setter và Serializable theo quy ước dự án.
> Bean còn giữ luật nghiệp vụ bằng instance method, ví dụ validateAttach kiểm một ý định lắp.
> Bean không đọc HttpServletRequest hoặc tự gọi JDBC. Servlet điều phối, XxxDB đọc/ghi dữ kiện.

**Mở:** [AssemblyLayout.java:9](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyLayout.java#L9)
và [dòng233](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyLayout.java#L233).

### “ConnectionPool, PreparedStatement và DBUtil ở đâu?”

> Pool cấp lại kết nối JDBC đã được quản lý qua JNDI, khác với kết nối HTTP của browser.
> PreparedStatement bind giá trị tham số; ResultSet đọc kết quả rồi map sang bean.
> Trong finally, code đóng ResultSet/statement và trả connection về pool, kể cả có lỗi.

**Mở:** [ConnectionPool.java:28](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ConnectionPool.java#L28), lookup JNDI;
[dòng43](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ConnectionPool.java#L43), getConnection;
[UserDB.java:130](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/UserDB.java#L130), bind/execute/map/finally;
[DBUtil.java:20](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/DBUtil.java#L20), đóng statement.

### “Transaction và FOR UPDATE để làm gì?”

> Một thao tác gồm nhiều bước liên quan phải cùng thành công hoặc cùng rollback.
> Khóa phiên ngăn hai thao tác cùng ghi dựa trên một trạng thái cũ;
> layoutVersion còn giúp phát hiện request từ tab đã lỗi thời.

**Mở:** [AssemblyPlacementDB.java:169](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblyPlacementDB.java#L169),
`change`, và [dòng311](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblyPlacementDB.java#L311), owner/lock.

### “EL, JSTL, c:out và include dùng ở đâu?”

> EL đọc property qua getter. c:if/c:choose chọn cách trình bày, c:forEach lặp danh sách.
> c:out escape chuỗi khi xuất HTML; c:url/c:param tạo URL. Fragment jspf giữ phần giao diện dùng chung.
> JSP không đặt SQL hoặc tự quyết định điểm/hoàn tất.

**Mở:** [assembly-review.jsp:75](../tomcat-app/src/main/webapp/WEB-INF/views/assembly-review.jsp#L75),
include viewer ở dòng77;
[assembly-result-viewer.jspf:7](../tomcat-app/src/main/webapp/WEB-INF/views/assembly-result-viewer.jspf#L7), c:out payload.
Payload nằm trong div HTML thường; JS đọc textContent rồi JSON.parse, không chèn JSON thô vào mã script.

### “CSRF, session và quyền có phải cùng một việc không?”

> Session xác định người đăng nhập; kiểm role/owner xác định họ được làm gì với dữ liệu nào.
> CSRF kiểm token của request ghi. Form dùng hidden csrfToken, API dùng X-CSRF-Token.
> session_version giúp vô hiệu phiên cũ khi dữ kiện tài khoản liên quan thay đổi.
> no-store yêu cầu không cache trang/phản hồi dữ liệu cá nhân.

**Mở:** [SessionUtil.java:47](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java#L47), user;
[dòng108](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java#L108), form CSRF;
[dòng121](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java#L121), API CSRF.

### “Ảnh tham khảo, kết quả3D và replay có gì khác?”

> Ảnh tham khảo là ảnh catalog. Kết quả3D dựng từ final placements của một phiên cụ thể.
> Replay đọc các sự kiện để xem trình tự. Phiên GROUP cũ không có vị trí/hướng từng vật thể
> thì không dựng giả mô hình chính xác; vẫn giữ ảnh và thông tin lịch sử còn có.

**Mở:** [assembly-result-viewer.js:43](../assets/js/assembly-result-viewer.js#L43), tọa độ/góc đã lưu;
[AssemblyReviewPageServlet.java:90](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblyReviewPageServlet.java#L90), payload từ server.

## 12. Chia người và tập để trình bày liền mạch

Nếu chia theo ba mảng đã có trong nhóm, có thể dùng phương án sau; người cầm chuột đứng theo người đang nói.

| Mảng trình bày | Nội dung cần nắm | Câu chuyển giao |
|---|---|---|
| Nền tảng/phiên học tập | Chặng1,3; owner/session; nhiệm vụ; một vòng request | “Sau khi chọn và chuẩn bị, phần tiếp theo minh họa thao tác lắp thực tế.” |
| Tương tác/thực hành | Chặng4–5;3D, phản hồi server, kết quả/replay; nối dây/chẩn đoán | “Từ thao tác trên mẫu cố định, nhóm tiếp tục minh họa trường hợp tự chọn linh kiện.” |
| Dữ liệu/nội dung | Chặng2, quiz, Xưởng/luật/vật tư; thư viện/tổng kết | “Các kết quả học tập được nối lại trong tài khoản và có thể dùng theo nhiệm vụ của giảng viên.” |

Đây là gợi ý chia việc, không khẳng định mỗi tính năng chỉ do một người làm.
Nếu một người nói toàn bộ, giữ nguyên thứ tự chặng và các câu chuyển ở trên.

**Cách tập một lượt:** bật hẹn giờ18 phút; nói và bấm đúng các chặng1–10,
không đọc câu hỏi dự phòng. Người còn lại hỏi xen hai câu ở đúng chặng đang chạy.
Lượt hai đổi người thao tác và người trả lời; mỗi câu chỉ mở một method làm bằng chứng trước.

**Nếu bị hỏi mà chưa nhớ:** “Dạ, em xin chỉ luồng dữ liệu ở chức năng này” rồi đi theo
**người dùng bấm gì → request gửi gì → Servlet nhận đâu → bean/XxxDB kiểm và lưu gì → response trả gì**.
Không trả lời bằng cách đọc tên package hoặc đoán số chương/slide.

Tra cứu sâu thêm tại [ON_TAP_VAN_DAP_CUOI_KY.md](ON_TAP_VAN_DAP_CUOI_KY.md).
Script này là hướng dẫn trình bày; không phải tuyên bố mọi thao tác ghi đã được chạy lại trong buổi soạn tài liệu.
