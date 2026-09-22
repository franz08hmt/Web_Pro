package vn.edu.webpro.robotlab.model;

import vn.edu.webpro.robotlab.web.Json;

/** Tài nguyên thư viện; robotId để null nghĩa là dùng chung cho mọi robot. */
public record LibraryResource(
        String id,
        String robotId,
        String title,
        String type,
        String url,
        String description
) {
    public String toJson() {
        return "{\"id\":" + Json.quote(id)
                + ",\"robotId\":" + Json.quote(robotId)
                + ",\"title\":" + Json.quote(title)
                + ",\"type\":" + Json.quote(type)
                + ",\"url\":" + Json.quote(url)
                + ",\"description\":" + Json.quote(description) + "}";
    }
}
