package app.arifnurrohim.linkding;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public final class Dialogs {
    public static class Item {
        public final int icon;
        public final String label;
        public final int color;
        public final boolean dividerBefore;
        public final Runnable action;

        public Item(int icon, String label, int color, boolean dividerBefore, Runnable action) {
            this.icon = icon;
            this.label = label;
            this.color = color;
            this.dividerBefore = dividerBefore;
            this.action = action;
        }
    }

    private Dialogs() {
    }

    public static Dialog sheet(Activity activity, String title, String subtitle, List<Item> items) {
        final Catppuccin.Palette p = Catppuccin.current(activity);
        final int accent = Catppuccin.accentColor(activity);
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = Ui.vertical(activity);
        root.setBackground(Ui.roundedTop(activity, p.surface0, 24));
        root.setPadding(Ui.dp(activity, 8), Ui.dp(activity, 8), Ui.dp(activity, 8), Ui.dp(activity, 20));

        View handle = new View(activity);
        handle.setBackground(Ui.rounded(activity, p.overlay0, 99));
        LinearLayout.LayoutParams handleParams = Ui.lp(Ui.dp(activity, 38), Ui.dp(activity, 4));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.topMargin = Ui.dp(activity, 4);
        handleParams.bottomMargin = Ui.dp(activity, 14);
        root.addView(handle, handleParams);

        if (title != null && !title.isEmpty()) {
            TextView titleView = Ui.text(activity, title, 17f, p.text, true);
            titleView.setPadding(Ui.dp(activity, 12), 0, Ui.dp(activity, 12), 0);
            root.addView(titleView);
        }
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView subtitleView = Ui.text(activity, subtitle, 12.5f, p.overlay1);
            subtitleView.setPadding(Ui.dp(activity, 12), Ui.dp(activity, 4), Ui.dp(activity, 12), Ui.dp(activity, 8));
            root.addView(subtitleView);
        }

        ScrollView scrollView = new ScrollView(activity);
        scrollView.setVerticalScrollBarEnabled(false);
        LinearLayout list = Ui.vertical(activity);
        list.setPadding(0, Ui.dp(activity, 4), 0, 0);
        scrollView.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        for (final Item item : items) {
            if (item.dividerBefore) {
                View divider = new View(activity);
                divider.setBackgroundColor(p.surface1);
                LinearLayout.LayoutParams dividerParams = Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                        Math.max(1, Ui.dp(activity, 0.7f)));
                dividerParams.setMargins(Ui.dp(activity, 12), Ui.dp(activity, 4),
                        Ui.dp(activity, 12), Ui.dp(activity, 4));
                list.addView(divider, dividerParams);
            }
            LinearLayout row = Ui.horizontal(activity);
            row.setPadding(Ui.dp(activity, 12), Ui.dp(activity, 13), Ui.dp(activity, 12), Ui.dp(activity, 13));
            row.setBackground(Ui.rounded(activity, Color.TRANSPARENT, 12));
            Ui.ripple(row, p.overlay2, 12);
            Ui.clickable(row);

            TextView icon = Ui.icon(activity, item.icon, 22f, item.color);
            row.addView(icon, Ui.lp(Ui.dp(activity, 28), Ui.dp(activity, 28)));
            TextView label = Ui.text(activity, item.label, 15f, item.color, false);
            LinearLayout.LayoutParams labelParams = Ui.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            labelParams.leftMargin = Ui.dp(activity, 14);
            row.addView(label, labelParams);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (item.action != null) item.action.run();
                }
            });
            list.addView(row, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        root.addView(scrollView, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.setContentView(root);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0.55f;
            window.setAttributes(params);
        }
        dialog.show();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    public static Dialog confirm(Activity activity, String title, String message,
                                 String positiveLabel, boolean danger, final Runnable onConfirm) {
        final Catppuccin.Palette p = Catppuccin.current(activity);
        final int accent = Catppuccin.accentColor(activity);
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout outer = new FrameLayout(activity);
        outer.setPadding(Ui.dp(activity, 26), Ui.dp(activity, 26), Ui.dp(activity, 26), Ui.dp(activity, 26));

        LinearLayout card = Ui.vertical(activity);
        card.setBackground(Ui.roundedStroke(activity, p.surface0, p.surface1, 1, 20));
        card.setPadding(Ui.dp(activity, 22), Ui.dp(activity, 20), Ui.dp(activity, 22), Ui.dp(activity, 16));

        TextView titleView = Ui.text(activity, title, 18f, p.text, true);
        card.addView(titleView);

        TextView messageView = Ui.text(activity, message, 14f, p.subtext0);
        messageView.setLineSpacing(Ui.dp(activity, 3), 1f);
        LinearLayout.LayoutParams messageParams = Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        messageParams.topMargin = Ui.dp(activity, 8);
        card.addView(messageView, messageParams);

        LinearLayout buttons = Ui.horizontal(activity);
        buttons.setGravity(Gravity.END);
        LinearLayout.LayoutParams buttonsParams = Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonsParams.topMargin = Ui.dp(activity, 20);
        card.addView(buttons, buttonsParams);

        TextView cancel = Ui.text(activity, "Cancel", 14f, p.subtext1, true);
        cancel.setPadding(Ui.dp(activity, 16), Ui.dp(activity, 10), Ui.dp(activity, 16), Ui.dp(activity, 10));
        cancel.setBackground(Ui.rounded(activity, Color.TRANSPARENT, 10));
        Ui.ripple(cancel, p.overlay2, 10);
        Ui.clickable(cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        buttons.addView(cancel);

        int positiveColor = danger ? p.red : accent;
        TextView positive = Ui.text(activity, positiveLabel, 14f, Ui.onColor(activity, positiveColor), true);
        positive.setPadding(Ui.dp(activity, 18), Ui.dp(activity, 10), Ui.dp(activity, 18), Ui.dp(activity, 10));
        positive.setBackground(Ui.rounded(activity, positiveColor, 10));
        Ui.ripple(positive, p.base, 10);
        Ui.clickable(positive);
        LinearLayout.LayoutParams positiveParams = Ui.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        positiveParams.leftMargin = Ui.dp(activity, 8);
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (onConfirm != null) onConfirm.run();
            }
        });
        buttons.addView(positive, positiveParams);

        outer.addView(card, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.setContentView(outer);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0.6f;
            window.setAttributes(params);
        }
        dialog.show();
        return dialog;
    }
}
