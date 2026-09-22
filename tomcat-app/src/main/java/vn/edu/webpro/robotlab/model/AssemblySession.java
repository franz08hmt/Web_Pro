package vn.edu.webpro.robotlab.model;

import java.util.List;
import vn.edu.webpro.robotlab.web.Json;

/** Một phiên thực hành của một tài khoản với một mô hình robot. */
public record AssemblySession(
        long id,
        long userId,
        String robotId,
        String status,
        List<SessionComponent> components,
        List<SessionStep> steps,
        List<String> assembledPartIds,
        int requiredComponentCount
) {
    public String toJson() {
        String componentJson = join(components.stream().map(SessionComponent::toJson).toList());
        String stepJson = join(steps.stream().map(SessionStep::toJson).toList());
        String assembledJson = join(assembledPartIds.stream().map(Json::quote).toList());

        return "{\"id\":" + Json.quote(Long.toString(id))
                + ",\"userId\":" + Json.quote(Long.toString(userId))
                + ",\"robotId\":" + Json.quote(robotId)
                + ",\"status\":" + Json.quote(status)
                + ",\"progressPercent\":" + progressPercent()
                + ",\"components\":[" + componentJson + "]"
                + ",\"steps\":[" + stepJson + "]"
                + ",\"assembledPartIds\":[" + assembledJson + "]}";
    }

    /* Tiến độ tính lại từ dữ liệu thật mỗi lần trả về, không lưu sẵn trong
       database: distinct() phòng trường hợp một linh kiện xuất hiện nhiều dòng,
       và requiredComponentCount do service truyền vào theo bảng robot_components. */
    private int progressPercent() {
        if (requiredComponentCount == 0) return 0;

        long prepared = components.stream()
                .filter(SessionComponent::isPrepared)
                .map(SessionComponent::componentId)
                .distinct()
                .count();
        return (int) (prepared * 100 / requiredComponentCount);
    }

    private String join(List<String> values) {
        return values.stream().reduce((left, right) -> left + "," + right).orElse("");
    }
}
