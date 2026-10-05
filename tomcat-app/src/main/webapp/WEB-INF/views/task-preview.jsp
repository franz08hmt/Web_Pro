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
        <h1>Xem trước bài nộp</h1>
        <h2><c:out value="${task.title}"/></h2>
        <p>Phiên lắp ráp #<c:out value="${taskSubmission.sessionId}"/>, hoàn thành lúc <c:out
    value="${taskSubmission.assemblyCompletedDisplay}"/></p>
        <p>Quiz lượt #<c:out value="${taskSubmission.quizAttemptId}"/> – <c:out value="${taskSubmission.quizScore}"/>/<c:out
    value="${taskSubmission.quizTotal}"/> – <c:out value="${taskSubmission.quizSubmittedDisplay}"/> <c:if
        test="${taskSubmission.reusedQuiz}">(dùng lại từ lần nộp #<c:out value="${taskSubmission.quizReusedFrom}"/>)</c:if></p>
        <c:if test="${taskSubmission.hasDiagnosis}">
            <p>Tình huống: <c:out value="${taskSubmission.diagnosisTitle}"/></p>
            <p>Chẩn đoán lượt #<c:out value="${taskSubmission.diagnosisAttemptId}"/> – <c:out
        value="${taskSubmission.diagnosisScoreDisplay}"/>/10 – lúc <c:out value="${taskSubmission.diagnosisSubmittedAtDisplay}"/>
            <c:choose>
                <c:when test="${taskSubmission.reusedDiagnosis}">(dùng lại từ lần nộp #<c:out
                value="${taskSubmission.diagnosisReusedFrom}"/>)</c:when>
                <c:otherwise>(lượt của vòng này)</c:otherwise>
            </c:choose>
            </p>
        </c:if>
        <p>Điểm tự động dự kiến: <c:out value="${taskSubmission.automaticDisplay}"/>/<c:out
    value="${taskSubmission.rubric.automaticMaximum}"/> — chờ đánh giá phần giải thích</p>
        <c:if test="${taskSubmission.late}">
            <p>Nộp muộn</p>
        </c:if>
        <h2>Giải thích</h2>
        <h3>Bạn gặp vấn đề gì?</h3>
        <p class="task-text"><c:out value="${taskSubmission.problem}"/></p>
        <h3>Vì sao chọn cách kiểm tra/xử lý đó?</h3>
        <p class="task-text"><c:out value="${taskSubmission.reasoning}"/></p>
        <h3>Nếu làm lại, bạn sẽ thay đổi điều gì?</h3>
        <p class="task-text"><c:out value="${taskSubmission.improvement}"/></p>
        <form method="post" action="${pageContext.request.contextPath}/tasks">
            <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
            <input type="hidden" name="id" value="<c:out value="${task.id}"/>">
            <input type="hidden" name="action" value="confirm">
            <input type="hidden" name="sessionId" value="<c:out value="${taskSubmission.sessionId}"/>">
            <input type="hidden" name="roundId" value="<c:out value="${taskSubmission.roundId}"/>">
            <c:if test="${taskSubmission.hasDiagnosis}">
                <p>Tình huống: <c:out value="${taskSubmission.diagnosisTitle}"/></p>
                <input type="hidden" name="diagnosisAttemptId" value="<c:out value="${taskSubmission.diagnosisAttemptId}"/>">
            </c:if>
            <input type="hidden" name="problem" value="<c:out value="${taskSubmission.problem}"/>">
            <input type="hidden" name="reasoning" value="<c:out value="${taskSubmission.reasoning}"/>">
            <input type="hidden" name="improvement" value="<c:out value="${taskSubmission.improvement}"/>">
            <button>Xác nhận nộp bài</button>
        </form>
        <p><a href="?action=submit&amp;id=<c:out value="${task.id}"/>">Quay lại chọn bằng chứng</a></p>
    </body>
</html>
