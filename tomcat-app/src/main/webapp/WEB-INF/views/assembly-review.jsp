<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta name="robots" content="noindex, nofollow">
        <title>Hoàn thiện mẫu robot | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.5">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/assembly-review.css?v=20261014.4">
    </head>
    <body class="learning-workspace assembly-review">
        <%@ include file="workspace-header.jspf" %>
        <main id="workspace-main" class="workspace-content" tabindex="-1">
            <h1>Hoàn thiện mẫu robot</h1>
            <c:choose>
                <c:when test="${not empty assemblyReviewError}">
                    <p role="alert"><c:out value="${assemblyReviewError}" /></p>
                    <p><a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Về tài khoản</a></p>
                </c:when>
                <c:when test="${not session.completed}">
                    <p>Phiên này chưa hoàn tất. Hãy tiếp tục lắp ráp để mở trang hoàn thiện.</p>
                    <c:url var="reviewContinueUrl" value="/pages/${assemblyReview.continuePage}">
                        <c:param name="model" value="${session.robotId}" />
                        <c:param name="session" value="${session.id}" />
                    </c:url>
                    <a href="<c:out value='${reviewContinueUrl}'/>">Tiếp tục lắp ráp</a>
                </c:when>
                <c:otherwise>
                    <c:url var="reviewReceiptUrl" value="/assembly-receipt">
                        <c:param name="session" value="${session.id}" />
                    </c:url>
                    <c:url var="reviewQuizUrl" value="/pages/kiem-tra.html">
                        <c:param name="model" value="${session.robotId}" />
                    </c:url>
                    <c:url var="reviewRetryUrl" value="/pages/lap-rap.html">
                        <c:param name="model" value="${session.robotId}" />
                    </c:url>
                    <c:url var="reviewReplayUrl" value="/pages/lap-rap-3d.html">
                        <c:param name="model" value="${session.robotId}" />
                        <c:param name="session" value="${session.id}" />
                        <c:param name="replay" value="1" />
                    </c:url>
                    <p><span class="status-badge status-badge--success">Đã hoàn tất mô hình lắp ráp</span></p>
                    <h2 class="review-robot-name"><c:out value="${robot.name}" /></h2>
                    <p><c:out value="${robot.summary}" /></p>
                    <dl class="review-facts">
                        <dt>Chế độ</dt><dd><c:out value="${assemblyReview.modeLabel}" /></dd>
                        <dt>Bắt đầu</dt><dd><c:out value="${assemblyReview.startedAtLabel}" /></dd>
                        <dt>Hoàn tất</dt><dd><c:out value="${assemblyReview.completedAtLabel}" /></dd>
                    </dl>
                    <nav class="review-links no-print" aria-label="Mục lục trang hoàn thiện">
                        <a href="#review-model">Mẫu hoàn chỉnh</a>
                        <a href="#review-process">Quy trình lắp ráp</a>
                        <a href="#review-receipt">Phiếu kết quả</a>
                        <a href="#review-quiz">Kiểm tra kiến thức</a>
                    </nav>
                    <section id="review-model">
                        <h2>Mẫu hoàn chỉnh</h2>
                        <c:if test="${not empty assemblyReview.imagePath}">
                            <figure>
                                <img src="${pageContext.request.contextPath}/assets/<c:out value='${assemblyReview.imagePath}'/>"
                                     alt="Ảnh mẫu: <c:out value='${robot.name}'/>" width="1024" height="1024">
                                <figcaption>Ảnh mẫu sản phẩm hoàn chỉnh để đối chiếu.</figcaption>
                            </figure>
                        </c:if>
                        <c:choose>
                            <c:when test="${assemblyDrawing.available}">
                                <%@ include file="assembly-drawing.jspf" %>
                            </c:when>
                            <c:otherwise>
                                <p>Bản vẽ chi tiết chỉ có ở phiên lắp chi tiết.</p>
                            </c:otherwise>
                        </c:choose>
                    </section>
                    <section id="review-process">
                        <h2>Quy trình lắp ráp</h2>
                        <h3>Các bước chuẩn của mẫu</h3>
                        <ol class="review-standard-steps">
                            <c:forEach var="reviewStep" items="${reviewSteps}">
                                <li>
                                    <h4><c:out value="${reviewStep.title}" /></h4>
                                    <p><c:out value="${reviewStep.instruction}" /></p>
                                </li>
                            </c:forEach>
                        </ol>
                        <h3>Trình tự em đã thực hiện</h3>
                        <c:choose>
                            <c:when test="${assemblyReview.oldJournal}">
                                <p>Phiên này được tạo trước khi có nhật ký.</p>
                            </c:when>
                            <c:when test="${empty assemblyReview.activeEvents}">
                                <p>Phiên này chưa có nhật ký gắn từng vật thể.</p>
                            </c:when>
                            <c:otherwise>
                                <div class="workspace-table-scroll" role="region" aria-label="Trình tự lắp ráp" tabindex="0">
                                    <table>
                                        <caption>Các thao tác được lưu, bỏ thao tác đã hoàn tác</caption>
                                        <thead>
                                            <tr>
                                                <th scope="col">Thời điểm</th>
                                                <th scope="col">Thao tác</th>
                                                <th scope="col">Vật thể → điểm gắn</th>
                                                <th scope="col">Hướng</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="reviewEvent" items="${assemblyReview.activeEvents}">
                                                <tr>
                                                    <td><c:out value="${reviewEvent.createdAtLabel}" /></td>
                                                    <td><c:out value="${reviewEvent.actionLabel}" /></td>
                                                    <td>
                                                        <c:if test="${reviewEvent.hasInstance}">
                                                            <c:out value="${reviewEvent.instanceLabel}" /> →
                                                            <c:out value="${reviewEvent.slotLabel}" />
                                                        </c:if>
                                                    </td>
                                                    <td><c:out value="${reviewEvent.orientationLabel}" /></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <h3>Những lần cần sửa</h3>
                        <c:choose>
                            <c:when test="${empty assemblyReview.rejectionSummary}">
                                <p>Không có lần từ chối nào được ghi trong nhật ký.</p>
                            </c:when>
                            <c:otherwise>
                                <ul>
                                    <c:forEach var="reviewError" items="${assemblyReview.rejectionSummary}">
                                        <li><c:out value="${reviewError.reasonLabel}" />:
                                            <c:out value="${reviewError.repeatCount}" /> lần</li>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${assemblyReview.canReplay}">
                            <a class="no-print" href="<c:out value='${reviewReplayUrl}'/>">Xem lại quá trình 3D</a>
                        </c:if>
                    </section>
                    <section id="review-receipt">
                        <h2>Phiếu kết quả</h2>
                        <c:choose>
                            <c:when test="${session.detailed}">
                                <p>Đã gắn đúng <c:out value="${assemblyReview.correctCount}" />/<c:out
                                    value="${assemblyReview.requiredCount}" /> vật thể bắt buộc.</p>
                            </c:when>
                            <c:otherwise>
                                <p>Đã hoàn tất lắp ráp theo nhóm linh kiện của mẫu.</p>
                            </c:otherwise>
                        </c:choose>
                        <p>Hoàn tất lúc <c:out value="${assemblyReview.completedAtLabel}" />.</p>
                        <p class="review-links no-print">
                            <a href="<c:out value='${reviewReceiptUrl}'/>">Xem phiếu kết quả</a>
                            <button id="assembly-review-print" type="button">In trang hoàn thiện</button>
                        </p>
                    </section>
                    <section id="review-quiz">
                        <h2>Kiểm tra kiến thức</h2>
                        <c:if test="${assemblyReview.hasQuiz}">
                            <p>Điểm tốt nhất: <c:out value="${assemblyReview.bestQuizLabel}" />.
                                Gần nhất: <c:out value="${assemblyReview.latestQuizLabel}" />.</p>
                        </c:if>
                        <c:if test="${not assemblyReview.hasQuiz}">
                            <p>Em chưa có kết quả kiểm tra cho mẫu này.</p>
                        </c:if>
                        <c:choose>
                            <c:when test="${assemblyReview.hasQuizQuestions}">
                                <a class="no-print" href="<c:out value='${reviewQuizUrl}'/>">
                                    Làm bài kiểm tra</a>
                            </c:when>
                            <c:otherwise>
                                <p>Mẫu này chưa có bài kiểm tra.</p>
                            </c:otherwise>
                        </c:choose>
                    </section>
                    <p>Đây là kết quả lắp ráp mô phỏng, không xác nhận robot thật đấu nối hoặc vận hành an toàn.</p>
                    <p class="no-print">
                        <a href="<c:out value='${reviewRetryUrl}'/>">Thực hành lại</a>
                    </p>
                </c:otherwise>
            </c:choose>
        </main>
        <%@ include file="workspace-footer.jspf" %>
        <script src="${pageContext.request.contextPath}/assets/js/assembly-review.js?v=20261014.1" defer></script>
    </body>
</html>
