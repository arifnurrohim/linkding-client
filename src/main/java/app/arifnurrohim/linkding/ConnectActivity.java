package app.arifnurrohim.linkding;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

public class ConnectActivity extends Activity {
    private EditText serverInput;
    private EditText tokenInput;
    private TextView errorView;
    private TextView connectButton;
    private Switch trustSwitch;
    private boolean connecting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Theme.apply(this);

        if (Prefs.isConfigured(this) && !getIntent().getBooleanExtra("edit", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        boolean addMode = getIntent().getBooleanExtra("add", false);

        Catppuccin.Palette p = Catppuccin.current(this);
        int accent = Catppuccin.accentColor(this);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(p.base);
        setContentView(root);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(false);
        root.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout content = Ui.vertical(this);
        content.setPadding(Ui.dp(this, 24), Ui.dp(this, 20), Ui.dp(this, 24), Ui.dp(this, 32));
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        scrollView.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView logo = Ui.icon(this, Icons.LINK, 40f, accent);
        logo.setBackground(Ui.rounded(this, Catppuccin.withAlpha(accent, 0.15f), 22));
        LinearLayout.LayoutParams logoParams = Ui.lp(Ui.dp(this, 78), Ui.dp(this, 78));
        logoParams.topMargin = Ui.dp(this, 24);
        content.addView(logo, logoParams);

        TextView title = Ui.text(this, "LinkDing", 30f, p.text, true);
        LinearLayout.LayoutParams titleParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = Ui.dp(this, 18);
        content.addView(title, titleParams);

        TextView subtitle = Ui.text(this, "A calm place for your bookmarks", 14f, p.subtext0);
        LinearLayout.LayoutParams subtitleParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = Ui.dp(this, 6);
        content.addView(subtitle, subtitleParams);

        LinearLayout card = Ui.vertical(this);
        card.setBackground(Ui.roundedStroke(this, p.surface0, p.surface1, 1, 20));
        card.setPadding(Ui.dp(this, 18), Ui.dp(this, 18), Ui.dp(this, 18), Ui.dp(this, 18));
        LinearLayout.LayoutParams cardParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = Ui.dp(this, 30);
        content.addView(card, cardParams);

        TextView connectTitle = Ui.text(this,
                addMode ? "Add another server" : "Connect your server", 17f, p.text, true);
        card.addView(connectTitle);

        TextView connectHint = Ui.text(this, addMode
                        ? "Your current account stays saved so you can switch back later."
                        : "Your data stays on your own Linkding instance.",
                12.5f, p.subtext0);
        LinearLayout.LayoutParams connectHintParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        connectHintParams.topMargin = Ui.dp(this, 5);
        connectHintParams.bottomMargin = Ui.dp(this, 18);
        card.addView(connectHint, connectHintParams);

        serverInput = Ui.input(this, "https://linkding.example.com",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI, false);
        serverInput.setText(addMode ? "" : Prefs.getServer(this));
        card.addView(field("Server URL", serverInput, null));

        tokenInput = Ui.input(this, "Paste your API token",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD, false);
        tokenInput.setText(addMode ? "" : Prefs.getToken(this));

        TextView reveal = Ui.icon(this, Icons.VISIBILITY, 19f, p.overlay1);
        reveal.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8));
        Ui.ripple(reveal, p.overlay2, 99);
        Ui.clickable(reveal);
        reveal.setOnClickListener(new View.OnClickListener() {
            private boolean visible;

            @Override
            public void onClick(View v) {
                visible = !visible;
                tokenInput.setInputType(InputType.TYPE_CLASS_TEXT
                        | (visible ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        : InputType.TYPE_TEXT_VARIATION_PASSWORD));
                tokenInput.setSelection(tokenInput.getText().length());
                ((TextView) v).setText(new String(Character.toChars(
                        visible ? Icons.VISIBILITY_OFF : Icons.VISIBILITY)));
            }
        });
        card.addView(field("API token", tokenInput, reveal));

