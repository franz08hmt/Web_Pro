# Kịch bản trình bày Robot Assembly Lab — bản dùng để tập nói

Mở website bằng cấu hình duy nhất **Tomcat 9.0.120**, tại `http://localhost:8080/`.
Không mở helper Start cùng IntelliJ. Dùng USER để thực hành; ADMIN để giao/chấm nhiệm vụ.
Các bước bấm dưới đây dành cho người trình bày, không phải log đã chạy test ghi dữ liệu.

## Chuẩn bị trước buổi trình bày

- Đăng nhập đúng USER trên8080, mở sẵn danh mục, một phiên thực hành của mình và một kết quả đã hoàn tất của mình.
- Muốn demo hoàn tất nhanh: tự chuẩn bị một phiên còn thiếu chi tiết cuối. Không giả trạng thái/điểm trong DB.
- Nếu chưa có phiên gần hoàn tất, demo gắn sai/đúng trên phiên thực hành rồi mở kết quả có sẵn;
  nói rõ đó là kết quả đã lưu, không nhận vừa lắp xong toàn bộ.
- Không dùng ID của tài khoản khác. Lấy link kết quả từ Bảng điều khiển/Kết quả học tập của chính tài khoản.
- Chỉ chọn bài nối dây/tình huống chính thức. DB làm việc còn bài QA công bố và nhiệm vụ thử;
  đổi tài khoản không loại được bài QA dùng chung. Không tự xóa hoặc giấu dữ liệu.
- Code: mở tên file dưới mỗi đoạn, `Ctrl+G` đến dòng ghi kèm. Số dòng đối chiếu ngày18/10/2026;
  nếu source thay đổi, tìm đúng tên method. Không học thuộc số dòng hoặc số slide.

**Cách nói:** chỉ đọc phần “Nói”; phần “Cô hỏi” là câu trả lời khi được hỏi, không đọc hết.
Phần chính khoảng5–6 phút nếu có phiên chuẩn bị sẵn. Nếu cô chỉ cho3 phút, ưu tiên mục1–4;
mục5–7 mở theo câu hỏi, không chạy qua từng menu để kể danh sách tính năng.

## 1. Website phục vụ ai, làm việc gì? — 20 giây

**Màn hình:** Trang chủ → Học → Mẫu robot (`/pages/mau-robot.html`). Chỉ đọc.

**Nói:** “Thưa cô, website của nhóm em giúp người mới học lắp robot từ các linh kiện.
Luồng chính là chọn mẫu, tự lắp từng vật thể, nhận giải thích khi lắp sai và xem lại sản phẩm đã lưu.
Sau đó người học xem quy trình, phiếu kết quả và làm bài kiểm tra của mẫu vừa học.
Khi muốn tự chọn cấu hình, người học chuyển sang Xưởng thiết kế để kiểm tra tương thích và xuất hồ sơ lắp.”

**Cô hỏi: Có phải mô phỏng robot thật không?**
“Dạ, đây là mô hình học tập với luật lắp ghép và cấu hình cơ bản. Website không mô phỏng điện,
vật lý hay firmware, nên kết quả không thay thế kiểm tra thực tế khi lắp robot thật.”

**Cô hỏi: Chức năng chính là bán linh kiện hay làm quiz?**
“Dạ, chính là học lắp ráp có kiểm chứng và xem lại kết quả của mình. Quiz kiểm tra kiến thức sau khi lắp;
cửa hàng hỗ trợ chuẩn bị vật tư, không phải mục đích chính của website.”

## 2. Chọn mẫu → chuẩn bị → tự lắp, sai được giải thích — 90 giây

**Bấm:** USER chọn **Tránh vật cản → Lắp mẫu này**, chuẩn bị linh kiện, vào phòng3D.
Mở phiên thực hành của mình. Việc bắt đầu/chuẩn bị/gắn có ghi DB.

**Nói:** “Em chọn một mẫu cụ thể. Phần chuẩn bị xác nhận bộ linh kiện;
đánh dấu đủ bộ không có nghĩa là đã lắp xong. Trong phòng3D, mỗi động cơ, bánh xe hoặc servo là một vật thể riêng.
Em phải kéo từng vật thể tới điểm gắn và chọn đúng hướng.”

**Thao tác minh họa:** Gắn khung trước. Thử động cơ trái hướng0°, xem thông báo từ chối;
xoay90° bằng nút hoặc Q/E rồi kéo-thả vào điểm gắn. Chỉ đọc là thành công khi server đã nhận.
Nếu khung đã gắn sẵn, không lắp lại khung để minh họa.

