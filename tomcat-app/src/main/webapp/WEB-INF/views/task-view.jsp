<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <link rel="icon" href="${pageContext.request.contextPath}/assets/icons/cpu-chip.svg" type="image/svg+xml">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Chi tiết nhiệm vụ | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-task-view practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
            <h1><c:out value="${task.title}"/></h1>
            <section class="workspace-panel" aria-label="Yêu cầu và tiêu chí nhiệm vụ">
                <p class="task-text"><c:out value="${task.description}"/></p>
                <p>Mẫu: <c:out value="${task.robotName}"/> · <c:out value="${task.stateLabel}"/></p>
                <p>Hạn nộp: <c:out value="${task.dueDisplay}"/> · <c:out value="${task.latePolicyLabel}"/></p>
                <p>Tối đa <c:out value="${task.maxSubmissions}"/> lần nộp · Ngưỡng đạt <c:out value="${task.passThreshold}"/>/100.</p>
                <p><c:out value="${task.rubric.label}"/>. Điểm tự động làm tròn HALF_UP đến 1 chữ số thập phân; tổng điểm dùng điểm tự
                động đã lưu.</p>
            </section>
            <c:choose>
                <c:when test="${adminView}">
                    <c:if test="${adminPreview}">
                        <p class="notice">Xem trước nội dung cho người học. ADMIN không được nộp bài.</p>
                    </c:if>
                    <c:if test="${not adminPreview}">
                        <nav>
                            <a href="?action=preview&amp;id=<c:out value="${task.id}"/>">Xem trước như User</a>
                            ·
                            <a href="?">Danh sách nhiệm vụ</a>
                        </nav>
                        <c:if test="${task.draft}">
                            <p><a href="?action=edit&amp;id=<c:out value="${task.id}"/>">Sửa nháp và người được giao</a></p>
                            <form method="post" action="${pageContext.request.contextPath}/admin-tasks">
                                <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                                <input type="hidden" name="id" value="<c:out value="${task.id}"/>">
                                <input type="hidden" name="action" value="publish">
                                <button type="submit">Công bố</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/admin-tasks">
                                <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                                <input type="hidden" name="id" value="<c:out value="${task.id}"/>">
                                <input type="hidden" name="action" value="deleteDraft">
                                <button type="submit">Xoá nháp</button>
                            </form>
                        </c:if>
                        <c:if test="${task.open}">
                            <c:set var="learnerAction" value="view"/>
                            <form method="get" action="${pageContext.request.contextPath}/admin-tasks">
                                <input type="hidden" name="id" value="<c:out value="${task.id}"/>">
                                <input type="hidden" name="action" value="${learnerAction}">
                                <label>Tìm User theo tên <input name="q" value="<c:out value="${searchQuery}"/>" maxlength="100"></label>
                                <label>Trang danh sách User <input type="number" name="page" min="1" max="10000" value="<c:out
                                value="${learnerPage}"/>"></label>
                                <button>Tìm</button>
                            </form>
                            <p>Mỗi trang tối đa 20 User. Người đã được giao vẫn được giữ trong nháp; bỏ chọn để gỡ.</p>
                            <form method="post" action="${pageContext.request.contextPath}/admin-tasks">
                                <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                                <input type="hidden" name="id" value="<c:out value="${task.id}"/>">
                                <input type="hidden" name="action" value="addRecipients">
                                <fieldset>
                                    <legend>Thêm người được giao</legend>
                                    <c:forEach var="learner" items="${learners}">
                                        <label class="task-choice"><input type="checkbox" name="recipientId" value="<c:out
                                        value="${learner.id}"/>"> <c:out value="${learner.fullName}"/></label>
                                    </c:forEach>
                                </fieldset>
                                <button type="submit">Thêm User</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/admin-tasks">
                                <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                                <input type="hidden" name="id" value="<c:out value="${task.id}"/>">
                                <input type="hidden" name="action" value="extend">
                                <label>Hạn mới (giờ Việt Nam) <input type="datetime-local" name="dueAt" required></label>
                                <button type="submit">Gia hạn</button>
                            </form>
                            <form method="post" action="${pageContext.request.contextPath}/admin-tasks">
                                <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                                <input type="hidden" name="id" value="<c:out value="${task.id}"/>">
                                <input type="hidden" name="action" value="close">
                                <button type="submit">Đóng nhiệm vụ</button>
                            </form>
                        </c:if>
                        <c:if test="${task.closed}">
                            <form method="post" action="${pageContext.request.contextPath}/admin-tasks">
                                <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                                <input type="hidden" name="id" value="<c:out value="${task.id}"/>">
                                <input type="hidden" name="action" value="archive">
                                <button type="submit">Lưu trữ</button>
                            </form>
                        </c:if>
                        <form method="post" action="${pageContext.request.contextPath}/admin-tasks">
                            <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                            <input type="hidden" name="id" value="<c:out value="${task.id}"/>">
                            <input type="hidden" name="action" value="duplicate">
                            <button type="submit">Nhân bản thành nháp mới</button>
                        </form>
                        <div class="workspace-table" role="region" aria-label="Bảng dữ liệu có thể cuộn ngang" tabindex="0">
                            <table>
                                <caption>Người được giao và kết quả mới nhất</caption>
                                <thead>
                                    <tr>
                                        <th scope="col">Họ tên</th>
                                        <th scope="col">Trạng thái</th>
                                        <th scope="col">Điểm</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="recipient" items="${recipients}">
                                        <tr>
                                            <td><c:out value="${recipient.fullName}"/></td>
                                            <td><c:out value="${recipient.statusLabel}"/><c:if
                                            test="${recipient.latestSubmission.late}"> · Nộp
                                            muộn</c:if></td>
                                        <td><c:if test="${not empty recipient.latestSubmission}"><a
                                            href="${pageContext.request.contextPath}/admin-task-reviews?id=<c:out
                                            value="${recipient.latestSubmission.id}"/>"><c:choose><c:when
                                                test="${recipient.latestSubmission.reviewed}"><c:out
                                                value="${recipient.latestSubmission.currentReview.totalDisplay}"/>/100</c:when>
                                            <c:otherwise><c:out value="${recipient.latestSubmission.automaticDisplay}"/>/<c:out
                                            value="${task.rubric.automaticMaximum}"/> — chờ đánh giá phần giải
                                                thích</c:otherwise></c:choose></a></c:if></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>
        </c:when>
        <c:otherwise>
            <p><strong><c:out value="${recipient.statusLabel}"/></strong></p>
            <p>Vòng <c:out value="${recipient.currentRound.roundNo}"/> · bắt đầu <c:out
            value="${recipient.currentRound.startedDisplay}"/></p>
            <c:if test="${recipient.showCurrentRoundQuiz and not empty taskQuiz}">
                <p>Quiz lượt #<c:out value="${taskQuiz.quizAttemptId}"/> – <c:out value="${taskQuiz.quizScore}"/>/<c:out
                value="${taskQuiz.quizTotal}"/> – <c:out value="${taskQuiz.quizSubmittedDisplay}"/> <c:choose><c:when
                    test="${taskQuiz.reusedQuiz}">(dùng lại từ lần nộp #<c:out value="${taskQuiz.quizReusedFrom}"/>)</c:when>
                <c:otherwise>(lượt của vòng này)</c:otherwise></c:choose></p>
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
        <c:if test="${recipient.canTakeQuiz}">
            <p><a href="?action=quiz&amp;id=<c:out value="${task.id}"/>">Làm quiz cho nhiệm vụ này</a></p>
        </c:if>
        <c:if test="${recipient.canSubmit}">
            <p><a href="?action=submit&amp;id=<c:out value="${task.id}"/>">Chọn bằng chứng và nộp bài</a></p>
        </c:if>
        <p><a href="${pageContext.request.contextPath}/pages/kiem-tra.html?model=<c:out value="${task.robotId}"/>">Quiz luyện tập
        (không thay lượt tính điểm)</a></p>
    </c:otherwise>
</c:choose>
<c:if test="${not adminPreview}">
    <h2>Lịch sử các vòng nộp và chấm</h2>
    <c:if test="${empty submissions}">
        <p>Chưa có bài nộp.</p>
    </c:if>
    <c:forEach var="taskSubmission" items="${submissions}">
        <section class="task-card">
            <h3>Lần nộp #<c:out value="${taskSubmission.submissionNo}"/></h3>
            <p><c:out value="${taskSubmission.submittedDisplay}"/> <c:if test="${taskSubmission.late}"><strong> · Nộp
                muộn</strong></c:if></p>
            <p>Phiên lắp ráp #<c:out value="${taskSubmission.sessionId}"/> · <c:out value="${taskSubmission.robotName}"/> · hoàn
            thành <c:out value="${taskSubmission.assemblyCompletedDisplay}"/></p>
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
        <p>Lắp ráp: <c:out value="${taskSubmission.assemblyPointsDisplay}"/>/<c:out value="${taskSubmission.rubric.assemblyWeight}"/> ·
        Quiz: <c:out value="${taskSubmission.quizPointsDisplay}"/>/<c:out value="${taskSubmission.rubric.quizWeight}"/></p>
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
        <p><a href="${pageContext.request.contextPath}/admin-task-reviews?id=<c:out value="${taskSubmission.id}"/>">Chấm /
        xem lịch sử chấm bài này</a></p>
    </c:if>
    <c:forEach var="taskReview" items="${taskSubmission.reviews}">
        <div class="task-review">
            <p><strong><c:out value="${taskReview.conclusionLabel}"/> · Tổng: <c:out value="${taskReview.totalDisplay}"/>/100</strong> ·
            Giải thích: <c:out value="${taskReview.explanationPoints}"/>/20 ·
            <c:out value="${taskReview.reviewedDisplay}"/></p>
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
</c:if>
</main>
<%@ include file="workspace-footer.jspf" %>
</body>
</html>
