package vn.edu.webpro.robotlab.web;

import java.io.IOException;
import java.io.Reader;

/** Minimal JSON writer for server-owned primitives and JSON columns. */
public final class Json {
    private Json() {
    }

    /** Account requests are tiny; reject oversized bodies before parsing. */
    public static String readBody(Reader reader, int maxChars) throws IOException {
        StringBuilder body = new StringBuilder();
        char[] buffer = new char[1024];
        int count;
        while ((count = reader.read(buffer)) != -1) {
            if (body.length() + count > maxChars) throw new IllegalArgumentException("body-too-large");
            body.append(buffer, 0, count);
        }
        return body.toString();
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

    public static String objectField(String body, String key) {
        String marker = "\"" + key + "\"";
        int keyStart = body == null ? -1 : body.indexOf(marker);
        int colon = keyStart < 0 ? -1 : body.indexOf(':', keyStart + marker.length());
        int start = colon < 0 ? -1 : body.indexOf('{', colon + 1);
        if (start < 0) throw new IllegalArgumentException("missing-" + key);
        int depth = 0; boolean quoted = false; boolean escaped = false;
        for (int index = start; index < body.length(); index++) {
            char character = body.charAt(index);
            if (quoted) { if (escaped) escaped = false; else if (character == '\\') escaped = true; else if (character == '"') quoted = false; continue; }
            if (character == '"') { quoted = true; continue; }
            if (character == '{') depth++;
            if (character == '}' && --depth == 0) return body.substring(start, index + 1);
        }
        throw new IllegalArgumentException("invalid-json");
    }
}
