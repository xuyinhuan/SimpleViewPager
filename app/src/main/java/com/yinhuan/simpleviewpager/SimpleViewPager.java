package com.yinhuan.simpleviewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;


public class SimpleViewPager extends ViewGroup {

    /**
     * Scroller滚动
     */
    private Scroller mScroller;

    /**
     * 最小滑动距离
     */
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
        int childCount = getChildCount();//获取到子元素的数量
        //遍历并测量每一个子元素，measureChild方法传入子 View 和父 View 的测量规格
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
                //获取测量宽高
                int childMeasuredWidth = childView.getMeasuredWidth();
                int childMeasuredHeight = childView.getMeasuredHeight();
                //为每一个子View布局
                childView.layout(i * childMeasuredWidth, 0, (i + 1) * childMeasuredWidth, childMeasuredHeight);
            }
            leftBorder = getChildAt(0).getLeft();// leftBorder == 0
            rightBorder = getChildAt(getChildCount() - 1).getRight();
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d("SimpleViewPager", "onInterceptTouchEvent: " + ev.getAction());
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mXDown = ev.getRawX();
                mXLastMove = mXDown;
                break;
            case MotionEvent.ACTION_MOVE:
                //这个分支多次回调，直至拦截进入onTouchEvent 方法
                mXMove = ev.getRawX();
                float diff = Math.abs(mXMove - mXDown);
                mXLastMove = mXMove;
                //如果滑动距离大于最小距离，拦截掉事件，进入 onTouchEvent 方法处理事件
                if (diff > mTouchSlop){
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("SimpleViewPager", "onTouchEvent:"+event.getAction());
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                mXMove = event.getRawX();
                //此次 ACTION_MOVE滚动了多少距离，getScrollX()是总的滚动距离，当然会越来越大
                int scrolledX = (int) (mXLastMove - mXMove);
                //边界检测
                if (getScrollX() + scrolledX < leftBorder){
                    scrollTo(leftBorder, 0);
                    return true;
                }else if (getScrollX() + getWidth() + scrolledX > rightBorder){
                    scrollTo(rightBorder - getWidth(), 0);
                    return true;
                }
                //相对自身滚动
                scrollBy(scrolledX, 0);
                mXLastMove = mXMove;
                break;
            case MotionEvent.ACTION_UP:
                //targeIndex的值为1，2，3...滚动到哪个页面
                int targeIndex = (getScrollX() + getWidth() / 2) / getWidth();
                //还需要滚动 dx 距离
                int dx = (int) (targeIndex * getWidth() - getScrollX());
                //初始化滚动数据，从 getScrollX()滚动到dx，刚好是当前页面
                mScroller.startScroll(getScrollX(), 0, dx, 0);
                //刷新页面
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

}
