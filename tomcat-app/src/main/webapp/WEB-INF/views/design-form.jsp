<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta name="robots" content="noindex, nofollow">
        <title>Chọn linh kiện cho thiết kế | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.5">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/design.css?v=20261014.1">
    </head>
    <body class="learning-workspace design-workspace">
        <%@ include file="workspace-header.jspf" %>
        <main id="workspace-main" class="workspace-content" tabindex="-1">
            <h1>Chọn linh kiện cho thiết kế</h1>
            <c:if test="${not empty designError}">
                <p role="alert"><c:out value="${designError}" /></p>
            </c:if>
            <p><a href="${pageContext.request.contextPath}/designs">Về danh sách thiết kế</a></p>
            <c:choose>
                <c:when test="${not empty metadataAction}">
                    <h2><c:out value="${designDraft.name}" /></h2>
                    <form method="post" action="${pageContext.request.contextPath}/designs">
                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                        <input type="hidden" name="action" value="<c:out value='${metadataAction}'/>">
                        <input type="hidden" name="id" value="<c:out value='${designDraft.id}'/>">
                        <input type="hidden" name="expectedLayoutVersion" value="<c:out value='${designDraft.layoutVersion}'/>">
                        <c:choose>
                            <c:when test="${metadataAction eq 'rename'}">
                                <label>Tên thiết kế
                                    <input name="name" required maxlength="80" value="<c:out value='${designDraft.name}'/>">
                                </label>
                                <button type="submit">Lưu tên thiết kế</button>
                            </c:when>
                            <c:otherwise>
                                <p>Xóa thiết kế và nhật ký của thiết kế; không thể khôi phục.</p>
                                <label><input type="checkbox" name="confirmDelete" value="yes" required>
                                    Tôi xác nhận xóa thiết kế này</label>
                                <button type="submit">Xóa thiết kế</button>
                            </c:otherwise>
                        </c:choose>
                    </form>
                </c:when>
                <c:otherwise>
                    <p>Đúng một khung nền, tối đa 30 vật thể và 10 mỗi loại. Linh kiện chưa có luật không được coi là đúng.</p>
                    <form method="post" action="${pageContext.request.contextPath}/designs">
                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.csrfToken}'/>">
                        <input type="hidden" name="action" value="create">
                        <label>Tên thiết kế
                            <input name="name" required maxlength="80" value="<c:out value='${designDraft.name}'/>">
                        </label>
                        <fieldset>
                            <legend>Mục tiêu</legend>
                            <c:forEach var="designGoal" items="${designGoals}">
                                <label>
                                    <input type="radio" name="goalCode" value="<c:out value='${designGoal.code}'/>" required
                                           <c:if test="${designGoal.code eq designDraft.goalCode}">checked</c:if>>
                                    <c:out value="${designGoal.name}" />
                                </label>
                            </c:forEach>
                        </fieldset>
                        <fieldset>
                            <legend>Nguồn linh kiện</legend>
                            <label><input type="radio" name="source" value="KIT" required
                                <c:if test="${designDraft.fromKit}">checked</c:if>>Từ bộ linh kiện của tôi</label>
                            <label><input type="radio" name="source" value="LIBRARY" required
                                <c:if test="${not designDraft.fromKit}">checked</c:if>>Từ thư viện linh kiện</label>
                            <p>Nguồn bộ linh kiện không vượt số đang có; đổi bộ sau này không đổi thiết kế đã tạo.</p>
                        </fieldset>
                        <div class="kit-grid">
                            <c:forEach var="kitItem" items="${kitItems}">
                                <fieldset>
                                    <legend><c:out value="${kitItem.name}" /></legend>
                                    <p><c:out value="${kitItem.supportLabel}" /> · Đang có <c:out value="${kitItem.quantity}" /></p>
                                    <label>Số vật thể chọn (0–10)
                                        <input type="hidden" name="componentId" value="<c:out value='${kitItem.componentId}'/>">
                                        <input type="number" name="quantity_<c:out value='${kitItem.componentId}'/>"
                                               min="0" max="10" required value="<c:out value='${kitItem.inputValue}'/>">
                                    </label>
                                </fieldset>
                            </c:forEach>
                        </div>
                        <button type="submit">Tạo thiết kế</button>
                    </form>
                </c:otherwise>
            </c:choose>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
