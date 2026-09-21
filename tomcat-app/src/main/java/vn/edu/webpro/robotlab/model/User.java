package vn.edu.webpro.robotlab.model;

import vn.edu.webpro.robotlab.web.Json;

public record User(long id, String fullName, String email, String role) {
    public String toJson() {
        return "{\"id\":" + Json.quote(Long.toString(id)) + ",\"fullName\":" + Json.quote(fullName)
                + ",\"email\":" + Json.quote(email) + ",\"role\":" + Json.quote(role) + "}";
    }
}
