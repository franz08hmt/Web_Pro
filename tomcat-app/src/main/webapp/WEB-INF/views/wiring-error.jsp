<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Thông báo nối dây | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring.css?v=20261006.2">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-wiring-error wiring">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <nav aria-label="Điều hướng">
                <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Tài khoản</a>
                <c:choose>
                    <c:when test="${sessionScope.user.admin}">
                        <a href="${pageContext.request.contextPath}/admin-wiring">Quản lý bài nối dây</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/wiring">Thực hành nối dây</a>
                    </c:otherwise>
                </c:choose>
            </nav>
            <h1>Chưa thực hiện được thao tác</h1>
            <p role="alert"><c:out value="${wiringError}"/></p>
            <p>Hãy mở lại lượt đã lưu nếu bản nháp thay đổi ở tab khác.</p>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
