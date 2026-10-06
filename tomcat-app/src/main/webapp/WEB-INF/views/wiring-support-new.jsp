<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Gửi yêu cầu hỗ trợ | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring.css?v=20261006.2">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring-support.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-wiring-support-new wiring wiring-support">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <c:set var="supportRoute" value="wiring-support"/>
            <c:if test="${supportAdmin}">
                <c:set var="supportRoute" value="admin-wiring-support"/>
            </c:if>
            <nav aria-label="Điều hướng">
                <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Tài khoản</a>
                <a href="${pageContext.request.contextPath}/<c:out value="${supportRoute}"/>">Danh sách hỗ trợ</a>
            </nav>
            <h1>Gửi yêu cầu hỗ trợ</h1>
            <p>Chỉ gửi dữ liệu đã lưu trên server. Hãy lưu nháp trước; thao tác đang kéo/chưa lưu không nằm trong bản chụp.</p>
            <p>Lượt #<c:out value="${wiringAttempt.id}"/> · phiên bản <c:out value="${wiringAttempt.version}"/></p>
            <p><c:out value="${wiringExercise.title}"/></p>
            <c:set var="wiringDiagramConnections" value="${wiringAttempt.connections}"/>
            <%@ include file="wiring-diagram.jsp" %>
            <form method="post" action="${pageContext.request.contextPath}/wiring-support">
                <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                <input type="hidden" name="action" value="create">
                <input type="hidden" name="attemptId" value="${wiringAttempt.id}">
                <input type="hidden" name="expectedVersion" value="${wiringAttempt.version}">
                <label for="support-content">Câu hỏi (20–2000 ký tự)</label>
                <textarea id="support-content" name="content" minlength="20" maxlength="4000" rows="5" required></textarea>
                <fieldset>
                    <legend>Đầu nối liên quan (tùy chọn)</legend>
                    <label><input type="radio" name="terminalId" value="" checked>Không chọn đầu nối riêng</label>
                    <c:forEach var="wiringTerminal" items="${wiringExercise.terminals}">
                        <label>
                        <input type="radio" name="terminalId" value="${wiringTerminal.id}">
                        <c:out value="${wiringTerminal.label}"/>
                        </label>
                    </c:forEach>
                </fieldset>
                <button type="submit">Gửi câu hỏi và bản chụp đã lưu</button>
            </form>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
