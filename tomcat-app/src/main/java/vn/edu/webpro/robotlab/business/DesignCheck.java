package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/** Dữ kiện và quy tắc trình bày của DesignCheck. */
public class DesignCheck implements Serializable {
    private List<DesignCheckItem> items = new ArrayList<>();
    private boolean passed;
    private int placedCount;
    private int totalCount;

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignCheck() {
    }

    /** Đọc dữ kiện items. */
    public List<DesignCheckItem> getItems() {
        return items;
    }

    /** Ghi dữ kiện items. */
    public void setItems(List<DesignCheckItem> items) {
        this.items = items;
    }

    /** Đọc dữ kiện passed. */
    public boolean isPassed() {
        return passed;
    }

    /** Ghi dữ kiện passed. */
    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    /** Đọc dữ kiện placedCount. */
    public int getPlacedCount() {
        return placedCount;
    }

    /** Ghi dữ kiện placedCount. */
    public void setPlacedCount(int placedCount) {
        this.placedCount = placedCount;
    }

    /** Đọc dữ kiện totalCount. */
    public int getTotalCount() {
        return totalCount;
    }

    /** Ghi dữ kiện totalCount. */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /** Kết luận không biến thiết kế riêng thành bằng chứng hoàn thành mẫu. */
    public String getSummary() {
        return passed ? "Đạt mục tiêu theo luật được hỗ trợ" : "Chưa đạt mục tiêu theo luật được hỗ trợ";
    }

    /** Ba lớp được tính lại từ snapshot và phiên bản luật đã lưu, không đọc kết luận client. */
    public void evaluate(DesignLayout layout) {
        // Tính lại ba lớp kiểm từ layout đã lưu; kết luận không phải dữ liệu client gửi.
        items = new ArrayList<>();
        passed = true;
        placedCount = layout.getPlacements().size();
        totalCount = layout.getInstances().size();
        HashMap<String, Integer> counts = new HashMap<>();
        int channels = 0;
        boolean servoPower = false;
        boolean scanningSensor = false;
        for (DesignInstance item : layout.getInstances()) {
            DesignComponentRole role = layout.getRules().roleFor(item.getComponentId());
            if (role == null) {
                add("Lắp ghép cơ khí", "UNSUPPORTED", item.getLabel() + ": chưa hỗ trợ kiểm chứng.");
                continue;
            }
            DesignPlacement placed = layout.findPlacement(item.getId());
            if (placed == null) {
                add("Lắp ghép cơ khí", "WARN", item.getLabel() + " còn trong khay, chưa được tính cho mục tiêu.");
                continue;
            }
            DesignSlot slot = layout.resolveSlot(placed.getSlotId());
            if (slot == null || !slot.isHostAvailable() || layout.poseOf(item.getId()) == null
                    || !layout.getRules().fitsOrientation(item.getComponentId(), slot.getMountType(),
                            placed.getOrientationCode())) {
                add("Lắp ghép cơ khí", "FAIL", item.getLabel() + " chưa đúng chuỗi gắn hoặc hướng lắp.");
                continue;
            }
            Integer count = counts.get(role.getRole());
            counts.put(role.getRole(), count == null ? 1 : count + 1);
            channels += "MOTOR_DRIVER".equals(role.getRole()) ? role.getDriverChannels() : 0;
            servoPower = servoPower || "power-5v".equals(item.getComponentId());
            scanningSensor = scanningSensor || ("DISTANCE_SENSOR".equals(role.getRole())
                    && "SERVO_HORN".equals(slot.getMountType()));
        }
        if (placedCount > 0 && !hasResult("Lắp ghép cơ khí", "FAIL")) {
            add("Lắp ghép cơ khí", "PASS", "Các vật thể đã gắn đúng loại, hướng và chuỗi cha–con được hỗ trợ.");
        }
        for (DesignRequirement required : layout.getRules().getRequirements()) {
            if (required.getGoalCode().equals(layout.getDesign().getGoalCode())) {
                int actual = count(counts, required.getRole());
                add("Vai trò theo mục tiêu", actual >= required.getMinCount() ? "PASS" : "FAIL",
                        required.getMessage() + " Đã gắn " + actual + "/" + required.getMinCount() + ".");
            }
        }
        int motors = count(counts, "DRIVE_MOTOR");
        int drivers = count(counts, "MOTOR_DRIVER");
        int controllers = count(counts, "CONTROLLER");
        int sensors = count(counts, "DISTANCE_SENSOR") + count(counts, "LINE_SENSOR");
        if (motors > 0 && drivers == 0) {
            add("Tương thích cấu hình", "FAIL", "Động cơ DC cần mạch điều khiển động cơ (ví dụ L298N); "
                    + "chân Arduino không cấp đủ dòng cho động cơ.");
        }
        if (motors > channels) {
            add("Tương thích cấu hình", "FAIL", "Số động cơ DC vượt số kênh của mạch điều khiển đã gắn.");
        }
        if (count(counts, "POWER") == 0) {
            add("Tương thích cấu hình", "FAIL", "Cần gắn bộ nguồn cho thiết kế.");
        }
        if ((motors > 0 || sensors > 0 || count(counts, "SERVO") > 0) && controllers == 0) {
            add("Tương thích cấu hình", "FAIL", "Cảm biến và động cơ cần mạch điều khiển đã gắn.");
        }
        if (controllers > 1) {
            add("Tương thích cấu hình", "WARN", "Có nhiều hơn một mạch điều khiển; cần kiểm tra phân công thực tế.");
        }
        if (motors >= 2 && count(counts, "SUPPORT_WHEEL") == 0) {
            add("Tương thích cấu hình", "WARN", "Thiếu bánh tự do: xe dễ mất cân bằng.");
        }
        if (count(counts, "SERVO") > 0 && !servoPower) {
            add("Tương thích cấu hình", "WARN", "Nên cấp nguồn 5V riêng cho servo khi tải lớn.");
        }
        if ("SCANNING_AVOIDER".equals(layout.getDesign().getGoalCode()) && !scanningSensor) {
            add("Vai trò theo mục tiêu", "FAIL", "Cảm biến khoảng cách phải gắn trên đầu servo để quét.");
        }
        if (!hasResult("Tương thích cấu hình", "FAIL")) {
            add("Tương thích cấu hình", "PASS", "Cấu hình cơ bản phù hợp các luật đã được hỗ trợ.");
        }
    }

    private int count(HashMap<String, Integer> counts, String role) {
        Integer value = counts.get(role);
        return value == null ? 0 : value;
    }

    private boolean hasResult(String layer, String result) {
        for (DesignCheckItem item : items) {
            if (layer.equals(item.getLayer()) && result.equals(item.getResult())) {
                return true;
            }
        }
        return false;
    }

    private void add(String layer, String result, String message) {
        DesignCheckItem item = new DesignCheckItem();
        item.setLayer(layer);
        item.setResult(result);
        item.setMessage(message);
        items.add(item);
        if ("FAIL".equals(result) || "UNSUPPORTED".equals(result)) {
            passed = false;
        }
    }

}
