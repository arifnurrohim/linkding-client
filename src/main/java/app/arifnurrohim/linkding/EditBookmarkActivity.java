package app.arifnurrohim.linkding;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EditBookmarkActivity extends Activity {
    public static final String EXTRA_ID = "bookmark_id";
    public static final String EXTRA_URL = "bookmark_url";
    public static final String EXTRA_TITLE = "bookmark_title";
    public static final String EXTRA_DESCRIPTION = "bookmark_description";
    public static final String EXTRA_NOTES = "bookmark_notes";
    public static final String EXTRA_TAGS = "bookmark_tags";
    public static final String EXTRA_UNREAD = "bookmark_unread";
    public static final String EXTRA_SHARED = "bookmark_shared";
    public static final String EXTRA_ARCHIVED = "bookmark_archived";

    private Catppuccin.Palette p;
    private int accent;
    private int id;
    private boolean isNew;
    private boolean saving;

    private EditText urlInput;
    private EditText titleInput;
    private EditText descriptionInput;
    private EditText tagsInput;
    private EditText notesInput;
    private Switch unreadSwitch;
    private Switch sharedSwitch;
    private Switch archivedSwitch;
    private TextView saveButton;
    private TextView duplicateWarning;
    private FlowLayout suggestions;
    private LinearLayout suggestionsWrapper;
    private List<Tag> allTags = new ArrayList<>();
    private String lastCheckedUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Theme.apply(this);
        p = Catppuccin.current(this);
        accent = Catppuccin.accentColor(this);

        Intent intent = getIntent();
        id = intent.getIntExtra(EXTRA_ID, 0);
        isNew = id == 0;

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(p.base);
        setContentView(root);

        LinearLayout page = Ui.vertical(this);
        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout topBar = Ui.horizontal(this);
        topBar.setPadding(Ui.dp(this, 12), Ui.dp(this, 8), Ui.dp(this, 14), Ui.dp(this, 8));

        TextView back = Ui.icon(this, Icons.BACK, 22f, p.text);
        back.setBackground(Ui.rounded(this, Color.TRANSPARENT, 99));
        Ui.ripple(back, p.overlay2, 99);
        Ui.clickable(back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        topBar.addView(back, Ui.lp(Ui.dp(this, 40), Ui.dp(this, 40)));

        TextView heading = Ui.text(this, isNew ? "New bookmark" : "Edit bookmark", 18f, p.text, true);
        LinearLayout.LayoutParams headingParams = Ui.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        headingParams.leftMargin = Ui.dp(this, 8);
        topBar.addView(heading, headingParams);

        saveButton = Ui.text(this, "Save", 13f, Ui.onColor(this, accent), true);
        saveButton.setGravity(Gravity.CENTER);
        saveButton.setBackground(Ui.rounded(this, accent, 11));
        saveButton.setPadding(Ui.dp(this, 17), Ui.dp(this, 9), Ui.dp(this, 17), Ui.dp(this, 9));
        Ui.ripple(saveButton, p.base, 11);
        Ui.clickable(saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        topBar.addView(saveButton);
        page.addView(topBar, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        View topDivider = new View(this);
        topDivider.setBackgroundColor(p.surface0);
        page.addView(topDivider, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Ui.dp(this, 0.7f))));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(false);
        page.addView(scrollView, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout content = Ui.vertical(this);
        content.setPadding(Ui.dp(this, 20), Ui.dp(this, 18), Ui.dp(this, 20), Ui.dp(this, 40));
        scrollView.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        urlInput = Ui.input(this, "https://example.com/article",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI, false);
        urlInput.setText(intent.getStringExtra(EXTRA_URL));
        content.addView(field("URL", true, urlInput, null));

        titleInput = Ui.input(this, "Leave empty to let Linkding fetch it",
                InputType.TYPE_CLASS_TEXT, false);
        titleInput.setText(intent.getStringExtra(EXTRA_TITLE));
        content.addView(field("Title", false, titleInput, null));

        descriptionInput = Ui.input(this, "A short description",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES, true);
        descriptionInput.setText(intent.getStringExtra(EXTRA_DESCRIPTION));
        content.addView(field("Description", false, descriptionInput, null));

        tagsInput = Ui.input(this, "comma, separated, tags",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS, false);
        tagsInput.setText(intent.getStringExtra(EXTRA_TAGS));
        content.addView(field("Tags", false, tagsInput, null));

        notesInput = Ui.input(this, "Private notes about this bookmark",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES, true);
        notesInput.setText(intent.getStringExtra(EXTRA_NOTES));
        content.addView(field("Notes", false, notesInput, null));

        duplicateWarning = Ui.text(this, "", 12f, p.yellow, true);
        duplicateWarning.setVisibility(View.GONE);
        duplicateWarning.setLineSpacing(Ui.dp(this, 2), 1f);
        LinearLayout.LayoutParams warningParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        warningParams.bottomMargin = Ui.dp(this, 10);
        content.addView(duplicateWarning, warningParams);

        if (!isNew) {
            urlInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    duplicateWarning.setVisibility(View.GONE);
                }
            });
        } else {
            urlInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) checkDuplicate();
                }
            });
        }

        suggestionsWrapper = Ui.vertical(this);
        suggestionsWrapper.setVisibility(View.GONE);
        content.addView(suggestionsWrapper);

        TextView suggestionsLabel = Ui.text(this, "Your tags", 12f, p.subtext0, true);
        LinearLayout.LayoutParams suggestionsLabelParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        suggestionsLabelParams.topMargin = Ui.dp(this, 2);
        suggestionsLabelParams.bottomMargin = Ui.dp(this, 8);
        suggestionsWrapper.addView(suggestionsLabel, suggestionsLabelParams);

        suggestions = new FlowLayout(this);
        suggestions.setSpacing(Ui.dp(this, 6), Ui.dp(this, 6));
        suggestionsWrapper.addView(suggestions, Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView optionsLabel = Ui.text(this, "Options", 12f, p.subtext0, true);
        LinearLayout.LayoutParams optionsLabelParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        optionsLabelParams.topMargin = Ui.dp(this, 8);
        optionsLabelParams.bottomMargin = Ui.dp(this, 8);
        content.addView(optionsLabel, optionsLabelParams);

        LinearLayout optionsCard = Ui.vertical(this);
        optionsCard.setBackground(Ui.roundedStroke(this, p.surface0, p.surface1, 1, 16));
        optionsCard.setPadding(Ui.dp(this, 15), Ui.dp(this, 5), Ui.dp(this, 15), Ui.dp(this, 5));
        content.addView(optionsCard, Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        unreadSwitch = addSwitchRow(optionsCard, "Unread", "Keep this bookmark in your unread list",
                intent.getBooleanExtra(EXTRA_UNREAD, false));
        sharedSwitch = addSwitchRow(optionsCard, "Shared", "Share with other Linkding users",
                intent.getBooleanExtra(EXTRA_SHARED, false));
        archivedSwitch = addSwitchRow(optionsCard, "Archived", "Hide from the default bookmark list",
                intent.getBooleanExtra(EXTRA_ARCHIVED, false));

        TextView saveLarge = Ui.text(this, isNew ? "Add bookmark" : "Save changes", 15f,
                Ui.onColor(this, accent), true);
        saveLarge.setGravity(Gravity.CENTER);
        saveLarge.setBackground(Ui.rounded(this, accent, 14));
        Ui.ripple(saveLarge, p.base, 14);
        Ui.clickable(saveLarge);
        saveLarge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        LinearLayout.LayoutParams saveLargeParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 50));
        saveLargeParams.topMargin = Ui.dp(this, 24);
        content.addView(saveLarge, saveLargeParams);

        loadTags();
        Theme.handleInsets(this, root);
    }

    private View field(String label, boolean required, EditText edit, View trailing) {
        LinearLayout wrapper = Ui.vertical(this);
        LinearLayout labelRow = Ui.horizontal(this);
        TextView labelView = Ui.text(this, label, 12f, p.subtext0, true);
        labelRow.addView(labelView, Ui.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (required) {
            TextView requiredView = Ui.text(this, "required", 10.5f, p.overlay0, true);
            labelRow.addView(requiredView);
        }
        wrapper.addView(labelRow);

        LinearLayout box = Ui.horizontal(this);
        box.setGravity(Gravity.TOP);
        box.setBackground(Ui.roundedStroke(this, p.surface0, p.surface1, 1, 12));
        box.setPadding(Ui.dp(this, 13), Ui.dp(this, 12), Ui.dp(this, 13), Ui.dp(this, 12));
        box.addView(edit, Ui.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (trailing != null) box.addView(trailing);
        LinearLayout.LayoutParams boxParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        boxParams.topMargin = Ui.dp(this, 6);
        wrapper.addView(box, boxParams);

        LinearLayout.LayoutParams wrapperParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wrapperParams.bottomMargin = Ui.dp(this, 16);
        wrapper.setLayoutParams(wrapperParams);
        return wrapper;
    }

    private Switch addSwitchRow(LinearLayout parent, String title, String subtitle, boolean checked) {
        LinearLayout row = Ui.horizontal(this);
        row.setPadding(0, Ui.dp(this, 11), 0, Ui.dp(this, 11));

        LinearLayout labels = Ui.vertical(this);
        TextView titleView = Ui.text(this, title, 14.5f, p.text, true);
        TextView subtitleView = Ui.text(this, subtitle, 11.5f, p.overlay1);
        labels.addView(titleView);
        LinearLayout.LayoutParams subtitleParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = Ui.dp(this, 3);
        labels.addView(subtitleView, subtitleParams);
        row.addView(labels, Ui.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        final Switch toggle = new Switch(this);
        toggle.setChecked(checked);
        Ui.tintSwitch(toggle, accent, Catppuccin.withAlpha(accent, 0.45f),
                p.overlay0, p.surface2);
        row.addView(toggle);

        final View.OnClickListener clicker = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle.setChecked(!toggle.isChecked());
            }
        };
        row.setOnClickListener(clicker);
        Ui.clickable(row);

        if (parent.getChildCount() > 0) {
            View divider = new View(this);
            divider.setBackgroundColor(p.surface1);
            LinearLayout.LayoutParams dividerParams = Ui.lp(
                    ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Ui.dp(this, 0.7f)));
            parent.addView(divider, dividerParams);
        }
        parent.addView(row, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return toggle;
    }

    private void loadTags() {
        Api.from(this).listTags(null, new Api.Callback<List<Tag>>() {
            @Override
            public void onResult(Api.Result<List<Tag>> result) {
                if (isFinishing() || !result.ok || result.data == null) return;
                allTags = result.data;
                renderSuggestions();
            }
        });
    }

    private void renderSuggestions() {
        if (allTags.isEmpty()) {
            suggestionsWrapper.setVisibility(View.GONE);
            return;
        }
        suggestions.removeAllViews();
        Set<String> selected = currentTags();
        int shown = 0;
        for (final Tag tag : allTags) {
            if (tag.name == null || tag.name.trim().isEmpty()) continue;
            if (shown++ >= 30) break;
            final boolean isSelected = selected.contains(tag.name);
            TextView pill = Ui.pill(this, "#" + tag.name,
                    isSelected ? Catppuccin.withAlpha(accent, 0.22f)
                            : Catppuccin.withAlpha(p.blue, 0.13f),
                    isSelected ? accent : p.blue);
            pill.setPadding(Ui.dp(this, 10), Ui.dp(this, 5), Ui.dp(this, 10), Ui.dp(this, 5));
            LinearLayout.LayoutParams params = Ui.lp(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.rightMargin = Ui.dp(this, 2);
            params.bottomMargin = Ui.dp(this, 2);
            pill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleTag(tag.name);
                }
            });
            suggestions.addView(pill, params);
        }
        suggestionsWrapper.setVisibility(suggestions.getChildCount() == 0 ? View.GONE : View.VISIBLE);
    }

    private Set<String> currentTags() {
        Set<String> tags = new LinkedHashSet<>();
        for (String part : tagsInput.getText().toString().split("[,\\s]+")) {
            String tag = part.trim();
            if (!tag.isEmpty()) tags.add(tag);
        }
        return tags;
    }

    private void toggleTag(String tag) {
        Set<String> tags = currentTags();
        if (tags.contains(tag)) {
            tags.remove(tag);
        } else {
            tags.add(tag);
        }
        tagsInput.setText(TextUtils.join(", ", tags));
        renderSuggestions();
    }

    private void checkDuplicate() {
        String url = normalizeUrl(urlInput.getText().toString());
        if (url.isEmpty() || url.equals(lastCheckedUrl) || !url.startsWith("http")) return;
        lastCheckedUrl = url;
        Api.from(this).checkUrl(url, new Api.Callback<Bookmark>() {
            @Override
            public void onResult(Api.Result<Bookmark> result) {
                if (isFinishing() || !result.ok || result.data == null) return;
                duplicateWarning.setText("Already saved: " + result.data.displayTitle());
                duplicateWarning.setVisibility(View.VISIBLE);
            }
        });
    }

    private String normalizeUrl(String value) {
        String url = value == null ? "" : value.trim();
        if (url.isEmpty()) return "";
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        return url;
    }

    private void save() {
        if (saving) return;
        String url = normalizeUrl(urlInput.getText().toString());
        if (TextUtils.isEmpty(url)) {
            urlInput.requestFocus();
            Ui.snack(this, "Enter a URL first.");
            return;
        }
        if (!url.matches("(?i)^https?://.+")) {
            urlInput.requestFocus();
            Ui.snack(this, "That URL doesn't look right.");
            return;
        }

        hideKeyboard();
        saving = true;
        saveButton.setText("Saving…");
        saveButton.setAlpha(0.7f);
        saveButton.setEnabled(false);

        Bookmark bookmark = new Bookmark();
        bookmark.id = id;
        bookmark.url = url;
        bookmark.title = titleInput.getText().toString().trim();
        bookmark.description = descriptionInput.getText().toString().trim();
        bookmark.notes = notesInput.getText().toString().trim();
        bookmark.tags.addAll(currentTags());
        bookmark.unread = unreadSwitch.isChecked();
        bookmark.shared = sharedSwitch.isChecked();
        bookmark.archived = archivedSwitch.isChecked();

        Api.from(this).save(bookmark, isNew, new Api.Callback<Bookmark>() {
            @Override
            public void onResult(Api.Result<Bookmark> result) {
                if (isFinishing()) return;
                saving = false;
                saveButton.setText("Save");
                saveButton.setAlpha(1f);
                saveButton.setEnabled(true);
                if (result.ok) {
                    Toast.makeText(EditBookmarkActivity.this,
                            isNew ? "Bookmark added" : "Bookmark updated",
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Ui.snack(EditBookmarkActivity.this, result.unauthorized()
                            ? "Invalid API token. Reconnect in settings."
                            : result.error);
                }
            }
        });
    }

    private void hideKeyboard() {
        try {
            InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (manager != null) {
                manager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            }
        } catch (Exception ignored) {
        }
    }
}
