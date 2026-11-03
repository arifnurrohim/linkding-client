package app.arifnurrohim.linkding;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class PullToRefreshLayout extends FrameLayout {
    public interface OnRefreshListener {
        void onRefresh();
    }

    private static final float DRAG_FACTOR = 0.55f;

    private final int touchSlop;
    private final float refreshOffset;
    private final float maxDrag;

    private float startY;
    private float dragDistance;
    private boolean dragging;
    private boolean refreshing;
    private boolean enabled = true;

    private View content;
    private ProgressBar indicator;
    private OnRefreshListener refreshListener;

    public PullToRefreshLayout(Context context, int indicatorColor) {
        super(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        float density = getResources().getDisplayMetrics().density;
        refreshOffset = 56 * density;
        maxDrag = 140 * density;
        setClipToPadding(false);
        setClipChildren(false);

        indicator = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
        indicator.setIndeterminate(true);
        Ui.tintProgress(indicator, indicatorColor);
        int size = (int) (28 * density);
        LayoutParams params = new LayoutParams(size, size, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        params.topMargin = (int) (14 * density);
        indicator.setAlpha(0f);
        addView(indicator, params);
    }

    public void setContent(View view) {
        if (content != null) {
            removeView(content);
        }
        content = view;
        addView(content, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        refreshListener = listener;
    }

    public void setPullEnabled(boolean value) {
        enabled = value;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public void setRefreshing(boolean value) {
        if (value) {
            if (!refreshing) {
                refreshing = true;
                if (indicator != null) indicator.setAlpha(1f);
                animateContent(refreshOffset, 180, null);
            }
        } else if (refreshing) {
            refreshing = false;
            animateContent(0f, 220, new Runnable() {
                @Override
                public void run() {
                    if (indicator != null) indicator.setAlpha(0f);
                }
            });
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!enabled || refreshing) return false;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startY = ev.getY();
                dragging = false;
                dragDistance = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if (canChildScrollUp()) return false;
                float dy = ev.getY() - startY;
                if (dy > touchSlop) {
                    dragging = true;
                    dragDistance = dy - touchSlop;
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!enabled) return false;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float dy = ev.getY() - startY - touchSlop;
                if (dy < 0) dy = 0;
                dragDistance = dy;
                applyDrag(dy);
                return true;
            case MotionEvent.ACTION_UP:
                if (dragging) {
                    dragging = false;
                    if (!refreshing && damped(dragDistance) >= refreshOffset) {
                        beginRefresh();
                    } else {
                        cancelRefresh();
                    }
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                if (dragging) {
                    dragging = false;
                    cancelRefresh();
                }
                return true;
            default:
                return super.onTouchEvent(ev);
        }
    }

    private void applyDrag(float dy) {
        float dampedValue = damped(dy);
        if (content != null) content.setTranslationY(dampedValue);
        if (indicator != null) indicator.setAlpha(Math.min(1f, dampedValue / refreshOffset));
    }

    private void beginRefresh() {
        refreshing = true;
        if (indicator != null) indicator.setAlpha(1f);
        animateContent(refreshOffset, 180, null);
        if (refreshListener != null) refreshListener.onRefresh();
    }

    private void cancelRefresh() {
        animateContent(0f, 200, new Runnable() {
            @Override
            public void run() {
                if (indicator != null) indicator.setAlpha(0f);
            }
        });
    }

    private float damped(float dy) {
        return Math.min(dy * DRAG_FACTOR, maxDrag);
    }

    private boolean canChildScrollUp() {
        return content != null && content.canScrollVertically(-1);
    }

    private void animateContent(float target, int duration, final Runnable endAction) {
        if (content == null) {
            if (endAction != null) endAction.run();
            return;
        }
        content.animate().translationY(target).setDuration(duration)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(endAction).start();
    }
}
