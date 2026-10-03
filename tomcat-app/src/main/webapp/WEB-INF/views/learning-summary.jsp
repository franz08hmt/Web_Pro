<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tổng kết học tập | Robot Assembly Lab</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
</head>
<body>
<h1>Tổng kết học tập</h1>
<p class="lead">
    Trang do <code>LearningSummaryServlet</code> dựng ở server: đọc số liệu của
    đúng tài khoản đang đăng nhập qua <code>StatsDB</code> (dùng <code>GROUP BY</code>
    và <code>COUNT DISTINCT</code> ngay trong SQL để không đếm trùng), đặt vào
    request bằng <code>setAttribute</code> rồi <code>forward()</code> sang JSP này.
    Không có tài khoản nào xem được số liệu của tài khoản khác.
</p>

<dl>
    <dt>Tài khoản</dt><dd><c:out value="${user.fullName}"/> (<c:out value="${user.email}"/>)</dd>
    <dt>Tổng số phiên lắp ráp</dt><dd><c:out value="${totalSessions}"/></dd>
    <dt>Số phiên đã hoàn thành</dt><dd><c:out value="${sessionCounts.COMPLETED}"/></dd>
    <dt>Đang chuẩn bị / sẵn sàng</dt><dd><c:out value="${sessionCounts.PREPARING + sessionCounts.READY}"/></dd>
    <dt>Đang lắp ráp</dt><dd><c:out value="${sessionCounts.IN_PROGRESS}"/></dd>
    <dt>Đã dừng</dt><dd><c:out value="${sessionCounts.ABANDONED}"/></dd>
</dl>

<h2>Mẫu robot đã hoàn tất</h2>
<c:choose>
<c:when test="${empty completedRobots}">
    <p class="notice">Bạn chưa hoàn tất mẫu robot nào. Hãy vào phòng lắp ráp 3D và hoàn tất một phiên để xem tại đây.</p>
</c:when>
<c:otherwise>
    <table>
        <caption>Mẫu robot đã từng lắp ráp xong ít nhất một lần</caption>
        <thead><tr><th scope="col">Mẫu robot</th><th scope="col">Độ khó</th><th scope="col">Mô tả</th></tr></thead>
        <tbody>
        <c:forEach var="robot" items="${completedRobots}">
            <tr>
                <td><strong><c:out value="${robot.name}"/></strong></td>
                <td><c:out value="${robot.level}"/></td>
                <td><c:out value="${robot.summary}"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</c:otherwise>
</c:choose>

<h2>Điểm kiểm tra kiến thức</h2>
<c:choose>
<c:when test="${empty quizScores}">
    <p class="notice">Bạn chưa làm bài kiểm tra nào. Vào trang lắp ráp của một mẫu robot để bắt đầu.</p>
</c:when>
<c:otherwise>
    <table>
        <caption>Điểm tốt nhất và gần nhất theo từng mẫu robot đã từng làm bài</caption>
        <thead>
        <tr>
            <th scope="col">Mẫu robot</th>
            <th scope="col">Điểm tốt nhất</th>
            <th scope="col">Điểm gần nhất</th>
            <th scope="col">Số lần làm bài</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="score" items="${quizScores}">
            <tr>
                <td><c:out value="${score.robotName}"/></td>
                <td><c:out value="${score.bestScore}"/>/<c:out value="${score.totalQuestions}"/></td>
                <td><c:out value="${score.latestScore}"/>/<c:out value="${score.totalQuestions}"/></td>
                <td><c:out value="${score.attemptCount}"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</c:otherwise>
</c:choose>

<p class="links">
    <a href="${pageContext.request.contextPath}/learning-profile">Hồ sơ học tập (in/PDF)</a> ·
    <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Xem lịch sử phiên và phiếu kết quả</a> ·
    <a href="${pageContext.request.contextPath}/pages/tra-cuu-loi.html">Tra cứu lỗi lắp ráp</a> ·
    <a href="${pageContext.request.contextPath}/index.html">Về trang chủ</a>
</p>
</body>
</html>
