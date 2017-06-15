package com.pandadentist.ui.activity;

import android.os.Bundle;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;

import butterknife.OnClick;

/**
 * Created by fudaye on 2017/6/15.
 * <p>
 * <p>
 * 设置指示灯
 */

public class GuideActivity extends SwipeRefreshBaseActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolBarTtitle.setText("如何设置指示灯");
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_guide;
    }

    @OnClick(R.id.btn)
    public void onViewClicked() {
        finish();
    }
}
