package com.yinhuan.simpleviewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by yinhuan on 2017/3/2.
 */

public class SimpleViewPager extends ViewGroup {

    private Scroller mScroller;

    private int mTouchSlop;

    /**
     * 手机按下时的屏幕坐标
     */
    private float mXDown;

    /**
     * 手机当时所处的屏幕坐标
     */
    private float mXMove;

    /**
     * 上次触发ACTION_MOVE事件时的屏幕坐标
     */
    private float mXLastMove;

    /**
     * 界面可滚动的左边界
     */
    private int leftBorder;

    /**
     * 界面可滚动的右边界
     */
    private int rightBorder;

    public SimpleViewPager(Context context) {
        super(context);
    }

    public SimpleViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledPagingTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++){
            View childView = getChildAt(i);
            measureChild(childView,widthMeasureSpec,heightMeasureSpec);
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed){
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++){
                View childView = getChildAt(i);
                int childMeasuredWidth = childView.getMeasuredWidth();
                int childMeasuredHeight = childView.getMeasuredHeight();
                childView.layout(i * childMeasuredWidth, 0, (i + 1) * childMeasuredWidth, childMeasuredHeight);
            }
            leftBorder = getChildAt(0).getLeft();
            rightBorder = getChildAt(getChildCount() - 1).getRight();
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d("SinpleViewPager", "onInterceptTouchEvent: " + ev.getAction());
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mXDown = ev.getRawX();
                mXLastMove = mXDown;
                break;
            case MotionEvent.ACTION_MOVE:
                mXMove = ev.getRawX();
                float diff = Math.abs(mXMove - mXDown);
                mXLastMove = mXMove;
                if (diff > mTouchSlop){
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("SinpleViewPager", "onTouchEvent:"+event.getAction());
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                mXMove = event.getRawX();
                int scrolledX = (int) (mXLastMove - mXMove);
                if (getScrollX() + scrolledX < leftBorder){
                    scrollTo(leftBorder, 0);
                    return true;
                }else if (getScrollX() + getWidth() + scrolledX > rightBorder){
                    scrollTo(rightBorder - getWidth(), 0);
                    return true;
                }
                scrollBy(scrolledX, 0);
                mXLastMove = mXMove;
                break;
            case MotionEvent.ACTION_UP:
                int targeIndex = (getScrollX() + getWidth() / 2) / getWidth();
                int dx = (int) (targeIndex * getWidth() - getScrollX());
                mScroller.startScroll(getScrollX(), 0, dx, 0);
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        Log.d("SinpleViewPager", "computeScroll()");

        if (mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

}
