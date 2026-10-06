<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Biên soạn nhiệm vụ | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-admin-task-form practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
            <h1>Tạo / sửa nháp nhiệm vụ</h1>
            <c:choose>
                <c:when test="${task.id == 0}">
                    <c:set var="learnerAction" value="new"/>
                </c:when>
                <c:otherwise>
                    <c:set var="learnerAction" value="edit"/>
                </c:otherwise>
            </c:choose>
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
                <input type="hidden" name="action" value="saveDraft">
                <fieldset>
                    <legend>Mẫu tiêu chí cố định</legend>
                    <label class="task-choice">
                    <input type="radio" name="rubricTemplate" value="A" required
                    <c:if test="${not task.rubricB}">checked</c:if>>
                    <c:out value="${task.rubric.templateALabel}"/>
                    </label>
                    <label class="task-choice">
                    <input type="radio" name="rubricTemplate" value="B" required
                    <c:if test="${task.rubricB}">checked</c:if>>
                    <c:out value="${task.rubric.templateBLabel}"/>
                    </label>
                </fieldset>
                <fieldset>
                    <legend>Tình huống chẩn đoán (bắt buộc với mẫu B, cùng robot)</legend>
                    <label class="task-choice">
                    <input type="radio" name="diagnosisScenarioId" value="0"
                    <c:if test="${task.diagnosisScenarioId == 0}">checked</c:if>>
                    Không dùng (mẫu A)
                    </label>
                    <c:forEach var="diagnosisScenario" items="${diagnosisScenarios}">
                        <label class="task-choice">
                        <input type="radio" name="diagnosisScenarioId" value="<c:out value="${diagnosisScenario.id}"/>"
                        <c:if test="${diagnosisScenario.id == task.diagnosisScenarioId}">checked</c:if>>
                        <c:out value="${diagnosisScenario.robotName}"/> · <c:out value="${diagnosisScenario.title}"/>
                        </label>
                    </c:forEach>
                </fieldset>
                <label>Tiêu đề <input name="title" required minlength="5" maxlength="150" value="<c:out value="${task.title}"/>"></label>
                <label>Mô tả <textarea name="description" required maxlength="4000"><c:out value="${task.description}"/></textarea></label>
                <fieldset>
                    <legend>Mẫu robot</legend>
                    <c:forEach var="robot" items="${robots}">
                        <label class="task-choice">
                        <input type="radio" name="robotId" value="<c:out value="${robot.id}"/>" required
                        <c:if test="${robot.id eq task.robotId}">checked</c:if>>
                        <c:out value="${robot.name}"/>
                        </label>
                    </c:forEach>
                </fieldset>
                <label>Hạn nộp (giờ Việt Nam) <input type="datetime-local" name="dueAt" required value="<c:out
                value="${task.dueInput}"/>"></label>
                <fieldset>
                    <legend>Chính sách muộn</legend>
                    <label class="task-choice">
                    <input type="radio" name="latePolicy" value="REJECT_LATE" required
                    <c:if test="${not task.acceptLate}">checked</c:if>>
                    Không nhận bài quá hạn
                    </label>
                    <label class="task-choice">
                    <input type="radio" name="latePolicy" value="ACCEPT_LATE_FLAGGED" required
                    <c:if test="${task.acceptLate}">checked</c:if>>
                    Nhận và gắn cờ nộp muộn
                    </label>
                </fieldset>
                <label>Số lần nộp tối đa <input type="number" name="maxSubmissions" min="1" max="3" value="<c:out
                value="${task.maxSubmissions}"/>" required></label>
                <label>Ngưỡng đạt <input type="number" name="passThreshold" min="50" max="100" value="<c:out
                value="${task.passThreshold}"/>"
                required></label>
                <label class="task-choice"><input type="checkbox" name="allowPriorEvidence" value="true" <c:if
                test="${task.allowPriorEvidence}">checked</c:if>> Cho phép phiên lắp ráp hoàn thành trước vòng 1</label>
            <p><c:out value="${task.rubric.label}"/>.</p>
            <fieldset>
                <legend>Chọn người được giao (chỉ USER)</legend>
                <c:forEach var="recipient" items="${recipients}">
                    <label class="task-choice"><input type="checkbox" name="recipientId" value="<c:out value="${recipient.userId}"/>"
                    checked> <c:out value="${recipient.fullName}"/></label>
                </c:forEach>
                <c:forEach var="learner" items="${learners}">
                    <label class="task-choice"><input type="checkbox" name="recipientId" value="<c:out value="${learner.id}"/>"> <c:out
                    value="${learner.fullName}"/></label>
                </c:forEach>
            </fieldset>
            <button>Lưu nháp</button>
        </form>
    </main>
    <%@ include file="workspace-footer.jspf" %>
</body>
</html>
