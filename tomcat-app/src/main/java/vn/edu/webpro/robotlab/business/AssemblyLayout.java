package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

/** Dữ kiện và luật lắp ráp mô phỏng do server xác thực. */
public class AssemblyLayout implements Serializable {
    private boolean unchanged;
    private boolean transitioned;
    private long sessionId;
    private String mode = "DETAILED";
    private String status = "PREPARING";
    private int layoutVersion = 1;
    private AssemblySpec spec = new AssemblySpec();
    private List<AssemblyInstance> instances = new ArrayList<>();
    private List<AssemblyPlacement> placements = new ArrayList<>();

    /** Tạo bean rỗng theo quy ước môn học. */
    public AssemblyLayout() {
    }

    /** Retry đã có dữ liệu nên không ghi lại. */
    public boolean isUnchanged() {
        return unchanged;
    }

    /** Ghi dấu retry cho response, không dùng làm căn cứ xác thực. */
    public void setUnchanged(boolean unchanged) {
        this.unchanged = unchanged;
    }

    /** Chỉ true khi transaction này vừa hoàn tất phiên. */
    public boolean isTransitioned() {
        return transitioned;
    }

    /** Ghi kết quả transaction cho giao diện ăn mừng. */
    public void setTransitioned(boolean transitioned) {
        this.transitioned = transitioned;
    }

    /** Đọc dữ kiện sessionId. */
    public long getSessionId() {
        return sessionId;
    }

    /** Ghi dữ kiện sessionId. */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    /** Đọc dữ kiện mode. */
    public String getMode() {
        return mode;
    }

    /** Ghi dữ kiện mode. */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /** Đọc dữ kiện status. */
    public String getStatus() {
        return status;
    }

    /** Ghi dữ kiện status. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** Đọc dữ kiện layoutVersion. */
    public int getLayoutVersion() {
        return layoutVersion;
    }

    /** Ghi dữ kiện layoutVersion. */
    public void setLayoutVersion(int layoutVersion) {
        this.layoutVersion = layoutVersion;
    }

    /** Đọc dữ kiện spec. */
    public AssemblySpec getSpec() {
        return spec;
    }

    /** Ghi dữ kiện spec. */
    public void setSpec(AssemblySpec spec) {
        this.spec = spec;
    }

    /** Đọc dữ kiện instances. */
    public List<AssemblyInstance> getInstances() {
        return instances;
    }

    /** Ghi dữ kiện instances. */
    public void setInstances(List<AssemblyInstance> instances) {
        this.instances = instances;
    }

    /** Đọc dữ kiện placements. */
    public List<AssemblyPlacement> getPlacements() {
        return placements;
    }

    /** Ghi dữ kiện placements. */
    public void setPlacements(List<AssemblyPlacement> placements) {
        this.placements = placements;
    }

    /** Chỉ phiên chi tiết đang lắp được chỉnh sửa. */
    public boolean isCanEdit() {
        return "DETAILED".equals(mode) && "IN_PROGRESS".equals(status);
    }

    /** Reset trước hoàn tất giữ bộ kit, kể cả lúc chưa bắt đầu gắn vật thể. */
    public boolean isCanReset() {
        return "DETAILED".equals(mode) && ("PREPARING".equals(status)
                || "READY".equals(status) || "IN_PROGRESS".equals(status));
    }

    /** Tạo vật thể đúng số lượng từ bộ kit server, không dùng quantity client. */
    public void createInstances(List<RobotComponent> kit) {
        instances = new ArrayList<>();
        for (RobotComponent component : kit) {
            if (component.getQuantity() < 1 || component.getQuantity() > 40) {
                throw new IllegalArgumentException("Số lượng kit ngoài giới hạn.");
            }
            for (int n = 1; n <= component.getQuantity(); n++) {
                AssemblyInstance instance = new AssemblyInstance();
                instance.setSessionId(sessionId);
                instance.setComponentId(component.getComponentId());
                instance.setInstanceNo(n);
                instances.add(instance);
            }
        }
    }

