<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Lịch sử đơn mô phỏng | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.1">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-order-history">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/cua-hang.html">← Quay lại cửa hàng</a></p>
            <h1>Lịch sử đơn mô phỏng</h1>
            <p class="lead">
            Đây là các đơn học tập của <c:out value="${user.fullName}"/>.
            Đơn chỉ ghi nhận dữ liệu trong bài, không có thanh toán hoặc giao hàng thật.
            </p>

            <c:choose>
                <c:when test="${empty orders}">
                    <p class="notice">Bạn chưa có đơn nào. Hãy xem giá tham khảo của linh kiện trong cửa hàng.</p>
                </c:when>
                <c:otherwise>
                    <c:forEach var="order" items="${orders}">
                        <section class="notice" aria-label="Đơn hàng ${order.id}">
                            <h2>Đơn #<c:out value="${order.id}"/></h2>
                            <p>Trạng thái: <c:out value="${order.statusLabel}"/> · <c:out value="${order.createdAt}"/></p>
                            <div class="workspace-table" role="region" aria-label="Bảng dữ liệu có thể cuộn ngang" tabindex="0">
                                <table>
                                    <caption>Các mặt hàng đã được chụp thông tin tại lúc xác nhận</caption>
                                    <thead><tr><th scope="col">Linh kiện</th><th scope="col">Đơn giá</th><th scope="col">Số
                                            lượng</th><th scope="col">Thành tiền</th></tr></thead>
                                    <tbody>
                                        <c:forEach var="item" items="${order.items}">
                                            <tr>
                                                <td><c:out value="${item.productNameSnapshot}"/></td>
                                                <td><fmt:formatNumber value="${item.unitPriceVnd}" type="number" groupingUsed="true"/>
                                                đ</td>
                                                <td><c:out value="${item.quantity}"/></td>
                                                <td><fmt:formatNumber value="${item.lineTotalVnd}" type="number" groupingUsed="true"/>
                                                đ</td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                            <p><strong>Tổng mô phỏng: <fmt:formatNumber value="${order.totalVnd}" type="number" groupingUsed="true"/>
                            đ</strong></p>
                        </section>
                    </c:forEach>
                </c:otherwise>
            </c:choose>

            <nav class="paging" aria-label="Phân trang lịch sử đơn">
                <c:if test="${page > 1}"><a href="?page=${page - 1}">← Mới hơn</a></c:if>
                <span>Trang <c:out value="${page}"/> / <c:out value="${totalPages}"/></span>
                <c:if test="${page < totalPages}"><a href="?page=${page + 1}">Cũ hơn →</a></c:if>
            </nav>
            <p class="links"><a href="${pageContext.request.contextPath}/pages/gio-hang.html">Giỏ hàng</a> ·
            <a href="${pageContext.request.contextPath}/index.html">Trang chủ</a></p>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
