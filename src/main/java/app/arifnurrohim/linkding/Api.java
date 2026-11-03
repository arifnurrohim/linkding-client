package app.arifnurrohim.linkding;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Api {
    public enum Filter {
        ALL, UNREAD, ARCHIVED, SHARED
    }

    public interface Callback<T> {
        void onResult(Result<T> result);
    }

    public static class Result<T> {
        public final boolean ok;
        public final T data;
        public final int code;
        public final String error;

        Result(boolean ok, T data, int code, String error) {
            this.ok = ok;
            this.data = data;
            this.code = code;
            this.error = error;
        }

        public static <T> Result<T> ok(T data) {
            return new Result<>(true, data, 200, null);
        }

        public static <T> Result<T> error(int code, String message) {
            return new Result<>(false, null, code, message);
        }

        public boolean unauthorized() {
            return code == 401 || code == 403;
        }
    }

    private interface Task<T> {
        Result<T> run() throws Exception;
    }

    private static final ExecutorService POOL = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static SSLContext trustAllContext;

    private final String server;
    private final String token;
    private final boolean trustAll;

    private Api(String server, String token, boolean trustAll) {
        this.server = server;
        this.token = token;
        this.trustAll = trustAll;
    }

    public static Api from(Context context) {
        return new Api(Prefs.getServer(context), Prefs.getToken(context), Prefs.trustAll(context));
    }

    public static Api with(String server, String token, boolean trustAll) {
        return new Api(Prefs.normalizeServer(server), token == null ? "" : token.trim(), trustAll);
    }

    private <T> void exec(final Task<T> task, final Callback<T> callback) {
        POOL.execute(new Runnable() {
            @Override
            public void run() {
                Result<T> result;
                try {
                    result = task.run();
                } catch (Exception e) {
                    result = Result.error(0, friendlyError(e));
                }
                final Result<T> finalResult = result;
                if (callback != null) {
                    MAIN.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResult(finalResult);
                        }
                    });
                }
            }
        });
    }

    public void testConnection(final Callback<Boolean> callback) {
        exec(new Task<Boolean>() {
            @Override
            public Result<Boolean> run() throws Exception {
                HttpURLConnection connection = open("/bookmarks/?limit=1", "GET", null);
                int code = connection.getResponseCode();
                String body = readBody(connection);
                connection.disconnect();
                if (code == 401 || code == 403) {
                    return Result.error(code, "Invalid API token");
                }
                if (code >= 200 && code < 300) {
                    return Result.ok(Boolean.TRUE);
                }
                return Result.error(code, errorMessage(code, body));
            }
        }, callback);
    }

    public void listBookmarks(final Filter filter, final String query, final String sort,
                              final int limit, final int offset,
                              final Callback<List<Bookmark>> callback) {
        exec(new Task<List<Bookmark>>() {
            @Override
            public Result<List<Bookmark>> run() throws Exception {
                StringBuilder path = new StringBuilder(filterPath(filter));
                StringBuilder params = new StringBuilder();
                if (query != null && !query.trim().isEmpty()) {
                    appendParam(params, "q", query.trim());
                }
                if (filter == Filter.UNREAD) {
                    appendParam(params, "unread", "yes");
                }
                if (sort != null && !sort.isEmpty()) {
                    appendParam(params, "sort", sort);
                }
                appendParam(params, "limit", String.valueOf(limit));
                appendParam(params, "offset", String.valueOf(offset));
                if (params.length() > 0) {
                    path.append(path.indexOf("?") >= 0 ? "&" : "?").append(params);
                }

                HttpURLConnection connection = open(path.toString(), "GET", null);
                int code = connection.getResponseCode();
                String body = readBody(connection);
                connection.disconnect();

                if (code == 401 || code == 403) {
                    return Result.error(code, "Invalid API token");
                }
                if (code < 200 || code >= 300) {
                    return Result.error(code, errorMessage(code, body));
                }
                JSONObject json = new JSONObject(body);
                JSONArray results = json.optJSONArray("results");
                List<Bookmark> bookmarks = new ArrayList<>();
                if (results != null) {
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject item = results.optJSONObject(i);
                        if (item != null) bookmarks.add(Bookmark.fromJson(item));
                    }
                }
                return Result.ok(bookmarks);
            }
        }, callback);
    }

    public void listTags(final String query, final Callback<List<Tag>> callback) {
        exec(new Task<List<Tag>>() {
            @Override
            public Result<List<Tag>> run() throws Exception {
                StringBuilder path = new StringBuilder("/tags/?limit=200");
                if (query != null && !query.trim().isEmpty()) {
                    path.append("&q=").append(URLEncoder.encode(query.trim(), "UTF-8"));
                }
                HttpURLConnection connection = open(path.toString(), "GET", null);
                int code = connection.getResponseCode();
                String body = readBody(connection);
                connection.disconnect();

                if (code == 401 || code == 403) {
                    return Result.error(code, "Invalid API token");
                }
                if (code < 200 || code >= 300) {
                    return Result.error(code, errorMessage(code, body));
                }
                JSONObject json = new JSONObject(body);
                JSONArray results = json.optJSONArray("results");
                List<Tag> tags = new ArrayList<>();
                if (results != null) {
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject item = results.optJSONObject(i);
                        if (item != null) tags.add(Tag.fromJson(item));
                    }
                }
                return Result.ok(tags);
            }
        }, callback);
    }

    public void checkUrl(final String url, final Callback<Bookmark> callback) {
        exec(new Task<Bookmark>() {
            @Override
            public Result<Bookmark> run() throws Exception {
                String path = "/bookmarks/check?url=" + URLEncoder.encode(url, "UTF-8");
                HttpURLConnection connection = open(path, "GET", null);
                int code = connection.getResponseCode();
                String body = readBody(connection);
                connection.disconnect();
                if (code == 401 || code == 403) {
                    return Result.error(code, "Invalid API token");
                }
                if (code < 200 || code >= 300) {
                    return Result.error(code, errorMessage(code, body));
                }
                JSONObject json = new JSONObject(body);
                JSONObject bookmarkJson = json.optJSONObject("bookmark");
                return Result.ok(bookmarkJson == null ? null : Bookmark.fromJson(bookmarkJson));
            }
        }, callback);
    }

    public void save(final Bookmark bookmark, final boolean create, final Callback<Bookmark> callback) {
        exec(new Task<Bookmark>() {
            @Override
            public Result<Bookmark> run() throws Exception {
                String path = create ? "/bookmarks/" : "/bookmarks/" + bookmark.id + "/";
                String method = create ? "POST" : "PATCH";
                HttpURLConnection connection = open(path, method, bookmark.toJson().toString());
                int code = connection.getResponseCode();
                String body = readBody(connection);
                connection.disconnect();
                if (code == 401 || code == 403) {
                    return Result.error(code, "Invalid API token");
                }
                if (code < 200 || code >= 300) {
                    return Result.error(code, errorMessage(code, body));
                }
                if (body != null && !body.trim().isEmpty()) {
                    try {
                        return Result.ok(Bookmark.fromJson(new JSONObject(body)));
                    } catch (Exception ignored) {
                    }
                }
                return Result.ok(bookmark);
            }
        }, callback);
    }

    public void setUnread(final int id, final boolean unread, final Callback<Bookmark> callback) {
        exec(new Task<Bookmark>() {
            @Override
            public Result<Bookmark> run() throws Exception {
                JSONObject body = new JSONObject();
                body.put("unread", unread);
                HttpURLConnection connection = open("/bookmarks/" + id + "/", "PATCH", body.toString());
                int code = connection.getResponseCode();
                String response = readBody(connection);
                connection.disconnect();
                if (code == 401 || code == 403) {
                    return Result.error(code, "Invalid API token");
                }
                if (code < 200 || code >= 300) {
                    return Result.error(code, errorMessage(code, response));
                }
                if (response != null && !response.trim().isEmpty()) {
                    try {
                        return Result.ok(Bookmark.fromJson(new JSONObject(response)));
                    } catch (Exception ignored) {
                    }
                }
                return Result.ok(null);
            }
        }, callback);
    }

    public void setArchived(final int id, final boolean archived, final Callback<Bookmark> callback) {
        exec(new Task<Bookmark>() {
            @Override
            public Result<Bookmark> run() throws Exception {
                String path = "/bookmarks/" + id + (archived ? "/archive/" : "/unarchive/");
                HttpURLConnection connection = open(path, "POST", "");
                int code = connection.getResponseCode();
                String body = readBody(connection);
                connection.disconnect();
                if (code == 401 || code == 403) {
                    return Result.error(code, "Invalid API token");
                }
                if (code < 200 || code >= 300) {
                    return Result.error(code, errorMessage(code, body));
                }
                return Result.ok(null);
            }
        }, callback);
    }

    public void delete(final int id, final Callback<Boolean> callback) {
        exec(new Task<Boolean>() {
            @Override
            public Result<Boolean> run() throws Exception {
                HttpURLConnection connection = open("/bookmarks/" + id + "/", "DELETE", null);
                int code = connection.getResponseCode();
                String body = readBody(connection);
                connection.disconnect();
                if (code == 401 || code == 403) {
                    return Result.error(code, "Invalid API token");
                }
                if (code < 200 || code >= 300) {
                    return Result.error(code, errorMessage(code, body));
                }
                return Result.ok(Boolean.TRUE);
            }
        }, callback);
    }

    private static String filterPath(Filter filter) {
        switch (filter) {
            case ARCHIVED: return "/bookmarks/archived/";
            case SHARED: return "/bookmarks/shared/";
            case UNREAD:
            case ALL:
            default: return "/bookmarks/";
        }
    }

    private static void appendParam(StringBuilder builder, String key, String value) {
        if (builder.length() > 0) builder.append('&');
        try {
            if (key != null) {
                builder.append(URLEncoder.encode(key, "UTF-8")).append('=');
            }
            builder.append(URLEncoder.encode(value == null ? "" : value, "UTF-8"));
        } catch (Exception ignored) {
        }
    }

    private HttpURLConnection open(String path, String method, String body) throws IOException {
        URL url;
        try {
            url = new URL(server + "/api" + path);
        } catch (MalformedURLException e) {
            throw new IOException("Invalid server URL", e);
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (trustAll && connection instanceof HttpsURLConnection) {
            HttpsURLConnection https = (HttpsURLConnection) connection;
            try {
                https.setSSLSocketFactory(trustAllContext().getSocketFactory());
            } catch (Exception ignored) {
            }
            https.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        }
        connection.setRequestMethod(method);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(25000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Authorization", "Token " + token);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "LinkDing-Android/1.0");
        if (body != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            OutputStream output = connection.getOutputStream();
            output.write(body.getBytes(StandardCharsets.UTF_8));
            output.close();
        }
        return connection;
    }

    private static synchronized SSLContext trustAllContext() throws Exception {
        if (trustAllContext == null) {
            TrustManager[] trustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, new SecureRandom());
            trustAllContext = context;
        }
        return trustAllContext;
    }

    private static String readBody(HttpURLConnection connection) throws IOException {
        int code;
        try {
            code = connection.getResponseCode();
        } catch (IOException e) {
            return "";
        }
        InputStream stream;
        if (code >= 400) {
            stream = connection.getErrorStream();
        } else {
            stream = connection.getInputStream();
        }
        if (stream == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append('\n');
        }
        reader.close();
        return builder.toString().trim();
    }

    private static String errorMessage(int code, String body) {
        String detail = null;
        try {
            JSONObject json = new JSONObject(body);
            if (json.has("detail")) {
                detail = json.optString("detail", null);
            }
            if (detail == null) {
                JSONArray keys = json.names();
                if (keys != null && keys.length() > 0) {
                    String key = keys.optString(0, "");
                    Object value = json.opt(key);
                    if (value instanceof JSONArray) {
                        JSONArray array = (JSONArray) value;
                        if (array.length() > 0) detail = key + ": " + array.optString(0);
                    } else if (value != null) {
                        detail = key + ": " + value;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        if (detail != null && !detail.isEmpty()) {
            return detail;
        }
        if (code == 404) return "Not found. Check the server URL.";
        if (code == 400) return "The server rejected the request.";
        if (code >= 500) return "Server error (" + code + ")";
        if (code > 0) return "Request failed (" + code + ")";
        return "Request failed";
    }

    private static String friendlyError(Exception error) {
        if (error instanceof UnknownHostException) {
            return "Server not found. Check the URL.";
        }
        if (error instanceof ConnectException) {
            return "Could not connect to the server.";
        }
        if (error instanceof SocketTimeoutException) {
            return "The server took too long to respond.";
        }
        if (error instanceof SSLException) {
            return "TLS error. Try enabling self-signed certificates.";
        }
        if (error instanceof MalformedURLException) {
            return "Invalid server URL.";
        }
        String message = error.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = error.getClass().getSimpleName();
        }
        return message;
    }
}
