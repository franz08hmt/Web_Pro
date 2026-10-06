<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <link rel="icon" href="${pageContext.request.contextPath}/assets/icons/cpu-chip.svg" type="image/svg+xml">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Quản lý chẩn đoán | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-admin-diagnosis-list practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
            <h1>Quản lý tình huống chẩn đoán</h1>
            <p><a href="?action=new">Tạo nháp tình huống</a></p>
            <div class="workspace-grid">
                <c:forEach var="diagnosisScenario" items="${diagnosisScenarios}">
                    <section class="task-card">
                        <h2><a href="?action=view&amp;id=<c:out value="${diagnosisScenario.id}"/>">
                        <c:out value="${diagnosisScenario.title}"/>
                        </a></h2>
                        <p><c:out value="${diagnosisScenario.robotName}"/> · <c:out value="${diagnosisScenario.stateLabel}"/>
                        · Phiên bản <c:out value="${diagnosisScenario.versionNo}"/></p>
                    </section>
                </c:forEach>
            </div>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
