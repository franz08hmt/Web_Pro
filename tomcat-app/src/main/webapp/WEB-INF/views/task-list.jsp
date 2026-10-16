<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <link rel="icon" href="${pageContext.request.contextPath}/assets/icons/cpu-chip.svg" type="image/svg+xml">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Nhiệm vụ của tôi | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.5">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-task-list practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
            <h1>Nhiệm vụ của tôi</h1>
            <p>Chỉ hiển thị nhiệm vụ được giao cho tài khoản hiện tại.</p>
            <c:if test="${empty tasks}">
                <p>Chưa có nhiệm vụ.</p>
            </c:if>
            <div class="workspace-grid">
                <c:forEach var="task" items="${tasks}">
                    <section class="task-card">
                        <h2><a href="?action=view&amp;id=<c:out value="${task.id}"/>"><c:out value="${task.title}"/></a></h2>
                        <p><c:out value="${task.robotName}"/> · Trạng thái nhiệm vụ:
                        <c:set var="workspaceStatusTone" value="neutral"/>
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
                        <c:url var="taskPersonalStatus" value="/tasks">
                            <c:param name="action" value="view"/><c:param name="id" value="${task.id}"/>
                        </c:url>
                        <a href="<c:out value="${taskPersonalStatus}"/>">Mở nhiệm vụ và xem trạng thái của em</a>
                    </section>
                </c:forEach>
            </div>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
