<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Phòng thực hành nối dây | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring.css?v=20261006.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.1">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-wiring-list wiring">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <nav aria-label="Điều hướng">
                <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Tài khoản</a>
                <a href="${pageContext.request.contextPath}/wiring">Thực hành nối dây</a>
            </nav>
            <h1>Phòng thực hành nối dây</h1>
            <p>Luyện tập có tài liệu: server chấm các cặp trực tiếp của bài mẫu, không mô phỏng điện hay xác nhận robot thật.</p>
            <h2>Bài đang công bố</h2>
            <div class="workspace-grid">
                <c:forEach var="wiringExercise" items="${wiringExercises}">
                    <section class="workspace-panel workspace-course">
                        <h3><c:out value="${wiringExercise.title}"/></h3>
                        <p><c:out value="${wiringExercise.robotName}"/></p>
                        <p><c:out value="${wiringExercise.objective}"/></p>
                        <p class="workspace-scope"><c:out value="${wiringExercise.scopeText}"/></p>
                        <form method="post" action="${pageContext.request.contextPath}/wiring">
                            <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                            <input type="hidden" name="action" value="start">
                            <input type="hidden" name="exerciseId" value="<c:out value="${wiringExercise.id}"/>">
                            <button>Bắt đầu lượt mới</button>
                        </form>
                    </section>
                </c:forEach>
            </div>
            <h2>Lượt đã lưu và lịch sử của tôi</h2>
            <c:if test="${empty wiringAttempts}">
                <p>Chưa có lượt thực hành.</p>
            </c:if>
            <c:forEach var="wiringAttempt" items="${wiringAttempts}">
                <section class="workspace-panel">
                    <h3><c:out value="${wiringAttempt.exercise.title}"/> · lượt #<c:out value="${wiringAttempt.id}"/></h3>
                    <p><c:out value="${wiringAttempt.stateLabel}"/> · <c:out value="${wiringAttempt.updatedAtDisplay}"/></p>
                    <c:if test="${wiringAttempt.submitted}">
                        <p><c:out value="${wiringAttempt.grade.scoreDisplay}"/>/100 —
                        <c:out value="${wiringAttempt.grade.resultLabel}"/></p>
                    </c:if>
                    <a href="?action=play&amp;id=<c:out value="${wiringAttempt.id}"/>">Tiếp tục / xem kết quả</a>
                </section>
            </c:forEach>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
