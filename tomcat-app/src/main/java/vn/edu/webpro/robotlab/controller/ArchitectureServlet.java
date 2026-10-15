package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.CourseMapItem;

/** Bản đồ môn học công khai: Servlet đặt bean vào request, JSP chỉ trình bày. */
@WebServlet("/architecture")
public class ArchitectureServlet extends HttpServlet {
    /** GET không cần đăng nhập, không truy vấn hoặc thay đổi dữ liệu cá nhân. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<CourseMapItem> items = new ArrayList<>();
        add(items, "Servlet, @WebServlet, doGet/doPost",
                "Bộ linh kiện", "/my-kit",
                "tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/MyKitServlet.java", 32, true,
                "GET đọc dữ liệu và forward form. POST nhận số lượng, kiểm server rồi redirect; không "
                + "đặt JDBC trong JSP.");
        add(items, "Request và response; form GET/POST",
                "Tạo thiết kế", "/designs?action=new",
                "tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/DesignFormUtil.java", 23, true,
                "Trình duyệt gửi các field. Servlet đọc getParameter/getParameterValues qua tiện ích "
                + "form; response trả lỗi hoặc chuyển hướng khi thành công.");
        add(items, "setAttribute và forward",
                "Bảng điều khiển", "/account",
                "tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AccountPageServlet.java", 106, true,
                "Servlet lấy dữ liệu của user trong session, đặt JavaBean vào request rồi forward JSP "
                + "trong WEB-INF.");
        add(items, "sendRedirect và PRG",
                "Bổ sung vật tư từ hồ sơ", "/designs",
                "tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/DesignCartServlet.java", 44, true,
                "Form POST có token. Server kiểm chủ thiết kế và bù thiếu trong transaction; redirect "
                + "tới GET giỏ để tải lại không gửi POST lần nữa.");
        add(items, "JSP, EL và JSTL core",
                "Bảng điều khiển", "/account",
                "tomcat-app/src/main/webapp/WEB-INF/views/dashboard.jsp", 19, true,
                "EL gọi getter của bean. c:forEach lặp thẻ, c:if/c:choose chọn nhánh, c:out escape "
                + "chữ; c:url/c:param tạo liên kết.");
        add(items, "Include .jspf",
                "Điều hướng dùng chung", "/architecture",
                "tomcat-app/src/main/webapp/WEB-INF/views/workspace-header.jspf", 24, false,
                "JSP nạp menu bằng include lúc dịch trang. Menu cá nhân/quản trị được quyết định bởi "
                + "c:if theo session, không bằng JavaScript.");
        add(items, "JavaBean",
                "Quy tắc và trạng thái thẻ", "/account",
                "tomcat-app/src/main/java/vn/edu/webpro/robotlab/business/DashboardSummary.java", 9, true,
                "Bean Serializable có constructor rỗng, private field và getter/setter; phân biệt điểm "
                + "0 thật với chưa có lượt kiểm tra.");
        add(items, "HttpSession, phân quyền, session_version",
                "Thông tin tài khoản", "/pages/tai-khoan.html",
                "tomcat-app/src/main/java/vn/edu/webpro/robotlab/util/SessionUtil.java", 47, false,
                "Server đọc lại user và session_version trong DB. Ẩn menu không thay thế kiểm quyền, "
                + "cũng không tin vai trò trong localStorage.");
        add(items, "Filter, request/response",
                "API kiểm tra kết nối", "/api/health",
                "tomcat-app/src/main/java/vn/edu/webpro/robotlab/filter/RequestContextFilter.java", 19, false,
                "Filter đặt UTF-8, mã request và xử lý lỗi an toàn trước/sau Servlet; API giữ response "
                + "JSON.");
        add(items, "JNDI và ConnectionPool",
                "Danh mục minh họa", "/components",
                "tomcat-app/src/main/webapp/META-INF/context.xml", 13, false,
                "context.xml khai báo DataSource. ConnectionPool lấy qua JNDI, cấp và trả kết nối; "
                + "DBUtil đóng tài nguyên trong finally.");
        add(items, "JDBC, PreparedStatement, ResultSet",
                "Catalog linh kiện", "/components",
                "tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/ComponentDB.java", 26, false,
                "PreparedStatement bind giá trị vào dấu ?. ResultSet được chuyển thành JavaBean; "
                + "DBUtil đóng statement/result trước khi trả connection.");
        add(items, "Transaction và SELECT FOR UPDATE",
                "Hồ sơ thiết kế → giỏ", "/designs",
                "tomcat-app/src/main/java/vn/edu/webpro/robotlab/data/CartDB.java", 128, true,
                "Khóa user và thiết kế đúng chủ; xét kit, giỏ và kho trong cùng transaction. Commit "
                + "toàn bộ hoặc rollback; phục hồi autoCommit ở finally.");
        add(items, "Model 2 (MVC)",
                "Trang hoàn thiện mẫu", "/account",
                "tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AssemblyReviewPageServlet.java", 55, true,
                "Controller đọc request/session và điều phối; JavaBean tổng hợp, XxxDB đọc dữ liệu, "
                + "JSP chỉ trình bày. Mở mẫu đã hoàn tất từ dashboard.");
        add(items, "CSRF và Cache-Control",
                "Form bộ linh kiện", "/my-kit",
                "tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/MyKitServlet.java", 47, true,
                "POST form cần hidden csrfToken; API ghi cần X-CSRF-Token. Trang cá nhân dùng no-store "
                + "để hạn chế lưu dữ liệu riêng trên cache.");
        add(items, "Xử lý lỗi thân thiện",
                "Trang không tồn tại", "/trang-khong-ton-tai",
                "tomcat-app/src/main/java/vn/edu/webpro/robotlab/filter/RequestContextFilter.java", 31, false,
                "Không hiển thị SQL hoặc stack trace. Mã HTTP được giữ; lỗi API trả JSON, trang người "
                + "dùng có đường quay về.");
        request.setAttribute("courseItems", items);
        request.setAttribute("controllerName", getClass().getSimpleName());
        request.setAttribute("databaseLayer", "XxxDB + ConnectionPool + PreparedStatement + DBUtil");
        request.setAttribute("apiContract", "/api/health");
        getServletContext().getRequestDispatcher("/WEB-INF/views/architecture.jsp").forward(request, response);
    }

    private void add(List<CourseMapItem> items, String topic, String feature, String path,
            String sourcePath, int sourceLine, boolean requiresLogin, String explanation) {
        CourseMapItem item = new CourseMapItem();
        item.setTopic(topic);
        item.setFeature(feature);
        item.setPath(path);
        item.setSourcePath(sourcePath);
        item.setSourceLine(sourceLine);
        item.setRequiresLogin(requiresLogin);
        item.setExplanation(explanation);
        items.add(item);
    }
}
