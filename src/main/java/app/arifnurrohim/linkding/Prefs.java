package app.arifnurrohim.linkding;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    private static final String FILE = "linkding_settings";

    private static final String KEY_SERVER = "server";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_FLAVOR = "flavor";
    private static final String KEY_ACCENT = "accent";
    private static final String KEY_FAVICONS = "favicons";
    private static final String KEY_TRUST_ALL = "trust_all";
    private static final String KEY_SORT = "sort";

    public static final String DEFAULT_FLAVOR = "mocha";
    public static final String DEFAULT_ACCENT = "mauve";
    public static final String DEFAULT_SORT = "-date_added";

    private Prefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean isConfigured(Context context) {
        return !getServer(context).isEmpty() && !getToken(context).isEmpty();
    }

    public static String getServer(Context context) {
        return prefs(context).getString(KEY_SERVER, "");
    }

    public static void setServer(Context context, String server) {
        prefs(context).edit().putString(KEY_SERVER, normalizeServer(server)).apply();
    }

    public static String getToken(Context context) {
        return prefs(context).getString(KEY_TOKEN, "");
    }

    public static void setToken(Context context, String token) {
        prefs(context).edit().putString(KEY_TOKEN, token == null ? "" : token.trim()).apply();
    }

    public static String getFlavor(Context context) {
        return prefs(context).getString(KEY_FLAVOR, DEFAULT_FLAVOR);
    }

    public static void setFlavor(Context context, String flavor) {
        prefs(context).edit().putString(KEY_FLAVOR, flavor).apply();
    }

    public static String getAccent(Context context) {
        return prefs(context).getString(KEY_ACCENT, DEFAULT_ACCENT);
    }

    public static void setAccent(Context context, String accent) {
        prefs(context).edit().putString(KEY_ACCENT, accent).apply();
    }

    public static boolean showFavicons(Context context) {
        return prefs(context).getBoolean(KEY_FAVICONS, true);
    }

    public static void setShowFavicons(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_FAVICONS, value).apply();
    }

    public static boolean trustAll(Context context) {
        return prefs(context).getBoolean(KEY_TRUST_ALL, false);
    }

    public static void setTrustAll(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_TRUST_ALL, value).apply();
    }

    public static String getSort(Context context) {
        return prefs(context).getString(KEY_SORT, DEFAULT_SORT);
    }

    public static void setSort(Context context, String sort) {
        prefs(context).edit().putString(KEY_SORT, sort).apply();
    }

    public static void clear(Context context) {
        prefs(context).edit().clear().apply();
    }

    public static String normalizeServer(String value) {
        if (value == null) return "";
        String server = value.trim();
        if (server.isEmpty()) return "";
        if (!server.startsWith("http://") && !server.startsWith("https://")) {
            server = "https://" + server;
        }
        while (server.endsWith("/")) {
            server = server.substring(0, server.length() - 1);
        }
        return server;
    }

    public static String host(Context context) {
        String server = getServer(context);
        try {
            java.net.URI uri = java.net.URI.create(server);
            String host = uri.getHost();
            if (host != null && !host.isEmpty()) return host;
        } catch (Exception ignored) {
        }
        return server;
    }

    public static String maskedToken(Context context) {
        String token = getToken(context);
        if (token.isEmpty()) return "";
        if (token.length() <= 6) return "••••••";
        return token.substring(0, 3) + "••••••" + token.substring(token.length() - 3);
    }
}
