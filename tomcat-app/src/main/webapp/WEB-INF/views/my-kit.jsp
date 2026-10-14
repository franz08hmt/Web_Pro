<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta name="robots" content="noindex, nofollow">
        <title>Bộ linh kiện của tôi | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.5">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/design.css?v=20261014.1">
    </head>
    <body class="learning-workspace design-workspace">
        <%@ include file="workspace-header.jspf" %>
        <main id="workspace-main" class="workspace-content" tabindex="-1">
            <h1>Bộ linh kiện của tôi</h1>
            <p>Tự thiết kế trong bộ linh kiện được hỗ trợ kiểm chứng, không phải ghép mọi linh kiện bất kỳ.</p>
            <p>Số lượng là thông tin bạn tự khai. Đơn hàng trên website là mô phỏng, không phải giao dịch mua thật.</p>
            <c:if test="${not empty designError}">
                <p role="alert"><c:out value="${designError}" /></p>
            </c:if>
            <c:if test="${param.saved eq '1'}">
                <p role="status">Đã cập nhật bộ linh kiện.</p>
            </c:if>
            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Về tài khoản</a></p>
            <form method="post" action="${pageContext.request.contextPath}/my-kit">
                <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                <input type="hidden" name="action" value="save">
                <div class="kit-grid">
                    <c:forEach var="kitItem" items="${kitItems}">
                        <fieldset>
                            <legend><c:out value="${kitItem.name}" /></legend>
                            <c:if test="${not empty kitItem.imagePath}">
                                <img src="${pageContext.request.contextPath}/assets/<c:out value='${kitItem.imagePath}'/>"
                                     alt="<c:out value='${kitItem.name}'/>" width="128" height="128" loading="lazy">
                            </c:if>
                            <p><c:out value="${kitItem.category}" /></p>
                            <p class="status-badge"><c:out value="${kitItem.supportLabel}" /></p>
                            <label>
                                Số lượng đang có (0–10)
                                <input type="hidden" name="componentId" value="<c:out value='${kitItem.componentId}'/>">
                                <input type="number" name="quantity_<c:out value='${kitItem.componentId}'/>"
                                       min="0" max="10" required value="<c:out value='${kitItem.inputValue}'/>">
                            </label>
                            <c:if test="${not empty kitItem.shopPath}">
                                <p><a href="${pageContext.request.contextPath}<c:out value='${kitItem.shopPath}'/>">
                                    Xem sản phẩm ở cửa hàng</a></p>
                            </c:if>
                        </fieldset>
                    </c:forEach>
                </div>
                <p>Nhập 0 để bỏ linh kiện khỏi bộ. Thiết kế đã tạo giữ nguyên số lượng đã chọn.</p>
                <button type="submit">Lưu bộ linh kiện</button>
            </form>
            <section>
                <h2>Nhập từ đơn hàng đã mua</h2>
                <p>Lấy tổng số lượng của các đơn đã xác nhận, tối đa 10 mỗi loại.
                    Giữ số đang có nếu lớn hơn; bấm lại không cộng trùng.</p>
                <form method="post" action="${pageContext.request.contextPath}/my-kit">
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                    <input type="hidden" name="action" value="import">
                    <button type="submit">Nhập từ đơn hàng đã mua</button>
                </form>
            </section>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
