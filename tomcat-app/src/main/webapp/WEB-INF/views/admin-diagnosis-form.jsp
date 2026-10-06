<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <link rel="icon" href="${pageContext.request.contextPath}/assets/icons/cpu-chip.svg" type="image/svg+xml">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Biên soạn chẩn đoán | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-admin-diagnosis-form practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
            <h1>Tạo / sửa nháp tình huống</h1>
            <p>Bốn ô cố định cho mỗi nhóm. Ô nhãn trống được bỏ qua. Công bố cần 2–4 phép kiểm tra,
            ít nhất một phép cần thiết, 3–4 nguyên nhân và 3–4 biện pháp; mỗi nhóm lựa chọn có đúng một đáp án.</p>
            <form method="post" action="${pageContext.request.contextPath}/admin-diagnosis">
                <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                <input type="hidden" name="action" value="saveDraft">
                <input type="hidden" name="id" value="<c:out value="${diagnosisScenario.id}"/>">
                <label>Tiêu đề
                <input name="title" maxlength="150" value="<c:out value="${diagnosisScenario.title}"/>">
                </label>
                <label>Bối cảnh
                <textarea name="contextText" maxlength="1500"><c:out value="${diagnosisScenario.contextText}"/></textarea>
                </label>
                <label>Triệu chứng
                <textarea name="symptomText" maxlength="1500"><c:out value="${diagnosisScenario.symptomText}"/></textarea>
                </label>
                <label>Giải thích sau khi chốt
                <textarea name="explanationText" maxlength="2000"><c:out value="${diagnosisScenario.explanationText}"/></textarea>
                </label>
                <fieldset>
                    <legend>Robot</legend>
                    <c:forEach var="robot" items="${robots}">
                        <label class="task-choice">
                        <input type="radio" name="robotId" value="<c:out value="${robot.id}"/>" required
                        <c:if test="${robot.id eq diagnosisScenario.robotId}">checked</c:if>>
                        <c:out value="${robot.name}"/>
                        </label>
                    </c:forEach>
                </fieldset>
                <fieldset>
                    <legend>Hướng dẫn liên quan (cùng robot hoặc dùng chung)</legend>
                    <label class="task-choice">
                    <input type="radio" name="guideId" value="" <c:if test="${empty diagnosisScenario.guideId}">checked</c:if>> Không chọn
                </label>
                <c:forEach var="diagnosisGuide" items="${diagnosisGuides}">
                    <label class="task-choice">
                    <input type="radio" name="guideId" value="<c:out value="${diagnosisGuide.id}"/>"
                    <c:if test="${diagnosisGuide.id eq diagnosisScenario.guideId}">checked</c:if>>
                    <c:out value="${diagnosisGuide.symptom}"/>
                    </label>
                </c:forEach>
            </fieldset>
            <h2>Phép kiểm tra</h2>
            <div class="workspace-grid workspace-editor-grid">
                <c:forEach var="diagnosisCheck" items="${diagnosisScenario.checkSlots}">
                    <fieldset>
                        <legend>Ô <c:out value="${diagnosisCheck.displayOrder}"/></legend>
                        <label>Tên phép kiểm tra
                        <input name="checkLabel<c:out value="${diagnosisCheck.displayOrder}"/>" maxlength="200"
                        value="<c:out value="${diagnosisCheck.label}"/>">
                        </label>
                        <label>Quan sát
                        <textarea name="observation<c:out value="${diagnosisCheck.displayOrder}"/>" maxlength="600"><c:out
                        value="${diagnosisCheck.observationText}"/></textarea>
                        </label>
                        <label class="task-choice">
                        <input type="checkbox" name="required<c:out value="${diagnosisCheck.displayOrder}"/>" value="true"
                        <c:if test="${diagnosisCheck.required}">checked</c:if>> Cần thiết
                        </label>
                    </fieldset>
                </c:forEach>
            </div>
            <h2>Nguyên nhân</h2>
            <div class="workspace-grid workspace-editor-grid">
                <c:forEach var="diagnosisOption" items="${diagnosisScenario.causeSlots}">
                    <fieldset>
                        <legend>Ô <c:out value="${diagnosisOption.displayOrder}"/></legend>
                        <label>Nhãn
                        <input name="causeLabel<c:out value="${diagnosisOption.displayOrder}"/>" maxlength="200" value="<c:out
                        value="${diagnosisOption.label}"/>">
                        </label>
                        <label>Phản hồi sau khi chốt
                        <textarea name="causeFeedback<c:out value="${diagnosisOption.displayOrder}"/>" maxlength="2000"><c:out
                        value="${diagnosisOption.feedbackText}"/></textarea>
                        </label>
                        <label class="task-choice">
                        <input type="radio" name="causeCorrect" value="<c:out value="${diagnosisOption.displayOrder}"/>" <c:if
                        test="${diagnosisOption.correct}">checked</c:if>> Đáp án đúng
                    </label>
                </fieldset>
            </c:forEach>
        </div>
        <h2>Biện pháp</h2>
        <div class="workspace-grid workspace-editor-grid">
            <c:forEach var="diagnosisOption" items="${diagnosisScenario.actionSlots}">
                <fieldset>
                    <legend>Ô <c:out value="${diagnosisOption.displayOrder}"/></legend>
                    <label>Nhãn
                    <input name="actionOptionLabel<c:out value="${diagnosisOption.displayOrder}"/>" maxlength="200" value="<c:out
                    value="${diagnosisOption.label}"/>">
                    </label>
                    <label>Phản hồi sau khi chốt
                    <textarea name="actionOptionFeedback<c:out value="${diagnosisOption.displayOrder}"/>" maxlength="2000"><c:out
                    value="${diagnosisOption.feedbackText}"/></textarea>
                    </label>
                    <label class="task-choice">
                    <input type="radio" name="actionOptionCorrect" value="<c:out value="${diagnosisOption.displayOrder}"/>" <c:if
                    test="${diagnosisOption.correct}">checked</c:if>> Đáp án đúng
                </label>
            </fieldset>
        </c:forEach>
    </div>
    <button>Lưu nháp</button>
</form>
</main>
<%@ include file="workspace-footer.jspf" %>
</body>
</html>
