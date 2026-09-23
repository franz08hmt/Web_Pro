package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.Set;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Tài nguyên thư viện; robotId rỗng (null) nghĩa là dùng chung cho mọi robot. */
public class LibraryResource implements Serializable {
    private static final Set<String> TYPES = Set.of("image", "document", "link");

    private String id;
    private String robotId;
    private String title;
    private String type;
    private String url;
    private String description;

    public LibraryResource() {
        id = "";
        title = "";
        type = "link";
        url = "";
        description = "";
    }

    public LibraryResource(String id, String robotId, String title, String type,
                           String url, String description) {
        this.id = id;
        this.robotId = robotId;
        this.title = title;
        this.type = type;
        this.url = url;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static boolean isValidType(String type) {
        return TYPES.contains(type);
    }

    public String toJson() {
        return "{\"id\":" + JsonUtil.quote(id)
                + ",\"robotId\":" + JsonUtil.quote(robotId)
                + ",\"title\":" + JsonUtil.quote(title)
                + ",\"type\":" + JsonUtil.quote(type)
                + ",\"url\":" + JsonUtil.quote(url)
                + ",\"description\":" + JsonUtil.quote(description) + "}";
    }
}
