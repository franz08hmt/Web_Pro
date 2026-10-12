package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Dữ kiện và luật lắp ráp mô phỏng do server xác thực. */
public class AssemblySpec implements Serializable {
    private long id;
    private String robotId = "";
    private int versionNo = 1;
    private String state = "PUBLISHED";
    private List<AssemblySlot> slots = new ArrayList<>();

    /** Tạo bean rỗng theo quy ước môn học. */
    public AssemblySpec() {
    }

    /** Đọc dữ kiện id. */
    public long getId() {
        return id;
    }

    /** Ghi dữ kiện id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc dữ kiện robotId. */
    public String getRobotId() {
        return robotId;
    }

    /** Ghi dữ kiện robotId. */
    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    /** Đọc dữ kiện versionNo. */
    public int getVersionNo() {
        return versionNo;
    }

    /** Ghi dữ kiện versionNo. */
    public void setVersionNo(int versionNo) {
        this.versionNo = versionNo;
    }

    /** Đọc dữ kiện state. */
    public String getState() {
        return state;
    }

    /** Ghi dữ kiện state. */
    public void setState(String state) {
        this.state = state;
    }

    /** Đọc dữ kiện slots. */
    public List<AssemblySlot> getSlots() {
        return slots;
    }

    /** Ghi dữ kiện slots. */
    public void setSlots(List<AssemblySlot> slots) {
        this.slots = slots;
    }

    /** Chỉ bản công bố được dùng cho phiên mới. */
    public boolean isPublished() {
        return "PUBLISHED".equals(state);
    }

    /** Kiểm dữ liệu cấu hình, tính duy nhất và chuỗi cha không có vòng. */
    public String validate() {
        if (robotId.isEmpty() || slots.isEmpty() || slots.size() > 40 || versionNo < 1) {
            return "Cấu hình cần robot và 1–40 điểm gắn.";
        }
        HashSet<String> codes = new HashSet<>();
        HashSet<Long> ids = new HashSet<>();
        int requiredCount = 0;
        for (AssemblySlot slot : slots) {
            if (!slot.isValid() || slot.getSpecId() != id
                    || !codes.add(slot.getSlotCode()) || !ids.add(slot.getId())) {
                return "Điểm gắn không hợp lệ hoặc trùng mã.";
            }
            if (slot.isRequired()) {
                requiredCount++;
            }
        }
        for (AssemblySlot slot : slots) {
            HashSet<Long> visited = new HashSet<>();
            AssemblySlot current = slot;
            while (current != null && current.getParentSlotId() > 0) {
                if (!visited.add(current.getId())) {
                    return "Chuỗi bộ phận cha có vòng lặp.";
                }
                current = findSlot(current.getParentSlotId());
                if (current == null) {
                    return "Bộ phận cha phải thuộc cùng cấu hình.";
                }
            }
        }
        return requiredCount > 0 ? "" : "Cần ít nhất một điểm gắn bắt buộc.";
    }

    /** Tra điểm gắn trong cùng cấu hình. */
    public AssemblySlot findSlot(long slotId) {
        for (AssemblySlot slot : slots) {
            if (slot.getId() == slotId) {
                return slot;
            }
        }
        return null;
    }

}