    /** Kit và cấu hình phải có cùng số vật thể cho từng loại linh kiện. */
    public String validateKit() {
        for (AssemblySlot slot : spec.getSlots()) {
            int slotCount = 0;
            int itemCount = 0;
            for (AssemblySlot other : spec.getSlots()) {
                if (other.getComponentId().equals(slot.getComponentId())) {
                    slotCount++;
                }
            }
            for (AssemblyInstance item : instances) {
                if (item.getComponentId().equals(slot.getComponentId())) {
                    itemCount++;
                }
            }
            if (slotCount != itemCount) {
                return "KIT_SPEC_MISMATCH";
            }
        }
        return instances.size() == spec.getSlots().size() ? "" : "KIT_SPEC_MISMATCH";
    }

    /** Hoàn tất chỉ một lần; reset và hoàn tất đều kiểm trạng thái/version ở server. */
    public String validateChange(String action, int expectedVersion) {
        if ("complete".equals(action) && "COMPLETED".equals(status)) {
            return "UNCHANGED";
        }
        if ("reset".equals(action) ? !isCanReset() : !isCanEdit()) {
            return "SESSION_LOCKED";
        }
        if (layoutVersion != expectedVersion) {
            return "LAYOUT_CONFLICT";
        }
        if ("complete".equals(action)) {
            return check().isComplete() ? "" : "ASSEMBLY_INCOMPLETE";
        }
        return "reset".equals(action) ? "" : "VALIDATION_ERROR";
    }

    /** Tra vật thể của chính layout này. */
    public AssemblyInstance findInstance(long id) {
        for (AssemblyInstance instance : instances) {
            if (instance.getId() == id) {
                return instance;
            }
        }
        return null;
    }

    /** Tra vị trí của vật thể, không nhận cờ đã lắp từ client. */
    public AssemblyPlacement findPlacement(long instanceId) {
        for (AssemblyPlacement placement : placements) {
            if (placement.getInstanceId() == instanceId) {
                return placement;
            }
        }
        return null;
    }

    /** Tra vật thể hiện chiếm điểm gắn. */
    public AssemblyPlacement atSlot(long slotId) {
        for (AssemblyPlacement placement : placements) {
            if (placement.getSlotId() == slotId) {
                return placement;
            }
        }
        return null;
    }

    /** Hoàn tác vẫn kiểm chế độ, trạng thái, phiên bản và cùng luật gắn/tháo. */
    public String validateUndo(AssemblyEvent event, int expectedVersion) {
        if (!"DETAILED".equals(mode)) {
            return "GROUP_MODE";
        }
        if (!isCanEdit()) {
            return "SESSION_LOCKED";
        }
        if (expectedVersion != layoutVersion) {
            return "LAYOUT_CONFLICT";
        }
        if (event == null || !event.isAccepted() || event.isUndone() || event.getInverseAction().isEmpty()) {
            return "NOTHING_TO_UNDO";
        }
        if ("detach".equals(event.getInverseAction())) {
            return validateDetach(event.getInstanceId(), expectedVersion);
        }
        return validateAttach(event.getInstanceId(), event.getSlotId(), event.getInverseOrientation(), expectedVersion);
    }

    /** Kiểm toàn bộ ý định gắn; retry giống hệt không tạo thao tác mới. */
    public String validateAttach(long instanceId, long slotId, String orientation, int expectedVersion) {
        AssemblyInstance instance = findInstance(instanceId);
        AssemblySlot slot = spec.findSlot(slotId);
        if (instance == null || slot == null) {
            return "NOT_FOUND";
        }
        AssemblyPlacement old = findPlacement(instanceId);
        if (old != null && old.getSlotId() == slotId && old.getOrientationCode().equals(orientation)) {
            return "UNCHANGED";
        }
        if (!isCanEdit()) {
            return "SESSION_LOCKED";
        }
        if (expectedVersion != layoutVersion) {
            return "LAYOUT_CONFLICT";
        }
        if (old != null && old.getSlotId() != slotId) {
            return "ALREADY_PLACED";
        }
        if (!instance.getComponentId().equals(slot.getComponentId())) {
            return "WRONG_COMPONENT";
        }
        AssemblyPlacement occupied = atSlot(slotId);
        if (occupied != null && occupied.getInstanceId() != instanceId) {
            return "SLOT_OCCUPIED";
        }
        if (!slot.acceptsOrientation(orientation)) {
            return "ORIENTATION_NOT_ALLOWED";
        }
        if (slot.getParentSlotId() > 0 && !validAt(slot.getParentSlotId(), new HashSet<>())) {
            return "PARENT_MISSING";
        }
        return "";
    }

