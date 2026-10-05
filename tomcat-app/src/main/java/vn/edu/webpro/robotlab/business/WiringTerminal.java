package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.*;
import java.math.*;
import java.text.*;

/** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
public class WiringTerminal implements Serializable {
    private long id = 0;
    private String code = "";
    private String deviceCode = "";
    private String deviceLabel = "";
    private String pinLabel = "";
    private int displayOrder = 0;
    private int x = 0;
    private int y = 0;

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public WiringTerminal() {
    }

    /** Đọc id. */
    public long getId() {
        return id;
    }

    /** Gán id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc code. */
    public String getCode() {
        return code;
    }

    /** Gán code. */
    public void setCode(String code) {
        this.code = code;
    }

    /** Đọc deviceCode. */
    public String getDeviceCode() {
        return deviceCode;
    }

    /** Gán deviceCode. */
    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    /** Đọc deviceLabel. */
    public String getDeviceLabel() {
        return deviceLabel;
    }

    /** Gán deviceLabel. */
    public void setDeviceLabel(String deviceLabel) {
        this.deviceLabel = deviceLabel;
    }

    /** Đọc pinLabel. */
    public String getPinLabel() {
        return pinLabel;
    }

    /** Gán pinLabel. */
    public void setPinLabel(String pinLabel) {
        this.pinLabel = pinLabel;
    }

    /** Đọc displayOrder. */
    public int getDisplayOrder() {
        return displayOrder;
    }

    /** Gán displayOrder. */
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    /** Đọc x. */
    public int getX() {
        return x;
    }

    /** Gán x. */
    public void setX(int x) {
        this.x = x;
    }

    /** Đọc y. */
    public int getY() {
        return y;
    }

    /** Gán y. */
    public void setY(int y) {
        this.y = y;
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public String getLabel() {
        return deviceLabel + " · " + pinLabel + " [" + code + "]";
    }

    /** Nhãn ở cạnh phải được kéo về bên trái để không bị cắt khỏi viewBox. */
    public int getLabelX() {
        if (x > 700) {
            return x - 32;
        }
        return x + 32;
    }

    /** Hướng căn chữ chỉ là hằng an toàn, không ghép style từ dữ liệu người dùng. */
    public String getLabelAnchor() {
        if (x > 700) {
            return "end";
        }
        return "start";
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public void validate() {
        if (!code.matches("[A-Za-z0-9_.-]{1,64}") || !deviceCode.matches("[A-Za-z0-9_.-]{1,64}")) {
            throw new IllegalArgumentException("Mã đầu nối/thiết bị chỉ gồm chữ ASCII, số, dấu . _ -.");
        }
        if (deviceLabel.trim().isEmpty() || deviceLabel.length() > 100
                || pinLabel.trim().isEmpty() || pinLabel.length() > 50
                || x < 40 || x > 960 || y < 40 || y > 960 || displayOrder < 0 || displayOrder > 40) {
            throw new IllegalArgumentException("Nhãn, thứ tự hoặc tọa độ đầu nối ngoài giới hạn.");
        }
    }

}
