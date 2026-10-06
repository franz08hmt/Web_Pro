<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Thực hành chẩn đoán | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/practice-tasks.css?v=20261005.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.1">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-diagnosis-play practice-tasks">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Tài khoản</a></p>
            <h1><c:out value="${diagnosisAttempt.scenario.title}"/></h1>
            <p>Lượt #<c:out value="${diagnosisAttempt.id}"/> · <c:out value="${diagnosisAttempt.scenario.robotName}"/></p>
            <p class="task-text"><c:out value="${diagnosisAttempt.scenario.contextText}"/></p>
            <p class="task-text"><c:out value="${diagnosisAttempt.scenario.symptomText}"/></p>
            <p>Quan sát là dữ liệu mô phỏng của bài tập, không phải phép đo từ robot thật.</p>
            <h2>Chọn phép kiểm tra</h2>
            <c:forEach var="diagnosisCheck" items="${diagnosisAttempt.scenario.checks}">
                <section class="task-card">
                    <h3><c:out value="${diagnosisCheck.label}"/></h3>
                    <c:choose>
                        <c:when test="${diagnosisCheck.chosen}">
                            <p class="task-text"><c:out value="${diagnosisCheck.observationDisplay}"/></p>
                            <p>Đã kiểm tra lúc <c:out value="${diagnosisCheck.chosenAtDisplay}"/></p>
                        </c:when>
                        <c:otherwise>
                            <form method="post" action="${pageContext.request.contextPath}/diagnosis">
                                <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                                <input type="hidden" name="action" value="check">
                                <input type="hidden" name="id" value="<c:out value="${diagnosisAttempt.id}"/>">
                                <input type="hidden" name="checkId" value="<c:out value="${diagnosisCheck.id}"/>">
                                <button>Thực hiện phép kiểm tra</button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:forEach>
            <aside class="workspace-panel" aria-label="Quy tắc kết luận">
                <h2>Kết luận</h2>
                <p>Thu thập đủ quan sát cần thiết: 4 điểm; xác định nguyên nhân: 3; chọn cách xử lý: 3.</p>
                <p>Có thể kết luận khi chưa kiểm tra; điểm thu thập quan sát khi đó bằng 0. Kết luận chỉ được chốt một lần.</p>
            </aside>
            <form method="post" action="${pageContext.request.contextPath}/diagnosis">
                <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                <input type="hidden" name="action" value="conclude">
                <input type="hidden" name="id" value="<c:out value="${diagnosisAttempt.id}"/>">
                <fieldset>
                    <legend>Nguyên nhân</legend>
                    <c:forEach var="diagnosisOption" items="${diagnosisAttempt.scenario.causes}">
                        <label class="task-choice">
                        <input type="radio" name="causeOptionId" value="<c:out value="${diagnosisOption.id}"/>" required>
                        <c:out value="${diagnosisOption.label}"/>
                        </label>
                    </c:forEach>
                </fieldset>
                <fieldset>
                    <legend>Cách xử lý</legend>
                    <c:forEach var="diagnosisOption" items="${diagnosisAttempt.scenario.actions}">
                        <label class="task-choice">
                        <input type="radio" name="actionOptionId" value="<c:out value="${diagnosisOption.id}"/>" required>
                        <c:out value="${diagnosisOption.label}"/>
                        </label>
                    </c:forEach>
                </fieldset>
                <button>Chốt kết luận</button>
            </form>
            <p><a href="${pageContext.request.contextPath}/tasks">Nhiệm vụ của tôi</a> · <a href="?">Danh sách luyện</a></p>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
