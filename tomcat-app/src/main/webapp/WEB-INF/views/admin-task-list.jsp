<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <link rel="icon" href="${pageContext.request.contextPath}/assets/icons/cpu-chip.svg" type="image/svg+xml">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Quản lý nhiệm vụ | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.5">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-admin-task-list practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
            <h1>Quản lý nhiệm vụ</h1>
            <p><a href="?action=new">Tạo nháp mới</a></p>
            <form method="get">
                <fieldset>
                    <legend>Lọc trạng thái</legend>
                    <label class="task-choice">
                    <input type="radio" name="state" value=""
                    <c:if test="${empty taskStateFilter}">checked</c:if>>
                    Danh sách mặc định
                    </label>
                    <label class="task-choice">
                    <input type="radio" name="state" value="DRAFT"
                    <c:if test="${taskStateFilter eq 'DRAFT'}">checked</c:if>>
                    Nháp
                    </label>
                    <label class="task-choice">
                    <input type="radio" name="state" value="OPEN"
                    <c:if test="${taskStateFilter eq 'OPEN'}">checked</c:if>>
                    Đang mở
                    </label>
                    <label class="task-choice">
                    <input type="radio" name="state" value="CLOSED"
                    <c:if test="${taskStateFilter eq 'CLOSED'}">checked</c:if>>
                    Đã đóng
                    </label>
                    <label class="task-choice">
                    <input type="radio" name="state" value="ARCHIVED"
                    <c:if test="${taskStateFilter eq 'ARCHIVED'}">checked</c:if>>
                    Đã lưu trữ
                    </label>
                </fieldset>
                <button>Lọc</button>
            </form>
            <c:if test="${empty tasks}">
                <p>Chưa có nhiệm vụ.</p>
            </c:if>
            <div class="workspace-grid">
                <c:forEach var="task" items="${tasks}">
                    <section class="task-card">
                        <h2><a href="?action=view&amp;id=<c:out value="${task.id}"/>"><c:out value="${task.title}"/></a></h2>
                        <p><c:out value="${task.robotName}"/> · <c:set var="workspaceStatusTone" value="neutral"/>
                        <c:choose>
                            <c:when test="${task.open}">
                                <c:set var="workspaceStatusTone" value="info"/>
                            </c:when>
                            <c:when test="${task.draft}">
                                <c:set var="workspaceStatusTone" value="warning"/>
                            </c:when>
                        </c:choose>
                        <span class="status-badge status-badge--<c:out value="${workspaceStatusTone}"/>">
                            <c:out value="${task.stateLabel}"/>
                        </span> · Hạn nộp <c:out
                        value="${task.dueDisplay}"/></p>
                    </section>
                </c:forEach>
            </div>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
