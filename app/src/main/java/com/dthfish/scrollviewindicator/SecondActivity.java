package com.dthfish.scrollviewindicator;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Description ${Desc}
 * Author zlz
 * Date 2017/3/31.
 */

public class SecondActivity extends AppCompatActivity implements NestedScrollView.OnScrollChangeListener {

    private NestedScrollView mSv;
    private ScrollViewTabIndicator mTab;
    private ScrollViewTabIndicator mTab2;

    private ScrollViewTabIndicator mTab3;
    private ScrollViewTabIndicator mTab4;
    private ScrollViewTabIndicator mTab5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        mSv = (NestedScrollView) findViewById(R.id.sv);
        mTab = (ScrollViewTabIndicator) findViewById(R.id.tab);
        mTab2 = (ScrollViewTabIndicator) findViewById(R.id.tab2);
        mTab3 = (ScrollViewTabIndicator) findViewById(R.id.tab3);
        mTab4 = (ScrollViewTabIndicator) findViewById(R.id.tab4);
        mTab5 = (ScrollViewTabIndicator) findViewById(R.id.tab5);
        View view1 = findViewById(R.id.tv_1);
        View view2 = findViewById(R.id.tv_2);
        View view3 = findViewById(R.id.tv_3);
        List<String> names = new ArrayList<>();
        names.add("详情");
        names.add("评论");
        names.add("须知");
        List<View> views = new ArrayList<>();
        views.add(view1);
        views.add(view2);
        views.add(view3);
        mTab.setScrollView(mSv,this,names,views);

        mTab3.setScrollView(mSv,mTab,names,views);
        mTab4.setScrollView(mSv,mTab3,names,views);
        mTab5.setScrollView(mSv,mTab4,names,views);
        mTab2.setScrollView(mSv,mTab5,names,views);
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

    }
}
