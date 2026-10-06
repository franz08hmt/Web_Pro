<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Thực hành nối dây | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring.css?v=20261006.2">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-wiring-play wiring">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <nav aria-label="Điều hướng">
                <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Tài khoản</a>
                <a href="${pageContext.request.contextPath}/wiring">Thực hành nối dây</a>
            </nav>
            <h1><c:out value="${wiringExercise.title}"/></h1>
            <p>Lượt #<c:out value="${wiringAttempt.id}"/> · phiên bản <c:out value="${wiringAttempt.version}"/> · chưa chấm.</p>
            <p>
            <a href="${pageContext.request.contextPath}/wiring-support?action=new&amp;attemptId=${wiringAttempt.id}">
            Gửi câu hỏi hỗ trợ (lưu nháp trước)
            </a>
            · <a href="${pageContext.request.contextPath}/wiring-support">Xem lịch sử hỗ trợ</a>
            </p>
            <div class="workspace-workbench">
                <section class="workspace-panel" id="wiring-interactive-diagram">
                    <h2>Sơ đồ thực hành</h2>
                    <c:set var="wiringDiagramEditable" value="true"/>
                    <c:set var="wiringDiagramConnections" value="${wiringAttempt.connections}"/>
                    <%@ include file="wiring-diagram.jsp" %>
                    <p id="wiring-status" role="status" aria-live="polite">Sơ đồ đang hiển thị các dây đã lưu.</p>
                    <button type="button" id="wiring-cancel" hidden>Hủy chân đang chọn</button>
                    <button type="button" id="wiring-delete" hidden>Xóa dây đang chọn</button>
                    <noscript><p>JavaScript đang tắt: chọn hai chân bên dưới rồi bấm Thêm dây. Lưu và nộp vẫn hoạt động.</p></noscript>
                </section>
                <aside class="workspace-panel" aria-label="Mục tiêu và hướng dẫn thực hành">
                    <h2>Mục tiêu và thao tác</h2>
                    <p><c:out value="${wiringExercise.objective}"/></p>
                    <p><c:out value="${wiringExercise.scopeText}"/></p>
                    <p>Nhấn hai chân hoặc kéo chân A sang B. Dùng Enter/Space để chọn, Escape để hủy; chọn dây rồi nhấn Xóa dây.</p>
                    <p>Trên màn hình nhỏ có thể cuộn sơ đồ; các nhóm chọn chân bằng văn bản luôn dùng được.</p>
                </aside>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/wiring" id="wiring-form">
                <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                <input type="hidden" name="id" value="<c:out value="${wiringAttempt.id}"/>">
                <input type="hidden" name="expectedVersion" value="<c:out value="${wiringAttempt.version}"/>">

                <div id="wiring-payload">
                    <c:forEach var="wiringConnection" items="${wiringAttempt.connections}">
                        <input type="hidden" name="pair" value="<c:out value="${wiringConnection.pairKey}"/>">
                    </c:forEach>
                </div>
                <div class="wiring-choices">
                    <fieldset>
                        <legend>Chân A</legend>
                        <c:forEach var="wiringTerminal" items="${wiringExercise.terminals}">
                            <label>
                            <input type="radio" name="terminalA" value="${wiringTerminal.id}">
                            <c:out value="${wiringTerminal.label}"/>
                            </label>
                        </c:forEach>
                    </fieldset>
                    <fieldset>
                        <legend>Chân B</legend>
                        <c:forEach var="wiringTerminal" items="${wiringExercise.terminals}">
                            <label>
                            <input type="radio" name="terminalB" value="${wiringTerminal.id}">
                            <c:out value="${wiringTerminal.label}"/>
                            </label>
                        </c:forEach>
                    </fieldset>
                </div>
                <button name="action" value="add">Thêm dây và lưu nháp</button>
                <h2>Danh sách dây bằng văn bản</h2>
                <div id="wiring-connections">
                    <c:forEach var="wiringConnection" items="${wiringAttempt.connections}">
                        <label>
                        <input type="radio" name="removePair" value="<c:out value="${wiringConnection.pairKey}"/>">
                        <c:out value="${wiringConnection.first.label}"/> · <c:out value="${wiringConnection.second.label}"/>
                        </label>
                    </c:forEach>
                </div>
                <button name="action" value="remove">Xóa dây đã chọn và lưu nháp</button>
                <button name="action" value="save">Lưu nháp</button>
                <button name="action" value="submit">Nộp để chấm (chốt sơ đồ)</button>
            </form>
            <p>Lưu nháp không chấm. Nộp sẽ khóa sơ đồ; muốn sửa sau đó hãy tạo lượt luyện lại.</p>
            <a href="${pageContext.request.contextPath}/pages/lap-rap.html?model=<c:out value="${wiringExercise.robotId}"/>">
            Sơ đồ tham khảo hiện có
            </a>
            <script src="${pageContext.request.contextPath}/assets/js/wiring.js?v=20261006.1" defer></script>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
