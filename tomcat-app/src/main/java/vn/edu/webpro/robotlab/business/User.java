package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import vn.edu.webpro.robotlab.util.JsonUtil;

/**
 * Tài khoản người dùng — JavaBean theo Chapter 6 slide 6: constructor rỗng,
 * get/set cho mọi thuộc tính private và implements Serializable.
 *
 * Serializable cần thiết vì object này được lưu trong HttpSession
 * (session.setAttribute("user", user)). Không có thuộc tính mật khẩu: hash chỉ
 * được UserDB đọc riêng khi đăng nhập, nên không thể lọt ra JSON hay JSP.
 */
public class User implements Serializable {
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    private long id;
    private String fullName;
    private String email;
    private String role;
    private String createdAt;
    private int sessionVersion;

    public User() {
        fullName = "";
        email = "";
        role = ROLE_USER;
        createdAt = "";
    }

    public User(long id, String fullName, String email, String role, String createdAt, int sessionVersion) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.sessionVersion = sessionVersion;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /** Tăng mỗi khi đổi mật khẩu hoặc đổi quyền; phiên cũ mang số nhỏ hơn sẽ hết hiệu lực. */
    public int getSessionVersion() {
        return sessionVersion;
    }

    public void setSessionVersion(int sessionVersion) {
        this.sessionVersion = sessionVersion;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    public static boolean isValidRole(String role) {
        return ROLE_USER.equals(role) || ROLE_ADMIN.equals(role);
    }

    /** Chỉ quản trị viên được đổi quyền, và không được tự đổi quyền của chính mình. */
    public boolean canChangeRoleOf(User target) {
        return isAdmin() && target != null && target.getId() != id;
    }

    /** sessionVersion là thông tin nội bộ nên không đưa ra JSON. */
    public String toJson() {
        return "{\"id\":" + JsonUtil.quote(Long.toString(id))
                + ",\"fullName\":" + JsonUtil.quote(fullName)
                + ",\"email\":" + JsonUtil.quote(email)
                + ",\"role\":" + JsonUtil.quote(role)
                + ",\"createdAt\":" + JsonUtil.quote(createdAt) + "}";
    }
}
