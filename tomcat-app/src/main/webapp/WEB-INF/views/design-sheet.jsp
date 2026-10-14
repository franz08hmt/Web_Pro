<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta name="robots" content="noindex, nofollow">
        <title>Hồ sơ thiết kế robot | Robot Assembly Lab</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/learning-workspace.css?v=20261006.5">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/design.css?v=20261014.1">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/assembly-review.css?v=20261014.2">
        <script src="${pageContext.request.contextPath}/assets/js/assembly-review.js?v=20261014.1" defer></script>
    </head>
    <body class="learning-workspace design-workspace assembly-review">
        <%@ include file="workspace-header.jspf" %>
        <main id="workspace-main" class="workspace-content" tabindex="-1">
            <h1>Hồ sơ thiết kế robot</h1>
            <c:if test="${not empty designError}">
                <p role="alert"><c:out value="${designError}" /></p>
            </c:if>
            <c:if test="${not empty designSheet}">
                <p>Thiết kế tự do – mô phỏng giản lược; không thay thế kiểm tra thực tế.</p>
                <h2><c:out value="${sheetDesign.name}" /></h2>
                <dl class="review-facts">
                    <dt>Mục tiêu</dt><dd><c:out value="${sheetDesign.goalName}" /></dd>
                    <dt>Nguồn linh kiện</dt><dd><c:out value="${sheetDesign.sourceLabel}" /></dd>
                    <dt>Cập nhật</dt><dd><c:out value="${sheetDesign.updatedAtLabel}" /></dd>
                    <dt>Luật kiểm chứng</dt><dd>Phiên bản <c:out value="${designSheet.layout.rules.versionNo}" /></dd>
                </dl>
                <p class="review-links no-print">
                    <a href="${pageContext.request.contextPath}/designs">Thiết kế của tôi</a>
                    <a href="${pageContext.request.contextPath}/pages/xuong-thiet-ke.html?design=${sheetDesign.id}">
                        Tiếp tục lắp thiết kế</a>
                    <button id="assembly-review-print" type="button">In / Lưu PDF</button>
                </p>
                <section aria-labelledby="sheet-drawing-heading">
                    <h2 id="sheet-drawing-heading">1. Bản vẽ mô hình đã gắn</h2>
                    <c:choose>
                        <c:when test="${assemblyDrawing.available}">
                            <%@ include file="assembly-drawing.jspf" %>
                        </c:when>
                        <c:otherwise><p>Chưa có vật thể được gắn để dựng bản vẽ.</p></c:otherwise>
                    </c:choose>
                    <p>Vật thể còn trong khay không có trên hình. Khi nhãn quá đông, dùng bảng chú giải để đọc đầy đủ.</p>
                </section>
                <section aria-labelledby="sheet-bom-heading">
                    <h2 id="sheet-bom-heading">2. Danh sách vật tư cần chuẩn bị</h2>
                    <p>Số cần lấy từ bộ linh kiện đã chọn cho thiết kế; số đang có lấy từ Bộ linh kiện của tôi hiện tại.</p>
                    <div class="workspace-table-scroll" role="region" aria-label="Danh sách vật tư" tabindex="0">
                        <table>
                            <caption>Vật tư của thiết kế, gồm cả vật thể còn trong khay</caption>
                            <thead><tr><th scope="col">Linh kiện</th><th scope="col">Cần</th>
                                <th scope="col">Đang có</th><th scope="col">Còn thiếu</th>
                                <th scope="col">Sản phẩm</th></tr></thead>
                            <tbody>
                                <c:forEach var="designBom" items="${designSheet.bomItems}">
                                    <tr>
                                        <th scope="row"><c:out value="${designBom.name}" /></th>
                                        <td><c:out value="${designBom.needed}" /></td>
                                        <td><c:out value="${designBom.owned}" /></td>
                                        <td><c:out value="${designBom.missing}" /></td>
                                        <td><c:choose>
                                            <c:when test="${not empty designBom.shopPath}">
                                                <a href="${pageContext.request.contextPath}<c:out value='${designBom.shopPath}' />">
                                                    Xem ở cửa hàng</a>
                                            </c:when>
                                            <c:otherwise>Chưa có sản phẩm liên kết</c:otherwise>
                                        </c:choose></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </section>
                <section aria-labelledby="sheet-steps-heading">
                    <h2 id="sheet-steps-heading">3. Trình tự lắp đề xuất</h2>
                    <p>Cha trước con, theo bố cục đã lưu. Đây là gợi ý lắp cơ khí, chưa bao gồm nối dây.</p>
                    <c:if test="${empty designSheet.steps}"><p>Gắn khung nền để bắt đầu lập trình tự.</p></c:if>
                    <ol>
                        <c:forEach var="designStep" items="${designSheet.steps}">
                            <li>Bước <c:out value="${designStep.number}" />:
                                <c:out value="${designStep.description}" /></li>
                        </c:forEach>
                    </ol>
                    <p>Vật thể chưa gắn cần được bố trí và kiểm tra trước khi dùng trình tự này để tham khảo.</p>
                </section>
                <section aria-labelledby="sheet-check-heading">
                    <h2 id="sheet-check-heading">4. Kết quả kiểm tra và lưu ý lắp thật</h2>
                    <c:choose>
                        <c:when test="${designSheet.hasReport}">
                            <p><strong><c:out value="${sheetDesign.lastCheckSummary}" /></strong></p>
                            <p>Kiểm tra lúc <c:out value="${sheetDesign.lastCheckedAtLabel}" />.</p>
                            <ul>
                                <c:forEach var="sheetCheckItem" items="${sheetCheck.items}">
                                    <li><c:out value="${sheetCheckItem.layer}" /> ·
                                        <strong><c:out value="${sheetCheckItem.resultLabel}" /></strong>:
                                        <c:out value="${sheetCheckItem.message}" /></li>
                                </c:forEach>
                            </ul>
                        </c:when>
                        <c:otherwise>
                            <p>Chưa có kết quả cho bố cục hiện tại. Vào Xưởng và chọn Kiểm tra thiết kế.</p>
                        </c:otherwise>
                    </c:choose>
                    <p>Kết quả dựa trên luật lắp ghép giản lược, không mô phỏng điện/vật lý hoặc firmware.</p>
                    <p>Ngắt nguồn trước khi đổi dây. Đối chiếu thông số điện, cực nguồn, khả năng cấp dòng
                        và cách cố định bộ phận theo tài liệu nhà sản xuất trước khi lắp robot thật.</p>
                    <p>Thiết kế riêng không tính là hoàn thành mẫu chính thức và không làm bằng chứng nhiệm vụ.</p>
                    <h3>Những lần cần sửa trong quá trình lắp</h3>
                    <c:if test="${empty designSheet.learningErrors}"><p>Chưa ghi nhận lỗi lắp ghép.</p></c:if>
                    <ul>
                        <c:forEach var="designErrorCount" items="${designSheet.learningErrors}">
                            <li><c:out value="${designErrorCount.label}" />:
                                <c:out value="${designErrorCount.count}" /> lần.</li>
                        </c:forEach>
                    </ul>
                    <p>Lỗi tab, khóa phiên hoặc định dạng yêu cầu không tính là lỗi lắp ghép của người học.</p>
                </section>
            </c:if>
        </main>
        <%@ include file="workspace-footer.jspf" %>
    </body>
</html>
