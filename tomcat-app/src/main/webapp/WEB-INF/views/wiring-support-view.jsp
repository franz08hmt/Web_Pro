<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Trao đổi hỗ trợ nối dây | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring.css?v=20261006.2">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring-support.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-wiring-support-view wiring wiring-support">
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
            <h1>Trao đổi hỗ trợ nối dây</h1>
            <h2>Yêu cầu #<c:out value="${supportRequest.id}"/> · <c:out value="${supportRequest.stateLabel}"/></h2>
            <p>Lượt gốc #<c:out value="${supportRequest.attemptId}"/> · <c:out value="${supportRequest.exerciseTitle}"/></p>
            <p>Đã giải quyết hỗ trợ và sơ đồ đúng toàn bộ là hai kết quả riêng biệt.</p>
            <c:if test="${not supportAdmin}">
                <a href="${pageContext.request.contextPath}/wiring?action=play&amp;id=${supportRequest.attemptId}">Mở lượt gốc</a>
            </c:if>
            <c:forEach var="supportMessage" items="${supportRequest.messages}">
                <section class="support-message">
                    <h2>Tin #<c:out value="${supportMessage.id}"/> · <c:out value="${supportMessage.authorName}"/></h2>
                    <p>
                    <c:choose>
                        <c:when test="${supportMessage.admin}">Admin phản hồi</c:when>
                        <c:otherwise>User gửi</c:otherwise>
                    </c:choose>
                    · <c:out value="${supportMessage.createdAtDisplay}"/>
                    </p>
                    <p class="support-text"><c:out value="${supportMessage.content}"/></p>
                    <p>Đầu nối liên quan: <c:out value="${supportMessage.terminalLabel}"/></p>
                    <c:if test="${supportMessage.hasSnapshot}">
                        <h3>Bản chụp tại lúc gửi tin #<c:out value="${supportMessage.id}"/></h3>
                        <p class="support-text support-snapshot"><c:out value="${supportMessage.snapshotText}"/></p>
                        <c:set var="wiringDiagramConnections" value="${supportMessage.snapshotConnections}"/>
                        <%@ include file="wiring-diagram.jsp" %>
                    </c:if>
                </section>
            </c:forEach>
            <c:if test="${not supportRequest.closed}">
                <form method="post" action="${pageContext.request.contextPath}/${supportRoute}">
                    <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                    <input type="hidden" name="id" value="${supportRequest.id}">
                    <input type="hidden" name="expectedVersion" value="${supportRequest.version}">
                    <input type="hidden" name="action" value="message">
                    <label for="support-content">Nội dung trao đổi (User 20–2000; Admin 10–2000 ký tự)</label>
                    <textarea id="support-content" name="content" maxlength="4000" rows="5" required></textarea>
                    <c:if test="${not supportAdmin}">
                        <label><input type="checkbox" name="sendSnapshot">Gửi bản chụp mới từ lượt gốc đã lưu</label>
                    </c:if>
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
                    <button type="submit">Gửi nội dung</button>
                </form>
                <form method="post" action="${pageContext.request.contextPath}/${supportRoute}">
                    <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                    <input type="hidden" name="id" value="${supportRequest.id}">
                    <input type="hidden" name="expectedVersion" value="${supportRequest.version}">
                    <input type="hidden" name="action" value="close">
                    <button type="submit">
                    <c:choose>
                        <c:when test="${supportAdmin}">Đóng hỗ trợ</c:when>
                        <c:otherwise>Đánh dấu đã giải quyết hỗ trợ</c:otherwise>
                    </c:choose>
                    </button>
                </form>
            </c:if>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
