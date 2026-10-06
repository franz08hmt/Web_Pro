# Ôn tập kiểm tra code Web Programming — 01/10/2026

Tài liệu này dùng để ôn phần đã thực hành trong thư mục `D:\Web-Pro` và Chapter 14. Đề kiểm tra chưa được công bố, nên thứ tự dưới đây là **ưu tiên ôn tập**, không phải dự đoán chắc chắn về đề.

Nguồn Chapter 14 hiện có là `D:\Web-Pro\Chapter 14 slides.pptx` (28 slide). Chưa tìm thấy `chap 14.pdf` trong thư mục này. Nếu bản PDF giảng viên phát có nội dung khác, cần so lại.

## 1. Học theo thứ tự này

| Ưu tiên | Kiến thức | Việc bạn phải tự code được |
| --- | --- | --- |
| 1 | MVC, Servlet, JSP, JavaBean (Ch. 2, 5, 6) | Nhận form bằng `doPost`, kiểm tra dữ liệu, tạo bean, `setAttribute`, `forward`, hiển thị bằng EL. |
| 2 | JavaMail (Ch. 14) | Tạo `Properties`, `Session`, `MimeMessage`, địa chỉ người nhận; dùng `Transport` và xử lý `MessagingException`. |
| 3 | EL, JSTL, session/cookie (Ch. 7–9) | Hiển thị `${user.email}`, thông báo bằng `<c:if>`, duy trì trạng thái bằng session hoặc cookie. |
| 4 | JDBC/JPA (Ch. 12–13) | Giải thích và code một thao tác thêm/đọc dữ liệu; phân biệt `PreparedStatement` với `EntityManager`. |
| 5 | HTTP, custom tag (Ch. 10, 18) | Giải thích status, header, redirect/forward; nhận diện TLD/tag handler nếu cô hỏi. |

Không cần học thuộc nguyên một project. Hãy tự viết lại luồng nhỏ từ form đến kết quả mà không mở code mẫu.

## 2. Luồng xương sống: form đến JSP

```text
index.jsp hoặc index.html
   POST /emailList
      EmailListServlet.doPost()
      ├─ request.getParameter("firstName")
      ├─ kiểm tra dữ liệu
      ├─ new User(firstName, lastName, email)
      ├─ UserDB.insert(user) hoặc tầng dữ liệu khác
      ├─ request.setAttribute("user", user)
      └─ forward("/thanks.jsp")
            ${user.firstName}, ${user.email}
```

Mẫu tối thiểu cần viết từ trí nhớ:

```java
@WebServlet("/emailList")
public class EmailListServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String firstName = request.getParameter("firstName");
        String email = request.getParameter("email");

        if (firstName == null || firstName.isBlank()
                || email == null || email.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập đủ thông tin");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            return;
        }

        User user = new User(firstName.trim(), email.trim());
        request.setAttribute("user", user);
        request.getRequestDispatcher("/thanks.jsp").forward(request, response);
    }
}
```

Ví dụ trên là **khung luyện tập**, không phải chữ ký constructor của mọi `User` trong các project. Khi làm bài, xem đề yêu cầu `User` có những field nào.

Nhớ phân biệt:

- `getParameter`: đọc chuỗi do trình duyệt gửi trong request.
- `setAttribute`: đặt object để servlet/JSP cùng dùng trong request.
- `forward`: xử lý tiếp trên server trong cùng request; URL trên trình duyệt giữ nguyên.
- `sendRedirect`: trình duyệt tạo request mới; request attribute cũ không đi theo.
- JavaBean: field riêng tư, constructor không tham số, getter/setter theo tên thuộc tính (và thường `Serializable` khi lưu trong session).
- Tomcat 9 dùng `javax.servlet.*`; đừng trộn với `jakarta.servlet.*` trong cùng project.

Mở lại code đã làm:

- `D:\Web-Pro\chapter-02-mvc-email-list\src\main\java\vn\edu\webpro\chapter02\controller\EmailListServlet.java`
- `D:\Web-Pro\chapter-05-develop-servlets\src\main\java\murach\email\EmailListServlet.java`
- `D:\Web-Pro\chapter-06-develop-JSP\src\main\java\murach\business\User.java`
- `D:\Web-Pro\chapter-08-09-10-jsp-lab\src\main\webapp\chapter08\el-demo.jsp`

## 3. Chapter 14: điều cần code được

### Đường đi của email

