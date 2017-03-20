package com.dthfish.scrollviewindicator;

import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NestedScrollView mSv;
    private ScrollViewTabIndicator mTab;
    private ScrollViewTabIndicator mTab2;

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
        List<String> names = new ArrayList<>();
        names.add("tttt");
        names.add("cccc");
        names.add("kkkk");
        List<View> views = new ArrayList<>();
        views.add(view1);
        views.add(view2);
        views.add(view3);
        mTab.setScrollView(mSv,null,names,views);
        //将mTab本身作为参数传入mTab2已达到同步状态
        mTab2.setScrollView(mSv,mTab,names,views);

    }

}
