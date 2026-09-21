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

    public static String arrayOrEmpty(String value) {
        if (value == null) return "[]";
        String trimmed = value.trim();
        return trimmed.startsWith("[") && trimmed.endsWith("]") ? trimmed : "[]";
    }

    /** Small strict parser for the flat JSON body accepted by the auth API. */
    public static String stringField(String body, String key) {
        if (body == null) throw new IllegalArgumentException("invalid-json");
        String marker = "\"" + key + "\"";
        int keyStart = body.indexOf(marker);
        if (keyStart < 0) throw new IllegalArgumentException("missing-" + key);
        int colon = body.indexOf(':', keyStart + marker.length());
        int quote = colon < 0 ? -1 : body.indexOf('"', colon + 1);
        if (quote < 0) throw new IllegalArgumentException("invalid-json");
        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int index = quote + 1; index < body.length(); index++) {
            char character = body.charAt(index);
            if (escaped) { value.append(character); escaped = false; continue; }
            if (character == '\\') { escaped = true; continue; }
            if (character == '"') return value.toString();
            value.append(character);
        }
        throw new IllegalArgumentException("invalid-json");
    }
}
