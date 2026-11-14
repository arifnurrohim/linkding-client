package app.arifnurrohim.linkding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Urls {
    private static final Pattern URL = Pattern.compile(
            "https?://[^\\s<>\"']+", Pattern.CASE_INSENSITIVE);

    private Urls() {
    }

    public static String extract(String text) {
        if (text == null) return null;
        Matcher matcher = URL.matcher(text);
        if (matcher.find()) {
            String url = matcher.group();
            while (url.length() > 1 && ".,;:!?)]}'\"".indexOf(url.charAt(url.length() - 1)) >= 0) {
                url = url.substring(0, url.length() - 1);
            }
            return url;
        }
        String trimmed = text.trim();
        if (trimmed.matches("^[\\w.-]+\\.[a-z]{2,}(/\\S*)?$")) {
            return "https://" + trimmed;
        }
        return null;
    }
}
