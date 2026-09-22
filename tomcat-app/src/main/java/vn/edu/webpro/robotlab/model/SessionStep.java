package vn.edu.webpro.robotlab.model;

import vn.edu.webpro.robotlab.web.Json;

/** Trạng thái hoàn thành của một bước lắp ráp: PENDING hoặc COMPLETED. */
public record SessionStep(String stepId, String status) {
    public String toJson() {
        return "{\"stepId\":" + Json.quote(stepId)
                + ",\"status\":" + Json.quote(status) + "}";
    }
}
