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
            <input type="hidden" name="rubricTemplate" value="A">
            <label>Tiêu đề <input name="title" required minlength="5" maxlength="150" value="<c:out value="${task.title}"/>"></label>
            <label>Mô tả <textarea name="description" required maxlength="4000"><c:out value="${task.description}"/></textarea></label>
            <label>Mẫu robot <select name="robotId"><c:forEach var="robot" items="${robots}"><option value="<c:out value="${robot.id}"/>"
                <c:if test="${robot.id eq task.robotId}">selected</c:if>><c:out
                value="${robot.name}"/></option></c:forEach></select></label>
            <label>Hạn nộp (giờ Việt Nam) <input type="datetime-local" name="dueAt" required value="<c:out
                value="${task.dueInput}"/>"></label>
            <label>Chính sách muộn <select name="latePolicy"><option value="REJECT_LATE" <c:if
                test="${not task.acceptLate}">selected</c:if>>Không nhận bài quá hạn</option><option value="ACCEPT_LATE_FLAGGED" <c:if
                test="${task.acceptLate}">selected</c:if>>Nhận và gắn cờ nộp muộn</option></select></label>
            <label>Số lần nộp tối đa <input type="number" name="maxSubmissions" min="1" max="3" value="<c:out
                value="${task.maxSubmissions}"/>" required></label>
            <label>Ngưỡng đạt <input type="number" name="passThreshold" min="50" max="100" value="<c:out value="${task.passThreshold}"/>"
                required></label>
            <label class="task-choice"><input type="checkbox" name="allowPriorEvidence" value="true" <c:if
                test="${task.allowPriorEvidence}">checked</c:if>> Cho phép phiên lắp ráp hoàn thành trước vòng 1</label>
            <p>Mẫu A cố định: lắp ráp 40 + quiz 40 + giải thích 20 = 100.</p>
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
    </body>
</html>
