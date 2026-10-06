<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Thống kê quản trị | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-admin-stats">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <h1>Thống kê quản trị</h1>


            <h2>Phiên lắp ráp theo trạng thái</h2>
            <div class="workspace-table" role="region" aria-label="Bảng dữ liệu có thể cuộn ngang" tabindex="0">
                <table>
                    <caption>Tổng <c:out value="${totalSessions}"/> phiên trên toàn hệ thống</caption>
                    <thead><tr><th scope="col">Trạng thái</th><th scope="col">Số phiên</th></tr></thead>
                    <tbody>
                        <tr><td>Đang chuẩn bị (PREPARING)</td><td><c:out value="${sessionCounts.PREPARING}"/></td></tr>
                        <tr><td>Sẵn sàng (READY)</td><td><c:out value="${sessionCounts.READY}"/></td></tr>
                        <tr><td>Đang lắp ráp (IN_PROGRESS)</td><td><c:out value="${sessionCounts.IN_PROGRESS}"/></td></tr>
                        <tr><td>Đã hoàn thành (COMPLETED)</td><td><c:out value="${sessionCounts.COMPLETED}"/></td></tr>
                        <tr><td>Đã dừng (ABANDONED)</td><td><c:out value="${sessionCounts.ABANDONED}"/></td></tr>
                    </tbody>
                </table>
            </div>

            <div class="workspace-metrics">
                <dl class="workspace-metric">
                <dt>Tổng số người học (role USER)</dt>
                <dd><c:out value="${totalLearners}"/></dd>
                </dl>
                <dl class="workspace-metric">
                <dt>Số người có hoạt động</dt>
                <dd><c:out value="${activeLearners}"/> (đã có ít nhất một phiên lắp ráp hoặc một lượt làm bài kiểm tra)</dd>
                </dl>
            </div>

            <h2>Mẫu robot được chọn nhiều nhất</h2>
            <c:choose>
                <c:when test="${empty popularRobots}">
                    <p class="notice">Chưa có phiên lắp ráp nào trong hệ thống.</p>
                </c:when>
                <c:otherwise>
                    <div class="workspace-table" role="region" aria-label="Bảng dữ liệu có thể cuộn ngang" tabindex="0">
                        <table>
                            <caption>Xếp hạng theo số phiên lắp ráp đã tạo</caption>
                            <thead><tr><th scope="col">Mẫu robot</th><th scope="col">Số phiên</th></tr></thead>
                            <tbody>
                                <c:forEach var="popular" items="${popularRobots}">
                                    <tr><td><c:out value="${popular.robotName}"/></td><td><c:out value="${popular.sessionCount}"/></td></tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>

            <h2>Kết quả kiểm tra tổng hợp</h2>
            <c:choose>
                <c:when test="${empty quizAggregates}">
                    <p class="notice">Chưa có lượt làm bài kiểm tra nào trong hệ thống.</p>
                </c:when>
                <c:otherwise>
                    <div class="workspace-table" role="region" aria-label="Bảng dữ liệu có thể cuộn ngang" tabindex="0">
                        <table>
                            <caption>Điểm trung bình tính theo phần trăm số câu đúng trên mỗi lượt làm bài</caption>
                            <thead><tr><th scope="col">Mẫu robot</th><th scope="col">Số lượt làm bài</th><th scope="col">Điểm trung
                                    bình</th></tr></thead>
                            <tbody>
                                <c:forEach var="aggregate" items="${quizAggregates}">
                                    <tr>
                                        <td><c:out value="${aggregate.robotName}"/></td>
                                        <td><c:out value="${aggregate.attemptCount}"/></td>
                                        <td><c:out value="${aggregate.averageScorePercent}"/>%</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>

            <h2>Câu hỏi có tỷ lệ trả lời sai cao</h2>
            <c:choose>
                <c:when test="${empty highMissQuestions}">
                    <p class="notice">
                    Chưa đủ dữ liệu (cần ít nhất <c:out value="${minQuestionAttempts}"/> lượt trả lời cho một câu hỏi
                    mới đưa vào thống kê, để tránh một lượt làm sai duy nhất bị tính thành "sai 100%").
                    </p>
                </c:when>
                <c:otherwise>
                    <div class="workspace-table" role="region" aria-label="Bảng dữ liệu có thể cuộn ngang" tabindex="0">
                        <table>
                            <caption>Chỉ tính câu hỏi đã có từ <c:out value="${minQuestionAttempts}"/> lượt trả lời trở lên</caption>
                            <thead><tr><th scope="col">Câu hỏi</th><th scope="col">Số lượt trả lời</th><th scope="col">Tỷ lệ
                                    sai</th></tr></thead>
                            <tbody>
                                <c:forEach var="question" items="${highMissQuestions}">
                                    <tr>
                                        <td><c:out value="${question.prompt}"/></td>
                                        <td><c:out value="${question.attemptCount}"/></td>
                                        <td><c:out value="${question.missRatePercent}"/>%</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>

            <p class="links">
            <a href="${pageContext.request.contextPath}/pages/admin-content.html">Về quản trị nội dung</a> ·
            <a href="${pageContext.request.contextPath}/index.html">Về trang chủ</a>
            </p>
            <details class="workspace-technical no-print">
                <summary>Ghi chú kỹ thuật</summary>
                <p class="lead">
                Trang do <code>AdminStatsServlet</code> dựng ở server (chỉ role <code>ADMIN</code>
                xem được, tài khoản thường bị từ chối với mã 403): mọi số liệu đọc trực tiếp từ
                MySQL qua <code>StatsDB</code> bằng <code>GROUP BY</code>/<code>COUNT DISTINCT</code>
                ngay trong câu SQL — không cộng dồn ở tầng Java — để tránh đếm trùng khi có JOIN.
                Không có số liệu nào ở đây được bịa ra để biểu đồ đẹp hơn.
                </p>
            </details>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
