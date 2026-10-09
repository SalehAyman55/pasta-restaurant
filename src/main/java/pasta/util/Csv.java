package pasta.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public final class Csv {

    private Csv() {
    }

    public static String join(String... fields) {
        StringJoiner joiner = new StringJoiner(",");
        for (String field : fields) {
            joiner.add(escape(field));
        }
        return joiner.toString();
    }

    public static String joinSafe(String... fields) {
        String[] safe = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            safe[i] = neutralize(fields[i]);
        }
        return join(safe);
    }

    public static List<String> parse(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                out.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        out.add(current.toString());
        return out;
    }

    private static String escape(String value) {
        String v = value == null ? "" : value.replace('\r', ' ').replace('\n', ' ');
        if (v.indexOf(',') >= 0 || v.indexOf('"') >= 0) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    private static String neutralize(String value) {
        if (value != null && !value.isEmpty() && "=+-@\t".indexOf(value.charAt(0)) >= 0) {
            return "'" + value;
        }
        return value;
    }
}
