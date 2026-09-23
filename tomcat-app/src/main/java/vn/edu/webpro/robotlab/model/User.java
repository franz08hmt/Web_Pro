package vn.edu.webpro.robotlab.model;

import vn.edu.webpro.robotlab.web.Json;

public record User(long id, String fullName, String email, String role, String createdAt, int sessionVersion) {
    public String toJson() {
        return "{\"id\":" + Json.quote(Long.toString(id)) + ",\"fullName\":" + Json.quote(fullName)
                + ",\"email\":" + Json.quote(email) + ",\"role\":" + Json.quote(role)
                + ",\"createdAt\":" + Json.quote(createdAt) + "}";
    }

    /* EL 3.0 của Tomcat 9 đọc thuộc tính qua BeanELResolver, tức là chỉ nhận
       getter kiểu JavaBean. Record chỉ sinh accessor fullName(), nên nếu thiếu
       các getter dưới đây thì ${user.fullName} trong JSP ném
       PropertyNotFoundException và trang trả HTTP 500. */
    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getSessionVersion() {
        return sessionVersion;
    }
}
