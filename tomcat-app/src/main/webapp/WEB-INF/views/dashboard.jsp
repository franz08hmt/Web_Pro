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
</head>
<body class="learning-workspace">
    <%@ include file="workspace-header.jspf" %>
    <main id="workspace-main" class="workspace-content" tabindex="-1">
        <h1>Bảng điều khiển</h1>
        <p>Chào <c:out value="${user.fullName}"/>. Chọn một hành động để tiếp tục hành trình học của bạn.</p>
        <div class="workspace-metrics">
            <c:forEach var="dashboardCard" items="${dashboard.cards}">
                <article class="workspace-metric">
                    <h2><c:out value="${dashboardCard.title}"/></h2>
                    <p><c:out value="${dashboardCard.summary}"/></p>
                    <c:url var="dashboardTarget" value="${dashboardCard.path}">
                        <c:if test="${not empty dashboardCard.parameterName}">
                            <c:param name="${dashboardCard.parameterName}" value="${dashboardCard.parameterValue}"/>
                        </c:if>
                        <c:if test="${not empty dashboardCard.model}"><c:param name="model" value="${dashboardCard.model}"/></c:if>
                    </c:url>
                    <a href="<c:out value="${dashboardTarget}"/>"><c:out value="${dashboardCard.action}"/></a>
                </article>
            </c:forEach>
        </div>
        <c:url var="accountSettings" value="/pages/tai-khoan.html"/>
        <p><a href="<c:out value="${accountSettings}"/>">Thông tin tài khoản, đổi mật khẩu và lịch sử lắp ráp</a></p>
    </main>
    <%@ include file="workspace-footer.jspf" %>
</body>
</html>
