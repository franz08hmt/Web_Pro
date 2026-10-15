<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bản đồ môn học | Robot Assembly Lab</title>
    <meta name="description" content="Kiến thức Servlet/JSP, chức năng và luồng trình bày Robot Assembly Lab.">
    <c:url var="courseCss" value="/assets/css/learning-workspace.css"><c:param name="v" value="20261006.5"/></c:url>
    <link rel="stylesheet" href="<c:out value="${courseCss}"/>">
    <c:url var="coursePrintCss" value="/assets/css/course-map.css"><c:param name="v" value="20261015.1"/></c:url>
    <link rel="stylesheet" href="<c:out value="${coursePrintCss}"/>">
</head>
<body class="learning-workspace course-map">
    <%@ include file="workspace-header.jspf" %>
    <main id="workspace-main" class="workspace-content" tabindex="-1">
        <h1>Bản đồ môn học</h1>
        <p>Học lắp theo mẫu → xem sản phẩm và quy trình → kiểm tra kiến thức.
            Khi có bộ linh kiện riêng, vào Xưởng để thiết kế, kiểm tra và xuất hồ sơ.</p>
        <p>Mô phỏng lắp ghép giản lược, không mô phỏng điện, vật lý hoặc firmware.
            Thiết kế riêng không thay thế bằng chứng hoàn thành mẫu chính thức.</p>
        <nav aria-label="Mục lục bản đồ" class="no-print">
            <a href="#course-demo">Luồng chính trong 3 phút</a> ·
            <a href="#course-topics">Kiến thức trong code</a> · <a href="#course-compare">Cùng dữ liệu, hai cách</a>
        </nav>
        <section id="course-demo">
            <h2>Luồng chính trong 3 phút</h2>
            <c:url var="courseModels" value="/pages/mau-robot.html"/>
            <c:url var="courseDashboard" value="/account"/>
            <c:url var="courseDesigns" value="/designs"/>
            <ol>
                <li><a href="<c:out value="${courseModels}"/>">Chọn mẫu robot</a> và mở phòng lắp.</li>
                <li>Thử gắn sai để đọc giải thích; sửa hướng, gắn đúng và hoàn tác một thao tác.</li>
                <li>Lắp đủ, hoàn tất; mở trang hoàn thiện để xem ảnh, quy trình, phiếu và làm quiz đúng mẫu.</li>
                <li><a href="<c:out value="${courseDashboard}"/>">Bảng điều khiển</a> giúp mở lại kết quả của mình.</li>
                <li><a href="<c:out value="${courseDesigns}"/>">Xưởng thiết kế</a>: chọn kit → lắp → kiểm tra
                    → hồ sơ/BOM → thêm phần thiếu vào giỏ; không cần checkout.</li>
            </ol>
            <p>Cần đăng nhập cho phiên lắp, Bảng điều khiển và Xưởng; dùng dữ liệu demo để trình bày.</p>
        </section>
        <section id="course-topics">
            <h2>Kiến thức ứng dụng ở đâu trong code?</h2>
            <p>Chỉ nêu tên chủ đề; không gắn số chương hoặc số slide chưa đối chiếu được.</p>
            <c:forEach var="courseItem" items="${courseItems}">
                <article class="workspace-panel">
                    <h3><c:out value="${courseItem.topic}"/></h3>
                    <p><strong><c:out value="${courseItem.feature}"/></strong>:
                        <c:out value="${courseItem.explanation}"/></p>
                    <p><code><c:out value="${courseItem.sourcePath}"/>:<c:out value="${courseItem.sourceLine}"/></code></p>
                    <c:url var="courseTry" value="${courseItem.path}"/>
                    <a class="no-print" href="<c:out value="${courseTry}"/>">Mở thử</a>
                    <c:if test="${courseItem.requiresLogin}"><span> — Cần đăng nhập</span></c:if>
                </article>
            </c:forEach>
        </section>
        <section id="course-compare">
            <h2>Cùng dữ liệu, hai cách</h2>
            <c:url var="courseServerRobots" value="/robots"/>
            <c:url var="courseServerComponents" value="/components"/>
            <c:url var="courseClientComponents" value="/pages/linh-kien.html"/>
            <ul>
                <li><a href="<c:out value="${courseServerRobots}"/>">Robot bằng Servlet → JSP</a>:
                    server đọc JDBC, đặt attribute rồi dựng HTML. Hợp với form, kết quả và trang tổng hợp.</li>
                <li><a href="<c:out value="${courseModels}"/>">Mẫu robot bằng HTML + API</a>:
                    trình duyệt fetch JSON rồi dựng DOM. Hợp với tìm kiếm và phòng 3D tương tác.</li>
                <li>Linh kiện: <a href="<c:out value="${courseServerComponents}"/>">bản Servlet/JSP</a> và
                    <a href="<c:out value="${courseClientComponents}"/>">catalog chính thức</a>.</li>
            </ul>
            <p>Cùng JavaBean, XxxDB và MySQL; server luôn kiểm quyền và quyết định hợp lệ.</p>
        </section>
        <details class="workspace-technical">
            <summary>Request đang dựng trang này</summary>
            <p>Controller: <code><c:out value="${controllerName}"/></code> → setAttribute → forward → JSP.</p>
            <p>Tầng dữ liệu: <c:out value="${databaseLayer}"/>.</p>
            <p>Context path: <code><c:out value="${pageContext.request.contextPath}"/></code>
                (rỗng nghĩa là ứng dụng ở đường dẫn gốc).</p>
            <c:url var="courseHealth" value="${apiContract}"/>
            <a href="<c:out value="${courseHealth}"/>">Mở response JSON kiểm tra kết nối</a>
        </details>
        <p>Dùng lệnh In của trình duyệt để in bản đồ; thanh điều hướng được ẩn khi in.</p>
    </main>
    <%@ include file="workspace-footer.jspf" %>
</body>
</html>
