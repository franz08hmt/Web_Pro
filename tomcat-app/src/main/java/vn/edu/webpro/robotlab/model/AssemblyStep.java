package vn.edu.webpro.robotlab.model;

import vn.edu.webpro.robotlab.web.Json;

public record AssemblyStep(String id, String robotId, int stepOrder, String title,
                           String instruction, String illustrationJson) {
    public String toJson() {
        return "{\"id\":" + Json.quote(id) + ",\"robotId\":" + Json.quote(robotId)
                + ",\"stepOrder\":" + stepOrder + ",\"title\":" + Json.quote(title)
                + ",\"instruction\":" + Json.quote(instruction)
                + ",\"illustration\":" + Json.objectOrEmpty(illustrationJson) + "}";
    }
}
