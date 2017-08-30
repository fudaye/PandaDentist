package com.pandadentist.ui.activity;

import android.os.Bundle;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;

/**
 * Created by fudaye on 2017/8/29.
 *
 */

public class BlueHelperActivity  extends SwipeRefreshBaseActivity{


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolBarTtitle.setText("帮助");
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_blue_helper;
    }
}
