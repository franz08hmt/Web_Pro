package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Kết quả bổ sung giỏ từ BOM; bấm lại không cộng lượng đã có trong giỏ. */
public class DesignCartResult implements Serializable {
    private List<String> messages = new ArrayList<>();

    /** Tạo bean rỗng. */
    public DesignCartResult() {
    }

    /** Đọc các thông báo để hiển thị sau PRG. */
    public List<String> getMessages() { return messages; }

    /** Ghi các thông báo của lần bổ sung. */
    public void setMessages(List<String> messages) { this.messages = messages; }

    /** Giữ lượng cũ; chỉ bù thiếu, không tự giảm giỏ hoặc thêm vượt kho/giới hạn. */
    public int targetQuantity(int needed, int owned, int current, int stock, int maximum) {
        int missing = Math.max(needed - owned, 0);
        int permitted = Math.max(Math.min(stock, maximum), 0);
        return Math.max(current, Math.min(missing, permitted));
    }

    /** Dữ liệu thông báo sau PRG; mọi chuỗi được quote trước khi trả JSON. */
    public String toJson() {
        StringBuilder json = new StringBuilder("[");
        for (String message : messages) {
            if (json.length() > 1) {
                json.append(',');
            }
            json.append(JsonUtil.quote(message));
        }
        return json.append(']').toString();
    }
}
