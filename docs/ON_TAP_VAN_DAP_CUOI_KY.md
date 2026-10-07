# Ôn tập vấn đáp cuối kỳ — đọc kiến thức ngay trong code

Đối chiếu source ngày 07/10/2026. Đây là tài liệu để **đọc, chỉ code và giải thích chức năng**,
ưu tiên phần của Huỳnh Minh Tài: request/response, đăng nhập–session–quyền,
phiên lắp ráp–phiếu kết quả, nhiệm vụ A/B, hồ sơ học tập và cửa hàng/giỏ hàng/đơn mô phỏng.

Không học theo câu hỏi “nội dung ở trang tài liệu nào”. Mỗi lần trả lời dùng thứ tự:
**thao tác ở client → request gửi gì → Servlet đọc đâu → DB/bean xử lý gì → response trả gì → client thấy gì**.

## Cách dùng trên laptop

- Mở Markdown preview rồi bấm liên kết tên file để mở code trong repo.
- Mỗi liên kết kèm `file:dòng`, anchor `#L...` và tên method để định vị.
  Nếu IDE chỉ mở file, dùng `Ctrl+G` nhập số dòng; tìm thêm method khi source đổi.
- Không phải mọi IDE đều hỗ trợ nhảy dòng bằng anchor GitHub. Không cần copy đường dẫn thủ công khi preview mở file được.
- Các khối code là trích đoạn để đọc, không phải chương trình độc lập để copy chạy.

## Bản đồ mở file theo câu hỏi kiến thức

