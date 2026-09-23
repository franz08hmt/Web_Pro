<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Danh mục linh kiện (JSP) | Robot Assembly Lab</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
</head>
<body>
<h1>Danh mục linh kiện</h1>
<p class="lead">
    Trang do <code>ComponentCatalogPageServlet</code> dựng ở server:
    tham số <code>?page=</code> được đọc từ <code>HttpServletRequest</code>,
    <code>ComponentDB.selectComponents()</code> lấy kết nối từ <code>ConnectionPool</code> và chạy
    <code>PreparedStatement</code> trên MySQL, kết quả đi qua <code>setAttribute</code>
    rồi <code>forward()</code> tới JSP.
</p>

<c:if test="${not empty errorMessage}">
    <p class="notice notice-error">${errorMessage}</p>
</c:if>

<c:if test="${not empty components}">
    <p class="notice">Tổng số bản ghi trong bảng <code>components</code>: <strong>${total}</strong> · đang xem trang ${page}</p>

    <table>
        <caption>Dữ liệu đọc trực tiếp từ MySQL qua JDBC</caption>
        <thead>
        <tr>
            <th scope="col">Ảnh</th>
            <th scope="col">ID</th>
            <th scope="col">Tên linh kiện</th>
            <th scope="col">Danh mục</th>
            <th scope="col">Mô tả</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="component" items="${components}">
            <tr>
                <td class="thumb">
                    <img src="${pageContext.request.contextPath}${component.image}"
                         alt="${component.name}" width="64" height="64" loading="lazy">
                </td>
                <td><code>${component.id}</code></td>
                <td><strong>${component.name}</strong></td>
                <td>${component.category}</td>
                <td>${component.description}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <p class="paging">
        <c:if test="${page > 1}">
            <a href="${pageContext.request.contextPath}/components?page=${page - 1}">&larr; Trang trước</a>
        </c:if>
        <c:if test="${hasNextPage}">
            <a href="${pageContext.request.contextPath}/components?page=${page + 1}">Trang sau &rarr;</a>
        </c:if>
    </p>
</c:if>

<c:if test="${empty components and empty errorMessage}">
    <p class="notice">Bảng <code>components</code> chưa có dữ liệu. Hãy chạy <code>database/seed.sql</code>.</p>
</c:if>

<p class="links">
    <a href="${pageContext.request.contextPath}/robots">Danh mục robot (JSP)</a> ·
    <a href="${pageContext.request.contextPath}/architecture">Sơ đồ kiến trúc</a> ·
    <a href="${pageContext.request.contextPath}/index.html">Về trang chủ</a>
</p>
</body>
</html>
