<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Hồ sơ học tập - <c:out value="${profile.fullName}"/> | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/server-view.css">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-profile.css?v=20261006.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.3">
        <meta name="robots" content="noindex, nofollow">
    </head>
    <body class="learning-workspace workspace-page-learning-profile">
        <%@ include file="workspace-header.jspf" %>
        <main class="learning-profile workspace-content" id="workspace-main" tabindex="-1">
            <nav class="profile-toolbar no-print" aria-label="Công cụ hồ sơ">
                <a class="profile-back" href="${pageContext.request.contextPath}/pages/tai-khoan.html">← Quay lại tài khoản</a>
                <button class="profile-print-button" id="print-profile" type="button">In / Lưu PDF</button>
                <span class="print-hint">Trong hộp thoại in, chọn Lưu dưới dạng PDF, khổ A4; nên tắt Đầu trang và chân trang để tránh
                thêm ngày giờ và tiêu đề của trình duyệt.</span>
            </nav>
            <noscript><p class="profile-noscript no-print">Bạn có thể dùng Ctrl+P (Windows/Linux) hoặc Command+P (macOS) để in hoặc lưu
            PDF.</p></noscript>

            <article class="profile-paper" aria-label="Bản xem trước hồ sơ học tập">
                <header class="profile-header">
                    <p class="profile-kicker">Robot Assembly Lab</p>
                    <h1>Hồ sơ học tập Robot Assembly Lab</h1>
                    <p>Họ tên: <strong><c:out value="${profile.fullName}"/></strong></p>
                    <p>Lập lúc <c:out value="${profile.generatedAtDisplay}"/></p>
                    <p class="student-id-line">Lớp/MSSV: ........</p>
                </header>

                <section aria-labelledby="profile-overview-heading">
                    <h2 id="profile-overview-heading">Tổng quan kết quả</h2>
                    <dl class="profile-summary">
                    <div><dt>Mẫu robot</dt><dd><c:out value="${profile.completionSummary}"/></dd></div>
                    <div><dt>Lượt làm bài</dt><dd><c:out value="${profile.quizAttemptCount}"/></dd></div>
                    <div><dt>Mẫu đã làm
                        bài</dt><dd><c:out value="${profile.quizRobotCount}"/>/<c:out value="${profile.robotCount}"/></dd></div>
                    <div><dt>Điểm trung bình</dt><dd><c:out value="${profile.averageBestScoreLabel}"/></dd></div>
                    <div><dt>Tình trạng chung</dt><dd><c:out value="${profile.overallStatusLabel}"/></dd></div>
                    </dl>
                    <p class="formula-note">Điểm trung bình là trung bình cộng tỷ lệ phần trăm của lượt tốt nhất ở từng mẫu đã làm bài;
                    làm tròn HALF_UP đến số nguyên. Tốt nhất là tỷ lệ score/tổng câu cao nhất (hòa lấy lượt mới hơn); gần nhất là lượt
                    có thời điểm nộp mới nhất (hòa lấy mã lượt lớn hơn).</p>
                </section>

                <section aria-labelledby="profile-robots-heading">
                    <h2 id="profile-robots-heading">Kết quả theo từng mẫu robot</h2>
                    <c:choose>
                        <c:when test="${empty profile.robotEntries}">
                            <p class="no-data">Chưa có dữ liệu trong danh mục robot.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="profile-robot-list">
                                <c:forEach var="profileRobot" items="${profile.robotEntries}">
                                    <article class="profile-robot" aria-label="Kết quả robot">
                                        <h3><c:out value="${profileRobot.robot.name}"/></h3>
                                        <p class="robot-description"><c:out value="${profileRobot.robot.summary}"/></p>
                                        <dl class="robot-facts">
                                        <div><dt>Trạng thái</dt><dd><c:out value="${profileRobot.statusLabel}"/></dd></div>
                                        <c:choose>
                                            <c:when test="${profileRobot.completed}">
                                                <div><dt>Hoàn thành</dt><dd><c:out value="${profileRobot.completionDate}"/> · phiên
                                                    #<c:out value="${profileRobot.completionSessionId}"/> ·
                                                    <c:out value="${profileRobot.completedCount}"/> lần</dd></div>
                                            </c:when>
                                            <c:when test="${profileRobot.inProgress or profileRobot.stopped}">
                                                <div><dt>Phiên gần
                                                    nhất</dt><dd><c:out value="${profileRobot.latestSessionStatusLabel}"/> · cập nhật
                                                    <c:out value="${profileRobot.latestSessionDate}"/></dd></div>
                                            </c:when>
                                            <c:otherwise>
                                                <div><dt>Phiên lắp ráp</dt><dd>Chưa có dữ liệu</dd></div>
                                            </c:otherwise>
                                        </c:choose>
                                        <c:choose>
                                            <c:when test="${profileRobot.quizDataAvailable}">
                                                <div><dt>Quiz tốt
                                                    nhất</dt><dd><c:out value="${profileRobot.bestQuizScore}"/>/<c:out
                                                    value="${profileRobot.bestQuizTotalQuestions}"/>
                                                    · <c:out value="${profileRobot.bestQuizDate}"/></dd></div>
                                                <div><dt>Quiz gần
                                                    nhất</dt><dd><c:out value="${profileRobot.latestQuizScore}"/>/<c:out
                                                    value="${profileRobot.latestQuizTotalQuestions}"/>
                                                    · <c:out value="${profileRobot.latestQuizDate}"/></dd></div>
                                                <div><dt>Số lượt quiz</dt><dd><c:out value="${profileRobot.quizAttemptCount}"/></dd></div>
                                            </c:when>
                                            <c:otherwise>
                                                <div><dt>Quiz</dt><dd>Chưa có dữ liệu</dd></div>
                                            </c:otherwise>
                                        </c:choose>
                                        </dl>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section aria-labelledby="profile-skills-heading">
                    <h2 id="profile-skills-heading">Kỹ năng và kiến thức đã thực hành</h2>
                    <c:choose>
                        <c:when test="${empty profile.skillLines}">
                            <p class="no-data"><c:out value="${profile.skillEmptyMessage}"/></p>
                        </c:when>
                        <c:otherwise>
                            <table class="profile-skill-table">
                                <caption>Các nội dung được ghi nhận từ mẫu đã hoàn thành và lượt quiz hiện có</caption>
                                <thead><tr><th scope="col">Nội dung đã thực hành</th><th scope="col">Căn cứ ghi nhận</th></tr></thead>
                                <tbody>
                                    <c:forEach var="skillLine" items="${profile.skillLines}">
                                        <tr>
                                            <th scope="row"><c:out value="${skillLine.title}"/><br><span
                                            class="skill-content"><c:out value="${skillLine.content}"/></span></th>
                                            <td><c:out value="${skillLine.evidence}"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section class="confirmation-section" aria-labelledby="confirmation-heading">
                    <h2 id="confirmation-heading">Nhận xét/xác nhận của giảng viên</h2>
                    <div class="writing-lines" aria-hidden="true"><span></span><span></span><span></span></div>
                    <div class="teacher-fields">
                        <p>Họ tên giảng viên: <span></span></p>
                        <p>Ngày: <span></span></p>
                        <p>Chữ ký: <span></span></p>
                    </div>
                </section>

                <footer class="profile-footer">
                    <p>Hoàn tất/kết quả trong hồ sơ này là kết quả mô phỏng trong phạm vi website, không khẳng định robot thật đã được
                    đấu nối hay vận hành đúng, và không thay thế đánh giá của giảng viên.</p>
                    <p>Thời điểm lập: <c:out value="${profile.generatedAtDisplay}"/></p>
                </footer>
            </article>

            <aside class="profile-technical-note no-print" aria-label="Ghi chú kỹ thuật cho buổi bảo vệ">
                Luồng trong mã: <code>LearningProfileServlet → StatsDB/RobotDB → LearningProfile → JSP</code>. Bộ đếm trang ở chân giấy
                phụ thuộc hỗ trợ margin box của trình duyệt. Nên tắt Đầu trang và chân trang; nếu trình duyệt không hiện số trang do CSS
                tạo và bạn cần số trang, có thể bật lại tùy chọn này, nhưng bản in sẽ có thêm ngày giờ, tiêu đề hoặc URL do trình duyệt
                tạo, theo định dạng của trình duyệt.
            </aside>
        </main>
        <script src="${pageContext.request.contextPath}/assets/js/learning-profile.js?v=20261003.1"></script>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