- **Client gửi form POST:** [task-submit.jsp:60](../tomcat-app/src/main/webapp/WEB-INF/views/task-submit.jsp#L60) — method/action/name quyết định request gửi đi.
- **GET: nhận request mở trang:** [LearningProfileServlet.java:22](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/LearningProfileServlet.java#L22) — request là đầu vào; response là đầu ra.
- **POST: nhận form bài nộp:** [TaskServlet.java:137](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L137) — kiểm session/token trước khi xử lý action.
- **Đọc parameter:** [TaskFormUtil.java:19](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/TaskFormUtil.java#L19) — getParameter trả String, thiếu thì null.
- **Đọc nhiều checkbox:** [AdminTaskServlet.java:51](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AdminTaskServlet.java#L51) — getParameterValues trả String[].
- **Đưa object vào request:** [LearningProfileServlet.java:52](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/LearningProfileServlet.java#L52) — attribute dùng giữa Servlet và JSP trên server.
- **Forward sang JSP:** [TaskServlet.java:35](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L35) — RequestDispatcher dùng cùng request/response.
- **Redirect sau khi lưu:** [TaskServlet.java:197](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L197) — browser gửi GET mới; attribute cũ không đi theo.
- **EL đọc dữ liệu request:** [learning-profile.jsp:31](../tomcat-app/src/main/webapp/WEB-INF/views/learning-profile.jsp#L31) — fullName ánh xạ sang getter getFullName().
- **Getter thật mà EL gọi:** [LearningProfile.java:60](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/LearningProfile.java#L60) — trả tên từ bean do Servlet đã dựng.
- **JSTL lặp và escape:** [task-submit.jsp:67](../tomcat-app/src/main/webapp/WEB-INF/views/task-submit.jsp#L67) — render danh sách bằng chứng và c:out.
- **Session sau đăng nhập:** [SessionUtil.java:23](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java#L23) — user/csrfToken nằm ở server, cookie giữ mã phiên.
- **Danh tính của request:** [SessionUtil.java:47](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java#L47) — getSession(false), getAttribute, đọc lại UserDB.
- **Pool cấp kết nối DB:** [ConnectionPool.java:42](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ConnectionPool.java#L42) — JDBC Connection khác kết nối HTTP.
- **SELECT bằng JDBC:** [UserDB.java:121](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/UserDB.java#L121) — prepareStatement/setString/executeQuery/rs.next.
- **Ghi DB bằng JDBC:** [UserDB.java:26](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/UserDB.java#L26) — executeUpdate trả số dòng tác động, không trả ResultSet.
- **Transaction nộp bài:** [TaskSubmissionDB.java:315](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/TaskSubmissionDB.java#L315) — lock/bean/INSERT/commit hoặc rollback.
- **Response lỗi có HTML:** [TaskServlet.java:46](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L46) — status 422 và forward tới task-error vẫn có body HTML.
- **Client gọi API bằng fetch:** [api.js:15](../assets/js/api.js#L15) — method/header/body → fetch → status/json.
- **Servlet nhận POST JSON:** [AuthServlet.java:65](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AuthServlet.java#L65) — doPost vẫn thật; phần body đọc bằng JsonUtil.
- **Server ghi response JSON:** [ResponseUtil.java:12](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/ResponseUtil.java#L12) — status/content type/header/getWriter().write.
- **Giới hạn sửa phiên hoàn tất:** [assembly-3d.js:140](../assets/js/assembly-3d.js#L140) — khóa nút/checkbox; server vẫn giữ quyền quyết định.

## Thứ tự ôn thực dụng

1. Mục 2: chỉ hai đầu request/response; đọc form POST và dữ liệu gửi về JSP.
2. Mục 3–4: GET hồ sơ, API JSON đăng nhập, JDBC và bean.
3. Mục 7: đi sâu các chức năng mình phụ trách, không cần kể lại cấu trúc package đã chốt.
4. Mục 8: tự trả lời, sau mỗi câu mở source để chứng minh.

Nếu chỉ có 30 phút: form `task-submit.jsp` → `TaskServlet.doPost` → `TaskFormUtil`
→ preview/redirect; sau đó `LearningProfileServlet` → `learning-profile.jsp` và `UserDB.selectUser`.

## 1. Giới thiệu dự án trong 60 giây

> Robot Assembly Lab là website học lắp ráp robot. Người học chọn mẫu robot, chuẩn bị
> linh kiện, lắp ráp trong phòng 3D, làm quiz, luyện chẩn đoán lỗi, nối dây, và nhận
> nhiệm vụ do Admin giao. Điểm, trạng thái và quyền đều do **server** quyết định.
> Server là Tomcat 9 chạy Servlet/JSP, dữ liệu ở MySQL qua JDBC. Code chia bốn tầng
> theo luồng Servlet–JavaBean–JSP: `business` (JavaBean), `controller` (Servlet), `data` (XxxDB),
> JSP là view.

Không ưu tiên học thuộc số lượng file/bảng. Khi được hỏi, mở đúng Servlet, method, bean và câu SQL của chức năng.

Công nghệ: Java 17, Tomcat 9 (`javax.servlet`), JSP/JSTL, MySQL 8, JDBC. Không có
Spring, ORM, JPA hay thư viện JSON.

## 2. Request và response: đọc được một vòng client–server

### 2.1. Khi cô hỏi: “Client và server nối với nhau ở đâu?”

Không bắt đầu bằng kể tên kiến trúc. Chỉ vào nơi browser gửi request và nơi Servlet nhận request.

```text
Browser (client)
  → HTTP request: method + URL + header/cookie + dữ liệu form hoặc JSON
Tomcat nhận HTTP, tìm Servlet theo URL, gọi service/doGet/doPost tương ứng
  → Servlet: đọc request, kiểm danh tính, gọi bean/XxxDB
  → JSP dựng HTML hoặc Servlet ghi JSON vào response
HTTP response: status + header + body
  → Browser hiển thị HTML hoặc JavaScript đọc JSON
```

Đây là sơ đồ giải thích, không phải code chạy. Browser không kết nối thẳng MySQL.
Kết nối browser–Tomcat là HTTP; kết nối Java–MySQL là JDBC. `ConnectionPool` phụ trách đường thứ hai.

**Câu nói mẫu:** “Em chỉ form ở client, method và URL; tiếp theo chỉ `doPost` bên server.
Request mang dữ liệu vào. Response mang status, header và HTML/JSON trả về.”

### 2.2. Client gửi form: phải chỉ đúng method, action, name và value

Mở [task-submit.jsp:60](../tomcat-app/src/main/webapp/WEB-INF/views/task-submit.jsp#L60): form nộp bài thực hành.

**Source:** [task-submit.jsp:60](../tomcat-app/src/main/webapp/WEB-INF/views/task-submit.jsp#L60), dòng 60–64.

```jsp
<form method="post" action="${pageContext.request.contextPath}/tasks">
    <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
    <input type="hidden" name="id" value="<c:out value="${task.id}"/>">
    <input type="hidden" name="roundId" value="<c:out value="${recipient.currentRound.id}"/>">
    <input type="hidden" name="action" value="preview">
```

- `method="post"`: browser gửi HTTP POST khi bấm nút trong form.
- Thuộc tính HTML `action=".../tasks"`: URL nhận request, khớp mapping `/tasks` của Servlet.
- Hidden **`name="action" value="preview"`**: dữ liệu của form để Servlet chọn nhánh xem trước.
  Nó khác thuộc tính HTML `action` và không tự đổi HTTP method.
- `name="id"`: server đọc tham số tên `id`; id không phải bằng chứng về chủ sở hữu.
- `name="roundId"`: vòng nộp đang chọn; server vẫn kiểm vòng đó thuộc User.
- `csrfToken`: server kiểm token trước khi xử lý; không phải điểm hay quyền.
- `${pageContext.request.contextPath}`: prefix context của ứng dụng, không phải địa chỉ DB.

Ở cuối cùng form, các `textarea name="problem|reasoning|improvement"` là dữ liệu người dùng nhập.
Browser gửi **giá trị control theo name**, không gửi cả DOM hoặc JavaBean.
`required/minlength/maxlength` hướng dẫn ở client; server vẫn kiểm lại.

**Cô hỏi: “Nhập giải thích thì Java nhận chỗ nào?”**

Mở [TaskFormUtil.java:86](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/TaskFormUtil.java#L86).
`setProblem(text(request, "problem"))` lấy parameter `problem` rồi gán vào bean input.

### 2.3. Tomcat đưa request vào doGet/doPost như thế nào?

Mở [TaskServlet.java:16](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L16): mapping `/tasks`.
Mở [TaskServlet.java:85](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L85): đọc danh sách/form.
Mở [TaskServlet.java:137](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L137): xử lý form.

**Source:** [TaskServlet.java:16](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L16), dòng 16–18.

```java
@WebServlet("/tasks")
/** Điều phối trang nhiệm vụ của USER theo danh tính trong session. */
public class TaskServlet extends HttpServlet {
```

`HttpServlet.service()` phân phối theo HTTP method. Không phải hidden `action` chọn `doGet` hay `doPost`.
Servlet này phân nhánh **bên trong** method dựa trên parameter `action`.
Nhập URL trên thanh địa chỉ hoặc bấm liên kết thường tạo GET; bấm form ở mục 2.2 tạo POST.

GET đọc trang hồ sơ: chú ý chữ ký `doGet` và đối tượng request/response.

**Source:** [LearningProfileServlet.java:21](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/LearningProfileServlet.java#L21), dòng 21–31.

```java
@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    response.setHeader("Cache-Control", "no-store");

    User user;
    try {
        user = SessionUtil.getCurrentUser(request);
    } catch (SQLException e) {
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        return;
```

**Source:** [TaskServlet.java:137](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L137), dòng 137–147.

```java
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    try {
        User user = user(request, response);
        if (user == null) {
            return;
        }
        if (!SessionUtil.hasValidFormCsrfToken(request, response)) {
            return;
        }
```

- `HttpServletRequest request`: Tomcat cung cấp để đọc parameter, header và session.
- `HttpServletResponse response`: Tomcat cung cấp để đặt status/header, redirect hoặc ghi body.
- `setCharacterEncoding("UTF-8")`: đặt cách đọc dữ liệu chữ trước khi đọc parameter.
- `user(request, response)`: lấy người đang đăng nhập, chặn vai trò sai.
- `hasValidFormCsrfToken`: chặn token thiếu/sai trước nhánh xử lý.

**Cô hỏi: “GET khác POST tại chức năng của em ở đâu?”**

> GET `/tasks?action=submit&id=...` đọc bằng chứng rồi hiện form.
> POST `/tasks` với `action=preview` kiểm dữ liệu rồi hiện bản xem trước.
> POST tiếp với `action=confirm` mới xác nhận lưu, sau đó redirect.
> POST không tự mã hóa dữ liệu; HTTPS mới bảo vệ dữ liệu trên đường truyền.

### 2.4. getParameter khác getParameterValues như thế nào?

**Source:** [TaskFormUtil.java:18](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/TaskFormUtil.java#L18), dòng 18–25.

```java
/** Đọc trường văn bản, dùng chuỗi rỗng khi thiếu. */
public static String text(HttpServletRequest request, String name) {
    String value = request.getParameter(name);
    if (value == null) {
        return "";
    }
    return value.trim();
}
```

`getParameter(name)` trả String hoặc null. Helper này xử lý null và trim; nó không đọc DB.
Số id vẫn là chuỗi từ request; `number(...)` mới parse và báo lỗi định dạng.

**Source:** [AdminTaskServlet.java:51](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AdminTaskServlet.java#L51), dòng 51–56.

```java
private long[] recipientIds(HttpServletRequest request) {
    String[] values = request.getParameterValues("recipientId");
    if (values == null) {
        return new long[0];
    }
    long[] ids = new long[values.length];
```

`getParameterValues("recipientId")` đọc nhiều checkbox cùng tên khi Admin giao nhiều User.
Server còn kiểm từng id; việc checkbox tồn tại trong form không chứng minh người được giao hợp lệ.

**Câu nói mẫu:** “Một ô giải thích dùng `getParameter`; nhiều checkbox cùng name dùng
`getParameterValues`. Cả hai là dữ liệu client gửi lên, chưa phải dữ liệu đáng tin.”

### 2.5. setAttribute → forward → EL: dữ liệu đi xuống client ở bước nào?

Ví dụ rõ nhất là Hồ sơ học tập; đọc toàn luồng ở mục 3.

**Source:** [LearningProfileServlet.java:52](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/LearningProfileServlet.java#L52), dòng 52–56.

```java
        request.setAttribute("profile", profile);
        String url = "/WEB-INF/views/learning-profile.jsp";
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
}
```

1. `buildProfile()` dựng dữ liệu của bean ở server.
2. `setAttribute("profile", profile)` gắn **object Java** vào request ở server.
3. `String url` chỉ JSP đích nằm dưới `WEB-INF/views`.
4. `forward(request, response)` giao cùng request/response cho JSP, không yêu cầu browser mở URL khác.
5. JSP đọc `${profile.fullName}`, gọi `getFullName()` rồi xuất giá trị vào HTML.
6. **HTML được ghi vào HTTP response** và tới browser. Browser không nhận object Java `profile`.

Mở [learning-profile.jsp:31](../tomcat-app/src/main/webapp/WEB-INF/views/learning-profile.jsp#L31) và
[LearningProfile.java:60](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/LearningProfile.java#L60) để chỉ hai đầu của ánh xạ EL.

**Cô hỏi: “setAttribute có gửi dữ liệu qua mạng chưa?”**

> Chưa. Nó đặt object trong request trên server. JSP chuyển các giá trị cần hiển thị
> thành HTML; response HTML mới được gửi về client.

### 2.6. POST preview và POST confirm: vì sao một bên forward, một bên redirect?

**Source:** [TaskServlet.java:181](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L181), dòng 181–198.

```java
} else if ("preview".equals(action) || "confirm".equals(action)) {
    TaskSubmission submission =
            TaskSubmissionDB.submit(
                    task.getId(),
                    user.getId(),
                    TaskFormUtil.submission(request),
                    "confirm".equals(action));
    if ("preview".equals(action)) {
        request.setAttribute("task", task);
        request.setAttribute("taskSubmission", submission);
        forward(request, response, "task-preview");
        return;
    }
} else {
    throw new IllegalArgumentException("Thao tác không hợp lệ.");
}
String url = request.getContextPath() + "/tasks?action=view&id=" + task.getId();
response.sendRedirect(url);
```

- Hai action đọc cùng dữ liệu form qua `TaskFormUtil.submission(request)`.
- Đối số `"confirm".equals(action)` cho biết đang xem trước hay xác nhận lưu.
- **Preview:** `setAttribute("taskSubmission", submission)` rồi forward `task-preview`;
  dữ liệu đã kiểm được hiện ngay trong cùng request, chưa tạo bài nộp.
- **Confirm:** server kiểm lại và lưu thành công; `sendRedirect(url)` để browser GET trang chi tiết.
- GET mới đọc bài đã lưu từ DB. Request attribute của POST cũ không tự sống sang GET mới.

Form xác nhận nằm tại [task-preview.jsp:50](../tomcat-app/src/main/webapp/WEB-INF/views/task-preview.jsp#L50).
Các hidden field mang nội dung sang **request mới**; server không tin điểm/owner từ hidden.

**Câu nói mẫu:** “Preview dùng forward vì cần hiển thị object vừa kiểm.
Confirm dùng redirect sau lưu: đây là Post/Redirect/Get; reload trang chi tiết không gửi lại form confirm.”
PRG không thay kiểm trùng hoặc transaction; request đồng thời vẫn phải được server xử lý.

### 2.7. Response có những phần nào? Chỉ rõ chỗ code tạo từng phần

Response gồm **status**, **header**, **body**.

- Status: [TaskServlet.java:46](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L46) — lỗi dữ liệu.
- Header: [LearningProfileServlet.java:24](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/LearningProfileServlet.java#L24) — `Cache-Control: no-store`.
- HTML body: JSP `learning-profile.jsp` sau forward; JSP directive khai báo `text/html; charset=UTF-8`.
- Redirect: [TaskServlet.java:198](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L198) — Tomcat gửi redirect, browser GET URL đích.
- JSON body: helper sau đây viết vào response.

**Source:** [ResponseUtil.java:12](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/ResponseUtil.java#L12), dòng 12–18.

```java
public static void sendJson(HttpServletResponse response, int status, String body) throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Cache-Control", "no-store");
    response.getWriter().write(body);
}
```

`getWriter().write(body)` thực sự ghi nội dung response JSON, không phải lưu DB.
`setContentType` nói cho browser biết kiểu nội dung. Header không phải body.

**Cô hỏi: “422 thì có hiện JSP được không?”**

> Có. `TaskServlet.invalid(...)` đặt status 422, gắn `taskError` vào request rồi forward
> trang `task-error`; browser vẫn nhận HTML có nội dung lỗi.
> `sendError(403)` là một cách phản hồi lỗi khác, không phải gán bean để JSP của chức năng hiển thị.

### 2.8. API JSON cũng dùng Servlet, nhưng không giả thành form truyền thống

Mở [api.js:15](../assets/js/api.js#L15): `fetch` gửi method/header/body.
Mở [AuthServlet.java:65](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AuthServlet.java#L65): Servlet nhận POST đăng nhập.
Mở [AuthServlet.java:164](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AuthServlet.java#L164): đọc body bằng `JsonUtil`, kiểm UserDB và tạo session.

**Source:** [api.js:25](../assets/js/api.js#L25), dòng 25–35.

```js
try {
  const response = await fetch(new URL(`${apiBase}${path}`, window.location.origin), {
    method,
    credentials: "same-origin",
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
    signal: controller.signal
  });
  const payload = response.status === 204 ? null : await response.json().catch(() => null);
  if (!response.ok) {
    throw clientError(
```

Ở đây `JSON.stringify(options.body)` gửi JSON, không phải form URL encoded.
`getParameter` không tự phân tích JSON body. Vì thế AuthServlet đọc `request.getReader()` qua helper `JsonUtil`.
`response.json()` ở **JavaScript** đọc body response; nó không phải object `HttpServletResponse` ở Java.

**Câu nói mẫu:** “Nhiệm vụ dùng form POST và JSP. Đăng nhập/phòng 3D dùng fetch và JSON.
Cả hai đều đi qua HTTP request/response và Servlet; riêng JSON không dùng setAttribute để trả dữ liệu cho JavaScript.”

### 2.9. Ba nguồn dữ liệu dễ nhầm: parameter, request attribute, session attribute

- **Parameter:** client gửi; đọc bằng `request.getParameter("problem")` trong TaskFormUtil.
  Kiểu String, có thể thiếu hoặc bị sửa; server phải validate.
- **Request attribute:** server đặt object bằng `request.setAttribute("profile", profile)`.
  JSP sau forward đọc được; không tự giữ qua redirect và không phải dữ liệu client vừa gửi.
- **Session attribute:** server đặt `session.setAttribute("user", user)`.
  Request sau lấy lại qua session; cookie JSESSIONID giúp Tomcat nhận diện đúng session.

**Source:** [SessionUtil.java:23](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java#L23), dòng 23–30.

```java
public static void startSession(HttpServletRequest request, User user) {
    HttpSession old = request.getSession(false);
    if (old != null) {
        old.invalidate();
    }
    HttpSession session = request.getSession(true);
    session.setAttribute("user", user);
    session.setAttribute("csrfToken", UUID.randomUUID().toString());
```

`getSession(false)` lấy phiên đang có, không tạo nếu thiếu; `getSession(true)` tạo khi cần.
`invalidate()` bỏ phiên cũ khi đăng nhập. Đọc sâu session ở phần đăng nhập của tài liệu.
Không nhầm HttpSession với `Connection` tới MySQL hoặc một phiên lắp ráp trong bảng `assembly_sessions`.

**Câu nói mẫu:** “Cùng có tên setAttribute nhưng request chỉ dùng cho lượt xử lý này,
session giữ dữ liệu qua nhiều request của phiên đăng nhập. JDBC Connection phục vụ DB, không giữ danh tính browser.”

## 3. Ba luồng phải vẽ được trên bảng

### 3.1 Trang dựng ở server (Model 2 chuẩn)

```text
GET /learning-profile
  → LearningProfileServlet.doGet                                 controller
      ├─ SessionUtil.getCurrentUser(request)    chưa đăng nhập → sendRedirect(/pages/tai-khoan.html)
      ├─ RobotDB.selectAllRobots()                               data
      ├─ StatsDB.selectLearningProfileSessionStats(user.getId()) mọi câu SQL có user_id = ?
      ├─ profile.buildProfile()                                  business (luật nằm trong bean)
      ├─ request.setAttribute("profile", profile)
      └─ String url = "/WEB-INF/views/learning-profile.jsp";
         getServletContext().getRequestDispatcher(url).forward(request, response)
  → learning-profile.jsp: ${profile.fullName}, <c:forEach>, <c:out>   view
```

### 3.2 API JSON cho JavaScript

```text
assets/js/api.js  fetch("/api/auth/login", {method: "POST", body: JSON})
  → RequestContextFilter (UTF-8, X-Request-Id)
  → AuthServlet.doPost  path "/login"
      ├─ JsonUtil.readBody / stringField           đọc JSON thủ công
      ├─ UserDB.selectUser(email) + PasswordUtil.verifyPassword
      ├─ SessionUtil.startSession → HttpSession (user, csrfToken) + cookie JSESSIONID
      └─ ResponseUtil.sendJson(response, 200, ...)
```

Mọi request ghi của API gửi header `X-CSRF-Token`; [SessionUtil.hasValidCsrfToken:117](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java#L117) so với
token trong session.

### 3.3 Form POST có chống gian lận (nhiệm vụ, nối dây, chẩn đoán)

```text
GET  /wiring?action=play&id=7        hiện form, hidden: csrfToken, id, expectedVersion, pair…
POST /wiring  action=save|submit
  → WiringServlet.doPost
      ├─ SessionUtil.hasValidFormCsrfToken      thiếu/sai → 403
      ├─ WiringDB.mutate(id, user.getId(), expectedVersion, ...)    một transaction
      │     ├─ khoá bài rồi lượt FOR UPDATE; kiểm chủ lượt, trạng thái, version
      │     ├─ WiringExercise.grade(...)        luật chấm nằm ở JavaBean
      │     └─ UPDATE ... SET version = version + 1, state = 'SUBMITTED' ; commit
      └─ sendRedirect(/wiring?action=play&id=...)    PRG: F5 không gửi lại form
```

Ý để nói: **client chỉ gửi các cặp dây; điểm, quyền, trạng thái luôn do server tính.**

## 4. Database, JavaBean và JSP: đọc dữ liệu qua từng dòng

Ba khuôn dưới đây lặp lại trong toàn dự án. Hiểu một lần là đọc được hầu hết file.

### 4.1. SELECT: từ tham số Java đến ResultSet rồi JavaBean

**Source:** [UserDB.java:121](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/UserDB.java#L121), dòng 121–142.

```java
public static User selectUser(String email) throws SQLException {
    ConnectionPool pool = ConnectionPool.getInstance();
    Connection connection = pool.getConnection();
    PreparedStatement ps = null;
    ResultSet rs = null;

    String query = "SELECT " + FIELDS + " FROM users WHERE email = ?";
    try {
        ps = connection.prepareStatement(query);
        ps.setString(1, email);
        rs = ps.executeQuery();
        User user = null;
        if (rs.next()) {
            user = readUser(rs);
        }
        return user;
    } finally {
        DBUtil.closeResultSet(rs);
        DBUtil.closePreparedStatement(ps);
        pool.freeConnection(connection);
    }
}
```

Chỉ từng dòng: pool cấp `Connection`; SQL có `?`; `setString(1, email)` bind dữ liệu.
`executeQuery()` trả ResultSet; `rs.next()` chuyển đến dòng; `readUser(rs)` dựng User.
Không có dòng thì trả null; lỗi SQL thì ném SQLException, không báo nhầm là không tìm thấy.
`finally` đóng rs/ps và trả Connection về pool cả khi lỗi.

**Ghi DB:** [UserDB.java:26](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/UserDB.java#L26)
dùng `executeUpdate()`, trả số dòng tác động. SELECT và INSERT không dùng cùng kiểu kết quả.

#### Ánh xạ cột DB sang property của bean

**Source:** [UserDB.java:281](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/UserDB.java#L281), dòng 281–284.

```java
private static User readUser(ResultSet rs) throws SQLException {
    User user = new User();
    user.setId(rs.getLong("id"));
    user.setFullName(rs.getString("display_name"));
```

`users.display_name` là cột DB; `User.fullName` là property Java.
`rs.getString("display_name")` đọc cột; `setFullName(...)` gán bean.
JSP dùng getter của bean, không dùng tên cột MySQL để tự truy vấn.

### 4.2. JavaBean: object nào JSP thực sự đọc?

Lớp `implements Serializable`, constructor rỗng, mọi field `private` có getter/setter.
Khác một class dữ liệu thông thường ở chỗ **có luật nghiệp vụ**, ví dụ
`AssemblySession.canChangeStatusTo(...)`, `TaskRubric.automaticPoints(...)`,
`WiringExercise.grade(...)`, `User.canChangeRoleOf(...)`. Servlet chỉ hỏi bean rồi gọi
`XxxDB`; JSP chỉ hiển thị.

**Source:** [LearningProfile.java:60](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/LearningProfile.java#L60), dòng 60–66.

```java
public String getFullName() {
    return fullName;
}

public void setFullName(String fullName) {
    this.fullName = fullName;
}
```

`request.setAttribute("profile", profile)` gắn bean vào request;
`${profile.fullName}` gọi `getFullName()` bên trên.
Mở [learning-profile.jsp:31](../tomcat-app/src/main/webapp/WEB-INF/views/learning-profile.jsp#L31)
để chỉ nơi hiển thị bằng `c:out`. `setAttribute` chưa gửi Java object tới browser; JSP xuất HTML vào response.

### 4.3. Khuôn Servlet trang

Lấy user từ session → đọc dữ liệu qua `XxxDB` → `setAttribute` → `String url` → `forward`.
Trường hợp lỗi: chưa đăng nhập `sendRedirect`, sai quyền 403, không thuộc quyền 404,
dữ liệu sai 422, database lỗi 503.

## 5. Bản đồ chức năng theo đợt

| Đợt | Chức năng | Route chính | Bảng chính |
| --- | --- | --- | --- |
| Nền | Đăng ký/đăng nhập, phân quyền, CSRF | `/api/auth/*`, `/api/admin/users/*` | `users` |
| Nền | Catalog robot/linh kiện/thư viện, CRUD Admin | `/robots`, `/components`, `/api/robots/*`… | `robots`, `components`, `robot_components`, `assembly_steps`, `library_resources` |
| Nền | Phiên lắp ráp, phòng 3D | `/api/assembly-sessions/*`, [pages/lap-rap-3d.html:1](../pages/lap-rap-3d.html#L1) | `assembly_sessions`, `session_*` |
| 1 | Hoàn tất lắp ráp, phiếu kết quả, ăn mừng, quiz, tra cứu lỗi, tổng kết/thống kê | `/assembly-receipt`, `/api/quiz/*`, `/learning-summary`, `/admin-stats` | `quiz_*`, `troubleshooting_guides` |
| 2 | Lái thử mô hình sau khi hoàn tất | [assembly-3d-drive.js:1](../assets/js/assembly-3d-drive.js#L1) | không đổi |
| 3 | Hai mẫu robot mới | seed | `robots`… |
| 4 | Cửa hàng, giỏ, đơn mô phỏng | `/api/shop/*`, `/api/cart/*`, `/api/orders`, `/order-history` | `shop_products`, `cart_items`, `orders`, `order_items` |
| 5, 5b | Hồ sơ học tập in A4, giữ quy tắc code môn học | `/learning-profile` | đọc bảng sẵn có |
| 6 | Nhiệm vụ thực hành, vòng nộp, rubric A/B, chẩn đoán lỗi | `/tasks`, `/admin-tasks`, `/admin-task-reviews`, `/diagnosis`, `/admin-diagnosis` | `practice_tasks`, `task_*`, `diagnosis_*` |
| 7 | Phòng nối dây có chấm, hỗ trợ Admin–User | `/wiring`, `/admin-wiring`, `/wiring-support`, `/admin-wiring-support` | `wiring_*`, `wiring_support_*` |
| UI | Giao diện học tập dùng chung (charcoal/cam, Poppins, Heroicons) | các JSP, [learning-workspace.css:1](../assets/css/learning-workspace.css#L1) | không đổi |

Bản chi tiết kèm kịch bản demo và Hỏi–Đáp theo từng đợt nằm trong `docs/TEAM_FLOW_DEMO_GUIDE.md` của project.

## 6. Cơ chế bảo vệ request, dữ liệu và lịch sử

| Khái niệm | Giải thích ngắn | Ví dụ trong code |
| --- | --- | --- |
| Server-authoritative | Client chỉ gửi dữ kiện thô; server tính điểm, kiểm quyền | điểm nối dây, phần trăm hoàn thành phiên lắp ráp, tổng đơn hàng |
| Kiểm quyền sở hữu khi đổi ID trên URL | Mọi truy vấn của USER kèm `user_id` lấy từ session; ID của người khác trả 404 | `selectAttempt(id, user.getId())` |
| CSRF | Trang lạ tự gửi form thay người dùng; chặn bằng token trong session so với token trong request | header `X-CSRF-Token` (API), hidden `csrfToken` (form) |
| XSS | Chèn HTML/JS vào dữ liệu; chặn bằng `c:out`  | mọi chuỗi từ DB trong JSP |
| PRG (Post/Redirect/Get) | Sau POST thành công chuyển hướng bằng 302; F5 không gửi lại | mọi servlet form |
| Transaction | Nhiều câu SQL cùng thành công hoặc cùng huỷ | checkout, nộp bài, tạo vòng mới |
| `SELECT … FOR UPDATE` | Khoá dòng để hai request đồng thời tuần tự hoá | nộp bài nhiệm vụ, bắt đầu chẩn đoán |
| Optimistic version | Form mang `expectedVersion`; lệch thì 422, không ghi đè bản mới | lưu nháp nối dây, trả lời hỗ trợ |
| Bản chụp dữ liệu (snapshot) | Lưu bản chụp dữ liệu lúc xảy ra để lịch sử không đổi khi nội dung gốc đổi | `quiz_attempt_answers`, `order_items`, bài nộp nhiệm vụ |
| `session_version` | Đổi mật khẩu/quyền thì tăng; phiên cũ mất hiệu lực ngay | [SessionUtil.isCurrent:37](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java#L37) |
| PBKDF2 | Băm mật khẩu có salt, 210 000 vòng, SHA-256 | [PasswordUtil:17](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/PasswordUtil.java#L17) |
| `Cache-Control: no-store` | Trang cá nhân không bị cache | mọi servlet trang |

## 7. Phân công theo thành viên

**Mục đích:** mỗi thành viên **làm chủ** một mảng code (đọc được từng dòng, mở đúng file khi
được hỏi), đồng thời nắm phần chung để trả lời khi giảng viên hỏi chéo.

Cơ sở phân công:

1. Giữ nguyên vai trò ban đầu của từng người (trang [pages/thanh-vien.html:1](../pages/thanh-vien.html#L1)
   và bảng "Phân công" trong `docs/TEAM_FLOW_DEMO_GUIDE.md`).
2. Các chức năng thêm sau (Đợt 1–7 và giao diện) được xếp về mảng gần nhất với vai trò
   gốc. Theo phân công cập nhật ngày 07/10/2026, cửa hàng, giỏ hàng và đơn mô phỏng thuộc Tài.
3. Mỗi người sở hữu **một luồng đầy đủ từ trình duyệt đến bảng MySQL**, vì giảng viên hỏi
   theo chuỗi `trang → servlet → bean → XxxDB → bảng`, không hỏi riêng một tầng.

Đây là phân công hiện tại; mỗi người cần giải thích được trọn luồng chức năng mình phụ trách.
Kiến thức chung nằm ở các mục 1–6 và 8 của file này.

### 7.1 Bảng tổng quan

| | Huỳnh Minh Tài | Văn Phạm Thảo Nhi | Phạm Tuấn Anh |
| --- | --- | --- | --- |
| Vai trò gốc | Giao diện dùng chung, tích hợp; Servlet/Tomcat, đăng nhập, session, phân quyền | Database, nội dung robot/linh kiện/thư viện, CRUD nội dung | Phòng 3D, luồng lắp ráp, tương tác JavaScript, đa thiết bị |
| Mảng sở hữu | **Nền tảng, phiên học tập và cửa hàng mô phỏng** | **Dữ liệu và nội dung** | **Tương tác và thực hành** |
| Kiến thức cần chỉ rõ trong code | Request/response, Servlet, session, form | JDBC, ResultSet, ràng buộc DB | Client/server, tương tác, lưu dữ liệu |

Không dùng số dòng cũ để suy ra khối lượng sau khi đổi phân công; đối chiếu các chức năng dưới đây.

### 7.2 Huỳnh Minh Tài — Nền tảng và phiên học tập

**Câu chốt:** "Em phụ trách khung chạy của ứng dụng: Tomcat/Servlet, đăng nhập, session,
CSRF, phiên lắp ráp, nhiệm vụ thực hành, hồ sơ học tập và cửa hàng/giỏ hàng/đơn mô phỏng;
quyền, giá và kết quả đều do server quyết định."

| Phần | Route | File nên mở (theo chuỗi) | Bảng |
| --- | --- | --- | --- |
| Đăng ký, đăng nhập, phân quyền, CSRF | `/api/auth/*`, `/api/admin/users/*` | [api.js:1](../assets/js/api.js#L1) → [AuthServlet:29](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AuthServlet.java#L29) → [SessionUtil:18](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java#L18), [PasswordUtil:17](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/PasswordUtil.java#L17), [ValidationUtil:11](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/ValidationUtil.java#L11) → [UserDB:21](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/UserDB.java#L21) → [User:14](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/User.java#L14) | `users` |
| Khung chung | mọi `/api/*`, `/account`, `/architecture` | [RequestContextFilter:17](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/filter/RequestContextFilter.java#L17), [ResponseUtil:8](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/ResponseUtil.java#L8), [JsonUtil:14](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/JsonUtil.java#L14), [HealthServlet:14](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/HealthServlet.java#L14) → [HealthDB:8](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/HealthDB.java#L8) | — |
| Phiên lắp ráp, hoàn tất, phiếu kết quả | `/api/assembly-sessions/*`, `/assembly-receipt` | [AssemblySessionServlet:38](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblySessionServlet.java#L38) → [AssemblySession:20](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblySession.java#L20) (luật) → [AssemblySessionDB:21](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblySessionDB.java#L21) → [assembly-receipt.jsp:1](../tomcat-app/src/main/webapp/WEB-INF/views/assembly-receipt.jsp#L1) | `assembly_sessions`, `session_components`, `session_steps`, `session_visual_parts` |
| Nhiệm vụ thực hành, vòng nộp, rubric A/B | `/tasks`, `/admin-tasks`, `/admin-task-reviews` | [TaskServlet:18](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L18), [AdminTaskServlet:18](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AdminTaskServlet.java#L18) → [PracticeTask:10](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/PracticeTask.java#L10), [TaskRecipient:10](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/TaskRecipient.java#L10), [TaskRound:10](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/TaskRound.java#L10), [TaskRubric:13](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/TaskRubric.java#L13), [TaskSubmission:10](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/TaskSubmission.java#L10), [TaskReview:10](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/TaskReview.java#L10) → [PracticeTaskDB:11](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/PracticeTaskDB.java#L11), [TaskSubmissionDB:12](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/TaskSubmissionDB.java#L12) | `practice_tasks`, `task_recipients`, `task_rounds`, `task_submissions`, `task_reviews` |
| Hồ sơ học tập in A4 | `/learning-profile` | [LearningProfileServlet:19](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/LearningProfileServlet.java#L19) → [LearningProfile:16](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/LearningProfile.java#L16), `Profile*` → [StatsDB:29](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/StatsDB.java#L29) → [learning-profile.jsp:1](../tomcat-app/src/main/webapp/WEB-INF/views/learning-profile.jsp#L1) + [learning-profile.css:1](../assets/css/learning-profile.css#L1) | đọc bảng sẵn có |
| Giao diện dùng chung | tất cả JSP | [workspace-header.jspf:1](../tomcat-app/src/main/webapp/WEB-INF/views/workspace-header.jspf#L1), [workspace-footer.jspf:1](../tomcat-app/src/main/webapp/WEB-INF/views/workspace-footer.jspf#L1), [learning-workspace.css:1](../assets/css/learning-workspace.css#L1) | — |
| Cửa hàng, giỏ, đơn mô phỏng (giao diện giỏ bằng JS, checkout là transaction nhiều bảng) | `/api/shop/*`, `/api/cart/*`, `/api/orders`, `/order-history` | [shop.js:1](../assets/js/shop.js#L1), [cart.js:1](../assets/js/cart.js#L1) → [CartServlet:21](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/CartServlet.java#L21), [OrderServlet:19](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/OrderServlet.java#L19) → [CartDB:13](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/CartDB.java#L13), [OrderDB:17](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/OrderDB.java#L17), [ShopProductDB:12](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ShopProductDB.java#L12) → [ShopOrder:10](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/ShopOrder.java#L10), [CartItem:8](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/CartItem.java#L8), [OrderItem:8](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/OrderItem.java#L8) → [order-history.jsp:1](../tomcat-app/src/main/webapp/WEB-INF/views/order-history.jsp#L1) | `shop_products`, `cart_items`, `orders`, `order_items` |

**Câu hỏi dự kiến cho phần của Tài:**

1. Vẽ luồng đăng nhập từ [api.js:1](../assets/js/api.js#L1) đến cookie `JSESSIONID`. Session lưu những gì?
2. Vì sao `getCurrentUser` đọc lại user từ database thay vì tin bản trong session?
3. Một phiên lắp ráp chuyển trạng thái như thế nào, ai kiểm tra? (`canChangeStatusTo`,
   `getExpectedPreparationStatus`.) Vì sao không tin phần trăm hoàn thành từ trình duyệt?
4. Nộp bài nhiệm vụ làm những bước nào trong một transaction? Thứ tự khoá để tránh deadlock?
5. "Chỉ tính lượt quiz đầu trong vòng nộp" nhằm chống điều gì? Còn hạn chế gì?
6. Hồ sơ dùng `Date` và `SimpleDateFormat` thay `java.time` vì sao? `BigDecimal` dùng ở đâu?
7. Vì sao hồ sơ có `@media print` mà không có dịch vụ xuất PDF?
8. `include` trong JSP khác custom tag ở đâu? Dự án dùng cái nào, vì sao?
9. Checkout làm những bước nào, rollback khi nào? Giá lấy từ đâu, vì sao không tin giá từ trình duyệt? `order_items` giữ snapshot để làm gì?

**Demo 3 phút:** đăng nhập → mở DevTools thấy `JSESSIONID` → `/learning-profile` (xem source
Ctrl+U thấy dữ liệu đã dựng sẵn) → in PDF → mở [TaskServlet:18](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L18) chỉ `hasValidFormCsrfToken` và PRG.

**Demo cửa hàng riêng:** vào cửa hàng → thêm vào giỏ → đổi số lượng → đặt đơn mô phỏng → xem lịch sử đơn.

### 7.3 Văn Phạm Thảo Nhi — Dữ liệu và nội dung

**Câu chốt:** "Em phụ trách lớp dữ liệu: thiết kế bảng, migration, seed, [ConnectionPool:20](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ConnectionPool.java#L20) và
các lớp `XxxDB`, cùng nội dung robot, linh kiện, quiz, hướng dẫn xử lý lỗi và thống kê."

| Phần | Route | File nên mở (theo chuỗi) | Bảng |
| --- | --- | --- | --- |
| Lớp dữ liệu nền | — | [context.xml:1](../tomcat-app/src/main/webapp/META-INF/context.xml#L1) → [ConnectionPool:20](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ConnectionPool.java#L20) → [DBUtil:8](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/DBUtil.java#L8) → [DatabaseLifecycleListener:18](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/DatabaseLifecycleListener.java#L18); [schema.sql:1](../database/schema.sql#L1), `migrations/001–012`, `erd.md` | 37 bảng |
| Catalog robot, linh kiện, bước, thư viện (đọc và CRUD Admin) | `/robots`, `/components`, `/api/robots/*`, `/api/admin/*` | [RobotCatalogPageServlet:20](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/RobotCatalogPageServlet.java#L20), [ComponentCatalogPageServlet:19](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/ComponentCatalogPageServlet.java#L19) → [RobotDB:13](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/RobotDB.java#L13), [ComponentDB:14](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ComponentDB.java#L14), [AssemblyStepDB:12](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblyStepDB.java#L12), [LibraryResourceDB:12](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/LibraryResourceDB.java#L12) → [Robot:8](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/Robot.java#L8), [Component:7](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/Component.java#L7), [AssemblyStep:7](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/AssemblyStep.java#L7) → [robots.jsp:1](../tomcat-app/src/main/webapp/WEB-INF/views/robots.jsp#L1), [components.jsp:1](../tomcat-app/src/main/webapp/WEB-INF/views/components.jsp#L1) | `robots`, `components`, `robot_components`, `assembly_steps`, `library_resources` |
| Quiz | `/api/quiz/*`, `/api/admin/quiz/*` | [QuizServlet:35](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/QuizServlet.java#L35), [AdminQuizServlet:30](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AdminQuizServlet.java#L30) → [QuizAttemptDB:27](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/QuizAttemptDB.java#L27), [QuizQuestionDB:15](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/QuizQuestionDB.java#L15) → `Quiz*` | `quiz_questions`, `quiz_options`, `quiz_attempts`, `quiz_attempt_answers` |
| Tra cứu lỗi | `/api/troubleshooting-guides`, `/api/admin/troubleshooting-guides/*` | [TroubleshootingServlet:25](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TroubleshootingServlet.java#L25) → [TroubleshootingGuideDB:12](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/TroubleshootingGuideDB.java#L12) → [TroubleshootingGuide:11](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/TroubleshootingGuide.java#L11) | `troubleshooting_guides` |
| Tổng kết và thống kê | `/learning-summary`, `/admin-stats` | [LearningSummaryServlet:26](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/LearningSummaryServlet.java#L26), [AdminStatsServlet:29](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AdminStatsServlet.java#L29) → [StatsDB:29](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/StatsDB.java#L29) (`GROUP BY`, `COUNT DISTINCT`) → [QuizRobotScore:6](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/QuizRobotScore.java#L6), [RobotPopularity:6](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/RobotPopularity.java#L6), [QuizRobotAggregate:6](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/QuizRobotAggregate.java#L6), [QuestionMissRate:9](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/QuestionMissRate.java#L9) | đọc nhiều bảng |


**Câu hỏi dự kiến:**

1. Giải thích từng dòng [UserDB.selectUser:121](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/UserDB.java#L121): vì sao có `finally`, vì sao `static`?
2. [ConnectionPool:20](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ConnectionPool.java#L20) lấy kết nối từ đâu? [context.xml:1](../tomcat-app/src/main/webapp/META-INF/context.xml#L1) khai báo gì (`maxTotal`, `maxWaitMillis`)?
3. Migration khác [schema.sql:1](../database/schema.sql#L1) thế nào? Vì sao có 12 migration thay vì sửa thẳng schema?
4. Quan hệ `robots — robot_components — components` là loại quan hệ gì? Khoá chính, khoá ngoại?
5. Quiz lưu "bản chụp" câu trả lời để làm gì? Admin sửa câu hỏi thì điểm cũ có đổi không?
6. Vì sao `quiz_attempt_answers` lưu cả nhãn lựa chọn và đáp án đúng tại thời điểm làm bài?
7. Số liệu thống kê tránh đếm trùng bằng cách nào (`COUNT(DISTINCT …)`)?
8. Vì sao `ON DELETE RESTRICT` được dùng cho dữ liệu lịch sử?
9. Vì sao không dùng JPA mà dùng JDBC?

**Demo 3 phút:** `/api/health` → `/components` (xem source) → chạy một truy vấn trong
MySQL Workbench cho `robots` ↔ `components` → thêm một câu hỏi quiz qua Admin → làm quiz →
mở `quiz_attempt_answers` thấy bản chụp câu trả lời → mở `/admin-stats` thấy số liệu `GROUP BY`.

### 7.4 Phạm Tuấn Anh — Tương tác và thực hành

**Câu chốt:** "Em phụ trách phần người dùng thao tác trực tiếp: phòng 3D, lắp ráp, chẩn đoán lỗi
tương tác và phòng nối dây; JavaScript chỉ quản lý thao tác, còn chấm điểm và quyền nằm ở server."

| Phần | Route | File nên mở (theo chuỗi) | Bảng |
| --- | --- | --- | --- |
| Phòng 3D, dựng linh kiện, camera, hiệu ứng ăn mừng, toàn màn hình, lái thử | [pages/lap-rap-3d.html:1](../pages/lap-rap-3d.html#L1) | [assembly-3d-parts.js:1](../assets/js/assembly-3d-parts.js#L1), [assembly-3d-config.js:1](../assets/js/assembly-3d-config.js#L1), [assembly-3d.js:1](../assets/js/assembly-3d.js#L1), [assembly-3d-drive.js:1](../assets/js/assembly-3d-drive.js#L1), [confetti.js:1](../assets/js/confetti.js#L1) (dữ liệu từ [data.js:1](../assets/js/data.js#L1) / API) | `session_visual_parts` (qua phiên) |
| Chẩn đoán lỗi tương tác | `/diagnosis`, `/admin-diagnosis` | [DiagnosisServlet:14](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/DiagnosisServlet.java#L14) → [DiagnosisAttempt:11](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/DiagnosisAttempt.java#L11) (điểm 4/3/3) → [DiagnosisDB:10](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/DiagnosisDB.java#L10) → [diagnosis-play.jsp:1](../tomcat-app/src/main/webapp/WEB-INF/views/diagnosis-play.jsp#L1) | `diagnosis_scenarios`, `_checks`, `_options`, `_attempts`, `_attempt_checks` |
| Phòng nối dây có chấm | `/wiring`, `/admin-wiring` | [wiring.js:1](../assets/js/wiring.js#L1) (nhấn/kéo/bàn phím) → [WiringServlet:14](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/WiringServlet.java#L14) → [WiringExercise.grade:314](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/WiringExercise.java#L314) → [WiringDB:10](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/WiringDB.java#L10) → [wiring-play.jsp:1](../tomcat-app/src/main/webapp/WEB-INF/views/wiring-play.jsp#L1), [wiring-diagram.jsp:1](../tomcat-app/src/main/webapp/WEB-INF/views/wiring-diagram.jsp#L1) | `wiring_exercises`, `_terminals`, `_rules`, `_attempts`, `_attempt_connections` |
| Hỗ trợ Admin–User | `/wiring-support`, `/admin-wiring-support` | [WiringSupportServlet:14](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/WiringSupportServlet.java#L14) → [WiringSupportRequest:9](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/WiringSupportRequest.java#L9), [WiringSupportMessage:9](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/WiringSupportMessage.java#L9) → [WiringSupportDB:9](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/WiringSupportDB.java#L9) | `wiring_support_requests`, `wiring_support_messages` |
| Giao diện đáp ứng màn hình | tất cả trang | [style.css:1](../assets/css/style.css#L1), [wiring.css:1](../assets/css/wiring.css#L1), [wiring-support.css:1](../assets/css/wiring-support.css#L1), kiểm 390/768/1024/1440 px | — |


**Câu hỏi dự kiến:**

1. Khi tắt JavaScript, phòng nối dây còn dùng được không? Bằng cách nào? (Hai nhóm radio, form POST.)
2. Kéo thả có quyết định điểm không? Dữ liệu nào thật sự gửi lên server? (`pair`, `expectedVersion`.)
3. Công thức điểm nối dây, ví dụ 4 dây đúng, 2 sai, 14 bắt buộc? (14,3.) Vì sao đúng hết cộng một dây thừa không được 100?
4. Vì sao đầu nối `line-left.OUT` và `line-right.OUT` phải có ID khác nhau?
5. `expectedVersion` giải quyết tình huống nào? Hai tab cùng lưu thì kết quả ra sao?
6. Bài chẩn đoán: vì sao quan sát chỉ hiện sau khi bấm kiểm tra? Điểm quá trình tính thế nào (4/3/3)?
7. Hỗ trợ Admin–User: vì sao bản chụp do server dựng, không nhận sơ đồ từ trình duyệt? Mỗi lượt chỉ một yêu cầu mở được bảo đảm bằng gì?
8. Phòng 3D: ai quyết định phiên "hoàn tất"? (Server kiểm linh kiện bắt buộc.)
9. Heroicons SVG ở header có thể làm hỏng sơ đồ nối dây không? (Selector đã giới hạn vào đúng SVG sơ đồ.)

**Demo 3 phút:** lắp robot trong phòng 3D → hoàn tất → ăn mừng → `/wiring`: nối đúng, nối sai và một
cặp bị cấm 5V–GND → nộp → xem điểm và giải thích → luyện lại → gửi câu hỏi hỗ trợ → xem phản hồi và gửi cập nhật.

### 7.5 Kiến thức chung cả ba người phải biết

- Ba luồng ở mục 3 của file này (trang JSP, API JSON, form POST).
- Ba khuôn code: `XxxDB`, JavaBean, Servlet trang.
- Phân biệt phần hiện dùng trong code với công nghệ chỉ ôn lý thuyết: project dùng JDBC, chưa dùng JPA/JavaMail/custom tag.
- Danh sách chỗ mở rộng hỗ trợ chức năng (filter, API JSON, `FOR UPDATE`) và cách giải thích.
- Chạy được dự án trên máy mình: JDK 17, Tomcat 9, biến `DB_*`, `npm test`, `mvn test`.

## 8. Câu hỏi–đáp ngắn (đọc to được trong 30 giây mỗi câu)

### A. Servlet và dữ liệu request

1. **Dự án theo mô hình nào?** Model 2 (MVC): Servlet là controller,
   JavaBean là model, JSP là view, `XxxDB` là tầng truy cập dữ liệu.
2. **Controller ở đâu?** Mở [TaskServlet.java:16](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L16).
   `@WebServlet` ánh xạ URL; mỗi Servlet nhận request,
   kiểm quyền, gọi `XxxDB` và bean, rồi forward hoặc trả JSON.
3. **Luật và SQL nằm ở đâu?** Mở [TaskRubric.java:28](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/TaskRubric.java#L28): luật điểm ở bean.
   Mở [UserDB.java:121](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/UserDB.java#L121): SQL/PreparedStatement ở XxxDB.
   Servlet đọc request và điều phối, không tự tính điểm hoặc viết SQL ở JSP.
4. **forward khác redirect thế nào?** Mở [TaskServlet.java:36](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L36)
   và [TaskServlet.java:197](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L197).
   `forward` chuyển nội bộ trong cùng request, URL browser không đổi và JSP đọc được request attribute.
   `sendRedirect` bảo browser gửi request mới tới URL đích; dùng sau confirm thành công.
5. **request attribute khác session attribute?** `profile` đặt vào request chỉ sống trong
   một request; `user` đặt vào session được giữ qua nhiều request của phiên đăng nhập.
6. **Vì sao không đặt currentUser thành field thay đổi của Servlet?** Một instance Servlet có thể xử lý
   nhiều request đồng thời. Field dùng chung có thể bị request khác ghi đè. Mở
   [TaskServlet.java:141](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L141): `user` là biến local.
7. **URL vào Servlet được ánh xạ ở đâu?** Mở [TaskServlet.java:16](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/TaskServlet.java#L16):
   `@WebServlet("/tasks")`. [web.xml:1](../tomcat-app/src/main/webapp/WEB-INF/web.xml#L1) có cấu hình ứng dụng và trang chào;
   không nhầm URL mapping với hidden parameter `action` của form.

### B. JDBC

8. **PreparedStatement khác Statement?** Tham số `?` gán bằng `setXxx`, nên dữ liệu người
   dùng không thành mã SQL. Mở [UserDB.java:129](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/UserDB.java#L129):
   `prepareStatement`, `setString`, `executeQuery`.
9. **Connection pool là gì, cấu hình ở đâu?** Giữ sẵn một tập kết nối để dùng lại. Khai báo
   trong [META-INF/context.xml:13](../tomcat-app/src/main/webapp/META-INF/context.xml#L13).
   [ConnectionPool.java:26](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ConnectionPool.java#L26) lookup DataSource;
   `getConnection` mượn connection, `freeConnection` trả lại qua `close()` trong `finally` của XxxDB.
10. **Vì sao ném `SQLException` thay vì trả null khi DB lỗi?** Để servlet trả 503 "database chưa
    sẵn sàng"; nuốt lỗi rồi trả `null` khiến sai mật khẩu và database hỏng khó phân biệt.
11. **Transaction là gì? Ví dụ?** Nhóm câu SQL cùng thành công hoặc cùng huỷ
    (`setAutoCommit(false)`, `commit`, `rollback`). Ví dụ checkout: trừ kho, tạo đơn, tạo
    từng dòng đơn, xoá giỏ; lỗi bước nào cũng quay về như cũ.
12. **`FOR UPDATE` để làm gì?** Khoá dòng trong transaction để request sửa cùng dòng phải đợi nhau.
    Mở [TaskSubmissionDB.java:315](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/TaskSubmissionDB.java#L315),
    chỉ thứ tự khoá, kiểm trạng thái sau khoá, ghi và commit/rollback.
    Khoá cần đi cùng kiểm luật và ràng buộc UNIQUE; riêng câu SELECT không bảo đảm toàn bộ luồng nộp.
13. **Bản chụp dữ liệu (snapshot) là gì, vì sao cần?** Lưu bản chụp tại thời điểm xảy ra (tên, giá, câu trả lời,
    điểm). Admin đổi giá hay sửa câu hỏi sau đó không làm lịch sử cũ sai.
14. **Vì sao điểm dùng `DECIMAL` và `BigDecimal`?** Số thực nhị phân sai số; điểm cần làm tròn
    HALF_UP chính xác một lần ở kết quả cuối.

### C. Session và bảo mật

15. **Đăng nhập lưu ở đâu?** Trong `HttpSession` ở server (`user` và `csrfToken`); trình duyệt
    chỉ giữ cookie `JSESSIONID`.
16. **Mật khẩu lưu thế nào?** Băm PBKDF2-HMAC-SHA256, salt ngẫu nhiên 16 byte, 210 000 vòng;
    cột `users.password_hash` không chứa mật khẩu gốc.
17. **`session_version` để làm gì?** Đổi mật khẩu hoặc quyền làm tăng số này; mỗi request
    `getCurrentUser` đọc lại từ database và so, nên phiên cũ hết hiệu lực ngay.
18. **CSRF là gì, chống thế nào?** Trang lạ lợi dụng cookie đang đăng nhập để gửi request thay
    bạn. Mỗi phiên có `csrfToken`; API gửi trong header `X-CSRF-Token`, form HTML gửi trong
    hidden field; thiếu hoặc sai thì 403.
19. **XSS là gì, chống thế nào?** Chèn mã vào dữ liệu hiển thị. JSP bọc mọi chuỗi từ DB bằng
    `c:out` để chuỗi chứa `<` được hiển thị như văn bản.
20. **Vì sao đổi `?id=` trên URL không xem được bài người khác?** Mọi truy vấn của USER kèm
    `user_id` từ session; id lạ trả 404.
21. **Vì sao client không tự gửi điểm?** Server tính lại từ dữ liệu gốc; trường `score`,
    `userId` trong request bị bỏ qua (đã kiểm bằng test).

### D. JSP, EL, JSTL

22. **EL tìm biến ở đâu?** Với tên attribute không ghi scope: lần lượt page, request, session, application. Vì vậy
    không đặt tên vòng lặp trùng implicit object như `header`, `param`, `cookie`.
23. **Dự án dùng `c:forEach`, `c:if`, `c:choose` ở đâu?** Các JSP trong `WEB-INF/views`, ví dụ
    danh sách bài trong [wiring-list.jsp:1](../tomcat-app/src/main/webapp/WEB-INF/views/wiring-list.jsp#L1), trạng thái nhiệm vụ trong [task-view.jsp:1](../tomcat-app/src/main/webapp/WEB-INF/views/task-view.jsp#L1).
24. **include dùng để làm gì?** Gắn header/footer chung: `<%@ include file="workspace-header.jspf" %>`
    để các trang có cùng khung.
25. **JavaBean cần ba quy tắc nào?** Constructor không tham số, get/set cho mọi field private,
    `Serializable`. [JavaBeanRulesTest:1](../tomcat-app/src/test/java/vn/edu/webpro/robotlab/business/JavaBeanRulesTest.java#L1) quét mọi lớp trong `business/`.
26. **EL đọc getter boolean ở đâu?** Mở [learning-profile.jsp:68](../tomcat-app/src/main/webapp/WEB-INF/views/learning-profile.jsp#L68)
    và [ProfileRobotEntry.java:55](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/ProfileRobotEntry.java#L55):
    `profileRobot.completed` gọi `isCompleted()` của bean. `c:choose` chọn phần HTML cần hiển thị;
    điều kiện nghiệp vụ được dựng ở bean, không viết Java scriptlet trong JSP.
27. **Dự án có dùng custom tag không?** Không. Phần dùng chung làm bằng include và
    getter của bean.

### E. HTTP

28. **Mã trạng thái nào được dùng?** 200 OK, 201 tạo mới, 302 chuyển hướng, 401 chưa xác thực ở API,
    403 sai vai trò hoặc sai token, 404 không có/không thuộc quyền, 409 xung đột ở API có quy ước này,
    422 dữ liệu không hợp lệ, 503 database lỗi. Không suy ra mọi route đều dùng cùng một mã lỗi;
    chỉ nhánh `response.setStatus`/`sendError`/`sendRedirect` của Servlet đang hỏi.
29. **PRG là gì?** POST xử lý xong thì trả 302 sang trang GET; tải lại trang không gửi lại form.
30. **Vì sao vừa có JSP vừa có API JSON?** Trang tĩnh trong `pages/` (phòng 3D, tick linh kiện)
    cần dữ liệu không tải lại trang; trang cá nhân dựng ở server cho dễ kiểm quyền. Cả hai dùng
    chung `data/` và `business/`.
31. **[RequestContextFilter:17](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/filter/RequestContextFilter.java#L17) làm gì?** Đặt UTF-8, gắn `X-Request-Id`, bắt lỗi 500 để
    trả thông báo an toàn; không chứa nghiệp vụ.
32. **`Cache-Control: no-store` để làm gì?** Yêu cầu cache không lưu response cá nhân.
    Quyền vẫn được kiểm lại ở server, không dựa riêng vào hành vi nút Back của trình duyệt.

### F. Tính năng

33. **Chấm nối dây thế nào?** Cặp dây là không hướng, chuẩn hoá theo ID. `N` cặp bắt buộc, `C`
    cặp đúng, `W` cặp sai/thừa/cấm. Điểm = `100 × max(C − W, 0) / N`, làm tròn HALF_UP một chữ số.
    Chỉ ghi "đúng toàn bộ" khi `C = N` và `W = 0`. Luật ở [WiringExercise.grade:314](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/WiringExercise.java#L314) và [WiringGrade:9](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/WiringGrade.java#L9).
34. **Hai tab cùng sửa một lượt thì sao?** Mỗi form mang `expectedVersion`; tab lưu sau bị 422,
    không ghi đè.
35. **Quiz chấm ở đâu?** Server ([QuizAttemptDB:27](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/QuizAttemptDB.java#L27) + bean), lưu bản chụp câu trả lời. Trình duyệt chỉ
    gửi lựa chọn, không gửi đáp án đúng.
36. **Nhiệm vụ có vòng nộp thế nào?** Mỗi người được giao có các vòng; chỉ tính lượt quiz/chẩn
    đoán đầu của vòng; Admin chấm, "cần bổ sung" mở vòng mới. Mẫu A: lắp ráp 40, quiz 40, giải
    thích 20. Mẫu B: lắp ráp 30, quiz 25, chẩn đoán 25, giải thích 20.
37. **Vì sao quan sát chẩn đoán không có sẵn trong HTML?** Chỉ trả sau khi server ghi nhận lượt
    chọn phép kiểm tra đó; nếu có sẵn thì xem mã nguồn là biết đáp án.
38. **Hồ sơ học tập khác tổng kết ở đâu?** Hồ sơ là tài liệu in A4 có chỗ cho giảng viên nhận xét,
    chọn "tốt nhất" theo tỷ lệ điểm; tổng kết là bảng thống kê trên màn hình.
39. **Phòng 3D lấy dữ liệu từ đâu?** JavaScript đọc mô hình từ catalog (API/seed); việc coi phiên là
    hoàn tất do server kiểm các linh kiện bắt buộc, không tin phần trăm client.
40. **Dữ liệu nối dây có phải mô phỏng điện không?** Không. Chỉ chấm các cặp nối trực tiếp trong
    bài mẫu; không mô phỏng dòng điện, không xác nhận robot thật.

### G. Câu hỏi bẫy

41. **"Dự án có dùng JPA, JavaMail, custom tag không?"** Không. Truy cập DB hiện dùng JDBC;
    chưa có email thông báo; phần trình bày dùng chung dùng include và getter của bean. Biết
    phân biệt đúng các công nghệ, không chỉ vào source chức năng chưa tồn tại.
42. **"Client–server và Java–DB khác nhau ở đâu?"** [api.js:15](../assets/js/api.js#L15)
    dùng HTTP/fetch; [ConnectionPool.java:42](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ConnectionPool.java#L42) dùng JDBC Connection.
    Browser không gọi MySQL trực tiếp. JSON là body HTTP, không phải giao thức JDBC.
43. **"Phần này em làm hay công cụ làm?"** Trả lời trung thực về cách làm việc; quan trọng hơn là
    giải thích được từng dòng và chỉ ra file, hàm, bảng liên quan.

### H. Câu hỏi bổ sung (từ phần demo)

44. **Dữ liệu khởi nguồn từ đâu?** [database/seed.sql:1](../database/seed.sql#L1) là dữ liệu mẫu cho MySQL; nguồn chính khi chạy
    đầy đủ là MySQL qua servlet. [assets/js/data.js:1](../assets/js/data.js#L1) chỉ là bản dự phòng để giao diện vẫn có nội dung khi
    API chưa sẵn sàng.
45. **Vì sao không chạy [index.html:1](../index.html#L1) trực tiếp?** HTML chỉ là view. Đăng nhập, `HttpSession`, servlet và
    JDBC chỉ hoạt động khi WAR được deploy trên Tomcat; giao diện gọi `/api` cùng origin nên cần deploy đúng context và URL đã cấu hình.
46. **Vì sao không tin phần trăm hoàn thành do trình duyệt tính?** Người dùng sửa được JavaScript hoặc gọi
    thẳng API. [AssemblySessionDB.completeSession():87](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/AssemblySessionDB.java#L87) chỉ ghi `COMPLETED` khi chính câu `UPDATE` đối chiếu
    `session_visual_parts` với `robot_components` và thấy đủ; điều kiện nằm ngay trong `WHERE` nên kiểm tra và
    ghi xảy ra nguyên tử, hai request gần đồng thời không tạo hai kết quả khác nhau.
47. **Sao vừa có `/components` vừa có `/api/components`?** Hai view của cùng dữ liệu: `/components` trả HTML dựng ở
    server (xem source thấy bảng đã đầy đủ), `/api/components` trả JSON cho JavaScript; cùng dùng [ComponentDB:14](../tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ComponentDB.java#L14).

## 9. Kiểm thử và chạy dự án

| Loại | Công cụ | Kiểm gì |
| --- | --- | --- |
| Kiểm nguồn | Node (`npm test`, các test trong `tests/server/`) | cấu trúc môn học (`course-structure`), kiểm quy tắc code môn học, form/CSRF/no-store, hợp đồng giao diện |
| Luật nghiệp vụ | JUnit (`mvn -f tomcat-app/pom.xml test`) | điểm rubric, chấm nối dây, luật công bố, [JavaBeanRulesTest:1](../tomcat-app/src/test/java/vn/edu/webpro/robotlab/business/JavaBeanRulesTest.java#L1), [JspExpressionContractTest:1](../tomcat-app/src/test/java/vn/edu/webpro/robotlab/view/JspExpressionContractTest.java#L1) |
| Chạy thật | Tomcat + MySQL (cổng 8080) | `/api/health` phải trả `"database":"connected"` |

Số test thay đổi khi code đổi; lấy số thật từ output của lần chạy đang trình bày,
không học thuộc con số từ báo cáo cũ. Node và JUnit là công cụ kiểm tra lúc phát triển, **không phải runtime**.

Nhớ: IntelliJ phải dùng **JDK 17** (Project SDK); Tomcat truyền
`-DDB_HOST -DDB_PORT -DDB_NAME -DDB_USER -DDB_PASSWORD` qua VM options vì [context.xml:1](../tomcat-app/src/main/webapp/META-INF/context.xml#L1)
đọc các giá trị đó; Tomcat không tự đọc `.env`.

## 10. Cách luyện chéo và lịch ôn

1. **Vòng 1 — kể phần mình (10 phút/người):** vẽ luồng của mảng mình trên bảng, không nhìn tài liệu.
2. **Vòng 2 — hỏi chéo (15 phút/người):** người khác chọn ngẫu nhiên 5 câu trong mục 8 của file này
   và 3 câu trong mục của bạn, yêu cầu "mở file nào, dòng nào".
3. **Vòng 3 — đổi chỗ:** mỗi người trình bày một luồng của người khác (ví dụ Tuấn Anh giải thích checkout của Tài,
   Nhi giải thích chấm nối dây, Tài giải thích quiz của Nhi). Ai bị vấp thì ghi lại để ôn.
4. **Vòng cuối — mô phỏng:** giả lập giảng viên, mỗi người trả lời liên tục 10 phút, chỉ được dùng
   chuỗi file làm gợi ý.

Quy tắc khi trả lời: nói tên file và hàm trước, giải thích tác dụng sau; nếu không chắc thì
nói "em kiểm tra lại trong code" và mở file, không đoán.

### Lịch gợi ý

| Mốc | Việc |
| --- | --- |
| Còn 7 ngày | Mỗi người đọc phần của mình và mục 1–6 và mục 8 của file này |
| Còn 5 ngày | Vẽ luồng không nhìn tài liệu; chạy lại dự án trên máy từng người |
| Còn 3 ngày | Hỏi chéo vòng 2; chốt dữ liệu demo |
| Còn 1 ngày | Mô phỏng vòng cuối; kiểm `npm test`, `mvn test`; kiểm tài khoản demo |
| Ngày kiểm tra | Mang bản chạy được; không thay đổi code mới ngay trước giờ trình bày |

## 11. Khi không nhớ và việc trước ngày kiểm tra

Đừng đoán. Đi theo chuỗi file, đọc to điều thấy:

```text
trang/JS → Servlet (doGet/doPost) → bean (luật) → XxxDB (SQL, transaction) → bảng → JSP
```

Mở bản đồ đầu tài liệu và mục 2 để đi thẳng tới file:dòng; mục 3 có luồng nối các file đó.

### Danh sách việc trước ngày kiểm tra

- [ ] Chạy được Tomcat 8080; `/api/health` báo `connected`; đăng nhập USER và ADMIN.
- [ ] IntelliJ dùng JDK 17; Rebuild Project trước khi chạy.
- [ ] Tài khoản demo đăng nhập được (bảng ở `docs/TEAM_FLOW_DEMO_GUIDE.md` trong project); nếu không, tạo
      tài khoản mới và ghi lại mật khẩu ở nơi riêng, **không** ghi vào Git.
- [ ] Dữ liệu demo: ít nhất một phiên đã hoàn tất, một lượt quiz, một nhiệm vụ đang mở.
- [ ] `npm test` và `mvn -f tomcat-app/pom.xml test` đều xanh trên máy sẽ trình bày.
- [ ] Mỗi người tự vẽ lại ba luồng ở mục 3 không nhìn tài liệu.
- [ ] Mỗi người trả lời được các câu của mình ở mục 7 và 43+ câu ở mục 8.
- [ ] Không mang `.env`, `.idea/workspace.xml` (chứa mật khẩu DB) khi nộp file nén.
