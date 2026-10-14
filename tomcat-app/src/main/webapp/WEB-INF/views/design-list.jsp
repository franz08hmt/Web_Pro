<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta name="robots" content="noindex, nofollow">
        <title>Thiết kế của tôi | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.5">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/design.css?v=20261014.1">
    </head>
    <body class="learning-workspace design-workspace">
        <%@ include file="workspace-header.jspf" %>
        <main id="workspace-main" class="workspace-content" tabindex="-1">
            <h1>Thiết kế của tôi</h1>
            <c:if test="${not empty designError}">
                <p role="alert"><c:out value="${designError}" /></p>
            </c:if>
            <p>Tự thiết kế trong bộ linh kiện được hỗ trợ kiểm chứng. Không phải ghép mọi linh kiện bất kỳ.</p>
            <p>Thiết kế riêng không tính là hoàn thành mẫu chính thức hoặc bằng chứng nhiệm vụ.</p>
            <p class="design-actions">
                <a href="${pageContext.request.contextPath}/designs?action=new">Tạo thiết kế</a>
                <a href="${pageContext.request.contextPath}/my-kit">Bộ linh kiện của tôi</a>
            </p>
            <c:if test="${param.saved eq '1'}"><p role="status">Đã lưu thay đổi.</p></c:if>
            <c:if test="${empty designRecords}"><p>Chưa có thiết kế. Chọn Tạo thiết kế để bắt đầu.</p></c:if>
            <c:forEach var="designRecord" items="${designRecords}">
                <article class="workspace-course">
                    <h2><c:out value="${designRecord.name}" /></h2>
                    <p><c:out value="${designRecord.goalName}" /> · <c:out value="${designRecord.sourceLabel}" /></p>
                    <p class="status-badge"><c:out value="${designRecord.statusLabel}" /></p>
                    <p>Cập nhật <c:out value="${designRecord.updatedAtLabel}" />.</p>
                    <c:if test="${designRecord.checked}">
                        <p><c:out value="${designRecord.lastCheckSummary}" /></p>
                    </c:if>
                    <p class="design-actions">
                        <a href="${pageContext.request.contextPath}/pages/xuong-thiet-ke.html?design=${designRecord.id}">
                            Mở phòng lắp thiết kế</a>
                        <a href="${pageContext.request.contextPath}/designs?action=rename&amp;id=${designRecord.id}">Đổi tên</a>
                        <a href="${pageContext.request.contextPath}/designs?action=delete&amp;id=${designRecord.id}">Xóa thiết kế</a>
                    </p>
                </article>
            </c:forEach>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
