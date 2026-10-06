<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Phiếu kết quả lắp ráp | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.1">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-assembly-receipt">
        <%@ include file="workspace-header.jspf" %>
        <main class="workspace-content" id="workspace-main" tabindex="-1">

            <h1>Phiếu kết quả lắp ráp</h1>


            <c:choose>
                <c:when test="${session.status != 'COMPLETED'}">
                    <p class="notice notice-error">
                    Phiên lắp ráp này chưa hoàn tất nên chưa có phiếu kết quả.
                    Trạng thái hiện tại: <strong><c:out value="${session.status}"/></strong>.
                    </p>
                    <p class="links">
                    <a
                    href="${pageContext.request.contextPath}/pages/lap-rap-3d.html?model=${session.robotId}&amp;session=${session.id}">Vào
                    phòng lắp ráp 3D để tiếp tục &rarr;</a>
                    </p>
                </c:when>
                <c:otherwise>
                    <section class="notice">
                        <p class="workspace-eyebrow">Đã hoàn tất lắp ráp trong mô hình 3D</p>
                        <strong><c:out value="${robot.name}"/></strong> · <c:out value="${robot.level}"/>
                        <p class="summary"><c:out value="${robot.summary}"/></p>
                    </section>

                    <dl>
                    <dt>Mã phiên</dt><dd><c:out value="${session.id}"/></dd>
                    <dt>Mẫu robot</dt><dd><c:out value="${session.robotId}"/></dd>
                    <dt>Bắt đầu phiên</dt><dd><c:out value="${session.createdAt}"/></dd>
                    <dt>Hoàn tất lắp ráp</dt><dd><c:out value="${session.completedAt}"/></dd>
                    <dt>Kết quả</dt><dd>Đã lắp đủ toàn bộ <c:out value="${fn:length(requiredComponents)}"/> nhóm linh kiện bắt buộc
                    trong mô hình 3D.</dd>
                    </dl>

                    <div class="workspace-table" role="region" aria-label="Bảng dữ liệu có thể cuộn ngang" tabindex="0">
                        <table>
                            <caption>Linh kiện bắt buộc của mẫu robot này (bảng <code>robot_components</code>)</caption>
                            <thead>
                                <tr>
                                    <th scope="col">Mã linh kiện</th>
                                    <th scope="col">Tên linh kiện</th>
                                    <th scope="col">Số lượng yêu cầu</th>
                                    <th scope="col">Đã lắp trong mô hình 3D</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="rc" items="${requiredComponents}">
                                    <tr>
                                        <td><code><c:out value="${rc.componentId}"/></code></td>
                                        <td><c:out value="${componentNames[rc.componentId]}"/></td>
                                        <td><c:out value="${rc.quantity}"/></td>
                                        <td>✓ Đã lắp</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <p class="notice">
                    "Hoàn tất" ở đây có nghĩa là đã hoàn tất bài lắp ráp trong phạm vi mô phỏng
                    của website (đủ mọi linh kiện bắt buộc trong mô hình 3D); phiếu này không
                    khẳng định robot thật đã được đấu nối điện hoặc vận hành đúng.
                    </p>

                    <p class="links">
                    <a href="${pageContext.request.contextPath}/pages/kiem-tra.html?model=${session.robotId}">Làm bài kiểm tra kiến thức
                    &rarr;</a> ·
                    <a href="${pageContext.request.contextPath}/pages/lap-rap.html?model=${session.robotId}">Thực hành lại mẫu này
                    &rarr;</a> ·
                    <a href="${pageContext.request.contextPath}/pages/tra-cuu-loi.html?robotId=${session.robotId}">Tra cứu lỗi lắp ráp
                    &rarr;</a> ·
                    <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Xem lịch sử tài khoản</a>
                    </p>
                </c:otherwise>
            </c:choose>

            <p class="links">
            <a href="${pageContext.request.contextPath}/index.html">Về trang chủ</a>
            </p>
            <details class="workspace-technical no-print">
                <summary>Ghi chú kỹ thuật</summary>
                <p class="lead">
                Trang do <code>AssemblyReceiptPageServlet</code> dựng ở server: đọc phiên
                theo <code>(id, user_id)</code> qua <code>AssemblySessionDB.selectSession()</code>
                nên chỉ chủ phiên mới xem được, đặt robot và danh sách linh kiện vào request
                bằng <code>setAttribute</code> rồi <code>forward()</code> sang JSP này. Trạng thái
                <code>COMPLETED</code> và <code>completed_at</code> chỉ được
                <code>AssemblySessionDB.completeSession()</code> ghi sau khi đối chiếu tập hợp
                linh kiện bắt buộc với dữ liệu thật trong <code>session_visual_parts</code> —
                trang này không tin số phần trăm do trình duyệt tự tính.
                </p>
            </details>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
