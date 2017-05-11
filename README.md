# ScrollViewIndicator
a tab indicator for NestedScrollView


### 一、思路

现在很多应用都采用 ViewPager 加 Fragment 的结构，在 github 上随便一搜也可以找出各种各样的动画效果的 ViewPagerIndicator。前不久在项目详情页改版的需求中，需要把原来的 ViewPager 切换的结构修改成垂直滚动的结构（如下图）。


![scrollviewindicator1.gif](http://upload-images.jianshu.io/upload_images/5463583-99d80feefb15541f.gif?imageMogr2/auto-orient/strip)


第一个反应就是把原来的 ViewPagerIndicator 替换成 RadioGroup 和 RadioButton 然后设置监听，但是又不想放弃原来的 ViewPagerIndicator 的 tab 的切换动画效果。

然后我选择了第二种方法——在原来的 NestedScrollView 包含的子 ViewGroup 中插入一个宽为 match_parent，高为 1px 的 ViewPager，起到辅助动画的功能，来与 NestedScrollView 联动达到上图的效果。

### 二、效果
讲完了思路，先来看下最终实现的效果，效果图就是上边这张，这里主要是给大家看下代码里如何使用，使用是否方便。
``` java
public class MainActivity extends AppCompatActivity implements NestedScrollView.OnScrollChangeListener{

    private NestedScrollView mSv;
    private ScrollViewTabIndicator mTab;
    private ScrollViewTabIndicator mTab2;
    private int[] mTabMiddleLocation = new int[2];
    private int[] mTabTopLocation = new int[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mSv = (NestedScrollView) findViewById(R.id.sv);
        mTab = (ScrollViewTabIndicator) findViewById(R.id.tab);//在TitleBar下方的indicator
        mTab2 = (ScrollViewTabIndicator) findViewById(R.id.tab2);//在ScrollView中的indicator
        View view1 = findViewById(R.id.tv_1);//详情View
        View view2 = findViewById(R.id.tv_2);//评论View
        View view3 = findViewById(R.id.tv_3);//须知View
        List<String> names = new ArrayList<>();
        names.add("详情");
        names.add("评论");
        names.add("须知");
        List<View> views = new ArrayList<>();
        views.add(view1);
        views.add(view2);
        views.add(view3);
        mTab.setScrollView(mSv,this,names,views);
        //将mTab本身作为参数传入mTab2已达到同步状态
        mTab2.setScrollView(mSv,mTab,names,views);
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        setVisibleAndGone();
    }

    private void setVisibleAndGone() {
        mTab2.getLocationOnScreen(mTabMiddleLocation);
        mTab.getLocationOnScreen(mTabTopLocation);
        if (mTabMiddleLocation[1] <= mTabTopLocation[1]) {
            mTab.setVisibility(View.VISIBLE);
            mTab2.setVisibility(View.INVISIBLE);
        } else {
            mTab.setVisibility(View.INVISIBLE);
            mTab2.setVisibility(View.VISIBLE);
        }
    }
}
```

可以看到使用的方法仅仅是找出 ScrollView 中对应的 View，并给出对应的 tab 标题，然后调用 setScrollView 方法设置到 ScrollViewTabIndicator，其余的事都交给 ScrollViewTabIndicator 来执行，唯一要自己处理的就是监听滚动来控制 mTab 和 mTab2 的显示和隐藏。 
### 三、封装
当然这里我将很多 ViewPager 和 ScrollView 的逻辑都封装起来了，否则你会发现的 Activity 或者 Fragment 中你会发现要增加很多与业务无关的代码，而且也不利于后期的复用。下边我就介绍下基于 ViewPagerIndicator 的一些修改。

##### 3.1 设置逻辑
``` java
    /**
     * 因为会替换 scrollview 上的 listener, 所以要传进来.
     * 如果传进来是一个 TabIndicator 对象, 则两者状态会同步, 并且自定义的滚动监听要设置到第一个上边
     * @param scrollView 监听的 NestedScrollView
     * @param listener 原先设置在 NestedScrollView 上的监听
     * @param tabs tab 的标题
     * @param views 各个 tab 对应的需要滚动到的 View
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
            ScrollViewTabIndicator synchronize = (ScrollViewTabIndicator) listener;
            mAssistViewPager = synchronize.getAssistViewPager();
            //接收覆盖监听，避免走多余的监听流程
            mScrollListener = synchronize.mScrollListener;
            if (mAssistViewPager != null) {
                mAssistViewPager.addOnPageChangeListener(this);
            } else {
                initAssistViewPager(tabs.size());
            }
        } else {
            initAssistViewPager(tabs.size());
        }
    }
```

去除了原来的具备的 setViewPager 方法，添加了 setScrollView，这里主要就是进行各种判空，接收传进来的参数(listener)，并调用 initTabs(tabs) 来生成对应的 tab。之后调用 initAssistViewPager(tabs.size()) 来创建辅助动画的 ViewPager，可以先不管 if() 里面的代码。

##### 3.2 辅助ViewPager

``` java
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
```
创建了一个只有 1px 高度的 ViewPager 但是由于 ScrollViewTabIndicator 本身继承的是一个水平方向的 LinearLayout，而且需要给予 ViewPager 一定宽度以保证 tab 切换有一定动画效果，所以这里只能在 ScrollView 的子 view 中插入 ViewPager。并且这个 ViewPager 仅仅是为了可以在它的 page 切换的时候在它的 OnPageChangeListener 中实现 tab 切换的动画。ps：到这里我觉得针对任何一个 ViewPagerIndicator 都可以采用这个形式来修改成我所谓的 ScrollViewTabIndicator。
##### 3.3 对需要定位的 Views 在 onScrollChange 中的处理
``` java
@Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

        if (mScrollListener != null) {
            mScrollListener.onScrollChange(v, scrollX, scrollY, oldScrollX, oldScrollY);
        }

        if (isScrolling()) {
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
```
首先这里的 mScrollListener 就是我们在「1」中 setScrollView 里面传入的 listener；isScrolling()是 ViewPager 是否在滚动；然后给个需要定位的 View 计算在屏幕上的 y 坐标,并进行从小到大排序。
``` java
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
```
之后进行判断来确定是否需要切换 tab：
1.如果所有的坐标都大于 top (TitleBar 高度 + 控件高度 + StatusBar 高度，因为我们要做到有悬浮在标题栏下方的视觉效果所以这里要加控件高度，大家可自行根据需求修改这里)，则 position 为 0；
2.如果存在坐标小于 top：
3. 存在 top 介于坐标 j 和 坐标 j + 1 之间，且可以继续向下滚，则取 position 为 j；
4. 存在 top 介于坐标 j 和 坐标 j + 1 之间，且不可以向下滚，取 position 为 size - 1；(为了解决最底部 View 过短永远也滚不到的情况)
5. 如果所有坐标都大于 top，则取 position 为 size - 1；

如果取到的 position 和 之前的不同则让 ViewPager 滚到新的一页，并且 tabs 进行相应的切换，当然之后的动画逻辑其实是原来 ViewPagerIndicator 的代码，这里就不进行说明，有兴趣的可以之后看下完整的代码。
##### 3.4 点击 Tab 实现切换和滚动
``` java
	@Override
    public void onClick(android.view.View v) {
        int position = (Integer) v.getTag();
        if (mScrollView != null) {
            int location;
            location = getViewLocation(position);
            // 待滑动距离 = 当前坐标 - (ActionBar高度) - indicator高度 - 状态栏高度
            if (mStatusBarHeight == 0) {
                mStatusBarHeight = getBarHeight();
            }
            location += -mActionBarHeight - getMeasuredHeight() - mStatusBarHeight;
            mScrollView.smoothScrollBy(0, location);
        }

        if (mAssistViewPager != null) {
            mIsClick = true;
            mAssistViewPager.setCurrentItem(position, true);
        }
    }
```
这里的点击事件是在 initTabs(tabs) 的时候设置在每个 TabView 上的，这里 TabView 的仅仅是继承了 AppCompatRadioButton 做了一些颜色和背景的设置。点击事件做了两件事，一件是计算 ScrollView 需要滚动的距离并进行平滑滚动，另外一件就是让 ViewPager 进行平滑的滚动。

上面在「3」中的 onScrollChange 我们刚才已经知道它对 ViewPager 的滚动进行了判断，当 ViewPager 滚动过程中不会进一步进行处理。但是事实上这里还是会有所影响，因为两者的滚动时间不一致！ScrollView 往往会慢一点，所以常常会发生点击过后 tab 回滚的现象，所以用 mIsClick 进行了进一步判断的处理。
``` java

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
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
            if(mIsClick) {
                removeCallbacks(mScrollOffRunnable);
                postDelayed(mScrollOffRunnable, 200);
            }else{
                mScrolling = false;
            }
            mIsClick = false;
        } else {
            removeCallbacks(mScrollOffRunnable);
            mScrolling = true;
        }

    }

    private Runnable mScrollOffRunnable = new Runnable() {
        @Override
        public void run() {
            mScrolling = false;
        }
    };
```
可以看到 mIsClick 仅仅是把 mScrolling 延迟 200ms 设置成 false，为了让 ScrollView 先滚完，目前还没想到其他的方法。到这里这个控件可以独立使用了，但是为了实现下面的效果，而不至于监听太乱所以进一步进行优化。

![scrollviewindicator2.gif](http://upload-images.jianshu.io/upload_images/5463583-8947587b0a48f24f.gif?imageMogr2/auto-orient/strip)


##### 3.5 ScrollViewTabIndicator 之间的同步
其实代码很简单，细心的同学可能已经看见了，就是「1」中让大家跳过的 if() 中的语句。
``` java
	if (listener instanceof ScrollViewTabIndicator) {
	    ScrollViewTabIndicator synchronize = (ScrollViewTabIndicator) listener;
	    mAssistViewPager = synchronize.getAssistViewPager();
	    //接收覆盖监听，避免走多余的监听流程
	    mScrollListener = synchronize.mScrollListener;
	    if (mAssistViewPager != null) {
	        mAssistViewPager.addOnPageChangeListener(this);
	    } else {
	        initAssistViewPager(tabs.size());
	    }
	} else {
	    initAssistViewPager(tabs.size());
	}
```
判断如果传入的 listener 如果是 ScrollVIewTabIndicator 对象则直接共用创建的辅助动画的 ViewPager，并且接收其中的 mScrollListener。最终会走的 onScrollChange 的只有最后一个控件实现的方法，和最初传进来的 listener。下边看看 5 个控件的同步过程。
``` java
        mTab.setScrollView(mSv,this,names,views);
        mTab3.setScrollView(mSv,mTab,names,views);
        mTab4.setScrollView(mSv,mTab3,names,views);
        mTab5.setScrollView(mSv,mTab4,names,views);
        mTab2.setScrollView(mSv,mTab5,names,views);
```
这里只走 this 和 mTab2 的 onScrollChange 方法。
### 四、重申几个注意点

 * 该类会在 ScrollView 的子 View 中插入一个宽度为 match_parent, 高度为 1px 的 ViewPager (用来辅助动画), 因此确保 ScrollView 中包含的是 ViewGroup；
 * 使用时调用 {{@link #setScrollView(NestedScrollView, NestedScrollView.OnScrollChangeListener, List, List)}}与 NestedScrollView  关联,并且原来要设置再 NestedScrollView 上的监听要在此传入, 否则讲被替换；
 * TabIndicator 本身也可以作为一个{ NestedScrollView.OnScrollChangeListener} 传入, 如果这样, 两个控件将会同步, 共享已经创建的 ViewPager；
 * 默认用48dp的像素值作为 ActionBar 的高度, 计算滚动距离, 如果有需求用{{@link #setActionBarHeight(int)}} 来设置；


### 五、 5月11日更新
#####1. 修复快速滑动 tab 没有切换的问题
不再使用 isScrolling() 方法和 mScrolling 拦截 ScrollView 中的监听，改用 mIsClick 判断；修改原先 mScrollOffRunnable 中的 run 方法。
~~~java
	@Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            mScrolling = false;
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
             * 因 ScrollView 的滚动可能持续比 ViewPager 长,
             * 因此此处不设置延时将存在{@link #onScrollChange(NestedScrollView, int, int, int, int)} 中调用的 mIsClick 不能拦截掉一些多余的处理,
             * 导致indicator回滚的现象, 暂时未考虑到更好的处理方式
             */
            if (mIsClick) {
                removeCallbacks(mScrollOffRunnable);
                postDelayed(mScrollOffRunnable, 220);
            }
        } else {
            mScrolling = true;
        }
    }

    private Runnable mScrollOffRunnable = new Runnable() {
        @Override
        public void run() {
            mIsClick = false;
        }
    };
~~~

修改「3.5」中的同步代码。
~~~java
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
~~~
可以看到这里不仅保持传进来的 ScrollViewTabIndicator 对象为 mSynchronize，而且如果本身如果被设置给其他的 ScrollViewTabIndicator 他的 mNextSynchronize 也会被赋值。保持这两个引用主要是为了保证多个 ScrollViewTabIndicator 同步时，在点击不同对象的 tab 的时候，他们的 mIsClick 能保持一致，下边看一下 onClick(View v) 方法的改变。
~~~java
    @Override
    public void onClick(android.view.View v) {

        int position = (Integer) v.getTag();

        if (mAssistViewPager != null) {
            synchronizeClickStatus();
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
            //因为这里经常会出现 scrollView 没有滚动的现象这里才加了 delay
            final int finalLocation = position == 0 ? (location > 0 ? location - 1 : location + 1) : location;
            mScrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScrollView.smoothScrollBy(0, finalLocation);
                }
            }, 100);
        }

    }

    private void synchronizeClickStatus() {
        mIsClick = true;
        if (mSynchronize != null && !mSynchronize.mIsClick) {
            mSynchronize.synchronizeClickStatus();
        }
        if (mNextSynchronize != null && !mNextSynchronize.mIsClick) {
            mNextSynchronize.synchronizeClickStatus();
        }
    }

~~~
修改了两方面，一是点击的时候同时修改了前一个和后一个同步的 ScrollViewTabIndicator 的 mIsClick，二是因为直接调用 ScrollView 的 smoothScrollBy 方法，如果点击速度过快 smoothScrollBy 方法中对点击的时间间隔做了判断，导致 ScrollView 常常滚动不到预期的位置，所以做了 100 毫秒的延迟处理。

到这里对于快速滚动的处理算是完成了，可能还有其他的问题，如果大家有发现问题或者意见还望提醒我改正，谢谢。


### 六、 最后
感谢 [J!nL!n](https://daijinlin.com/) 同学的 TabIndicator 以及 CF 同学的思路。

[这里有源码](https://github.com/DthFish/ScrollViewIndicator)
