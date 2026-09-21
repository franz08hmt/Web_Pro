package vn.edu.webpro.robotlab.model;

import java.util.List;

/** Paginated API model that preserves the established data/meta envelope. */
public record ComponentPage(List<Component> data, int page, int limit, long total) {
    public String toJson() {
        String components = data.stream().map(Component::toJson).reduce((left, right) -> left + "," + right).orElse("");
        return "{\"data\":[" + components + "],\"meta\":{\"page\":" + page
                + ",\"limit\":" + limit + ",\"total\":" + total + "}}";
    }
}
