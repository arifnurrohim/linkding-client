package app.arifnurrohim.linkding;

import android.content.Context;
import android.graphics.Outline;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class BookmarkAdapter extends BaseAdapter {
    public interface Listener {
        void onOpen(Bookmark bookmark);

        void onMenu(Bookmark bookmark, View anchor);
    }

    private final Context context;
    private final List<Bookmark> bookmarks;
    private final Listener listener;
    private final Catppuccin.Palette p;
    private final int accent;
    private final int blue;
    private final int green;

    public BookmarkAdapter(Context context, List<Bookmark> bookmarks, Listener listener) {
        this.context = context;
        this.bookmarks = bookmarks;
        this.listener = listener;
        this.p = Catppuccin.current(context);
        this.accent = Catppuccin.accentColor(context);
        this.blue = p.blue;
        this.green = p.green;
    }

    @Override
    public int getCount() {
        return bookmarks.size();
    }

    @Override
    public Bookmark getItem(int position) {
        return bookmarks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return bookmarks.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = createHolder();
            convertView = holder.wrapper;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Bookmark bookmark = getItem(position);
        holder.title.setText(bookmark.displayTitle());
        holder.url.setText(bookmark.readableUrl());
        String description = bookmark.displayDescription();
        if (description.isEmpty()) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(description);
        }

        if (Prefs.showFavicons(context)) {
            holder.favicon.setVisibility(View.VISIBLE);
            FaviconLoader.load(holder.favicon, bookmark.url);
        } else {
            holder.favicon.setVisibility(View.GONE);
            holder.favicon.setTag(null);
            holder.favicon.setImageDrawable(null);
        }

        holder.unreadDot.setVisibility(bookmark.unread ? View.VISIBLE : View.GONE);
        holder.tags.removeAllViews();
        if (bookmark.tags.isEmpty()) {
            holder.tags.setVisibility(View.GONE);
        } else {
            holder.tags.setVisibility(View.VISIBLE);
            int count = 0;
            for (String tag : bookmark.tags) {
                if (count++ >= 10) break;
                TextView pill = Ui.pill(context, "#" + tag, Catppuccin.withAlpha(blue, 0.16f), blue);
                LinearLayout.LayoutParams params = Ui.lp(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.rightMargin = Ui.dp(context, 6);
                params.bottomMargin = Ui.dp(context, 5);
                holder.tags.addView(pill, params);
            }
        }

        String date = bookmark.relativeDate();
        holder.date.setText(date);
        holder.date.setVisibility(date.isEmpty() ? View.GONE : View.VISIBLE);

        holder.sharedBadge.setVisibility(bookmark.shared ? View.VISIBLE : View.GONE);
        holder.archivedBadge.setVisibility(bookmark.archived ? View.VISIBLE : View.GONE);

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onOpen(bookmark);
            }
        });
        holder.card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onMenu(bookmark, v);
                return true;
            }
        });
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onMenu(bookmark, v);
            }
        });
        holder.card.setContentDescription(bookmark.displayTitle());
        return convertView;
    }

    private ViewHolder createHolder() {
        ViewHolder holder = new ViewHolder();

        holder.wrapper = new FrameLayout(context);
        holder.wrapper.setPadding(Ui.dp(context, 16), Ui.dp(context, 6),
                Ui.dp(context, 16), Ui.dp(context, 6));

        holder.card = Ui.vertical(context);
        holder.card.setBackground(Ui.roundedStroke(context, p.surface0, p.surface1, 1f, 16));
        holder.card.setPadding(Ui.dp(context, 15), Ui.dp(context, 14),
                Ui.dp(context, 13), Ui.dp(context, 12));
        holder.card.setElevation(Ui.dp(context, 1));
        Ui.ripple(holder.card, p.overlay2, 16);
        holder.card.setClickable(true);
        holder.card.setFocusable(true);
        holder.wrapper.addView(holder.card, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout top = Ui.horizontal(context);

        holder.favicon = new ImageView(context);
        holder.favicon.setBackground(Ui.rounded(context, p.surface1, 5));
        holder.favicon.setClipToOutline(true);
        holder.favicon.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), Ui.dp(context, 5));
            }
        });
        LinearLayout.LayoutParams faviconParams = Ui.lp(Ui.dp(context, 20), Ui.dp(context, 20));
        faviconParams.rightMargin = Ui.dp(context, 9);
        top.addView(holder.favicon, faviconParams);

        holder.title = Ui.text(context, "", 16f, p.text, true);
        holder.title.setSingleLine(true);
        holder.title.setEllipsize(TextUtils.TruncateAt.END);
        top.addView(holder.title, Ui.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        holder.unreadDot = new View(context);
        holder.unreadDot.setBackground(Ui.oval(context, accent));
        LinearLayout.LayoutParams dotParams = Ui.lp(Ui.dp(context, 8), Ui.dp(context, 8));
        dotParams.leftMargin = Ui.dp(context, 8);
        top.addView(holder.unreadDot, dotParams);

        holder.more = Ui.icon(context, Icons.MORE, 20f, p.overlay1);
        holder.more.setBackground(Ui.rounded(context, android.graphics.Color.TRANSPARENT, 99));
        Ui.ripple(holder.more, p.overlay2, 99);
        Ui.clickable(holder.more);
        LinearLayout.LayoutParams moreParams = Ui.lp(Ui.dp(context, 30), Ui.dp(context, 30));
        moreParams.leftMargin = Ui.dp(context, 4);
        top.addView(holder.more, moreParams);

        holder.card.addView(top, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        holder.url = Ui.text(context, "", 12f, blue, false);
        holder.url.setSingleLine(true);
        holder.url.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        LinearLayout.LayoutParams urlParams = Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        urlParams.topMargin = Ui.dp(context, 4);
        holder.card.addView(holder.url, urlParams);

        holder.description = Ui.text(context, "", 13f, p.subtext0, false);
        holder.description.setMaxLines(3);
        holder.description.setEllipsize(TextUtils.TruncateAt.END);
        holder.description.setLineSpacing(Ui.dp(context, 2), 1f);
        LinearLayout.LayoutParams descriptionParams = Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        descriptionParams.topMargin = Ui.dp(context, 6);
        holder.card.addView(holder.description, descriptionParams);

        holder.tags = new FlowLayout(context);
        holder.tags.setSpacing(Ui.dp(context, 0), Ui.dp(context, 1));
        LinearLayout.LayoutParams tagsParams = Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tagsParams.topMargin = Ui.dp(context, 7);
        holder.card.addView(holder.tags, tagsParams);

        LinearLayout bottom = Ui.horizontal(context);
        LinearLayout.LayoutParams bottomParams = Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        bottomParams.topMargin = Ui.dp(context, 8);
        holder.card.addView(bottom, bottomParams);

        holder.date = Ui.text(context, "", 11f, p.overlay1, false);
        bottom.addView(holder.date, Ui.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        holder.archivedBadge = Ui.pill(context, "archived",
                Catppuccin.withAlpha(p.overlay1, 0.18f), p.overlay2);
        LinearLayout.LayoutParams archivedParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        archivedParams.leftMargin = Ui.dp(context, 6);
        bottom.addView(holder.archivedBadge, archivedParams);

        holder.sharedBadge = Ui.pill(context, "shared",
                Catppuccin.withAlpha(green, 0.16f), green);
        LinearLayout.LayoutParams sharedParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sharedParams.leftMargin = Ui.dp(context, 6);
        bottom.addView(holder.sharedBadge, sharedParams);

        return holder;
    }

    static class ViewHolder {
        FrameLayout wrapper;
        LinearLayout card;
        ImageView favicon;
        TextView title;
        View unreadDot;
        TextView more;
        TextView url;
        TextView description;
        FlowLayout tags;
        TextView date;
        TextView archivedBadge;
        TextView sharedBadge;
    }
}
