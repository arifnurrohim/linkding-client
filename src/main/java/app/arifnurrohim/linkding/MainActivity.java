package app.arifnurrohim.linkding;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements BookmarkAdapter.Listener {
    private static final int REQ_EDIT = 100;
    private static final int PAGE = 50;

    private Catppuccin.Palette p;
    private int accent;
    private Api api;

    private FrameLayout root;
    private LinearLayout page;
    private TextView headerTitle;
    private TextView headerSubtitle;
    private TextView refreshButton;
    private TextView sortButton;
    private LinearLayout searchBar;
    private HorizontalScrollView chipsScroll;
    private LinearLayout chipsContainer;
    private EditText searchInput;
    private TextView clearSearch;
    private FrameLayout contentHost;
    private LinearLayout bottomNav;
    private TextView fab;

    private FrameLayout bookmarksPage;
    private FrameLayout tagsPage;
    private View settingsPage;

    private PullToRefreshLayout pullToRefresh;
    private ListView bookmarksList;
    private ListView tagsList;
    private BookmarkAdapter adapter;
    private BaseAdapter tagAdapter;

    private LinearLayout booksEmpty;
    private TextView booksEmptyIcon;
    private TextView booksEmptyTitle;
    private TextView booksEmptyText;
    private TextView booksEmptyAction;
    private ProgressBar booksLoading;

    private LinearLayout tagsEmpty;
    private TextView tagsEmptyIcon;
    private TextView tagsEmptyTitle;
    private TextView tagsEmptyText;
    private TextView tagsEmptyAction;
    private ProgressBar tagsLoadingView;

    private View booksFooter;
    private ProgressBar booksFooterProgress;

    private final List<Bookmark> bookmarks = new ArrayList<>();
    private final List<Tag> tags = new ArrayList<>();

    private Api.Filter filter = Api.Filter.ALL;
    private String tagFilter;
    private String query = "";
    private String tagsQuery = "";
    private int currentTab;
    private boolean bookmarkLoading;
    private boolean tagLoading;
    private boolean hasMore = true;
    private int offset;
    private int loadSeq;
    private int tagSeq;
    private String booksError;
    private String tagsError;
    private boolean booksReconnectAction;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Runnable tagsSearchRunnable;

    private final TextView[] navIcons = new TextView[3];
    private final TextView[] navLabels = new TextView[3];
    private final FrameLayout[] navHolders = new FrameLayout[3];

    private SettingsPage settingsPageBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Theme.apply(this);

        if (!Prefs.isConfigured(this)) {
            startActivity(new Intent(this, ConnectActivity.class));
            finish();
            return;
        }

        p = Catppuccin.current(this);
        accent = Catppuccin.accentColor(this);
        api = Api.from(this);

        buildUi();
        int initialTab = getIntent().getIntExtra("tab", 0);
        if (initialTab < 0 || initialTab > 2) initialTab = 0;
        switchTab(initialTab);
        if (initialTab == 0) {
            loadBookmarks(true);
        }
        handleShareIntent(getIntent());
    }

    private void buildUi() {
        root = new FrameLayout(this);
        root.setBackgroundColor(p.base);
        setContentView(root);

        page = Ui.vertical(this);
        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout header = Ui.horizontal(this);
        header.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 8), Ui.dp(this, 2));

        LinearLayout headerText = Ui.vertical(this);
        headerTitle = Ui.text(this, "Bookmarks", 24f, p.text, true);
        headerText.addView(headerTitle);
        headerSubtitle = Ui.text(this, "", 11.5f, p.overlay1);
        LinearLayout.LayoutParams subtitleParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = Ui.dp(this, 3);
        headerText.addView(headerSubtitle, subtitleParams);
        header.addView(headerText, Ui.lp(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        sortButton = Ui.icon(this, Icons.SORT, 21f, p.subtext0);
        sortButton.setBackground(Ui.rounded(this, Color.TRANSPARENT, 99));
        Ui.ripple(sortButton, p.overlay2, 99);
        Ui.clickable(sortButton);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortSheet();
            }
        });
        header.addView(sortButton, Ui.lp(Ui.dp(this, 40), Ui.dp(this, 40)));

        refreshButton = Ui.icon(this, Icons.REFRESH, 21f, p.subtext0);
        refreshButton.setBackground(Ui.rounded(this, Color.TRANSPARENT, 99));
        Ui.ripple(refreshButton, p.overlay2, 99);
        Ui.clickable(refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshCurrent();
            }
        });
        header.addView(refreshButton, Ui.lp(Ui.dp(this, 40), Ui.dp(this, 40)));

        page.addView(header, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        searchBar = Ui.horizontal(this);
        searchBar.setBackground(Ui.roundedStroke(this, p.surface0, p.surface1, 1, 14));
        searchBar.setPadding(Ui.dp(this, 13), 0, Ui.dp(this, 8), 0);
        LinearLayout.LayoutParams searchParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 46));
        searchParams.leftMargin = Ui.dp(this, 16);
        searchParams.rightMargin = Ui.dp(this, 16);
        searchParams.topMargin = Ui.dp(this, 10);
        page.addView(searchBar, searchParams);

        TextView searchIcon = Ui.icon(this, Icons.SEARCH, 18f, p.overlay1);
        searchBar.addView(searchIcon, Ui.lp(Ui.dp(this, 24), Ui.dp(this, 24)));

        searchInput = Ui.input(this, "Search bookmarks", InputType.TYPE_CLASS_TEXT, false);
        searchInput.setSingleLine(true);
        searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchInput.setTextSize(14f);
        LinearLayout.LayoutParams inputParams = Ui.lp(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        inputParams.leftMargin = Ui.dp(this, 8);
        searchBar.addView(searchInput, inputParams);

        clearSearch = Ui.icon(this, Icons.CLOSE, 16f, p.overlay1);
        clearSearch.setBackground(Ui.rounded(this, Color.TRANSPARENT, 99));
        Ui.ripple(clearSearch, p.overlay2, 99);
        Ui.clickable(clearSearch);
        clearSearch.setVisibility(View.GONE);
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchInput.setText("");
            }
        });
        searchBar.addView(clearSearch, Ui.lp(Ui.dp(this, 30), Ui.dp(this, 30)));

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                clearSearch.setVisibility(value.isEmpty() ? View.GONE : View.VISIBLE);
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                searchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        query = value.trim();
                        loadBookmarks(true);
                    }
                };
                handler.postDelayed(searchRunnable, 350);
            }
        });
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    query = searchInput.getText().toString().trim();
                    loadBookmarks(true);
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });

        chipsScroll = new HorizontalScrollView(this);
        chipsScroll.setHorizontalScrollBarEnabled(false);
        chipsScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout.LayoutParams chipsParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44));
        chipsParams.topMargin = Ui.dp(this, 4);
        page.addView(chipsScroll, chipsParams);

        chipsContainer = Ui.horizontal(this);
        chipsContainer.setPadding(Ui.dp(this, 16), 0, Ui.dp(this, 16), 0);
        chipsScroll.addView(chipsContainer, new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        contentHost = new FrameLayout(this);
        page.addView(contentHost, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        buildBookmarksPage();
        buildTagsPage();

        View divider = new View(this);
        divider.setBackgroundColor(p.surface0);
        page.addView(divider, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Ui.dp(this, 0.7f))));

        buildBottomNav();
        page.addView(bottomNav, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 62)));

        fab = Ui.icon(this, Icons.ADD, 26f, Ui.onColor(this, accent));
        fab.setGravity(Gravity.CENTER);
        fab.setBackground(Ui.oval(this, accent));
        fab.setElevation(Ui.dp(this, 8));
        Ui.ripple(fab, p.base, 99);
        Ui.clickable(fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditor(null);
            }
        });
        FrameLayout.LayoutParams fabParams = new FrameLayout.LayoutParams(
                Ui.dp(this, 56), Ui.dp(this, 56), Gravity.BOTTOM | Gravity.END);
        fabParams.rightMargin = Ui.dp(this, 20);
        fabParams.bottomMargin = Ui.dp(this, 82);
        root.addView(fab, fabParams);

        rebuildChips();
        updateNav();
        updateHeaderSubtitle();
        Theme.handleInsets(this, root);
    }

    private void buildBookmarksPage() {
        bookmarksPage = new FrameLayout(this);
        contentHost.addView(bookmarksPage, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        pullToRefresh = new PullToRefreshLayout(this, accent);
        pullToRefresh.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadBookmarks(true);
            }
        });
        bookmarksPage.addView(pullToRefresh, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        bookmarksList = new ListView(this);
        bookmarksList.setDivider(null);
        bookmarksList.setDividerHeight(0);
        bookmarksList.setSelector(new ColorDrawable(Color.TRANSPARENT));
        bookmarksList.setCacheColorHint(Color.TRANSPARENT);
        bookmarksList.setVerticalScrollBarEnabled(false);
        bookmarksList.setOverScrollMode(View.OVER_SCROLL_NEVER);
        bookmarksList.setClipToPadding(false);
        bookmarksList.setPadding(0, Ui.dp(this, 2), 0, Ui.dp(this, 92));

        FrameLayout footer = new FrameLayout(this);
        footer.setLayoutParams(new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 52)));
        booksFooterProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        booksFooterProgress.setIndeterminate(true);
        Ui.tintProgress(booksFooterProgress, accent);
        footer.addView(booksFooterProgress, new FrameLayout.LayoutParams(
                Ui.dp(this, 26), Ui.dp(this, 26), Gravity.CENTER));
        footer.setVisibility(View.GONE);
        booksFooter = footer;
        bookmarksList.addFooterView(footer, null, false);

        adapter = new BookmarkAdapter(this, bookmarks, this);
        bookmarksList.setAdapter(adapter);
        bookmarksList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if (!bookmarkLoading && hasMore && totalItemCount > 0
                        && firstVisibleItem + visibleItemCount >= totalItemCount - 4) {
                    loadBookmarks(false);
                }
            }
        });
        pullToRefresh.setContent(bookmarksList);

        booksLoading = new ProgressBar(this);
        booksLoading.setIndeterminate(true);
        Ui.tintProgress(booksLoading, accent);
        booksLoading.setVisibility(View.GONE);
        bookmarksPage.addView(booksLoading, new FrameLayout.LayoutParams(
                Ui.dp(this, 32), Ui.dp(this, 32), Gravity.CENTER));

        booksEmpty = buildEmptyState();
        booksEmptyIcon = (TextView) booksEmpty.getChildAt(0);
        LinearLayout emptyTexts = (LinearLayout) booksEmpty.getChildAt(1);
        booksEmptyTitle = (TextView) emptyTexts.getChildAt(0);
        booksEmptyText = (TextView) emptyTexts.getChildAt(1);
        booksEmptyAction = (TextView) booksEmpty.getChildAt(2);
        booksEmpty.setVisibility(View.GONE);
        bookmarksPage.addView(booksEmpty, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));
    }

    private void buildTagsPage() {
        tagsPage = new FrameLayout(this);
        contentHost.addView(tagsPage, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout tagsColumn = Ui.vertical(this);
        tagsPage.addView(tagsColumn, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout searchWrap = Ui.horizontal(this);
        searchWrap.setBackground(Ui.roundedStroke(this, p.surface0, p.surface1, 1, 14));
        searchWrap.setPadding(Ui.dp(this, 13), 0, Ui.dp(this, 13), 0);
        LinearLayout.LayoutParams wrapParams = Ui.lp(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44));
        wrapParams.leftMargin = Ui.dp(this, 16);
        wrapParams.rightMargin = Ui.dp(this, 16);
        wrapParams.topMargin = Ui.dp(this, 8);
        wrapParams.bottomMargin = Ui.dp(this, 4);
        tagsColumn.addView(searchWrap, wrapParams);

        TextView tagSearchIcon = Ui.icon(this, Icons.SEARCH, 18f, p.overlay1);
        searchWrap.addView(tagSearchIcon, Ui.lp(Ui.dp(this, 24), Ui.dp(this, 24)));

        final EditText tagSearch = Ui.input(this, "Search tags", InputType.TYPE_CLASS_TEXT, false);
        tagSearch.setSingleLine(true);
        tagSearch.setTextSize(14f);
        LinearLayout.LayoutParams tagSearchParams = Ui.lp(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        tagSearchParams.leftMargin = Ui.dp(this, 8);
        searchWrap.addView(tagSearch, tagSearchParams);

        tagSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String value = s.toString();
                if (tagsSearchRunnable != null) handler.removeCallbacks(tagsSearchRunnable);
                tagsSearchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        tagsQuery = value.trim();
                        loadTags(true);
                    }
                };
                handler.postDelayed(tagsSearchRunnable, 350);
            }
        });

        tagsList = new ListView(this);
        tagsList.setDivider(null);
        tagsList.setDividerHeight(0);
        tagsList.setSelector(new ColorDrawable(Color.TRANSPARENT));
        tagsList.setCacheColorHint(Color.TRANSPARENT);
        tagsList.setVerticalScrollBarEnabled(false);
        tagsList.setOverScrollMode(View.OVER_SCROLL_NEVER);
        tagsList.setClipToPadding(false);
        tagsList.setPadding(0, 2, 0, Ui.dp(this, 24));
        tagAdapter = new TagAdapter();
        tagsList.setAdapter(tagAdapter);
        tagsColumn.addView(tagsList, Ui.lp(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        tagsLoadingView = new ProgressBar(this);
        tagsLoadingView.setIndeterminate(true);
        Ui.tintProgress(tagsLoadingView, accent);
        tagsLoadingView.setVisibility(View.GONE);
        tagsPage.addView(tagsLoadingView, new FrameLayout.LayoutParams(
                Ui.dp(this, 32), Ui.dp(this, 32), Gravity.CENTER));

        tagsEmpty = buildEmptyState();
        tagsEmptyIcon = (TextView) tagsEmpty.getChildAt(0);
        LinearLayout emptyTexts = (LinearLayout) tagsEmpty.getChildAt(1);
        tagsEmptyTitle = (TextView) emptyTexts.getChildAt(0);
        tagsEmptyText = (TextView) emptyTexts.getChildAt(1);
        tagsEmptyAction = (TextView) tagsEmpty.getChildAt(2);
        tagsEmpty.setVisibility(View.GONE);
        tagsPage.addView(tagsEmpty, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));
        tagsPage.setVisibility(View.GONE);
    }

    private LinearLayout buildEmptyState() {
        LinearLayout empty = Ui.vertical(this);
        empty.setPadding(Ui.dp(this, 32), Ui.dp(this, 32), Ui.dp(this, 32), Ui.dp(this, 32));
        empty.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView icon = Ui.icon(this, Icons.BOOKMARK_BORDER, 42f, p.overlay0);
        empty.addView(icon);

        LinearLayout texts = Ui.vertical(this);
        texts.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams textsParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textsParams.topMargin = Ui.dp(this, 14);
        empty.addView(texts, textsParams);

        TextView title = Ui.text(this, "", 16.5f, p.text, true);
        title.setGravity(Gravity.CENTER);
        texts.addView(title);

        TextView message = Ui.text(this, "", 12.5f, p.overlay1);
        message.setGravity(Gravity.CENTER);
        message.setLineSpacing(Ui.dp(this, 3), 1f);
        LinearLayout.LayoutParams messageParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        messageParams.topMargin = Ui.dp(this, 6);
        texts.addView(message, messageParams);

        TextView action = Ui.text(this, "Retry", 13f, accent, true);
        action.setGravity(Gravity.CENTER);
        action.setBackground(Ui.roundedStroke(this, Color.TRANSPARENT, accent, 1, 11));
        action.setPadding(Ui.dp(this, 16), Ui.dp(this, 9), Ui.dp(this, 16), Ui.dp(this, 9));
        Ui.ripple(action, accent, 11);
        Ui.clickable(action);
        action.setVisibility(View.GONE);
        LinearLayout.LayoutParams actionParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionParams.topMargin = Ui.dp(this, 16);
        empty.addView(action, actionParams);

        return empty;
    }

    private void buildBottomNav() {
        bottomNav = Ui.horizontal(this);
        bottomNav.setBackgroundColor(p.crust);
        bottomNav.setPadding(Ui.dp(this, 8), Ui.dp(this, 4),
                Ui.dp(this, 8), Ui.dp(this, 4));
        bottomNav.addView(navItem(Icons.BOOKMARK, "Bookmarks", 0),
                Ui.lp(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        bottomNav.addView(navItem(Icons.TAG, "Tags", 1),
                Ui.lp(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        bottomNav.addView(navItem(Icons.SETTINGS, "Settings", 2),
                Ui.lp(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
    }

    private LinearLayout navItem(int iconCode, String label, final int tab) {
        LinearLayout item = Ui.vertical(this);
        item.setGravity(Gravity.CENTER);
        Ui.clickable(item);

        FrameLayout holder = new FrameLayout(this);
        holder.setBackground(Ui.rounded(this, Color.TRANSPARENT, 99));
        TextView icon = Ui.icon(this, iconCode, 21f, p.overlay1);
        icon.setGravity(Gravity.CENTER);
        holder.addView(icon, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        item.addView(holder, Ui.lp(Ui.dp(this, 52), Ui.dp(this, 27)));

        TextView labelView = Ui.text(this, label, 10.5f, p.overlay1, true);
        LinearLayout.LayoutParams labelParams = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = Ui.dp(this, 3);
        item.addView(labelView, labelParams);

        navIcons[tab] = icon;
        navLabels[tab] = labelView;
        navHolders[tab] = holder;

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTab == tab) {
                    if (tab == 0) loadBookmarks(true);
                    else if (tab == 1) loadTags(true);
                    return;
                }
                switchTab(tab);
                if (tab == 0 && bookmarks.isEmpty()) loadBookmarks(true);
            }
        });
        return item;
    }

    private void switchTab(int tab) {
        currentTab = tab;
        bookmarksPage.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        tagsPage.setVisibility(tab == 1 ? View.VISIBLE : View.GONE);

        boolean isBookmarks = tab == 0;
        headerTitle.setText(tab == 0 ? "Bookmarks" : tab == 1 ? "Tags" : "Settings");
        searchBar.setVisibility(isBookmarks ? View.VISIBLE : View.GONE);
        chipsScroll.setVisibility(isBookmarks ? View.VISIBLE : View.GONE);
        sortButton.setVisibility(isBookmarks ? View.VISIBLE : View.GONE);
        refreshButton.setVisibility(tab == 2 ? View.GONE : View.VISIBLE);
        fab.setVisibility(isBookmarks ? View.VISIBLE : View.GONE);

        if (tab == 1) {
            if (tags.isEmpty()) loadTags(true);
        } else if (tab == 2) {
            if (settingsPage == null) {
                settingsPageBuilder = new SettingsPage(this);
                settingsPageBuilder.build();
                settingsPage = settingsPageBuilder.root();
                contentHost.addView(settingsPage, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            settingsPage.setVisibility(View.VISIBLE);
        }
        if (settingsPage != null && tab != 2) {
            settingsPage.setVisibility(View.GONE);
        }

        updateNav();
        updateHeaderSubtitle();
    }

    private void updateNav() {
        for (int i = 0; i < 3; i++) {
            boolean selected = i == currentTab;
            int color = selected ? accent : p.overlay1;
            navIcons[i].setTextColor(color);
            navLabels[i].setTextColor(color);
            navLabels[i].setTypeface(Typeface.create(
                    selected ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
            navHolders[i].setBackground(selected
                    ? Ui.rounded(this, Catppuccin.withAlpha(accent, 0.15f), 99)
                    : Ui.rounded(this, Color.TRANSPARENT, 99));
            navIcons[i].setContentDescription(navLabels[i].getText());
        }
    }

    private void updateHeaderSubtitle() {
        if (headerSubtitle == null) return;
        if (currentTab == 0) {
            String host = Prefs.host(this);
            if (bookmarks.isEmpty()) {
                headerSubtitle.setText(host);
            } else {
                headerSubtitle.setText(host + " · " + bookmarks.size()
                        + (bookmarks.size() == 1 ? " bookmark" : " bookmarks"));
            }
        } else if (currentTab == 1) {
            headerSubtitle.setText(tags.isEmpty() ? Prefs.host(this)
                    : tags.size() + (tags.size() == 1 ? " tag" : " tags"));
        } else {
            headerSubtitle.setText("Appearance, server and about");
        }
    }

    private void rebuildChips() {
        chipsContainer.removeAllViews();
        addChip("All", filter == Api.Filter.ALL && tagFilter == null, new Runnable() {
            @Override
            public void run() {
                filter = Api.Filter.ALL;
                tagFilter = null;
                rebuildChips();
                loadBookmarks(true);
            }
        });
        addChip("Unread", filter == Api.Filter.UNREAD && tagFilter == null, new Runnable() {
            @Override
            public void run() {
                filter = Api.Filter.UNREAD;
                tagFilter = null;
                rebuildChips();
                loadBookmarks(true);
            }
        });
        addChip("Archived", filter == Api.Filter.ARCHIVED && tagFilter == null, new Runnable() {
            @Override
            public void run() {
                filter = Api.Filter.ARCHIVED;
                tagFilter = null;
                rebuildChips();
                loadBookmarks(true);
            }
        });
        addChip("Shared", filter == Api.Filter.SHARED && tagFilter == null, new Runnable() {
            @Override
            public void run() {
                filter = Api.Filter.SHARED;
                tagFilter = null;
                rebuildChips();
                loadBookmarks(true);
            }
        });
        if (tagFilter != null) {
            addChip("#" + tagFilter + "  ×", true, new Runnable() {
                @Override
                public void run() {
                    tagFilter = null;
                    rebuildChips();
                    loadBookmarks(true);
                }
            });
        }
    }

    private void addChip(String label, boolean selected, final Runnable click) {
        TextView chip = Ui.text(this, label, 12.5f,
                selected ? Ui.onColor(this, accent) : p.subtext0, true);
        chip.setBackground(selected
                ? Ui.rounded(this, accent, 99)
                : Ui.roundedStroke(this, p.surface0, p.surface1, 1, 99));
        chip.setPadding(Ui.dp(this, 14), Ui.dp(this, 7), Ui.dp(this, 14), Ui.dp(this, 7));
        Ui.ripple(chip, selected ? p.base : p.overlay2, 99);
        Ui.clickable(chip);
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click.run();
            }
        });
        LinearLayout.LayoutParams params = Ui.lp(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.rightMargin = Ui.dp(this, 8);
        chipsContainer.addView(chip, params);
    }

    private void refreshCurrent() {
        refreshButton.animate().rotationBy(360f).setDuration(450).start();
        if (currentTab == 0) {
            loadBookmarks(true);
        } else if (currentTab == 1) {
            loadTags(true);
        }
    }

    private void loadBookmarks(final boolean reset) {
        if (!reset && bookmarkLoading) return;
        bookmarkLoading = true;
        final int seq = ++loadSeq;
        final int requestOffset = reset ? 0 : offset;

        if (reset) {
            offset = 0;
            hasMore = true;
            booksError = null;
            booksReconnectAction = false;
            if (bookmarks.isEmpty()) {
                booksLoading.setVisibility(View.VISIBLE);
                booksEmpty.setVisibility(View.GONE);
            }
        } else {
            booksFooter.setVisibility(View.VISIBLE);
        }

        String effectiveQuery = query == null ? "" : query.trim();
        if (tagFilter != null && !tagFilter.isEmpty()) {
            effectiveQuery = effectiveQuery.isEmpty()
                    ? "#" + tagFilter
                    : effectiveQuery + " #" + tagFilter;
        }
        api.listBookmarks(filter, effectiveQuery, Prefs.getSort(this), PAGE, requestOffset,
                new Api.Callback<List<Bookmark>>() {
                    @Override
                    public void onResult(Api.Result<List<Bookmark>> result) {
                        if (isFinishing() || seq != loadSeq) return;
                        bookmarkLoading = false;
                        pullToRefresh.setRefreshing(false);
                        booksFooter.setVisibility(View.GONE);
                        booksLoading.setVisibility(View.GONE);

                        if (result.ok && result.data != null) {
                            if (reset) bookmarks.clear();
                            bookmarks.addAll(result.data);
                            offset += result.data.size();
                            hasMore = result.data.size() >= PAGE;
                            booksError = null;
                            booksReconnectAction = false;
                            adapter.notifyDataSetChanged();
                            updateEmptyState();
                            updateHeaderSubtitle();
                        } else {
                            if (reset) {
                                bookmarks.clear();
                                adapter.notifyDataSetChanged();
                            }
                            showBooksError(result);
                        }
                    }
                });
    }

    private void showBooksError(Api.Result<?> result) {
        if (!bookmarks.isEmpty()) {
            Ui.snack(this, result.unauthorized() ? "Invalid API token" : result.error);
            return;
        }
        booksError = result.unauthorized() ? "Your API token was rejected."
                : (result.error == null ? "Could not load bookmarks." : result.error);
        booksReconnectAction = result.unauthorized();
        showBooksEmpty(Icons.CLOUD_OFF, "Can't load bookmarks", booksError,
                booksReconnectAction ? "Reconnect" : "Retry", new Runnable() {
                    @Override
                    public void run() {
                        if (booksReconnectAction) {
                            reconnect();
                        } else {
                            loadBookmarks(true);
                        }
                    }
                });
    }

    private void updateEmptyState() {
        if (bookmarkLoading) return;
        if (!bookmarks.isEmpty()) {
            booksEmpty.setVisibility(View.GONE);
            return;
        }
        if (booksError != null) {
            showBooksError(Api.Result.error(0, booksError));
            return;
        }
        String title;
        String message;
        if (!query.isEmpty()) {
            title = "No matches";
            message = "Try a different search term or filter.";
        } else if (filter == Api.Filter.ARCHIVED) {
            title = "Nothing archived";
            message = "Archived bookmarks will appear here.";
        } else if (filter == Api.Filter.SHARED) {
            title = "Nothing shared";
            message = "Bookmarks you share will appear here.";
        } else if (tagFilter != null) {
            title = "No bookmarks with this tag";
            message = "Try another tag or add a bookmark with #" + tagFilter + ".";
        } else {
            title = "No bookmarks yet";
            message = "Tap the + button to save your first link.";
        }
        showBooksEmpty(Icons.BOOKMARK_BORDER, title, message, null, null);
    }

    private void showBooksEmpty(int icon, String title, String message, String action,
                                final Runnable actionRun) {
        booksEmptyIcon.setText(new String(Character.toChars(icon)));
        booksEmptyTitle.setText(title);
        booksEmptyText.setText(message);
        if (action == null) {
            booksEmptyAction.setVisibility(View.GONE);
            booksEmptyAction.setOnClickListener(null);
        } else {
            booksEmptyAction.setText(action);
            booksEmptyAction.setVisibility(View.VISIBLE);
            booksEmptyAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionRun != null) actionRun.run();
                }
            });
        }
        booksEmpty.setVisibility(View.VISIBLE);
    }

    private void loadTags(final boolean reset) {
        if (!reset && tagLoading) return;
        tagLoading = true;
        final int seq = ++tagSeq;
        if (reset && tags.isEmpty()) {
            tagsLoadingView.setVisibility(View.VISIBLE);
            tagsEmpty.setVisibility(View.GONE);
        }
        api.listTags(tagsQuery, new Api.Callback<List<Tag>>() {
            @Override
            public void onResult(Api.Result<List<Tag>> result) {
                if (isFinishing() || seq != tagSeq) return;
                tagLoading = false;
                tagsLoadingView.setVisibility(View.GONE);
                if (result.ok && result.data != null) {
                    tags.clear();
                    tags.addAll(result.data);
                    tagsError = null;
                    tagAdapter.notifyDataSetChanged();
                    updateTagsEmpty();
                    updateHeaderSubtitle();
                } else {
                    tagsError = result.unauthorized() ? "Your API token was rejected."
                            : (result.error == null ? "Could not load tags." : result.error);
                    if (tags.isEmpty()) {
                        showTagsError();
                    } else {
                        Ui.snack(MainActivity.this, tagsError);
                    }
                }
            }
        });
    }

    private void showTagsError() {
        tagsEmptyIcon.setText(new String(Character.toChars(Icons.CLOUD_OFF)));
        tagsEmptyTitle.setText("Can't load tags");
        tagsEmptyText.setText(tagsError);
        tagsEmptyAction.setText("Retry");
        tagsEmptyAction.setVisibility(View.VISIBLE);
        tagsEmptyAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTags(true);
            }
        });
        tagsEmpty.setVisibility(View.VISIBLE);
    }

    private void updateTagsEmpty() {
        if (tagLoading) return;
        if (!tags.isEmpty()) {
            tagsEmpty.setVisibility(View.GONE);
            return;
        }
        if (tagsError != null) {
            showTagsError();
            return;
        }
        tagsEmptyIcon.setText(new String(Character.toChars(Icons.TAG)));
        tagsEmptyTitle.setText(tagsQuery.isEmpty() ? "No tags yet" : "No matching tags");
        tagsEmptyText.setText(tagsQuery.isEmpty()
                ? "Tags appear here when you add them to bookmarks."
                : "Try a different search term.");
        tagsEmptyAction.setVisibility(View.GONE);
        tagsEmpty.setVisibility(View.VISIBLE);
    }

    private void reconnect() {
        Intent intent = new Intent(this, ConnectActivity.class);
        intent.putExtra("edit", true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onOpen(Bookmark bookmark) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(bookmark.url)));
        } catch (Exception e) {
            Ui.snack(this, "No app can open this link.");
        }
    }

    @Override
    public void onMenu(Bookmark bookmark, View anchor) {
        List<Dialogs.Item> items = new ArrayList<>();
        items.add(new Dialogs.Item(Icons.OPEN_IN_NEW, "Open in browser", p.text, false,
                new Runnable() {
                    @Override
                    public void run() {
                        onOpen(bookmark);
                    }
                }));
        items.add(new Dialogs.Item(Icons.EDIT, "Edit bookmark", p.text, false,
                new Runnable() {
                    @Override
                    public void run() {
                        openEditor(bookmark);
                    }
                }));
        items.add(new Dialogs.Item(bookmark.unread ? Icons.DONE : Icons.BOOKMARK_BORDER,
                bookmark.unread ? "Mark as read" : "Mark as unread", p.text, false,
                new Runnable() {
                    @Override
                    public void run() {
                        setUnread(bookmark, !bookmark.unread);
                    }
                }));
        items.add(new Dialogs.Item(bookmark.archived ? Icons.UNARCHIVE : Icons.ARCHIVE,
                bookmark.archived ? "Unarchive" : "Archive", p.text, false,
                new Runnable() {
                    @Override
                    public void run() {
                        setArchived(bookmark, !bookmark.archived);
                    }
                }));
        items.add(new Dialogs.Item(Icons.SHARE, "Share link", p.text, false,
                new Runnable() {
                    @Override
                    public void run() {
                        share(bookmark);
                    }
                }));
        items.add(new Dialogs.Item(Icons.COPY, "Copy link", p.text, false,
                new Runnable() {
                    @Override
                    public void run() {
                        copy(bookmark.url);
                    }
                }));
        items.add(new Dialogs.Item(Icons.DELETE, "Delete", p.red, true,
                new Runnable() {
                    @Override
                    public void run() {
                        confirmDelete(bookmark);
                    }
                }));
        Dialogs.sheet(this, bookmark.displayTitle(), bookmark.readableUrl(), items);
    }

    private void setUnread(final Bookmark bookmark, final boolean unread) {
        api.setUnread(bookmark.id, unread, new Api.Callback<Bookmark>() {
            @Override
            public void onResult(Api.Result<Bookmark> result) {
                if (isFinishing()) return;
                if (result.ok) {
                    Ui.snack(MainActivity.this, unread ? "Marked as unread" : "Marked as read");
                    loadBookmarks(true);
                } else {
                    Ui.snack(MainActivity.this, result.error);
                }
            }
        });
    }

    private void setArchived(final Bookmark bookmark, final boolean archived) {
        api.setArchived(bookmark.id, archived, new Api.Callback<Bookmark>() {
            @Override
            public void onResult(Api.Result<Bookmark> result) {
                if (isFinishing()) return;
                if (result.ok) {
                    Ui.snack(MainActivity.this, archived ? "Bookmark archived" : "Bookmark unarchived");
                    loadBookmarks(true);
                } else {
                    Ui.snack(MainActivity.this, result.error);
                }
            }
        });
    }

    private void confirmDelete(final Bookmark bookmark) {
        Dialogs.confirm(this, "Delete bookmark?",
                "\"" + bookmark.displayTitle() + "\" will be removed from your Linkding server.",
                "Delete", true, new Runnable() {
                    @Override
                    public void run() {
                        api.delete(bookmark.id, new Api.Callback<Boolean>() {
                            @Override
                            public void onResult(Api.Result<Boolean> result) {
                                if (isFinishing()) return;
                                if (result.ok) {
                                    Ui.snack(MainActivity.this, "Bookmark deleted");
                                    loadBookmarks(true);
                                } else {
                                    Ui.snack(MainActivity.this, result.error);
                                }
                            }
                        });
                    }
                });
    }

    private void share(Bookmark bookmark) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, bookmark.displayTitle());
        intent.putExtra(Intent.EXTRA_TEXT, bookmark.url);
        startActivity(Intent.createChooser(intent, "Share link"));
    }

    private void copy(String value) {
        try {
            ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (manager != null) {
                manager.setPrimaryClip(ClipData.newPlainText("Link", value));
                Ui.snack(this, "Link copied");
            }
        } catch (Exception ignored) {
        }
    }

    private void openEditor(Bookmark bookmark) {
        Intent intent = new Intent(this, EditBookmarkActivity.class);
        if (bookmark != null) {
            intent.putExtra(EditBookmarkActivity.EXTRA_ID, bookmark.id);
            intent.putExtra(EditBookmarkActivity.EXTRA_URL, bookmark.url);
            intent.putExtra(EditBookmarkActivity.EXTRA_TITLE, bookmark.title);
            intent.putExtra(EditBookmarkActivity.EXTRA_DESCRIPTION, bookmark.description);
            intent.putExtra(EditBookmarkActivity.EXTRA_NOTES, bookmark.notes);
            intent.putExtra(EditBookmarkActivity.EXTRA_TAGS, TextUtils.join(", ", bookmark.tags));
            intent.putExtra(EditBookmarkActivity.EXTRA_UNREAD, bookmark.unread);
            intent.putExtra(EditBookmarkActivity.EXTRA_SHARED, bookmark.shared);
            intent.putExtra(EditBookmarkActivity.EXTRA_ARCHIVED, bookmark.archived);
        }
        startActivityForResult(intent, REQ_EDIT);
    }

    private void showSortSheet() {
        String current = Prefs.getSort(this);
        List<Dialogs.Item> items = new ArrayList<>();
        items.add(sortItem("Newest first", "-date_added", current));
        items.add(sortItem("Oldest first", "date_added", current));
        items.add(sortItem("Recently modified", "-date_modified", current));
        items.add(sortItem("Title A–Z", "title", current));
        items.add(sortItem("Title Z–A", "-title", current));
        Dialogs.sheet(this, "Sort bookmarks", "Choose the order for this list", items);
    }

    private Dialogs.Item sortItem(String label, final String value, String current) {
        boolean selected = value.equals(current);
        return new Dialogs.Item(Icons.SORT, label + (selected ? "   ✓" : ""),
                selected ? accent : p.text, false, new Runnable() {
            @Override
            public void run() {
                Prefs.setSort(MainActivity.this, value);
                loadBookmarks(true);
            }
        });
    }

    public void onFaviconsChanged() {
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public void restartAtTab(int tab) {
        getIntent().putExtra("tab", tab);
        recreate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_EDIT && resultCode == RESULT_OK) {
            loadBookmarks(true);
            if (!tags.isEmpty()) loadTags(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        api = Api.from(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleShareIntent(intent);
    }

    private void handleShareIntent(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) return;
        if (!"text/plain".equals(intent.getType())) return;
        intent.setAction(null);
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        String url = extractUrl(text);
        if (url == null) {
            Ui.snack(this, "No link found in the shared text.");
            return;
        }
        Intent edit = new Intent(this, EditBookmarkActivity.class);
        edit.putExtra(EditBookmarkActivity.EXTRA_URL, url);
        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        if (subject != null && !subject.trim().isEmpty()) {
            edit.putExtra(EditBookmarkActivity.EXTRA_TITLE, subject.trim());
        }
        startActivityForResult(edit, REQ_EDIT);
    }

    private String extractUrl(String text) {
        if (text == null) return null;
        Matcher matcher = Pattern.compile("https?://[^\\s<>\"']+", Pattern.CASE_INSENSITIVE)
                .matcher(text);
        if (matcher.find()) {
            String url = matcher.group();
            while (url.length() > 1 && ".,;:!?)]}'\"".indexOf(url.charAt(url.length() - 1)) >= 0) {
                url = url.substring(0, url.length() - 1);
            }
            return url;
        }
        String trimmed = text.trim();
        if (trimmed.matches("^[\\w.-]+\\.[a-z]{2,}(/\\S*)?$")) {
            return "https://" + trimmed;
        }
        return null;
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

    @Override
    public void onBackPressed() {
        if (currentTab != 0) {
            switchTab(0);
            return;
        }
        if (tagFilter != null) {
            tagFilter = null;
            rebuildChips();
            loadBookmarks(true);
            return;
        }
        if (searchInput != null && searchInput.getText().length() > 0) {
            searchInput.setText("");
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
        if (tagsSearchRunnable != null) handler.removeCallbacks(tagsSearchRunnable);
        super.onDestroy();
    }

    private class TagAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return tags.size();
        }

        @Override
        public Tag getItem(int position) {
            return tags.get(position);
        }

        @Override
        public long getItemId(int position) {
            return tags.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FrameLayout wrapper;
            LinearLayout card;
            TextView nameView;
            if (convertView == null) {
                wrapper = new FrameLayout(MainActivity.this);
                wrapper.setPadding(Ui.dp(MainActivity.this, 16), Ui.dp(MainActivity.this, 4),
                        Ui.dp(MainActivity.this, 16), Ui.dp(MainActivity.this, 4));

                card = Ui.horizontal(MainActivity.this);
                card.setBackground(Ui.roundedStroke(MainActivity.this, p.surface0, p.surface1, 1, 14));
                card.setPadding(Ui.dp(MainActivity.this, 14), Ui.dp(MainActivity.this, 13),
                        Ui.dp(MainActivity.this, 14), Ui.dp(MainActivity.this, 13));
                Ui.ripple(card, p.overlay2, 14);
                Ui.clickable(card);
                wrapper.addView(card, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                TextView icon = Ui.icon(MainActivity.this, Icons.TAG, 18f, p.blue);
                card.addView(icon, Ui.lp(Ui.dp(MainActivity.this, 26), Ui.dp(MainActivity.this, 26)));

                nameView = Ui.text(MainActivity.this, "", 15f, p.text, true);
                LinearLayout.LayoutParams nameParams = Ui.lp(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                nameParams.leftMargin = Ui.dp(MainActivity.this, 8);
                card.addView(nameView, nameParams);

                TextView chevron = Ui.icon(MainActivity.this, Icons.CHEVRON, 18f, p.overlay1);
                card.addView(chevron, Ui.lp(Ui.dp(MainActivity.this, 22), Ui.dp(MainActivity.this, 22)));

                wrapper.setTag(new TagHolder(card, nameView));
                convertView = wrapper;
            } else {
                TagHolder holder = (TagHolder) convertView.getTag();
                card = holder.card;
                nameView = holder.name;
            }

            final Tag tag = getItem(position);
            nameView.setText("#" + tag.name);
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tagFilter = tag.name;
                    filter = Api.Filter.ALL;
                    query = "";
                    searchInput.setText("");
                    switchTab(0);
                    rebuildChips();
                    loadBookmarks(true);
                }
            });
            return convertView;
        }
    }

    private static class TagHolder {
        final LinearLayout card;
        final TextView name;

        TagHolder(LinearLayout card, TextView name) {
            this.card = card;
            this.name = name;
        }
    }
}
