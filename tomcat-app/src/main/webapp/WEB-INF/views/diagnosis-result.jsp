<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Kết quả chẩn đoán | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-diagnosis-result practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
            <h1>Kết quả: <c:out value="${diagnosisAttempt.scenario.title}"/></h1>
            <p>Lượt #<c:out value="${diagnosisAttempt.id}"/> · Chốt lúc <c:out value="${diagnosisAttempt.submittedAtDisplay}"/></p>
            <h2>Quá trình chẩn đoán</h2>
            <p>Thu thập đủ quan sát cần thiết: <c:out value="${diagnosisAttempt.requiredDone}"/>/<c:out
            value="${diagnosisAttempt.requiredTotal}"/> phép · <c:out value="${diagnosisAttempt.collectionPointsDisplay}"/>/4</p>
            <p>Tổng điểm chẩn đoán: <c:out value="${diagnosisAttempt.scoreDisplay}"/>/10</p>
            <c:forEach var="diagnosisCheck" items="${diagnosisAttempt.scenario.checks}">
                <c:if test="${diagnosisCheck.chosen}">
                    <h3><c:out value="${diagnosisCheck.label}"/></h3>
                    <p>Đã kiểm tra lúc <c:out value="${diagnosisCheck.chosenAtDisplay}"/></p>
                    <p class="task-text"><c:out value="${diagnosisCheck.observationDisplay}"/></p>
                </c:if>
            </c:forEach>
            <h3>Phép cần thiết còn thiếu</h3>
            <c:if test="${empty diagnosisAttempt.missingChecks}">
                <p>Đã thu thập đủ quan sát cần thiết.</p>
            </c:if>
            <c:forEach var="diagnosisCheck" items="${diagnosisAttempt.missingChecks}">
                <p><c:out value="${diagnosisCheck.label}"/></p>
            </c:forEach>
            <h3>Nguyên nhân đã chọn</h3>
            <p><c:out value="${diagnosisAttempt.chosenCause.label}"/> —
            <c:choose><c:when test="${diagnosisAttempt.causeCorrect}">Đúng (3/3)</c:when>
                <c:otherwise>Sai
                    (0/3)</c:otherwise></c:choose></p>
            <p class="task-text"><c:out value="${diagnosisAttempt.chosenCause.feedbackText}"/></p>
            <h3>Cách xử lý đã chọn</h3>
            <p><c:out value="${diagnosisAttempt.chosenAction.label}"/> —
            <c:choose><c:when test="${diagnosisAttempt.actionCorrect}">Đúng (3/3)</c:when>
                <c:otherwise>Sai
                    (0/3)</c:otherwise></c:choose></p>
            <p class="task-text"><c:out value="${diagnosisAttempt.chosenAction.feedbackText}"/></p>
            <h3>Giải thích tình huống</h3>
            <p class="task-text"><c:out value="${diagnosisAttempt.scenario.explanationText}"/></p>
            <p>Quan sát là dữ liệu mô phỏng của bài tập, không phải phép đo từ robot thật.</p>
            <c:if test="${not empty diagnosisAttempt.scenario.guideId}">
                <p><a href="${pageContext.request.contextPath}/pages/tra-cuu-loi.html?robotId=<c:out
                value="${diagnosisAttempt.scenario.robotId}"/>&amp;guideId=<c:out value="${diagnosisAttempt.scenario.guideId}"/>">Xem
                hướng dẫn tra cứu liên quan</a></p>
            </c:if>
            <c:if test="${empty diagnosisAttempt.scenario.guideId}">
                <p><a href="${pageContext.request.contextPath}/pages/tra-cuu-loi.html?robotId=<c:out
                value="${diagnosisAttempt.scenario.robotId}"/>">Tra cứu hướng dẫn theo mẫu robot</a></p>
            </c:if>
            <p><a href="${pageContext.request.contextPath}/tasks">Nhiệm vụ của tôi</a> · <a href="?">Danh sách luyện</a></p>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
