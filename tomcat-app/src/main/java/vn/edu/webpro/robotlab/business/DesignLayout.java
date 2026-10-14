package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** Luật lắp ghép và tọa độ server của thiết kế riêng, không phải phiên mẫu. */
public class DesignLayout implements Serializable {
    private RobotDesign design = new RobotDesign();
    private DesignRuleSet rules = new DesignRuleSet();
    private List<DesignInstance> instances = new ArrayList<>();
    private List<DesignPlacement> placements = new ArrayList<>();
    private boolean unchanged;

    /** Constructor rỗng theo JavaBean. */
    public DesignLayout() {
    }

    /** Header riêng, không có trạng thái COMPLETED của mẫu. */
    public RobotDesign getDesign() {
        return design;
    }

    /** Ghi header đã kiểm owner. */
    public void setDesign(RobotDesign design) {
        this.design = design;
    }

    /** Phiên bản luật cố định của thiết kế. */
    public DesignRuleSet getRules() {
        return rules;
    }

    /** Ghi luật đã đọc từ FK rule_set_id. */
    public void setRules(DesignRuleSet rules) {
        this.rules = rules;
    }

    /** Snapshot các vật thể có ID riêng. */
    public List<DesignInstance> getInstances() {
        return instances;
    }

    /** Ghi snapshot JDBC. */
    public void setInstances(List<DesignInstance> instances) {
        this.instances = instances;
    }

    /** Các placement đang có. */
    public List<DesignPlacement> getPlacements() {
        return placements;
    }

    /** Ghi bố cục đã lưu. */
    public void setPlacements(List<DesignPlacement> placements) {
        this.placements = placements;
    }

    /** Retry không sinh sự kiện hoặc tăng version. */
    public boolean isUnchanged() {
        return unchanged;
    }

    /** Ghi nhãn phản hồi, không nhận từ client. */
    public void setUnchanged(boolean unchanged) {
        this.unchanged = unchanged;
    }

