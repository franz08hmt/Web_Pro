package vn.edu.webpro.robotlab.util;

import vn.edu.webpro.robotlab.business.*;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;

/** Đọc form URL encoded, không nhận chủ sở hữu hay điểm từ client. */
public final class WiringFormUtil {
    private WiringFormUtil() {
    }

    /** Mã tài nguyên phải là số dương; số 0 chỉ dành cho nháp mới trong form admin. */
    public static long id(HttpServletRequest request, String name) {
        long value = TaskFormUtil.number(request, name);
        if (value == 0) {
            throw new IllegalArgumentException("Mã tài nguyên phải lớn hơn 0.");
        }
        return value;
    }

    /** Kiểm mọi cặp trước ghi; tối đa 80 dây thô kể cả cặp lặp. */
    public static ArrayList<WiringConnection> connections(HttpServletRequest request) {
        String[] pairs = request.getParameterValues("pair");
        ArrayList<WiringConnection> values = new ArrayList<>();
        if (pairs != null) {
            if (pairs.length > 80) {
                throw new IllegalArgumentException("Tối đa 80 dây.");
            }
            for (String pair : pairs) {
                String[] ends = pair.split(":", -1);
                if (ends.length != 2) {
                    throw new IllegalArgumentException("Cặp đầu nối không hợp lệ.");
                }
                try {
                    WiringConnection w = new WiringConnection();
                    w.setTerminalA(Long.parseLong(ends[0]));
                    w.setTerminalB(Long.parseLong(ends[1]));
                    w.normalize();
                    values.add(w);
                } catch (NumberFormatException error) {
                    throw new IllegalArgumentException("Mã đầu nối không hợp lệ.");
                }
            }
        }
        if ("add".equals(TaskFormUtil.text(request, "action"))) {
            WiringConnection w = new WiringConnection();
            w.setTerminalA(id(request, "terminalA"));
            w.setTerminalB(id(request, "terminalB"));
            w.normalize();
            values.add(w);
        }
        return values;
    }

    /** Đọc các ô cố định của nháp, giữ đầu nối khác thiết bị bằng mã riêng. */
    public static WiringExercise exercise(HttpServletRequest request) {
        WiringExercise e = new WiringExercise();
        e.setId(TaskFormUtil.number(request, "id"));
        e.setCode(TaskFormUtil.text(request, "code"));
        e.setTitle(TaskFormUtil.text(request, "title"));
        e.setRobotId(TaskFormUtil.text(request, "robotId"));
        e.setObjective(TaskFormUtil.text(request, "objective"));
        e.setScopeText(TaskFormUtil.text(request, "scopeText"));
        for (int i = 1; i <= 40; i++) {
            String code = TaskFormUtil.text(request, "terminalCode_" + i);
            if (!code.isEmpty()) {
                WiringTerminal t = new WiringTerminal();
                t.setId(i);
                t.setCode(code);
                t.setDeviceCode(TaskFormUtil.text(request, "deviceCode_" + i));
                t.setDeviceLabel(TaskFormUtil.text(request, "deviceLabel_" + i));
                t.setPinLabel(TaskFormUtil.text(request, "pinLabel_" + i));
                t.setDisplayOrder(i);
                t.setX(TaskFormUtil.integer(request, "x_" + i));
                t.setY(TaskFormUtil.integer(request, "y_" + i));
                e.getTerminals().add(t);
            }
        }
        for (int i = 1; i <= 80; i++) {
            String a = TaskFormUtil.text(request, "ruleA_" + i);
            String b = TaskFormUtil.text(request, "ruleB_" + i);
            if (!a.isEmpty() || !b.isEmpty()) {
                WiringRule r = new WiringRule();
                r.setTerminalA(id(request, "ruleA_" + i));
                r.setTerminalB(id(request, "ruleB_" + i));
                r.setKind(TaskFormUtil.text(request, "ruleKind_" + i));
                r.setExplanation(TaskFormUtil.text(request, "ruleExplanation_" + i));
                e.getRules().add(r);
            }
        }
        return e;
    }
}
