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
        <h1>Quản lý bài nối dây</h1>
        <a href="?action=new">Tạo bài nháp</a>
        <c:forEach var="wiringExercise" items="${wiringExercises}">
            <section>
                <h2><c:out value="${wiringExercise.title}"/></h2>
                <p><c:out value="${wiringExercise.code}"/> · <c:out value="${wiringExercise.stateLabel}"/></p>
                <a href="?action=view&amp;id=${wiringExercise.id}">Chi tiết / quản lý</a>
            </section>
        </c:forEach>
    </body>
</html>
