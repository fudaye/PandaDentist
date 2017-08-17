package com.pandadentist.ui.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.util.IntentHelper;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * Created by fudaye on 2017/6/15.
 */

public class AddDeviceActivity extends SwipeRefreshBaseActivity {

    @Bind(R.id.tv_indicator)
    TextView mTv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolBarTtitle.setText(getResources().getString(R.string.addDevice));
        mTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mTv.getPaint().setAntiAlias(true);
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_add_device;
    }

    @OnClick({R.id.tv_indicator, R.id.btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_indicator:
                Intent intent = new Intent(AddDeviceActivity.this,GuideActivity.class);
                startActivity(intent);
                break;
            case R.id.btn:
                IntentHelper.gotoCapture(AddDeviceActivity.this);
                break;
        }
    }
}
