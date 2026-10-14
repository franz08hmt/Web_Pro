package vn.edu.webpro.robotlab.util;

import java.util.List;
import vn.edu.webpro.robotlab.business.AssemblyLayout;
import vn.edu.webpro.robotlab.business.AssemblySlot;
import vn.edu.webpro.robotlab.business.AssemblyInstance;
import vn.edu.webpro.robotlab.business.AssemblyPlacement;
import vn.edu.webpro.robotlab.business.AssemblyCheckReport;
import vn.edu.webpro.robotlab.business.AssemblyEvent;

/** Chuyển dữ kiện bean thành JSON; không chứa công thức hoặc quyết định gắn. */
public final class AssemblyPlacementJson {
    private AssemblyPlacementJson() {
    }

    /** Chỉ phát lại các thay đổi được lưu, kể cả dấu hoàn tác của từng sự kiện. */
    public static String events(List<AssemblyEvent> events) {
        StringBuilder json = new StringBuilder("[");
        for (AssemblyEvent event : events) {
            if (!event.isAccepted()) {
                continue;
            }
            if (json.length() > 1) {
                json.append(',');
            }
            json.append("{\"id\":").append(JsonUtil.quote(Long.toString(event.getId())))
                    .append(",\"action\":").append(JsonUtil.quote(event.getAction()))
                    .append(",\"actionLabel\":").append(JsonUtil.quote(event.getActionLabel()))
                    .append(",\"instanceId\":").append(JsonUtil.quote(Long.toString(event.getInstanceId())))
                    .append(",\"slotId\":").append(JsonUtil.quote(Long.toString(event.getSlotId())))
                    .append(",\"orientation\":").append(JsonUtil.quote(event.getOrientationCode()))
                    .append(",\"prevOrientation\":").append(JsonUtil.quote(event.getPrevOrientationCode()))
                    .append(",\"instanceLabel\":").append(JsonUtil.quote(event.getInstanceLabel()))
                    .append(",\"slotLabel\":").append(JsonUtil.quote(event.getSlotLabel()))
                    .append(",\"orientationLabel\":").append(JsonUtil.quote(event.getOrientationLabel()))
                    .append(",\"createdAtLabel\":").append(JsonUtil.quote(event.getCreatedAtLabel()))
                    .append(",\"undoOfEventId\":").append(event.getUndoOfEventId())
                    .append(",\"undone\":").append(event.isUndone()).append('}');
        }
        return json.append(']').toString();
    }

    private static String strings(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (String value : values) {
            if (json.length() > 1) {
                json.append(',');
            }
            json.append(JsonUtil.quote(value));
        }
        return json.append(']').toString();
    }

    /** Serialize kết quả kiểm tra đã được bean tính từ layout server. */
    public static String report(AssemblyCheckReport report) {
        return "{\"correctCount\":" + report.getCorrectCount() + ",\"requiredCount\":" + report.getRequiredCount()
                + ",\"percent\":" + report.getPercent() + ",\"complete\":" + report.isComplete()
                + ",\"trayLabels\":" + strings(report.getTrayLabels())
                + ",\"missingSlots\":" + strings(report.getMissingSlots())
                + ",\"errors\":" + strings(report.getErrors()) + "}";
    }

    /** Serialize pose/hướng được phép hiển thị và vật thể của phiên owner. */
    public static String layout(AssemblyLayout layout) {
        StringBuilder slots = new StringBuilder("[");
        for (AssemblySlot slot : layout.getSpec().getSlots()) {
            if (slots.length() > 1) {
                slots.append(',');
            }
            slots.append("{\"id\":").append(JsonUtil.quote(Long.toString(slot.getId())))
                    .append(",\"componentId\":").append(JsonUtil.quote(slot.getComponentId()))
                    .append(",\"label\":").append(JsonUtil.quote(slot.getLabel()))
                    .append(",\"parentSlotId\":").append(JsonUtil.quote(Long.toString(slot.getParentSlotId())))
                    .append(",\"x\":").append(slot.getX()).append(",\"y\":").append(slot.getY())
                    .append(",\"z\":").append(slot.getZ()).append(",\"rx\":").append(slot.getRx())
                    .append(",\"ry\":").append(slot.getRy()).append(",\"rz\":").append(slot.getRz())
                    .append(",\"orientations\":").append(strings(slot.getOrientations())).append('}');
        }
        slots.append(']');
        StringBuilder instances = new StringBuilder("[");
        for (AssemblyInstance item : layout.getInstances()) {
            if (instances.length() > 1) {
                instances.append(',');
            }
            instances.append("{\"id\":").append(JsonUtil.quote(Long.toString(item.getId())))
                    .append(",\"componentId\":").append(JsonUtil.quote(item.getComponentId()))
                    .append(",\"instanceNo\":").append(item.getInstanceNo())
                    .append(",\"label\":").append(JsonUtil.quote(item.getLabel())).append('}');
        }
        instances.append(']');
        StringBuilder placements = new StringBuilder("[");
        for (AssemblyPlacement placed : layout.getPlacements()) {
            if (placements.length() > 1) {
                placements.append(',');
            }
            placements.append("{\"instanceId\":").append(JsonUtil.quote(Long.toString(placed.getInstanceId())))
                    .append(",\"slotId\":").append(JsonUtil.quote(Long.toString(placed.getSlotId())))
                    .append(",\"orientation\":").append(JsonUtil.quote(placed.getOrientationCode()))
                    .append(",\"placedAt\":").append(JsonUtil.quote(placed.getPlacedAt())).append('}');
        }
        placements.append(']');
        return "{\"sessionId\":" + JsonUtil.quote(Long.toString(layout.getSessionId()))
                + ",\"mode\":" + JsonUtil.quote(layout.getMode()) + ",\"status\":" + JsonUtil.quote(layout.getStatus())
                + ",\"layoutVersion\":" + layout.getLayoutVersion() + ",\"canEdit\":" + layout.isCanEdit()
                + ",\"unchanged\":" + layout.isUnchanged() + ",\"transitioned\":" + layout.isTransitioned()
                + ",\"spec\":{\"id\":" + JsonUtil.quote(Long.toString(layout.getSpec().getId()))
                + ",\"versionNo\":" + layout.getSpec().getVersionNo() + ",\"slots\":" + slots + "}"
                + ",\"instances\":" + instances + ",\"placements\":" + placements
                + ",\"report\":" + report(layout.check()) + "}";
    }
}
