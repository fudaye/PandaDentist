package com.pandadentist.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;


import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseFragment;


public class OverallFragment extends SwipeRefreshBaseFragment {


    @Override
    public void createView(ViewGroup container, View parentView, Bundle savedInstanceState) {
        assert mToolBarTtitle != null;
        assert mToolBackRl != null;
        mToolBarTtitle.setText("综合分析");
        mToolBackRl.setVisibility(View.GONE);
    }

    @Override
    public int getLayoutId() {
        return R.layout.common_rv_layout;
    }
}
