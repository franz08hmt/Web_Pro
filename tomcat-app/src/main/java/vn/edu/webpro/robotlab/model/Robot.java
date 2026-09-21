package vn.edu.webpro.robotlab.model;

import vn.edu.webpro.robotlab.web.Json;

/** Public robot catalogue model. */
public record Robot(String id, String name, String level, String summary, String image,
                    String buildTime, String mainSensor, String skills, String wiringJson) {
    public String toJson() {
        return "{\"id\":" + Json.quote(id) + ",\"name\":" + Json.quote(name)
                + ",\"level\":" + Json.quote(level) + ",\"summary\":" + Json.quote(summary)
                + ",\"image\":" + Json.quote(image) + ",\"buildTime\":" + Json.quote(buildTime)
                + ",\"mainSensor\":" + Json.quote(mainSensor) + ",\"skills\":" + Json.quote(skills)
                + ",\"wiring\":" + Json.arrayOrEmpty(wiringJson) + "}";
    }
}
