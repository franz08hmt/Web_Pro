<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <link rel="icon" href="${pageContext.request.contextPath}/assets/icons/cpu-chip.svg" type="image/svg+xml">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Tài khoản | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-account">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <h1>Tài khoản thực hành</h1>
            <article>
                <p>Trang được <code>AccountPageServlet</code> bảo vệ bằng <code>HttpSession</code>,
                sau đó controller đặt JavaBean <code>user</code> vào request và forward đến JSP.</p>
                <dl>
                <dt>Họ tên</dt><dd><c:out value="${user.fullName}"/></dd>
                <dt>Email</dt><dd><c:out value="${user.email}"/></dd>
                <dt>Vai trò</dt><dd><c:out value="${user.role}"/></dd>
                <dt>Ngày tạo</dt><dd><c:out value="${user.createdAt}"/></dd>
                </dl>
                <c:if test="${isAdmin}"><p><a href="${pageContext.request.contextPath}/pages/admin-users.html">Quản lý tài
                    khoản</a></p></c:if>
                <p><a href="${pageContext.request.contextPath}/index.html">Về trang chủ</a></p>
            </article>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