**Nói:** “Hướng này bị từ chối. Khi em sửa đúng hướng, server mới lưu.
Server kiểm loại linh kiện, điểm gắn, hướng và bộ phận cha; giao diện không tự chấm đúng.”

**Cô hỏi: Kiểm ở JavaScript có đủ không?**
“Dạ không. JavaScript nhận thao tác và gửi yêu cầu. Servlet kiểm đăng nhập, token và tham số;
JavaBean kiểm luật từ bố cục/spec đã lưu. XxxDB chỉ ghi kết quả hợp lệ trong transaction.”

**Mở code khi hỏi:**

- [AssemblySessionServlet.java:434](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblySessionServlet.java#L434): CSRF;
  [dòng466](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblySessionServlet.java#L466): nhận ý định gắn.
- [AssemblyLayout.java:232](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyLayout.java#L232), `validateAttach`:
  kiểm phiên/version, loại linh kiện, hướng và cha. `validateDetach` ở dòng268 chặn tháo cha còn con.
- [AssemblyPlacementDB.java:169](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblyPlacementDB.java#L169), `change`:
  một connection/transaction, đọc có khóa, gọi bean rồi lưu placement và event.

**Cô hỏi: Năm mẫu có đều kéo-thả không?**
“Dạ, cả năm mẫu có phiên lắp chi tiết. Khay lấy số vật thể từ dữ liệu từng mẫu;
ví dụ cánh tay mini có bốn servo riêng. Phiên nhóm cũ vẫn giữ lịch sử, không bị đổi thành phiên chi tiết.”

**Cô hỏi: Góc90°/270° có chứng minh trục động cơ quay đúng cơ khí không?**
“Dạ, đó là quy ước lắp trái/phải của mô hình3D giản lược. Hình khối động cơ không mô phỏng chính xác chiều trục.”

## 3. Sửa thao tác → hoàn tất do server xác nhận — 30 giây

**Bấm:** Gắn một chi tiết lá → **Hoàn tác** → gắn lại. Nếu phiên đã chuẩn bị gần xong, lắp chi tiết cuối và **Hoàn tất**.
Đây là thao tác ghi DB; không làm trên phiên dự phòng đã hoàn tất.

**Nói:** “Em có thể hoàn tác thao tác vừa làm, nhưng server vẫn kiểm lại quan hệ cha–con.
Chỉ khi đủ các điểm bắt buộc và bố cục hợp lệ thì server mới xác nhận hoàn tất.
Tải lại trang vẫn đọc được bố cục đã lưu.”

**Cô hỏi: Hai tab cùng sửa có ghi đè nhau không?**
“Client gửi expectedLayoutVersion. Server khóa và so với phiên bản hiện tại;
nếu tab đã cũ thì trả xung đột để tải lại, không ghi đè âm thầm.”

**Chỉ code:** [AssemblyLayout.java:245](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyLayout.java#L245)
và [AssemblyPlacementDB.java:181](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblyPlacementDB.java#L181).
Khóa và lọc chủ ở [AssemblyPlacementDB.java:308](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblyPlacementDB.java#L308).
Kiểm toàn bộ điểm bắt buộc ở [AssemblyLayout.java:312](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyLayout.java#L312), `check`.

## 4. Hoàn tất rồi người học nhận được gì? — 60 giây

**Bấm:** **Xem kết quả** → `/assembly-review?session=...`. Nếu mở kết quả dự phòng, nói rõ từ đầu.
Xoay/zoom mô hình, chỉ bốn phần, mở link quiz đúng mẫu. Xem kết quả/đề bài là đọc;
chỉ khi bấm **Nộp bài** quiz mới ghi điểm.

**Nói:** “Sau khi hoàn tất, em xem ngay mô hình của chính phiên vừa lưu, không phải mô hình mặc định theo tên robot.
Ảnh bên dưới chỉ là ảnh tham khảo. Trang có bốn phần: mẫu hoàn chỉnh, quy trình lắp,
phiếu kết quả và kiểm tra kiến thức. Quy trình gồm bước chuẩn và thứ tự em thực sự đã làm;
em còn có thể mở phát lại để xem quá trình. Quiz dẫn đúng mẫu này để học tiếp.”

**Cô hỏi: Làm sao biết đó là kết quả của em?**
“Servlet lấy user từ session, đọc phiên theo cả id và user_id. Layout lấy spec_id gắn với phiên,
các instance và placements cuối đã lưu; không lấy spec mới nhất rồi tự dựng lại.”

**Mở code:** [AssemblyReviewPageServlet.java:40](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblyReviewPageServlet.java#L40),
đọc chủ ở dòng46; đọc layout ở dòng76; payload qua request attribute ở dòng87 và forward ở dòng66.
[AssemblyReview.java:36](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyReview.java#L36)
kiểm dữ kiện đủ để hiển thị.

**Cô hỏi: JSP làm gì? JavaScript có sửa kết quả không?**
“JSP trình bày dữ kiện bằng EL/JSTL. JSON đi qua c:out trong phần tử HTML ẩn;
JavaScript đọc textContent, dựng hình theo từng placement và chỉ điều khiển camera.
GET xem lại không gắn/tháo hoặc chấm lại.”

**Mở code:** [assembly-result-viewer.jspf:7](../tomcat-app/src/main/webapp/WEB-INF/views/assembly-result-viewer.jspf#L7),
[assembly-result-viewer.js:43](../assets/js/assembly-result-viewer.js#L43): tọa độ/góc đã lưu;
[assembly-review.jsp:75](../tomcat-app/src/main/webapp/WEB-INF/views/assembly-review.jsp#L75): include phần hiển thị.

**Cô hỏi: Quiz ai chấm?**
“Server đọc câu hỏi/lựa chọn hợp lệ rồi chấm. Client không gửi điểm để được tin là đúng.”
[QuizServlet.java:62](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/QuizServlet.java#L62)
nhận POST, gọi `QuizAttemptDB.submitAttempt` ở dòng87;
[QuizQuestion.java:95](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/QuizQuestion.java#L95)
kiểm đáp án. Mở đề không phải nộp bài.

## 5. Từ lắp theo mẫu sang tự thiết kế — 45 giây

**Màn hình:** Học → **Xưởng thiết kế** (`/designs`), chọn linh kiện/mục tiêu,
mở phòng lắp; **Kiểm tra thiết kế** → **Hồ sơ thiết kế**. Tạo/gắn/kiểm có ghi DB;
chỉ mở một hồ sơ có sẵn là đọc. Không cần tạo thiết kế mới nếu thời gian ngắn.

**Nói:** “Nếu người học có bộ linh kiện khác, Xưởng cho tự thiết kế trong bộ linh kiện được hỗ trợ kiểm chứng.
Server kiểm điểm gắn và ba lớp: lắp ghép cơ khí, đủ vai trò theo mục tiêu, tương thích cấu hình cơ bản.
Ví dụ động cơ DC cần mạch điều khiển, số động cơ không được vượt số kênh điều khiển.
Linh kiện chưa có luật được báo chưa hỗ trợ, không mặc định đúng.
Hồ sơ có bản vẽ2D, danh sách vật tư và phần còn thiếu, trình tự lắp, kết quả kiểm và lưu ý an toàn.”

**Cô hỏi: Tự thiết kế đạt thì có tính hoàn thành một mẫu chính thức không?**
“Dạ không. Đây là thiết kế riêng theo mục tiêu/luật tương thích giản lược,
không thay bằng chứng hoàn thành mẫu chính thức và không chứng nhận điện hay cơ khí ngoài đời.”

**Mở code:** [DesignLayout.java:91](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/DesignLayout.java#L91),
`validateAttach`; [DesignCheck.java:65](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/DesignCheck.java#L65),
`evaluate`; [DesignSheetServlet.java:21](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/DesignSheetServlet.java#L21)
đọc đúng chủ và chuyển dữ kiện sang JSP. Luật có phiên bản, thiết kế cũ giữ phiên bản đã dùng.

## 6. Chức năng phụ phục vụ luồng chính thế nào? — 30 giây, chọn một ví dụ

**Nói:** “Các chức năng phụ được nối với việc học lắp ráp: xem thông tin linh kiện khi chưa hiểu,
tra cứu khi gặp lỗi, luyện nối dây/chẩn đoán để hiểu thêm về cấu hình, rồi xem kết quả học tập.
Giảng viên giao nhiệm vụ, người học nộp bằng chứng và nhận phản hồi.
Bộ linh kiện và cửa hàng giúp chuẩn bị vật tư cho thiết kế; cửa hàng hiện là mô phỏng.”

**Bấm minh họa một đường:** từ thông báo sai → **Xem thông tin linh kiện** hoặc từ mẫu → **Bài nối dây**.
Nếu chọn tra cứu: lọc đúng robot → mở chi tiết → luyện chẩn đoán khi có bài tương ứng.
Không bấm mọi module để thay cho việc trình bày một hành trình.

**Cô hỏi: Tra cứu khác luyện chẩn đoán/nối dây thế nào?**
“Tra cứu cho đọc hướng dẫn. Bài luyện có lượt làm, dữ liệu thao tác và server phản hồi/chấm theo luật bài.”
[WiringExercise.java:314](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/WiringExercise.java#L314), `grade`.

**Cô hỏi: Nhiệm vụ đang mở có nghĩa em đã đạt không?**
“Dạ không. Đang mở là trạng thái nhiệm vụ; Chưa nộp/Chờ chấm/Cần bổ sung/Đạt/Chưa đạt là trạng thái cá nhân.
Kết luận lấy từ bài nộp và lần chấm, không suy từ một điểm trên trình duyệt.”
[TaskRecipient.java:106](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/TaskRecipient.java#L106), `statusLabel`.
Luyện tự do không tự thành điểm nhiệm vụ; làm quiz nhiệm vụ từ đúng vòng trong chi tiết nhiệm vụ.

## 7. Nếu cô yêu cầu chỉ kiến thức môn học — 60 giây

**Nói:** “Nhóm dùng Java17, Servlet/JSP trên Tomcat9 và JDBC/MySQL.
Với trang kết quả, request vào Servlet; Servlet kiểm session, gọi XxxDB lấy dữ kiện,
JavaBean kiểm/tổng hợp, setAttribute rồi forward tới JSP. Với phòng3D, Servlet trả JSON,
JavaScript dựng giao diện tương tác; việc kiểm đúng/sai vẫn ở server.”

| Cô hỏi | Trả lời ngắn và chỗ mở code |
|---|---|
| Servlet nhận form ở đâu? | [MyKitServlet.java:32](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/MyKitServlet.java#L32) là `doPost`; đọc `getParameter` dòng55. `getParameterValues` cho nhiều checkbox: [AdminTaskServlet.java:52](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AdminTaskServlet.java#L52). |
| forward khác redirect? | Forward chuyển tới JSP trong cùng request, giữ attribute: [AssemblyReviewPageServlet.java:66](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblyReviewPageServlet.java#L66). Lưu form thành công rồi redirect GET là PRG: [MyKitServlet.java:63](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/MyKitServlet.java#L63); refresh không gửi lại POST đó. |
| JavaBean là gì? | [AssemblyLayout.java:9](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyLayout.java#L9): Serializable, field private, constructor rỗng/getter/setter; `validateAttach` chứa luật. Bean không đọc request hoặc tự gọi JDBC. |
| User lấy từ URL à? | Không. [SessionUtil.java:47](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java#L47) đọc HttpSession và kiểm session_version; [dòng82](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java#L82) kiểm ADMIN ở server. Ẩn menu không thay kiểm quyền. |
| JDBC/pool nằm đâu? | [ConnectionPool.java:28](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ConnectionPool.java#L28) lookup JNDI; `getConnection` dòng42. [UserDB.java:129](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/UserDB.java#L129) PreparedStatement/bind/ResultSet; `finally` đóng và trả connection. [DBUtil.java:20](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/DBUtil.java#L20) đóng statement. |
| CSRF khác đăng nhập? | Session xác định ai; quyền xác định được làm gì; CSRF kiểm request ghi mang token đúng. Form dùng hidden token ở [my-kit.jsp](../tomcat-app/src/main/webapp/WEB-INF/views/my-kit.jsp); API dùng X-CSRF-Token. |
| Vì sao JSP không tự tính đúng/sai? | JSP là view, dùng getter/EL/JSTL để trình bày. Luật ở JavaBean và server; trình duyệt có thể bị sửa nên không tin điểm/mode/role client gửi. |

Nếu cô hỏi sâu hơn: mở [file vấn đáp đầy đủ](ON_TAP_VAN_DAP_CUOI_KY.md).
Không khẳng định mọi cú pháp cũ trong repo đều là nội dung slide; không bịa số chương/slide.

## Tập ngay trong 10 phút

1. Hai phút: nói mục1 và nêu đúng một đường đi chính, không đọc tên toàn bộ menu.
2. Ba phút: tự bấm thao tác sai → giải thích → sửa đúng; nói ai quyết định và ai lưu.
3. Hai phút: mở kết quả, chỉ mô hình đúng phiên và đủ bốn phần; phân biệt ảnh tham khảo/replay.
4. Ba phút: tự trả lời “user lấy đâu?”, “bean kiểm gì?”, “JDBC chỗ nào?”, “forward hay redirect?”.
   Mỗi câu mở đúng một file và method làm bằng chứng.

Nếu chưa nhớ, trả lời theo dữ liệu: **người dùng bấm gì → request gửi gì → Servlet nào nhận →
bean/XxxDB xử lý gì → response trả gì → người dùng thấy gì**. Không đọc thuộc tên package thay cho giải thích chức năng.
