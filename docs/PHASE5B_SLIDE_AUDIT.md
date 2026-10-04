# Đợt 5b — code cũ ngoài phạm vi cần người dùng quyết định

Quét source Java/JSP của repo ngày 04/10/2026 sau refactor Đợt 5b; không quét
binary/WAR/target, dependency hoặc gán cú pháp Java cho JavaScript phía client.
Danh sách này chỉ ghi nhận theo yêu cầu, **không sửa các file dưới đây**. Dòng
được tính theo source hiện tại. Chuẩn cấm ở prompt Đợt 5b áp dụng cho code ứng
dụng Hồ sơ học tập; việc mở rộng sang chức năng cũ cần quyết định riêng.

## Code ứng dụng cũ

Đường dẫn Java đều dưới `tomcat-app/src/main/java/vn/edu/webpro/robotlab/`;
JSP dưới `tomcat-app/src/main/webapp/WEB-INF/views/`.

| File | Dòng | Kỹ thuật còn dùng |
| --- | --- | --- |
| `business/AssemblySession.java` | 151, 152, 199, 200, 201 | stream, lambda, method reference |
| `business/ShopOrder.java` | 29, 30 | stream, method reference, reduce |
| `controller/AdminQuizServlet.java` | 136 | stream/lambda |
| `controller/AdminStatsServlet.java` | 55 | stream/method reference |
| `controller/LearningSummaryServlet.java` | 45 | stream/method reference |
| `controller/AssemblySessionServlet.java` | 251, 254, 259, 263 | switch với nhánh `->` |
| `controller/OrderServlet.java` | 68, 69, 70, 71, 74, 75, 76, 77 | switch expression/`->` |
| `util/JsonUtil.java` | 40, 41, 42, 43, 44, 45, 46, 47 | switch với nhánh `->` |
| `data/QuizAttemptDB.java` | 52 | stream/method reference/toList |
| `data/HealthDB.java` | 16 | createStatement (truy vấn health cố định, không có input người dùng) |
| `order-history.jsp` | 36, 38, 43 | fmt:formatNumber |

## Công cụ test cũ

Đường dẫn dưới `tomcat-app/src/test/java/vn/edu/webpro/robotlab/`. Các file này
không thuộc runtime website, vẫn giữ nguyên vì nằm ngoài phạm vi refactor.

| File | Dòng | Kỹ thuật còn dùng |
| --- | --- | --- |
| `business/JavaBeanRulesTest.java` | 83 | Stream xử lý file, lambda/toList |
| `view/JspExpressionContractTest.java` | 94, 95, 116 | Stream xử lý file, lambda/toList |
| `util/UtilTest.java` | 53, 54, 55, 77, 78, 83, 107, 116 | lambda cho assertThrows |
| `util/JsonUtilIntegerFieldTest.java` | 17, 19, 21 | lambda cho assertThrows |
| `data/DatabaseLifecycleListenerTest.java` | 26, 27, 60 | lambda cho proxy/DriverAction |

Không kết luận toàn bộ dự án đã bám hoàn toàn cú pháp slide. Guard Đợt 5b chỉ
áp dụng cho 6 file Java mới của Đợt 5, ba hàm đọc hồ sơ trong StatsDB và JSP
hồ sơ; bảng chapter/slide và giải thích nằm trong TEAM_FLOW_DEMO_GUIDE.md.
