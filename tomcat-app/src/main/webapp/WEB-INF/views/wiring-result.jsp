<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <link rel="icon" href="${pageContext.request.contextPath}/assets/icons/cpu-chip.svg" type="image/svg+xml">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Kết quả nối dây | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring.css?v=20261006.2">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.5">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-wiring-result wiring">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <nav aria-label="Điều hướng">
                <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Tài khoản</a>
                <a href="${pageContext.request.contextPath}/wiring">Thực hành nối dây</a>
            </nav>
            <h1>Kết quả nối dây</h1>
            <c:if test="${wiringAlreadySubmitted}">
                <p role="status">Lượt đã nộp trước đó. Đây là kết quả cũ; sơ đồ gửi lại không được lưu.</p>
            </c:if>
            <p>Lượt #<c:out value="${wiringAttempt.id}"/> · <c:out value="${wiringExercise.title}"/></p>
            <p><c:out value="${wiringExercise.robotName}"/> · Nộp lúc <c:out value="${wiringAttempt.submittedAtDisplay}"/></p>
            <h2><c:set var="workspaceStatusTone" value="neutral"/>
            <c:choose>
                <c:when test="${wiringAttempt.grade.entirelyCorrect}">
                    <c:set var="workspaceStatusTone" value="success"/>
                </c:when>
                <c:when test="${not wiringAttempt.grade.entirelyCorrect}">
                    <c:set var="workspaceStatusTone" value="warning"/>
                </c:when>
            </c:choose>
            <span class="status-badge status-badge--<c:out value="${workspaceStatusTone}"/>">
                <c:out value="${wiringAttempt.grade.resultLabel}"/>
            </span></h2>
            <p>Điểm: <c:out value="${wiringAttempt.grade.scoreDisplay}"/>/100</p>
            <p>Dây đúng: <c:out value="${wiringAttempt.grade.correctCount}"/> ·
Dây sai/thừa: <c:out value="${wiringAttempt.grade.wrongCount}"/> ·
Còn thiếu: <c:out value="${wiringAttempt.grade.missingCount}"/> ·
Tổng dây bắt buộc: <c:out value="${wiringAttempt.grade.requiredCount}"/>.</p>
<details class="workspace-technical">
<summary>Cách tính điểm</summary>
<p>Điểm = 100 × max(C − W, 0) / N; C là số dây đúng, W là số dây sai/thừa, N là tổng dây bắt buộc.
Làm tròn đến 1 chữ số thập phân: chữ số tiếp theo từ 5 trở lên thì tăng chữ số cuối.</p>
</details>
            <p><c:out value="${wiringExercise.scopeText}"/></p>
            <p>Kết quả chỉ ghi nhận cặp trực tiếp trong bài mô phỏng, không xác nhận robot thật đấu nối/vận hành an toàn.</p>
            <c:set var="wiringDiagramConnections" value="${wiringAttempt.connections}"/>
            <%@ include file="wiring-diagram.jsp" %>
            <p>Chú giải: sơ đồ hiện dây đã nộp; nhãn Đúng, Sai/thừa, Cặp bị cấm và Thiếu trong bảng giải thích từng cặp.</p>
            <div class="wiring-table">
                <div class="workspace-table" role="region" aria-label="Bảng dữ liệu có thể cuộn ngang" tabindex="0">
                    <table>
                        <caption>Kết quả từng cặp nối</caption>
                        <thead>
                            <tr><th scope="col">Hai đầu nối</th><th scope="col">Kết quả</th><th scope="col">Giải thích</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="wiringConnection" items="${wiringAttempt.grade.rows}">
                                <tr>
                                    <td><span class="terminal-name" aria-label="<c:out value="${wiringConnection.first.label}"/>">
<c:out value="${wiringConnection.first.deviceLabel}"/> · <c:out value="${wiringConnection.first.pinLabel}"/>
<span class="terminal-code">[<c:out value="${wiringConnection.first.code}"/>]</span>
</span> · <span class="terminal-name" aria-label="<c:out value="${wiringConnection.second.label}"/>">
<c:out value="${wiringConnection.second.deviceLabel}"/> · <c:out value="${wiringConnection.second.pinLabel}"/>
<span class="terminal-code">[<c:out value="${wiringConnection.second.code}"/>]</span>
</span></td>
                                    <td><strong><c:set var="workspaceStatusTone" value="neutral"/>
                                    <c:choose>
                                        <c:when test="${wiringConnection.forbidden}">
                                            <c:set var="workspaceStatusTone" value="danger"/>
                                        </c:when>
                                        <c:when test="${wiringConnection.resultLabel eq 'Đúng'}">
                                            <c:set var="workspaceStatusTone" value="success"/>
                                        </c:when>
                                        <c:when test="${wiringConnection.resultLabel eq 'Sai/thừa'}">
                                            <c:set var="workspaceStatusTone" value="danger"/>
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="workspaceStatusTone" value="warning"/>
                                        </c:otherwise>
                                    </c:choose>
                                    <span class="status-badge status-badge--<c:out value="${workspaceStatusTone}"/>">
                                        <c:out value="${wiringConnection.resultLabel}"/>
                                    </span></strong></td>
                                    <td><c:out value="${wiringConnection.explanation}"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
            <c:if test="${wiringExercise.published}">
                <form method="post" action="${pageContext.request.contextPath}/wiring">
                    <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
                    <input type="hidden" name="id" value="<c:out value="${wiringAttempt.id}"/>">
                    <input type="hidden" name="expectedVersion" value="<c:out value="${wiringAttempt.version}"/>">

                    <input type="hidden" name="action" value="retry">
                    <input type="hidden" name="exerciseId" value="${wiringExercise.id}">
                    <button>Luyện lại (sao chép dây sang nháp mới)</button>
                </form>
            </c:if>
            <a href="${pageContext.request.contextPath}/pages/lap-rap.html?model=<c:out value="${wiringExercise.robotId}"/>">
            Sơ đồ tham khảo hiện có
            </a>
            <p>
            <a href="${pageContext.request.contextPath}/wiring-support?action=new&amp;attemptId=${wiringAttempt.id}">
            Gửi câu hỏi về kết quả đã nộp
            </a>
            · <a href="${pageContext.request.contextPath}/wiring-support">Xem lịch sử hỗ trợ</a>
            </p>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
