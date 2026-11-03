package app.arifnurrohim.linkding;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Bookmark {
    public int id;
    public String url = "";
    public String title = "";
    public String description = "";
    public String notes = "";
    public String websiteTitle = "";
    public String websiteDescription = "";
    public boolean archived;
    public boolean unread;
    public boolean shared;
    public final List<String> tags = new ArrayList<>();
    public String dateAdded = "";
    public String dateModified = "";

    public static Bookmark fromJson(JSONObject json) {
        Bookmark bookmark = new Bookmark();
        bookmark.id = json.optInt("id", 0);
        bookmark.url = string(json, "url");
        bookmark.title = string(json, "title");
        bookmark.description = string(json, "description");
        bookmark.notes = string(json, "notes");
        bookmark.websiteTitle = string(json, "website_title");
        bookmark.websiteDescription = string(json, "website_description");
        bookmark.archived = json.optBoolean("is_archived", false);
        bookmark.unread = json.optBoolean("unread", false);
        bookmark.shared = json.optBoolean("shared", false);
        bookmark.dateAdded = string(json, "date_added");
        bookmark.dateModified = string(json, "date_modified");
        JSONArray tagArray = json.optJSONArray("tag_names");
        if (tagArray != null) {
            for (int i = 0; i < tagArray.length(); i++) {
                String tag = tagArray.optString(i, "").trim();
                if (!tag.isEmpty()) bookmark.tags.add(tag);
            }
        }
        return bookmark;
    }

    private static String string(JSONObject json, String key) {
        if (json.isNull(key)) return "";
        String value = json.optString(key, "");
        return value == null ? "" : value;
    }

    public String displayTitle() {
        if (!title.trim().isEmpty()) return title.trim();
        if (!websiteTitle.trim().isEmpty()) return websiteTitle.trim();
        if (!url.trim().isEmpty()) return url;
        return "Untitled bookmark";
    }

    public String displayDescription() {
        if (!description.trim().isEmpty()) return description.trim();
        if (!websiteDescription.trim().isEmpty()) return websiteDescription.trim();
        return "";
    }

    public String host() {
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host != null) {
                return host.startsWith("www.") ? host.substring(4) : host;
            }
        } catch (Exception ignored) {
        }
        return url;
    }

    public String readableUrl() {
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host == null) return url;
            if (host.startsWith("www.")) host = host.substring(4);
            String path = uri.getEncodedPath();
            if (path == null || path.equals("/")) return host;
            if (path.length() > 40) path = path.substring(0, 40) + "…";
            return host + path;
        } catch (Exception ignored) {
            return url;
        }
    }

    public String relativeDate() {
        String value = dateAdded;
        if (value == null || value.isEmpty()) return "";
        try {
            OffsetDateTime parsed = OffsetDateTime.parse(value);
            LocalDate date = parsed.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
            return formatDate(date);
        } catch (Exception ignored) {
        }
        try {
            Instant instant = Instant.parse(value);
            LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            return formatDate(date);
        } catch (Exception ignored) {
        }
        return "";
    }

    private static String formatDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(date, today);
        if (days <= 0) return "Today";
        if (days == 1) return "Yesterday";
        if (days < 7) return days + "d ago";
        if (days < 30) return (days / 7) + "w ago";
        if (date.getYear() == today.getYear()) {
            return DateTimeFormatter.ofPattern("MMM d").format(date);
        }
        return DateTimeFormatter.ofPattern("MMM d, yyyy").format(date);
    }

    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("url", url);
            json.put("title", title == null ? "" : title);
            json.put("description", description == null ? "" : description);
            json.put("notes", notes == null ? "" : notes);
            JSONArray tagArray = new JSONArray();
            for (String tag : tags) {
                if (!tag.trim().isEmpty()) tagArray.put(tag.trim());
            }
            json.put("tag_names", tagArray);
            json.put("unread", unread);
            json.put("shared", shared);
            json.put("is_archived", archived);
            return json;
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
