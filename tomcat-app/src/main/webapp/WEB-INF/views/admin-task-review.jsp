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
        <p>Người nộp: <c:out value="${reviewRecipient.fullName}"/> · Đang chấm lần nộp #<c:out value="${taskSubmission.submissionNo}"/></p>
        <p>Không điều chỉnh điểm tự động. Mỗi lần chấm lưu một bản ghi mới; sửa chấm phải có lý do và vòng sau chưa có hoạt động.</p>
        <p><a href="${pageContext.request.contextPath}/admin-tasks?action=view&amp;id=<c:out value="${task.id}"/>">Tổng quan nhiệm
            vụ</a></p>
        <form method="post" action="${pageContext.request.contextPath}/admin-task-reviews">
            <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
            <input type="hidden" name="id" value="<c:out value="${taskSubmission.id}"/>">
            <input type="hidden" name="action" value="review">
            <input type="hidden" name="expectedReviewId" value="<c:choose><c:when test="${empty
                currentReview}">0</c:when><c:otherwise><c:out value="${currentReview.id}"/></c:otherwise></c:choose>">
            <label>Mức giải thích <select name="explanationLevel"><option value="0" <c:if
                test="${not empty currentReview and currentReview.explanationLevel == 0}">selected</c:if>>0 điểm — Không có nội dung hoặc
                không liên quan</option><option value="1" <c:if
                test="${not empty currentReview and currentReview.explanationLevel == 1}">selected</c:if>>5 điểm — Kể lại thao tác nhưng
                chưa giải thích</option><option value="2" <c:if
                test="${not empty currentReview and currentReview.explanationLevel == 2}">selected</c:if>>10 điểm — Giải thích đúng một
                phần, còn thiếu căn cứ</option><option value="3" <c:if
                test="${not empty currentReview and currentReview.explanationLevel == 3}">selected</c:if>>15 điểm — Lập luận đúng, liên hệ
                với kết quả quan sát</option><option value="4" <c:if
                test="${not empty currentReview and currentReview.explanationLevel == 4}">selected</c:if>>20 điểm — Lập luận rõ, có căn cứ
                và đề xuất cải thiện hợp lý</option></select></label>
            <label>Kết luận <select name="conclusion"><option value="PASSED" <c:if test="${currentReview.passed}">selected</c:if>>Đạt yêu
                cầu</option><option value="NEEDS_REVISION" <c:if test="${currentReview.needsRevision}">selected</c:if>>Cần bổ
                sung</option><option value="NOT_PASSED" <c:if test="${currentReview.notPassed}">selected</c:if>>Chưa đạt yêu
                cầu</option></select></label>
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
                <p>Lắp ráp: 40,0/40 · Quiz: <c:out value="${taskSubmission.quizPointsDisplay}"/>/40</p>
                <p><strong>Điểm tự động: <c:out value="${taskSubmission.automaticDisplay}"/>/80</strong><c:if
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
