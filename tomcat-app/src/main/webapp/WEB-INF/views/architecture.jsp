<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Luồng Servlet/JSP | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="description" content="Danh mục và kiến thức thực hành Robot Assembly Lab.">
    </head>
    <body class="learning-workspace workspace-page-architecture">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <h1>Luồng Servlet/JSP của Robot Assembly Lab</h1>
            <p>Trang này do <code><c:out value="${controllerName}"/></code> đặt dữ liệu vào request rồi
            <code>forward()</code> sang JSP. JSP chỉ hiển thị, không truy vấn MySQL.</p>
            <pre class="flow">Browser (view: HTML/JSP)
            → <c:out value="${controllerName}"/> (controller: servlet)
            → JavaBean trong package business (model)
            → <c:out value="${databaseLayer}"/> (data access layer)
            → MySQL

            JSON contract: <c:out value="${apiContract}"/></pre>
            <p><a href="${pageContext.request.contextPath}/api/health">Mở response JSON health</a></p>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
