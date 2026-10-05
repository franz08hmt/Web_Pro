<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Hỗ trợ thực hành nối dây | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring.css?v=20261005.2">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring-support.css?v=20261005.1">
    </head>
    <body class="wiring wiring-support">
        <c:set var="supportRoute" value="wiring-support"/>
        <c:if test="${supportAdmin}">
            <c:set var="supportRoute" value="admin-wiring-support"/>
        </c:if>
        <nav aria-label="Điều hướng">
            <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Tài khoản</a>
            <a href="${pageContext.request.contextPath}/<c:out value="${supportRoute}"/>">Danh sách hỗ trợ</a>
        </nav>
        <h1>Hỗ trợ thực hành nối dây</h1>
        <p>Hỗ trợ trao đổi về lượt đã lưu; kết thúc hỗ trợ không có nghĩa sơ đồ đúng toàn bộ.</p>
        <c:if test="${not supportAdmin}">
            <p><a href="${pageContext.request.contextPath}/wiring">Mở lượt nối dây để gửi câu hỏi</a></p>
        </c:if>
        <c:if test="${empty supportRequests}">
            <p>Chưa có yêu cầu hỗ trợ.</p>
        </c:if>
        <c:forEach var="supportItem" items="${supportRequests}">
            <section>
                <h2>Yêu cầu #<c:out value="${supportItem.id}"/> · <c:out value="${supportItem.stateLabel}"/></h2>
                <p><c:out value="${supportItem.exerciseTitle}"/> · lượt #<c:out value="${supportItem.attemptId}"/></p>
                <c:if test="${supportAdmin}">
                    <p>Người hỏi: <c:out value="${supportItem.ownerName}"/></p>
                </c:if>
                <a href="${pageContext.request.contextPath}/${supportRoute}?action=view&amp;id=${supportItem.id}">Xem trao đổi</a>
            </section>
        </c:forEach>
    </body>
</html>
