package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện và quy tắc trình bày của DesignStep. */
public class DesignStep implements Serializable {
    private int number;
    private long instanceId;
    private String description = "";

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignStep() {
    }

    /** Đọc dữ kiện number. */
    public int getNumber() {
        return number;
    }

    /** Ghi dữ kiện number. */
    public void setNumber(int number) {
        this.number = number;
    }

    /** Đọc dữ kiện instanceId. */
    public long getInstanceId() {
        return instanceId;
    }

    /** Ghi dữ kiện instanceId. */
    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
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
