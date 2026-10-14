package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/** Dữ kiện và quy tắc trình bày của AssemblyEvent. */
public class AssemblyEvent implements Serializable {
    private long id;
    private long sessionId;
    private long specId;
    private long instanceId;
    private long slotId;
    private long undoOfEventId;
    private String action = null;
    private String outcome = null;
    private String reasonCode = null;
    private String orientationCode = null;
    private String prevOrientationCode = null;
    private String instanceLabel = "";
    private String slotLabel = "";
    private int layoutVersionAfter;
    private int repeatCount;
    private Date createdAt = null;
    private boolean undone;

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public AssemblyEvent() {
    }

    /** Đọc dữ kiện id. */
    public long getId() {
        return id;
    }

    /** Ghi dữ kiện id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc dữ kiện sessionId. */
    public long getSessionId() {
        return sessionId;
    }

    /** Ghi dữ kiện sessionId. */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    /** Đọc dữ kiện specId. */
    public long getSpecId() {
        return specId;
    }

    /** Ghi dữ kiện specId. */
    public void setSpecId(long specId) {
        this.specId = specId;
    }

    /** Đọc dữ kiện instanceId. */
    public long getInstanceId() {
        return instanceId;
    }

    /** Ghi dữ kiện instanceId. */
    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    /** Đọc dữ kiện slotId. */
    public long getSlotId() {
        return slotId;
    }

    /** Ghi dữ kiện slotId. */
    public void setSlotId(long slotId) {
        this.slotId = slotId;
    }

    /** Đọc dữ kiện undoOfEventId. */
    public long getUndoOfEventId() {
        return undoOfEventId;
    }

    /** Ghi dữ kiện undoOfEventId. */
    public void setUndoOfEventId(long undoOfEventId) {
        this.undoOfEventId = undoOfEventId;
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

    /** Đọc dữ kiện layoutVersionAfter. */
    public int getLayoutVersionAfter() {
        return layoutVersionAfter;
    }

    /** Ghi dữ kiện layoutVersionAfter. */
    public void setLayoutVersionAfter(int layoutVersionAfter) {
        this.layoutVersionAfter = layoutVersionAfter;
    }

    /** Đọc dữ kiện repeatCount. */
    public int getRepeatCount() {
        return repeatCount;
    }

    /** Ghi dữ kiện repeatCount. */
    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    /** Đọc dữ kiện createdAt. */
    public Date getCreatedAt() {
        return createdAt;
    }

    /** Ghi dữ kiện createdAt. */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /** Đọc dữ kiện undone. */
    public boolean isUndone() {
        return undone;
    }

    /** Ghi dữ kiện undone. */
    public void setUndone(boolean undone) {
        this.undone = undone;
    }

    /** Chụp ý định và dữ kiện trước thao tác trong chính layout đã kiểm quyền. */
    public void capture(String intent, AssemblyLayout layout, long item, long slot, String orientation) {
        sessionId = layout.getSessionId();
        specId = layout.getSpec().getId();
        layoutVersionAfter = layout.getLayoutVersion();
        instanceId = item;
        slotId = slot;
        orientationCode = orientation;
        outcome = "ACCEPTED";
        createdAt = new Date();
        AssemblyPlacement old = layout.findPlacement(item);
        if ("attach".equals(intent)) {
            action = "ATTACH";
            prevOrientationCode = old == null ? null : old.getOrientationCode();
        } else if ("detach".equals(intent)) {
            action = "DETACH";
            if (old != null) {
                slotId = old.getSlotId();
                orientationCode = old.getOrientationCode();
            }
        } else if ("reset".equals(intent)) {
            action = "RESET";
            orientationCode = null;
        } else if ("complete".equals(intent)) {
            action = "COMPLETE";
            orientationCode = null;
        } else {
            action = "UNDO";
            orientationCode = null;
        }
    }

    /** Chỉ các thao tác thực sự được lưu mới là bước của quá trình. */
    public boolean isAccepted() {
        return "ACCEPTED".equals(outcome);
    }

    /** Trình tự hiện tại bỏ thao tác đã hoàn tác và dòng hoàn tác kỹ thuật. */
    public boolean isActiveStep() {
        return isAccepted() && !undone && !"UNDO".equals(action);
    }

    /** Có vật thể để hiển thị tên, tránh hiện mã nội bộ. */
    public boolean isHasInstance() {
        return instanceId > 0;
    }

    /** Nhãn thao tác cho người học. */
    public String getActionLabel() {
        if ("ATTACH".equals(action)) {
            return prevOrientationCode == null ? "Gắn vật thể" : "Đổi hướng";
        }
        if ("DETACH".equals(action)) {
            return "Tháo vật thể";
        }
        if ("RESET".equals(action)) {
            return "Đặt lại mô hình";
        }
        if ("COMPLETE".equals(action)) {
            return "Hoàn tất";
        }
        return "Hoàn tác";
    }

    /** Nhãn lỗi ngắn, không đưa mã kỹ thuật vào giao diện. */
    public String getReasonLabel() {
        if ("ORIENTATION_NOT_ALLOWED".equals(reasonCode)) {
            return "Sai hướng";
        }
        if ("WRONG_COMPONENT".equals(reasonCode)) {
            return "Sai loại linh kiện";
        }
        if ("SLOT_OCCUPIED".equals(reasonCode)) {
            return "Điểm gắn đã có vật thể";
        }
        if ("PARENT_MISSING".equals(reasonCode)) {
            return "Thiếu bộ phận cha";
        }
        if ("HAS_DEPENDENTS".equals(reasonCode)) {
            return "Còn bộ phận con";
        }
        if ("ALREADY_PLACED".equals(reasonCode)) {
            return "Vật thể đang ở điểm khác";
        }
        if ("LAYOUT_CONFLICT".equals(reasonCode)) {
            return "Mô hình thay đổi ở tab khác";
        }
        if ("NOTHING_TO_UNDO".equals(reasonCode)) {
            return "Không còn thao tác để hoàn tác";
        }
        return "Thao tác chưa hợp lệ";
    }

    /** Định dạng theo giờ Việt Nam, formatter riêng mỗi lần dùng. */
    public String getCreatedAtLabel() {
        if (createdAt == null) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return format.format(createdAt);
    }

    /** Góc hiển thị có đơn vị, không lộ mã hướng. */
    public String getOrientationLabel() {
        return orientationCode == null ? "—" : orientationCode.substring(1) + "°";
    }

    /** Đảo ngược gắn mới, đổi hướng và tháo; không đảo RESET hay UNDO. */
    public String getInverseAction() {
        if ("ATTACH".equals(action)) {
            return prevOrientationCode == null ? "detach" : "attach";
        }
        return "DETACH".equals(action) ? "attach" : "";
    }

    /** Hướng cần phục hồi từ sự kiện server đã lưu. */
    public String getInverseOrientation() {
        return "ATTACH".equals(action) ? prevOrientationCode : orientationCode;
    }
}
