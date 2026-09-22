package vn.edu.webpro.robotlab.model;

import vn.edu.webpro.robotlab.web.Json;

/** Trạng thái chuẩn bị của một linh kiện trong một phiên lắp ráp. */
public record SessionComponent(String componentId, boolean isPrepared) {
    public String toJson() {
        return "{\"componentId\":" + Json.quote(componentId)
                + ",\"isPrepared\":" + isPrepared + "}";
    }
}
