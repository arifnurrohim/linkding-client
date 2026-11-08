package app.arifnurrohim.linkding;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

public class SettingsPage {
    private final MainActivity activity;
    private final Catppuccin.Palette p;
    private final int accent;
    private final LinearLayout content;

    public SettingsPage(MainActivity activity) {
        this.activity = activity;
        this.p = Catppuccin.current(activity);
        this.accent = Catppuccin.accentColor(activity);

        ScrollView scrollView = new ScrollView(activity);
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(false);
        content = Ui.vertical(activity);
        content.setPadding(Ui.dp(activity, 16), Ui.dp(activity, 4),
                Ui.dp(activity, 16), Ui.dp(activity, 100));
        scrollView.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.root = scrollView;
    }

    private final View root;

    public View root() {
        return root;
    }

    public void build() {
        section("Appearance");

        LinearLayout flavors = card();
        for (int i = 0; i < Catppuccin.FLAVOR_KEYS.length; i++) {
            final String key = Catppuccin.FLAVOR_KEYS[i];
            Catppuccin.Palette flavor = Catppuccin.palette(key);
            boolean selected = Prefs.getFlavor(activity).equals(key);
            flavors.addView(flavorRow(flavor, selected));
            if (i < Catppuccin.FLAVOR_KEYS.length - 1) {
                flavors.addView(innerDivider());
            }
        }
        addCard(flavors);

        TextView accentLabel = Ui.text(activity, "Accent", 12f, p.subtext0, true);
        LinearLayout.LayoutParams accentLabelParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        accentLabelParams.topMargin = Ui.dp(activity, 18);
        accentLabelParams.bottomMargin = Ui.dp(activity, 8);
        content.addView(accentLabel, accentLabelParams);

        FlowLayout accentFlow = new FlowLayout(activity);
        accentFlow.setSpacing(Ui.dp(activity, 10), Ui.dp(activity, 10));
        for (String key : Catppuccin.ACCENT_KEYS) {
            accentFlow.addView(accentDot(key));
        }
        content.addView(accentFlow, Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        section("Preview");
        content.addView(previewCard());

        section("Behavior");
        LinearLayout behavior = card();
        behavior.setPadding(Ui.dp(activity, 15), Ui.dp(activity, 4),
                Ui.dp(activity, 15), Ui.dp(activity, 4));
        behavior.addView(toggleRow(Icons.LINK, "Show favicons",
                "Load site icons for each bookmark",
                Prefs.showFavicons(activity), new ToggleAction() {
                    @Override
                    public void onChanged(boolean value) {
                        Prefs.setShowFavicons(activity, value);
                        FaviconLoader.clear();
                        activity.onFaviconsChanged();
                    }
                }));

        behavior.addView(innerDivider());
        behavior.addView(toggleRow(Icons.SECURITY, "Trust self-signed certificates",
                "Only enable this for a server you trust",
                Prefs.trustAll(activity), new ToggleAction() {
                    @Override
                    public void onChanged(boolean value) {
                        Prefs.setTrustAll(activity, value);
                    }
                }));
        addCard(behavior);

        section("Accounts");
        LinearLayout accountsCard = card();
        List<Prefs.Account> accounts = Prefs.getAccounts(activity);
        for (int i = 0; i < accounts.size(); i++) {
            accountsCard.addView(accountRow(accounts.get(i)));
            accountsCard.addView(innerDivider());
        }
        accountsCard.addView(addAccountRow());
        addCard(accountsCard);

        section("Server");
        LinearLayout server = card();
        server.addView(infoRow(Icons.DNS, "Server", Prefs.host(activity)));
        server.addView(innerDivider());
        server.addView(infoRow(Icons.KEY, "API token", Prefs.maskedToken(activity)));

        View serverDivider = new View(activity);
        serverDivider.setBackgroundColor(p.surface1);
        LinearLayout.LayoutParams serverDividerParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Ui.dp(activity, 0.7f)));
        serverDividerParams.topMargin = Ui.dp(activity, 6);
        server.addView(serverDivider, serverDividerParams);

        LinearLayout serverButtons = Ui.horizontal(activity);
        serverButtons.setPadding(0, Ui.dp(activity, 12), 0, Ui.dp(activity, 4));