    /** Chỉ tìm trong snapshot của thiết kế đã kiểm quyền. */
    public DesignInstance findInstance(long id) {
        for (DesignInstance item : instances) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    /** Vật thể có tối đa một placement. */
    public DesignPlacement findPlacement(long id) {
        for (DesignPlacement placed : placements) {
            if (placed.getInstanceId() == id) {
                return placed;
            }
        }
        return null;
    }

    /** Luật gắn theo thứ tự đã chốt; client chỉ gửi ý định. */
    public String validateAttach(long instanceId, String slotId, String orientation, int version) {
        DesignInstance item = findInstance(instanceId);
        DesignSlot slot = resolveSlot(slotId);
        if (item == null || slot == null || slot.getHostInstanceId() == instanceId) {
            return "NOT_FOUND";
        }
        DesignPlacement old = findPlacement(instanceId);
        if (old != null && old.getSlotId().equals(slotId) && old.getOrientationCode().equals(orientation)) {
            return "UNCHANGED";
        }
        if (!design.isCanEdit()) {
            return "LOCKED";
        }
        if (version != design.getLayoutVersion()) {
            return "LAYOUT_CONFLICT";
        }
        if (old != null && !old.getSlotId().equals(slotId)) {
            return "ALREADY_PLACED";
        }
        if (rules.roleFor(item.getComponentId()) == null) {
            return "NOT_SUPPORTED";
        }
        if (!rules.fitsType(item.getComponentId(), slot.getMountType())) {
            return "MOUNT_INCOMPATIBLE";
        }
        for (DesignPlacement placed : placements) {
            if (placed.getSlotId().equals(slotId) && placed.getInstanceId() != instanceId) {
                return "MOUNT_OCCUPIED";
            }
        }
        if (!rules.fitsOrientation(item.getComponentId(), slot.getMountType(), orientation)) {
            return "ORIENTATION_NOT_ALLOWED";
        }
        return slot.isHostAvailable() ? "" : "HOST_MISSING";
    }

    /** Không để bộ phận con treo sau khi tháo host. */
    public String validateDetach(long instanceId, int version) {
        if (findInstance(instanceId) == null) {
            return "NOT_FOUND";
        }
        if (!design.isCanEdit()) {
            return "LOCKED";
        }
        if (version != design.getLayoutVersion()) {
            return "LAYOUT_CONFLICT";
        }
        if (findPlacement(instanceId) == null) {
            return "UNCHANGED";
        }
        for (DesignPlacement placed : placements) {
            if (placed.getHostInstanceId() == instanceId) {
                return "HAS_DEPENDENTS";
            }
        }
        return "";
    }

    /** Undo dùng lại luật gắn/tháo, không bỏ qua cha hoặc version. */
    public String validateUndo(DesignEvent event, int version) {
        if (!design.isCanEdit()) {
            return "LOCKED";
        }
        if (version != design.getLayoutVersion()) {
            return "LAYOUT_CONFLICT";
        }
        if (event == null) {
            return "NOTHING_TO_UNDO";
        }
        if ("detach".equals(event.getInverseAction())) {
            return validateDetach(event.getInstanceId(), version);
        }
        return validateAttach(event.getInstanceId(), event.getHostInstanceId() + ":" + event.getMountCode(),
                event.getInverseOrientation(), version);
    }

    /** Chỉ root và các điểm do host đã gắn cung cấp được đưa lên view. */
    public List<DesignSlot> getSlots() {
        List<DesignSlot> slots = new ArrayList<>();
        slots.add(resolveSlot("0:base"));
        for (DesignInstance host : instances) {
            if (poseOf(host.getId()) == null) {
                continue;
            }
            for (DesignMount mount : rules.getMounts()) {
                if (mount.getComponentId().equals(host.getComponentId())) {
                    slots.add(resolveSlot(host.getId() + ":" + mount.getMountCode()));
                }
            }
        }
        return slots;
    }

    /** Phân giải host:mount bằng roster riêng; không truy vấn thiết kế khác. */
    public DesignSlot resolveSlot(String id) {
        return resolveSlot(id, new HashSet<Long>());
    }

    /** Pose tuyệt đối do Java tính, không lưu tọa độ tự khai của trình duyệt. */
    public DesignSlot poseOf(long instanceId) {
        return poseOf(instanceId, new HashSet<Long>());
    }

    private DesignSlot poseOf(long id, HashSet<Long> visited) {
        if (!visited.add(id)) {
            return null;
        }
        DesignPlacement placed = findPlacement(id);
        if (placed == null) {
            return null;
        }
        DesignSlot slot = resolveSlot(placed.getSlotId(), visited);
        if (slot == null || !slot.isHostAvailable()) {
            return null;
        }
        double angle = angle(placed.getOrientationCode());
        slot.setRy(slot.getRy() + Math.toRadians(angle));
        slot.setLogicalYaw((slot.getLogicalYaw() + angle) % 360);
        return slot;
    }

    private DesignSlot resolveSlot(String id, HashSet<Long> visited) {
        if (id == null || !id.matches("[0-9]{1,18}:[a-z0-9-]{1,64}")) {
            return null;
        }
        String[] parts = id.split(":");
        long hostId = Long.parseLong(parts[0]);
        DesignSlot slot = new DesignSlot();
        slot.setId(id);
        slot.setHostInstanceId(hostId);
        slot.setMountCode(parts[1]);
        if (hostId == 0) {
            if (!"base".equals(parts[1])) {
                return null;
            }
            slot.setMountType("ROOT");
            slot.setLabel("Vị trí khung nền");
            slot.setY(.42);
            slot.setHostAvailable(true);
        } else {
            DesignInstance host = findInstance(hostId);
            if (host == null) {
                return null;
            }
            DesignMount mount = rules.mountFor(host.getComponentId(), parts[1]);
            if (mount == null) {
                return null;
            }
            slot.setHostComponentId(host.getComponentId());
            slot.setMountType(mount.getMountType());
            slot.setDisplayOrder(mount.getDisplayOrder());
            slot.setLabel(mount.getLabel() + " của " + host.getLabel());
            DesignSlot pose = poseOf(hostId, visited);
            slot.setHostAvailable(pose != null);
            if (pose != null) {
                double angle = Math.toRadians(pose.getLogicalYaw());
                slot.setX(pose.getX() + Math.cos(angle) * mount.getX() + Math.sin(angle) * mount.getZ());
                slot.setZ(pose.getZ() - Math.sin(angle) * mount.getX() + Math.cos(angle) * mount.getZ());
                slot.setY(pose.getY() + mount.getY());
                slot.setRx(mount.getRx());
                slot.setRy(pose.getRy() + mount.getRy());
                slot.setRz(mount.getRz());
                slot.setLogicalYaw(pose.getLogicalYaw());
            }
        }
        for (DesignFit fit : rules.getFits()) {
            if (fit.getMountType().equals(slot.getMountType())
                    && !slot.getOrientations().contains(fit.getOrientationCode())) {
                slot.getOrientations().add(fit.getOrientationCode());
            }
        }
        return slot;
    }

    private double angle(String orientation) {
        return Double.parseDouble(orientation.substring(1));
    }

    /** Thông báo có tên chỉ từ layout của owner; lỗi quyền dùng câu chung. */
    public String messageFor(String code, long itemId, String slotId) {
        if ("NOT_FOUND".equals(code)) {
            return "Không tìm thấy vật thể hoặc điểm gắn bạn được phép dùng.";
        }
        if ("LAYOUT_CONFLICT".equals(code)) {
            return "Thiết kế đã thay đổi ở tab khác. Hãy tải lại mô hình.";
        }
        if ("LOCKED".equals(code)) {
            return "Thiết kế hiện không sửa được. Hãy về danh sách để kiểm tra.";
        }
        if ("NOTHING_TO_UNDO".equals(code)) {
            return "Không còn thao tác lắp ghép để hoàn tác.";
        }
        DesignInstance item = findInstance(itemId);
        DesignSlot slot = resolveSlot(slotId);
        String name = item == null ? "Vật thể đã chọn" : item.getLabel();
        String mount = slot == null ? "điểm gắn đã chọn" : slot.getLabel();
        if ("NOT_SUPPORTED".equals(code)) {
            return name + ": chưa hỗ trợ kiểm chứng. Không thể tự coi linh kiện này là đúng.";
        }
        if ("MOUNT_INCOMPATIBLE".equals(code)) {
            return "Không thể gắn " + name + " vào " + mount + ": loại điểm gắn không phù hợp.";
        }
        if ("MOUNT_OCCUPIED".equals(code)) {
            for (DesignPlacement placed : placements) {
                if (placed.getSlotId().equals(slotId)) {
                    DesignInstance occupant = findInstance(placed.getInstanceId());
                    if (occupant != null) {
                        return mount + " đã có " + occupant.getLabel() + ". Hãy tháo vật thể đó trước.";
                    }
                }
            }
            return mount + " đã có vật thể khác. Hãy tháo vật thể đó trước.";
        }
        if ("ORIENTATION_NOT_ALLOWED".equals(code)) {
            return name + " chưa đúng hướng tại " + mount + ". Hãy xoay rồi thử lại.";
        }
        if ("HOST_MISSING".equals(code)) {
            return "Cần gắn vật thể cha của " + mount + " trước khi gắn " + name + ".";
        }
        if ("ALREADY_PLACED".equals(code)) {
            DesignPlacement placed = findPlacement(itemId);
            DesignSlot old = placed == null ? null : resolveSlot(placed.getSlotId());
            if (old != null) {
                return name + " đang ở " + old.getLabel() + ". Hãy tháo trước khi chuyển.";
            }
            return name + " đang ở điểm gắn khác. Hãy tháo trước khi chuyển.";
        }
        for (DesignPlacement placed : placements) {
            if (placed.getHostInstanceId() == itemId) {
                DesignInstance child = findInstance(placed.getInstanceId());
                if (child != null) {
                    return "Cần tháo " + child.getLabel() + " trước khi tháo " + name + ".";
                }
            }
        }
        return "Cần tháo các vật thể con trước khi tháo " + name + ".";
    }
}
