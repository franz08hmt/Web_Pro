<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <link rel="icon" href="${pageContext.request.contextPath}/assets/icons/cpu-chip.svg" type="image/svg+xml">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Thông báo nhiệm vụ | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-task-error practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
            <h1>Chưa thực hiện được thao tác</h1>
            <p role="alert"><c:out value="${taskError}"/></p>
            <p>Vui lòng quay lại nhiệm vụ để kiểm tra vòng hiện tại và điều kiện nộp.</p>
            <p>
            <c:choose>
                <c:when test="${sessionScope.user.admin}">
                    <a href="${pageContext.request.contextPath}/admin-tasks">Quản lý nhiệm vụ</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/tasks">Nhiệm vụ của tôi</a>
                </c:otherwise>
            </c:choose>
            </p>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
