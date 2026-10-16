<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="robots" content="noindex, nofollow">
    <title>Bảng điều khiển | Robot Assembly Lab</title>
    <c:url var="dashboardCss" value="/assets/css/learning-workspace.css"><c:param name="v" value="20261006.5"/></c:url>
    <link rel="stylesheet" href="<c:out value="${dashboardCss}"/>">
    <c:url var="dashboardLayoutCss" value="/assets/css/dashboard.css"><c:param name="v" value="20261016.1"/></c:url>
    <link rel="stylesheet" href="<c:out value="${dashboardLayoutCss}"/>">
</head>
<body class="learning-workspace workspace-page-dashboard">
    <%@ include file="workspace-header.jspf" %>
    <main id="workspace-main" class="workspace-content" tabindex="-1">
        <h1>Bảng điều khiển</h1>
        <p>Chào <c:out value="${user.fullName}"/>. Chọn một hành động để tiếp tục hành trình học của bạn.</p>
        <h2>Tiếp tục học và thực hành</h2>
        <div class="dashboard-grid">
            <c:forEach var="dashboardCard" items="${dashboard.learningCards}">
                <%@ include file="dashboard-card.jspf" %>
            </c:forEach>
        </div>
        <section class="dashboard-resources" aria-label="Vật tư và đơn hàng">
            <h2>Vật tư và đơn hàng</h2>
            <div class="dashboard-grid">
                <c:forEach var="dashboardCard" items="${dashboard.resourceCards}">
                    <%@ include file="dashboard-card.jspf" %>
                </c:forEach>
            </div>
        </section>
        <c:url var="accountSettings" value="/pages/tai-khoan.html"/>
        <p><a href="<c:out value="${accountSettings}"/>">Thông tin tài khoản, đổi mật khẩu và lịch sử lắp ráp</a></p>
    </main>
    <%@ include file="workspace-footer.jspf" %>
</body>
</html>
