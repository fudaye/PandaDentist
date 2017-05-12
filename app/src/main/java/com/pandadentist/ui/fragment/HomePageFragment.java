package com.pandadentist.ui.fragment;


import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseFragment;


/**
 * 通讯录模块
 * Created by packy on 2016/9/12.
 */
public class HomePageFragment extends SwipeRefreshBaseFragment {


    @Override
    public int getLayoutId() {
        return R.layout.common_rv_layout;
    }

    @Override
    public void createView(ViewGroup container, View parentView, Bundle savedInstanceState) {
        assert mToolBarTtitle != null;
        assert mToolBackRl != null;
        mToolBarTtitle.setText("首页");
        mToolBackRl.setVisibility(View.GONE);
    }


}
