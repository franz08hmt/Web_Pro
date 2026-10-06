<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <link rel="icon" href="${pageContext.request.contextPath}/assets/icons/cpu-chip.svg" type="image/svg+xml">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Xem trước chẩn đoán | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.5">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-admin-diagnosis-preview practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
            <h1><c:out value="${diagnosisScenario.title}"/></h1>
            <p><c:out value="${diagnosisScenario.robotName}"/> · <c:set var="workspaceStatusTone" value="neutral"/>
            <c:choose>
                <c:when test="${diagnosisScenario.published}">
                    <c:set var="workspaceStatusTone" value="info"/>
                </c:when>
                <c:when test="${diagnosisScenario.draft}">
                    <c:set var="workspaceStatusTone" value="warning"/>
                </c:when>
            </c:choose>
            <span class="status-badge status-badge--<c:out value="${workspaceStatusTone}"/>">
                <c:out value="${diagnosisScenario.stateLabel}"/>
            </span>
            · Phiên bản <c:out value="${diagnosisScenario.versionNo}"/></p>
            <p class="task-text"><c:out value="${diagnosisScenario.contextText}"/></p>
            <p class="task-text"><c:out value="${diagnosisScenario.symptomText}"/></p>
            <c:if test="${diagnosisPreview}">
                <p>Xem trước như người học, chỉ đọc. Không tạo lượt hoặc ghi phép kiểm tra.</p>
            </c:if>
            <h2>Phép kiểm tra</h2>
            <c:forEach var="diagnosisCheck" items="${diagnosisScenario.checks}">
                <h3><c:out value="${diagnosisCheck.label}"/></h3>
                <c:if test="${not diagnosisPreview}">
                    <p class="task-text"><c:out value="${diagnosisCheck.observationText}"/></p>
                    <p><c:choose><c:when test="${diagnosisCheck.required}">
                        <span class="status-badge status-badge--info">Cần thiết</span></c:when>
                        <c:otherwise><span class="status-badge">Không liên quan</span></c:otherwise></c:choose></p>
                </c:if>
            </c:forEach>
            <h2>Nguyên nhân / biện pháp</h2>
            <c:forEach var="diagnosisOption" items="${diagnosisScenario.options}">
                <p><c:out value="${diagnosisOption.label}"/></p>
                <c:if test="${not diagnosisPreview}">
                    <p><c:choose><c:when test="${diagnosisOption.correct}">
                        <span class="status-badge status-badge--success">Đáp án đúng</span></c:when>
                        <c:otherwise><span class="status-badge status-badge--danger">Đáp án sai</span></c:otherwise></c:choose></p>
                    <p class="task-text"><c:out value="${diagnosisOption.feedbackText}"/></p>
                </c:if>
            </c:forEach>
            <p>Quan sát là dữ liệu mô phỏng của bài tập, không phải phép đo từ robot thật.</p>
            <c:if test="${not diagnosisPreview}">
                <h2>Giải thích sau khi chốt</h2>
                <p class="task-text"><c:out value="${diagnosisScenario.explanationText}"/></p>
                <p><a href="?action=preview&amp;id=<c:out value="${diagnosisScenario.id}"/>">Xem trước như người học</a></p>
                <c:if test="${diagnosisScenario.draft}">
                    <p><a href="?action=edit&amp;id=<c:out value="${diagnosisScenario.id}"/>">Sửa nháp</a></p>
                    <form method="post" action="${pageContext.request.contextPath}/admin-diagnosis">
                        <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                        <input type="hidden" name="action" value="publish">
                        <input type="hidden" name="id" value="<c:out value="${diagnosisScenario.id}"/>">
                        <button>Công bố</button>
                    </form>
                </c:if>
                <c:if test="${diagnosisScenario.published}">
                    <form method="post" action="${pageContext.request.contextPath}/admin-diagnosis">
                        <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                        <input type="hidden" name="action" value="archive">
                        <input type="hidden" name="id" value="<c:out value="${diagnosisScenario.id}"/>">
                        <button>Lưu trữ</button>
                    <p class="summary">Lưu trữ sẽ ẩn nội dung khỏi danh sách mặc định và ngăn bắt đầu lượt mới; lịch sử vẫn được giữ.</p>
</form>
                </c:if>
                <form method="post" action="${pageContext.request.contextPath}/admin-diagnosis">
                    <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                    <input type="hidden" name="action" value="duplicate">
                    <input type="hidden" name="id" value="<c:out value="${diagnosisScenario.id}"/>">
                    <button>Nhân bản thành nháp mới</button>
                </form>
            </c:if>
            <p><a href="?">Danh sách tình huống</a></p>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
