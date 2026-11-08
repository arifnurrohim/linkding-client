package app.arifnurrohim.linkding;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class Prefs {
    private static final String FILE = "linkding_settings";

    private static final String KEY_SERVER = "server";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ACCOUNTS = "accounts";
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

    public static final class Account {
        public final String server;
        public final String token;
        public final boolean trustAll;

        public Account(String server, String token, boolean trustAll) {
            this.server = server;
            this.token = token;
            this.trustAll = trustAll;
        }

        public String label() {
            return host(server);
        }
    }

    public static List<Account> getAccounts(Context context) {
        List<Account> accounts = new ArrayList<>();
        String raw = prefs(context).getString(KEY_ACCOUNTS, "");
        if (!raw.isEmpty()) {
            try {
                JSONArray array = new JSONArray(raw);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = array.optJSONObject(i);
                    if (json == null) continue;
                    String server = normalizeServer(json.optString("server", ""));
                    String token = json.optString("token", "");
                    if (server.isEmpty() || token.isEmpty()) continue;
                    accounts.add(new Account(server, token, json.optBoolean("trust_all", false)));
                }
            } catch (Exception ignored) {
            }
        }
        if (accounts.isEmpty() && isConfigured(context)) {
            Account current = new Account(getServer(context), getToken(context), trustAll(context));
            accounts.add(current);
            saveAccounts(context, accounts);
        }
        return accounts;
    }

    private static void saveAccounts(Context context, List<Account> accounts) {
        JSONArray array = new JSONArray();
        for (Account account : accounts) {
            try {
                JSONObject json = new JSONObject();
                json.put("server", account.server);
                json.put("token", account.token);
                json.put("trust_all", account.trustAll);
                array.put(json);
            } catch (Exception ignored) {
            }
        }
        prefs(context).edit().putString(KEY_ACCOUNTS, array.toString()).apply();
    }

    public static void saveAccount(Context context, String server, String token, boolean trustAllValue) {
        Account account = new Account(normalizeServer(server),
                token == null ? "" : token.trim(), trustAllValue);
        if (account.server.isEmpty() || account.token.isEmpty()) return;
        List<Account> accounts = getAccounts(context);
        boolean replaced = false;
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).server.equals(account.server)) {
                accounts.set(i, account);
                replaced = true;
                break;
            }
        }
        if (!replaced) accounts.add(account);
        saveAccounts(context, accounts);
    }

    public static Account getCurrentAccount(Context context) {
        String server = getServer(context);
        if (server.isEmpty()) return null;
        for (Account account : getAccounts(context)) {
            if (account.server.equals(server)) return account;
        }
        return null;
    }

    public static boolean isCurrent(Context context, Account account) {
        return account != null && account.server.equals(getServer(context));
    }

    public static void switchAccount(Context context, Account account) {
        if (account == null) return;
        prefs(context).edit()
                .putString(KEY_SERVER, account.server)
                .putString(KEY_TOKEN, account.token)
                .putBoolean(KEY_TRUST_ALL, account.trustAll)
                .apply();
    }

    public static void removeAccount(Context context, String server) {
        List<Account> accounts = getAccounts(context);
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).server.equals(server)) {
                accounts.remove(i);
                break;
            }
        }
        saveAccounts(context, accounts);
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
        return host(getServer(context));
    }

    public static String host(String server) {
        try {
            java.net.URI uri = java.net.URI.create(server);
            String host = uri.getHost();
            if (host != null && !host.isEmpty()) return host;
        } catch (Exception ignored) {
        }
        return server;
    }

    public static String maskedToken(Context context) {
        return maskToken(getToken(context));
    }

    public static String maskToken(String token) {
        if (token == null || token.isEmpty()) return "";
        if (token.length() <= 6) return "••••••";
        return token.substring(0, 3) + "••••••" + token.substring(token.length() - 3);
    }
}
