package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.business.RobotDesign;
import vn.edu.webpro.robotlab.business.DesignRuleSet;
import vn.edu.webpro.robotlab.business.KitItem;
import vn.edu.webpro.robotlab.data.DesignDB;
import vn.edu.webpro.robotlab.data.DesignRuleDB;
import vn.edu.webpro.robotlab.data.UserKitDB;
import vn.edu.webpro.robotlab.util.DesignException;
import vn.edu.webpro.robotlab.util.DesignFormUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;

/** Danh sách/form thiết kế riêng theo Model2; POST thành công dùng PRG. */
@WebServlet("/designs")
public class DesignPageServlet extends HttpServlet {
    /** Đọc trang, không tạo snapshot hoặc sửa thiết kế bằng GET. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        render(request, response, false);
    }

    /** Đọc form UTF-8 và token, chỉ chủ thiết kế được đổi/xóa. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        render(request, response, true);
    }

    private void render(HttpServletRequest request, HttpServletResponse response, boolean post)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        String url = "/WEB-INF/views/design-list.jsp";
        try {
            User user = SessionUtil.getCurrentUser(request);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/pages/tai-khoan.html");
                return;
            }
            if (post && !SessionUtil.hasValidFormCsrfToken(request, response)) {
                return;
            }
            String action = request.getParameter("action");
            if (!post && (action == null || action.isEmpty())) {
                request.setAttribute("designRecords", DesignDB.selectDesigns(user.getId()));
            } else {
                url = "/WEB-INF/views/design-form.jsp";
                DesignRuleSet rules = DesignRuleDB.selectPublished();
                List<KitItem> items = UserKitDB.selectItems(user.getId(), rules);
                request.setAttribute("designGoals", rules.getGoals());
                request.setAttribute("kitItems", items);
                RobotDesign draft = new RobotDesign();
                request.setAttribute("designDraft", draft);
                if ("rename".equals(action) || "delete".equals(action)) {
                    long id = DesignFormUtil.id(request.getParameter("id"));
                    draft = DesignDB.selectDesign(id, user.getId());
                    if (draft == null) {
                        throw new DesignException("NOT_FOUND", "Không tìm thấy thiết kế bạn được phép xem.");
                    }
                    request.setAttribute("designDraft", draft);
                    request.setAttribute("metadataAction", action);
                    if (post) {
                        if ("delete".equals(action) && !"yes".equals(request.getParameter("confirmDelete"))) {
                            throw new IllegalArgumentException(
                                    "Hãy xác nhận xóa thiết kế và nhật ký trước khi tiếp tục.");
                        }
                        long version = DesignFormUtil.id(request.getParameter("expectedLayoutVersion"));
                        if (version <= 0 || version > Integer.MAX_VALUE) {
                            throw new IllegalArgumentException("Phiên bản form không hợp lệ. Hãy tải lại.");
                        }
                        if ("rename".equals(action)) {
                            draft.setName(request.getParameter("name"));
                        }
                        DesignDB.changeMeta(id, user.getId(), draft.getName(), "delete".equals(action), (int) version);
                    }
                } else if ("new".equals(action) || "create".equals(action)) {
                    for (KitItem item : items) {
                        item.setInputValue("0");
                    }
                    if (post) {
                        draft.setName(request.getParameter("name"));
                        draft.setGoalCode(request.getParameter("goalCode"));
                        draft.setSource(request.getParameter("source"));
                        long createdDesignId = DesignDB.create(user.getId(), draft,
                                DesignFormUtil.quantities(request, items));
                        response.sendRedirect(request.getContextPath()
                                + "/pages/xuong-thiet-ke.html?design=" + createdDesignId);
                        return;
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Thao tác không hợp lệ. Hãy dùng liên kết hoặc form trên trang.");
                }
                if (post) {
                    response.sendRedirect(request.getContextPath() + "/designs?saved=1");
                    return;
                }
            }
        } catch (DesignException e) {
            response.setStatus("NOT_FOUND".equals(e.getCode()) ? 404 : 409);
            request.setAttribute("designError", e.getMessage());
        } catch (IllegalArgumentException e) {
            response.setStatus(422);
            request.setAttribute("designError", e.getMessage());
        } catch (SQLException e) {
            response.setStatus(503);
            request.setAttribute("designError", "Dữ liệu tạm chưa sẵn sàng. Vui lòng thử lại sau.");
        }
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
}