SMTP dùng để gửi thư; POP và IMAP dùng để nhận/đọc thư. MIME mô tả kiểu nội dung thư, ví dụ `text/plain` hoặc `text/html`. Đây là nội dung slide 3–6. Slide 4 diễn đạt IMAP qua ví dụ đọc trên trình duyệt; ý cần nhớ là client truy cập và đồng bộ thư còn lưu trên server.

### Bốn bước JavaMail trong slide 9, 21–22

```java
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class MailUtilLocal {
    public static void sendMail(String to, String from, String subject,
                                String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "25");
        Session session = Session.getInstance(props);       // 1. Phiên mail

        MimeMessage message = new MimeMessage(session);      // 2. Nội dung
        message.setSubject(subject, "UTF-8");
        message.setText(body, "UTF-8");
        message.setFrom(new InternetAddress(from));         // 3. Địa chỉ
        message.setRecipient(Message.RecipientType.TO,
                new InternetAddress(to));

        Transport.send(message);                            // 4. Gửi
    }
}
```

`localhost:25` trong ví dụ chỉ chạy nếu có SMTP server cục bộ. Compile thành công **không chứng minh** email đã gửi được. Không dùng tài khoản hay mật khẩu email thật trong bài luyện. `javax.mail` phải có JAR hoặc dependency tương thích; JDK 17 không tự cấp thư viện này. Slide 5–7 nói về `activation.jar` và cho rằng Java SE 6+ đã có sẵn. Điều này đúng với Java SE 6–10, nhưng module `java.activation` đã bị gỡ từ JDK 11. Trên Java 17, kiểm tra để có **cả Mail lẫn Activation** phù hợp, qua Maven hoặc JAR do cô cấp.

Nắm các biến thể:

- Slide 10–13: `mail.smtp.host`, `mail.smtp.port`, `mail.smtp.auth`; `Session` giữ cấu hình. Với `smtps`, tên thuộc tính là `mail.smtps.*` như slide 12.
- Slide 14–15: `setText` tạo thư văn bản; `setContent(html, "text/html")` tạo body HTML.
- Slide 16–18: `setFrom`; `setRecipient(TO/CC/BCC, ...)`; `setRecipients` dùng khi nhiều người nhận; `addRecipient` thêm vào danh sách sẵn có.
- Slide 19–20: `Transport.send(message)` là mẫu không tự gọi `connect`; với xác thực có thể lấy `Transport`, `connect`, `sendMessage`, rồi `close`.
- Slide 23–26: servlet đọc form, lưu `User`, gọi helper gửi email, bắt `MessagingException`, ghi log và đưa thông báo lỗi sang JSP. Nếu đã lưu user trước khi gửi mail mà mail lỗi, user **vẫn có thể đã được lưu**; cần nói rõ trạng thái đó.
- Slide 27–28: cấu hình Gmail là ví dụ lịch sử của slide 2014. Đừng chép nguyên tài khoản/mật khẩu mẫu hay dựa vào nó để gửi mail thật.

Khung gọi từ Servlet:

```java
try {
    MailUtilLocal.sendMail(email, "noreply@example.test",
            "Welcome", "Thank you for registering");
    request.setAttribute("message", "Đã gửi email xác nhận");
} catch (MessagingException ex) {
    log("Không gửi được email", ex);
    request.setAttribute("error", "Đăng ký đã xử lý, nhưng chưa gửi được email");
}
request.getRequestDispatcher("/thanks.jsp").forward(request, response);
```

Ghi nhớ đường đi để tránh nhầm: **Servlet điều phối**, **MailUtil đóng gói JavaMail**, **JSP hiển thị kết quả**.

## 4. Nhánh đề có database

Nếu cô cho JDBC (Ch. 12):

```java
String sql = "INSERT INTO users (full_name, email) VALUES (?, ?)";
try (Connection connection = dataSource.getConnection();
     PreparedStatement ps = connection.prepareStatement(sql)) {
    ps.setString(1, fullName);
    ps.setString(2, email);
    ps.executeUpdate();
}
```

`?` và `setString` tách dữ liệu khỏi lệnh SQL. `executeUpdate()` dùng cho `INSERT/UPDATE/DELETE`; `executeQuery()` cho `SELECT`. Sau `SELECT`, đọc từng dòng bằng `ResultSet.next()`.

Nếu cô cho JPA (Ch. 13):

```java
EntityManager em = emf.createEntityManager();
EntityTransaction tx = em.getTransaction();
try {
    tx.begin();
    em.persist(user);
    tx.commit();
} catch (RuntimeException ex) {
    if (tx.isActive()) tx.rollback();
    throw ex;
} finally {
    em.close();
}
```

