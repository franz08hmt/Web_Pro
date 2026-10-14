package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện và quy tắc trình bày của DesignGoal. */
public class DesignGoal implements Serializable {
    private String code = "";
    private String name = "";
    private String description = "";

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignGoal() {
    }

    /** Đọc dữ kiện code. */
    public String getCode() {
        return code;
    }

    /** Ghi dữ kiện code. */
    public void setCode(String code) {
        this.code = code;
    }

    /** Đọc dữ kiện name. */
    public String getName() {
        return name;
    }

    /** Ghi dữ kiện name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Đọc dữ kiện description. */
    public String getDescription() {
        return description;
    }

    /** Ghi dữ kiện description. */
    public void setDescription(String description) {
        this.description = description;
    }


}
