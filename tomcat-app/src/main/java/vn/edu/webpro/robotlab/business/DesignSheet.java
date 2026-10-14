package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** Dữ kiện và quy tắc trình bày của DesignSheet. */
public class DesignSheet implements Serializable {
    private DesignLayout layout = new DesignLayout();
    private AssemblyDrawing drawing = new AssemblyDrawing();
    private List<DesignBomItem> bomItems = new ArrayList<>();
    private List<DesignStep> steps = new ArrayList<>();
    private List<DesignLearningError> learningErrors = new ArrayList<>();
    private DesignCheck report = new DesignCheck();

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignSheet() {
    }

    /** Đọc dữ kiện layout. */
    public DesignLayout getLayout() {
        return layout;
    }

    /** Ghi dữ kiện layout. */
    public void setLayout(DesignLayout layout) {
        this.layout = layout;
    }

    /** Đọc dữ kiện drawing. */
    public AssemblyDrawing getDrawing() {
        return drawing;
    }

    /** Ghi dữ kiện drawing. */
    public void setDrawing(AssemblyDrawing drawing) {
        this.drawing = drawing;
    }

    /** Đọc dữ kiện bomItems. */
    public List<DesignBomItem> getBomItems() {
        return bomItems;
    }

    /** Ghi dữ kiện bomItems. */
    public void setBomItems(List<DesignBomItem> bomItems) {
        this.bomItems = bomItems;
    }

    /** Đọc dữ kiện steps. */
    public List<DesignStep> getSteps() {
        return steps;
    }

    /** Ghi dữ kiện steps. */
    public void setSteps(List<DesignStep> steps) {
        this.steps = steps;
    }

    /** Đọc dữ kiện learningErrors. */
    public List<DesignLearningError> getLearningErrors() {
        return learningErrors;
    }

    /** Ghi dữ kiện learningErrors. */
    public void setLearningErrors(List<DesignLearningError> learningErrors) {
        this.learningErrors = learningErrors;
    }

    /** Đọc dữ kiện report. */
    public DesignCheck getReport() {
        return report;
    }

    /** Ghi dữ kiện report. */
    public void setReport(DesignCheck report) {
        this.report = report;
    }

    /** Kết quả cũ bị xóa khi bố cục thay đổi; GET hồ sơ không tự ghi một lần kiểm mới. */
    public boolean isHasReport() {
        return layout.getDesign().isChecked();
    }

    /** Dựng tài liệu từ snapshot owner và bộ linh kiện hiện có, không sửa dữ liệu mẫu. */
    public void build(DesignLayout source, List<KitItem> kit, List<DesignEvent> events) {
        layout = source;
        bomItems = new ArrayList<>();
        steps = new ArrayList<>();
        learningErrors = new ArrayList<>();
        HashMap<String, DesignBomItem> grouped = new HashMap<>();
        for (DesignInstance item : source.getInstances()) {
            DesignBomItem row = grouped.get(item.getComponentId());
            if (row == null) {
                row = new DesignBomItem();
                row.setComponentId(item.getComponentId());
                row.setName(item.getName());
                for (KitItem owned : kit) {
                    if (owned.getComponentId().equals(item.getComponentId())) {
                        row.setOwned(owned.getQuantity());
                        row.setProductId(owned.getProductId());
                    }
                }
                grouped.put(item.getComponentId(), row);
                bomItems.add(row);
            }
            row.setNeeded(row.getNeeded() + 1);
        }
        AssemblyLayout adapter = new AssemblyLayout();
        adapter.setMode("DETAILED");
        for (DesignInstance item : source.getInstances()) {
            DesignSlot pose = source.poseOf(item.getId());
            DesignPlacement placed = source.findPlacement(item.getId());
            if (pose == null || placed == null) {
                continue;
            }
            AssemblySlot slot = new AssemblySlot();
            slot.setId(item.getId());
            slot.setComponentId(item.getComponentId());
            slot.setX(pose.getX());
            slot.setY(pose.getY());
            slot.setZ(pose.getZ());
            slot.setLabel(pose.getLabel());
            adapter.getSpec().getSlots().add(slot);
            AssemblyPlacement p = new AssemblyPlacement();
            p.setInstanceId(item.getId());
            p.setSlotId(slot.getId());
            p.setInstanceLabel(item.getLabel());
            p.setSlotLabel(pose.getLabel());
            p.setOrientationCode(placed.getOrientationCode());
            adapter.getPlacements().add(p);
        }
        drawing = new AssemblyDrawing();
        drawing.build(adapter);
        walk(0, new HashSet<Long>());
        report = new DesignCheck();
        if (isHasReport()) {
            report.evaluate(source);
        }
        HashMap<String, DesignLearningError> errors = new HashMap<>();
        AssemblyReview rule = new AssemblyReview();
        for (DesignEvent event : events) {
            if ("REJECTED".equals(event.getOutcome()) && rule.isLearningError(event.getReasonCode())) {
                DesignLearningError error = errors.get(event.getReasonCode());
                if (error == null) {
                    error = new DesignLearningError();
                    error.setCode(event.getReasonCode());
                    errors.put(event.getReasonCode(), error);
                    learningErrors.add(error);
                }
                error.setCount(error.getCount() + 1);
            }
        }
    }

    // Duyệt cây cha trước con, anh em theo display_order; guard tránh lặp khi dữ liệu giả lập bị vòng.
    private void walk(long hostId, HashSet<Long> visited) {
        List<DesignPlacement> children = new ArrayList<>();
        for (DesignPlacement placed : layout.getPlacements()) {
            if (placed.getHostInstanceId() == hostId) {
                int index = 0;
                while (index < children.size() && order(children.get(index)) <= order(placed)) {
                    index++;
                }
                children.add(index, placed);
            }
        }
        for (DesignPlacement placed : children) {
            DesignInstance item = layout.findInstance(placed.getInstanceId());
            DesignSlot slot = layout.resolveSlot(placed.getSlotId());
            if (item == null || slot == null || layout.poseOf(item.getId()) == null || !visited.add(item.getId())) {
                continue;
            }
            DesignStep step = new DesignStep();
            step.setNumber(steps.size() + 1);
            step.setInstanceId(item.getId());
            step.setDescription("Gắn " + item.getLabel() + " vào " + slot.getLabel() + ", hướng "
                    + placed.getOrientationCode().substring(1) + "°.");
            steps.add(step);
            walk(item.getId(), visited);
        }
    }

    private int order(DesignPlacement placed) {
        DesignSlot slot = layout.resolveSlot(placed.getSlotId());
        return slot == null ? Integer.MAX_VALUE : slot.getDisplayOrder();
    }

}
