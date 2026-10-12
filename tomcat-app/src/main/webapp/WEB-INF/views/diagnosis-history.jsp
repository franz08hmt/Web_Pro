<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <link rel="icon" href="${pageContext.request.contextPath}/assets/icons/cpu-chip.svg" type="image/svg+xml">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Lịch sử chẩn đoán | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.5">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-diagnosis-history practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">
            <p><a href="${pageContext.request.contextPath}/diagnosis">← Quay lại luyện chẩn đoán</a></p>
            <h1>Lịch sử chẩn đoán</h1>
            <p>Xem lại các lượt luyện đã nộp, từ mới nhất đến cũ nhất. Lượt nhiệm vụ không nằm trong danh sách này.</p>
            <dl class="workspace-metrics">
                <div class="workspace-metric">
                    <dt>Lượt luyện đã nộp</dt>
                    <dd><c:out value="${historyPagination.totalItems}"/></dd>
                </div>
                <div class="workspace-metric">
                    <dt>Trang hiện tại</dt>
                    <dd><c:out value="${historyPagination.page}"/> / <c:out value="${historyPagination.totalPages}"/></dd>
                </div>
            </dl>
            <section class="workspace-panel" aria-labelledby="history-heading">
                <h2 id="history-heading">Kết quả luyện tập</h2>
                <c:choose>
                    <c:when test="${empty diagnosisHistory}">
                        <p>Chưa có lịch sử chẩn đoán. Hoàn thành một lượt luyện để xem lại kết quả ở đây.</p>
                        <p><a href="${pageContext.request.contextPath}/diagnosis">Bắt đầu luyện tập →</a></p>
                    </c:when>
                    <c:otherwise>
                        <div class="workspace-table" tabindex="0" role="region" aria-label="Kết quả chẩn đoán có thể cuộn ngang">
                            <table>
                                <caption>Chỉ các lượt PRACTICE đã nộp của tài khoản đang đăng nhập</caption>
                                <thead>
                                    <tr>
                                        <th scope="col">Mã lượt / tình huống</th>
                                        <th scope="col">Mẫu robot</th>
                                        <th scope="col">Điểm /10</th>
                                        <th scope="col">Thời gian nộp (giờ Việt Nam)</th>
                                        <th scope="col">Kết quả</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="historyItem" items="${diagnosisHistory}">
                                        <tr>
                                            <th scope="row">#<c:out value="${historyItem.id}"/> ·
                                                <c:out value="${historyItem.scenarioTitle}"/></th>
                                            <td><c:out value="${historyItem.robotName}"/></td>
                                            <td><c:out value="${historyItem.scoreDisplay}"/></td>
                                            <td><c:out value="${historyItem.submittedAtDisplay}"/></td>
                                            <td>
                                                <a href="?action=result&amp;id=${historyItem.id}">
                                                Xem lại lượt #<c:out value="${historyItem.id}"/> →</a></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        <nav class="paging" aria-label="Phân trang lịch sử chẩn đoán">
                            <c:if test="${historyPagination.page > 1}">
                                <a href="?action=history&amp;page=${historyPagination.page - 1}">
                                    ← Trang trước</a>
                            </c:if>
                            <span>Trang <c:out value="${historyPagination.page}"/> /
                                <c:out value="${historyPagination.totalPages}"/></span>
                            <c:if test="${historyPagination.page < historyPagination.totalPages}">
                                <a href="?action=history&amp;page=${historyPagination.page + 1}">
                                    Trang sau →</a>
                            </c:if>
                        </nav>
                    </c:otherwise>
                </c:choose>
            </section>
            <p>Đây là kết quả thực hành mô phỏng trên website, không xác nhận robot thật đã vận hành đúng.</p>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
