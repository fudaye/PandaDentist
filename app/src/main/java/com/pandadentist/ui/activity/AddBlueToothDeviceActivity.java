package com.pandadentist.ui.activity;

import android.os.Bundle;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;

/**
 * Created by fudaye on 2017/8/17.
 */

public class AddBlueToothDeviceActivity extends SwipeRefreshBaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_add_blue_tooth_device;
    }
}
