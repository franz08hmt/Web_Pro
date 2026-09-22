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

    /* Getter JavaBean cho JSP: EL 3.0 không đọc được accessor của record.
       Xem giải thích đầy đủ trong User.java. */
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }

    public String getSummary() {
        return summary;
    }

    public String getImage() {
        return image;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public String getMainSensor() {
        return mainSensor;
    }

    public String getSkills() {
        return skills;
    }

    public String getWiringJson() {
        return wiringJson;
    }
}
