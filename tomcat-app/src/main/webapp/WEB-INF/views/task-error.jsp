<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Nhiệm vụ thực hành | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
    </head>
    <body class="practice-tasks">
        <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
        <h1>Chưa thực hiện được thao tác</h1>
        <p role="alert"><c:out value="${taskError}"/></p>
        <p>Vui lòng quay lại nhiệm vụ để kiểm tra vòng hiện tại và điều kiện nộp.</p>
        <p><a href="${pageContext.request.contextPath}/tasks">Nhiệm vụ của tôi</a> · <a
            href="${pageContext.request.contextPath}/admin-tasks">Quản lý nhiệm vụ</a></p>
    </body>
</html>