    /** Tháo không làm bộ phận con đang gắn bị treo. */
    public String validateDetach(long instanceId, int expectedVersion) {
        if (findInstance(instanceId) == null) {
            return "NOT_FOUND";
        }
        if (!isCanEdit()) {
            return "SESSION_LOCKED";
        }
        if (expectedVersion != layoutVersion) {
            return "LAYOUT_CONFLICT";
        }
        AssemblyPlacement placed = findPlacement(instanceId);
        if (placed == null) {
            return "UNCHANGED";
        }
        for (AssemblySlot child : spec.getSlots()) {
            if (child.getParentSlotId() == placed.getSlotId() && atSlot(child.getId()) != null) {
                return "HAS_DEPENDENTS";
            }
        }
        return "";
    }

    private boolean validAt(long slotId, HashSet<Long> visited) {
        long current = slotId;
        while (current > 0) {
            if (!visited.add(current)) {
                return false;
            }
            AssemblySlot slot = spec.findSlot(current);
            AssemblyPlacement placed = atSlot(current);
            if (slot == null || placed == null) {
                return false;
            }
            AssemblyInstance instance = findInstance(placed.getInstanceId());
            if (instance == null || !instance.getComponentId().equals(slot.getComponentId())
                    || !slot.acceptsOrientation(placed.getOrientationCode())) {
                return false;
            }
            current = slot.getParentSlotId();
        }
        return true;
    }

    /** Đếm X/N điểm bắt buộc và giải thích thiếu/sai/phụ thuộc từ layout thật. */
    public AssemblyCheckReport check() {
        AssemblyCheckReport report = new AssemblyCheckReport();
        for (AssemblySlot slot : spec.getSlots()) {
            if (slot.isRequired()) {
                report.setRequiredCount(report.getRequiredCount() + 1);
                if (validAt(slot.getId(), new HashSet<>())) {
                    report.setCorrectCount(report.getCorrectCount() + 1);
                } else if (atSlot(slot.getId()) == null) {
                    report.getMissingSlots().add(slot.getLabel());
                } else {
                    report.getErrors().add("Kiểm tra hướng, linh kiện và bộ phận cha tại " + slot.getLabel());
                }
            } else if (atSlot(slot.getId()) != null && !validAt(slot.getId(), new HashSet<>())) {
                report.getErrors().add("Bộ phận tùy chọn chưa hợp lệ: " + slot.getLabel());
            }
        }
        for (AssemblyInstance instance : instances) {
            if (findPlacement(instance.getId()) == null) {
                report.getTrayLabels().add(instance.getLabel());
            }
        }
        return report;
    }

