package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/** Dữ kiện và quy tắc trình bày của RobotDesign. */
public class RobotDesign implements Serializable {
    private long id;
    private long userId;
    private long ruleSetId;
    private int ruleVersion;
    private String name = "";
    private String goalCode = "BASIC_2WD";
    private String goalName = "";
    private String source = "LIBRARY";
    private String status = "DRAFT";
    private int layoutVersion = 1;
    private String lastCheckSummary = "";
    private Date lastCheckedAt;
    private Date createdAt;
    private Date updatedAt;

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public RobotDesign() {
    }

    /** Đọc dữ kiện id. */
    public long getId() {
        return id;
    }

    /** Ghi dữ kiện id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc dữ kiện userId. */
    public long getUserId() {
        return userId;
    }

    /** Ghi dữ kiện userId. */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /** Đọc dữ kiện ruleSetId. */
    public long getRuleSetId() {
        return ruleSetId;
    }

    /** Ghi dữ kiện ruleSetId. */
    public void setRuleSetId(long ruleSetId) {
        this.ruleSetId = ruleSetId;
    }

    /** Phiên bản của chính FK luật đã lưu, không phải phiên bản mới nhất. */
    public int getRuleVersion() {
        return ruleVersion;
    }

    /** Ghi phiên bản đọc từ JOIN của thiết kế. */
    public void setRuleVersion(int ruleVersion) {
        this.ruleVersion = ruleVersion;
    }

    /** Nhãn phiên bản để người dùng phân biệt thiết kế cũ và mới. */
    public String getRuleVersionLabel() {
        return "v" + ruleVersion;
    }

    /** v1 giữ bất biến, chỉ cảnh báo thay vì tự đổi hình hoặc luật. */
    public boolean isLegacyDirections() {
        return ruleVersion == 1;
    }

    /** Giải thích đúng phạm vi lỗi hiển thị của luật cũ. */
    public String getDirectionNotice() {
        return isLegacyDirections() ? "Thiết kế dùng luật v1: hướng hiển thị của khoang pin và mặt trước bị lệch; "
                + "hãy tạo thiết kế mới để dùng luật đã sửa." : "";
    }

    /** Đọc dữ kiện name. */
    public String getName() {
        return name;
    }

    /** Ghi dữ kiện name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Đọc dữ kiện goalCode. */
    public String getGoalCode() {
        return goalCode;
    }

    /** Ghi dữ kiện goalCode. */
    public void setGoalCode(String goalCode) {
        this.goalCode = goalCode;
    }

    /** Đọc dữ kiện goalName. */
    public String getGoalName() {
        return goalName;
    }

    /** Ghi dữ kiện goalName. */
    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    /** Đọc dữ kiện source. */
    public String getSource() {
        return source;
    }

    /** Ghi dữ kiện source. */
    public void setSource(String source) {
        this.source = source;
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

    /** Đọc dữ kiện lastCheckSummary. */
    public String getLastCheckSummary() {
        return lastCheckSummary;
    }

    /** Ghi dữ kiện lastCheckSummary. */
    public void setLastCheckSummary(String lastCheckSummary) {
        this.lastCheckSummary = lastCheckSummary;
    }

    /** Đọc dữ kiện lastCheckedAt. */
    public Date getLastCheckedAt() {
        return lastCheckedAt;
    }

    /** Ghi dữ kiện lastCheckedAt. */
    public void setLastCheckedAt(Date lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    /** Đọc dữ kiện createdAt. */
    public Date getCreatedAt() {
        return createdAt;
    }

    /** Ghi dữ kiện createdAt. */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /** Đọc dữ kiện updatedAt. */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /** Ghi dữ kiện updatedAt. */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** Kiểm tên ở bean cho cả tạo và đổi tên. */
    public String validateName() {
        return name == null || name.trim().isEmpty() || name.trim().length() > 80
                ? "Tên thiết kế phải có từ 1 đến 80 ký tự." : "";
    }

    /** Toàn bộ số lượng, nguồn, mục tiêu và giới hạn được kiểm trước khi insert. */
    public String validateCreation(DesignRuleSet rules, List<KitItem> kit,
            HashMap<String, Integer> quantities, int designCount) {
        if (!validateName().isEmpty()) {
            return validateName();
        }
        if (designCount >= 20) {
            return "Bạn đã có 20 thiết kế. Hãy xóa thiết kế không cần trước khi tạo thêm.";
        }
        if (!"KIT".equals(source) && !"LIBRARY".equals(source)) {
            return "Hãy chọn bộ linh kiện của tôi hoặc thư viện linh kiện.";
        }
        if (!rules.hasGoal(goalCode)) {
            return "Hãy chọn mục tiêu được hỗ trợ trong phiên bản luật này.";
        }
        int total = 0;
        int bases = 0;
        DesignCatalog catalog = new DesignCatalog();
        for (String component : quantities.keySet()) {
            Integer quantity = quantities.get(component);
            if (!catalog.isRenderable(component) || quantity == null || quantity < 0 || quantity > 10) {
                return "Số lượng mỗi loại linh kiện phải từ 0 đến 10.";
            }
            total += quantity;
            DesignComponentRole role = rules.roleFor(component);
            if (role != null && "BASE".equals(role.getRole())) {
                bases += quantity;
            }
            if (isFromKit()) {
                int available = 0;
                String label = "linh kiện đã chọn";
                for (KitItem item : kit) {
                    if (item.getComponentId().equals(component)) {
                        available = item.getQuantity();
                        label = item.getName();
                    }
                }
                if (quantity > available) {
                    return "Số lượng " + label + " vượt bộ linh kiện đang có. Hãy cập nhật bộ hoặc giảm số lượng.";
                }
            }
        }
        if (bases != 1) {
            return "Thiết kế cần đúng một khung xe làm bộ phận nền.";
        }
        return total > 30 ? "Thiết kế chỉ được chọn tối đa 30 vật thể." : "";
    }

    /** CHECKED vẫn chỉnh được; mutation sẽ làm dữ kiện kiểm cũ mất hiệu lực. */
    public boolean isCanEdit() {
        return "DRAFT".equals(status) || "CHECKED".equals(status);
    }

    /** Trạng thái trình bày không lộ enum. */
    public boolean isChecked() {
        return "CHECKED".equals(status);
    }

    /** Nguồn lựa chọn của form. */
    public boolean isFromKit() {
        return "KIT".equals(source);
    }

    /** Nhãn nguồn tiếng Việt. */
    public String getSourceLabel() {
        return isFromKit() ? "Bộ linh kiện của tôi" : "Thư viện linh kiện";
    }

    /** Nhãn trạng thái chỉ nói đã kiểm, không tự tuyên bố đạt. */
    public String getStatusLabel() {
        return isChecked() ? "Đã kiểm tra" : "Bản nháp";
    }

    /** Ngày cập nhật giữ nguyên nguồn JDBC. */
    public String getUpdatedAtLabel() {
        return format(updatedAt);
    }

    /** Ngày kiểm gần nhất, không giả thành điểm không. */
    public String getLastCheckedAtLabel() {
        return format(lastCheckedAt);
    }

    private String format(Date date) {
        if (date == null) {
            return "Chưa kiểm tra";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return formatter.format(date);
    }

}
