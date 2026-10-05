package vn.edu.webpro.robotlab.util;

import vn.edu.webpro.robotlab.business.*;
import javax.servlet.http.HttpServletRequest;

/** Đọc bốn ô cố định của từng nhóm trong form biên soạn. */
public final class DiagnosisFormUtil {
    private DiagnosisFormUtil() {
    }

    /** Bỏ ô trống và chuyển checkbox/radio thành dữ kiện của nháp. */
    public static DiagnosisScenario scenario(HttpServletRequest request) {
        DiagnosisScenario value = new DiagnosisScenario();
        value.setId(TaskFormUtil.number(request, "id"));
        value.setRobotId(TaskFormUtil.text(request, "robotId"));
        value.setTitle(TaskFormUtil.text(request, "title"));
        value.setContextText(TaskFormUtil.text(request, "contextText"));
        value.setSymptomText(TaskFormUtil.text(request, "symptomText"));
        value.setGuideId(TaskFormUtil.text(request, "guideId"));
        value.setExplanationText(TaskFormUtil.text(request, "explanationText"));
        for (int i = 1; i <= 4; i++) {
            String label = TaskFormUtil.text(request, "checkLabel" + i);
            if (!label.isEmpty()) {
                DiagnosisCheck check = new DiagnosisCheck();
                check.setLabel(label);
                check.setObservationText(TaskFormUtil.text(request, "observation" + i));
                check.setRequired("true".equals(TaskFormUtil.text(request, "required" + i)));
                check.setDisplayOrder(i);
                value.getChecks().add(check);
            }
            readOption(request, value, "CAUSE", "cause", i);
            readOption(request, value, "ACTION", "actionOption", i);
        }
        return value;
    }

    private static void readOption(HttpServletRequest request, DiagnosisScenario value,
            String kind, String prefix, int i) {
        String label = TaskFormUtil.text(request, prefix + "Label" + i);
        if (!label.isEmpty()) {
            DiagnosisOption option = new DiagnosisOption();
            option.setKind(kind);
            option.setLabel(label);
            option.setFeedbackText(TaskFormUtil.text(request, prefix + "Feedback" + i));
            option.setCorrect(String.valueOf(i).equals(TaskFormUtil.text(request, prefix + "Correct")));
            option.setDisplayOrder(i);
            value.getOptions().add(option);
        }
    }
}