        TextView test = outlineButton("Test connection", accent);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testConnection();
            }
        });
        serverButtons.addView(test, Ui.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView disconnect = outlineButton("Disconnect", p.red);
        LinearLayout.LayoutParams disconnectParams = Ui.lp(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        disconnectParams.leftMargin = Ui.dp(activity, 10);
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDisconnect();
            }
        });
        serverButtons.addView(disconnect, disconnectParams);
        server.addView(serverButtons);
        addCard(server);

        section("About");
        LinearLayout about = card();
        about.setPadding(Ui.dp(activity, 16), Ui.dp(activity, 16),
                Ui.dp(activity, 16), Ui.dp(activity, 12));

        LinearLayout appRow = Ui.horizontal(activity);
        TextView appIcon = Ui.icon(activity, Icons.LINK, 24f, accent);
        appIcon.setBackground(Ui.rounded(activity, Catppuccin.withAlpha(accent, 0.15f), 12));
        appIcon.setGravity(Gravity.CENTER);
        appRow.addView(appIcon, Ui.lp(Ui.dp(activity, 46), Ui.dp(activity, 46)));

        LinearLayout appLabels = Ui.vertical(activity);
        LinearLayout.LayoutParams appLabelsParams = Ui.lp(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        appLabelsParams.leftMargin = Ui.dp(activity, 12);
        appRow.addView(appLabels, appLabelsParams);
        appLabels.addView(Ui.text(activity, "LinkDing", 17f, p.text, true));
        TextView version = Ui.text(activity, "Version 1.0.3 · app.arifnurrohim.linkding",
                11.5f, p.overlay1);
        LinearLayout.LayoutParams versionParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        versionParams.topMargin = Ui.dp(activity, 3);
        appLabels.addView(version, versionParams);
        about.addView(appRow);

        TextView description = Ui.text(activity,
                "A native Linkding client inspired by Linkdy. Bookmarks are shown as calm, "
                        + "rounded cards and every surface follows the Catppuccin palette.",
                12.5f, p.subtext0);
        description.setLineSpacing(Ui.dp(activity, 3), 1f);
        LinearLayout.LayoutParams descriptionParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descriptionParams.topMargin = Ui.dp(activity, 14);
        descriptionParams.bottomMargin = Ui.dp(activity, 6);
        about.addView(description, descriptionParams);

        about.addView(linkRow(Icons.PERSON, "Creator · arifnurrohim.my.id",
                "https://arifnurrohim.my.id/"));
        about.addView(linkRow(Icons.EMAIL, "jobs.arifnurrohim@gmail.com",
                "mailto:jobs.arifnurrohim@gmail.com"));
        about.addView(linkRow(Icons.SEND, "Telegram · @Inheritation",
                "https://t.me/Inheritation"));
        about.addView(innerDivider());

        about.addView(linkRow(Icons.EDIT, "Linkdy by JGeek00",
                "https://github.com/JGeek00/linkdy"));
        about.addView(linkRow(Icons.PALETTE, "Catppuccin style guide",
                "https://github.com/catppuccin/catppuccin/blob/main/docs/style-guide.md"));
        about.addView(linkRow(Icons.TERMINAL, "Material Icons by Google",
                "https://github.com/google/material-design-icons"));

        TextView disclaimer = Ui.text(activity,
                "Not affiliated with Linkding or Linkdy. Your bookmarks stay on your own server.",
                10.5f, p.overlay0);
        disclaimer.setLineSpacing(Ui.dp(activity, 2), 1f);
        LinearLayout.LayoutParams disclaimerParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        disclaimerParams.topMargin = Ui.dp(activity, 12);
        about.addView(disclaimer, disclaimerParams);
        addCard(about);
    }

    private void section(String title) {
        TextView label = Ui.text(activity, title.toUpperCase(), 11f, p.overlay1, true);
        label.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams params = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = Ui.dp(activity, 20);
        params.bottomMargin = Ui.dp(activity, 8);
        params.leftMargin = Ui.dp(activity, 4);
        content.addView(label, params);
    }

    private LinearLayout card() {
        LinearLayout card = Ui.vertical(activity);
        card.setBackground(Ui.roundedStroke(activity, p.surface0, p.surface1, 1, 18));
        card.setPadding(Ui.dp(activity, 4), Ui.dp(activity, 4),
                Ui.dp(activity, 4), Ui.dp(activity, 4));
        return card;
    }

    private void addCard(View card) {
        content.addView(card, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private View flavorRow(Catppuccin.Palette flavor, boolean selected) {
        LinearLayout row = Ui.horizontal(activity);
        row.setPadding(Ui.dp(activity, 12), Ui.dp(activity, 12),
                Ui.dp(activity, 12), Ui.dp(activity, 12));
        row.setBackground(Ui.roundedStroke(activity,
                selected ? Catppuccin.withAlpha(accent, 0.12f) : Color.TRANSPARENT,
                selected ? accent : Color.TRANSPARENT,
                selected ? 1.4f : 0f, 14));
        Ui.ripple(row, p.overlay2, 14);
        Ui.clickable(row);

        LinearLayout labels = Ui.vertical(activity);
        row.addView(labels, Ui.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        labels.addView(Ui.text(activity, flavor.name, 15f, p.text, true));
        TextView tag = Ui.text(activity, flavor.dark ? "Dark" : "Light", 11f, p.overlay1);
        LinearLayout.LayoutParams tagParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tagParams.topMargin = Ui.dp(activity, 3);
        labels.addView(tag, tagParams);

        int[] dots = new int[]{flavor.base, flavor.mauve, flavor.blue, flavor.text};
        LinearLayout dotRow = Ui.horizontal(activity);
        for (int i = 0; i < dots.length; i++) {
            View dot = new View(activity);
            dot.setBackground(Ui.roundedStroke(activity, dots[i], flavor.surface1, 1, 99));
            LinearLayout.LayoutParams dotParams = Ui.lp(
                    Ui.dp(activity, 16), Ui.dp(activity, 16));
            if (i > 0) dotParams.leftMargin = Ui.dp(activity, 4);
            dotRow.addView(dot, dotParams);
        }
        row.addView(dotRow);

        TextView check = Ui.icon(activity, selected ? Icons.CHECK_CIRCLE : Icons.RADIO_UNCHECKED,
                20f, selected ? accent : p.overlay0);
        LinearLayout.LayoutParams checkParams = Ui.lp(
                Ui.dp(activity, 26), Ui.dp(activity, 26));
        checkParams.leftMargin = Ui.dp(activity, 10);
        row.addView(check, checkParams);

        final String key = flavor.key;
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Prefs.getFlavor(activity).equals(key)) {
                    Prefs.setFlavor(activity, key);
                    activity.restartAtTab(2);
                }
            }
        });
        return row;
    }

    private View accentDot(final String key) {
        boolean selected = Prefs.getAccent(activity).equals(key);
        int color = p.accent(key);

        FrameLayout holder = new FrameLayout(activity);
        holder.setBackground(Ui.rounded(activity, Color.TRANSPARENT, 99));
        holder.setPadding(Ui.dp(activity, 3), Ui.dp(activity, 3),
                Ui.dp(activity, 3), Ui.dp(activity, 3));

        FrameLayout circle = new FrameLayout(activity);
        circle.setBackground(Ui.roundedStroke(activity, color, selected ? p.text : Color.TRANSPARENT,
                selected ? 2f : 0f, 99));
        holder.addView(circle, new FrameLayout.LayoutParams(
                Ui.dp(activity, 38), Ui.dp(activity, 38), Gravity.CENTER));

        if (selected) {
            TextView check = Ui.icon(activity, Icons.CHECK, 18f, Ui.onColor(activity, color));
            check.setGravity(Gravity.CENTER);
            circle.addView(check, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        Ui.ripple(holder, p.overlay2, 99);
        Ui.clickable(holder);
        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Prefs.getAccent(activity).equals(key)) {
                    Prefs.setAccent(activity, key);
                    activity.restartAtTab(2);
                }
            }
        });
        holder.setContentDescription(Catppuccin.accentName(key));
        return holder;
    }

    private View previewCard() {
        LinearLayout card = Ui.vertical(activity);
        card.setBackground(Ui.roundedStroke(activity, p.surface0, p.surface1, 1, 16));
        card.setPadding(Ui.dp(activity, 15), Ui.dp(activity, 14),
                Ui.dp(activity, 15), Ui.dp(activity, 12));
        Ui.ripple(card, p.overlay2, 16);

        LinearLayout top = Ui.horizontal(activity);
        View icon = new View(activity);
        icon.setBackground(Ui.roundedStroke(activity, p.surface1, p.surface2, 1, 5));
        LinearLayout.LayoutParams iconParams = Ui.lp(Ui.dp(activity, 20), Ui.dp(activity, 20));
        iconParams.rightMargin = Ui.dp(activity, 9);
        top.addView(icon, iconParams);

        TextView title = Ui.text(activity, "Catppuccin in LinkDing", 16f, p.text, true);
        title.setSingleLine(true);
        top.addView(title, Ui.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        View dot = new View(activity);
        dot.setBackground(Ui.oval(activity, accent));
        LinearLayout.LayoutParams dotParams = Ui.lp(Ui.dp(activity, 8), Ui.dp(activity, 8));
        dotParams.leftMargin = Ui.dp(activity, 8);
        top.addView(dot, dotParams);
        card.addView(top);

        TextView url = Ui.text(activity, "github.com/catppuccin", 12f, p.blue);
        LinearLayout.LayoutParams urlParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        urlParams.topMargin = Ui.dp(activity, 4);
        card.addView(url, urlParams);

        TextView description = Ui.text(activity,
                "Soothing pastel theme for the high-spirited. Cards use Surface 0 with a soft border, "
                        + "tags use Blue, and links use Blue.",
                13f, p.subtext0);
        description.setMaxLines(3);
        description.setLineSpacing(Ui.dp(activity, 2), 1f);
        LinearLayout.LayoutParams descriptionParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descriptionParams.topMargin = Ui.dp(activity, 6);
        card.addView(description, descriptionParams);

        FlowLayout tags = new FlowLayout(activity);
        tags.setSpacing(Ui.dp(activity, 6), Ui.dp(activity, 5));
        tags.addView(Ui.pill(activity, "#theme",
                Catppuccin.withAlpha(p.blue, 0.16f), p.blue));
        tags.addView(Ui.pill(activity, "#design",
                Catppuccin.withAlpha(p.blue, 0.16f), p.blue));
        LinearLayout.LayoutParams tagsParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tagsParams.topMargin = Ui.dp(activity, 8);
        card.addView(tags, tagsParams);

        TextView date = Ui.text(activity, "Today", 11f, p.overlay1);
        LinearLayout.LayoutParams dateParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dateParams.topMargin = Ui.dp(activity, 8);
        card.addView(date, dateParams);

        return card;
    }

    private View toggleRow(int iconCode, String title, String subtitle, boolean checked,
                           final ToggleAction action) {
        LinearLayout row = Ui.horizontal(activity);
        row.setPadding(Ui.dp(activity, 11), Ui.dp(activity, 11),
                Ui.dp(activity, 11), Ui.dp(activity, 11));

        TextView icon = Ui.icon(activity, iconCode, 19f, p.overlay1);
        row.addView(icon, Ui.lp(Ui.dp(activity, 28), Ui.dp(activity, 28)));

        LinearLayout labels = Ui.vertical(activity);
        LinearLayout.LayoutParams labelsParams = Ui.lp(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        labelsParams.leftMargin = Ui.dp(activity, 10);
        row.addView(labels, labelsParams);
        labels.addView(Ui.text(activity, title, 14.5f, p.text, true));
        TextView subtitleView = Ui.text(activity, subtitle, 11.5f, p.overlay1);
        LinearLayout.LayoutParams subtitleParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = Ui.dp(activity, 3);
        labels.addView(subtitleView, subtitleParams);

        final Switch toggle = new Switch(activity);
        toggle.setChecked(checked);
        Ui.tintSwitch(toggle, accent, Catppuccin.withAlpha(accent, 0.45f),
                p.overlay0, p.surface2);
        toggle.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                action.onChanged(isChecked);
            }
        });
        row.addView(toggle);
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle.setChecked(!toggle.isChecked());
            }
        });
        return row;
    }

    private View accountRow(final Prefs.Account account) {
        final boolean current = Prefs.isCurrent(activity, account);
        LinearLayout row = Ui.horizontal(activity);
        row.setPadding(Ui.dp(activity, 11), Ui.dp(activity, 10),
                Ui.dp(activity, 11), Ui.dp(activity, 10));
        row.setBackground(Ui.roundedStroke(activity,
                current ? Catppuccin.withAlpha(accent, 0.12f) : Color.TRANSPARENT,
                current ? accent : Color.TRANSPARENT,
                current ? 1.4f : 0f, 14));
        Ui.ripple(row, p.overlay2, 14);
        Ui.clickable(row);

        TextView icon = Ui.icon(activity, Icons.DNS, 18f, current ? accent : p.overlay1);
        row.addView(icon, Ui.lp(Ui.dp(activity, 28), Ui.dp(activity, 28)));

        LinearLayout labels = Ui.vertical(activity);
        LinearLayout.LayoutParams labelsParams = Ui.lp(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        labelsParams.leftMargin = Ui.dp(activity, 10);
        row.addView(labels, labelsParams);
        labels.addView(Ui.text(activity, account.label(), 14f, p.text, true));
        TextView token = Ui.text(activity, Prefs.maskToken(account.token), 11.5f, p.overlay1);
        LinearLayout.LayoutParams tokenParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tokenParams.topMargin = Ui.dp(activity, 3);
        labels.addView(token, tokenParams);

        if (current) {
            TextView check = Ui.icon(activity, Icons.CHECK_CIRCLE, 20f, accent);
            row.addView(check, Ui.lp(Ui.dp(activity, 26), Ui.dp(activity, 26)));
        } else {
            TextView remove = Ui.icon(activity, Icons.DELETE, 18f, p.overlay1);
            remove.setPadding(Ui.dp(activity, 8), Ui.dp(activity, 8),
                    Ui.dp(activity, 8), Ui.dp(activity, 8));
            Ui.ripple(remove, p.red, 99);
            Ui.clickable(remove);
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmRemoveAccount(account);
                }
            });
            LinearLayout.LayoutParams removeParams = Ui.lp(
                    Ui.dp(activity, 34), Ui.dp(activity, 34));
            removeParams.leftMargin = Ui.dp(activity, 6);
            row.addView(remove, removeParams);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Prefs.switchAccount(activity, account);
                    FaviconLoader.clear();
                    Ui.snack(activity, "Switched to " + account.label());
                    activity.restartAtTab(0);
                }
            });
        }
        return row;
    }

    private View addAccountRow() {
        LinearLayout row = Ui.horizontal(activity);
        row.setPadding(Ui.dp(activity, 11), Ui.dp(activity, 11),
                Ui.dp(activity, 11), Ui.dp(activity, 11));
        row.setBackground(Ui.rounded(activity, Color.TRANSPARENT, 14));
        Ui.ripple(row, p.overlay2, 14);
        Ui.clickable(row);

        TextView icon = Ui.icon(activity, Icons.ADD, 18f, accent);
        row.addView(icon, Ui.lp(Ui.dp(activity, 28), Ui.dp(activity, 28)));

        LinearLayout labels = Ui.vertical(activity);
        LinearLayout.LayoutParams labelsParams = Ui.lp(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        labelsParams.leftMargin = Ui.dp(activity, 10);
        row.addView(labels, labelsParams);
        labels.addView(Ui.text(activity, "Add account", 14f, p.text, true));
        TextView subtitle = Ui.text(activity, "Connect another Linkding server", 11.5f, p.overlay1);
        LinearLayout.LayoutParams subtitleParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = Ui.dp(activity, 3);
        labels.addView(subtitle, subtitleParams);

        row.addView(Ui.icon(activity, Icons.CHEVRON, 18f, p.overlay0),
                Ui.lp(Ui.dp(activity, 24), Ui.dp(activity, 24)));

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ConnectActivity.class);
                intent.putExtra("edit", true);
                intent.putExtra("add", true);
                activity.startActivity(intent);
            }
        });
        return row;
    }

    private void confirmRemoveAccount(final Prefs.Account account) {
        Dialogs.confirm(activity, "Remove account?",
                "This forgets the saved server URL and API token for "
                        + account.label() + ". You can add it again at any time.",
                "Remove", true, new Runnable() {
                    @Override
                    public void run() {
                        Prefs.removeAccount(activity, account);
                        activity.restartAtTab(2);
                    }
                });
    }

    private View infoRow(int iconCode, String label, String value) {
        LinearLayout row = Ui.horizontal(activity);
        row.setPadding(Ui.dp(activity, 11), Ui.dp(activity, 12),
                Ui.dp(activity, 11), Ui.dp(activity, 12));
        TextView icon = Ui.icon(activity, iconCode, 18f, p.overlay1);
        row.addView(icon, Ui.lp(Ui.dp(activity, 26), Ui.dp(activity, 26)));
        row.addView(Ui.text(activity, label, 13.5f, p.subtext0), Ui.lp(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView valueView = Ui.text(activity, value, 13f, p.text, true);
        valueView.setSingleLine(true);
        valueView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        row.addView(valueView);
        return row;
    }

    private TextView outlineButton(String label, int color) {
        TextView button = Ui.text(activity, label, 13.5f, color, true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(Ui.roundedStroke(activity, Color.TRANSPARENT, color, 1, 12));
        button.setPadding(Ui.dp(activity, 10), Ui.dp(activity, 11),
                Ui.dp(activity, 10), Ui.dp(activity, 11));
        Ui.ripple(button, color, 12);
        Ui.clickable(button);
        return button;
    }

    private View linkRow(int iconCode, String label, final String url) {
        LinearLayout row = Ui.horizontal(activity);
        row.setPadding(0, Ui.dp(activity, 12), 0, Ui.dp(activity, 12));
        Ui.clickable(row);
        row.setBackground(Ui.rounded(activity, Color.TRANSPARENT, 10));
        Ui.ripple(row, p.overlay2, 10);

        TextView icon = Ui.icon(activity, iconCode, 17f, p.blue);
        row.addView(icon, Ui.lp(Ui.dp(activity, 26), Ui.dp(activity, 26)));
        TextView labelView = Ui.text(activity, label, 13.5f, p.blue);
        LinearLayout.LayoutParams labelParams = Ui.lp(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        labelParams.leftMargin = Ui.dp(activity, 8);
        row.addView(labelView, labelParams);
        row.addView(Ui.icon(activity, Icons.OPEN_IN_NEW, 15f, p.overlay1),
                Ui.lp(Ui.dp(activity, 20), Ui.dp(activity, 20)));
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception ignored) {
                }
            }
        });
        return row;
    }

    private View innerDivider() {
        View divider = new View(activity);
        divider.setBackgroundColor(p.surface1);
        LinearLayout.LayoutParams params = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Ui.dp(activity, 0.7f)));
        params.leftMargin = Ui.dp(activity, 10);
        params.rightMargin = Ui.dp(activity, 10);
        divider.setLayoutParams(params);
        return divider;
    }

    private void testConnection() {
        Ui.snack(activity, "Testing connection…");
        Api.from(activity).testConnection(new Api.Callback<Boolean>() {
            @Override
            public void onResult(Api.Result<Boolean> result) {
                if (activity.isFinishing()) return;
                if (result.ok) {
                    Ui.snack(activity, "Connected to " + Prefs.host(activity));
                } else {
                    Ui.snack(activity, result.unauthorized()
                            ? "Invalid API token" : result.error);
                }
            }
        });
    }

    private void confirmDisconnect() {
        Prefs.Account current = Prefs.getCurrentAccount(activity);
        final String label = current != null ? current.label() : Prefs.host(activity);
        Dialogs.confirm(activity, "Disconnect " + label + "?",
                "This forgets the saved server URL and API token for " + label
                        + ". Other saved accounts stay available.",
                "Disconnect", true, new Runnable() {
                    @Override
                    public void run() {
                        Prefs.Account active = Prefs.getCurrentAccount(activity);
                        if (active != null) Prefs.removeAccount(activity, active);
                        Prefs.setServer(activity, "");
                        Prefs.setToken(activity, "");
                        FaviconLoader.clear();
                        List<Prefs.Account> remaining = Prefs.getAccounts(activity);
                        if (!remaining.isEmpty()) {
                            Prefs.Account next = remaining.get(0);
                            Prefs.switchAccount(activity, next);
                            Ui.snack(activity, "Switched to " + next.label());
                            activity.restartAtTab(0);
                            return;
                        }
                        Prefs.clear(activity);
                        Intent intent = new Intent(activity, ConnectActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                });
    }

    private interface ToggleAction {
        void onChanged(boolean value);
    }
}
