<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Nộp bài nhiệm vụ | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.1">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-task-submit practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
            <h1>Nộp bài: <c:out value="${task.title}"/></h1>
            <c:if test="${recipient.showCurrentRoundQuiz and not empty taskQuiz}">
                <p>Quiz lượt #<c:out value="${taskQuiz.quizAttemptId}"/> – <c:out value="${taskQuiz.quizScore}"/>/<c:out
                value="${taskQuiz.quizTotal}"/> – <c:out value="${taskQuiz.quizSubmittedDisplay}"/> <c:choose><c:when
                    test="${taskQuiz.reusedQuiz}">(dùng lại từ lần nộp #<c:out
                    value="${taskQuiz.quizReusedFrom}"/>)</c:when><c:otherwise>(lượt
                    của vòng này)</c:otherwise></c:choose></p>
        </c:if>
        <c:if test="${task.rubricB and recipient.showCurrentRoundQuiz}">
            <c:if test="${not empty taskDiagnosis}">
                <p>Chẩn đoán lượt #<c:out value="${taskDiagnosis.diagnosisAttemptId}"/> – <c:out
                value="${taskDiagnosis.diagnosisScoreDisplay}"/>/10 – lúc <c:out value="${taskDiagnosis.diagnosisSubmittedAtDisplay}"/>
                <c:choose>
                    <c:when test="${taskDiagnosis.reusedDiagnosis}">(dùng lại từ lần nộp #<c:out
                    value="${taskDiagnosis.diagnosisReusedFrom}"/>)</c:when>
                    <c:otherwise>(lượt của vòng này)</c:otherwise>
                </c:choose>
                </p>
            </c:if>
            <c:if test="${recipient.canStartDiagnosis}">
                <form method="post" action="${pageContext.request.contextPath}/tasks">
                    <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                    <input type="hidden" name="action" value="diagnosis">
                    <input type="hidden" name="id" value="<c:out value="${task.id}"/>">
                    <button>
                    <c:choose>
                        <c:when test="${recipient.currentRound.hasDiagnosisAttempt}">Tiếp tục chẩn đoán</c:when>
                        <c:otherwise>Bắt đầu chẩn đoán cho nhiệm vụ này</c:otherwise>
                    </c:choose>
                    </button>
                </form>
            </c:if>
        </c:if>
        <p>Chọn một phiên của chính bạn đã hoàn thành đúng mẫu. Mốc bằng chứng là lúc bắt đầu vòng 1; các vòng sau có thể dùng lại phiên này
        nếu hợp lệ.</p>
        <c:choose>
            <c:when test="${empty evidenceList}">
                <p>Chưa có phiên lắp ráp phù hợp. Hãy hoàn thành mẫu robot rồi quay lại.</p>
            </c:when>
            <c:otherwise>
                <form method="post" action="${pageContext.request.contextPath}/tasks">
                    <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                    <input type="hidden" name="id" value="<c:out value="${task.id}"/>">
                    <input type="hidden" name="roundId" value="<c:out value="${recipient.currentRound.id}"/>">
                    <input type="hidden" name="action" value="preview">
                    <fieldset>
                        <legend>Bằng chứng lắp ráp</legend>
                        <c:forEach var="evidence" items="${evidenceList}">
                            <label class="task-choice"><input type="radio" name="sessionId" value="<c:out value="${evidence.sessionId}"/>"
                            required> Phiên #<c:out value="${evidence.sessionId}"/> · <c:out value="${evidence.robotName}"/> · hoàn
                            thành <c:out value="${evidence.assemblyCompletedDisplay}"/></label>
                        </c:forEach>
                    </fieldset>
                    <label>Bạn gặp vấn đề gì?<textarea name="problem" required minlength="20" maxlength="1500"></textarea></label>
                    <label>Vì sao chọn cách kiểm tra/xử lý đó?<textarea name="reasoning" required minlength="20"
                    maxlength="1500"></textarea></label>
                    <label>Nếu làm lại, bạn sẽ thay đổi điều gì?<textarea name="improvement" required minlength="20"
                    maxlength="1500"></textarea></label>
                    <button>Xem trước bài nộp</button>
                </form>
            </c:otherwise>
        </c:choose>
    </main>
    <%@ include file="workspace-footer.jspf" %>
</body>
</html>
