package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/** Dữ kiện và quy tắc trình bày của DesignCatalog. */
public class DesignCatalog implements Serializable {

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignCatalog() {
    }

    private static final String[] CODES = {"chassis-2wd", "dc-motor", "wheel", "caster-wheel",
        "arduino-uno", "l298n", "battery-holder", "power-5v", "hc-sr04", "line-sensor", "sg90",
        "arm-frame", "mini-gripper"};

    /** Chỉ các vật thể renderer hiện có; không mở thư viện tùy ý. */
    public List<String> getComponentIds() {
        List<String> result = new ArrayList<>();
        for (String code : CODES) {
            result.add(code);
        }
        return result;
    }

    /** Có hình dựng không đồng nghĩa có luật kiểm chứng. */
    public boolean isRenderable(String code) {
        for (String value : CODES) {
            if (value.equals(code)) {
                return true;
            }
        }
        return false;
    }

}
