package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Một bước trong quy trình lắp ráp của robot (bảng assembly_steps). */
public class AssemblyStep implements Serializable {
    private String id;
    private String robotId;
    private int stepOrder;
    private String title;
    private String instruction;
    private String illustrationJson;

    public AssemblyStep() {
        id = "";
        robotId = "";
        title = "";
        instruction = "";
        illustrationJson = "{}";
    }

    public AssemblyStep(String id, String robotId, int stepOrder, String title,
                        String instruction, String illustrationJson) {
        this.id = id;
        this.robotId = robotId;
        this.stepOrder = stepOrder;
        this.title = title;
        this.instruction = instruction;
        this.illustrationJson = illustrationJson;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    /** Dữ liệu minh họa 2D lưu dạng object JSON trong cột assembly_steps.illustration. */
    public String getIllustrationJson() {
        return illustrationJson;
    }

    public void setIllustrationJson(String illustrationJson) {
        this.illustrationJson = illustrationJson;
    }

    public String toJson() {
        return "{\"id\":" + JsonUtil.quote(id)
                + ",\"robotId\":" + JsonUtil.quote(robotId)
                + ",\"stepOrder\":" + stepOrder
                + ",\"title\":" + JsonUtil.quote(title)
                + ",\"instruction\":" + JsonUtil.quote(instruction)
                + ",\"illustration\":" + JsonUtil.objectOrEmpty(illustrationJson) + "}";
    }
}
