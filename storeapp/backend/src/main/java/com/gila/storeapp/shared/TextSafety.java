package com.gila.storeapp.shared;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class TextSafety {
    private static final Pattern HTML_TAG = Pattern.compile(".*<\\s*/?\\s*[a-zA-Z][^>]*>.*");
    private static final Pattern SCRIPT_PROTOCOL = Pattern.compile(".*javascript\\s*:.*", Pattern.CASE_INSENSITIVE);

    public String requireSafe(String fieldName, String value) {
        String cleaned = clean(value);
        if (cleaned == null) {
            return null;
        }
        if (HTML_TAG.matcher(cleaned).matches() || SCRIPT_PROTOCOL.matcher(cleaned).matches()) {
            throw new IllegalArgumentException(fieldName + " contains unsafe markup");
        }
        return cleaned;
    }

    public String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
