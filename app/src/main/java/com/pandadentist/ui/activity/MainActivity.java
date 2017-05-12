package com.pandadentist.ui.activity;

import android.graphics.Color;

import android.os.Bundle;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.ui.fragment.HomePageFragment;
import com.pandadentist.ui.fragment.MeFragment;
import com.pandadentist.ui.fragment.TodayFragment;
import com.pandadentist.ui.fragment.OverallFragment;

public class MainActivity extends SwipeRefreshBaseActivity {

    private TodayFragment messageFragment;
    private OverallFragment workFragment;
    private HomePageFragment contactFragment;
    private MeFragment meFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messageFragment = new TodayFragment();
        addFragment(R.id.fl_container, messageFragment);
        initBottomNavigation();
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_main;
    }


    private void initBottomNavigation() {
        AHBottomNavigation ahBottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        AHBottomNavigationItem item1 = new AHBottomNavigationItem("今日分析", R.drawable.ic_home_messge, android.R.color.white);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("综合分析", R.drawable.ic_home_work, android.R.color.white);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem("首页", R.drawable.ic_home_contact, android.R.color.white);
        // Add items
        if (ahBottomNavigation == null) {
            return;
        }
        ahBottomNavigation.addItem(item1);
        ahBottomNavigation.addItem(item2);
        ahBottomNavigation.addItem(item3);

        ahBottomNavigation.setAccentColor(getResources().getColor(R.color.themeColor));
        ahBottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));
        ahBottomNavigation.setForceTint(true);
        ahBottomNavigation.setForceTitlesDisplay(true);
        ahBottomNavigation.setCurrentItem(0);
//        ahBottomNavigation.setAccentColor(getResources().getColor(R.color.theme_color));
//        ahBottomNavigation.setInactiveColor(getResources().getColor(R.color.blue));
//        ahBottomNavigation.setColored(false);
//        setBottomNoti();q

        ahBottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            hideFragments();
            switch (position) {
                case 0://消息
                    if (messageFragment == null) {
                        // 如果shouYeFragment为空，则创建一个并添加到界面上
                        messageFragment = new TodayFragment();
                        addFragment(R.id.fl_container, messageFragment);
                    } else {
                        // 如果shouYeFragment不为空，则直接将它显示出来
                        showFragment(messageFragment);
                    }
                    break;
                case 1://改造
                    if (workFragment == null) {
                        // 如果DingZhiFragment为空，则创建一个并添加到界面上
                        workFragment = new OverallFragment();
                        addFragment(R.id.fl_container, workFragment);
                    } else {
                        // 如果DingZhiFragment不为空，则直接将它显示出来
                        showFragment(workFragment);
                    }
                    break;
                case 2:
                    if (contactFragment == null) {
                        // 如果findFragment为空，则创建一个并添加到界面上
                        contactFragment = new HomePageFragment();
                        addFragment(R.id.fl_container, contactFragment);
                    } else {
                        // 如果findFragment不为空，则直接将它显示出来
                        showFragment(contactFragment);
                    }
                    break;
                case 3:
                    if (meFragment == null) {
                        // 如果MeFragment为空，则创建一个并添加到界面上
                        meFragment = new MeFragment();
                        addFragment(R.id.fl_container, meFragment);
                    } else {
                        // 如果MeFragment不为空，则直接将它显示出来
                        showFragment(meFragment);
                    }
                    break;
            }
        });
    }

    /**
     * 将所有的Fragment都置为隐藏状态。
     */
    private void hideFragments() {
        if (messageFragment != null) {
            hideFragment(messageFragment);
        }
        if (workFragment != null) {
            hideFragment(workFragment);
        }
        if (contactFragment != null) {
            hideFragment(contactFragment);
        }
        if (meFragment != null) {
            hideFragment(meFragment);
        }
    }
}
