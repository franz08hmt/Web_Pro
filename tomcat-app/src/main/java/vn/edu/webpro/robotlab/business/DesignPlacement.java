package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.Date;

/** Dữ kiện và quy tắc trình bày của DesignPlacement. */
public class DesignPlacement implements Serializable {
    private long instanceId;
    private long hostInstanceId;
    private String mountCode = "base";
    private String orientationCode = "Y0";
    private Date placedAt;

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignPlacement() {
    }

    /** Đọc dữ kiện instanceId. */
    public long getInstanceId() {
        return instanceId;
    }

    /** Ghi dữ kiện instanceId. */
    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    /** Đọc dữ kiện hostInstanceId. */
    public long getHostInstanceId() {
        return hostInstanceId;
    }

    /** Ghi dữ kiện hostInstanceId. */
    public void setHostInstanceId(long hostInstanceId) {
        this.hostInstanceId = hostInstanceId;
    }

    /** Đọc dữ kiện mountCode. */
    public String getMountCode() {
        return mountCode;
    }

    /** Ghi dữ kiện mountCode. */
    public void setMountCode(String mountCode) {
        this.mountCode = mountCode;
    }

    /** Đọc dữ kiện orientationCode. */
    public String getOrientationCode() {
        return orientationCode;
    }

    /** Ghi dữ kiện orientationCode. */
    public void setOrientationCode(String orientationCode) {
        this.orientationCode = orientationCode;
    }

    /** Đọc dữ kiện placedAt. */
    public Date getPlacedAt() {
        return placedAt;
    }

    /** Ghi dữ kiện placedAt. */
    public void setPlacedAt(Date placedAt) {
        this.placedAt = placedAt;
    }

    /** Khóa giao diện ổn định; không phải ID điểm gắn của phòng mẫu. */
    public String getSlotId() {
        return hostInstanceId + ":" + mountCode;
    }

}
