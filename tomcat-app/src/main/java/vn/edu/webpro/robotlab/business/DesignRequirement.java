package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện và quy tắc trình bày của DesignRequirement. */
public class DesignRequirement implements Serializable {
    private String goalCode = "";
    private String role = "";
    private int minCount;
    private String message = "";

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignRequirement() {
    }

    /** Đọc dữ kiện goalCode. */
    public String getGoalCode() {
        return goalCode;
    }

    /** Ghi dữ kiện goalCode. */
    public void setGoalCode(String goalCode) {
        this.goalCode = goalCode;
    }

    /** Đọc dữ kiện role. */
    public String getRole() {
        return role;
    }

    /** Ghi dữ kiện role. */
    public void setRole(String role) {
        this.role = role;
    }

    /** Đọc dữ kiện minCount. */
    public int getMinCount() {
        return minCount;
    }

    /** Ghi dữ kiện minCount. */
    public void setMinCount(int minCount) {
        this.minCount = minCount;
    }

    /** Đọc dữ kiện message. */
    public String getMessage() {
        return message;
    }

    /** Ghi dữ kiện message. */
    public void setMessage(String message) {
        this.message = message;
    }


}
