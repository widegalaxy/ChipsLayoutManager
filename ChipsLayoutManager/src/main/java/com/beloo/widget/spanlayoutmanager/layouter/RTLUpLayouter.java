package com.beloo.widget.spanlayoutmanager.layouter;

import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.beloo.widget.spanlayoutmanager.ChipsLayoutManager;
import com.beloo.widget.spanlayoutmanager.cache.IViewCacheStorage;
import com.beloo.widget.spanlayoutmanager.gravity.IChildGravityResolver;

class RTLUpLayouter extends AbstractLayouter implements ILayouter {
    private static final String TAG = RTLUpLayouter.class.getSimpleName();

    protected int viewLeft;

    RTLUpLayouter(ChipsLayoutManager spanLayoutManager,
                  IChildGravityResolver childGravityResolver,
                  IViewCacheStorage cacheStorage,
                  int topOffset, int leftOffset, int bottomOffset) {
        super(spanLayoutManager, topOffset, bottomOffset, cacheStorage, childGravityResolver);
        Log.d(TAG, "start bottom offset = " + bottomOffset);
        this.viewLeft = leftOffset;
    }

    @Override
    void addView(View view) {
        getLayoutManager().addView(view, 0);
    }

    @Override
    void onPreLayout() {
        int leftOffsetOfRow = -(getCanvasRightBorder() - viewLeft);

        for (Pair<Rect, View> rowViewRectPair : rowViews) {
            Rect viewRect = rowViewRectPair.first;

            viewRect.left = viewRect.left - leftOffsetOfRow;
            viewRect.right = viewRect.right - leftOffsetOfRow;

            rowTop = Math.min(rowTop, viewRect.top);
            rowBottom = Math.max(rowBottom, viewRect.bottom);
        }
    }

    @Override
    void onAfterLayout() {
        //go to next row, increase top coordinate, reset left
        viewLeft = getCanvasLeftBorder();
        rowBottom = rowTop;
    }

    @Override
    Rect createViewRect(View view) {
        int right = viewLeft + currentViewWidth;
        int viewTop = rowBottom - currentViewHeight;
        Rect viewRect = new Rect(viewLeft, viewTop, right, rowBottom);
        viewLeft = viewRect.right;
        return viewRect;
    }

    @Override
    public boolean onAttachView(View view) {

        if (viewLeft != getCanvasLeftBorder() && viewLeft + getLayoutManager().getDecoratedMeasuredWidth(view) > getCanvasRightBorder()) {
            viewLeft = getCanvasLeftBorder();
            rowBottom = rowTop;
        } else {
            viewLeft = getLayoutManager().getDecoratedRight(view);
        }

        rowTop = Math.min(rowTop, getLayoutManager().getDecoratedTop(view));

        return super.onAttachView(view);
    }

    @Override
    public boolean isFinishedLayouting() {
        return rowBottom < getCanvasTopBorder();
    }

    @Override
    public boolean canNotBePlacedInCurrentRow() {
        //when go up, check cache to layout according previous down algorithm
        boolean stopDueToCache = getCacheStorage().isPositionEndsRow(getCurrentViewPosition());
        if (stopDueToCache) return true;

        int bufRight = viewLeft + currentViewWidth;
        return super.canNotBePlacedInCurrentRow() || (bufRight > getCanvasRightBorder() && viewLeft > getCanvasLeftBorder());
    }

    @Override
    public AbstractPositionIterator positionIterator() {
        return new DecrementalPositionIterator();
    }

}
