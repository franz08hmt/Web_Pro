<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Quiz nhiệm vụ | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-task-quiz practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
            <h1>Quiz nhiệm vụ: <c:out value="${task.title}"/></h1>
            <p>Chỉ lượt hợp lệ đầu tiên của mỗi vòng được chốt tính điểm. Gửi thiếu câu hoặc lựa chọn không thuộc câu sẽ không lưu lượt.</p>
            <c:choose>
                <c:when test="${not empty quizQuestions}">
                    <form method="post" action="${pageContext.request.contextPath}/tasks">
                        <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                        <input type="hidden" name="id" value="<c:out value="${task.id}"/>">
                        <input type="hidden" name="roundId" value="<c:out value="${recipient.currentRound.id}"/>">
                        <input type="hidden" name="action" value="quiz">
                        <c:forEach var="quizQuestion" items="${quizQuestions}">
                            <fieldset>
                                <legend><c:out value="${quizQuestion.prompt}"/></legend>
                                <c:forEach var="quizOption" items="${quizQuestion.options}">
                                    <label class="task-choice"><input type="radio" name="answer_<c:out value="${quizQuestion.id}"/>"
                                    value="<c:out value="${quizOption.id}"/>" required> <c:out value="${quizOption.label}"/></label>
                                </c:forEach>
                            </fieldset>
                        </c:forEach>
                        <button>Chốt lượt tính điểm của vòng này</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <p>Lượt tính điểm của vòng này đã chốt hoặc vòng không còn nhận quiz.</p>
                    <c:if test="${not empty taskQuiz}">
                        <p>Quiz lượt #<c:out value="${taskQuiz.quizAttemptId}"/> – <c:out value="${taskQuiz.quizScore}"/>/<c:out
                        value="${taskQuiz.quizTotal}"/> – <c:out value="${taskQuiz.quizSubmittedDisplay}"/> <c:choose><c:when
                            test="${taskQuiz.reusedQuiz}">(dùng lại từ lần nộp #<c:out value="${taskQuiz.quizReusedFrom}"/>)</c:when>
                        <c:otherwise>(lượt của vòng này)</c:otherwise></c:choose></p>
                </c:if>
            </c:otherwise>
        </c:choose>
        <c:if test="${not empty quizAttempt}">
            <h2>Kết quả lượt đã chốt</h2>
            <c:forEach var="quizAnswer" items="${quizAttempt.answers}">
                <section class="task-card">
                    <h3><c:out value="${quizAnswer.questionPromptSnapshot}"/></h3>
                    <p>Bạn chọn: <c:out value="${quizAnswer.selectedOptionLabelSnapshot}"/></p>
                    <p>Đáp án: <c:out value="${quizAnswer.correctOptionLabelSnapshot}"/></p>
                    <p><c:out value="${quizAnswer.explanationSnapshot}"/></p>
                </section>
            </c:forEach>
        </c:if>
        <p><a href="?action=view&amp;id=<c:out value="${task.id}"/>">Trở về nhiệm vụ</a></p>
        <p><a href="${pageContext.request.contextPath}/pages/kiem-tra.html?model=<c:out value="${task.robotId}"/>">Làm quiz luyện
        tập</a></p>
    </main>
    <%@ include file="workspace-footer.jspf" %>
</body>
</html>
