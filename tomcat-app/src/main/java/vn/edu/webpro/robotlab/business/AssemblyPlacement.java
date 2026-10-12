package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Dữ kiện và luật lắp ráp mô phỏng do server xác thực. */
public class AssemblyPlacement implements Serializable {
    /** Góc tương đối dễ đọc trên phiếu, giữ nguyên mã nguồn dữ kiện. */
    public String getOrientationDisplay() {
        if (orientationCode != null && orientationCode.matches("Y(0|90|180|270)")) {
            return orientationCode.substring(1) + "°";
        }
        return "Chưa xác định";
    }
    private long instanceId;
    private long sessionId;
    private long slotId;
    private String orientationCode = "";
    private String placedAt = "";
    private String instanceLabel = "";
    private String slotLabel = "";

    /** Tạo bean rỗng theo quy ước môn học. */
    public AssemblyPlacement() {
    }

    /** Đọc dữ kiện instanceId. */
    public long getInstanceId() {
        return instanceId;
    }

    /** Ghi dữ kiện instanceId. */
    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    /** Đọc dữ kiện sessionId. */
    public long getSessionId() {
        return sessionId;
    }

    /** Ghi dữ kiện sessionId. */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    /** Đọc dữ kiện slotId. */
    public long getSlotId() {
        return slotId;
    }

    /** Ghi dữ kiện slotId. */
    public void setSlotId(long slotId) {
        this.slotId = slotId;
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
    public String getPlacedAt() {
        return placedAt;
    }

    /** Ghi dữ kiện placedAt. */
    public void setPlacedAt(String placedAt) {
        this.placedAt = placedAt;
    }

    /** Đọc dữ kiện instanceLabel. */
    public String getInstanceLabel() {
        return instanceLabel;
    }

    /** Ghi dữ kiện instanceLabel. */
    public void setInstanceLabel(String instanceLabel) {
        this.instanceLabel = instanceLabel;
    }

    /** Đọc dữ kiện slotLabel. */
    public String getSlotLabel() {
        return slotLabel;
    }

    /** Ghi dữ kiện slotLabel. */
    public void setSlotLabel(String slotLabel) {
        this.slotLabel = slotLabel;
    }

}
