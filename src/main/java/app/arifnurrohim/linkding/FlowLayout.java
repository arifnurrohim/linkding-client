package app.arifnurrohim.linkding;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class FlowLayout extends ViewGroup {
    private int horizontalSpacing;
    private int verticalSpacing;

    public FlowLayout(Context context) {
        super(context);
    }

    public void setSpacing(int horizontal, int vertical) {
        horizontalSpacing = horizontal;
        verticalSpacing = vertical;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int available = widthMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : width - getPaddingLeft() - getPaddingRight();

        int lineWidth = 0;
        int lineHeight = 0;
        int totalHeight = 0;
        int maxLineWidth = 0;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (lineWidth > 0 && lineWidth + horizontalSpacing + childWidth > available) {
                totalHeight += lineHeight + verticalSpacing;
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                lineWidth = childWidth;
                lineHeight = childHeight;
            } else {
                if (lineWidth > 0) lineWidth += horizontalSpacing;
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
        }
        totalHeight += lineHeight;
        maxLineWidth = Math.max(maxLineWidth, lineWidth);

        int measuredWidth = widthMode == MeasureSpec.UNSPECIFIED ? maxLineWidth : width;
        setMeasuredDimension(measuredWidth + getPaddingLeft() + getPaddingRight(),
                totalHeight + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int available = r - l - getPaddingLeft() - getPaddingRight();
        int x = getPaddingLeft();
        int y = getPaddingTop();
        int lineHeight = 0;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (x > getPaddingLeft() && x + childWidth > getPaddingLeft() + available) {
                x = getPaddingLeft();
                y += lineHeight + verticalSpacing;
                lineHeight = 0;
            }
            child.layout(x, y, x + childWidth, y + childHeight);
            x += childWidth + horizontalSpacing;
            lineHeight = Math.max(lineHeight, childHeight);
        }
    }
}
