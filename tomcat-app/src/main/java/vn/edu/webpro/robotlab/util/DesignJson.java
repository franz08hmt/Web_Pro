package vn.edu.webpro.robotlab.util;

import vn.edu.webpro.robotlab.business.DesignLayout;
import vn.edu.webpro.robotlab.business.DesignSlot;
import vn.edu.webpro.robotlab.business.DesignInstance;
import vn.edu.webpro.robotlab.business.DesignPlacement;

/** Adapter JSON cho view chung; không tính luật hoặc đọc client compatible. */
public class DesignJson {
    /** Serialize trạng thái đã kiểm/lưu; chuỗi qua JsonUtil.quote. */
    public static String layout(DesignLayout layout) {
        StringBuilder json = new StringBuilder("{\"designId\":");
        json.append(JsonUtil.quote(Long.toString(layout.getDesign().getId())))
                .append(",\"name\":").append(JsonUtil.quote(layout.getDesign().getName()))
                .append(",\"goalLabel\":").append(JsonUtil.quote(layout.getDesign().getGoalName()))
                .append(",\"statusLabel\":").append(JsonUtil.quote(layout.getDesign().getStatusLabel()))
                .append(",\"layoutVersion\":").append(layout.getDesign().getLayoutVersion())
                .append(",\"ruleVersion\":").append(layout.getRules().getVersionNo())
                .append(",\"canEdit\":").append(layout.getDesign().isCanEdit())
                .append(",\"unchanged\":").append(layout.isUnchanged()).append(",\"spec\":{\"slots\":[");
        boolean first = true;
        for (DesignSlot slot : layout.getSlots()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append("{\"id\":").append(JsonUtil.quote(slot.getId()))
                    .append(",\"label\":").append(JsonUtil.quote(slot.getLabel()))
                    .append(",\"componentId\":\"\",\"parentSlotId\":")
                    .append(JsonUtil.quote(Long.toString(slot.getHostInstanceId())))
                    .append(",\"x\":").append(slot.getX()).append(",\"y\":").append(slot.getY())
                    .append(",\"z\":").append(slot.getZ()).append(",\"rx\":").append(slot.getRx())
                    .append(",\"ry\":").append(slot.getRy()).append(",\"rz\":").append(slot.getRz())
                    .append(",\"orientations\":[");
            boolean firstOrientation = true;
            for (String orientation : slot.getOrientations()) {
                if (!firstOrientation) {
                    json.append(',');
                }
                firstOrientation = false;
                json.append(JsonUtil.quote(orientation));
            }
            json.append("]}");
        }
        json.append("]},\"instances\":[");
        first = true;
        for (DesignInstance item : layout.getInstances()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append("{\"id\":").append(JsonUtil.quote(Long.toString(item.getId())))
                    .append(",\"componentId\":").append(JsonUtil.quote(item.getComponentId()))
                    .append(",\"label\":").append(JsonUtil.quote(item.getLabel())).append('}');
        }
        json.append("],\"placements\":[");
        first = true;
        for (DesignPlacement placed : layout.getPlacements()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append("{\"instanceId\":").append(JsonUtil.quote(Long.toString(placed.getInstanceId())))
                    .append(",\"slotId\":").append(JsonUtil.quote(placed.getSlotId()))
                    .append(",\"orientation\":").append(JsonUtil.quote(placed.getOrientationCode())).append('}');
        }
        return json.append("]}").toString();
    }
}