        LinearLayout trustRow = Ui.horizontal(this);
        trustRow.setPadding(0, Ui.dp(this, 2), 0, 0);
        TextView trustLabel = Ui.text(this, "Trust self-signed certificates", 13f, p.subtext0);
        trustRow.addView(trustLabel, Ui.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        trustSwitch = new Switch(this);
        trustSwitch.setChecked(!addMode && Prefs.trustAll(this));
        Ui.tintSwitch(trustSwitch, accent, Catppuccin.withAlpha(accent, 0.45f),
                p.overlay0, p.surface2);
        trustRow.addView(trustSwitch);
        LinearLayout.LayoutParams trustParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        trustParams.topMargin = Ui.dp(this, 14);
        card.addView(trustRow, trustParams);

        errorView = Ui.text(this, "", 12.5f, p.red);
        errorView.setVisibility(View.GONE);
        errorView.setLineSpacing(Ui.dp(this, 2), 1f);
        LinearLayout.LayoutParams errorParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        errorParams.topMargin = Ui.dp(this, 12);
        card.addView(errorView, errorParams);

        connectButton = Ui.text(this, "Connect", 15f, Ui.onColor(this, accent), true);
        connectButton.setGravity(Gravity.CENTER);
        connectButton.setBackground(Ui.rounded(this, accent, 14));
        Ui.ripple(connectButton, p.base, 14);
        Ui.clickable(connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
        LinearLayout.LayoutParams buttonParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 50));
        buttonParams.topMargin = Ui.dp(this, 20);
        card.addView(connectButton, buttonParams);

        TextView help = Ui.text(this,
                "Find your token in Linkding under Settings → Integrations. "
                        + "The server URL is the address you use in your browser.",
                12f, p.overlay1);
        help.setLineSpacing(Ui.dp(this, 3), 1f);
        LinearLayout.LayoutParams helpParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        helpParams.topMargin = Ui.dp(this, 16);
        card.addView(help, helpParams);

        TextView footer = Ui.text(this, "Based on Linkdy · Themed with Catppuccin", 11f, p.overlay0);
        LinearLayout.LayoutParams footerParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        footerParams.topMargin = Ui.dp(this, 26);
        content.addView(footer, footerParams);

        tokenInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                    connect();
                    return true;
                }
                return false;
            }
        });

        Theme.handleInsets(this, root);
    }

    private View field(String label, EditText edit, View trailing) {
        Catppuccin.Palette p = Catppuccin.current(this);
        LinearLayout wrapper = Ui.vertical(this);
        TextView labelView = Ui.text(this, label, 12f, p.subtext0, true);
        wrapper.addView(labelView);

        LinearLayout box = Ui.horizontal(this);
        box.setBackground(Ui.roundedStroke(this, p.base, p.surface1, 1, 12));
        box.setPadding(Ui.dp(this, 13), Ui.dp(this, 12), Ui.dp(this, 13), Ui.dp(this, 12));
        box.addView(edit, Ui.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (trailing != null) {
            box.addView(trailing);
        }
        LinearLayout.LayoutParams boxParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        boxParams.topMargin = Ui.dp(this, 6);
        wrapper.addView(box, boxParams);

        LinearLayout.LayoutParams wrapperParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wrapperParams.bottomMargin = Ui.dp(this, 14);
        wrapper.setLayoutParams(wrapperParams);
        return wrapper;
    }

    private void connect() {
        if (connecting) return;
        String server = Prefs.normalizeServer(serverInput.getText().toString());
        String token = tokenInput.getText().toString().trim();
        if (TextUtils.isEmpty(server)) {
            showError("Enter your server URL.");
            return;
        }
        if (TextUtils.isEmpty(token)) {
            showError("Enter your API token.");
            return;
        }
        if (!server.startsWith("http://") && !server.startsWith("https://")) {
            showError("The server URL must start with http:// or https://.");
            return;
        }

        connecting = true;
        errorView.setVisibility(View.GONE);
        connectButton.setEnabled(false);
        connectButton.setAlpha(0.7f);
        connectButton.setText("Connecting…");

        Api.with(server, token, trustSwitch.isChecked()).testConnection(
                new Api.Callback<Boolean>() {
                    @Override
                    public void onResult(Api.Result<Boolean> result) {
                        if (isFinishing()) return;
                        connecting = false;
                        connectButton.setEnabled(true);
                        connectButton.setAlpha(1f);
                        connectButton.setText("Connect");
                        if (result.ok) {
                            Prefs.saveAccount(ConnectActivity.this, server, token, trustSwitch.isChecked());
                            Prefs.setServer(ConnectActivity.this, server);
                            Prefs.setToken(ConnectActivity.this, token);
                            Prefs.setTrustAll(ConnectActivity.this, trustSwitch.isChecked());
                            Intent intent = new Intent(ConnectActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            showError(result.unauthorized()
                                    ? "That API token was rejected. Check the token and try again."
                                    : result.error);
                        }
                    }
                });
    }

    private void showError(String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }
}
