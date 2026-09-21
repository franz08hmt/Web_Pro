package vn.edu.webpro.robotlab.model;

import vn.edu.webpro.robotlab.web.Json;

public record RobotComponent(String componentId, int quantity) {
    public String toJson() {
        return "{\"componentId\":" + Json.quote(componentId) + ",\"quantity\":" + quantity + "}";
    }
}
