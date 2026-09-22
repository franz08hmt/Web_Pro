package vn.edu.webpro.robotlab.model;

import vn.edu.webpro.robotlab.web.Json;

/** Java model mapped from one row in the components table. */
public record Component(
        String id,
        String name,
        String category,
        String image,
        String description,
        String specsJson
) {
    public String toJson() {
        return "{\"id\":" + Json.quote(id)
                + ",\"name\":" + Json.quote(name)
                + ",\"category\":" + Json.quote(category)
                + ",\"image\":" + Json.quote(image)
                + ",\"description\":" + Json.quote(description)
                + ",\"specs\":" + Json.objectOrEmpty(specsJson) + "}";
    }

    /* Getter JavaBean cho JSP: EL 3.0 không đọc được accessor của record.
       Xem giải thích đầy đủ trong User.java. */
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public String getSpecsJson() {
        return specsJson;
    }
}
