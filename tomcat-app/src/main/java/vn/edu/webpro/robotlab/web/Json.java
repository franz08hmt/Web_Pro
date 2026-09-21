package vn.edu.webpro.robotlab.web;

/** Minimal JSON writer for server-owned primitives and JSON columns. */
public final class Json {
    private Json() {
    }

    public static String quote(String value) {
        if (value == null) return "null";

        StringBuilder json = new StringBuilder("\"");
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '\\' -> json.append("\\\\");
                case '"' -> json.append("\\\"");
                case '\b' -> json.append("\\b");
                case '\f' -> json.append("\\f");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (character < 0x20) {
                        json.append(String.format("\\u%04x", (int) character));
                    } else {
                        json.append(character);
                    }
                }
            }
        }
        return json.append('"').toString();
    }

    /** JSON in specs is written by the content admin validation layer and must be an object. */
    public static String objectOrEmpty(String value) {
        if (value == null) return "{}";
        String trimmed = value.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}") ? trimmed : "{}";
    }
}
