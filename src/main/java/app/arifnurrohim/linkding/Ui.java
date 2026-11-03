package app.arifnurrohim.linkding;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

public final class Ui {
    private static Typeface iconTypeface;

    private Ui() {
    }

    public static float density(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int dp(Context context, float value) {
        return Math.round(value * density(context));
    }

    public static TextView text(Context context, CharSequence value, float sp, int color) {
        return text(context, value, sp, color, false);
    }

    public static TextView text(Context context, CharSequence value, float sp, int color, boolean medium) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.create(medium ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        view.setIncludeFontPadding(false);
        return view;
    }

    public static TextView icon(Context context, int codepoint, float sizeSp, int color) {
        TextView view = new TextView(context);
        view.setText(new String(Character.toChars(codepoint)));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);
        view.setIncludeFontPadding(false);
        view.setTypeface(iconTypeface(context));
        return view;
    }

    public static synchronized Typeface iconTypeface(Context context) {
        if (iconTypeface == null) {
            iconTypeface = Typeface.createFromAsset(context.getApplicationContext().getAssets(), "materialicons.ttf");
        }
        return iconTypeface;
    }

    public static LinearLayout vertical(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    public static LinearLayout horizontal(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        return layout;
    }

    public static GradientDrawable rounded(Context context, int fill, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    public static GradientDrawable roundedStroke(Context context, int fill, int strokeColor,
                                                 float strokeDp, float radiusDp) {
        GradientDrawable drawable = rounded(context, fill, radiusDp);
        drawable.setStroke(dp(context, strokeDp), strokeColor);
        return drawable;
    }

    public static GradientDrawable roundedTop(Context context, int fill, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        float r = dp(context, radiusDp);
        drawable.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return drawable;
    }

    public static GradientDrawable oval(Context context, int fill) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(fill);
        return drawable;
    }

    public static void ripple(View view, int color, float radiusDp) {
        GradientDrawable mask = rounded(view.getContext(), Color.WHITE, radiusDp);
        RippleDrawable ripple = new RippleDrawable(
                ColorStateList.valueOf(Catppuccin.withAlpha(color, 0.30f)), null, mask);
        view.setForeground(ripple);
    }

    public static void clickable(View view) {
        view.setClickable(true);
        view.setFocusable(true);
    }

    public static TextView pill(Context context, String label, int bgColor, int textColor) {
        TextView view = text(context, label, 11.5f, textColor, true);
        view.setBackground(rounded(context, bgColor, 999));
        view.setPadding(dp(context, 9), dp(context, 3), dp(context, 9), dp(context, 3));
        return view;
    }

    public static LinearLayout.LayoutParams lp(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    public static LinearLayout.LayoutParams lp(int width, int height, float weight) {
        return new LinearLayout.LayoutParams(width, height, weight);
    }

    public static FrameLayout.LayoutParams flp(int width, int height, int gravity) {
        return new FrameLayout.LayoutParams(width, height, gravity);
    }

    public static void margins(View view, int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
            marginParams.setMargins(left, top, right, bottom);
            view.setLayoutParams(marginParams);
        }
    }

    public static View divider(Context context, int color, int heightDp) {
        View view = new View(context);
        view.setBackgroundColor(color);
        view.setLayoutParams(lp(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, heightDp)));
        return view;
    }

    public static EditText input(Context context, String hint, int inputType, boolean multiline) {
        EditText edit = new EditText(context);
        edit.setHint(hint);
        edit.setHintTextColor(Catppuccin.current(context).overlay0);
        edit.setTextColor(Catppuccin.current(context).text);
        edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        edit.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        edit.setInputType(inputType);
        edit.setSingleLine(!multiline);
        if (multiline) {
            edit.setMinLines(3);
            edit.setGravity(Gravity.TOP | Gravity.START);
        }
        edit.setBackground(null);
        edit.setPadding(0, 0, 0, 0);
        edit.setHighlightColor(Catppuccin.withAlpha(Catppuccin.accentColor(context), 0.22f));
        edit.setIncludeFontPadding(false);
        return edit;
    }

    public static void tintProgress(ProgressBar progressBar, int color) {
        Drawable drawable = progressBar.getIndeterminateDrawable();
        if (drawable != null) {
            drawable.setTint(color);
        }
    }

    @SuppressWarnings("deprecation")
    public static void tintSwitch(Switch view, int accent, int track, int offThumb, int offTrack) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
        };
        int[] thumbColors = new int[]{accent, offThumb};
        int[] trackColors = new int[]{track, offTrack};
        view.setThumbTintList(new ColorStateList(states, thumbColors));
        view.setTrackTintList(new ColorStateList(states, trackColors));
        view.setShowText(false);
    }

    public static void snack(Activity activity, String message) {
        final ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) return;
        Catppuccin.Palette p = Catppuccin.current(activity);
        final TextView view = text(activity, message, 13.5f, p.text, true);
        view.setBackground(roundedStroke(activity, p.surface1, p.surface2, 1, 12));
        view.setPadding(dp(activity, 18), dp(activity, 12), dp(activity, 18), dp(activity, 12));
        view.setElevation(dp(activity, 8));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        params.bottomMargin = dp(activity, 88);
        content.addView(view, params);
        view.setAlpha(0f);
        view.setTranslationY(dp(activity, 24));
        view.animate().alpha(1f).translationY(0f).setDuration(180).start();
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.animate().alpha(0f).translationY(dp(activity, 24)).setDuration(180)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                content.removeView(view);
                            }
                        }).start();
            }
        }, 2600);
    }

    public static int onColor(Context context, int background) {
        double luminance = (0.299 * Color.red(background) + 0.587 * Color.green(background)
                + 0.114 * Color.blue(background)) / 255.0;
        Catppuccin.Palette p = Catppuccin.current(context);
        return luminance > 0.58 ? p.crust : p.base;
    }

    public static int dpFromAttribute(Context context) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.actionBarSize, value, true);
        return TypedValue.complexToDimensionPixelSize(value.data, context.getResources().getDisplayMetrics());
    }
}
