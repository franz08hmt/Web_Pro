package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import vn.edu.webpro.robotlab.util.JsonUtil;

/**
 * Một tình huống trong hướng dẫn kiểm tra lỗi lắp ráp — JavaBean theo Chapter 6
 * slide 6. Đây là nội dung tham khảo do admin biên soạn, không phải dữ liệu đọc
 * trực tiếp từ robot thật.
 */
public class TroubleshootingGuide implements Serializable {
    private String id;
    private String robotId;
    private String componentGroup;
    private String symptom;
    private String possibleCauses;
    private String resolutionSteps;
    private String relatedComponentId;
    private int displayOrder;

    public TroubleshootingGuide() {
        id = "";
        robotId = null;
        componentGroup = "";
        symptom = "";
        possibleCauses = "";
        resolutionSteps = "";
        relatedComponentId = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** NULL nghĩa là tình huống dùng chung cho nhiều mẫu robot. */
    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public String getComponentGroup() {
        return componentGroup;
    }

    public void setComponentGroup(String componentGroup) {
        this.componentGroup = componentGroup;
    }

    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }

    public String getPossibleCauses() {
        return possibleCauses;
    }

    public void setPossibleCauses(String possibleCauses) {
        this.possibleCauses = possibleCauses;
    }

    public String getResolutionSteps() {
        return resolutionSteps;
    }

    public void setResolutionSteps(String resolutionSteps) {
        this.resolutionSteps = resolutionSteps;
    }

    public String getRelatedComponentId() {
        return relatedComponentId;
    }

    public void setRelatedComponentId(String relatedComponentId) {
        this.relatedComponentId = relatedComponentId;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String toJson() {
        return "{\"id\":" + JsonUtil.quote(id)
                + ",\"robotId\":" + JsonUtil.quote(robotId)
                + ",\"componentGroup\":" + JsonUtil.quote(componentGroup)
                + ",\"symptom\":" + JsonUtil.quote(symptom)
                + ",\"possibleCauses\":" + JsonUtil.quote(possibleCauses)
                + ",\"resolutionSteps\":" + JsonUtil.quote(resolutionSteps)
                + ",\"relatedComponentId\":" + JsonUtil.quote(relatedComponentId)
                + ",\"displayOrder\":" + displayOrder + "}";
    }
}
