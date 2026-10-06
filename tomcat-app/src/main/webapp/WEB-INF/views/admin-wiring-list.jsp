<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Quản lý bài nối dây | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring.css?v=20261006.2">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-admin-wiring-list wiring">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <nav aria-label="Điều hướng">
                <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Tài khoản</a>
                <a href="${pageContext.request.contextPath}/admin-wiring">Quản lý bài nối dây</a>
            </nav>
            <h1>Quản lý bài nối dây</h1>
            <a href="?action=new">Tạo bài nháp</a>
            <div class="workspace-grid">
                <c:forEach var="wiringExercise" items="${wiringExercises}">
                    <section class="workspace-panel">
                        <h2><c:out value="${wiringExercise.title}"/></h2>
                        <p><c:out value="${wiringExercise.code}"/> · <c:out value="${wiringExercise.stateLabel}"/></p>
                        <a href="?action=view&amp;id=${wiringExercise.id}">Chi tiết / quản lý</a>
                    </section>
                </c:forEach>
            </div>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
