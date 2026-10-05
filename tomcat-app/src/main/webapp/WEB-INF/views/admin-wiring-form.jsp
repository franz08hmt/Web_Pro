<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Phòng thực hành nối dây | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/wiring.css?v=20261005.2">
    </head>
    <body class="wiring">
        <nav aria-label="Điều hướng">
            <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Tài khoản</a>
            <a href="${pageContext.request.contextPath}/wiring">Thực hành nối dây</a>
        </nav>
        <h1>Biên soạn bài nối dây nháp</h1>
        <p>Số ô cố định: tối đa 40 đầu nối, 80 quy tắc. Bỏ trống mã đầu nối/cả hai ô quy tắc để bỏ qua ô đó.</p>
        <form method="post" action="${pageContext.request.contextPath}/admin-wiring">
            <input type="hidden" name="csrfToken" value="<c:out value="${sessionScope.csrfToken}"/>">
            <input type="hidden" name="action" value="saveDraft">
            <input type="hidden" name="id" value="${wiringExercise.id}">
            <label>Mã ổn định (ASCII, số, dấu . _ -)
                <input name="code" maxlength="64" value="<c:out value="${wiringExercise.code}"/>" required>
            </label>
            <label>Tên bài
                <input name="title" maxlength="150" value="<c:out value="${wiringExercise.title}"/>" required>
            </label>
            <fieldset>
                <legend>Robot</legend>
                <c:forEach var="robot" items="${robots}">
                    <label>
                        <input type="radio" name="robotId" value="<c:out value="${robot.id}"/>"
                            <c:if test="${wiringExercise.robotId eq robot.id}">checked</c:if> required>
                        <c:out value="${robot.name}"/>
                    </label>
                </c:forEach>
            </fieldset>
            <label>Mục tiêu
                <textarea name="objective" maxlength="2000" required><c:out value="${wiringExercise.objective}"/></textarea>
            </label>
            <label>Phạm vi mô phỏng (ghi rõ phần không chấm)
                <textarea name="scopeText" maxlength="2000" required><c:out value="${wiringExercise.scopeText}"/></textarea>
            </label>
            <h2>Đầu nối</h2>
            <c:forEach var="slot" begin="1" end="40">
                <c:set var="wiringTerminal" value="${wiringExercise.terminals[slot - 1]}"/>
                <fieldset>
                    <legend>Ô đầu nối <c:out value="${slot}"/></legend>
                    <label>Mã riêng (ví dụ line-left.OUT)
                        <input name="terminalCode_${slot}" maxlength="64" value="<c:out value="${wiringTerminal.code}"/>">
                    </label>
                    <label>Mã thiết bị
                        <input name="deviceCode_${slot}" maxlength="64" value="<c:out value="${wiringTerminal.deviceCode}"/>">
                    </label>
                    <label>Tên thiết bị
                        <input name="deviceLabel_${slot}" maxlength="100" value="<c:out value="${wiringTerminal.deviceLabel}"/>">
                    </label>
                    <label>Nhãn chân
                        <input name="pinLabel_${slot}" maxlength="50" value="<c:out value="${wiringTerminal.pinLabel}"/>">
                    </label>
                    <label>Tọa độ X (40–960)
                        <input name="x_${slot}" type="number" min="40" max="960" value="<c:out value="${wiringTerminal.x}"/>">
                    </label>
                    <label>Tọa độ Y (40–960)
                        <input name="y_${slot}" type="number" min="40" max="960" value="<c:out value="${wiringTerminal.y}"/>">
                    </label>
                </fieldset>
            </c:forEach>
            <h2>Quy tắc (đầu A/B là số ô đầu nối bên trên)</h2>
            <c:set var="wiringEditRules" value="${wiringExercise.editRules}"/>
            <c:forEach var="slot" begin="1" end="80">
                <c:set var="wiringRule" value="${wiringEditRules[slot - 1]}"/>
                <fieldset>
                    <legend>Quy tắc <c:out value="${slot}"/></legend>
                    <label>Ô đầu nối A
                        <input name="ruleA_${slot}" type="number" min="1" max="40"
                            value="<c:out value="${wiringRule.terminalA}"/>">
                    </label>
                    <label>Ô đầu nối B
                        <input name="ruleB_${slot}" type="number" min="1" max="40"
                            value="<c:out value="${wiringRule.terminalB}"/>">
                    </label>
                    <label>
                        <input type="radio" name="ruleKind_${slot}" value="REQUIRED"
                            <c:if test="${empty wiringRule or wiringRule.required}">checked</c:if>>Bắt buộc
                    </label>
                    <label>
                        <input type="radio" name="ruleKind_${slot}" value="FORBIDDEN"
                            <c:if test="${wiringRule.forbidden}">checked</c:if>>Bị cấm
                    </label>
                    <label>Giải thích đúng/thiếu hoặc lý do cấm
                        <textarea name="ruleExplanation_${slot}" maxlength="1000"><c:out value="${wiringRule.explanation}"/></textarea>
                    </label>
                </fieldset>
            </c:forEach>
            <button>Lưu nháp</button>
        </form>
    </body>
</html>
