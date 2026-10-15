<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="robots" content="noindex, nofollow">
    <title>Không tìm thấy trang | Robot Assembly Lab</title>
    <c:url var="errorPageCss" value="/assets/css/learning-workspace.css"><c:param name="v" value="20261006.5"/></c:url>
    <link rel="stylesheet" href="<c:out value="${errorPageCss}"/>">
</head>
<body class="learning-workspace">
    <%@ include file="workspace-header.jspf" %>
    <main id="workspace-main" class="workspace-content" tabindex="-1">
        <h1>Không tìm thấy trang</h1>
        <p>Trang bạn mở không tồn tại hoặc bạn không được phép xem nội dung này.</p>
        <c:url var="errorHome" value="/index.html"/>
        <c:url var="errorDashboard" value="/account"/>
        <p><a href="<c:out value="${errorHome}"/>">Về trang chủ</a> ·
            <a href="<c:out value="${errorDashboard}"/>">Mở Bảng điều khiển</a></p>
    </main>
    <%@ include file="workspace-footer.jspf" %>
</body>
</html>

