package com.dthfish.scrollviewindicator;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.Space;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by J!nl!n on 2016/7/12 17:08.
 * Copyright © 1990-2015 J!nl!n™ Inc. All rights reserved.
 * <p/>
 * ━━━━━━神兽出没━━━━━━
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　　　　　┃
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　　　　　┃
 * 　　┃　　　┻　　　┃
 * 　　┃　　　　　　　┃
 * 　　┗━┓　　　┏━┛Code is far away from bug with the animal protecting
 * 　　　　┃　　　┃    神兽保佑,代码无bug
 * 　　　　┃　　　┃
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━━━感觉萌萌哒━━━━━━
 * <p>
 * modified by zlz on 2017/3/16
 * 为 NestedScrollView 改造的 ViewPager TabIndicator,
 * 请注意：
 * 1、该类会在 ScrollView 的子 View 中插入一个宽度为 match_parent, 高度为 1px 的 ViewPager (用来辅助动画), 因此确保 ScrollView 中包含的是 ViewGroup
 * 2、使用时调用 {{@link #setScrollView(NestedScrollView, NestedScrollView.OnScrollChangeListener, List, List)}}与 NestedScrollView  关联,
 * 并且原来要设置再 NestedScrollView 上的监听要在此传入, 否则讲被替换
 * 3、TabIndicator 本身也可以作为一个{ NestedScrollView.OnScrollChangeListener} 传入, 如果这样, 两个控件将会同步, 共享已经创建的 ViewPager
 * 4、默认用48dp的像素值作为 ActionBar 的高度, 计算滚动距离, 如果有需求用{{@link #setActionBarHeight(int)}} 来设置
 */
public class ScrollViewTabIndicator extends LinearLayout implements ViewPager.OnPageChangeListener, OnClickListener, NestedScrollView.OnScrollChangeListener {

    private int mMode;
    private int mTabPadding;
    private int mTextAppearance;
    private int tabBackground;

    private int mIndicatorOffset;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int mIndicatorMode;

    private int mUnderLineHeight;

    private Paint mPaint;
    private Paint mUnderLinePaint;

    public static final int MODE_SCROLL = 0;
    public static final int MODE_FIXED = 1;

    private int mSelectedPosition;
    private boolean mScrolling = false;

    private Runnable mTabAnimSelector;

    private static final int MATCH_PARENT = -1;
    private static final int WRAP_CONTENT = -2;
    private int mActionBarHeight = dip2px(48);
    private boolean mIsClick = false;
    /**
     * 仅仅用来协助完成动画
     */
    private ViewPager mAssistViewPager;

    private NestedScrollView mScrollView;
    private NestedScrollView.OnScrollChangeListener mScrollListener;
    private List<View> mViews;

    public ScrollViewTabIndicator(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public ScrollViewTabIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ScrollViewTabIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public ScrollViewTabIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setGravity(Gravity.CENTER_VERTICAL);
        setHorizontalScrollBarEnabled(false);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);

        mUnderLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnderLinePaint.setStyle(Paint.Style.FILL);

        applyStyle(context, attrs, defStyleAttr, defStyleRes);

        if (isInEditMode())
            addTemporaryTab();
    }

    @SuppressWarnings("unused")
    public void applyStyle(int resId) {
        applyStyle(getContext(), null, 0, resId);
    }

    private void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollViewTabIndicator, defStyleAttr, defStyleRes);
        int indicatorColor;
        int underLineColor;
        try {
            mTabPadding = a.getDimensionPixelSize(R.styleable.ScrollViewTabIndicator_tpi_tabPadding, 12);
            indicatorColor = a.getColor(R.styleable.ScrollViewTabIndicator_tpi_indicatorColor, Color.WHITE);
            mIndicatorMode = a.getInt(R.styleable.ScrollViewTabIndicator_tpi_indicatorMode, MATCH_PARENT); /* MATCH_PARENT = -1  &&  WRAP_CONTENT = -2 */
            mIndicatorHeight = a.getDimensionPixelSize(R.styleable.ScrollViewTabIndicator_tpi_indicatorHeight, 2);
            underLineColor = a.getColor(R.styleable.ScrollViewTabIndicator_tpi_underLineColor, Color.LTGRAY);
            mUnderLineHeight = a.getDimensionPixelSize(R.styleable.ScrollViewTabIndicator_tpi_underLineHeight, 1);
            mTextAppearance = a.getResourceId(R.styleable.ScrollViewTabIndicator_android_textAppearance, 0);
            tabBackground = a.getResourceId(R.styleable.ScrollViewTabIndicator_tpi_tabBackground, 0);
            mMode = a.getInteger(R.styleable.ScrollViewTabIndicator_tpi_mode, MODE_SCROLL);
        } finally {
            a.recycle();
        }
        removeAllViews();

        mPaint.setColor(indicatorColor);
        mUnderLinePaint.setColor(underLineColor);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Re-post the selector we saved
        if (mTabAnimSelector != null)
            post(mTabAnimSelector);
    }

    public void setActionBarHeight(int height) {
        mActionBarHeight = height;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mScrollOffRunnable);
        if (mTabAnimSelector != null)
            removeCallbacks(mTabAnimSelector);
    }

    private TabView getTabView(int position) {

        return (TabView) getChildAt(position);
    }

    private void animateToTab(final int position) {
        final TabView tv = getTabView(position);
        if (tv == null)
            return;

        if (mTabAnimSelector != null)
            removeCallbacks(mTabAnimSelector);

        mTabAnimSelector = new Runnable() {
            public void run() {
                if (!mScrolling)
                    switch (mIndicatorMode) {
                        case MATCH_PARENT:
                            updateIndicator(tv.getLeft(), tv.getWidth());
                            break;
                        case WRAP_CONTENT:
                            int textWidth = getTextWidth(tv);
                            updateIndicator(tv.getLeft() + tv.getWidth() / 2 - textWidth / 2, textWidth);
                            break;
                    }
                mTabAnimSelector = null;
            }
        };

        post(mTabAnimSelector);
    }


    public void updateIndicator(int offset, int width) {
        mIndicatorOffset = offset;
        mIndicatorWidth = width;
        invalidate();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        // draw underline
        canvas.drawRect(0, getHeight() - mUnderLineHeight, getWidth(), getHeight(), mUnderLinePaint); // must do it first

        int x = mIndicatorOffset + getPaddingLeft();
        canvas.drawRect(x, getHeight() - mIndicatorHeight, x + mIndicatorWidth, getHeight(), mPaint);

        if (isInEditMode())
            canvas.drawRect(getPaddingLeft(), getHeight() - mIndicatorHeight, getPaddingLeft() + getChildAt(0).getWidth(), getHeight(), mPaint);
    }

    public boolean isScrolling() {
        return mScrolling;
    }

    int mPreState = ViewPager.SCROLL_STATE_IDLE;

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mPreState == ViewPager.SCROLL_STATE_SETTLING && state == ViewPager.SCROLL_STATE_IDLE) {
            TextView tv = getTabView(mSelectedPosition);
            if (tv != null)
                switch (mIndicatorMode) {
                    case MATCH_PARENT:
                        updateIndicator(tv.getLeft(), tv.getMeasuredWidth());
                        break;
                    case WRAP_CONTENT:
                        int textWidth = getTextWidth(tv);
                        updateIndicator(tv.getLeft() + tv.getWidth() / 2 - textWidth / 2, textWidth);
                        break;
                }

            /*
             * 因 ScrollView 的滚动可能持续比ViewPager长,
             * 因此此处不设置延时将存在{@link #onScrollChange(NestedScrollView, int, int, int, int)} 中调用的 isScrolling() 不能拦截掉一些多余的处理,
             * 导致indicator回滚的现象, 暂时未考虑到更好的处理方式
             */
            if (mIsClick) {
                removeCallbacks(mScrollOffRunnable);
                postDelayed(mScrollOffRunnable, 200);
            } else {
                mScrolling = false;
            }

        } else {
            mScrolling = true;
        }
        mPreState = state;

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        TabView tv_scroll = getTabView(position);
        TabView tv_next = getTabView(position + 1);

        if (tv_scroll != null && tv_next != null) {
            int width_scroll = mIndicatorMode == MATCH_PARENT ? tv_scroll.getWidth() : getTextWidth(tv_scroll);
            int width_next = mIndicatorMode == MATCH_PARENT ? tv_next.getWidth() : getTextWidth(tv_next);
            float distance = mIndicatorMode == MATCH_PARENT ? (width_scroll + width_next) / 2f : width_scroll / 2 + tv_scroll.getWidth() / 2 + tv_next.getWidth() / 2 - width_next / 2;

            int width = (int) (width_scroll + (width_next - width_scroll) * positionOffset + 0.5f);
            int offset = mIndicatorMode == MATCH_PARENT ?
                    (int) (tv_scroll.getLeft() + width_scroll / 2f + distance * positionOffset - width / 2f + 0.5f)
                    : (int) (tv_scroll.getLeft() + tv_scroll.getWidth() / 2 - width_scroll / 2 + distance * positionOffset + 0.5f);
            updateIndicator(offset, width);
        }
    }

    @Override
    public void onPageSelected(int position) {
        setCurrentItem(position);
    }

    private Runnable mScrollOffRunnable = new Runnable() {
        @Override
        public void run() {
            mScrolling = false;
            mIsClick = false;
        }
    };

    public int getTextWidth(TextView tv) {
        return (int) tv.getPaint().measureText(tv.getText().toString());
    }

    private int mStatusBarHeight;

    @Override
    public void onClick(android.view.View v) {

        int position = (Integer) v.getTag();
        if(mSelectedPosition == position){
            return;
        }
        if (mAssistViewPager != null) {
            mIsClick = true;

            if (mSynchronize != null) {
                mSynchronize.mIsClick = true;
            }
            if (mNextSynchronize != null) {
                mNextSynchronize.mIsClick = true;
            }
            mAssistViewPager.setCurrentItem(position, true);
        }
        if (mScrollView != null) {
            int location;
            location = getViewLocation(position);
            // 待滑动距离 = 当前坐标 - (ActionBar高度) - indicator高度 - 状态栏高度
            if (mStatusBarHeight == 0) {
                mStatusBarHeight = getBarHeight();
            }
            location += -mActionBarHeight - getMeasuredHeight() - mStatusBarHeight;

//            location -= getViewMarginTop(position);
            mScrollView.smoothScrollBy(0, location);
        }
    }

    private int getViewMarginTop(int position) {
        int marginTop = 0;
        if (mViews != null && mViews.size() > position) {
            View view = mViews.get(position);
            if (view != null) {
                try {
                    ViewGroup.MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
                    marginTop = params.topMargin;
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        }
        return marginTop;
    }

    private int getViewLocation(int position) {
        if (mViews != null && mViews.size() > position) {
            View view = mViews.get(position);
            if (view != null) {
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                return location[1];
            }
        }
        return 0;
    }

    /**
     * Set the current page of this TabPageIndicator.
     *
     * @param position The position of current page.
     */
    public void setCurrentItem(int position) {
        if (mSelectedPosition != position) {
            TabView tv = getTabView(mSelectedPosition);
            if (tv != null)
                tv.setChecked(false);
        }

        mSelectedPosition = position;
        TabView tv = getTabView(mSelectedPosition);
        if (tv != null)
            tv.setChecked(true);
        animateToTab(position);
    }

    public int getCurrentIndex() {
        return mSelectedPosition;
    }

    public static int dip2px(int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, Resources.getSystem().getDisplayMetrics());
    }

    /**
     * 获取当前状态栏
     */
    public int getBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 38;//默认为38，貌似大部分是这样的

        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = getResources().getDimensionPixelSize(x);

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }


    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

        if (mScrollListener != null) {
            mScrollListener.onScrollChange(v, scrollX, scrollY, oldScrollX, oldScrollY);
        }

        if (mIsClick) {
            return;
        }

        if (mStatusBarHeight == 0) {
            mStatusBarHeight = getBarHeight();
        }
        int top = mActionBarHeight + getMeasuredHeight() + mStatusBarHeight;//TitleBar 高度 + 控件高度 + StatusBar 高度
        List<Integer> locations = new ArrayList<>();
        for (int i = 0, size = mViews.size(); i < size; i++) {
            locations.add(getViewLocation(i));
        }
        Collections.sort(locations);
        int position = 0;

        if (top < locations.get(0)) {
            position = 0;
        } else {
            for (int j = 0, size = locations.size(); j < size; j++) {
                if (j + 1 == size) {
                    position = j;
                    break;
                }
                if (top >= locations.get(j) && top < locations.get(j + 1)) {
                    //如果已经不能向下滚动了就
                    if (!v.canScrollVertically(VERTICAL)) {
                        position = size - 1;
                        break;
                    }

                    position = j;
                    break;
                }
            }
        }
        if (getCurrentIndex() == position) {
            return;
        }
        mAssistViewPager.setCurrentItem(position, true);
    }

    /**
     * 因为会替换 scrollview 上的 listener, 所以要传进来.
     * 如果传进来是一个 TabIndicator 对象, 则两者状态会同步, 并且自定义的滚动监听要设置到第一个上边
     *
     * @param scrollView 监听的 NestedScrollView
     * @param listener   原先设置在 NestedScrollView 上的监听
     * @param tabs       tab 的标题
     * @param views      各个 tab 对应的需要滚动到的 View
     */
    public void setScrollView(NestedScrollView scrollView, NestedScrollView.OnScrollChangeListener listener, List<String> tabs, List<View> views) {
        if (mScrollView == scrollView) {
            return;
        }

        if (tabs == null || views == null) {
            throw new IllegalArgumentException("tabs and views should not be null!");
        }
        if (tabs.isEmpty() || views.isEmpty()) {
            throw new IllegalArgumentException("tabs and views should not be empty!");
        }
        if (tabs.size() != views.size()) {
            throw new IllegalArgumentException("tabs and views should be the same length!");
        }
        mScrollListener = listener;
        mScrollView = scrollView;
        mViews = views;
        if (mScrollView != null) {
            mScrollView.setOnScrollChangeListener(this);
        }
        initTabs(tabs);
        if (listener instanceof ScrollViewTabIndicator) {
            mSynchronize = (ScrollViewTabIndicator) listener;
            mSynchronize.mNextSynchronize = this;
            mAssistViewPager = mSynchronize.getAssistViewPager();
            //接收覆盖监听，避免走多余的监听流程
            mScrollListener = mSynchronize.mScrollListener;

            if (mAssistViewPager != null) {
                mAssistViewPager.addOnPageChangeListener(this);
            } else {
                initAssistViewPager(tabs.size());
            }
        } else {
            initAssistViewPager(tabs.size());
        }
    }

    //传入
    private ScrollViewTabIndicator mSynchronize;

    //仅仅用于设置mIsClick
    private ScrollViewTabIndicator mNextSynchronize;

    private ViewPager getAssistViewPager() {

        return mAssistViewPager;
    }

    private void initAssistViewPager(int size) {
        if (mAssistViewPager != null) {
            return;
        }
        mAssistViewPager = new ViewPager(getContext());
        ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        mAssistViewPager.setLayoutParams(p);

        //请注意这段代码, 因为会在 ScrollView 的子 view 中插入一个 ViewPager
        View viewGroup = mScrollView.getChildAt(0);
        if (viewGroup == null) {
            throw new IllegalStateException(" The child view of the ScrollView must be not null!");
        }
        if (!(viewGroup instanceof ViewGroup)) {
            throw new IllegalStateException(" The child view of the ScrollView must be a ViewGroup!");
        }
        viewGroup = mScrollView.getChildAt(0);
        ((ViewGroup) viewGroup).addView(mAssistViewPager);


        final List<View> viewList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            viewList.add(new Space(getContext()));
        }
        mAssistViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(viewList.get(position));
                return viewList.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(viewList.get(position));
            }
        });
        mAssistViewPager.addOnPageChangeListener(this);
    }

    private void initTabs(List<String> tabs) {
        removeAllViews();
        final int count = tabs.size();

        if (mSelectedPosition > count)
            mSelectedPosition = count - 1;

        for (int i = 0; i < count; i++) {
            String title = tabs.get(i);
            if (TextUtils.isEmpty(title))
                title = "NULL";

            TabView tv = new TabView(getContext());
            tv.setText(title);
            tv.setTag(i);

            if (mMode == MODE_SCROLL) {
                LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.leftMargin = mTabPadding;
                lp.rightMargin = mTabPadding;
                addView(tv, lp);
            } else if (mMode == MODE_FIXED) {
                LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT);
                params.weight = 1f;
                addView(tv, params);
            }
        }
        setCurrentItem(mSelectedPosition);
        requestLayout();
    }

    private void addTemporaryTab() {
        for (int i = 0; i < 3; i++) {
            CharSequence title = null;
            if (i == 0)
                title = "流行新品";
            else if (i == 1)
                title = "最近上新";
            else if (i == 2)
                title = "人气热销";

            TabView tv = new TabView(getContext());
            tv.setText(title);
            tv.setTag(i);
            tv.setChecked(i == 0);
            if (mMode == MODE_SCROLL) {
                tv.setPadding(mTabPadding, 0, mTabPadding, 0);
                addView(tv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else if (mMode == MODE_FIXED) {
                LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT);
                params.weight = 1f;
                addView(tv, params);
            }
        }
    }


    class TabView extends android.support.v7.widget.AppCompatRadioButton {
        public TabView(Context context) {
            super(context, null, mTextAppearance);
            init();
        }

        private void init() {
            setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
            setGravity(Gravity.CENTER);
            setTextAppearance(getContext(), mTextAppearance);
            if (0 != tabBackground) {
                setBackgroundResource(tabBackground);
            } else {
                setBackground(this, new ColorDrawable(Color.TRANSPARENT));
            }
            setSingleLine(true);
            setEllipsize(TextUtils.TruncateAt.END);
            setOnClickListener(ScrollViewTabIndicator.this);
        }

        public void setBackground(View view, Drawable drawable) {
            if (view == null) {
                return;
            }
            int left = view.getPaddingLeft();
            int right = view.getPaddingRight();
            int top = view.getPaddingTop();
            int bottom = view.getPaddingBottom();
            if (Build.VERSION.SDK_INT >= 16) {
                view.setBackground(drawable);
            } else {
                view.setBackgroundDrawable(drawable);
            }
            view.setPadding(left, top, right, bottom);
        }
    }


}