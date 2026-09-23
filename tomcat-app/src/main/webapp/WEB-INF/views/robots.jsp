<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Danh mục robot (JSP) | Robot Assembly Lab</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
</head>
<body>
<h1>Danh mục mô hình robot</h1>
<p class="lead">
    Trang do <code>RobotCatalogPageServlet</code> dựng ở server:
    <code>doGet()</code> gọi <code>RobotDB.selectRobots()</code> &rarr; <code>ConnectionPool</code> &rarr; MySQL,
    đặt danh sách vào request bằng <code>setAttribute("robots", ...)</code>
    rồi <code>forward()</code> sang JSP này. JSP chỉ hiển thị, không mở kết nối database.
</p>

<c:if test="${not empty errorMessage}">
    <p class="notice notice-error">${errorMessage}</p>
</c:if>

<c:if test="${not empty robots}">
    <p class="notice">Tổng số bản ghi trong bảng <code>robots</code>: <strong>${total}</strong> · đang xem trang ${page}</p>

    <table>
        <caption>Dữ liệu đọc trực tiếp từ MySQL qua JDBC</caption>
        <thead>
        <tr>
            <th scope="col">ID</th>
            <th scope="col">Tên mô hình</th>
            <th scope="col">Độ khó</th>
            <th scope="col">Cảm biến chính</th>
            <th scope="col">Thời gian lắp</th>
            <th scope="col">Bước lắp ráp</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="robot" items="${robots}">
            <tr>
                <td><code>${robot.id}</code></td>
                <td>
                    <strong>${robot.name}</strong>
                    <span class="summary">${robot.summary}</span>
                </td>
                <td>${robot.level}</td>
                <td>${robot.mainSensor}</td>
                <td>${robot.buildTime}</td>
                <td><a href="${pageContext.request.contextPath}/api/robots/${robot.id}/steps">Xem JSON</a></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <p class="paging">
        <c:if test="${page > 1}">
            <a href="${pageContext.request.contextPath}/robots?page=${page - 1}">&larr; Trang trước</a>
        </c:if>
        <c:if test="${hasNextPage}">
            <a href="${pageContext.request.contextPath}/robots?page=${page + 1}">Trang sau &rarr;</a>
        </c:if>
    </p>
</c:if>

<c:if test="${empty robots and empty errorMessage}">
    <p class="notice">Bảng <code>robots</code> chưa có dữ liệu. Hãy chạy <code>database/seed.sql</code>.</p>
</c:if>

<p class="links">
    <a href="${pageContext.request.contextPath}/components">Danh mục linh kiện (JSP)</a> ·
    <a href="${pageContext.request.contextPath}/architecture">Sơ đồ kiến trúc</a> ·
    <a href="${pageContext.request.contextPath}/index.html">Về trang chủ</a>
</p>
</body>
</html>
