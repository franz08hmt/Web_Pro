<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <link rel="icon" href="${pageContext.request.contextPath}/assets/icons/cpu-chip.svg" type="image/svg+xml">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Phòng thực hành nối dây | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring.css?v=20261006.2">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.5">
        <meta name="robots" content="noindex, nofollow">
        <c:url var="practiceIndexCss" value="/assets/css/practice-index.css"><c:param name="v" value="20261016.1"/></c:url>
        <link rel="stylesheet" href="<c:out value="${practiceIndexCss}"/>">
    </head>
    <body class="learning-workspace workspace-page-wiring-list wiring practice-index">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <nav aria-label="Điều hướng">
                <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Tài khoản</a>
                <a href="${pageContext.request.contextPath}/wiring">Thực hành nối dây</a>
            </nav>
            <h1>Phòng thực hành nối dây</h1>
            <p>Luyện tập có tài liệu: server chấm các cặp trực tiếp của bài mẫu, không mô phỏng điện hay xác nhận robot thật.</p>
            <nav class="practice-sections" aria-label="Các phần thực hành nối dây">
                <a href="#wiring-exercises">Bài thực hành</a>
                <a href="#wiring-history">Lịch sử của tôi</a>
            </nav>
            <section id="wiring-exercises" aria-labelledby="wiring-exercises-title">
            <h2 id="wiring-exercises-title">Bài thực hành</h2>
            <c:set var="wiringVisibleCount" value="0"/>
            <div class="workspace-grid">
                <c:forEach var="wiringExercise" items="${wiringExercises}">
                    <c:if test="${empty param.robotId or wiringExercise.robotId eq param.robotId}">
                    <c:set var="wiringVisibleCount" value="${wiringVisibleCount + 1}"/>
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
                    </c:if>
                </c:forEach>
            </div>
            <c:if test="${wiringVisibleCount eq 0}">
                <p>Chưa có bài nối dây phù hợp. Hãy chọn mẫu khác hoặc quay lại danh sách bài đang công bố.</p>
            </c:if>
            </section>
            <section id="wiring-history" class="practice-history" aria-labelledby="wiring-history-title">
            <h2 id="wiring-history-title">Lượt đã lưu và lịch sử của tôi</h2>
            <c:if test="${empty wiringAttempts}">
                <p>Chưa có lượt thực hành.</p>
            </c:if>
            <c:forEach var="wiringAttempt" items="${wiringAttempts}">
                <section class="workspace-panel">
                    <h3><c:out value="${wiringAttempt.exercise.title}"/> · lượt #<c:out value="${wiringAttempt.id}"/></h3>
                    <p><c:set var="workspaceStatusTone" value="neutral"/>
                    <c:choose>
                        <c:when test="${wiringAttempt.draft}">
                            <c:set var="workspaceStatusTone" value="warning"/>
                        </c:when>
                        <c:when test="${wiringAttempt.submitted}">
                            <c:set var="workspaceStatusTone" value="info"/>
                        </c:when>
                    </c:choose>
                    <span class="status-badge status-badge--<c:out value="${workspaceStatusTone}"/>">
                        <c:out value="${wiringAttempt.stateLabel}"/>
                    </span> · <c:out value="${wiringAttempt.updatedAtDisplay}"/></p>
                    <c:if test="${wiringAttempt.submitted}">
                        <p><c:out value="${wiringAttempt.grade.scoreDisplay}"/>/100 —
                        <c:set var="workspaceStatusTone" value="neutral"/>
                        <c:choose>
                            <c:when test="${wiringAttempt.grade.entirelyCorrect}">
                                <c:set var="workspaceStatusTone" value="success"/>
                            </c:when>
                            <c:when test="${not wiringAttempt.grade.entirelyCorrect}">
                                <c:set var="workspaceStatusTone" value="warning"/>
                            </c:when>
                        </c:choose>
                        <span class="status-badge status-badge--<c:out value="${workspaceStatusTone}"/>">
                            <c:out value="${wiringAttempt.grade.resultLabel}"/>
                        </span></p>
                    </c:if>
                    <c:url var="wiringHistoryTarget" value="/wiring">
                        <c:param name="action" value="play"/><c:param name="id" value="${wiringAttempt.id}"/>
                    </c:url>
                    <a href="<c:out value="${wiringHistoryTarget}"/>">Tiếp tục / xem kết quả</a>
                </section>
            </c:forEach>
            <nav class="history-pagination" aria-label="Trang lịch sử nối dây">
                <p>Trang <c:out value="${wiringHistory.page}"/>/<c:out value="${wiringHistory.totalPages}"/>
                    · <c:out value="${wiringHistory.totalItems}"/> lượt; mỗi trang tối đa 20 lượt.</p>
                <c:if test="${wiringHistory.hasPrevious}">
                    <c:url var="wiringPrevious" value="/wiring">
                        <c:param name="page" value="${wiringHistory.page - 1}"/>
                        <c:if test="${not empty param.robotId}"><c:param name="robotId" value="${param.robotId}"/></c:if>
                    </c:url>
                    <a href="<c:out value="${wiringPrevious}"/>#wiring-history">Trang trước</a>
                </c:if>
                <c:if test="${wiringHistory.hasNext}">
                    <c:url var="wiringNext" value="/wiring">
                        <c:param name="page" value="${wiringHistory.page + 1}"/>
                        <c:if test="${not empty param.robotId}"><c:param name="robotId" value="${param.robotId}"/></c:if>
                    </c:url>
                    <a href="<c:out value="${wiringNext}"/>#wiring-history">Trang sau</a>
                </c:if>
            </nav>
            </section>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
