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
        <h1>Chấm bài: <c:out value="${task.title}"/></h1>
        <p>Người nộp: <c:out value="${reviewRecipient.fullName}"/> · Đang chấm lần nộp #<c:out
    value="${taskSubmission.submissionNo}"/></p>
        <p>Không điều chỉnh điểm tự động. Mỗi lần chấm lưu một bản ghi mới; sửa chấm phải có lý do và vòng sau chưa có hoạt động.</p>
        <p><a href="${pageContext.request.contextPath}/admin-tasks?action=view&amp;id=<c:out value="${task.id}"/>">Tổng quan nhiệm
        vụ</a></p>
        <form method="post" action="${pageContext.request.contextPath}/admin-task-reviews">
            <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
            <input type="hidden" name="id" value="<c:out value="${taskSubmission.id}"/>">
            <input type="hidden" name="action" value="review">
            <input type="hidden" name="expectedReviewId" value="<c:choose><c:when test="${empty
                currentReview}">0</c:when><c:otherwise><c:out value="${currentReview.id}"/></c:otherwise></c:choose>">
            <fieldset>
                <legend>Mức giải thích</legend>
                <label class="task-choice">
                    <input type="radio" name="explanationLevel" value="0" required
                    <c:if test="${empty currentReview or currentReview.explanationLevel == 0}">checked</c:if>>
                    0 điểm — Không có nội dung hoặc không liên quan
                </label>
                <label class="task-choice">
                    <input type="radio" name="explanationLevel" value="1" required
                    <c:if test="${not empty currentReview and currentReview.explanationLevel == 1}">checked</c:if>>
                    5 điểm — Kể lại thao tác nhưng chưa giải thích
                </label>
                <label class="task-choice">
                    <input type="radio" name="explanationLevel" value="2" required
                    <c:if test="${not empty currentReview and currentReview.explanationLevel == 2}">checked</c:if>>
                    10 điểm — Giải thích đúng một phần, còn thiếu căn cứ
                </label>
                <label class="task-choice">
                    <input type="radio" name="explanationLevel" value="3" required
                    <c:if test="${not empty currentReview and currentReview.explanationLevel == 3}">checked</c:if>>
                    15 điểm — Lập luận đúng, liên hệ với kết quả quan sát
                </label>
                <label class="task-choice">
                    <input type="radio" name="explanationLevel" value="4" required
                    <c:if test="${not empty currentReview and currentReview.explanationLevel == 4}">checked</c:if>>
                    20 điểm — Lập luận rõ, có căn cứ và đề xuất cải thiện hợp lý
                </label>
            </fieldset>
            <fieldset>
                <legend>Kết luận</legend>
                <label class="task-choice">
                    <input type="radio" name="conclusion" value="PASSED" required
                    <c:if test="${empty currentReview or currentReview.passed}">checked</c:if>>
                    Đạt yêu cầu
                </label>
                <label class="task-choice">
                    <input type="radio" name="conclusion" value="NEEDS_REVISION" required
                    <c:if test="${currentReview.needsRevision}">checked</c:if>>
                    Cần bổ sung
                </label>
                <label class="task-choice">
                    <input type="radio" name="conclusion" value="NOT_PASSED" required
                    <c:if test="${currentReview.notPassed}">checked</c:if>>
                    Chưa đạt yêu cầu
                </label>
            </fieldset>
            <p>Cần gia hạn trước nếu muốn yêu cầu bổ sung khi đã quá hạn và chính sách không nhận bài muộn. Nhiệm vụ đã đóng chỉ được kết
            luận Đạt / Chưa đạt.</p>
            <label>Điểm mạnh <textarea name="strengths" maxlength="4000"><c:out value="${currentReview.strengths}"/></textarea></label>
            <label>Cần cải thiện (bắt buộc với Cần bổ sung / Chưa đạt)<textarea name="improvements" maxlength="4000"><c:out
            value="${currentReview.improvements}"/></textarea></label>
            <label>Hướng làm lại (bắt buộc với Cần bổ sung / Chưa đạt)<textarea name="retryGuidance" maxlength="4000"><c:out
            value="${currentReview.retryGuidance}"/></textarea></label>
            <label>Lý do sửa chấm (bắt buộc khi đã chấm)<textarea name="changeReason" maxlength="4000"></textarea></label>
            <button>Lưu lần chấm</button>
        </form>
        <c:if test="${not empty diagnosisAttempt}">
            <h2>Quá trình chẩn đoán</h2>
            <p>Điểm thu thập quan sát: <c:out value="${diagnosisAttempt.collectionPointsDisplay}"/>/4</p>
            <p>Thu thập đủ quan sát cần thiết: <c:out value="${diagnosisAttempt.requiredDone}"/>/<c:out
        value="${diagnosisAttempt.requiredTotal}"/> phép · Điểm: <c:out value="${diagnosisAttempt.scoreDisplay}"/>/10</p>
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
            <c:choose><c:when
                test="${diagnosisAttempt.causeCorrect}">Đúng (3/3)</c:when><c:otherwise>Sai (0/3)</c:otherwise></c:choose></p>
            <p class="task-text"><c:out value="${diagnosisAttempt.chosenCause.feedbackText}"/></p>
            <h3>Cách xử lý đã chọn</h3>
            <p><c:out value="${diagnosisAttempt.chosenAction.label}"/> —
            <c:choose><c:when
            test="${diagnosisAttempt.actionCorrect}">Đúng (3/3)</c:when><c:otherwise>Sai (0/3)</c:otherwise></c:choose></p>
            <p class="task-text"><c:out value="${diagnosisAttempt.chosenAction.feedbackText}"/></p>
            <h3>Giải thích tình huống</h3>
            <p class="task-text"><c:out value="${diagnosisAttempt.scenario.explanationText}"/></p>
            <p>Quan sát là dữ liệu mô phỏng của bài tập, không phải phép đo từ robot thật.</p>
        </c:if>
        <h2>Bằng chứng và lịch sử nộp / chấm</h2>
        <c:forEach var="taskSubmission" items="${submissions}">
            <section class="task-card">
                <h3>Lần nộp #<c:out value="${taskSubmission.submissionNo}"/></h3>
                <p><c:out value="${taskSubmission.submittedDisplay}"/> <c:if test="${taskSubmission.late}"><strong> · Nộp
                    muộn</strong></c:if></p>
                <p>Phiên lắp ráp #<c:out value="${taskSubmission.sessionId}"/> · <c:out value="${taskSubmission.robotName}"/> · hoàn thành
                <c:out value="${taskSubmission.assemblyCompletedDisplay}"/></p>
                <p>Quiz lượt #<c:out value="${taskSubmission.quizAttemptId}"/>: <c:out value="${taskSubmission.quizScore}"/>/<c:out
            value="${taskSubmission.quizTotal}"/> · <c:out value="${taskSubmission.quizSubmittedDisplay}"/> <c:if
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
                    <p>Đóng góp chẩn đoán: <c:out value="${taskSubmission.diagnosisPointsDisplay}"/>/<c:out
                value="${taskSubmission.rubric.diagnosisWeight}"/></p>
                </c:if>
                <p>Lắp ráp: <c:out value="${taskSubmission.assemblyPointsDisplay}"/>/<c:out
            value="${taskSubmission.rubric.assemblyWeight}"/> · Quiz: <c:out
            value="${taskSubmission.quizPointsDisplay}"/>/<c:out value="${taskSubmission.rubric.quizWeight}"/></p>
                <p><strong>Điểm tự động: <c:out value="${taskSubmission.automaticDisplay}"/>/<c:out
            value="${taskSubmission.rubric.automaticMaximum}"/></strong><c:if
                test="${taskSubmission.waiting}"> — chờ đánh giá phần giải thích</c:if></p>
                <h4>Bạn gặp vấn đề gì?</h4>
                <p class="task-text"><c:out value="${taskSubmission.problem}"/></p>
                <h4>Vì sao chọn cách kiểm tra/xử lý đó?</h4>
                <p class="task-text"><c:out value="${taskSubmission.reasoning}"/></p>
                <h4>Nếu làm lại, bạn sẽ thay đổi điều gì?</h4>
                <p class="task-text"><c:out value="${taskSubmission.improvement}"/></p>
                <c:if test="${adminView}">
                    <p><a href="${pageContext.request.contextPath}/admin-task-reviews?id=<c:out value="${taskSubmission.id}"/>">Chấm / xem
                    lịch sử chấm bài này</a></p>
                </c:if>
                <c:forEach var="taskReview" items="${taskSubmission.reviews}">
                    <div class="task-review">
                    <p><strong><c:out value="${taskReview.conclusionLabel}"/> · Tổng: <c:out
                value="${taskReview.totalDisplay}"/>/100</strong> · Giải thích: <c:out
                value="${taskReview.explanationPoints}"/>/20 · <c:out value="${taskReview.reviewedDisplay}"/></p>
                    <p>Người chấm: <c:out value="${taskReview.reviewerName}"/> · <c:out value="${taskReview.explanationLabel}"/></p>
                    <p>Điểm mạnh: <c:out value="${taskReview.strengths}"/></p>
                    <p>Cần cải thiện: <c:out value="${taskReview.improvements}"/></p>
                    <p>Hướng làm lại: <c:out value="${taskReview.retryGuidance}"/></p>
                    <c:if test="${taskReview.superseded}">
                        <p>Đã được sửa lúc <c:out value="${taskReview.replacedDisplay}"/> – lý do: <c:out
                    value="${taskReview.replacementReason}"/></p>
                    </c:if>
                    </div>
                </c:forEach>
            </section>
        </c:forEach>
    </body>
</html>
