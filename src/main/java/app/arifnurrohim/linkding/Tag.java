package app.arifnurrohim.linkding;

import org.json.JSONObject;

public class Tag {
    public int id;
    public String name = "";
    public String dateAdded = "";

    public static Tag fromJson(JSONObject json) {
        Tag tag = new Tag();
        tag.id = json.optInt("id", 0);
        tag.name = json.optString("name", "");
        tag.dateAdded = json.optString("date_added", "");
        return tag;
    }
}
