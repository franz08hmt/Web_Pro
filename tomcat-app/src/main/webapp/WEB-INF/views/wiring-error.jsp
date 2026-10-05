<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Phòng thực hành nối dây | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring.css?v=20261005.2">
    </head>
    <body class="wiring">
        <nav aria-label="Điều hướng">
            <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Tài khoản</a>
            <a href="${pageContext.request.contextPath}/wiring">Thực hành nối dây</a>
        </nav>
        <h1>Chưa thực hiện được thao tác</h1>
        <p role="alert"><c:out value="${wiringError}"/></p>
        <p>Hãy mở lại lượt đã lưu nếu bản nháp thay đổi ở tab khác.</p>
    </body>
</html>
