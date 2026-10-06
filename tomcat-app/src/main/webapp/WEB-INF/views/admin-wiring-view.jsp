<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <link rel="icon" href="${pageContext.request.contextPath}/assets/icons/cpu-chip.svg" type="image/svg+xml">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Chi tiết bài nối dây | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring.css?v=20261006.2">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.5">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-admin-wiring-view wiring">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <nav aria-label="Điều hướng">
                <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Tài khoản</a>
                <a href="${pageContext.request.contextPath}/admin-wiring">Quản lý bài nối dây</a>
            </nav>
            <h1><c:out value="${wiringExercise.title}"/></h1>
            <p><c:set var="workspaceStatusTone" value="neutral"/>
            <c:choose>
                <c:when test="${wiringExercise.published}">
                    <c:set var="workspaceStatusTone" value="info"/>
                </c:when>
                <c:when test="${wiringExercise.draft}">
                    <c:set var="workspaceStatusTone" value="warning"/>
                </c:when>
            </c:choose>
            <span class="status-badge status-badge--<c:out value="${workspaceStatusTone}"/>">
                <c:out value="${wiringExercise.stateLabel}"/>
            </span> · <c:out value="${wiringExercise.robotName}"/></p>
            <p><c:out value="${wiringExercise.objective}"/></p>
            <p><c:out value="${wiringExercise.scopeText}"/></p>
            <c:set var="wiringDiagramConnections" value="${wiringExercise.referenceConnections}"/>
            <%@ include file="wiring-diagram.jsp" %>
            <h2>Quy tắc biên soạn</h2>
            <c:forEach var="wiringRule" items="${wiringExercise.rules}">
                <p>#<c:out value="${wiringRule.terminalA}"/> · #<c:out value="${wiringRule.terminalB}"/>
                · <c:set var="workspaceRuleKind" value="${wiringRule.kind}"/>
<c:choose>
    <c:when test="${wiringRule.required}">
        <span class="status-badge status-badge--info">Bắt buộc</span>
    </c:when>
    <c:otherwise>
        <span class="status-badge status-badge--danger">Cặp bị cấm</span>
    </c:otherwise>
</c:choose>: <c:out value="${wiringRule.explanation}"/></p>
            </c:forEach>
            <c:choose>
                <c:when test="${wiringPreview}">
                    <p>Xem trước chỉ đọc; không tạo lượt của người học.</p>
                </c:when>
                <c:otherwise>
                    <a href="?action=preview&amp;id=${wiringExercise.id}">Xem trước như người học</a>
                    <c:if test="${wiringExercise.draft}">
                        <a href="?action=edit&amp;id=${wiringExercise.id}">Sửa nháp và đầu nối/quy tắc</a>
                    </c:if>
                    <form method="post" action="${pageContext.request.contextPath}/admin-wiring">
                        <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                        <input type="hidden" name="id" value="${wiringExercise.id}">
                        <c:if test="${wiringExercise.draft}">
                            <button name="action" value="publish">Công bố và khóa nội dung</button>
                        </c:if>
                        <c:if test="${wiringExercise.published}">
                            <button name="action" value="archive">Lưu trữ</button>
                            <p class="summary">Lưu trữ sẽ ẩn nội dung khỏi danh sách mặc định và ngăn bắt đầu lượt mới; lịch sử vẫn
được giữ.</p>
                        </c:if>
                        <button name="action" value="duplicate">Nhân bản thành nháp mới</button>

</form>
                </c:otherwise>
            </c:choose>
            <a href="${pageContext.request.contextPath}/admin-wiring">Danh sách bài</a>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
