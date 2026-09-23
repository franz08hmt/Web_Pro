package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.Set;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Mô hình robot trong danh mục — JavaBean theo Chapter 6 slide 6. */
public class Robot implements Serializable {
    private static final Set<String> LEVELS = Set.of("Cơ bản", "Trung bình", "Nâng cao");

    private String id;
    private String name;
    private String level;
    private String summary;
    private String image;
    private String buildTime;
    private String mainSensor;
    private String skills;
    private String wiringJson;

    public Robot() {
        id = "";
        name = "";
        level = "";
        summary = "";
        image = "";
        buildTime = "";
        mainSensor = "";
        skills = "";
        wiringJson = "[]";
    }

    public Robot(String id, String name, String level, String summary, String image,
                 String buildTime, String mainSensor, String skills, String wiringJson) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.summary = summary;
        this.image = image;
        this.buildTime = buildTime;
        this.mainSensor = mainSensor;
        this.skills = skills;
        this.wiringJson = wiringJson;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    public String getMainSensor() {
        return mainSensor;
    }

    public void setMainSensor(String mainSensor) {
        this.mainSensor = mainSensor;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    /** Sơ đồ nối dây lưu dạng mảng JSON trong cột robots.wiring. */
    public String getWiringJson() {
        return wiringJson;
    }

    public void setWiringJson(String wiringJson) {
        this.wiringJson = wiringJson;
    }

    public static boolean isValidLevel(String level) {
        return LEVELS.contains(level);
    }

    public String toJson() {
        return "{\"id\":" + JsonUtil.quote(id)
                + ",\"name\":" + JsonUtil.quote(name)
                + ",\"level\":" + JsonUtil.quote(level)
                + ",\"summary\":" + JsonUtil.quote(summary)
                + ",\"image\":" + JsonUtil.quote(image)
                + ",\"buildTime\":" + JsonUtil.quote(buildTime)
                + ",\"mainSensor\":" + JsonUtil.quote(mainSensor)
                + ",\"skills\":" + JsonUtil.quote(skills)
                + ",\"wiring\":" + JsonUtil.arrayOrEmpty(wiringJson) + "}";
    }
}
