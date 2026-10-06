<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Luyện chẩn đoán | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-diagnosis-list practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
            <h1>Luyện chẩn đoán</h1>
            <p>Chọn phép kiểm tra để thu thập quan sát mô phỏng, rồi kết luận nguyên nhân và cách xử lý.</p>
            <p>Lượt luyện không tính vào nhiệm vụ. Muốn làm bài được giao, hãy bắt đầu từ trang nhiệm vụ.</p>
            <h2>Đang làm dở</h2>
            <c:if test="${empty diagnosisAttempts}">
                <p>Chưa có lượt đang làm.</p>
            </c:if>
            <c:forEach var="diagnosisAttempt" items="${diagnosisAttempts}">
                <p><a href="?action=play&amp;id=<c:out value="${diagnosisAttempt.id}"/>">
                Lượt #<c:out value="${diagnosisAttempt.id}"/> · <c:out value="${diagnosisAttempt.scenario.title}"/>
                </a></p>
            </c:forEach>
            <h2>Tình huống đang công bố</h2>
            <div class="workspace-grid">
                <c:forEach var="diagnosisScenario" items="${diagnosisScenarios}">
                    <section class="task-card">
                        <h3><c:out value="${diagnosisScenario.title}"/></h3>
                        <p><c:out value="${diagnosisScenario.robotName}"/> · Phiên bản <c:out value="${diagnosisScenario.versionNo}"/></p>
                        <p class="task-text"><c:out value="${diagnosisScenario.symptomText}"/></p>
                        <form method="post" action="${pageContext.request.contextPath}/diagnosis">
                            <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                            <input type="hidden" name="action" value="start">
                            <input type="hidden" name="scenarioId" value="<c:out value="${diagnosisScenario.id}"/>">
                            <button>Bắt đầu luyện</button>
                        </form>
                    </section>
                </c:forEach>
            </div>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
