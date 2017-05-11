package com.dthfish.scrollviewindicator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

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
        mTab = (ScrollViewTabIndicator) findViewById(R.id.tab);
        mTab2 = (ScrollViewTabIndicator) findViewById(R.id.tab2);

        View view1 = findViewById(R.id.tv_1);
        View view2 = findViewById(R.id.tv_2);
        View view3 = findViewById(R.id.tv_3);
        View view4 = findViewById(R.id.tv_4);
        List<String> names = new ArrayList<>();
        names.add("详情");
        names.add("评论");
        names.add("须知");
        names.add("更多");
        List<View> views = new ArrayList<>();
        views.add(view1);
        views.add(view2);
        views.add(view3);
        views.add(view4);
        mTab.setScrollView(mSv,this,names,views);
        //将mTab本身作为参数传入mTab2已达到同步状态
        mTab2.setScrollView(mSv,mTab,names,views);

        findViewById(R.id.tv_banner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SecondActivity.class));
            }
        });
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
