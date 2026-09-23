package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Linh kiện trong danh mục — JavaBean theo Chapter 6 slide 6. */
public class Component implements Serializable {
    private String id;
    private String name;
    private String category;
    private String image;
    private String description;
    private String specsJson;

    public Component() {
        id = "";
        name = "";
        category = "";
        image = "";
        description = "";
        specsJson = "{}";
    }

    public Component(String id, String name, String category, String image,
                     String description, String specsJson) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.image = image;
        this.description = description;
        this.specsJson = specsJson;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** Thông số kỹ thuật lưu dạng object JSON trong cột components.specs. */
    public String getSpecsJson() {
        return specsJson;
    }

    public void setSpecsJson(String specsJson) {
        this.specsJson = specsJson;
    }

    public String toJson() {
        return "{\"id\":" + JsonUtil.quote(id)
                + ",\"name\":" + JsonUtil.quote(name)
                + ",\"category\":" + JsonUtil.quote(category)
                + ",\"image\":" + JsonUtil.quote(image)
                + ",\"description\":" + JsonUtil.quote(description)
                + ",\"specs\":" + JsonUtil.objectOrEmpty(specsJson) + "}";
    }
}
