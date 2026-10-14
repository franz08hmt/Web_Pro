package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/** Dữ kiện và quy tắc trình bày của DesignEvent. */
public class DesignEvent implements Serializable {
    private long id;
    private long designId;
    private String action = "";
    private String outcome = "";
    private String reasonCode;
    private long instanceId;
    private long hostInstanceId;
    private String mountCode;
    private String orientationCode;
    private String prevOrientationCode;
    private long undoOfEventId;
    private int layoutVersionAfter;
    private Date createdAt;
    private String instanceLabel = "";
    private String mountLabel = "";
    private boolean undone;

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignEvent() {
    }

    /** Chụp dữ kiện trước thao tác từ snapshot server trong cùng transaction. */
    public void capture(String intent, DesignLayout layout, long item, String slotId, String orientation) {
        designId = layout.getDesign().getId();
        instanceId = item;
        outcome = "ACCEPTED";
        createdAt = new Date();
        layoutVersionAfter = layout.getDesign().getLayoutVersion();
        DesignSlot slot = layout.resolveSlot(slotId);
        if (slot != null) {
            hostInstanceId = slot.getHostInstanceId();
            mountCode = slot.getMountCode();
        }
        orientationCode = orientation;
        DesignPlacement old = layout.findPlacement(item);
        if ("attach".equals(intent)) {
            action = "ATTACH";
            prevOrientationCode = old == null ? null : old.getOrientationCode();
        } else if ("detach".equals(intent)) {
            action = "DETACH";
            if (old != null) {
                hostInstanceId = old.getHostInstanceId();
                mountCode = old.getMountCode();
                orientationCode = old.getOrientationCode();
            }
        } else {
            action = "undo".equals(intent) ? "UNDO" : "CHECK";
            if ("check".equals(intent)) {
                orientationCode = null;
            }
        }
    }

    /** Đọc dữ kiện id. */
    public long getId() {
        return id;
    }

    /** Ghi dữ kiện id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc dữ kiện designId. */
    public long getDesignId() {
        return designId;
    }

    /** Ghi dữ kiện designId. */
    public void setDesignId(long designId) {
        this.designId = designId;
    }

    /** Đọc dữ kiện action. */
    public String getAction() {
        return action;
    }

    /** Ghi dữ kiện action. */
    public void setAction(String action) {
        this.action = action;
    }

    /** Đọc dữ kiện outcome. */
    public String getOutcome() {
        return outcome;
    }

    /** Ghi dữ kiện outcome. */
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    /** Đọc dữ kiện reasonCode. */
    public String getReasonCode() {
        return reasonCode;
    }

    /** Ghi dữ kiện reasonCode. */
    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
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

    /** Đọc dữ kiện prevOrientationCode. */
    public String getPrevOrientationCode() {
        return prevOrientationCode;
    }

    /** Ghi dữ kiện prevOrientationCode. */
    public void setPrevOrientationCode(String prevOrientationCode) {
        this.prevOrientationCode = prevOrientationCode;
    }

    /** Đọc dữ kiện undoOfEventId. */
    public long getUndoOfEventId() {
        return undoOfEventId;
    }

    /** Ghi dữ kiện undoOfEventId. */
    public void setUndoOfEventId(long undoOfEventId) {
        this.undoOfEventId = undoOfEventId;
    }

    /** Đọc dữ kiện layoutVersionAfter. */
    public int getLayoutVersionAfter() {
        return layoutVersionAfter;
    }

    /** Ghi dữ kiện layoutVersionAfter. */
    public void setLayoutVersionAfter(int layoutVersionAfter) {
        this.layoutVersionAfter = layoutVersionAfter;
    }

    /** Đọc dữ kiện createdAt. */
    public Date getCreatedAt() {
        return createdAt;
    }

    /** Ghi dữ kiện createdAt. */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /** Đọc dữ kiện instanceLabel. */
    public String getInstanceLabel() {
        return instanceLabel;
    }

    /** Ghi dữ kiện instanceLabel. */
    public void setInstanceLabel(String instanceLabel) {
        this.instanceLabel = instanceLabel;
    }

    /** Đọc dữ kiện mountLabel. */
    public String getMountLabel() {
        return mountLabel;
    }

    /** Ghi dữ kiện mountLabel. */
    public void setMountLabel(String mountLabel) {
        this.mountLabel = mountLabel;
    }

    /** Đọc dữ kiện undone. */
    public boolean isUndone() {
        return undone;
    }

    /** Ghi dữ kiện undone. */
    public void setUndone(boolean undone) {
        this.undone = undone;
    }

    /** Đảo gắn mới, đổi hướng hoặc tháo; không có Redo. */
    public String getInverseAction() {
        if ("ATTACH".equals(action) && prevOrientationCode == null) {
            return "detach";
        }
        return "attach";
    }

    /** Hướng trước do server chụp, không do client tự khai. */
    public String getInverseOrientation() {
        return "ATTACH".equals(action) ? prevOrientationCode : orientationCode;
    }

    /** Thời gian Việt Nam, tạo formatter theo lần dùng. */
    public String getCreatedAtLabel() {
        if (createdAt == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return formatter.format(createdAt);
    }

    /** Nhãn sự kiện, không lộ enum cho người học. */
    public String getActionLabel() {
        if ("ATTACH".equals(action)) {
            return prevOrientationCode == null ? "Gắn vật thể" : "Đổi hướng";
        }
        if ("DETACH".equals(action)) {
            return "Tháo vật thể";
        }
        return "UNDO".equals(action) ? "Hoàn tác" : "Kiểm tra thiết kế";
    }

}