    /** Lý do cụ thể cho người học; không suy ra nguy cơ điện. */
    public String messageFor(String code, long instanceId, long slotId) {
        if (!"WRONG_COMPONENT".equals(code) && !"SLOT_OCCUPIED".equals(code)
                && !"ORIENTATION_NOT_ALLOWED".equals(code) && !"PARENT_MISSING".equals(code)
                && !"ALREADY_PLACED".equals(code) && !"HAS_DEPENDENTS".equals(code)) {
            return messageFor(code);
        }
        AssemblyInstance item = findInstance(instanceId);
        AssemblySlot slot = spec.findSlot(slotId);
        if (item == null || !hasLabel(item.getLabel())) {
            return messageFor(code);
        }
        String name = item.getLabel();
        if ("HAS_DEPENDENTS".equals(code)) {
            AssemblyPlacement placed = findPlacement(instanceId);
            if (placed != null) {
                for (AssemblySlot child : spec.getSlots()) {
                    AssemblyPlacement childPlaced = atSlot(child.getId());
                    if (child.getParentSlotId() == placed.getSlotId() && childPlaced != null) {
                        AssemblyInstance dependent = findInstance(childPlaced.getInstanceId());
                        if (dependent != null && hasLabel(dependent.getLabel())) {
                            return "Cần tháo " + dependent.getLabel() + " trước khi tháo " + name + ".";
                        }
                    }
                }
            }
            return messageFor(code);
        }
        if (slot == null || !hasLabel(slot.getLabel())) {
            return messageFor(code);
        }
        String target = slot.getLabel();
        if ("WRONG_COMPONENT".equals(code)) {
            return "Không thể gắn " + name + " vào " + target + ": điểm này dành cho loại bộ phận khác.";
        }
        if ("ORIENTATION_NOT_ALLOWED".equals(code)) {
            return name + " chưa đúng hướng tại " + target + ". Hãy xoay rồi thử lại.";
        }
        if ("SLOT_OCCUPIED".equals(code)) {
            AssemblyPlacement placed = atSlot(slotId);
            AssemblyInstance occupant = placed == null ? null : findInstance(placed.getInstanceId());
            if (occupant != null && hasLabel(occupant.getLabel())) {
                return target + " đã có " + occupant.getLabel() + ". Hãy tháo vật thể đó trước.";
            }
        } else if ("PARENT_MISSING".equals(code)) {
            AssemblySlot parent = spec.findSlot(slot.getParentSlotId());
            if (parent != null && hasLabel(parent.getLabel())) {
                return "Cần gắn " + parent.getLabel() + " trước khi gắn " + name + ".";
            }
        } else if ("ALREADY_PLACED".equals(code)) {
            AssemblyPlacement placed = findPlacement(instanceId);
            AssemblySlot old = placed == null ? null : spec.findSlot(placed.getSlotId());
            if (old != null && hasLabel(old.getLabel())) {
                return name + " đang ở " + old.getLabel() + ". Hãy tháo trước khi chuyển.";
            }
        }
        return messageFor(code);
    }

    private boolean hasLabel(String label) {
        return label != null && !label.trim().isEmpty();
    }

    /** Câu dự phòng không tiết lộ tên cho lỗi quyền hoặc trạng thái. */
    public String messageFor(String code) {
        if ("WRONG_COMPONENT".equals(code)) {
            return "Linh kiện không phù hợp với điểm gắn đã chọn. Hãy chọn đúng loại bộ phận.";
        }
        if ("SLOT_OCCUPIED".equals(code)) {
            return "Điểm gắn này đã có vật thể khác. Hãy tháo vật thể đó trước.";
        }
        if ("ORIENTATION_NOT_ALLOWED".equals(code)) {
            return "Hướng này không được chấp nhận tại điểm gắn; hãy đối chiếu hướng được hỗ trợ.";
        }
        if ("PARENT_MISSING".equals(code)) {
            return "Cần gắn bộ phận cha trước, chẳng hạn động cơ trước bánh xe.";
        }
        if ("ALREADY_PLACED".equals(code)) {
            return "Vật thể đang ở vị trí khác; hãy tháo trước khi chuyển điểm gắn.";
        }
        if ("HAS_DEPENDENTS".equals(code)) {
            return "Cần tháo bộ phận con trước khi tháo bộ phận cha, chẳng hạn bánh trước động cơ.";
        }
        if ("LAYOUT_CONFLICT".equals(code)) {
            return "Mô hình đã thay đổi ở tab khác, hãy tải lại trước khi tiếp tục.";
        }
        if ("SESSION_LOCKED".equals(code)) {
            return "Phiên hiện không sửa được. Nếu đã hoàn tất, tạo lượt mới để lắp lại.";
        }
        if ("GROUP_MODE".equals(code)) {
            return "Phiên này dùng cách lắp theo nhóm linh kiện, không dùng điểm gắn riêng.";
        }
        if ("NOTHING_TO_UNDO".equals(code)) {
            return "Không còn thao tác để hoàn tác sau lần đặt lại gần nhất.";
        }
        return "Không tìm thấy vật thể hoặc điểm gắn trong phiên này.";
    }

}