`@Entity` ánh xạ class với bảng; `@Id` là khóa chính; `persistence.xml` khai báo persistence unit. `persist` để thêm, `find` tìm theo khóa chính, `merge` cập nhật, `remove` xóa. Bạn đã chứng minh đường đi giao diện → JPA → MySQL trong báo cáo Chapter 13; hãy giải thích được **vì sao phải `commit`**.

## 5. Các câu hỏi dễ bị hỏi thêm

1. Vì sao `request.getParameter("email")` trả `String` còn `${user.email}` đọc được thuộc tính? Vì Servlet nhận form dạng chuỗi; EL gọi getter của JavaBean trong scope.
2. Vì sao gọi `forward` sau khi đặt request attribute? Để JSP thấy object trong cùng request.
3. Sau `sendRedirect`, request attribute còn không? Không. Muốn mang trạng thái qua request mới, dùng session hoặc tham số URL tùy trường hợp.
4. Session và cookie khác gì? Dữ liệu session ở server; browser thường giữ session ID trong cookie. Cookie tùy chọn có thể lưu thông tin nhỏ phía client.
5. Khi `request.getCookies()` là `null`? Khi request không gửi cookie; kiểm tra trước khi lặp.
6. `<c:if test="${not empty error}">` dùng để làm gì? Chỉ hiển thị vùng báo lỗi khi có dữ liệu.
7. `setText` và `setContent(..., "text/html")` khác gì? Khác MIME type và cách client hiển thị body.
8. `Transport.send` và `Transport.sendMessage` khác gì trong slide? Mẫu đơn giản dùng `send`; mẫu có `connect` thủ công dùng `sendMessage` rồi `close`.
9. Vì sao không để mật khẩu SMTP trong code? Dễ lộ qua source, screenshot hoặc Git. Trong bài kiểm tra, dùng biến môi trường/cấu hình được cô cung cấp.
10. Servlet bị lỗi 404 thì kiểm gì đầu tiên? Context path của WAR, `@WebServlet` hoặc `web.xml`, URL form, artifact đã deploy.

## 6. Lịch ôn từ hôm nay đến buổi kiểm tra

| Thời điểm | Bài tập | Tự kiểm tra |
| --- | --- | --- |
| Thứ Ba 29/9, 75–90 phút | Viết lại `User`, form, `EmailListServlet`, `thanks.jsp` không nhìn mẫu. | Form trống báo lỗi; form hợp lệ hiển thị đúng dữ liệu; giải thích `forward`. |
| Thứ Tư 30/9, 75–90 phút | Học slide 9, 14–20, 21–28; tự viết `MailUtilLocal` và nhánh `try/catch` trong servlet. | Chỉ ra 4 bước JavaMail; phân biệt plain/HTML và `TO/CC/BCC`; không cần gửi mail thật. |
| Thứ Tư 30/9, thêm 45 phút | Làm một đề trong `DE_LUYEN_GIUA_KY_WEB_2026.md` có đồng hồ. | So với checklist sau khi nộp bài của chính mình. |
| Thứ Năm 01/10, 20–30 phút | Xem lại lỗi sai, URL Tomcat, import `javax.*`, file mapping. | Có thể phác lại luồng trên giấy trong 2 phút. |

## 7. Checklist trước khi rời máy

- Đã mở đúng project và thấy `pom.xml`/module được IntelliJ nhận.
- JRE Java 17, Tomcat 9, artifact `war exploded`, context path đúng.
- Form `action` khớp servlet mapping; dùng `method="post"` nếu xử lý bằng `doPost`.
- Với JSP/EL/JSTL, thư viện trong Maven hoặc `WEB-INF/lib` đã có.
- Đọc **dòng lỗi đầu tiên có ý nghĩa** trong Tomcat log: 404 thường là URL/deploy; 500 thường là lỗi code/runtime; mail connection refused là SMTP chưa chạy.
- Không mất thời gian sửa giao diện CSS nếu luồng cơ bản chưa chạy.

Nguồn đối chiếu: `Chapter 14 slides.pptx`, slide 1–28 trong thư mục này; các project thực hành Chapter 2, 5–10, 12–13, 18 đã học. Tham khảo API chính thức: [JavaMail `javax.mail` package](https://javaee.github.io/javamail/docs/api/javax/mail/package-summary.html), [Jakarta Mail lịch sử phiên bản](https://jakartaee.github.io/mail-api/), [SMTP RFC 5321](https://www.rfc-editor.org/info/rfc5321/), [Oracle JDK 11 migration guide về `java.activation`](https://docs.oracle.com/en/java/javase/11/migrate/).
