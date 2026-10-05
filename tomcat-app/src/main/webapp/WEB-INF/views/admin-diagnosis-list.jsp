<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Chẩn đoán thực hành | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
    </head>
    <body class="practice-tasks">
        <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
        <h1>Quản lý tình huống chẩn đoán</h1>
        <p><a href="?action=new">Tạo nháp tình huống</a></p>
        <c:forEach var="diagnosisScenario" items="${diagnosisScenarios}">
            <section class="task-card">
                <h2><a href="?action=view&amp;id=<c:out value="${diagnosisScenario.id}"/>">
                <c:out value="${diagnosisScenario.title}"/>
                </a></h2>
                <p><c:out value="${diagnosisScenario.robotName}"/> · <c:out value="${diagnosisScenario.stateLabel}"/>
                · Phiên bản <c:out value="${diagnosisScenario.versionNo}"/></p>
            </section>
        </c:forEach>
    </body>
</html>
